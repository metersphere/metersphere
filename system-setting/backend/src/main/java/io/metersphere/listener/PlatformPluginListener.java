package io.metersphere.listener;


import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.service.PlatformPluginService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Component
public class PlatformPluginListener {
    public static final String TEST_TRACK_PLATFORM_PLUGIN = "test_track_platform_plugin";

    @Resource
    private PlatformPluginService platformPluginService;

    // groupId 必须是每个实例唯一
    @KafkaListener(id = TEST_TRACK_PLATFORM_PLUGIN, topics = KafkaTopicConstants.PLATFORM_PLUGIN, groupId = TEST_TRACK_PLATFORM_PLUGIN + "_" + "${random.uuid}")
    public void handlePluginChange(ConsumerRecord<?, String> record) {
        LogUtil.info("track service consume platform_plugin add message, plugin id: " + record);
        String[] info = record.value().split(":");
        String operate = info[0];
        String pluginId = info[1];
        switch (operate) {
            case "ADD":
                platformPluginService.loadPlugin(pluginId);
                break;
            case "DELETE":
                platformPluginService.unload(pluginId);
                break;
            default:
                break;
        }
    }
}
