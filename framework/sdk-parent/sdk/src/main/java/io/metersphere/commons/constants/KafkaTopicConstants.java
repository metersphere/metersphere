package io.metersphere.commons.constants;

public interface KafkaTopicConstants {
    String PROJECT_DELETED_TOPIC = "PROJECT_DELETED";
    String TEST_PLAN_DELETED_TOPIC = "TEST_PLAN_DELETED";
    String PROJECT_CREATED_TOPIC = "PROJECT_CREATED";
    String CLEAN_UP_REPORT_SCHEDULE = "CLEAN_UP_REPORT_SCHEDULE";
    // 原接口执行结果TOPIC
    String API_REPORT_TOPIC = "ms-api-exec-topic";
    String DEBUG_TOPICS = "MS-API-DEBUG-TOPIC";
    // 测试计划接收执行结果TOPIC
    String TEST_PLAN_REPORT_TOPIC = "TEST_PLAN_REPORT_TOPIC";
    // 保存当前站点时检查MOCK环境
    String CHECK_MOCK_ENV_TOPIC = "CHECK_MOCK_ENV_TOPIC";

}
