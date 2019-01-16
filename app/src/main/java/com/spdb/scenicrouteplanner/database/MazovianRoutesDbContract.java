package com.spdb.scenicrouteplanner.database;

final class MazovianRoutesDbContract
{
    private MazovianRoutesDbContract()
    { }

    interface GeometryColumn
    {
        static final String GEOMETRY_COL_NAME = "geometry";
    }

    static class RoadsNodesTable implements GeometryColumn
    {
        static final String TABLE_NAME = "roads_nodes";
        static final String NODE_ID_COL_NAME = "node_id";
        static final String OSM_ID_COL_NAME = "osm_id";
        static final String CARDINALITY_COL_NAME = "cardinality";
    }

    static class RoadsTable implements GeometryColumn
    {
        static final String TABLE_NAME = "roads";
        static final String ID_COL_NAME = "id";
        static final String CLASS_COL_NAME = "class";
        static final String NODE_FROM_COL_NAME = "node_from";
        static final String NODE_TO_COL_NAME = "node_to";
        static final String NAME_COL_NAME = "name";
        static final String ONEWAY_FROMTO_COL_NAME = "oneway_fromto";
        static final String ONEWAY_TOFROM_COL_NAME = "oneway_tofrom";
        static final String LENGTH_COL_NAME = "length";
        static final String COST_COL_NAME = "cost";
    }

    static class RoadsNetDataTable
    {
        static final String TABLE_NAME = "roads_net_data";
        static final String ID_COL_NAME = "Id";
        static final String NETWORK_DATA_COL_NAME = "NetworkData";
    }

    static class RoadsNetTable
    {
        static final String TABLE_NAME = "roads_net";
        static final String ALGORITHM_COL_NAME = "Algorithm";
        static final String ARCROWID_COL_NAME = "ArcRowid";
        static final String NODEFROM_COL_NAME = "NodeFrom";
        static final String NODETO_COL_NAME = "NodeTo";
        static final String COST_COL_NAME = "Cost";
        static final String GEOMETRY_COL_NAME = "Geometry";
        static final String NAME_COL_NAME = "Name";
    }

    // Nazwy kolumn w zapytaniach
    static final String DISTANCE_COL_NAME = "distance";
    static final String GEOMETRY_TEXT_COL_NAME = "geometry_text";
    static final String SCENIC_ROUTE_COL_NAME = "ScenicRoute";

    // Tablice tymczasowe
    // Ta sama struktura co RoadsNetTable
    static final String SHORTEST_PATH_TABLE_NAME = "shortest_path";
}

enum GeometryType
{
    POINT,
    LINESTRING,
    POLYGON,
    MULTIPOINT,
    MULTILINESTRING,
    MULTIPOLYGON,
    GEOMETRYCOLLECTION
}
