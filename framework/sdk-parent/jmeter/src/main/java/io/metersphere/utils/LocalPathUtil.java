package io.metersphere.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class LocalPathUtil {
    public static final String NODE = "node";
    public static final String MS = "ms";
    public static final String PRE_PATH = File.separator + "opt"
            + File.separator + "metersphere"
            + File.separator + "data"
            + File.separator + "api-folder"
            + File.separator;

    public static String JAR_PATH =
            StringUtils.join(PRE_PATH, "jar", File.separator);

    public static String PLUGIN_PATH = StringUtils.join(PRE_PATH, "plugin", File.separator);
}
