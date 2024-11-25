package com.example.detectoma;

import android.content.Intent;
import android.content.SharedPreferences;
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

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
        backIcon.setOnClickListener(v -> finish());

        // Analyze button listener
        analyzeButton.setOnClickListener(v -> analyzeAndSaveResults());

        // Set initial state of the Analyze button
        updateAnalyzeButtonState();
    }

    private void updateAnalyzeButtonState() {
        // Enable Analyze button only when all tasks are completed
        boolean allStepsCompleted = userDataCheckBox.isChecked() &&
                takePhotoCheckBox.isChecked() &&
                takeTempCheckBox.isChecked() &&
                takeDistCheckBox.isChecked();

        analyzeButton.setEnabled(allStepsCompleted);
        analyzeButton.setBackgroundTintList(getResources().getColorStateList(
                allStepsCompleted ? R.color.darkGreen : R.color.grey
        ));
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
        long timestamp = System.currentTimeMillis();
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(timestamp));
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("profiles").child(uid).child("screenings");
        DatabaseReference timestampRef = databaseRef.child(formattedDate); // Reference for this timestamp

        double currentTemperature = 37.5; // Replace with actual temperature value
        double currentDistance = 15.0; // Replace with actual distance value

        // Retrieve Tempdifference from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        float tempDifferenceFloat = sharedPreferences.getFloat("TempDifference", -1.0f);

        // Check if tempDifferenceFloat is valid
        String roundedTempDifferenceStr;
        if (tempDifferenceFloat != -1.0f) {
            // Use BigDecimal for precise rounding
            BigDecimal bd = new BigDecimal(Float.toString(tempDifferenceFloat));
            bd = bd.setScale(2, RoundingMode.HALF_UP); // Rounds to two decimal places
            roundedTempDifferenceStr = bd.toPlainString();
        } else {
            // Handle the case where TempDifference is not found
            roundedTempDifferenceStr = "0.00";
        }

        timestampRef.child("temperature").setValue(currentTemperature);
        timestampRef.child("temperatureDiff").setValue(roundedTempDifferenceStr);
        timestampRef.child("distance").setValue(currentDistance);
        timestampRef.child("asymmetry").setValue(asymmetry);
        timestampRef.child("border").setValue(border);
        timestampRef.child("color").setValue(color);
        timestampRef.child("diameter").setValue(diameter);
        timestampRef.child("evolving").setValue(evolving)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Data saved successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save data.", Toast.LENGTH_SHORT).show());

        renameLocalImageAndUpload(uid, formattedDate);

        Intent intent = new Intent(this, resultsActivity.class);
        intent.putExtra("asymmetry", asymmetry);
        intent.putExtra("border", border);
        intent.putExtra("color", color);
        intent.putExtra("diameter", diameter);
        intent.putExtra("evolving", evolving);
        intent.putExtra("FORMATTED_DATE", formattedDate);
        startActivity(intent);
    }

    private void renameLocalImageAndUpload(String uid, String formattedDate) {
        try {
            // Load the existing Take Photo image from internal storage
            FileInputStream fis = openFileInput("image.jpg");
            byte[] imageBytes = new byte[fis.available()];
            fis.read(imageBytes);
            fis.close();

            // Load the existing graph image from internal storage
            FileInputStream fisG = openFileInput("saved_graph.jpg");
            byte[] imageBytesGraph = new byte[fisG.available()];
            fisG.read(imageBytesGraph);
            fisG.close();

            // Rename and upload to Firebase Storage
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference newImageRef = storage.getReference("/Patients/" + uid + "/i_" + formattedDate + ".jpg");
            StorageReference newImageRefGraph = storage.getReference("/Patients/" + uid + "/g_" + formattedDate + ".jpg");

            newImageRef.putBytes(imageBytes).addOnSuccessListener(taskSnapshot ->
                            Toast.makeText(this, "Image renamed and uploaded successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to rename and upload image.", Toast.LENGTH_SHORT).show());

            newImageRefGraph.putBytes(imageBytesGraph).addOnSuccessListener(taskSnapshot ->
                            Toast.makeText(this, "Graph Image renamed and uploaded successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to rename and upload Graph image.", Toast.LENGTH_SHORT).show());

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to access the local image.", Toast.LENGTH_SHORT).show();
        }
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

        // Update Analyze button state after each step
        updateAnalyzeButtonState();
    }
}
