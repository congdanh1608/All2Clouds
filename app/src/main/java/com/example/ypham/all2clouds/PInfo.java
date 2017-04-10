package com.example.ypham.all2clouds;

import android.graphics.drawable.Drawable;

/**
 * Created by Silver Wolf on 08/10/2014.
 */
public class PInfo {
    public String appname = "";
    public String pname = "";
    public String versionName = "";
    public int versionCode = 0;
    public Drawable icon;

    public PInfo(){
    }

    public PInfo(String _appname, String _pname, String _versionName, int _versionCode, Drawable _icon){
        appname = _appname;
        pname = _pname;
        versionName = _versionName;
        versionCode = _versionCode;
        icon = _icon;
    }
}

