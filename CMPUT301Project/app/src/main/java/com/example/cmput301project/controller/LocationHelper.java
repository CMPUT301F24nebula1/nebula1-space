package com.example.cmput301project.controller;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationHelper {
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Context context;

    public LocationHelper(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

    }

    public void requestLocationUpdates(LocationListener listener) {
        locationListener = listener;
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 86400, 10000, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void stopLocationUpdates() {
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

}

