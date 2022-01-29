package io.metersphere.driver;


import org.apache.commons.beanutils.BeanMap;

import java.util.HashMap;
import java.util.Map;

public class Capability {

    private String platformName;

    private String platformVersion;

    private String deviceName;

    private String automationName;

    public Map<String, String> toMap(Capability capability) {
        BeanMap beanMap = new BeanMap(capability);
        Map<String, String> map = new HashMap(beanMap);
        map.remove("class");
        return map;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getAutomationName() {
        return automationName;
    }

    public void setAutomationName(String automationName) {
        this.automationName = automationName;
    }
}
