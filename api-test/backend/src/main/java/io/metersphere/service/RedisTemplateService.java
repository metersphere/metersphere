package io.metersphere.service;

import io.metersphere.api.jmeter.utils.JmxFileUtil;
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
    private static final String PRX = "TEST_PLAN_";
    public static final long TIME_OUT = 60;
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

    public void delFilePathAndScript(String reportId, String testId) {
        delete(JmxFileUtil.getExecuteScriptKey(reportId, testId));
        delete(JmxFileUtil.getExecuteFileKeyInRedis(reportId));
    }

    public void delFilePath(String reportId) {
        delete(JmxFileUtil.getExecuteFileKeyInRedis(reportId));
    }

    /**
     * 加锁
     */
    public boolean lock(String key, String value) {
        boolean hasReport = redisTemplate.opsForValue().setIfAbsent(
                StringUtils.join(PRX, key),
                value,
                TIME_OUT,
                TimeUnit.MINUTES);
        if (!hasReport) {
            redisTemplate.opsForValue().setIfPresent(
                    StringUtils.join(PRX, key),
                    value,
                    TIME_OUT,
                    TimeUnit.MINUTES);
        }
        return hasReport;
    }

    public boolean has(String key, String reportId) {
        try {
            Object value = redisTemplate.opsForValue().get(StringUtils.join(PRX, key));
            return ObjectUtils.isNotEmpty(value) && StringUtils.equals(reportId, String.valueOf(value));
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return false;
    }

    /**
     * 解锁
     */
    public boolean unlock(String key, String value) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        RedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(StringUtils.join(PRX, key)), value);
        if (Objects.equals(1L, result)) {
            return true;
        }
        return false;
    }
}
