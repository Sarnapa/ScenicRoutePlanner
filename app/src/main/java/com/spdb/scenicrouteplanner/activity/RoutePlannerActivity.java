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

import java.util.Locale;

public class RoutePlannerActivity extends Fragment {
    // ==============================
    // Private fields
    // ==============================

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
                // Pobranie danych wejściowych do algorytmu.
                String startText = startLocationEditText.getText().toString();
                String destText = destinationEditText.getText().toString();
                String scenicRouteMinText = scenicRouteMinEditText.getText().toString();

                // Pobranie modułu do pracy z bazą danych.
                MazovianRoutesDbProvider dbProvider = MapActivity.getDbProvider();
                // Wyczyszczenie mapy.
                MapActivity.getMapService().clear();

                if (dbProvider.isDbAvailable()) {
                    if (!(startText.isEmpty() || destText.isEmpty() || scenicRouteMinText.isEmpty())) {
                        // km -> m
                        double scenicRouteMin = Double.parseDouble(scenicRouteMinText) * 1000;

                        // Pobranie współrzędnych podanych lokalizacji, korzystając z REST serwisu.
                        GeoCoords startCoords = osmService.getPlaceCoords(startText);
                        GeoCoords destCoords = osmService.getPlaceCoords(destText);
                        if (startCoords != null && destCoords != null) {
                            Log.d("ROUTE_PLANNER", "COORDS READY");
                            try
                            {
                                // Pobranie identyfikatorów węzłów, które odpowiadają podanym lokalizacjom.
                                long startNodeId = dbProvider.getClosestNodeId(startCoords);
                                long endNodeId = dbProvider.getClosestNodeId(destCoords);
                                Log.d("ROUTE_PLANNER", "START NODE:" + startNodeId);
                                Log.d("ROUTE_PLANNER", "END NODE:" + endNodeId);

                                if (startNodeId >= 0 && endNodeId >= 0)
                                {
                                    boolean success = findScenicRoutesPath(dbProvider, startNodeId, endNodeId, scenicRouteMin);
                                    if (success)
                                    {
                                        MapActivity.setScenicRoutesPathStats(dbProvider.getScenicRoutesPathStats());
                                    }
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
    private boolean findScenicRoutesPath(MazovianRoutesDbProvider dbProvider, long startNodeId, long endNodeId,
                                      double scenicRouteMin)
    {
        // Stałe algorytmu.
        final double BUFFER_INC = 0.1f;
        final double BUFFER_EXTRA = 1.1f;

        // Zmienne lokalne algorytmu.
        double buffer = 0.05f;
        long lastStartBufferSize = 0;
        double currentScenicRoutesLength;

        // Przygotowanie bazy do pracy z algorytmem.
        dbProvider.prepareDb();
        MapActivity.setScenicRoutesPathStats(null);

        // Wyszukanie optymalnej trasy dla podanych lokalizacji.
        dbProvider.getShortestPath(startNodeId, endNodeId);
        Log.d("ROUTE_PLANNER", "SHORTEST PATH FOUND");

        currentScenicRoutesLength = dbProvider.getShortestPathScenicRoutesSumLength();
        // Czy optymalna trasa spełnia warunek minimalnej długości odcinków widokowych?
        if (currentScenicRoutesLength >= scenicRouteMin)
        {
            Log.d("ROUTE_PLANNER", "SHORTEST PATH HAVE MET SCENIC ROUTES CONDITION");
            dbProvider.moveShortestPathToScenicRoutesPath();
        }
        // Nie spełnia, ale nie ma także błędu.
        else if (currentScenicRoutesLength != -1)
        {
            Log.d("ROUTE_PLANNER", "SHORTEST PATH HAVE NOT MET SCENIC ROUTES CONDITION");

            double scenicRoutesLengthInBuffer = 0.0f;
            while (scenicRoutesLengthInBuffer < scenicRouteMin * BUFFER_EXTRA)
            {
                // Pobranie bufora odcinków widokowych, znajdujących się
                // w odległości nie większej niż zdefiniowana.
                dbProvider.getScenicRoutesBuffer(buffer);
                long currentStartBufferSize = dbProvider.getScenicRoutesBufferSize();
                Log.d("ROUTE_PLANNER", String.format(Locale.US, "BUFFER SIZE - %d", currentStartBufferSize));
                // Czy przestaliśmy pobierać nowe odcinki widokowe?
                if (currentStartBufferSize == lastStartBufferSize) {
                    Log.d("ROUTE_PLANNER", String.format("GET THE SAME BUFFER SIZE AGAIN"));
                    return false;
                }
                lastStartBufferSize = currentStartBufferSize;
                scenicRoutesLengthInBuffer = dbProvider.getScenicRoutesBufferScenicRoutesSumLength();
                // Zwiększenie rozmiaru bufora.
                buffer += BUFFER_INC;
            }

            // Dopóki nie spełnimy warunku minimalnej długości odcinków widokowych.
            while(currentScenicRoutesLength < scenicRouteMin)
            {
                // Posprzątanie z tabeli wynikowej dotychczasowego wyniku.
                dbProvider.cleanScenicRoutesPath();

                long currentBufferSize = lastStartBufferSize;
                long currentNodeId = startNodeId;
                Log.d("ROUTE_PLANNER", String.format("START_NODE - %d", currentNodeId));
                currentScenicRoutesLength = 0.0f;

                dbProvider.startTransaction();
                while (currentBufferSize > 0) {
                    currentNodeId = dbProvider.getPathToNearestScenicRoute(currentNodeId);
                    Log.d("ROUTE_PLANNER", String.format("CURRENT_NODE - %d", currentNodeId));

                    currentScenicRoutesLength = dbProvider.getScenicRoutesPathScenicRoutesSumLength();
                    // Warunek minimalnej dlugości odcinków widokowych spełniony.
                    if (currentScenicRoutesLength >= scenicRouteMin)
                    {
                        Log.d("ROUTE_PLANNER", String.format("CURRENT PATH HAVE MET SCENIC ROUTES CONDITION"));
                        // Docieramy do celu po optymalnej trasie.
                        dbProvider.getPathToDestination(currentNodeId, endNodeId);
                        break;
                    }
                    currentBufferSize = dbProvider.getScenicRoutesBufferSize();
                }
                dbProvider.commitTransaction();

                // Jeśli błąd.
                if (currentNodeId == -1 || currentBufferSize == -1 || currentScenicRoutesLength == -1) {
                    Log.d("ROUTE_PLANNER", "ERROR OCCURIED");
                    break;
                }
            }
        }
        if (currentScenicRoutesLength >= scenicRouteMin)
        {
            Log.d("ROUTE_PLANNER", "SCENIC ROUTES PATH FOUND");
            return true;
        }
        else
        {
            Log.d("ROUTE_PLANNER", "SCENIC ROUTES PATH NOT FOUND");
            return false;
        }
    }


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
