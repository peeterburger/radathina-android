package com.fallmerayer.radathina.menufragments;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.fallmerayer.radathina.R;
import com.fallmerayer.radathina.api.clients.InternalApiClient;
import com.fallmerayer.radathina.api.core.ApiClientOptions;
import com.fallmerayer.radathina.api.core.VolleyCallback;
import com.fallmerayer.radathina.background.BackgroundService;
import com.fallmerayer.radathina.global.Config;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NotificationFragment extends Fragment implements LocationListener {

    private LocationManager locationManager;

    private InternalApiClient internalApiClient;

    private Location lastReceivedLocation;

    public static long     DEFAULT_LOCATION_REFRESH_TIME_MILLIS = 100000;
    public static float    DEFAULT_LOCATION_REFRESH_DISTANCE_METERS = 100;

    private View nView;

    private TextView test;

    private LinearLayout notificationFeed;

    private SharedPreferences sharedPreferences;

    public float RADAR_RADIUS_METERS = 1000;

    public NotificationFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationManager = (LocationManager) this.getActivity().getSystemService(
                Context.LOCATION_SERVICE);

        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, DEFAULT_LOCATION_REFRESH_TIME_MILLIS,
                    DEFAULT_LOCATION_REFRESH_DISTANCE_METERS,
                    this);

            lastReceivedLocation = BackgroundService.getLastKnownLocation();

            onLocationChanged(lastReceivedLocation);
        } catch (SecurityException se) {
            Log.d("DBG", "GPS permission denied!");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        nView = inflater.inflate(R.layout.fragment_notification, container, false);

        notificationFeed = nView.findViewById(R.id.notificationFeed);

        sharedPreferences = getActivity().getSharedPreferences("Settings", Activity.MODE_PRIVATE);

        test = nView.findViewById(R.id.test);

        RADAR_RADIUS_METERS = sharedPreferences.getFloat(Config.CURRENT_RADAR_RADIUS_METER,
                1000);

        internalApiClient = new InternalApiClient(this.getActivity(), new ApiClientOptions()
                .protocol("http")
                .host(sharedPreferences.getString(Config.CURRENT_INTERNAL_SERVER_IP, "185.5.199.33"))
                .port(sharedPreferences.getInt(Config.CURRENT_INTERNAL_SERVER_PORT, 5052))
                .apiPath("/api/v1"));

        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onLocationChanged(Location location) {
        lastReceivedLocation = location;

        LatLng latLng = new LatLng(lastReceivedLocation.getLatitude(),
                lastReceivedLocation.getLongitude());

        internalApiClient.getAttractionsNearby(latLng, RADAR_RADIUS_METERS,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        String test = "";
                        try {
                            Log.d("DBG", "onSuccess: SERVER RESULT: " + result);
                            JSONArray attractions = new JSONArray(result);

                            Log.d("DBG", "" + attractions.length());

                            for (int i = 0; i < attractions.length(); i++) {
                                JSONObject attraction = attractions.getJSONObject(i);

                                double lat = attraction.getJSONObject("coordinates").getDouble("lat");
                                double lon = attraction.getJSONObject("coordinates").getDouble("lon");

                                String category = attraction.getString("category");

                                float color;

                                switch (category) {
                                    case "Sehenswürdigkeit":
                                        color = BitmapDescriptorFactory.HUE_RED;
                                        break;
                                    case "Essen":
                                        color = BitmapDescriptorFactory.HUE_AZURE;
                                        break;
                                    case "Shoppen":
                                        color = BitmapDescriptorFactory.HUE_ORANGE;
                                        break;
                                    default:
                                        color = BitmapDescriptorFactory.HUE_CYAN;
                                        break;
                                }

                                String name = attraction.getString("name");

                                test += name + "\n";

                                TextView textView = new TextView(getActivity());
                                textView.setText(name);

                                notificationFeed.addView(textView);
                            }
                        } catch (JSONException e) {
                            Log.d("DBG", "JSON exception!");
                        }
                    }
                });
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
