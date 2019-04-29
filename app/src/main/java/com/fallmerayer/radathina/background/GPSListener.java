package com.fallmerayer.radathina.background;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class GPSListener implements LocationListener {

    public GPSListener (long locationRefreshTimeMilliSeconds,
                           float locationRefreshDistanceMeters) {
        if (!BackgroundService.initialized)
            throw new IllegalStateException("BackgroundService not initialized!");

        if (BackgroundService.context == null)
            throw new NullPointerException("BackgroundService.context must not be null");

        LocationManager locationManager = (LocationManager) BackgroundService.context.getSystemService(
                Context.LOCATION_SERVICE);

        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    locationRefreshTimeMilliSeconds,
                    locationRefreshDistanceMeters,
                    this);
        } catch (SecurityException se) {
            Log.d("GPS", "GPS Permissions denied!");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location == null)
            throw new NullPointerException("Location must not be null");

        if(!BackgroundService.initialized)
            throw new IllegalStateException("BackgroundService not initialized!");

        BackgroundService.lastKnownLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
