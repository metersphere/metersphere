package io.metersphere.metadata.utils;

import org.apache.commons.lang3.StringUtils;

public class MetadataUtils {
    public static String getFileType(String filename) {
        if (StringUtils.isNotEmpty(filename) && filename.indexOf(".") != -1) {
            int s = filename.lastIndexOf(".") + 1;
            String type = filename.substring(s);
            return type.toUpperCase();
        }
        return "";
    }

    public static String getFileNameByRemotePath(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            return "";
        } else {
            String[] stringArr = StringUtils.split(filePath, "/");
            return stringArr[stringArr.length - 1];
        }
    }

    public static String getFileName(String name, String type) {
        type = StringUtils.isNotEmpty(type) ? type.toLowerCase() : null;
        if (type != null && !name.endsWith(type)) {
            name = StringUtils.join(name, ".", type);
        }
        return name;
    }

}
