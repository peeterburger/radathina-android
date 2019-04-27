package com.fallmerayer.radathina.menufragments;


import android.content.Context;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.fallmerayer.radathina.R;
import com.fallmerayer.radathina.api.clients.InternalApiClient;
import com.fallmerayer.radathina.api.clients.LocationSender;
import com.fallmerayer.radathina.api.clients.OpenRoutesServiceApiClient;
import com.fallmerayer.radathina.api.core.ApiClientOptions;
import com.fallmerayer.radathina.api.core.VolleyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class HomeFragment extends Fragment {

    View hView;

    Button btnSendLocation;

    LocationSender locationSender;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("ONCALLBACK", "onCreate");

        super.onCreate(savedInstanceState);

        locationSender = new LocationSender(getActivity(), new ApiClientOptions()
                .protocol("http")
                .host("185.5.199.33")
                .port(5051));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        hView = inflater.inflate(R.layout.fragment_home, container, false);

        btnSendLocation = hView.findViewById(R.id.btnSendLocation);

        btnSendLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String androidId = Settings.Secure.getString(getActivity().getContentResolver(),
                        Settings.Secure.ANDROID_ID);

                locationSender.sendLocation(new LatLng(0, 0), androidId, new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.d("DBG", "locationSender onSuccess: " + result);
                    }
                });
            }
        });

        return hView;
    }

}
