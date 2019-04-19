package com.fallmerayer.radathina.api.clients;

import android.content.Context;
import android.util.Log;

import com.fallmerayer.radathina.api.core.ApiClient;
import com.fallmerayer.radathina.api.core.ApiClientOptions;
import com.fallmerayer.radathina.api.core.VolleyCallback;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

public class InternalApiClient extends ApiClient {

    public void calculateBeeline(LatLng position1, LatLng position2,
                                 final VolleyCallback volleyCallback) {
        HashMap<String, String> queryStrings = new HashMap<>(4);
        queryStrings.put("lat1", "" + position1.latitude);
        queryStrings.put("lon1", "" + position1.longitude);
        queryStrings.put("lat2", "" + position2.latitude);
        queryStrings.put("lon2", "" + position2.longitude);

        String requestUrl = buildRequest("/beeline", "/calculate", queryStrings);

        Log.d("DBG", "calculateBeeline: " + requestUrl);

        addVolley(requestUrl, volleyCallback);
    }

    public void getAttractions(int amount, final VolleyCallback volleyCallback) {
        HashMap<String, String> queryStrings = new HashMap<>(1);
        queryStrings.put("amount", "" + amount);

        String requestUrl = buildRequest("/attractions/list", "", queryStrings);
        Log.d("DBG", "getAttractions: " + requestUrl);

        addVolley(requestUrl, volleyCallback);
    }

    public void getAttractionByName(String name, final VolleyCallback volleyCallback) {
        HashMap<String, String> queryStrings = new HashMap<>(0);

        String requestUrl = buildRequest("/attractions/list", name, queryStrings);
        Log.d("DBG", "getAttractionByName: " + requestUrl);

        addVolley(requestUrl, volleyCallback);
    }

    public void getAttractionsNearby(LatLng currentPosition, double radiusInMeter,
                                     final VolleyCallback volleyCallback) {
        HashMap<String, String> queryStrings = new HashMap<>(3);
        queryStrings.put("lat", "" + currentPosition.latitude);
        queryStrings.put("lon", "" + currentPosition.longitude);
        queryStrings.put("radius", "" + radiusInMeter);

        String requestUrl = buildRequest("/attractions/nearby", "", queryStrings);
        Log.d("DBG", "getAttractionsNearby: " + requestUrl);

        addVolley(requestUrl, volleyCallback);
    }

    public InternalApiClient(Context context, ApiClientOptions apiClientOptions) {
        super(context, apiClientOptions);
    }
}
