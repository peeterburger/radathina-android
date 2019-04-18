package com.fallmerayer.radathina;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fallmerayer.radathina.api.InternalApiClient;
import com.fallmerayer.radathina.api.VolleyCallback;
import com.fallmerayer.radathina.menufragments.HomeFragment;
import com.fallmerayer.radathina.menufragments.NotificationFragment;
import com.fallmerayer.radathina.menufragments.RadarFragment;
import com.fallmerayer.radathina.menufragments.SettingsFragment;
import com.fallmerayer.radathina.menufragments.WeatherFragment;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity {

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

        // testInternalApi();
    }

    private void testInternalApi() {
        InternalApiClient internalApiClient = new InternalApiClient(this)
                .protocol("http")
                .host("192.168.1.100")
                .port(12345)
                .apiPath("/api/v1");

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
