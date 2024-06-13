package io.metersphere.plan.dto.request;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ApiExecutionMapService {

    public static final String MAP_PREFIX = "map:";

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 初始化执行集合
     * 保存需要执行的资源ID
     *
     * @param mapId
     * @param resourceIdsMap
     */
    public void initMap(String mapId, Map<String, List<String>> resourceIdsMap) {
        String key = getKey(mapId);
        redisTemplate.opsForHash().putAll(key, resourceIdsMap);
        redisTemplate.expire(key, 1, TimeUnit.DAYS);
    }

    private String getKey(String mapId) {
        return MAP_PREFIX + mapId;
    }

    public List<String> getAndRemove(String mapId, String key) {
        List resourceIds = (List<String>) redisTemplate.opsForHash().get(MAP_PREFIX + mapId, key);
        removeItem(mapId, key);
        return resourceIds;
    }

    /**
     * 从执行集合中去除选项
     */
    public void removeItem(String mapId, String key) {
        redisTemplate.opsForHash().delete(MAP_PREFIX + mapId, key);
        Long size = redisTemplate.opsForHash().size(MAP_PREFIX + mapId);
        if (size == null || size == 0) {
            // 集合没有元素，则删除集合
            redisTemplate.delete(MAP_PREFIX + mapId);
        }
    }
}
