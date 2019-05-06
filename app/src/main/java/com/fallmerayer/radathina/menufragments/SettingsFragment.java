package com.fallmerayer.radathina.menufragments;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.fallmerayer.radathina.R;
import com.fallmerayer.radathina.global.Global;


public class SettingsFragment extends Fragment {

    View cView;

    TextView settingsTxtViewAndroidID;

    TextInputEditText settingsTxtInputRadarRadius;
    TextInputEditText settingsTxtInputInternalServerIP;
    TextInputEditText settingsTxtInputInternalServerPort;

    CheckBox settingsCheckAttractions;
    CheckBox settingsCheckFood;
    CheckBox settingsCheckShopping;

    Button settingsBtnSave;
    Button settingsBtnRestoreDefaults;

    SharedPreferences sharedPreferences;

    public SettingsFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getActivity().getSharedPreferences("Settings", Activity.MODE_PRIVATE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        cView = inflater.inflate(R.layout.fragment_settings, container, false);

        settingsTxtViewAndroidID = cView.findViewById(R.id.settingsTxtViewAndroidID);

        settingsTxtInputRadarRadius = cView.findViewById(R.id.settingsTxtInputRadarRadius);
        settingsTxtInputInternalServerIP = cView.findViewById(R.id.settingsTxtInputInternalServerIP);
        settingsTxtInputInternalServerPort = cView.findViewById(R.id.txtViewSettingsInternalServerPort);

        settingsCheckAttractions = cView.findViewById(R.id.settingsCheckAttractions);
        settingsCheckFood = cView.findViewById(R.id.settingsCheckFood);
        settingsCheckShopping = cView.findViewById(R.id.settingsCheckShopping);

        settingsBtnSave = cView.findViewById(R.id.settingsBtnSave);
        settingsBtnRestoreDefaults = cView.findViewById(R.id.settingsBtnRestoreDefaults);

        settingsBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sharedPreferences.edit().putFloat(Global.CURRENT_RADAR_RADIUS_METER,
                                Float.valueOf(settingsTxtInputRadarRadius.getText().toString()))
                        .putString(Global.CURRENT_INTERNAL_SERVER_IP,
                                settingsTxtInputInternalServerIP.getText().toString())
                        .putInt(Global.CURRENT_INTERNAL_SERVER_PORT,
                                Integer.valueOf(settingsTxtInputInternalServerPort.getText().toString()))
                        .putBoolean(Global.CURRENT_CHECK_ATTRACTIONS,
                                settingsCheckAttractions.isChecked())
                        .putBoolean(Global.CURRENT_CHECK_FOOD,
                                settingsCheckFood.isChecked())
                        .putBoolean(Global.CURRENT_CHECK_SHOPPING,
                                settingsCheckShopping.isChecked())
                        .apply();
            }


        });

        settingsBtnRestoreDefaults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsTxtInputRadarRadius.setText("1000.0");
                settingsTxtInputInternalServerIP.setText("185.5.199.33");
                settingsTxtInputInternalServerPort.setText("5052");

                settingsCheckAttractions.setChecked(true);
                settingsCheckFood.setChecked(false);
                settingsCheckShopping.setChecked(false);
            }
        });

        return cView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        settingsTxtViewAndroidID.setText(Settings.Secure.getString(getActivity().getContentResolver(),
                Settings.Secure.ANDROID_ID));

        settingsTxtInputRadarRadius.setText("" + sharedPreferences.
                getFloat(Global.CURRENT_RADAR_RADIUS_METER, 1000));
        settingsTxtInputInternalServerIP.setText("" + sharedPreferences.
                getString(Global.CURRENT_INTERNAL_SERVER_IP, "185.5.199.33"));
        settingsTxtInputInternalServerPort.setText("" + sharedPreferences.
                getInt(Global.CURRENT_INTERNAL_SERVER_PORT, 5052));

        settingsCheckAttractions.setChecked(sharedPreferences
                .getBoolean(Global.CURRENT_CHECK_ATTRACTIONS, true));
        settingsCheckFood.setChecked(sharedPreferences
                .getBoolean(Global.CURRENT_CHECK_FOOD, false));
        settingsCheckShopping.setChecked(sharedPreferences
                .getBoolean(Global.CURRENT_CHECK_SHOPPING, false));

    }
}
