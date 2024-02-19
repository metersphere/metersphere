package io.metersphere.api.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.page.PageMethod;
import io.metersphere.api.dto.report.ReportDTO;
import io.metersphere.api.mapper.ApiReportMapper;
import io.metersphere.api.mapper.ExtApiReportMapper;
import io.metersphere.api.mapper.ExtApiScenarioReportMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.TaskCenterResourceType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.DateUtils;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.Organization;
import io.metersphere.system.dto.pool.TestResourceNodeDTO;
import io.metersphere.system.dto.pool.TestResourcePoolReturnDTO;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.taskcenter.TaskCenterDTO;
import io.metersphere.system.dto.taskcenter.request.TaskCenterBatchRequest;
import io.metersphere.system.dto.taskcenter.request.TaskCenterPageRequest;
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
    ApiReportMapper apiReportMapper;

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
    private static final String DEFAULT_SORT = "start_time desc";

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

    public void systemStop(TaskCenterBatchRequest request) {
        stopApiTask(request, null);
    }

    private void stopApiTask(TaskCenterBatchRequest request, List<String> projectIds) {
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
            detailReport(request, reports);
        }
    }

    private void detailReport(TaskCenterBatchRequest request, List<ReportDTO> reports) {
        Map<String, List<String>> poolIdMap = reports.stream()
                .collect(Collectors.groupingBy(ReportDTO::getPoolId, Collectors.mapping(ReportDTO::getId, Collectors.toList())));

        poolIdMap.forEach((poolId, reportList) -> {
            TestResourcePoolReturnDTO testResourcePoolDTO = testResourcePoolService.getTestResourcePoolDetail(poolId);
            List<TestResourceNodeDTO> nodesList = testResourcePoolDTO.getTestResourceReturnDTO().getNodesList();
            if (CollectionUtils.isNotEmpty(nodesList)) {
                stopTask(request, reportList, nodesList);
            }
        });
    }

    private static void stopTask(TaskCenterBatchRequest request, List<String> reportList, List<TestResourceNodeDTO> nodesList) {
        nodesList.forEach(node -> {
            String endpoint = TaskRunnerClient.getEndpoint(node.getIp(), node.getPort());
            //需要去除取消勾选的report
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                reportList.removeAll(request.getExcludeIds());
            }
            SubListUtils.dealForSubList(reportList, 1000, (subList) -> {
                try {
                    LogUtils.info(String.format("开始发送停止请求到 %s 节点执行", endpoint), subList.toString());
                    TaskRunnerClient.stopApi(endpoint, subList);
                } catch (Exception e) {
                    LogUtils.error(e);
                    throw new MSException(RESOURCE_POOL_EXECUTE_ERROR, e.getMessage());
                }
            });
        });
    }

    public void orgStop(TaskCenterBatchRequest request, String orgId) {
        checkOrganizationExist(orgId);
        List<OptionDTO> projectList = getOrgProjectList(orgId);
        List<String> projectIds = projectList.stream().map(OptionDTO::getId).toList();
        stopApiTask(request, projectIds);

    }

    public void projectStop(TaskCenterBatchRequest request, String currentProjectId) {
        checkProjectExist(currentProjectId);
        stopApiTask(request, List.of(currentProjectId));
    }

    public void stopById(String id) {
        List<String> reportIds = new ArrayList<>();
        reportIds.add(id);
        TaskCenterBatchRequest request = new TaskCenterBatchRequest();
        request.setSelectIds(reportIds);
        stopApiTask(request, null);
    }
}
