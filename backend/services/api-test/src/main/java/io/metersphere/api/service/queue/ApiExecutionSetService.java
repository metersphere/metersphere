package io.metersphere.api.service.queue;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ApiExecutionSetService {

    public static final String SET_PREFIX = "set:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 初始化执行集合
     * 保存需要执行的资源ID
     * @param setId
     * @param resourceIds
     */
    public void initSet(String setId, List<String> resourceIds) {
        String key = getKey(setId);
        stringRedisTemplate.opsForSet().add(key, resourceIds.toArray(new String[0]));
        stringRedisTemplate.expire(key, 1, TimeUnit.DAYS);
    }

    private String getKey(String setId) {
        return SET_PREFIX + setId;
    }

    /**
     * 从执行集合中去除选项
     */
    public Long removeItem(String setId, String taskItemId) {
        stringRedisTemplate.opsForSet().remove(SET_PREFIX + setId, taskItemId);
        Long size = stringRedisTemplate.opsForSet().size(SET_PREFIX + setId);
        if (size == null || size == 0) {
            // 集合没有元素，则删除集合
            stringRedisTemplate.delete(SET_PREFIX + setId);
        }
        return size;
    }

    /**
     * 从执行集合中去除选项
     */
    public Long removeItems(String setId, List<String> taskItemIds) {
        stringRedisTemplate.opsForSet().remove(SET_PREFIX + setId, taskItemIds.toArray());
        Long size = stringRedisTemplate.opsForSet().size(SET_PREFIX + setId);
        if (size == null || size == 0) {
            // 集合没有元素，则删除集合
            stringRedisTemplate.delete(SET_PREFIX + setId);
        }
        return size;
    }
}
