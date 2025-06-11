package io.metersphere.system.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.dto.AIRenderConfig;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
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

    private VelocityEngine velocityEngine;

    {
        Properties props = new Properties();
        props.setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath");
        props.setProperty("resource.loader.classpath.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityEngine = new VelocityEngine(props);
        velocityEngine.init();
    }

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

    public String getTemplate(AIRenderConfig renderConfig) {
        // 加载模板文件
        Template template = velocityEngine.getTemplate(getTemplateFileName());

        // 创建上下文并添加数据
        VelocityContext context = new VelocityContext();
        Map<String, Object> kvs = JSON.parseMap(JSON.toJSONString(renderConfig));
        kvs.forEach(context::put);

        // 渲染模板
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        return writer.toString();
    }
}
