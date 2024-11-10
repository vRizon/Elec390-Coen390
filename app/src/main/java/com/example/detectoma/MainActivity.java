// Showing the trial demo

package com.example.detectoma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button signUpButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

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

    private void navigateToSignUp() {
        Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
        startActivity(intent);
    }
}
