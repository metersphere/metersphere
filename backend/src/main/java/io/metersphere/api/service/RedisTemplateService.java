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
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
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
}
