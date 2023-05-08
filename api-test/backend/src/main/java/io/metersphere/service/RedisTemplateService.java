package io.metersphere.service;

import io.metersphere.api.jmeter.utils.JmxFileUtil;
import io.metersphere.commons.enums.LockEnum;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.utils.LoggerUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

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
    public boolean lock(String key) {
        return redisTemplate.opsForValue().setIfAbsent(StringUtils.join(PRX, key), LockEnum.LOCK.name());
    }

    public boolean has(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(StringUtils.join(PRX, key));
            if (ObjectUtils.isNotEmpty(value)) {
                if (StringUtils.equals(LockEnum.LOCK.name(), String.valueOf(value))) {
                    // 设置一分钟超时
                    redisTemplate.opsForValue().setIfPresent(StringUtils.join(PRX, key),
                            LockEnum.WAITING.name(), TIME_OUT, TimeUnit.SECONDS);
                    return false;
                }
            } else {
                redisTemplate.opsForValue().setIfAbsent(StringUtils.join(PRX, key),
                        LockEnum.WAITING.name(), TIME_OUT, TimeUnit.SECONDS);
                return false;
            }
            return true;
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return false;
    }
}
