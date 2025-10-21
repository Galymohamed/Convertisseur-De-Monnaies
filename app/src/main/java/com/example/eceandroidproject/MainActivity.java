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


    EditText MoneyInput = null; // Input Money
    TextView Vresult = null; // Result veiw
    public Spinner S1 = null; // first Spinner
    public Spinner S2 = null; // Secand Spinner
    Button ConvertBt = null; // Convert button
    double result = 0; // result of the exchange
    private DB_Sqlite db_sqlite;
    private static final String TAG = "MyApplication";
    private DownloadFileTask downloadFileTask;
    private FireBaseManager FireBRateData;
    TextView countryN;
    EditText UAddress;
    Button btnGetResult;
    ////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        Toast.makeText(getApplicationContext(), "Merci de patienter..", Toast.LENGTH_LONG).show();
        db_sqlite = new DB_Sqlite(getApplicationContext());
        downloadFileTask = new DownloadFileTask(this);
        downloadFileTask.execute();
    }
    @Override
    protected void onStart() {
        super.onStart();
        UAddress = findViewById(R.id.UeserAdresse);
        countryN = findViewById(R.id.CountryN);
        btnGetResult = findViewById(R.id.btnGetResult);

        ////////////////////////////////////////////
        Log.d(TAG, "démarre l'application");
        ConvertBt = findViewById(R.id.button);      // Using FindViewbyId method in order to link ConvertBt with the button in the layout
        MoneyInput = findViewById(R.id.editText);   // Using FindViewbyId method in order to link MoneyInput with the editText id in th layout
        Vresult = findViewById(R.id.textView);      // Using FindViewbyId method in order to link Vresult with the textView id in the layout
        S1 = (Spinner) findViewById(R.id.spinner1); // Using FindViewbyId method in order to link S1 with the first spinner in the layout
        S2 = (Spinner) findViewById(R.id.spinner2); // Using FindViewbyId method in order to link S2 with the secand spinner in the layout
        final TextView ListTextView = (TextView) findViewById(R.id.button2); // Currency rates list button
        ListTextView.setOnClickListener(TurnInListView); // Check the list view button
        // listen ( Check the convert Button) ..
        FireBRateData = new FireBaseManager();
        ConvertBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check the Input value
                if ((android.text.TextUtils.isDigitsOnly(MoneyInput.getText()))) {
                    if (android.text.TextUtils.isEmpty(MoneyInput.getText())) {
                        Log.e(TAG, "Pas d'entrée");
                        Toast.makeText(getApplicationContext(), "Veuillez vérifier le montant saisi" +
                                " " + "Merci", Toast.LENGTH_LONG).show();
                    } else { // if the input is correct then we apply the following calcul ...
                        result = Double.parseDouble(MoneyInput.getText().toString()) /
                                downloadFileTask.getCBdataXML().get(S1.getSelectedItem());
                        result *= downloadFileTask.getCBdataXML().get(S2.getSelectedItem());

                        Vresult.setText(String.valueOf(result));
                    }
                }
            }
        });
        btnGetResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationService();
            }
        });
    }
    String address;
    private void LocationService() {
        Geocoder mGeocoder = new Geocoder(this);
        if (!UAddress.getText().toString().isEmpty()) {
            address = UAddress.getText().toString();
            String addressFromIntent = address;
            String addressString = null;
            List<Address> addresses = null;
            try {
                addresses = mGeocoder.getFromLocationName(
                        addressFromIntent,
                        1
                );

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            // Handle case where no address found
            if (addresses == null || addresses.size() == 0) {
                Log.e(TAG, "error");
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage("veuillez verifier l'adresse ou votre connexion internet Merci");
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
            } else {
                Address address = addresses.get(0);
                addressString = address.getCountryName();
                countryN.setText(addressString);
            }
        } else{
            Log.e(TAG, "error");
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setMessage("veuillez entrer une adresse Merci");
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
        }
    }
    private View.OnClickListener TurnInListView = new View.OnClickListener() {
        public void onClick(View v) {
            Log.i(TAG, "Liste vue");
            launchActivity(); // call lanchactivity method
        }
    };

    private void launchActivity() {
        // open the listviewActivity class in order to display the current list...
        Intent intent = new Intent(this, ListActivity.class);
        intent.putExtra("CurrencyList", downloadFileTask.getCBdataXML());  // get the list data form dowloadfiletask Class
        startActivity(intent);
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        downloadFileTask = new DownloadFileTask(this);
        downloadFileTask.execute();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
}