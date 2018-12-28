package com.spdb.scenicrouteplanner.utils;

import android.os.Environment;

import java.io.File;

public final class FileUtils
{
    public static File buildPath(String directory, String filename)
    {
        File dir = new File(Environment.getExternalStorageDirectory() + directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir.getAbsolutePath() + "/" + filename);
        return file;
    }
}
