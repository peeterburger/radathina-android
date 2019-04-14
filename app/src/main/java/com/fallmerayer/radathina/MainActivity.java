package com.fallmerayer.radathina;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.fallmerayer.radathina.menufragments.HomeFragment;
import com.fallmerayer.radathina.menufragments.NotificationFragment;
import com.fallmerayer.radathina.menufragments.RadarFragment;
import com.fallmerayer.radathina.menufragments.SettingsFragment;
import com.fallmerayer.radathina.menufragments.WeatherFragment;

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
    }

}
