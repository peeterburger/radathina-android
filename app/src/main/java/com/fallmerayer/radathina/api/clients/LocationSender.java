package com.example.jonib.exploreathens;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class LocationSender {
    private String url = "http://185.5.199.33:5051/addLocation/";

    private Context context;

    public LocationSender(Context context){
        this.context = context;
    }

    //Der eigene Standort  mit Android ID wird an den Webservice der Webseite gesendet
    public void sendLocation(String lat, String lng){
        url += Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID).toString();
        url += "/" + lat + "/" + lng;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> {
            if (response.matches("NaN")) {
                Toast.makeText(context, "Fehler: Der Standort enthält ungültige Zeichen", Toast.LENGTH_LONG).show();
            } else if (response.matches("ID ungültig")) {
                Toast.makeText(context, "Fehler: Die gesendete AndroidID ist nicht registriert oder falsch", Toast.LENGTH_LONG).show();
            }else if(response.matches("Koordinaten ungültig")){
                Toast.makeText(context, "Fehler: Der gesendete Standort enthält ungültige Koordinaten", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(context, "Standort gesendet", Toast.LENGTH_SHORT).show();
            }
        }, error -> Toast.makeText(context, "Fehler: Der Server hat die Anfrage abgelehnt", Toast.LENGTH_LONG).show());
        Volley.newRequestQueue(context).add(stringRequest);
    }
}
