// Showing the trial demo

package com.example.detectoma;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button signUpButton;
    private Button forgotPassword;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference();

        //sending signed in userUID to RTDB
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            sendUserUIDToDatabase(currentUser.getUid());
        }

        usernameEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signUpButton);
        forgotPassword = findViewById(R.id.forgotPassword);

        loginButton.setOnClickListener(v -> loginUser());
        signUpButton.setOnClickListener(v -> navigateToSignUp());
        forgotPassword.setOnClickListener(v -> resetPassword());

    }

    private void loginUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();


        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter your username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!username.matches("[a-zA-Z0-9@._-]+")) {
            Toast.makeText(this, "Username can only contain letters and numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if email exists in Firebase Authentication
        mAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null && user.getEmail().equals("doctor@detectoma.com")) {
                    // If the logged-in user is the doctor, navigate to HealthcareProviderProfileActivity
                    Intent intent = new Intent(MainActivity.this, HealthcareProvider_ProfileActivity.class);
                    startActivity(intent);
                    finish();
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                } else {
                    // If the logged-in user is not the doctor, navigate to HomeActivity or handle as necessary
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    finish();
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Login failed, show error message
                Toast.makeText(MainActivity.this, "Authentication failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void sendUserUIDToDatabase(String uid){
        databaseReference.child("temp").setValue(uid)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Log.d("DatabaseUpdate", "UID successfully written to Firebase Database");
                    } else {
                        Log.e("DatabaseUpdate", "Failed to write UID to Firebase Database", task.getException());
                    }

                });
    }

    private void resetPassword() {
        String email = usernameEditText.getText().toString().trim();

        if (email.isEmpty()) {
            // Prompt the user to enter their email
            usernameEditText.setError("Please enter your registered email");
            usernameEditText.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // Check if the email format is valid
            usernameEditText.setError("Please enter a valid email address");
            usernameEditText.requestFocus();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Password reset email sent
                        Toast.makeText(MainActivity.this,
                                "Password reset email sent. Please check your email.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        // Failed to send reset email
                        Toast.makeText(MainActivity.this,
                                "Failed to send password reset email. Please try again.",
                                Toast.LENGTH_LONG).show();
                        Log.e("ResetPassword", "Error: " + task.getException().getMessage());
                    }
                });
    }


    private void navigateToSignUp() {
        Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
        startActivity(intent);
    }
}
