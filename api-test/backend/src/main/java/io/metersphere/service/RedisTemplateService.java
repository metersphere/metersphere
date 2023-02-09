package io.metersphere.service;

import io.metersphere.commons.utils.LogUtil;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisTemplateService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    public static final long TIME_OUT = 30;

    public boolean setIfAbsent(String key, String value) {
        try {
            return redisTemplate.opsForValue().setIfAbsent(key, value);
        } catch (Exception e) {
            LogUtil.error(e);
            return true;
        }
    }

    public boolean expire(String key) {
        try {
            return redisTemplate.expire(key, TIME_OUT, TimeUnit.MINUTES);
        } catch (Exception e) {
            LogUtil.error(e);
            return false;
        }
    }

    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return null;
    }

    public boolean delete(String key) {
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            LogUtil.error(e);
            return false;
        }
    }
}
