package com.fallmerayer.radathina.api.clients;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.fallmerayer.radathina.api.core.ApiClient;
import com.fallmerayer.radathina.api.core.ApiClientOptions;
import com.fallmerayer.radathina.api.core.VolleyCallback;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

public class LocationSender extends ApiClient {

    public void sendLocation (LatLng location, String androidId, VolleyCallback volleyCallback) {
        String parameter = androidId + "/" + location.latitude + "/" + location.longitude;

        String requestUrl = buildRequest("/addLocation", parameter, new HashMap<String, String>(0));

        Log.d("DBG", "sendLocation: " + requestUrl);

        addVolley(Request.Method.POST, requestUrl, volleyCallback);
    }


    public LocationSender(Context context, ApiClientOptions apiClientOptions) {
        super(context, apiClientOptions);
    }
}
