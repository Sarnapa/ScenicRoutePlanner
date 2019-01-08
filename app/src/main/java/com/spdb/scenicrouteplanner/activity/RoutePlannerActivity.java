package com.spdb.scenicrouteplanner.activity;

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
import com.spdb.scenicrouteplanner.service.OSMService;
import com.spdb.scenicrouteplanner.utils.AStar;
import com.spdb.scenicrouteplanner.utils.OSMParser;

import java.util.List;

public class RoutePlannerActivity extends Fragment {
    // ==============================
    // Private fields
    // ==============================
    private View routePlannerView;
    private EditText startLocationEditText;
    private EditText destinationEditText;
    private EditText scenicRouteMinEditText;

    private OSMService osmService;

    public RoutePlannerActivity()
    {
        super();

        this.osmService = new OSMService();
    }

    @Override
    public void onCreate(Bundle savedInstance)
    {
        super.onCreate(savedInstance);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        routePlannerView = inflater.inflate(R.layout.activity_route_planner, container, false);

        if (routePlannerView != null)
        {
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
            public void onClick(View v)
            {
                String start = startLocationEditText.getText().toString();
                String dest = destinationEditText.getText().toString();
                String scenicRouteMin = scenicRouteMinEditText.getText().toString();

                RoutesDbProvider dbProvider = MapActivity.getDbProvider();

                if (dbProvider.isDbAvailable()) {
                    if (!(start.isEmpty() || dest.isEmpty() || scenicRouteMin.isEmpty())) {
                        MapActivity.getMapService().removeAllEdges();
                        MapActivity.getMapService().removeStartEndNode();

                        dbProvider.clearDb();

                        GeoCoords startCoords = osmService.getPlaceCoords(start);
                        GeoCoords destCoords = osmService.getPlaceCoords(dest);
                        Log.d("ROUTE_PLANNER", "COORDS READY");

                        try {
                            osmService.getMapExtent(startCoords, destCoords);
                            Log.d("ROUTE_PLANNER", "MAPS DOWNLOADED");

                            OSMParser parser = new OSMParser();
                            Model model = parser.parseOSMFile(PathsClassLib.MAPS_DIRECTORY.concat("/osm"));
                            Log.d("ROUTE_PLANNER", "FILES PARSED");

                            Log.d("ROUTE_PLANNER", "ADDING WAYS STARTED");
                            dbProvider.addWays(model.getWaysList());
                            Log.d("ROUTE_PLANNER", "ADDING WAYS ENDED");
                            Log.d("ROUTE_PLANNER", "ADDING EDGES STARTED");
                            dbProvider.addEdges(model.getEdgesList());
                            Log.d("ROUTE_PLANNER", "ADDING EDGED ENDED");
                            model.setEdges(dbProvider.getAllEdges());
                            Log.d("ROUTE_PLANNER", "DATABASE UPDATED");

                            long startNodeId = dbProvider.getClosestNodeId(startCoords);
                            long endNodeId = dbProvider.getClosestNodeId(destCoords);
                            Log.d("ROUTE_PLANNER", "START NODE:" + startNodeId);
                            Log.d("ROUTE_PLANNER", "END NODE:" + endNodeId);

                            AStar shortestPath = new AStar(dbProvider);
                            List<Edge> route = shortestPath.aStar(model.getNodeById(startNodeId), model.getNodeById(endNodeId));

                            MapActivity.getMapService().addEdges(model.getEdgesList());
                            MapActivity.getMapService().setStartMapExtent(startCoords, destCoords);
                            MapActivity.getMapService().setStartNode(model.getNodeById(startNodeId));
                            MapActivity.getMapService().setEndNode(model.getNodeById(endNodeId));
                            if(route != null) {
                                Log.d("ROUTE_PLANNER", "SHORTEST PATH FOUND WITH ASTAR");

                                MapActivity.getMapService().addEdges(route);
                            } else {
                                Log.d("ROUTE_PLANNER", "SHORTEST PATH NOT FOUND");
                            }


                        } catch(IllegalArgumentException e){
                            //TODO:Invalid coords or map too big!
                            Log.e("ROUTE_PLANNER", "INVALID COORDS OR MAP TOO BIG");
                        } catch (Exception e) {
                            //TODO:You requested too many nodes (limit is 50000). Either request a smaller area, or use planet.osm
                            e.printStackTrace();
                        } finally {
                            getFragmentManager().popBackStack();
                        }
                    }
                } else {
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
}
