package com.spdb.scenicrouteplanner.database;

import android.database.Cursor;
import android.util.Log;

import com.spdb.scenicrouteplanner.database.structures.PathResult;
import com.spdb.scenicrouteplanner.lib.GeoCoords;
import com.spdb.scenicrouteplanner.lib.OSM.OSMClassLib;
import com.spdb.scenicrouteplanner.lib.PathsClassLib;
import com.spdb.scenicrouteplanner.utils.FileUtils;

import org.spatialite.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.spdb.scenicrouteplanner.lib.OSM.OSMClassLib.OSM_SRID;

public class MazovianRoutesDbProvider
{
    // ==============================
    // Private fields
    // ==============================
    private static final String DATABASE_NAME = "mazowieckie.sqlite";
    private static final String DATABASE_PATH = PathsClassLib.DB_DIRECTORY + "/" + DATABASE_NAME;

    private SQLiteDatabase db;

    // W stopniach
    private static double MAX_DISTANCE_TO_NODE = 0.001;

    // ==============================
    // Constructors
    // ==============================
    public MazovianRoutesDbProvider()
    {
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

    public void finalize()
    {
        db.close();
    }

    // ==============================
    // Shortest path service
    // ==============================

    public void calculateShortestPath(long startNodeId, long endNodeId)
    {
        if (startNodeId < 0)
            throw new IllegalArgumentException("MazovianRoutesDbProvider.calculateShortestPath - invalid startNodeId");

        if (endNodeId < 0)
            throw new IllegalArgumentException("MazovianRoutesDbProvider.calculateShortestPath - invalid endNodeId");

        setAStarAlgorithm();

        final String SQL_DROP_TABLE = String.format(Locale.US, "DROP TABLE IF EXISTS %s;",
                MazovianRoutesDbContract.SHORTEST_PATH_TABLE_NAME);

        db.execSQL(SQL_DROP_TABLE);

        final String SQL_CALC_SHORTEST_PATH = String.format(Locale.US, "CREATE TEMP TABLE %s AS " +
                "SELECT * FROM %s WHERE %s=%d AND %s=%d;", MazovianRoutesDbContract.SHORTEST_PATH_TABLE_NAME,
                MazovianRoutesDbContract.RoadsNetTable.TABLE_NAME,
                MazovianRoutesDbContract.RoadsNetTable.NODEFROM_COL_NAME, startNodeId,
                MazovianRoutesDbContract.RoadsNetTable.NODETO_COL_NAME, endNodeId);

        db.execSQL(SQL_CALC_SHORTEST_PATH);
    }

    public Cursor getShortestPath()
    {
        try {
            final String SQL_GET_SHORTEST_PATH = String.format(Locale.US, "SELECT %s, %s, %s, %s, %s, AsText(%s) AS %s, %s FROM %s",
                    MazovianRoutesDbContract.RoadsNetTable.ALGORITHM_COL_NAME,
                    MazovianRoutesDbContract.RoadsNetTable.ARCROWID_COL_NAME,
                    MazovianRoutesDbContract.RoadsNetTable.NODEFROM_COL_NAME,
                    MazovianRoutesDbContract.RoadsNetTable.NODETO_COL_NAME,
                    MazovianRoutesDbContract.RoadsNetTable.COST_COL_NAME,
                    MazovianRoutesDbContract.RoadsNetTable.GEOMETRY_COL_NAME, MazovianRoutesDbContract.GEOMETRY_TEXT_COL_NAME,
                    MazovianRoutesDbContract.RoadsNetTable.NAME_COL_NAME,
                    MazovianRoutesDbContract.SHORTEST_PATH_TABLE_NAME);

            return db.rawQuery(SQL_GET_SHORTEST_PATH, null);
        }
        catch (Exception e)
        {
            Log.d("MAZOVIAN_ROUTES_DB_PROVIDER", e.getMessage());
            return null;
        }
    }

    // Pamiętać, żeby przed użyciem "przesunąć" kursor
    public PathResult getPathResult(Cursor cursor)
    {
        PathResult res = new PathResult();

        if (!cursor.isClosed()) {
            String algorithm = cursor.getString(cursor.getColumnIndex(
                    MazovianRoutesDbContract.RoadsNetTable.ALGORITHM_COL_NAME
            ));
            Long arcRowid = cursor.getLong(cursor.getColumnIndex(
                    MazovianRoutesDbContract.RoadsNetTable.ARCROWID_COL_NAME
            ));
            long nodeFrom = cursor.getLong(cursor.getColumnIndex(
                    MazovianRoutesDbContract.RoadsNetTable.NODEFROM_COL_NAME
            ));
            long nodeTo = cursor.getLong(cursor.getColumnIndex(
                    MazovianRoutesDbContract.RoadsNetTable.NODETO_COL_NAME
            ));
            double cost = cursor.getDouble(cursor.getColumnIndex(
                    MazovianRoutesDbContract.RoadsNetTable.COST_COL_NAME
            ));
            String geometry = cursor.getString(cursor.getColumnIndex(
                    MazovianRoutesDbContract.GEOMETRY_TEXT_COL_NAME
            ));
            String name = cursor.getString(cursor.getColumnIndex(
                    MazovianRoutesDbContract.RoadsNetTable.NAME_COL_NAME
            ));

            res = new PathResult(algorithm, arcRowid, nodeFrom, nodeTo, cost, geometry, name);
        }

        return res;
    }

    private void setAStarAlgorithm()
    {
        db.beginTransaction();

        final String SQL_SET_ASTAR = String.format(Locale.US, "UPDATE %s SET %s = 'A*'",
                MazovianRoutesDbContract.RoadsNetTable.TABLE_NAME,
                MazovianRoutesDbContract.RoadsNetTable.ALGORITHM_COL_NAME);
        db.execSQL(SQL_SET_ASTAR);

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    // ==============================
    // Spatial service
    // ==============================

    public long getClosestNodeId(GeoCoords coords) throws IllegalArgumentException
    {
        if (coords == null)
            throw new IllegalArgumentException("MazovianRoutesDbProvider.getClosestNodeId - null coords argument");

        double latCoord = coords.getLatitude();
        double longCoord = coords.getLongitude();

        final String SQL_NEAREST_NODE = String.format(Locale.US,
                "SELECT %s , Min(Distance(%s, GeomFromText('Point(%f %f)', %d))) AS %s \n" +
                "FROM %s;", MazovianRoutesDbContract.RoadsNodesTable.NODE_ID_COL_NAME,
                MazovianRoutesDbContract.RoadsNodesTable.GEOMETRY_COL_NAME, longCoord, latCoord, OSM_SRID,
                MazovianRoutesDbContract.DISTANCE_COL_NAME,
                MazovianRoutesDbContract.RoadsNodesTable.TABLE_NAME);

        Cursor cursor = db.rawQuery(SQL_NEAREST_NODE, null);

        double distance = -1.0f;
        long nodeId = -1;
        if (cursor.moveToFirst())
        {
            nodeId = cursor.getLong(cursor.getColumnIndexOrThrow(MazovianRoutesDbContract.RoadsNodesTable.NODE_ID_COL_NAME));
            distance = cursor.getDouble(cursor.getColumnIndexOrThrow(MazovianRoutesDbContract.DISTANCE_COL_NAME));
        }
        cursor.close();

        if (nodeId < 0 || distance < -1.0f || distance > MAX_DISTANCE_TO_NODE)
            return -1;
        else
            return nodeId;
    }

    // ==============================
    // Reading methods
    // ==============================
    public GeoCoords getNodeGeoCoords(long nodeId)
    {
        if (nodeId < 0)
            throw new IllegalArgumentException("MazovianRoutesDbProvider.calculateShortestPath - invalid nodeId");

        final String SQL_GET_GEO_COORDS = String.format(Locale.US, "SELECT AsText(%s) AS %s FROM %s WHERE %s=%d;",
                MazovianRoutesDbContract.RoadsNodesTable.GEOMETRY_COL_NAME,
                MazovianRoutesDbContract.GEOMETRY_TEXT_COL_NAME,
                MazovianRoutesDbContract.RoadsNodesTable.TABLE_NAME,
                MazovianRoutesDbContract.RoadsNodesTable.NODE_ID_COL_NAME,
                nodeId);

        Cursor cursor = db.rawQuery(SQL_GET_GEO_COORDS, null);
        GeoCoords res = null;
        if (cursor.moveToFirst())
        {
            String nodeGeoCoordsText = cursor.getString(cursor.getColumnIndex(
                    MazovianRoutesDbContract.GEOMETRY_TEXT_COL_NAME
            ));
            res = getNodeGeoCoordsFromNode(nodeGeoCoordsText);
        }
        cursor.close();

        return res;
    }

    public boolean isScenicRouteByNodes(long nodeFrom, long nodeTo)
    {
        if (nodeFrom < 0)
            throw new IllegalArgumentException("MazovianRoutesDbProvider.calculateShortestPath - invalid nodeFrom");

        if (nodeTo < 0)
            throw new IllegalArgumentException("MazovianRoutesDbProvider.calculateShortestPath - invalid nodeTo");

        final String SQL_GET_ROAD_CLASS = String.format(Locale.US, "SELECT %s FROM %s WHERE " +
                "(%s=%d AND %s=%d AND %s=1) OR (%s=%d AND %s=%d AND %s=1)",
                MazovianRoutesDbContract.RoadsTable.CLASS_COL_NAME,
                MazovianRoutesDbContract.RoadsTable.TABLE_NAME,
                MazovianRoutesDbContract.RoadsTable.NODE_FROM_COL_NAME, nodeFrom,
                MazovianRoutesDbContract.RoadsTable.NODE_TO_COL_NAME, nodeTo,
                MazovianRoutesDbContract.RoadsTable.ONEWAY_FROMTO_COL_NAME,
                MazovianRoutesDbContract.RoadsTable.NODE_FROM_COL_NAME, nodeTo,
                MazovianRoutesDbContract.RoadsTable.NODE_TO_COL_NAME, nodeFrom,
                MazovianRoutesDbContract.RoadsTable.ONEWAY_TOFROM_COL_NAME);
        Cursor cursor = db.rawQuery(SQL_GET_ROAD_CLASS, null);

        String wayTypeName = "";
        if (cursor.moveToFirst())
            wayTypeName = cursor.getString(cursor.getColumnIndex(MazovianRoutesDbContract.RoadsTable.CLASS_COL_NAME));

        if (wayTypeName != null && !wayTypeName.isEmpty())
            return OSMClassLib.WayType.isScenicRoute(wayTypeName);
        else
            return false;
    }

    // ==============================
    // Private methods
    // ==============================
    private static List<GeoCoords> getNodeGeoCoordsFromEdge(String geom)
    {
        List<GeoCoords> res = new ArrayList();
        String coordsText = geom.substring(geom.indexOf('(') + 1, geom.indexOf(')')).replaceAll(",", "");
        String[] coordsArray = coordsText.split(" ");
        for (int i = 0; i < coordsArray.length; i += 2)
        {
            res.add(new GeoCoords(Double.parseDouble(coordsArray[i + 1]),
                    Double.parseDouble(coordsArray[i])));
        }

        return res;
    }

    private static GeoCoords getNodeGeoCoordsFromNode(String geom)
    {
        String coordsText = geom.substring(geom.indexOf('(') + 1, geom.indexOf(')')).replaceAll(",", "");
        String[] coordsArray = coordsText.split(" ");
        if (coordsArray.length == 2)
            return new GeoCoords(Double.parseDouble(coordsArray[1]), Double.parseDouble(coordsArray[0]));
        else
            return null;
    }
}
