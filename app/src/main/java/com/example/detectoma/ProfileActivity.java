package com.example.detectoma;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {




    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button startScreeningButton = findViewById(R.id.startScreeningButton);
        startScreeningButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ScreeningActivity.class);
            startActivity(intent);
        });

        Button goToResultsButton = findViewById(R.id.btnGoToResults);
        goToResultsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, resultsActivity.class);
            startActivity(intent);
        });


        // Initialize RecyclerView
        RecyclerView recyclerView = findViewById(R.id.screeningsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load past screenings (timestamps)
        ArrayList<String> screenings = loadScreeningsFromStorage(); // Method to fetch saved screenings

        ScreeningAdapter adapter = new ScreeningAdapter(screenings, this, timestamp -> {
            Intent intent = new Intent(ProfileActivity.this, resultsActivity.class);
            intent.putExtra("TIMESTAMP", Long.parseLong(timestamp));
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);


    }
    private ArrayList<String> loadScreeningsFromStorage() {
        // Implement method to load saved screenings
        return new ArrayList<>();
    }
}
