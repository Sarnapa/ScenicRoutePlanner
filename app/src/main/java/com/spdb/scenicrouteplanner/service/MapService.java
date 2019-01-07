package com.spdb.scenicrouteplanner.service;

import android.graphics.Color;
import android.util.Log;

import com.spdb.scenicrouteplanner.lib.GeoCoords;
import com.spdb.scenicrouteplanner.model.Edge;
import com.spdb.scenicrouteplanner.model.Node;
import com.spdb.scenicrouteplanner.service.interfaces.IMapService;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class MapService implements IMapService
{
    // ==============================
    // Private fields
    // ==============================
    private OverlayManager mapOverlayManager;
    private BoundingBox startMapExtent;
    private Node startNode;
    private Node endNode;
    private List<Edge> mapEdges = new ArrayList<>();

    // ==============================
    // Getters and Setters
    // ==============================
    public OverlayManager getMapOverlayManager() {
        return mapOverlayManager;
    }

    public void setMapOverlayManager(OverlayManager mapOverlayManager) {
        this.mapOverlayManager = mapOverlayManager;
    }

    // ==============================
    // Constructors
    // ==============================
    public MapService() { }

    // ==============================
    // Override IOSMService
    // ==============================

    // Gdy będzie baza, tej metody nie będzie i getStartMapExtent będzie czytal z bazy
    @Override
    public void setStartMapExtent(GeoCoords v1, GeoCoords v2)
    {
        double buffer = 0.001;

        double north = Math.max(v1.getLatitude(), v2.getLatitude());
        double south = Math.min(v1.getLatitude(), v2.getLatitude());
        double east = Math.max(v1.getLongitude(), v2.getLongitude());
        double west = Math.min(v1.getLongitude(), v2.getLongitude());

        startMapExtent = new BoundingBox(north + buffer, east + buffer, south - buffer,
                west - buffer);
    }

    @Override
    public void setStartNode(Node n) { this.startNode = n; }

    @Override
    public void setEndNode(Node n) { this.endNode = n; }

    @Override
    public BoundingBox getStartMapExtent()
    {
        return startMapExtent;
    }

    @Override
    public void addEdge(Edge edge) throws IllegalArgumentException {
        if (edge == null)
            throw new IllegalArgumentException("MapService.addEdge - empty edge argument");
        Node startNode = edge.getStartNode();
        if (startNode == null)
            throw new IllegalArgumentException("MapService.addEdge - not defined start node");
        Node endNode = edge.getEndNode();
        if (endNode == null)
            throw new IllegalArgumentException("MapService.addEdge - not defined end node");

        mapEdges.add(edge);
    }

    @Override
    public void addEdges(List<Edge> edges) throws IllegalArgumentException {
        for (Edge e : edges)
            addEdge(e);
    }

    @Override
    public void removeStartEndNode() {
        startNode = null;
        endNode = null;
    }

    @Override
    public void removeAllEdges() {
        if (mapEdges != null)
            mapEdges.clear();
    }

    @Override
    public void putStartEndNodeOnMap()
    {
        if (mapOverlayManager != null && startNode != null && endNode != null)
        {
            Overlay startPoint = getNodePoint(startNode, NodeColor.START_COLOR);
            Overlay endPoint = getNodePoint(endNode, NodeColor.END_COLOR);

            mapOverlayManager.add(startPoint);
            mapOverlayManager.add(endPoint);
        }
        else
            Log.d("MAP_SERVICE", "Not initialized map Overlay Manager or null nodes.");
    }

    @Override
    public void putAllEdgesOnMap()
    {
        if (mapOverlayManager != null && mapEdges != null && mapEdges.size() > 0)
        {
            List<Polyline> polylines = new ArrayList<>();
            for (Edge e: mapEdges)
            {
                Node startNode = e.getStartNode();
                Node endNode = e.getEndNode();
                final GeoPoint startPoint = new GeoPoint(startNode.getGeoCoords().getLatitude(),
                        startNode.getGeoCoords().getLongitude());
                final GeoPoint endPoint = new GeoPoint(endNode.getGeoCoords().getLatitude(),
                        endNode.getGeoCoords().getLongitude());

                Polyline polyline = new Polyline();

                polyline.setPoints(new ArrayList<GeoPoint>() {
                    {
                        add(startPoint);
                        add(endPoint);
                    }
                });
                /*
                if (e.isTourRoute()) {
                    if (e.getWayInfo().isScenicRoute())
                        polyline.setColor(EdgeColor.SCENIC_TOUR_ROUTE_COLOR);
                    else
                        polyline.setColor(EdgeColor.STANDARD_TOUR_ROUTE_COLOR);
                } else {
                    if (e.getWayInfo().isScenicRoute())
                        polyline.setColor(EdgeColor.SCENIC_ROUTE_COLOR);
                    else
                        polyline.setColor(EdgeColor.STANDARD_ROUTE_COLOR);
                }
                */
                polyline.setColor(EdgeColor.STANDARD_ROUTE_COLOR);
                polylines.add(polyline);
            }

            mapOverlayManager.addAll(polylines);
        }
        else
            Log.d("MAP_SERVICE", "Not initialized map Overlay Manager or empty map edges collection.");
    }

    // ==============================
    // Private methods
    // ==============================

    private Polygon getNodePoint(Node n, int color)
    {
        final GeoPoint point = new GeoPoint(n.getGeoCoords().getLatitude(), n.getGeoCoords().getLongitude());

        final double radius = 5;
        ArrayList<GeoPoint> circlePoints = new ArrayList<GeoPoint>();
        for (float f = 0; f < 360; f += 1.0)
        {
            circlePoints.add(point.destinationPoint(radius, f));
        }

        Polygon nodePoint = new Polygon();
        nodePoint.setPoints(circlePoints);
        nodePoint.setStrokeColor(Color.BLACK);
        nodePoint.setFillColor(color);

        return nodePoint;
    }

    private static final class NodeColor {
        final static int START_COLOR = Color.rgb(0, 200, 200);
        final static int END_COLOR = Color.rgb(0, 200, 0);
    }

    private static final class EdgeColor {
        final static int STANDARD_ROUTE_COLOR = Color.BLACK;
        final static int SCENIC_ROUTE_COLOR = Color.BLUE;
        final static int STANDARD_TOUR_ROUTE_COLOR = Color.RED;
        final static int SCENIC_TOUR_ROUTE_COLOR = Color.MAGENTA;
    }
}