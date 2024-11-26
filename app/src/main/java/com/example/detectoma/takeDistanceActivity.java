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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class takeDistanceActivity extends AppCompatActivity {

    private static final String TAG = "takeDistanceActivity";

    private DatabaseReference databaseReference;
    private TextView distanceSurfaceTextView;
    private TextView distanceArmTextView;
    private Float firstDistance = null; // Store the first reading
    private Float secondDistance = null; // Store the second reading
    private static final String SHARED_PREFS = "SharedPrefs";
    private static final String DISTANCE_SURFACE_KEY = "distanceSurface";
    private static final String DISTANCE_ARM_KEY = "distanceArm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_distance);

        // Initialize Firebase Database
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("profiles").child(uid).child("Distance");
        }

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
                    // Attempt to retrieve the value as a Double and handle different cases
                    try {
                        Object value = dataSnapshot.getValue();
                        Log.d(TAG, "Retrieved value type: " + (value != null ? value.getClass().getSimpleName() : "null"));

                        Float distance = null;

                        if (value instanceof Double) {
                            distance = ((Double) value).floatValue();
                        } else if (value instanceof Long) {
                            distance = ((Long) value).floatValue();
                        } else if (value instanceof Float) {
                            distance = (Float) value;
                        } else if (value instanceof String) {
                            try {
                                distance = Float.parseFloat((String) value);
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Failed to parse String to Float", e);
                            }
                        }

                        if (distance != null) {
                            Log.d(TAG, "Parsed Distance: " + distance);
                            processDistanceReading(distance);
                        } else {
                            Toast.makeText(takeDistanceActivity.this, "Distance data could not be parsed or is null", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Distance data is either null or could not be converted to a Float.");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error retrieving distance. Data format might be incorrect.", e);
                        Toast.makeText(takeDistanceActivity.this, "Failed to parse distance data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(takeDistanceActivity.this, "No distance data available", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "No distance data found in Firebase.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error: ", databaseError.toException());
                Toast.makeText(takeDistanceActivity.this, "Failed to retrieve data", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void processDistanceReading(Float distance) {
        // Round the distance to two decimal places and log it
        distance = roundToTwoDecimalPlaces(distance);
        Log.d(TAG, "Rounded Distance: " + distance);

        if (firstDistance == null) {
            firstDistance = distance;
            distanceSurfaceTextView.setText("Distance to surface: " + String.format("%.2f", firstDistance) + " cm");
            Log.d(TAG, "First distance (surface) set to: " + firstDistance);
            Toast.makeText(this, "First reading (surface) set to " + String.format("%.2f", firstDistance) + " cm", Toast.LENGTH_SHORT).show();
        } else if (secondDistance == null) {
            secondDistance = distance;

            if (firstDistance < secondDistance) {
                distanceArmTextView.setText("Distance to arm: " + String.format("%.2f", firstDistance) + " cm");
                distanceSurfaceTextView.setText("Distance to surface: " + String.format("%.2f", secondDistance) + " cm");
                Log.d(TAG, "Distances - Arm: " + firstDistance + " cm, Surface: " + secondDistance + " cm");
            } else {
                distanceArmTextView.setText("Distance to arm: " + String.format("%.2f", secondDistance) + " cm");
                distanceSurfaceTextView.setText("Distance to surface: " + String.format("%.2f", firstDistance) + " cm");
                Log.d(TAG, "Distances - Arm: " + secondDistance + " cm, Surface: " + firstDistance + " cm");
            }

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Confirmation")
                    .setMessage("Are you sure you want to submit this data?")
                    .setPositiveButton("Yes", (dialogInterface, which) -> {
                        // Save distances to SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putFloat(DISTANCE_SURFACE_KEY, firstDistance);
                        editor.putFloat(DISTANCE_ARM_KEY, secondDistance);
                        editor.apply();
                        Log.d(TAG, "Distances saved to SharedPreferences - Surface: " + firstDistance + ", Arm: " + secondDistance);

                        // Save distances to Firebase as Strings with two decimal places
                        saveDistancesToFirebase(firstDistance, secondDistance);

                        setResult(RESULT_OK);
                        Toast.makeText(this, "Data submitted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("No", (dialogInterface, which) -> {
                        dialogInterface.dismiss();
                        firstDistance = null;
                        secondDistance = null;
                        Log.d(TAG, "Submission canceled. Distances reset.");
                    })
                    .create();

            dialog.setOnShowListener(dialogInterface -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.darkGreen));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.darkGreen));
            });

            dialog.show();
        } else {
            Toast.makeText(this, "Both readings are already set", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Both distances are already set. No further readings are allowed.");
        }
    }

    private void saveDistancesToFirebase(Float distanceSurface, Float distanceArm) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("profiles").child(uid).child("Distance");

            // Format distances to two decimal places as strings before saving to Firebase
            String formattedDistanceSurface = String.format(Locale.US, "%.2f", distanceSurface);
            String formattedDistanceArm = String.format(Locale.US, "%.2f", distanceArm);

            Log.d(TAG, "Saving to Firebase - Surface: " + formattedDistanceSurface + ", Arm: " + formattedDistanceArm);

            // Create a map of values to ensure we save them as Strings
            Map<String, String> distancesMap = new HashMap<>();
            distancesMap.put("distanceSurface", formattedDistanceSurface);
            distancesMap.put("distanceArm", formattedDistanceArm);

            // Save formatted values as strings to ensure correct precision
            databaseReference.setValue(distancesMap)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Distances saved successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to save distances to Firebase", e));
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "User not logged in. Unable to save distances to Firebase.");
        }
    }


    private Float roundToTwoDecimalPlaces(Float value) {
        Float roundedValue = (float) (Math.round(value * 100.0) / 100.0);
        Log.d(TAG, "Rounded value to two decimal places: " + roundedValue);
        return roundedValue;
    }



}
