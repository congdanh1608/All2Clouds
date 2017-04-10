package com.example.ypham.all2clouds;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Y Pham on 11/27/2014.
 */
public class Overview_Tab extends Activity {

    Button btLinkDropbox;
    ListView listView;
    Button btHelp, btAbout;
    List<SystemInfo> listSystemInfo = new ArrayList<SystemInfo>();
    Tools tools = new Tools();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.overview_tab);
        btHelp = (Button)findViewById(R.id.bt_help);
        btHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tools.createDialog(Overview_Tab.this, "Help",tools.ReadRawTextFile(Overview_Tab.this, R.raw.help)).show();
            }
        });

        btAbout = (Button)findViewById(R.id.bt_about);
        btAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tools.createDialog(Overview_Tab.this, "About",tools.ReadRawTextFile(Overview_Tab.this, R.raw.about)).show();
            }
        });
        listView = (ListView)findViewById(R.id.sysInfoListView);
        btLinkDropbox = (Button)findViewById(R.id.btLinkToDropbox);

        SystemInfoArrayAdapter sysAdapter = new SystemInfoArrayAdapter(this, listSystemInfo.toArray(getConfigData()));
        listView.setAdapter(sysAdapter);
    }
    //Tao mang du lieu config system info
    private SystemInfo[] getConfigData(){
        String rootAvailable = "";
        if(tools.CheckRoot()){
            rootAvailable = "OK";
        } else {
            rootAvailable = "Denied";
            tools.createDialog(this, "Root Permission", tools.ReadRawTextFile(this, R.raw.alertpermission)).show();
        }
        SystemInfo rootAccess = new SystemInfo(getResources().getString(R.string.root_access),rootAvailable);
        listSystemInfo.add(rootAccess);

        String pathBackup = "";
        File folder = new File(ConstPath.PATH_TO_ALL2CLOUDS);
        if (folder.exists()){
            pathBackup = ConstPath.PATH_TO_ALL2CLOUDS;
            //tvBakDirect2.setText(ConstPath.PATH_TO_MY_BACKUP);
        }else pathBackup = "Not Exists.";//tvBakDirect2.setText("Not Found");
        SystemInfo configPath = new SystemInfo(getResources().getString(R.string.backup_directory),pathBackup);
        listSystemInfo.add(configPath);

        SystemInfo cloudStorage = new SystemInfo(getResources().getString(R.string.cloud_storage), "Dropbox");
        listSystemInfo.add(cloudStorage);
        SystemInfo[] aSytemInfo = new SystemInfo[listSystemInfo.size()];
        return aSytemInfo;
    }
    /*//Doc file txt trong raw tra ve kieu string
    public String ReadRawTextFile(int resId){

        InputStream inputStream = this.getResources().openRawResource(resId);

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
    //Tao alert dialog voi tieu de tiitle va noi dung content
    public Dialog createDialog(String title, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final TextView input = new TextView(this);
        input.setTextColor(Color.parseColor("#34495E"));
        input.setPadding(30,5,25,0);
        input.setText(content);
        builder.setView(input);
        builder.setTitle(title)
                .setIcon(R.drawable.all2clouds)
                .setCancelable(false)//("Are you sure you want to exit?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = builder.create();
        return alertDialog;
    }*/

}
