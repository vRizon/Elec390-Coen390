package com.example.detectoma;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
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

import java.io.FileOutputStream;

public class TakePhotoActivity extends AppCompatActivity {

    private ImageView imageView;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private Handler handler = new Handler();
    private Runnable imageUpdateTask;
    private static final int REFRESH_INTERVAL = 5000; // Refresh every 5 seconds
    private StorageReference imageRef;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);

        imageView = findViewById(R.id.imageView_takePhoto);
        imageView.setVisibility(View.GONE);

        // Initialize the Save Photo button
        Button savePhotoButton = findViewById(R.id.savePhotoButton);

        // Get the current user's UID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            uid = currentUser.getUid();
            Log.d("CurrentUser UID: ", uid);
            setupFirebaseImageReference(uid);
            startImageAutoRefresh();
        } else {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
        }

        // Set up click listener for the Save Photo button
        savePhotoButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirmation")
                    .setMessage("Are you sure you want to save this photo?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        saveImageToDevice(); // Save the image to the device
                        Toast.makeText(this, "Photo saved successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    private void setupFirebaseImageReference(String uid) {
        imageRef = storage.getReference("/Patients/" + uid + "/photo.jpg");
        loadUserImage();
    }

    private void loadUserImage() {
        if (imageRef == null) {
            Toast.makeText(this, "Image reference not available.", Toast.LENGTH_SHORT).show();
            return;
        }

        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this).load(uri).into(imageView);
            imageView.setVisibility(View.VISIBLE);
            Log.d("TakePhotoActivity", "Image updated successfully from Firebase.");
        }).addOnFailureListener(exception -> {
            Log.e("Firebase Storage", "Error fetching image", exception);
            Toast.makeText(this, "Photo not available", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveImageToDevice() {
        if (imageView.getDrawable() == null) {
            Toast.makeText(this, "No image loaded to save", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Get the Bitmap from the ImageView
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();

            // Save the bitmap to internal storage
            String filename = "saved_image.jpg"; // Adjust filename as needed
            FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

            Log.d("TakePhotoActivity", "Image saved successfully to device storage.");
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startImageAutoRefresh() {
        imageUpdateTask = new Runnable() {
            @Override
            public void run() {
                loadUserImage(); // Fetch the image from Firebase
                handler.postDelayed(this, REFRESH_INTERVAL); // Schedule the next refresh
            }
        };
        handler.postDelayed(imageUpdateTask, REFRESH_INTERVAL); // Start the refresh task
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && imageUpdateTask != null) {
            handler.removeCallbacks(imageUpdateTask); // Stop the periodic updates
        }
    }
}
