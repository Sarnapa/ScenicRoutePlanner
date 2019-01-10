package com.spdb.scenicrouteplanner.model;

import com.spdb.scenicrouteplanner.lib.GeoCoords;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private long id;
    private GeoCoords geoCoords;
    private List<Edge> edges = new ArrayList<>();

    public Node() {
    }

    public Node(long id, GeoCoords geoCoords) {
        this.id = id;
        this.geoCoords = geoCoords;
    }

    public Node(long id, GeoCoords geoCoords, List<Edge> edges) {
        this.id = id;
        this.geoCoords = geoCoords;
        this.edges = edges;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public GeoCoords getGeoCoords() {
        return geoCoords;
    }

    public void setGeoCoords(GeoCoords geoCoords) {
        this.geoCoords = geoCoords;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    /*public List<Edge> getOutgoingEdges() {
        ArrayList<Edge> outgoingEdges = new ArrayList<>();
        for (Edge e : edges) {
            if (e.getEndNode() != this)
                outgoingEdges.add(e);
        }
        return outgoingEdges;
    }*/

    public void setEdges(ArrayList<Edge> edges) {
        this.edges = edges;
    }
}
