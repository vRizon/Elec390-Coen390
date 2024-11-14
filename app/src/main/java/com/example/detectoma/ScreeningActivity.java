package com.example.detectoma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
        analyzeButton.setOnClickListener(v -> openResultsActivity());

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
