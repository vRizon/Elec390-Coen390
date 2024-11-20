package com.example.detectoma;


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

        findViewById(R.id.btnSignUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });


        //
//        dateOfBirth = findViewById(R.id.dateOfBirth);
//        dateOfBirth.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showDatePickerDialog();
//            }
//        });


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
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userUID = user.getUid();
                            saveUserProfile(userUID, firstName, lastName, dob);
                            sendUserUIDToDatabase(user.getUid());
                            navigateToHome();
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserProfile(String userUID, String firstName, String lastName, String dob) {
        UserProfile userProfile = new UserProfile(firstName, lastName, dob);

        // Save user profile information in Firebase Realtime Database under "profiles/userUID"
        databaseReference.child(userUID).setValue(userProfile)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUpActivity.this, "User profile created successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SignUpActivity.this, "Failed to create user profile", Toast.LENGTH_SHORT).show();
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
        finish();  // Close the sign-up activity
    }
}
