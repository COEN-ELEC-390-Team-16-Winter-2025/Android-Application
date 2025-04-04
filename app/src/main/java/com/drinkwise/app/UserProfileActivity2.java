package com.drinkwise.app;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private Button SaveButton;
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
        Button nextButton = findViewById(R.id.NextButton);

        //Initialize Firebase references
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //Open date picker for birthday
        BirthdayTextView.setOnClickListener(v -> showDatePicker());

        //Navigate to the dashboard
        nextButton.setOnClickListener(v -> {
            if(validInputs()){
                saveToFirebase();
                SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("returningUser", true);
                editor.apply();
                Intent intent = new Intent(UserProfileActivity2.this, LandingActivity.class);
                startActivity(intent);
            }
        });

    }

    private void showDatePicker(){
        final Calendar calendar = Calendar.getInstance();

        // Subtract 18 years to set default year
        calendar.add(Calendar.YEAR, -18);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        @SuppressLint("SetTextI18n") DatePickerDialog datePicker = new DatePickerDialog(this,  R.style.DatePickerDialogTheme, (view, selectedYear, selectedMonth, selectedDay) -> {

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

        // Max selectable date (today)
        datePicker.getDatePicker().setMaxDate(calendar.getTimeInMillis());

        // Min selectable date (optional, e.g., 100 years ago)
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -100);
        datePicker.getDatePicker().setMinDate(minDate.getTimeInMillis());

        // Move selection to 18 years ago by default
        Calendar defaultDate = Calendar.getInstance();
        defaultDate.add(Calendar.YEAR, -18);
        datePicker.updateDate(defaultDate.get(Calendar.YEAR), defaultDate.get(Calendar.MONTH), defaultDate.get(Calendar.DAY_OF_MONTH));

        datePicker.show();
    }

    @SuppressLint("SetTextI18n")
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

        assert user != null;
        String userID = user.getUid();
        Map<String, Object> userData =  new HashMap<>();
        userData.put("name", name);
        userData.put("birthday", birthday);

        DocumentReference DocRef = db.collection("users")
                .document(userID)
                .collection("profile")
                .document("stats");

        DocRef.update(userData).addOnSuccessListener(aVoid -> Toast.makeText(UserProfileActivity2.this, "Profile saved!", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(UserProfileActivity2.this, "Failed to save profile!",Toast.LENGTH_SHORT).show());
    }

}
