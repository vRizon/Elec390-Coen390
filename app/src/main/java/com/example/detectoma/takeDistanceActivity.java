package com.example.detectoma;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
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
    private ImageView tutorialGif;
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
        Button measureSurfaceButton = findViewById(R.id.measureSurfaceButton);
        Button measureArmButton = findViewById(R.id.measureArmButton);
        distanceSurfaceTextView = findViewById(R.id.distanceSurface);
        distanceArmTextView = findViewById(R.id.distanceArm);
        tutorialGif = findViewById(R.id.tutorialGif);
        ImageView backIcon = findViewById(R.id.backIcon);

        // Set up back button
        backIcon.setOnClickListener(v -> finish());

        // Load the GIF into the ImageView using Glide
        Glide.with(this)
                .asGif()
                .load(R.drawable.takedistance) // Replace with the correct resource name for the GIF
                .into(tutorialGif);

        // Set up click listener for Measure Surface button
        measureSurfaceButton.setOnClickListener(v -> retrieveDistanceDataForSurface());

        // Set up click listener for Measure Arm button
        measureArmButton.setOnClickListener(v -> retrieveDistanceDataForArm());
    }

    private void retrieveDistanceDataForSurface() {
        Log.d(TAG, "Retrieving distance to surface...");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        Double distance = dataSnapshot.getValue(Double.class);
                        if (distance != null) {
                            firstDistance = distance;
                            distanceSurfaceTextView.setText("Distance to surface: " + distance + " cm");
                            Toast.makeText(takeDistanceActivity.this, "Surface distance set to " + distance + " cm", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(takeDistanceActivity.this, "Distance data is null", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error retrieving distance for surface", e);
                        Toast.makeText(takeDistanceActivity.this, "Failed to retrieve surface distance", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(takeDistanceActivity.this, "No distance data available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error retrieving surface distance: ", databaseError.toException());
                Toast.makeText(takeDistanceActivity.this, "Failed to retrieve surface distance", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void retrieveDistanceDataForArm() {
        Log.d(TAG, "Retrieving distance to arm...");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        Double distance = dataSnapshot.getValue(Double.class);
                        if (distance != null) {
                            secondDistance = distance;
                            distanceArmTextView.setText("Distance to hand: " + distance + " cm");
                            Toast.makeText(takeDistanceActivity.this, "Arm distance set to " + distance + " cm", Toast.LENGTH_SHORT).show();
                            showConfirmationDialog();
                        } else {
                            Toast.makeText(takeDistanceActivity.this, "Distance data is null", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error retrieving distance for arm", e);
                        Toast.makeText(takeDistanceActivity.this, "Failed to retrieve arm distance", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(takeDistanceActivity.this, "No distance data available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error retrieving arm distance: ", databaseError.toException());
                Toast.makeText(takeDistanceActivity.this, "Failed to retrieve arm distance", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showConfirmationDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to submit this data?")
                .setPositiveButton("Yes", (dialogInterface, which) -> {
                    // Save distances to SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (firstDistance != null) editor.putFloat(DISTANCE_SURFACE_KEY, firstDistance.floatValue());
                    if (secondDistance != null) editor.putFloat(DISTANCE_ARM_KEY, secondDistance.floatValue());
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
    }
}
