package io.metersphere.task.service;

import io.metersphere.base.mapper.ext.ExtTaskMapper;
import io.metersphere.task.dto.TaskCenterDTO;
import io.metersphere.task.dto.TaskCenterRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TaskService {
    @Resource
    private ExtTaskMapper extTaskMapper;

    public List<TaskCenterDTO> getTasks(TaskCenterRequest request) {
        if (StringUtils.isEmpty(request.getProjectId())) {
            return new ArrayList<>();
        }
        return extTaskMapper.getTasks(request);
    }

    public int getRunningTasks(TaskCenterRequest request) {
        if (StringUtils.isEmpty(request.getProjectId())) {
            return 0;
        }
        return extTaskMapper.getRunningTasks(request);
    }

}
