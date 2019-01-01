package com.spdb.scenicrouteplanner.database;

import android.content.Context;
import org.spatialite.database.SQLiteDatabase;

public class RoutesDbProvider
{
    private RoutesDbHelper dbHelper;

    public RoutesDbProvider(Context context)
    {
        dbHelper = new RoutesDbHelper(context);
    }

    public SQLiteDatabase getRoutesDb()
    {
        return dbHelper.getReadableDatabase();
    }
}
