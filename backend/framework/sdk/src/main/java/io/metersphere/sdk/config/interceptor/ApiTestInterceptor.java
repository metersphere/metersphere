package io.metersphere.sdk.config.interceptor;

import io.metersphere.api.domain.*;
import io.metersphere.sdk.util.CompressUtils;
import io.metersphere.sdk.util.MybatisInterceptorConfig;
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
        configList.add(new MybatisInterceptorConfig(ApiDefinitionBlob.class, "remark", CompressUtils.class, "zip", "unzip"));
        // ApiTestCaseBlob
        configList.add(new MybatisInterceptorConfig(ApiTestCaseBlob.class, "request", CompressUtils.class, "zip", "unzip"));
        // ApiReportBlob
        configList.add(new MybatisInterceptorConfig(ApiReportBlob.class, "content", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(ApiReportBlob.class, "console", CompressUtils.class, "zip", "unzip"));
        // ApiScenarioBlob
        configList.add(new MybatisInterceptorConfig(ApiScenarioBlob.class, "content", CompressUtils.class, "zip", "unzip"));
        // ApiScenarioReportBlob
        configList.add(new MybatisInterceptorConfig(ApiScenarioReportDetail.class, "content", CompressUtils.class, "zip", "unzip"));
        // ApiScenarioReportLog
        configList.add(new MybatisInterceptorConfig(ApiScenarioReportLog.class, "console", CompressUtils.class, "zip", "unzip"));
        return configList;
    }
}
