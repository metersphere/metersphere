package io.metersphere.project.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.page.PageMethod;
import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.mapper.ApiDefinitionSwaggerMapper;
import io.metersphere.sdk.constants.ScheduleResourceType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.taskcenter.TaskCenterScheduleDTO;
import io.metersphere.system.dto.taskcenter.enums.ScheduleTagType;
import io.metersphere.system.dto.taskcenter.request.OrganizationTaskCenterSchedulePageRequest;
import io.metersphere.system.dto.taskcenter.request.ProjectTaskCenterSchedulePageRequest;
import io.metersphere.system.dto.taskcenter.request.TaskCenterSchedulePageRequest;
import io.metersphere.system.mapper.BaseProjectMapper;
import io.metersphere.system.mapper.ExtOrganizationMapper;
import io.metersphere.system.mapper.ExtScheduleMapper;
import io.metersphere.system.mapper.ScheduleMapper;
import io.metersphere.system.sechedule.ScheduleService;
import io.metersphere.system.service.UserLoginService;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.*;
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
    ApiDefinitionSwaggerMapper apiDefinitionSwaggerMapper;

    @Resource
    ScheduleMapper scheduleMapper;

    @Resource
    ScheduleService scheduleService;


    private static final String CREATE_TIME_SORT = "create_time desc";


    public Pager<List<TaskCenterScheduleDTO>> getProjectScheduledPage(ProjectTaskCenterSchedulePageRequest request) {
        List<OptionDTO> projectList = getProjectOption(request.getProjectId());
        return createTaskCenterSchedulePager(request, projectList);
    }

    public Pager<List<TaskCenterScheduleDTO>> getOrgScheduledPage(OrganizationTaskCenterSchedulePageRequest request) {
        List<OptionDTO> projectList = getOrgProjectList(request.getOrganizationId());
        return createTaskCenterSchedulePager(request, projectList);
    }

    public Pager<List<TaskCenterScheduleDTO>> getSystemScheduledPage(TaskCenterSchedulePageRequest request) {
        List<OptionDTO> projectList = getSystemProjectList();
        return createTaskCenterSchedulePager(request, projectList);
    }

    private Pager<List<TaskCenterScheduleDTO>> createTaskCenterSchedulePager(TaskCenterSchedulePageRequest request, List<OptionDTO> projectList) {
        Page<Object> page = PageMethod.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : CREATE_TIME_SORT);
        return PageUtils.setPageInfo(page, getScheduledPage(request, projectList));
    }

    public List<TaskCenterScheduleDTO> getScheduledPage(TaskCenterSchedulePageRequest request, List<OptionDTO> projectList) {
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
                String resourceId = item.getResourceId();
                if (ScheduleTagType.TEST_RESOURCE.toString().equals(scheduleTagType)) {
                    processTaskCenterScheduleData(list, resourceId, item);
                }
                item.setCreateUserName(userMap.getOrDefault(item.getCreateUserName(), StringUtils.EMPTY));
                item.setProjectName(projectMap.getOrDefault(item.getProjectId(), StringUtils.EMPTY));
                item.setOrganizationName(orgMap.getOrDefault(item.getProjectId(), StringUtils.EMPTY));
                item.setNextTime(getNextExecution(item.getValue()));
            });
        }
    }

    private void processTaskCenterScheduleData (List<TaskCenterScheduleDTO> list, String resourceId, TaskCenterScheduleDTO taskCenterScheduleDTO) {
        // 业务数据
        // 根据 resourceType 分组，并获取每个类型对应的 resourceId 数组
        Map<String, List<String>> resultMap = list.stream()
                .collect(Collectors.groupingBy(TaskCenterScheduleDTO::getResourceType,
                        Collectors.mapping(TaskCenterScheduleDTO::getResourceId, Collectors.toList())));
        Map<String, ApiScenario> apiScenarioMap = new HashMap<>();
        resultMap.forEach((type, resourceIds) ->{
            if (type.equals(ScheduleResourceType.API_SCENARIO.toString())) {
                List<ApiScenario> apiScenarios = extScheduleMapper.getApiScenarioListByIds(resourceIds);
                apiScenarioMap.putAll(apiScenarios.stream().collect(Collectors.toMap(ApiScenario::getId, Function.identity())));
            }
        });

        // TODO ui test load test ...
        if (apiScenarioMap.containsKey(resourceId)) {
            ApiScenario apiScenario = apiScenarioMap.get(resourceId);
            taskCenterScheduleDTO.setResourceName(apiScenario.getName());
            taskCenterScheduleDTO.setResourceNum(apiScenario.getNum());
        } else {
            taskCenterScheduleDTO.setResourceName(StringUtils.EMPTY);
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
     * 返回下一个执行时间根据给定的Cron表达式
     *
     * @param cronExpression Cron表达式
     * @return Date 下次Cron表达式执行时间
     */
    public static Date getNextExecution(String cronExpression)
    {
        try
        {
            CronExpression cron = new CronExpression(cronExpression);
            return cron.getNextValidTimeAfter(new Date(System.currentTimeMillis()));
        }
        catch (ParseException e)
        {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public void delete(String id) {
        Schedule schedule = checkScheduleExit(id);
        if (ScheduleTagType.API_IMPORT.getNames().contains(schedule.getResourceType())) {
            apiDefinitionSwaggerMapper.deleteByPrimaryKey(schedule.getResourceId());
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
