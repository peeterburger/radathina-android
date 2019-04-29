package com.fallmerayer.radathina.menufragments;


import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.fallmerayer.radathina.R;
import com.fallmerayer.radathina.api.clients.InternalApiClient;
import com.fallmerayer.radathina.api.clients.LocationSender;
import com.fallmerayer.radathina.api.clients.OpenRoutesServiceApiClient;
import com.fallmerayer.radathina.api.core.ApiClientOptions;
import com.fallmerayer.radathina.api.core.VolleyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class HomeFragment extends Fragment implements LocationListener {

    private View hView;

    private Button btnSendLocation;

    private LocationManager locationManager;

    private LocationSender locationSender;

    private TextView txtViewCurrentLocation;

    private Location lastReceivedLocation;

    public static long     DEFAULT_LOCATION_REFRESH_TIME_MILLIS = 1000;
    public static float    DEFAULT_LOCATION_REFRESH_DISTANCE_METERS = 10;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("ONCALLBACK", "onCreate");

        super.onCreate(savedInstanceState);

        locationManager = (LocationManager) this.getActivity().getSystemService(
                Context.LOCATION_SERVICE);

        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, DEFAULT_LOCATION_REFRESH_TIME_MILLIS,
                    DEFAULT_LOCATION_REFRESH_DISTANCE_METERS,
                    this);
        } catch (SecurityException se) {
            Log.d("DBG", "permission denied");
        }

        locationSender = new LocationSender(getActivity(), new ApiClientOptions()
                .protocol("http")
                .host("185.5.199.33")
                .port(5051));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        hView = inflater.inflate(R.layout.fragment_home, container, false);

        txtViewCurrentLocation = hView.findViewById(R.id.txtViewCurrentLocation);
        btnSendLocation = hView.findViewById(R.id.btnSendLocation);

        btnSendLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String androidId = Settings.Secure.getString(getActivity().getContentResolver(),
                        Settings.Secure.ANDROID_ID);

                locationSender.sendLocation(getLastReceivedLatLng(), androidId, new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.d("DBG", "locationSender onSuccess: " + result);
                    }
                });
            }
        });

        return hView;
    }

    public LatLng getLastReceivedLatLng() {
        if (lastReceivedLocation == null) {
            return new LatLng(0, 0);
        }

        return new LatLng(lastReceivedLocation.getLatitude(), lastReceivedLocation.getLongitude());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onLocationChanged(Location location) {
        lastReceivedLocation = location;
        txtViewCurrentLocation.setText("Latitude:\t" + location.getLatitude() +
                "\nLongitude:\t" + location.getLongitude());
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
