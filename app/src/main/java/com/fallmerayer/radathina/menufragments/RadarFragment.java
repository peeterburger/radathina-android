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
import com.fallmerayer.radathina.api.clients.InternalApiClient;
import com.fallmerayer.radathina.api.clients.OpenRoutesServiceApiClient;
import com.fallmerayer.radathina.api.core.ApiClient;
import com.fallmerayer.radathina.api.core.ApiClientOptions;
import com.fallmerayer.radathina.api.core.VolleyCallback;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RadarFragment extends Fragment implements OnMapReadyCallback,
        LocationListener, GoogleMap.OnMarkerClickListener {

    private Marker myMarker;

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

    private boolean mapLoaded = false;

    private InternalApiClient internalApiClient;
    private OpenRoutesServiceApiClient openRoutesServiceApiClient;


    public RadarFragment() {
        // Required empty public constructor
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
        enableMyLocation();

        myMarker = map.addMarker(new MarkerOptions()
                .position(new LatLng(46.7202238,11.6465904))
                .title("My Spot")
                .snippet("This is my spot!")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        if(locationPermissionGranted) {
            try {
                map.setOnMarkerClickListener(this);
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
                loadTestRoute();

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
        Log.d("ONCALLBACK", "onLocationChanged");

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

    public void loadTestRoute() {
        openRoutesServiceApiClient.getDirection("foot-walking",
                "5b3ce3597851110001cf624892e3aee660dd4e36a94e389509ba388c",
                new LatLng(46.667462, 11.595469), new LatLng(46.669583, 11.599975),
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {

                        PolylineOptions options = new PolylineOptions();

                        Log.d("DBG", "loadTestRoute: " + result);
                        try {
                            JSONObject route = new JSONObject(result);
                            JSONArray coordinates = route.getJSONArray("features").
                                    getJSONObject(0).getJSONObject("geometry").
                                    getJSONArray("coordinates");

                            for (int i = 0; i < coordinates.length(); i++) {
                                JSONArray coordinate = coordinates.getJSONArray(i);
                                Log.d("DBG", coordinate.getDouble(0) + ";"
                                        + coordinate.getDouble(1));

                                options.add(new LatLng(coordinate.getDouble(0),
                                        coordinate.getDouble(1)));
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d("ONCALLBACK", "onRequestPermissionsResult");
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

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("ONCALLBACK", "onMarkerClick");
        return false;
    }
}
