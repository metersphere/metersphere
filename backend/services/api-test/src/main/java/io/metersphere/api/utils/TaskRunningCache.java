package io.metersphere.api.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.metersphere.sdk.constants.CommonConstants;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 记录正在运行的任务
 * runningTasks 作为内存级别的一级缓存，减少网络交互
 * redis 作为分布式的二级缓存
 * 执行结束后，result-hub 清除二级缓存
 *
 * @Author: jianxing
 * @CreateTime: 2024-10-09  10:57
 */
@Component
public class TaskRunningCache {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 记录正在运行的任务
     */
    private final Cache<String, Boolean> runningTasks = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    /**
     * 如果没有缓存则设置缓存
     * 设置成功，则返回 true，说明没有缓存
     * 设置失败，则返回 false，说明有缓存
     * @param taskId
     * @return
     */
    public boolean setIfAbsent(String taskId) {
        Boolean hasCache = BooleanUtils.isTrue(runningTasks.getIfPresent(taskId));
        if (!hasCache) {
            // 原子操作，没有线程安全问题
            // 一级缓存没有，则查询二级缓存
            Boolean success = stringRedisTemplate.opsForValue()
                    .setIfAbsent(
                            getKey(taskId),
                            StringUtils.EMPTY,
                            1,
                            TimeUnit.DAYS
                    );

            // 设置二级缓存
            runningTasks.put(taskId, true);

            return success;
        }
        return false;
    }

    private static String getKey(String taskId) {
        return CommonConstants.RUNNING_TASK_PREFIX + taskId;
    }
}
