package com.example.detectoma;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TakeTempActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private TextView tempTextView;
    private Button startMeasurementButton;
    private static final String TAG = "TakeTempActivity";

    private boolean isMeasuring = false;
    private List<TemperatureReading> temperatureReadings = new ArrayList<>();
    private ValueEventListener temperatureListener;
    private DatabaseReference buttonRef;
    private ValueEventListener buttonListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_temp);

        // Initialize Firebase Auth and Database Reference
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("profiles");

        // Initialize UI components
        tempTextView = findViewById(R.id.tempTextView); // Add this TextView to your layout
        startMeasurementButton = findViewById(R.id.startMeasurementButton);



//        // Set up click listener for the Start Measurement button
//        startMeasurementButton.setOnClickListener(v -> {
//            FirebaseUser currentUser = mAuth.getCurrentUser();
//            if (currentUser != null) {
//                String userId = currentUser.getUid(); // Get the current user's UID
//                Log.d(TAG, "Fetching temperature for User ID: " + userId);
//                fetchTemperature(userId);
//                new AlertDialog.Builder(this)
//                        .setTitle("Confirmation")
//                        .setMessage("Are you sure you want to submit this data?")
//                        .setPositiveButton("Yes", (dialog, which) -> {
//                            setResult(RESULT_OK);
//                            Toast.makeText(this, "Data submitted successfully", Toast.LENGTH_SHORT).show();
//                            finish();
//                        })
//                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
//                        .show();
//            } else {
//                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
//                Log.e(TAG, "User not logged in");
//            }
//        });

        // Get the current user's UID
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Set up a listener on the 'Button' node in Firebase
            buttonRef = databaseReference.child(userId).child("Button");

            buttonListener = buttonRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Boolean buttonState = snapshot.getValue(Boolean.class);
                    if (buttonState != null) {
                        if (buttonState && !isMeasuring) {
                            // Button is true and measurement is not ongoing
                            isMeasuring = true;
                            Log.d(TAG, "Button state is true. Starting temperature measurement.");
                            startTemperatureMeasurement();
                        } else if (!buttonState && isMeasuring) {
                            // Button is false and measurement is ongoing
                            isMeasuring = false;
                            Log.d(TAG, "Button state is false. Stopping temperature measurement.");
                            stopTemperatureMeasurement();
                        }
                        // Update UI to reflect measurement state
                        updateMeasurementButtonUI(buttonState);
                    } else {
                        Log.e(TAG, "Button state is null");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error fetching button state: " + error.getMessage());
                }
            });
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "User not logged in");
        }

        // Set up click listener for the Start/Stop Measurement button
//        startMeasurementButton.setOnClickListener(v -> {
//            if (!isMeasuring) {
//                // Start measurement
//                isMeasuring = true;
//                startMeasurementButton.setText("Stop Measurement");
//                startTemperatureMeasurement();
//            } else {
//                // Stop measurement
//                isMeasuring = false;
//                startMeasurementButton.setText("Start Measurement");
//                stopTemperatureMeasurement();
//            }
//        });
    }

    private void updateMeasurementButtonUI(boolean isMeasuring) {
        if (isMeasuring) {
            startMeasurementButton.setText("Measurement Ongoing");
        } else {
            startMeasurementButton.setText("Measurement Stopped");
        }
    }


    private void fetchTemperature(String userId) {
        DatabaseReference tempRef = databaseReference.child(userId).child("Temperature");

        tempRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        // Fetch temperature as Double
                        Double temperature = snapshot.getValue(Double.class);

                        // Update UI
                        if (temperature != null) {
                            tempTextView.setText("Temperature: " + temperature);
                        } else {
                            tempTextView.setText("Temperature: N/A");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing temperature: ", e);
                        Toast.makeText(TakeTempActivity.this, "Error parsing temperature", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TakeTempActivity.this, "Temperature data not found for this user", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Temperature data not found for User ID: " + userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TakeTempActivity.this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error fetching data: " + error.getMessage());
            }
        });
    }

    private void startTemperatureMeasurement() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d(TAG, "Starting temperature measurement for User ID: " + userId);

            // Clear previous readings
            temperatureReadings.clear();

            // Set up the listener
            DatabaseReference tempRef = databaseReference.child(userId).child("Temperature");

            temperatureListener = tempRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Double temperature = snapshot.getValue(Double.class);
                    if (temperature != null) {
                        long timestamp = System.currentTimeMillis();
                        TemperatureReading reading = new TemperatureReading(temperature, timestamp);
                        temperatureReadings.add(reading);

                        // Update UI
                        tempTextView.setText("Latest Temperature: " + temperature + "°C at " + timestamp);
                        Log.d(TAG, "Temperature reading: " + temperature + " at " + timestamp);

                        // Check for significant difference
                        checkForSignificantDifference();
                    } else {
                        Log.e(TAG, "Temperature data is null");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(TakeTempActivity.this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error fetching data: " + error.getMessage());
                }
            });
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "User not logged in");
        }
    }

    private void stopTemperatureMeasurement() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && temperatureListener != null) {
            String userId = currentUser.getUid();
            DatabaseReference tempRef = databaseReference.child(userId).child("Temperature");
            tempRef.removeEventListener(temperatureListener);
            Log.d(TAG, "Stopped temperature measurement for User ID: " + userId);

            // Process the collected data
            processTemperatureData();
        }
    }

    private void checkForSignificantDifference() {
        if (temperatureReadings.size() < 2) {
            // Need at least two readings to compare
            return;
        }

        int lastIndex = temperatureReadings.size() - 1;
        TemperatureReading lastReading = temperatureReadings.get(lastIndex);
        TemperatureReading previousReading = temperatureReadings.get(lastIndex - 1);

        double difference = Math.abs(lastReading.getTemperature() - previousReading.getTemperature());

        if (difference > 2.0) {
            // Found a significant difference
            String message = "Significant temperature change detected: " + difference + "°C";
            Log.d(TAG, message);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

            // Optionally, you can stop measurement automatically
            // isMeasuring = false;
            // startMeasurementButton.setText("Start Measurement");
            // stopTemperatureMeasurement();
        }
    }

    private void processTemperatureData() {
        if (temperatureReadings.isEmpty()) {
            Toast.makeText(this, "No temperature data collected", Toast.LENGTH_SHORT).show();
            return;
        }

        // You can perform additional analysis here if needed
        // For this example, we'll just log the collected readings
        for (TemperatureReading reading : temperatureReadings) {
            Log.d(TAG, "Collected Temperature: " + reading.getTemperature() + " at " + reading.getTimestamp());
        }

        // Show a confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Measurement Complete")
                .setMessage("Temperature measurement has stopped. Do you want to submit this data?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    setResult(RESULT_OK);
                    Toast.makeText(this, "Data submitted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listeners to prevent memory leaks
        if (buttonRef != null && buttonListener != null) {
            buttonRef.removeEventListener(buttonListener);
        }
        if (isMeasuring) {
            stopTemperatureMeasurement();
        }
    }



}
