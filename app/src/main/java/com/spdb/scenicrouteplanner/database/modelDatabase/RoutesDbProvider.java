package com.spdb.scenicrouteplanner.database.modelDatabase;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.spdb.scenicrouteplanner.lib.GeoCoords;
import com.spdb.scenicrouteplanner.lib.OSM.OSMClassLib;
import com.spdb.scenicrouteplanner.lib.PathsClassLib;
import com.spdb.scenicrouteplanner.model.Edge;
import com.spdb.scenicrouteplanner.model.Node;
import com.spdb.scenicrouteplanner.model.Way;
import com.spdb.scenicrouteplanner.utils.FileUtils;

import static com.spdb.scenicrouteplanner.database.modelDatabase.RoutesDbContract.*;
import static com.spdb.scenicrouteplanner.lib.OSM.OSMClassLib.*;

import org.spatialite.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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

    // ==============================
    // Database service
    // ==============================
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
            GeoCoords coords = n.getGeoCoords();

            ContentValues values = new ContentValues();
            values.put(NodesTable._ID, n.getId());
            if(coords != null)
            {
                values.put(NodesTable.LATITUDE_COL_NAME, coords.getLatitude());
                values.put(NodesTable.LONGTITUDE_COL_NAME, coords.getLongitude());
            }

            db.insert(NodesTable.TABLE_NAME, null, values);
        }
        catch (Exception e)
        {
            Log.d("ROUTES_DB_PROVIDER", e.getMessage());
        }
    }

    public void addNodes(List<Node> nodes)
    {
        db.beginTransaction();
        for (Node n : nodes)
            addNode(n);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void addEdge(Edge e)
    {
        try
        {
            List<Edge> existedEdgeRes = getEdges(String.format("%s = %d AND %s = %d",
                    EdgesTable.START_NODE_ID_COL_NAME, e.getStartNodeId(),
                    EdgesTable.END_NODE_ID_COL_NAME, e.getEndNodeId()));
            if (existedEdgeRes == null || existedEdgeRes.size() == 0)
            {
                Node startNode = getNodeById(e.getStartNodeId());
                Node endNode = getNodeById(e.getEndNodeId());
                GeoCoords startNodeGeoCoords = startNode.getGeoCoords();
                GeoCoords endNodeGeoCoords = endNode.getGeoCoords();

               /* ContentValues values = new ContentValues();
                values.put(EdgesTable._ID, e.getId());
                values.put(EdgesTable.WAY_ID_COL_NAME, e.getWayId());
                values.put(EdgesTable.START_NODE_ID_COL_NAME, startNode.getId());
                values.put(EdgesTable.END_NODE_ID_COL_NAME, endNode.getId());
                values.put(EdgesTable.IS_TOUR_ROUTE_COL_NAME, e.isTourRoute() ? 1 : 0);

                // W 4326 najpierw long, później lat
                values.put(EdgesTable.GEOMETRY_COL_NAME, String.format(Locale.US,
                        "GeomFromText('%s(%f %f, %f %f)', %d)",
                        GeometryType.MULTIPOINT.name(),
                        startNodeGeoCoords.getLongitude(), startNodeGeoCoords.getLatitude(),
                        endNodeGeoCoords.getLongitude(), endNodeGeoCoords.getLatitude(),
                        OSM_SRID));

                db.insert(EdgesTable.TABLE_NAME, null, values);*/

                // W 4326 najpierw long, później lat
                String SQL_INSERT_EDGE = String.format(Locale.US, "INSERT INTO %s VALUES (%d, %d, %d, %d, %d, " +
                       "GeomFromText('%s(%f %f, %f %f)', %d));",
                        EdgesTable.TABLE_NAME, e.getId(), e.getWayId(), startNode.getId(), endNode.getId(), e.isTourRoute() ? 1 : 0,
                        GeometryType.MULTIPOINT.name(), startNodeGeoCoords.getLongitude(), startNodeGeoCoords.getLatitude(),
                        endNodeGeoCoords.getLongitude(), endNodeGeoCoords.getLatitude(), OSM_SRID);

                db.execSQL(SQL_INSERT_EDGE);
            }
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
            /*Long startNodeId = e.getStartNodeId();
            Long endNodeId = e.getEndNodeId();
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
            }*/
            addEdge(e);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void addWay(Way w)
    {
        try
        {
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
    public void setScenicRoute(List<Way> ways)
    {
        db.beginTransaction();
        for (Way w: ways)
        {
            ContentValues values = new ContentValues();
            values.put(WaysTable.IS_SCENIC_ROUTE_COL_NAME, w.isScenicRoute());

            updateWayById(w.getId(), values);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

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
        db.update(EdgesTable.TABLE_NAME, values, String.format("%s = %d", EdgesTable._ID, id), null);
    }

    public void updateWayById(long id, ContentValues values)
    {
        db.update(WaysTable.TABLE_NAME, values, String.format("%s = %d", WaysTable._ID, id), null);
    }
    // ==============================
    // Removing data methods
    // ==============================

    public void clearDb()
    {
        removeAllWays();
        removeAllNodes();
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

    public Node getNodeById(long id)
    {
        List<Node> res = getNodes(String.format("%s = %d", NodesTable._ID, id));

        return res.size() == 1 ? res.get(0) : null;
    }

    public List<Node> getNodes(String whereClause)
    {
        String[] nodesProjection = {
                NodesTable._ID,
                NodesTable.LATITUDE_COL_NAME,
                NodesTable.LONGTITUDE_COL_NAME
        };

        Cursor nodesCursor = db.query(NodesTable.TABLE_NAME,
                nodesProjection,
                whereClause,
                null,
                null,
                null,
                null);

        List<Node> res = new ArrayList();

        while (nodesCursor.moveToNext())
        {
            long id = nodesCursor.getLong(nodesCursor.getColumnIndex(NodesTable._ID));
            double latCoord = nodesCursor.getDouble(nodesCursor.getColumnIndex(NodesTable.LATITUDE_COL_NAME));
            double longCoord = nodesCursor.getDouble(nodesCursor.getColumnIndex(NodesTable.LONGTITUDE_COL_NAME));

            List<Edge> edges = getEdges(String.format("%s = %d", EdgesTable.START_NODE_ID_COL_NAME, id));

            res.add(new Node(id, new GeoCoords(latCoord, longCoord), edges));
        }
        nodesCursor.close();

        return res;
    }

    public Edge getEdgeById(long id)
    {
        List<Edge> res = getEdges(String.format("%s = %d", EdgesTable._ID, id));

        return res.size() == 1 ? res.get(0) : null;
    }

    public List<Edge> getEdges(String whereClause)
    {
        List<Edge> res = new ArrayList<>();

        String[] edgesProjection = {
                EdgesTable._ID,
                EdgesTable.WAY_ID_COL_NAME,
                EdgesTable.START_NODE_ID_COL_NAME,
                EdgesTable.END_NODE_ID_COL_NAME,
                EdgesTable.IS_TOUR_ROUTE_COL_NAME,
                //EdgesTable.GEOMETRY_COL_NAME,
                getLengthSQL(EdgesTable.GEOMETRY_COL_NAME, LENGTH_COL_NAME, ETRS89_SRID)
        };

        Cursor edgesCursor = db.query(EdgesTable.TABLE_NAME,
                edgesProjection,
                whereClause,
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
            //String edgeGeom = edgesCursor.getString(edgesCursor.getColumnIndexOrThrow(EdgesTable.GEOMETRY_COL_NAME));
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
            e.setWayId(wayId);
            e.setWayInfo(edgeWayInfo);
            e.setStartNodeId(startNodeId);
            e.setEndNodeId(endNodeId);
            e.setLength(edgeLength);
            e.setTourRoute(isTourRoute);

            res.add(e);
            wayCursor.close();
        }
        edgesCursor.close();

        return res;
    }

    public Way getWayById(long id)
    {
        List<Way> res = getWays(String.format("%s = %d", WaysTable._ID, id));

        return res.size() == 1 ? res.get(0) : null;
    }

    public List<Way> getWays(String whereClause)
    {
        String[] waysProjection = {
                WaysTable._ID,
                WaysTable.WAY_TYPE_COL_NAME,
                WaysTable.IS_SCENIC_ROUTE_COL_NAME,
                WaysTable.MAX_SPEED_COL_NAME
        };

        Cursor waysCursor = db.query(WaysTable.TABLE_NAME,
                waysProjection,
                whereClause,
                null,
                null,
                null,
                null);

        List<Way> res = new ArrayList();

        while (waysCursor.moveToNext())
        {
            long id = waysCursor.getLong(waysCursor.getColumnIndex(WaysTable._ID));
            WayType wayType = WayType.valueOf(waysCursor.getString(waysCursor.getColumnIndex(WaysTable.WAY_TYPE_COL_NAME)));
            boolean isScenicRoute = waysCursor.getInt(waysCursor.getColumnIndex(WaysTable.IS_SCENIC_ROUTE_COL_NAME)) == 1;
            int maxSpeed = waysCursor.getInt(waysCursor.getColumnIndex(WaysTable.MAX_SPEED_COL_NAME));

            res.add(new Way(id, wayType, isScenicRoute, maxSpeed));
        }
        waysCursor.close();

        return res;
    }

    public Node getClosestNode(GeoCoords coords) throws IllegalArgumentException
    {
        if (coords == null)
            throw new IllegalArgumentException("RoutesDbProvider.getClosestNodeId - null coords argument");

        return getNodeById(getClosestNodeId(coords));
    }

    public long getClosestNodeId(GeoCoords coords) throws IllegalArgumentException
    {
        if (coords == null)
            throw new IllegalArgumentException("RoutesDbProvider.getClosestNodeId - null coords argument");

        double latCoord = coords.getLatitude();
        double longCoord = coords.getLongitude();

        final String START_NODE_LEN_COL_NAME = "len1";
        final String END_NODE_LEN_COL_NAME = "len2";

        final String SQL_NEAREST_POINT = String.format(Locale.US,
                "SELECT Min(Distance(%s, GeomFromText('Point(%f %f)'))), \n" +
                "%s, Distance(GeometryN(%s, 1), GeomFromText('Point(%f %f)')) AS %s,\n" +
                "%s, Distance(GeometryN(%s, 2), GeomFromText('Point(%f %f)')) AS %s\n" +
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

    public long getNodesCount()
    {
        return getTableRowsCount(NodesTable.TABLE_NAME);
    }

    public long getEdgesCount()
    {
        return getTableRowsCount(EdgesTable.TABLE_NAME);
    }

    public long getWaysCount()
    {
        return getTableRowsCount(WaysTable.TABLE_NAME);
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

    private static String getLengthSQL(String geomColName, String lengthColName, int dstSrid)
    {
        return String.format(Locale.US,
                "Distance(Transform(GeometryN(AsText(%s), 1), %d), " +
                "Transform(GeometryN(AsText(%s), 2), %d)) AS %s",
                geomColName, dstSrid, geomColName, dstSrid, lengthColName);

    }

    private long getTableRowsCount(String tableName)
    {
        String countQuery = "SELECT * FROM " + tableName;
        Cursor cursor = db.rawQuery(countQuery, null);
        long count = cursor.getCount();
        cursor.close();

        return count;
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
