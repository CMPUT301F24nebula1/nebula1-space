package com.example.cmput301project.view;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.cmput301project.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class waitlistMapFragment extends DialogFragment {

    private static final String TAG = "waitlistMapFragment";
    private String eventId;
    private ScaleGestureDetector scaleGestureDetector;
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    // Touch event mode
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    // Touch points
    private PointF start = new PointF();
    private float oldDist = 1f;

    public static waitlistMapFragment newInstance(String eventId) {
        waitlistMapFragment fragment = new waitlistMapFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId); // pass event id to fragment
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.waitlist_map, container, false);

        ImageView mapImageView = view.findViewById(R.id.mapImageView);
        mapImageView.setImageResource(R.drawable.equirectangular_map);

        // init zoom and pan
        setupZoomAndPan(mapImageView);

        // get event id from args
        String eventId = getArguments() != null ? getArguments().getString("eventId") : null;

        // fetch n display entrant locations
        if (eventId != null) {
            fetchEntrantLocations(eventId, view);
        } else {
            Log.e(TAG, "Event ID not provided. Cannot fetch entrant locations.");
        }

        // close button
        ImageView closeButton = view.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> dismiss());

        return view;
    }

    private void setupZoomAndPan(ImageView mapImageView) {
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener(mapImageView));

        mapImageView.setOnTouchListener((v, event) -> {
            ImageView view = (ImageView) v;
            view.setScaleType(ImageView.ScaleType.MATRIX);

            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN: // Start drag
                    savedMatrix.set(matrix);
                    start.set(event.getX(), event.getY());
                    mode = DRAG;
                    break;

                case MotionEvent.ACTION_POINTER_DOWN: // Start zoom
                    oldDist = spacing(event);
                    if (oldDist > 10f) {
                        savedMatrix.set(matrix);
                        mode = ZOOM;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP: // End drag/zoom
                    mode = NONE;
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) {
                        matrix.set(savedMatrix);
                        matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                    } else if (mode == ZOOM) {
                        float newDist = spacing(event);
                        if (newDist > 10f) {
                            matrix.set(savedMatrix);
                            float scale = newDist / oldDist;
                            matrix.postScale(scale, scale, event.getX(0), event.getY(0));
                        }
                    }
                    break;
            }

            view.setImageMatrix(matrix);
            scaleGestureDetector.onTouchEvent(event);
            return true;
        });
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private final ImageView mapImageView;

        public ScaleListener(ImageView mapImageView) {
            this.mapImageView = mapImageView;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            mapImageView.setImageMatrix(matrix);
            return true;
        }
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void fetchEntrantLocations(String eventId, View rootView) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Getting locations from Firestore
        db.collection("entrants")
                .whereEqualTo("waitlistEventId", eventId)
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
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching entrant locations: ", e));
    }

    private void addPinToMap(double latitude, double longitude, View rootView) {
        FrameLayout pinContainer = rootView.findViewById(R.id.pinContainer);

        // Creating pin
        ImageView pin = new ImageView(requireContext());
        pin.setImageResource(R.drawable.pin_icon);
        pin.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        ));

        // Calculate pixel positions for the pin based on map dimensions and coordinates
        int x = calculateXPosition(longitude);
        int y = calculateYPosition(latitude);

        // Setting pin
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) pin.getLayoutParams();
        params.leftMargin = x;
        params.topMargin = y;
        pin.setLayoutParams(params);

        // Adding pin
        pinContainer.addView(pin);
    }

    private int calculateXPosition(double longitude) {
        int mapWidth = 2058;
        return (int) ((longitude + 180) / 360 * mapWidth);
    }

    private int calculateYPosition(double latitude) {
        int mapHeight = 1036;
        return (int) ((-latitude + 90) / 180 * mapHeight);
    }
}
