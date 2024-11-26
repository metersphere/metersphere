package io.metersphere.system.config.interceptor;

import io.metersphere.system.utils.MybatisInterceptorConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class BugInterceptor {
    @Bean
    public List<MybatisInterceptorConfig> bugCompressConfigs() {
        List<MybatisInterceptorConfig> configList = new ArrayList<>();
        return configList;
    }
}
