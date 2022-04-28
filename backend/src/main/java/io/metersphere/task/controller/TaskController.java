package io.metersphere.task.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.task.dto.TaskCenterDTO;
import io.metersphere.task.dto.TaskCenterRequest;
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
        request.setGoPage(goPage);
        request.setPageSize(pageSize);
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, taskService.getTasks(request));
    }

    @GetMapping("/case/{goPage}/{pageSize}/{id}")
    public Pager<List<TaskCenterDTO>> getCase(@PathVariable int goPage, @PathVariable int pageSize, @PathVariable String id) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, taskService.getCases(id));
    }

    @GetMapping("/scenario/{goPage}/{pageSize}/{id}")
    public Pager<List<TaskCenterDTO>> getScenario(@PathVariable int goPage, @PathVariable int pageSize, @PathVariable String id) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, taskService.getScenario(id));
    }

    @PostMapping("/count/running")
    public int getRunningTasks(@RequestBody TaskCenterRequest request) {
        return taskService.getRunningTasks(request);
    }

}
