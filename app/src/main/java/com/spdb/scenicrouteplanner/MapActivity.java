package com.spdb.scenicrouteplanner;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;

public class MapActivity extends AppCompatActivity
{
    private MapView mapView = null;

    @Override
    public void onCreate(Bundle savedInstance)
    {
        super.onCreate(savedInstance);

        Context appCtx = getApplicationContext();
        Configuration.getInstance().load(appCtx,
                PreferenceManager.getDefaultSharedPreferences(appCtx));

        setContentView(R.layout.activity_map);
        mapView = findViewById(R.id.mapView);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        Context appCtx = getApplicationContext();
        Configuration.getInstance().load(appCtx,
                PreferenceManager.getDefaultSharedPreferences(appCtx));
        if (mapView != null)
            mapView.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        Context appCtx = getApplicationContext();
        Configuration.getInstance().load(appCtx,
                PreferenceManager.getDefaultSharedPreferences(appCtx));
        if (mapView != null)
            mapView.onPause();
    }

}