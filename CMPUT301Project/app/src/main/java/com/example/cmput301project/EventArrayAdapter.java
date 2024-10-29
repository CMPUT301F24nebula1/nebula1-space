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

/**
 * Custom ArrayAdapter for event list
 * @author Xinjia Fan
 */
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

        try {
            if (!e.getPosterUrl().isEmpty()) {
                Glide.with(getContext())
                        .load(e.getPosterUrl())
                        .placeholder(R.drawable.placeholder_image)  // placeholder
                        .error(R.drawable.error_image)              // error image
                        .into(poster);
            }
        } catch (NullPointerException exception) {
            Log.e("Error", "Poster URL is null", exception);
        }
        name.setText(e.getName());


        return view;
    }
}
