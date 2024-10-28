package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiDefinitionSwagger;
import io.metersphere.api.domain.ApiDefinitionSwaggerExample;
import io.metersphere.api.dto.definition.ApiScheduleDTO;
import io.metersphere.api.dto.definition.SwaggerBasicAuth;
import io.metersphere.api.dto.definition.request.ApiScheduleRequest;
import io.metersphere.api.mapper.ApiDefinitionSwaggerMapper;
import io.metersphere.api.service.schedule.SwaggerUrlImportJob;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.ScheduleResourceType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.dto.request.ScheduleConfig;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.mapper.ScheduleMapper;
import io.metersphere.system.schedule.ScheduleService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    public String createSchedule(ApiScheduleRequest request, String userId) {
        /*保存swaggerUrl*/
        ApiDefinitionSwagger apiSwagger = new ApiDefinitionSwagger();
        BeanUtils.copyBean(apiSwagger, request);
        apiSwagger.setId(IDGenerator.nextStr());
        apiSwagger.setNum(NumGenerator.nextNum(request.getProjectId(), ApplicationNumScope.API_IMPORT));
        checkSwaggerUrl(request.getProjectId(), request.getSwaggerUrl());
        // 设置鉴权信息
        SwaggerBasicAuth basicAuth = new SwaggerBasicAuth();
        basicAuth.setUserName(request.getAuthUsername());
        basicAuth.setPassword(request.getAuthPassword());
        basicAuth.setAuthSwitch(request.isAuthSwitch());
        basicAuth.setToken(request.getSwaggerToken());
        apiSwagger.setConfig(ApiDataUtils.toJSONString(basicAuth));
        apiDefinitionSwaggerMapper.insertSelective(apiSwagger);

        ScheduleConfig scheduleConfig = ScheduleConfig.builder()
                .resourceId(apiSwagger.getId())
                .key(apiSwagger.getId())
                .projectId(apiSwagger.getProjectId())
                .name(apiSwagger.getName())
                .enable(request.getEnable())
                .cron(request.getValue().trim())
                .resourceType(ScheduleResourceType.API_IMPORT.name())
                .config(apiSwagger.getConfig())
                .build();

        LogDTO dto = new LogDTO(
                request.getProjectId(),
                projectMapper.selectByPrimaryKey(request.getProjectId()).getOrganizationId(),
                request.getId(),
                userId,
                OperationLogType.ADD.name(),
                OperationLogModule.API_TEST_MANAGEMENT_DEFINITION,
                Translator.get("api_import_schedule") + ": " + request.getName());
        dto.setHistory(false);
        dto.setPath("/api/definition/schedule/add");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(request));
        operationLogService.add(dto);
        return scheduleService.scheduleConfig(
                scheduleConfig,
                SwaggerUrlImportJob.getJobKey(apiSwagger.getId()),
                SwaggerUrlImportJob.getTriggerKey(apiSwagger.getId()),
                SwaggerUrlImportJob.class,
                userId);
    }

    public ApiDefinitionSwagger checkSchedule(String id) {
        ApiDefinitionSwagger apiDefinitionSwagger = apiDefinitionSwaggerMapper.selectByPrimaryKey(id);
        if (apiDefinitionSwagger == null) {
            throw new MSException(Translator.get("schedule_not_exist"));
        }
        return apiDefinitionSwagger;
    }

    public void checkSwaggerUrl(String projectId, String url) {
        ApiDefinitionSwaggerExample example = new ApiDefinitionSwaggerExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andSwaggerUrlEqualTo(url);
        List<ApiDefinitionSwagger> apiDefinitionSwaggers = apiDefinitionSwaggerMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(apiDefinitionSwaggers)) {
            throw new MSException(Translator.get("api_import_url_is_exist"));
        }
    }

    public String updateSchedule(ApiScheduleRequest request, String userId) {
        ApiDefinitionSwagger apiDefinitionSwagger = checkSchedule(request.getId());
        BeanUtils.copyBean(apiDefinitionSwagger, request);
        // 设置鉴权信息
        SwaggerBasicAuth basicAuth = new SwaggerBasicAuth();
        basicAuth.setUserName(request.getAuthUsername());
        basicAuth.setPassword(request.getAuthPassword());
        basicAuth.setAuthSwitch(request.isAuthSwitch());
        apiDefinitionSwagger.setConfig(ApiDataUtils.toJSONString(basicAuth));
        apiDefinitionSwaggerMapper.updateByPrimaryKeySelective(apiDefinitionSwagger);

        ScheduleConfig scheduleConfig = ScheduleConfig.builder()
                .resourceId(apiDefinitionSwagger.getId())
                .key(apiDefinitionSwagger.getId())
                .projectId(apiDefinitionSwagger.getProjectId())
                .name(apiDefinitionSwagger.getName())
                .enable(request.getEnable())
                .cron(request.getValue().trim())
                .resourceType(ScheduleResourceType.API_IMPORT.name())
                .config(apiDefinitionSwagger.getConfig())
                .build();

        LogDTO dto = new LogDTO(
                request.getProjectId(),
                projectMapper.selectByPrimaryKey(request.getProjectId()).getOrganizationId(),
                request.getId(),
                userId,
                OperationLogType.UPDATE.name(),
                OperationLogModule.API_TEST_MANAGEMENT_DEFINITION,
                Translator.get("api_import_schedule") + ": " + request.getName());
        dto.setHistory(false);
        dto.setPath("/api/definition/schedule/update");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(request));
        operationLogService.add(dto);
        return scheduleService.scheduleConfig(
                scheduleConfig,
                SwaggerUrlImportJob.getJobKey(apiDefinitionSwagger.getId()),
                SwaggerUrlImportJob.getTriggerKey(apiDefinitionSwagger.getId()),
                SwaggerUrlImportJob.class,
                userId);
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
            apiScheduleDTO.setSwaggerToken(basicAuth.getToken());
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

    public void deleteSchedule(String id, String userId, String path, String method, String module) {
        Schedule schedule = checkScheduleExit(id);
        apiDefinitionSwaggerMapper.deleteByPrimaryKey(schedule.getResourceId());
        scheduleService.deleteByResourceId(schedule.getResourceId(), SwaggerUrlImportJob.class.getName());
        saveLog(schedule, userId, path, method, module, OperationLogType.DELETE.name());
    }

    private void saveLog(Schedule schedule, String userId, String path, String method, String module, String type) {
        Optional.ofNullable(schedule).ifPresent(item -> {
            LogDTO dto = new LogDTO(
                    item.getProjectId(),
                    projectMapper.selectByPrimaryKey(item.getProjectId()).getOrganizationId(),
                    item.getResourceId(),
                    userId,
                    type,
                    module,
                    Translator.get("api_import_schedule") + ": " + item.getName());
            dto.setPath(path);
            dto.setMethod(method);
            operationLogService.add(dto);
        });
    }

    private Schedule checkScheduleExit(String id) {
        Schedule schedule = scheduleMapper.selectByPrimaryKey(id);
        if (schedule == null) {
            throw new MSException(Translator.get("schedule_not_exist"));
        }
        return schedule;
    }

}
