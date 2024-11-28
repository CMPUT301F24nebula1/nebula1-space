package com.example.cmput301project.view;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cmput301project.R;
import com.example.cmput301project.controller.QRCodeGenerator;
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

        // Display the QR code
        if (event.getQrCode() != null && !event.getQrCode().isEmpty()) {
            holder.qrCodeImageView.setVisibility(View.VISIBLE);
            // Generate QR code using QRCodeGenerator
            Bitmap qrCodeBitmap = QRCodeGenerator.generateQRCode(event.getQrCode());
            if (qrCodeBitmap != null) {
                // Display the QR code
                holder.qrCodeImageView.setImageBitmap(qrCodeBitmap);

                // Add click listener to enlarge the QR code
                holder.qrCodeImageView.setOnClickListener(v -> {
                    showEnlargedQRCode(holder.qrCodeImageView.getContext(), qrCodeBitmap);
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

    private void showEnlargedQRCode(Context context, Bitmap qrCodeBitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_enlarged_photo, null);
        builder.setView(dialogView);

        ImageView enlargedQrCodeImageView = dialogView.findViewById(R.id.enlargedImage);
        enlargedQrCodeImageView.setImageBitmap(qrCodeBitmap);

        AlertDialog dialog = builder.create();
        dialog.show();
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


