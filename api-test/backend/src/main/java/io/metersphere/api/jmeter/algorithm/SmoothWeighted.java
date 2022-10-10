package io.metersphere.api.jmeter.algorithm;


import io.metersphere.commons.utils.GenerateHashTreeUtil;
import io.metersphere.commons.utils.JSON;
import io.metersphere.dto.JvmInfoDTO;
import io.metersphere.dto.NodeDTO;
import io.metersphere.dto.TestResourceDTO;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.vo.BooleanPool;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;

/**
 * 平滑加权轮询算法
 */
public class SmoothWeighted {

    private static final String BASE_URL = "http://%s:%d";
    public static final String CONFIG = "CONFIG_";
    public static final String EXEC_INDEX = "EXEC_INDEX_";

    public static void setServerConfig(String poolId, RedisTemplate client) {
        if (StringUtils.isEmpty(poolId)) {
            return;
        }
        List<JvmInfoDTO> resources = new ArrayList<>();
        BooleanPool pool = GenerateHashTreeUtil.isResourcePool(poolId);
        if (pool.isPool()) {
            resources = GenerateHashTreeUtil.setPoolResource(poolId);
        }
        if (CollectionUtils.isEmpty(resources)) {
            client.delete(CONFIG + poolId);
            client.delete(EXEC_INDEX + poolId);
            return;
        }

        Map<String, ServerConfig> configs = new HashMap<>();
        for (JvmInfoDTO jvmInfoDTO : resources) {
            String configuration = jvmInfoDTO.getTestResource().getConfiguration();
            if (StringUtils.isNotEmpty(configuration)) {
                NodeDTO node = JSON.parseObject(configuration, NodeDTO.class);
                String uri = String.format(BASE_URL + "/jmeter/api/start", node.getIp(), node.getPort());
                configs.put(uri, new ServerConfig(uri, 1, 1, node.getMaxConcurrency(), node.isEnable()));
            }
        }

        if (client.opsForValue().get(CONFIG + poolId) == null) {
            client.opsForValue().set(CONFIG + poolId, new ArrayList<>(configs.values()));
        } else {
            // 合并相同节点信息，更改权重
            List<ServerConfig> serverConfigs = (List<ServerConfig>) client.opsForValue().get(CONFIG + poolId);
            serverConfigs.forEach(item -> {
                if (configs.containsKey(item.getUrl())) {
                    item.setCorePoolSize(configs.get(item.getUrl()).getCorePoolSize());
                    item.setEnable(configs.get(item.getUrl()).isEnable());
                    item.setWeight(configs.get(item.getUrl()).getWeight());
                }
            });
            // 添加新增节点
            configs.forEach((k, v) -> {
                if (!serverConfigs.contains(v)) {
                    serverConfigs.add(v);
                }
            });
            // 异常已经废弃的节点
            serverConfigs.removeIf(item -> !configs.containsKey(item.getUrl()));

            client.opsForValue().set(CONFIG + poolId, serverConfigs);
        }
        if (client.opsForValue().get(EXEC_INDEX + poolId) == null) {
            client.opsForValue().set(EXEC_INDEX + poolId, 1);
        }
    }

    public static ServerConfig calculate(String poolId, RedisTemplate client) {
        if (client.opsForValue().get(EXEC_INDEX + poolId) == null) {
            client.opsForValue().set(EXEC_INDEX + poolId, 1);
        }
        List<ServerConfig> serverList = new ArrayList<>();
        if (client.opsForValue().get(CONFIG + poolId) != null) {
            serverList = (List<ServerConfig>) client.opsForValue().get(CONFIG + poolId);
        }
        // 最大权重
        int weightSum = serverList.stream().map(ServerConfig::getWeight).reduce((x, y) -> x += y).get();

        long execIndex = Long.parseLong(client.opsForValue().get(EXEC_INDEX + poolId).toString());
        // 选出当前有效权重最大的实例，将当前有效权重currentWeight减去所有实例的"权重和"（weightSum），且变量tmpSv指向此位置；
        ServerConfig max = Collections.max(serverList, Comparator.comparingLong(ServerConfig::getCurrentWeight));
        // 选中的实例
        ServerConfig tmpConfig = null;
        for (ServerConfig serverConfig : serverList) {
            if (max.equals(serverConfig)) {
                serverConfig.setCurrentWeight(serverConfig.getCurrentWeight() - weightSum);
                if (tmpConfig == null || serverConfig.getCurrentWeight() > tmpConfig.getCurrentWeight()) {
                    tmpConfig = serverConfig;
                }
            }
            //将每个实例的当前有效权重currentWeight都加上配置权重weight；
            serverConfig.setCurrentWeight(serverConfig.getCurrentWeight() + serverConfig.getWeight());
        }

        // 选中前的当前权重
        LoggerUtil.info("第" + (execIndex) + "次选中前的当前权重:" + JSON.toJSONString(serverList));

        if (client.opsForValue().get(CONFIG + poolId) != null) {
            client.opsForValue().set(CONFIG + poolId, serverList);
        }
        return tmpConfig;
    }

    public static ServerConfig getResource(String poolId) {
        BooleanPool pool = GenerateHashTreeUtil.isResourcePool(poolId);
        if (pool.isPool()) {
            List<JvmInfoDTO> resources = GenerateHashTreeUtil.setPoolResource(poolId);
            if (CollectionUtils.isNotEmpty(resources)) {
                int index = (int) (Math.random() * resources.size());
                TestResourceDTO testResource = resources.get(index).getTestResource();
                NodeDTO node = JSON.parseObject(testResource.getConfiguration(), NodeDTO.class);
                String nodeIp = node.getIp();
                Integer port = node.getPort();
                String uri = String.format(BASE_URL + "/jmeter/api/start", nodeIp, port);
                return new ServerConfig(uri, 1, 1, node.getMaxConcurrency(), node.isEnable());
            }
        }
        return null;
    }
}