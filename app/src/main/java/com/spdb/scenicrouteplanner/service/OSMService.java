package com.spdb.scenicrouteplanner.service;

import android.util.Log;

import com.spdb.scenicrouteplanner.httpService.OSMDataHttpService.GetPlaceTask;
import com.spdb.scenicrouteplanner.httpService.OSMMapHttpService.GetMapTask;
import com.spdb.scenicrouteplanner.lib.GeoCoords;
import com.spdb.scenicrouteplanner.lib.OSM.Place;
import com.spdb.scenicrouteplanner.service.interfaces.IOSMService;

import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class OSMService implements IOSMService
{
    // ==============================
    // Override IOSMService
    // ==============================
    @Override
    public Place getPlace(String phrase) throws IllegalArgumentException
    {
        if (phrase == null || phrase.isEmpty())
            throw new IllegalArgumentException("OSMService.getPlace - empty phrase argument");
        try
        {
            GetPlaceTask getPlaceTask = new GetPlaceTask();
            return getPlaceTask.execute(phrase).get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            Log.d("OSM_SERVICE", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public GeoCoords getPlaceCoords(String phrase) throws IllegalArgumentException
    {
        if (phrase == null || phrase.isEmpty())
            throw new IllegalArgumentException("OSMService.getPlaceCoords - empty phrase argument");

        Place place = getPlace(phrase);
        if (place != null)
            return new GeoCoords(place.getLatitude(), place.getLongitude());
        else
            return null;
    }

    @Override
    public void getMapExtent(GeoCoords v1, GeoCoords v2) throws IllegalArgumentException
    {
        if (v1 == null)
            throw new IllegalArgumentException("OSMService.GetMapExtent - null v1 argument");
        if (v2 == null)
            throw new IllegalArgumentException("OSMService.GetMapExtent - null v2 argument");

        double minLongitude = Double.min(v1.getLongitude(), v2.getLongitude());
        double maxLongitude = Double.max(v1.getLongitude(), v2.getLongitude());
        double minLatitude = Double.min(v1.getLatitude(), v2.getLatitude());
        double maxLatitude = Double.max(v1.getLatitude(), v2.getLatitude());

        String getMapTaskParam = String.format(Locale.US, "*[bbox=%f,%f,%f,%f]", minLongitude, minLatitude,
                maxLongitude, maxLatitude);

        try
        {
            GetMapTask getMapTask = new GetMapTask();
            getMapTask.execute(getMapTaskParam).get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            Log.d("OSM_SERVICE", e.getMessage());
            e.printStackTrace();
        }
    }
}
