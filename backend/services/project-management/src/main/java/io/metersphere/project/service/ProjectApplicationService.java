package io.metersphere.project.service;

import io.metersphere.plugin.platform.spi.AbstractPlatformPlugin;
import io.metersphere.plugin.platform.spi.Platform;
import io.metersphere.plugin.sdk.spi.MsPlugin;
import io.metersphere.project.domain.FakeErrorExample;
import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.domain.ProjectApplicationExample;
import io.metersphere.project.dto.ModuleDTO;
import io.metersphere.project.job.BugSyncJob;
import io.metersphere.project.mapper.*;
import io.metersphere.project.request.ProjectApplicationRequest;
import io.metersphere.project.utils.ModuleSortUtils;
import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.constants.ScheduleResourceType;
import io.metersphere.sdk.constants.ScheduleType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.mapper.PluginMapper;
import io.metersphere.system.mapper.ServiceIntegrationMapper;
import io.metersphere.system.schedule.ScheduleService;
import io.metersphere.system.service.PlatformPluginService;
import io.metersphere.system.service.PluginLoadService;
import io.metersphere.system.service.ServiceIntegrationService;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static io.metersphere.system.controller.handler.result.MsHttpResultCode.NOT_FOUND;

@Service
@Transactional(rollbackFor = Exception.class)
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
    private PluginMapper pluginMapper;

    @Resource
    private PluginLoadService pluginLoadService;

    @Resource
    private ExtProjectMapper extProjectMapper;

    @Resource
    private PlatformPluginService platformPluginService;

    @Resource
    private FakeErrorMapper fakeErrorMapper;

    @Resource
    private ServiceIntegrationService serviceIntegrationService;
    @Resource
    private ProjectMapper projectMapper;

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

    public void createOrUpdateConfig(ProjectApplication application) {
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
        //TODO 后续清理（合并完没问题再清理）===== 清理报告只有一个定时任务，项目配置时不需要在添加定时任务了
        /*String type = application.getType();
        if (StringUtils.equals(type, ProjectApplicationType.TEST_PLAN.TEST_PLAN_CLEAN_REPORT.name())
                || StringUtils.equals(type, ProjectApplicationType.UI.UI_CLEAN_REPORT.name())
                || StringUtils.equals(type, ProjectApplicationType.LOAD_TEST.LOAD_TEST_CLEAN_REPORT.name())
                || StringUtils.equals(type, ProjectApplicationType.API.API_CLEAN_REPORT.name())) {
            //清除 测试计划/UI测试/性能测试/接口测试 报告 定时任务
            this.doHandleSchedule(application, currentUser);
        }*/
    }

    /*private void doHandleSchedule(ProjectApplication application, String currentUser) {
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
            request.setResourceType(ScheduleResourceType.CLEAN_REPORT.name());
            scheduleService.addSchedule(request);
            scheduleService.addOrUpdateCronJob(request,
                    CleanUpReportJob.getJobKey(projectId),
                    CleanUpReportJob.getTriggerKey(projectId),
                    CleanUpReportJob.class);
        });

    }*/


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
        Set<String> orgPluginIds = platformPluginService.getOrgEnabledPlatformPlugins(organizationId)
                .stream()
                .map(Plugin::getId)
                .collect(Collectors.toSet());
        // 查询服务集成中启用并且支持第三方模板的插件
        List<ServiceIntegration> plusins = serviceIntegrationService.getServiceIntegrationByOrgId(organizationId)
                .stream()
                .filter(serviceIntegration -> {
                    return serviceIntegration.getEnable()    // 服务集成开启
                            && orgPluginIds.contains(serviceIntegration.getPluginId());  // 该服务集成对应的插件有权限
                }).collect(Collectors.toList());
        List<OptionDTO> options = new ArrayList<>();
        plusins.forEach(serviceIntegration -> {
            PluginWrapper pluginWrapper = pluginLoadService.getPluginWrapper(serviceIntegration.getPluginId());
            MsPlugin plugin = (MsPlugin) pluginWrapper.getPlugin();
            if (plugin instanceof AbstractPlatformPlugin) {
                OptionDTO optionDTO = new OptionDTO();
                optionDTO.setName(plugin.getName());
                optionDTO.setId(pluginWrapper.getPluginId());
                options.add(optionDTO);
            }
        });
        return options;
    }

    public Object getBugPluginScript(String pluginId) {
        this.checkResourceExist(pluginId);
        AbstractPlatformPlugin platformPlugin = (AbstractPlatformPlugin) pluginLoadService.getMsPluginManager().getPlugin(pluginId).getPlugin();
        return pluginLoadService.getPluginScriptContent(pluginId, platformPlugin.getProjectBugScriptId());
    }

    public Object getDemandPluginScript(String pluginId) {
        this.checkResourceExist(pluginId);
        AbstractPlatformPlugin platformPlugin = (AbstractPlatformPlugin) pluginLoadService.getMsPluginManager().getPlugin(pluginId).getPlugin();
        return pluginLoadService.getPluginScriptContent(pluginId, platformPlugin.getProjectDemandScriptId());
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
                request.setResourceType(ScheduleResourceType.BUG_SYNC.name());
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
     * @param application
     * @return
     */
    public LogDTO updateTestPlanLog(ProjectApplication application) {
        return delLog(application, OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT, "测试计划配置");
    }


    /**
     * UI 日志
     *
     * @param application
     * @return
     */
    public LogDTO updateUiLog(ProjectApplication application) {
        return delLog(application, OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT, "UI配置");
    }

    /**
     * 性能测试 日志
     *
     * @param application
     * @return
     */
    public LogDTO updatePerformanceLog(ProjectApplication application) {
        return delLog(application, OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT, "性能测试配置");
    }

    /**
     * 接口测试 日志
     *
     * @param application
     * @return
     */
    public LogDTO updateApiLog(ProjectApplication application) {
        return delLog(application, OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT, "接口测试配置");
    }


    /**
     * 用例管理 日志
     *
     * @param application
     * @return
     */
    public LogDTO updateCaseLog(ProjectApplication application) {
        return delLog(application, OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT, "功能测试配置");
    }

    /**
     * 工作台 日志
     *
     * @param application
     * @return
     */
    public LogDTO updateWorkstationLog(ProjectApplication application) {
        return delLog(application, OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT, "工作台配置");
    }

    public LogDTO updateBugLog(ProjectApplication application) {
        return delLog(application, OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT, "缺陷管理配置");
    }

    private LogDTO delLog(ProjectApplication application, String module, String content) {
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
        return dto;
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


    /**
     * 获取缺陷项目配置信息
     *
     * @param projectId
     * @return
     */
    public String getProjectBugThirdPartConfig(String projectId) {
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andTypeLike(ProjectApplicationType.BUG.BUG_SYNC.name() + "_" + ProjectApplicationType.PLATFORM_BUG_CONFIG.BUG_PLATFORM_CONFIG.name());
        List<ProjectApplication> list = projectApplicationMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0).getTypeValue().replaceAll("\\\\", "");
        }
        return null;
    }


    /**
     * 获取需求项目配置信息
     *
     * @param projectId
     * @return
     */
    public String getProjectDemandThirdPartConfig(String projectId) {
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andTypeLike(ProjectApplicationType.CASE_RELATED_CONFIG.CASE_RELATED.name() + "_" + ProjectApplicationType.PLATFORM_DEMAND_CONFIG.DEMAND_PLATFORM_CONFIG.name());
        List<ProjectApplication> list = projectApplicationMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0).getTypeValue().replaceAll("\\\\", "");
        }
        return null;
    }

    public ProjectApplication getByType(String projectId, String type) {
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andTypeEqualTo(type);
        List<ProjectApplication> projectApplications = projectApplicationMapper.selectByExample(example);
        return CollectionUtils.isEmpty(projectApplications) ? null : projectApplications.get(0);
    }

    /**
     * 获取项目所属平台
     *
     * @param projectId 项目ID
     * @return 项目所属平台
     */
    public String getPlatformName(String projectId) {
        ProjectApplication platformEnableConfig = getByType(projectId, ProjectApplicationType.BUG.BUG_SYNC.name() + "_" + ProjectApplicationType.BUG_SYNC_CONFIG.SYNC_ENABLE.name());
        ProjectApplication platformKeyConfig = getByType(projectId, ProjectApplicationType.BUG.BUG_SYNC.name() + "_PLATFORM_KEY");
        boolean isEnable = platformEnableConfig != null && Boolean.parseBoolean(platformEnableConfig.getTypeValue()) && platformKeyConfig != null;
        if (!isEnable) {
            return "Local";
        } else {
            ServiceIntegration serviceIntegration = getPlatformServiceIntegrationWithSyncOrDemand(projectId, true);
            if (serviceIntegration == null) {
                // 项目未配置第三方平台
                return "Local";
            }
        }
        return getPluginName(platformKeyConfig.getTypeValue());
    }

    /**
     * 过滤出能够同步的项目
     *
     * @param projectIds 项目ID集合
     * @return 同步的项目ID集合
     */
    public List<String> filterNeedSyncProject(List<String> projectIds) {
        Iterator<String> iterator = projectIds.iterator();
        while (iterator.hasNext()) {
            String projectId = iterator.next();
            ServiceIntegration serviceIntegration = getPlatformServiceIntegrationWithSyncOrDemand(projectId, true);
            String platformName = getPlatformName(projectId);
            if (serviceIntegration == null || StringUtils.equals("Local", platformName)) {
                // 项目未配置第三方平台 或者 项目同步配置为Local
                iterator.remove();
            }
        }
        return projectIds;
    }

    /**
     * 获取项目同步机制
     *
     * @param projectId 项目ID
     * @return 项目所属平台
     */
    public boolean isPlatformSyncMethodByIncrement(String projectId) {
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andTypeEqualTo(ProjectApplicationType.BUG.BUG_SYNC.name() + "_MECHANISM");
        List<ProjectApplication> list = projectApplicationMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(list)) {
            return StringUtils.equals(list.get(0).getTypeValue(), "increment");
        } else {
            return false;
        }
    }

    /**
     * 查询插件具体的服务集成信息(缺陷PLATFORM_KEY, 需求PLATFORM_KEY)
     *
     * @param projectId 项目ID
     * @param isSync    是否缺陷同步配置
     * @return 插件服务集成信息
     */
    public ServiceIntegration getPlatformServiceIntegrationWithSyncOrDemand(String projectId, boolean isSync) {
        // 是否开启项目配置第三方平台
        ProjectApplication platformEnableConfig;
        ProjectApplication platformKeyConfig;
        if (isSync) {
            platformEnableConfig = getByType(projectId, ProjectApplicationType.BUG.BUG_SYNC.name() + "_" + ProjectApplicationType.BUG_SYNC_CONFIG.SYNC_ENABLE.name());
            platformKeyConfig = getByType(projectId, ProjectApplicationType.BUG.BUG_SYNC.name() + "_PLATFORM_KEY");
        } else {
            platformEnableConfig = getByType(projectId, ProjectApplicationType.CASE_RELATED_CONFIG.CASE_RELATED.name() + "_" + ProjectApplicationType.CASE_RELATED_CONFIG.CASE_ENABLE.name());
            platformKeyConfig = getByType(projectId, ProjectApplicationType.CASE_RELATED_CONFIG.CASE_RELATED.name() + "_PLATFORM_KEY");
        }

        boolean isEnable = platformEnableConfig != null && Boolean.parseBoolean(platformEnableConfig.getTypeValue()) && platformKeyConfig != null;
        if (!isEnable) {
            return null;
        }
        Project project = projectMapper.selectByPrimaryKey(projectId);
        // 查询组织下有权限的插件
        Set<String> orgPluginIds = platformPluginService.getOrgEnabledPlatformPlugins(project.getOrganizationId())
                .stream()
                .map(Plugin::getId)
                .collect(Collectors.toSet());
        // 查询服务集成中启用并且支持第三方模板的插件
        return serviceIntegrationService.getServiceIntegrationByOrgId(project.getOrganizationId())
                .stream()
                .filter(serviceIntegration -> {
                    return serviceIntegration.getEnable()    // 服务集成开启
                            && orgPluginIds.contains(serviceIntegration.getPluginId())
                            && StringUtils.equals(serviceIntegration.getPluginId(), platformKeyConfig.getTypeValue());  // 该服务集成对应的插件有权限
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取项目同步配置或需求配置的所属平台
     *
     * @param projectId 项目ID
     * @param isSync    是否同步
     * @return 平台
     */
    public Platform getPlatform(String projectId, boolean isSync) {
        // 第三方平台状态流
        ServiceIntegration serviceIntegration = getPlatformServiceIntegrationWithSyncOrDemand(projectId, isSync);
        if (serviceIntegration == null) {
            // 项目未配置第三方平台
            throw new MSException(Translator.get("third_party_not_config"));
        }
        return platformPluginService.getPlatform(serviceIntegration.getPluginId(), serviceIntegration.getOrganizationId(),
                new String(serviceIntegration.getConfiguration()));
    }

    private String getPluginName(String platformKey) {
        PluginWrapper pluginWrapper = pluginLoadService.getPluginWrapper(platformKey);
        if (pluginWrapper == null) {
            // 插件未找到
            return "Local";
        }
        MsPlugin plugin = (MsPlugin) pluginWrapper.getPlugin();
        if (plugin == null) {
            // 插件未找到
            return "Local";
        }
        return plugin.getName();
    }
}
