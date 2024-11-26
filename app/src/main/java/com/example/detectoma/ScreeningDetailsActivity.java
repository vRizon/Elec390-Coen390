package com.example.detectoma;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ScreeningDetailsActivity extends AppCompatActivity {
    private TextView screeningDate;
    private TextView temperature;
    private TextView distances;
    private ImageView screeningImage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screening_details);

        // Initialize UI components
        screeningDate = findViewById(R.id.screeningDate);
        temperature = findViewById(R.id.temperature);
        distances = findViewById(R.id.distances);
        screeningImage = findViewById(R.id.screeningImage);

        // Get the data passed from the intent
        String timestamp = getIntent().getStringExtra("timestamp");
        String temperatureValue = getIntent().getStringExtra("temperature");
        String distance1 = getIntent().getStringExtra("distance1");
        String distance2 = getIntent().getStringExtra("distance2");

        if (timestamp == null) {
            Toast.makeText(this, "No screening data available.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set the data to the views
        screeningDate.setText(timestamp);
        temperature.setText("Temperature: " + temperatureValue);
        distances.setText("Distance 1: " + distance1 + " Distance 2: " + distance2);

        // Set up Firebase Storage reference for the image
        storageReference = FirebaseStorage.getInstance().getReference().child("screening_images").child(timestamp + ".jpg");

        // Load image from Firebase Storage using custom method
        loadScreeningImage();
    }

    private void loadScreeningImage() {
        Task<Void> downloadTask = downloadImageAndDisplay();
        downloadTask.addOnFailureListener(e -> {
            Toast.makeText(ScreeningDetailsActivity.this, "Failed to load image.", Toast.LENGTH_SHORT).show();
        });
    }

    private Task<Void> downloadImageAndDisplay() {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        try {
            FileOutputStream fos = openFileOutput("temp_image.jpg", MODE_PRIVATE);

            storageReference.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
                try {
                    fos.write(bytes);
                    fos.close();

                    FileInputStream fis = openFileInput("temp_image.jpg");
                    screeningImage.setImageBitmap(android.graphics.BitmapFactory.decodeStream(fis));
                    fis.close();

                    taskCompletionSource.setResult(null);
                } catch (IOException e) {
                    e.printStackTrace();
                    taskCompletionSource.setException(e);
                }
            }).addOnFailureListener(e -> {
                e.printStackTrace();
                taskCompletionSource.setException(e);
            });
        } catch (IOException e) {
            e.printStackTrace();
            taskCompletionSource.setException(e);
        }

        return taskCompletionSource.getTask();
    }
}
