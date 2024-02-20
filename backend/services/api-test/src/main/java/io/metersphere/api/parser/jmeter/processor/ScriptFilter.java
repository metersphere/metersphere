package io.metersphere.api.parser.jmeter.processor;

import io.metersphere.project.constants.ScriptLanguageType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.LogUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptFilter {
    public static final String beanshell = "/blacklist/beanshell.bk";
    public static final String groovy = "/blacklist/groovy.bk";
    public static final String python = "/blacklist/python.bk";
    // 关键字内容较小，全局缓存下来避免重复读取
    public static final Map<String, List<String>> scriptCache = new HashMap<>();

    static {
        // 初始化安全过滤脚本
        ScriptFilter.initScript(ScriptFilter.beanshell);
        ScriptFilter.initScript(ScriptFilter.python);
        ScriptFilter.initScript(ScriptFilter.groovy);
    }

    public static void initScript(String path) {
        try (InputStream in = ScriptFilter.class.getResourceAsStream(path)) {
            List<String> bks = IOUtils.readLines(in, Charset.defaultCharset());
            if (CollectionUtils.isNotEmpty(bks)) {
                scriptCache.put(path, bks);
            }
        } catch (Exception ex) {
            LogUtils.error(ex.getMessage());
        }
    }

    private static void blackList(StringBuffer buffer, String script, String path) {
        try {
            List<String> bks = scriptCache.get(path);
            if (CollectionUtils.isNotEmpty(bks)) {
                bks.forEach(item -> {
                    if (script.contains(item)) {
                        buffer.append(item).append(",");
                    }
                });
            }
        } catch (Exception ex) {
            LogUtils.error(ex.getMessage());
        }
    }

    public static void verify(String language, String label, String script) {
        // 默认 groovy
        ScriptLanguageType scriptLanguageType = Arrays.stream(ScriptLanguageType.values())
                .filter(item -> StringUtils.equals(item.name(), language))
                .findFirst()
                .orElse(ScriptLanguageType.GROOVY);

        if (StringUtils.isNotEmpty(script)) {
            final StringBuffer buffer = new StringBuffer();
            switch (scriptLanguageType) {
                case BEANSHELL:
                    blackList(buffer, script, beanshell);
                    break;
                case PYTHON:
                    blackList(buffer, script, python);
                    break;
                default:
                    blackList(buffer, script, groovy);
                    break;
            }
            if (StringUtils.isNotEmpty(buffer.toString())) {
                String message = "脚本内包含敏感函数：【" + buffer.substring(0, buffer.toString().length() - 1) + "】";
                if (StringUtils.isNotEmpty(label)) {
                    message = label + "," + message;
                }
                throw new MSException(message);
            }
        }
    }
}
