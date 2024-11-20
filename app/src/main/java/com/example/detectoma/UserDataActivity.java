package com.example.detectoma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserDataActivity extends AppCompatActivity {

    private Switch asymmetrySwitch, borderSwitch, colorSwitch, diameterSwitch, evolvingSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data);

        // Initialize UI elements
        asymmetrySwitch = findViewById(R.id.asymmetrySwitch);
        borderSwitch = findViewById(R.id.borderSwitch);
        colorSwitch = findViewById(R.id.colorSwitch);
        diameterSwitch = findViewById(R.id.diameterSwitch);
        evolvingSwitch = findViewById(R.id.evolvingSwitch);
        Button submitButton = findViewById(R.id.submitButton);

        // Set up the submit button click listener
        submitButton.setOnClickListener(v -> submitData());
    }

    private void submitData() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to submit?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Get switch values
                    boolean asymmetry = asymmetrySwitch.isChecked();
                    boolean border = borderSwitch.isChecked();
                    boolean color = colorSwitch.isChecked();
                    boolean diameter = diameterSwitch.isChecked();
                    boolean evolving = evolvingSwitch.isChecked();

                    // Get current user ID and Firebase reference
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("profiles").child(uid).child("screenings");

                    // Get the latest timestamp
                    long timestamp = System.currentTimeMillis();
                    String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(timestamp));
                    DatabaseReference timestampRef = databaseRef.child(formattedDate);

                    // Save switch data under the timestamp
                    timestampRef.child("asymmetry").setValue(asymmetry);
                    timestampRef.child("border").setValue(border);
                    timestampRef.child("color").setValue(color);
                    timestampRef.child("diameter").setValue(diameter);
                    timestampRef.child("evolving").setValue(evolving)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(UserDataActivity.this, "User data saved successfully!", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish(); // Close the activity
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(UserDataActivity.this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // User declined, stay on the same page
                    dialog.dismiss();
                })
                .show();
    }

}
