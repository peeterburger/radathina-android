package com.fallmerayer.radathina;

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

import com.fallmerayer.radathina.global.Config;
import com.fallmerayer.radathina.global.Global;
import com.fallmerayer.radathina.menufragments.HomeFragment;
import com.fallmerayer.radathina.menufragments.RadarFragment;
import com.fallmerayer.radathina.menufragments.MapFragment;
import com.fallmerayer.radathina.menufragments.SettingsFragment;
import com.fallmerayer.radathina.menufragments.WeatherFragment;
import com.google.android.gms.location.LocationServices;

/**
 * Einstiegspunkt der App. Hier werden das Menü und die Fragmente geladen.
 */
public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener{

    /**
     * Überprüft, ob alle Berechtigungen vom User gegeben sind (has permission). Falls nicht, werden
     * sie von der App angefordert.
     * @param permissionList Eine Liste mit allen Berechtigungen, welche gegeben sein müssen, damit
     *                       die App erfolgreich funtionieren kann.
     * @return true, wenn alle Berechtigungen nach bzw. vor der Abfrage gegeben sind.
     * False, wenn die Abfrage abgelehnt wird.
     */
    private boolean checkPermissions (String... permissionList) {
        if(!hasPermissions(this, permissionList)){

            ActivityCompat.requestPermissions(this, permissionList,
                    Config.PERMISSION_REQUEST_CODE);

            return hasPermissions(this, permissionList);
        }

        return true;
    }

    /**
     * Überprüft, ob alle Berechtigungen vom User gegeben sind.
     * @return true, wenn alle Berechtigungen sind. False, wenn sie noch abgefragt werden müssen.
     */
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context == null || permissions == null)
            return false;

        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // "Versteckt" die ActionBar.
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();

        setContentView(R.layout.activity_main);

        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(this);

        if(checkPermissions(Config.REQUIRED_PERMISSIONS)) {
            Global.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            // Das HomeFragment wird bei App-Start direkt geladen.
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fram, new HomeFragment())
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        // Wird die Zurück-Taste betätigt, wird der Attraction Feed einer Sehenswürdigkeit, falls
        // geöffnet, geschlossen.
        try {
            ScrollView scrollView = findViewById(R.id.attraction_feed);
            scrollView.setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            Log.d("DBG", "Exception thrown at onBackPressed()");
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        switch (menuItem.getItemId()) {
            case R.id.navigation_weather:
                fragmentTransaction.replace(R.id.fram, new WeatherFragment());
                break;

            case R.id.navigation_radar:
                fragmentTransaction.replace(R.id.fram, new RadarFragment());
                break;

            case R.id.navigation_map:
                fragmentTransaction.replace(R.id.fram, new MapFragment());
                break;

            case R.id.navigation_home:
                fragmentTransaction.replace(R.id.fram, new HomeFragment());
                break;

            case R.id.navigation_settings:
                fragmentTransaction.replace(R.id.fram, new SettingsFragment());
        }

        fragmentTransaction.commit();

        return true;
    }

}
