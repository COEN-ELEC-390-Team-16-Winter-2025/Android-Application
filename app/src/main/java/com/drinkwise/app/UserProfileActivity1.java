package com.drinkwise.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserProfileActivity1 extends AppCompatActivity {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch unitSwitch;
    private EditText editTextHeight, editTextWeight;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile1);

        // link UI components
        unitSwitch = findViewById(R.id.unitSwitch);
        editTextHeight = findViewById(R.id.editTextHeight);
        editTextWeight = findViewById(R.id.editTextWeight);
        Button nextButton = findViewById(R.id.nextButton);

        unitSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Switch ON: metric
                unitSwitch.setText("Metric Units");
                editTextHeight.setHint("Height (cm)");
                editTextWeight.setHint("Weight (kg)");
            } else {
                // Switch OFF: imperial
                unitSwitch.setText("Imperial Units");
                editTextHeight.setHint("Height (ft)");
                editTextWeight.setHint("Weight (lb)");
            }
        });

        // Next Button logic : goes to user profile activity 2
        nextButton.setOnClickListener(v -> {
            if(validateInputs()){
                storeUserData();
            }
            Intent intent = new Intent(UserProfileActivity1.this, UserProfileActivity2.class);
            startActivity(intent);
            finish();
        });
    }

    //Data validation
    private boolean validateInputs() {
        // Reset errors
        editTextHeight.setError(null);
        editTextWeight.setError(null);

        // Read strings
        String heightStr = editTextHeight.getText().toString().trim();
        String weightStr = editTextWeight.getText().toString().trim();

        // Check if fields are empty
        if (TextUtils.isEmpty(heightStr)) {
            editTextHeight.setError("Height is required");
            editTextHeight.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(weightStr)) {
            editTextWeight.setError("Weight is required");
            editTextWeight.requestFocus();
            return false;
        }

        // Convert to floats
        float height;
        float weight;
        try {
            height = Float.parseFloat(heightStr);
            weight = Float.parseFloat(weightStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid numeric value", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check range (metric example: height 50-250 cm, weight 2-300 kg)
        if (unitSwitch.isChecked()) {
            // Metric
            if (height < 50f || height > 250f) {
                editTextHeight.setError("Height must be between 50 cm and 250 cm");
                editTextHeight.requestFocus();
                return false;
            }
            if (weight < 2f || weight > 300f) {
                editTextWeight.setError("Weight must be between 2 kg and 300 kg");
                editTextWeight.requestFocus();
                return false;
            }
        } else {
            // Imperial - example: 1.6 ft to ~8.2 ft, 4.4 lb to 661 lb
            if (height < 1.6f || height > 8.2f) {
                editTextHeight.setError("Height must be between 1.6 ft and 8.2 ft");
                editTextHeight.requestFocus();
                return false;
            }
            if (weight < 4.4f || weight > 661f) {
                editTextWeight.setError("Weight must be between 4.4 lb and 661 lb");
                editTextWeight.requestFocus();
                return false;
            }
        }

        // If all checks pass, inputs are valid
        return true;
    }

    // store the user's data in firestore

    private void storeUserData() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String userId = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // convert to float
        float height = Float.parseFloat(editTextHeight.getText().toString());
        float weight = Float.parseFloat(editTextWeight.getText().toString());
        boolean isMetric = unitSwitch.isChecked();

        // create a map with the user profile info
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("height", height);
        userProfile.put("weight", weight);
        userProfile.put("isMetric", isMetric);

        db.collection("users")
                .document(userId)
                .collection("profile")
                .document("stats")
                .set(userProfile)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(UserProfileActivity1.this, "Profile saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(UserProfileActivity1.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}