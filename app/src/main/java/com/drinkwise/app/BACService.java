package com.drinkwise.app;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BACService {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    //Upload BAC readings
    public void uploadBACReading(float bacValue) {
        //Retrieve user by userID
        String userId = auth.getCurrentUser().getUid();
        //Ensure user is logged in
        if(userId == null) {
            Log.e("Firestore", "User not authenticated");
            return;
        }
        //Validate BAC Range (0.00-0.50%)
        if(bacValue < 0.00 || bacValue > 0.50 ) {
            Log.e("Firestore", "Invalid BAC value: " + bacValue);
            return;
        }

        //Generate a timestamp
        String timeStamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        //Check for duplicate timestamp in collection
        db.collection("users").document(userId).collection("bacData").document(timeStamp)
                .get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()) {
                Log.e("Firestore", "Duplicate timestamp detected");
            } else {
                //Create map to store data
                Map<String, Object> data = new HashMap<>();
                data.put("timeStamp", new Timestamp(new Date()));
                data.put("bac", bacValue);

                db.collection("users").document(userId).collection("bacData").document(timeStamp)
                        .set(data).addOnSuccessListener(aVoid -> Log.d("Firestore", "BAC Stored successfully"))
                        .addOnFailureListener(e -> Log.e("Firestore", "Error when storing BAC", e));
            }
        });
    }


}
