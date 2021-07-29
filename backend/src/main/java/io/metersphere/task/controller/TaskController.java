package io.metersphere.task.controller;

import io.metersphere.task.dto.TaskCenterDTO;
import io.metersphere.task.dto.TaskCenterRequest;
import io.metersphere.task.service.TaskService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/task/center")
public class TaskController {
    @Resource
    private TaskService taskService;

    @PostMapping("/list")
    @RequiresPermissions("PROJECT_API_SCENARIO:READ")
    public List<TaskCenterDTO> getTasks(@RequestBody TaskCenterRequest request) {
        return taskService.getTasks(request);
    }

    @GetMapping("/case/{id}")
    @RequiresPermissions("PROJECT_API_SCENARIO:READ")
    public List<TaskCenterDTO> getCase(@PathVariable String id) {
        return taskService.getCases(id);
    }

    @GetMapping("/scenario/{id}")
    @RequiresPermissions("PROJECT_API_SCENARIO:READ")
    public List<TaskCenterDTO> getScenario(@PathVariable String id) {
        return taskService.getScenario(id);
    }

    @PostMapping("/count/running")
    @RequiresPermissions("PROJECT_API_SCENARIO:READ")
    public int getRunningTasks(@RequestBody TaskCenterRequest request) {
        return taskService.getRunningTasks(request);
    }

}
