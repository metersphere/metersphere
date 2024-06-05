package io.metersphere.plan.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.page.PageMethod;
import io.metersphere.api.dto.definition.ExecuteReportDTO;
import io.metersphere.api.mapper.ExtApiScenarioReportMapper;
import io.metersphere.plan.mapper.ExtTestPlanReportMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.DateUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.Organization;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.taskcenter.TaskCenterDTO;
import io.metersphere.system.dto.taskcenter.request.TaskCenterPageRequest;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.mapper.BaseProjectMapper;
import io.metersphere.system.mapper.ExtOrganizationMapper;
import io.metersphere.system.mapper.OrganizationMapper;
import io.metersphere.system.service.TestResourcePoolService;
import io.metersphere.system.service.UserLoginService;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    ExtApiScenarioReportMapper extApiScenarioReportMapper;

    @Resource
    TestResourcePoolService testResourcePoolService;
    @Resource
    OperationLogService operationLogService;
    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;
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
        Map<String, ExecuteReportDTO> historyDeletedMap = new HashMap<>();
        list = extTestPlanReportMapper.taskCenterlist(request, isSystem ? new ArrayList<>() : projectIds, DateUtils.getDailyStartTime(), DateUtils.getDailyEndTime());
        //执行历史列表
        /*List<String> reportIds = list.stream().map(TaskCenterDTO::getId).toList();
        if (CollectionUtils.isNotEmpty(reportIds)) {
            List<ExecuteReportDTO> historyDeletedList = extTestPlanReportMapper.getHistoryDeleted(reportIds);
            historyDeletedMap = historyDeletedList.stream().collect(Collectors.toMap(ExecuteReportDTO::getId, Function.identity()));
        }*/
        processTaskCenter(list, projectList, projectIds, historyDeletedMap);
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
    
}
