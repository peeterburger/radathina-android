package com.fallmerayer.radathina.menufragments;


import android.app.ProgressDialog;
import android.location.Location;
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
import com.fallmerayer.radathina.api.clients.myweather.common.Common;
import com.fallmerayer.radathina.api.clients.myweather.helper.Helper;
import com.fallmerayer.radathina.api.clients.myweather.model.OpenWeatherMap;
import com.fallmerayer.radathina.global.Global;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;

public class WeatherFragment extends Fragment {

    private View wView;

    private TextView txtCity, txtLastUpdate, txtDescription, txtHumidity, txtTime, txtTimeh, txtCelcius;
    private ImageView imageView;

    private OpenWeatherMap openWeatherMap = new OpenWeatherMap();

    public WeatherFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("WEATHER", "onCreate");
        super.onCreate(savedInstanceState);

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(600000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                Location location = locationResult.getLastLocation();

                new GetWeather()
                        .execute(Common.apiRequest(
                                String.valueOf(location.getLatitude()),
                                String.valueOf(location.getLongitude())
                        ));

            }
        };

        try {

            Global.fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback, null);

            Global.fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {

                            if (location != null) {
                                new GetWeather()
                                        .execute(Common.apiRequest(
                                                String.valueOf(location.getLatitude()),
                                                String.valueOf(location.getLongitude())
                                        ));
                            }
                        }
                    });

        } catch (SecurityException se) {
            Log.d("DBG", "GPS Permission denied");
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

    private class GetWeather extends AsyncTask<String, Void, String> {
        ProgressDialog pd = new ProgressDialog(getActivity());

        @Override
        protected String doInBackground(String... params) {
            String stream;
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

}
