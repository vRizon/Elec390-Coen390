package com.example.detectoma;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.CustomModel;
import com.google.firebase.ml.modeldownloader.FirebaseMlException;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


//This acitvity does all the analysis
public class resultsActivity extends AppCompatActivity {

    private static final String TAG = "ScreeningResultsActivity";

    // Views for AI Prediction
    private TextView predictionTextView;
    private ImageView imageView;

    //Just to track the heatmap state
    //Did the same thing in the previous assignents
    private boolean isHeatmapVisible = false;

    //Here we are storing the images
    private Bitmap originalBitmap;
    private Bitmap heatmapBitmap;

    // Temp hraph
    private ImageView temperatureGraphImageView;

    // Distance analysis text
    private TextView distanceAnalysisTextView;

    //Questionnaire Results
    private TextView resultsTextView;
    private TextView recommendationTextView;
    private TextView timestamp_results;

    // Initializing firebase stuff
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef;

    private Interpreter tflite;
    private final long ONE_MEGABYTE = 5 * 1024 * 1024;

    //Here I had to take initial classweights that can be used for the heatmap
    private float[] classWeights;

    //Here we declare the initialized boolean values
    private boolean asymmetry = false;
    private boolean border = false;
    private boolean color = false;
    private boolean diameter = false;
    private boolean evolving = false;
    private float tempdiff = 0;

    //We assume the result as negative
    String result = "Negative";

    // Variables to hold Intent extras
    // These are very important, it is basically how we know what data to pull from firebase
    // I wrote my code this way such that no matter where you access the results activity from
    // As long as you have the formatted date and UID, you can pull the correct data
    private String formattedDate;
    private String uid;

    // Path templates
    // The i_ just indicates that it is an image, and g_ indicates that it is a temp graph
    private String questionnaireResultsPathTemplate = "/profiles/%s/screenings/%s/";
    private String imagePathTemplate = "/Patients/%s/i_%s.jpg";
    private String temperatureGraphPathTemplate = "/Patients/%s/g_%s.jpg";

    // Honestly dont even remember this and why it's here
    private static final float DISTANCE_THRESHOLD = 0.5f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_results);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseApp.initializeApp(this);

        Button toggleHeatmapButton = findViewById(R.id.toggleHeatmapButton);
        toggleHeatmapButton.setOnClickListener(v -> toggleHeatmap());

        ImageView backIcon = findViewById(R.id.backIcon);
        backIcon.setOnClickListener(v -> {
            finish(); // Close the current activity and navigate back
        });

        // Initializing the sensor stuff excluding the questionnare
        imageView = findViewById(R.id.imageView_image);
        temperatureGraphImageView = findViewById(R.id.imageView_temperature_graph);
        distanceAnalysisTextView = findViewById(R.id.distanceAnalysisTextView);

        // Now same just for questionnare
        resultsTextView = findViewById(R.id.resultsTextView);
        recommendationTextView = findViewById(R.id.recommendationTextView);
        timestamp_results = findViewById(R.id.timestamp_results);

        // Basically what I was mentioning earlier, we need the formatted date and UID
        Intent intent = getIntent();
        formattedDate = intent.getStringExtra("FORMATTED_DATE");
        uid = intent.getStringExtra("UID");
        timestamp_results.setText(formattedDate);

        //Just some error checcking
        if (formattedDate == null || uid == null) {
            Log.e(TAG, "FORMATTED_DATE or UID not provided in Intent");
            showErrorToUser("Required data not provided. Please try again.");
            return;
        }

        // Preparing all the AI stuff
        loadClassWeights();
        downloadAndLoadModel();

        // Getting the questionnare results
        fetchQuestionnaireResults();

        // Getting the temp graph
        fetchTemperatureGraphImage();

        Button backToHomeButton = findViewById(R.id.backToHomeButton);
        backToHomeButton.setOnClickListener(v -> navigateToHome());
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, ProfileActivity.class); // Replace with your home activity
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clears the back stack
        startActivity(intent);
        finish(); // Closes the current activity
    }

   //While it is called feth questionnare results, this ended up doing a lottttt more LOL
    private void fetchQuestionnaireResults() {

        //Setting up the references
        String questionnaireResultsPath = String.format(questionnaireResultsPathTemplate, uid, formattedDate);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(questionnaireResultsPath);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                asymmetry = getBooleanValue(dataSnapshot.child("asymmetry"));
                border = getBooleanValue(dataSnapshot.child("border"));
                color = getBooleanValue(dataSnapshot.child("color"));
                diameter = getBooleanValue(dataSnapshot.child("diameter"));
                evolving = getBooleanValue(dataSnapshot.child("evolving"));

                // Getting the temp diff, also fixing whatever issues with it.
                String tempdiffStr = dataSnapshot.child("temperatureDiff").getValue(String.class);
                if (tempdiffStr != null) {
                    try {
                        tempdiff = Float.parseFloat(tempdiffStr);
                    } catch (NumberFormatException e) {
                        tempdiff = 0.0f;
                    }
                }

                // Retrieve distanceArm and distanceSurface and parse to float
                String distanceArmStr = dataSnapshot.child("distanceArm").getValue(String.class);
                String distanceSurfaceStr = dataSnapshot.child("distanceSurface").getValue(String.class);

                float distanceArmValue = 0.0f;
                float distanceSurfaceValue = 0.0f;

                //Same old checks to make sure stuff doesnt break.
                if (distanceArmStr != null) {
                    try {
                        distanceArmValue = Float.parseFloat(distanceArmStr);
                    } catch (NumberFormatException e) {
                        distanceArmValue = 0.0f;
                    }
                }

                if (distanceSurfaceStr != null) {
                    try {
                        distanceSurfaceValue = Float.parseFloat(distanceSurfaceStr);
                    } catch (NumberFormatException e) {
                        distanceSurfaceValue = 0.0f;
                    }
                }

                // Computing the ditsance difference
                float distanceDifference = distanceArmValue - distanceSurfaceValue;
                //Just want to clarify here why it's the abs, basically if somehow there is an error and the
                //uset took the wrong measurements at the wrong time, and the app didnt catch it somehow,
                // it still works
                float absDistanceDifference = Math.abs(distanceDifference);
                String distanceAnalysisText;

                //Adding this distance measurement, it's not with the rest of the values since it shouldnt trigger the
                //"you need to visit a doctor" prompt
                distanceAnalysisText = "Distance to Arm Analysis: \n " + String.format("%.2f", absDistanceDifference) + " cm. \n\nThis value can be used to keep track of mole growth. ";

                distanceAnalysisTextView.setText(distanceAnalysisText);

                // Now analyze and display questionnaire results
                analyzeResults(asymmetry, border, color, diameter, evolving, tempdiff, result);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read questionnaire results", databaseError.toException());
                showErrorToUser("Failed to load questionnaire results.");
            }
        });
    }

    // Helper method to safely retrieve Boolean values
    private boolean getBooleanValue(DataSnapshot dataSnapshot) {
        Object value = dataSnapshot.getValue();
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        } else {
            return false; // Default value if not found or invalid
        }
    }

    //-------------------------------------------------------------------

    // AI STUFF Starting Here

    //-------------------------------------------------------------------

    //This is the download and load model, the code for this is derived from firebase.
    //to see how this works, you can try creating a custom model on firebase.
    // Also a lot of the information was gathered from
    // https://firebase.google.com/docs/ml/android/use-custom-models#java
    // https://blog.tensorflow.org/2020/06/enhance-your-tensorflow-lite-deployment-with-firebase.html
    // These two are official sources for implementing TF lite models in android studio

    //Generic function to download and load the model.
    private void downloadAndLoadModel() {
        Log.d(TAG, "Starting model download...");
        CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
                .requireWifi()
                .build();

        FirebaseModelDownloader.getInstance()
                .getModel("MelanomaCamMap", DownloadType.LOCAL_MODEL, conditions)
                .addOnSuccessListener(model -> {
                    Log.d(TAG, "Model download successful.");
                    // Initialize the interpreter with the downloaded model
                    processModel(model);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to download model", e);
                    if (e instanceof FirebaseMlException) {
                        if (e.getMessage().contains("Model download in bad state")) {
                            Log.d(TAG, "Deleting local model and retrying download...");
                            deleteLocalModel("MelanomaDetectorWithCAM");
                            // Retry downloading the model
                            downloadAndLoadModel();
                        } else {
                            showErrorToUser("Failed to download model. Please try again later.");
                        }
                    } else {
                        showErrorToUser("Failed to download model. Please try again later.");
                    }
                });
    }

    //Processing the AI model for inference.
    private void processModel(CustomModel model) {
        File modelFile = model.getFile();
        if (modelFile != null) {
            try {
                tflite = new Interpreter(modelFile);
                Log.d(TAG, "Interpreter initialized.");
                processImageFromFirebase();
            } catch (Exception e) {
                Log.e(TAG, "Error initializing interpreter", e);
                showErrorToUser("Failed to initialize the model.");
            }
        } else {
            Log.e(TAG, "Model file is null");
            showErrorToUser("Model file is unavailable.");
        }
    }

    //Deleting the local model if and when needed
    //Shouldnt be needed, considered basic implementation though
    private void deleteLocalModel(String modelName) {
        FirebaseModelDownloader.getInstance().deleteDownloadedModel(modelName)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Local model deleted successfully.");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete local model", e);
                });
    }

    //Not specifically AI, but deals with getting the image
    private void processImageFromFirebase() {
        Log.d(TAG, "Starting image processing...");

        // Build the image path
        String imagePath = String.format(imagePathTemplate, uid, formattedDate);

        // Update storage reference to the path of the current user's image
        storageRef = storage.getReference(imagePath);
        Log.d(TAG, "Fetching image from path: " + imagePath);

        // Proceed to fetch the image from Firebase Storage
        storageRef.getBytes(ONE_MEGABYTE)
                .addOnSuccessListener(bytes -> {
                    Log.d(TAG, "Image successfully fetched from Firebase.");

                    // Decode the image bytes into a Bitmap
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    // Display the image in the ImageView
                    imageView.setImageBitmap(bitmap);

                    // Run the model on the fetched image
                    runModel(bitmap);
                })
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Error fetching image from Firebase", exception);
                    showErrorToUser("Failed to fetch image from Firebase. Please try again later.");
                });
    }

    //Basically just running the model
    //Both the run model function and computeCam function extensively use generative AI as they were
    //too complicated for me complete on my own, especially the computeCAM function.
    private void runModel(Bitmap bitmap) {
        Log.d(TAG, "Running model...");
        if (tflite == null) {
            Log.e(TAG, "Interpreter is not initialized");
            showErrorToUser("Model is not loaded.");
            return;
        }

        // Save the original bitmap for toggling later
        originalBitmap = bitmap;

        // Preprocess the image
        TensorImage tensorImage = preprocessImage(bitmap);

        // Prepare input and output buffers
        Map<Integer, Object> outputs = new HashMap<>();

        // Get output shapes
        int[] featureMapShape = tflite.getOutputTensor(0).shape(); // [1,8,8,1280]
        int[] predictionShape = tflite.getOutputTensor(1).shape();  // [1,1]

        Log.d(TAG, "featureMapShape: " + Arrays.toString(featureMapShape));
        Log.d(TAG, "predictionShape: " + Arrays.toString(predictionShape));

        // Validate output shapes
        if (featureMapShape.length != 4 || featureMapShape[0] != 1 ||
                predictionShape.length != 2 || predictionShape[0] != 1) {
            Log.e(TAG, "Unexpected output tensor shapes.");
            showErrorToUser("Unexpected output tensor shapes.");
            return;
        }

        // Create output buffers
        TensorBuffer featureMapBuffer = TensorBuffer.createFixedSize(featureMapShape, DataType.FLOAT32);
        TensorBuffer predictionBuffer = TensorBuffer.createFixedSize(predictionShape, DataType.FLOAT32);

        outputs.put(0, featureMapBuffer.getBuffer());
        outputs.put(1, predictionBuffer.getBuffer());

        try {
            // Run inference
            Object[] inputArray = {tensorImage.getBuffer()};
            tflite.runForMultipleInputsOutputs(inputArray, outputs);

            // Get the prediction from output[1]
            float prediction = predictionBuffer.getFloatArray()[0];
            result = prediction > 0.5 ? "Positive" : "Negative";

            Log.d(TAG, "Prediction: " + prediction + ", Result: " + result);

            // Get the feature maps from output[0]
            float[] featureMaps = featureMapBuffer.getFloatArray();

            // Compute the CAM
            Bitmap camBitmap = computeCAM(featureMaps, classWeights, featureMapShape, bitmap.getWidth(), bitmap.getHeight());

            if (camBitmap != null) {
                // Overlay the CAM on the original image
                heatmapBitmap = overlayHeatmapOnImage(camBitmap, bitmap); // Save heatmap bitmap

                // Display the original image by default
                runOnUiThread(() -> {
                    isHeatmapVisible = false; // Start with the original image
                    imageView.setImageBitmap(originalBitmap); // Set the original image
                });
            } else {
                showErrorToUser("Failed to compute CAM.");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error during model inference", e);
            showErrorToUser("Error during model inference.");
        }
    }
    private Bitmap computeCAM(float[] featureMaps, float[] classWeights, int[] featureMapShape, int outputWidth, int outputHeight) {
        int batchSize = featureMapShape[0]; // Should be 1
        int h = featureMapShape[1];
        int w = featureMapShape[2];
        int c = featureMapShape[3];

        Log.d(TAG, "computeCAM: h=" + h + ", w=" + w + ", c=" + c);
        Log.d(TAG, "computeCAM: classWeights.length=" + classWeights.length);

        if (classWeights.length != c) {
            Log.e(TAG, "Mismatch between classWeights length and feature map channels.");
            showErrorToUser("Error computing CAM: mismatched weights and feature maps.");
            return null;
        }

        // Reshape featureMaps to [h][w][c]
        float[][][] featureMaps3D = new float[h][w][c];
        int index = 0;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                for (int k = 0; k < c; k++) {
                    featureMaps3D[i][j][k] = featureMaps[index++];
                }
            }
        }

        // Compute the weighted sum
        float[][] cam = new float[h][w];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                float value = 0;
                for (int k = 0; k < c; k++) {
                    value += featureMaps3D[i][j][k] * classWeights[k];
                }
                cam[i][j] = value;
            }
        }

        // Apply ReLU and normalize
        float maxVal = Float.NEGATIVE_INFINITY;
        float minVal = Float.POSITIVE_INFINITY;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                cam[i][j] = Math.max(0, cam[i][j]); // ReLU
                if (cam[i][j] > maxVal) maxVal = cam[i][j];
                if (cam[i][j] < minVal) minVal = cam[i][j];
            }
        }
        float range = maxVal - minVal + 1e-5f; // Avoid division by zero
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                cam[i][j] = (cam[i][j] - minVal) / range; // Normalize to [0,1]
            }
        }

        // Create heatmap bitmap
        Bitmap camBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int value = (int) (cam[i][j] * 255);
                // Apply a colormap or use semi-transparent red
                int color = Color.argb(value / 2, 255, 0, 0); // Adjust transparency as needed
                camBitmap.setPixel(j, i, color);
            }
        }

        // Resize CAM to match the original image size
        return Bitmap.createScaledBitmap(camBitmap, outputWidth, outputHeight, true);
    }

    //This is the toggler, same logic as stuff ive done in assignment 1 and 2.
    public void toggleHeatmap() {
        if (heatmapBitmap == null || originalBitmap == null) {
            Toast.makeText(this, "Heatmap or original image not ready.", Toast.LENGTH_SHORT).show();
            return;
        }
        //Original Image
        if (isHeatmapVisible) {
            imageView.setImageBitmap(originalBitmap);
            isHeatmapVisible = false;
            // Image with heat map on top
        } else {
            imageView.setImageBitmap(heatmapBitmap);
            isHeatmapVisible = true;
        }
    }

    //Ok this is preprocessing the image
    //Basically as you can see in the link below (My kaggle training)
    // Note that the kaggle page itself is a fork of open source code
    // https://www.kaggle.com/code/erebusue/melanoma-basic-training
    // We train the model to be able to use images of size 256 x 256 pixels, normalized to [-1,1]
    // so basically I just had to ensure that the inference on the app uses the same values
    private TensorImage preprocessImage(Bitmap bitmap) {
        TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
        tensorImage.load(bitmap);
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(256, 256, ResizeOp.ResizeMethod.BILINEAR))
                .add(new NormalizeOp(127.5f, 127.5f)) // Normalize to [-1, 1]
                .build();
        tensorImage = imageProcessor.process(tensorImage);
        return tensorImage;
    }

    //So I was having a tonnnnn of issues with this originally, I had to train a fresh model on the kaggle
    // https://www.kaggle.com/code/erebusue/melanoma-basic-training
    // to get the class weights.
    private void loadClassWeights() {
        try {
            InputStream is = getAssets().open("class_weights.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            List<Float> weightList = new ArrayList<>();

            int lineNumber = 0; // To track line numbers for debugging

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                lineNumber++;
                if (!line.isEmpty()) {
                    try {
                        float value = Float.parseFloat(line);
                        weightList.add(value);

                        // Log the first 5 weights for verification
                        if (weightList.size() <= 5) {
                            Log.d(TAG, "Weight " + (weightList.size() - 1) + " (Line " + lineNumber + "): " + value);
                        }
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Invalid weight value at line " + lineNumber + ": " + line);
                    }
                }
            }
            reader.close();
            is.close();

            // Convert List<Float> to float[]
            classWeights = new float[weightList.size()];
            for (int i = 0; i < weightList.size(); i++) {
                classWeights[i] = weightList.get(i);
            }

            Log.d(TAG, "Loaded " + classWeights.length + " class weights.");

        } catch (IOException e) {
            Log.e(TAG, "Error loading class weights", e);
            showErrorToUser("Failed to load class weights.");
        }
    }


    //One of the good references for overlaying two images.
    //https://codingtechroom.com/question/how-to-merge-two-bitmaps-in-android-a-step-by-step-guide
    private Bitmap overlayHeatmapOnImage(Bitmap heatmap, Bitmap originalImage) {
        if (heatmap == null) {
            return originalImage;
        }

        Bitmap overlayedImage = Bitmap.createBitmap(originalImage.getWidth(), originalImage.getHeight(), originalImage.getConfig());
        Canvas canvas = new Canvas(overlayedImage);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Draw the original image
        canvas.drawBitmap(originalImage, new Matrix(), paint);

        // Draw the heatmap on top
        canvas.drawBitmap(heatmap, 0, 0, paint);

        return overlayedImage;
    }


//-------------------------------------------------------------------------------------------

    //AI STUFF ENDS HERE

    // --------------------------------------------------------------------------------------

    // Ok here we make the temperature graph feture, really just following the same as the fetch image
    private void fetchTemperatureGraphImage() {
        String temperatureGraphPath = String.format(temperatureGraphPathTemplate, uid, formattedDate);
        StorageReference temperatureGraphRef = storage.getReference(temperatureGraphPath);

        Log.d(TAG, "Fetching temperature graph from path: " + temperatureGraphPath);

        temperatureGraphRef.getBytes(ONE_MEGABYTE)
                .addOnSuccessListener(bytes -> {
                    Log.d(TAG, "Temperature graph image successfully fetched from Firebase.");
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    temperatureGraphImageView.setImageBitmap(bitmap);
                })
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Error fetching temperature graph image from Firebase", exception);
                    showErrorToUser("Failed to fetch temperature graph image from Firebase. Please try again later.");
                });
    }

    //Now for the longest strings of text in my life.
    //Basically this implements our "decision tree"
    //its basically a huge or gate, if any one is true, then the patient needs to seek medical help
    //I also added long texts for each criteria explaining them, this is then shown to the user.
    private void analyzeResults(boolean asymmetry, boolean border, boolean color, boolean diameter, boolean evolving, float tempdiff, String prediction) {
        List<String> flaggedCriteria = new ArrayList<>();

        if (asymmetry) {
            flaggedCriteria.add("You selected that the mole is asymmetrical, meaning that one half of the mole is not like the other half. This should be mentioned to your health care provider.");
        }
        if (border) {
            flaggedCriteria.add("You selected that the border of the mole is irregular. Uneven or bumpy borders on a mole should be mentioned to your health care provider.");
        }
        if (color) {
            flaggedCriteria.add("You selected that the mole has color variation. Color variation on a mole can be a sign of multiple ailments and should be mentioned to your health care provider.");
        }
        if (diameter) {
            flaggedCriteria.add("You selected that your mole is larger than 6mm in diameter. Larger moles are considered higher risk. Regularly measure the size of the mole and mention it to your health care provider.");
        }
        if (evolving) {
            flaggedCriteria.add("You selected that your mole is evolving in size or shape. If your mole is evolving, you must see a health care provider as soon as possible.");
        }
        if (tempdiff > 2) {
            flaggedCriteria.add("There seems to be a temperature differential greater than 2 degrees Celsius around the affected mole. This may be concerning and should be mentioned to your health care provider.");
        }

        if (Objects.equals(prediction, "Positive")) {
            flaggedCriteria.add("The machine learning model indicates a positive result. You can toggle the button under the image to see the points the model considers important. Consider showing your health care provider the image of your mole and the highlighted points for examination.");
        }

        // Build results explanation
        if (flaggedCriteria.isEmpty()) {
            resultsTextView.setText("No immediate concerns based on your responses. Keep monitoring for any changes.");
            recommendationTextView.setText("Recommendation: No need to seek medical consultation at this time.");
        } else {
            StringBuilder resultsBuilder = new StringBuilder();
            for (String criteria : flaggedCriteria) {
                resultsBuilder.append(criteria).append("\n\n");
            }
            resultsTextView.setText(resultsBuilder.toString());
            recommendationTextView.setText("Recommendation: We suggest consulting a health care provider for further evaluation.");
        }
    }

    private void showErrorToUser(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        });
    }
}
