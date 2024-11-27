package com.example.cmput301project.controller;

import android.content.Context;
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
import com.example.cmput301project.R;
import com.example.cmput301project.model.Event;

import java.util.ArrayList;

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
        TextView endDateTextView = view.findViewById(R.id.lottery_ends_date);

        // Clear the ImageView before loading a new image
        Glide.with(getContext()).clear(poster);

        if (e != null) {
            String text = "Ends date: " + e.getEndDate();
            endDateTextView.setText(text);

            name.setText(e.getName());

            if (!(e.getPosterUrl() == null) && !e.getPosterUrl().isEmpty()) {
                poster.setVisibility(View.VISIBLE);
                Glide.with(getContext())
                        .load(e.getPosterUrl())
                        .placeholder(R.drawable.placeholder_image)  // placeholder
                        .error(R.drawable.error_image)              // error image
                        .into(poster);
            }
            else {
                poster.setVisibility(View.GONE);
                Log.e("Error", "Poster URL is null");
            }
        }
        return view;
    }
}
