package com.fallmerayer.radathina.api;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class InternalApiClient {
    private String protocol = "http";
    private String host = "localhost";
    private int port = 80;
    private String apiPath = "";

    private Context context;

    public InternalApiClient protocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public InternalApiClient host(String host) {
        this.host = host;
        return this;
    }

    public InternalApiClient port(int port) {
        this.port = port;
        return this;
    }

    public InternalApiClient apiPath(String apiPath) {
        this.apiPath = apiPath;
        return this;
    }

    private void addVolley(String requestUrl, final VolleyCallback volleyCallback) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        volleyCallback.onSuccess(response);
                    }

                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("DBG", "onResponse: " + error.getMessage());
            }

        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private String buildRequest(String path, String parameter, HashMap<String, String> queryStrings) {
        StringBuilder sb = new StringBuilder();

        sb.append(protocol + "://");
        sb.append(host + ":");
        sb.append(port);
        sb.append(apiPath);
        sb.append(path);
        sb.append("/" + parameter + "?");

        Iterator it = queryStrings.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            sb.append(pair.getKey() + "=" + pair.getValue() + "&");
            it.remove(); // avoids a ConcurrentModificationException
        }

        return sb.toString();
    }

    public void calculateBeeline(LatLng position1, LatLng position2, final VolleyCallback volleyCallback) {
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

    public InternalApiClient(Context context) {
        this.context = context;
    }
}
