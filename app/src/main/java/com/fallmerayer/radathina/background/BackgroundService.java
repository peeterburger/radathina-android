package com.fallmerayer.radathina.background;

import android.content.Context;
import android.location.Location;
import com.google.android.gms.maps.model.LatLng;

public class BackgroundService {

    protected static Context context;

    protected static Location lastKnownLocation;

    protected static boolean initialized = false;

    public static Location getLastKnownLocation() {
        if(!initialized)
            throw new IllegalStateException("BackgroundService has to be initialized first!");

        return BackgroundService.lastKnownLocation;
    }

    public static LatLng getLastKnownLatLng() {
        if(!initialized)
            throw new IllegalStateException("BackgroundService has to be initialized first!");

        if (lastKnownLocation == null)
            return new LatLng(0, 0);

        return new LatLng(getLastKnownLocation().getLatitude(),
                getLastKnownLocation().getLongitude());
    }

    public static void initialize(Context context) {
        if (context == null)
            throw new IllegalArgumentException("Context must not be null!");

        BackgroundService.context = context;
        BackgroundService.initialized = true;
    }

}
