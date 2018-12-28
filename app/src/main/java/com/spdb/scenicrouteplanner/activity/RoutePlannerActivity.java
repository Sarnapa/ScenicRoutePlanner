package com.spdb.scenicrouteplanner.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.spdb.scenicrouteplanner.R;
import com.spdb.scenicrouteplanner.lib.GeoCoords;
import com.spdb.scenicrouteplanner.service.OSMService;

public class RoutePlannerActivity extends Fragment
{
    private View routePlannerView;
    private OSMService osmService;

    @Override
    public void onCreate(Bundle savedInstance)
    {
        super.onCreate(savedInstance);

        this.osmService = new OSMService();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        routePlannerView = inflater.inflate(R.layout.activity_route_planner, container, false);

        final ImageButton findRoutePlannerButton = getView().findViewById(R.id.find_route_planner_button);

        // dla testu
        findRoutePlannerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                GeoCoords coords = osmService.GetPlaceCoords("wydzial elektroniki i technik informacyjnych warszawa");
                osmService.GetMapExtent(new GeoCoords(52.2179, 21.00957),
                        new GeoCoords(52.21987, 21.01356));
            }
        });

        return routePlannerView;
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
    }
}
