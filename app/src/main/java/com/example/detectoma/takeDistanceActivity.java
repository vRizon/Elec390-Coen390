package com.example.detectoma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class takeDistanceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_distance);

        // Initialize the Start Distance Measurement button
        Button startDistanceMeasurementButton = findViewById(R.id.startDistanceMeasurementButton);

        // Set up click listener for the Start Distance Measurement button
        startDistanceMeasurementButton.setOnClickListener(v -> {
            // Simulate distance measurement process
            Toast.makeText(this, "Distance measurement started", Toast.LENGTH_SHORT).show();

            // Set result to indicate successful completion
            setResult(RESULT_OK);

            // Finish the activity and return to ScreeningActivity
            finish();
        });
    }
}
