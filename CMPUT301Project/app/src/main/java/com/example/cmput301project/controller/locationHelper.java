package com.example.cmput301project.controller;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class locationHelper {

    private static final String TAG = "locationHelper";

    // Callback interface for location fetching
    public interface LocationCallback {
        void onLocationRetrieved(double latitude, double longitude);
        void onLocationError(String errorMessage);
    }

    /**
     * Fetch the current latitude and longitude of the entrant.
     * @param context Context for accessing system services.
     * @param callback A callback to handle the location results.
     */
    public static void fetchEntrantLocation(Context context, LocationCallback callback) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager == null) {
            callback.onLocationError("Location Manager not available.");
            return;
        }

        try {
            // Request a single location update from GPS
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        callback.onLocationRetrieved(latitude, longitude);
                    } else {
                        callback.onLocationError("Location is null.");
                    }
                }

                @Override
                public void onProviderDisabled(String provider) {
                    callback.onLocationError("GPS is disabled.");
                }
            }, null);
        } catch (SecurityException e) {
            callback.onLocationError("Location permission not granted.");
            Log.e(TAG, "Permission error: ", e);
        }
    }
}