package com.drinkwise.app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Button;

// LandingActivity is the first screen shown to users when they open the app
public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout for this activity.
        setContentView(R.layout.activity_landing);

        // Find the "Get Started" button by its ID.
        Button getStartedButton = findViewById(R.id.getStartedButton);
        // Set a click listener on the "Get Started" button.
        getStartedButton.setOnClickListener(v -> {
            // Get the shared preferences to store that the landing page was shown.
            SharedPreferences preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            // Mark that this is not the first time the user has seen the landing page.
            editor.putBoolean("isFirstTime", false);
            editor.apply();

            // Start the SignUpActivity when "Get Started" is clicked.
            startActivity(new Intent(LandingActivity.this, SignUpActivity.class));
            // Close the LandingActivity so it is removed from the back stack.
            finish();
        });

        // Find the "Sign In" button by its ID.
        Button signIn = findViewById(R.id.signInButton);
        // Set a click listener on the "Sign In" button to open SignInActivity.
        signIn.setOnClickListener(v -> startActivity(new Intent(LandingActivity.this, SignInActivity.class)));
    }
}