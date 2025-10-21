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

        final TextView exitTextView = (TextView) findViewById(R.id.button3);
        exitTextView.setOnClickListener(exitView);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        HashMap<String, Double> message;
        message = (HashMap<String, Double>)intent.getSerializableExtra("CurrencyList");
        //Log.i("MyApp", message.toString());

        // Capture the layout's TextView and set the string as its text
        ListView viewmylist = findViewById(R.id.liste);
        int j = 0;
        String[] data = new String[message.size()];

        for (String i : message.keySet()) {
            data[j] = i + " = " + message.get(i);
            j++;
        }

        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data);
        viewmylist.setAdapter(adapter);

    }
    private View.OnClickListener exitView = new View.OnClickListener() {
        public void onClick(View v) {
            finish();
        }
    };

}
