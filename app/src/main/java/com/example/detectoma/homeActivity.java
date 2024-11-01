package com.example.detectoma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class homeActivity extends AppCompatActivity {
    private Button startScreeningButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        startScreeningButton = findViewById(R.id.startScreeningButton);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Home");


        startScreeningButton.setOnClickListener(v -> {
            Intent intent = new Intent(homeActivity.this, screeningActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {

        navigateToLogin();
        return true;
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(homeActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
