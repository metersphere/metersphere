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

    // groupId 必须是每个实例唯一
    @KafkaListener(id = ADD_CONSUME_ID, topics = KafkaTopicConstants.PLATFORM_PLUGIN_ADD, groupId = "${eureka.instance.instance-id}")
    public void handlePluginAdd(ConsumerRecord<?, String> record) {
        String pluginId = record.value();
        LogUtil.info("track service consume platform_plugin add message, plugin id: " + pluginId);
        platformPluginService.loadPlugin(pluginId);
    }

    @KafkaListener(id = DELETE_CONSUME_ID, topics = KafkaTopicConstants.PLATFORM_PLUGIN_DELETED, groupId = "${eureka.instance.instance-id}")
    public void handlePluginDelete(ConsumerRecord<?, String> record) {
        String pluginId = record.value();
        LogUtil.info("track service consume platform_plugin delete message, plugin id: " + pluginId);
        platformPluginService.unloadPlugin(pluginId);
    }
}
