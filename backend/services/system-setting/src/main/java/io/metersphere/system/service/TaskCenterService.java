package io.metersphere.system.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.page.PageMethod;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.exception.MSException;
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
import io.metersphere.system.schedule.BaseScheduleJob;
import io.metersphere.system.schedule.ScheduleService;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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


    private static final String CREATE_TIME_SORT = "create_time desc";


    public Pager<List<TaskCenterScheduleDTO>> getProjectSchedulePage(TaskCenterSchedulePageRequest request, String projectId) {
        checkProjectExist(projectId);
        List<OptionDTO> projectList = getProjectOption(projectId);
        return createTaskCenterSchedulePager(request, projectList);
    }

    public Pager<List<TaskCenterScheduleDTO>> getOrgSchedulePage(TaskCenterSchedulePageRequest request, String organizationId) {
        checkOrganizationExist(organizationId);
        List<OptionDTO> projectList = getOrgProjectList(organizationId);
        return createTaskCenterSchedulePager(request, projectList);
    }

    public Pager<List<TaskCenterScheduleDTO>> getSystemSchedulePage(TaskCenterSchedulePageRequest request) {
        List<OptionDTO> projectList = getSystemProjectList();
        return createTaskCenterSchedulePager(request, projectList);
    }

    private Pager<List<TaskCenterScheduleDTO>> createTaskCenterSchedulePager(TaskCenterSchedulePageRequest request, List<OptionDTO> projectList) {
        Page<Object> page = PageMethod.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : CREATE_TIME_SORT);
        return PageUtils.setPageInfo(page, getSchedulePage(request, projectList));
    }

    public List<TaskCenterScheduleDTO> getSchedulePage(TaskCenterSchedulePageRequest request, List<OptionDTO> projectList) {
        List<TaskCenterScheduleDTO> list = new ArrayList<>();
        if (request != null && !projectList.isEmpty()) {
            List<String> projectIds = projectList.stream().map(OptionDTO::getId).toList();
            list = extScheduleMapper.taskCenterSchedulelist(request, projectIds);
            processTaskCenterSchedule(list, projectList, projectIds);
        }
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

    public void delete(String id, String userId, String path, String module) {
        Schedule schedule = checkScheduleExit(id);
        if (ScheduleTagType.API_IMPORT.getNames().contains(schedule.getResourceType())) {
            extSwaggerMapper.deleteByPrimaryKey(schedule.getResourceId());
        }
        scheduleService.deleteByResourceId(schedule.getResourceId(), schedule.getJob());
        saveLog(List.of(schedule), userId, path, HttpMethodConstants.GET.name(), module, OperationLogType.DELETE.name());
    }

    private Schedule checkScheduleExit(String id) {
        Schedule schedule = scheduleMapper.selectByPrimaryKey(id);
        if (schedule == null) {
            throw new MSException(Translator.get("schedule_not_exist"));
        }
        return schedule;
    }

    public void enable(String id, String userId, String path, String module) {
        Schedule schedule = checkScheduleExit(id);
        schedule.setEnable(!schedule.getEnable());
        scheduleService.editSchedule(schedule);
        scheduleService.addOrUpdateCronJob(schedule, new JobKey(schedule.getKey(), schedule.getJob()),
                new TriggerKey(schedule.getKey(), schedule.getJob()), BaseScheduleJob.class);
        saveLog(List.of(schedule), userId, path, HttpMethodConstants.GET.name(), module, OperationLogType.UPDATE.name());
    }

    public void update(String id, String cron, String userId, String path, String module) {
        Schedule schedule = checkScheduleExit(id);
        schedule.setValue(cron);
        scheduleService.editSchedule(schedule);
        scheduleService.addOrUpdateCronJob(schedule, new JobKey(schedule.getKey(), schedule.getJob()),
                new TriggerKey(schedule.getKey(), schedule.getJob()), schedule.getJob().getClass());
        saveLog(List.of(schedule), userId, path, HttpMethodConstants.POST.name(), module, OperationLogType.UPDATE.name());
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

    public void batchEnable(TaskCenterScheduleBatchRequest request, String userId, String path, String module, boolean enable) {
        List<OptionDTO> projectList = getSystemProjectList();
        batchOperation(request, userId, path, module, projectList, enable);
    }

    public void batchEnableOrg(TaskCenterScheduleBatchRequest request, String userId, String orgId, String path, String module, boolean enable) {
        List<OptionDTO> projectList = getOrgProjectList(orgId);
        batchOperation(request, userId, path, module, projectList, enable);

    }

    private void batchOperation(TaskCenterScheduleBatchRequest request, String userId, String path, String module, List<OptionDTO> projectList, boolean enable) {
        List<Schedule> scheduleList = new ArrayList<>();
        if (request.isSelectAll()) {
            List<String> projectIds = projectList.stream().map(OptionDTO::getId).toList();
            scheduleList = extScheduleMapper.getSchedule(request, projectIds);
        } else {
            ScheduleExample example = new ScheduleExample();
            example.createCriteria().andIdIn(request.getSelectIds()).andResourceTypeEqualTo(request.getScheduleTagType());
            scheduleList = scheduleMapper.selectByExample(example);
        }
        //过滤掉不需要的 和已经开启过的
        scheduleList = scheduleList.stream().filter(s -> s.getEnable() != enable || !request.getExcludeIds().contains(s.getId())).collect(Collectors.toList());

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ScheduleMapper batchMapper = sqlSession.getMapper(ScheduleMapper.class);
        scheduleList.forEach(s -> {
            s.setEnable(enable);
            batchMapper.updateByPrimaryKeySelective(s);
            scheduleService.addOrUpdateCronJob(s, new JobKey(s.getKey(), s.getJob()),
                    new TriggerKey(s.getKey(), s.getJob()), BaseScheduleJob.class);
        });
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        saveLog(scheduleList, userId, path, HttpMethodConstants.POST.name(), module, OperationLogType.UPDATE.name());
    }

    public void batchEnableProject(TaskCenterScheduleBatchRequest request, String userId, String projectId, String path, String module, boolean enable) {
        List<OptionDTO> projectList = getProjectOption(projectId);
        batchOperation(request, userId, path, module, projectList, enable);
    }
}
