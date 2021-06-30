package io.metersphere.task.service;

import io.metersphere.base.mapper.ext.ExtTaskMapper;
import io.metersphere.task.dto.TaskCenterDTO;
import io.metersphere.task.dto.TaskCenterRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TaskService {
    @Resource
    private ExtTaskMapper extTaskMapper;

    public List<TaskCenterDTO> getTasks(TaskCenterRequest request) {
        return extTaskMapper.getTasks(request);
    }

    public List<TaskCenterDTO> getRunningTasks(TaskCenterRequest request) {
        return extTaskMapper.getRunningTasks(request);
    }

}
