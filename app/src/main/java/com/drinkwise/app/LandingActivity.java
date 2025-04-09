package com.drinkwise.app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Button;

// LandingActivity is the first screen shown to the user when they open the app
public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        Button getStartedButton = findViewById(R.id.getStartedButton);
        getStartedButton.setOnClickListener(v -> {
            SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isFirstTime", false);
            editor.apply();
            startActivity(new Intent(LandingActivity.this, SignUpActivity.class));
            finish();
        });

        Button signIn = findViewById(R.id.signInButton);
        signIn.setOnClickListener(v -> startActivity(new Intent(LandingActivity.this, SignInActivity.class)));
    }
}