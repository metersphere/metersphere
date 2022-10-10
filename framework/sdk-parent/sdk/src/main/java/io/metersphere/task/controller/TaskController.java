package io.metersphere.task.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.commons.utils.CronUtils;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.TaskInfoResult;
import io.metersphere.request.BaseQueryRequest;
import io.metersphere.task.dto.TaskCenterDTO;
import io.metersphere.task.dto.TaskCenterRequest;
import io.metersphere.task.dto.TaskRequestDTO;
import io.metersphere.task.service.TaskService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
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

    @GetMapping("/case/{id}")
    public List<TaskCenterDTO> getCase(@PathVariable String id) {
        return taskService.getCases(id);
    }

    @GetMapping("/scenario/{id}")
    public List<TaskCenterDTO> getScenario(@PathVariable String id) {
        return taskService.getScenario(id);
    }

    @PostMapping("/count/running")
    public int getRunningTasks(@RequestBody TaskCenterRequest request) {
        return taskService.getRunningTasks(request);
    }

    @PostMapping("/runningTask/{projectID}")
    public List<TaskInfoResult> runningTask(@PathVariable String projectID, @RequestBody BaseQueryRequest request) {
        List<TaskInfoResult> resultList = taskService.findRunningTaskInfoByProjectID(projectID, request);
        int dataIndex = 1;
        for (TaskInfoResult taskInfo :
                resultList) {
            taskInfo.setIndex(dataIndex++);
            Date nextExecutionTime = CronUtils.getNextTriggerTime(taskInfo.getRule());
            if (nextExecutionTime != null) {
                taskInfo.setNextExecutionTime(nextExecutionTime.getTime());
            }
        }
        return resultList;
    }

    @PostMapping(value = "/stop/batch")
    public void stopBatch(@RequestBody List<TaskRequestDTO> reportIds) {
        taskService.stop(reportIds);
    }

    @PostMapping(value = "/stop/perf")
    public void stopPerf(@RequestBody List<TaskRequestDTO> reportIds) {
        taskService.stopPerf(reportIds);
    }

    @PostMapping(value = "/stop/api")
    public void stopApi(@RequestBody List<TaskRequestDTO> reportIds) {
        taskService.stopApi(reportIds);
    }
}
