package com.example.detectoma;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScreeningAdapter extends RecyclerView.Adapter<ScreeningAdapter.ViewHolder> {
    private List<Screening> screeningList;
    private Context context;

    public ScreeningAdapter(List<Screening> screeningList, Context context) {
        this.screeningList = screeningList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_screening, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Screening screening = screeningList.get(position);
        holder.screeningDate.setText(screening.getTimestamp());
        holder.temperature.setText("Temperature: " + screening.getTempDifference());
        holder.distances.setText("Distance 1: " + screening.getDistance1() + " Distance 2: " + screening.getDistance2());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ScreeningDetailsActivity.class);
            intent.putExtra("timestamp", screening.getTimestamp());
            intent.putExtra("temperature", screening.getTempDifference());
            intent.putExtra("distance1", screening.getDistance1());
            intent.putExtra("distance2", screening.getDistance2());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return screeningList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView screeningDate;
        TextView temperature;
        TextView distances;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            screeningDate = itemView.findViewById(R.id.screeningDate);
            temperature = itemView.findViewById(R.id.temperature);
            distances = itemView.findViewById(R.id.distances);
        }
    }
}
