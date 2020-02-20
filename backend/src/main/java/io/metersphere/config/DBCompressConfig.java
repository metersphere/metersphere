package io.metersphere.config;

import io.metersphere.commons.utils.MybatisInterceptorConfig;
import io.metersphere.commons.utils.MybatisInterceptorConfigHolder;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DBCompressConfig implements MybatisInterceptorConfigHolder {
    @Override
    public List<MybatisInterceptorConfig> interceptorConfig() {
        return Arrays.asList(
                new MybatisInterceptorConfig("io.metersphere.base.domain.FileContent", "file", "io.metersphere.commons.utils.CompressUtils", "zip", "unzip")
        );
//        return Collections.emptyList();
    }
}