package com.example.detectoma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class UserDataActivity extends AppCompatActivity {

    private Switch asymmetrySwitch, borderSwitch, colorSwitch, diameterSwitch, evolvingSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data);

        // Initialize UI elements
        asymmetrySwitch = findViewById(R.id.asymmetrySwitch);
        borderSwitch = findViewById(R.id.borderSwitch);
        colorSwitch = findViewById(R.id.colorSwitch);
        diameterSwitch = findViewById(R.id.diameterSwitch);
        evolvingSwitch = findViewById(R.id.evolvingSwitch);
        Button submitButton = findViewById(R.id.submitButton);

        // Set up the submit button click listener
        submitButton.setOnClickListener(v -> submitData());
    }

    private void submitData() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to submit?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // User confirmed, proceed with submission
                    setResult(RESULT_OK);
                    finish(); // Close the activity
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // User declined, stay on the same page
                    dialog.dismiss();
                })
                .show();
    }
}
