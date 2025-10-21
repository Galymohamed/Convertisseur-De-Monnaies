package com.example.eceandroidproject;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import static android.content.ContentValues.TAG;

public class FireBaseManager {

    private DatabaseReference mDatabase;
    @IgnoreExtraProperties
    public class Rate {

        public String devise;
        public Double rate;

        public Rate() {
            // Default constructor required for calls to DataSnapshot.getValue(Rate.class)
            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // ...
                    Log.d("firebase", "data changed");

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                }
            };
            mDatabase.addValueEventListener(postListener);
        }

        public Rate(String devise, double rate) {
            this.devise = devise;
            this.rate = rate;
        }
    }
    public void getInstance() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void writeNewRate(String rateId, String devise, double rateValue) {
        Rate rate = new Rate(devise, rateValue);
        mDatabase.child("rates").child(rateId).setValue(rate).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("firebase", "data written");

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Write failed
                        Log.e("firebase", "error when writing data");

                    }
                });;
    }



    public void setRate(String rateId, String devise, double rateValue) {
        mDatabase.child("rates").child(rateId).child(devise).setValue(rateValue);
    }
}
