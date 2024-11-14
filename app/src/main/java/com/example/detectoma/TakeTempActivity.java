package com.example.detectoma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TakeTempActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_temp);

        // Initialize the Start Measurement button
        Button startMeasurementButton = findViewById(R.id.startMeasurementButton);

        // Set up click listener for the Start Measurement button
        startMeasurementButton.setOnClickListener(v -> {
            // Simulate temperature measurement process
            Toast.makeText(this, "Temperature measurement started", Toast.LENGTH_SHORT).show();

            // Set result to indicate successful completion
            setResult(RESULT_OK);

            // Finish the activity and return to ScreeningActivity
            finish();
        });
    }
}
