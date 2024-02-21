package io.metersphere.api.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.page.PageMethod;
import io.metersphere.api.dto.report.ReportDTO;
import io.metersphere.api.mapper.ExtApiReportMapper;
import io.metersphere.api.mapper.ExtApiScenarioReportMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.TaskCenterResourceType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.DateUtils;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.Organization;
import io.metersphere.system.dto.builder.LogDTOBuilder;
import io.metersphere.system.dto.pool.TestResourceNodeDTO;
import io.metersphere.system.dto.pool.TestResourcePoolReturnDTO;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.taskcenter.TaskCenterDTO;
import io.metersphere.system.dto.taskcenter.request.TaskCenterBatchRequest;
import io.metersphere.system.dto.taskcenter.request.TaskCenterPageRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.mapper.BaseProjectMapper;
import io.metersphere.system.mapper.ExtOrganizationMapper;
import io.metersphere.system.mapper.OrganizationMapper;
import io.metersphere.system.service.TestResourcePoolService;
import io.metersphere.system.service.UserLoginService;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.TaskRunnerClient;
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
import java.util.stream.Stream;

import static io.metersphere.api.controller.result.ApiResultCode.RESOURCE_POOL_EXECUTE_ERROR;

/**
 * @author: LAN
 * @date: 2024/1/17 11:24
 * @version: 1.0
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApiTaskCenterService {

    @Resource
    ExtApiReportMapper extApiReportMapper;

    @Resource
    ExtOrganizationMapper extOrganizationMapper;

    @Resource
    BaseProjectMapper baseProjectMapper;

    @Resource
    UserLoginService userLoginService;

    @Resource
    ProjectMapper projectMapper;

    @Resource
    OrganizationMapper organizationMapper;

    @Resource
    ExtApiScenarioReportMapper extApiScenarioReportMapper;

    @Resource
    TestResourcePoolService testResourcePoolService;
    @Resource
    OperationLogService operationLogService;
    private static final String DEFAULT_SORT = "start_time desc";
    private final static String PROJECT_STOP = "/task/center/api/project/stop";
    private final static String ORG_STOP = "/task/center/api/org/stop";
    private final static String SYSTEM_STOP = "/task/center/api/system/stop";

    /**
     * 任务中心实时任务列表-项目级
     *
     * @param request 请求参数
     * @return 任务中心实时任务列表
     */
    public Pager<List<TaskCenterDTO>> getProjectPage(TaskCenterPageRequest request, String projectId) {
        checkProjectExist(projectId);
        List<OptionDTO> projectList = getProjectOption(projectId);
        return createTaskCenterPager(request, projectList);
    }

    /**
     * 任务中心实时任务列表-组织级
     *
     * @param request 请求参数
     * @return 任务中心实时任务列表
     */
    public Pager<List<TaskCenterDTO>> getOrganizationPage(TaskCenterPageRequest request, String organizationId) {
        checkOrganizationExist(organizationId);
        List<OptionDTO> projectList = getOrgProjectList(organizationId);
        return createTaskCenterPager(request, projectList);
    }

    /**
     * 任务中心实时任务列表-系统级
     *
     * @param request 请求参数
     * @return 任务中心实时任务列表
     */
    public Pager<List<TaskCenterDTO>> getSystemPage(TaskCenterPageRequest request) {
        List<OptionDTO> projectList = getSystemProjectList();
        return createTaskCenterPager(request, projectList);
    }

    private Pager<List<TaskCenterDTO>> createTaskCenterPager(TaskCenterPageRequest request, List<OptionDTO> projectList) {
        Page<Object> page = PageMethod.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : DEFAULT_SORT);
        return PageUtils.setPageInfo(page, getPage(request, projectList));
    }

    public List<TaskCenterDTO> getPage(TaskCenterPageRequest request, List<OptionDTO> projectList) {
        List<TaskCenterDTO> list = new ArrayList<>();
        List<String> projectIds = projectList.stream().map(OptionDTO::getId).toList();
        if (request != null && !projectIds.isEmpty()) {
            if (request.getModuleType().equals(TaskCenterResourceType.API_CASE.toString())) {
                list = extApiReportMapper.taskCenterlist(request, projectIds, DateUtils.getDailyStartTime(), DateUtils.getDailyEndTime());
            } else if (request.getModuleType().equals(TaskCenterResourceType.API_SCENARIO.toString())) {
                list = extApiScenarioReportMapper.taskCenterlist(request, projectIds, DateUtils.getDailyStartTime(), DateUtils.getDailyEndTime());
            }
            processTaskCenter(list, projectList, projectIds);
        }
        return list;
    }

    private void processTaskCenter(List<TaskCenterDTO> list, List<OptionDTO> projectList, List<String> projectIds) {
        if (!list.isEmpty()) {
            // 取所有的userid
            Set<String> userSet = list.stream()
                    .flatMap(item -> Stream.of(item.getOperationName()))
                    .collect(Collectors.toSet());
            Map<String, String> userMap = userLoginService.getUserNameMap(new ArrayList<>(userSet));
            // 项目
            Map<String, String> projectMap = projectList.stream().collect(Collectors.toMap(OptionDTO::getId, OptionDTO::getName));
            // 组织
            List<OptionDTO> orgListByProjectList = getOrgListByProjectIds(projectIds);
            Map<String, String> orgMap = orgListByProjectList.stream().collect(Collectors.toMap(OptionDTO::getId, OptionDTO::getName));

            list.forEach(item -> {
                item.setOperationName(userMap.getOrDefault(item.getOperationName(), StringUtils.EMPTY));
                item.setProjectName(projectMap.getOrDefault(item.getProjectId(), StringUtils.EMPTY));
                item.setOrganizationName(orgMap.getOrDefault(item.getProjectId(), StringUtils.EMPTY));
            });
        }
    }

    private List<OptionDTO> getProjectOption(String id) {
        return baseProjectMapper.getProjectOptionsById(id);
    }

    private List<OptionDTO> getOrgProjectList(String orgId) {
        return baseProjectMapper.getProjectOptionsByOrgId(orgId);
    }

    private List<OptionDTO> getSystemProjectList() {
        return baseProjectMapper.getProjectOptions();
    }

    private List<OptionDTO> getOrgListByProjectIds(List<String> projectIds) {
        return extOrganizationMapper.getOrgListByProjectIds(projectIds);
    }

    /**
     * 查看项目是否存在
     *
     * @param projectId 项目ID
     */
    private void checkProjectExist(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null) {
            throw new MSException(Translator.get("project_not_exist"));
        }
    }

    /**
     * 查看组织是否存在
     *
     * @param orgId 组织ID
     */
    private void checkOrganizationExist(String orgId) {
        Organization organization = organizationMapper.selectByPrimaryKey(orgId);
        if (organization == null) {
            throw new MSException(Translator.get("organization_not_exist"));
        }
    }

    public void systemStop(TaskCenterBatchRequest request, String userId) {
        stopApiTask(request, null, userId, SYSTEM_STOP, HttpMethodConstants.POST.name(), OperationLogModule.SETTING_SYSTEM_TASK_CENTER);
    }

    private void stopApiTask(TaskCenterBatchRequest request, List<String> projectIds, String userId, String path, String method, String module) {
        List<ReportDTO> reports = new ArrayList<>();
        if (request.getModuleType().equals(TaskCenterResourceType.API_CASE.toString())) {
            if (request.isSelectAll()) {
                reports = extApiReportMapper.getReports(request, projectIds, null, DateUtils.getDailyStartTime(), DateUtils.getDailyEndTime());
            } else {
                reports = extApiReportMapper.getReports(request, projectIds, request.getSelectIds(), DateUtils.getDailyStartTime(), DateUtils.getDailyEndTime());
            }
        } else if (request.getModuleType().equals(TaskCenterResourceType.API_SCENARIO.toString())) {
            if (request.isSelectAll()) {
                reports = extApiScenarioReportMapper.getReports(request, projectIds, null, DateUtils.getDailyStartTime(), DateUtils.getDailyEndTime());
            } else {
                reports = extApiScenarioReportMapper.getReports(request, projectIds, request.getSelectIds(), DateUtils.getDailyStartTime(), DateUtils.getDailyEndTime());
            }
        }
        if (CollectionUtils.isNotEmpty(reports)) {
            detailReport(request, reports, userId, path, method, module);
        }
    }

    private void detailReport(TaskCenterBatchRequest request,
                              List<ReportDTO> reports,
                              String userId,
                              String path,
                              String method,
                              String module) {
        Map<String, List<String>> poolIdMap = reports.stream()
                .collect(Collectors.groupingBy(ReportDTO::getPoolId, Collectors.mapping(ReportDTO::getId, Collectors.toList())));

        poolIdMap.forEach((poolId, reportList) -> {
            TestResourcePoolReturnDTO testResourcePoolDTO = testResourcePoolService.getTestResourcePoolDetail(poolId);
            List<TestResourceNodeDTO> nodesList = testResourcePoolDTO.getTestResourceReturnDTO().getNodesList();
            if (CollectionUtils.isNotEmpty(nodesList)) {
                stopTask(request, reportList, nodesList, userId, path, method, module);
            }
        });
    }

    public void stopTask(TaskCenterBatchRequest request,
                         List<String> reportList,
                         List<TestResourceNodeDTO> nodesList,
                         String userId,
                         String path,
                         String method,
                         String module) {
        nodesList.forEach(node -> {
            String endpoint = TaskRunnerClient.getEndpoint(node.getIp(), node.getPort());
            //需要去除取消勾选的report
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                reportList.removeAll(request.getExcludeIds());
            }
            SubListUtils.dealForSubList(reportList, 500, (subList) -> {
                try {
                    LogUtils.info(String.format("开始发送停止请求到 %s 节点执行", endpoint), subList.toString());
                    TaskRunnerClient.stopApi(endpoint, subList);
                    if (request.getModuleType().equals(TaskCenterResourceType.API_CASE.toString())) {
                        extApiReportMapper.updateReportStatus(subList, System.currentTimeMillis(), userId);
                        //记录日志
                        saveLog(subList, userId, path, method, module, TaskCenterResourceType.API_CASE.toString());
                    } else if (request.getModuleType().equals(TaskCenterResourceType.API_SCENARIO.toString())) {
                        extApiScenarioReportMapper.updateReportStatus(subList, System.currentTimeMillis(), userId);
                        saveLog(subList, userId, path, method, module, TaskCenterResourceType.API_SCENARIO.toString());
                    }

                } catch (Exception e) {
                    LogUtils.error(e);
                    throw new MSException(RESOURCE_POOL_EXECUTE_ERROR, e.getMessage());
                }
            });
        });
    }

    private void saveLog(List<String> ids, String userId, String path, String method, String module, String type) {
        List<ReportDTO> reports = new ArrayList<>();
        if (StringUtils.equals(type, TaskCenterResourceType.API_CASE.toString())) {
            reports = extApiReportMapper.selectByIds(ids);
        } else if (StringUtils.equals(type, TaskCenterResourceType.API_SCENARIO.toString())) {
            reports = extApiScenarioReportMapper.selectByIds(ids);
        }
        //取出所有的项目id
        List<String> projectIds = reports.stream().map(ReportDTO::getProjectId).distinct().toList();
        //根据项目id取出组织id
        List<ReportDTO> orgList = extApiScenarioReportMapper.getOrgListByProjectIds(projectIds);
        //生成map key:项目id value:组织id
        Map<String, String> orgMap = orgList.stream().collect(Collectors.toMap(ReportDTO::getProjectId, ReportDTO::getOrganizationId));
        List<LogDTO> logs = new ArrayList<>();
        reports.forEach(reportDTO -> {
            LogDTO dto = LogDTOBuilder.builder()
                    .projectId(reportDTO.getProjectId())
                    .organizationId(orgMap.get(reportDTO.getProjectId()))
                    .type(OperationLogType.UPDATE.name())
                    .module(module)
                    .method(method)
                    .path(path)
                    .sourceId(reportDTO.getId())
                    .content(String.format("停止任务：%s", reportDTO.getName()))
                    .createUser(userId)
                    .build().getLogDTO();
            logs.add(dto);
        });
        operationLogService.batchAdd(logs);
    }

    public void orgStop(TaskCenterBatchRequest request, String orgId, String userId) {
        checkOrganizationExist(orgId);
        List<OptionDTO> projectList = getOrgProjectList(orgId);
        List<String> projectIds = projectList.stream().map(OptionDTO::getId).toList();
        stopApiTask(request, projectIds, userId, ORG_STOP, HttpMethodConstants.POST.name(), OperationLogModule.SETTING_ORGANIZATION_TASK_CENTER);

    }

    public void projectStop(TaskCenterBatchRequest request, String currentProjectId, String userId) {
        checkProjectExist(currentProjectId);
        stopApiTask(request, List.of(currentProjectId), userId, PROJECT_STOP, HttpMethodConstants.POST.name(), OperationLogModule.PROJECT_MANAGEMENT_TASK_CENTER);
    }

    public void stopById(String id, String userId, String module, String path) {
        List<String> reportIds = new ArrayList<>();
        reportIds.add(id);
        TaskCenterBatchRequest request = new TaskCenterBatchRequest();
        request.setSelectIds(reportIds);
        stopApiTask(request, null, userId, path, HttpMethodConstants.GET.name(), module);
    }
}
