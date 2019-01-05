package com.spdb.scenicrouteplanner.activity;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.spdb.scenicrouteplanner.R;
import com.spdb.scenicrouteplanner.database.RoutesDbProvider;
import com.spdb.scenicrouteplanner.lib.GeoCoords;
import com.spdb.scenicrouteplanner.model.Model;
import com.spdb.scenicrouteplanner.service.OSMService;
import com.spdb.scenicrouteplanner.utils.OSMParser;

import org.spatialite.database.SQLiteDatabase;

public class RoutePlannerActivity extends Fragment {
    // ==============================
    // Private fields
    // ==============================
    private View routePlannerView;
    private EditText startLocationEditText;
    private EditText destinationEditText;
    private EditText scenicRouteMinEditText;

    private OSMService osmService;

    public RoutePlannerActivity() {
        super();

        this.osmService = new OSMService();
    }

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
            scenicRouteMinEditText.setText("100");
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
                String start = startLocationEditText.getText().toString();
                String dest = destinationEditText.getText().toString();
                String scenicRouteMin = scenicRouteMinEditText.getText().toString();

                if (!(start.isEmpty() || dest.isEmpty() || scenicRouteMin.isEmpty())) {
                    MapActivity.getMapService().removeAllEdges();

                    GeoCoords startCoords = osmService.getPlaceCoords(start);
                    GeoCoords destCoords = osmService.getPlaceCoords(dest);
                    Double latDiff = Math.abs(startCoords.getLatitude()-destCoords.getLatitude());
                    Double lonDiff = Math.abs(startCoords.getLongitude()-destCoords.getLongitude());
                    if((latDiff > 0.15) || (lonDiff > 0.15)){
                        //TODO:Alert
                    } else {
                        osmService.getMapExtent(startCoords, destCoords);
                        try {
                            OSMParser parser = new OSMParser();
                            Model model = parser.parseOSMFile(Environment.getExternalStorageDirectory() + "/SRP/maps/osm");
                            MapActivity.getMapService().addEdges(model.getEdgesList());
                        } catch (Exception e) {
                            //TODO:You requested too many nodes (limit is 50000). Either request a smaller area, or use planet.osm
                            e.printStackTrace();
                        }
                        RoutesDbProvider routesDbProvider = new RoutesDbProvider(getContext());
                        SQLiteDatabase db = routesDbProvider.getRoutesDb();

                        // Rozwiązanie chwilowe - start i koniec bedzie czytany z bazy
                        MapActivity.getMapService().setStartMapExtent(startCoords, destCoords);

                        getFragmentManager().popBackStack();
                    }
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
