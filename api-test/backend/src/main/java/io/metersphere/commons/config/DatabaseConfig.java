package io.metersphere.commons.config;

import io.metersphere.base.domain.*;
import io.metersphere.commons.utils.CompressUtils;
import io.metersphere.interceptor.MybatisInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DatabaseConfig {
    @Bean
    public MybatisInterceptor dbInterceptor() {
        MybatisInterceptor interceptor = new MybatisInterceptor();
        List<io.metersphere.commons.utils.MybatisInterceptorConfig> configList = new ArrayList<>();
        configList.add(new io.metersphere.commons.utils.MybatisInterceptorConfig(FileContent.class, "file", CompressUtils.class, "zip", "unzip"));
        configList.add(new io.metersphere.commons.utils.MybatisInterceptorConfig(TestResource.class, "configuration"));
        configList.add(new io.metersphere.commons.utils.MybatisInterceptorConfig(AuthSource.class, "configuration"));

        configList.add(new io.metersphere.commons.utils.MybatisInterceptorConfig(ApiDefinitionExecResultWithBLOBs.class, "content", CompressUtils.class, "zipString", "unzipString"));
        configList.add(new io.metersphere.commons.utils.MybatisInterceptorConfig(ApiScenarioReportStructureWithBLOBs.class, "resourceTree", CompressUtils.class, "zip", "unzip"));
        configList.add(new io.metersphere.commons.utils.MybatisInterceptorConfig(ApiScenarioReportResultWithBLOBs.class, "content", CompressUtils.class, "zip", "unzip"));
        configList.add(new io.metersphere.commons.utils.MybatisInterceptorConfig(ApiScenarioReportResultWithBLOBs.class, "baseInfo", CompressUtils.class, "zipString", "unzipString"));
        interceptor.setInterceptorConfigList(configList);
        return interceptor;
    }
}
