package io.metersphere.system.dto.request;

import io.metersphere.sdk.constants.ScheduleType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.Schedule;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ScheduleConfig {

    private String resourceId;

    private String key;

    private String projectId;

    private String name;

    private Boolean enable;

    private String cron;

    private String resourceType;

    Map<String, Object> configMap;

    public Schedule genCronSchedule(Schedule schedule) {
        if (schedule == null) {
            schedule = new Schedule();
        }
        schedule.setName(this.getName());
        schedule.setResourceId(this.getResourceId());
        schedule.setType(ScheduleType.CRON.name());
        schedule.setKey(this.getKey());
        schedule.setEnable(this.getEnable());
        schedule.setProjectId(this.getProjectId());
        schedule.setValue(this.getCron());
        schedule.setResourceType(this.getResourceType());
        //配置数据为null，意味着不更改； 如果要清空配置，需要传入空对象
        if (configMap != null) {
            schedule.setConfig(JSON.toJSONString(configMap));
        }
        return schedule;
    }
}
