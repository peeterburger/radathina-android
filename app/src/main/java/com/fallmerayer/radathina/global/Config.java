package com.fallmerayer.radathina.global;

import android.Manifest;

public class Config {

    public static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final int PERMISSION_REQUEST_CODE = 1;

    public static final String  KEY_RADAR_RADIUS_METER = "current_radarRadiusMeter";
    public static final String  KEY_INTERNAL_SERVER_IP = "current_internalServerIP";
    public static final String  KEY_INTERNAL_SERVER_PORT = "current_internalServerPort";
    public static final String  KEY_CHECK_ATTRACTIONS = "current_checkAttractions";
    public static final String  KEY_CHECK_FOOD = "current_checkFood";
    public static final String  KEY_CHECK_SHOPPING = "current_checkShopping";

    public static final double  DEFAULT_RADAR_RADIUS_METER = 1000;
    public static final String  DEFAULT_INTERNAL_SERVER_IP = "127.0.0.1";
    public static final int     DEFAULT_INTERNAL_SERVER_PORT = 8080;
    public static final boolean DEFAULT_CHECK_ATTRACTIONS = true;
    public static final boolean DEFAULT_CHECK_FOOD = false;
    public static final boolean DEFAULT_CHECK_SHOPPING = false;

}
