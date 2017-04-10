package com.example.ypham.all2clouds;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Browser;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Created by Y Pham on 11/27/2014.
 */
public class Backup_Tab extends Activity{

    Button btBackup, btCancel;
    CheckBox cbApp, cbMmsSms, cbContact, cbWifiDb, cbCalevent, cbBookmark;
    //private String path_to_data = Environment.getDataDirectory().getAbsolutePath();
    ProgressDialog progressDialog;
    Boolean flagRoot = false;
    Tools tools;
    //De dem so file da upload len dropbox. 2 la xong
    int count;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_tab);
        tools = new Tools();

        btBackup = (Button) this.findViewById(R.id.bt_backup);
        btCancel = (Button) this.findViewById(R.id.bt_bkcancel);

        cbApp = (CheckBox) this.findViewById(R.id.cb_app);
        cbMmsSms = (CheckBox) this.findViewById(R.id.cb_mmssms);
        cbContact = (CheckBox) this.findViewById(R.id.cb_contact);
        cbWifiDb = (CheckBox) this.findViewById(R.id.cb_wifidb);
        cbCalevent = (CheckBox) this.findViewById(R.id.cb_calevent);
        cbBookmark = (CheckBox) this.findViewById(R.id.cb_bookmark);

        btBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doBackup();
            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doCancel();
            }
        });
        //Yeu cau root de su dung app - thuc hien enable hoac disable
        if (tools.CheckRoot()){
            flagRoot = true;
            cbApp.setEnabled(true);
            cbContact.setEnabled(true);
            cbMmsSms.setEnabled(true);
            cbWifiDb.setEnabled(true);
        }else{
            flagRoot = false;
            cbApp.setEnabled(false);
            cbContact.setEnabled(false);
            cbMmsSms.setEnabled(false);
            cbWifiDb.setEnabled(false);
        }
    }

    private void doBackup() {
        //Run Backup on AsyncTask Background.
        new Backup_AsyncTask().execute(cbApp, cbMmsSms, cbContact, cbWifiDb, cbCalevent, cbBookmark);
    }
    //Event Cancel Button
    private void doCancel(){
        cbApp.setChecked(false);
        cbMmsSms.setChecked(false);
        cbContact.setChecked(false);
        cbWifiDb.setChecked(false);
        cbCalevent.setChecked(false);
        cbBookmark.setChecked(false);
    }
    //Zip Function
    private boolean zipFolder(String srcFolder, String destZipFile) {
        try {
            ZipOutputStream zos = null;
            FileOutputStream fileWriter = null;
            fileWriter = new FileOutputStream(destZipFile);
            zos = new ZipOutputStream(fileWriter);
            addFolderToZip("", srcFolder, zos);
            zos.flush();
            zos.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void addFolderToZip(String path, String srcFolder, ZipOutputStream zos) throws Exception {
        File folder = new File(srcFolder);

        //check the empty folder
        if (folder.list().length == 0) {
            System.out.println(folder.getName());
            addFileToZip(path, srcFolder, zos, true);
        } else {
            //list the files in the folder
            for (String fileName : folder.list()) {
                if (path.equals("")) {
                    addFileToZip(folder.getName(), srcFolder + "/" + fileName, zos, false);
                } else {
                    addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zos, false);
                }
            }
        }
    }

    private void addFileToZip(String path, String srcFile, ZipOutputStream zos, boolean flag) throws Exception {
        File folder = new File(srcFile);
        // if the folder is empty add empty folder to the Zip file
        if (flag == true) {
            zos.putNextEntry(new ZipEntry(path + "/" + folder.getName() + "/"));
        } else {
            //If a folder
            if (folder.isDirectory()) {
                addFolderToZip(path, srcFile, zos);
            } else {
                //write the file to the output
                byte[] buf = new byte[1024];
                int len;
                FileInputStream in = new FileInputStream(srcFile);
                zos.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
                while ((len = in.read(buf)) > 0) {
                    //Write the Result
                    zos.write(buf, 0, len);
                }
            }
        }
    }

    //Write XML items Backup
    protected void WriteXMLItemsBackup(File filename) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(filename);
            Node root = doc.getFirstChild();
            Element items = doc.createElement("items");
            root.appendChild(items);

            Element app = doc.createElement("app");
            app.appendChild(doc.createTextNode(cbApp.isChecked() ? "true" : "false"));
            items.appendChild(app);

            Element wifi = doc.createElement("wifi");
            wifi.appendChild(doc.createTextNode(cbWifiDb.isChecked() ? "true" : "false"));
            items.appendChild(wifi);

            Element mmssms = doc.createElement("mmssms");
            mmssms.appendChild(doc.createTextNode(cbMmsSms.isChecked() ? "true" : "false"));
            items.appendChild(mmssms);

            Element contact = doc.createElement("contact");
            contact.appendChild(doc.createTextNode(cbContact.isChecked() ? "true" : "false"));
            items.appendChild(contact);

            Element cal = doc.createElement("calendar");
            cal.appendChild(doc.createTextNode(cbCalevent.isChecked() ? "true" : "false"));
            items.appendChild(cal);

            Element bookmark = doc.createElement("bookmark");
            bookmark.appendChild(doc.createTextNode(cbBookmark.isChecked() ? "true" : "false"));
            items.appendChild(bookmark);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource src = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filename.getAbsolutePath()));
            transformer.transform(src, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Write App Info XML with DOM
    protected void WriteAppInfoXML(ArrayList<PInfo> pInfos, File filename) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(filename);
            Node root = doc.getFirstChild();
            for (PInfo pInfo : pInfos) {
                Element application = doc.createElement("application");
                root.appendChild(application);

                Element app_name = doc.createElement("app_name");
                app_name.appendChild(doc.createTextNode(pInfo.appname == null ? "null" : pInfo.appname.toString()));
                application.appendChild(app_name);

                Element package_name = doc.createElement("package_name");
                package_name.appendChild(doc.createTextNode(pInfo.pname == null ? "null" : pInfo.pname.toString()));
                application.appendChild(package_name);

                Element version_name = doc.createElement("version_name");
                version_name.appendChild(doc.createTextNode(pInfo.versionName == null ? "null" : pInfo.versionName.toString()));
                application.appendChild(version_name);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource src = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filename.getAbsolutePath()));
            transformer.transform(src, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Write Device Info XML with DOM
    private void WriteDeviceInfoXML(Dinfo dinfo, File filename) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(filename);
            Node root = doc.getFirstChild();

            Element device_info = doc.createElement("device_info");
            root.appendChild(device_info);

            Element imei_no = doc.createElement("IMEI_no");
            imei_no.appendChild(doc.createTextNode(dinfo.IMEI == null ? "null" : dinfo.IMEI.toString()));
            device_info.appendChild(imei_no);

            Element imsi_no = doc.createElement("IMSI_no");
            imsi_no.appendChild(doc.createTextNode(dinfo.IMSI == null ? "null" : dinfo.IMSI.toString()));
            device_info.appendChild(imsi_no);

            Element android_id = doc.createElement("android_ID");
            android_id.appendChild(doc.createTextNode(dinfo.androidID == null ? "null" : dinfo.androidID.toString()));
            device_info.appendChild(android_id);

            Element uuid = doc.createElement("UUID");
            uuid.appendChild(doc.createTextNode(dinfo.UUIDdevice == "" ? "null" : dinfo.UUIDdevice.toString()));
            device_info.appendChild(uuid);

            Element serial = doc.createElement("serial");
            serial.appendChild(doc.createTextNode(dinfo.tmSerial == null ? "null" : dinfo.tmSerial.toString()));
            device_info.appendChild(serial);

            Element device_model_name = doc.createElement("device_model_name");
            device_model_name.appendChild(doc.createTextNode(dinfo.deviceModelName == null ? "null" : dinfo.deviceModelName.toString()));
            device_info.appendChild(device_model_name);

            Element device_user = doc.createElement("device_user");
            device_user.appendChild(doc.createTextNode(dinfo.deviceUser == null ? "null" : dinfo.deviceUser.toString()));
            device_info.appendChild(device_user);

            Element device_product = doc.createElement("device_product");
            device_product.appendChild(doc.createTextNode(dinfo.deviceProduct == null ? "null" : dinfo.deviceProduct.toString()));
            device_info.appendChild(device_product);

            Element device_hardware = doc.createElement("device_hardware");
            device_hardware.appendChild(doc.createTextNode(dinfo.deviceHardWare == null ? "null" : dinfo.deviceHardWare.toString()));
            device_info.appendChild(device_hardware);

            Element device_brand = doc.createElement("device_brand");
            device_brand.appendChild(doc.createTextNode(dinfo.deviceBrand == null ? "null" : dinfo.deviceBrand.toString()));
            device_info.appendChild(device_brand);

            Element version = doc.createElement("version");
            version.appendChild(doc.createTextNode(dinfo.myVersion == null ? "null" : dinfo.myVersion.toString()));
            device_info.appendChild(version);

            Element sdk_version = doc.createElement("SDK_version");
            sdk_version.appendChild(doc.createTextNode(Integer.toString(dinfo.sdkVersion)));
            device_info.appendChild(sdk_version);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource src = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filename.getAbsolutePath()));
            transformer.transform(src, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Write Calander Event XML with DOM
    private void WriteCalEventXML(ArrayList<CEvent> CEvents, File filename) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(filename);
            Node root = doc.getFirstChild();
            for (CEvent cEvent : CEvents) {
                Element calendar = doc.createElement("calander");
                root.appendChild(calendar);

                Element calendar_id = doc.createElement("calendar_ID");
                calendar_id.appendChild(doc.createTextNode(cEvent.CAL_ID == null ? "null" : cEvent.CAL_ID.toString()));
                calendar.appendChild(calendar_id);

                Element event_id = doc.createElement("event_ID");
                event_id.appendChild(doc.createTextNode(cEvent.Event_ID == null ? "null" : cEvent.Event_ID.toString()));
                calendar.appendChild(event_id);

                Element event_title = doc.createElement("event_title");
                event_title.appendChild(doc.createTextNode(cEvent.Event_TITLE == null ? "null" : cEvent.Event_TITLE.toString()));
                calendar.appendChild(event_title);

                Element event_describer = doc.createElement("event_describer");
                event_describer.appendChild(doc.createTextNode(cEvent.Event_DESC == null ? "null" : cEvent.Event_DESC.toString()));
                calendar.appendChild(event_describer);

                Element event_timezone = doc.createElement("event_timezone");
                event_timezone.appendChild(doc.createTextNode(cEvent.Event_TimeZone == null ? "null" : cEvent.Event_TimeZone.toString()));
                calendar.appendChild(event_timezone);

                Element event_start = doc.createElement("event_start");
                event_start.appendChild(doc.createTextNode(cEvent.Event_START == null ? "null" : cEvent.Event_START.toString()));
                calendar.appendChild(event_start);

                Element event_end = doc.createElement("event_end");
                event_end.appendChild(doc.createTextNode(cEvent.Event_END == null ? "null" : cEvent.Event_END.toString()));
                calendar.appendChild(event_end);

                Element event_location = doc.createElement("event_location");
                event_location.appendChild(doc.createTextNode(cEvent.Event_LOC == null ? "null" : cEvent.Event_LOC.toString()));
                calendar.appendChild(event_location);

                Element display_name = doc.createElement("display_name");
                display_name.appendChild(doc.createTextNode(cEvent.displayName == null ? "null" : cEvent.displayName.toString()));
                calendar.appendChild(display_name);

                Element account_name = doc.createElement("account_name");
                account_name.appendChild(doc.createTextNode(cEvent.accountName == null ? "null" : cEvent.accountName.toString()));
                calendar.appendChild(account_name);

                Element owner_name = doc.createElement("owner_name");
                owner_name.appendChild(doc.createTextNode(cEvent.ownerName == null ? "null" : cEvent.ownerName.toString()));
                calendar.appendChild(owner_name);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource src = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filename.getAbsolutePath()));
            transformer.transform(src, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Write Bookmark XML with DOM
    private void WriteBMarkXML(ArrayList<BMark> bmarks, File filename) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(filename);
            Node root = doc.getFirstChild();
            for (BMark bMark : bmarks) {
                Element bookmark = doc.createElement("bookmark");
                root.appendChild(bookmark);

                Element bookmark_ID = doc.createElement("bookmark_ID");
                bookmark_ID.appendChild(doc.createTextNode(bMark.id == null ? "null" : bMark.id.toString()));
                bookmark.appendChild(bookmark_ID);

                Element title = doc.createElement("title");
                title.appendChild(doc.createTextNode(bMark.title == null ? "null" : bMark.title.toString()));
                bookmark.appendChild(title);

                Element url = doc.createElement("url");
                url.appendChild(doc.createTextNode(bMark.url == null ? "null" : bMark.url.toString()));
                bookmark.appendChild(url);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource src = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filename.getAbsolutePath()));
            transformer.transform(src, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Write Device Info to SDCard
    public void WriteDeviceInfo(Dinfo dinfo) {
        if (tools.isExternalStorageWritable() && tools.isExternalStorageReadable()) {
            try {
                if (tools.createFolder(ConstPath.PATH_TO_MY_BACKUP)) {
                    WriteDeviceInfoXML(dinfo, tools.CreateXML(ConstPath.PATH_TO_MY_BACKUP, "DeviceInfo.xml"));
                    Toast.makeText(getBaseContext(), "Done writing SD DeviceInfo.xml", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getBaseContext(), "Fail create new MyBackup Folder", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(getBaseContext(), "Can Not Read or Write on SD.", Toast.LENGTH_SHORT).show();
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

    //Write App Info to SDCard
    public Boolean WriteAppInfoFileOnSD(ArrayList<PInfo> apps) {
        if (tools.isExternalStorageWritable() && tools.isExternalStorageReadable()) {
            try {
                if (tools.createFolder(ConstPath.PATH_TO_MY_BACKUP)) {
                    WriteAppInfoXML(apps, tools.CreateXML(ConstPath.PATH_TO_MY_BACKUP, "Appinfo.xml"));
                    return true;    //Create Complete.
                } else {
                    return false;   //Create Fail.
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return false;    //Fail
//            Toast.makeText(getBaseContext(), "Not Read or Write on SD.", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    protected ArrayList<PInfo> getInstalledApps(boolean getSysPackages) {
        String appname, pname, versionName;
        int versionCode;
        Drawable icon;
        ArrayList<PInfo> res = new ArrayList<PInfo>();
        List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packs.size(); i++) {
            PackageInfo p = packs.get(i);
            //Check App System
            if ((!getSysPackages) && (p.applicationInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) {
                continue;
            }
            appname = p.applicationInfo.loadLabel(getPackageManager()).toString();
            pname = p.packageName;
            versionName = p.versionName;
            versionCode = p.versionCode;
            icon = p.applicationInfo.loadIcon(getPackageManager());
            PInfo newInfo = new PInfo(appname, pname, versionName, versionCode, icon);
            res.add(newInfo);
        }
        return res;
    }

    //Copy All apk from /data/app require ROOT #Viet lai sau de ko can ROOT
    public Boolean CopyAPK() throws InterruptedException {
        final String source = ConstPath.PATH_TO_DATA + "//app//*.apk";
//        String dest = path_to_MyBackup;
        final String dest = ConstPath.STORE_BACKUP_DIRECTORY + ConstPath.APP_DIRECTORY;
        if (tools.createFolder(ConstPath.PATH_TO_MY_BACKUP + ConstPath.APP_DIRECTORY) && tools.createFolder(ConstPath.PATH_TO_MY_BACKUP)) {
            if (RootTools.remount(source, "RW")) {
                Thread thread1 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        RootTools.copyFile(source, dest, true, true);
                    }
                });
                thread1.start();
                thread1.join();         //Wait thread1 finish.
                return true;    //Complete.
//                Toast.makeText(this, "Copied All APK to MyBackup folder", Toast.LENGTH_SHORT).show();
            } else {
                return false;   //Fail
//                Toast.makeText(this, "File don't have permission RW", Toast.LENGTH_SHORT).show();
            }
        }
        return false;    //Fail
    }

    //Copy App database require ROOT
    public Boolean Copyappdb(final List<String> ListPackageName) throws InterruptedException {
        if (tools.isExternalStorageWritable()) {
            if (tools.createFolder(ConstPath.PATH_TO_MY_BACKUP) && tools.createFolder(ConstPath.PATH_TO_MY_BACKUP + "/App/")) {
                Thread thread2 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < ListPackageName.size(); i++) {
//                            String dest = path_to_MyBackup;
                            File dest = new File(ConstPath.STORE_BACKUP_DIRECTORY + ConstPath.APP_DIRECTORY + ListPackageName.get(i).toString() + "/data/");
                            if (tools.createFolder(dest.getAbsolutePath())) {
                                //Copy Package database from Internal Storage
                                String source = new String(ConstPath.PATH_TO_DATA + "/data/" + ListPackageName.get(i).toString());
                                if (RootTools.remount(source, "rw")) {
                                    String command = "cp -r " + source + " " + dest;

                                    try {
                                        CommandCapture cmdCapture = new CommandCapture(0, command);
                                        RootTools.getShell(true).add(cmdCapture);
                                    } catch (IOException ioe) {
                                        ioe.printStackTrace();
                                    } catch (TimeoutException toe) {
                                        toe.printStackTrace();
                                    } catch (RootDeniedException rde) {
                                        rde.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                });
                thread2.start();
                thread2.join();         //wait thread2 finish.
                return true;    //Complete.
//                Toast.makeText(getBaseContext(), "Copied App database to sdcard/MyBackup", Toast.LENGTH_SHORT).show();
            }
        }
        return false;   //Fail.
    }

    //Get List Package Name for function Copyappdb(...)
    private List<String> GetListPackageName() {
        List<String> PackageNameList = new ArrayList<String>();
        List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packs.size(); i++) {
            PackageInfo p = packs.get(i);
            //Check App system
            if ((p.applicationInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) {
                continue;
            }
            PackageNameList.add(p.packageName);
        }
        return PackageNameList;
    }

    //Copy mms sms database to MyBackup folder require ROOT
    public Boolean Copymmssms() {
        if (tools.isExternalStorageWritable()) {
            if (tools.createFolder(ConstPath.PATH_TO_MY_BACKUP)) {
                String source = ConstPath.PATH_TO_DATA + ConstPath.PATH_MMS_SMS + ConstPath.FILE_NAME_SMS;
//                String dest = path_to_MyBackup;
                //String dest = "/sdcard/MyBackup/";
                if (RootTools.remount(source, "rw")) {
                    RootTools.copyFile(source, ConstPath.STORE_BACKUP_DIRECTORY, true, true);
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;    //Fail
//                Toast.makeText(this, "Fail create new MyBackup Folder", Toast.LENGTH_SHORT).show();
            }
        }
        return false;    //Fail
    }

    //Copy Contacts database require ROOT
    public Boolean Copycontacts() {
        if (tools.isExternalStorageWritable()) {
            if (tools.createFolder(ConstPath.PATH_TO_MY_BACKUP)) {
                String source = ConstPath.PATH_TO_DATA + ConstPath.PATH_DATA_CONTACT + ConstPath.FILE_CONTACT;
                String dest = ConstPath.STORE_BACKUP_DIRECTORY + ConstPath.FILE_CONTACT;
                if (RootTools.remount(source, "rw")) {
                    RootTools.copyFile(source, dest, true, true);
                    return true;    //Complete.
//                    Toast.makeText(getBaseContext(), "Copied contacts2.db to sdcard/MyBackup", Toast.LENGTH_SHORT).show();
                } else {
                    return false;    //Fail
//                    Toast.makeText(getBaseContext(), "File don't have permission RW", Toast.LENGTH_SHORT).show();
                }
            } else {
                return false;    //Fail
//                Toast.makeText(this, "Fail create new MyBackup Folder", Toast.LENGTH_SHORT).show();
            }
        }
        return false;    //Fail
    }

    //Copy wifi Database to MyBackups folder require ROOT
    public Boolean Copywifidb() {
        if (tools.isExternalStorageWritable()) {
            if (tools.createFolder(ConstPath.PATH_TO_MY_BACKUP)) {
                String source = ConstPath.PATH_TO_DATA + ConstPath.PATH_WIFI + ConstPath.FILE_NAME_WIFI;
//                String dest = path_to_MyBackup;
                //String dest = "/sdcard/MyBackup/";
                if (RootTools.remount(source, "rw")) {
                    RootTools.copyFile(source, ConstPath.STORE_BACKUP_DIRECTORY, true, true);
                    return true;    //Complete.
//                    Toast.makeText(getBaseContext(), "Copied wifi database to sdcard/MyBackup", Toast.LENGTH_SHORT).show();
                } else {
                    return false;    //Fail
//                    Toast.makeText(getBaseContext(), "File don't have permission RW", Toast.LENGTH_SHORT).show();
                }
            } else {
                return false;    //Fail
//                Toast.makeText(this, "Fail", Toast.LENGTH_SHORT).show();
            }
        }
        return false;    //Fail
    }

    //Copy Calendar Event require ROOT
    public Boolean CopyCalendardb() {
        String source = ConstPath.PATH_TO_DATA + ConstPath.PATH_CALENDAR + ConstPath.FILE_CALENDAR;
        String dest = ConstPath.STORE_BACKUP_DIRECTORY;
        if (RootTools.remount(source, "rw") && RootTools.remount(dest, "rw")) {
            RootTools.copyFile(source, dest, true, true);
            return true;    //Complete.
//          Toast.makeText(getBaseContext(), "Copied calendar.db to sdcard/MyBackup", Toast.LENGTH_SHORT).show();
        }
//        Toast.makeText(getBaseContext(), "File don't have permission RW", Toast.LENGTH_SHORT).show();
        return false;    //Fail
    }
    //Get Calendar Event not require ROOT
    //More info in site: http://developer.android.com/guide/topics/providers/calendar-provider.html
    public Boolean WriteCalEventtoSD(ArrayList<CEvent> CalEvents) {
        if (tools.isExternalStorageWritable() && tools.isExternalStorageReadable()) {
            try {
                if (tools.createFolder(ConstPath.PATH_TO_MY_BACKUP)) {
                    WriteCalEventXML(CalEvents, tools.CreateXML(ConstPath.PATH_TO_MY_BACKUP, "CalEvent.xml"));
                    return true;
//                    Toast.makeText(getBaseContext(), "Done writing SD 'CalEvent.xml'", Toast.LENGTH_SHORT).show();
                } else {
//                    Toast.makeText(getBaseContext(), "Fail create new MyBackup Folder", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;    //Fail
            }
        } else {
            return false;    //Fail
//            Toast.makeText(getBaseContext(), "Not Read or Write on SD.", Toast.LENGTH_SHORT).show();
        }
        return false;    //Fail
    }

    //Get Calendar Event NOT require ROOT
    //More info in site: http://developer.android.com/guide/topics/providers/calendar-provider.html
    public ArrayList<CEvent> GetCalendarEvent() {
        Cursor cur = null;
        ContentResolver cr = getContentResolver();
        ArrayList<CEvent> calendars = new ArrayList<CEvent>();
        // Projection array. Creating indices for this array instead of doing
        final String[] EVENT_PROJECTION = new String[]{
                CalendarContract.Events.CALENDAR_ID,
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DESCRIPTION,
                CalendarContract.Events.EVENT_TIMEZONE,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND,
                CalendarContract.Events.EVENT_LOCATION,
                CalendarContract.Events.ACCOUNT_NAME,
                CalendarContract.Events.CALENDAR_DISPLAY_NAME,
                CalendarContract.Events.OWNER_ACCOUNT
        };
        Uri uri = CalendarContract.Events.CONTENT_URI;
        // Run query
        cur = cr.query(uri, EVENT_PROJECTION, null, null, null);

        // Use the cursor to step through the returned records
        if (cur != null) {
            while (cur.moveToNext()) {
                String CAL_ID = null;
                String Event_ID = null;
                String Event_TITLE = null;
                String Event_DESC = null;
                String Event_TimeZone = null;
                Date Event_START = null;
                Date Event_END = null;
                String Event_LOC = null;
                String displayName = null;
                String accountName = null;
                String ownerName = null;
                // Get the field values
                CAL_ID = cur.getString(cur.getColumnIndex(CalendarContract.Events.CALENDAR_ID));
                Event_ID = cur.getString(cur.getColumnIndex(CalendarContract.Events._ID));
                Event_TITLE = cur.getString(cur.getColumnIndex(CalendarContract.Events.TITLE));
                Event_DESC = cur.getString(cur.getColumnIndex(CalendarContract.Events.DESCRIPTION));
                Event_TimeZone = cur.getString(cur.getColumnIndex(CalendarContract.Events.EVENT_TIMEZONE));
                Event_START = new Date(cur.getLong(cur.getColumnIndex(CalendarContract.Events.DTSTART)));
                Event_END = new Date(cur.getLong(cur.getColumnIndex(CalendarContract.Events.DTEND)));
                Event_LOC = cur.getString(cur.getColumnIndex(CalendarContract.Events.EVENT_LOCATION));
                displayName = cur.getString(cur.getColumnIndex(CalendarContract.Events.CALENDAR_DISPLAY_NAME));
                accountName = cur.getString(cur.getColumnIndex(CalendarContract.Events.ACCOUNT_NAME));
                ownerName = cur.getString(cur.getColumnIndex(CalendarContract.Events.OWNER_ACCOUNT));

                //Do something
                CEvent _calendars = new CEvent(CAL_ID, Event_ID, Event_TITLE, Event_DESC, Event_TimeZone, Event_START, Event_END, Event_LOC, displayName, accountName, ownerName);
                calendars.add(_calendars);
            }
        }
        return calendars;
    }

    //Copy Bookmark database require ROOT.
    public Boolean CopyBookmarkdb() {
        String source = ConstPath.PATH_TO_DATA + "/data/com.android.browser/databases/*";
        String dest = "/sdcard/MyBackup/Browser/";
        if (tools.createFolder(dest) && RootTools.remount(source, "rw") && RootTools.remount(dest, "rw")) {
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

    public Boolean WriteBookmarktoSD(ArrayList<BMark> bmarks) {
        if (tools.isExternalStorageWritable() && tools.isExternalStorageReadable()) {
            try {
                if (tools.createFolder(ConstPath.PATH_TO_MY_BACKUP)) {
                    WriteBMarkXML(bmarks, tools.CreateXML(ConstPath.PATH_TO_MY_BACKUP, "Bookmark.xml"));
                    return true;        //Complete.
//                    Toast.makeText(getBaseContext(), "Done writing SD 'Bookmark.xml'", Toast.LENGTH_SHORT).show();
                } else {
                    return false;    //Fail
//                    Toast.makeText(getBaseContext(), "Fail create new MyBackup Folder", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;    //Fail
            }
        } else {
            return false;    //Fail
//            Toast.makeText(getBaseContext(), "Can Not Read or Write on SD.", Toast.LENGTH_SHORT).show();
        }
    }

    //Get Bookmark from browser.
    public ArrayList<BMark> GetBookmarks() {
        ArrayList<BMark> bookmarks = new ArrayList<BMark>();
        final String[] bookmask_proj = new String[]{
                Browser.BookmarkColumns._ID,
                Browser.BookmarkColumns.TITLE,
                Browser.BookmarkColumns.URL,
        };
        Cursor cur = null;
        Uri uri = Browser.BOOKMARKS_URI;
        String sel = Browser.BookmarkColumns.BOOKMARK + "=1"; // 0 = History, 1 = Bookmask
        cur = managedQuery(uri, bookmask_proj, sel, null, null);
        startManagingCursor(cur);
        if (cur != null) {
            while (cur.moveToNext()) {
                String id = null;
                String title = null;
                String url = null;
                id = cur.getString(cur.getColumnIndex(Browser.BookmarkColumns._ID));
                title = cur.getString(cur.getColumnIndex(Browser.BookmarkColumns.TITLE));
                url = cur.getString(cur.getColumnIndex(Browser.BookmarkColumns.URL));
                BMark bmark = new BMark(id, title, url);
                bookmarks.add(bmark);
            }
        }
        return bookmarks;
    }

    public class Backup_AsyncTask extends AsyncTask<CheckBox, Integer, Boolean> {
        ProgressDialog pd;

        public void callBackXYZ(String message) {
            pd.dismiss();
            tools.Deletefiles(ConstPath.PATH_TO_ALL2CLOUDS);
            Toast.makeText(Backup_Tab.this, message, Toast.LENGTH_SHORT).show();
        }

        public synchronized void notifyFinished(UploadFile upload0) {
            count++;
        }

        public boolean isFinished() {

            if(count == 2){
                count = 0;
                return true;
            }
            return false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(Backup_Tab.this);
            pd.setTitle("Backup");
            pd.setMessage("Running Backup. Please Wait...");
            pd.show();
            //Khong cho nguoi dung tuong tac voi view khi dang thuc hien backup
            pd.setCancelable(false);
        }

        @Override
        protected Boolean doInBackground(CheckBox... cb) {

            //Get current date time.
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            Date d = new Date();
            String filename = sdf.format(d);

            //Delete all file in MyBackup Folder.
            tools.Deletefiles(ConstPath.PATH_TO_MY_BACKUP);

            //Check and Backup
            //0,1,2,3,4,5 --> cbApp, cbMmsSms, cbContact, cbWifiDb, cbCalevent, cbBookmark
            if (cb[0].isChecked() || cb[1].isChecked() || cb[2].isChecked() || cb[3].isChecked() || cb[4].isChecked() || cb[5].isChecked()) {
                //Backup
                if (cb[0].isChecked()) {
                    WriteAppInfoFileOnSD(getInstalledApps(false));
                    try {
                        CopyAPK();
                        Copyappdb(GetListPackageName());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (cb[1].isChecked()) Copymmssms();
                if (cb[2].isChecked()) Copycontacts();
                if (cb[3].isChecked()) Copywifidb();
                if (cb[4].isChecked()){
                    if (flagRoot) {
                        //Backup Calendar require ROOT.
                        CopyCalendardb();
                    }else {
                        //Backup Calendar NOT require ROOT.
                        WriteCalEventtoSD(GetCalendarEvent());
                    }
                }
                if (cb[5].isChecked()){
                    if (flagRoot) {
                        //Backup Bookmark require ROOT.
                        CopyBookmarkdb();
                    }else {
                        //Backup Bookmark NOT require ROOT.
                        WriteBookmarktoSD(GetBookmarks());
                    }
                }

                String dest = ConstPath.STORE_RESTORE_DIRECTORY + filename + ".zip";
                if (tools.createFolder(ConstPath.STORE_RESTORE_DIRECTORY)) {
                    //Create XML items select backup
                    WriteXMLItemsBackup(tools.CreateXML(ConstPath.PATH_TO_ALL2CLOUDS, filename + ".xml"));
                    //Zip
                    if (zipFolder(ConstPath.PATH_TO_MY_BACKUP, dest)) {
                        //Delete MyBackup Folder
                        tools.Deletefiles(ConstPath.PATH_TO_MY_BACKUP);
                        return true;
//                        Toast.makeText(getBaseContext(), "Created Zip in All2Clouds Folder!", Toast.LENGTH_SHORT).show();
                    } else {
                        return false;
//                        Toast.makeText(getBaseContext(), "Create Zip fail!", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                return false;
//                Toast.makeText(getBaseContext(), "You must choose option!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getBaseContext(), "Create Backup Complete.", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getBaseContext(), "Create Backup Fail.", Toast.LENGTH_SHORT).show();
            }
            pd.setMessage("Uploading. Please wait...");

            //Thuc hien upload file da backup len may\
            File folder = new File(ConstPath.PATH_TO_ALL2CLOUDS);
            if(folder.length()==0){
                //Thong bao khong co file trong thu muc backup
                Toast.makeText(Backup_Tab.this, "Folder is empty!", Toast.LENGTH_LONG);
            } else {
                for (String fileName : folder.list()){
                    File file = new File(folder, fileName);
                    UploadFile upload = new UploadFile(
                            this,
                            Backup_Tab.this,
                            ((MainActivity)getParent()).mDBU.getmApi(),
                            "/" + GetDeviceInfo().IMEI + "/",
                            file);
                    upload.execute();
                }
            }
        }
    }
}
