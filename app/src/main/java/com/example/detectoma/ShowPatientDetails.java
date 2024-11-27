package com.example.detectoma;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

// Import Firebase dependencies
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class ShowPatientDetails extends AppCompatActivity {

    private String timestamp;
    private String userUid;
    private DatabaseReference mDatabase;

    // UI elements
    private TextView timestampTextView;
    private TextView temperatureDiffTextView;
    private TextView distanceSurfaceTextView;
    private TextView distanceArmTextView;
    private TextView asymmetryTextView;
    private TextView borderTextView;
    private TextView colorTextView;
    private TextView diameterTextView;
    private TextView evolvingTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_patient_details);

        // Initialize UI elements
        timestampTextView = findViewById(R.id.timestampTextView);
        temperatureDiffTextView = findViewById(R.id.temperatureDiffTextView);
        distanceSurfaceTextView = findViewById(R.id.distanceSurfaceTextView);
        distanceArmTextView = findViewById(R.id.distanceArmTextView);
        asymmetryTextView = findViewById(R.id.asymmetryTextView);
        borderTextView = findViewById(R.id.borderTextView);
        colorTextView = findViewById(R.id.colorTextView);
        diameterTextView = findViewById(R.id.diameterTextView);
        evolvingTextView = findViewById(R.id.evolvingTextView);

        // Get timestamp from intent extras
        timestamp = getIntent().getStringExtra("timestamp");

        if (timestamp == null) {
            Toast.makeText(this, "Invalid timestamp.", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if data is missing
            return;
        }

        // Get current user's UID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userUid = currentUser.getUid();
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase Database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Load screening details
        loadScreeningDetails();
    }

    private void loadScreeningDetails() {
        // Reference to the specific screening data under the current user's UID
        DatabaseReference screeningRef = mDatabase.child("profiles").child(userUid).child("screenings").child(timestamp);

        screeningRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    // Fetch data from snapshot
                    String timestampValue = snapshot.child("timestamp").getValue(String.class);
                    String temperatureDiff = snapshot.child("temperatureDiff").getValue(String.class);
                    String distanceSurface = snapshot.child("distanceSurface").getValue(String.class);
                    String distanceArm = snapshot.child("distanceArm").getValue(String.class);
                    Boolean asymmetry = snapshot.child("asymmetry").getValue(Boolean.class);
                    Boolean border = snapshot.child("border").getValue(Boolean.class);
                    Boolean color = snapshot.child("color").getValue(Boolean.class);
                    Boolean diameter = snapshot.child("diameter").getValue(Boolean.class);
                    Boolean evolving = snapshot.child("evolving").getValue(Boolean.class);

                    // Update UI elements
                    timestampTextView.setText("Timestamp: " + timestamp);
                    temperatureDiffTextView.setText("Temperature Difference: " + temperatureDiff + "°C");
                    distanceSurfaceTextView.setText("Distance Surface: " + distanceSurface + " cm");
                    distanceArmTextView.setText("Distance Arm: " + distanceArm + " cm");
                    asymmetryTextView.setText("Asymmetry: " + (asymmetry != null && asymmetry ? "Yes" : "No"));
                    borderTextView.setText("Border: " + (border != null && border ? "Yes" : "No"));
                    colorTextView.setText("Color: " + (color != null && color ? "Yes" : "No"));
                    diameterTextView.setText("Diameter: " + (diameter != null && diameter ? "Yes" : "No"));
                    evolvingTextView.setText("Evolving: " + (evolving != null && evolving ? "Yes" : "No"));
                } else {
                    Toast.makeText(ShowPatientDetails.this, "Screening data not found.", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity if data is missing
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ShowPatientDetails.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
