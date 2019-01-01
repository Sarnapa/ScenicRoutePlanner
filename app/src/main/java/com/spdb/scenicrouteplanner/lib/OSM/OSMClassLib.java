package com.spdb.scenicrouteplanner.lib.OSM;

public final class OSMClassLib
{
    public enum WayType
    {
        MOTORWAY("motorway"),
        TRUNK("trunk"),
        PRIMARY("primary"),
        SECONDARY("secondary"),
        TERTIARY("tertiary"),
        UNCLASSIFIED("unclassified"),
        RESIDENTIAL("residential"),
        SERVICE("service"),
        UNKNOWN("unknown");

        private final String wayName;

        public String getWayName()
        {
            return wayName;
        }

        public static String getWayTypesListText()
        {
            return String.format("'%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s'", MOTORWAY.getWayName(),
                    TRUNK.getWayName(), PRIMARY.getWayName(), SECONDARY.getWayName(), TERTIARY.getWayName(),
                    UNCLASSIFIED.getWayName(), RESIDENTIAL.getWayName(), SERVICE.getWayName(),
                    UNKNOWN.getWayName());
        }

        WayType(String value)
        {
            wayName = value;
        }
    }

    public static final int OSM_SRID = 3857;
}
