package io.metersphere.plan.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.page.PageMethod;
import io.metersphere.api.dto.definition.ExecuteReportDTO;
import io.metersphere.api.dto.report.ReportDTO;
import io.metersphere.api.mapper.ExtApiScenarioReportMapper;
import io.metersphere.api.service.ApiTaskCenterService;
import io.metersphere.engine.EngineFactory;
import io.metersphere.engine.MsHttpClient;
import io.metersphere.plan.mapper.ExtTestPlanReportMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.ResourcePoolTypeEnum;
import io.metersphere.sdk.constants.TaskCenterResourceType;
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
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanTaskCenterService {

    @Resource
    ExtTestPlanReportMapper extTestPlanReportMapper;

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
    TestResourcePoolService testResourcePoolService;
    @Resource
    OperationLogService operationLogService;
    @Resource
    ExtApiScenarioReportMapper extApiScenarioReportMapper;
    @Resource
    TestPlanExecuteService testPlanExecuteService;
    @Resource
    ApiTaskCenterService apiTaskCenterService;
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
        if (CollectionUtils.isNotEmpty(projectIds)) {
            Map<String, ExecuteReportDTO> historyDeletedMap = new HashMap<>();
            list = extTestPlanReportMapper.taskCenterlist(request, isSystem ? new ArrayList<>() : projectIds, DateUtils.getDailyStartTime(), DateUtils.getDailyEndTime());
            // 执行历史ID集合
            List<String> reportIds = list.stream().map(TaskCenterDTO::getId).collect(Collectors.toList());
            // 查询计划组的任务的子计划任务
            List<String> groupReportIds = list.stream().filter(TaskCenterDTO::isIntegrated).map(TaskCenterDTO::getId).toList();
            if (CollectionUtils.isNotEmpty(groupReportIds)) {
                List<TaskCenterDTO> childTaskCenterList = extTestPlanReportMapper.getChildTaskCenter(groupReportIds);
                Map<String, List<TaskCenterDTO>> childTaskMap = childTaskCenterList.stream().collect(Collectors.groupingBy(TaskCenterDTO::getParent));
                list.forEach(item -> {
                    item.setChildren(childTaskMap.get(item.getId()));
                    if (CollectionUtils.isNotEmpty(item.getChildren())) {
                        reportIds.addAll(item.getChildren().stream().map(TaskCenterDTO::getId).toList());
                    }
                });
            }

            if (CollectionUtils.isNotEmpty(reportIds)) {
                List<ExecuteReportDTO> historyDeletedList = extTestPlanReportMapper.getHistoryDeleted(reportIds);
                historyDeletedMap = historyDeletedList.stream().collect(Collectors.toMap(ExecuteReportDTO::getId, Function.identity()));
            }

            // 准备参数
            Set<String> userSet = list.stream()
                    .flatMap(item -> Stream.of(item.getOperationName()))
                    .collect(Collectors.toSet());
            Map<String, String> userMap = userLoginService.getUserNameMap(new ArrayList<>(userSet));
            // 项目
            Map<String, String> projectMap = projectList.stream().collect(Collectors.toMap(OptionDTO::getId, OptionDTO::getName));
            // 组织
            List<OptionDTO> orgListByProjectList = getOrgListByProjectIds(projectIds);
            Map<String, String> orgMap = orgListByProjectList.stream().collect(Collectors.toMap(OptionDTO::getId, OptionDTO::getName));
            processTaskCenter(list, userMap, projectMap, orgMap, historyDeletedMap);
        }

        return list;
    }

    private void processTaskCenter(List<TaskCenterDTO> list, Map<String, String> userMap, Map<String, String> projectMap, Map<String, String> orgMap, Map<String, ExecuteReportDTO> historyDeletedMap) {
        if (!list.isEmpty()) {
            list.forEach(item -> {
                item.setOperationName(userMap.getOrDefault(item.getOperationName(), StringUtils.EMPTY));
                item.setProjectName(projectMap.getOrDefault(item.getProjectId(), StringUtils.EMPTY));
                item.setOrganizationName(orgMap.getOrDefault(item.getProjectId(), StringUtils.EMPTY));
                item.setHistoryDeleted(MapUtils.isNotEmpty(historyDeletedMap) && !historyDeletedMap.containsKey(item.getId()));
                if (CollectionUtils.isNotEmpty(item.getChildren())) {
                    processTaskCenter(item.getChildren(), userMap, projectMap, orgMap, historyDeletedMap);
                }
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

    public void hasPermission(String type, String orgId, String projectId) {
        List<String> orgPermission = List.of(PermissionConstants.ORGANIZATION_TASK_CENTER_READ_STOP, PermissionConstants.TEST_PLAN_READ_EXECUTE);
        List<String> projectPermission = List.of(PermissionConstants.TEST_PLAN_READ_EXECUTE);
        List<String> systemPermission = List.of(PermissionConstants.SYSTEM_TASK_CENTER_READ_STOP, PermissionConstants.TEST_PLAN_READ_EXECUTE);

        boolean hasPermission = switch (type) {
            case "org" -> orgPermission.stream().anyMatch(item -> SessionUtils.hasPermission(orgId, projectId, item));
            case "project" ->
                    projectPermission.stream().anyMatch(item -> SessionUtils.hasPermission(orgId, projectId, item));
            case "system" ->
                    systemPermission.stream().anyMatch(item -> SessionUtils.hasPermission(orgId, projectId, item));
            default -> false;
        };

        if (!hasPermission) {
            throw new MSException(Translator.get("no_permission_to_resource"));
        }
    }

    public void systemStop(TaskCenterBatchRequest request, String userId) {
        stopApiTask(request, new ArrayList<>(), userId, OperationLogModule.SETTING_SYSTEM_TASK_CENTER, getCheckPermissionFunc(SYSTEM));
    }

    public void orgStop(TaskCenterBatchRequest request, String orgId, String userId) {
        checkOrganizationExist(orgId);
        List<OptionDTO> projectList = getOrgProjectList(orgId);
        List<String> projectIds = projectList.stream().map(OptionDTO::getId).toList();
        stopApiTask(request, projectIds, userId, OperationLogModule.SETTING_ORGANIZATION_TASK_CENTER, getCheckPermissionFunc(ORG));
    }

    public void projectStop(TaskCenterBatchRequest request, String currentProjectId, String userId) {
        checkProjectExist(currentProjectId);
        stopApiTask(request, List.of(currentProjectId), userId, OperationLogModule.PROJECT_MANAGEMENT_TASK_CENTER);
    }

    public void systemStopById(String id, String userId, String logModule) {
        stopById(id, userId, logModule, getCheckPermissionFunc(SYSTEM));
    }

    public void orgStopById(String id, String userId, String logModule) {
        stopById(id, userId, logModule, getCheckPermissionFunc(ORG));
    }

    public void stopById(String id, String userId, String logModule, Consumer<Map<String, List<String>>> checkPermissionFunc) {
        List<String> reportIds = new ArrayList<>();
        reportIds.add(id);
        TaskCenterBatchRequest request = new TaskCenterBatchRequest();
        request.setSelectIds(reportIds);
        request.setModuleType(TaskCenterResourceType.TEST_PLAN.name());
        stopApiTask(request, null, userId, logModule, checkPermissionFunc);
    }

    private void stopApiTask(TaskCenterBatchRequest request, List<String> projectIds, String userId, String module) {
        stopApiTask(request, projectIds, userId, module, null);
    }

    private void stopApiTask(TaskCenterBatchRequest request, List<String> projectIds, String userId, String module,
                             Consumer<Map<String, List<String>>> checkPermissionFunc) {
        List<ReportDTO> reports;
        if (request.isSelectAll()) {
            reports = extTestPlanReportMapper.getReports(request, projectIds, null, DateUtils.getDailyStartTime(), DateUtils.getDailyEndTime());
        } else {
            reports = extTestPlanReportMapper.getReports(request, projectIds, request.getSelectIds(), DateUtils.getDailyStartTime(), DateUtils.getDailyEndTime());
        }

        apiTaskCenterService.checkBatchPermission(checkPermissionFunc, reports);

        // 需要处理  如果是集成报告， 需要找到计划组下面的所有的测试计划 然后全部停掉
        if (CollectionUtils.isNotEmpty(reports)) {
            //过滤所有为集合的报告，取测试计划ID
            List<String> reportIds = reports.stream().filter(item -> BooleanUtils.isTrue(item.getIntegrated()))
                    .map(ReportDTO::getId).toList();
            // 取集合报告的所有子报告
            if (CollectionUtils.isNotEmpty(reportIds)) {
                List<ReportDTO> childReports = extTestPlanReportMapper.selectByParentIds(reportIds);
                if (CollectionUtils.isNotEmpty(childReports)) {
                    reports.addAll(childReports);
                }
            }
            //根据id去重
            List<String> allReportIds = reports.stream().distinct().map(ReportDTO::getId).toList();

            if (CollectionUtils.isNotEmpty(allReportIds)) {
                // 查找和测试计划管理的接口用例的所有数据
                //停止测试计划的队列
                SubListUtils.dealForSubList(allReportIds, 10, (subList) -> {
                    subList.forEach(item -> {
                        try {
                            LogUtils.info(String.format("开始停止测试计划队列：%s", item));
                            testPlanExecuteService.stopTestPlanRunning(item);
                        } catch (Exception e) {
                            LogUtils.error("停止测试计划队列异常", e);
                        }
                    });
                    List<ReportDTO> apiReports = extTestPlanReportMapper.getCaseReports(subList);
                    detailReport(request, apiReports);
                    List<ReportDTO> scenarioReports = extTestPlanReportMapper.getScenarioReports(subList);
                    detailReport(request, scenarioReports);
                    saveLog(subList, userId, StringUtils.join(module, "_REAL_TIME_TEST_PLAN"));
                });
            }

        }
    }

    private void detailReport(TaskCenterBatchRequest request,
                              List<ReportDTO> reports) {
        Map<String, List<String>> poolIdMap = reports.stream()
                .collect(Collectors.groupingBy(ReportDTO::getPoolId, Collectors.mapping(ReportDTO::getId, Collectors.toList())));
        poolIdMap.forEach((poolId, reportList) -> {
            TestResourcePoolReturnDTO testResourcePoolDTO = testResourcePoolService.getTestResourcePoolDetail(poolId);
            // 判断是否为 K8S 资源池
            boolean isK8SResourcePool = StringUtils.equals(testResourcePoolDTO.getType(), ResourcePoolTypeEnum.K8S.getName());
            if (isK8SResourcePool) {
                SubListUtils.dealForSubList(reportList, 100, (subList) -> {
                    try {
                        TestResourceDTO testResourceDTO = new TestResourceDTO();
                        BeanUtils.copyBean(testResourceDTO, testResourcePoolDTO.getTestResourceReturnDTO());
                        EngineFactory.stopApiTask(subList, testResourceDTO);
                    } catch (Exception e) {
                        LogUtils.error(e);
                    }
                });
            } else {
                List<TestResourceNodeDTO> nodesList = testResourcePoolDTO.getTestResourceReturnDTO().getNodesList();
                if (CollectionUtils.isNotEmpty(nodesList)) {
                    stopTask(request, reportList, nodesList);
                }
            }
        });
    }

    public void stopTask(TaskCenterBatchRequest request,
                         List<String> reportList,
                         List<TestResourceNodeDTO> nodesList) {
        nodesList.parallelStream().forEach(node -> {
            String endpoint = MsHttpClient.getEndpoint(node.getIp(), node.getPort());
            //需要去除取消勾选的report
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                reportList.removeAll(request.getExcludeIds());
            }
            SubListUtils.dealForSubList(reportList, 100, (subList) -> {
                try {
                    LogUtils.info(String.format("开始发送停止请求到 %s 节点执行", endpoint), subList.toString());
                    MsHttpClient.stopApiTask(endpoint, subList);
                } catch (Exception e) {
                    LogUtils.error(e);
                }
            });
        });
    }

    private void saveLog(List<String> ids, String userId, String module) {
        List<ReportDTO> reports = extTestPlanReportMapper.getReportsByIds(ids);
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

    public Consumer<Map<String, List<String>>> getCheckPermissionFunc(String type) {
        return (orgProjectMap) ->
                orgProjectMap.keySet().forEach(orgId ->
                        orgProjectMap.get(orgId).forEach(projectId ->
                                hasPermission(type, orgId, projectId)
                        )
                );
    }
}
