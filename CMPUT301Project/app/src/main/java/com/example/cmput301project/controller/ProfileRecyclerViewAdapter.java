package com.example.cmput301project.controller;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cmput301project.R;
import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.view.EditProfileActivity;

import java.util.List;

public class ProfileRecyclerViewAdapter extends RecyclerView.Adapter<ProfileRecyclerViewAdapter.ProfileViewHolder> {

    private final List<Entrant> entrantList;
    private final Context context;
    private final OnProfileClickListener listener;

    public ProfileRecyclerViewAdapter(Context context, List<Entrant> entrantList, OnProfileClickListener listener) {
        this.context = context;
        this.entrantList = entrantList;
        this.listener = listener;

    }
    //  click listener interface
    public interface OnProfileClickListener {
        void onProfileClick(Entrant entrant);
    }

    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.profile_list_item, parent, false);
        return new ProfileViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        Entrant entrant = entrantList.get(position);
        holder.nameTextView.setText(entrant.getName());
        holder.emailTextView.setText(entrant.getEmail());
        holder.initialsTextView.setText(entrant.getInitials());


        if (entrant.getProfilePictureUrl() != null && ! entrant.getProfilePictureUrl().isEmpty()){
            // Load profile image with Glide
            Glide.with(context)
                    .load(entrant.getProfilePictureUrl()) // Ensure `getImageUrl()` provides the image URL or URI
                     // Set a default image
                    .error(R.drawable.error_image) // Image to show if there's an error loading
                    .into(holder.profilePictureImageView);
        }
        // click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProfileClick(entrant);
            }
        });


    }



    @Override
    public int getItemCount() {
        return entrantList.size();
    }

    public static class ProfileViewHolder extends RecyclerView.ViewHolder {
        TextView initialsTextView;
        TextView nameTextView;
        TextView emailTextView;
        ImageView profilePictureImageView;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            initialsTextView = itemView.findViewById(R.id.initialsTextView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            emailTextView = itemView.findViewById(R.id.emailTextView); // Ensure this is defined
            profilePictureImageView = itemView.findViewById(R.id.profilePicture);
        }
    }



    public void updateList(List<Entrant> newList) {
        entrantList.clear();
        entrantList.addAll(newList);
        notifyDataSetChanged();
    }

}
