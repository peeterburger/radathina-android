package com.fallmerayer.radathina.menufragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fallmerayer.radathina.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

public class RadarFragment extends Fragment implements OnMapReadyCallback,
        LocationListener {

    private GoogleMap   mMap;
    private MapView     mMapView;
    private View        mView;

    private int     PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted = false;

    private LocationManager mLocationManager;

    static long     DEFAULT_LOCATION_REFRESH_TIME_MILLIS = 1000;
    static float    DEFAULT_LOCATION_REFRESH_DISTANCE_METERS = 10;
    static float    DEFAULT_ZOOM = 16;
    static float    DEFAULT_RADAR_RADIUS_METERS = 100;

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
        enableMyLocation();

        if(locationPermissionGranted) {
            try {
                map.setMyLocationEnabled(true);

                Log.d("DBG", "setMyLocationEnabled: true");

                mLocationManager = (LocationManager) this.getActivity().getSystemService(
                        Context.LOCATION_SERVICE);

                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        DEFAULT_LOCATION_REFRESH_TIME_MILLIS,
                        DEFAULT_LOCATION_REFRESH_DISTANCE_METERS,
                        this);

                loadMarkers();

                onLocationChanged(mLocationManager.getLastKnownLocation(
                        LocationManager.GPS_PROVIDER));

                Log.d("DBG", "requestLocationUpdates...");
            } catch (SecurityException se) {
                Log.d("DBG", "Permission denied");
            }
        } else {
            Log.d("DBG", "Permission denied");
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        while(location == null) {
            try {
                location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            } catch (SecurityException se) {
                Log.d("DBG", "permission denied: ");
            }
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        Log.d("DBG", "location change detected: lat=" + latLng.latitude + ";lon="
                + latLng.longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM);
        mMap.animateCamera(cameraUpdate);
        Log.d("DBG", "camera moved...");

        /* CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .radius(DEFAULT_RADAR_RADIUS_METERS)
                .strokeWidth(4);
        // mMap.addCircle(circleOptions);
        Log.d("DBG", "radar updated..."); */
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("DBG", "Request success!");
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public void loadMarkers () {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this.getActivity());
        String url ="http://10.171.154.205:3000/api/v1/attractions/all";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONArray jsonResponse = new JSONArray(response);
                            Log.d("DBG", "loadMarkers(): " + response);
                            Log.d("DBG", "loadJson(): " + jsonResponse);

                            for (int i = 0; i < jsonResponse.length(); i++) {
                                JSONObject attraction = jsonResponse.getJSONObject(i);
                                JSONObject coordinates = attraction.getJSONObject("koordinaten");

                                double lon = coordinates.getDouble("lon");
                                double lat = coordinates.getDouble("lat");

                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(lon, lat))
                                        .snippet("TEST")
                                        .visible(true));

                                Log.d("DBG", "" + lon + "," + lat);
                            }
                        } catch (Exception e) {
                            Log.d("DBG", "error parsing");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("DBG", "error connecting to server " + error.networkResponse);
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("DBG", "Request success");
                locationPermissionGranted = true;
            } else {
                Log.d("DBG", "Permission denied");
                locationPermissionGranted = false;
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onProviderDisabled(String provider) { }
}
