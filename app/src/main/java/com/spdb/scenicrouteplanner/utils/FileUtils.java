package com.spdb.scenicrouteplanner.utils;

import java.io.File;

public final class FileUtils
{
    public static boolean fileExists(String filePath)
    {
        return new File(filePath).exists();
    }

    public static File createDir(String directory)
    {
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

    public static File buildPath(String directory, String filename)
    {
        File dir = createDir(directory);
        File file = new File(dir.getAbsolutePath() + "/" + filename);
        return file;
    }
}
