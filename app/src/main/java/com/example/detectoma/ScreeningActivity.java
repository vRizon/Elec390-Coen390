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

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.storage.UploadTask;
//import com.google.gson.Gson;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

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

    public static final String SHARED_PREFS = "SharedPrefs";
    private static final String DISTANCE_SURFACE_KEY = "distanceSurface";
    private static final String DISTANCE_ARM_KEY = "distanceArm";


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
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd_HH:mm", Locale.getDefault()).format(new Date(timestamp));
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                .getReference("profiles")
                .child(uid)
                .child("screenings");
        DatabaseReference timestampRef = databaseRef.child(formattedDate); // Reference for this timestamp

        // Retrieve the distance values from SharedPreferences
        SharedPreferences sharedPreferencesL = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        float distanceSurface = sharedPreferencesL.getFloat(DISTANCE_SURFACE_KEY, -1.0f);
        float distanceArm = sharedPreferencesL.getFloat(DISTANCE_ARM_KEY, -1.0f);

        String roundedDistanceSurfaceStr, roundedDistanceArmStr;
        if (distanceSurface != -1.0f) {
            // Use BigDecimal for precise rounding
            BigDecimal bd = new BigDecimal(Float.toString(distanceSurface));
            bd = bd.setScale(2, RoundingMode.HALF_UP); // Rounds to two decimal places
            roundedDistanceSurfaceStr = bd.toPlainString();
        } else {
            // Handle the case where distanceSurface is not found
            roundedDistanceSurfaceStr = "0.00";
        }

        if (distanceArm != -1.0f) {
            // Use BigDecimal for precise rounding
            BigDecimal bd = new BigDecimal(Float.toString(distanceArm));
            bd = bd.setScale(2, RoundingMode.HALF_UP); // Rounds to two decimal places
            roundedDistanceArmStr = bd.toPlainString();
        } else {
            // Handle the case where distanceArm is not found
            roundedDistanceArmStr = "0.00";
        }

        // Retrieve TempDifference from SharedPreferences
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

        // Collect all database write tasks
        List<Task<Void>> databaseTasks = new ArrayList<>();
        databaseTasks.add(timestampRef.child("asymmetry").setValue(asymmetry));
        databaseTasks.add(timestampRef.child("border").setValue(border));
        databaseTasks.add(timestampRef.child("color").setValue(color));
        databaseTasks.add(timestampRef.child("diameter").setValue(diameter));
        databaseTasks.add(timestampRef.child("evolving").setValue(evolving));
        databaseTasks.add(timestampRef.child("temperatureDiff").setValue(roundedTempDifferenceStr));
        databaseTasks.add(timestampRef.child("distanceSurface").setValue(roundedDistanceSurfaceStr));
        databaseTasks.add(timestampRef.child("distanceArm").setValue(roundedDistanceArmStr));

        // Get the image upload task
        Task<Void> uploadTask = renameLocalImageAndUpload(uid, formattedDate);

        // Combine all tasks
        List<Task<?>> allTasks = new ArrayList<>();
        allTasks.addAll(databaseTasks);
        allTasks.add(uploadTask);

        // Wait for all tasks to complete
        Tasks.whenAll(allTasks)
                .addOnSuccessListener(aVoid -> {
                    // All tasks completed successfully
                    Toast.makeText(this, "Data saved and images uploaded successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, resultsActivity.class);
                    intent.putExtra("FORMATTED_DATE", formattedDate);
                    intent.putExtra("UID", uid);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    // At least one of the tasks failed
                    Toast.makeText(this, "Failed to complete all tasks.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    private Task<Void> renameLocalImageAndUpload(String uid, String formattedDate) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        try {
            // Load the existing image from internal storage
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

            // Start the upload tasks
            UploadTask uploadTask1 = newImageRef.putBytes(imageBytes);
            UploadTask uploadTask2 = newImageRefGraph.putBytes(imageBytesGraph);

            // Wait for both uploads to complete
            Tasks.whenAll(uploadTask1, uploadTask2)
                    .addOnSuccessListener(aVoid -> {
                        // Both uploads succeeded
                        taskCompletionSource.setResult(null);
                    })
                    .addOnFailureListener(e -> {
                        // At least one of the uploads failed
                        e.printStackTrace();
                        taskCompletionSource.setException(e);
                    });


        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to access the local image.", Toast.LENGTH_SHORT).show();
            taskCompletionSource.setException(e);
            return taskCompletionSource.getTask(); // Return early to prevent further execution
        }

        return taskCompletionSource.getTask();
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
