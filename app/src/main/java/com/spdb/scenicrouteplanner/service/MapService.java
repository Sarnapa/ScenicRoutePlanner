package com.spdb.scenicrouteplanner.service;

import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;

import com.spdb.scenicrouteplanner.database.MazovianRoutesDbProvider;
import com.spdb.scenicrouteplanner.database.modelDatabase.RoutesDbProvider;
import com.spdb.scenicrouteplanner.database.structures.PathResult;
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
    private MazovianRoutesDbProvider dbProvider;
    private GeoCoords start;
    private GeoCoords end;

    private static double MAP_BUFFER = 0.001f;

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
    public MapService(MazovianRoutesDbProvider dbProvider)
    {
        this.dbProvider = dbProvider;
    }

    // ==============================
    // Override IOSMService
    // ==============================
    @Override
    public BoundingBox getStartMapExtent()
    {
        Cursor resultCursor = getResultCursor();
        if (mapOverlayManager != null && resultCursor != null) {
            try
            {
                if(resultCursor.moveToFirst()) {
                    PathResult res = dbProvider.getPathResult(resultCursor);

                    if (res != null) {
                        long startNodeId = res.getNodeFrom();
                        long endNodeId = res.getNodeTo();

                        if (startNodeId >= 0 && endNodeId >= 0) {
                            start = dbProvider.getNodeGeoCoords(startNodeId);
                            end = dbProvider.getNodeGeoCoords(endNodeId);

                            if (start != null && end != null) {
                                double north = Math.max(start.getLatitude(), end.getLatitude());
                                double south = Math.min(start.getLatitude(), end.getLatitude());
                                double east = Math.max(start.getLongitude(), end.getLongitude());
                                double west = Math.min(start.getLongitude(), end.getLongitude());

                                putStartEndNodeOnMap();

                                return new BoundingBox(north + MAP_BUFFER, east + MAP_BUFFER,
                                        south - MAP_BUFFER, west - MAP_BUFFER);
                            }
                        }
                    }
                }
            }
            finally
            {
                resultCursor.close();
            }
        }
        return null;
    }

    @Override
    public void putAllEdgesOnMap()
    {
        Cursor resultCursor = getResultCursor();
        if (mapOverlayManager != null && resultCursor != null)
        {
            try {
                List<Polyline> polylines = new ArrayList<>();
                // Pierwszy wynik to cała droga
                resultCursor.moveToNext();
                while (resultCursor.moveToNext()) {
                    PathResult edgeResult = dbProvider.getPathResult(resultCursor);
                    long startNodeId = edgeResult.getNodeFrom();
                    long endNodeId = edgeResult.getNodeTo();

                    GeoCoords startNodeGeoCoords = dbProvider.getNodeGeoCoords(startNodeId);
                    GeoCoords endNodeGeoCoords = dbProvider.getNodeGeoCoords(endNodeId);
                    if (startNodeGeoCoords != null && endNodeGeoCoords != null)
                    {
                        final List<GeoCoords> routeGeoCoords = dbProvider.getRouteGeoCoords(startNodeId, endNodeId);

                        Polyline polyline = new Polyline();

                        List<GeoPoint> routeGeoPoints = new ArrayList<>();
                        for (GeoCoords coords: routeGeoCoords)
                        {
                            routeGeoPoints.add(new GeoPoint(coords.getLatitude(), coords.getLongitude()));
                        }
                        polyline.setPoints(routeGeoPoints);

                    /*if (e.isTourRoute()) {
                        if (e.getWayInfo().isScenicRoute())
                            polyline.setColor(EdgeColor.SCENIC_TOUR_ROUTE_COLOR);
                        else
                            polyline.setColor(EdgeColor.STANDARD_TOUR_ROUTE_COLOR);
                    } else {
                        if (e.getWayInfo().isScenicRoute())
                            polyline.setColor(EdgeColor.SCENIC_ROUTE_COLOR);
                        else
                            polyline.setColor(EdgeColor.STANDARD_ROUTE_COLOR);
                    }*/

                        //if (dbProvider.isScenicRouteByNodes(startNodeId, endNodeId))
                            //polyline.setColor(EdgeColor.SCENIC_ROUTE_COLOR);
                        //else
                        polyline.setColor(EdgeColor.STANDARD_ROUTE_COLOR);

                        polylines.add(polyline);
                    }
                }

                mapOverlayManager.addAll(polylines);
            }
            finally
            {
                resultCursor.close();
            }
        }
        else
            Log.d("MAP_SERVICE", "Not initialized map Overlay Manager or invalid nodes.");
    }

    @Override
    public void clear()
    {
        mapOverlayManager.clear();
        start = null;
        end = null;
    }
    // ==============================
    // Private methods
    // ==============================
    private Cursor getResultCursor()
    {
        if (dbProvider.isDbAvailable())
        {
            return dbProvider.getShortestPath();
        }
        return null;
    }

    private void putStartEndNodeOnMap()
    {
        if (mapOverlayManager != null && start != null && end != null)
        {
            Overlay startPoint = getNodePoint(start, NodeColor.START_COLOR);
            Overlay endPoint = getNodePoint(end, NodeColor.END_COLOR);

            mapOverlayManager.add(startPoint);
            mapOverlayManager.add(endPoint);
        }
        else
            Log.d("MAP_SERVICE", "Not initialized map Overlay Manager or invalid nodes.");
    }

    private Polygon getNodePoint(GeoCoords node, int color)
    {
        final GeoPoint point = new GeoPoint(node.getLatitude(), node.getLongitude());

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