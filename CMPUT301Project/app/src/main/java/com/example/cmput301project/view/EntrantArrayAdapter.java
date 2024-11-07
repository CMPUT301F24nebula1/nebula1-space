package com.example.cmput301project.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.cmput301project.R;
import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.model.Event;

import java.util.ArrayList;

public class EntrantArrayAdapter extends ArrayAdapter<Entrant> {
    public EntrantArrayAdapter(@NonNull Context context, @NonNull ArrayList<Entrant> entrants) {
        super(context, 0, entrants);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.participant_list_item,
                    parent, false);
        } else {
            view = convertView;
        }
        Entrant e = getItem(position);
        ImageView profile = view.findViewById(R.id.entrant_profile);
        TextView name = view.findViewById(R.id.entrant_name);
        CheckBox select = view.findViewById(R.id.select_participant_checkbox);
        if (e != null) {
            name.setText(e.getName());

            if (!(e.getProfilePictureUrl() == null) && !e.getProfilePictureUrl().isEmpty()) {
                Glide.with(getContext())
                        .load(e.getProfilePictureUrl())
                        .placeholder(R.drawable.placeholder_image)  // placeholder
                        .error(R.drawable.error_image)              // error image
                        .into(profile);
            }
            else {
                Log.e("Error", "Profile picture URL is null");
            }
        }
        return view;
    }
}
