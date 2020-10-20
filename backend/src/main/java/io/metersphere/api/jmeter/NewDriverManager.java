package io.metersphere.api.jmeter;

import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import org.apache.jmeter.NewDriver;

import java.io.File;
import java.net.MalformedURLException;

public class NewDriverManager {

    public static void loadJar(File file) {
        if (file != null) {
            try {
                NewDriver.addURL(file.toURI().toURL());
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
                MSException.throwException(e.getMessage());
            }
        }
    }

    public static void loadJar(String path) {
        try {
            NewDriver.addPath(path);
        } catch (MalformedURLException e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(e.getMessage());
        }
    }
}
