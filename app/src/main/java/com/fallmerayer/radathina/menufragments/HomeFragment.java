package com.fallmerayer.radathina.menufragments;


import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.fallmerayer.radathina.R;
import com.fallmerayer.radathina.api.clients.LocationSender;
import com.fallmerayer.radathina.api.core.ApiClientOptions;
import com.fallmerayer.radathina.api.core.VolleyCallback;
import com.fallmerayer.radathina.global.Global;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

public class HomeFragment extends Fragment {

    private View hView;

    private Button btnSendLocation;

    private LocationSender locationSender;

    private TextView txtViewCurrentLocation;

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("ONCALLBACK", "onCreate");

        super.onCreate(savedInstanceState);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                Location location = locationResult.getLastLocation();

                txtViewCurrentLocation.setText("Latitude:\t" + location.getLatitude() +
                        "\nLongitude:\t" + location.getLongitude());

            }
        };

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

                try {
                    Global.fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(HomeFragment.this.getActivity(), new OnSuccessListener<Location>() {

                                @Override
                                public void onSuccess(Location location) {

                                    LatLng latLng = new LatLng(location.getLatitude(),
                                            location.getLongitude());

                                    String androidId = Settings.Secure.getString(getActivity().getContentResolver(),
                                            Settings.Secure.ANDROID_ID);

                                    locationSender.sendLocation(latLng, androidId, new VolleyCallback() {
                                        @Override
                                        public void onSuccess(String result) {
                                            btnSendLocation.setTextColor(Color.GREEN);
                                            Log.d("DBG", "locationSender onSuccess: " + result);
                                        }
                                    });
                                }
                            });
                } catch (SecurityException se) {
                    Log.d("DBG", "GPS permission denied");
                }
            }
        });

        return hView;
    }

    @Override
    public void onStart() {
        super.onStart();

        try {
            Global.fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback, null);

            Global.fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(HomeFragment.this.getActivity(), new OnSuccessListener<Location>() {

                        @Override
                        public void onSuccess(Location location) {
                            txtViewCurrentLocation.setText("Latitude:\t" + location.getLatitude() +
                                    "\nLongitude:\t" + location.getLongitude());
                        }

                    });
        } catch (SecurityException se) {
            Log.d("DBG", "GPS permission denied");
        }
    }
}
