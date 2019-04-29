package com.fallmerayer.radathina.global;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public final class Config {

    public static final String DEFAULT_RADAR_RADIUS_METER = "default_radarRadiusMeter";
    public static final String CURRENT_RADAR_RADIUS_METER = "current_radarRadiusMeter";

    public static final String DEFAULT_INTERNAL_SERVER_IP = "default_internalServerIP";
    public static final String CURRENT_INTERNAL_SERVER_IP = "current_internalServerIP";

    public static final String DEFAULT_INTERNAL_SERVER_PORT = "default_internalServerPort";
    public static final String CURRENT_INTERNAL_SERVER_PORT = "current_internalServerPort";

    public static final String DEFAULT_CHECK_ATTRACTIONS = "default_checkAttractions";
    public static final String CURRENT_CHECK_ATTRACTIONS = "current_checkAttractions";

    public static final String DEFAULT_CHECK_FOOD = "default_checkFood";
    public static final String CURRENT_CHECK_FOOD = "current_checkFood";

    public static final String DEFAULT_CHECK_SHOPPING= "default_checkShopping";
    public static final String CURRENT_CHECK_SHOPPING = "current_checkShopping";

    private static String PREFERENCE_NAME = "settings";
    private static SharedPreferences sharedPreferences;

    public static SharedPreferences sharedPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCE_NAME, Activity.MODE_PRIVATE);
    }

}