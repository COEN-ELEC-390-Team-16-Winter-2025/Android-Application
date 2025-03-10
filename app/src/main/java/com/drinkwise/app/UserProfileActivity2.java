package com.drinkwise.app;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity2 extends AppCompatActivity {

    private EditText NameTextView;
    private TextView BirthdayTextView, NameErrorTextView, BirthdayErrorTextView;
    private Button SaveButton, NextButton;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String selectedDate;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile2);

        NameTextView = findViewById(R.id.editTextHeight);
        BirthdayTextView = findViewById(R.id.editTextWeight);
        NameErrorTextView = findViewById(R.id.NameErrorTextView);
        BirthdayErrorTextView = findViewById(R.id.BirthdayErrorTextView);
        NextButton = findViewById(R.id.NextButton);

        //Initialize Firebase references
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //Open date picker for birthday
        BirthdayTextView.setOnClickListener(v -> showDatePicker());

        validInputs();


        //Navigate to the dashboard
        NextButton.setOnClickListener(v -> {
            if(validInputs()){
                saveToFirebase();
            }
            Intent intent = new Intent(UserProfileActivity2.this, MainActivity.class);
            startActivity(intent);
        });

    }

    private void showDatePicker(){
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(selectedYear, selectedMonth, selectedDay);

            if(selectedCalendar.after(calendar)) {
                BirthdayErrorTextView.setText("Birthday cannot be in the future.");
                BirthdayErrorTextView.setVisibility(View.VISIBLE);
                BirthdayTextView.setText("Select your birthday");
                selectedDate = null;
            } else {
                selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                BirthdayTextView.setText(selectedDate);
                BirthdayErrorTextView.setVisibility(View.GONE);
            }
        }, year, month, day);
        datePicker.show();
    }

    private boolean validInputs() {
        boolean isValid = true;

        //Name validation
        String name = NameTextView.getText().toString().trim();
        if(name.isEmpty()) {
            NameErrorTextView.setText("Invalid, Name cannot be empty.");
            NameErrorTextView.setVisibility(View.VISIBLE);
            isValid = false;
        } else if(!name.matches("^[a-zA-Z\\s]+$")) {
            NameErrorTextView.setText("Invalid, Use only letters for your name.");
            NameErrorTextView.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            NameErrorTextView.setVisibility(View.GONE);
        }

        //Birthday validation
        if(selectedDate == null) {
            BirthdayErrorTextView.setText("Select a valid birthday.");
            BirthdayErrorTextView.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            BirthdayErrorTextView.setVisibility(View.GONE);
        }
        return isValid;
    }

    private void saveToFirebase(){
        String name = NameTextView.getText().toString().trim();
        String birthday = selectedDate;

        //Getting usr from firebase
        FirebaseUser user = auth.getCurrentUser();

        String userID = user.getUid();
        Map<String, Object> userData =  new HashMap<>();
        userData.put("name", name);
        userData.put("birthday", birthday);

        DocumentReference DocRef = db.collection("users")
                .document(userID)
                .collection("profile")
                .document("stats");

        DocRef.update(userData).addOnSuccessListener(aVoid -> {
            Toast.makeText(UserProfileActivity2.this, "Profile saved!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(UserProfileActivity2.this, "Failed to save profile!",Toast.LENGTH_SHORT).show();
        });
    }

}
