package com.example.detectoma;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {

    private List<Patient> patients;
    private OnPatientActionListener actionListener;

    public PatientAdapter(List<Patient> patients, OnPatientActionListener actionListener) {
        this.patients = patients;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_item, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient patient = patients.get(position);
        holder.patientNameTextView.setText(patient.getName());

        holder.unlinkButton.setOnClickListener(v -> actionListener.onUnlinkPatient(patient));
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    public static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView patientNameTextView;
        Button unlinkButton;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            patientNameTextView = itemView.findViewById(R.id.patientName);
            unlinkButton = itemView.findViewById(R.id.unlinkButton);
        }
    }

    public interface OnPatientActionListener {
        void onUnlinkPatient(Patient patient);
    }
}
