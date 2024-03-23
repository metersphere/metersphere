package io.metersphere.system.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.page.PageMethod;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.Organization;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.taskcenter.TaskCenterScheduleDTO;
import io.metersphere.system.dto.taskcenter.enums.ScheduleTagType;
import io.metersphere.system.dto.taskcenter.request.TaskCenterSchedulePageRequest;
import io.metersphere.system.mapper.*;
import io.metersphere.system.schedule.ScheduleService;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
            ScheduleTagType scheduleTagType = ScheduleTagType.valueOf(request.getScheduleTagType());
            List<String> resourceTypes = scheduleTagType.getNames();
            if (!resourceTypes.isEmpty()) {
                list = extScheduleMapper.taskCenterSchedulelist(request, projectIds, resourceTypes);
                processTaskCenterSchedule(list, projectList, projectIds, request.getScheduleTagType());
            }
        }
        return list;
    }

    private void processTaskCenterSchedule(List<TaskCenterScheduleDTO> list, List<OptionDTO> projectList, List<String> projectIds, String scheduleTagType) {
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
                item.setNextTime(getNextTriggerTime(item.getValue()));
            });
        }
    }

    private List<OptionDTO> getProjectOption(String id){
        return baseProjectMapper.getProjectOptionsById(id);
    }

    private List<OptionDTO> getOrgProjectList(String orgId){
        return baseProjectMapper.getProjectOptionsByOrgId(orgId);
    }

    private List<OptionDTO> getSystemProjectList(){
        return baseProjectMapper.getProjectOptions();
    }

    private List<OptionDTO> getOrgListByProjectIds(List<String> projectIds){
        return extOrganizationMapper.getOrgListByProjectIds(projectIds);
    }

    /**
     * 获取下次执行时间（getFireTimeAfter，也可以下下次...）
     *
     * @param cron cron表达式
     * @return 下次执行时间
     */
    private static Long getNextTriggerTime(String cron) {
        if (!CronExpression.isValidExpression(cron)) {
            return null;
        }
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity("Calculate Date").withSchedule(CronScheduleBuilder.cronSchedule(cron)).build();
        Date time0 = trigger.getStartTime();
        Date time1 = trigger.getFireTimeAfter(time0);
        return time1 == null ? 0 : time1.getTime();
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

    public void delete(String id) {
        Schedule schedule = checkScheduleExit(id);
        if (ScheduleTagType.API_IMPORT.getNames().contains(schedule.getResourceType())) {
            extSwaggerMapper.deleteByPrimaryKey(schedule.getResourceId());
        }
        scheduleService.deleteByResourceId(schedule.getResourceId(), schedule.getJob());
    }

    private Schedule checkScheduleExit(String id) {
        Schedule schedule = scheduleMapper.selectByPrimaryKey(id);
        if (schedule == null) {
            throw new MSException(Translator.get("schedule_not_exist"));
        }
        return schedule;
    }

}
