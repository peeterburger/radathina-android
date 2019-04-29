package com.fallmerayer.radathina.menufragments;

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
import android.widget.TextView;

import com.fallmerayer.radathina.R;
import com.fallmerayer.radathina.api.clients.InternalApiClient;
import com.fallmerayer.radathina.api.clients.OpenRoutesServiceApiClient;
import com.fallmerayer.radathina.api.core.ApiClientOptions;
import com.fallmerayer.radathina.api.core.VolleyCallback;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RadarFragment extends Fragment implements
        OnMapReadyCallback,
        LocationListener,
        LocationSource.OnLocationChangedListener,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap   mMap;
    private MapView     mMapView;
    private View        mView;

    private LocationManager locationManager;
    private ConnectivityManager connectivityManager;

    private LatLng attractionLatLng;

    private InternalApiClient internalApiClient;
    private OpenRoutesServiceApiClient openRoutesServiceApiClient;

    private CircleOptions radarCircleOptions;
    private Circle radarCircle;

    private PolylineOptions currentRouteOptions;
    private Polyline currentRoute;

    public static long     DEFAULT_LOCATION_REFRESH_TIME_MILLIS = 1000;
    public static float    DEFAULT_LOCATION_REFRESH_DISTANCE_METERS = 10;
    public static float    DEFAULT_ZOOM = 16;
    public static float    DEFAULT_RADAR_RADIUS_METERS = 3000;

    private LatLng lastReceivedLocation;

    private TextView attractionDescription;

    private Button btnRoute;
    private ScrollView attractionFeed;
    private TextView txtViewAttractionTitle;

    private boolean isRouteSet = false;


    public RadarFragment() {
        // Required empty public constructor
    }

    public void loadInitialPosition() {
        try {
            mMap.setMyLocationEnabled(true);

            if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) == null) {
                lastReceivedLocation = new LatLng(0, 0);
            } else {
                lastReceivedLocation = new LatLng(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude(),
                        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude());
            }

            updateCamera(lastReceivedLocation);
        } catch (SecurityException se) {
            Log.d("PERMISSIONS", "Permission denied");
        }
    }

    public void initializeGpsListener() {
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, DEFAULT_LOCATION_REFRESH_TIME_MILLIS,
                    DEFAULT_LOCATION_REFRESH_DISTANCE_METERS,
                    this);
        } catch (SecurityException se) {
            Log.d("PERMISSIONS", "Permission denied");
        }
    }

    public void initializeGMap(GoogleMap map) {
        mMap = map;

        MapsInitializer.initialize(getContext());
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setOnMarkerClickListener(this);
    }

    public void loadRoute(LatLng start, LatLng end) {
        openRoutesServiceApiClient.getDirection("foot-walking",
                "5b3ce3597851110001cf624892e3aee660dd4e36a94e389509ba388c",
                start, end,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {

                        ArrayList<LatLng> points = new ArrayList<>();

                        try {
                            JSONObject jsonRoute = new JSONObject(result);
                            JSONArray coordinates = jsonRoute.getJSONArray("features").
                                    getJSONObject(0).getJSONObject("geometry").
                                    getJSONArray("coordinates");

                            for (int i = 0; i < coordinates.length(); i++) {
                                JSONArray coordinate = coordinates.getJSONArray(i);

                                points.add(new LatLng(coordinate.getDouble(1),
                                        coordinate.getDouble(0)));
                            }

                            currentRoute.setPoints(points);

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

                        String category = attraction.getString("category");

                        float color = BitmapDescriptorFactory.HUE_RED;

                        switch (category) {
                            case "Sehenswürdigkeiten":
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

                        Log.d("DBG", "lat: " + lat + "; lon: " + lon);

                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(lat, lon))
                                .title(name)
                                .snippet(category)
                                .icon(BitmapDescriptorFactory.defaultMarker(color)));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void loadMarkersNearby () {
        Log.d("DBG", "loadMarkers: ");

        internalApiClient.getAttractionsNearby(new LatLng(lastReceivedLocation.latitude,
                lastReceivedLocation.longitude), DEFAULT_RADAR_RADIUS_METERS, new VolleyCallback() {
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

                        String category = attraction.getString("category");

                        float color = BitmapDescriptorFactory.HUE_RED;

                        switch (category) {
                            case "Sehenswürdigkeiten":
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

                        Log.d("DBG", "lat: " + lat + "; lon: " + lon);

                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(lat, lon))
                                .title(name)
                                .snippet(category)
                                .icon(BitmapDescriptorFactory.defaultMarker(color)));
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

    public boolean isGpsAvailable() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void updateRadarCircle(LatLng currentLatLng) {
        radarCircle.setCenter(currentLatLng);
    }

    public void updateCamera(LatLng currentLatLng) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM);
        mMap.animateCamera(cameraUpdate);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("ONCALLBACK", "onCreate");

        super.onCreate(savedInstanceState);

        locationManager = (LocationManager) this.getActivity().getSystemService(
                Context.LOCATION_SERVICE);

        connectivityManager = (ConnectivityManager) this.getActivity().getSystemService(
                Context.CONNECTIVITY_SERVICE);


        internalApiClient = new InternalApiClient(this.getActivity(), new ApiClientOptions()
                .protocol("http")
                .host("185.5.199.33")
                .port(5052)
                .apiPath("/api/v1")
        );

        openRoutesServiceApiClient = new OpenRoutesServiceApiClient(this.getActivity(), new ApiClientOptions()
                .protocol("https")
                .host("api.openrouteservice.org")
                .port(443)
                .apiPath("/v2"));

        radarCircleOptions = new CircleOptions()
                .strokeColor(Color.argb(255, 0, 0, 255))
                .strokeWidth(3)
                .fillColor(Color.argb(30, 0, 0,255))
                .radius(DEFAULT_RADAR_RADIUS_METERS)
                .center(new LatLng(-33.87365, 151.20689));

        currentRouteOptions = new PolylineOptions()
                .color(Color.argb(150, 100, 100, 255));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("ONCALLBACK", "onCreateView");

        mView = inflater.inflate(R.layout.fragment_radar, container, false);

        btnRoute = mView.findViewById(R.id.button_route);
        attractionFeed = mView.findViewById(R.id.attraction_feed);
        attractionDescription = mView.findViewById(R.id.txtViewAttractionDescription);
        txtViewAttractionTitle = mView.findViewById(R.id.txtViewAttractionTitle);

        btnRoute.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                try {
                    isRouteSet = true;
                    loadRoute(new LatLng(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude(),
                                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude()),
                            attractionLatLng);
                    attractionFeed.setVisibility(View.INVISIBLE);
                    updateCamera(lastReceivedLocation);
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

        initializeGMap(map);
        if (!isGpsAvailable()) {
            Log.d("GPS", "GPS not enabled. No provider found!");
        } if (!isNetworkAvailable()) {
            Log.d("GPS", "No internet connection...");
        } else {

            radarCircle = mMap.addCircle(radarCircleOptions);
            currentRoute = mMap.addPolyline(currentRouteOptions);

            initializeGpsListener();
            loadInitialPosition();
            loadMarkers();
            // loadMarkersNearby();
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

        lastReceivedLocation = new LatLng(location.getLatitude(), location.getLongitude());

        updateRadarCircle(lastReceivedLocation);

        if (isRouteSet) {
            updateCamera(lastReceivedLocation);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("ONCALLBACK", "onMarkerClick");


        attractionDescription.setText("loading...");

        internalApiClient.getAttractionByName(marker.getTitle(), new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject attraction = new JSONObject(result);
                    String description = attraction.getString("description");
                    attractionDescription.setText(description);
                } catch (JSONException e) {
                    Log.d("DBG", "error parsing json");
                }
            }
        });

        txtViewAttractionTitle.setText(marker.getTitle());

        attractionFeed.setVisibility(View.VISIBLE);
        attractionLatLng = marker.getPosition();

        return false;
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
