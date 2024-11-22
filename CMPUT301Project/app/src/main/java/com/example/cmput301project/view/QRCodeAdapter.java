package com.example.cmput301project.view;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cmput301project.R;
import com.example.cmput301project.model.Event;

import java.util.List;

public class QRCodeAdapter extends RecyclerView.Adapter<QRCodeAdapter.QRCodeViewHolder> {

    private List<Event> events;
    private OnQRCodeSelectedListener selectedListener;
    private int selectedPosition = -1;

    public interface OnQRCodeSelectedListener {
        void onQRCodeSelected(Event event);
    }

    public QRCodeAdapter(List<Event> events, OnQRCodeSelectedListener selectedListener) {
        this.events = events;
        this.selectedListener = selectedListener;
    }

    @NonNull
    @Override
    public QRCodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.qrcode_list, parent, false);
        return new QRCodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QRCodeViewHolder holder, int position) {
        Event event = events.get(position);
        holder.eventNameTextView.setText(event.getName());

        // Display a hashed placeholder or indicate it's a hashed QR code
        if (event.getHashedQRCode() != null && !event.getHashedQRCode().isEmpty()) {
            holder.qrCodeImageView.setVisibility(View.VISIBLE);
            holder.qrCodeImageView.setImageResource(R.drawable.ic_launcher_foreground);
            holder.qrCodeImageView.setColorFilter(Color.DKGRAY); // Placeholder for hashed QR code
        } else {
            holder.qrCodeImageView.setVisibility(View.INVISIBLE);  // Indicate no QR code exists
        }

        holder.itemView.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition();
            selectedListener.onQRCodeSelected(event);
            notifyDataSetChanged();
        });

        // Highlight selected item
        holder.itemView.setBackgroundColor(
                selectedPosition == position ? Color.LTGRAY : Color.WHITE
        );
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class QRCodeViewHolder extends RecyclerView.ViewHolder {
        TextView eventNameTextView;
        ImageView qrCodeImageView;

        public QRCodeViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            qrCodeImageView = itemView.findViewById(R.id.qrCode);
        }
    }
}


