package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.BodyFileRequest;
import io.metersphere.api.dto.definition.request.MsTestPlan;
import io.metersphere.api.dto.scenario.request.BodyFile;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class RedisTemplateService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    public static final long TIME_OUT = 30;
    private static final String PRX = "TEST_PLAN_";

    public boolean setIfAbsent(String key, String value) {
        try {
            return redisTemplate.opsForValue().setIfAbsent(key, value, TIME_OUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            LogUtil.error(e);
            return true;
        }
    }

    public boolean delete(String key) {
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            LogUtil.error(e);
            return false;
        }
    }

    public void initDebug(JmeterRunRequestDTO request) {
        if (request.getHashTree() != null) {
            String key = StringUtils.join(request.getReportId(), "-", request.getTestId());
            redisTemplate.opsForValue().set(key, new MsTestPlan().getJmx(request.getHashTree()));

            List<BodyFile> files = new ArrayList<>();
            FileUtils.getFiles(request.getHashTree(), files);
            String file = StringUtils.join(key, "-FILE");
            redisTemplate.opsForValue().set(file, JSON.toJSONString(files));
        }
    }

    public List<BodyFile> getDebugFiles(BodyFileRequest request) {
        try {
            if (request != null) {
                String key = StringUtils.join(request.getReportId(), "-", request.getTestId(), "-FILE");
                Object script = redisTemplate.opsForValue().get(key);
                if (ObjectUtils.isNotEmpty(script)) {
                    return JSON.parseArray(script.toString(), BodyFile.class);
                }
                redisTemplate.delete(key);
            }
        } catch (Exception e) {
            LoggerUtil.error("获取附件异常", request.getReportId(), e);
        }
        return new ArrayList<>();
    }

    public void deleteDebug(JmeterRunRequestDTO request) {
        try {
            String key = StringUtils.join(request.getReportId(), "-", request.getTestId());
            String file = StringUtils.join(key, "-FILE");
            redisTemplate.delete(file);
            redisTemplate.delete(key);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    /**
     * 加锁
     */
    public boolean lock(String key, String value) {
        return redisTemplate.opsForValue().setIfAbsent(StringUtils.join(PRX, key), value, TIME_OUT, TimeUnit.SECONDS);
    }

    public boolean hasReport(String key, String reportId) {
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
