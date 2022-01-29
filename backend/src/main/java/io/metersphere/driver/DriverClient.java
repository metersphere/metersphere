package io.metersphere.driver;

import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;
import java.util.Map;

public class DriverClient {

    private final static String IOS = "ios";

    private final static String ANDROID = "android";

    private AndroidCapability androidCapability;

    private IosCapability iosCapability;

    private final String appiumUrl;

    public DriverClient(AndroidCapability capability, String appiumUrl) {
        this.androidCapability = capability;
        this.appiumUrl = appiumUrl;
    }

    public DriverClient(IosCapability capability, String appiumUrl) {
        this.iosCapability = capability;
        this.appiumUrl = appiumUrl;
    }


    public AndroidDriver<MobileElement> getAndroidDriver() {
        try {
            return new AndroidDriver<>(new URL(appiumUrl), getCaps(ANDROID));
        } catch (Exception e) {
            // log
        }
        return null;
    }

    public IOSDriver<MobileElement> getIosDriver() {
        try {
            return new IOSDriver<>(new URL(appiumUrl), getCaps(IOS));
        } catch (Exception e) {
            // log
        }
        return null;
    }

    private DesiredCapabilities getCaps(String platformName) {
        DesiredCapabilities caps = new DesiredCapabilities();
        switch (platformName) {
            case ANDROID: {
                Map<String, String> map = androidCapability.toMap();
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    caps.setCapability(entry.getKey(), entry.getValue());
                }
                return caps;
            }
            case IOS: {
                Map<String, String> map = iosCapability.toMap();
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    caps.setCapability(entry.getKey(), entry.getValue());
                }
                return caps;
            }
            default: return null;
        }
    }
}
