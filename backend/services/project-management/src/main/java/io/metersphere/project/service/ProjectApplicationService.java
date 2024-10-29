package io.metersphere.project.service;

import io.metersphere.plugin.platform.spi.AbstractPlatformPlugin;
import io.metersphere.plugin.platform.spi.Platform;
import io.metersphere.plugin.sdk.spi.MsPlugin;
import io.metersphere.project.domain.*;
import io.metersphere.project.dto.ModuleDTO;
import io.metersphere.project.mapper.*;
import io.metersphere.project.request.ProjectApplicationRequest;
import io.metersphere.project.utils.ModuleSortUtils;
import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.dto.api.result.MsRegexDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.mapper.PluginMapper;
import io.metersphere.system.mapper.TestResourcePoolMapper;
import io.metersphere.system.service.*;
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
    private ExtProjectUserRoleMapper extProjectUserRoleMapper;

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
    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;
    @Resource
    private CommonProjectPoolService commonProjectPoolService;

    /**
     * 更新配置信息
     *
     * @param application
     * @return
     */
    public void update(ProjectApplication application, String currentUser) {
        //更新应用配置状态 改变项目是否允许对接三方平台，不影响定时任务状态 只能任务中心开启/关闭 暂时注释下面一行
        //this.doBeforeUpdate(application, currentUser);
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
        BaseBugScheduleService baseBugScheduleService = CommonBeanFactory.getBean(BaseBugScheduleService.class);
        if (StringUtils.equals(ProjectApplicationType.BUG.BUG_SYNC.name() + "_" + ProjectApplicationType.BUG_SYNC_CONFIG.SYNC_ENABLE.name(),
                application.getType()) && baseBugScheduleService != null) {
            // 缺陷同步配置开启或关闭
            baseBugScheduleService.enableOrNotBugSyncSchedule(application.getProjectId(), currentUser, Boolean.valueOf(application.getTypeValue()));
        }
        BaseDemandScheduleService baseDemandScheduleService = CommonBeanFactory.getBean(BaseDemandScheduleService.class);
        if (StringUtils.equals(ProjectApplicationType.CASE_RELATED_CONFIG.CASE_RELATED.name() + "_" + ProjectApplicationType.CASE_RELATED_CONFIG.CASE_ENABLE.name(),
                application.getType()) && baseDemandScheduleService != null && !Boolean.parseBoolean(application.getTypeValue())) {
            // 需求同步配置关闭
            baseDemandScheduleService.enableOrNotDemandSyncSchedule(application.getProjectId(), currentUser, Boolean.valueOf(application.getTypeValue()));
        }
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
            putResourcePool(request.getProjectId(), configMap, request.getType());
            return configMap;
        }
        putResourcePool(request.getProjectId(), configMap, request.getType());
        return configMap;
    }

    public void putResourcePool(String projectId, Map<String, Object> configMap, String type) {
        //如果存在  需要判断  如果不存在  需要判断资源池在不在  是否还在关系表中 如果不存在  需要删除  并重新处理数据
        String poolType = null;
        String moduleType = null;
        if (StringUtils.isBlank(type)) {
            return;
        }
        if (type.equals("apiTest")) {
            poolType = ProjectApplicationType.API.API_RESOURCE_POOL_ID.name();
            moduleType = "api_test";
        }
        Project project = projectMapper.selectByPrimaryKey(projectId);
        List<TestResourcePool> testResourcePools = new ArrayList<>();
        if (project.getAllResourcePool()) {
            testResourcePools= commonProjectPoolService.getProjectAllPoolsByEffect(project);
        }
        if (StringUtils.isNotBlank(poolType) && StringUtils.isNotBlank(moduleType)) {
            if (configMap.containsKey(poolType)) {
                //如果是适用于所有的组织
                int count = 0;
                if (project.getAllResourcePool()) {
                    count = testResourcePools.size();
                }else {
                    TestResourcePoolExample example = new TestResourcePoolExample();
                    example.createCriteria().andIdEqualTo(configMap.get(poolType).toString()).andAllOrgEqualTo(true);
                    if (testResourcePoolMapper.countByExample(example) > 0) {
                        count = extProjectMapper.resourcePoolIsExist(configMap.get(poolType).toString(), projectId);
                    } else {
                        //指定组织  则需要关联组织-资源池的关系表  看看是否再全部存在
                        count = extProjectMapper.resourcePoolIsExistByOrg(configMap.get(poolType).toString(), projectId);
                    }
                }
                if (count == 0) {
                    configMap.remove(poolType);
                }
            }
            if (!configMap.containsKey(poolType)) {
                if (project.getAllResourcePool()){
                    if (CollectionUtils.isNotEmpty(testResourcePools)) {
                        testResourcePools.sort(Comparator.comparing(TestResourcePool::getId));
                        configMap.put(poolType, testResourcePools.getFirst().getId());
                    }

                } else {
                    List<ProjectTestResourcePool> projectTestResourcePools = extProjectMapper.getResourcePool(projectId);
                    if (CollectionUtils.isNotEmpty(projectTestResourcePools)) {
                        projectTestResourcePools.sort(Comparator.comparing(ProjectTestResourcePool::getTestResourcePoolId));
                        configMap.put(poolType, projectTestResourcePools.getFirst().getTestResourcePoolId());
                    }
                }
            }
        }

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
                }).toList();
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
    public void syncBugConfig(String projectId, Map<String, Object> configs, String currentUser) {
        List<ProjectApplication> bugSyncConfigs = configs.entrySet().stream().map(config -> new ProjectApplication(projectId, ProjectApplicationType.BUG.BUG_SYNC.name() + "_" + config.getKey().toUpperCase(),
                (config.getValue() instanceof String) ? config.getValue().toString() : JSON.toJSONString(config.getValue()))).toList();
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andTypeLike(ProjectApplicationType.BUG.BUG_SYNC.name() + "%");
        if (projectApplicationMapper.countByExample(example) > 0) {
            example.clear();
            example.createCriteria().andProjectIdEqualTo(projectId).andTypeLike(ProjectApplicationType.BUG.BUG_SYNC.name() + "%");
            projectApplicationMapper.deleteByExample(example);
            projectApplicationMapper.batchInsert(bugSyncConfigs);
        } else {
            projectApplicationMapper.batchInsert(bugSyncConfigs);
        }
        // 更新缺陷定时任务配置
        BaseBugScheduleService baseBugScheduleService = CommonBeanFactory.getBean(BaseBugScheduleService.class);
        if (baseBugScheduleService != null) {
            baseBugScheduleService.updateBugSyncScheduleConfig(bugSyncConfigs, projectId, currentUser);
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
     * 任务中心 日志
     *
     * @param application 配置项
     * @return 日志
     */
    public LogDTO updateTaskLog(ProjectApplication application) {
        return delLog(application, OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT, "任务中心配置");
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
                OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT,
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
                OperationLogModule.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT,
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
        // 任务中心设置项默认展示
        moduleMap.put("taskCenter", true);
        if (StringUtils.isNotEmpty(moduleSetting)) {
            ProjectApplicationExample example = new ProjectApplicationExample();
            JSON.parseArray(moduleSetting).forEach(module -> {
                example.clear();
                example.createCriteria().andTypeEqualTo(String.valueOf(module)).andProjectIdEqualTo(projectId);
                List<ProjectApplication> applications = projectApplicationMapper.selectByExample(example);
                if (CollectionUtils.isNotEmpty(applications)) {
                    moduleMap.put(String.valueOf(module), Boolean.valueOf(applications.getFirst().getTypeValue()));
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
     * @param projectId 项目ID
     * @param configs   关联需求配置信息
     */
    public void updateRelated(String projectId, Map<String, String> configs, String currentUser) {
        List<ProjectApplication> relatedConfigs = configs.entrySet().stream().map(config -> new ProjectApplication(projectId, ProjectApplicationType.CASE_RELATED_CONFIG.CASE_RELATED.name() + "_" + config.getKey().toUpperCase(), config.getValue())).collect(Collectors.toList());
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andTypeLike(ProjectApplicationType.CASE_RELATED_CONFIG.CASE_RELATED.name() + "%");
        if (projectApplicationMapper.countByExample(example) > 0) {
            example.clear();
            example.createCriteria().andProjectIdEqualTo(projectId).andTypeLike(ProjectApplicationType.CASE_RELATED_CONFIG.CASE_RELATED.name() + "%");
            projectApplicationMapper.deleteByExample(example);
            projectApplicationMapper.batchInsert(relatedConfigs);
        } else {
            projectApplicationMapper.batchInsert(relatedConfigs);
        }
        // 更新需求定时任务配置
        BaseDemandScheduleService baseDemandScheduleService = CommonBeanFactory.getBean(BaseDemandScheduleService.class);
        if (baseDemandScheduleService != null) {
            baseDemandScheduleService.updateDemandSyncScheduleConfig(relatedConfigs, projectId, currentUser);
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
    public void validateProjectConfig(String pluginId, Map configs, String organizationId) {
        Platform platform = this.getPlatform(pluginId, organizationId);
        platform.validateProjectConfig(JSON.toJSONString(configs));
    }

    private Platform getPlatform(String pluginId, String organizationId) {
        Set<String> orgPluginIds = platformPluginService.getOrgEnabledPlatformPlugins(organizationId)
                .stream()
                .map(Plugin::getId)
                .collect(Collectors.toSet());
        // 查询服务集成中启用并且支持第三方模板的插件
        ServiceIntegration integration = serviceIntegrationService.getServiceIntegrationByOrgId(organizationId)
                .stream()
                .filter(serviceIntegration -> serviceIntegration.getEnable()    // 服务集成开启
                        && orgPluginIds.contains(pluginId)
                        && StringUtils.equals(serviceIntegration.getPluginId(), pluginId))
                .findFirst()
                .orElse(null);
        if (integration == null) {
            throw new MSException(NOT_FOUND);
        }
        return platformPluginService.getPlatform(pluginId, integration.getOrganizationId(), new String(integration.getConfiguration()));
    }

    public int getFakeErrorList(String projectId) {
        FakeErrorExample example = new FakeErrorExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        long l = fakeErrorMapper.countByExample(example);
        return (int) l;
    }

    public List<MsRegexDTO> get(List<String> projectIds) {
        List<MsRegexDTO> regexList = new ArrayList<>();
        if (CollectionUtils.isEmpty(projectIds)) {
            return regexList;
        }
        FakeErrorExample example = new FakeErrorExample();
        example.createCriteria().andProjectIdIn(projectIds).andEnableEqualTo(true);
        List<FakeError> fakeErrors = fakeErrorMapper.selectByExample(example);
        fakeErrors.forEach(item -> {
            MsRegexDTO regexConfig = new MsRegexDTO();
            BeanUtils.copyBean(regexConfig, item);
            regexList.add(regexConfig);
        });
        return regexList;
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
            return list.getFirst().getTypeValue().replaceAll("\\\\", "");
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
            return list.getFirst().getTypeValue().replaceAll("\\\\", "");
        }
        return null;
    }

    public ProjectApplication getByType(String projectId, String type) {
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andTypeEqualTo(type);
        List<ProjectApplication> projectApplications = projectApplicationMapper.selectByExample(example);
        return CollectionUtils.isEmpty(projectApplications) ? null : projectApplications.getFirst();
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
     * 获取项目所属平台
     *
     * @param projectId 项目ID
     * @return 项目所属平台
     */
    public String getDemandPlatformId(String projectId) {
        ProjectApplication platformEnableConfig = getByType(projectId, ProjectApplicationType.CASE_RELATED_CONFIG.CASE_RELATED.name() + "_" + ProjectApplicationType.CASE_RELATED_CONFIG.CASE_ENABLE.name());
        ProjectApplication platformKeyConfig = getByType(projectId, ProjectApplicationType.CASE_RELATED_CONFIG.CASE_RELATED.name() + "_PLATFORM_KEY");
        boolean isEnable = platformEnableConfig != null && Boolean.parseBoolean(platformEnableConfig.getTypeValue()) && platformKeyConfig != null;
        if (!isEnable) {
            return "Metersphere";
        } else {
            ServiceIntegration serviceIntegration = getPlatformServiceIntegrationWithSyncOrDemand(projectId, false);
            if (serviceIntegration == null) {
                // 项目未配置第三方平台
                return "Metersphere";
            }
        }
        PluginWrapper pluginWrapper = pluginLoadService.getPluginWrapper(platformKeyConfig.getTypeValue());
        if (pluginWrapper == null) {
            // 插件未找到
            return "Metersphere";
        }
        return pluginWrapper.getPluginId();
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
            return StringUtils.equals(list.getFirst().getTypeValue(), "increment");
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

    public int getEnableFakeErrorList(String projectId) {
        FakeErrorExample example = new FakeErrorExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andEnableEqualTo(true);
        long l = fakeErrorMapper.countByExample(example);
        return (int) l;
    }
}
