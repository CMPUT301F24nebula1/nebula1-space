package com.example.cmput301project.view;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cmput301project.R;
import com.example.cmput301project.model.Entrant;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private ArrayList<Entrant> entrants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Get entrants passed from the intent
        entrants = (ArrayList<Entrant>) getIntent().getSerializableExtra("entrants");
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // Adding pins for each entrant
        if (entrants != null && !entrants.isEmpty()) {
            for (Entrant entrant : entrants) {
                if (entrant.getLatitude() != null && entrant.getLongitude() != null) {
                    LatLng location = new LatLng(entrant.getLatitude(), entrant.getLongitude());
                    googleMap.addMarker(new MarkerOptions()
                            .position(location)
                            .title(entrant.getName()));
                }
            }

            // Move the camera to the oldest (first in list) entrant's location
            LatLng firstLocation = new LatLng(entrants.get(0).getLatitude(), entrants.get(0).getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 12));
        } else {
            // Set a default location if no entrants exist or their location is missing
            LatLng defaultLocation = new LatLng(53.5461, -113.4937); //edmonton coords
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
            Toast.makeText(this, "No entrant locations available. Displaying Edmonton Alberta, the City of Champions.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
