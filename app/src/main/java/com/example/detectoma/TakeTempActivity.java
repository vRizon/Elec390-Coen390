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

public class TakeTempActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private TextView tempTextView;
    private static final String TAG = "TakeTempActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_temp);

        // Initialize Firebase Auth and Database Reference
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("profiles");

        // Initialize UI components
        tempTextView = findViewById(R.id.tempTextView); // Add this TextView to your layout
        Button startMeasurementButton = findViewById(R.id.startMeasurementButton);

        // Set up click listener for the Start Measurement button
        startMeasurementButton.setOnClickListener(v -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid(); // Get the current user's UID
                Log.d(TAG, "Fetching temperature for User ID: " + userId);
                fetchTemperature(userId);
                new AlertDialog.Builder(this)
                        .setTitle("Confirmation")
                        .setMessage("Are you sure you want to submit this data?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            setResult(RESULT_OK);
                            Toast.makeText(this, "Data submitted successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();
            } else {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "User not logged in");
            }
        });
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
}
