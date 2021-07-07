package io.metersphere.task.controller;

import io.metersphere.task.dto.TaskCenterDTO;
import io.metersphere.task.dto.TaskCenterRequest;
import io.metersphere.task.service.TaskService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/count/running")
    @RequiresPermissions("PROJECT_API_SCENARIO:READ")
    public int getRunningTasks(@RequestBody TaskCenterRequest request) {
        return taskService.getRunningTasks(request).size();
    }

}
