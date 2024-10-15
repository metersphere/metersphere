package io.metersphere.api.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.page.PageMethod;
import io.metersphere.api.dto.definition.ExecuteReportDTO;
import io.metersphere.api.dto.report.ReportDTO;
import io.metersphere.api.mapper.ExtApiReportMapper;
import io.metersphere.api.mapper.ExtApiScenarioReportMapper;
import io.metersphere.engine.EngineFactory;
import io.metersphere.engine.MsHttpClient;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.dto.api.result.ProcessResultDTO;
import io.metersphere.sdk.dto.api.result.TaskResultDTO;
import io.metersphere.sdk.dto.api.task.TaskInfo;
import io.metersphere.sdk.dto.api.task.TaskItem;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.*;
import io.metersphere.system.domain.Organization;
import io.metersphere.system.dto.builder.LogDTOBuilder;
import io.metersphere.system.dto.pool.TestResourceDTO;
import io.metersphere.system.dto.pool.TestResourceNodeDTO;
import io.metersphere.system.dto.pool.TestResourcePoolReturnDTO;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.taskcenter.TaskCenterDTO;
import io.metersphere.system.dto.taskcenter.request.TaskCenterBatchRequest;
import io.metersphere.system.dto.taskcenter.request.TaskCenterPageRequest;
import io.metersphere.system.log.aspect.OperationLogAspect;
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
import io.metersphere.system.utils.SessionUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;
    private static final String DEFAULT_SORT = "start_time desc";
    private static final String ORG = "org";
    private static final String SYSTEM = "system";

    /**
     * 任务中心实时任务列表-项目级
     *
     * @param request 请求参数
     * @return 任务中心实时任务列表
     */
    public Pager<List<TaskCenterDTO>> getProjectPage(TaskCenterPageRequest request, String projectId) {
        checkProjectExist(projectId);
        List<OptionDTO> projectList = getProjectOption(projectId);
        return createTaskCenterPager(request, projectList, false);
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
        return createTaskCenterPager(request, projectList, false);
    }

    /**
     * 任务中心实时任务列表-系统级
     *
     * @param request 请求参数
     * @return 任务中心实时任务列表
     */
    public Pager<List<TaskCenterDTO>> getSystemPage(TaskCenterPageRequest request) {
        List<OptionDTO> projectList = getSystemProjectList();
        return createTaskCenterPager(request, projectList, true);
    }

    private Pager<List<TaskCenterDTO>> createTaskCenterPager(TaskCenterPageRequest request, List<OptionDTO> projectList, boolean isSystem) {
        Page<Object> page = PageMethod.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : DEFAULT_SORT);
        return PageUtils.setPageInfo(page, getPage(request, projectList, isSystem));
    }

    public List<TaskCenterDTO> getPage(TaskCenterPageRequest request, List<OptionDTO> projectList, boolean isSystem) {
        List<TaskCenterDTO> list = new ArrayList<>();
        List<String> projectIds = projectList.stream().map(OptionDTO::getId).toList();
        if (request != null && !projectIds.isEmpty()) {
            Map<String, ExecuteReportDTO> historyDeletedMap = new HashMap<>();
            if (request.getModuleType().equals(TaskCenterResourceType.API_CASE.toString())) {
                list = extApiReportMapper.taskCenterlist(request, isSystem ? new ArrayList<>() : projectIds, DateUtils.getDailyStartTime(), DateUtils.getDailyEndTime());
                //执行历史列表
                List<String> reportIds = list.stream().map(TaskCenterDTO::getId).toList();
                if (CollectionUtils.isNotEmpty(reportIds)) {
                    List<ExecuteReportDTO> historyDeletedList = extApiReportMapper.getHistoryDeleted(reportIds);
                    historyDeletedMap = historyDeletedList.stream().collect(Collectors.toMap(ExecuteReportDTO::getId, Function.identity()));
                }
            } else if (request.getModuleType().equals(TaskCenterResourceType.API_SCENARIO.toString())) {
                list = extApiScenarioReportMapper.taskCenterlist(request, isSystem ? new ArrayList<>() : projectIds, DateUtils.getDailyStartTime(), DateUtils.getDailyEndTime());
                List<String> reportIds = list.stream().map(TaskCenterDTO::getId).toList();
                if (CollectionUtils.isNotEmpty(reportIds)) {
                    List<ExecuteReportDTO> historyDeletedList = extApiScenarioReportMapper.getHistoryDeleted(reportIds);
                    historyDeletedMap = historyDeletedList.stream().collect(Collectors.toMap(ExecuteReportDTO::getId, Function.identity()));
                }
            }
            processTaskCenter(list, projectList, projectIds, historyDeletedMap);
        }
        return list;
    }

    private void processTaskCenter(List<TaskCenterDTO> list, List<OptionDTO> projectList, List<String> projectIds, Map<String, ExecuteReportDTO> historyDeletedMap) {
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
                item.setHistoryDeleted(MapUtils.isNotEmpty(historyDeletedMap) && !historyDeletedMap.containsKey(item.getId()));
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
        stopApiTask(request, new ArrayList<>(), userId, OperationLogModule.SETTING_SYSTEM_TASK_CENTER, getCheckPermissionFunc(SYSTEM, request.getModuleType()));
    }

    private void stopApiTask(TaskCenterBatchRequest request, List<String> projectIds, String userId, String module,
                             Consumer<Map<String, List<String>>> checkPermissionFunc) {
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
        checkBatchPermission(checkPermissionFunc, reports);
        if (CollectionUtils.isNotEmpty(reports)) {
            detailReport(request, reports, userId, module);
        }
    }

    /**
     * 校验权限
     *
     * @param checkPermissionFunc
     * @param reports
     */
    public void checkBatchPermission(Consumer<Map<String, List<String>>> checkPermissionFunc, List<ReportDTO> reports) {
        if (checkPermissionFunc != null && CollectionUtils.isNotEmpty(reports)) {
            Map<String, List<String>> reportOrgProjectMap = new HashMap<>();
            reports.forEach(report -> {
                // 获取组织和项目信息，校验对应权限
                List<String> reportIds = reportOrgProjectMap.getOrDefault(report.getOrganizationId(), new ArrayList<>());
                reportIds.add(report.getProjectId());
                reportOrgProjectMap.put(report.getOrganizationId(), reportIds);
            });
            // 校验权限
            checkPermissionFunc.accept(reportOrgProjectMap);
        }
    }

    private void detailReport(TaskCenterBatchRequest request,
                              List<ReportDTO> reports,
                              String userId,
                              String module) {
        Map<String, List<String>> poolIdMap = reports.stream()
                .collect(Collectors.groupingBy(ReportDTO::getPoolId,
                        Collectors.mapping(ReportDTO::getId, Collectors.toList())));

        poolIdMap.forEach((poolId, reportList) -> {
            TestResourcePoolReturnDTO testResourcePoolDTO = testResourcePoolService.getTestResourcePoolDetail(poolId);

            // Remove excluded report IDs
            if (request.getExcludeIds() != null && !request.getExcludeIds().isEmpty()) {
                reportList.removeAll(request.getExcludeIds());
            }

            boolean isK8SResourcePool = StringUtils.equals(testResourcePoolDTO.getType(), ResourcePoolTypeEnum.K8S.getName());

            if (isK8SResourcePool) {
                handleK8STask(request, reports, reportList, testResourcePoolDTO);
            } else {
                List<TestResourceNodeDTO> nodesList = testResourcePoolDTO.getTestResourceReturnDTO().getNodesList();
                if (CollectionUtils.isNotEmpty(nodesList)) {
                    stopTask(request, reportList, nodesList, reports);
                }
            }
        });

        logReports(request, reports, userId, module);
    }

    private void handleK8STask(TaskCenterBatchRequest request, List<ReportDTO> reports,
                               List<String> reportList, TestResourcePoolReturnDTO testResourcePoolDTO) {
        TaskRequestDTO taskRequestDTO = new TaskRequestDTO();
        TaskResultDTO result = createStoppedTaskResult();

        // Prepare mapping for integration and resource IDs
        Map<String, Boolean> integrationMap = prepareIntegrationMap(reports);
        Map<String, String> resourceIdMap = prepareResourceIdMap(reports);
        Map<String, String> testPlanIdMap = prepareTestPlanIdMap(reports);

        SubListUtils.dealForSubList(reportList, 100, subList -> {
            try {
                TestResourceDTO testResourceDTO = new TestResourceDTO();
                BeanUtils.copyBean(testResourceDTO, testResourcePoolDTO.getTestResourceReturnDTO());
                EngineFactory.stopApiTask(subList, testResourceDTO);
            } catch (Exception e) {
                LogUtils.error(e);
            } finally {
                processSubListReports(subList, request, result, taskRequestDTO, integrationMap, resourceIdMap, testPlanIdMap);
            }
        });
    }

    private TaskResultDTO createStoppedTaskResult() {
        TaskResultDTO result = new TaskResultDTO();
        result.setRequestResults(Collections.emptyList());
        result.setHasEnded(true);

        ProcessResultDTO processResultDTO = new ProcessResultDTO();
        processResultDTO.setStatus(ExecStatus.STOPPED.name());
        result.setProcessResultDTO(processResultDTO);
        result.setConsole("任务已终止");

        return result;
    }

    private void processSubListReports(List<String> subList, TaskCenterBatchRequest request,
                                       TaskResultDTO result, TaskRequestDTO taskRequestDTO,
                                       Map<String, Boolean> integrationMap, Map<String, String> resourceIdMap,
                                       Map<String, String> testPlanIdMap) {
        subList.forEach(reportId -> {
            TaskInfo taskInfo = taskRequestDTO.getTaskInfo();
            taskInfo.setResourceType(request.getModuleType());

            TaskItem taskItem = new TaskItem();
            taskItem.setReportId(reportId);
            taskItem.setResourceId(resourceIdMap.get(reportId));

            // Set resource type based on the test plan ID
            String testPlanId = testPlanIdMap.get(reportId);
            if (testPlanId != null && !"NONE".equals(testPlanId)) {
                taskInfo.setResourceType(getResourceType(request.getModuleType()));
            }

            // Set integrated report information
            taskInfo.getRunModeConfig().setIntegratedReport(integrationMap.get(reportId));
            if (Boolean.TRUE.equals(integrationMap.get(reportId))) {
                taskInfo.getRunModeConfig().getCollectionReport().setReportId(reportId);
            }

            taskRequestDTO.setTaskItem(taskItem);
            result.setRequest(taskRequestDTO);
            kafkaTemplate.send(KafkaTopicConstants.API_REPORT_TOPIC, JSON.toJSONString(result));
        });
    }

    private String getResourceType(String moduleType) {
        return TaskCenterResourceType.API_CASE.toString().equals(moduleType)
                ? ApiExecuteResourceType.TEST_PLAN_API_CASE.name()
                : TaskCenterResourceType.API_SCENARIO.toString().equals(moduleType)
                ? ApiExecuteResourceType.TEST_PLAN_API_SCENARIO.name()
                : "";
    }

    private void logReports(TaskCenterBatchRequest request, List<ReportDTO> reports,
                            String userId, String module) {
        List<String> reportIds = reports.stream().map(ReportDTO::getId).toList();
        SubListUtils.dealForSubList(reportIds, 100, subList -> {
            String logPrefix = StringUtils.join(module, "_REAL_TIME_");
            String resourceType = request.getModuleType().equals(TaskCenterResourceType.API_CASE.toString())
                    ? TaskCenterResourceType.API_CASE.toString()
                    : TaskCenterResourceType.API_SCENARIO.toString();

            saveLog(subList, userId, logPrefix + resourceType, resourceType);
        });
    }

    public void stopTask(TaskCenterBatchRequest request,
                         List<String> reportList,
                         List<TestResourceNodeDTO> nodesList,
                         List<ReportDTO> reports) {
        Map<String, Boolean> integrationMap = prepareIntegrationMap(reports);
        Map<String, String> resourceIdMap = prepareResourceIdMap(reports);
        Map<String, String> testPlanIdMap = prepareTestPlanIdMap(reports);

        // Remove excluded report IDs
        if (request.getExcludeIds() != null && !request.getExcludeIds().isEmpty()) {
            reportList.removeAll(request.getExcludeIds());
        }

        nodesList.parallelStream().forEach(node -> {
            String endpoint = MsHttpClient.getEndpoint(node.getIp(), node.getPort());
            TaskResultDTO result = createStoppedTaskResult();

            SubListUtils.dealForSubList(reportList, 100, subList -> {
                try {
                    LogUtils.info(String.format("开始发送停止请求到 %s 节点执行", endpoint), subList.toString());
                    MsHttpClient.stopApiTask(endpoint, subList);  // todo taskIds
                } catch (Exception e) {
                    LogUtils.error(e);
                } finally {
                    processSubListReports(subList, request, result, new TaskRequestDTO(), integrationMap, resourceIdMap, testPlanIdMap);
                }
            });
        });
    }

    private Map<String, Boolean> prepareIntegrationMap(List<ReportDTO> reports) {
        return reports.stream().collect(Collectors.toMap(ReportDTO::getId, ReportDTO::getIntegrated));
    }

    private Map<String, String> prepareResourceIdMap(List<ReportDTO> reports) {
        return reports.stream().collect(Collectors.toMap(ReportDTO::getId, ReportDTO::getResourceId));
    }

    private Map<String, String> prepareTestPlanIdMap(List<ReportDTO> reports) {
        return reports.stream().collect(Collectors.toMap(ReportDTO::getId, ReportDTO::getTestPlanId));
    }

    private void saveLog(List<String> ids, String userId, String module, String type) {
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
                    .type(OperationLogType.STOP.name())
                    .module(module)
                    .method(OperationLogAspect.getMethod())
                    .path(OperationLogAspect.getPath())
                    .sourceId(reportDTO.getId())
                    .content(reportDTO.getName())
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
        stopApiTask(request, projectIds, userId, OperationLogModule.SETTING_ORGANIZATION_TASK_CENTER, getCheckPermissionFunc(ORG, request.getModuleType()));
    }

    public void projectStop(TaskCenterBatchRequest request, String currentProjectId, String userId) {
        checkProjectExist(currentProjectId);
        stopApiTask(request, List.of(currentProjectId), userId, OperationLogModule.PROJECT_MANAGEMENT_TASK_CENTER, null);
    }

    public void systemStopById(String moduleType, String id, String userId, String module) {
        stopById(moduleType, id, userId, module, getCheckPermissionFunc(SYSTEM, moduleType));
    }

    public void orgStopById(String moduleType, String id, String userId, String module) {
        stopById(moduleType, id, userId, module, getCheckPermissionFunc(ORG, moduleType));
    }

    public void stopById(String moduleType, String id, String userId, String module, Consumer<Map<String, List<String>>> checkPermissionFunc) {
        List<String> reportIds = new ArrayList<>();
        reportIds.add(id);
        TaskCenterBatchRequest request = new TaskCenterBatchRequest();
        request.setSelectIds(reportIds);
        request.setModuleType(moduleType);
        stopApiTask(request, null, userId, module, checkPermissionFunc);
    }

    public void hasPermission(String type, String moduleType, String orgId, String projectId) {
        Map<String, List<String>> orgPermission = Map.of(
                TaskCenterResourceType.API_CASE.name(), List.of(PermissionConstants.ORGANIZATION_TASK_CENTER_READ_STOP, PermissionConstants.PROJECT_API_DEFINITION_CASE_EXECUTE),
                TaskCenterResourceType.API_SCENARIO.name(), List.of(PermissionConstants.ORGANIZATION_TASK_CENTER_READ_STOP, PermissionConstants.PROJECT_API_SCENARIO_EXECUTE)
        );

        Map<String, List<String>> projectPermission = Map.of(
                TaskCenterResourceType.API_CASE.name(), List.of(PermissionConstants.PROJECT_API_DEFINITION_CASE_EXECUTE),
                TaskCenterResourceType.API_SCENARIO.name(), List.of(PermissionConstants.PROJECT_API_SCENARIO_EXECUTE)
        );

        Map<String, List<String>> systemPermission = Map.of(
                TaskCenterResourceType.API_CASE.name(), List.of(PermissionConstants.SYSTEM_TASK_CENTER_READ_STOP, PermissionConstants.PROJECT_API_DEFINITION_CASE_EXECUTE),
                TaskCenterResourceType.API_SCENARIO.name(), List.of(PermissionConstants.SYSTEM_TASK_CENTER_READ_STOP, PermissionConstants.PROJECT_API_SCENARIO_EXECUTE)
        );

        boolean hasPermission = switch (type) {
            case "org" ->
                    orgPermission.get(moduleType).stream().anyMatch(item -> SessionUtils.hasPermission(orgId, projectId, item));
            case "project" ->
                    projectPermission.get(moduleType).stream().anyMatch(item -> SessionUtils.hasPermission(orgId, projectId, item));
            case "system" ->
                    systemPermission.get(moduleType).stream().anyMatch(item -> SessionUtils.hasPermission(orgId, projectId, item));
            default -> false;
        };

        if (!hasPermission) {
            throw new MSException(Translator.get("no_permission_to_resource"));
        }
    }

    public Consumer<Map<String, List<String>>> getCheckPermissionFunc(String type, String moduleType) {
        return (orgProjectMap) ->
                orgProjectMap.keySet().forEach(orgId ->
                        orgProjectMap.get(orgId).forEach(projectId ->
                                hasPermission(type, moduleType, orgId, projectId)
                        )
                );
    }
}
