package io.metersphere.system.config.interceptor;

import io.metersphere.functional.domain.FunctionalCaseHistory;
import io.metersphere.sdk.util.CompressUtils;
import io.metersphere.system.utils.MybatisInterceptorConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class FunctionalCaseInterceptor {
    @Bean
    public List<MybatisInterceptorConfig> functionalCaseCompressConfigs() {
        List<MybatisInterceptorConfig> configList = new ArrayList<>();

        configList.add(new MybatisInterceptorConfig(FunctionalCaseHistory.class, "content", CompressUtils.class, "zip", "unzip"));


        return configList;
    }
}
