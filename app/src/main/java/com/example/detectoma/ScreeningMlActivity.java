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

import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.google.firebase.ml.modeldownloader.CustomModel;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;
import com.google.firebase.ml.modeldownloader.DownloadType;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.Arrays;

public class ScreeningMlActivity extends AppCompatActivity {

    TextView Label;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference().child("Test/test1.jpg");
    ImageView imageView;

    private Interpreter tflite;
    private final long ONE_MEGABYTE = 5 * 1024 * 1024; // 5 MB

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

        FirebaseApp.initializeApp(this);

        Label = findViewById(R.id.Label);
        imageView = findViewById(R.id.imageView_image);

        // Load the image into the ImageView
        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this)
                    .load(uri)
                    .into(imageView);
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Log.e("Firebase Storage", "Error fetching image", exception);
        });

        // Download and load the model
        downloadAndLoadModel();
    }

    private void downloadAndLoadModel() {
        CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
                .requireWifi()
                .build();

        FirebaseModelDownloader.getInstance()
                .getModel("MelanomaDetector", DownloadType.LOCAL_MODEL, conditions)
                .addOnSuccessListener(model -> {
                    // Model downloaded successfully
                    File modelFile = model.getFile();
                    if (modelFile != null) {
                        // Initialize the interpreter with the downloaded model
                        tflite = new Interpreter(modelFile);

                        // Optionally, log model input and output shapes
                        int[] inputShape = tflite.getInputTensor(0).shape();
                        int[] outputShape = tflite.getOutputTensor(0).shape();
                        Log.d("Model Info", "Input shape: " + Arrays.toString(inputShape));
                        Log.d("Model Info", "Output shape: " + Arrays.toString(outputShape));

                        // Now that the model is ready, process the image
                        processImageFromFirebase();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Model Download", "Failed to download model", e);
                });
    }

    private void processImageFromFirebase() {
        storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            runModel(bitmap);
        }).addOnFailureListener(exception -> {
            // Handle any errors here
            Log.e("Firebase Storage", "Error downloading image", exception);
        });
    }

    private void runModel(Bitmap bitmap) {
        if (tflite == null) {
            Log.e("TFLite", "Interpreter is not initialized");
            return;
        }

        // Preprocess the image
        TensorImage tensorImage = preprocessImage(bitmap);

        // Prepare the output buffer
        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(new int[]{1, 1}, DataType.FLOAT32);

        // Run inference
        tflite.run(tensorImage.getBuffer(), outputBuffer.getBuffer());

        // Get the result
        float prediction = outputBuffer.getFloatArray()[0];
        String result = prediction > 0.5 ? "Positive" : "Negative";

        // Display the result
        Label.setText("Prediction: " + result + " (" + prediction + ")");
    }

    private TensorImage preprocessImage(Bitmap bitmap) {
        // Create a TensorImage object
        TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
        tensorImage.load(bitmap);

        // Define image preprocessing steps
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(256, 256, ResizeOp.ResizeMethod.BILINEAR))
                .add(new NormalizeOp(127.5f, 127.5f)) // Normalize to [-1, 1]
                .build();

        // Preprocess the image
        tensorImage = imageProcessor.process(tensorImage);

        return tensorImage;
    }
}
