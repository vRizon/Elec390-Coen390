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
import java.util.Map;

public class ScreeningAdapter extends RecyclerView.Adapter<ScreeningAdapter.ViewHolder> {
//    private List<Screening> screeningList;
    private List<Map<String, Object>> screeningList;
    private Context context;

    public ScreeningAdapter(List<Map<String, Object>> screeningList, Context context) {
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
        Map<String, Object> screeningData = screeningList.get(position);

        // Safely access data with type checking and casting
        // Get timestamp
        String timestamp = screeningData.get("timestamp") != null ?
                screeningData.get("timestamp").toString() : "N/A";
        String temperatureDiff = screeningData.get("temperatureDiff") != null ?
                String.valueOf(screeningData.get("temperatureDiff")) : "N/A";
        String distanceSurface = screeningData.get("distanceSurface") != null ?
                String.valueOf(screeningData.get("distanceSurface")) : "N/A";
//        Boolean asymmetry = screeningData.get("asymmetry") != null ?
//                (Boolean) screeningData.get("asymmetry") : null;

        // Set data to your views
        holder.screeningDate.setText(timestamp);
        holder.temperature.setText("Temperature Difference: " + temperatureDiff + "°C");
        holder.distances.setText("Distance Surface: " + distanceSurface + " cm");

//        holder.asymmetryTextView.setText(asymmetry != null ? asymmetry.toString() : "N/A");

//        holder.temperature.setText("Temperature: " + screening.getTempDifference());
//        holder.distances.setText("Distance 1: " + screening.getDistance1() + " Distance 2: " + screening.getDistance2());

        holder.itemView.setOnClickListener(v -> {
//            Intent intent = new Intent(context, ScreeningDetailsActivity.class);
//            intent.putExtra("timestamp", screening.getTimestamp());
//            intent.putExtra("temperature", screening.getTempDifference());
//            intent.putExtra("distance1", screening.getDistance1());
//            intent.putExtra("distance2", screening.getDistance2());
//            context.startActivity(intent);
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
