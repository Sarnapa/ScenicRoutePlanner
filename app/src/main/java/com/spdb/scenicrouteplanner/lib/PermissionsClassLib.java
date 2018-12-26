package com.spdb.scenicrouteplanner.lib;

import android.Manifest;

import java.util.ArrayList;
import java.util.Arrays;

public final class PermissionsClassLib
{
    // ==============================
    // Public fields
    // ==============================
    public final static ArrayList<String> DANGEROUS_PERMISSIONS = new ArrayList<String>()
    {
        {
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    };

    public final static int UNKNOWN_PERMISSION_CODE = 0x00;
    public final static int WRONG_PERMISSIONS_CODE = 0x01;
    public final static int SOME_PERMISSIONS_CODE = 0x02;
    public final static int WRITE_EXTERNAL_STORAGE_CODE = 0x03;
    public final static int ACCESS_FINE_LOCATION_CODE = 0x04;

    // ==============================
    // Public methods
    // ==============================
    public static int getRequestPermissionCode(String... requestPermissions)
    {
        ArrayList<String> requestPermissionsList = new ArrayList<>(Arrays.asList(requestPermissions));
        int requestPermissionsCount = requestPermissionsList.size();
        if (requestPermissionsCount == 1)
        {
            switch (requestPermissionsList.get(0))
            {
                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    return WRITE_EXTERNAL_STORAGE_CODE;
                case Manifest.permission.ACCESS_FINE_LOCATION:
                    return ACCESS_FINE_LOCATION_CODE;
                default:
                    return UNKNOWN_PERMISSION_CODE;
            }
        }
        else
        {
            if (requestPermissionsCount <= DANGEROUS_PERMISSIONS.size()
                    && DANGEROUS_PERMISSIONS.containsAll(requestPermissionsList))
                return SOME_PERMISSIONS_CODE;
            else
                return WRONG_PERMISSIONS_CODE;
        }
    }
}
