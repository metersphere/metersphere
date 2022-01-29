package io.metersphere.driver;

import java.util.Map;

public class IosCapability extends Capability {

    private String udid;

    public String getUdid() {
        return udid;
    }

    public void setUdid(String udid) {
        this.udid = udid;
    }

    public Map<String, String> toMap() {
        return super.toMap(this);
    }
}
