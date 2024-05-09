package io.metersphere.system.config.interceptor;

import io.metersphere.plan.domain.TestPlanAllocation;
import io.metersphere.sdk.util.CompressUtils;
import io.metersphere.system.utils.MybatisInterceptorConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class TestPlanInterceptor {
    @Bean
    public List<MybatisInterceptorConfig> tstPlanCompressConfigs() {
        List<MybatisInterceptorConfig> configList = new ArrayList<>();

        configList.add(new MybatisInterceptorConfig(TestPlanAllocation.class, "runModeConfig", CompressUtils.class, "zip", "unzip"));

        return configList;
    }
}
