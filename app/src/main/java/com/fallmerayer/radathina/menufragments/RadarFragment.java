package com.fallmerayer.radathina.menufragments;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;

import com.fallmerayer.radathina.R;
import com.fallmerayer.radathina.api.clients.InternalApiClient;
import com.fallmerayer.radathina.api.clients.OpenRoutesServiceApiClient;
import com.fallmerayer.radathina.api.core.ApiClientOptions;
import com.fallmerayer.radathina.api.core.VolleyCallback;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RadarFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener,
        LocationListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap   mMap;
    private MapView     mMapView;
    private View        mView;

    private LocationManager locationManager;
    private ConnectivityManager connectivityManager;

    public static long     DEFAULT_LOCATION_REFRESH_TIME_MILLIS = 1000;
    public static float    DEFAULT_LOCATION_REFRESH_DISTANCE_METERS = 10;
    public static float    DEFAULT_ZOOM = 16;
    public static float    DEFAULT_RADAR_RADIUS_METERS = 300;

    private LatLng attractionLatLng;

    private InternalApiClient internalApiClient;
    private OpenRoutesServiceApiClient openRoutesServiceApiClient;

    private Circle radar;

    public RadarFragment() {
        // Required empty public constructor
    }

    public void loadRoute(LatLng start, LatLng end) {
        openRoutesServiceApiClient.getDirection("foot-walking",
                "5b3ce3597851110001cf624892e3aee660dd4e36a94e389509ba388c",
                start, end,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {

                        PolylineOptions options = new PolylineOptions();

                        try {
                            JSONObject route = new JSONObject(result);
                            JSONArray coordinates = route.getJSONArray("features").
                                    getJSONObject(0).getJSONObject("geometry").
                                    getJSONArray("coordinates");

                            for (int i = 0; i < coordinates.length(); i++) {
                                JSONArray coordinate = coordinates.getJSONArray(i);

                                options.add(new LatLng(coordinate.getDouble(1),
                                        coordinate.getDouble(0)));
                            }

                            mMap.addPolyline(options);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("DBG", "JSONException: " + e.getStackTrace());
                        }
                    }
                });
    }

    public void loadMarkers () {
        Log.d("DBG", "loadMarkers: ");

        internalApiClient.getAttractions(0, new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    Log.d("DBG", "onSuccess: SERVER RESULT: " + result);
                    JSONArray attractions = new JSONArray(result);

                    Log.d("DBG", "" + attractions.length());

                    for (int i = 0; i < attractions.length(); i++) {
                        JSONObject attraction = attractions.getJSONObject(i);

                        double lat = attraction.getJSONObject("coordinates").getDouble("lat");
                        double lon = attraction.getJSONObject("coordinates").getDouble("lon");

                        String name = attraction.getString("name");

                        Log.d("DBG", "lat: " + lat + "; lon: " + lon);

                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(lat, lon))
                                .title(name)
                                .snippet("snippet"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("ONCALLBACK", "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("ONCALLBACK", "onCreateView");
        mView = inflater.inflate(R.layout.fragment_radar, container, false);
        Button btn = mView.findViewById(R.id.button_route);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                try {
                    loadRoute(new LatLng(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude(),
                                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude()),
                            attractionLatLng);
                } catch (SecurityException se) {

                }
            }
        });
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("ONCALLBACK", "onViewCreated");


        internalApiClient = new InternalApiClient(this.getActivity(), new ApiClientOptions()
                .protocol("http")
                .host("192.168.1.100")
                .port(12345)
                .apiPath("/api/v1")
        );

        openRoutesServiceApiClient = new OpenRoutesServiceApiClient(this.getActivity(), new ApiClientOptions()
                .protocol("https")
                .host("api.openrouteservice.org")
                .port(443)
                .apiPath("/v2"));


        mMapView = mView.findViewById(R.id.map);
        if(mMapView != null){
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        Log.d("ONCALLBACK", "onMapReady");

        MapsInitializer.initialize(getContext());
        mMap = map;

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        locationManager = (LocationManager) this.getActivity().getSystemService(
                Context.LOCATION_SERVICE);

        connectivityManager = (ConnectivityManager) this.getActivity().getSystemService(
                Context.CONNECTIVITY_SERVICE);

        if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            Log.d("GPS", "GPS not enabled. No provider found!");
        } if (!isNetworkAvailable()) {
            Log.d("GPS", "No internet connection...");
        } else {

            try {
                radar = map.addCircle(new CircleOptions()
                        .strokeColor(Color.argb(255, 0, 0, 255))
                        .strokeWidth(3)
                        .fillColor(Color.argb(30, 0, 0,255))
                        .radius(DEFAULT_RADAR_RADIUS_METERS)
                        .center(new LatLng(-33.87365, 151.20689)));

                map.setOnMarkerClickListener(this);
                map.setMyLocationEnabled(true);

                Log.d("DBG", "setMyLocationEnabled: true");

                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        DEFAULT_LOCATION_REFRESH_TIME_MILLIS,
                        DEFAULT_LOCATION_REFRESH_DISTANCE_METERS,
                        this);



                loadMarkers();

                onLocationChanged(locationManager.getLastKnownLocation(
                        LocationManager.GPS_PROVIDER));

                Log.d("DBG", "requestLocationUpdates...");
            } catch (SecurityException se) {
                Log.d("PERMISSIONS", "Permission denied");
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("ONCALLBACK", "onLocationChanged");

        while(location == null) {
            try {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            } catch (SecurityException se) {
                Log.d("DBG", "permission denied: ");
            }
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        radar.setCenter(latLng);

        Log.d("DBG", "location change detected: lat=" + latLng.latitude + ";lon="
                + latLng.longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM);
        mMap.animateCamera(cameraUpdate);
        Log.d("DBG", "camera moved...");

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onProviderDisabled(String provider) { }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("ONCALLBACK", "onMarkerClick");
        ScrollView scrollView = this.getActivity().findViewById(R.id.attraction_feed);
        scrollView.setVisibility(View.VISIBLE);

        attractionLatLng = marker.getPosition();

        ObjectAnimator animation = ObjectAnimator.ofFloat(scrollView, "translationY", 500f);
        animation.setDuration(500);
        animation.start();

        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d("DBG", "onMapClick: ");
        ScrollView scrollView = this.getActivity().findViewById(R.id.attraction_feed);
        scrollView.setVisibility(View.INVISIBLE);
    }

}
