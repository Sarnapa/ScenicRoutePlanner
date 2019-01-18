package com.spdb.scenicrouteplanner.database;

final class MazovianRoutesDbContract
{
    private MazovianRoutesDbContract()
    { }

    interface GeometryColumn
    {
        static final String GEOMETRY_COL_NAME = "geometry";
    }

    // ==============================
    // Tabele - Mazowieckie
    // ==============================
    static class RoadsTable
    {
        static final String TABLE_NAME = "roads";
        static final String ID_COL_NAME = "id";
        static final String OSM_ID_COL_NAME = "osm_id";
        static final String CLASS_COL_NAME = "class";
        static final String NODE_FROM_COL_NAME = "node_from";
        static final String NODE_TO_COL_NAME = "node_to";
        static final String NAME_COL_NAME = "name";
        static final String ONEWAY_FROMTO_COL_NAME = "oneway_fromto";
        static final String ONEWAY_TOFROM_COL_NAME = "oneway_tofrom";
        static final String LENGTH_COL_NAME = "length";
        static final String COST_COL_NAME = "cost";
        static final String GEOMETRY_COL_NAME = "geometry";
    }

    static class RoadsNodesTable
    {
        static final String TABLE_NAME = "roads_nodes";
        static final String NODE_ID_COL_NAME = "node_id";
        static final String OSM_ID_COL_NAME = "osm_id";
        static final String CARDINALITY_COL_NAME = "cardinality";
        static final String GEOMETRY_COL_NAME = "geometry";
    }

    static class RoadsNetTable
    {
        static final String TABLE_NAME = "roads_net";
        static final String ALGORITHM_COL_NAME = "Algorithm";
        static final String ARCROWID_COL_NAME = "ArcRowId";
        static final String NODEFROM_COL_NAME = "NodeFrom";
        static final String NODETO_COL_NAME = "NodeTo";
        static final String COST_COL_NAME = "Cost";
        static final String GEOMETRY_COL_NAME = "Geometry";
        static final String NAME_COL_NAME = "Name";
    }

    static class RoadsNetDataTable
    {
        static final String TABLE_NAME = "roads_net_data";
        static final String ID_COL_NAME = "Id";
        static final String NETWORKDATA_COL_NAME = "NetworkData";
    }

    // ==============================
    // Tabele tymczasowe
    // ==============================
    static class ShortestPathResultTable
    {
        static final String TABLE_NAME = "shortest_path_result";
        static final String ALGORITHM_COL_NAME = "Algorithm";
        static final String ARCROWID_COL_NAME = "ArcRowid";
        static final String NODEFROM_COL_NAME = "NodeFrom";
        static final String NODETO_COL_NAME = "NodeTo";
        static final String COST_COL_NAME = "Cost";
        static final String GEOMETRY_COL_NAME = "Geometry";
        static final String NAME_COL_NAME = "Name";
    }

    static class ShortestPathRoutesTable
    {
        static final String TABLE_NAME = "shortest_path_routes";
        static final String ARCROWID_COL_NAME = "ArcRowid";
        static final String NODEFROM_COL_NAME = "NodeFrom";
        static final String NODETO_COL_NAME = "NodeTo";
        static final String COST_COL_NAME = "Cost";
        static final String LENGTH_COL_NAME = "Length";
        static final String GEOMETRY_COL_NAME = "Geometry";
        static final String NAME_COL_NAME = "Name";
        static final String SCENIC_ROUTE_COL_NAME = "ScenicRoute";
    }

    static class ScenicRoutesPathTable
    {
        static final String TABLE_NAME = "scenic_routes_path";
        static final String ARCROWID_COL_NAME = "ArcRowid";
        static final String NODEFROM_COL_NAME = "NodeFrom";
        static final String NODETO_COL_NAME = "NodeTo";
        static final String COST_COL_NAME = "Cost";
        static final String LENGTH_COL_NAME = "Length";
        static final String GEOMETRY_COL_NAME = "Geometry";
        static final String NAME_COL_NAME = "Name";
        static final String SCENIC_ROUTE_COL_NAME = "ScenicRoute";
    }

    static class ScenicRoutesBufferTable
    {
        static final String TABLE_NAME = "scenic_routes_buffer";
        static final String ID_COL_NAME = "Id";
        static final String OSMID_COL_NAME = "OsmId";
        static final String CLASS_COL_NAME = "Class";
        static final String NODEFROM_COL_NAME = "NodeFrom";
        static final String NODETO_COL_NAME = "NodeTo";
        static final String ONEWAYFROMTO_COL_NAME = "OnewayFromTo";
        static final String ONEWAYTOFROM_COL_NAME = "OnewayToFrom";
        static final String LENGTH_COL_NAME = "Length";
        static final String COST_COL_NAME = "Cost";
        static final String GEOMETRY_COL_NAME = "Geometry";
        static final String NAME_COL_NAME = "Name";;
    }

    // ==============================
    // Nazwy kolumn w zapytaniach
    // ==============================
    static final String DISTANCE_COL_NAME = "distance";
    static final String GEOMETRY_TEXT_COL_NAME = "geometry_text";
    static final String LENGTH_COL_NAME = "Length";
    static final String COST_COL_NAME = "Cost";
    static final String SCENIC_ROUTES_LENGTH_COL_NAME = "ScenicRoutesLength";

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
