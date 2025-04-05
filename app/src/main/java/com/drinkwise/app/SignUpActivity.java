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

// SignUpActivity handles user registration by collecting email and password.
public class SignUpActivity extends AppCompatActivity {
    // EditTexts for user input.
    private EditText emailTextView, passwordTextView, confirmPasswordTextView;
    // FirebaseAuth instance for handling authentication.
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the activity layout.
        setContentView(R.layout.activity_signup);

        // Find the info button (for password rules) and set its click listener.
        ImageButton infoButton = findViewById(R.id.password_info_button);
        infoButton.setOnClickListener(this::showPasswordPopup);

        // Initialize FirebaseAuth.
        auth = FirebaseAuth.getInstance();

        // Find views for email, password, and confirm password input.
        emailTextView = findViewById(R.id.email_edittext);
        passwordTextView = findViewById(R.id.password_edittext);
        confirmPasswordTextView = findViewById(R.id.confirm_password_edittext);

        // Find the sign-up button and set its click listener to register a new user.
        Button button = findViewById(R.id.signup_button);
        button.setOnClickListener(v -> registerNewUser());
    }

    // Registers a new user using the input email and password.
    private void registerNewUser() {
        // Get trimmed input values.
        String email = emailTextView.getText().toString().trim();
        String password = passwordTextView.getText().toString().trim();
        String confirmPassword = confirmPasswordTextView.getText().toString().trim();

        // Validate that all fields are filled.
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please enter all credentials", Toast.LENGTH_LONG).show();
            return;
        }
        // Check if the passwords match.
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_LONG).show();
            return;
        }
        // Check if the password meets the required format.
        if (!isValidPassword(password)) {
            Toast.makeText(this, "Invalid password format", Toast.LENGTH_LONG).show();
            return;
        }

        // Attempt to create a new user with email and password.
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Registration successful: show a success message and launch UserProfileActivity1.
                Toast.makeText(SignUpActivity.this, "Registration successful", Toast.LENGTH_LONG).show();
                startActivity(new Intent(SignUpActivity.this, UserProfileActivity1.class));
                finish();
            } else {
                // If registration fails, display the error message.
                if (task.getException() != null) {
                    Toast.makeText(SignUpActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SignUpActivity.this, "Registration failed", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Validates that the password meets specific criteria.
    private boolean isValidPassword(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&      // Contains at least one uppercase letter.
                password.matches(".*[@#$%^&+=!].*") && // Contains at least one special character.
                password.matches(".*\\d.*");          // Contains at least one digit.
    }

    // Displays a popup window showing the password rules.
    private void showPasswordPopup(View anchorView) {
        // Inflate the custom popup layout.
        @SuppressLint("InflateParams") View popupView = LayoutInflater.from(this).inflate(R.layout.popup_password_rules, null);

        // Set the password rules text.
        TextView passwordRulesText = popupView.findViewById(R.id.password_rules_text);
        passwordRulesText.setText("Password must:\n• Be at least 8 characters\n• Have 1 uppercase letter\n• Include 1 number\n• Include 1 special character");

        // Create a PopupWindow with wrap_content dimensions.
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        // Allow the popup to be dismissed when touching outside.
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        popupWindow.showAsDropDown(anchorView, 0, 10, Gravity.END);
    }
}