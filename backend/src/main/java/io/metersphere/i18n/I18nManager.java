package io.metersphere.i18n;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import io.metersphere.commons.utils.IOUtils;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class I18nManager implements ApplicationRunner {

    private static Map<String, Map<String, String>> i18nMap = new HashMap<>();

    private List<String> dirs;

    public I18nManager(List<String> dirs) {
        this.dirs = dirs;
    }

    public static Map<String, Map<String, String>> getI18nMap() {
        return i18nMap;
    }

    private static Resource[] getResources(String dir, String suffix) throws IOException {
        Resource[] result = new Resource[0];
        PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
        if (!patternResolver.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + dir).exists()) {
            return result;
        }
        Resource[] resources = patternResolver.getResources(ResourceUtils.CLASSPATH_URL_PREFIX + dir + "*");
        for (Resource resource : resources) {
            if (StringUtils.endsWithIgnoreCase(resource.getFilename(), suffix)) {
                result = ArrayUtils.add(result, resource);
            }
        }
        return result;
    }

    private void init() {
        try {
            for (Lang lang : Lang.values()) {
                Resource[] resources = new Resource[0];
                String i18nKey = lang.getDesc().toLowerCase();
                for (String dir : dirs) {
                    resources = ArrayUtils.addAll(resources, getResources(dir, i18nKey + ".json"));
                }
                for (Resource resource : resources) {
                    if (resource.exists()) {
                        try (InputStream inputStream = resource.getInputStream()) {
                            String fileContent = IOUtils.toString(inputStream, Charset.defaultCharset());
                            Map<String, String> langMap = JSON.parseObject(fileContent, new TypeReference<HashMap<String, String>>() {
                            });
                            i18nMap.computeIfAbsent(i18nKey, k -> new HashMap<>());
                            i18nMap.get(i18nKey).putAll(langMap);
                        } catch (Exception e) {
                            e.printStackTrace();
                            LogUtil.error("failed to load resource: " + resource.getURI());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error("failed to load i18n.", e);
        }
    }

    /**
     * 国际化配置初始化
     */
    @Override
    public void run(ApplicationArguments args) {
        init();
    }
}
