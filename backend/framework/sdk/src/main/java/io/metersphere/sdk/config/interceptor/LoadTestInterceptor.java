package io.metersphere.sdk.config.interceptor;

import io.metersphere.load.domain.*;
import io.metersphere.sdk.util.CompressUtils;
import io.metersphere.sdk.util.MybatisInterceptorConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class LoadTestInterceptor {
    @Bean
    public List<MybatisInterceptorConfig> projectCompressConfigs() {
        List<MybatisInterceptorConfig> configList = new ArrayList<>();

        configList.add(new MybatisInterceptorConfig(LoadTestBlob.class, "loadConfiguration", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(LoadTestBlob.class, "advancedConfiguration", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(LoadTestBlob.class, "envInfo", CompressUtils.class, "zip", "unzip"));

        configList.add(new MybatisInterceptorConfig(LoadTestReportBlob.class, "loadConfiguration", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(LoadTestReportBlob.class, "jmxContent", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(LoadTestReportBlob.class, "advancedConfiguration", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(LoadTestReportBlob.class, "envInfo", CompressUtils.class, "zip", "unzip"));

        configList.add(new MybatisInterceptorConfig(LoadTestReportLog.class, "content", CompressUtils.class, "zip", "unzip"));

        configList.add(new MybatisInterceptorConfig(LoadTestReportResult.class, "reportValue", CompressUtils.class, "zip", "unzip"));

        configList.add(new MybatisInterceptorConfig(LoadTestReportResultPart.class, "reportValue", CompressUtils.class, "zip", "unzip"));

        configList.add(new MybatisInterceptorConfig(LoadTestReportResultRealtime.class, "reportValue", CompressUtils.class, "zip", "unzip"));


        return configList;
    }
}
