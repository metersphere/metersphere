package io.metersphere.task.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.mapper.ext.BaseInformationSchemaTableMapper;
import io.metersphere.base.mapper.ext.BaseScheduleMapper;
import io.metersphere.base.mapper.ext.BaseTaskMapper;
import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.commons.constants.TaskCenterType;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.dto.TaskInfoResult;
import io.metersphere.request.BaseQueryRequest;
import io.metersphere.service.BaseCheckPermissionService;
import io.metersphere.service.MicroService;
import io.metersphere.task.dto.TaskCenterDTO;
import io.metersphere.task.dto.TaskCenterRequest;
import io.metersphere.task.dto.TaskRequestDTO;
import io.metersphere.task.dto.TaskStatisticsDTO;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private BaseInformationSchemaTableMapper baseInformationSchemaTableMapper;
    @Resource
    private BaseCheckPermissionService baseCheckPermissionService;
    @Resource
    private MicroService microService;
    private static final String API = "API";
    private static final String SCENARIO = "SCENARIO";
    private static final String PERF = "PERFORMANCE";

    private static final String UI = "UI_SCENARIO";
    private static final String UI_SCENARIO_REPORT = "ui_scenario_report";

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

    public Pager<List<TaskCenterDTO>> getTasks(TaskCenterRequest request) {
        Page<Object> page = PageHelper.startPage(request.getGoPage(), request.getPageSize(), true);
        if (StringUtils.equals(request.getActiveName(), TaskCenterType.UI.name())) {
            return PageUtils.setPageInfo(page, getUiTasks(request));
        } else if (StringUtils.equals(request.getActiveName(), TaskCenterType.SCENARIO.name())) {
            return PageUtils.setPageInfo(page, getScenarioTasks(request));
        } else if (StringUtils.equals(request.getActiveName(), TaskCenterType.PERF.name())) {
            return PageUtils.setPageInfo(page, getPerfTasks(request));
        } else {
            return PageUtils.setPageInfo(page, getApiTasks(request));
        }
    }

    public List<TaskCenterDTO> getApiTasks(TaskCenterRequest request) {
        if (CollectionUtils.isEmpty(request.getProjects())) {
            return new ArrayList<>();
        }
        return baseTaskMapper.getApiTasks(request);
    }

    public List<TaskCenterDTO> getUiTasks(TaskCenterRequest request) {
        if (CollectionUtils.isEmpty(request.getProjects())) {
            return new ArrayList<>();
        }
        return baseTaskMapper.getUiTasks(request);
    }

    public List<TaskCenterDTO> getPerfTasks(TaskCenterRequest request) {
        if (CollectionUtils.isEmpty(request.getProjects())) {
            return new ArrayList<>();
        }
        return baseTaskMapper.getPerfTasks(request);
    }

    public List<TaskCenterDTO> getScenarioTasks(TaskCenterRequest request) {
        if (CollectionUtils.isEmpty(request.getProjects())) {
            return new ArrayList<>();
        }
        return baseTaskMapper.getScenarioTasks(request);
    }

    public TaskStatisticsDTO getRunningTasks(TaskCenterRequest request) {
        request.setProjects(this.getOwnerProjectIds(request.getUserId()));
        request.setStartTime(DateUtils.getDailyStartTime());
        request.setEndTime(DateUtils.getDailyEndTime());
        if (CollectionUtils.isEmpty(request.getProjects())) {
            return new TaskStatisticsDTO();
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
                try {
                    microService.postForData(MicroServiceName.API_TEST, "/api/automation/stop/batch", reportIds);
                } catch (Exception e) {
                    LogUtil.error("接口测试批量关闭失败!", e);
                }
            }
            if (taskRequestMap.containsKey(PERF)) {
                try {
                    microService.postForData(MicroServiceName.PERFORMANCE_TEST, "/performance/stop/batch", taskRequestMap.get(PERF));
                } catch (Exception e) {
                    LogUtil.error("性能测试批量关闭失败!", e);
                }
            }
            if (taskRequestMap.containsKey(UI)) {
                try {
                    microService.postForData(MicroServiceName.UI_TEST, "/ui/automation/stop/batch", reportIds);
                } catch (Exception e) {
                    LogUtil.error("ui测试批量关闭失败!", e);
                }
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

    private boolean checkUiPermission() {
        try {
            String uiScenarioReport = baseInformationSchemaTableMapper.checkExist(UI_SCENARIO_REPORT);
            if (StringUtils.isNotEmpty(uiScenarioReport)) {
                return true;
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return false;
    }

    public List<TaskInfoResult> findScenarioAndSwaggerRunningTaskInfoByProjectID(String projectId, String versionId) {
        return baseScheduleMapper.findScenarioAndSwaggerRunningTaskInfoByProjectID(projectId, versionId);
    }
}
