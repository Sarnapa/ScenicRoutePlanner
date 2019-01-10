package com.spdb.scenicrouteplanner.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.spdb.scenicrouteplanner.database.RoutesDbProvider;
import com.spdb.scenicrouteplanner.service.MapService;
import com.spdb.scenicrouteplanner.service.interfaces.IMapService;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;

public class MapActivity extends Fragment
{
    // ==============================
    // Private fields
    // ==============================
    private MapView mapView;
    private static MapService mapService;
    private static RoutesDbProvider dbProvider;

    // ==============================
    // Getters and Setters
    // ==============================

    public static IMapService getMapService()
    {
        return mapService;
    }
    public static RoutesDbProvider getDbProvider()
    {
        return dbProvider;
    }


    public MapActivity()
    {
        super();

        dbProvider = new RoutesDbProvider();
        mapService = new MapService(dbProvider);
    }

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

        mapView.setTilesScaledToDpi(true);

        TileSystem tileSystem = MapView.getTileSystem();
        mapView.setScrollableAreaLimitLatitude(tileSystem.getMaxLatitude(),
                tileSystem.getMinLatitude(), 0);
        mapView.setScrollableAreaLimitLongitude(tileSystem.getMinLongitude(),
                tileSystem.getMaxLongitude(), 0);

        mapView.setMinZoomLevel(3.0);
        mapView.setMaxZoomLevel(18.0);

        // Żeby się szybciej kafelki ładowały - do sprawdzenia
        mapView.getTileProvider().getTileCache().getProtectedTileComputers().clear();
        mapView.getTileProvider().getTileCache().setAutoEnsureCapacity(false);

        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);

        mapService.setMapOverlayManager(mapView.getOverlayManager());
        mapService.putAllEdgesOnMap();
        mapService.putStartEndNodeOnMap();

        mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                BoundingBox startMapExtent = mapService.getStartMapExtent();
                if (startMapExtent != null)
                {
                    mapView.zoomToBoundingBox(startMapExtent, false);
                }
                mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

            }
        });

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
}