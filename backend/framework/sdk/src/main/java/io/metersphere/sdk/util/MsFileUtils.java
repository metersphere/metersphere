package io.metersphere.sdk.util;

import io.metersphere.sdk.exception.MSException;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class MsFileUtils {

    public static final String DATE_ROOT_DIR = "/opt/metersphere/data/app";
    public static final String PLUGIN_DIR_NAME = "plugins";
    public static final String PLUGIN_DIR = DATE_ROOT_DIR + "/" + PLUGIN_DIR_NAME;

    public static final String FUNCTIONAL_CASE_ATTACHMENT_DIR_NAME = "functionalCaseAttachment";
    public static final String FUNCTIONAL_CASE_ATTACHMENT_DIR = DATE_ROOT_DIR + "/" + FUNCTIONAL_CASE_ATTACHMENT_DIR_NAME;

    public static void validateFileName(String... fileNames) {
        if (fileNames != null) {
            for (String fileName : fileNames) {
                if (StringUtils.isNotBlank(fileName) && StringUtils.contains(fileName, "." + File.separator)) {
                    throw new MSException(Translator.get("invalid_parameter"));
                }
            }
        }
    }
}
