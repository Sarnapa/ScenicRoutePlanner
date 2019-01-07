package com.spdb.scenicrouteplanner.lib;

import android.os.Environment;

public class PathsClassLib
{
    public static final String MAIN_DIRECTORY = Environment.getExternalStorageDirectory() + "/SRP/";
    public static final String MAPS_DIRECTORY = MAIN_DIRECTORY.concat("maps");
    public static final String DB_DIRECTORY = MAIN_DIRECTORY.concat("databases");
}
