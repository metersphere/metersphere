package io.metersphere.commons.utils;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class DBCompressConfig implements MybatisInterceptorConfigHolder {
    @Override
    public List<MybatisInterceptorConfig> interceptorConfig() {
//        return Arrays.asList(
//                new MybatisInterceptorConfig("io.metersphere.base.domain.FileContent", "file", "io.metersphere.commons.utils.CompressUtils", "zip", "unzip")
//        );
        return Collections.emptyList();
    }
}