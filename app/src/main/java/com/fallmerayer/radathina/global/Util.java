package com.fallmerayer.radathina.global;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class Util {

    public static LatLng locationToLatLng (Location location) {
        if (location == null)
            throw new IllegalArgumentException("Location must not be null");

        return new LatLng(location.getLatitude(), location.getLongitude());
    }

}
