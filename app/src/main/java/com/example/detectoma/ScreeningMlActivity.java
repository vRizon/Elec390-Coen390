package com.example.detectoma;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.bumptech.glide.Glide;

public class ScreeningMlActivity extends AppCompatActivity {

    TextView Label;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference().child("Test/cat.jpg");
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_screening_ml);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Label = findViewById(R.id.Label);
        // Find your ImageView
        imageView = findViewById(R.id.imageView_image);

        // Get the download URL and load it into the ImageView
        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this)
                    .load(uri)
                    .into(imageView);
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Log.e("Firebase Storage", "Error fetching image", exception);
        });

        // Set the maximum image size you want to download (5 MB here)
        final long ONE_MEGABYTE = 5 * 1024 * 1024;
        storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            // Now, you can use this bitmap with ML Kit
            InputImage image = InputImage.fromBitmap(bitmap, 0);

            processImage(image);
        }).addOnFailureListener(exception -> {
            // Handle any errors here
            Log.e("Firebase Storage", "Error downloading image", exception);
        });



    }

    private void processImage(InputImage image) {
        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

        labeler.process(image)
                .addOnSuccessListener(labels -> {
                    StringBuilder labelsText = new StringBuilder();
                    for (ImageLabel label : labels) {
                        String text = label.getText();
                        float confidence = label.getConfidence();
                        labelsText.append("Label: ").append(text).append(", Confidence: ").append(confidence).append("\n");
                        Log.d("ML Kit Labeling", "Label: " + text + ", Confidence: " + confidence);
                    }
                    // Set all labels to the TextView at once
                    Label.setText(labelsText.toString());
                })
                .addOnFailureListener(e -> {
                    Log.e("ML Kit Labeling", "Error processing image", e);
                });
    }
}
