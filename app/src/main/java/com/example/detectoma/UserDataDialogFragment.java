package com.example.detectoma;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class UserDataDialogFragment extends DialogFragment {

    public interface UserDataListener {
        void onUserDataCompleted(boolean success);
    }

    private UserDataListener listener;
    private EditText ageEditText;
    private Switch asymmetrySwitch, borderSwitch, colorSwitch, diameterSwitch, evolvingSwitch;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof UserDataListener) {
            listener = (UserDataListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement UserDataListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_user_data, container, false);

        // Initialize UI elements

        asymmetrySwitch = view.findViewById(R.id.asymmetrySwitch);
        borderSwitch = view.findViewById(R.id.borderSwitch);
        colorSwitch = view.findViewById(R.id.colorSwitch);
        diameterSwitch = view.findViewById(R.id.diameterSwitch);
        evolvingSwitch = view.findViewById(R.id.evolvingSwitch);
        Button submitButton = view.findViewById(R.id.submitButton);

        submitButton.setOnClickListener(v -> validateAndSubmitData());

        return view;
    }

    private void validateAndSubmitData() {
        String ageInput = ageEditText.getText().toString().trim();
        if (ageInput.isEmpty()) {
            Toast.makeText(getContext(), "Please enter your age", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gather Switch values
        boolean asymmetry = asymmetrySwitch.isChecked();
        boolean border = borderSwitch.isChecked();
        boolean color = colorSwitch.isChecked();
        boolean diameter = diameterSwitch.isChecked();
        boolean evolving = evolvingSwitch.isChecked();

        // Prepare data for Firebase Realtime Database
        Map<String, Object> screeningData = new HashMap<>();
        screeningData.put("age", ageInput);
        screeningData.put("asymmetry", asymmetry);
        screeningData.put("border", border);
        screeningData.put("color", color);
        screeningData.put("diameter", diameter);
        screeningData.put("evolving", evolving);

        // Save data to Firebase Realtime Database
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("screenings");
        dbRef.push().setValue(screeningData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Data submitted successfully", Toast.LENGTH_SHORT).show();
                    listener.onUserDataCompleted(true); // Notify success
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to submit data", Toast.LENGTH_SHORT).show();
                    listener.onUserDataCompleted(false); // Notify failure
                });
    }
}
