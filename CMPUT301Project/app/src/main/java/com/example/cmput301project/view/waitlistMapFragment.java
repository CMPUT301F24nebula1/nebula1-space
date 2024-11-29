package com.example.cmput301project.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.cmput301project.R;

public class waitlistMapFragment extends DialogFragment {

    public static waitlistMapFragment newInstance() {
        return new waitlistMapFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this dialog
        View view = inflater.inflate(R.layout.waitlist_map, container, false);

        // Reference the ImageView and set the map image
        ImageView mapImageView = view.findViewById(R.id.mapImageView);
        mapImageView.setImageResource(R.drawable.equirectangular_map);

        // Close the dialog when the user clicks outside
        view.setOnClickListener(v -> dismiss());

        return view;
    }
}
