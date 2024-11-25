package com.example.detectoma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScreeningActivity extends AppCompatActivity {

    private static final int USER_DATA_REQUEST_CODE = 1;
    private static final int TAKE_PHOTO_REQUEST_CODE = 2;
    private static final int TAKE_TEMPERATURE_REQUEST_CODE = 3;
    private static final int TAKE_DISTANCE_REQUEST_CODE = 4;

    private Button userDataButton, takePhotoButton, takeTempButton, takeDistButton, analyzeButton;
    private CheckBox userDataCheckBox, takePhotoCheckBox, takeTempCheckBox, takeDistCheckBox;

    private boolean asymmetry = false;
    private boolean border = false;
    private boolean color = false;
    private boolean diameter = false;
    private boolean evolving = false;

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


        // Set up button listeners
        userDataButton.setOnClickListener(v -> openUserDataActivity());
        takePhotoButton.setOnClickListener(v -> openTakePhotoActivity());
        takeTempButton.setOnClickListener(v -> openTakeTemperatureActivity());
        takeDistButton.setOnClickListener(v -> openTakeDistanceActivity());
        ImageView backIcon = findViewById(R.id.backIcon);
        backIcon.setOnClickListener(v -> {
            finish(); // Close the current activity and navigate back
        });
        // Analyze button listener
        analyzeButton.setOnClickListener(v -> analyzeAndSaveResults());

        updateButtonState();
    }

    private void updateButtonState() {
        // Enable or disable buttons based on the completion of previous steps
        takePhotoButton.setEnabled(userDataCheckBox.isChecked());
        takeTempButton.setEnabled(takePhotoCheckBox.isChecked());
        takeDistButton.setEnabled(takeTempCheckBox.isChecked());
        analyzeButton.setEnabled(userDataCheckBox.isChecked() &&
                takePhotoCheckBox.isChecked() &&
                takeTempCheckBox.isChecked() &&
                takeDistCheckBox.isChecked());
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

    private void analyzeAndSaveResults() {
        long timestamp = System.currentTimeMillis(); // Capture the current timestamp
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(timestamp));
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("profiles").child(uid).child("screenings");
        DatabaseReference timestampRef = databaseRef.child(formattedDate); // Reference for this timestamp

        double currentTemperature = 37.5; // Replace with actual temperature value
        double currentDistance = 15.0; // Replace with actual distance value

        timestampRef.child("temperature").setValue(currentTemperature);
        timestampRef.child("distance").setValue(currentDistance);
        timestampRef.child("asymmetry").setValue(asymmetry);
        timestampRef.child("border").setValue(border);
        timestampRef.child("color").setValue(color);
        timestampRef.child("diameter").setValue(diameter);
        timestampRef.child("evolving").setValue(evolving)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Data saved successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save data.", Toast.LENGTH_SHORT).show());

        renameImageInStorage(uid, formattedDate);

        Intent intent = new Intent(this, resultsActivity.class);
        intent.putExtra("asymmetry", asymmetry);
        intent.putExtra("border", border);
        intent.putExtra("color", color);
        intent.putExtra("diameter", diameter);
        intent.putExtra("evolving", evolving);
        intent.putExtra("FORMATTED_DATE", formattedDate);
        startActivity(intent);
    }

    private void renameImageInStorage(String uid, String formattedDate) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference oldImageRef = storage.getReference("/Patients/" + uid + "/photo.jpg");
        StorageReference newImageRef = storage.getReference("/Patients/" + uid + "/" + formattedDate + ".jpg");

        oldImageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            newImageRef.putBytes(bytes).addOnSuccessListener(taskSnapshot -> {
                oldImageRef.delete().addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Image renamed successfully!", Toast.LENGTH_SHORT).show());
            }).addOnFailureListener(e -> Toast.makeText(this, "Failed to rename image.", Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to access current image.", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == USER_DATA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            asymmetry = data.getBooleanExtra("asymmetry", false);
            border = data.getBooleanExtra("border", false);
            color = data.getBooleanExtra("color", false);
            diameter = data.getBooleanExtra("diameter", false);
            evolving = data.getBooleanExtra("evolving", false);

            userDataCheckBox.setChecked(true);
        } else if (requestCode == TAKE_PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {
            takePhotoCheckBox.setChecked(true);
        } else if (requestCode == TAKE_TEMPERATURE_REQUEST_CODE && resultCode == RESULT_OK) {
            takeTempCheckBox.setChecked(true);
        } else if (requestCode == TAKE_DISTANCE_REQUEST_CODE && resultCode == RESULT_OK) {
            takeDistCheckBox.setChecked(true);
        }

        updateButtonState();
    }
}
