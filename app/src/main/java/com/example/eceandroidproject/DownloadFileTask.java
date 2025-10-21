package com.example.eceandroidproject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static com.example.eceandroidproject.DB_Sqlite.RateManager.RateEntry.COLUMN_NAME;
import static com.example.eceandroidproject.DB_Sqlite.RateManager.RateEntry.COLUMN_RATE;
import static com.example.eceandroidproject.DB_Sqlite.RateManager.RateEntry.TABLE_NAME;


public class DownloadFileTask extends AsyncTask {

    Boolean NetworkCHK; //Boolean for the internet connection ..
    private HashMap<String, Double> CBdataXML = null; // define a HashMap for the Central bank XML data information
    private int versionDB = 1; //version DB
    private static final String TAG = "MyApplication";
    FireBaseManager FireBRateData = new FireBaseManager();
    public Activity activity = null;
    public DownloadFileTask(Activity mainActivity) {
        this.activity = mainActivity;
        s1 = (Spinner) activity.findViewById(R.id.spinner1);
        s2 = (Spinner) activity.findViewById(R.id.spinner2);
        ST = activity.findViewById(R.id.textView1);
    }
    @SuppressLint("WrongThread")
    TextView ST;
    Spinner s1;
    Spinner s2;
    Boolean DownloadTest = true; //Boolean in order to check the dowload done correctly
    // data base
    private DB_Sqlite db_sqlite;
    SQLiteDatabase db;

    protected Object doInBackground(Object[] objects) {

        CBdataXML = new HashMap<>();
        CBdataXML.put("EUR", 1.0); // put data in the Hashmap if the data base was empty and the dowload not correct ..
        try {
            URL url = new URL("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml");
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(1000);
            connection.connect();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder  docBuildder = factory.newDocumentBuilder();
            Document document = docBuildder.parse((url.openStream()));
            NodeList NodelistDoc = document.getElementsByTagName("Cube");
            for (int i = 0; i < NodelistDoc.getLength(); i++) {
                Element ele = (Element) NodelistDoc.item(i);
                if (!ele.getAttribute("currency").equals("")) {
                    CBdataXML.put(ele.getAttribute("currency"), Double.parseDouble(ele.getAttribute("rate")));
                    Log.w(TAG, "1 EUR = " + ele.getAttribute("rate") + " " + ele.getAttribute("currency")); // Show the rates compared with the euro in the Log
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            Log.e("except1", e.getMessage());
            DownloadTest = false; //Send false if any problem in the download file
        }
        return null;
    }
    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        db_sqlite = new DB_Sqlite(activity.getApplicationContext());
        if (DownloadTest) {
            ST.setText("Mode En Ligne");
            ST.setTextColor(activity.getResources().getColor(R.color.colorAccent));
            // IF everything are alright then we add the information into the data base
            // put the info into the Spinners
            Log.d("hashmap", CBdataXML.toString());
            List<String> keys = new ArrayList<>(CBdataXML.keySet());
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.activity, android.R.layout.simple_spinner_item, keys);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            s1.setAdapter(adapter);
            s2.setAdapter(adapter);
            // Write in the data base SQL
            db = db_sqlite.getWritableDatabase();
            Iterator dataIterator = CBdataXML.entrySet().iterator();
            while (dataIterator.hasNext()) {
                Map.Entry mapElement = (Map.Entry) dataIterator.next();
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME, (String) mapElement.getKey());
                values.put(COLUMN_RATE, (Double) mapElement.getValue());
                db.insert(TABLE_NAME, null, values);
                long newRowId = db.insert(TABLE_NAME, null, values);
                Log.d("DataBase", Long.toString(newRowId));
                // FireBase
                FireBRateData.getInstance();
                int id = 0;
                Iterator dataIterator2 = CBdataXML.entrySet().iterator();
                while (dataIterator2.hasNext()) {
                    Map.Entry mapElement2 = (Map.Entry) dataIterator2.next();
                    FireBRateData.writeNewRate(Integer.toString(id), (String) mapElement2.getKey(), (Double) mapElement2.getValue());
                    id++;
                }
            }
            } else {
                ST.setText("Mode Hors Ligne");
                ST.setTextColor(activity.getResources().getColor(R.color.design_default_color_error));
                // if there no Internet connection then show alert to the user
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this.activity);
                builder1.setMessage("Connexion impossible, Merci de vérifier votre connexion internet");
                builder1.setCancelable(true);
                builder1.setPositiveButton(
                        "Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert11 = builder1.create();
                alert11.show();
                Log.e("Network", "no network reachable");

                db = db_sqlite.getReadableDatabase(); // Read our data base ..
                Cursor cursor = db.rawQuery("SELECT * FROM " +TABLE_NAME, null);
                Log.d("cursor", cursor.toString());
                if(cursor.moveToFirst()) {
                    do {
                        CBdataXML.put(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                                cursor.getDouble(cursor.getColumnIndex(COLUMN_RATE)));

                    } while (cursor.moveToNext());
                }
                db.close();
                // Put the data into the spinners
                Log.d("hashmap", CBdataXML.toString());
                List<String> keys = new ArrayList<>(CBdataXML.keySet());
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.activity, android.R.layout.simple_spinner_item, keys);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                s1.setAdapter(adapter);
                s2.setAdapter(adapter);
            }
        }

        public HashMap<String, Double> getCBdataXML() {
            return this.CBdataXML; // CBdataXML Getter
        }
    }



