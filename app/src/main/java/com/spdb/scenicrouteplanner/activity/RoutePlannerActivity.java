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
import com.spdb.scenicrouteplanner.lib.OSM.Place;
import com.spdb.scenicrouteplanner.model.Model;
import com.spdb.scenicrouteplanner.service.OSMService;
import com.spdb.scenicrouteplanner.utils.OSMParser;

import org.spatialite.database.SQLiteDatabase;

public class RoutePlannerActivity extends Fragment {
    // ==============================
    // Private fields
    // ==============================
    private View routePlannerView;
    private OSMService osmService;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        this.osmService = new OSMService();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        routePlannerView = inflater.inflate(R.layout.activity_route_planner, container, false);

        return routePlannerView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final ImageButton findRoutePlannerButton = getView().findViewById(R.id.find_route_planner_button);

        findRoutePlannerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapActivity.getMapService().removeAllEdges();
                // Tutaj dostarczenie tych edgow

                EditText start_location = routePlannerView.findViewById(R.id.start_location);
                String start = start_location.getText().toString();
                EditText destination = routePlannerView.findViewById(R.id.destination);
                String dest = destination.getText().toString();
                EditText scenic_route_min_ = routePlannerView.findViewById(R.id.scenic_route_min_length);
                String scenicRouteMin = destination.getText().toString();

                OSMService osmService = new OSMService();
                GeoCoords startCoords = osmService.getPlaceCoords(start);
                GeoCoords destCoords = osmService.getPlaceCoords(dest);
                osmService.getMapExtent(startCoords, destCoords);

                OSMParser parser = new OSMParser();
                Model model = parser.parseOSMFile(Environment.getExternalStorageDirectory() + "/SRP/maps/osm");
                MapActivity.getMapService().addEdges(model.getEdges());

                RoutesDbProvider routesDbProvider = new RoutesDbProvider(getContext());
                SQLiteDatabase db = routesDbProvider.getRoutesDb();

                getFragmentManager().popBackStack();
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
