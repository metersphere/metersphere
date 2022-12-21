package io.metersphere.task.controller;

import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.task.dto.TaskCenterDTO;
import io.metersphere.task.dto.TaskCenterRequest;
import io.metersphere.task.dto.TaskStatisticsDTO;
import io.metersphere.task.service.TaskService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/task/center")
public class TaskController {
    @Resource
    private TaskService taskService;

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<TaskCenterDTO>> getTasks(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody TaskCenterRequest request) {
        request.setProjects(taskService.getOwnerProjectIds(request.getUserId()));
        request.setStartTime(DateUtils.getDailyStartTime());
        request.setEndTime(DateUtils.getDailyEndTime());
        request.setGoPage(goPage);
        request.setPageSize(pageSize);
        return taskService.getTasks(request);
    }

    @GetMapping("/case/{id}")
    public List<TaskCenterDTO> getCase(@PathVariable String id) {
        return taskService.getCases(id);
    }

    @GetMapping("/scenario/{id}")
    public List<TaskCenterDTO> getScenario(@PathVariable String id) {
        return taskService.getScenario(id);
    }

    @PostMapping("/count/running")
    public TaskStatisticsDTO getRunningTasks(@RequestBody TaskCenterRequest request) {
        return taskService.getRunningTasks(request);
    }

}
