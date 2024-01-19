package io.metersphere.api.service;

import io.metersphere.sdk.util.JSON;
import io.metersphere.system.dto.pool.TestResourceNodeDTO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoundRobinService {
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 获取下一个节点
     */
    public TestResourceNodeDTO getNextNode(String poolId) throws Exception {
        // 从列表头部获取下一个节点
        String node = redisTemplate.opsForList().leftPop(poolId);

        if (StringUtils.isBlank(node)) {
            // 重试3次获取
            for (int i = 0; i < 3; i++) {
                node = redisTemplate.opsForList().leftPop(poolId);
                if (StringUtils.isNotBlank(node)) {
                    break;
                }
                Thread.sleep(1000);
            }
        }

        if (StringUtils.isNotBlank(node)) {
            // 将节点重新放回列表尾部，实现轮询
            redisTemplate.opsForList().rightPush(poolId, node);
        } else {
            return null;
        }

        return JSON.parseObject(node, TestResourceNodeDTO.class);
    }

    /**
     * 初始化节点列表
     */
    public void initializeNodes(String poolId, List<TestResourceNodeDTO> nodes) {
        // 检查节点是否有变更
        Long poolSize = redisTemplate.opsForList().size(poolId);
        int size = poolSize != null ? poolSize.intValue() : 0;
        if (size == nodes.size()) {
            // 对比redis中的节点列表和传入的节点列表是否一致
            boolean isSame = true;
            for (TestResourceNodeDTO node : nodes) {
                boolean isExist = false;
                for (int i = 0; i < size; i++) {
                    if (JSON.toJSONString(node).equals(redisTemplate.opsForList().index(poolId, i))) {
                        isExist = true;
                        break;
                    }
                }
                if (!isExist) {
                    isSame = false;
                    break;
                }
            }
            if (!isSame) {
                // 清空旧的节点列表
                redisTemplate.delete(poolId);
                // 添加节点到列表
                nodes.forEach(n -> redisTemplate.opsForList().rightPush(poolId, JSON.toJSONString(n)));
            }
        } else {
            // 清空旧的节点列表
            redisTemplate.delete(poolId);
            // 添加节点到列表
            nodes.forEach(n -> redisTemplate.opsForList().rightPush(poolId, JSON.toJSONString(n)));
        }
    }
}
