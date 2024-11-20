package com.example.detectoma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class ScreeningActivity extends AppCompatActivity {

    private static final int USER_DATA_REQUEST_CODE = 1;
    private static final int TAKE_PHOTO_REQUEST_CODE = 2;
    private static final int TAKE_TEMPERATURE_REQUEST_CODE = 3;
    private static final int TAKE_DISTANCE_REQUEST_CODE = 4;

    private Button userDataButton, takePhotoButton, takeTempButton, takeDistButton, analyzeButton;
    private CheckBox userDataCheckBox, takePhotoCheckBox, takeTempCheckBox, takeDistCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screening);

        // Initialize buttons and checkboxes
        userDataButton = findViewById(R.id.userDataButton);
        takePhotoButton = findViewById(R.id.takePhotoButton);
        takeTempButton = findViewById(R.id.takeTempButton);
        takeDistButton = findViewById(R.id.takeDistButton);
        analyzeButton = findViewById(R.id.analyzeButton);
        userDataCheckBox = findViewById(R.id.userDataCheckBox);
        takePhotoCheckBox = findViewById(R.id.takePhotoCheckBox);
        takeTempCheckBox = findViewById(R.id.takeTempCheckBox);
        takeDistCheckBox = findViewById(R.id.takeDistCheckBox);

        userDataButton.setOnClickListener(v -> openUserDataActivity());
        takePhotoButton.setOnClickListener(v -> openTakePhotoActivity());
        takeTempButton.setOnClickListener(v -> openTakeTemperatureActivity());
        takeDistButton.setOnClickListener(v -> openTakeDistanceActivity());

        analyzeButton.setOnClickListener(v -> {
            long timestamp = System.currentTimeMillis(); // Capture the current timestamp
            String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(timestamp));
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("profiles").child(uid).child("screenings");
            DatabaseReference timestampRef = databaseRef.child(formattedDate); // Reference for this timestamp

            // Example temperature and distance data (Replace these with actual values)
            double currentTemperature = 37.5; // Replace with actual temperature value
            double currentDistance = 15.0; // Replace with actual distance value

            // Save temperature and distance to RTDB
            timestampRef.child("temperature").setValue(currentTemperature);
            timestampRef.child("distance").setValue(currentDistance)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ScreeningActivity.this, "Data saved successfully!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ScreeningActivity.this, "Failed to save data.", Toast.LENGTH_SHORT).show();
                    });

            // Rename the current image in Firebase Storage
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference oldImageRef = storage.getReference("/Patients/" + uid + "/photo.jpg");
            StorageReference newImageRef = storage.getReference("/Patients/" + uid + "/" + formattedDate + ".jpg");

            oldImageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
                // Copy the image to the new path with the new name
                newImageRef.putBytes(bytes).addOnSuccessListener(taskSnapshot -> {
                    // Delete the old image after copying
                    oldImageRef.delete().addOnSuccessListener(aVoid -> {
                        Toast.makeText(ScreeningActivity.this, "Image renamed successfully!", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(ScreeningActivity.this, "Failed to delete old image.", Toast.LENGTH_SHORT).show();
                    });
                }).addOnFailureListener(e -> {
                    Toast.makeText(ScreeningActivity.this, "Failed to rename image.", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(ScreeningActivity.this, "Failed to access current image.", Toast.LENGTH_SHORT).show();
            });

            // Navigate to resultsActivity and pass the timestamp
            Intent intent = new Intent(ScreeningActivity.this, resultsActivity.class);
            intent.putExtra("FORMATTED_DATE", formattedDate);
            startActivity(intent);
        });



        updateButtonState();
    }

    private void updateButtonState() {
        takePhotoButton.setEnabled(userDataCheckBox.isChecked());
        takeTempButton.setEnabled(takePhotoCheckBox.isChecked());
        takeDistButton.setEnabled(takeTempCheckBox.isChecked());
        analyzeButton.setEnabled(userDataCheckBox.isChecked() && takePhotoCheckBox.isChecked()
                && takeTempCheckBox.isChecked() && takeDistCheckBox.isChecked());
    }

    private void openUserDataActivity() {
        Intent intent = new Intent(this, UserDataActivity.class);
        startActivityForResult(intent, USER_DATA_REQUEST_CODE);
    }

    private void openTakePhotoActivity() {
        Intent intent = new Intent(this, TakePhotoActivity.class);
        startActivityForResult(intent, TAKE_PHOTO_REQUEST_CODE);
    }

    private void openTakeTemperatureActivity() {
        Intent intent = new Intent(this, TakeTempActivity.class);
        startActivityForResult(intent, TAKE_TEMPERATURE_REQUEST_CODE);
    }

    private void openTakeDistanceActivity() {
        Intent intent = new Intent(this, takeDistanceActivity.class);
        startActivityForResult(intent, TAKE_DISTANCE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == USER_DATA_REQUEST_CODE && resultCode == RESULT_OK) {
            userDataCheckBox.setChecked(true);
            updateButtonState();
        } else if (requestCode == TAKE_PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {
            takePhotoCheckBox.setChecked(true);
            updateButtonState();
        } else if (requestCode == TAKE_TEMPERATURE_REQUEST_CODE && resultCode == RESULT_OK) {
            takeTempCheckBox.setChecked(true);
            updateButtonState();
        } else if (requestCode == TAKE_DISTANCE_REQUEST_CODE && resultCode == RESULT_OK) {
            takeDistCheckBox.setChecked(true);
            updateButtonState();
        }
    }

    private void openResultsActivity() {
        if (userDataCheckBox.isChecked() && takePhotoCheckBox.isChecked() &&
                takeTempCheckBox.isChecked() && takeDistCheckBox.isChecked()) {
            Intent intent = new Intent(ScreeningActivity.this, resultsActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Complete all steps before analyzing.", Toast.LENGTH_SHORT).show();
        }
    }
}
