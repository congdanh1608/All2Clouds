package com.example.ypham.all2clouds;

import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Silver Wolf on 27/11/2014.
 */
public class Unzipping {
    static public void unzip(File archive, File outputDir)
    {
        try {
            ZipFile zipfile = new ZipFile(archive);
            for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                unzipEntry(zipfile, entry, outputDir);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    static private void unzipEntry(ZipFile zipfile, ZipEntry entry, File outputDir) throws IOException
    {
        if (entry.isDirectory()) {
            createDirectory(new File(outputDir, entry.getName()));
            return;
        }

        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists()){
            createDirectory(outputFile.getParentFile());
        }
        BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        try {
            IOUtils.copy(inputStream, outputStream);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            outputStream.close();
            inputStream.close();
        }
    }

    static private void createDirectory(File dir)
    {
        Log.d("control", "ZipHelper.createDir() - Creating directory: " + dir.getName());
        if (!dir.exists()){
            if(!dir.mkdirs()) throw new RuntimeException("Can't create directory "+dir);
        }
        else Log.d("control", "ZipHelper.createDir() - Exists directory: " + dir.getName());
    }
}
