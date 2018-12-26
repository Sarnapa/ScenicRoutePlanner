package com.spdb.scenicrouteplanner.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spdb.scenicrouteplanner.R;

import org.mapsforge.map.layer.download.tilesource.TileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.views.MapView;

import java.util.Objects;

public class MapActivity extends Fragment
{
    // ==============================
    // Private fields
    // ==============================
    private MapView mapView;

    // ==============================
    // Override Fragment
    // ==============================
    @Override
    public void onCreate(Bundle savedInstance)
    {
        super.onCreate(savedInstance);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mapView = new MapView(inflater.getContext());
        mapView.setMultiTouchControls(true);
        mapView.setHorizontalMapRepetitionEnabled(false);
        mapView.setVerticalMapRepetitionEnabled(false);
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        //setMapViewTilesSource();

        return mapView;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (mapView != null)
            mapView.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        if (mapView != null)
            mapView.onPause();
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();

        mapView.onDetach();
    }

    // ==============================
    // Private methods
    // ==============================

    private void setMapViewTilesSource()
    {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int newScale = (int) (256 * displayMetrics.density);

        String[] OSMSource = new String[2];
        OSMSource[0] = "http://a.tile.openstreetmap.org/";
        OSMSource[1] = "http://b.tile.openstreetmap.org/";
        XYTileSource mapSource = new XYTileSource(
                "OSM",
                1,
                18,
                newScale,
                ".png",
                OSMSource
        );

        mapView.setTileSource(mapSource);
    }
}