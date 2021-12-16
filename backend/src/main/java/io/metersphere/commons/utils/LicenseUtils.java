package io.metersphere.commons.utils;

import java.lang.reflect.Method;

public class LicenseUtils {
    public static boolean valid() {
        try {
            Class<?> aClass = Class.forName("io.metersphere.xpack.license.util.LicenseCache");
            Method get = aClass.getMethod("valid");
            System.out.println("====");
            return (boolean) get.invoke(null);
        } catch (Exception e) {
            LogUtil.error(e);
            e.printStackTrace();
            return false;
        }
    }
}
