package io.metersphere.api.jmeter.utils;

import io.metersphere.base.domain.TestResource;
import io.metersphere.commons.utils.GenerateHashTreeUtil;
import io.metersphere.commons.utils.JSON;
import io.metersphere.dto.NodeDTO;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.vo.BooleanPool;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 平滑加权轮询算法
 */
public class SmoothWeighted {

    public static final String BASE_URL = "http://%s:%d";
    public static final String CONFIG = "CONFIG_";
    public static final String EXEC_INDEX = "EXEC_INDEX_";

    public static void setServerConfig(String poolId, RedisTemplate<String, Object> client) {
        if (StringUtils.isEmpty(poolId)) {
            return;
        }

        List<TestResource> resources = new ArrayList<>();
        BooleanPool pool = GenerateHashTreeUtil.isResourcePool(poolId);
        if (pool.isPool()) {
            resources = GenerateHashTreeUtil.setPoolResource(poolId);
        }

        if (CollectionUtils.isEmpty(resources)) {
            client.delete(CONFIG + poolId);
            client.delete(EXEC_INDEX + poolId);
            return;
        }

        Map<String, ServerConfig> configs = resources.stream()
                .map(testResource -> {
                    String configuration = testResource.getConfiguration();
                    if (StringUtils.isNotEmpty(configuration)) {
                        NodeDTO node = JSON.parseObject(configuration, NodeDTO.class);
                        String uri = String.format(BASE_URL + "/jmeter/api/start", node.getIp(), node.getPort());
                        return new ServerConfig(uri, 1, 1, node.getMaxConcurrency(), node.isEnable());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ServerConfig::getUrl, serverConfig -> serverConfig));

        List<ServerConfig> existingConfigs = (List<ServerConfig>) client.opsForValue().get(CONFIG + poolId);
        if (existingConfigs == null) {
            client.opsForValue().set(CONFIG + poolId, new ArrayList<>(configs.values()));
        } else {
            Map<String, ServerConfig> existingConfigMap = existingConfigs.stream()
                    .collect(Collectors.toMap(ServerConfig::getUrl, serverConfig -> serverConfig));

            // 合并相同节点信息，更改权重
            existingConfigMap.forEach((url, existingConfig) -> {
                if (configs.containsKey(url)) {
                    ServerConfig newConfig = configs.get(url);
                    existingConfig.setCorePoolSize(newConfig.getCorePoolSize());
                    existingConfig.setEnable(newConfig.isEnable());
                    existingConfig.setWeight(newConfig.getWeight());
                }
            });

            // 添加新增节点
            existingConfigs.addAll(configs.values().stream()
                    .filter(config -> !existingConfigMap.containsKey(config.getUrl()))
                    .toList());

            // 移除废弃的节点
            existingConfigs.removeIf(config -> !configs.containsKey(config.getUrl()));

            client.opsForValue().set(CONFIG + poolId, existingConfigs);
        }

        if (client.opsForValue().get(EXEC_INDEX + poolId) == null) {
            client.opsForValue().set(EXEC_INDEX + poolId, 1);
        }
    }

    public static ServerConfig calculate(String poolId, RedisTemplate<String, Object> client) {
        if (client.opsForValue().get(EXEC_INDEX + poolId) == null) {
            client.opsForValue().set(EXEC_INDEX + poolId, 1);
        }

        List<ServerConfig> serverList = Optional.ofNullable((List<ServerConfig>) client.opsForValue().get(CONFIG + poolId))
                .orElse(new ArrayList<>());

        // 最大权重
        int weightSum = serverList.stream()
                .mapToInt(ServerConfig::getWeight)
                .sum();

        long execIndex = Long.parseLong(Objects.requireNonNull(client.opsForValue().get(EXEC_INDEX + poolId)).toString());

        // 选出当前有效权重最大的实例
        ServerConfig max = serverList.stream()
                .max(Comparator.comparingLong(ServerConfig::getCurrentWeight))
                .orElse(null);

        // 选中的实例
        ServerConfig tmpConfig = null;
        for (ServerConfig serverConfig : serverList) {
            if (serverConfig.equals(max)) {
                serverConfig.setCurrentWeight(serverConfig.getCurrentWeight() - weightSum);
                if (tmpConfig == null || serverConfig.getCurrentWeight() > tmpConfig.getCurrentWeight()) {
                    tmpConfig = serverConfig;
                }
            }
            serverConfig.setCurrentWeight(serverConfig.getCurrentWeight() + serverConfig.getWeight());
        }

        LoggerUtil.info(String.format("第%d次选中前的当前权重:%s", execIndex, JSON.toJSONString(serverList)));

        client.opsForValue().set(CONFIG + poolId, serverList);
        return tmpConfig;
    }

    public static ServerConfig getResource(String poolId) {
        BooleanPool pool = GenerateHashTreeUtil.isResourcePool(poolId);
        if (pool.isPool()) {
            List<TestResource> resources = GenerateHashTreeUtil.setPoolResource(poolId);
            if (CollectionUtils.isNotEmpty(resources)) {
                int index = new Random().nextInt(resources.size());
                TestResource testResource = resources.get(index);
                NodeDTO node = JSON.parseObject(testResource.getConfiguration(), NodeDTO.class);
                String uri = String.format(BASE_URL + "/jmeter/api/start", node.getIp(), node.getPort());
                return new ServerConfig(uri, 1, 1, node.getMaxConcurrency(), node.isEnable());
            }
        }
        return null;
    }
}
