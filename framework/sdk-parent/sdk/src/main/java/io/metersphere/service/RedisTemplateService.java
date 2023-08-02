package io.metersphere.service;

import io.metersphere.commons.utils.LogUtil;
import io.metersphere.utils.LoggerUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class RedisTemplateService {
    public static final long TIME_OUT = 480;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public boolean setIfAbsent(String key, String value) {
        try {
            return redisTemplate.opsForValue().setIfAbsent(key, value);
        } catch (Exception e) {
            LoggerUtil.error(key, e);
            return true;
        }
    }

    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            LoggerUtil.error(key, e);
        }
        return null;
    }

    public boolean delete(String key) {
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            LoggerUtil.error(key, e);
            return false;
        }
    }

    /**
     * 加锁
     */
    public boolean lock(String testPlanReportId, String key, String value) {
        Boolean hasReport = redisTemplate.opsForValue().setIfAbsent(
                StringUtils.join(testPlanReportId, key),
                value,
                TIME_OUT,
                TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(hasReport)) {
            redisTemplate.opsForValue().setIfPresent(
                    StringUtils.join(testPlanReportId, key),
                    value,
                    TIME_OUT,
                    TimeUnit.MINUTES);
            return false;
        } else {
            return true;
        }
    }

    public boolean has(String testPlanReportId, String key, String reportId) {
        try {
            Object value = redisTemplate.opsForValue().get(StringUtils.join(testPlanReportId, key));
            return ObjectUtils.isNotEmpty(value) && StringUtils.equals(reportId, String.valueOf(value));
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return false;
    }

    /**
     * 解锁
     */
    public boolean unlock(String testPlanReportId, String key, String value) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        RedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(StringUtils.join(testPlanReportId, key)), value);
        if (Objects.equals(1L, result)) {
            return true;
        }
        return false;
    }
}
