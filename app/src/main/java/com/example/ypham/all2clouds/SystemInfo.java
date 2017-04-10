package com.example.ypham.all2clouds;

/**
 * Created by Y Pham on 12/6/2014.
 */
public class SystemInfo {
    private String systemConfig;
    private String configValue;

    public SystemInfo (String sysconfig, String configvalue){
        this.systemConfig = sysconfig;
        this.configValue = configvalue;
    }

    public String getSystemConfig(){
        return systemConfig;
    }

    public String getConfigValue() {
        return configValue;
    }
}
