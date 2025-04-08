package com.drinkwise.app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.text.Html;
import android.text.method.LinkMovementMethod;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class InfoActivity extends AppCompatActivity {
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.custom_actionbar_title);
            TextView title = actionBar.getCustomView().findViewById(R.id.action_bar_title);
            title.setText("Information Page");


            ImageButton infoButton = actionBar.getCustomView().findViewById(R.id.info_button);
            ImageButton profileButton = actionBar.getCustomView().findViewById(R.id.profile_icon);

            infoButton.setVisibility(View.GONE);
            profileButton.setVisibility(View.GONE);


            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.arrow_info);

            //Support resources links
            TextView supportResourcesText = findViewById(R.id.support_resources_text);
            supportResourcesText.setText(Html.fromHtml(getString(R.string.support_resources), Html.FROM_HTML_MODE_LEGACY));


            supportResourcesText.setMovementMethod(LinkMovementMethod.getInstance());
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
