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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.modeldownloader.CustomModel;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;

import java.io.File;

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
        imageView = findViewById(R.id.imageView_image);

        // Load and display the image from Firebase Storage
        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this)
                    .load(uri)
                    .into(imageView);
        }).addOnFailureListener(exception -> {
            Log.e("Firebase Storage", "Error fetching image", exception);
        });

        // Download and initialize the custom model
        CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
                .requireWifi()
                .build();

        FirebaseModelDownloader.getInstance()
                .getModel("MelanomaDetector", DownloadType.LOCAL_MODEL, conditions)
                .addOnSuccessListener(this::initializeCustomModel)
                .addOnFailureListener(e -> {
                    Log.e("ModelDownload", "Failed to download model", e);
                });
    }

    private void initializeCustomModel(CustomModel model) {
        File modelFile = model.getFile();
        if (modelFile != null) {
            LocalModel localModel = new LocalModel.Builder()
                    .setAbsoluteFilePath(modelFile.getAbsolutePath())
                    .build();

            CustomImageLabelerOptions customImageLabelerOptions =
                    new CustomImageLabelerOptions.Builder(localModel)
                            .setConfidenceThreshold(0.5f)
                            .setMaxResultCount(5)
                            .build();

            ImageLabeler labeler = ImageLabeling.getClient(customImageLabelerOptions);

            // Proceed to download and process the image
            downloadAndProcessImage(labeler);
        } else {
            Log.e("ModelInitialization", "Model file is null");
        }
    }

    private void downloadAndProcessImage(ImageLabeler labeler) {
        final long ONE_MEGABYTE = 5 * 1024 * 1024;
        storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            InputImage image = InputImage.fromBitmap(bitmap, 0);

            // Process the image with the custom labeler
            processImageWithCustomLabeler(labeler, image);
        }).addOnFailureListener(exception -> {
            Log.e("Firebase Storage", "Error downloading image", exception);
        });
    }

    private void processImageWithCustomLabeler(ImageLabeler labeler, InputImage image) {
        labeler.process(image)
                .addOnSuccessListener(labels -> {
                    StringBuilder labelsText = new StringBuilder();
                    for (ImageLabel label : labels) {
                        String text = label.getText();
                        float confidence = label.getConfidence();
                        labelsText.append("Label: ").append(text)
                                .append(", Confidence: ").append(confidence)
                                .append("\n");
                        Log.d("Custom Model Labeling", "Label: " + text + ", Confidence: " + confidence);
                    }
                    // Update UI with the labels
                    runOnUiThread(() -> Label.setText(labelsText.toString()));
                })
                .addOnFailureListener(e -> {
                    Log.e("Custom Model Labeling", "Error processing image", e);
                });
    }
}
