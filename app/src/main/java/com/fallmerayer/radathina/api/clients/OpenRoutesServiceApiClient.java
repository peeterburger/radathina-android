package com.fallmerayer.radathina.api.clients;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.fallmerayer.radathina.api.core.ApiClient;
import com.fallmerayer.radathina.api.core.ApiClientOptions;
import com.fallmerayer.radathina.api.core.VolleyCallback;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

public class OpenRoutesServiceApiClient extends ApiClient {

    public void getDirection (String profile, String apiKey, LatLng start, LatLng end,
                                 final VolleyCallback volleyCallback) {

        HashMap<String, String> queryStrings = new HashMap<>(3);
        queryStrings.put("api_key", apiKey);
        queryStrings.put("start", "" + start.longitude + "," + start.latitude);
        queryStrings.put("end", "" + end.longitude + "," + end.latitude);

        String requestUrl = buildRequest("/directions", profile, queryStrings);

        Log.d("DBG", "getDirection: " + requestUrl);

        addVolley(Request.Method.GET, requestUrl, volleyCallback);
    }

    public OpenRoutesServiceApiClient(Context context, ApiClientOptions apiClientOptions) {
        super(context, apiClientOptions);
    }
}
