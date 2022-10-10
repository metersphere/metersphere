package io.metersphere.autoconfigure;

import io.metersphere.commons.constants.RedisKey;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class PermissionConfig implements ApplicationRunner {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Value("${spring.application.name}")
    private String service;

    @Override
    public void run(ApplicationArguments args) {
        try (InputStream inputStream = PermissionConfig.class.getResourceAsStream("/permission.json")){
            String permission = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            stringRedisTemplate.opsForHash().put(RedisKey.MS_PERMISSION_KEY, service, permission);
        } catch (Exception e) {
            LogUtil.error("load permissions file error: " + service, e);
        }
    }
}
