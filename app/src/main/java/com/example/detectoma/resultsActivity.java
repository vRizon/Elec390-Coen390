package com.example.detectoma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class resultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_results);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Retrieve the formatted date and details from the Intent
        String formattedDate = getIntent().getStringExtra("FORMATTED_DATE");
        String details = getIntent().getStringExtra("DETAILS");

        TextView timestampView = findViewById(R.id.timestampTextView);
        TextView detailsView = findViewById(R.id.detailsTextView);

        // Display the formatted date
        if (formattedDate != null) {
            timestampView.setText(formattedDate);
        } else {
            timestampView.setText(getString(R.string.no_date_available));
        }

        // Display additional details
        if (details != null) {
            detailsView.setText(details);
        } else {
            detailsView.setText(getString(R.string.no_details_available));
        }

        Button doneButton = findViewById(R.id.doneButton);
        doneButton.setOnClickListener(v -> {
            Intent intent = new Intent(resultsActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

    }
}