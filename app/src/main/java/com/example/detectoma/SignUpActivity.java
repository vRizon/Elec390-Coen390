package com.example.detectoma;


import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;

import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    private EditText firstNameEditText, lastNameEditText, dobEditText, emailEditText, passwordEditText;


    Button dateOfBirth;

    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("profiles");

        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        dobEditText = findViewById(R.id.dobEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        // Initialize the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_signup);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        // Handle the back button functionality
        toolbar.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.btnSignUp).setOnClickListener(v -> registerUser());
    }



    //show date Picker Dialog
    public void showDatePickerDialog(){

        DialogFragment newFragment = new DatePickerDialog();

        newFragment.show(getSupportFragmentManager(),"DatePicker");

    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String dob = dobEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || dob.isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user with email and password in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign-up successful
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Send verification email
                            user.sendEmailVerification()
                                    .addOnCompleteListener(verificationTask -> {
                                        if (verificationTask.isSuccessful()) {
                                            // Email sent
                                            Toast.makeText(SignUpActivity.this,
                                                    "Verification email sent to " + user.getEmail(),
                                                    Toast.LENGTH_SHORT).show();

                                            // Save user profile information
                                            String userUID = user.getUid();
                                            saveUserProfile(userUID, firstName, lastName, dob);

                                            // Sign out the user to prevent access before verification
                                            mAuth.signOut();

                                            // Redirect to login or appropriate activity
                                            navigateToLogin();
                                        } else {
                                            // Email not sent
                                            Toast.makeText(SignUpActivity.this,
                                                    "Failed to send verification email.",
                                                    Toast.LENGTH_SHORT).show();
                                            Log.e(TAG, "sendEmailVerification", verificationTask.getException());
                                        }
                                    });
                        }
                    } else {
                        // Sign-up failed
                        Toast.makeText(SignUpActivity.this,
                                "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "createUserWithEmail:failure", task.getException());
                    }
                });

    }

    private void navigateToLogin() {
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveUserProfile(String userUID, String firstName, String lastName, String dob) {
        UserProfile userProfile = new UserProfile(firstName, lastName, dob);

        // Save user profile information in Firebase Realtime Database under "profiles/userUID"
        databaseReference.child(userUID).setValue(userProfile)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUpActivity.this, "User profile created successfully", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "User profile created successfully");
                    } else {
                        Toast.makeText(SignUpActivity.this, "Failed to create user profile", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to create user profile", task.getException());
                    }
                });
    }
    private void sendUserUIDToDatabase(String uid) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("temp").setValue(uid)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("DatabaseUpdate", "UID successfully written to Firebase Database");
                    } else {
                        Log.e("DatabaseUpdate", "Failed to write UID to Firebase Database", task.getException());
                    }
                });
    }

    private void navigateToHome() {
        Intent intent = new Intent(SignUpActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }
}
