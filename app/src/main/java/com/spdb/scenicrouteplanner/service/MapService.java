package com.spdb.scenicrouteplanner.service;

import android.graphics.Color;
import android.util.Log;

import com.spdb.scenicrouteplanner.model.Edge;
import com.spdb.scenicrouteplanner.model.Node;
import com.spdb.scenicrouteplanner.service.interfaces.IMapService;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapService implements IMapService
{
    // ==============================
    // Private fields
    // ==============================
    private OverlayManager mapOverlayManager;
    private HashMap<Integer, Polyline> mapPolylines = new HashMap<>();

    // ==============================
    // Getters and Setters
    // ==============================

    public OverlayManager getMapOverlayManager()
    {
        return mapOverlayManager;
    }

    public void setMapOverlayManager(OverlayManager mapOverlayManager)
    {
        this.mapOverlayManager = mapOverlayManager;
    }

    public MapService()
    { }

    // ==============================
    // Override IOSMService
    // ==============================
    @Override
    public void addEdge(Edge edge) throws IllegalArgumentException
    {
        if (edge == null)
            throw new IllegalArgumentException("MapService.addEdge - empty edge argument");
        Node startNode = edge.getStartNode();
        if (startNode == null)
            throw new IllegalArgumentException("MapService.addEdge - not defined start node");
        Node endNode = edge.getEndNode();
        if (endNode == null)
            throw new IllegalArgumentException("MapService.addEdge - not defined end node");

        Polyline result = new Polyline();
        final GeoPoint startPoint = new GeoPoint(startNode.getGeoCoords().getLatitude(),
                startNode.getGeoCoords().getLongitude());
        final GeoPoint endPoint = new GeoPoint(endNode.getGeoCoords().getLatitude(),
                endNode.getGeoCoords().getLongitude());

        result.setPoints(new ArrayList<GeoPoint>()
        {
            {
                add(startPoint);
                add(endPoint);
            }
        });
        if (edge.isTourRoute())
        {
            if (edge.getWayInfo().isScenicRoute())
                result.setColor(EdgeColor.SCENIC_TOUR_ROUTE_COLOR);
            else
                result.setColor(EdgeColor.STANDARD_TOUR_ROUTE_COLOR);
        }
        else
        {
            if (edge.getWayInfo().isScenicRoute())
                result.setColor(EdgeColor.SCENIC_ROUTE_COLOR);
            else
                result.setColor(EdgeColor.STANDARD_ROUTE_COLOR);
        }

        mapPolylines.put(edge.getId(), result);
    }

    @Override
    public void addEdges(List<Edge> edges) throws IllegalArgumentException
    {
        for (Edge e: edges)
            addEdge(e);
    }

    @Override
    public void removeAllEdges()
    {
        if (mapPolylines != null)
            mapPolylines.clear();
    }

    @Override
    public void putAllEdgesOnMap()
    {
        if (mapOverlayManager != null && mapPolylines != null && mapPolylines.size() > 0)
            mapOverlayManager.addAll(mapPolylines.values());
        else
            Log.d("MAP_SERVICE", "Not initialized map Overlay Manager.");
    }

    private static final class EdgeColor
    {
        final static int STANDARD_ROUTE_COLOR = Color.BLACK;
        final static int SCENIC_ROUTE_COLOR = Color.BLUE;
        final static int STANDARD_TOUR_ROUTE_COLOR = Color.RED;
        final static int SCENIC_TOUR_ROUTE_COLOR = Color.MAGENTA;
    }
}