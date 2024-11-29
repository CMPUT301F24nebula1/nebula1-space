package com.example.cmput301project.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.cmput301project.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class waitlistMapFragment extends DialogFragment {

    private static final String TAG = "waitlistMapFragment";
    private String eventId;


    public static waitlistMapFragment newInstance(String eventId) {
        waitlistMapFragment fragment = new waitlistMapFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId); //passing event id to the fragment
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.waitlist_map, container, false);

        ImageView mapImageView = view.findViewById(R.id.mapImageView);
        mapImageView.setImageResource(R.drawable.equirectangular_map);

        //get id for args
        String eventId = getArguments() != null ? getArguments().getString("eventId") : null;

        // Fetch and display entrant locations if eventId is provided
        if (eventId != null) {
            fetchEntrantLocations(eventId, view);
        } else {
            Log.e(TAG, "Event ID not provided. Cannot fetch entrant locations.");
        }

        // close when click outside of map
        view.setOnClickListener(v -> dismiss());

        return view;
    }

    private void fetchEntrantLocations(String eventId, View rootView) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // getting locations from the firestore
        db.collection("entrants")
                .whereEqualTo("waitlistEventId", eventId) // only getting lat and long info from entrants who have a waitlistEventId matching the event id
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Extract latitude and longitude
                            Double latitude = document.getDouble("latitude");
                            Double longitude = document.getDouble("longitude");

                            if (latitude != null && longitude != null) {
                                // Add a pin for this entrant
                                addPinToMap(latitude, longitude, rootView);
                            }
                        }
                    } else {
                        Log.d(TAG, "No entrant locations found for event ID: " + eventId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching entrant locations: ", e);
                });
    }

    private void addPinToMap(double latitude, double longitude, View rootView) {
        FrameLayout mapLayout = rootView.findViewById(R.id.mapImageView);

        // creating pin
        ImageView pin = new ImageView(requireContext());
        pin.setImageResource(R.drawable.pin_icon);
        pin.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        ));

        // Calculate pixel positions for the pin based on map dimensions and coordinates
        int x = calculateXPosition(longitude);
        int y = calculateYPosition(latitude);

        // setting pin
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) pin.getLayoutParams();
        params.leftMargin = x;
        params.topMargin = y;
        pin.setLayoutParams(params);

        // adding pin
        mapLayout.addView(pin);
    }

//    map: https://commons.wikimedia.org/wiki/File:Equirectangular_projection_SW.jpg
    private int calculateXPosition(double longitude) {
        int mapWidth = 2058;
        return (int) ((longitude + 180) / 360 * mapWidth);
    }

    private int calculateYPosition(double latitude) {
        int mapHeight = 1036;
        return (int) ((-latitude + 90) / 180 * mapHeight);
    }
}