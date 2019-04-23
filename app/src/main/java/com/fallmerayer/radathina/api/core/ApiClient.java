package com.fallmerayer.radathina.api.core;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ApiClient {
    private ApiClientOptions apiClientOptions;

    private Context context;

    protected void addVolley(int requestMethod, String requestUrl, final VolleyCallback volleyCallback) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(requestMethod, requestUrl,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        volleyCallback.onSuccess(response);
                    }

                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("DBG", "onError: " + error.getMessage());
            }

        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    protected String buildRequest(String path, String parameter, HashMap<String, String> queryStrings) {
        StringBuilder sb = new StringBuilder();

        sb.append(apiClientOptions.protocol + "://");
        sb.append(apiClientOptions.host + ":");
        sb.append(apiClientOptions.port);
        sb.append(apiClientOptions.apiPath);
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

    public ApiClient(Context context, ApiClientOptions apiClientOptions) {
        this.context = context;
        this.apiClientOptions = apiClientOptions;
    }
}
