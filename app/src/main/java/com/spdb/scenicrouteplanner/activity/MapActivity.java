package com.spdb.scenicrouteplanner.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.spdb.scenicrouteplanner.R;
import com.spdb.scenicrouteplanner.database.MazovianRoutesDbProvider;
import com.spdb.scenicrouteplanner.database.modelDatabase.RoutesDbProvider;
import com.spdb.scenicrouteplanner.database.structures.ScenicRoutesPathStats;
import com.spdb.scenicrouteplanner.service.MapService;
import com.spdb.scenicrouteplanner.service.interfaces.IMapService;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;

import java.util.Locale;

public class MapActivity extends Fragment {
    // ==============================
    // Private fields
    // ==============================
    private MapView mapView;
    private static MapService mapService;
    private static MazovianRoutesDbProvider dbProvider;
    private static ScenicRoutesPathStats scenicRoutesPathStats;

    // ==============================
    // Getters and Setters
    // ==============================

    public static IMapService getMapService() {
        return mapService;
    }

    public static MazovianRoutesDbProvider getDbProvider() {
        return dbProvider;
    }

    public static void setScenicRoutesPathStats (ScenicRoutesPathStats value)
    {
        scenicRoutesPathStats = value;
    }

    public MapActivity() {
        super();

        dbProvider = new MazovianRoutesDbProvider();
        mapService = new MapService(dbProvider);
    }

    // ==============================
    // Override Fragment
    // ==============================
    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                try {
                    if (scenicRoutesPathStats != null) {
                        BoundingBox startMapExtent = mapService.getStartMapExtent(scenicRoutesPathStats.getStartNodeId(),
                                scenicRoutesPathStats.getEndNodeId());
                        if (startMapExtent != null) {
                            mapService.putAllEdgesOnMap();
                            mapView.zoomToBoundingBox(startMapExtent, false);

                            MainActivity main = (MainActivity) getActivity();
                            TextView distance = main.findViewById(R.id.distanceValue);
                            TextView scenicDistance = main.findViewById(R.id.scenicDistanceValue);
                            TextView time = main.findViewById(R.id.timeValue);
                            distance.setText(String.format(Locale.US, "%f", scenicRoutesPathStats.getLength()));
                            scenicDistance.setText(String.format(Locale.US, "%f", scenicRoutesPathStats.getScenicRoutesLength()));
                            time.setText(String.format(Locale.US, "%f", scenicRoutesPathStats.getCost()));
                        }
                    }
                } catch (Exception e) {
                    showAlertDialog(getContext(), com.spdb.scenicrouteplanner.R.string.dialog_alert_title,
                            com.spdb.scenicrouteplanner.R.string.put_route_on_map_error);
                    Log.d("MapActivity", e.getMessage());
                } finally {
                    mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });

        return mapView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mapView != null)
            mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mapView != null)
            mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mapView.onDetach();
    }

    // ==============================
    // Private methods
    // ==============================
    private void showAlertDialog(Context context, int titleId, int msgId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        try {
            builder.setTitle(getResources().getString(titleId))
                    .setMessage(getResources().getString(msgId))
                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } catch (Resources.NotFoundException e) {
            Log.d("ROUTE_PLANNER", "NOT FOUND RESOURCE: " + msgId);
        }
    }
}