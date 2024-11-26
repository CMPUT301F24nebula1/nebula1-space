package com.example.cmput301project.controller;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cmput301project.R;
import com.example.cmput301project.model.Entrant;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProfileRecyclerViewAdapter extends RecyclerView.Adapter<ProfileRecyclerViewAdapter.ProfileViewHolder> {

    private final List<Entrant> entrantList;
    private final Context context;
    private final OnProfileClickListener listener;

    public interface OnProfileClickListener {
        void onDeleteClick(Entrant entrant, int position);
    }

    public ProfileRecyclerViewAdapter(Context context, List<Entrant> entrantList, OnProfileClickListener listener) {
        this.context = context;
        this.entrantList = entrantList;
        this.listener = listener;
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

        // Load profile picture using Glide (or similar library)
        if (entrant.getProfilePictureUrl() != null && !entrant.getProfilePictureUrl().isEmpty()) {
            Glide.with(context)
                    .load(entrant.getProfilePictureUrl())
                    .placeholder(R.drawable.ic_launcher_background) // Placeholder image
                    .into(holder.profilePictureImageView);
        } else {
            holder.profilePictureImageView.setImageResource(R.drawable.ic_launcher_background); // Default image
        }

        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(entrant, position));
    }


    @Override
    public int getItemCount() {
        return entrantList.size();
    }

    public static class ProfileViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePictureImageView; // Profile picture
        TextView nameTextView;
        TextView emailTextView;
        ImageView deleteButton; // Delete button

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePictureImageView = itemView.findViewById(R.id.profilePicture);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }


    public void updateList(List<Entrant> newList) {
        entrantList.clear();
        entrantList.addAll(newList);
        notifyDataSetChanged();
    }
}

