package com.example.detectoma;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ScreeningActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screening);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Screening");
        }

        TextView screeningTextView = findViewById(R.id.screeningTextView);
        screeningTextView.setText("Please upload pictures from the device");
    }

    @Override
    public boolean onSupportNavigateUp() {

        navigateToHome();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateToHome();
    }

    private void navigateToHome() {
        Intent intent = new Intent(ScreeningActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
