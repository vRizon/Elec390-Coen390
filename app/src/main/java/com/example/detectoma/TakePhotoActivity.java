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

        // Initialize back button functionality
        ImageView backIcon = findViewById(R.id.backIcon);
        backIcon.setOnClickListener(v -> finish());

        // Initialize Save Photo button
        Button savePhotoButton = findViewById(R.id.savePhotoButton);

        // Initialize tutorial GIF
        ImageView tutorialGif = findViewById(R.id.tutorialGif);
        Glide.with(this)
                .load(R.drawable.takephoto) // Replace with the name of your GIF file in `res/drawable`
                .into(tutorialGif);

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
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Confirmation")
                    .setMessage("Are you sure you want to save this photo?")
                    .setPositiveButton("Yes", (dialogInterface, which) -> {
                        saveImageToDevice();
                        Toast.makeText(this, "Photo saved successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .setNegativeButton("No", (dialogInterface, which) -> dialogInterface.dismiss())
                    .create();

            dialog.setOnShowListener(dialogInterface -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.darkGreen));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.darkGreen));
            });

            dialog.show();
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
        if (imageRef == null) {
            Toast.makeText(this, "No image reference available", Toast.LENGTH_SHORT).show();
            return;
        }

        imageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            try {
                // Save the image bytes to internal storage
                String filename = "image.jpg";
                FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);
                fos.write(bytes);
                fos.close();

                Log.d("TakePhotoActivity", "Image downloaded and saved successfully to device storage.");
                Toast.makeText(this, "Photo saved successfully", Toast.LENGTH_SHORT).show();

                setResult(RESULT_OK);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to save image.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            e.printStackTrace();
            Toast.makeText(this, "Failed to download image from Firebase.", Toast.LENGTH_SHORT).show();
        });
    }

    private void startImageAutoRefresh() {
        imageUpdateTask = new Runnable() {
            @Override
            public void run() {
                loadUserImage();
                handler.postDelayed(this, REFRESH_INTERVAL);
            }
        };
        handler.postDelayed(imageUpdateTask, REFRESH_INTERVAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && imageUpdateTask != null) {
            handler.removeCallbacks(imageUpdateTask);
        }
    }
}
