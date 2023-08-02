package io.metersphere.system.listener;


import io.metersphere.sdk.constants.KafkaPluginTopicType;
import io.metersphere.sdk.constants.KafkaTopicConstants;
import io.metersphere.sdk.service.PluginLoadService;
import io.metersphere.sdk.util.LogUtils;
import jakarta.annotation.Resource;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PluginListener {

    public static final String PLUGIN_CONSUMER = "plugin_consumer";

    @Resource
    private PluginLoadService pluginLoadService;

    // groupId 必须是每个实例唯一
    @KafkaListener(id = PLUGIN_CONSUMER, topics = KafkaTopicConstants.PLUGIN, groupId = PLUGIN_CONSUMER + "_" + "${random.uuid}")
    public void handlePluginChange(ConsumerRecord<?, String> record) {
        LogUtils.info("Service consume platform_plugin message: " + record);
        String[] info = record.value().split(":");
        String operate = info[0];
        String pluginId = info[1];
        switch (operate) {
            case KafkaPluginTopicType.ADD:
                String pluginName = info[2];
                pluginLoadService.loadPlugin(pluginId, pluginName);
                break;
            case KafkaPluginTopicType.DELETE:
                pluginLoadService.unloadPlugin(pluginId);
                break;
            default:
                break;
        }
    }
}
