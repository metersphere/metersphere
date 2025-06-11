package io.metersphere.api.utils;

import io.metersphere.system.utils.PromptTemplateCache;
import org.springframework.stereotype.Component;

/**
 *
 * 缓存提示词模板
 *
 * @Author: jianxing
 * @CreateTime: 2024-10-09  10:57
 */
@Component
public class ApiCasePromptTemplateCache extends PromptTemplateCache {
    public static final String TEMPLATE_FILENAME = "apiCaseAiTemplate.vm";

    @Override
    public String getCacheKey() {
        return null;
    }

    @Override
    public String getTemplateFileName() {
        return TEMPLATE_FILENAME;
    }
}
