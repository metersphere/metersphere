package io.metersphere.api.exec.scenario;

import io.metersphere.api.dto.EnvironmentType;
import io.metersphere.api.dto.RunModeConfigWithEnvironmentDTO;
import io.metersphere.api.dto.ScenarioEnv;
import io.metersphere.api.dto.automation.ApiScenarioDTO;
import io.metersphere.api.dto.automation.RunScenarioRequest;
import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.dto.definition.request.MsScenario;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.sampler.MsJDBCSampler;
import io.metersphere.api.dto.definition.request.sampler.MsTCPSampler;
import io.metersphere.api.dto.plan.TestPlanApiScenarioInfoDTO;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.dto.scenario.environment.item.MsScenarioEnv;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.ApiTestCaseMapper;
import io.metersphere.base.mapper.ApiTestEnvironmentMapper;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.plan.TestPlanApiScenarioMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.CommonConstants;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.constants.MsTestElementConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.environment.service.BaseEnvGroupProjectService;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.i18n.Translator;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.service.definition.ApiDefinitionService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApiScenarioEnvService {
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private BaseEnvGroupProjectService environmentGroupProjectService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ApiTestEnvironmentMapper apiTestEnvironmentMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    private BaseEnvironmentService apiTestEnvironmentService;

    public ScenarioEnv getApiScenarioEnv(String definition) {
        ScenarioEnv env = new ScenarioEnv();
        if (StringUtils.isEmpty(definition)) {
            return env;
        }
        List<MsTestElement> hashTree = GenerateHashTreeUtil.getScenarioHashTree(definition);
        if (CollectionUtils.isNotEmpty(hashTree)) {
            // 过滤掉禁用的步骤
            hashTree = hashTree.stream().filter(item -> item.isEnable()).collect(Collectors.toList());
        }
        List<Boolean> hasFullUrlList = new ArrayList<>();
        for (MsTestElement testElement : hashTree) {
            this.formatElement(testElement, env, hasFullUrlList);
            if (CollectionUtils.isNotEmpty(testElement.getHashTree())) {
                getHashTree(testElement.getHashTree(), env, hasFullUrlList);
            }
        }
        env.setFullUrl(!hasFullUrlList.contains(false));
        return env;
    }


    private void getHashTree(List<MsTestElement> tree, ScenarioEnv env, List<Boolean> hasFullUrlList) {
        try {
            // 过滤掉禁用的步骤
            tree = tree.stream().filter(item -> item.isEnable()).collect(Collectors.toList());
            for (MsTestElement element : tree) {
                this.formatElement(element, env, hasFullUrlList);
                if (CollectionUtils.isNotEmpty(element.getHashTree())) {
                    getHashTree(element.getHashTree(), env, hasFullUrlList);
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    private void formatElement(MsTestElement testElement, ScenarioEnv env, List<Boolean> hasFullUrlList) {
        if (StringUtils.equals(MsTestElementConstants.REF.name(), testElement.getReferenced())) {
            if (StringUtils.equals(testElement.getType(), ElementConstants.HTTP_SAMPLER)) {
                MsHTTPSamplerProxy http = (MsHTTPSamplerProxy) testElement;
                // 引用用例URL清空
                http.setUrl(StringUtils.equals(testElement.getRefType(), CommonConstants.CASE) ? null : http.getUrl());

                // 非全路径校验
                if (!StringUtils.equalsIgnoreCase(http.getReferenced(), ElementConstants.STEP_CREATED)
                        || (http.getIsRefEnvironment() != null && http.getIsRefEnvironment())) {
                    env.getProjectIds().add(http.getProjectId());
                    hasFullUrlList.add(false);
                }
            } else if (StringUtils.equals(testElement.getType(), ElementConstants.JDBC_SAMPLER)
                    || StringUtils.equals(testElement.getType(), ElementConstants.TCP_SAMPLER)) {
                if (StringUtils.isEmpty(testElement.getProjectId())) {
                    if (StringUtils.equals(testElement.getRefType(), CommonConstants.CASE)) {
                        ApiTestCase testCase = apiTestCaseMapper.selectByPrimaryKey(testElement.getId());
                        if (testCase != null) {
                            env.getProjectIds().add(testCase.getProjectId());
                            hasFullUrlList.add(false);
                        }
                    } else {
                        ApiDefinition apiDefinition = apiDefinitionService.get(testElement.getId());
                        if (apiDefinition != null) {
                            env.getProjectIds().add(apiDefinition.getProjectId());
                            hasFullUrlList.add(false);
                        }
                    }
                } else {
                    env.getProjectIds().add(testElement.getProjectId());
                    hasFullUrlList.add(false);
                }
            } else if (StringUtils.equals(testElement.getType(), ElementConstants.SCENARIO) && StringUtils.isEmpty(testElement.getProjectId())) {
                ApiScenarioWithBLOBs apiScenario = apiScenarioMapper.selectByPrimaryKey(testElement.getId());
                if (apiScenario != null) {
                    env.getProjectIds().add(apiScenario.getProjectId());
                    String scenarioDefinition = apiScenario.getScenarioDefinition();
                    JSONObject element = JSONUtil.parseObject(scenarioDefinition);
                    ElementUtil.dataFormatting(element);
                    testElement.setHashTree(GenerateHashTreeUtil.getScenarioHashTree(scenarioDefinition));
                }
            }
        } else {
            if (StringUtils.equals(testElement.getType(), ElementConstants.HTTP_SAMPLER)) {
                // 校验是否是全路径
                MsHTTPSamplerProxy httpSamplerProxy = (MsHTTPSamplerProxy) testElement;
                checkCustomEnv(env, httpSamplerProxy.isCustomizeReq(), httpSamplerProxy.getProjectId(), httpSamplerProxy.getIsRefEnvironment(), httpSamplerProxy.getReferenced(), hasFullUrlList);

            } else if (StringUtils.equals(testElement.getType(), ElementConstants.TCP_SAMPLER)) {
                MsTCPSampler tcpSampler = (MsTCPSampler) testElement;
                checkCustomEnv(env, tcpSampler.isCustomizeReq(), tcpSampler.getProjectId(), tcpSampler.getIsRefEnvironment(), tcpSampler.getReferenced(), hasFullUrlList);
            } else if (StringUtils.equals(testElement.getType(), ElementConstants.JDBC_SAMPLER)) {
                MsJDBCSampler jdbcSampler = (MsJDBCSampler) testElement;
                checkCustomEnv(env, jdbcSampler.isCustomizeReq(), jdbcSampler.getProjectId(), jdbcSampler.getIsRefEnvironment(), jdbcSampler.getReferenced(), hasFullUrlList);
            }
        }
        if (StringUtils.equals(testElement.getType(), ElementConstants.SCENARIO)
                && !((MsScenario) testElement).isEnvironmentEnable()) {
            env.getProjectIds().add(testElement.getProjectId());
        }
    }

    private void checkCustomEnv(ScenarioEnv env, boolean customizeReq, String projectId, Boolean isRefEnvironment, String referenced, List<Boolean> hasFullUrlList) {
        if (customizeReq) {
            env.getProjectIds().add(projectId);
            hasFullUrlList.add(isRefEnvironment == null ? true : !isRefEnvironment);
        } else if (!StringUtils.equalsIgnoreCase(referenced, ElementConstants.STEP_CREATED)
                || (isRefEnvironment != null && isRefEnvironment)) {
            env.getProjectIds().add(projectId);
            hasFullUrlList.add(false);
        }
    }

    /**
     * 设置场景的运行环境 环境组/环境JSON
     *
     * @param apiScenarioWithBLOBs
     */
    public void setScenarioEnv(ApiScenarioWithBLOBs apiScenarioWithBLOBs, RunScenarioRequest request) {
        if (apiScenarioWithBLOBs == null) {
            return;
        }
        String environmentType = apiScenarioWithBLOBs.getEnvironmentType();
        String environmentJson = apiScenarioWithBLOBs.getEnvironmentJson();
        String environmentGroupId = apiScenarioWithBLOBs.getEnvironmentGroupId();
        if (StringUtils.isBlank(environmentType)) {
            environmentType = EnvironmentType.JSON.toString();
        }
        String definition = apiScenarioWithBLOBs.getScenarioDefinition();
        JSONObject element = JSONUtil.parseObject(definition);
        ElementUtil.dataFormatting(element);
        definition = element.toString();
        MsScenario scenario = JSON.parseObject(definition, MsScenario.class);
        GenerateHashTreeUtil.parse(definition, scenario);
        if (StringUtils.equals(environmentType, EnvironmentType.JSON.toString()) && StringUtils.isNotEmpty(environmentJson)) {
            scenario.setEnvironmentMap(JSON.parseObject(environmentJson, Map.class));
        } else if (StringUtils.equals(environmentType, EnvironmentType.GROUP.toString())) {
            Map<String, String> map = environmentGroupProjectService.getEnvMap(environmentGroupId);
            scenario.setEnvironmentMap(map);
        }
        if (request != null && request.getConfig() != null && request.getConfig().getEnvMap() != null && !request.getConfig().getEnvMap().isEmpty()) {
            scenario.setEnvironmentMap(request.getConfig().getEnvMap());
        }
        apiScenarioWithBLOBs.setScenarioDefinition(JSON.toJSONString(scenario));
    }

    public boolean verifyScenarioEnv(ApiScenarioWithBLOBs apiScenarioWithBLOBs) {
        if (apiScenarioWithBLOBs != null) {
            String definition = apiScenarioWithBLOBs.getScenarioDefinition();
            MsScenarioEnv scenario = JSON.parseObject(definition, MsScenarioEnv.class);
            Map<String, String> envMap = scenario.getEnvironmentMap();
            return this.check(definition, envMap, scenario.getEnvironmentId(), apiScenarioWithBLOBs.getProjectId());
        }
        return true;
    }

    private boolean check(String definition, Map<String, String> envMap, String envId, String projectId) {
        boolean isEnv = true;
        ScenarioEnv apiScenarioEnv = getApiScenarioEnv(definition);
        // 所有请求非全路径检查环境
        if (!apiScenarioEnv.getFullUrl()) {
            try {
                if (envMap == null || envMap.isEmpty()) {
                    isEnv = false;
                } else {
                    Set<String> projectIds = apiScenarioEnv.getProjectIds();
                    projectIds.remove(null);
                    if (CollectionUtils.isNotEmpty(envMap.keySet())) {
                        for (String id : projectIds) {
                            Project project = projectMapper.selectByPrimaryKey(id);
                            String s = envMap.get(id);
                            if (project == null) {
                                s = envMap.get(projectId);
                            }
                            if (StringUtils.isBlank(s)) {
                                isEnv = false;
                                break;
                            } else {
                                ApiTestEnvironmentWithBLOBs env = apiTestEnvironmentMapper.selectByPrimaryKey(s);
                                if (env == null) {
                                    isEnv = false;
                                    break;
                                }
                            }
                        }
                    } else {
                        isEnv = false;
                    }
                }
            } catch (Exception e) {
                isEnv = false;
                LogUtil.error(e.getMessage(), e);
            }
        }

        // 1.8 之前环境是 environmentId
        if (!isEnv) {
            if (StringUtils.isNotBlank(envId)) {
                ApiTestEnvironmentWithBLOBs env = apiTestEnvironmentMapper.selectByPrimaryKey(envId);
                if (env != null) {
                    isEnv = true;
                }
            }
        }
        return isEnv;
    }

    public boolean verifyPlanScenarioEnv(ApiScenarioWithBLOBs apiScenarioWithBLOBs, TestPlanApiScenarioInfoDTO testPlanApiScenarios) {
        if (apiScenarioWithBLOBs != null) {
            String definition = apiScenarioWithBLOBs.getScenarioDefinition();
            MsScenarioEnv scenario = JSON.parseObject(definition, MsScenarioEnv.class);
            Map<String, String> envMap = scenario.getEnvironmentMap();
            if (testPlanApiScenarios != null) {
                String envType = testPlanApiScenarios.getEnvironmentType();
                String envJson = testPlanApiScenarios.getEnvironment();
                String envGroupId = testPlanApiScenarios.getEnvironmentGroupId();
                if (StringUtils.equals(envType, EnvironmentType.JSON.toString()) && StringUtils.isNotBlank(envJson)) {
                    envMap = JSON.parseObject(testPlanApiScenarios.getEnvironment(), Map.class);
                } else if (StringUtils.equals(envType, EnvironmentType.GROUP.name()) && StringUtils.isNotBlank(envGroupId)) {
                    envMap = environmentGroupProjectService.getEnvMap(envGroupId);
                } else {
                    envMap = new HashMap<>();
                }
            }
            return this.check(definition, envMap, scenario.getEnvironmentId(), apiScenarioWithBLOBs.getProjectId());
        }
        return true;
    }

    /**
     * 检查是否存在运行环境。若不存在则报错，存在的话返回所存在的运行环境
     *
     * @param request
     * @param apiScenarios
     * @return <projectId,envIds>
     */
    public Map<String, List<String>> checkEnv(RunScenarioRequest request, List<ApiScenarioWithBLOBs> apiScenarios) {
        Map<String, List<String>> projectEnvMap = new HashMap<>();
        if (StringUtils.equals(request.getRequestOriginator(), CommonConstants.TEST_PLAN)) {
            this.checkPlanScenarioEnv(request);
        } else if (StringUtils.isNotBlank(request.getRunMode()) && StringUtils.equalsAny(request.getRunMode(), ApiRunMode.SCENARIO.name(), ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
            StringBuilder builder = new StringBuilder();
            for (ApiScenarioWithBLOBs apiScenarioWithBLOBs : apiScenarios) {
                try {
                    this.setScenarioEnv(apiScenarioWithBLOBs, request);
                    boolean haveEnv = this.verifyScenarioEnv(apiScenarioWithBLOBs);
                    if (!haveEnv) {
                        builder.append(apiScenarioWithBLOBs.getName()).append("; ");
                    }
                } catch (Exception e) {
                    MSException.throwException("场景：" + apiScenarioWithBLOBs.getName() + "，步骤解析错误，检查是否包含插件步骤!");
                }
            }
            if (builder.length() > 0) {
                MSException.throwException("场景：" + builder + "运行环境未配置，请检查!");
            }
        } else if (StringUtils.equals(request.getRunMode(), ApiRunMode.SCHEDULE_SCENARIO.name())) {
            for (ApiScenarioWithBLOBs apiScenarioWithBLOBs : apiScenarios) {
                try {
                    this.setScenarioEnv(apiScenarioWithBLOBs, request);
                } catch (Exception e) {
                    MSException.throwException("定时任务设置场景环境失败，场景ID： " + apiScenarioWithBLOBs.getId());
                }
            }
        }
        return projectEnvMap;
    }

    //检查测试计划场景的环境，没有环境的场景从执行队列中移除
    public void checkPlanScenarioEnv(RunScenarioRequest request) {
        if (request.getProcessVO() != null &&
                MapUtils.isNotEmpty(request.getProcessVO().getTestPlanScenarioMap())
                && MapUtils.isNotEmpty(request.getProcessVO().getTestPlanScenarioMap())) {
            List<String> noEnvScenarioIds = new ArrayList<>();
            for (String key : request.getProcessVO().getTestPlanScenarioMap().keySet()) {
                try {
                    TestPlanApiScenarioInfoDTO dto = request.getProcessVO().getTestPlanScenarioMap().get(key);
                    ApiScenarioWithBLOBs apiScenarioWithBLOBs = request.getProcessVO().getScenarioMap().get(dto.getApiScenarioId());
                    boolean haveEnv = this.verifyPlanScenarioEnv(apiScenarioWithBLOBs, dto);
                    if (!haveEnv) {
                        noEnvScenarioIds.add(key);
                    }
                } catch (Exception e) {
                    LogUtil.error("解析测试计划场景环境出错!", e);
                    noEnvScenarioIds.add(key);
                }
            }
            if (CollectionUtils.isNotEmpty(noEnvScenarioIds)) {
                noEnvScenarioIds.forEach(id -> request.getProcessVO().getTestPlanScenarioMap().remove(id));
            }
        }
    }

    public Map<String, List<String>> selectApiScenarioEnv(List<? extends ApiScenarioWithBLOBs> list) {
        Map<String, List<String>> projectEnvMap = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            try {
                Map<String, String> map = new HashMap<>();
                String environmentType = list.get(i).getEnvironmentType();
                String environmentGroupId = list.get(i).getEnvironmentGroupId();
                String env = list.get(i).getEnvironmentJson();
                if (StringUtils.equals(environmentType, EnvironmentType.JSON.name())) {
                    // 环境属性为空 跳过
                    if (StringUtils.isBlank(env)) {
                        continue;
                    }
                    map = JSON.parseObject(env, Map.class);
                } else if (StringUtils.equals(environmentType, EnvironmentType.GROUP.name())) {
                    map = environmentGroupProjectService.getEnvMap(environmentGroupId);
                }

                Set<String> set = map.keySet();
                HashMap<String, String> envMap = new HashMap<>(16);
                // 项目为空 跳过
                if (set.isEmpty()) {
                    continue;
                }
                for (String projectId : set) {
                    String envId = map.get(projectId);
                    envMap.put(projectId, envId);
                }
                for (Map.Entry<String, String> entry : envMap.entrySet()) {
                    String projectId = entry.getKey();
                    String envId = entry.getValue();
                    if (projectEnvMap.containsKey(projectId)) {
                        if (!projectEnvMap.get(projectId).contains(envId)) {
                            projectEnvMap.get(projectId).add(envId);
                        }
                    } else {
                        projectEnvMap.put(projectId, new ArrayList<>() {{
                            this.add(envId);
                        }});
                    }
                }
            } catch (Exception e) {
                LogUtil.error("api scenario environment map incorrect parsing. api scenario id:" + list.get(i).getId());
            }
        }
        return projectEnvMap;
    }

    public void setApiScenarioEnv(List<ApiScenarioDTO> list) {
        List<Project> projectList = projectMapper.selectByExample(new ProjectExample());
        List<ApiTestEnvironmentWithBLOBs> apiTestEnvironments = apiTestEnvironmentMapper.selectByExampleWithBLOBs(new ApiTestEnvironmentExample());
        for (ApiScenarioDTO scenarioDTO : list) {
            Map<String, String> map = new HashMap<>();
            String env = scenarioDTO.getEnv();
            if (StringUtils.equals(scenarioDTO.getEnvironmentType(), EnvironmentType.JSON.name())) {
                // 环境属性为空 跳过
                if (StringUtils.isBlank(env)) {
                    continue;
                }
                map = JSON.parseObject(env, Map.class);
            } else if (StringUtils.equals(scenarioDTO.getEnvironmentType(), EnvironmentType.GROUP.name())) {
                map = environmentGroupProjectService.getEnvMap(scenarioDTO.getEnvironmentGroupId());
            }

            Set<String> set = map.keySet();
            HashMap<String, String> envMap = new HashMap<>(16);
            // 项目为空 跳过
            if (set.isEmpty()) {
                continue;
            }
            for (String projectId : set) {
                String envId = map.get(projectId);
                if (StringUtils.isBlank(envId)) {
                    continue;
                }
                List<Project> projects = projectList.stream().filter(p -> StringUtils.equals(p.getId(), projectId)).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(projects)) {
                    continue;
                }
                Project project = projects.get(0);
                List<ApiTestEnvironmentWithBLOBs> envs = apiTestEnvironments.stream().filter(e -> StringUtils.equals(e.getId(), envId)).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(envs)) {
                    continue;
                }
                ApiTestEnvironmentWithBLOBs environment = envs.get(0);
                String projectName = project.getName();
                String envName = environment.getName();
                if (StringUtils.isBlank(projectName) || StringUtils.isBlank(envName)) {
                    continue;
                }
                envMap.put(projectName, envName);
            }
            scenarioDTO.setEnvironmentMap(envMap);
        }
    }

    public void setEnvConfig(Map<String, String> environmentMap, ParameterConfig config) {
        final Map<String, EnvironmentConfig> envConfig = new HashMap<>(16);
        if (MapUtils.isNotEmpty(environmentMap)) {
            environmentMap.keySet().forEach(projectId -> {
                BaseEnvironmentService apiTestEnvironmentService = CommonBeanFactory.getBean(BaseEnvironmentService.class);
                ApiTestEnvironmentWithBLOBs environment = apiTestEnvironmentService.get(environmentMap.get(projectId));
                if (environment != null && environment.getConfig() != null) {
                    EnvironmentConfig env = JSONUtil.parseObject(environment.getConfig(), EnvironmentConfig.class);
                    env.setEnvironmentId(environment.getId());
                    envConfig.put(projectId, env);
                }
            });
            config.setConfig(envConfig);
        }
    }

    public Map<String, String> planEnvMap(String testPlanScenarioId) {
        Map<String, String> planEnvMap = new HashMap<>();
        TestPlanApiScenario planApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(testPlanScenarioId);
        String envJson = planApiScenario.getEnvironment();
        String envType = planApiScenario.getEnvironmentType();
        String envGroupId = planApiScenario.getEnvironmentGroupId();
        if (StringUtils.equals(envType, EnvironmentType.JSON.toString()) && StringUtils.isNotBlank(envJson)) {
            planEnvMap = JSON.parseObject(envJson, Map.class);
        } else if (StringUtils.equals(envType, EnvironmentType.GROUP.toString()) && StringUtils.isNotBlank(envGroupId)) {
            planEnvMap = environmentGroupProjectService.getEnvMap(envGroupId);
        }
        return planEnvMap;
    }

    public LinkedHashMap<String, List<String>> selectProjectNameAndEnvName(Map<String, List<String>> projectEnvIdMap) {
        LinkedHashMap<String, List<String>> returnMap = new LinkedHashMap<>();
        if (MapUtils.isNotEmpty(projectEnvIdMap)) {
            for (Map.Entry<String, List<String>> entry : projectEnvIdMap.entrySet()) {
                String projectId = entry.getKey();
                List<String> envIdList = entry.getValue();
                String projectName = this.selectNameById(projectId);
                List<String> envNameList = apiTestEnvironmentService.selectNameByIds(envIdList);
                if (CollectionUtils.isNotEmpty(envNameList) && StringUtils.isNotEmpty(projectName)) {
                    //考虑到存在不同工作空间下有相同名称的项目，这里还是要检查一下项目名称是否已被记录
                    if (returnMap.containsKey(projectName)) {
                        envNameList.forEach(envName -> {
                            if (!returnMap.get(projectName).contains(envName)) {
                                returnMap.get(projectName).add(envName);
                            }
                        });
                    } else {
                        returnMap.put(projectName, new ArrayList<>() {{
                            this.addAll(envNameList);
                        }});
                    }
                }
            }
        }
        return returnMap;
    }

    public Map<String, List<String>> selectProjectEnvMapByTestPlanScenarioIds(List<String> resourceIds) {
        Map<String, List<String>> returnMap = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(resourceIds)) {
            List<String> reportEnvConfList = testPlanApiScenarioMapper.selectReportEnvConfByResourceIds(resourceIds);
            reportEnvConfList.forEach(envConf -> {
                LinkedHashMap<String, List<String>> projectEnvMap = this.getProjectEnvMapByEnvConfig(envConf);
                for (Map.Entry<String, List<String>> entry : projectEnvMap.entrySet()) {
                    String projectName = entry.getKey();
                    List<String> envNameList = entry.getValue();
                    if (StringUtils.isEmpty(projectName) || CollectionUtils.isEmpty(envNameList)) {
                        continue;
                    }
                    if (returnMap.containsKey(projectName)) {
                        envNameList.forEach(envName -> {
                            if (!returnMap.get(projectName).contains(envName)) {
                                returnMap.get(projectName).add(envName);
                            }
                        });
                    } else {
                        returnMap.put(projectName, envNameList);
                    }
                }
            });
        }
        return returnMap;
    }

    public LinkedHashMap<String, List<String>> getProjectEnvMapByEnvConfig(String envConfig) {
        LinkedHashMap<String, List<String>> returnMap = new LinkedHashMap<>();
        //运行设置中选择的环境信息（批量执行时在前台选择了执行信息）
        Map<String, String> envMapByRunConfig = null;
        //执行时选择的环境信息 （一般在集合报告中会记录）
        Map<String, List<String>> envMapByExecution = null;

        String groupId = null;
        try {
            JSONObject jsonObject = JSONUtil.parseObject(envConfig);
            if (jsonObject.has("executionEnvironmentMap")) {
                RunModeConfigWithEnvironmentDTO configWithEnvironment = JSON.parseObject(envConfig, RunModeConfigWithEnvironmentDTO.class);
                if (StringUtils.equals(EnvironmentType.GROUP.name(), configWithEnvironment.getEnvironmentType())
                        && StringUtils.isNotEmpty(configWithEnvironment.getEnvironmentGroupId())) {
                    groupId = configWithEnvironment.getEnvironmentGroupId();
                }
                if (MapUtils.isNotEmpty(configWithEnvironment.getExecutionEnvironmentMap())) {
                    envMapByExecution = configWithEnvironment.getExecutionEnvironmentMap();
                } else {
                    envMapByRunConfig = configWithEnvironment.getEnvMap();
                }
            } else {
                RunModeConfigDTO config = JSON.parseObject(envConfig, RunModeConfigDTO.class);
                if (StringUtils.equals(EnvironmentType.GROUP.name(), config.getEnvironmentType())
                        && StringUtils.isNotEmpty(config.getEnvironmentGroupId())) {
                    groupId = config.getEnvironmentGroupId();
                }
                envMapByRunConfig = config.getEnvMap();
            }
        } catch (Exception e) {
            LogUtil.error("解析RunModeConfig失败!参数：" + envConfig, e);
        }

        if (StringUtils.isNotEmpty(groupId)) {
            EnvironmentGroup environmentGroup = apiTestEnvironmentService.selectById(groupId);
            if (StringUtils.isNotEmpty(environmentGroup.getName())) {
                returnMap.put(Translator.get("environment_group"), new ArrayList<>() {{
                    this.add(environmentGroup.getName());
                }});
            }
        } else {
            returnMap.putAll(this.selectProjectNameAndEnvName(envMapByExecution));
            if (MapUtils.isNotEmpty(envMapByRunConfig)) {
                for (Map.Entry<String, String> entry : envMapByRunConfig.entrySet()) {
                    String projectId = entry.getKey();
                    String envId = entry.getValue();
                    String projectName = this.selectNameById(projectId);
                    String envName = apiTestEnvironmentService.selectNameById(envId);
                    if (StringUtils.isNoneEmpty(projectName, envName) && this.isProjectEnvMapNotContainsEnv(returnMap, projectName, envName)) {
                        returnMap.put(projectName, new ArrayList<>() {{
                            this.add(envName);
                        }});
                    }
                }

            }
        }
        return returnMap;
    }

    private boolean isProjectEnvMapNotContainsEnv(LinkedHashMap<String, List<String>> returnMap, String projectName, String envName) {
        if (MapUtils.isNotEmpty(returnMap)) {
            if (returnMap.containsKey(projectName) && CollectionUtils.isNotEmpty(returnMap.get(projectName)) && returnMap.get(projectName).contains(envName)) {
                return false;
            }
        }
        return true;
    }

    public String selectNameById(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null) {
            return null;
        } else {
            return project.getName();
        }
    }

    public void setScenarioEnv(RunScenarioRequest request, Map<String, String> configEnvMap) {
        if (StringUtils.equals(request.getConfig().getEnvironmentType(), EnvironmentType.JSON.toString())
                && MapUtils.isNotEmpty(request.getConfig().getEnvMap())) {
            configEnvMap.putAll(request.getConfig().getEnvMap());
        } else if (StringUtils.equals(request.getConfig().getEnvironmentType(), EnvironmentType.GROUP.toString())
                && StringUtils.isNotBlank(request.getConfig().getEnvironmentGroupId())) {
            configEnvMap.putAll(environmentGroupProjectService.getEnvMap(request.getConfig().getEnvironmentGroupId()));
        }
    }

    public Map<String, String> getPlanScenarioEnv(TestPlanApiScenarioInfoDTO planApiScenario, Map<String, String> configEnvMap) {
        Map<String, String> planEnvMap = new LinkedHashMap<>();
        if (StringUtils.equals(planApiScenario.getEnvironmentType(), EnvironmentType.JSON.toString())
                && StringUtils.isNotBlank(planApiScenario.getEnvironment())) {
            planEnvMap.putAll(JSON.parseObject(planApiScenario.getEnvironment(), Map.class));
        } else if (StringUtils.equals(planApiScenario.getEnvironmentType(), EnvironmentType.GROUP.toString())
                && StringUtils.isNotBlank(planApiScenario.getEnvironmentGroupId())) {
            planEnvMap.putAll(environmentGroupProjectService.getEnvMap(planApiScenario.getEnvironmentGroupId()));
        }
        if (MapUtils.isNotEmpty(configEnvMap)) {
            planEnvMap.putAll(configEnvMap);
        }
        return planEnvMap;
    }
}
