package com.example.detectoma;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import java.util.Random;

public class HealthcareProvider_ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Button generateCodeButton, addPatientButton;
    private TextView viewCodeTextView,patientName;
    private String generatedCode;
//    private LinearLayout patientsContainer;
    private ValueEventListener patientsListener;

//Recycler View
    private RecyclerView patientsRecyclerView;
    private PatientAdapter adapter;
    private List<Patient> patientsList;




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


    @Override
    protected void onStart() {
        super.onStart();
        loadPatientData();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (patientsListener != null) {
            mDatabase.child("patients").removeEventListener(patientsListener);
        }
    }

    private void initializeViews() {


        patientName = findViewById(R.id.patientName);

        generateCodeButton = findViewById(R.id.generateCodeButton);
        viewCodeTextView = findViewById(R.id.viewCodeTextView);
//        patientsContainer = findViewById(R.id.patientsContainer);
        ImageView logoutIcon = findViewById(R.id.logoutIcon);
        logoutIcon.setOnClickListener(v -> logOutUser());


        patientsRecyclerView = findViewById(R.id.patientsRecyclerView);
        patientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        patientsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        patientsList = new ArrayList<>();
        adapter = new PatientAdapter(patientsList, patient -> unlinkPatient(patient.getId()));
        patientsRecyclerView.setAdapter(adapter);
    }


    private void logOutUser() {
        // Log out from Firebase Auth
        FirebaseAuth.getInstance().signOut();

        // Redirect to LoginActivity or the main entry point
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(HealthcareProvider_ProfileActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    private void loadPatientData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No user is signed in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String doctorId = currentUser.getUid();

        patientsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                patientsList.clear(); // Clear the list before adding new data

                List<String> patientIds = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String patientId = snapshot.getKey();
                    patientIds.add(patientId);
                }

                if (patientIds.isEmpty()) {
                    adapter.notifyDataSetChanged();
                    return;
                }

                loadPatientDetails(patientIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HealthcareProvider_ProfileActivity.this, "Failed to load patients.", Toast.LENGTH_SHORT).show();
            }
        };

        mDatabase.child("patients").addValueEventListener(patientsListener);

//        mDatabase.child("patients").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
////                patientsContainer.removeAllViews();
//                patientsList.clear();
//                List<String> patientIds = new ArrayList<>();
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    String patientId = snapshot.getKey();
//                    patientIds.add(patientId);
//                }
//                if (patientIds.isEmpty()) {
//                    adapter.notifyDataSetChanged();
//                    return;
//                }
//
//                loadPatientDetails(patientIds);
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Toast.makeText(HealthcareProvider_ProfileActivity.this, "Failed to load patients.", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    private void loadPatientDetails(List<String> patientIds) {

        // Fetch all patients' details at once
        DatabaseReference profilesRef = FirebaseDatabase.getInstance().getReference("profiles");
        profilesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot profilesSnapshot) {
                // Clear the list again to ensure no duplicates in case of rapid data changes
                patientsList.clear();
                for (String patientId : patientIds) {
                    DataSnapshot patientSnapshot = profilesSnapshot.child(patientId);
                    String firstName = patientSnapshot.child("firstName").getValue(String.class);
                    String lastName = patientSnapshot.child("lastName").getValue(String.class);

                    // Check for null values
                    if (firstName == null) {
                        firstName = "Unknown";
                    }
                    if (lastName == null) {
                        lastName = "Patient";
                    }

                    String fullName = firstName + " " + lastName;
                    patientsList.add(new Patient(patientId, fullName));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HealthcareProvider_ProfileActivity.this, "Failed to load patient details.", Toast.LENGTH_SHORT).show();
            }
        });

//        mDatabase.getParent().child(patientId).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot patientSnapshot) {
//                String firstName = patientSnapshot.child("firstName").getValue(String.class);
//                String lastName = patientSnapshot.child("lastName").getValue(String.class);
//
//                // Check for null values
//                if (firstName == null) {
//                    firstName = "Unknown";
//                }
//                if (lastName == null) {
//                    lastName = "Patient";
//                }
//
//                Log.d("NAME",firstName);
//                Log.d("NAME",lastName);
//
//                String fullName = firstName + " " + lastName;
//
//                Log.d("PatientData", "Loaded: " + fullName); // Now, fullName won't be null
//
//                patientsList.add(new Patient(patientId, fullName));
//                adapter.notifyDataSetChanged();


            }


//    private void loadPatientDetails(String patientId) {
//        mDatabase.getParent().child(patientId).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
////            public void onDataChange(@NonNull DataSnapshot patientSnapshot) {
////                String firstName = patientSnapshot.child("firstName").getValue(String.class);
////                String lastName = patientSnapshot.child("lastName").getValue(String.class);
////                if (firstName != null && lastName != null) {
////                    String fullName = firstName + " " + lastName;
////                    addPatientView(fullName, patientId);
////                }
////            }
//
//            public void onDataChange(@NonNull DataSnapshot patientSnapshot) {
//                String firstName = patientSnapshot.child("firstName").getValue(String.class);
//                String lastName = patientSnapshot.child("lastName").getValue(String.class);
//                if (firstName != null && lastName != null) {
//                    String fullName = firstName + " " + lastName;
//                    patientsList.add(new Patient(patientId, fullName));
//                    adapter.notifyDataSetChanged();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(HealthcareProvider_ProfileActivity.this, "Failed to load patient details.", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

//    private void addPatientView(String fullName, String patientId) {
//        // Inflate the custom layout
//        View patientView = getLayoutInflater().inflate(R.layout.patient_item, patientsContainer, false);
//
//        // Get references to the UI elements in the custom layout
//        TextView patientNameTextView = ((View) patientView).findViewById(R.id.patientName);
//        Button unlinkButton = patientView.findViewById(R.id.unlinkButton);
//
//        // Set patient details
//        patientNameTextView.setText(fullName);
//
//        // Handle unlink button click
//        unlinkButton.setOnClickListener(v -> unlinkPatient(patientId));
//
//        // Add the view to the container
//        patientsContainer.addView(patientView);
//    }





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

                    // Disable the button if a code already exists
                    disableGenerateCodeButton();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HealthcareProvider_ProfileActivity.this, "Failed to load code.", Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void unlinkPatient(String patientId) {
//        AlertDialog dialog = new AlertDialog.Builder(this)
//                .setTitle("Unlink Patient")
//                .setMessage("Are you sure you want to unlink this patient?")
//                .setPositiveButton("Yes", (dialogInterface, which) -> {
//                    String doctorId = mAuth.getCurrentUser().getUid();
//                    DatabaseReference doctorRef = mDatabase.child("patients").child(patientId);
//                    DatabaseReference patientRef = mDatabase.getParent().child(patientId).child("linkedDoctorId");
//
//                    doctorRef.removeValue().addOnSuccessListener(aVoid -> {
//                        patientRef.removeValue().addOnSuccessListener(aVoid1 -> {
//                            Toast.makeText(HealthcareProvider_ProfileActivity.this, "Patient unlinked successfully!", Toast.LENGTH_SHORT).show();
//                            patientsContainer.removeAllViews();
//                            loadPatientData();
//                        }).addOnFailureListener(e -> Toast.makeText(HealthcareProvider_ProfileActivity.this, "Failed to unlink patient from patient's profile.", Toast.LENGTH_SHORT).show());
//                    }).addOnFailureListener(e -> Toast.makeText(HealthcareProvider_ProfileActivity.this, "Failed to unlink patient from doctor's profile.", Toast.LENGTH_SHORT).show());
//                })
//                .setNegativeButton("No", (dialogInterface, which) -> dialogInterface.dismiss())
//                .create();
//
//        dialog.setOnShowListener(dialogInterface -> {
//            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.darkGreen));
//            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.darkGreen));
//        });
//
//        dialog.show();
//    }

    private void unlinkPatient(String patientId) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Unlink Patient")
                .setMessage("Are you sure you want to unlink this patient?")
                .setPositiveButton("Yes", (dialogInterface, which) -> {
                    String doctorId = mAuth.getCurrentUser().getUid();
//                    DatabaseReference doctorRef = mDatabase.child("patients").child(patientId);
//                    DatabaseReference patientRef = mDatabase.getParent().child(patientId).child("linkedDoctorId");

                    DatabaseReference doctorRef = FirebaseDatabase.getInstance()
                            .getReference("profiles")
                            .child(doctorId)
                            .child("patients")
                            .child(patientId);

                    DatabaseReference patientRef = FirebaseDatabase.getInstance()
                            .getReference("profiles")
                            .child(patientId)
                            .child("linkedDoctorId");

                    doctorRef.removeValue().addOnSuccessListener(aVoid -> {
                        patientRef.removeValue().addOnSuccessListener(aVoid1 -> {
                            Toast.makeText(HealthcareProvider_ProfileActivity.this, "Patient unlinked successfully!", Toast.LENGTH_SHORT).show();
//                            loadPatientData();
                        }).addOnFailureListener(e -> Toast.makeText(HealthcareProvider_ProfileActivity.this, "Failed to unlink patient from patient's profile.", Toast.LENGTH_SHORT).show());
                    }).addOnFailureListener(e -> Toast.makeText(HealthcareProvider_ProfileActivity.this, "Failed to unlink patient from doctor's profile.", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("No", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.darkGreen));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.darkGreen));
        });

//        adapter.notifyDataSetChanged();

        dialog.show();
    }

    private void disableGenerateCodeButton() {
        generateCodeButton.setEnabled(false); //Disable btn
        generateCodeButton.setAlpha(0.5f);   //change color
    }


}
//    private void addPatientView(String fullName, String patientId) {
//        LinearLayout patientLayout = new LinearLayout(this);
//        patientLayout.setOrientation(LinearLayout.HORIZONTAL);
//        patientLayout.setLayoutParams(new LinearLayout.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT
//        ));
//        patientLayout.setPadding(16, 16, 16, 16);
//
//        TextView patientNameTextView = new TextView(this);
//        patientNameTextView.setText(fullName);
//        patientNameTextView.setLayoutParams(new LinearLayout.LayoutParams(
//                0,
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                1.0f
//        ));
//
//        TextView unlinkTextView = new TextView(this);
//        unlinkTextView.setText("Unlink");
//        unlinkTextView.setTextColor(Color.RED);
//        unlinkTextView.setPadding(16, 0, 16, 0);
//        unlinkTextView.setLayoutParams(new LinearLayout.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT
//        ));
//        unlinkTextView.setOnClickListener(v -> unlinkPatient(patientId));
//
//        patientLayout.addView(patientNameTextView);
//        patientLayout.addView(unlinkTextView);
//
//        patientsContainer.addView(patientLayout);
//    }
