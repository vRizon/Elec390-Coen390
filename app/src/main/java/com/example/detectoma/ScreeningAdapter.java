package com.example.detectoma;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// ScreeningAdapter.java (Same as previously shared)
public class ScreeningAdapter extends RecyclerView.Adapter<ScreeningAdapter.ScreeningViewHolder> {

    private final List<String> screenings;
    private final Context context;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String timestamp);
    }

    public ScreeningAdapter(List<String> screenings, Context context, OnItemClickListener listener) {
        this.screenings = screenings;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ScreeningViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ScreeningViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScreeningViewHolder holder, int position) {
        String timestamp = screenings.get(position);
        holder.bind(timestamp, listener);
    }

    @Override
    public int getItemCount() {
        return screenings.size();
    }

    static class ScreeningViewHolder extends RecyclerView.ViewHolder {
        private final TextView timestampTextView;

        public ScreeningViewHolder(@NonNull View itemView) {
            super(itemView);
            timestampTextView = itemView.findViewById(android.R.id.text1);
        }

        public void bind(final String timestamp, final OnItemClickListener listener) {
            timestampTextView.setText(timestamp);
            itemView.setOnClickListener(v -> listener.onItemClick(timestamp));
        }
    }
}
