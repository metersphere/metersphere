package io.metersphere.system.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.page.PageMethod;
import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.TaskCenterResourceType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.DateUtils;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.Organization;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.domain.ScheduleExample;
import io.metersphere.system.dto.ProjectDTO;
import io.metersphere.system.dto.builder.LogDTOBuilder;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.taskcenter.TaskCenterScheduleDTO;
import io.metersphere.system.dto.taskcenter.enums.ScheduleTagType;
import io.metersphere.system.dto.taskcenter.request.TaskCenterScheduleBatchRequest;
import io.metersphere.system.dto.taskcenter.request.TaskCenterSchedulePageRequest;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.mapper.*;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.schedule.ApiScheduleNoticeService;
import io.metersphere.system.schedule.ScheduleService;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author: LAN
 * @date: 2024/1/17 11:24
 * @version: 1.0
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class TaskCenterService {

    @Resource
    ExtOrganizationMapper extOrganizationMapper;

    @Resource
    BaseProjectMapper baseProjectMapper;

    @Resource
    UserLoginService userLoginService;

    @Resource
    ExtScheduleMapper extScheduleMapper;

    @Resource
    ProjectMapper projectMapper;

    @Resource
    OrganizationMapper organizationMapper;

    @Resource
    ExtSwaggerMapper extSwaggerMapper;

    @Resource
    ScheduleMapper scheduleMapper;

    @Resource
    ScheduleService scheduleService;
    @Resource
    OperationLogService operationLogService;
    @Resource
    SqlSessionFactory sqlSessionFactory;
    @Resource
    ApiScheduleNoticeService apiScheduleNoticeService;
    @Resource
    UserMapper userMapper;
    @Resource
    ExtRealMapper extRealMapper;


    private static final String CREATE_TIME_SORT = "create_time desc";
    private static final String ORG = "org";
    private static final String SYSTEM = "system";


    public Pager<List<TaskCenterScheduleDTO>> getProjectSchedulePage(TaskCenterSchedulePageRequest request, String projectId) {
        checkProjectExist(projectId);
        List<OptionDTO> projectList = getProjectOption(projectId);
        return createTaskCenterSchedulePager(request, projectList, false);
    }

    public Pager<List<TaskCenterScheduleDTO>> getOrgSchedulePage(TaskCenterSchedulePageRequest request, String organizationId) {
        checkOrganizationExist(organizationId);
        List<OptionDTO> projectList = getOrgProjectList(organizationId);
        return createTaskCenterSchedulePager(request, projectList, false);
    }

    public Pager<List<TaskCenterScheduleDTO>> getSystemSchedulePage(TaskCenterSchedulePageRequest request) {
        List<OptionDTO> projectList = getSystemProjectList();
        return createTaskCenterSchedulePager(request, projectList, true);
    }

    private Pager<List<TaskCenterScheduleDTO>> createTaskCenterSchedulePager(TaskCenterSchedulePageRequest request, List<OptionDTO> projectList, boolean isSystem) {
        Page<Object> page = PageMethod.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : CREATE_TIME_SORT);
        return PageUtils.setPageInfo(page, getSchedulePage(request, projectList, isSystem));
    }

    public List<TaskCenterScheduleDTO> getSchedulePage(TaskCenterSchedulePageRequest request, List<OptionDTO> projectList, boolean isSystem) {
        List<TaskCenterScheduleDTO> list = new ArrayList<>();
        List<String> projectIds = projectList.stream().map(OptionDTO::getId).toList();
        list = extScheduleMapper.taskCenterSchedulelist(request, isSystem ? new ArrayList<>() : projectIds);
        processTaskCenterSchedule(list, projectList, projectIds);
        return list;
    }

    private void processTaskCenterSchedule(List<TaskCenterScheduleDTO> list, List<OptionDTO> projectList, List<String> projectIds) {
        if (!list.isEmpty()) {
            // 组织
            List<OptionDTO> orgListByProjectList = getOrgListByProjectIds(projectIds);
            Map<String, String> orgMap = orgListByProjectList.stream().collect(Collectors.toMap(OptionDTO::getId, OptionDTO::getName));
            // 取所有的userid
            Set<String> userSet = list.stream()
                    .flatMap(item -> Stream.of(item.getCreateUserName()))
                    .collect(Collectors.toSet());
            Map<String, String> userMap = userLoginService.getUserNameMap(new ArrayList<>(userSet));
            // 项目
            Map<String, String> projectMap = projectList.stream().collect(Collectors.toMap(OptionDTO::getId, OptionDTO::getName));


            list.forEach(item -> {
                item.setCreateUserName(userMap.getOrDefault(item.getCreateUserName(), StringUtils.EMPTY));
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
    private Project checkProjectExist(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null) {
            throw new MSException(Translator.get("project_not_exist"));
        }
        return project;
    }

    /**
     * 查看组织是否存在
     *
     * @param orgId 组织ID
     */
    private Organization checkOrganizationExist(String orgId) {
        Organization organization = organizationMapper.selectByPrimaryKey(orgId);
        if (organization == null) {
            throw new MSException(Translator.get("organization_not_exist"));
        }
        return organization;
    }

    public void delete(String id, String moduleType, String userId, String path, String module) {
        Schedule schedule = checkScheduleExit(id);
        String logModule = getLogModule(moduleType, module);
        if (StringUtils.equals(schedule.getResourceType(), ScheduleTagType.API_IMPORT.name())) {
            extSwaggerMapper.deleteByPrimaryKey(schedule.getResourceId());
        }
        scheduleService.deleteByResourceId(schedule.getResourceId(), schedule.getJob());
        saveLog(List.of(schedule), userId, path, HttpMethodConstants.GET.name(), logModule, OperationLogType.DELETE.name());
    }

    private static String getLogModule(String moduleType, String module) {
        return switch (ScheduleTagType.valueOf(moduleType)) {
            case API_IMPORT -> StringUtils.join(module, "_TIME_API_IMPORT");
            case API_SCENARIO -> StringUtils.join(module, "_TIME_API_SCENARIO");
            case TEST_PLAN -> StringUtils.join(module, "_TIME_TEST_PLAN");
            default -> throw new MSException(Translator.get("module_type_error"));
        };
    }

    public Schedule checkScheduleExit(String id) {
        Schedule schedule = scheduleMapper.selectByPrimaryKey(id);
        if (schedule == null) {
            throw new MSException(Translator.get("schedule_not_exist"));
        }
        return schedule;
    }

    public void enable(String id, String moduleType, String userId, String path, String module) {
        Schedule schedule = checkScheduleExit(id);
        schedule.setEnable(!schedule.getEnable());
        scheduleService.editSchedule(schedule);
        try {
            scheduleService.addOrUpdateCronJob(schedule, new JobKey(schedule.getKey(), schedule.getJob()),
                    new TriggerKey(schedule.getKey(), schedule.getJob()), Class.forName(schedule.getJob()));
        } catch (ClassNotFoundException e) {
            LogUtils.error(e);
            throw new RuntimeException(e);
        }
        apiScheduleNoticeService.sendScheduleNotice(schedule, userId);
        String logModule = getLogModule(moduleType, module);
        saveLog(List.of(schedule), userId, path, HttpMethodConstants.GET.name(), logModule, OperationLogType.UPDATE.name());
    }

    public void update(String id, String moduleType, String cron, String userId, String path, String module) {
        Schedule schedule = checkScheduleExit(id);
        schedule.setValue(cron);
        scheduleService.editSchedule(schedule);
        scheduleService.addOrUpdateCronJob(schedule, new JobKey(schedule.getKey(), schedule.getJob()),
                new TriggerKey(schedule.getKey(), schedule.getJob()), schedule.getJob().getClass());
        String logModule = getLogModule(moduleType, module);
        saveLog(List.of(schedule), userId, path, HttpMethodConstants.POST.name(), logModule, OperationLogType.UPDATE.name());
    }

    private void saveLog(List<Schedule> scheduleList, String userId, String path, String method, String module, String operationType) {
        //取出所有的项目id
        if (scheduleList.isEmpty()) {
            return;
        }
        List<String> projectIds = scheduleList.stream().map(Schedule::getProjectId).distinct().toList();
        //根据项目id取出组织id
        List<ProjectDTO> orgList = extScheduleMapper.getOrgListByProjectIds(projectIds);
        //生成map key:项目id value:组织id
        Map<String, String> orgMap = orgList.stream().collect(Collectors.toMap(ProjectDTO::getId, ProjectDTO::getOrganizationId));
        List<LogDTO> logs = new ArrayList<>();
        scheduleList.forEach(s -> {
            LogDTO dto = LogDTOBuilder.builder()
                    .projectId(s.getProjectId())
                    .organizationId(orgMap.get(s.getProjectId()))
                    .type(operationType)
                    .module(module)
                    .method(method)
                    .path(path)
                    .sourceId(s.getResourceId())
                    .content(s.getName())
                    .createUser(userId)
                    .build().getLogDTO();
            logs.add(dto);
        });
        operationLogService.batchAdd(logs);
    }

    public void batchEnable(TaskCenterScheduleBatchRequest request, String userId, String path, String module,
                            boolean enable, String projectId, Consumer<Map<String, List<String>>> checkPermissionFunc) {
        batchOperation(request, userId, path, module, new ArrayList<>(), enable, projectId, checkPermissionFunc);
    }

    public void systemBatchEnable(TaskCenterScheduleBatchRequest request, String userId, String path, String module,
                                  boolean enable, String projectId) {
        batchEnable(request, userId, path, module, enable, projectId, getCheckPermissionFunc(SYSTEM, request.getScheduleTagType()));
    }

    public void orgBatchEnable(TaskCenterScheduleBatchRequest request, String userId, String orgId, String path, String module,
                               boolean enable, String projectId) {
        List<OptionDTO> projectList = getOrgProjectList(orgId);
        batchOperation(request, userId, path, module, projectList, enable, projectId, getCheckPermissionFunc(ORG, request.getScheduleTagType()));
    }

    private void batchOperation(TaskCenterScheduleBatchRequest request, String userId, String path, String module,
                                List<OptionDTO> projectList, boolean enable, String projectId, Consumer<Map<String, List<String>>> checkPermissionFunc) {
        List<Schedule> scheduleList;
        if (request.isSelectAll()) {
            List<String> projectIds = projectList.stream().map(OptionDTO::getId).toList();
            scheduleList = extScheduleMapper.getSchedule(request, projectIds);
        } else {
            ScheduleExample example = new ScheduleExample();
            example.createCriteria().andIdIn(request.getSelectIds()).andResourceTypeEqualTo(request.getScheduleTagType());
            scheduleList = scheduleMapper.selectByExample(example);
        }
        //过滤掉不需要的 和已经开启过的
        scheduleList = scheduleList.stream().filter(s -> s.getEnable() != enable).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
            scheduleList.removeAll(request.getExcludeIds());
        }

        // 校验权限
        checkBatchPermission(checkPermissionFunc, scheduleList);

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ScheduleMapper batchMapper = sqlSession.getMapper(ScheduleMapper.class);
        SubListUtils.dealForSubList(scheduleList, 100, list -> {
            list.forEach(s -> {
                s.setEnable(enable);
                batchMapper.updateByPrimaryKeySelective(s);
                try {
                    scheduleService.addOrUpdateCronJob(s, new JobKey(s.getKey(), s.getJob()),
                            new TriggerKey(s.getKey(), s.getJob()), Class.forName(s.getJob()));
                } catch (ClassNotFoundException e) {
                    LogUtils.error(e);
                    throw new RuntimeException(e);
                }
            });
            sqlSession.flushStatements();
        });
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        apiScheduleNoticeService.batchSendNotice(projectId, scheduleList, userMapper.selectByPrimaryKey(userId), enable ? NoticeConstants.Event.OPEN : NoticeConstants.Event.CLOSE);
        String logModule = getLogModule(request.getScheduleTagType(), module);
        saveLog(scheduleList, userId, path, HttpMethodConstants.POST.name(), logModule, OperationLogType.UPDATE.name());
    }

    /**
     * 校验权限
     *
     * @param checkPermissionFunc
     * @param schedules
     */
    public void checkBatchPermission(Consumer<Map<String, List<String>>> checkPermissionFunc, List<Schedule> schedules) {
        if (checkPermissionFunc != null && CollectionUtils.isNotEmpty(schedules)) {
            List<String> projectIds = schedules.stream().map(Schedule::getProjectId).distinct().toList();
            ProjectExample example = new ProjectExample();
            example.createCriteria().andIdIn(projectIds);
            Map<String, String> projectOrgMap = projectMapper.selectByExample(example)
                    .stream()
                    .collect(Collectors.toMap(Project::getId, Project::getOrganizationId));
            Map<String, List<String>> reportOrgProjectMap = new HashMap<>();
            schedules.forEach(schedule -> {
                // 获取组织和项目信息，校验对应权限
                List<String> reportIds = reportOrgProjectMap.getOrDefault(projectOrgMap.get(schedule.getProjectId()), new ArrayList<>());
                reportIds.add(schedule.getProjectId());
                reportOrgProjectMap.put(projectOrgMap.get(schedule.getProjectId()), reportIds);
            });
            // 校验权限
            checkPermissionFunc.accept(reportOrgProjectMap);
        }
    }

    public void batchEnableProject(TaskCenterScheduleBatchRequest request, String userId, String projectId, String path,
                                   String module, boolean enable) {
        List<OptionDTO> projectList = getProjectOption(projectId);
        batchOperation(request, userId, path, module, projectList, enable, projectId, null);
    }

    public void hasPermission(String type, String moduleType, String orgId, String projectId) {
        Map<String, List<String>> orgPermission = Map.of(
                ScheduleTagType.API_IMPORT.name(), List.of(PermissionConstants.ORGANIZATION_TASK_CENTER_READ_STOP, PermissionConstants.PROJECT_API_DEFINITION_IMPORT),
                TaskCenterResourceType.API_SCENARIO.name(), List.of(PermissionConstants.ORGANIZATION_TASK_CENTER_READ_STOP, PermissionConstants.PROJECT_API_SCENARIO_EXECUTE),
                TaskCenterResourceType.TEST_PLAN.name(), List.of(PermissionConstants.ORGANIZATION_TASK_CENTER_READ_STOP, PermissionConstants.TEST_PLAN_READ_EXECUTE)
        );

        Map<String, List<String>> projectPermission = Map.of(
                ScheduleTagType.API_IMPORT.name(), List.of(PermissionConstants.PROJECT_API_DEFINITION_IMPORT),
                TaskCenterResourceType.API_SCENARIO.name(), List.of(PermissionConstants.PROJECT_API_SCENARIO_EXECUTE),
                TaskCenterResourceType.TEST_PLAN.name(), List.of(PermissionConstants.TEST_PLAN_READ_EXECUTE)
        );

        Map<String, List<String>> systemPermission = Map.of(
                ScheduleTagType.API_IMPORT.name(), List.of(PermissionConstants.SYSTEM_TASK_CENTER_READ_STOP, PermissionConstants.PROJECT_API_DEFINITION_IMPORT),
                TaskCenterResourceType.API_SCENARIO.name(), List.of(PermissionConstants.SYSTEM_TASK_CENTER_READ_STOP, PermissionConstants.PROJECT_API_SCENARIO_EXECUTE),
                TaskCenterResourceType.TEST_PLAN.name(), List.of(PermissionConstants.SYSTEM_TASK_CENTER_READ_STOP, PermissionConstants.TEST_PLAN_READ_EXECUTE)
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

    public int getSystemScheduleTotal() {
        return extScheduleMapper.countByProjectIds(new ArrayList<>());
    }

    public int getOrgScheduleTotal(String currentOrganizationId) {
        checkOrganizationExist(currentOrganizationId);
        List<OptionDTO> projectList = getOrgProjectList(currentOrganizationId);
        //获取项目id
        List<String> projectIds = projectList.stream().map(OptionDTO::getId).toList();
        return extScheduleMapper.countByProjectIds(projectIds);
    }

    public int getProjectScheduleTotal(String currentProjectId) {
        checkProjectExist(currentProjectId);
        return extScheduleMapper.countByProjectIds(List.of(currentProjectId));
    }

    public int getSystemRealTotal() {
        int apiTestCaseTotal = extRealMapper.caseReportCountByProjectIds(new ArrayList<>(), DateUtils.getDailyStartTime(), DateUtils.getDailyEndTime());
        int apiScenarioTotal = extRealMapper.scenarioReportCountByProjectIds(new ArrayList<>(), DateUtils.getDailyStartTime(), DateUtils.getDailyEndTime());
        int testPlanTotal = extRealMapper.testPlanReportCountByProjectIds(new ArrayList<>(), DateUtils.getDailyStartTime(), DateUtils.getDailyEndTime());
        return apiTestCaseTotal + apiScenarioTotal + testPlanTotal;
    }

    public int getOrgRealTotal(String currentOrganizationId) {
        checkOrganizationExist(currentOrganizationId);
        List<OptionDTO> projectList = getOrgProjectList(currentOrganizationId);
        //获取项目id
        List<String> projectIds = projectList.stream().map(OptionDTO::getId).toList();
        int apiTestCaseTotal = extRealMapper.caseReportCountByProjectIds(projectIds, DateUtils.getDailyStartTime(), DateUtils.getDailyEndTime());
        int apiScenarioTotal = extRealMapper.scenarioReportCountByProjectIds(projectIds, DateUtils.getDailyStartTime(), DateUtils.getDailyEndTime());
        int testPlanTotal = extRealMapper.testPlanReportCountByProjectIds(projectIds, DateUtils.getDailyStartTime(), DateUtils.getDailyEndTime());
        return apiTestCaseTotal + apiScenarioTotal + testPlanTotal;
    }

    public int getProjectRealTotal(String currentProjectId) {
        checkProjectExist(currentProjectId);
        int apiTestCaseTotal = extRealMapper.caseReportCountByProjectIds(List.of(currentProjectId), DateUtils.getDailyStartTime(), DateUtils.getDailyEndTime());
        int apiScenarioTotal = extRealMapper.scenarioReportCountByProjectIds(List.of(currentProjectId), DateUtils.getDailyStartTime(), DateUtils.getDailyEndTime());
        int testPlanTotal = extRealMapper.testPlanReportCountByProjectIds(List.of(currentProjectId), DateUtils.getDailyStartTime(), DateUtils.getDailyEndTime());
        return apiTestCaseTotal + apiScenarioTotal + testPlanTotal;
    }

    private Consumer<Map<String, List<String>>> getCheckPermissionFunc(String type, String moduleType) {
        return (orgProjectMap) ->
                orgProjectMap.keySet().forEach(orgId ->
                        orgProjectMap.get(orgId).forEach(projectId ->
                                hasPermission(type, moduleType, orgId, projectId)
                        )
                );
    }

    public void checkSystemPermission(String moduleType, String id) {
        Schedule schedule = checkScheduleExit(id);
        Project project = projectMapper.selectByPrimaryKey(schedule.getProjectId());
        hasPermission(SYSTEM, moduleType,
                project.getOrganizationId(), schedule.getProjectId());
    }

    public void checkOrgPermission(String moduleType, String id) {
        Schedule schedule = checkScheduleExit(id);
        hasPermission(ORG, moduleType,
                SessionUtils.getCurrentOrganizationId(), schedule.getProjectId());
    }
}
