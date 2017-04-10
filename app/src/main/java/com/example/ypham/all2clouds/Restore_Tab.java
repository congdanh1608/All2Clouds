package com.example.ypham.all2clouds;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Browser;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Y Pham on 11/27/2014.
 */
public class Restore_Tab extends Activity {
    Button btRestore, btCancel;
    Spinner spinner;
    CheckBox cbApp, cbMmsSms, cbContact, cbWifiDb, cbCalevent, cbBookmark;
    CheckBox checkbox;
    LinearLayout group_cb;
    Tools tools;
    String item_select_name;
    Boolean flagRoot = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restore_tab);
        tools = new Tools();


        btRestore = (Button) this.findViewById(R.id.bt_restore);
        btCancel = (Button) this.findViewById(R.id.bt_rscancel);
        spinner = (Spinner) this.findViewById(R.id.spinner_selpackage);

        group_cb = (LinearLayout) this.findViewById(R.id.group_cb);

        //Load Backup Package when Click Backup Tab First.
//        LoadBackupPackage();

        //Event select spinner.
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                item_select_name = spinner.getSelectedItem().toString();
                LoadXMLBackupPackage(ConvertFileName2(item_select_name) + ".xml");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //YxPham Update
                //Thuc hien download file da chon truoc khi tien hanh restore
                DownloadBackupFile downloadFileBackup = new DownloadBackupFile(Restore_Tab.this,
                        ((MainActivity)getParent()).mDBU.getmApi(),
                        "/" + GetDeviceInfo().IMEI + "/",
                        ConstPath.TYPE_ZIP);
                String backupFileName = spinner.getSelectedItem().toString().replace(".xml", ".zip");
                downloadFileBackup.setBackupFileName(backupFileName);
                downloadFileBackup.execute();
                //Thuc hien doRestore tai onPostExcute() cua class DownloadBackupFile
                doRestore();
            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doCancel();
            }
        });

        if (tools.CheckRoot()) flagRoot = true;
        else flagRoot = false;
    }

    //------------Cap nhat YxPham

    @Override
    protected void onResume() {
        super.onResume();
        RemoveAllCheckBox();
        if (tools.createFolder(ConstPath.PATH_TO_ALL2CLOUDS)) {
            //Download cac file xml tren dropbox
            DownloadBackupFile download = new DownloadBackupFile(Restore_Tab.this,
                    ((MainActivity) getParent()).mDBU.getmApi(),
                    "/" + GetDeviceInfo().IMEI + "/",
                    ConstPath.TYPE_XML);
            download.execute();
        }else showToast("Error Create All2Clouds Folder!");
        LoadBackupPackage();
    }

    //--------------------

    //Event Restore Button.
    protected void doRestore() {

        //Xác định Checkbox của từng items.
        if (group_cb.getChildCount() > 0) {
            for (int i = 0; i < group_cb.getChildCount(); i++) {
                int id = group_cb.getChildAt(i).getId();
                if (id == 0) {
                    cbApp = (CheckBox) group_cb.getChildAt(i);
                } else if (id == 1) {
                    cbWifiDb = (CheckBox) group_cb.getChildAt(i);
                } else if (id == 2) {
                    cbMmsSms = (CheckBox) group_cb.getChildAt(i);
                } else if (id == 3) {
                    cbContact = (CheckBox) group_cb.getChildAt(i);
                } else if (id == 4) {
                    cbCalevent = (CheckBox) group_cb.getChildAt(i);
                } else if (id == 5) {
                    cbBookmark = (CheckBox) group_cb.getChildAt(i);
                }
            }
        }

        new Restore_AsyncTask().execute(cbApp, cbWifiDb, cbMmsSms, cbContact, cbCalevent, cbBookmark);

    }

    //Event Cancel Button
    private void doCancel(){
        GetCheckBox();
        if (cbApp!=null) cbApp.setChecked(false);
        if (cbMmsSms!=null) cbMmsSms.setChecked(false);
        if (cbContact!=null) cbContact.setChecked(false);
        if (cbWifiDb!=null) cbWifiDb.setChecked(false);
        if (cbCalevent!=null) cbCalevent.setChecked(false);
        if (cbBookmark!=null) cbBookmark.setChecked(false);
    }

    private void GetCheckBox(){
        //Xác định Checkbox của từng items.
        if (group_cb.getChildCount() > 0) {
            for (int i = 0; i < group_cb.getChildCount(); i++) {
                int id = group_cb.getChildAt(i).getId();
                if (id == 0) {
                    cbApp = (CheckBox) group_cb.getChildAt(i);
                } else if (id == 1) {
                    cbWifiDb = (CheckBox) group_cb.getChildAt(i);
                } else if (id == 2) {
                    cbMmsSms = (CheckBox) group_cb.getChildAt(i);
                } else if (id == 3) {
                    cbContact = (CheckBox) group_cb.getChildAt(i);
                } else if (id == 4) {
                    cbCalevent = (CheckBox) group_cb.getChildAt(i);
                } else if (id == 5) {
                    cbBookmark = (CheckBox) group_cb.getChildAt(i);
                }
            }
        }
    }
    //Get Device Info
    private Dinfo GetDeviceInfo() {
        String IMEI, IMSI, androidID, tmSerial, deviceModelName, deviceUser, deviceProduct, deviceHardWare, deviceBrand, myVersion;
        int sdkVersion;
        TelephonyManager tm;
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        IMEI = tm.getDeviceId();
        IMSI = tm.getSubscriberId();
        androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
//        UUID deviceUUID = new UUID(androidID.hashCode(), ((long)imei.hashCode() <<32) | tmSerial.hashCode());
//        deviceID = deviceUUID.toString();
        tmSerial = tm.getSimSerialNumber();
        deviceModelName = Build.MODEL;
        deviceUser = Build.USER;
        deviceProduct = Build.PRODUCT;
        deviceHardWare = Build.HARDWARE;
        deviceBrand = Build.BRAND;
        myVersion = Build.VERSION.RELEASE;
        sdkVersion = Build.VERSION.SDK_INT;

        Dinfo dinfo = new Dinfo(IMEI, IMSI, androidID, "", tmSerial, deviceModelName, deviceUser, deviceProduct, deviceHardWare, deviceBrand, myVersion, sdkVersion);
        return dinfo;
    }

    //Load all Package available.
    //Kiem tra neu thu muc rong
    public void LoadBackupPackage() {
        ArrayAdapter<String> adapter;
        List<String> PackageName = new ArrayList<String>();
        File path = new File(ConstPath.PATH_TO_ALL2CLOUDS);
        if (path.exists() && path.isDirectory()) {
            File[] files = path.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(".xml");
                }
            });
            for (File file : files) {
                PackageName.add(CovertFileName1(file.getName()));
            }
        }
        adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_restore, PackageName);
        adapter.setDropDownViewResource(R.layout.spinner_restore_dropdown);
        spinner.setAdapter(adapter);
    }

    //Convert file name to Date
    private String CovertFileName1(String filename){
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        Date date = null;
        String sdate = null;
        try {
            date = dateFormat.parse(filename);
            sdate = simpleDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return sdate;
    }

    //Convert Date to file Name.
    private String ConvertFileName2(String filename){
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String sdate = null;
        Date date = null;
        try {
            date = dateFormat.parse(filename);
            sdate = simpleDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return sdate;
    }

    //Load XML file in Package selected.
    protected void LoadXMLBackupPackage(String filename) {
        boolean app = false, wifi = false, mmssms = false, contact = false, calendar = false, bookark = false;
        XmlPullParserFactory fc = null;
        try {
            fc = XmlPullParserFactory.newInstance();
            XmlPullParser parser = fc.newPullParser();
            String xmlfile = "/sdcard/All2Clouds/" + filename;
            FileInputStream fis = new FileInputStream(xmlfile);
            parser.setInput(fis, "UTF-8");
            int eventType = -1;
            String nodeName;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                eventType = parser.next();
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.END_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        nodeName = parser.getName();
                        if (nodeName.equals("app")) {
                            if (parser.nextText().equals("true")) app = true;
                        } else if (nodeName.equals("wifi")) {
                            if (parser.nextText().equals("true")) wifi = true;
                        } else if (nodeName.equals("mmssms")) {
                            if (parser.nextText().equals("true")) mmssms = true;
                        } else if (nodeName.equals("contact")) {
                            if (parser.nextText().equals("true")) contact = true;
                        } else if (nodeName.equals("calendar")) {
                            if (parser.nextText().equals("true")) calendar = true;
                        } else if (nodeName.equals("bookmark")) {
                            if (parser.nextText().equals("true")) bookark = true;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                    default:
                        break;
                }
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Update Check Box
        //Remove all Checkbox in Group_cb LinearLayout
        RemoveAllCheckBox();
        //Add Checkbox theo ID 0,1,2,3,4,5 tương ứng.
        if (app) {
            AddCheckBox(0, "Application");
        }
        if (wifi) {
            AddCheckBox(1, "Wifi");
        }
        if (mmssms) {
            AddCheckBox(2, "MMS SMS");
        }
        if (contact) {
            AddCheckBox(3, "Contact");
        }
        if (calendar) {
            AddCheckBox(4, "Calendar");
        }
        if (bookark) {
            AddCheckBox(5, "Bookmark");
        }
    }

    //Add Checkbox in Layout.
    private void AddCheckBox(int id, String name){
        checkbox = new CheckBox(getBaseContext());
        checkbox.setId(id);
        checkbox.setClickable(true);
        checkbox.setChecked(true);
        checkbox.setText(name);
        checkbox.setTextColor(Color.BLACK);
        checkbox.setButtonDrawable(R.drawable.abc_btn_check_material);
        group_cb.addView(checkbox);
    }

    //Remove All in Group_cb LinearLayout.
   /* private void RemoveCheckBox(){
        group_cb.removeAllViews();
    }
*/
    //Remove All in Group_cb LinearLayout.
    private void RemoveAllCheckBox() {
        group_cb.removeAllViews();
    }

    //Restore Database of App.
    public Boolean RestoreAppDB(String path) {
        try {
            List<String> Folders = new ArrayList<String>();
            File files = new File(path);
            //Load Folder in Folder path.
            if (files.exists()) {
                for (File file : files.listFiles()) {
                    if (file.isDirectory()) {
                        //Check Package CloudBackup.
                        if (file.getName().equals(getPackageName())) {
                            //nothing
                        }else Folders.add(file.getName());
                    }
                }
            }

            //Copy App Database Folder.
            if (Folders != null) {
                for (int i = 0; i < Folders.size(); i++) {
                    String source = path + Folders.get(i);
                    String dest = Environment.getDataDirectory() + "/data/";
                    if (RootTools.remount(source, "rw")) {
                        String command = "cp -r " + source + " " + dest;
                        CommandCapture cmdCapture = new CommandCapture(0, command);
                        RootTools.getShell(true).add(cmdCapture);
//                        Process proc = Runtime.getRuntime().exec(new String[] { "su", "-c", command });
//                        proc.waitFor();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //Load APK and Install.
    public Boolean InstallApp(String pathApp) {
        try {
            File path = new File(pathApp);
            if (path.exists() && path.isDirectory()) {
                //Get Package APK.
                File[] files = path.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        if (filename.startsWith(getPackageName())) return false;
                        else return filename.endsWith(".apk");
                    }
                });

                //Install APK
                for (File file : files) {
                    InstallInBackground(pathApp + file.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

//    Install App in Background.
    public void InstallInBackground(String filename) {
        File file = new File(filename);
        if(file.exists()){
            try {
                final String command = "pm install -r " + file.getAbsolutePath();
                CommandCapture cmdCapture = new CommandCapture(0, command);
                RootTools.getShell(true).add(cmdCapture);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //Restore App
    public Boolean RestoreApp(){
        if (!InstallApp(ConstPath.PATH_TO_MY_BACKUP + ConstPath.APP_DIRECTORY)) return false;
        //path_to_MyBackup error
        if (!(RestoreAppDB(ConstPath.STORE_BACKUP_DIRECTORY + ConstPath.APP_DIRECTORY))) return false;
        return true;
    }

    //Copy wifi Database to Phone folder require ROOT
    public Boolean Restorewifidb() {
        String dest = ConstPath.PATH_TO_DATA + ConstPath.PATH_WIFI;
//          String source = path_to_MyBackup + "wpa_supplicant.conf";
        String source = ConstPath.STORE_BACKUP_DIRECTORY + ConstPath.FILE_NAME_WIFI;//"/sdcard/MyBackup/wpa_supplicant.conf";
        if (RootTools.remount(source, "rw")) {
            if (RootTools.remount(dest, "rw")) {
                RootTools.copyFile(source, dest, true, true);
                return true;    //Complete.
//              Toast.makeText(getBaseContext(), "Copied wifi database to sdcard/MyBackup", Toast.LENGTH_SHORT).show();
            }
        } else {
            return false;    //Fail
//              Toast.makeText(getBaseContext(), "File don't have permission RW", Toast.LENGTH_SHORT).show();
        }
        return false;    //Fail
    }

    //Copy mms sms database to Phone folder require ROOT
    public Boolean Restoremmssms() {
        String dest = ConstPath.PATH_TO_DATA + ConstPath.PATH_MMS_SMS;
//                String source = path_to_MyBackup + "mmssms.db";
        String source = ConstPath.STORE_BACKUP_DIRECTORY + ConstPath.FILE_NAME_SMS;//"/sdcard/MyBackup/mmssms.db";
        if (RootTools.remount(dest, "rw")) {
            if (RootTools.remount(source, "rw")) {
                RootTools.copyFile(source, dest, true, true);
                return true;   //Complete.
//                    Toast.makeText(getBaseContext(), "Copied mmssms.db to sdcard/MyBackup", Toast.LENGTH_SHORT).show();
            } else {
                return false;
            }
        } else {
            return false;    //Fail
//                    Toast.makeText(getBaseContext(), "File don't have permission RW", Toast.LENGTH_SHORT).show();
        }
    }

    //Copy Contacts database to Phone require ROOT
    public Boolean Restorecontacts() {
        String dest = ConstPath.PATH_TO_DATA + ConstPath.PATH_DATA_CONTACT;
        String source = ConstPath.STORE_BACKUP_DIRECTORY + ConstPath.FILE_CONTACT;//"/sdcard/MyBackup/contacts2.db";
        if (RootTools.remount(source, "rw")) {
            if (RootTools.remount(dest, "rw")) {
                RootTools.copyFile(source, dest, true, true);
                return true;
            }
        } else {
            return false;
        }
        return false;
    }

    //Read Calendar from XML.
    public ArrayList<CEvent> ReadCalEvent(File filename) {
        try {
            ArrayList<CEvent> cEvents = new ArrayList<CEvent>();
            DateFormat formatter = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzzz yyyy");

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(filename);

            Node root = doc.getFirstChild();
            NodeList list = root.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                if (node instanceof Element) {
                    Element element = (Element) node;
                    //Tag calendar_ID.
                    NodeList nodeList = element.getElementsByTagName("calendar_ID");
                    String calendar_ID = nodeList.item(0).getTextContent();
                    //Tag event_ID.
                    nodeList = element.getElementsByTagName("event_ID");
                    String event_ID = nodeList.item(0).getTextContent();
                    //Tag event_title.
                    nodeList = element.getElementsByTagName("event_title");
                    String event_title = nodeList.item(0).getTextContent();
                    //Tag event_describer.
                    nodeList = element.getElementsByTagName("event_describer");
                    String event_describer = nodeList.item(0).getTextContent();
                    //Tag event_timezone.
                    nodeList = element.getElementsByTagName("event_timezone");
                    String event_timezone = nodeList.item(0).getTextContent();
                    //Tag event_start.
                    nodeList = element.getElementsByTagName("event_start");
                    String event_start = nodeList.item(0).getTextContent();
                    Date date_event_start = formatter.parse(event_start);
                    //Tag event_end.
                    nodeList = element.getElementsByTagName("event_end");
                    String event_end = nodeList.item(0).getTextContent();
                    Date date_event_end = formatter.parse(event_end);
                    //Tag event_location.
                    nodeList = element.getElementsByTagName("event_location");
                    String event_location = nodeList.item(0).getTextContent();
                    //Tag display_name.
                    nodeList = element.getElementsByTagName("display_name");
                    String display_name = nodeList.item(0).getTextContent();
                    //Tag account_name.
                    nodeList = element.getElementsByTagName("account_name");
                    String account_name = nodeList.item(0).getTextContent();
                    //Tag owner_name.
                    nodeList = element.getElementsByTagName("owner_name");
                    String owner_name = nodeList.item(0).getTextContent();

                    //Add to ArrayList
                    CEvent cEvent = new CEvent(calendar_ID, event_ID, event_title, event_describer, event_timezone, date_event_start, date_event_end, event_location, display_name, account_name, owner_name);
                    cEvents.add(cEvent);
                }
            }
            return cEvents;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //Part in Restore Calendar Event NOT require ROOT
    public boolean RemoveAllEventCalendar() {
        ArrayList<Integer> CalIDs = new ArrayList<Integer>();
        try {
            Cursor cur = getContentResolver().query(CalendarContract.Events.CONTENT_URI, null, null, null, null);
            if (cur != null) {
                while (cur.moveToNext()) {
                    CalIDs.add(cur.getInt(cur.getColumnIndex(CalendarContract.Events._ID)));
                }
            }

            for (Integer calIDs : CalIDs) {
                ContentResolver cr = getContentResolver();
                ContentValues values = new ContentValues();
                Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, calIDs);
                int rows = getContentResolver().delete(deleteUri, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //Restore Calendar Event NOT require ROOT.
    public void RestoreCalendarEvent(ArrayList<CEvent> cEvents) {
        if (RemoveAllEventCalendar()) {
            ContentResolver cr = getContentResolver();
            ContentValues values = new ContentValues();
            for (CEvent cEvent : cEvents) {
                values.put(CalendarContract.Events.CALENDAR_ID, cEvent.CAL_ID);
                values.put(CalendarContract.Events.TITLE, cEvent.Event_TITLE);
                values.put(CalendarContract.Events.DESCRIPTION, cEvent.Event_DESC);
                values.put(CalendarContract.Events.EVENT_TIMEZONE, cEvent.Event_TimeZone);
                values.put(CalendarContract.Events.DTSTART, cEvent.Event_START.getTime());
                values.put(CalendarContract.Events.DTEND, cEvent.Event_END.getTime());
                values.put(CalendarContract.Events.EVENT_LOCATION, cEvent.Event_LOC);
                try {
                    Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Restore Calendar Event require ROOT
    public Boolean RestoreCalendardb() {
        String dest = ConstPath.PATH_TO_DATA + ConstPath.PATH_CALENDAR;
        String source = ConstPath.STORE_BACKUP_DIRECTORY + ConstPath.FILE_CALENDAR;//"/sdcard/MyBackup/calendar.db";
        if (RootTools.remount(source, "rw") && RootTools.remount(dest, "rw")) {
            RootTools.copyFile(source, dest, true, true);
            return true;    //Complete.
//          Toast.makeText(getBaseContext(), "Copied calendar.db to sdcard/MyBackup", Toast.LENGTH_SHORT).show();
        }
//        Toast.makeText(getBaseContext(), "File don't have permission RW", Toast.LENGTH_SHORT).show();
        return false;    //Fail
    }

    //Read Calendar from XML.
    public ArrayList<BMark> ReadBookmark(File filename) {
        try {
            ArrayList<BMark> bMarks = new ArrayList<BMark>();

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(filename);

            Node root = doc.getFirstChild();
            NodeList list = root.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                if (node instanceof Element) {
                    Element element = (Element) node;
                    //Tag bookmark_ID.
                    NodeList nodeList = element.getElementsByTagName("bookmark_ID");
                    String bookmark_ID = nodeList.item(0).getTextContent();
                    //Tag title.
                    nodeList = element.getElementsByTagName("title");
                    String title = nodeList.item(0).getTextContent();
                    //Tag url.
                    nodeList = element.getElementsByTagName("url");
                    String url = nodeList.item(0).getTextContent();

                    //Add to ArrayList
                    BMark bMark = new BMark(bookmark_ID, title, url);
                    bMarks.add(bMark);
                }
            }
            return bMarks;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //Part in Restore Calendar Event NOT require ROOT
    public boolean RemoveAllBookmark() {
        ArrayList<Integer> BmIDs = new ArrayList<Integer>();
        try {
            Cursor cur = getContentResolver().query(Browser.BOOKMARKS_URI, null, null, null, null);
            if (cur != null) {
                while (cur.moveToNext()) {
                    BmIDs.add(cur.getInt(cur.getColumnIndex(Browser.BookmarkColumns._ID)));
                }
            }

            for (Integer bmIDs : BmIDs) {
                ContentResolver cr = getContentResolver();
                ContentValues values = new ContentValues();
                Uri deleteUri = ContentUris.withAppendedId(Browser.BOOKMARKS_URI, bmIDs);
                int rows = getContentResolver().delete(deleteUri, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //Restore Bookmark NOT require ROOT.
    public void RestoreBookmark(ArrayList<BMark> bMarks) {
        if (RemoveAllBookmark()) {
            ContentResolver cr = getContentResolver();
            ContentValues values = new ContentValues();
            for (BMark bMark : bMarks) {
                values.put(Browser.BookmarkColumns._ID, bMark.id);
                values.put(Browser.BookmarkColumns.TITLE, bMark.title);
                values.put(Browser.BookmarkColumns.URL, bMark.url);
                try {
                    Uri uri = cr.insert(Browser.BOOKMARKS_URI, values);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Restore Bookmark require ROOT
    public Boolean RestoreBookmarkdb() {
        String dest = ConstPath.PATH_TO_DATA + ConstPath.PATH_DATA_BROWSER;
        String source = ConstPath.STORE_BACKUP_DIRECTORY + ConstPath.BROWSER_DIRECTORY;
        if (RootTools.remount(source, "rw") && RootTools.remount(dest, "rw")) {
            String command = "cp -r " + source + " " + dest;
            try {
                CommandCapture cmdCapture = new CommandCapture(0, command);
                RootTools.getShell(true).add(cmdCapture);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;    //Complete.
//            Toast.makeText(getBaseContext(), "Copied browser2.db to sdcard/MyBackup", Toast.LENGTH_SHORT).show();
        }
//        Toast.makeText(getBaseContext(), "File don't have permission RW", Toast.LENGTH_SHORT).show();
        return false;    //Fail
    }

    //Show Toast Error.
    public void showToast(String msg) {
        Toast error = Toast.makeText(Restore_Tab.this, msg, Toast.LENGTH_LONG);
        error.show();
    }

    ///Class download file
    private class DownloadBackupFile extends AsyncTask<Void, Long, Boolean> {

        private Context mContext;
        private final ProgressDialog mDialog;
        private DropboxAPI<?> mApi;
        private String mPath;
        private String fileType;
        private FileOutputStream mFos;
        private boolean mCanceled;
        private Long mFileLen;
        private String mErrorMsg;
        private String backupFileName; //ten file backup zip

        public DownloadBackupFile(Context mContext, DropboxAPI<?> api, String dropboxPath, String fileType) {
            this.mContext = mContext.getApplicationContext();
            this.mApi = api;
            this.mPath = dropboxPath;
            this.fileType = fileType;
            this.backupFileName = "";

            mDialog = new ProgressDialog(mContext);
            mDialog.setMessage("Please wait...");
            if (fileType == ConstPath.TYPE_XML) {
                mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            } else {
                mDialog.setButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mCanceled = true;
                        mErrorMsg = "Canceled";

                        if (mFos != null) {
                            try {
                                mFos.close();
                            } catch (IOException e) {

                            }
                        }
                    }
                });
            }
            mDialog.show();
            mDialog.setCancelable(false);
        }

        public void setBackupFileName(String backupFileName) {
            this.backupFileName = backupFileName;
        }

        //Lay tat cac cac file xml luu thong tin cac goi backup
        public Boolean GetAllBackupXMLFile() {
            try {
                //lay metadata cua 1 thu muc
                DropboxAPI.Entry dirent = mApi.metadata(mPath, 1000, null, true, null);
                if (!dirent.isDir || dirent.contents == null) {
                    //Khong phai la thu muc hoac ko co gi trong do
                    mErrorMsg = "File or Directory is empty";
                    return false;
                }
                //Lay danh sach cac thumb trong thu muc va get thumnail
                ArrayList<DropboxAPI.Entry> listFile = new ArrayList<DropboxAPI.Entry>();
                for (DropboxAPI.Entry ent : dirent.contents) {
                    if (ent.mimeType.contains(ConstPath.TYPE_XML)) {
                        //Them no vao danh sach
                        listFile.add(ent);
                    }
                }

                if (listFile.size() == 0) {
                    //Khong co thumbs trong thu muc nay
                    mErrorMsg = "Folder empty!";
                    return false;
                }
                for (DropboxAPI.Entry ent : listFile) {
                    //lay duong dan file
                    //Download tuan tu cac file
                    String cachePath = ConstPath.PATH_TO_ALL2CLOUDS + "/" + ent.fileName();
                    //mContext.getCacheDir().getAbsolutePath() + "/" + ent.fileName(); //+ File name;
                    try {
                        mFos = new FileOutputStream(cachePath);
                    } catch (FileNotFoundException e) {
                        mErrorMsg = "Can not create local file to save file!";
                        //return false;
                    }
                    //Download file
                    mApi.getFile(ent.path, ent.rev, mFos, null);
                }

                //lay duong dan
                //Log.i("All2Clouds", ent.path);
                //mFileLen = ent.bytes;
                return true;
            } catch (DropboxUnlinkedException e) {
                //The AuthSession wasn't properly authenticated or user unlinked.
            } catch (DropboxPartialFileException e) {
                // We canceled the operation
                mErrorMsg = "Download canceled";
            } catch (DropboxServerException e) {
                // Server-side exception.  These are examples of what could happen,
                // but we don't do anything special with them here.
                if (e.error == DropboxServerException._304_NOT_MODIFIED) {
                    // won't happen since we don't pass in revision with metadata
                } else if (e.error == DropboxServerException._401_UNAUTHORIZED) {
                    // Unauthorized, so we should unlink them.  You may want to
                    // automatically log the user out in this case.
                } else if (e.error == DropboxServerException._403_FORBIDDEN) {
                    // Not allowed to access this
                } else if (e.error == DropboxServerException._404_NOT_FOUND) {
                    // path not found (or if it was the thumbnail, can't be
                    // thumbnailed)
                } else if (e.error == DropboxServerException._406_NOT_ACCEPTABLE) {
                    // too many entries to return
                } else if (e.error == DropboxServerException._415_UNSUPPORTED_MEDIA) {
                    // can't be thumbnailed
                } else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
                    // user is over quota
                } else {
                    // Something else
                }
                // This gets the Dropbox error, translated into the user's language
                mErrorMsg = e.body.userError;
                if (mErrorMsg == null) {
                    mErrorMsg = e.body.error;
                }
            } catch (DropboxIOException e) {
                // Happens all the time, probably want to retry automatically.
                mErrorMsg = "Network error.  Try again.";
            } catch (DropboxParseException e) {
                //Unknown error
                mErrorMsg = "Unknown error. Try again.";
            } catch (DropboxException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            return false;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (fileType == ConstPath.TYPE_XML) {
                return GetAllBackupXMLFile();
            } else if (fileType == ConstPath.TYPE_ZIP) { //download file backup
                try {
                    if (backupFileName.isEmpty()) {
                        return false;
                    }
                    if (mCanceled) {
                        return false;
                    }
                    //lay metadata cua 1 thu muc
                    DropboxAPI.Entry dirent = mApi.metadata(mPath, 1000, null, true, null);
                    if (!dirent.isDir || dirent.contents == null) {
                        //Khong phai la thu muc hoac ko co gi trong do
                        mErrorMsg = "File or Directory is empty";
                        return false;
                    }
                    //Lay danh sach cac file trong thu muc BackupFiles
                    ArrayList<DropboxAPI.Entry> listFile = new ArrayList<DropboxAPI.Entry>();
                    for (DropboxAPI.Entry ent : dirent.contents) {
                        if (ent.fileName().compareTo(ConvertFileName2(backupFileName)+".zip") == 0) {
                            listFile.add(ent);
                        }
                    }
                    if (mCanceled) {
                        return false;
                    }
                    if (listFile.size() == 0) {
                        //Khong co file trong thu muc nay
                        mErrorMsg = "Folder empty!";
                        return false;
                    }
                    //Lay file dau tien trong danh sach
                    DropboxAPI.Entry ent = listFile.get(0);//lay file dau tien

                    //lay duong dan
                    Log.i("All2Clouds", ent.path);
                    mFileLen = ent.bytes;
                    //Download file
                    String cachePath = ConstPath.PATH_TO_ALL2CLOUDS + "/" + ent.fileName();
                    //mContext.getCacheDir().getAbsolutePath() + "/" + ent.fileName(); //+ File name;
                    try {
                        mFos = new FileOutputStream(cachePath);
                    } catch (FileNotFoundException e) {
                        mErrorMsg = "Can not create local file to save file!";
                        return false;
                    }
                    //Download file
                    mApi.getFile(ent.path, ent.rev, mFos, null);

                    if (mCanceled) {
                        return false;
                    }
                    return true;
                } catch (DropboxUnlinkedException e) {
                    //The AuthSession wasn't properly authenticated or user unlinked.
                } catch (DropboxPartialFileException e) {
                    // We canceled the operation
                    mErrorMsg = "Download canceled";
                } catch (DropboxServerException e) {
                    // Server-side exception.  These are examples of what could happen,
                    // but we don't do anything special with them here.
                    if (e.error == DropboxServerException._304_NOT_MODIFIED) {
                        // won't happen since we don't pass in revision with metadata
                    } else if (e.error == DropboxServerException._401_UNAUTHORIZED) {
                        // Unauthorized, so we should unlink them.  You may want to
                        // automatically log the user out in this case.
                    } else if (e.error == DropboxServerException._403_FORBIDDEN) {
                        // Not allowed to access this
                    } else if (e.error == DropboxServerException._404_NOT_FOUND) {
                        // path not found (or if it was the thumbnail, can't be
                        // thumbnailed)
                    } else if (e.error == DropboxServerException._406_NOT_ACCEPTABLE) {
                        // too many entries to return
                    } else if (e.error == DropboxServerException._415_UNSUPPORTED_MEDIA) {
                        // can't be thumbnailed
                    } else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
                        // user is over quota
                    } else {
                        // Something else
                    }
                    // This gets the Dropbox error, translated into the user's language
                    mErrorMsg = e.body.userError;
                    if (mErrorMsg == null) {
                        mErrorMsg = e.body.error;
                    }
                } catch (DropboxIOException e) {
                    // Happens all the time, probably want to retry automatically.
                    mErrorMsg = "Network error.  Try again.";
                } catch (DropboxParseException e) {
                    //Unknown error
                    mErrorMsg = "Unknown error. Try again.";
                } catch (DropboxException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
            return false;
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            int percent = (int) (100.0 * (double) values[0] / mFileLen + 0.5);
            mDialog.setProgress(percent);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mDialog.dismiss();
            if (result) {
//                showToast("Completed!");
                LoadBackupPackage();
            } else {
                showToast("Not found backup file!");
            }
        }
    }
    //Class to restore
    public class Restore_AsyncTask extends AsyncTask<CheckBox, Integer, Boolean> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(Restore_Tab.this);
            pd.setTitle("Restoring...");
            pd.setMessage("Running. Please Wait...");
            pd.show();
            pd.setCancelable(false);
        }

        @Override
        protected Boolean doInBackground(CheckBox... cb) {
            //Check and Backup
            //0,1,2,3,4,5 --> cb_app, cb_mmssms, cb_contact, cb_wifidb, cb_calevent, cb_bookmark
            if ((cb[0]!=null && cb[0].isChecked()) || (cb[1]!=null && cb[1].isChecked()) || (cb[2]!=null && cb[2].isChecked()) || (cb[3]!=null && cb[3].isChecked()) || (cb[4]!=null && cb[4].isChecked()) || (cb[5]!=null && cb[5].isChecked())) {
                //Backup
//                Delete All File in MyBackup.
                tools.Deletefiles(ConstPath.PATH_TO_MY_BACKUP);

                //Unzip Package after download.
                String[] package_name = ConvertFileName2(item_select_name).split("\\.");
                File file = new File(ConstPath.STORE_RESTORE_DIRECTORY + package_name[0] + ".zip");
                File out = new File("/sdcard/");
                Unzipping.unzip(file, out);

                //Check Checkbox and Do restore function.
                if (cb[0] != null && cb[0].isChecked()) {
                    //Require ROOT.
                    RestoreApp();
                }
                if (cb[1] != null && cb[1].isChecked()) {
                    Restorewifidb();
                }
                if (cb[2] != null && cb[2].isChecked()) {
                    Restoremmssms();
                }
                if (cb[3] != null && cb[3].isChecked()) {
                    Restorecontacts();
                }
                if (cb[4] != null && cb[4].isChecked()) {
                    if (flagRoot) {
                        //Retore reqire ROOT
                        RestoreCalendardb();
                    } else {
                        //Restore NOT reqire ROOT.
                        File filename = new File(ConstPath.PATH_TO_MY_BACKUP, "CalEvent.xml");
                        RestoreCalendarEvent(ReadCalEvent(filename));
                    }
                }
                if (cb[5] != null && cb[5].isChecked()) {
                    if (flagRoot) {
                        //Restore require ROOT
                        RestoreBookmarkdb();
                    } else {
                        //Restore NOT reqire ROOT. nhung chua restore duoc
                        File filename = new File(ConstPath.PATH_TO_MY_BACKUP, "Bookmark.xml");
                        RestoreBookmark(ReadBookmark(filename));
                    }
                }

                //Delete All File in MyBackup.
                tools.Deletefiles(ConstPath.PATH_TO_MY_BACKUP);
                tools.Deletefiles(ConstPath.PATH_TO_ALL2CLOUDS);
            } else {
                return false;
            }
                return true;
            }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result){
                Toast.makeText(getBaseContext(), "Restore Completed.", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getBaseContext(), "Can not Restore.", Toast.LENGTH_SHORT).show();
            }
            pd.dismiss();
        }
    }

}
