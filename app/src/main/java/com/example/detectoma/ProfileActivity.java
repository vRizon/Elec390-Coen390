package com.example.detectoma;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("profiles").child(uid).child("screenings");

            databaseRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String formattedDate = snapshot.getKey(); // Formatted date key
                            String details = snapshot.getValue(String.class);

                            Intent intent = new Intent(ProfileActivity.this, resultsActivity.class);
                            intent.putExtra("FORMATTED_DATE", formattedDate); // Pass formatted date
                            intent.putExtra("DETAILS", details != null ? details : "No additional details available");
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, "No screening results available.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ProfileActivity.this, "Failed to fetch screening results.", Toast.LENGTH_SHORT).show();
                }
            });
        });








    }


}
