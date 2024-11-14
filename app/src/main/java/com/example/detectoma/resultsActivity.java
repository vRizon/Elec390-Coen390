package com.example.detectoma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

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

        // Retrieve timestamp from Intent
        long timestamp = getIntent().getLongExtra("TIMESTAMP", 0);
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(timestamp));

        // Set timestamp in the toolbar or menu bar (assuming a TextView in menu bar to display this)
        TextView timestampView = findViewById(R.id.timestampTextView); // Make sure to add this in the XML layout
        timestampView.setText(formattedDate);

        // Done button to go back to HomeActivity
        Button doneButton = findViewById(R.id.doneButton);
        doneButton.setOnClickListener(v -> {
            Intent intent = new Intent(resultsActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

    }
}