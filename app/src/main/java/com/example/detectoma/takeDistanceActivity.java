package com.example.detectoma;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class takeDistanceActivity extends AppCompatActivity {

    private static final String TAG = "takeDistanceActivity";

    private DatabaseReference databaseReference;
    private TextView distanceSurfaceTextView;
    private TextView distanceArmTextView;
    private Double firstDistance = null; // Store the first reading
    private Double secondDistance = null; // Store the second reading
    private static final String SHARED_PREFS = "SharedPrefs";
    private static final String DISTANCE_SURFACE_KEY = "distanceSurface";
    private static final String DISTANCE_ARM_KEY = "distanceArm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_distance);

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("profiles/4t34RojIIuNPeJ79j1OKWZJ75EJ2/Distance");

        // Initialize UI elements
        Button startDistanceMeasurementButton = findViewById(R.id.retreiveDistanceMeasurement);
        distanceSurfaceTextView = findViewById(R.id.distanceSurface); // Corrected to use class-level variable
        distanceArmTextView = findViewById(R.id.distanceArm); // Corrected to use class-level variable
        ImageView backIcon = findViewById(R.id.backIcon);
        backIcon.setOnClickListener(v -> {
            finish(); // Close the current activity and navigate back
        });
        // Set up click listener for the Start Distance Measurement button
        startDistanceMeasurementButton.setOnClickListener(v -> {
            // Retrieve distance data from Firebase
            retrieveDistanceData();
        });
    }

    private void retrieveDistanceData() {
        Log.d(TAG, "Attempting to retrieve data from Firebase...");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "DataSnapshot received: " + dataSnapshot.toString());
                if (dataSnapshot.exists()) {
                    // Attempt to retrieve the value as a Double
                    try {
                        Double distance = dataSnapshot.getValue(Double.class);
                        Log.d(TAG, "Distance retrieved: " + distance);
                        if (distance != null) {
                            processDistanceReading(distance);
                        } else {
                            Toast.makeText(takeDistanceActivity.this, "Distance data is null", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error retrieving distance as Double. Data format might be incorrect.", e);
                        Toast.makeText(takeDistanceActivity.this, "Failed to parse distance data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(takeDistanceActivity.this, "No distance data available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error: ", databaseError.toException());
                Toast.makeText(takeDistanceActivity.this, "Failed to retrieve data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processDistanceReading(Double distance) {
        if (firstDistance == null) {
            firstDistance = distance;
            distanceSurfaceTextView.setText("Distance to surface: " + distance + " cm");
            Toast.makeText(this, "First reading (surface) set to " + distance + " cm", Toast.LENGTH_SHORT).show();
        } else if (secondDistance == null) {
            secondDistance = distance;

            if (firstDistance < secondDistance) {
                distanceArmTextView.setText("Distance to arm: " + firstDistance + " cm");
                distanceSurfaceTextView.setText("Distance to surface: " + secondDistance + " cm");
            } else {
                distanceArmTextView.setText("Distance to arm: " + secondDistance + " cm");
                distanceSurfaceTextView.setText("Distance to surface: " + firstDistance + " cm");
            }

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Confirmation")
                    .setMessage("Are you sure you want to submit this data?")
                    .setPositiveButton("Yes", (dialogInterface, which) -> {
                        // Save distances to SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putFloat(DISTANCE_SURFACE_KEY, firstDistance.floatValue());
                        editor.putFloat(DISTANCE_ARM_KEY, secondDistance.floatValue());
                        editor.apply();

                        setResult(RESULT_OK);
                        Toast.makeText(this, "Data submitted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("No", (dialogInterface, which) -> {
                        dialogInterface.dismiss();
                        firstDistance = null;
                        secondDistance = null;
                    })
                    .create();

            dialog.setOnShowListener(dialogInterface -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.darkGreen));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.darkGreen));
            });

            dialog.show();
        } else {
            Toast.makeText(this, "Both readings are already set", Toast.LENGTH_SHORT).show();
        }
    }
}
