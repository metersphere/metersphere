package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiDefinitionSwagger;
import io.metersphere.api.domain.ApiDefinitionSwaggerExample;
import io.metersphere.api.dto.definition.ApiScheduleDTO;
import io.metersphere.api.dto.definition.SwaggerBasicAuth;
import io.metersphere.api.dto.definition.importdto.ApiScheduleRequest;
import io.metersphere.api.mapper.ApiDefinitionSwaggerMapper;
import io.metersphere.api.service.schedule.SwaggerUrlImportJob;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.ScheduleType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.mapper.ScheduleMapper;
import io.metersphere.system.sechedule.ScheduleService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDefinitionScheduleService {

    @Resource
    private ApiDefinitionSwaggerMapper apiDefinitionSwaggerMapper;
    @Resource
    private ScheduleService scheduleService;
    @Resource
    private ScheduleMapper scheduleMapper;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private ProjectMapper projectMapper;

    public void createSchedule(ApiScheduleRequest request, String userId) {
        /*保存swaggerUrl*/
        ApiDefinitionSwagger apiSwagger = new ApiDefinitionSwagger();
        BeanUtils.copyBean(apiSwagger, request);
        apiSwagger.setId(IDGenerator.nextStr());
        // 设置鉴权信息
        SwaggerBasicAuth basicAuth = new SwaggerBasicAuth();
        basicAuth.setUserName(request.getAuthUsername());
        basicAuth.setPassword(request.getAuthPassword());
        basicAuth.setAuthSwitch(request.isAuthSwitch());
        apiSwagger.setConfig(ApiDataUtils.toJSONString(basicAuth));
        apiDefinitionSwaggerMapper.insertSelective(apiSwagger);

        Schedule schedule = this.buildApiTestSchedule(request, userId);
        schedule.setResourceId(apiSwagger.getId());
        schedule.setJob(SwaggerUrlImportJob.class.getName());
        schedule.setType(ScheduleType.CRON.name());
        scheduleService.addSchedule(schedule);
        scheduleService.addOrUpdateCronJob(schedule,
                SwaggerUrlImportJob.getJobKey(apiSwagger.getId()),
                SwaggerUrlImportJob.getTriggerKey(apiSwagger.getId()),
                SwaggerUrlImportJob.class);
        //TODO 记录日志
        LogDTO dto = new LogDTO(
                request.getProjectId(),
                projectMapper.selectByPrimaryKey(request.getProjectId()).getOrganizationId(),
                request.getId(),
                userId,
                OperationLogType.ADD.name(),
                OperationLogModule.API_DEFINITION,
                request.getName());
        dto.setHistory(false);
        dto.setPath("/api/definition/schedule/add");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(request));
        operationLogService.add(dto);
    }

    public Schedule buildApiTestSchedule(ApiScheduleRequest request, String userId) {
        Schedule schedule = new Schedule();
        schedule.setName(request.getName());
        schedule.setResourceId(request.getId());
        schedule.setEnable(request.getEnable());
        schedule.setValue(request.getValue().trim());
        schedule.setKey(request.getId());
        schedule.setCreateUser(userId);
        schedule.setProjectId(request.getProjectId());
        schedule.setConfig(request.getConfig());
        return schedule;
    }

    public ApiDefinitionSwagger checkSchedule(String id) {
        ApiDefinitionSwagger apiDefinitionSwagger = apiDefinitionSwaggerMapper.selectByPrimaryKey(id);
        if (apiDefinitionSwagger == null) {
            throw new MSException(Translator.get("schedule_not_exist"));
        }
        return apiDefinitionSwagger;
    }

    public Boolean checkSwaggerUrl(String projectId, String url) {
        ApiDefinitionSwaggerExample example = new ApiDefinitionSwaggerExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andSwaggerUrlEqualTo(url);
        List<ApiDefinitionSwagger> apiDefinitionSwaggers = apiDefinitionSwaggerMapper.selectByExample(example);
        return CollectionUtils.isNotEmpty(apiDefinitionSwaggers);
    }

    public void updateSchedule(ApiScheduleRequest request, String userId) {
        ApiDefinitionSwagger apiDefinitionSwagger = checkSchedule(request.getId());
        BeanUtils.copyBean(apiDefinitionSwagger, request);
        // 设置鉴权信息
        SwaggerBasicAuth basicAuth = new SwaggerBasicAuth();
        basicAuth.setUserName(request.getAuthUsername());
        basicAuth.setPassword(request.getAuthPassword());
        basicAuth.setAuthSwitch(request.isAuthSwitch());
        apiDefinitionSwagger.setConfig(ApiDataUtils.toJSONString(basicAuth));
        apiDefinitionSwaggerMapper.updateByPrimaryKeySelective(apiDefinitionSwagger);
        // 只修改表达式和名称
        Schedule schedule = scheduleService.getScheduleByResource(request.getId(), SwaggerUrlImportJob.class.getName());
        schedule.setValue(request.getValue().trim());
        schedule.setEnable(request.getEnable());
        schedule.setName(request.getName());
        scheduleService.editSchedule(schedule);
        scheduleService.addOrUpdateCronJob(schedule,
                SwaggerUrlImportJob.getJobKey(request.getId()),
                SwaggerUrlImportJob.getTriggerKey(request.getId()),
                SwaggerUrlImportJob.class);

        LogDTO dto = new LogDTO(
                request.getProjectId(),
                projectMapper.selectByPrimaryKey(request.getProjectId()).getOrganizationId(),
                request.getId(),
                userId,
                OperationLogType.UPDATE.name(),
                OperationLogModule.API_DEFINITION,
                request.getName());
        dto.setHistory(false);
        dto.setPath("/api/definition/schedule/update");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(request));
        operationLogService.add(dto);
    }

    public ApiScheduleDTO getSchedule(String id) {
        ApiScheduleDTO apiScheduleDTO = new ApiScheduleDTO();
        ApiDefinitionSwagger apiDefinitionSwagger = checkSchedule(id);
        Schedule schedule = scheduleService.getScheduleByResource(id, SwaggerUrlImportJob.class.getName());
        BeanUtils.copyBean(apiScheduleDTO, apiDefinitionSwagger);
        if (StringUtils.isNotBlank(apiDefinitionSwagger.getConfig())) {
            SwaggerBasicAuth basicAuth = ApiDataUtils.parseObject(apiDefinitionSwagger.getConfig(), SwaggerBasicAuth.class);
            apiScheduleDTO.setAuthUsername(basicAuth.getUserName());
            apiScheduleDTO.setAuthPassword(basicAuth.getPassword());
            apiScheduleDTO.setAuthSwitch(basicAuth.getAuthSwitch());
        }
        apiScheduleDTO.setEnable(schedule.getEnable());
        apiScheduleDTO.setValue(schedule.getValue());
        return apiScheduleDTO;
    }

    public void switchSchedule(String id) {
        Schedule schedule = checkScheduleExit(id);
        schedule.setEnable(!schedule.getEnable());
        scheduleService.editSchedule(schedule);
        scheduleService.addOrUpdateCronJob(schedule, SwaggerUrlImportJob.getJobKey(schedule.getResourceId()),
                SwaggerUrlImportJob.getTriggerKey(schedule.getResourceId()), SwaggerUrlImportJob.class);
    }

    public void deleteSchedule(String id) {
        Schedule schedule = checkScheduleExit(id);
        apiDefinitionSwaggerMapper.deleteByPrimaryKey(schedule.getResourceId());
        scheduleService.deleteByResourceId(schedule.getResourceId(), SwaggerUrlImportJob.class.getName());
    }

    private Schedule checkScheduleExit(String id) {
        Schedule schedule = scheduleMapper.selectByPrimaryKey(id);
        if (schedule == null) {
            throw new MSException(Translator.get("schedule_not_exist"));
        }
        return schedule;
    }

}
