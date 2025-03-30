package com.drinkwise.app;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.drinkwise.app.R;

public class InfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // Set up action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.custom_actionbar_title);
            TextView title = actionBar.getCustomView().findViewById(R.id.action_bar_title);
            title.setText("Information Page");

            // Hide the Info and Profile icons
            ImageButton infoButton = actionBar.getCustomView().findViewById(R.id.info_button);
            ImageButton profileButton = actionBar.getCustomView().findViewById(R.id.profile_icon);

            infoButton.setVisibility(View.GONE);
            profileButton.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}