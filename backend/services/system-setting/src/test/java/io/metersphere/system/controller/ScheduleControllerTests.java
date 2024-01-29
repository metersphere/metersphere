package io.metersphere.system.controller;

import io.metersphere.sdk.constants.ScheduleResourceType;
import io.metersphere.sdk.constants.ScheduleType;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.dto.request.ScheduleConfig;
import io.metersphere.system.job.ApiScenarioScheduleJob;
import io.metersphere.system.schedule.ScheduleService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ScheduleControllerTests extends BaseTest {

    @Resource
    private ScheduleService scheduleService;

    @Test
    public void test() {
        Schedule schedule = new Schedule();
        schedule.setName("test-schedule");
        schedule.setResourceId("test-resource-id");
        schedule.setEnable(true);
        schedule.setValue("0 0/1 * * * ?");
        schedule.setKey("test-resource-id");
        schedule.setCreateUser("admin");
        schedule.setProjectId(DEFAULT_PROJECT_ID);
        schedule.setConfig("{}");
        schedule.setJob(ApiScenarioScheduleJob.class.getName());
        schedule.setType(ScheduleType.CRON.name());
        schedule.setResourceType(ScheduleResourceType.API_IMPORT.name());

        scheduleService.addSchedule(schedule);
        scheduleService.getSchedule(schedule.getId());
        scheduleService.editSchedule(schedule);
        scheduleService.getScheduleByResource(schedule.getResourceId(), schedule.getJob());
        scheduleService.deleteByResourceId(schedule.getResourceId(), schedule.getJob());
        schedule = new Schedule();
        schedule.setName("test-schedule-1");
        schedule.setResourceId("test-resource-id-1");
        schedule.setEnable(true);
        schedule.setValue("0 0/1 * * * ?");
        schedule.setKey("test-resource-id-1");
        schedule.setCreateUser("admin");
        schedule.setProjectId(DEFAULT_PROJECT_ID);
        schedule.setConfig("{}");
        schedule.setJob(ApiScenarioScheduleJob.class.getName());
        schedule.setType(ScheduleType.CRON.name());
        schedule.setResourceType(ScheduleResourceType.API_SCENARIO.name());
        scheduleService.addSchedule(schedule);
        scheduleService.deleteByResourceIds(List.of(schedule.getResourceId()), schedule.getJob());
        schedule = new Schedule();
        schedule.setName("test-schedule-2");
        schedule.setResourceId("test-resource-id-2");
        schedule.setEnable(true);
        schedule.setValue("0 0/1 * * * ?");
        schedule.setKey("test-resource-id-2");
        schedule.setCreateUser("admin");
        schedule.setProjectId(DEFAULT_PROJECT_ID);
        schedule.setConfig("{}");
        schedule.setJob("test-job");
        schedule.setType(ScheduleType.CRON.name());
        schedule.setResourceType(ScheduleResourceType.API_SCENARIO.name());
        scheduleService.addSchedule(schedule);
        scheduleService.addOrUpdateCronJob(schedule,
                new JobKey(schedule.getResourceId(), ApiScenarioScheduleJob.class.getName()),
                new TriggerKey(schedule.getResourceId(), ApiScenarioScheduleJob.class.getName()),
                ApiScenarioScheduleJob.class);
        scheduleService.deleteByProjectId(schedule.getProjectId());

        ScheduleConfig scheduleConfig = ScheduleConfig.builder()
                .resourceId("test-resource-id-3")
                .key("test-resource-id-3")
                .projectId(DEFAULT_PROJECT_ID)
                .name("test-schedule-3")
                .enable(true)
                .cron("0 0/1 * * * ?")
                .resourceType(ScheduleResourceType.API_SCENARIO.name())
                .configMap(new HashMap<>() {{
                    this.put("envId", "testEnv");
                    this.put("resourcePoolId", "testResourcePool");
                }})
                .build();

        scheduleService.scheduleConfig(
                scheduleConfig,
                new JobKey(scheduleConfig.getResourceId(), ApiScenarioScheduleJob.class.getName()),
                new TriggerKey(scheduleConfig.getResourceId(), ApiScenarioScheduleJob.class.getName()),
                ApiScenarioScheduleJob.class,
                "admin");


    }
}
