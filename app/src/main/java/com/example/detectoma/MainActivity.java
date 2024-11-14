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

        loginButton.setOnClickListener(v -> loginUser());
        signUpButton.setOnClickListener(v -> navigateToSignUp());
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
                if (user != null && user.getEmail().equals("val.nikandrova2000@gmail.com")) {
                    // If the logged-in user is the doctor, navigate to HealthcareProviderProfileActivity
                    Intent intent = new Intent(MainActivity.this, HealthcareProvider_ProfileActivity.class);
                    startActivity(intent);
                    finish();
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                } else {
                    // If the logged-in user is not the doctor, navigate to HomeActivity or handle as necessary
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
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

    private void navigateToSignUp() {
        Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
        startActivity(intent);
    }
}
