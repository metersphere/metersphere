package io.metersphere.autoconfigure;

import org.redisson.config.Config;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import javax.annotation.Resource;

@EnableConfigurationProperties({RedisProperties.class})
public class RedissonConfig implements RedissonAutoConfigurationCustomizer {
    @Resource
    private RedisProperties redisProperties;

    @Override
    public void customize(Config configuration) {
        int maxActive = redisProperties.getLettuce().getPool().getMaxActive();
        configuration.setNettyThreads(maxActive);
        configuration.setThreads(maxActive / 2 == 0 ? 1 : maxActive / 2);
    }
}
