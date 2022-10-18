package io.metersphere.task.service;

import io.metersphere.base.mapper.ext.BaseScheduleMapper;
import io.metersphere.base.mapper.ext.BaseTaskMapper;
import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.dto.TaskInfoResult;
import io.metersphere.request.BaseQueryRequest;
import io.metersphere.service.BaseCheckPermissionService;
import io.metersphere.service.MicroService;
import io.metersphere.task.dto.TaskCenterDTO;
import io.metersphere.task.dto.TaskCenterRequest;
import io.metersphere.task.dto.TaskRequestDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TaskService {
    @Resource
    private BaseTaskMapper baseTaskMapper;
    @Resource
    private BaseScheduleMapper baseScheduleMapper;
    @Resource
    private BaseCheckPermissionService baseCheckPermissionService;
    @Resource
    private MicroService microService;
    private static final String API = "API";
    private static final String SCENARIO = "SCENARIO";
    private static final String PERF = "PERFORMANCE";

    private static final String UI = "UI_SCENARIO";

    public List<String> getOwnerProjectIds(String userId) {
        Set<String> userRelatedProjectIds = null;
        if (StringUtils.isEmpty(userId)) {
            userRelatedProjectIds = baseCheckPermissionService.getUserRelatedProjectIds();
        } else {
            userRelatedProjectIds = baseCheckPermissionService.getOwnerByUserId(userId);
        }
        if (CollectionUtils.isEmpty(userRelatedProjectIds)) {
            return new ArrayList<>(0);
        }
        return new ArrayList<>(userRelatedProjectIds);
    }

    public List<TaskCenterDTO> getTasks(TaskCenterRequest request) {
        if (CollectionUtils.isEmpty(request.getProjects())) {
            return new ArrayList<>();
        }
        return baseTaskMapper.getTasks(request);
    }

    public int getRunningTasks(TaskCenterRequest request) {
        request.setProjects(this.getOwnerProjectIds(request.getUserId()));
        if (CollectionUtils.isEmpty(request.getProjects())) {
            return 0;
        }
        return baseTaskMapper.getRunningTasks(request);
    }

    public List<TaskCenterDTO> getCases(String id) {
        return baseTaskMapper.getCases(id);
    }

    public List<TaskCenterDTO> getScenario(String id) {
        return baseTaskMapper.getScenario(id);
    }


    public List<TaskInfoResult> findRunningTaskInfoByProjectID(String projectID, BaseQueryRequest request) {
        List<TaskInfoResult> runningTaskInfoList = baseScheduleMapper.findRunningTaskInfoByProjectID(projectID, request);
        return runningTaskInfoList;
    }

    public void stop(List<TaskRequestDTO> reportIds) {
        if (CollectionUtils.isNotEmpty(reportIds)) {
            // 任务中心单条停止/全部停止
            Map<String, TaskRequestDTO> taskRequestMap = reportIds.stream().collect(Collectors.toMap(TaskRequestDTO::getType, taskRequest -> taskRequest));
            if (taskRequestMap.containsKey(API) || taskRequestMap.containsKey(SCENARIO)) {
                microService.postForData(MicroServiceName.API_TEST, "/api/automation/stop/batch", reportIds);
            }
            if (taskRequestMap.containsKey(PERF)) {
                microService.postForData(MicroServiceName.PERFORMANCE_TEST, "/performance/stop/batch", taskRequestMap.get(PERF));
            }
            if(taskRequestMap.containsKey(UI)){
                microService.postForData(MicroServiceName.UI_TEST, "/ui/automation/stop/batch", reportIds);
            }
        }
    }

    public void stopPerf(List<TaskRequestDTO> reportIds) {
        if (CollectionUtils.isNotEmpty(reportIds)) {
            Map<String, TaskRequestDTO> taskRequestMap = reportIds.stream().collect(Collectors.toMap(TaskRequestDTO::getType, taskRequest -> taskRequest));
            microService.postForData(MicroServiceName.PERFORMANCE_TEST, "/performance/stop/batch", taskRequestMap.get(PERF));
        }
    }

    public void stopApi(List<TaskRequestDTO> reportIds) {
        Map<String, TaskRequestDTO> taskRequestMap = reportIds.stream().collect(Collectors.toMap(TaskRequestDTO::getType, taskRequest -> taskRequest));
        if (taskRequestMap.containsKey(API) || taskRequestMap.containsKey(SCENARIO)) {
            microService.postForData(MicroServiceName.API_TEST, "/api/automation/stop/batch", reportIds);
        }
    }
}
