package com.example.detectoma;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HealthcareProvider_ProfileActivity extends AppCompatActivity {


//    private ListView listPatients;
//    private FirebaseAuth mAuth;
//    private DatabaseReference mDatabase;  // Reference to Firebase Realtime Database
//    private List<String> patientNames;     // List to hold patient names
//    private ArrayAdapter<String> adapter;  // Adapter to display the patient names in the ListView


    private ListView listPatients;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;  // Reference to Firebase Realtime Database
    private List<String> patientNames;     // List to hold patient names
    private ArrayAdapter<String> adapter;  // Adapter to display the patient names in the ListView
    private TextView userNameTextView;     // TextView to display the user's name



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_healthcare_provider_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


//        listPatients = findViewById(R.id.listViewPatients);
//        mAuth = FirebaseAuth.getInstance();
//        mDatabase = FirebaseDatabase.getInstance().getReference("Names");  // Reference to the "Names" node
//        patientNames = new ArrayList<>();  // Initialize the list to hold patient names
//
//        // Adapter for ListView
//        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, patientNames);
//        listPatients.setAdapter(adapter);
//
//        // Load patient data from Firebase Realtime Database
//        loadPatientData();

        listPatients = findViewById(R.id.listViewPatients);
//        userNameTextView = findViewById(R.id.greetingText);  // Reference to the TextView for the user's name
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("profiles");  // Reference to the "profiles" node
        patientNames = new ArrayList<>();  // Initialize the list to hold patient names

        // Adapter for ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, patientNames);
        listPatients.setAdapter(adapter);

        // Load user data and patient data from Firebase Realtime Database
        loadUserName();
        loadPatientData();



    }
//
//    private void loadPatientData() {
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//
//        if (currentUser == null) {
//            Toast.makeText(HealthcareProvider_ProfileActivity.this, "No user is signed in.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Check if the current user is the doctor
//        String doctorEmail = "val.nikandrova2000@gmail.com";  // Doctor's email (hardcoded for demo)
//
//        if (currentUser.getEmail().equals(doctorEmail)) {
//            // Load all patient names from the "Names" node
//            mDatabase.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    // Clear previous data
//                    patientNames.clear();
//
//                    // Loop through the snapshot and add patient names
//                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                        String patientName = snapshot.getValue(String.class);  // Get the patient's name from the value
//                        if (patientName != null) {
//                            patientNames.add(patientName);  // Add to the list
//                        }
//                    }
//
//                    // Notify the adapter that data has changed
//                    adapter.notifyDataSetChanged();
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//                    // Handle database read failure
//                    Toast.makeText(HealthcareProvider_ProfileActivity.this, "Failed to load patients.", Toast.LENGTH_SHORT).show();
//                }
//            });
//        } else {
//            Toast.makeText(HealthcareProvider_ProfileActivity.this, "You are not authorized to view this page.", Toast.LENGTH_SHORT).show();
//        }
//    }



    private void loadUserName() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(HealthcareProvider_ProfileActivity.this, "No user is signed in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();  // Get the current user's UID

        // Fetch the user's name from the "Users" node in the database
        mDatabase.child(userId).child("firstName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                String userName = dataSnapshot.getValue(String.class);  // Get the user's first name
//                if (userName != null) {
//                    userNameTextView.setText("Hello, " + userName);  // Display the user's name
//                } else {
//                    userNameTextView.setText("Hello, User");  // Fallback if name is not found
//                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(HealthcareProvider_ProfileActivity.this, "Failed to load user name.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPatientData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(HealthcareProvider_ProfileActivity.this, "No user is signed in.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the current user is the doctor (you could use an email check or role check)
        String doctorEmail = "val.nikandrova2000@gmail.com";  // Doctor's email (hardcoded for demo)

        if (currentUser.getEmail().equals(doctorEmail)) {
            // Load all patient names (firstName and lastName) from the "profiles" node
            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Clear previous data
                    patientNames.clear();

                    // Loop through the snapshot and add patient full names (firstName and lastName)
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String firstName = snapshot.child("firstName").getValue(String.class);
                        String lastName = snapshot.child("lastName").getValue(String.class);

                        if (firstName != null && lastName != null) {
                            // Combine firstName and lastName
                            String fullName = firstName + " " + lastName;
                            patientNames.add(fullName);  // Add full name to the list
                        }
                    }

                    // Notify the adapter that data has changed
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle database read failure
                    Toast.makeText(HealthcareProvider_ProfileActivity.this, "Failed to load patients.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(HealthcareProvider_ProfileActivity.this, "You are not authorized to view this page.", Toast.LENGTH_SHORT).show();
        }
    }


}