package io.metersphere.commons.utils;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class DBEncryptConfig implements MybatisInterceptorConfigHolder {
    @Override
    public List<MybatisInterceptorConfig> interceptorConfig() {
        return Collections.emptyList();
    }
}