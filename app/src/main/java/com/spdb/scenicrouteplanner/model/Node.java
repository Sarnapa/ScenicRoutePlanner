package com.spdb.scenicrouteplanner.model;

import com.spdb.scenicrouteplanner.lib.GeoCoords;

import java.util.List;

public class Node
{
    private int id;
    private GeoCoords geoCoords;
    private List<Edge> edges;

    public Node(int id, GeoCoords geoCoords, List<Edge> edges)
    {
        this.id = id;
        this.geoCoords = geoCoords;
        this.edges = edges;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public GeoCoords getGeoCoords()
    {
        return geoCoords;
    }

    public void setGeoCoords(GeoCoords geoCoords)
    {
        this.geoCoords = geoCoords;
    }

    public List<Edge> getEdges()
    {
        return edges;
    }

    public void setEdges(List<Edge> edges)
    {
        this.edges = edges;
    }
}
