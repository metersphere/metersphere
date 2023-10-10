package io.metersphere.system.listener;


import io.metersphere.sdk.constants.KafkaTopicConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.dto.PluginNotifiedDTO;
import io.metersphere.system.service.PluginLoadService;
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
        LogUtils.info("Service consume platform_plugin message: " + record.value());
        PluginNotifiedDTO pluginNotifiedDTO = JSON.parseObject(record.value(), PluginNotifiedDTO.class);
        String operate = pluginNotifiedDTO.getOperate();
        String pluginId = pluginNotifiedDTO.getPluginId();
        String fileName = pluginNotifiedDTO.getFileName();
        switch (operate) {
            case KafkaTopicConstants.TYPE.ADD:
                pluginLoadService.handlePluginAddNotified(pluginId, fileName);
                break;
            case KafkaTopicConstants.TYPE.DELETE:
                pluginLoadService.handlePluginDeleteNotified(pluginId, fileName);
                break;
            default:
                break;
        }
    }
}
