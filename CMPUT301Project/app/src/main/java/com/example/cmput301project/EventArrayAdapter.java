package com.example.cmput301project;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class EventArrayAdapter extends ArrayAdapter<Event> {

    public EventArrayAdapter(@NonNull Context context, @NonNull ArrayList<Event> events) {
        super(context, 0, events);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.event_list_item,
                    parent, false);
        } else {
            view = convertView;
        }
        Event e = getItem(position);
        ImageView poster = view.findViewById(R.id.event_poster);
        TextView name = view.findViewById(R.id.event_name);
//        if (!e.getPosterUrl().isEmpty()) {
//            Glide.with(getContext())
//                    .load(e.getPosterUrl())
//                    .placeholder(R.drawable.placeholder_image)  // Display a placeholder while loading
//                    .error(R.drawable.error_image)              // Display an error image if loading fails
//                    .into(poster);
//        }
        try {
            if (!e.getPosterUrl().isEmpty()) {
                Glide.with(getContext())
                        .load(e.getPosterUrl())
                        .placeholder(R.drawable.placeholder_image)  // Optional placeholder
                        .error(R.drawable.error_image)              // Optional error image
                        .into(poster);
            }
        } catch (NullPointerException exception) {
            Log.e("Error", "Poster URL is null", exception);
        }
        name.setText(e.getName());


        return view;
    }


    public Bitmap convertBase64StringToBitmap(String base64String) {
        // Step 1: Decode the Base64 string into a byte array
        byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);

        // Step 2: Convert the byte array into a Bitmap
        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        return decodedBitmap;  // Return the decoded Bitmap
    }
}
