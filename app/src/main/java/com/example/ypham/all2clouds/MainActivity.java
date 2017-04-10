package com.example.ypham.all2clouds;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends TabActivity {

    DropboxUtilities mDBU;
    //Tab overview de chung thuc voi dropbox
    Overview_Tab overviewTab;
    TabHost tabHost;
    Tools tools = new Tools();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        mDBU = new DropboxUtilities(this);
        //Basic Android widgets
        setContentView(R.layout.activity_main);
        //Kiem tra khoa ung dung dropbox cap co hop le ko
        if(mDBU.checkAppKeySetup()){
            if (!mDBU.getmLoggedIn()){
                RequireLinkDropbox();
            }
        }else {
            showToast("You must apply for an app key and secret from developers.dropbox.com " +
                    "and add them to the All2Clouds app.");
            finish();
        }
        //Khoi tao cac tab
        tabHost = getTabHost();
       /* tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            //Doi mau tieu de cua Tab
            @Override
            public void onTabChanged(String tabId) {
                for (int i=0; i<tabHost.getTabWidget().getChildCount(); i++) {
                    TextView tv = (TextView)tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
                    tv.setTextColor(getResources().getColor(R.color.white));
                }
            }
        });*/

        TabHost.TabSpec spec;
        Intent intent;

        //Tab Overview
        intent = new Intent().setClass(this, Overview_Tab.class);
        spec = tabHost.newTabSpec("Overview_Tab").setIndicator("Overview").setContent(intent);
        tabHost.addTab(spec);

        //Tab Backup
        intent = new Intent().setClass(this, Backup_Tab.class);
        spec = tabHost.newTabSpec("Backup_Tab").setIndicator("Backup").setContent(intent);
        tabHost.addTab(spec);

        //Tab Restore
        intent = new Intent().setClass(this, Restore_Tab.class);
        spec = tabHost.newTabSpec("Restore_Tab").setIndicator("Restore").setContent(intent);
        tabHost.addTab(spec);

        //Default Tab Overview
        tabHost.getTabWidget().setCurrentTab(0);
        //Tao doi tuong cua tab overview truy cap den cac thuoc tinh cua activity Overview
        overviewTab = (Overview_Tab)getLocalActivityManager().getActivity("Overview_Tab");
        //

        for (int i=0; i<tabHost.getTabWidget().getChildCount(); i++) {
            TextView tv = (TextView)tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(getResources().getColor(R.color.white));
        }

        overviewTab.btLinkDropbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mDBU.getmLoggedIn()){
                    mDBU.logOut();
                    changeStatus(mDBU.getmLoggedIn());
                    //Thoat app
                    MainActivity.this.finish();
                } else {
                    mDBU.LinkToDropbox();
                }
            }
        });
    }
    //Yeu cau nguoi dung cap quyen truy cap tai khoan cloud dropbox
    public void RequireLinkDropbox() {
        //Bat dau chung thuc
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        mDBU.LinkToDropbox();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        finish();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Link Dropbox").setMessage("Allow access to your Dropbox account?").setPositiveButton("Ok", dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener).show();
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        error.show();
    }

    private void changeStatus(boolean loggedIn){
        if (loggedIn) {
            //showToast("Linked with Dropbox");
            overviewTab.btLinkDropbox.setText("Unlink with Dropbox");
            //mDisplay.setVisibility(View.VISIBLE);
        } else {
            showToast("Unlinked with Dropbox");
            overviewTab.btLinkDropbox.setText("Link with Dropbox");
            //mDisplay.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDBU.storeAuth();
        changeStatus(mDBU.getmLoggedIn());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_settings:
                /*Intent settingActivity = new Intent(this, SettingsActivity.class);
                startActivity(settingActivity);*/
                return true;
            case R.id.action_help:
                tools.createDialog(this, "Help",tools.ReadRawTextFile(this, R.raw.help)).show();
                return true;
            case R.id.action_about:
                tools.createDialog(this, "About",tools.ReadRawTextFile(this, R.raw.about)).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
