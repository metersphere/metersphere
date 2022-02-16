package io.metersphere.api.exec.scenario;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.dto.EnvironmentType;
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
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.ApiTestEnvironmentMapper;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.TestPlanApiScenarioMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.MsTestElementConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.service.EnvironmentGroupProjectService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApiScenarioEnvService {
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
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

    public ScenarioEnv getApiScenarioEnv(String definition) {
        ScenarioEnv env = new ScenarioEnv();
        List<MsTestElement> hashTree = GenerateHashTreeUtil.getScenarioHashTree(definition);
        for (int i = 0; i < hashTree.size(); i++) {
            MsTestElement tr = hashTree.get(i);
            if (!tr.isEnable()) {
                continue;
            }
            String referenced = tr.getReferenced();
            if (StringUtils.equals(MsTestElementConstants.REF.name(), referenced)) {
                if (StringUtils.equals(tr.getType(), "HTTPSamplerProxy")) {
                    MsHTTPSamplerProxy http = (MsHTTPSamplerProxy) tr;
                    String refType = tr.getRefType();
                    if (StringUtils.equals(refType, "CASE")) {
                        http.setUrl(null);
                    } else {
                        ApiDefinition apiDefinition = apiDefinitionService.get(tr.getId());
                        if (apiDefinition != null) {
                            http.setUrl(apiDefinition.getPath());
                        }
                    }
                    if (http.isEnable()) {
                        if (StringUtils.isBlank(http.getUrl()) || (http.getIsRefEnvironment() != null && http.getIsRefEnvironment())) {
                            env.getProjectIds().add(http.getProjectId());
                            env.setFullUrl(false);
                        }
                    }
                } else if (StringUtils.equals(tr.getType(), "JDBCSampler") || StringUtils.equals(tr.getType(), "TCPSampler")) {
                    if (StringUtils.equals(tr.getRefType(), "CASE")) {
                        ApiTestCaseWithBLOBs apiTestCaseWithBLOBs = apiTestCaseService.get(tr.getId());
                        if (apiTestCaseWithBLOBs != null) {
                            env.getProjectIds().add(apiTestCaseWithBLOBs.getProjectId());
                            env.setFullUrl(false);
                        }
                    } else {
                        ApiDefinition apiDefinition = apiDefinitionService.get(tr.getId());
                        if (apiDefinition != null) {
                            env.getProjectIds().add(apiDefinition.getProjectId());
                            env.setFullUrl(false);
                        }
                    }
                } else if (StringUtils.equals(tr.getType(), "scenario")) {
                    if (tr.isEnable()) {
                        ApiScenarioWithBLOBs apiScenario = apiScenarioMapper.selectByPrimaryKey(tr.getId());
                        if (apiScenario != null) {
                            env.getProjectIds().add(apiScenario.getProjectId());
                            String scenarioDefinition = apiScenario.getScenarioDefinition();
                            tr.setHashTree(GenerateHashTreeUtil.getScenarioHashTree(scenarioDefinition));
                        }
                    }
                }
            } else {
                if (StringUtils.equals(tr.getType(), "HTTPSamplerProxy")) {
                    // 校验是否是全路径
                    MsHTTPSamplerProxy httpSamplerProxy = (MsHTTPSamplerProxy) tr;
                    if (httpSamplerProxy.isEnable()) {
                        if (StringUtils.isBlank(httpSamplerProxy.getUrl()) || (httpSamplerProxy.getIsRefEnvironment() != null && httpSamplerProxy.getIsRefEnvironment())) {
                            env.getProjectIds().add(httpSamplerProxy.getProjectId());
                            env.setFullUrl(false);
                        }
                    }
                } else if (StringUtils.equals(tr.getType(), "JDBCSampler") || StringUtils.equals(tr.getType(), "TCPSampler")) {
                    env.getProjectIds().add(tr.getProjectId());
                    env.setFullUrl(false);
                }
            }
            if (StringUtils.equals(tr.getType(), "scenario")) {
                MsScenario scenario = (MsScenario) tr;
                if (scenario.isEnvironmentEnable()) {
                    continue;
                }
                env.getProjectIds().add(tr.getProjectId());
            }
            if (CollectionUtils.isNotEmpty(tr.getHashTree())) {
                getHashTree(tr.getHashTree(), env);
            }
        }
        return env;
    }

    private void getHashTree(List<MsTestElement> tree, ScenarioEnv env) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            for (int i = 0; i < tree.size(); i++) {
                MsTestElement tr = tree.get(i);
                if (!tr.isEnable()) {
                    continue;
                }
                String referenced = tr.getReferenced();
                if (StringUtils.equals(MsTestElementConstants.REF.name(), referenced)) {
                    if (StringUtils.equals(tr.getType(), "HTTPSamplerProxy")) {
                        MsHTTPSamplerProxy http = (MsHTTPSamplerProxy) tr;
                        String refType = tr.getRefType();
                        if (StringUtils.equals(refType, "CASE")) {
                            http.setUrl(null);
                        } else {
                            ApiDefinition apiDefinition = apiDefinitionService.get(tr.getId());
                            http.setUrl(apiDefinition.getPath());
                        }
                        if (http.isEnable()) {
                            if (StringUtils.isBlank(http.getUrl()) || (http.getIsRefEnvironment() != null && http.getIsRefEnvironment())) {
                                env.setFullUrl(false);
                                env.getProjectIds().add(http.getProjectId());
                            }
                        }
                    } else if (StringUtils.equals(tr.getType(), "JDBCSampler") || StringUtils.equals(tr.getType(), "TCPSampler")) {
                        if (StringUtils.equals(tr.getRefType(), "CASE")) {
                            ApiTestCaseWithBLOBs apiTestCaseWithBLOBs = apiTestCaseService.get(tr.getId());
                            env.getProjectIds().add(apiTestCaseWithBLOBs.getProjectId());
                            env.setFullUrl(false);
                        } else {
                            ApiDefinition apiDefinition = apiDefinitionService.get(tr.getId());
                            env.getProjectIds().add(apiDefinition.getProjectId());
                            env.setFullUrl(false);
                        }
                    } else if (StringUtils.equals(tr.getType(), "scenario")) {
                        if (tr.isEnable()) {
                            ApiScenarioWithBLOBs apiScenario = apiScenarioMapper.selectByPrimaryKey(tr.getId());
                            if (apiScenario != null) {
                                env.getProjectIds().add(apiScenario.getProjectId());
                                String scenarioDefinition = apiScenario.getScenarioDefinition();
                                JSONObject element1 = JSON.parseObject(scenarioDefinition);
                                ElementUtil.dataFormatting(element1);
                                LinkedList<MsTestElement> hashTree1 = mapper.readValue(element1.getString("hashTree"), new TypeReference<LinkedList<MsTestElement>>() {
                                });
                                tr.setHashTree(hashTree1);
                            }
                        }
                    }
                } else {
                    if (StringUtils.equals(tr.getType(), "HTTPSamplerProxy")) {
                        // 校验是否是全路径
                        MsHTTPSamplerProxy httpSamplerProxy = (MsHTTPSamplerProxy) tr;
                        if (httpSamplerProxy.isEnable()) {
                            if (StringUtils.isBlank(httpSamplerProxy.getUrl()) || !ElementUtil.isURL(httpSamplerProxy.getUrl())) {
                                env.setFullUrl(false);
                                env.getProjectIds().add(httpSamplerProxy.getProjectId());
                            }
                        }
                    } else if (StringUtils.equals(tr.getType(), "JDBCSampler") || StringUtils.equals(tr.getType(), "TCPSampler")) {
                        env.getProjectIds().add(tr.getProjectId());
                        env.setFullUrl(false);
                    }
                }
                if (StringUtils.equals(tr.getType(), "scenario")) {
                    MsScenario scenario = (MsScenario) tr;
                    if (scenario.isEnvironmentEnable()) {
                        continue;
                    }
                    env.getProjectIds().add(tr.getProjectId());
                }
                if (CollectionUtils.isNotEmpty(tr.getHashTree())) {
                    getHashTree(tr.getHashTree(), env);
                }
            }
        } catch (JsonProcessingException e) {
            LogUtil.error(e);
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
        MsScenario scenario = JSONObject.parseObject(definition, MsScenario.class);
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
            MsScenario scenario = JSONObject.parseObject(definition, MsScenario.class);
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

    public void checkEnv(RunScenarioRequest request, List<ApiScenarioWithBLOBs> apiScenarios) {
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
                    MSException.throwException("场景：" + builder.toString() + "运行环境未配置，请检查!");
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
}
