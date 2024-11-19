package io.metersphere.project.utils;

import org.apache.commons.lang3.StringUtils;

public class DataBaseStringUtils {

    public static String parseMaxString(String name, int maxLength) {
        if (StringUtils.isBlank(name)) {
            return StringUtils.EMPTY;
        }
        if (name.length() > maxLength) {
            return name.substring(0, maxLength - 3) + "...";
        } else {
            return name;
        }
    }
}
