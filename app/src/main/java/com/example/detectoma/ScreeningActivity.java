package com.example.detectoma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ScreeningActivity extends AppCompatActivity {

    private Button userDataButton, takePhotoButton, takeTempButton, takeDistButton, analyzeButton;
    private CheckBox userDataCheckBox, takePhotoCheckBox, takeTempCheckBox, takeDistCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screening);

        userDataButton = findViewById(R.id.userDataButton);
        takePhotoButton = findViewById(R.id.takePhotoButton);
        takeTempButton = findViewById(R.id.takeTempButton);
        takeDistButton = findViewById(R.id.takeDistButton);
        analyzeButton = findViewById(R.id.analyzeButton);

        userDataCheckBox = findViewById(R.id.userDataCheckBox);
        takePhotoCheckBox = findViewById(R.id.takePhotoCheckBox);
        takeTempCheckBox = findViewById(R.id.takeTempCheckBox);
        takeDistCheckBox = findViewById(R.id.takeDistCheckBox);

        userDataButton.setOnClickListener(v -> openUserDataDialog());
        takePhotoButton.setOnClickListener(v -> openTakePhotoActivity());
        takeTempButton.setOnClickListener(v -> openTakeTempDialog());
        takeDistButton.setOnClickListener(v -> openTakeDietDialog());

        analyzeButton.setOnClickListener(v -> openResultsActivity());

        updateButtonState();
    }

    private void updateButtonState() {
        takePhotoButton.setEnabled(userDataCheckBox.isChecked());
        takeTempButton.setEnabled(takePhotoCheckBox.isChecked());
        takeDistButton.setEnabled(takeTempCheckBox.isChecked());
        analyzeButton.setEnabled(userDataCheckBox.isChecked() && takePhotoCheckBox.isChecked() &&
                takeTempCheckBox.isChecked() && takeDistCheckBox.isChecked());
    }

    private void openUserDataDialog() {
        // Open a DialogFragment to gather user data
        // Once completed:
        userDataCheckBox.setChecked(true);
        updateButtonState();
    }

    private void openTakePhotoActivity() {
        // Open an Activity to take a photo
        // Once completed:
        takePhotoCheckBox.setChecked(true);
        updateButtonState();
    }

    private void openTakeTempDialog() {
        // Open a DialogFragment to take temperature
        // Once completed:
        takeTempCheckBox.setChecked(true);
        updateButtonState();
    }

    private void openTakeDietDialog() {
        // Open a DialogFragment to enter diet information
        // Once completed:
        takeDistCheckBox.setChecked(true);
        updateButtonState();
    }

    private void openResultsActivity() {
        // Open ResultsActivity once all items are completed
        Intent intent = new Intent(ScreeningActivity.this, resultsActivity.class);
        startActivity(intent);
    }
}
