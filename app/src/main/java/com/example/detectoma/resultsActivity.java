package com.example.detectoma;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class resultsActivity extends AppCompatActivity {

    private TextView resultsTextView;
    private TextView recommendationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // Initialize TextViews
        resultsTextView = findViewById(R.id.resultsTextView);
        recommendationTextView = findViewById(R.id.recommendationTextView);

        // Get the questionnaire results from the intent
        boolean asymmetry = getIntent().getBooleanExtra("asymmetry", false);
        boolean border = getIntent().getBooleanExtra("border", false);
        boolean color = getIntent().getBooleanExtra("color", false);
        boolean diameter = getIntent().getBooleanExtra("diameter", false);
        boolean evolving = getIntent().getBooleanExtra("evolving", false);

        // Analyze results
        analyzeResults(asymmetry, border, color, diameter, evolving);
    }

    private void analyzeResults(boolean asymmetry, boolean border, boolean color, boolean diameter, boolean evolving) {
        List<String> flaggedCriteria = new ArrayList<>();

        if (asymmetry) {
            flaggedCriteria.add("Asymmetry: The mole is asymmetrical, which can be a warning sign for melanoma.");
        }
        if (border) {
            flaggedCriteria.add("Border Irregularity: Uneven or notched borders can indicate a potentially dangerous mole.");
        }
        if (color) {
            flaggedCriteria.add("Color Variation: Multiple colors in a mole are concerning.");
        }
        if (diameter) {
            flaggedCriteria.add("Diameter: Moles larger than 6mm or unusually dark are flagged as concerning.");
        }
        if (evolving) {
            flaggedCriteria.add("Evolution: Changes in size, color, or symptoms like itching or bleeding are significant.");
        }

        // Build results explanation
        if (flaggedCriteria.isEmpty()) {
            resultsTextView.setText("No immediate concerns based on your responses. Keep monitoring for any changes.");
            recommendationTextView.setText("Recommendation: No need to seek medical consultation at this time.");
        } else {
            StringBuilder resultsBuilder = new StringBuilder();
            for (String criteria : flaggedCriteria) {
                resultsBuilder.append(criteria).append("\n\n");
            }
            resultsTextView.setText(resultsBuilder.toString());
            recommendationTextView.setText("Recommendation: We suggest consulting a dermatologist for further evaluation.");
        }
    }
}
