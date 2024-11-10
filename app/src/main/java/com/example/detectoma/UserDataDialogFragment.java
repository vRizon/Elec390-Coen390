package com.example.detectoma;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class UserDataDialogFragment extends DialogFragment {

    public interface UserDataListener {
        void onUserDataCompleted();
    }

    private UserDataListener listener;
    private EditText ageEditText;
    private CheckBox historyOfCancerCheckBox;
    private CheckBox familyCancerHistoryCheckBox;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof UserDataListener) {
            listener = (UserDataListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement UserDataListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_user_data, container, false);

        ageEditText = view.findViewById(R.id.ageEditText);
        historyOfCancerCheckBox = view.findViewById(R.id.historyOfCancerCheckBox);
        familyCancerHistoryCheckBox = view.findViewById(R.id.familyCancerHistoryCheckBox);
        Button submitButton = view.findViewById(R.id.submitButton);

        submitButton.setOnClickListener(v -> validateInput());

        return view;
    }

    private void validateInput() {
        String ageInput = ageEditText.getText().toString().trim();
        boolean hasCancerHistory = historyOfCancerCheckBox.isChecked();
        boolean hasFamilyCancerHistory = familyCancerHistoryCheckBox.isChecked();

        if (ageInput.isEmpty()) {
            Toast.makeText(getContext(), "Please enter your age", Toast.LENGTH_SHORT).show();
            return;
        }

        // If all required data is provided, notify the listener
        if (!ageInput.isEmpty() && (hasCancerHistory || hasFamilyCancerHistory)) {
            listener.onUserDataCompleted(); // Notify the activity that data entry is complete
            dismiss();
        } else {
            Toast.makeText(getContext(), "Please complete all information", Toast.LENGTH_SHORT).show();
        }
    }
}
