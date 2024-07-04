package io.metersphere.plugin.api.spi;

import io.metersphere.plugin.api.dto.ParameterConfig;
import org.apache.jorphan.collections.HashTree;

/**
 * @Author: jianxing
 * @CreateTime: 2024-06-16  19:23
 */
public interface JmeterElementConvertInterceptor {

    HashTree intercept(HashTree tree, MsTestElement element, ParameterConfig config);
}
