package io.metersphere.commons.utils;

import java.lang.reflect.Method;

public class LicenseUtils {
    public static boolean valid() {
        try {
            Class<?> aClass = Class.forName("io.metersphere.xpack.license.util.LicenseCache");
            Method get = aClass.getMethod("valid");
            return (boolean) get.invoke(null);
        } catch (Exception e) {
            LogUtil.error(e);
            return false;
        }
    }
}
