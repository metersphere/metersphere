package io.metersphere.service;

import io.metersphere.api.jmeter.utils.JmxFileUtil;
import io.metersphere.utils.LoggerUtil;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisTemplateService {
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
}
