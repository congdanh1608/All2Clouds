package com.example.ypham.all2clouds;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Environment;
import android.util.Xml;
import android.widget.TextView;

import com.stericson.RootTools.RootTools;

import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Silver Wolf on 27/11/2014.
 */
public class Tools {
    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }


    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if internal storage is available to at least write */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public boolean isInternalStorageWritable(File file) {
        String state = Environment.getStorageState(file);
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    //Check Folder and if it not exit then create New Folder
    public boolean createFolder(String path) {
        File folder = new File(path);
        if (folder.exists()) {
            return true;
        } else {
            try {
                folder.mkdirs();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    //Delete Files in folder With Source Direct
    public void Deletefiles(String src) {
        File dir = new File(src);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {                        //if file is Directory
                        Deletefiles(file.getAbsolutePath());
                        file.delete();
                    } else {
                        file.delete();
                    }
                }
            }
        }
    }

    //Create XML File
    public File CreateXML(String path, String filename) {
        File newxmlfile = new File(path, filename);
        try {
            newxmlfile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream fileos = null;
        try {
            fileos = new FileOutputStream(newxmlfile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        XmlSerializer serializer = Xml.newSerializer();
        try {
            serializer.setOutput(fileos, "UTF-8");
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.text("\n");
            serializer.startTag(null, "main");
            serializer.text("\n");
            serializer.endTag(null, "main");
            serializer.endDocument();
            serializer.flush();
            fileos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return newxmlfile;
    }

    protected Boolean CheckRoot(){
        return RootTools.isRootAvailable();
    }

    public String ReadRawTextFile(Activity activity, int resId){
        InputStream inputStream = activity.getResources().openRawResource(resId);

        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        StringBuilder text = new StringBuilder();

        try {
            while (( line = buffreader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            return null;
        }
        return text.toString();
    }

    public Dialog createDialog(Activity activity, String title, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final TextView input = new TextView(activity);
        input.setPadding(30,5,25,0);
        input.setText(content);
        builder.setView(input);
        builder.setTitle(title)
                .setCancelable(false)//("Are you sure you want to exit?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = builder.create();
        return alertDialog;
    }

}
