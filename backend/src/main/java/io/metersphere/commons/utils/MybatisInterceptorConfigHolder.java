package io.metersphere.commons.utils;

import java.util.ArrayList;
import java.util.List;

public interface MybatisInterceptorConfigHolder {
    default List<MybatisInterceptorConfig> interceptorConfig() {
        return new ArrayList<>();
    }
}