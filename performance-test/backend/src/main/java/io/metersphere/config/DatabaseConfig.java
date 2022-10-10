package io.metersphere.config;

import io.metersphere.base.domain.AuthSource;
import io.metersphere.base.domain.FileContent;
import io.metersphere.base.domain.LoadTestReportLog;
import io.metersphere.base.domain.TestResource;
import io.metersphere.commons.utils.CompressUtils;
import io.metersphere.commons.utils.MybatisInterceptorConfig;
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
        List<MybatisInterceptorConfig> configList = new ArrayList<>();
        configList.add(new MybatisInterceptorConfig(FileContent.class, "file", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(TestResource.class, "configuration"));
        configList.add(new MybatisInterceptorConfig(AuthSource.class, "configuration"));
        configList.add(new MybatisInterceptorConfig(LoadTestReportLog.class, "content", CompressUtils.class, "zipString", "unzipString"));
        interceptor.setInterceptorConfigList(configList);
        return interceptor;
    }
}
