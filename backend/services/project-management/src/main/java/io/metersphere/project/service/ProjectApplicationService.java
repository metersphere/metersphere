package io.metersphere.project.service;

import io.metersphere.plugin.platform.spi.AbstractPlatformPlugin;
import io.metersphere.plugin.platform.spi.Platform;
import io.metersphere.project.domain.FakeErrorExample;
import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.domain.ProjectApplicationExample;
import io.metersphere.project.dto.ModuleDTO;
import io.metersphere.project.job.CleanUpReportJob;
import io.metersphere.project.job.BugSyncJob;
import io.metersphere.project.mapper.ExtProjectMapper;
import io.metersphere.project.mapper.ExtProjectUserRoleMapper;
import io.metersphere.project.mapper.FakeErrorMapper;
import io.metersphere.project.mapper.ProjectApplicationMapper;
import io.metersphere.project.request.ProjectApplicationRequest;
import io.metersphere.project.utils.ModuleSortUtils;
import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.constants.ScheduleType;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.dto.OptionDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.*;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.ExtPluginMapper;
import io.metersphere.system.mapper.PluginMapper;
import io.metersphere.system.mapper.ServiceIntegrationMapper;
import io.metersphere.system.sechedule.ScheduleService;
import io.metersphere.system.service.PlatformPluginService;
import io.metersphere.system.service.PluginLoadService;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static io.metersphere.system.controller.handler.result.MsHttpResultCode.NOT_FOUND;

@Service
@Transactional
public class ProjectApplicationService {
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;

    @Resource
    private ScheduleService scheduleService;

    @Resource
    private ExtProjectUserRoleMapper extProjectUserRoleMapper;

    @Resource
    private ServiceIntegrationMapper serviceIntegrationMapper;
    @Resource
    private ExtPluginMapper extPluginMapper;

    @Resource
    private PluginMapper pluginMapper;

    @Resource
    private PluginLoadService pluginLoadService;

    @Resource
    private ExtProjectMapper extProjectMapper;

    @Resource
    private PlatformPluginService platformPluginService;

    @Resource
    private FakeErrorMapper fakeErrorMapper;

    /**
     * 更新配置信息
     *
     * @param application
     * @return
     */
    public void update(ProjectApplication application, String currentUser) {
        this.doBeforeUpdate(application, currentUser);
        //配置信息入库
        this.createOrUpdateConfig(application);
    }

    private void createOrUpdateConfig(ProjectApplication application) {
        String type = application.getType();
        String projectId = application.getProjectId();
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andTypeEqualTo(type);
        if (projectApplicationMapper.countByExample(example) > 0) {
            example.clear();
            example.createCriteria().andProjectIdEqualTo(projectId).andTypeEqualTo(type);
            projectApplicationMapper.updateByExample(application, example);
        } else {
            projectApplicationMapper.insertSelective(application);
        }
    }

    private void doBeforeUpdate(ProjectApplication application, String currentUser) {
        String type = application.getType();
        if (StringUtils.equals(type, ProjectApplicationType.TEST_PLAN.TEST_PLAN_CLEAN_REPORT.name())
                || StringUtils.equals(type, ProjectApplicationType.UI.UI_CLEAN_REPORT.name())
                || StringUtils.equals(type, ProjectApplicationType.PERFORMANCE_TEST.PERFORMANCE_TEST_CLEAN_REPORT.name())
                || StringUtils.equals(type, ProjectApplicationType.API.API_CLEAN_REPORT.name())) {
            //清除 测试计划/UI测试/性能测试/接口测试 报告 定时任务
            this.doHandleSchedule(application, currentUser);
        }
    }

    private void doHandleSchedule(ProjectApplication application, String currentUser) {
        String typeValue = application.getTypeValue();
        String projectId = application.getProjectId();
        Boolean enable = BooleanUtils.isTrue(Boolean.valueOf(typeValue));
        Schedule schedule = scheduleService.getScheduleByResource(application.getProjectId(), CleanUpReportJob.class.getName());
        Optional<Schedule> optional = Optional.ofNullable(schedule);
        optional.ifPresentOrElse(s -> {
            s.setEnable(enable);
            s.setCreateUser(currentUser);
            scheduleService.editSchedule(s);
            scheduleService.addOrUpdateCronJob(s,
                    CleanUpReportJob.getJobKey(projectId),
                    CleanUpReportJob.getTriggerKey(projectId),
                    CleanUpReportJob.class);
        }, () -> {
            Schedule request = new Schedule();
            request.setName("Clean Report Job");
            request.setResourceId(projectId);
            request.setKey(projectId);
            request.setProjectId(projectId);
            request.setEnable(enable);
            request.setCreateUser(currentUser);
            request.setType(ScheduleType.CRON.name());
            // 每天凌晨2点执行清理任务
            request.setValue("0 0 2 * * ?");
            request.setJob(CleanUpReportJob.class.getName());
            scheduleService.addSchedule(request);
            scheduleService.addOrUpdateCronJob(request,
                    CleanUpReportJob.getJobKey(projectId),
                    CleanUpReportJob.getTriggerKey(projectId),
                    CleanUpReportJob.class);
        });

    }


    /**
     * 获取配置信息
     *
     * @param request
     * @return
     */
    public Map<String, Object> get(ProjectApplicationRequest request, List<String> types) {
        Map<String, Object> configMap = new HashMap<>();
        ProjectApplicationExample projectApplicationExample = new ProjectApplicationExample();
        projectApplicationExample.createCriteria().andProjectIdEqualTo(request.getProjectId()).andTypeIn(types);
        List<ProjectApplication> applicationList = projectApplicationMapper.selectByExample(projectApplicationExample);
        if (CollectionUtils.isNotEmpty(applicationList)) {
            configMap = applicationList.stream().collect(Collectors.toMap(ProjectApplication::getType, ProjectApplication::getTypeValue));
            return configMap;
        }
        return configMap;
    }


    /**
     * 获取 项目成员（脚本审核人）
     *
     * @param projectId
     * @return
     */
    public List<User> getProjectUserList(String projectId) {
        return extProjectUserRoleMapper.getProjectUserList(projectId);
    }


    /**
     * 获取平台列表
     *
     * @return
     */
    public List<OptionDTO> getPlatformOptions(String organizationId) {
        ServiceIntegrationExample example = new ServiceIntegrationExample();
        example.createCriteria().andOrganizationIdEqualTo(organizationId).andEnableEqualTo(true);
        List<ServiceIntegration> serviceIntegrations = serviceIntegrationMapper.selectByExample(example);
        List<OptionDTO> options = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(serviceIntegrations)) {
            List<String> pluginIds = serviceIntegrations.stream().map(ServiceIntegration::getPluginId).collect(Collectors.toList());
            options = extPluginMapper.selectPluginOptions(pluginIds);
            return options;
        }
        return options;
    }

    public Object getPluginScript(String pluginId) {
        this.checkResourceExist(pluginId);
        AbstractPlatformPlugin platformPlugin = (AbstractPlatformPlugin) pluginLoadService.getMsPluginManager().getPlugin(pluginId).getPlugin();
        return pluginLoadService.getPluginScriptContent(pluginId, platformPlugin.getProjectScriptId());
    }

    private Plugin checkResourceExist(String id) {
        return ServiceUtils.checkResourceExist(pluginMapper.selectByPrimaryKey(id), "permission.system_plugin.name");
    }


    /**
     * 同步缺陷配置
     *
     * @param projectId
     * @param configs
     */
    public void syncBugConfig(String projectId, Map<String, String> configs, String currentUser) {
        List<ProjectApplication> bugSyncConfigs = configs.entrySet().stream().map(config -> new ProjectApplication(projectId, ProjectApplicationType.BUG.BUG_SYNC.name() + "_" + config.getKey().toUpperCase(), config.getValue())).collect(Collectors.toList());
        //处理同步缺陷定时任务配置
        doSaveOrUpdateSchedule(bugSyncConfigs, projectId, currentUser);
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andTypeLike(ProjectApplicationType.BUG.BUG_SYNC.name() + "%");
        if (projectApplicationMapper.countByExample(example) > 0) {
            example.clear();
            example.createCriteria().andTypeLike(ProjectApplicationType.BUG.BUG_SYNC.name() + "%");
            projectApplicationMapper.deleteByExample(example);
            projectApplicationMapper.batchInsert(bugSyncConfigs);
        } else {
            projectApplicationMapper.batchInsert(bugSyncConfigs);
        }
    }

    private void doSaveOrUpdateSchedule(List<ProjectApplication> bugSyncConfigs, String projectId, String currentUser) {
        List<ProjectApplication> syncCron = bugSyncConfigs.stream().filter(config -> config.getType().equals(ProjectApplicationType.BUG.BUG_SYNC.name() + "_" + ProjectApplicationType.BUG_SYNC_CONFIG.CRON_EXPRESSION.name())).collect(Collectors.toList());
        List<ProjectApplication> syncEnable = bugSyncConfigs.stream().filter(config -> config.getType().equals(ProjectApplicationType.BUG.BUG_SYNC.name() + "_" + ProjectApplicationType.BUG_SYNC_CONFIG.SYNC_ENABLE.name())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(syncCron)) {
            Boolean enable = Boolean.valueOf(syncEnable.get(0).getTypeValue());
            String typeValue = syncCron.get(0).getTypeValue();
            Schedule schedule = scheduleService.getScheduleByResource(projectId, BugSyncJob.class.getName());
            Optional<Schedule> optional = Optional.ofNullable(schedule);
            optional.ifPresentOrElse(s -> {
                s.setEnable(enable);
                s.setValue(typeValue);
                scheduleService.editSchedule(s);
                scheduleService.addOrUpdateCronJob(s,
                        BugSyncJob.getJobKey(projectId),
                        BugSyncJob.getTriggerKey(projectId),
                        BugSyncJob.class);
            }, () -> {
                Schedule request = new Schedule();
                request.setName("Bug Sync Job");
                request.setResourceId(projectId);
                request.setKey(projectId);
                request.setProjectId(projectId);
                request.setEnable(enable);
                request.setCreateUser(currentUser);
                request.setType(ScheduleType.CRON.name());
                // 每天凌晨2点执行清理任务
                request.setValue(typeValue);
                request.setJob(BugSyncJob.class.getName());
                scheduleService.addSchedule(request);
                scheduleService.addOrUpdateCronJob(request,
                        BugSyncJob.getJobKey(projectId),
                        BugSyncJob.getTriggerKey(projectId),
                        BugSyncJob.class);
            });
        }
    }


    /**
     * 获取同步缺陷配置
     *
     * @param projectId
     * @return
     */
    public Map<String, String> getBugConfigInfo(String projectId) {
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andTypeLike(ProjectApplicationType.BUG.BUG_SYNC.name() + "_%");
        List<ProjectApplication> list = projectApplicationMapper.selectByExample(example);
        Map<String, String> collect = new HashMap<>();
        if (CollectionUtils.isNotEmpty(list)) {
            list.stream().forEach(config -> {
                collect.put(config.getType().replace(ProjectApplicationType.BUG.BUG_SYNC.name() + "_", "").toLowerCase(), config.getTypeValue());
            });
        }
        return collect;
    }


    /**
     * 测试计划 日志
     *
     * @param applications
     * @return
     */
    public List<LogDTO> updateTestPlanLog(List<ProjectApplication> applications) {
        return delLog(applications, OperationLogModule.PROJECT_PROJECT_MANAGER, "测试计划配置");
    }


    /**
     * UI 日志
     *
     * @param applications
     * @return
     */
    public List<LogDTO> updateUiLog(List<ProjectApplication> applications) {
        return delLog(applications, OperationLogModule.PROJECT_PROJECT_MANAGER, "UI配置");
    }

    /**
     * 性能测试 日志
     *
     * @param applications
     * @return
     */
    public List<LogDTO> updatePerformanceLog(List<ProjectApplication> applications) {
        return delLog(applications, OperationLogModule.PROJECT_PROJECT_MANAGER, "性能测试配置");
    }

    /**
     * 接口测试 日志
     *
     * @param applications
     * @return
     */
    public List<LogDTO> updateApiLog(List<ProjectApplication> applications) {
        return delLog(applications, OperationLogModule.PROJECT_PROJECT_MANAGER, "接口测试配置");
    }


    /**
     * 用例管理 日志
     *
     * @param applications
     * @return
     */
    public List<LogDTO> updateCaseLog(List<ProjectApplication> applications) {
        return delLog(applications, OperationLogModule.PROJECT_PROJECT_MANAGER, "用例管理配置");
    }

    /**
     * 工作台 日志
     *
     * @param applications
     * @return
     */
    public List<LogDTO> updateWorkstationLog(List<ProjectApplication> applications) {
        return delLog(applications, OperationLogModule.PROJECT_PROJECT_MANAGER, "工作台配置");
    }

    private List<LogDTO> delLog(List<ProjectApplication> applications, String module, String content) {
        List<LogDTO> logs = new ArrayList<>();
        applications.forEach(application -> {
            ProjectApplicationExample example = new ProjectApplicationExample();
            example.createCriteria().andTypeEqualTo(application.getType()).andProjectIdEqualTo(application.getProjectId());
            List<ProjectApplication> list = projectApplicationMapper.selectByExample(example);
            LogDTO dto = new LogDTO(
                    application.getProjectId(),
                    "",
                    OperationLogConstants.SYSTEM,
                    null,
                    OperationLogType.UPDATE.name(),
                    module,
                    content);
            dto.setOriginalValue(JSON.toJSONBytes(list));
            logs.add(dto);
        });
        return logs;
    }


    /**
     * 缺陷同步配置 日志
     *
     * @param projectId
     * @param configs
     * @return
     */
    public LogDTO updateBugSyncLog(String projectId, Map<String, String> configs) {
        Map<String, String> originConfig = getBugConfigInfo(projectId);
        LogDTO dto = new LogDTO(
                projectId,
                "",
                OperationLogConstants.SYSTEM,
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.PROJECT_PROJECT_MANAGER,
                "同步缺陷");
        dto.setModifiedValue(JSON.toJSONBytes(configs));
        dto.setOriginalValue(JSON.toJSONBytes(originConfig));
        return dto;
    }


    /**
     * 关联需求配置 日志
     *
     * @param projectId
     * @param configs
     * @return
     */
    public LogDTO updateRelatedRequirementsLog(String projectId, Map<String, String> configs) {
        Map<String, String> originConfig = getRelatedConfigInfo(projectId);
        LogDTO dto = new LogDTO(
                projectId,
                "",
                OperationLogConstants.SYSTEM,
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.PROJECT_PROJECT_MANAGER,
                "关联需求");
        dto.setModifiedValue(JSON.toJSONBytes(configs));
        dto.setOriginalValue(JSON.toJSONBytes(originConfig));
        return dto;
    }


    /**
     * 获取菜单列表
     *
     * @param projectId
     * @return
     */
    public List<ModuleDTO> getModuleSetting(String projectId) {
        String moduleSetting = extProjectMapper.getModuleSetting(projectId);
        Map<String, Boolean> moduleMap = new HashMap<>();
        List<ModuleDTO> moduleDTOList = new ArrayList<>();
        if (StringUtils.isNotEmpty(moduleSetting)) {
            ProjectApplicationExample example = new ProjectApplicationExample();
            JSON.parseArray(moduleSetting).forEach(module -> {
                example.clear();
                example.createCriteria().andTypeEqualTo(String.valueOf(module)).andProjectIdEqualTo(projectId);
                List<ProjectApplication> applications = projectApplicationMapper.selectByExample(example);
                if (CollectionUtils.isNotEmpty(applications)) {
                    moduleMap.put(String.valueOf(module), Boolean.valueOf(applications.get(0).getTypeValue()));
                } else {
                    moduleMap.put(String.valueOf(module), Boolean.TRUE);
                }
            });
            moduleDTOList = moduleMap.entrySet().stream().map(entry -> new ModuleDTO(entry.getKey(), entry.getValue())).collect(Collectors.toList());
            Map<String, Integer> module = ModuleSortUtils.getHashMap();
            if (CollectionUtils.isNotEmpty(moduleDTOList)) {
                moduleDTOList.sort((o1, o2) -> module.getOrDefault(o1.getModule(), Integer.MAX_VALUE) - module.getOrDefault(o2.getModule(), Integer.MAX_VALUE));
            }
        }
        return moduleDTOList;
    }


    /**
     * 用例关联需求配置
     *
     * @param projectId
     * @param configs
     */
    public void updateRelated(String projectId, Map<String, String> configs) {
        List<ProjectApplication> relatedConfigs = configs.entrySet().stream().map(config -> new ProjectApplication(projectId, ProjectApplicationType.CASE_RELATED_CONFIG.CASE_RELATED.name() + "_" + config.getKey().toUpperCase(), config.getValue())).collect(Collectors.toList());
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andTypeLike(ProjectApplicationType.CASE_RELATED_CONFIG.CASE_RELATED.name() + "%");
        if (projectApplicationMapper.countByExample(example) > 0) {
            example.clear();
            example.createCriteria().andTypeLike(ProjectApplicationType.CASE_RELATED_CONFIG.CASE_RELATED.name() + "%");
            projectApplicationMapper.deleteByExample(example);
            projectApplicationMapper.batchInsert(relatedConfigs);
        } else {
            projectApplicationMapper.batchInsert(relatedConfigs);
        }
    }

    /**
     * 获取用例关联需求配置
     *
     * @param projectId
     * @return
     */
    public Map<String, String> getRelatedConfigInfo(String projectId) {
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andTypeLike(ProjectApplicationType.CASE_RELATED_CONFIG.CASE_RELATED.name() + "_%");
        List<ProjectApplication> list = projectApplicationMapper.selectByExample(example);
        Map<String, String> collect = new HashMap<>();
        if (CollectionUtils.isNotEmpty(list)) {
            list.stream().forEach(config -> {
                collect.put(config.getType().replace(ProjectApplicationType.CASE_RELATED_CONFIG.CASE_RELATED.name() + "_", "").toLowerCase(), config.getTypeValue());
            });
        }
        return collect;
    }


    /**
     * 校验插件key
     *
     * @param pluginId
     * @param configs
     */
    public void validateProjectConfig(String pluginId, Map configs) {
        Platform platform = this.getPlatform(pluginId);
        platform.validateProjectConfig(JSON.toJSONString(configs));
    }

    private Platform getPlatform(String pluginId) {
        ServiceIntegrationExample example = new ServiceIntegrationExample();
        example.createCriteria().andPluginIdEqualTo(pluginId);
        List<ServiceIntegration> serviceIntegrations = serviceIntegrationMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isEmpty(serviceIntegrations)) {
            throw new MSException(NOT_FOUND);
        }
        return platformPluginService.getPlatform(pluginId, serviceIntegrations.get(0).getOrganizationId(), new String(serviceIntegrations.get(0).getConfiguration()));
    }

    public int getFakeErrorList(String projectId) {
        FakeErrorExample example = new FakeErrorExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        long l = fakeErrorMapper.countByExample(example);
        return (int) l;
    }
}
