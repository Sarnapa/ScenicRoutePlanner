package com.spdb.scenicrouteplanner.service.interfaces;

import com.spdb.scenicrouteplanner.lib.GeoCoords;
import com.spdb.scenicrouteplanner.lib.OSM.Place;

public interface IOSMService
{
    Place GetPlace(String phrase);
    GeoCoords GetPlaceCoords(String phrase);

    void GetMapExtent(GeoCoords v1, GeoCoords v2);
}
