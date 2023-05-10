package io.metersphere.controller;

import io.metersphere.api.dto.datacount.request.ScheduleInfoRequest;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.service.scenario.ApiScenarioService;
import io.metersphere.base.domain.Schedule;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.notice.annotation.SendNotice;
import io.metersphere.service.BaseScheduleService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
public class ScheduleController {
    @Resource
    private BaseScheduleService baseScheduleService;
    @Resource
    private ApiScenarioService apiAutomationService;

    @PostMapping(value = "/api/schedule/enable")
    @SendNotice(taskType = NoticeConstants.TaskType.API_HOME_TASK, event = NoticeConstants.Event.CLOSE_SCHEDULE, subject = "接口测试通知")
    public Schedule disableSchedule(@RequestBody ScheduleInfoRequest request) {
        Schedule schedule = baseScheduleService.getSchedule(request.getTaskID());
        schedule.setEnable(false);
        apiAutomationService.updateSchedule(schedule);
        return schedule;
    }

    @GetMapping("/api/schedule/get/{testId}/{group}")
    public Schedule schedule(@PathVariable String testId, @PathVariable String group) {
        Schedule schedule = baseScheduleService.getScheduleByResource(testId, group);
        return schedule;
    }

    @PostMapping(value = "/api/schedule/update")
    @MsRequestLog(module = OperLogModule.API_AUTOMATION)
    public Schedule update(@RequestBody ScheduleInfoRequest request) {
        Schedule schedule = baseScheduleService.getSchedule(request.getTaskID());
        schedule.setEnable(request.isEnable());
        apiAutomationService.updateSchedule(schedule);
        return schedule;
    }
}
