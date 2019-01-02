package com.spdb.scenicrouteplanner.database;

import android.provider.BaseColumns;

final class RoutesDbContract
{
    private RoutesDbContract()
    {
    }

    interface GeometryColumn
    {
        public static final String GEOMETRY_COL_NAME = "geometry";
    }

    static class NodesTable implements BaseColumns, GeometryColumn
    {
        static final String TABLE_NAME = "nodes";
    }

    static class EdgesTable implements BaseColumns, GeometryColumn
    {
        static final String TABLE_NAME = "edges";
        static final String WAY_ID_COL_NAME = "way_id";
        static final String START_NODE_ID_COL_NAME = "start_node_id";
        static final String END_NODE_ID_COL_NAME = "end_node_id";
        static final String IS_TOUR_ROUTE_COL_NAME = "is_tour_route";
        static final String WAY_ID_FOREIGN_KEY_CONSTRAINT_NAME = "way_id_fk";
        static final String START_NODE_ID_FOREIGN_KEY_CONSTRAINT_NAME = "start_node_id_fk";
        static final String END_NODE_ID_FOREIGN_KEY_CONSTRAINT_NAME = "end_node_id_fk";
        static final String IS_TOUR_ROUTE_CHECK_NAME = "is_tour_route_chk";
    }

    static class WaysTable implements BaseColumns
    {
        static final String TABLE_NAME = "ways";
        static final String WAY_TYPE_COL_NAME = "way_type";
        static final String IS_SCENIC_ROUTE_COL_NAME = "is_scenic_route";
        static final String MAX_SPEED_COL_NAME = "max_speed";
        static final String WAY_TYPE_CHECK_NAME = "way_type_chk";
        static final String IS_SCENIC_ROUTE_CHECK_NAME = "is_scenic_route_chk";
    }

    static final String LENGTH_COL_NAME = "len";
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