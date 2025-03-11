package com.drinkwise.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {

    private EditText emailTextView, passwordTextView;
    private Button button;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        //Initialize FirebaseAuth instance

        auth = FirebaseAuth.getInstance();

        emailTextView = findViewById(R.id.email_edittext);
        passwordTextView = findViewById(R.id.password_edittext);
        button = findViewById(R.id.signin_button);

        button.setOnClickListener(v -> SignInUserAccount());

    }

    @Override
    protected void onStart(){
        super.onStart();
       /* if(auth.getCurrentUser() !=null){
            startActivity(new Intent(SignInActivity.this, MainActivity.class));
            finish();
        }

        */
    }

    private void SignInUserAccount() {
        String email = emailTextView.getText().toString().trim();
        String password = passwordTextView.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter credentials", Toast.LENGTH_LONG).show();
            return;
        }
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //Sign in successful
                    SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("returningUser", true);
                    editor.apply();
                    startActivity(new Intent(SignInActivity.this, MainActivity.class));

                    finish();
                } else {
                    if (task.getException() != null) {
                        Toast.makeText(SignInActivity.this, "Sign in failed: " +task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(SignInActivity.this, "Sign in failed", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
}
