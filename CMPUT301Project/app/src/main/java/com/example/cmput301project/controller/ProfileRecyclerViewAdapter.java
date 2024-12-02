package com.example.cmput301project.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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
            holder.profilePictureImageView.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(entrant.getProfilePictureUrl())
                    .placeholder(R.drawable.placeholder_image) // Placeholder image
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            // If the image fails to load, set the ImageView's visibility to GONE
//                            holder.profilePictureImageView.setVisibility(View.INVISIBLE);
                            holder.profilePictureImageView.setImageDrawable(createInitialsDrawable(entrant.getName()));
                            Log.e("Glide", "Image load failed", e); // Log the error
                            return true; // Return true to prevent Glide from applying the error placeholder
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            // If the image loads successfully, ensure the ImageView is visible
                            holder.profilePictureImageView.setVisibility(View.VISIBLE);
                            return false; // Return false to let Glide handle displaying the image
                        }
                    })
                    .into(holder.profilePictureImageView);
        } else {
//            holder.profilePictureImageView.setImageResource(R.drawable.ic_launcher_background); // Default image
//            holder.profilePictureImageView.setVisibility(View.INVISIBLE);
            holder.profilePictureImageView.setImageDrawable(createInitialsDrawable(entrant.getName()));
        }

        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(entrant, position));
    }

    private BitmapDrawable createInitialsDrawable(String name) {
        String initials = getInitials(name);
        int size = 150;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#FF1D2A97"));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(50f);
        canvas.drawText(initials, size / 2, size / 2 + 15, paint);

//        int MyVersion = Build.VERSION.SDK_INT;
//        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
//            checkIfAlreadyhavePermission();
//        }
//        imageUri = getImageUri(this.getContext(), bitmap);

        return new BitmapDrawable(context.getResources(), bitmap);
    }

    private String getInitials(String name) {
        if (TextUtils.isEmpty(name)) return "";
        String[] parts = name.trim().split(" ");
        String initials = "";
        for (String part : parts) {
            if (!TextUtils.isEmpty(part)) {
                initials += part.charAt(0);
            }
        }
        return initials.toUpperCase();
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

