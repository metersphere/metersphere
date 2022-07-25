package io.metersphere.api.exec.scenario;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import io.metersphere.api.dto.EnvironmentType;
import io.metersphere.api.dto.RunModeConfigWithEnvironmentDTO;
import io.metersphere.api.dto.ScenarioEnv;
import io.metersphere.api.dto.automation.ApiScenarioDTO;
import io.metersphere.api.dto.automation.RunScenarioRequest;
import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.dto.definition.request.MsScenario;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.exec.utils.GenerateHashTreeUtil;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.MsTestElementConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.service.EnvironmentGroupProjectService;
import io.metersphere.service.EnvironmentGroupService;
import io.metersphere.service.ProjectService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApiScenarioEnvService {
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private EnvironmentGroupProjectService environmentGroupProjectService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ApiTestEnvironmentMapper apiTestEnvironmentMapper;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Lazy
    @Resource
    private ProjectService projectService;
    @Resource
    private ApiTestEnvironmentService apiTestEnvironmentService;
    @Lazy
    @Resource
    private EnvironmentGroupService environmentGroupService;

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
        for (MsTestElement testElement : hashTree) {
            this.formatElement(testElement, env);
            if (CollectionUtils.isNotEmpty(testElement.getHashTree())) {
                getHashTree(testElement.getHashTree(), env);
            }
        }
        return env;
    }


    private void getHashTree(List<MsTestElement> tree, ScenarioEnv env) {
        try {
            // 过滤掉禁用的步骤
            tree = tree.stream().filter(item -> item.isEnable()).collect(Collectors.toList());
            for (MsTestElement element : tree) {
                this.formatElement(element, env);
                if (CollectionUtils.isNotEmpty(element.getHashTree())) {
                    getHashTree(element.getHashTree(), env);
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    private void formatElement(MsTestElement testElement, ScenarioEnv env) {
        if (StringUtils.equals(MsTestElementConstants.REF.name(), testElement.getReferenced())) {
            if (StringUtils.equals(testElement.getType(), "HTTPSamplerProxy")) {
                MsHTTPSamplerProxy http = (MsHTTPSamplerProxy) testElement;
                // 引用用例URL清空
                http.setUrl(StringUtils.equals(testElement.getRefType(), "CASE") ? null : http.getUrl());

                // 非全路径校验
                if (!StringUtils.equalsIgnoreCase(http.getReferenced(), "Created") || (http.getIsRefEnvironment() != null &&
                        http.getIsRefEnvironment())) {
                    env.getProjectIds().add(http.getProjectId());
                    env.setFullUrl(false);
                }
            } else if (StringUtils.equals(testElement.getType(), "JDBCSampler") || StringUtils.equals(testElement.getType(), "TCPSampler")) {
                if (StringUtils.isEmpty(testElement.getProjectId())) {
                    if (StringUtils.equals(testElement.getRefType(), "CASE")) {
                        ApiTestCase testCase = apiTestCaseMapper.selectByPrimaryKey(testElement.getId());
                        if (testCase != null) {
                            env.getProjectIds().add(testCase.getProjectId());
                            env.setFullUrl(false);
                        }
                    } else {
                        ApiDefinition apiDefinition = apiDefinitionService.get(testElement.getId());
                        if (apiDefinition != null) {
                            env.getProjectIds().add(apiDefinition.getProjectId());
                            env.setFullUrl(false);
                        }
                    }
                } else {
                    env.getProjectIds().add(testElement.getProjectId());
                    env.setFullUrl(false);
                }
            } else if (StringUtils.equals(testElement.getType(), "scenario") && StringUtils.isEmpty(testElement.getProjectId())) {
                ApiScenarioWithBLOBs apiScenario = apiScenarioMapper.selectByPrimaryKey(testElement.getId());
                if (apiScenario != null) {
                    env.getProjectIds().add(apiScenario.getProjectId());
                    String scenarioDefinition = apiScenario.getScenarioDefinition();
                    JSONObject element = JSON.parseObject(scenarioDefinition, Feature.DisableSpecialKeyDetect);
                    ElementUtil.dataFormatting(element);
                    testElement.setHashTree(GenerateHashTreeUtil.getScenarioHashTree(scenarioDefinition));
                }
            }
        } else {
            if (StringUtils.equals(testElement.getType(), "HTTPSamplerProxy")) {
                // 校验是否是全路径
                MsHTTPSamplerProxy httpSamplerProxy = (MsHTTPSamplerProxy) testElement;
                if (httpSamplerProxy.isCustomizeReq()) {
                    env.getProjectIds().add(httpSamplerProxy.getProjectId());
                    env.setFullUrl(httpSamplerProxy.getIsRefEnvironment() == null ? true : httpSamplerProxy.getIsRefEnvironment());
                } else if (!StringUtils.equalsIgnoreCase(httpSamplerProxy.getReferenced(), "Created") || (httpSamplerProxy.getIsRefEnvironment() != null &&
                        httpSamplerProxy.getIsRefEnvironment())) {
                    env.getProjectIds().add(httpSamplerProxy.getProjectId());
                    env.setFullUrl(false);
                }

            } else if (StringUtils.equals(testElement.getType(), "JDBCSampler") || StringUtils.equals(testElement.getType(), "TCPSampler")) {
                env.getProjectIds().add(testElement.getProjectId());
                env.setFullUrl(false);
            }
        }
        if (StringUtils.equals(testElement.getType(), "scenario") && !((MsScenario) testElement).isEnvironmentEnable()) {
            env.getProjectIds().add(testElement.getProjectId());
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
        MsScenario scenario = JSONObject.parseObject(definition, MsScenario.class, Feature.DisableSpecialKeyDetect);
        GenerateHashTreeUtil.parse(definition, scenario);
        if (StringUtils.equals(environmentType, EnvironmentType.JSON.toString())) {
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

    public boolean checkScenarioEnv(ApiScenarioWithBLOBs apiScenarioWithBLOBs, TestPlanApiScenario testPlanApiScenarios) {
        boolean isEnv = true;
        if (apiScenarioWithBLOBs != null) {
            String definition = apiScenarioWithBLOBs.getScenarioDefinition();
            MsScenario scenario = JSONObject.parseObject(definition, MsScenario.class, Feature.DisableSpecialKeyDetect);
            Map<String, String> envMap = scenario.getEnvironmentMap();
            if (testPlanApiScenarios != null) {
                String envType = testPlanApiScenarios.getEnvironmentType();
                String envJson = testPlanApiScenarios.getEnvironment();
                String envGroupId = testPlanApiScenarios.getEnvironmentGroupId();
                if (StringUtils.equals(envType, EnvironmentType.JSON.toString())
                        && StringUtils.isNotBlank(envJson)) {
                    envMap = JSON.parseObject(testPlanApiScenarios.getEnvironment(), Map.class);
                } else if (StringUtils.equals(envType, EnvironmentType.GROUP.name())
                        && StringUtils.isNotBlank(envGroupId)) {
                    envMap = environmentGroupProjectService.getEnvMap(envGroupId);
                } else {
                    envMap = new HashMap<>();
                }
            }
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
                                if (project == null) {
                                    id = apiScenarioWithBLOBs.getProjectId();
                                }
                                String s = envMap.get(id);
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
                String envId = scenario.getEnvironmentId();
                if (StringUtils.isNotBlank(envId)) {
                    ApiTestEnvironmentWithBLOBs env = apiTestEnvironmentMapper.selectByPrimaryKey(envId);
                    if (env != null) {
                        isEnv = true;
                    }
                }
            }
        }
        return isEnv;
    }

    public void checkPlanScenarioEnv(RunScenarioRequest request) {
        StringBuilder builder = new StringBuilder();
        List<String> planCaseIds = request.getPlanCaseIds();
        if (CollectionUtils.isNotEmpty(planCaseIds)) {
            TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
            example.createCriteria().andIdIn(planCaseIds);
            List<TestPlanApiScenario> testPlanApiScenarios = testPlanApiScenarioMapper.selectByExampleWithBLOBs(example);
            for (TestPlanApiScenario testPlanApiScenario : testPlanApiScenarios) {
                try {
                    ApiScenarioWithBLOBs apiScenarioWithBLOBs = apiScenarioMapper.selectByPrimaryKey(testPlanApiScenario.getApiScenarioId());
                    boolean haveEnv = this.checkScenarioEnv(apiScenarioWithBLOBs, testPlanApiScenario);
                    if (!haveEnv) {
                        builder.append(apiScenarioWithBLOBs.getName()).append("; ");
                    }
                } catch (Exception e) {
                    MSException.throwException("场景：" + builder.toString() + "运行环境未配置，请检查!");
                }
            }
            if (builder.length() > 0) {
                MSException.throwException("场景：" + builder.toString() + "运行环境未配置，请检查!");
            }
        }
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
        if (StringUtils.equals(request.getRequestOriginator(), "TEST_PLAN")) {
            this.checkPlanScenarioEnv(request);
        } else if (StringUtils.isNotBlank(request.getRunMode()) &&
                StringUtils.equalsAny(request.getRunMode(), ApiRunMode.SCENARIO.name(), ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
            StringBuilder builder = new StringBuilder();
            for (ApiScenarioWithBLOBs apiScenarioWithBLOBs : apiScenarios) {
                try {
                    this.setScenarioEnv(apiScenarioWithBLOBs, request);
                    boolean haveEnv = this.checkScenarioEnv(apiScenarioWithBLOBs, null);
                    if (!haveEnv) {
                        builder.append(apiScenarioWithBLOBs.getName()).append("; ");
                    }
                } catch (Exception e) {
                    MSException.throwException("场景：" +apiScenarioWithBLOBs.getName() +"，步骤解析错误，检查是否包含插件步骤!");
                }
            }
            if (builder.length() > 0) {
                MSException.throwException("场景：" + builder.toString() + "运行环境未配置，请检查!");
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
        for (int i = 0; i < list.size(); i++) {
            try {
                Map<String, String> map = new HashMap<>();
                String environmentType = list.get(i).getEnvironmentType();
                String environmentGroupId = list.get(i).getEnvironmentGroupId();
                String env = list.get(i).getEnv();
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
                list.get(i).setEnvironmentMap(envMap);
            } catch (Exception e) {
                LogUtil.error("api scenario environment map incorrect parsing. api scenario id:" + list.get(i).getId());
            }
        }
    }

    public void setEnvConfig(Map<String, String> environmentMap, ParameterConfig config) {
        final Map<String, EnvironmentConfig> envConfig = new HashMap<>(16);
        if (environmentMap != null) {
            environmentMap.keySet().forEach(projectId -> {
                ApiTestEnvironmentService environmentService = CommonBeanFactory.getBean(ApiTestEnvironmentService.class);
                ApiTestEnvironmentWithBLOBs environment = environmentService.get(environmentMap.get(projectId));
                if (environment != null && environment.getConfig() != null) {
                    EnvironmentConfig env = JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class);
                    env.setApiEnvironmentid(environment.getId());
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
                String projectName = projectService.selectNameById(projectId);
                List<String> envNameList = apiTestEnvironmentService.selectNameByIds(envIdList);
                if (CollectionUtils.isNotEmpty(envNameList) && StringUtils.isNotEmpty(projectName)) {
                    returnMap.put(projectName, new ArrayList<>() {{
                        this.addAll(envNameList);
                    }});
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
            JSONObject jsonObject = JSONObject.parseObject(envConfig);
            if (jsonObject.containsKey("executionEnvironmentMap")) {
                RunModeConfigWithEnvironmentDTO configWithEnvironment = JSONObject.parseObject(envConfig, RunModeConfigWithEnvironmentDTO.class);
                if (StringUtils.equals("GROUP", configWithEnvironment.getEnvironmentType()) && StringUtils.isNotEmpty(configWithEnvironment.getEnvironmentGroupId())) {
                    groupId = configWithEnvironment.getEnvironmentGroupId();
                }
                if (MapUtils.isNotEmpty(configWithEnvironment.getExecutionEnvironmentMap())) {
                    envMapByExecution = configWithEnvironment.getExecutionEnvironmentMap();
                } else {
                    envMapByRunConfig = configWithEnvironment.getEnvMap();
                }
            } else {
                RunModeConfigDTO config = JSONObject.parseObject(envConfig, RunModeConfigDTO.class);
                if (StringUtils.equals("GROUP", config.getEnvironmentType()) && StringUtils.isNotEmpty(config.getEnvironmentGroupId())) {
                    groupId = config.getEnvironmentGroupId();
                }
                envMapByRunConfig = config.getEnvMap();
            }
        } catch (Exception e) {
            LogUtil.error("解析RunModeConfig失败!参数：" + envConfig, e);
        }

        if(StringUtils.isNotEmpty(groupId)){
            EnvironmentGroup environmentGroup = environmentGroupService.selectById(groupId);
            if (StringUtils.isNotEmpty(environmentGroup.getName())) {
                returnMap.put(Translator.get("environment_group"),new ArrayList<>(){{this.add(environmentGroup.getName());}});
            }
        }else {
            returnMap.putAll(this.selectProjectNameAndEnvName(envMapByExecution));
            if (MapUtils.isNotEmpty(envMapByRunConfig)) {
                for (Map.Entry<String, String> entry : envMapByRunConfig.entrySet()) {
                    String projectId = entry.getKey();
                    String envId = entry.getValue();
                    String projectName = projectService.selectNameById(projectId);
                    String envName = apiTestEnvironmentService.selectNameById(envId);
                    if (StringUtils.isNoneEmpty(projectName, envName)) {
                        returnMap.put(projectName, new ArrayList<>() {{
                            this.add(envName);
                        }});
                    }
                }

            }
        }
        return returnMap;
    }
}
