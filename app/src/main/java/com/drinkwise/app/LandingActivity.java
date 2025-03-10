package com.drinkwise.app;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        Button getStartedButton = findViewById(R.id.getStartedButton);
        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save preference so Landing Page is not shown again
                SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("isFirstTime", false); // Mark that landing page was shown
                editor.apply();

                // Open sign up page
                startActivity(new Intent(LandingActivity.this, SignUpActivity.class));
                finish(); // Close LandingActivity
            }
        });

        Button signIn = findViewById(R.id.signInButton);
        signIn.setOnClickListener(v -> startActivity(new Intent(LandingActivity.this, SignInActivity.class)));
    }
}