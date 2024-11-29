package com.example.cmput301project.controller;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cmput301project.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImageRecyclerViewAdapter extends RecyclerView.Adapter<ImageRecyclerViewAdapter.ImageViewHolder> {

    private List<String> imageUrls;
    private Set<String> selectedImages = new HashSet<>(); // Use Set to avoid duplicates
    private OnImageDeleteListener deleteListener;
    private Context context;

    public interface OnImageDeleteListener {
        void onDelete(String imageUrl); // For single delete
        void onBatchDelete(List<String> imageUrls); // For batch delete
    }

    public ImageRecyclerViewAdapter(Context context, List<String> imageUrls, OnImageDeleteListener deleteListener) {
        this.context = context;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);

        // Load image using Glide
        Glide.with(holder.imageView.getContext())
                .load(imageUrl)
                .placeholder(android.R.color.darker_gray)
                .into(holder.imageView);
//
//        // Highlight selected images
//        holder.itemView.setBackgroundColor(selectedImages.contains(imageUrl) ?
//                context.getResources().getColor(R.color.grey) :
//                context.getResources().getColor(android.R.color.transparent));

        // Highlight selected images
        if (selectedImages.contains(imageUrl)) {
            holder.imageView.setAlpha(0.5f); // Dim the image
        } else {
            holder.imageView.setAlpha(1.0f); // Normal image
        }

        // Handle clicks for selection
        holder.itemView.setOnClickListener(v -> {
//            if (selectedImages.contains(imageUrl)) {
//                selectedImages.remove(imageUrl); // Deselect image
//            } else {
//                selectedImages.add(imageUrl); // Select image
//            }
            boolean wasSelected = selectedImages.contains(imageUrl);

            // Clear previous selection
            if (!wasSelected && !selectedImages.isEmpty()) {
                String previousSelection = selectedImages.iterator().next(); // Get the first selected image
                selectedImages.clear(); // Clear all selections since we allow only one
                int previousPosition = imageUrls.indexOf(previousSelection);
                if (previousPosition >= 0) {
                    notifyItemChanged(previousPosition); // Update UI for the previously selected item
                }
            }

            // Add or remove the current selection
            if (wasSelected) {
                selectedImages.remove(imageUrl); // Deselect image
            } else {
                selectedImages.add(imageUrl); // Select image
            }
            notifyItemChanged(position); // Update UI for this item
        });

//        // Handle long-click for immediate delete
//        holder.itemView.setOnLongClickListener(v -> {
//            if (deleteListener != null) {
//                deleteListener.onDelete(imageUrl); // Trigger single delete
//            } else {
//                Toast.makeText(context, "Delete listener not set.", Toast.LENGTH_SHORT).show();
//            }
//            return true;
//        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    // Method to delete all selected images
    public void deleteSelectedImages() {
        if (selectedImages.isEmpty()) {
            Toast.makeText(context, "No images selected for deletion.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (deleteListener != null) {
            deleteListener.onBatchDelete(new ArrayList<>(selectedImages)); // Trigger batch delete
        } else {
            Toast.makeText(context, "Delete listener not set.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Remove images from the main list and clear selections
        imageUrls.removeAll(selectedImages);
        clearSelections();

        // Notify the adapter to refresh the UI
        notifyDataSetChanged();

        // Provide user feedback
        Toast.makeText(context, "Selected images deleted.", Toast.LENGTH_SHORT).show();
    }

    // Get the list of selected images
    public List<String> getSelectedImages() {
        return new ArrayList<>(selectedImages);
    }

    // Clear all selections
    public void clearSelections() {
        selectedImages.clear();
        notifyDataSetChanged();
    }

    public void updateImageList(List<String> newImageUrls) {
        if (newImageUrls == null) return;
        imageUrls.clear();
        imageUrls.addAll(newImageUrls);
        notifyDataSetChanged();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}