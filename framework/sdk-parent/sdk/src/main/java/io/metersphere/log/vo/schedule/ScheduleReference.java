package io.metersphere.log.vo.schedule;

import java.util.LinkedHashMap;
import java.util.Map;

public class ScheduleReference {
    public static Map<String, String> scheduleColumns = new LinkedHashMap<>();

    static {
        scheduleColumns.clear();
        scheduleColumns.put("name", "定时任务名称");
        scheduleColumns.put("type", "定时任务类型");
        scheduleColumns.put("value", "cron表达式");
        scheduleColumns.put("group", "用例类型");
        scheduleColumns.put("enable", "定时任务状态");
        scheduleColumns.put("userId", "创建人");
        scheduleColumns.put("config", "定时任务配置");

        // 需要深度对比的字段，可以支持多个req1,req2
        scheduleColumns.put("ms-dff-col", "config");

    }
}