package io.metersphere.api.utils;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 记录场景批量执行集合报告一级步骤
 * 用来防止接口重试时，步骤重复创建
 *
 * @Author: jianxing
 * @CreateTime: 2025-05-16  13:56
 */
@Component
public class ApiScenarioIntegratedReportStepCache {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public static final String SCENARIO_INTEGRATED_REPORT_STEP_CACHE_PREFIX = "ASIRSC:";
    public static final Long EXPIRE_TIME = 20L;


    /**
     * 如果没有缓存则设置缓存
     * 设置成功，则返回 true，说明没有缓存
     * 设置失败，则返回 false，说明有缓存
     *
     * @return
     */
    public boolean setIfAbsent(String reportId, String scenarioId) {
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(
                        getKey(reportId, scenarioId),
                        StringUtils.EMPTY,
                        EXPIRE_TIME,
                        TimeUnit.SECONDS
                );
        if (!success) {
            // 如果缓存已存在，续期
            stringRedisTemplate.expire(
                    getKey(reportId, scenarioId),
                    EXPIRE_TIME,
                    TimeUnit.SECONDS
            );
        }
        return success;
    }

    private static String getKey(String reportId, String scenarioId) {
        return SCENARIO_INTEGRATED_REPORT_STEP_CACHE_PREFIX + reportId + "_" + scenarioId;
    }
}
