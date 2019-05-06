package com.fallmerayer.radathina;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;

import com.fallmerayer.radathina.api.core.ApiClientOptions;
import com.fallmerayer.radathina.api.clients.InternalApiClient;
import com.fallmerayer.radathina.api.core.VolleyCallback;
import com.fallmerayer.radathina.global.Global;
import com.fallmerayer.radathina.menufragments.HomeFragment;
import com.fallmerayer.radathina.menufragments.NotificationFragment;
import com.fallmerayer.radathina.menufragments.RadarFragment;
import com.fallmerayer.radathina.menufragments.SettingsFragment;
import com.fallmerayer.radathina.menufragments.WeatherFragment;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity {

    String[] PERMISSIONS = {
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
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
                    // fragmentTransaction.replace(R.id.fram, new SettingsFragment(), "FragmentName");
                    fragmentTransaction.replace(R.id.fram, new SettingsFragment(), "FragmentSettings");
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

        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        checkPermissions(PERMISSIONS);

        Global.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fram, new HomeFragment(), "FragmentName");
        setTitle("Home");
        fragmentTransaction.commit();

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
        try {
            ScrollView scrollView = findViewById(R.id.attraction_feed);
            scrollView.setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            Log.d("DBG", "Exception");
        }
    }
}
