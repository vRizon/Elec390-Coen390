package com.example.detectoma;

import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class HealthcareProvider_ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Button generateCodeButton;
    private TextView viewCodeTextView;
    private String generatedCode;
    private LinearLayout patientsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_healthcare_provider_profile);

        // Adjust window insets for edge-to-edge UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });

        initializeViews();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            mDatabase = FirebaseDatabase.getInstance().getReference("profiles").child(userId);
            loadExistingCode();
            loadPatientData();
        }

        generateCodeButton.setOnClickListener(v -> {
            if (generatedCode == null) {
                generateAndSaveCode();
            } else {
                Toast.makeText(this, "Code has already been generated: " + generatedCode, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeViews() {
        generateCodeButton = findViewById(R.id.generateCodeButton);
        viewCodeTextView = findViewById(R.id.viewCodeTextView);
        patientsContainer = findViewById(R.id.patientsContainer);
    }

    private void loadPatientData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No user is signed in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String doctorId = currentUser.getUid();
        mDatabase.child("patients").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                patientsContainer.removeAllViews();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String patientId = snapshot.getKey();
                    loadPatientDetails(patientId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HealthcareProvider_ProfileActivity.this, "Failed to load patients.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPatientDetails(String patientId) {
        mDatabase.getParent().child(patientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot patientSnapshot) {
                String firstName = patientSnapshot.child("firstName").getValue(String.class);
                String lastName = patientSnapshot.child("lastName").getValue(String.class);
                if (firstName != null && lastName != null) {
                    String fullName = firstName + " " + lastName;
                    addPatientView(fullName, patientId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HealthcareProvider_ProfileActivity.this, "Failed to load patient details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addPatientView(String fullName, String patientId) {
        LinearLayout patientLayout = new LinearLayout(this);
        patientLayout.setOrientation(LinearLayout.HORIZONTAL);
        patientLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        patientLayout.setPadding(16, 16, 16, 16);

        TextView patientNameTextView = new TextView(this);
        patientNameTextView.setText(fullName);
        patientNameTextView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.0f
        ));

        TextView unlinkTextView = new TextView(this);
        unlinkTextView.setText("Unlink");
        unlinkTextView.setTextColor(Color.RED);
        unlinkTextView.setPadding(16, 0, 16, 0);
        unlinkTextView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        unlinkTextView.setOnClickListener(v -> unlinkPatient(patientId));

        patientLayout.addView(patientNameTextView);
        patientLayout.addView(unlinkTextView);

        patientsContainer.addView(patientLayout);
    }

    private void generateAndSaveCode() {
        generatedCode = generate6DigitCode();
        mDatabase.child("linkCode").setValue(generatedCode)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Code generated successfully!", Toast.LENGTH_SHORT).show();
                    viewCodeTextView.setText("Your code: " + generatedCode);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to generate code.", Toast.LENGTH_SHORT).show());
    }

    private String generate6DigitCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        return code.toString();
    }

    private void loadExistingCode() {
        mDatabase.child("linkCode").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    generatedCode = snapshot.getValue(String.class);
                    viewCodeTextView.setText("Your code: " + generatedCode);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HealthcareProvider_ProfileActivity.this, "Failed to load code.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unlinkPatient(String patientId) {
        new AlertDialog.Builder(this)
                .setTitle("Unlink Patient")
                .setMessage("Are you sure you want to unlink this patient?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    String doctorId = mAuth.getCurrentUser().getUid();
                    DatabaseReference doctorRef = mDatabase.child("patients").child(patientId);
                    DatabaseReference patientRef = mDatabase.getParent().child(patientId).child("linkedDoctorId");

                    // Remove patient from doctor's list
                    doctorRef.removeValue().addOnSuccessListener(aVoid -> {
                        // Remove doctor reference from patient's profile
                        patientRef.removeValue().addOnSuccessListener(aVoid1 -> {
                            Toast.makeText(HealthcareProvider_ProfileActivity.this, "Patient unlinked successfully!", Toast.LENGTH_SHORT).show();
                            // Remove any existing UI to avoid duplication, then reload
                            patientsContainer.removeAllViews();
                            loadPatientData();
                        }).addOnFailureListener(e -> Toast.makeText(HealthcareProvider_ProfileActivity.this, "Failed to unlink patient from patient's profile.", Toast.LENGTH_SHORT).show());
                    }).addOnFailureListener(e -> Toast.makeText(HealthcareProvider_ProfileActivity.this, "Failed to unlink patient from doctor's profile.", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
