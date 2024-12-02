package com.example.detectoma;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private EditText linkCodeEditText;
    private Button linkToDoctorButton;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private RecyclerView pastScreeningsRecyclerView;
    private ScreeningAdapter adapter;
    private List<Map<String, Object>> screeningList = new ArrayList<>();
    private DatabaseReference linkedDoctorIdRef;
    private ValueEventListener linkedDoctorIdListener;
    private String patientId;
    private String currentUserId;
    private boolean isCurrentUserProfile;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Adjust window insets for edge-to-edge UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get the patientId from the intent if available
        patientId = getIntent().getStringExtra("patientId");

        // Initialize UI components
        linkCodeEditText = findViewById(R.id.linkCodeEditText);
        linkToDoctorButton = findViewById(R.id.linkToDoctorButton);
        ImageView logoutIcon = findViewById(R.id.logoutIcon); // Logout icon
        TextView greetingText = findViewById(R.id.greetingText);
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();


        // Initialize RecyclerView
        pastScreeningsRecyclerView = findViewById(R.id.pastScreeningsRecyclerView);
        pastScreeningsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pastScreeningsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new ScreeningAdapter(screeningList, this, patientId);
        pastScreeningsRecyclerView.setAdapter(adapter);


        // Fetch current user's name and update greetingText
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            // If patientId is null, we are viewing the current user's profile
            if (patientId == null) {
                patientId = currentUserId;
            }
            isCurrentUserProfile = patientId.equals(currentUserId);
        } else {
            // Handle not logged in
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch user's name and update greetingText
        DatabaseReference userRef = databaseReference.child("profiles").child(patientId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserProfile userProfile = snapshot.getValue(UserProfile.class);
                if (userProfile != null) {
                    String firstName = userProfile.getFirstName();
                    if (isCurrentUserProfile) {
                        greetingText.setText("Hello " + firstName);
                    } else {
                        greetingText.setText("Patient: " + firstName);
                    }
                } else {
                    greetingText.setText("Hello");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
            }
        });


        linkToDoctorButton.setOnClickListener(v -> linkToHealthcareProvider());

        Button startScreeningButton = findViewById(R.id.startScreeningButton);
        startScreeningButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ScreeningActivity.class);
            startActivity(intent);
        });

        // Logout icon functionality
        logoutIcon.setOnClickListener(v -> {
            mAuth.signOut(); // Firebase sign out
            Toast.makeText(ProfileActivity.this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
            startActivity(intent);
            finish(); // Finish current activity
        });

        // Load screenings from Firebase
        loadScreeningsFromFirebase();

        if (isCurrentUserProfile) {
            // The profile belongs to the current user
            // Keep UI components enabled or visible
            linkToDoctorButton.setVisibility(View.VISIBLE);
            linkCodeEditText.setVisibility(View.VISIBLE);
            logoutIcon.setVisibility(View.VISIBLE);
            startScreeningButton.setVisibility(View.VISIBLE);
        } else {
            // Viewing another user's profile (e.g., doctor viewing patient)
            // Hide or disable UI components not relevant
            linkToDoctorButton.setVisibility(View.GONE);
            linkCodeEditText.setVisibility(View.GONE);
            logoutIcon.setVisibility(View.GONE);
            startScreeningButton.setVisibility(View.GONE);
        }


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (isCurrentUserProfile) {
            if (currentUser != null) {
                String userId = currentUser.getUid();
                DatabaseReference userRef = databaseReference.child("profiles").child(userId);
                linkedDoctorIdRef = userRef.child("linkedDoctorId");

                linkedDoctorIdListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // If linked, disable the button and edit text
                            setComponentsDisabled();
                            Toast.makeText(ProfileActivity.this, "Already linked to a healthcare provider.", Toast.LENGTH_SHORT).show();
                        } else {
                            // If not linked, enable the components
                            setComponentsEnabled();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProfileActivity.this, "Failed to check link status.", Toast.LENGTH_SHORT).show();
                    }
                };

                linkedDoctorIdRef.addValueEventListener(linkedDoctorIdListener);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isCurrentUserProfile && linkedDoctorIdRef != null && linkedDoctorIdListener != null) {
            linkedDoctorIdRef.removeEventListener(linkedDoctorIdListener);
        }
    }



    private void linkToHealthcareProvider() {
        String linkCode = linkCodeEditText.getText().toString().trim();

        if (linkCode.isEmpty()) {
            Toast.makeText(this, "Please enter the code.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Search for the healthcare provider by link code
        databaseReference.child("profiles").orderByChild("linkCode").equalTo(linkCode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot providerSnapshot : snapshot.getChildren()) {
                                String providerId = providerSnapshot.getKey();
                                String patientId = mAuth.getCurrentUser().getUid();

                                // Link the patient to the healthcare provider
                                databaseReference.child("profiles").child(providerId).child("patients").child(patientId).setValue(true)
                                        .addOnSuccessListener(aVoid -> {
                                            databaseReference.child("profiles").child(patientId).child("linkedDoctorId").setValue(providerId)
                                                    .addOnSuccessListener(aVoid2 -> {
                                                        Toast.makeText(ProfileActivity.this, "Successfully linked to healthcare provider!", Toast.LENGTH_SHORT).show();
                                                        setComponentsDisabled();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(ProfileActivity.this, "Failed to link to healthcare provider.", Toast.LENGTH_SHORT).show();
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(ProfileActivity.this, "Failed to add patient to healthcare provider.", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            Toast.makeText(ProfileActivity.this, "Invalid code.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProfileActivity.this, "Failed to link to provider.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setComponentsEnabled() {
        // Enable the button and EditText
        linkToDoctorButton.setEnabled(true);
        linkToDoctorButton.setAlpha(1.0f);
        linkCodeEditText.setEnabled(true);
        linkCodeEditText.setAlpha(1.0f);
    }

    private void setComponentsDisabled() {
        // Disable the button and EditText
        linkToDoctorButton.setEnabled(false);
        linkToDoctorButton.setAlpha(0.5f);
        linkCodeEditText.setEnabled(false);
        linkCodeEditText.setAlpha(0.5f);
    }

    private void loadScreeningsFromFirebase() {
        DatabaseReference screeningsRef = FirebaseDatabase.getInstance().getReference()
                .child("profiles").child(patientId).child("screenings");

        screeningsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                screeningList.clear();
                for (DataSnapshot screeningSnapshot : snapshot.getChildren()) {
                    Map<String, Object> screeningData = (Map<String, Object>) screeningSnapshot.getValue();
                    if (screeningData != null) {
                        // Add the timestamp (key) to the data map
                        screeningData.put("timestamp", screeningSnapshot.getKey());
                        screeningList.add(screeningData);
                    }
                }

                /////

                // Sort the list by timestamp in descending order
                screeningList.sort((screening1, screening2) -> {
                    String timestamp1 = (String) screening1.get("timestamp");
                    String timestamp2 = (String) screening2.get("timestamp");

                    // Compare timestamps
                    return timestamp2.compareTo(timestamp1); // Descending order
                });

                ////



                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to load screenings.", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
