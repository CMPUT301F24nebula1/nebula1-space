package com.example.cmput301project.controller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cmput301project.R;
import com.example.cmput301project.model.Organizer;

import java.util.List;

public class FacilityAdapter extends RecyclerView.Adapter<FacilityAdapter.FacilityViewHolder> {
    private List<Organizer> facilityList;
    private OnDeleteFacilityListener onDeleteListener;

    public interface OnDeleteFacilityListener {
        void onDeleteClick(Organizer facility);
    }

    public FacilityAdapter(List<Organizer> facilities, OnDeleteFacilityListener listener) {
        this.facilityList = facilities;
        this.onDeleteListener = listener;
    }

    @NonNull
    @Override
    public FacilityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_facility_item, parent, false);
        return new FacilityViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FacilityViewHolder holder, int position) {
        Organizer facility = facilityList.get(position);
        holder.nameTextView.setText(facility.getName());
        holder.phoneTextView.setText(facility.getPhone());

        // Check if the facility has a profile picture URL
        if (facility.getProfilePictureUrl() != null && !facility.getProfilePictureUrl().isEmpty()) {
            holder.facilityImageView.setVisibility(View.VISIBLE); // Ensure the ImageView is visible
            Glide.with(holder.itemView.getContext())
                    .load(facility.getProfilePictureUrl())
                    .error(R.drawable.placeholder_image) // Optional fallback image
                    .into(holder.facilityImageView);
        } else {
            holder.facilityImageView.setVisibility(View.GONE); // Hide the ImageView if no URL
        }

        holder.deleteButton.setOnClickListener(v -> onDeleteListener.onDeleteClick(facility));
    }




    @Override
    public int getItemCount() {
        return facilityList.size();
    }

    static class FacilityViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, phoneTextView;
        ImageView facilityImageView;
        ImageButton deleteButton;

        public FacilityViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.facilityNameTextView);
            phoneTextView = itemView.findViewById(R.id.facilityPhoneTextView);
            facilityImageView = itemView.findViewById(R.id.facilityImageView); // Add this line
            deleteButton = itemView.findViewById(R.id.deleteFacilityButton);
        }
    }

    public void updateFacilities(List<Organizer> updatedList) {
        this.facilityList = updatedList;
        notifyDataSetChanged();
    }
}