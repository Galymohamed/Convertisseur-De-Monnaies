/**
 * Convertisseur De Monnaies — Projet Android ECE
 * ------------------------------------------------
 * Auteur  : Mohamed GALY
 * Cours   : Développement Mobile Android — ECE Paris
 * Licence : MIT
 *
 * Gestionnaire Firebase Realtime Database.
 * Assure la synchronisation des taux de change vers le cloud.
 */
package com.example.eceandroidproject;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;

public class FireBaseManager {

    private static final String TAG = "FireBaseManager";

    // Référence à la racine de la base de données Firebase
    private DatabaseReference mDatabase;

    /**
     * Modèle de données représentant un taux de change.
     * Annoté @IgnoreExtraProperties pour ignorer les champs inconnus lors
     * de la désérialisation depuis Firebase.
     */
    @IgnoreExtraProperties
    public class Rate {

        public String devise;
        public Double rate;

        /** Constructeur vide requis par Firebase pour la désérialisation. */
        public Rate() {}

        /**
         * Crée un taux de change avec le code de devise et la valeur.
         *
         * @param devise    Code ISO de la devise (ex. "USD", "GBP").
         * @param rate      Taux par rapport à l'EUR.
         */
        public Rate(String devise, double rate) {
            this.devise = devise;
            this.rate = rate;
        }
    }

    /**
     * Initialise la référence à la racine de la base de données Firebase.
     * Doit être appelé avant toute opération d'écriture ou de lecture.
     */
    public void getInstance() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Écrit ou met à jour un taux de change dans Firebase.
     *
     * @param rateId     Identifiant unique de l'entrée (index sous forme de chaîne).
     * @param devise     Code ISO de la devise.
     * @param rateValue  Valeur du taux par rapport à l'EUR.
     */
    public void writeNewRate(String rateId, String devise, double rateValue) {
        Rate rate = new Rate(devise, rateValue);
        mDatabase.child("rates").child(rateId).setValue(rate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Taux écrit avec succès");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Échec de l'écriture du taux : " + e.getMessage());
                    }
                });
    }
}
