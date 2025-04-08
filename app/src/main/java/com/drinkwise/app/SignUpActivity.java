package com.drinkwise.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;


public class SignUpActivity extends AppCompatActivity {

    private EditText emailTextView, passwordTextView, confirmPasswordTextView;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup);


        ImageButton infoButton = findViewById(R.id.password_info_button);
        infoButton.setOnClickListener(this::showPasswordPopup);


        auth = FirebaseAuth.getInstance();


        emailTextView = findViewById(R.id.email_edittext);
        passwordTextView = findViewById(R.id.password_edittext);
        confirmPasswordTextView = findViewById(R.id.confirm_password_edittext);


        Button button = findViewById(R.id.signup_button);
        button.setOnClickListener(v -> registerNewUser());
    }


    private void registerNewUser() {

        String email = emailTextView.getText().toString().trim();
        String password = passwordTextView.getText().toString().trim();
        String confirmPassword = confirmPasswordTextView.getText().toString().trim();


        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please enter all credentials", Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_LONG).show();
            return;
        }

        if (!isValidPassword(password)) {
            Toast.makeText(this, "Invalid password format", Toast.LENGTH_LONG).show();
            return;
        }


        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {

                Toast.makeText(SignUpActivity.this, "Registration successful", Toast.LENGTH_LONG).show();
                startActivity(new Intent(SignUpActivity.this, UserProfileActivity1.class));
                finish();
            } else {

                if (task.getException() != null) {
                    Toast.makeText(SignUpActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SignUpActivity.this, "Registration failed", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private boolean isValidPassword(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&      // at least one uppercase letter
                password.matches(".*[@#$%^&+=!].*") && // at least one special character
                password.matches(".*\\d.*");          // at least one digit
    }


    private void showPasswordPopup(View anchorView) {

        @SuppressLint("InflateParams") View popupView = LayoutInflater.from(this).inflate(R.layout.popup_password_rules, null);


        TextView passwordRulesText = popupView.findViewById(R.id.password_rules_text);
        passwordRulesText.setText("Password must:\n• Be at least 8 characters\n• Have 1 uppercase letter\n• Include 1 number\n• Include 1 special character");


        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );


        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        popupWindow.showAsDropDown(anchorView, 0, 10, Gravity.END);
    }
}