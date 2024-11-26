package com.example.detectoma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

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

        // Initialize Info Icons
        ImageView asymmetryInfo = findViewById(R.id.asymmetryInfo);
        ImageView borderInfo = findViewById(R.id.borderInfo);
        ImageView colorInfo = findViewById(R.id.colorInfo);
        ImageView diameterInfo = findViewById(R.id.diameterInfo);
        ImageView evolvingInfo = findViewById(R.id.evolvingInfo);

        // Set up the back button click listener
        backIcon.setOnClickListener(v -> finish()); // Close the current activity and navigate back

        // Set up the submit button click listener
        submitButton.setOnClickListener(v -> submitData());

        // Set up info icon click listeners for DialogFragment
        asymmetryInfo.setOnClickListener(v -> showExplanationDialog(
                "Asymmetry",
                "One half of the mole does not match the other half. This is a key indicator of abnormal growth."
        ));

        borderInfo.setOnClickListener(v -> showExplanationDialog(
                "Border",
                "Look for edges that are uneven, scalloped, or notched. These borders are often irregular."
        ));

        colorInfo.setOnClickListener(v -> showExplanationDialog(
                "Color",
                "Normal moles are typically a single shade of brown. Multiple or uneven colors can be a warning sign."
        ));

        diameterInfo.setOnClickListener(v -> showExplanationDialog(
                "Diameter",
                "Any mole larger than 6mm (the size of a pencil eraser) or growing rapidly should be examined."
        ));

        evolvingInfo.setOnClickListener(v -> showExplanationDialog(
                "Evolving",
                "Changes in size, shape, color, or new symptoms like itching or bleeding are warning signs."
        ));
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

    /**
     * Shows the explanation dialog for the given title and message.
     *
     * @param title   The title of the dialog.
     * @param message The explanatory message to display.
     */
    private void showExplanationDialog(String title, String message) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        ExplanationDialogFragment dialogFragment = ExplanationDialogFragment.newInstance(title, message);
        dialogFragment.show(fragmentManager, "ExplanationDialog");
    }
}
