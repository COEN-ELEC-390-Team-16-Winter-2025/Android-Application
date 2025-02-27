package com.drinkwise.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.drinkwise.app.ui.dashboard.DashboardFragment;

public class UserProfileActivity1 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile1);

        // Find the button and set a click listener
        Button homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity1.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
