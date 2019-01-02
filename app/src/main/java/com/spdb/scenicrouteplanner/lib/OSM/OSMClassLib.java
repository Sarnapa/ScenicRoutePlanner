package com.spdb.scenicrouteplanner.lib.OSM;

public final class OSMClassLib
{
    public enum WayType
    {
        MOTORWAY,
        TRUNK,
        PRIMARY,
        SECONDARY,
        TERTIARY,
        UNCLASSIFIED,
        RESIDENTIAL,
        SERVICE,
        UNKNOWN;

        public static String getWayTypesListText()
        {
            return String.format("'%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s'", MOTORWAY.name(),
                    TRUNK.name(), PRIMARY.name(), SECONDARY.name(), TERTIARY.name(),
                    UNCLASSIFIED.name(), RESIDENTIAL.name(), SERVICE.name(),
                    UNKNOWN.name());
        }
    }

    public static final int OSM_SRID = 3857;
}
