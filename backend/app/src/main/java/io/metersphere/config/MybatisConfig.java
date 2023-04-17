package io.metersphere.config;

import io.metersphere.sdk.interceptor.MybatisInterceptor;
import io.metersphere.sdk.util.CompressUtils;
import io.metersphere.sdk.util.MybatisInterceptorConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.ArrayList;
import java.util.List;

@Configuration
@MapperScan(basePackages = {"io.metersphere.*.mapper"}, sqlSessionFactoryRef = "sqlSessionFactory")
@EnableTransactionManagement
public class MybatisConfig {
    @Bean
    @ConditionalOnMissingBean
    public MybatisInterceptor dbInterceptor() {
        MybatisInterceptor interceptor = new MybatisInterceptor();
        List<MybatisInterceptorConfig> configList = new ArrayList<>();
//        configList.add(new MybatisInterceptorConfig(FileContent.class, "file", CompressUtils.class, "zip", "unZip"));
//        configList.add(new MybatisInterceptorConfig(ApiTestReportDetail.class, "content", CompressUtils.class, "gzip", "unGzip"));
//        configList.add(new MybatisInterceptorConfig(TestResource.class, "configuration"));
//        configList.add(new MybatisInterceptorConfig(AuthSource.class, "configuration"));
//        configList.add(new MybatisInterceptorConfig(LoadTestReportLog.class, "content", CompressUtils.class, "zipString", "unZipString"));
        interceptor.setInterceptorConfigList(configList);
        return interceptor;
    }

}
