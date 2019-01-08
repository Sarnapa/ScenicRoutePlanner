package com.spdb.scenicrouteplanner.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.spdb.scenicrouteplanner.lib.GeoCoords;
import com.spdb.scenicrouteplanner.lib.OSM.OSMClassLib;
import com.spdb.scenicrouteplanner.lib.PathsClassLib;
import com.spdb.scenicrouteplanner.model.Edge;
import com.spdb.scenicrouteplanner.model.Node;
import com.spdb.scenicrouteplanner.model.Way;
import com.spdb.scenicrouteplanner.utils.FileUtils;

import static com.spdb.scenicrouteplanner.database.RoutesDbContract.*;
import static com.spdb.scenicrouteplanner.lib.OSM.OSMClassLib.*;

import org.spatialite.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class RoutesDbProvider
{
    // ==============================
    // Private fields
    // ==============================
    private static final String DATABASE_NAME = "Routes.db";
    private static final String DATABASE_PATH = PathsClassLib.DB_DIRECTORY + "/" + DATABASE_NAME;

    //private RoutesDbHelper dbHelper;
    private SQLiteDatabase db;

    // ==============================
    // Constructors
    // ==============================
    public RoutesDbProvider()
    {
        //dbHelper = new RoutesDbHelper(context);
        FileUtils.createDir(PathsClassLib.DB_DIRECTORY);
        if (FileUtils.fileExists(DATABASE_PATH))
            db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
    }

    public boolean isDbAvailable()
    {
        return FileUtils.fileExists(DATABASE_PATH) && db != null;
    }

    // ==============================
    // Inserting data methods
    // ==============================
    public void addNode(Node n)
    {
        try
        {
            //SQLiteDatabase db = new GetDatabaseTask().execute(true).get();

            ContentValues values = new ContentValues();
            values.put(NodesTable._ID, n.getId());
            values.put(NodesTable.GEOMETRY_COL_NAME, String.format(Locale.US,"%s(%f %f)", GeometryType.POINT.name(),
                    n.getGeoCoords().getLongitude(), n.getGeoCoords().getLatitude()));

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
        try
        {
            //SQLiteDatabase db = new GetDatabaseTask().execute(true).get();

            Node startNode = e.getStartNode()   ;
            GeoCoords startNodeGeoCoords = startNode.getGeoCoords();
            Node endNode = e.getEndNode();
            GeoCoords endNodeGeoCoords = endNode.getGeoCoords();

            ContentValues values = new ContentValues();
            values.put(EdgesTable._ID, e.getId());
            values.put(EdgesTable.WAY_ID_COL_NAME, e.getWayInfo().getId());
            values.put(EdgesTable.START_NODE_ID_COL_NAME, startNode.getId());
            values.put(EdgesTable.END_NODE_ID_COL_NAME, endNode.getId());
            values.put(EdgesTable.IS_TOUR_ROUTE_COL_NAME, e.isTourRoute() ? 1 : 0);
            // W 4326 najpierw long, później lat
            values.put(EdgesTable.GEOMETRY_COL_NAME, String.format(Locale.US, "%s(%f %f, %f %f)",
                    GeometryType.MULTIPOINT.name(),
                    startNodeGeoCoords.getLongitude(), startNodeGeoCoords.getLatitude(),
                    endNodeGeoCoords.getLongitude(), endNodeGeoCoords.getLatitude()));

            db.insert(EdgesTable.TABLE_NAME, null, values);
            //db.execSQL("INSERT INTO inserts_1 (val) VALUES " +valuesBuilder.toString(),
            //        values
            //);
        }
        catch (Exception ex)
        {
            Log.d("ROUTES_DB_PROVIDER", ex.getMessage());
        }
    }

    public void addEdges(List<Edge> edges)
    {
        HashMap<Long, List<Long>> nodesHashMap = new HashMap<>();

        db.beginTransaction();
        for (Edge e : edges)
        {
            Long startNodeId = e.getStartNode().getId();
            Long endNodeId = e.getEndNode().getId();
            if (nodesHashMap.containsKey(startNodeId))
            {
                List<Long> destNodes = nodesHashMap.get(startNodeId);
                if (destNodes.contains(endNodeId))
                {
                    Log.d("ROUTES_DB_PROVIDER",
                            String.format("RoutesDbProvider.addEdge - duplicated edge(id: %d, startNode: %d, endNode: %d).",
                            e.getId(), startNodeId, endNodeId));
                    continue;
                }
                else
                    destNodes.add(endNodeId);
            }
            else
            {
                ArrayList newNodesList = new ArrayList<Long>();
                newNodesList.add(endNodeId);
                nodesHashMap.put(startNodeId, newNodesList);
            }
            addEdge(e);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void addWay(Way w)
    {
        try
        {
            //SQLiteDatabase db = new GetDatabaseTask().execute(true).get();

            ContentValues values = new ContentValues();
            values.put(WaysTable._ID, w.getId());
            values.put(WaysTable.WAY_TYPE_COL_NAME, w.getWayType().name());
            values.put(WaysTable.IS_SCENIC_ROUTE_COL_NAME, w.isScenicRoute() ? 1 : 0);
            values.put(WaysTable.MAX_SPEED_COL_NAME, w.getMaxSpeed());

            db.insert(WaysTable.TABLE_NAME, null, values);
        }
        catch (Exception e)
        {
            Log.d("ROUTES_DB_PROVIDER", e.getMessage());
        }
    }

    public void addWays(List<Way> ways)
    {
        db.beginTransaction();
        for (Way w : ways)
            addWay(w);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    // ==============================
    // Updating data methods
    // ==============================

    public void setTour(List<Edge> edges)
    {
        db.beginTransaction();
        for (Edge e: edges)
        {
            ContentValues values = new ContentValues();
            values.put(EdgesTable.IS_TOUR_ROUTE_COL_NAME, e.isTourRoute());

            updateEdgeById(e.getId(), values);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void updateEdgeById(long id, ContentValues values)
    {
        db.update(EdgesTable.TABLE_NAME, values, "_id = " + id,null);
    }

    // ==============================
    // Removing data methods
    // ==============================

    public void clearDb()
    {
        removeAllWays();
        //removeAllNodes();
        removeAllEdges();
    }

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
        try
        {
            //SQLiteDatabase db = new GetDatabaseTask().execute(true).get();

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
        /*SQLiteDatabase db;
        try
        {
            db = new GetDatabaseTask().execute(false).get();
        }
        catch (ExecutionException | InterruptedException e)
        {
            Log.d("ROUTES_DB_PROVIDER", e.getMessage());
            return null;
        }*/

        List<Edge> res = new ArrayList<>();
        HashMap<Long, Node> nodes = new HashMap<>();

        String[] edgesProjection = {
                EdgesTable._ID,
                EdgesTable.WAY_ID_COL_NAME,
                EdgesTable.START_NODE_ID_COL_NAME,
                EdgesTable.END_NODE_ID_COL_NAME,
                EdgesTable.IS_TOUR_ROUTE_COL_NAME,
                EdgesTable.GEOMETRY_COL_NAME,
                getLengthSQL(EdgesTable.GEOMETRY_COL_NAME, LENGTH_COL_NAME, OSM_SRID, ETRS89_SRID)
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
            double edgeLength = edgesCursor.getDouble(edgesCursor.getColumnIndexOrThrow(LENGTH_COL_NAME));

            Way edgeWayInfo = null;

            String[] wayProjection = {
                    WaysTable.WAY_TYPE_COL_NAME,
                    WaysTable.IS_SCENIC_ROUTE_COL_NAME,
                    WaysTable.MAX_SPEED_COL_NAME
            };

            String selection = WaysTable._ID + " = ?";
            String[] selectionArgs = { String.format(Locale.US, "%d", wayId)};

            Cursor wayCursor = db.query(WaysTable.TABLE_NAME,
                    wayProjection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null);

            if (wayCursor.moveToFirst())
                edgeWayInfo = new Way(wayId,
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
                GeoCoords startNodeGeoCoords = getNodeGeoCoordsFromEdge(edgeGeom, 0);
                startNode.setGeoCoords(startNodeGeoCoords);
            }

            Node endNode = new Node();
            if (nodes.containsKey(endNodeId))
                endNode = nodes.get(endNodeId);
            else
            {
                endNode.setId(endNodeId);
                GeoCoords endNodeGeoCoords = getNodeGeoCoordsFromEdge(edgeGeom, 1);
                endNode.setGeoCoords(endNodeGeoCoords);
            }

            e.setStartNode(startNode);
            e.setEndNode(endNode);
            startNode.getEdges().add(e);

            res.add(e);
            wayCursor.close();
        }
        edgesCursor.close();

        return res;
    }

    public long getClosestNodeId(GeoCoords coords) throws IllegalArgumentException
    {
        if (coords == null)
            throw new IllegalArgumentException("RoutesDbProvider.getClosestNodeId - null coords argument");

        /*SQLiteDatabase db;
        try
        {
            db = new GetDatabaseTask().execute(false).get();
        }
        catch (ExecutionException | InterruptedException e)
        {
            Log.d("ROUTES_DB_PROVIDER", e.getMessage());
            return -1;
        }*/

        double latCoord = coords.getLatitude();
        double longCoord = coords.getLongitude();

        final String START_NODE_LEN_COL_NAME = "len1";
        final String END_NODE_LEN_COL_NAME = "len2";

        final String SQL_NEAREST_POINT = String.format(Locale.US,
                "SELECT Min(Distance(GeomFromText(%s), GeomFromText('Point(%f %f)'))), \n" +
                "%s, Distance(GeometryN(GeomFromText(%s), 1), GeomFromText('Point(%f %f)')) AS %s,\n" +
                "%s, Distance(GeometryN(GeomFromText(%s), 2), GeomFromText('Point(%f %f)')) AS %s\n" +
                "FROM %s;", EdgesTable.GEOMETRY_COL_NAME, longCoord, latCoord,
                EdgesTable.START_NODE_ID_COL_NAME, EdgesTable.GEOMETRY_COL_NAME, longCoord, latCoord, START_NODE_LEN_COL_NAME,
                EdgesTable.END_NODE_ID_COL_NAME, EdgesTable.GEOMETRY_COL_NAME, longCoord, latCoord, END_NODE_LEN_COL_NAME,
                EdgesTable.TABLE_NAME);

        Cursor cursor = db.rawQuery(SQL_NEAREST_POINT, null);

        double len1 = -1.0f, len2 = -1.0f;
        long startNodeId = -1, endNodeId = -1;
        if (cursor.moveToFirst())
        {
            len1 = cursor.getDouble(cursor.getColumnIndexOrThrow(START_NODE_LEN_COL_NAME));
            startNodeId = cursor.getLong(cursor.getColumnIndexOrThrow(EdgesTable.START_NODE_ID_COL_NAME));
            len2 = cursor.getDouble(cursor.getColumnIndexOrThrow(END_NODE_LEN_COL_NAME));
            endNodeId = cursor.getLong(cursor.getColumnIndexOrThrow(EdgesTable.END_NODE_ID_COL_NAME));
        }
        cursor.close();

        if (len1 < 0.0f)
            return -1;
        else
        {
            if (len1 < len2)
                return startNodeId;
            else
                return endNodeId;
        }
    }

    public double getDistance(Node n1, Node n2) throws IllegalArgumentException
    {
        if (n1 == null)
            throw new IllegalArgumentException("RoutesDbProvider.getDistance - null first node argument");

        if (n2 == null)
            throw new IllegalArgumentException("RoutesDbProvider.getDistance - null second node argument");

        return getDistance(n1.getGeoCoords(), n2.getGeoCoords());
    }

    public double getDistance(GeoCoords geoCoords1, GeoCoords geoCoords2) throws IllegalArgumentException
    {
        if (geoCoords1 == null)
            throw new IllegalArgumentException("RoutesDbProvider.getDistance - null first node argument");

        if (geoCoords2 == null)
            throw new IllegalArgumentException("RoutesDbProvider.getDistance - null second node argument");

        double lat1 = geoCoords1.getLatitude();
        double long1 = geoCoords1.getLongitude();

        double lat2 = geoCoords2.getLatitude();
        double long2 = geoCoords2.getLongitude();

        final String SQL_LENGTH = String.format(Locale.US,
                "SELECT Distance(Transform(GeomFromText('Point(%f %f)', %d), %d), " +
                "Transform(GeomFromText('Point(%f %f)', %d), %d)) AS %s;",
                long1, lat1, OSMClassLib.OSM_SRID, OSMClassLib.ETRS89_SRID,
                long2, lat2, OSMClassLib.OSM_SRID, OSMClassLib.ETRS89_SRID,
                LENGTH_COL_NAME);

        Cursor cursor = db.rawQuery(SQL_LENGTH, null);

        double len = -1.0f;
        if (cursor.moveToFirst())
        {
            len = cursor.getDouble(cursor.getColumnIndex(LENGTH_COL_NAME));
        }
        cursor.close();

        return len;
    }

    public void finalize()
    {
        db.close();
    }

    // ==============================
    // Private methods / tasks
    // ==============================

    private static GeoCoords getNodeGeoCoordsFromEdge(String geom, int idx)
    {
        String coordsText = geom.substring(geom.indexOf('(') + 1, geom.indexOf(')')).replaceAll(",", "");
        String[] coordsArray = coordsText.split(" ");
        switch (idx)
        {
            case 0:
            case 1:
            {
                return new GeoCoords(Double.parseDouble(coordsArray[idx * 2 + 1]),
                        Double.parseDouble(coordsArray[idx * 2]));
            }
            default:
                return null;
        }
    }

    private static String getLengthSQL(String geomColName, String lengthColName, int srcSrid, int dstSrid)
    {
        return String.format(Locale.US,
                "Distance(Transform(GeometryN(GeomFromText(%s, %d), 1), %d), " +
                "Transform(GeometryN(GeomFromText(%s, %d), 2), %d)) AS %s",
                geomColName, srcSrid, dstSrid, geomColName, srcSrid, dstSrid, lengthColName);

    }

    /*class GetDatabaseTask extends AsyncTask<Boolean, Integer, SQLiteDatabase>
    {
        @Override
        protected SQLiteDatabase doInBackground(Boolean... isGettingWritable)
        {
            if(isGettingWritable.length > 0)
            {
                if (isGettingWritable[0])
                    return dbHelper.getWritableDatabase();
                else
                    return dbHelper.getReadableDatabase();
            }
            return null;
        }
    }*/
}
