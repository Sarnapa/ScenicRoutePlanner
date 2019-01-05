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

        public boolean isScenicRoute(){
            if(this.name().equals("UNCLASSIFIED") || this.name().equals("RESIDENTIAL") || this.name().equals("SERVICE"))
                return true;
            else
                return false;
        }
    }

    public static final int OSM_SRID = 4326;
    public static final int ETRS89_SRID = 3035;
}
