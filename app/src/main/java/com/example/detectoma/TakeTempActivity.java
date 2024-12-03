package com.example.detectoma;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class TakeTempActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private TextView tempTextView;
    private Button startMeasurementButton;
    private static final String TAG = "TakeTempActivity";

    private boolean isMeasuring = false;
    private List<TemperatureReading> temperatureReadings = new ArrayList<>();
    private ValueEventListener temperatureListener;
    private DatabaseReference buttonRef;
    private ValueEventListener buttonListener;
    private LineChart temperatureChart;
    double Tempdifference = 0.0;
    private ImageView gifImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_temp);
        ImageView backIcon = findViewById(R.id.backIcon);
        backIcon.setOnClickListener(v -> {
            finish(); // Close the current activity and navigate back
        });
        // Initialize Firebase Auth and Database Reference
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("profiles");

        // Initialize UI components
        tempTextView = findViewById(R.id.tempTextView); // Add this TextView to your layout
        startMeasurementButton = findViewById(R.id.startMeasurementButton);
        temperatureChart = findViewById(R.id.temperatureChart);
        gifImageView = findViewById(R.id.tutorialGif); // Initialize the GIF ImageView

        // Load the GIF into the ImageView using Glide
        Glide.with(this).asGif().load(R.drawable.taketemp).into(gifImageView);
        // Configure the chart
        configureChart();

        // Get the current user's UID
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Set up a listener on the 'Button' node in Firebase
            buttonRef = databaseReference.child(userId).child("Button");

            buttonListener = buttonRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Boolean buttonState = snapshot.getValue(Boolean.class);
                    if (buttonState != null) {
                        if (buttonState && !isMeasuring) {
                            // Button is true and measurement is not ongoing
                            isMeasuring = true;
                            Log.d(TAG, "Button state is true. Starting temperature measurement.");
                            startTemperatureMeasurement();
                        } else if (!buttonState && isMeasuring) {
                            // Button is false and measurement is ongoing
                            isMeasuring = false;
                            Log.d(TAG, "Button state is false. Stopping temperature measurement.");
                            stopTemperatureMeasurement();
                        }
                        // Update UI to reflect measurement state
                        updateMeasurementButtonUI(buttonState);
                    } else {
                        Log.e(TAG, "Button state is null");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error fetching button state: " + error.getMessage());
                }
            });
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "User not logged in");
        }

    }

    private void configureChart() {
        // Customize your chart
        temperatureChart.getDescription().setEnabled(false);
        temperatureChart.setTouchEnabled(true);
        temperatureChart.setDragEnabled(true);
        temperatureChart.setScaleEnabled(true);
        temperatureChart.setDrawGridBackground(false);
        temperatureChart.setPinchZoom(true);

        // Configure X-axis
        XAxis xAxis = temperatureChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f); // one second intervals
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm:ss");

            @Override
            public String getFormattedValue(float value) {
                if (temperatureReadings.isEmpty()) {
                    return "";
                }
                long millis = (long) (value * 1000L) + temperatureReadings.get(0).getTimestamp();
                return mFormat.format(new Date(millis));
            }
        });

        // Configure Y-axis
        YAxis leftAxis = temperatureChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = temperatureChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void updateMeasurementButtonUI(boolean isMeasuring) {
        if (isMeasuring) {
            startMeasurementButton.setText("Measurement Ongoing");
        } else {
            startMeasurementButton.setText("Measurement Stopped");
        }
    }


    private void fetchTemperature(String userId) {
        DatabaseReference tempRef = databaseReference.child(userId).child("Temperature");

        tempRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        // Fetch temperature as Double
                        Double temperature = snapshot.getValue(Double.class);

                        // Update UI
                        if (temperature != null) {
                            tempTextView.setText("Temperature: " + temperature);
                        } else {
                            tempTextView.setText("Temperature: N/A");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing temperature: ", e);
                        Toast.makeText(TakeTempActivity.this, "Error parsing temperature", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TakeTempActivity.this, "Temperature data not found for this user", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Temperature data not found for User ID: " + userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TakeTempActivity.this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error fetching data: " + error.getMessage());
            }
        });
    }

    public void debugcontinue(View v){
        setResult(RESULT_OK);
        Toast.makeText(this, "Data submitted successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void startTemperatureMeasurement() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d(TAG, "Starting temperature measurement for User ID: " + userId);

            // Clear previous readings
            temperatureReadings.clear();
            // Clear the chart
            temperatureChart.clear();

            // Set up the listener
            DatabaseReference tempRef = databaseReference.child(userId).child("Temperature");

            temperatureListener = tempRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Double temperature = snapshot.getValue(Double.class);
                    if (temperature != null) {
                        long timestamp = System.currentTimeMillis();
                        TemperatureReading reading = new TemperatureReading(temperature, timestamp);
                        temperatureReadings.add(reading);

                        // Update UI
                        tempTextView.setText("Latest Temperature: " + temperature + "°C");
                        Log.d(TAG, "Temperature reading: " + temperature + " at " + timestamp);

                        // Check for significant difference
                        checkForSignificantDifference();

                        // Update the chart with the new data point
                        addEntryToChart(reading);
                    } else {
                        Log.e(TAG, "Temperature data is null");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(TakeTempActivity.this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error fetching data: " + error.getMessage());
                }
            });
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "User not logged in");
        }
    }

    private void addEntryToChart(TemperatureReading reading) {
        float xValue = (reading.getTimestamp() - temperatureReadings.get(0).getTimestamp()) / 1000f; // Time in seconds since first reading
        float yValue = (float) reading.getTemperature();

        Entry entry = new Entry(xValue, yValue);

        LineData data = temperatureChart.getData();
        if (data == null) {
            data = new LineData();
            temperatureChart.setData(data);
        }

        ILineDataSet set = data.getDataSetByIndex(0);
        if (set == null) {
            set = createSet();
            data.addDataSet(set);
        }

        data.addEntry(entry, 0);
        data.notifyDataChanged();

        // let the chart know it's data has changed
        temperatureChart.notifyDataSetChanged();

        // limit the number of visible entries
        temperatureChart.setVisibleXRangeMaximum(10);

        // move to the latest entry
        temperatureChart.moveViewToX(data.getEntryCount());
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Temperature over Time");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.BLUE);
        set.setCircleColor(Color.BLUE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setDrawCircleHole(false);
        set.setValueTextSize(10f);
        set.setValueTextColor(Color.BLACK);
        return set;
    }


    private void stopTemperatureMeasurement() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && temperatureListener != null) {
            String userId = currentUser.getUid();
            DatabaseReference tempRef = databaseReference.child(userId).child("Temperature");
            tempRef.removeEventListener(temperatureListener);
            Log.d(TAG, "Stopped temperature measurement for User ID: " + userId);

            // Process the collected data
            processTemperatureData();
        }
    }

    private void checkForSignificantDifference() {
        if (temperatureReadings.size() < 2) {
            // Need at least two readings to compare
            return;
        }

//        int lastIndex = temperatureReadings.size() - 1;
        int maxIndex = temperatureReadings.indexOf(Collections.max(temperatureReadings, Comparator.comparingDouble(TemperatureReading::getTemperature)));
        int minIndex = temperatureReadings.indexOf(Collections.min(temperatureReadings, Comparator.comparingDouble(TemperatureReading::getTemperature)));
        TemperatureReading maxReading = temperatureReadings.get(maxIndex);
        TemperatureReading minReading = temperatureReadings.get(minIndex);

        Tempdifference = Math.abs(maxReading.getTemperature() - minReading.getTemperature());

        if (Tempdifference > 2.0) {
            // Found a significant difference
            String message = "Significant temperature change detected: " + Tempdifference + "°C";
            Log.d(TAG, message);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

            // Optionally, you can stop measurement automatically
            // isMeasuring = false;
            // startMeasurementButton.setText("Start Measurement");
            // stopTemperatureMeasurement();
        }
    }

    private void processTemperatureData() {
        if (temperatureReadings.isEmpty()) {
            Toast.makeText(this, "No temperature data collected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a list of Entry objects for the chart
        List<Entry> entries = new ArrayList<>();

        long startTime = temperatureReadings.get(0).getTimestamp();

        // You can perform additional analysis here if needed
        // For this example, we'll just log the collected readings
        for (TemperatureReading reading : temperatureReadings) {
            Log.d(TAG, "Collected Temperature: " + reading.getTemperature() + " at " + reading.getTimestamp());
            // Convert timestamp to seconds relative to startTime
            float xValue = (reading.getTimestamp() - startTime) / 1000f;
            float yValue = (float) reading.getTemperature();

            entries.add(new Entry(xValue, yValue));
        }

        // Create a LineDataSet with the entries
        LineDataSet dataSet = new LineDataSet(entries, "Temperature over Time");

        // Customize the LineDataSet (optional)
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.BLACK);

        // Create LineData object with the dataSet
        LineData lineData = new LineData(dataSet);

        // Set data to the chart
        temperatureChart.setData(lineData);

        // Refresh the chart
        temperatureChart.invalidate(); // Refreshes the chart

        // Show a confirmation dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Measurement Complete")
                .setMessage("Temperature measurement has stopped. Do you want to submit this data?")
                .setPositiveButton("Yes", (dialogInterface, which) -> {
                    setResult(RESULT_OK);
                    saveGraphToDevice();
                    saveTempDifferenceToPreferences();
                    Toast.makeText(this, "Data submitted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("No", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.darkGreen));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.darkGreen));
        });

        dialog.show();

    }

    private void saveTempDifferenceToPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("TempDifference", (float) Tempdifference);
        editor.apply(); // or editor.commit();

        Log.d(TAG, "Temperature difference saved to SharedPreferences: " + Tempdifference);
    }

    private void saveGraphToDevice() {
        if (temperatureChart.getData() == null || temperatureChart.getData().getEntryCount() == 0) {
            Toast.makeText(this, "No graph data available to save", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Get the Bitmap from the LineChart
            Bitmap bitmap = temperatureChart.getChartBitmap();

            // Save the bitmap to internal storage
            String filename = "saved_graph.jpg"; // Adjust filename as needed
            FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

            Toast.makeText(this, "Graph saved successfully to device storage", Toast.LENGTH_SHORT).show();
            Log.d("TakeTempActivity", "Graph saved successfully to device storage.");
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save graph", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listeners to prevent memory leaks
        if (buttonRef != null && buttonListener != null) {
            buttonRef.removeEventListener(buttonListener);
        }
        if (isMeasuring) {
            stopTemperatureMeasurement();
        }
    }



}
