package com.fallmerayer.radathina;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;

import com.fallmerayer.radathina.api.clients.LocationSender;
import com.fallmerayer.radathina.api.core.ApiClientOptions;
import com.fallmerayer.radathina.api.clients.InternalApiClient;
import com.fallmerayer.radathina.api.core.VolleyCallback;
import com.fallmerayer.radathina.menufragments.HomeFragment;
import com.fallmerayer.radathina.menufragments.NotificationFragment;
import com.fallmerayer.radathina.menufragments.RadarFragment;
import com.fallmerayer.radathina.menufragments.SettingsFragment;
import com.fallmerayer.radathina.menufragments.WeatherFragment;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity {

    String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            String title = "";
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            switch (item.getItemId()) {
                case R.id.navigation_weather:
                    title = "Weather";
                    Log.d("DBG", "Weather pressed: ");
                    fragmentTransaction.replace(R.id.fram, new WeatherFragment(), "FragmentName");
                    break;
                case R.id.navigation_radar:
                    title = "Radar";
                    Log.d("DBG", "Radar pressed: ");
                    fragmentTransaction.replace(R.id.fram, new RadarFragment(), "FragmentName");
                    break;
                case R.id.navigation_notifications:
                    title = "Notifications";
                    Log.d("DBG", "Notifications pressed: ");
                    fragmentTransaction.replace(R.id.fram, new NotificationFragment(), "FragmentName");
                    break;
                case R.id.navigation_home:
                    title = "Home";
                    Log.d("DBG", "Home pressed: ");
                    fragmentTransaction.replace(R.id.fram, new HomeFragment(), "FragmentName");
                    break;
                case R.id.navigation_settings:
                    title = "Settings";
                    Log.d("DBG", "Settings pressed: ");
                    fragmentTransaction.replace(R.id.fram, new SettingsFragment(), "FragmentName");
            }

            setTitle(title);
            fragmentTransaction.commit();

            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("DBG", "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        checkPermissions(PERMISSIONS);

        // testInternalApi();
        // testLocationSender();
    }

    private void checkPermissions (String... permissionList) {
        if(!hasPermissions(this, permissionList)){
            Log.d("PERMISSIONS", "Requesting permissions");
            ActivityCompat.requestPermissions(this, permissionList, 1);

            if (hasPermissions(this, permissionList)) {
                Log.d("PERMISSIONS", "All permissions are now granted...");
            } else {
                Log.d("PERMISSIONS", "Permissions still not granted...");
            }
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                Log.d("PERMISSIONS", "Checking permission " + permission + "...");

                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("PERMISSIONS", "Permission " + permission + " not granted!");
                    return false;
                }
                Log.d("PERMISSIONS", "Permission " + permission + " is already granted!");
            }
        }

        Log.d("PERMISSIONS", "All permissions already granted!");
        return true;
    }

    @Override
    public void onBackPressed() {
        ScrollView scrollView = findViewById(R.id.attraction_feed);
        scrollView.setVisibility(View.INVISIBLE);
    }

    private void testLocationSender () {
        LocationSender locationSender = new LocationSender(this, new ApiClientOptions()
                .protocol("http")
                .host("185.5.199.33")
                .port(5051));

        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        locationSender.sendLocation(new LatLng(0, 0), androidId, new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                Log.d("DBG", "locationSender onSuccess: " + result);
            }
        });
    }

    private void testInternalApi() {
        InternalApiClient internalApiClient = new InternalApiClient(this, new ApiClientOptions()
                .protocol("http")
                .host("192.168.1.100")
                .port(12345)
                .apiPath("/api/v1")
        );

        internalApiClient.calculateBeeline(new LatLng(10, 10), new LatLng(20, 20),
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.d("DBG", "calculateBeeline: " + result);
                    }
                });


        internalApiClient.getAttractionByName("Plaka", new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                Log.d("DBG", "getAttractionByName: " + result);
            }
        });

        internalApiClient.getAttractions(0, new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                Log.d("DBG", "getAttractions: " + result);
            }
        });

        internalApiClient.getAttractionsNearby(new LatLng(30, 30), 1000, new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                Log.d("DBG", "getAttractionsNearby: " + result);
            }
        });

    }
}
