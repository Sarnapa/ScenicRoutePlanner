package com.spdb.scenicrouteplanner.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.spdb.scenicrouteplanner.lib.GeoCoords;
import com.spdb.scenicrouteplanner.model.Edge;
import com.spdb.scenicrouteplanner.model.Node;
import com.spdb.scenicrouteplanner.model.Way;

import static com.spdb.scenicrouteplanner.database.RoutesDbContract.*;
import static com.spdb.scenicrouteplanner.lib.OSM.OSMClassLib.*;

import org.spatialite.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

public class RoutesDbProvider
{
    // ==============================
    // Private fields
    // ==============================
    private RoutesDbHelper dbHelper;

    // ==============================
    // Constructors
    // ==============================
    public RoutesDbProvider(Context context)
    {
        dbHelper = new RoutesDbHelper(context);
    }

    public SQLiteDatabase getRoutesDb()
    {
        return dbHelper.getReadableDatabase();
    }

    // ==============================
    // Inserting data methods
    // ==============================
    public void addNode(Node n)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NodesTable._ID, n.getId());
        values.put(NodesTable.GEOMETRY_COL_NAME, String.format("%s(%f %f)", GeometryType.POINT.name(),
                n.getGeoCoords().getLatitude(), n.getGeoCoords().getLongitude()));

        try
        {
            db.insert(NodesTable.TABLE_NAME, null, values);
        }
        catch (Exception e)
        {
            Log.d("ROUTES_DB_PROVIDER", e.getMessage());
        }
    }

    public void addNodes(List<Node> nodes)
    {
        for (Node n : nodes)
            addNode(n);
    }

    public void addEdge(Edge e)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Node startNode = e.getStartNode();
        GeoCoords startNodeGeoCoords = startNode.getGeoCoords();
        Node endNode = e.getEndNode();
        GeoCoords endNodeGeoCoords = endNode.getGeoCoords();

        ContentValues values = new ContentValues();
        values.put(EdgesTable._ID, e.getId());
        values.put(EdgesTable.WAY_ID_COL_NAME, e.getWayInfo().getId());
        values.put(EdgesTable.START_NODE_ID_COL_NAME, startNode.getId());
        values.put(EdgesTable.END_NODE_ID_COL_NAME, endNode.getId());
        values.put(EdgesTable.IS_TOUR_ROUTE_COL_NAME, e.isTourRoute() ? 1 : 0);
        values.put(EdgesTable.GEOMETRY_COL_NAME, String.format("%s(%f %f, %f %f)",
                GeometryType.LINESTRING.name(),
                startNodeGeoCoords.getLatitude(), startNodeGeoCoords.getLongitude(),
                endNodeGeoCoords.getLatitude(), endNodeGeoCoords.getLongitude()));

        try
        {
            db.insert(NodesTable.TABLE_NAME, null, values);
        }
        catch (Exception ex)
        {
            Log.d("ROUTES_DB_PROVIDER", ex.getMessage());
        }
    }

    public void addEdges(List<Edge> edges)
    {
        for (Edge e : edges)
            addEdge(e);
    }

    public void addWay(Way w)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(WaysTable._ID, w.getId());
        values.put(WaysTable.WAY_TYPE_COL_NAME, w.getWayType().name());
        values.put(WaysTable.IS_SCENIC_ROUTE_COL_NAME, w.isScenicRoute() ? 1 : 0);
        values.put(WaysTable.MAX_SPEED_COL_NAME, w.getMaxSpeed());

        try
        {
            db.insert(WaysTable.TABLE_NAME, null, values);
        }
        catch (Exception e)
        {
            Log.d("ROUTES_DB_PROVIDER", e.getMessage());
        }
    }

    public void addWays(List<Way> ways)
    {
        for (Way w : ways)
            addWay(w);
    }

    // ==============================
    // Removing data methods
    // ==============================

    public int removeAllNodes()
    {
        return removeAllRows(NodesTable.TABLE_NAME);
    }

    public int removeAllEdges()
    {
        return removeAllRows(EdgesTable.TABLE_NAME);
    }

    public int removeAllWays()
    {
        return removeAllRows(WaysTable.TABLE_NAME);
    }

    private int removeAllRows(String tableName)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try
        {
            return db.delete(tableName, null, null);
        }
        catch (Exception e)
        {
            Log.d("ROUTES_DB_PROVIDER", e.getMessage());
            return -1;
        }
    }

    // ==============================
    // Reading data methods
    // ==============================

    public List<Edge> getAllEdges()
    {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        List<Edge> res = new ArrayList<>();
        HashMap<Long, Node> nodes = new HashMap<>();

        String[] edgesProjection = {
                EdgesTable._ID,
                EdgesTable.WAY_ID_COL_NAME,
                EdgesTable.START_NODE_ID_COL_NAME,
                EdgesTable.END_NODE_ID_COL_NAME,
                EdgesTable.IS_TOUR_ROUTE_COL_NAME,
                EdgesTable.GEOMETRY_COL_NAME,
                String.format("Length(%s) AS %s", EdgesTable.GEOMETRY_COL_NAME, LENGTH_COL_NAME)
        };

        Cursor edgesCursor = db.query(EdgesTable.TABLE_NAME,
                edgesProjection,
                null,
                null,
                null,
                null,
                null);

        while (edgesCursor.moveToNext())
        {
            long edgeId = edgesCursor.getLong(edgesCursor.getColumnIndexOrThrow(EdgesTable._ID));
            long wayId = edgesCursor.getLong(edgesCursor.getColumnIndexOrThrow(EdgesTable.WAY_ID_COL_NAME));
            long startNodeId = edgesCursor.getLong(edgesCursor.getColumnIndexOrThrow(EdgesTable.START_NODE_ID_COL_NAME));
            long endNodeId = edgesCursor.getLong(edgesCursor.getColumnIndexOrThrow(EdgesTable.END_NODE_ID_COL_NAME));
            boolean isTourRoute = edgesCursor.getInt(edgesCursor.getColumnIndexOrThrow(EdgesTable.IS_TOUR_ROUTE_COL_NAME)) == 1;
            String edgeGeom = edgesCursor.getString(edgesCursor.getColumnIndexOrThrow(EdgesTable.GEOMETRY_COL_NAME));
            int edgeLength = edgesCursor.getInt(edgesCursor.getColumnIndexOrThrow(LENGTH_COL_NAME));

            Way edgeWayInfo = null;

            String[] wayProjection = {
                    WaysTable.WAY_TYPE_COL_NAME,
                    WaysTable.IS_SCENIC_ROUTE_COL_NAME,
                    WaysTable.MAX_SPEED_COL_NAME
            };

            String selection = WaysTable._ID + " = ?";
            String[] selectionArgs = { String.format("%d", wayId)};

            Cursor wayCursor = db.query(WaysTable.TABLE_NAME,
                    wayProjection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null);

            if (wayCursor.isFirst())
                edgeWayInfo = new Way(edgeId,
                        WayType.valueOf(wayCursor.getString(wayCursor.getColumnIndexOrThrow(WaysTable.WAY_TYPE_COL_NAME))),
                        wayCursor.getInt(wayCursor.getColumnIndexOrThrow(WaysTable.IS_SCENIC_ROUTE_COL_NAME)) == 1,
                        wayCursor.getInt(wayCursor.getColumnIndexOrThrow(WaysTable.MAX_SPEED_COL_NAME)));

            Edge e = new Edge();
            e.setId(edgeId);
            e.setWayInfo(edgeWayInfo);
            e.setLength(edgeLength);
            e.setTourRoute(isTourRoute);

            Node startNode = new Node();
            if (nodes.containsKey(startNodeId))
                startNode = nodes.get(startNodeId);
            else
            {
                startNode.setId(startNodeId);
                GeoCoords startNodeGeoCoords = GetNodeGeoCoordsFromEdge(edgeGeom, 0);
                startNode.setGeoCoords(startNodeGeoCoords);
            }

            Node endNode = new Node();
            if (nodes.containsKey(endNodeId))
                endNode = nodes.get(endNodeId);
            else
            {
                endNode.setId(endNodeId);
                GeoCoords endNodeGeoCoords = GetNodeGeoCoordsFromEdge(edgeGeom, 1);
                endNode.setGeoCoords(endNodeGeoCoords);
            }

            e.setStartNode(startNode);
            e.setEndNode(endNode);
            startNode.getEdges().add(e);

            res.add(e);
        }
        edgesCursor.close();

        return res;
    }

    // ==============================
    // Private methods
    // ==============================

    private static GeoCoords GetNodeGeoCoordsFromEdge(String geom, int idx)
    {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("-?\\d+");
        Matcher m = p.matcher(geom);
        switch (idx)
        {
            case 0:
            case 1:
            {
                return new GeoCoords(Double.parseDouble(m.group(idx * 2)),
                        Double.parseDouble(m.group(idx * 2 + 1)));
            }
            default:
                return null;
        }
    }
}
