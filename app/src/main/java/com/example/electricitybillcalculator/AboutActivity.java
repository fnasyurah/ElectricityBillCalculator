package com.example.electricitybillcalculator;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Set ActionBar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("ℹ️ About");
        }

        // Setup GitHub link click listener
        TextView githubLink = findViewById(R.id.githubLink);
        if (githubLink != null) {
            githubLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Open GitHub URL in browser
                    String url = "https://github.com/fnasyurah/ElectricityBillCalculator.git";
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }
            });
        }
    }
}