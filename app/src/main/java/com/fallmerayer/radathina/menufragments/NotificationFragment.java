package com.fallmerayer.radathina.menufragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fallmerayer.radathina.R;
import com.fallmerayer.radathina.api.clients.InternalApiClient;
import com.fallmerayer.radathina.api.core.ApiClientOptions;
import com.fallmerayer.radathina.api.core.VolleyCallback;
import com.fallmerayer.radathina.global.Global;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NotificationFragment extends Fragment {

    private View nView;

    private InternalApiClient internalApiClient;

    public float RADAR_RADIUS_METERS = 1000;

    private LinearLayout notificationFeed;

    private SharedPreferences sharedPreferences;

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    public NotificationFragment() {
        // Required empty public constructor
    }

    public void loadMarkersNearby (LatLng location) {
        Log.d("DBG", "loadMarkers: ");

        internalApiClient.getAttractionsNearby(location, RADAR_RADIUS_METERS, new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONArray attractions = new JSONArray(result);

                    Log.d("DBG", "" + attractions.length());

                    notificationFeed.removeAllViews();

                    for (int i = 0; i < attractions.length(); i++) {
                        JSONObject attraction = attractions.getJSONObject(i);

                        final double lat = attraction.getJSONObject("coordinates").getDouble("lat");
                        final double lon = attraction.getJSONObject("coordinates").getDouble("lon");

                        String category = attraction.getString("category");

                        int color;

                        switch (category) {
                            case "Sehenswürdigkeit":
                                color = Color.rgb(255, 0, 0);
                                break;
                            case "Essen":
                                color = Color.rgb(0 ,255, 0);
                                break;
                            case "Shoppen":
                                color = Color.rgb(0 ,0, 255);
                                break;
                            default:
                                color = Color.rgb(0 ,0, 0);
                                break;
                        }

                        String name = attraction.getString("name");

                        Log.d("DBG", "lat: " + lat + "; lon: " + lon);

                        LinearLayout attractionLayout = new LinearLayout(getActivity());
                        TextView attractionName = new TextView(getActivity());
                        TextView attractionCategory = new TextView(getActivity());
                        TextView attractionPosition = new TextView(getActivity());
                        final TextView attractionDistance = new TextView(getActivity());

                        attractionLayout.setOrientation(LinearLayout.VERTICAL);
                        attractionLayout.setPadding(0, 0, 0, 30);

                        attractionName.setText(name);
                        attractionName.setTextSize(20);
                        attractionName.setTypeface(null, Typeface.BOLD);

                        attractionCategory.setText(category);
                        attractionCategory.setTextColor(color);

                        attractionDistance.setText("Berechne Entfernung...");

                        attractionPosition.setText("(lat: " + lat + "; lng " + lon + ")");
                        attractionName.setTypeface(null, Typeface.BOLD);

                        attractionLayout.addView(attractionName);
                        attractionLayout.addView(attractionCategory);
                        attractionLayout.addView(attractionDistance);
                        attractionLayout.addView(attractionPosition);

                        notificationFeed.addView(attractionLayout);

                        try {
                            Global.fusedLocationClient.getLastLocation()
                                    .addOnSuccessListener(getActivity(),
                                            new OnSuccessListener<Location>() {

                                                @Override
                                                public void onSuccess(Location location) {
                                                    internalApiClient.calculateBeeline(new LatLng(location.getLatitude(),
                                                                    location.getLongitude()),
                                                            new LatLng(lat, lon), new VolleyCallback() {
                                                                @Override
                                                                public void onSuccess(String result) {
                                                                    double distance = Double.valueOf(result);
                                                                    int iDistance = (int) distance;
                                                                    attractionDistance.setText("" + iDistance + " Meter von hier");
                                                                }
                                                            });
                                                }

                                            });
                        } catch (SecurityException se) {
                            Log.d("DBG", "GPS Permission denied");
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("ONCALLBACK", "onCreate");

        super.onCreate(savedInstanceState);

        sharedPreferences = getActivity().getSharedPreferences("Settings", Activity.MODE_PRIVATE);

        RADAR_RADIUS_METERS = sharedPreferences.getFloat(Global.CURRENT_RADAR_RADIUS_METER,
                1000);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(60000);
        locationRequest.setFastestInterval(30000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                Location location = locationResult.getLastLocation();

                loadMarkersNearby(new LatLng(location.getLatitude(), location.getLongitude()));

            }
        };

        internalApiClient = new InternalApiClient(this.getActivity(), new ApiClientOptions()
                .protocol("http")
                .host(sharedPreferences.getString(Global.CURRENT_INTERNAL_SERVER_IP, "185.5.199.33"))
                .port(sharedPreferences.getInt(Global.CURRENT_INTERNAL_SERVER_PORT, 5052))
                .apiPath("/api/v1")
        );

        Log.d("DBG", "internalApiClient: " + sharedPreferences.getString(Global.CURRENT_INTERNAL_SERVER_IP, "185.5.199.33"));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("ONCALLBACK", "onCreateView");

        nView = inflater.inflate(R.layout.fragment_notification, container, false);

        notificationFeed = nView.findViewById(R.id.notificationFeed);

        return nView;
    }

    @Override
    public void onStart() {
        super.onStart();

        try {
            Global.fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                loadMarkersNearby(new LatLng(location.getLatitude(),
                                        location.getLongitude()));
                            }
                        }
                    });

            Global.fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,
                    null);
        } catch (SecurityException se) {
            Log.d("DBG", "GPS Permission denied");
        }
    }

}
