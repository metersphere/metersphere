package io.metersphere.config;

import com.github.pagehelper.PageInterceptor;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.MybatisInterceptor;
import io.metersphere.commons.utils.MybatisInterceptorConfig;
import io.metersphere.commons.utils.MybatisInterceptorConfigHolder;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Configuration
@MapperScan(basePackages = "io.metersphere.base.mapper", sqlSessionFactoryRef = "sqlSessionFactory")
@EnableTransactionManagement
@PropertySource(value = {"file:/opt/fit2cloud/conf/metersphere.properties"}, encoding = "UTF-8", ignoreResourceNotFound = true)
public class MybatisConfig {

    @Bean
    @ConditionalOnMissingBean
    public PageInterceptor pageInterceptor() {
        PageInterceptor pageInterceptor = new PageInterceptor();
        Properties properties = new Properties();
        properties.setProperty("helperDialect", "mysql");
        properties.setProperty("rowBoundsWithCount", "true");
        properties.setProperty("reasonable", "true");
        properties.setProperty("offsetAsPageNum", "true");
        properties.setProperty("pageSizeZero", "true");
        pageInterceptor.setProperties(properties);
        return pageInterceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    public MybatisInterceptor dbInterceptor() {
        MybatisInterceptor interceptor = new MybatisInterceptor();
        List<MybatisInterceptorConfig> configList = new ArrayList<>();
        interceptor.setInterceptorConfigList(configList);
        return interceptor;
    }

    /**
     * 等到ApplicationContext 加载完成之后 装配MybatisInterceptorConfigHolder
     */
    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        try {
            ApplicationContext context = event.getApplicationContext();
            MybatisInterceptor dBEncryptInterceptor = context.getBean(MybatisInterceptor.class);
            Map<String, MybatisInterceptorConfigHolder> beansOfType = context.getBeansOfType(MybatisInterceptorConfigHolder.class);
            for (MybatisInterceptorConfigHolder config : beansOfType.values()) {
                if (!CollectionUtils.isEmpty(config.interceptorConfig())) {
                    dBEncryptInterceptor.getInterceptorConfigList().addAll(config.interceptorConfig());
                }
            }
        } catch (Exception e) {
            LogUtil.error("装配Mybatis插件拦截配置错误，错误：" + e.getMessage());
        }

    }
}