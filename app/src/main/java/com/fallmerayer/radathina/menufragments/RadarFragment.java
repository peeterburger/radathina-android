package com.fallmerayer.radathina.menufragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fallmerayer.radathina.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class RadarFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapView mMapView;
    private View mView;

    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private final float mDefaultZoom = 16;
    private final float mDefaultBearing = 0;
    private final float mDefaultTilt = 0;

    public RadarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_radar, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMapView = mView.findViewById(R.id.map);
        if(mMapView != null){
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {

        MapsInitializer.initialize(getContext());

        mMap = map;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMap.addMarker(new MarkerOptions()
                .position(mDefaultLocation)
                .title("Test")
                .snippet("Boi, cool test"));

        CameraPosition cameraPosition = CameraPosition.builder()
                .target(mDefaultLocation)
                .zoom(mDefaultZoom)
                .bearing(mDefaultBearing)
                .tilt(mDefaultTilt)
                .build();

        map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}
