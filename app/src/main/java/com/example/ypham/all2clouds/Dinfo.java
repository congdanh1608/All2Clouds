package com.example.ypham.all2clouds;

/**
 * Created by Y Pham on 11/27/2014.
 */
public class Dinfo {
    public String IMEI = null;
    public String IMSI = null;
    public String androidID = null;
    public String UUIDdevice = null;
    public String tmSerial = null;
    public String deviceModelName = null;
    public String deviceUser = null;
    public String deviceProduct = null;
    public String deviceHardWare = null;
    public String deviceBrand =null;
    public String myVersion = null;
    public  int sdkVersion = 0;

    public Dinfo(){
    }

    public Dinfo(String _IMEI, String _IMSI, String _androidID, String _UUIDdevice, String _tmSerial, String _deviceModelName,
                 String _deviceUser, String _deviceProduct, String _deviceHardWare, String _deviceBrand, String _myVersion, int _sdkVersion){
        IMEI = _IMEI;
        IMEI = _IMEI;
        androidID = _androidID;
        UUIDdevice = _UUIDdevice;
        tmSerial = _tmSerial;
        deviceModelName = _deviceModelName;
        deviceUser = _deviceUser;
        deviceProduct = _deviceProduct;
        deviceHardWare = _deviceHardWare;
        deviceBrand = _deviceBrand;
        myVersion = _myVersion;
        sdkVersion = _sdkVersion;
    }
}
