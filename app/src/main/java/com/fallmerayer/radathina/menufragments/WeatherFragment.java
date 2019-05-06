package com.fallmerayer.radathina.menufragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fallmerayer.radathina.R;
import com.fallmerayer.radathina.background.BackgroundService;
import com.fallmerayer.radathina.api.clients.myweather.common.Common;
import com.fallmerayer.radathina.api.clients.myweather.helper.Helper;
import com.fallmerayer.radathina.api.clients.myweather.model.OpenWeatherMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;

public class WeatherFragment extends Fragment implements LocationListener {

    private View wView;

    private TextView txtCity, txtLastUpdate, txtDescription, txtHumidity, txtTime, txtTimeh, txtCelcius;
    private ImageView imageView;

    public static long     DEFAULT_LOCATION_REFRESH_TIME_MILLIS = 100000;
    public static float    DEFAULT_LOCATION_REFRESH_DISTANCE_METERS = 1000;

    private LocationManager locationManager;
    private String provider;

    private static double lat, lng;
    private OpenWeatherMap openWeatherMap = new OpenWeatherMap();

    public WeatherFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("WEATHER", "onCreate");
        super.onCreate(savedInstanceState);

        locationManager = (LocationManager) this.getActivity().getSystemService(
                Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);

        try {

            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, DEFAULT_LOCATION_REFRESH_TIME_MILLIS,
                    DEFAULT_LOCATION_REFRESH_DISTANCE_METERS,
                    this);

            if (BackgroundService.getLastKnownLocation() != null) {

                LatLng location = BackgroundService.getLastKnownLatLng();

                Log.e("DBG", "" + location);

                lat = location.latitude;
                lng = location.longitude;

                new GetWeather().execute(Common.apiRequest(String.valueOf(lat),String.valueOf(lng)));
            }

        } catch (SecurityException se) {
            Log.d("DBG", "Permission denied");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("WEATHER", "onCreateView");

        wView = inflater.inflate(R.layout.fragment_weather, container, false);

        txtCity = wView.findViewById(R.id.txtCity);

        txtCity.setText("Lade Wetter...");

        txtLastUpdate = wView.findViewById(R.id.txtLastUpdate);
        txtDescription = wView.findViewById(R.id.txtDescription);
        txtHumidity = wView.findViewById(R.id.txtHumidity);
        txtTime = wView.findViewById(R.id.txtTime);
        txtTimeh = wView.findViewById(R.id.txtTimeh);
        txtCelcius = wView.findViewById(R.id.txtCelcius);
        imageView = wView.findViewById(R.id.imageView);

        return wView;
    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();

        Log.d("DBG", "onLocationChanged: ");

        try {
            new GetWeather().execute(Common.apiRequest(String.valueOf(lat),String.valueOf(lng)));
        } catch (NullPointerException npe) {
            Log.d("DBG", "NullPointerException");
        }
    }

    private class GetWeather extends AsyncTask<String, Void, String> {
        ProgressDialog pd = new ProgressDialog(getActivity());


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // pd.setTitle("Bitte warten...");
            // pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String stream = null;
            String urlString = params[0];
            Helper http = new Helper();
            stream = http.getHTTPData(urlString);
            return stream;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(s.contains("Error: Not found city")){
                pd.dismiss();
                return;
            }
            Gson gson = new Gson();
            Type mType = new TypeToken<OpenWeatherMap>(){}.getType();
            openWeatherMap = gson.fromJson(s,mType);
            pd.dismiss();

            txtCity.setText(String.format("%s,%s", openWeatherMap.getName(), openWeatherMap.getSys().getCountry()));
            txtLastUpdate.setText(String.format("Zuletzt aktualisiert: %s", Common.getDateNow()));
            txtDescription.setText(String.format("%s", openWeatherMap.getWeather().get(0).getDescription()));
            txtHumidity.setText(String.format("Luftfeuchtigkeit %d%%", openWeatherMap.getMain().getHumidity()));
            txtTime.setText(String.format("Sonnenaufgang: %s", Common.unixTimeStampToDateTime(openWeatherMap.getSys().getSunrise())));
            txtTimeh.setText(String.format("Sonnenuntergang: %s", Common.unixTimeStampToDateTime(openWeatherMap.getSys().getSunset())));
            txtCelcius.setText(String.format("%.0f°", openWeatherMap.getMain().getTemp()));
            Picasso.with(getActivity())
                    .load(Common.getImage(openWeatherMap.getWeather().get(0).getIcon()))
                    .into(imageView);
        }

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
