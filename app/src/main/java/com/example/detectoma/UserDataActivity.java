package com.example.detectoma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class UserDataActivity extends AppCompatActivity {

    private CheckBox asymmetryCheckBox, borderCheckBox, colorCheckBox, diameterCheckBox, evolvingCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data);

        // Initialize UI elements
        asymmetryCheckBox = findViewById(R.id.asymmetryCheckBox);
        borderCheckBox = findViewById(R.id.borderCheckBox);
        colorCheckBox = findViewById(R.id.colorCheckBox);
        diameterCheckBox = findViewById(R.id.diameterCheckBox);
        evolvingCheckBox = findViewById(R.id.evolvingCheckBox);
        Button submitButton = findViewById(R.id.submitButton);
        ImageView backIcon = findViewById(R.id.backIcon);

        // Set up the back button click listener
        backIcon.setOnClickListener(v -> finish()); // Close the current activity and navigate back

        // Set up the submit button click listener
        submitButton.setOnClickListener(v -> submitData());
    }

    private void submitData() {
        // Collect the states of all checkboxes
        boolean asymmetry = asymmetryCheckBox.isChecked();
        boolean border = borderCheckBox.isChecked();
        boolean color = colorCheckBox.isChecked();
        boolean diameter = diameterCheckBox.isChecked();
        boolean evolving = evolvingCheckBox.isChecked();

        // Check if any data was selected
        if (asymmetry || border || color || diameter || evolving) {
            // Pass data back to ScreeningActivity using Intent
            Intent resultIntent = new Intent();
            resultIntent.putExtra("asymmetry", asymmetry);
            resultIntent.putExtra("border", border);
            resultIntent.putExtra("color", color);
            resultIntent.putExtra("diameter", diameter);
            resultIntent.putExtra("evolving", evolving);

            setResult(RESULT_OK, resultIntent); // Return the result
            finish(); // Close UserDataActivity and go back to ScreeningActivity
        } else {
            // Notify the user if no options are selected
            Toast.makeText(this, "Please select at least one checkbox to proceed.", Toast.LENGTH_SHORT).show();
        }
    }
}
