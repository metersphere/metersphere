package io.metersphere.commons.constants;

public interface KafkaTopicConstants {
    String PROJECT_DELETED_TOPIC = "PROJECT_DELETED";
    String TEST_PLAN_DELETED_TOPIC = "TEST_PLAN_DELETED";
    String PROJECT_CREATED_TOPIC = "PROJECT_CREATED";
    String CLEAN_UP_REPORT_SCHEDULE = "CLEAN_UP_REPORT_SCHEDULE";
    // 原接口执行结果TOPIC
    String API_REPORT_TOPIC = "ms-api-exec-topic";
    String DEBUG_TOPICS = "MS-API-DEBUG-TOPIC";
    String UI_DEBUG_TOPICS = "MS-UI-DEBUG-TOPIC";
    // 测试计划接收执行结果TOPIC
    String TEST_PLAN_REPORT_TOPIC = "TEST_PLAN_REPORT_TOPIC";
    // 上传插件后，通知各服务从 minio 加载插件, 删除插件后卸载插件
    String PLATFORM_PLUGIN = "PLATFORM_PLUGIN";
}
