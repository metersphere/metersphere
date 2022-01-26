package io.metersphere.api.dto.shell.filter;

import io.metersphere.commons.exception.MSException;
import io.metersphere.plugin.core.utils.LogUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.List;

public class ScriptFilter {
    public static final String beanshell = "/blacklist/beanshell.bk";
    public static final String groovy = "/blacklist/groovy.bk";
    public static final String python = "/blacklist/python.bk";

    private static void blackList(StringBuffer buffer, String script, String path) {
        try {
            InputStream in = ScriptFilter.class.getResourceAsStream(path);
            List<String> bks = IOUtils.readLines(in);
            bks.forEach(item -> {
                if (script.contains(item) && script.indexOf(item) != -1) {
                    buffer.append(item).append(",");
                }
            });
        } catch (Exception ex) {
            LogUtil.error(ex.getMessage());
        }
    }

    public static void verify(String language, String label, String script) {
        if(language == null){
            language = "";
        }
        if (StringUtils.isNotEmpty(script)) {
            final StringBuffer buffer = new StringBuffer();
            switch (language) {
                case "beanshell":
                    blackList(buffer, script, beanshell);
                    break;
                case "python":
                    blackList(buffer, script, python);
                    break;
                default:
                    blackList(buffer, script, groovy);
                    break;
            }
            if (StringUtils.isNotEmpty(buffer.toString())) {
                String message = "脚本内包含敏感函数：【" + buffer.toString().substring(0, buffer.toString().length() - 1) + "】";
                if (StringUtils.isNotEmpty(label)) {
                    message = label + "," + message;
                }
                MSException.throwException(message);
            }
        }
    }
}
