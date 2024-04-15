package io.metersphere.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@EnableAsync(proxyTargetClass = true)
@Configuration
public class AsyncConfig {
    /**
     * 核心线程数
     */
    private static final int CORE_POOL_SIZE = 10;
    /**
     * 最大线程数
     */
    private static final int MAX_POOL_SIZE = 10;


    /**
     * ThreadPoolTaskExecutor不会自动创建ThreadPoolExecutor，需要手动调initialize才会创建。
     * 如果@Bean就不需手动，会自动InitializingBean的afterPropertiesSet来调initialize
     *
     */
    @Bean(name = "threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置线程池最大线程数
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        // 线程池活跃的线程数
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setThreadNamePrefix("notice-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

}
