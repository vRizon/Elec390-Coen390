package com.example.detectoma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;

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

    ///
    private void submitData() {
        // Collect the states of all switches
        boolean asymmetry = asymmetrySwitch.isChecked();
        boolean border = borderSwitch.isChecked();
        boolean color = colorSwitch.isChecked();
        boolean diameter = diameterSwitch.isChecked();
        boolean evolving = evolvingSwitch.isChecked();

        // Create an intent to send data back to ScreeningActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("asymmetry", asymmetry);
        resultIntent.putExtra("border", border);
        resultIntent.putExtra("color", color);
        resultIntent.putExtra("diameter", diameter);
        resultIntent.putExtra("evolving", evolving);

        // Set the result and finish the activity
        setResult(RESULT_OK, resultIntent);
        finish(); // Close UserDataActivity and return to ScreeningActivity
    }
}
