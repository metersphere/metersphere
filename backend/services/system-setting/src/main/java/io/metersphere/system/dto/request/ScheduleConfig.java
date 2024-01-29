package io.metersphere.system.dto.request;

import io.metersphere.sdk.constants.ScheduleType;
import io.metersphere.system.domain.Schedule;
import lombok.Builder;
import lombok.Data;

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

    private String config;

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
        schedule.setConfig(this.getConfig());
        return schedule;
    }
}
