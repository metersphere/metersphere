package io.metersphere.driver;

import java.util.Map;

public class AndroidCapability extends Capability {

    private String app;

    private String appPackage;

    private String appActivity;

    public Map<String, String> toMap() {
        return super.toMap(this);
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }


    public String getAppPackage() {
        return appPackage;
    }

    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
    }

    public String getAppActivity() {
        return appActivity;
    }

    public void setAppActivity(String appActivity) {
        this.appActivity = appActivity;
    }

}
