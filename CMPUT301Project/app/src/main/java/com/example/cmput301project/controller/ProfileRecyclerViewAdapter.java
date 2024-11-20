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
import com.example.cmput301project.R;
import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.view.EditProfileActivity;

import java.util.List;

public class ProfileRecyclerViewAdapter extends RecyclerView.Adapter<ProfileRecyclerViewAdapter.ProfileViewHolder> {

    private final List<Entrant> entrantList;
    private final Context context;

    public ProfileRecyclerViewAdapter(Context context, List<Entrant> entrantList) {
        this.context = context;
        this.entrantList = entrantList;
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
    }



    @Override
    public int getItemCount() {
        return entrantList.size();
    }

    public static class ProfileViewHolder extends RecyclerView.ViewHolder {
        TextView initialsTextView;
        TextView nameTextView;
        TextView emailTextView; // Ensure this is added
        ImageView profilePictureImageView;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            initialsTextView = itemView.findViewById(R.id.initials);
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
