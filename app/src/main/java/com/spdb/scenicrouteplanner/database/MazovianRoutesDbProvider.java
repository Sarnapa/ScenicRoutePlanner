package com.spdb.scenicrouteplanner.database;

import android.database.Cursor;
import android.util.Log;

import com.spdb.scenicrouteplanner.database.structures.ScenicRoutesPathRow;
import com.spdb.scenicrouteplanner.database.structures.ScenicRoutesPathStats;
import com.spdb.scenicrouteplanner.lib.GeoCoords;
import com.spdb.scenicrouteplanner.lib.PathsClassLib;
import com.spdb.scenicrouteplanner.utils.FileUtils;

import org.spatialite.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.spdb.scenicrouteplanner.lib.OSM.OSMClassLib.OSM_SRID;

public class MazovianRoutesDbProvider {
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
    public MazovianRoutesDbProvider() {
        FileUtils.createDir(PathsClassLib.DB_DIRECTORY);
        if (FileUtils.fileExists(DATABASE_PATH))
            db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
    }

    // ==============================
    // Database service
    // ==============================
    public boolean isDbAvailable() {
        return FileUtils.fileExists(DATABASE_PATH) && db != null;
    }

    public void startTransaction() {
        db.beginTransaction();
    }

    public void commitTransaction() {
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void finalize() {
        db.close();
    }

    // ==============================
    // Algorithm service
    // ==============================

    // Przygotowanie bazy danych przed zastosowanie algorytmu do wyszukania danej trasy.
    // Polega na usunięciu istniejących wcześniej baz tymczasowych, które zawierały wynik poprzedniego wyszukania.
    // Także ustawia algorytm wyszukiwania optymalnej trasy na algorytm A*.
    public void prepareDb() {
        final String SQL_DROP_SHORTEST_PATH_RESULT_TABLE = getDropTableSQL(
                MazovianRoutesDbContract.ShortestPathResultTable.TABLE_NAME);
        final String SQL_DROP_SHORTEST_PATH_ROUTES_TABLE = getDropTableSQL(
                MazovianRoutesDbContract.ShortestPathRoutesTable.TABLE_NAME);
        final String SQL_DROP_SCENIC_ROUTES_PATH_TABLE = getDropTableSQL(
                MazovianRoutesDbContract.ScenicRoutesPathTable.TABLE_NAME);

        db.execSQL(SQL_DROP_SHORTEST_PATH_RESULT_TABLE);
        db.execSQL(SQL_DROP_SHORTEST_PATH_ROUTES_TABLE);
        db.execSQL(SQL_DROP_SCENIC_ROUTES_PATH_TABLE);

        setAStarAlgorithm();
    }

    // Ustawienie algorytmu wyszukiwania optymalnej trasy na algorytm A*.
    private void setAStarAlgorithm() {
        db.beginTransaction();

        final String SQL_SET_ASTAR = String.format(Locale.US, "UPDATE %s SET %s = 'A*'",
                MazovianRoutesDbContract.RoadsNetTable.TABLE_NAME,
                MazovianRoutesDbContract.RoadsNetTable.ALGORITHM_COL_NAME);
        db.execSQL(SQL_SET_ASTAR);

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    // Wyliczenie optymalnej trasy, z której będziemy korzystać na etapie ustalania trasy,
    // która uwzględnia warunek minimalnej długości odcinków widokowych.
    public void getShortestPath(long startNodeId, long endNodeId) {
        if (startNodeId < 0)
            throw new IllegalArgumentException("MazovianRoutesDbProvider.calculateShortestPath - invalid startNodeId");

        if (endNodeId < 0)
            throw new IllegalArgumentException("MazovianRoutesDbProvider.calculateShortestPath - invalid endNodeId");

        db.beginTransaction();

        // Wyliczenie optymalnej trasy
        final String SQL_CREATE_SHORTEST_PATH_RESULT = String.format(Locale.US,
                "CREATE TEMP TABLE shortest_path_result AS\n" +
                        "SELECT * FROM roads_net\n" +
                        "WHERE NodeFrom = %d AND NodeTo = %d;",
                startNodeId, endNodeId);
        db.execSQL(SQL_CREATE_SHORTEST_PATH_RESULT);

        // Zebranie z wyniku algorytmu poszczególne odcinki (bez pierwszego wiersza, bedącego całościowym
        // rezultatem algorytmu) i oznaczenie, ktory jest widokowy.
        final String SQL_CREATE_SHORTEST_PATH_ROUTES = String.format(Locale.US,
                "CREATE TEMP TABLE shortest_path_routes AS\n" +
                        "SELECT spr.ArcRowid AS ArcRowid, spr.NodeFrom AS NodeFrom, spr.NodeTo AS NodeTo,\n" +
                        "spr.Cost AS Cost, r.length AS Length, r.Geometry AS Geometry, r.Name AS Name,\n" +
                        "CASE\n" +
                        "\tWHEN r.class LIKE 'unclassified' THEN 1\n" +
                        "\tELSE 0\n" +
                        "\tEND ScenicRoute\n" +
                        "FROM shortest_path_result spr, roads r\n" +
                        "WHERE r.id = spr.ArcRowid;");
        db.execSQL(SQL_CREATE_SHORTEST_PATH_ROUTES);

        // Przygotowanie tabeli, zawierającej ostateczną trasę, z uwzględnieniemm warunku odcinków widokowych.
        final String SQL_CREATE_SCENIC_ROUTES_PATH = String.format(Locale.US,
                "CREATE TEMP TABLE scenic_routes_path AS\n" +
                        "SELECT * FROM shortest_path_routes\n" +
                        "LIMIT 1;\n" +
                        "DELETE FROM scenic_routes_path;\n");
        db.execSQL(SQL_CREATE_SCENIC_ROUTES_PATH);

        // Przygotowanie tabeli, która zawiera wyłącznie 1 wiersz, podsumowujący optymalną trasę.
        final String SQL_PREPARE_SHORTEST_PATH_RESULT = String.format(Locale.US,
                "DELETE FROM shortest_path_result\n" +
                        "WHERE ArcRowid IS NOT NULL;");
        db.execSQL(SQL_PREPARE_SHORTEST_PATH_RESULT);

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    // Jeśli spełniony jest warunek odcinków widokowych przez optymalną trasę - przenosimy ją do wynikowej tabeli.
    public void moveShortestPathToScenicRoutesPath() {
        db.beginTransaction();

        final String SQL_DROP_SCENIC_ROUTES_PATH_TABLE = getDropTableSQL(
                MazovianRoutesDbContract.ScenicRoutesPathTable.TABLE_NAME);
        db.execSQL(SQL_DROP_SCENIC_ROUTES_PATH_TABLE);

        final String SQL_RENAME_SHORTEST_PATH_ROUTES = String.format(Locale.US,
                "ALTER TABLE shortest_path_routes RENAME TO scenic_routes_path;");
        db.execSQL(SQL_RENAME_SHORTEST_PATH_ROUTES);

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void cleanScenicRoutesPath()
    {
        db.beginTransaction();

        final String SQL_DELETE_SCENIC_ROUTES_PATH = String.format(Locale.US,
                "DELETE FROM scenic_routes_path");
        db.execSQL(SQL_DELETE_SCENIC_ROUTES_PATH);

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    // Pobranie odcinków widokowych, znajdujących się w odległości nie większej niż podany argument.
    public void getScenicRoutesBuffer(double bufferSize)
    {
        db.beginTransaction();

        final String SQL_DROP_SCENIC_ROUTES_BUFFER_TABLE = getDropTableSQL(
                MazovianRoutesDbContract.ScenicRoutesBufferTable.TABLE_NAME);
        db.execSQL(SQL_DROP_SCENIC_ROUTES_BUFFER_TABLE);

        final String SQL_CREATE_SCENIC_ROUTES_BUFFER = String.format(Locale.US,
                "CREATE TEMP TABLE scenic_routes_buffer\n" +
                "AS\n" +
                "SELECT r.id AS Id, r.osm_id AS OsmId, r.node_from AS NodeFrom, r.node_to AS NodeTo, r.oneway_fromto AS OnewayFromTo,\n" +
                "r.oneway_tofrom AS OnewayToFrom, r.length AS Length, r.cost AS Cost, r.geometry AS Geometry, r.name AS Name\n" +
                "FROM roads r, shortest_path_result spr\n" +
                "WHERE r.class LIKE 'unclassified' AND Distance(spr.geometry, r.geometry) < %f;", bufferSize);
        db.execSQL(SQL_CREATE_SCENIC_ROUTES_BUFFER);

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    // Łączenie podanego punktu z najbliższym odcinkiem widokowym.
    // Zwraca identyfikator węzła, który jest końcem dotychczas wyliczonej trasy.
    public long getPathToNearestScenicRoute(long nodeId)
    {
        if (nodeId < 0)
            throw new IllegalArgumentException("MazovianRoutesDbProvider.getTourToNearestScenicRoute - invalid nodeId");

        final String SQL_INSERT_PATH_TO_NEAREST_SCENIC_ROUTE = String.format(Locale.US,
                "WITH nearest_scenic_route AS\n" +
                "(\n" +
                "SELECT rn.node_id AS NodeId, rn.geometry AS NodeGeometry, srb.Id AS ArcRowid,\n" +
                "srb.NodeFrom AS NodeFrom, srb.NodeTo AS NodeTo, srb.OnewayFromTo AS OnewayFromTo, srb.OnewayToFrom AS OnewayToFrom,\n" +
                "srb.Cost AS Cost, srb.Length AS Length, srb.Geometry AS Geometry, srb.Name AS Name,\n" +
                "Min(Distance(rn.geometry, srb.geometry)) FROM scenic_routes_buffer srb, roads_nodes rn\n" +
                "WHERE rn.node_id = %d\n" +
                "),\n" +
                "available_nodes AS\n" +
                "(\n" +
                "SELECT rn.node_id AS NodeId, rn.geometry AS Geometry\n" +
                "FROM nearest_scenic_route nsr, roads_nodes rn\n" +
                "WHERE (rn.node_id = nsr.NodeFrom AND nsr.OnewayFromTo = 1)\n" +
                "OR (rn.node_id = nsr.NodeTo AND nsr.OnewayToFrom = 1)\n" +
                "),\n" +
                "chosen_node AS\n" +
                "(\n" +
                "SELECT an.NodeId AS NodeId, Min(Distance(nsr.NodeGeometry, an.Geometry))\n" +
                "FROM nearest_scenic_route nsr, available_nodes an\n" +
                "),\n" +
                "astar_result AS\n" +
                "(\n" +
                "SELECT rn.ArcRowid AS ArcRowid, rn.NodeFrom AS NodeFrom, rn.NodeTo AS NodeTo,\n" +
                "rn.Cost AS Cost FROM roads_net rn, nearest_scenic_route nsr, chosen_node cn\n" +
                "WHERE rn.ArcRowid IS NOT NULL AND rn.NodeFrom = nsr.NodeId AND rn.NodeTo = cn.NodeId\n" +
                ")\n" +
                "INSERT INTO scenic_routes_path\n" +
                "SELECT ar.ArcRowid, ar.NodeFrom, ar.NodeTo,\n" +
                "ar.Cost, r.length, r.geometry, r.name,\n" +
                "CASE\n" +
                "WHEN r.class LIKE 'unclassified' THEN 1\n" +
                "\tELSE 0\n" +
                "\tEND\n" +
                "FROM astar_result ar LEFT JOIN roads r ON ar.ArcRowid = r.id\n" +
                "UNION ALL\n" +
                "SELECT nsr.ArcRowid, nsr.NodeFrom, nsr.NodeTo,\n" +
                "nsr.Cost, nsr.length, nsr.geometry, nsr.name, 1\n" +
                "FROM nearest_scenic_route nsr;", nodeId);
        db.execSQL(SQL_INSERT_PATH_TO_NEAREST_SCENIC_ROUTE);

        // Usuwanie duplikatów, które mogły powstać poprzez dodanie docelowego odcinka widokowego,
        // który znalazł się w wyszukanej trasie.
        final String SQL_DELETE_DUPLICATES = String.format(Locale.US,
                "DELETE FROM scenic_routes_path\n" +
                "WHERE ROWID IN\n" +
                "(\n" +
                "SELECT ROWID FROM scenic_routes_path AS srp\n" +
                "WHERE srp.ArcRowid = scenic_routes_path.ArcRowid\n" +
                "LIMIT -1 OFFSET 1\n" +
                ");");
        db.execSQL(SQL_DELETE_DUPLICATES);

        // Usunięcie odcinków widokowych z bufora, które zostały wstawione do docelowej trasy.
        final String SQL_DELETE_USED_SCENIC_ROUTES = String.format(Locale.US,
                "DELETE FROM scenic_routes_buffer\n" +
                "WHERE Id IN\n" +
                "(SELECT ArcRowid FROM scenic_routes_path);");
        db.execSQL(SQL_DELETE_USED_SCENIC_ROUTES);

        // Pobranie identyfikatora ostatniego węzła dotychczasowej trasy.
        final String SQL_SELECT_NODE_TO = String.format(Locale.US,
                "SELECT NodeTo FROM scenic_routes_path ORDER BY ROWID DESC LIMIT 1;");
        Cursor cursor = db.rawQuery(SQL_SELECT_NODE_TO, null);

        long res = -1;
        if (cursor.moveToFirst())
        {
            res = cursor.getLong(0);
        }
        cursor.close();

        return res;
    }

    // Łączy węzeł o podanym identyfikatorze z podanym końcowym węzłem.
    // Wyliczona trasa jest dodawana do tablicy wynikowej.
    public void getPathToDestination(long nodeId, long endNodeId)
    {
        if (nodeId < 0)
            throw new IllegalArgumentException("MazovianRoutesDbProvider.getPathToDestination - invalid nodeId");

        if (endNodeId < 0)
            throw new IllegalArgumentException("MazovianRoutesDbProvider.getPathToDestination - invalid endNodeId");

        final String SQL_INSERT_PATH_TO_DESTINATION = String.format(Locale.US,
                "WITH path_result AS\n" +
                "(\n" +
                "SELECT * FROM roads_net\n" +
                "WHERE NodeFrom = %d AND NodeTo = %d\n" +
                ")\n" +
                "INSERT INTO scenic_routes_path\n" +
                "SELECT pr.ArcRowid AS ArcRowid, pr.NodeFrom AS NodeFrom, pr.NodeTo AS NodeTo,\n" +
                "pr.Cost AS Cost, r.length AS Length, r.Geometry AS Geometry, r.Name AS Name,\n" +
                "CASE\n" +
                "\tWHEN r.class LIKE 'unclassified' THEN 1\n" +
                "\tELSE 0\n" +
                "\tEND ScenicRoute\n" +
                "FROM path_result pr, roads r\n" +
                "WHERE r.id = pr.ArcRowid;\n",
                nodeId, endNodeId);
        db.execSQL(SQL_INSERT_PATH_TO_DESTINATION);
    }

    // Pobranie sumy długości odcinków widokowych, które należą do optymalnej trasy.
    public double getShortestPathScenicRoutesSumLength()
    {
        final String SQL_SELECT_SCENIC_ROUTES_SUM_LENGTH = String.format(Locale.US,
                "SELECT SUM(Length) AS %s FROM shortest_path_routes\n" +
                "WHERE ScenicRoute = 1;", MazovianRoutesDbContract.SCENIC_ROUTES_LENGTH_COL_NAME);

        Cursor cursor = db.rawQuery(SQL_SELECT_SCENIC_ROUTES_SUM_LENGTH, null);

        double res = -1.0f;
        if (cursor.moveToFirst())
        {
            res = cursor.getDouble(cursor.getColumnIndex(MazovianRoutesDbContract.SCENIC_ROUTES_LENGTH_COL_NAME));
        }
        cursor.close();

        return res;
    }

    // Pobranie sumy długości odcinków widokowych, które należą do bufora, otaczającego optymalną trasę
    public double getScenicRoutesBufferScenicRoutesSumLength()
    {
        final String SQL_SELECT_SCENIC_ROUTES_SUM_LENGTH = String.format(Locale.US,
                "SELECT SUM(Length) AS %s FROM scenic_routes_buffer;",
                MazovianRoutesDbContract.SCENIC_ROUTES_LENGTH_COL_NAME);

        Cursor cursor = db.rawQuery(SQL_SELECT_SCENIC_ROUTES_SUM_LENGTH, null);

        double res = -1.0f;
        if (cursor.moveToFirst())
        {
            res = cursor.getDouble(cursor.getColumnIndex(MazovianRoutesDbContract.SCENIC_ROUTES_LENGTH_COL_NAME));
        }
        cursor.close();

        return res;
    }

    // Pobranie sumy długości odcinków widokowych, które należą do aktualnej trasy.
    public double getScenicRoutesPathScenicRoutesSumLength()
    {
        final String SQL_SELECT_SCENIC_ROUTES_SUM_LENGTH = String.format(Locale.US,
                "SELECT SUM(Length) AS %s FROM scenic_routes_path\n" +
                "WHERE ScenicRoute = 1;",
                MazovianRoutesDbContract.SCENIC_ROUTES_LENGTH_COL_NAME);

        Cursor cursor = db.rawQuery(SQL_SELECT_SCENIC_ROUTES_SUM_LENGTH, null);

        double res = -1.0f;
        if (cursor.moveToFirst())
        {
            res = cursor.getDouble(cursor.getColumnIndex(MazovianRoutesDbContract.SCENIC_ROUTES_LENGTH_COL_NAME));
        }
        cursor.close();

        return res;
    }

    // Sprawdzenie, ile jest odcinków widokowych w buforze.
    public long getScenicRoutesBufferSize()
    {
        final String SQL_SELECT_COUNT_SCENIC_ROUTES_BUFFER = String.format(Locale.US,
                "SELECT count(*) FROM scenic_routes_buffer;");

        Cursor cursor = db.rawQuery(SQL_SELECT_COUNT_SCENIC_ROUTES_BUFFER, null);

        long res = -1;
        if (cursor.moveToFirst())
        {
            res = cursor.getInt(0);
        }
        cursor.close();

        return res;
    }

    public Cursor getScenicRoutesPath()
    {
        try {
            final String SQL_SELECT_SCENIC_ROUTES_PATH = String.format(Locale.US,
                    "SELECT %s, %s, %s, %s, %s, AsText(%s) AS %s, %s, %s FROM %s",
                    MazovianRoutesDbContract.ScenicRoutesPathTable.ARCROWID_COL_NAME,
                    MazovianRoutesDbContract.ScenicRoutesPathTable.NODEFROM_COL_NAME,
                    MazovianRoutesDbContract.ScenicRoutesPathTable.NODETO_COL_NAME,
                    MazovianRoutesDbContract.ScenicRoutesPathTable.COST_COL_NAME,
                    MazovianRoutesDbContract.ScenicRoutesPathTable.LENGTH_COL_NAME,
                    MazovianRoutesDbContract.ScenicRoutesPathTable.GEOMETRY_COL_NAME, MazovianRoutesDbContract.GEOMETRY_TEXT_COL_NAME,
                    MazovianRoutesDbContract.ScenicRoutesPathTable.NAME_COL_NAME,
                    MazovianRoutesDbContract.ScenicRoutesPathTable.SCENIC_ROUTE_COL_NAME,
                    MazovianRoutesDbContract.ScenicRoutesPathTable.TABLE_NAME);

            return db.rawQuery(SQL_SELECT_SCENIC_ROUTES_PATH, null);
        }
        catch (Exception e)
        {
            Log.d("MAZOVIAN_ROUTES_DB_PROVIDER", e.getMessage());
            return null;
        }
    }

    // Pamiętać, żeby przed użyciem "przesunąć" kursor
    public ScenicRoutesPathRow getScenicRoutesPathRow(Cursor cursor)
    {
        ScenicRoutesPathRow res = new ScenicRoutesPathRow();

        if (!cursor.isClosed()) {

            Long arcRowid = cursor.getLong(cursor.getColumnIndex(
                    MazovianRoutesDbContract.ScenicRoutesPathTable.ARCROWID_COL_NAME
            ));
            long nodeFrom = cursor.getLong(cursor.getColumnIndex(
                    MazovianRoutesDbContract.ScenicRoutesPathTable.NODEFROM_COL_NAME
            ));
            long nodeTo = cursor.getLong(cursor.getColumnIndex(
                    MazovianRoutesDbContract.ScenicRoutesPathTable.NODETO_COL_NAME
            ));
            double cost = cursor.getDouble(cursor.getColumnIndex(
                    MazovianRoutesDbContract.ScenicRoutesPathTable.COST_COL_NAME
            ));
            double length = cursor.getDouble(cursor.getColumnIndex(
                    MazovianRoutesDbContract.ScenicRoutesPathTable.LENGTH_COL_NAME
            ));
            String geometry = cursor.getString(cursor.getColumnIndex(
                    MazovianRoutesDbContract.GEOMETRY_TEXT_COL_NAME
            ));
            String name = cursor.getString(cursor.getColumnIndex(
                    MazovianRoutesDbContract.ScenicRoutesPathTable.NAME_COL_NAME
            ));
            boolean isScenicRoute = cursor.getInt(cursor.getColumnIndex(
                    MazovianRoutesDbContract.ScenicRoutesPathTable.SCENIC_ROUTE_COL_NAME
            )) == 1;

            res = new ScenicRoutesPathRow(arcRowid, nodeFrom, nodeTo, cost, length, geometry, name, isScenicRoute);
        }

        return res;
    }

    public ScenicRoutesPathStats getScenicRoutesPathStats()
    {
        ScenicRoutesPathStats res = new ScenicRoutesPathStats();

        final String SQL_SELECT_NODES = String.format(Locale.US,
                "SELECT NodeFrom, NodeTo FROM shortest_path_result");
        final String SQL_SELECT_SCENIC_ROUTES_PATH_LENGTH = String.format(Locale.US,
                "SELECT SUM(Length) AS %s FROM scenic_routes_path",
                MazovianRoutesDbContract.LENGTH_COL_NAME);
        final String SQL_SELECT_SCENIC_ROUTES_PATH_SCENIC_ROUTES_LENGTH = String.format(Locale.US,
                "SELECT SUM(Length) AS %s FROM scenic_routes_path\n" +
                "WHERE ScenicRoute = 1",
                MazovianRoutesDbContract.SCENIC_ROUTES_LENGTH_COL_NAME);
        final String SQL_SELECT_SCENIC_ROUTES_PATH_COST = String.format(Locale.US,
                "SELECT SUM(Cost) AS %s FROM scenic_routes_path",
                MazovianRoutesDbContract.COST_COL_NAME);

        Cursor cursor = db.rawQuery(SQL_SELECT_NODES, null);
        if (cursor.moveToFirst()) {

            long startNodeId = cursor.getLong(cursor.getColumnIndex(
                    MazovianRoutesDbContract.ShortestPathResultTable.NODEFROM_COL_NAME
            ));
            long endNodeId = cursor.getLong(cursor.getColumnIndex(
                    MazovianRoutesDbContract.ShortestPathResultTable.NODETO_COL_NAME
            ));
            res.setStartNodeId(startNodeId);
            res.setEndNodeId(endNodeId);
        }
        cursor.close();

        cursor = db.rawQuery(SQL_SELECT_SCENIC_ROUTES_PATH_LENGTH, null);
        if (cursor.moveToFirst()) {

            double length = cursor.getDouble(cursor.getColumnIndex(
                    MazovianRoutesDbContract.LENGTH_COL_NAME
            ));
            res.setLength(length);
        }
        cursor.close();

        cursor = db.rawQuery(SQL_SELECT_SCENIC_ROUTES_PATH_SCENIC_ROUTES_LENGTH, null);
        if (cursor.moveToFirst()) {

            double scenicRoutesLength = cursor.getDouble(cursor.getColumnIndex(
                    MazovianRoutesDbContract.SCENIC_ROUTES_LENGTH_COL_NAME
            ));
            res.setScenicRoutesLength(scenicRoutesLength);
        }
        cursor.close();

        cursor = db.rawQuery(SQL_SELECT_SCENIC_ROUTES_PATH_COST, null);
        if (cursor.moveToFirst()) {

            double cost = cursor.getDouble(cursor.getColumnIndex(
                    MazovianRoutesDbContract.COST_COL_NAME
            ));
            res.setCost(cost);
        }
        cursor.close();

        return res;
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

    public List<GeoCoords> getRouteGeoCoords(long nodeFrom, long nodeTo)
    {
        if (nodeFrom < 0)
            throw new IllegalArgumentException("MazovianRoutesDbProvider.getGeoCoordsRoute - invalid nodeFrom");

        if (nodeTo < 0)
            throw new IllegalArgumentException("MazovianRoutesDbProvider.getGeoCoordsRoute - invalid nodeTo");

        final String SQL_GET_ROAD_CLASS = String.format(Locale.US, "SELECT AsText(%s) AS %s FROM %s WHERE " +
                        "(%s=%d AND %s=%d AND %s=1) OR (%s=%d AND %s=%d AND %s=1)",
                MazovianRoutesDbContract.RoadsTable.GEOMETRY_COL_NAME,
                MazovianRoutesDbContract.GEOMETRY_TEXT_COL_NAME,
                MazovianRoutesDbContract.RoadsTable.TABLE_NAME,
                MazovianRoutesDbContract.RoadsTable.NODE_FROM_COL_NAME, nodeFrom,
                MazovianRoutesDbContract.RoadsTable.NODE_TO_COL_NAME, nodeTo,
                MazovianRoutesDbContract.RoadsTable.ONEWAY_FROMTO_COL_NAME,
                MazovianRoutesDbContract.RoadsTable.NODE_FROM_COL_NAME, nodeTo,
                MazovianRoutesDbContract.RoadsTable.NODE_TO_COL_NAME, nodeFrom,
                MazovianRoutesDbContract.RoadsTable.ONEWAY_TOFROM_COL_NAME);
        Cursor cursor = db.rawQuery(SQL_GET_ROAD_CLASS, null);

        List<GeoCoords> geoCoords = new ArrayList<>();
        String geoCoordsText = null;
        if (cursor.moveToFirst())
            geoCoordsText = cursor.getString(cursor.getColumnIndex(MazovianRoutesDbContract.GEOMETRY_TEXT_COL_NAME));
        cursor.close();

        if (geoCoords != null || !geoCoords.isEmpty())
            geoCoords = getRouteGeoCoords(geoCoordsText);

        return geoCoords;
    }

    // ==============================
    // Private methods
    // ==============================
    private static String getDropTableSQL(String table)
    {
        return String.format(Locale.US, "DROP TABLE IF EXISTS %s;", table);
    }

    private static List<GeoCoords> getRouteGeoCoords(String geom)
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
