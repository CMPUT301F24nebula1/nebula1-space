package com.example.cmput301project.view;

import android.graphics.Bitmap;
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
import com.example.cmput301project.controller.QRCodeGenerator;
import com.example.cmput301project.model.Event;

import java.util.List;

public class QRCodeAdapter extends RecyclerView.Adapter<QRCodeAdapter.QRCodeViewHolder> {

    private List<Event> events;
    private OnQRCodeSelectedListener selectedListener;
    private int selectedPosition = -1;
    private QRCodeClickListener qrCodeClickListener;


    public interface OnQRCodeSelectedListener {
        void onQRCodeSelected(Event event);
    }
    public interface QRCodeClickListener {
        void onQRCodeClick(Bitmap qrCodeBitmap);
    }


    public QRCodeAdapter(List<Event> events, OnQRCodeSelectedListener selectedListener, QRCodeClickListener qrCodeClickListener) {
        this.events = events;
        this.selectedListener = selectedListener;
        this.qrCodeClickListener = qrCodeClickListener;
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

        // Display the QR code
        if (event.getQrCode() != null && !event.getQrCode().isEmpty()) {
            holder.qrCodeImageView.setVisibility(View.VISIBLE);
            // Generate QR code using QRCodeGenerator
            Bitmap qrCodeBitmap = QRCodeGenerator.generateQRCode(event.getId());
            if (qrCodeBitmap != null) {
                // Display the QR code
                holder.qrCodeImageView.setImageBitmap(qrCodeBitmap);


                // Add click listener to display enlarged QR code
                holder.qrCodeImageView.setOnClickListener(v -> {
                    if (qrCodeClickListener != null) {
                        qrCodeClickListener.onQRCodeClick(qrCodeBitmap);
                    }
                });
            }
        } else {
            holder.qrCodeImageView.setVisibility(View.INVISIBLE); // No QR code exists
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