package com.spdb.scenicrouteplanner.lib;

public class GeoCoords
{
    private double latitude;
    private double longitude;

    public GeoCoords(double latitude, double longitude)
    {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }
}
