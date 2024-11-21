package com.example.detectoma;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class TakePhotoActivity extends AppCompatActivity {

    private ImageView imageView;
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);

        imageView = findViewById(R.id.imageView_takePhoto);
        imageView.setVisibility(View.GONE); // or View.INVISIBLE

        // Initialize the Upload Photo button
        Button uploadPhotoButton = findViewById(R.id.uploadPhotoButton);

        // Get the current user's UID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            Log.d("CurrentUser UID: ",uid);
            loadUserImage(uid);
        } else {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
        }

        // Set up click listener for the Upload Photo button
        uploadPhotoButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to upload this photo?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Simulate photo upload process
                    Toast.makeText(this, "Photo uploaded successfully", Toast.LENGTH_SHORT).show();

                    // Set result to indicate successful completion
                    setResult(RESULT_OK);

                    // Finish the activity and return to ScreeningActivity
//                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
        });
    }

    private void loadUserImage(String uid) {
        // Reference to the user's image in Firebase Storage
        StorageReference imageRef = storage.getReference("/Patients/" + uid + "/photo.jpg");

        // Load image using Glide
        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this).load(uri).into(imageView);
            // Make the ImageView visible
            imageView.setVisibility(View.VISIBLE);
        }).addOnFailureListener(exception -> {
                Log.e("Firebase Storage", "Error fetching image", exception);
                // Display a toast message to the user
                Toast.makeText(this, "Photo not available", Toast.LENGTH_SHORT).show();
        });
    }
}
