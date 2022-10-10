package io.metersphere.gateway.config;

import io.metersphere.base.domain.AuthSource;
import io.metersphere.base.domain.FileContent;
import io.metersphere.base.domain.TestResource;
import io.metersphere.commons.utils.CompressUtils;
import io.metersphere.commons.utils.MybatisInterceptorConfig;
import io.metersphere.interceptor.MybatisInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@MapperScan(basePackages = {"io.metersphere.base.mapper"}, sqlSessionFactoryRef = "sqlSessionFactory")
@Configuration
public class DatabaseConfig {
    @Bean
    @ConditionalOnMissingBean
    public MybatisInterceptor dbInterceptor() {
        MybatisInterceptor interceptor = new MybatisInterceptor();
        List<MybatisInterceptorConfig> configList = new ArrayList<>();
        configList.add(new MybatisInterceptorConfig(FileContent.class, "file", CompressUtils.class, "zip", "unzip"));
        configList.add(new MybatisInterceptorConfig(TestResource.class, "configuration"));
        configList.add(new MybatisInterceptorConfig(AuthSource.class, "configuration"));
        interceptor.setInterceptorConfigList(configList);
        return interceptor;
    }
}