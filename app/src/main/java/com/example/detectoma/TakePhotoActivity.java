package com.example.detectoma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TakePhotoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);

        // Initialize the Upload Photo button
        Button uploadPhotoButton = findViewById(R.id.uploadPhotoButton);

        // Set up click listener for the Upload Photo button
        uploadPhotoButton.setOnClickListener(v -> {
            // Simulate photo upload process
            Toast.makeText(this, "Photo uploaded successfully", Toast.LENGTH_SHORT).show();

            // Set result to indicate successful completion
            setResult(RESULT_OK);

            // Finish the activity and return to ScreeningActivity
            finish();
        });
    }
}
