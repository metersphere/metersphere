package io.metersphere.system.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 *
 * 缓存提示词模板
 *
 * @Author: jianxing
 * @CreateTime: 2024-10-09  10:57
 */
public abstract class PromptTemplateCache {

    public abstract String getCacheKey();
    public abstract String getTemplateFileName();

    /**
     * 缓存提示词模板
     * 在内存中缓存一天
     */
    private final Cache<String, String> apiCaseAITemplateCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build();

    public String getTemplate() {
        String value = apiCaseAITemplateCache.getIfPresent(getCacheKey());
        if (StringUtils.isBlank(value)) {
            try {
                 // 缓存没有则从资源文件中读取
                URL resource = this.getClass().getClassLoader().getResource(getTemplateFileName());
                value = IOUtils.toString(resource.openStream(), StandardCharsets.UTF_8);
                apiCaseAITemplateCache.put(getCacheKey(), value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return value;
    }
}
