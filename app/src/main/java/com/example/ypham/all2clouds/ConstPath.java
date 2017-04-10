package com.example.ypham.all2clouds;

import android.os.Environment;

import java.io.File;

/**
 * Created by Y Pham on 11/27/2014.
 */
public class ConstPath {
    final static String PATH_TO_MY_BACKUP = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyBackup/";
    final static String PATH_TO_ALL2CLOUDS = Environment.getExternalStorageDirectory().getAbsolutePath() + "/All2Clouds/";
    final static File PATH_TO_SD = Environment.getExternalStorageDirectory();
    final static String PATH_TO_DATA = Environment.getDataDirectory().getAbsolutePath();
    final static String DBDIR = "/BackupFiles/";

    final static String TYPE_XML = "xml";
    final static String TYPE_ZIP = "zip";

    final static String TAG = "All2Clouds";

    //Path of data
    final static String STORE_BACKUP_DIRECTORY = "/sdcard/MyBackup/";
    final static String STORE_RESTORE_DIRECTORY = "/sdcard/All2Clouds/";
    final static String PATH_DATA_BROWSER = "/data/com.android.browser/databases/";
    final static String PATH_DATA_CONTACT = "/data/com.android.providers.contacts/databases/";
    final static String PATH_MMS_SMS = "/data/com.android.providers.telephony/databases/";
    final static String PATH_CALENDAR = "/data/com.android.providers.calendar/databases/";
    final static String PATH_WIFI = "/misc/wifi/";
    //File data name
    final static String APP_DIRECTORY = "App/";
    final static String FILE_NAME_SMS = "mmssms.db";
    final static String FILE_NAME_WIFI = "wpa_supplicant.conf";
    final static String FILE_CONTACT = "contacts2.db";
    final static String FILE_CALENDAR = "calendar.db";
    final static String BROWSER_DIRECTORY = "Browser/*";
    //


}
