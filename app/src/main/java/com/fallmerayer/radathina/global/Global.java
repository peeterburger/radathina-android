package com.fallmerayer.radathina.global;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.location.FusedLocationProviderClient;

public final class Global {

    public static final String CURRENT_RADAR_RADIUS_METER = "current_radarRadiusMeter";
    public static final String CURRENT_INTERNAL_SERVER_IP = "current_internalServerIP";
    public static final String CURRENT_INTERNAL_SERVER_PORT = "current_internalServerPort";
    public static final String CURRENT_CHECK_ATTRACTIONS = "current_checkAttractions";
    public static final String CURRENT_CHECK_FOOD = "current_checkFood";
    public static final String CURRENT_CHECK_SHOPPING = "current_checkShopping";

    public static FusedLocationProviderClient fusedLocationClient;
}