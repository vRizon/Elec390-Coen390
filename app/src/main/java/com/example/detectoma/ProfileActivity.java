package com.example.detectoma;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    private EditText linkCodeEditText;
    private Button linkToDoctorButton;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private RecyclerView pastScreeningsRecyclerView;
    private ScreeningAdapter adapter;
    private List<Screening> screeningList = new ArrayList<>();

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

        // Initialize UI components
        linkCodeEditText = findViewById(R.id.linkCodeEditText);
        linkToDoctorButton = findViewById(R.id.linkToDoctorButton);
        ImageView logoutIcon = findViewById(R.id.logoutIcon); // Logout icon
        TextView greetingText = findViewById(R.id.greetingText);
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Fetch current user's name and update greetingText
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = databaseReference.child("profiles").child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserProfile userProfile = snapshot.getValue(UserProfile.class);
                    if (userProfile != null) {
                        String firstName = userProfile.getFirstName();
                        greetingText.setText("Hello " + firstName);
                    } else {
                        greetingText.setText("Hello");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
                }
            });
            // Check if the user is already linked to a doctor
            userRef.child("linkedDoctorId").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // If linked, disable the button and edit text
                        setComponentsDisabled();
                        Toast.makeText(ProfileActivity.this, "Already linked to a healthcare provider.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "Failed to check link status.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            greetingText.setText("Hello");
        }

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

        // Get the screening object from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(ScreeningActivity.SHARED_PREFS, MODE_PRIVATE);
        String screeningJson = sharedPreferences.getString("lastScreening", null);

        if (screeningJson != null) {
            Gson gson = new Gson();
            Screening screening = gson.fromJson(screeningJson, Screening.class);

            screeningList.add(screening);

//            // Set the data to the views
//            screeningDate.setText(screening.getTimestamp());
//            temperature.setText("Temperature: " + screening.getTemperature());
//            distances.setText("Distance 1: " + screening.getDistance1() + " Distance 2: " + screening.getDistance2());

            // Load image from Firebase Storage or use a placeholder if needed
//            String timestamp = screening.getTimestamp();
//            storageReference = FirebaseStorage.getInstance().getReference().child("screening_images").child(timestamp + ".jpg");
//            loadScreeningImage();
        } else {
            Toast.makeText(this, "No screening data available.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initialize RecyclerView
        pastScreeningsRecyclerView = findViewById(R.id.pastScreeningsRecyclerView);
        pastScreeningsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pastScreeningsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new ScreeningAdapter(screeningList, this);
        pastScreeningsRecyclerView.setAdapter(adapter);

        // Load screenings from Firebase
        loadScreeningsFromFirebase();
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
    private void setComponentsDisabled() {
        // Disable the button and EditText
        linkToDoctorButton.setEnabled(false);
        linkToDoctorButton.setAlpha(0.5f);
        linkCodeEditText.setEnabled(false);
        linkCodeEditText.setAlpha(0.5f);


    }
    private void loadScreeningsFromFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference screeningsRef = FirebaseDatabase.getInstance().getReference()
                    .child("profiles").child(userId).child("screenings");

            screeningsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    screeningList.clear();
                    for (DataSnapshot screeningSnapshot : snapshot.getChildren()) {
                        Screening screening = screeningSnapshot.getValue(Screening.class);
                        if (screening != null){
                        screeningList.add(screening);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "Failed to load screenings.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
