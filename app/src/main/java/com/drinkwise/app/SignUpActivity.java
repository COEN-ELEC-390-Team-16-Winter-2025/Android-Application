package com.drinkwise.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {
    private EditText emailTextView, passwordTextView;
    private Button button;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);



        auth = FirebaseAuth.getInstance();

        emailTextView = findViewById(R.id.email_edittext);
        passwordTextView = findViewById(R.id.password_edittext);
        button = findViewById(R.id.signup_button);

        button.setOnClickListener(v -> registerNewUser());

    }
    @Override
    protected void onStart(){
        super.onStart();
//        if(auth.getCurrentUser() !=null){
//            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
//            finish();
//        }
    }
    private void registerNewUser() {
        // Get values from user input
        String email = emailTextView.getText().toString().trim();
        String password = passwordTextView.getText().toString().trim();


        if (!isValidEmail(email)) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_LONG).show();
            return;
        }

        if (!isValidPassword(password)) {
            Toast.makeText(this, "Invalid password format", Toast.LENGTH_LONG).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SignUpActivity.this, "Registration successful", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(SignUpActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Email validation
    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Password validation (Min 8 characters, 1 uppercase, 1 lowercase, 1 number)
    private boolean isValidPassword(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[@#$%^&+=!].*") &&
                password.matches(".*\\d.*");
    }
}
