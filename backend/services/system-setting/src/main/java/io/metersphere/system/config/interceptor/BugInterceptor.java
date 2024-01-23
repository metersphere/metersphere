package io.metersphere.system.config.interceptor;

import io.metersphere.bug.domain.BugContent;
import io.metersphere.sdk.util.CompressUtils;
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
        configList.add(new MybatisInterceptorConfig(BugContent.class, "description", CompressUtils.class, "zip", "unzip"));
        return configList;
    }
}
