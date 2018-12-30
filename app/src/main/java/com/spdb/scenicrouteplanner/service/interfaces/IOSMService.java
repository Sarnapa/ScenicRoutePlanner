package com.spdb.scenicrouteplanner.service.interfaces;

import com.spdb.scenicrouteplanner.lib.GeoCoords;
import com.spdb.scenicrouteplanner.lib.OSM.Place;

public interface IOSMService
{
    Place getPlace(String phrase);
    GeoCoords getPlaceCoords(String phrase);

    void getMapExtent(GeoCoords v1, GeoCoords v2);
}
