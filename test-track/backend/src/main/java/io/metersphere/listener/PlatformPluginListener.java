package io.metersphere.listener;

import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.service.PlatformPluginService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class PlatformPluginListener {
    public static final String ADD_CONSUME_ID = "test_track_platform_plugin_add";
    public static final String DELETE_CONSUME_ID = "test_track_platform_plugin_delete";

    @Resource
    private PlatformPluginService platformPluginService;

    @KafkaListener(id = ADD_CONSUME_ID, topics = KafkaTopicConstants.PLATFORM_PLUGIN_ADD, groupId = "${spring.application.name}")
    public void handlePluginAdd(ConsumerRecord<?, String> record) {
        String pluginId = record.value();
        LogUtil.info("track service consume platform_plugin add message, plugin id: " + pluginId);
        platformPluginService.loadPlugin(pluginId);
    }

    @KafkaListener(id = DELETE_CONSUME_ID, topics = KafkaTopicConstants.PLATFORM_PLUGIN_DELETED, groupId = "${spring.application.name}")
    public void handlePluginDelete(ConsumerRecord<?, String> record) {
        String pluginId = record.value();
        LogUtil.info("track service consume platform_plugin delete message, plugin id: " + pluginId);
        platformPluginService.unloadPlugin(pluginId);
    }
}
