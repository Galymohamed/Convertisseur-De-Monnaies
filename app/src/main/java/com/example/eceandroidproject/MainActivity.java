/**
 * Convertisseur De Monnaies — Projet Android ECE
 * ------------------------------------------------
 * Auteur  : Mohamed GALY
 * Cours   : Développement Mobile Android — ECE Paris
 * Licence : MIT
 *
 * Activité principale de l'application.
 * Gère la conversion de devises et la recherche de pays par adresse.
 */
package com.example.eceandroidproject;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Champ de saisie du montant à convertir
    EditText MoneyInput = null;
    // Zone d'affichage du résultat de la conversion
    TextView Vresult = null;
    // Premier sélecteur : devise source
    public Spinner S1 = null;
    // Second sélecteur : devise cible
    public Spinner S2 = null;
    // Bouton de lancement de la conversion
    Button ConvertBt = null;
    // Résultat numérique de la conversion
    double result = 0;

    private static final String TAG = "MyApplication";
    // Tâche asynchrone de téléchargement des taux de change
    private DownloadFileTask downloadFileTask;

    // Zone de saisie d'une adresse pour la géolocalisation
    EditText UAddress;
    // Affiche le nom du pays correspondant à l'adresse saisie
    TextView countryN;
    // Bouton de recherche géographique
    Button btnGetResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        // Message d'attente pendant le chargement des taux
        Toast.makeText(getApplicationContext(), "Merci de patienter..", Toast.LENGTH_LONG).show();

        // Lancement du téléchargement des taux de change à l'ouverture
        downloadFileTask = new DownloadFileTask(this);
        downloadFileTask.execute();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Démarrage de l'application");

        // Liaison des vues avec leurs identifiants dans le layout
        UAddress   = findViewById(R.id.UeserAdresse);
        countryN   = findViewById(R.id.CountryN);
        btnGetResult = findViewById(R.id.btnGetResult);
        ConvertBt  = findViewById(R.id.button);
        MoneyInput = findViewById(R.id.editText);
        Vresult    = findViewById(R.id.textView);
        S1         = (Spinner) findViewById(R.id.spinner1); // Devise source
        S2         = (Spinner) findViewById(R.id.spinner2); // Devise cible

        // Bouton vers la liste de toutes les devises disponibles
        final TextView ListTextView = (TextView) findViewById(R.id.button2);
        ListTextView.setOnClickListener(TurnInListView);

        // Écouteur du bouton de conversion
        ConvertBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Vérification que le champ contient uniquement des chiffres
                if (android.text.TextUtils.isDigitsOnly(MoneyInput.getText())) {
                    if (android.text.TextUtils.isEmpty(MoneyInput.getText())) {
                        Log.e(TAG, "Aucune valeur saisie");
                        Toast.makeText(getApplicationContext(),
                                "Veuillez vérifier le montant saisi. Merci",
                                Toast.LENGTH_LONG).show();
                    } else {
                        // Conversion : montant ÷ taux source × taux cible (base EUR = 1)
                        result = Double.parseDouble(MoneyInput.getText().toString())
                                / downloadFileTask.getCBdataXML().get(S1.getSelectedItem());
                        result *= downloadFileTask.getCBdataXML().get(S2.getSelectedItem());
                        Vresult.setText(String.valueOf(result));
                    }
                }
            }
        });

        // Écouteur du bouton de recherche géographique
        btnGetResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationService();
            }
        });
    }

    /**
     * Recherche le pays correspondant à l'adresse saisie par l'utilisateur
     * via le service de géocodage Android.
     */
    private void LocationService() {
        Geocoder mGeocoder = new Geocoder(this);

        if (UAddress.getText().toString().isEmpty()) {
            // Adresse non renseignée : affichage d'une alerte
            Log.e(TAG, "Adresse vide");
            afficherAlerte("Veuillez entrer une adresse. Merci");
            return;
        }

        String adresseSaisie = UAddress.getText().toString();
        List<Address> addresses = null;

        try {
            addresses = mGeocoder.getFromLocationName(adresseSaisie, 1);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        if (addresses == null || addresses.isEmpty()) {
            // Aucun résultat : adresse invalide ou pas de connexion
            Log.e(TAG, "Adresse introuvable");
            afficherAlerte("Veuillez vérifier l'adresse ou votre connexion internet. Merci");
        } else {
            // Affichage du nom du pays trouvé
            String nomPays = addresses.get(0).getCountryName();
            countryN.setText(nomPays);
        }
    }

    /**
     * Affiche une boîte de dialogue d'information avec un bouton OK.
     *
     * @param message Le texte à afficher dans la boîte de dialogue.
     */
    private void afficherAlerte(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    // Écouteur du bouton de navigation vers la liste des devises
    private View.OnClickListener TurnInListView = new View.OnClickListener() {
        public void onClick(View v) {
            Log.i(TAG, "Ouverture de la liste des devises");
            launchActivity();
        }
    };

    /**
     * Lance l'activité d'affichage de la liste des taux de change.
     */
    private void launchActivity() {
        Intent intent = new Intent(this, ListActivity.class);
        // Transmission de la liste des taux récupérés à l'activité de liste
        intent.putExtra("CurrencyList", downloadFileTask.getCBdataXML());
        startActivity(intent);
    }
}
