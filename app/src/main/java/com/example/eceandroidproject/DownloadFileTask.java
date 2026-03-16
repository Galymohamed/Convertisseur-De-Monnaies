/**
 * Convertisseur De Monnaies — Projet Android ECE
 * ------------------------------------------------
 * Auteur  : Mohamed GALY
 * Cours   : Développement Mobile Android — ECE Paris
 * Licence : MIT
 *
 * Tâche asynchrone chargée de récupérer les taux de change
 * depuis le flux XML de la Banque Centrale Européenne (BCE).
 * En cas d'absence de connexion, les données sont lues depuis
 * la base de données SQLite locale.
 */
package com.example.eceandroidproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static com.example.eceandroidproject.DB_Sqlite.RateManager.RateEntry.COLUMN_NAME;
import static com.example.eceandroidproject.DB_Sqlite.RateManager.RateEntry.COLUMN_RATE;
import static com.example.eceandroidproject.DB_Sqlite.RateManager.RateEntry.TABLE_NAME;

public class DownloadFileTask extends AsyncTask {

    private static final String TAG = "MyApplication";

    // Contient les paires (code devise → taux par rapport à l'EUR)
    private HashMap<String, Double> CBdataXML = null;
    // Indique si le téléchargement s'est effectué avec succès
    private boolean downloadSuccess = true;

    // Gestionnaire Firebase pour la synchronisation des taux
    private FireBaseManager fireBRateData = new FireBaseManager();
    // Référence à l'activité appelante (pour accéder aux vues)
    public Activity activity = null;

    // Indicateur de mode en ligne affiché dans l'interface
    private TextView statusTextView;
    // Sélecteur de devise source
    private Spinner s1;
    // Sélecteur de devise cible
    private Spinner s2;

    // Gestionnaire de la base de données SQLite
    private DB_Sqlite db_sqlite;
    private SQLiteDatabase db;

    public DownloadFileTask(Activity mainActivity) {
        this.activity = mainActivity;
        // Récupération des vues depuis le layout de l'activité
        s1 = (Spinner) activity.findViewById(R.id.spinner1);
        s2 = (Spinner) activity.findViewById(R.id.spinner2);
        statusTextView = activity.findViewById(R.id.textView1);
    }

    /**
     * Exécuté en arrière-plan : télécharge et parse le flux XML de la BCE.
     * L'EUR est ajouté manuellement (taux de base = 1,0).
     */
    @Override
    protected Object doInBackground(Object[] objects) {
        CBdataXML = new HashMap<>();
        // L'EUR sert de devise de référence avec un taux de 1,0
        CBdataXML.put("EUR", 1.0);

        try {
            URL url = new URL("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml");
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(5000);
            connection.connect();

            // Construction du parseur XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            Document document = docBuilder.parse(url.openStream());

            // Parcours des nœuds <Cube> contenant les taux de change
            NodeList nodeListDoc = document.getElementsByTagName("Cube");
            for (int i = 0; i < nodeListDoc.getLength(); i++) {
                Element ele = (Element) nodeListDoc.item(i);
                String currency = ele.getAttribute("currency");
                String rate = ele.getAttribute("rate");
                if (!currency.isEmpty()) {
                    CBdataXML.put(currency, Double.parseDouble(rate));
                    Log.w(TAG, "1 EUR = " + rate + " " + currency);
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            Log.e(TAG, "Erreur lors du téléchargement : " + e.getMessage());
            downloadSuccess = false;
        }
        return null;
    }

    /**
     * Exécuté sur le thread principal après doInBackground.
     * Met à jour l'interface et persiste les données selon le résultat du téléchargement.
     */
    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        db_sqlite = new DB_Sqlite(activity.getApplicationContext());

        if (downloadSuccess) {
            // ---- Mode en ligne ----
            statusTextView.setText("Mode En Ligne");
            statusTextView.setTextColor(activity.getResources().getColor(R.color.colorAccent));
            Log.d(TAG, "Données récupérées : " + CBdataXML.toString());

            // Alimentation des deux sélecteurs de devises
            List<String> keys = new ArrayList<>(CBdataXML.keySet());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this.activity, android.R.layout.simple_spinner_item, keys);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            s1.setAdapter(adapter);
            s2.setAdapter(adapter);

            // Écriture des taux dans la base de données SQLite locale
            db = db_sqlite.getWritableDatabase();
            for (Map.Entry<String, Double> entry : CBdataXML.entrySet()) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME, entry.getKey());
                values.put(COLUMN_RATE, entry.getValue());
                long newRowId = db.insert(TABLE_NAME, null, values);
                Log.d("DataBase", "Ligne insérée : " + newRowId);
            }

            // Synchronisation des taux sur Firebase Realtime Database
            fireBRateData.getInstance();
            int id = 0;
            for (Map.Entry<String, Double> entry : CBdataXML.entrySet()) {
                fireBRateData.writeNewRate(Integer.toString(id), entry.getKey(), entry.getValue());
                id++;
            }

        } else {
            // ---- Mode hors ligne ----
            statusTextView.setText("Mode Hors Ligne");
            statusTextView.setTextColor(
                    activity.getResources().getColor(R.color.design_default_color_error));
            Log.e("Réseau", "Aucune connexion disponible");

            // Affichage d'une alerte pour informer l'utilisateur
            new AlertDialog.Builder(this.activity)
                    .setMessage("Connexion impossible. Merci de vérifier votre connexion internet.")
                    .setCancelable(true)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .create()
                    .show();

            // Lecture des taux depuis la base de données SQLite locale
            db = db_sqlite.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
            Log.d("cursor", cursor.toString());

            if (cursor.moveToFirst()) {
                do {
                    CBdataXML.put(
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_RATE)));
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();

            Log.d(TAG, "Données locales chargées : " + CBdataXML.toString());

            // Alimentation des sélecteurs avec les données locales
            List<String> keys = new ArrayList<>(CBdataXML.keySet());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this.activity, android.R.layout.simple_spinner_item, keys);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            s1.setAdapter(adapter);
            s2.setAdapter(adapter);
        }
    }

    /**
     * Retourne la table des taux de change téléchargés ou chargés localement.
     *
     * @return HashMap associant chaque code de devise à son taux par rapport à l'EUR.
     */
    public HashMap<String, Double> getCBdataXML() {
        return this.CBdataXML;
    }
}
