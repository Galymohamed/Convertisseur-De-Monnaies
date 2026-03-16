/**
 * Convertisseur De Monnaies — Projet Android ECE
 * ------------------------------------------------
 * Auteur  : Mohamed GALY
 * Cours   : Développement Mobile Android — ECE Paris
 * Licence : MIT
 *
 * Activité secondaire affichant la liste complète des taux de change
 * reçus depuis MainActivity via un Intent.
 */
package com.example.eceandroidproject;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashMap;

public class ListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview);

        // Bouton de retour vers l'activité principale
        final TextView exitTextView = (TextView) findViewById(R.id.button3);
        exitTextView.setOnClickListener(exitView);

        // Récupération de la liste des taux transmise par MainActivity
        Intent intent = getIntent();
        HashMap<String, Double> taux =
                (HashMap<String, Double>) intent.getSerializableExtra("CurrencyList");

        // Construction du tableau d'affichage : "CODE = valeur"
        ListView viewmylist = findViewById(R.id.liste);
        String[] data = new String[taux.size()];
        int j = 0;
        for (String devise : taux.keySet()) {
            data[j] = devise + " = " + taux.get(devise);
            j++;
        }

        // Adaptateur liant le tableau à la ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, data);
        viewmylist.setAdapter(adapter);
    }

    // Ferme cette activité et retourne à MainActivity
    private View.OnClickListener exitView = new View.OnClickListener() {
        public void onClick(View v) {
            finish();
        }
    };
}
