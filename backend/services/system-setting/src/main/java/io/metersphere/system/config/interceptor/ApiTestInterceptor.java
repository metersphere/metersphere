package io.metersphere.system.config.interceptor;

import io.metersphere.api.domain.*;
import io.metersphere.sdk.util.CompressUtils;
import io.metersphere.system.utils.MybatisInterceptorConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class ApiTestInterceptor {
    @Bean
    public List<MybatisInterceptorConfig> apiTestCompressConfigs() {
        List<MybatisInterceptorConfig> configList = new ArrayList<>();
        // ApiDefinitionBlob
        configList.add(new MybatisInterceptorConfig(ApiDefinitionBlob.class, "request", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(ApiDefinitionBlob.class, "response", CompressUtils.class, "zip", "unzip"));
        // ApiDefinitionMockConfig
        configList.add(new MybatisInterceptorConfig(ApiDefinitionMockConfig.class, "matching", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(ApiDefinitionMockConfig.class, "response", CompressUtils.class, "zip", "unzip"));
        // ApiTestCaseBlob
        configList.add(new MybatisInterceptorConfig(ApiTestCaseBlob.class, "request", CompressUtils.class, "zip", "unzip"));
        // ApiReportBlob
        configList.add(new MybatisInterceptorConfig(ApiReportDetail.class, "content", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(ApiReportLog.class, "console", CompressUtils.class, "zip", "unzip"));
        // ApiScenarioBlob
        configList.add(new MybatisInterceptorConfig(ApiScenarioBlob.class, "config", CompressUtils.class, "zip", "unzip"));
        // ApiScenarioStepBlob
        configList.add(new MybatisInterceptorConfig(ApiScenarioStepBlob.class, "content", CompressUtils.class, "zip", "unzip"));
        // ApiScenarioReportBlob
        configList.add(new MybatisInterceptorConfig(ApiScenarioReportDetailBlob.class, "content", CompressUtils.class, "zip", "unzip"));
        // ApiScenarioReportLog
        configList.add(new MybatisInterceptorConfig(ApiScenarioReportLog.class, "console", CompressUtils.class, "zip", "unzip"));
        // ApiDebugBlob
        configList.add(new MybatisInterceptorConfig(ApiDebugBlob.class, "request", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(ApiDebugBlob.class, "response", CompressUtils.class, "zip", "unzip"));

        return configList;
    }
}
