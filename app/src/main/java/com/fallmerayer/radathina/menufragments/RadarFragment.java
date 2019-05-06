package com.fallmerayer.radathina.menufragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
import com.fallmerayer.radathina.api.clients.myweather.common.Common;
import com.fallmerayer.radathina.api.core.ApiClientOptions;
import com.fallmerayer.radathina.api.core.VolleyCallback;
import com.fallmerayer.radathina.global.Config;
import com.fallmerayer.radathina.global.Global;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RadarFragment extends Fragment implements
        OnMapReadyCallback,
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

    public static float DEFAULT_ZOOM = 16;

    public float RADAR_RADIUS_METERS = 1000;

    private TextView attractionDescription;
    private TextView txtViewAttractionDistance;

    private Button btnRoute;
    private ScrollView attractionFeed;
    private TextView txtViewAttractionTitle;

    private SharedPreferences sharedPreferences;

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    public RadarFragment() {
        // Required empty public constructor
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

                        Log.d("DBG", "lat: " + lat + "; lon: " + lon);

                        if (sharedPreferences.getBoolean(Config.KEY_CHECK_ATTRACTIONS,
                                true) && category.equals("Sehenswürdigkeit")) {
                            mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(lat, lon))
                                    .title(name)
                                    .snippet(category)
                                    .icon(BitmapDescriptorFactory.defaultMarker(color)));
                        }

                        if (sharedPreferences.getBoolean(Config.KEY_CHECK_FOOD,
                                false) && category.equals("Essen")) {
                            mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(lat, lon))
                                    .title(name)
                                    .snippet(category)
                                    .icon(BitmapDescriptorFactory.defaultMarker(color)));
                        }

                        if (sharedPreferences.getBoolean(Config.KEY_CHECK_SHOPPING,
                                false) && category.equals("Shoppen")) {
                            mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(lat, lon))
                                    .title(name)
                                    .snippet(category)
                                    .icon(BitmapDescriptorFactory.defaultMarker(color)));
                        }

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

        sharedPreferences = getActivity().getSharedPreferences("Settings", Activity.MODE_PRIVATE);

        RADAR_RADIUS_METERS = sharedPreferences.getFloat(Config.KEY_RADAR_RADIUS_METER,
                1000);

        locationManager = (LocationManager) this.getActivity().getSystemService(
                Context.LOCATION_SERVICE);

        connectivityManager = (ConnectivityManager) this.getActivity().getSystemService(
                Context.CONNECTIVITY_SERVICE);


        internalApiClient = new InternalApiClient(this.getActivity(), new ApiClientOptions()
                .protocol("http")
                .host(sharedPreferences.getString(Config.KEY_INTERNAL_SERVER_IP, "185.5.199.33"))
                .port(sharedPreferences.getInt(Config.KEY_INTERNAL_SERVER_PORT, 5052))
                .apiPath("/api/v1")
        );

        Log.d("DBG", "internalApiClient: " + sharedPreferences.getString(Config.KEY_INTERNAL_SERVER_IP, "185.5.199.33"));

        openRoutesServiceApiClient = new OpenRoutesServiceApiClient(this.getActivity(), new ApiClientOptions()
                .protocol("https")
                .host("api.openrouteservice.org")
                .port(443)
                .apiPath("/v2"));

        radarCircleOptions = new CircleOptions()
                .strokeColor(Color.argb(255, 0, 0, 255))
                .strokeWidth(3)
                .fillColor(Color.argb(30, 0, 0,255))
                .radius(RADAR_RADIUS_METERS)
                .center(new LatLng(0, 0));

        currentRouteOptions = new PolylineOptions()
                .color(Color.argb(150, 100, 100, 255));

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

                updateRadarCircle(new LatLng(location.getLatitude(),
                        location.getLongitude()));

            }
        };

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
        txtViewAttractionDistance = mView.findViewById(R.id.txtViewAttractionDistance);

        btnRoute.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                try {
                    loadRoute(new LatLng(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude(),
                                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude()),
                            attractionLatLng);

                    attractionFeed.setVisibility(View.INVISIBLE);

                    Global.fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {

                        @Override
                        public void onSuccess(Location location) {
                            updateCamera(new LatLng(location.getLatitude(),
                                    location.getLongitude()));
                        }

                    });
                } catch (SecurityException se) {
                    Log.d("DBG", "GPS permission denied");
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

            try {
                Global.fusedLocationClient.requestLocationUpdates(locationRequest,
                        locationCallback, null);

                Global.fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {

                                if (location != null) {
                                    mMap.setMyLocationEnabled(true);

                                    updateCamera(new LatLng(location.getLatitude(),
                                            location.getLongitude()));
                                }

                            }
                        });
                loadMarkers();
            } catch (SecurityException se) {
                Log.d("DBG", "GPS Permission denied");
            }
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        Log.d("ONCALLBACK", "onMarkerClick");

        attractionDescription.setText("Lade Beschreibung...");

        try {
            Global.fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {

                        @Override
                        public void onSuccess(Location location) {

                            LatLng latLng = new LatLng(location.getLatitude(),
                                    location.getLongitude());

                            internalApiClient.calculateBeeline(marker.getPosition(), latLng,
                                    new VolleyCallback() {
                                @Override
                                public void onSuccess(String result) {
                                    double distance = Double.valueOf(result);
                                    int iDistance = (int) distance;
                                    txtViewAttractionDistance.setText("In " + iDistance + " Metern Entfernung");
                                }
                            });
                        }
                    });
        } catch (SecurityException se) {
            Log.d("DBG", "GPS permission denied");
        }

        String url = marker.getTitle().replaceAll(" ", "%20");

        internalApiClient.getAttractionByName(url, new VolleyCallback() {
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
}
