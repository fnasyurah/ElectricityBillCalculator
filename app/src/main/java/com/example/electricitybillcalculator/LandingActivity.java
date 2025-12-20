package com.example.electricitybillcalculator;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class LandingActivity extends AppCompatActivity {

    private ImageView appImageView;
    private Button startButton;
    private Button aboutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        appImageView = findViewById(R.id.appImageView);
        startButton = findViewById(R.id.startButton);
        aboutButton = findViewById(R.id.aboutButton);
    }

    private void setupListeners() {
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to MainActivity (Calculator)
                Intent intent = new Intent(LandingActivity.this, MainActivity.class);
                startActivity(intent);
                // Optional: Add animation
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to AboutActivity
                Intent intent = new Intent(LandingActivity.this, AboutActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Exit app when back pressed from landing page
        finishAffinity();
    }
}