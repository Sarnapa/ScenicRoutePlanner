package com.spdb.scenicrouteplanner.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.spdb.scenicrouteplanner.R;
import com.spdb.scenicrouteplanner.database.MazovianRoutesDbProvider;
import com.spdb.scenicrouteplanner.lib.GeoCoords;
import com.spdb.scenicrouteplanner.service.OSMService;

public class RoutePlannerActivity extends Fragment {
    // ==============================
    // Private fields
    // ==============================
    private static final double DEFAULT_MULTIPLIER = 1.0;
    private static final double MULTIPLIER_HOP = 1.0;

    private View routePlannerView;
    private EditText startLocationEditText;
    private EditText destinationEditText;
    private EditText scenicRouteMinEditText;

    private OSMService osmService;

    // ==============================
    // Constructors
    // ==============================
    public RoutePlannerActivity() {
        super();

        this.osmService = new OSMService();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        routePlannerView = inflater.inflate(R.layout.activity_route_planner, container, false);

        if (routePlannerView != null) {
            startLocationEditText = routePlannerView.findViewById(R.id.start_location);
            startLocationEditText.setText("Warszawa, Nowowiejska 30");
            destinationEditText = routePlannerView.findViewById(R.id.destination);
            destinationEditText.setText("Warszawa, Nowogrodzka 70");
            scenicRouteMinEditText = routePlannerView.findViewById(R.id.scenic_route_min_length);
            scenicRouteMinEditText.setHint("Distance in kilometers");
        }

        return routePlannerView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final ImageButton findRoutePlannerButton = getView().findViewById(R.id.find_route_planner_button);

        findRoutePlannerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startText = startLocationEditText.getText().toString();
                String destText = destinationEditText.getText().toString();
                String scenicRouteMinText = scenicRouteMinEditText.getText().toString();

                MazovianRoutesDbProvider dbProvider = MapActivity.getDbProvider();
                MapActivity.getMapService().clear();

                if (dbProvider.isDbAvailable()) {
                    if (!(startText.isEmpty() || destText.isEmpty() || scenicRouteMinText.isEmpty())) {
                        double scenicRouteMin = Double.parseDouble(scenicRouteMinText);

                        GeoCoords startCoords = osmService.getPlaceCoords(startText);
                        GeoCoords destCoords = osmService.getPlaceCoords(destText);
                        if (startCoords != null && destCoords != null) {
                            Log.d("ROUTE_PLANNER", "COORDS READY");
                            try
                            {
                                long startNodeId = dbProvider.getClosestNodeId(startCoords);
                                long endNodeId = dbProvider.getClosestNodeId(destCoords);
                                Log.d("ROUTE_PLANNER", "START NODE:" + startNodeId);
                                Log.d("ROUTE_PLANNER", "END NODE:" + endNodeId);

                                if (startNodeId >= 0 && endNodeId >= 0)
                                {
                                    dbProvider.calculateShortestPath(startNodeId, endNodeId);
                                    Log.d("ROUTE_PLANNER", "SHORTEST PATH FOUND WITH ASTAR");
                                    getFragmentManager().popBackStack();
                                }
                                else
                                {
                                    if (startNodeId < 0) {
                                        showAlertDialog(getContext(), R.string.dialog_alert_title,
                                                R.string.not_found_start_point);
                                        Log.d("ROUTE_PLANNER", "NOT FOUND START - IN DB");
                                    }
                                    if (endNodeId < 0) {
                                        showAlertDialog(getContext(), R.string.dialog_alert_title,
                                                R.string.not_found_destination);
                                        Log.d("ROUTE_PLANNER", "NOT FOUND DESTINATION - IN DB");
                                    }
                                }

                            }
                            catch (Exception e)
                            {
                                showAlertDialog(getContext(), R.string.dialog_alert_title,
                                        R.string.find_route_process_error);
                                Log.e("ROUTE_PLANNER", "ERROR IN ALGORITHM CODE");
                            }
                        }
                        else {
                            if (startCoords == null) {
                                showAlertDialog(getContext(), R.string.dialog_alert_title,
                                        R.string.not_found_start_point);
                                Log.d("ROUTE_PLANNER", "NOT FOUND START");
                            }
                            if (destCoords == null) {
                                showAlertDialog(getContext(), R.string.dialog_alert_title,
                                        R.string.not_found_destination);
                                Log.d("ROUTE_PLANNER", "NOT FOUND DESTINATION");
                            }
                        }
                    } else {
                        if (startText.isEmpty()) {
                            showAlertDialog(getContext(), R.string.dialog_alert_title,
                                    R.string.start_field_empty);
                            Log.d("ROUTE_PLANNER", "START FIELD IS EMPTY");
                        }
                        if (destText.isEmpty()) {
                            showAlertDialog(getContext(), R.string.dialog_alert_title,
                                    R.string.destination_field_empty);
                            Log.d("ROUTE_PLANNER", "DESTINATION FIELD IS EMPTY");
                        }
                        if (scenicRouteMinText.isEmpty()) {
                            showAlertDialog(getContext(), R.string.dialog_alert_title,
                                    R.string.scenic_route_min_field_empty);
                            Log.d("ROUTE_PLANNER", "SCENIC ROUTES MINIMUM DISTANCE FIELD IS EMPTY");
                        }
                    }
                } else {
                    showAlertDialog(getContext(), R.string.dialog_alert_title,
                            R.string.database_not_available);
                    Log.d("ROUTE_PLANNER", "DATABASE NOT AVAILABLE");
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    // ==============================
    // Private methods
    // ==============================

    private void showAlertDialog(Context context, int titleId, int msgId)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        try
        {
            builder.setTitle(getResources().getString(titleId))
                    .setMessage(getResources().getString(msgId))
                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        catch (Resources.NotFoundException e)
        {
            Log.d("ROUTE_PLANNER", "NOT FOUND RESOURCE: " + msgId);
        }
    }
}
