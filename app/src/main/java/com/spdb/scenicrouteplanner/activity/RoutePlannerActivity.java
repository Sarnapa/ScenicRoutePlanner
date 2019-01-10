package com.spdb.scenicrouteplanner.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.spdb.scenicrouteplanner.R;
import com.spdb.scenicrouteplanner.database.RoutesDbProvider;
import com.spdb.scenicrouteplanner.lib.GeoCoords;
import com.spdb.scenicrouteplanner.lib.PathsClassLib;
import com.spdb.scenicrouteplanner.model.Edge;
import com.spdb.scenicrouteplanner.model.Model;
import com.spdb.scenicrouteplanner.model.Node;
import com.spdb.scenicrouteplanner.service.OSMService;
import com.spdb.scenicrouteplanner.utils.AStar;
import com.spdb.scenicrouteplanner.utils.OSMParser;
import com.spdb.scenicrouteplanner.utils.ScenicRoutesGenerator;

import java.util.ArrayList;
import java.util.List;

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

                RoutesDbProvider dbProvider = MapActivity.getDbProvider();

                if (dbProvider.isDbAvailable()) {
                    if (!(startText.isEmpty() || destText.isEmpty() || scenicRouteMinText.isEmpty())) {
                        double scenicRouteMin = Double.parseDouble(scenicRouteMinText);

                        GeoCoords startCoords = osmService.getPlaceCoords(startText);
                        GeoCoords destCoords = osmService.getPlaceCoords(destText);
                        if (startCoords != null && destCoords != null) {
                            Log.d("ROUTE_PLANNER", "COORDS READY");

                            MapActivity.getMapService().removeAllEdges();
                            MapActivity.getMapService().removeStartEndNode();

                            //dbProvider.clearDb();

                            Model model = new Model();
                            List<Edge> route = new ArrayList<>();
                            try {
                                //osmService.getMapExtent(startCoords, destCoords, DEFAULT_MULTIPLIER);
                                //Log.d("ROUTE_PLANNER", "MAPS DOWNLOADED");

                                //OSMParser parser = new OSMParser(dbProvider);
                                //parser.parseOSMFile(PathsClassLib.MAPS_DIRECTORY.concat("/mazowieckie-latest.osm"));
                                //Log.d("ROUTE_PLANNER", "PARSING PROCESS ENDED AND DATABASE UPDATED");

                                /*Log.d("ROUTE_PLANNER", "GENERATING SCENIC ROUTES STARTED");
                                ScenicRoutesGenerator scenicRoutesGenerator = new ScenicRoutesGenerator(dbProvider);
                                scenicRoutesGenerator.generate();
                                Log.d("ROUTE_PLANNER", "GENERATING SCENIC ROUTES ENDED");*/

                                long startNodeId = dbProvider.getClosestNodeId(startCoords);
                                long endNodeId = dbProvider.getClosestNodeId(destCoords);
                                Node startNode = dbProvider.getNodeById(startNodeId);
                                Node endNode = dbProvider.getNodeById(endNodeId);
                                Log.d("ROUTE_PLANNER", "START NODE:" + startNodeId);
                                Log.d("ROUTE_PLANNER", "END NODE:" + endNodeId);

                                //AStar shortestPath = new AStar(dbProvider);
                                //route = shortestPath.aStar(model.getNodeById(startNodeId), model.getNodeById(endNodeId));

                                MapActivity.getMapService().setStartMapExtent(startCoords, destCoords);
                                MapActivity.getMapService().setStartNode(startNode);
                                MapActivity.getMapService().setEndNode(endNode);

                                /*double multiplier = DEFAULT_MULTIPLIER;
                                while (route.isEmpty()) {
                                    Log.d("ROUTE_PLANNER", "SHORTEST PATH NOT FOUND");

                                    Log.d("ROUTE_PLANNER", "EXPANDING MAP AREA");
                                    multiplier = multiplier + MULTIPLIER_HOP;
                                    osmService.getMapExtent(startCoords, destCoords, multiplier);

                                    parser.parseOSMFile(PathsClassLib.MAPS_DIRECTORY.concat("/osm"));
                                    Log.d("ROUTE_PLANNER", "FILES PARSED");

                                    Log.d("ROUTE_PLANNER", "ADDING WAYS STARTED");
                                    dbProvider.addWays(model.getWaysList());
                                    Log.d("ROUTE_PLANNER", "ADDING WAYS ENDED");
                                    Log.d("ROUTE_PLANNER", "ADDING EDGES STARTED");
                                    dbProvider.addEdges(model.getEdgesList());
                                    Log.d("ROUTE_PLANNER", "ADDING EDGED ENDED");

                                    model.setEdges(dbProvider.getAllEdges());
                                    Log.d("ROUTE_PLANNER", "MODEL UPDATED");

                                    route = shortestPath.aStar(model.getNodeById(startNodeId), model.getNodeById(endNodeId));
                                }

                                Log.d("ROUTE_PLANNER", "SHORTEST PATH FOUND WITH ASTAR");*/

                            }
                            catch (IllegalArgumentException e)
                            {
                                //TODO:Invalid coords or map too big!
                                Log.e("ROUTE_PLANNER", "INVALID COORDS OR MAP TOO BIG");
                            }
                            catch (Exception e)
                            {
                                //TODO:You requested too many nodes (limit is 50000). Either request a smaller area, or use planet.osm
                                e.printStackTrace();
                            }
                            finally
                            {
                                Log.d("ROUTE_PLANNER", "PUT ROUTE ON MAP");
                                //MapActivity.getMapService().addEdges(model.getEdgesList());
                                //MapActivity.getMapService().addEdges(route);
                                Log.d("ROUTE_PLANNER", "READY TO RETURN TO MAP");
                                getFragmentManager().popBackStack();
                            }
                        } else {
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
