package io.metersphere.commons.utils;


import io.metersphere.api.dto.EnvironmentType;
import io.metersphere.api.dto.definition.request.*;
import io.metersphere.api.dto.definition.request.variable.ScenarioVariable;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.jmeter.NewDriverManager;
import io.metersphere.api.jmeter.ResourcePoolCalculation;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.base.domain.TestResource;
import io.metersphere.base.domain.TestResourcePool;
import io.metersphere.base.mapper.TestResourcePoolMapper;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.constants.ResourcePoolTypeEnum;
import io.metersphere.commons.exception.MSException;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.dto.ProjectJarConfig;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.environment.service.BaseEnvGroupProjectService;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.vo.BooleanPool;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.collections.HashTree;
import org.json.JSONObject;

import java.util.*;

public class GenerateHashTreeUtil {
    public static MsScenario parseScenarioDefinition(String scenarioDefinition) {
        if (StringUtils.isNotEmpty(scenarioDefinition)) {
            MsScenario scenario = JSON.parseObject(scenarioDefinition, MsScenario.class);
            if (scenario != null) {
                parse(scenarioDefinition, scenario);
            }
            return scenario;
        }
        return null;
    }

    public static void parse(String scenarioDefinition, MsScenario scenario) {
        try {
            JSONObject element = JSONUtil.parseObject(scenarioDefinition);
            ElementUtil.dataFormatting(element);
            // 多态JSON普通转换会丢失内容，需要通过 ObjectMapper 获取
            if (element.has(ElementConstants.HASH_TREE)) {
                scenario.setHashTree(JSONUtil.readValue(element.optJSONArray(ElementConstants.HASH_TREE).toString()));
            }
            if (StringUtils.isNotEmpty(element.optString("variables"))) {
                scenario.setVariables(JSONUtil.parseArray(element.optString("variables"), ScenarioVariable.class));
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    public static LinkedList<MsTestElement> getScenarioHashTree(String definition) {
        JSONObject element = JSONUtil.parseObject(definition);
        if (element.has(ElementConstants.HASH_TREE)) {
            ElementUtil.dataFormatting(element);
            return JSONUtil.readValue(element.optJSONArray(ElementConstants.HASH_TREE).toString());
        }
        return new LinkedList<>();
    }

    public static BooleanPool isResourcePool(String id) {
        BooleanPool pool = new BooleanPool();
        pool.setPool(StringUtils.isNotEmpty(id));
        if (pool.isPool()) {
            TestResourcePool resourcePool = CommonBeanFactory.getBean(TestResourcePoolMapper.class).selectByPrimaryKey(id);
            pool.setK8s(resourcePool != null && resourcePool.getApi() && resourcePool.getType().equals(ResourcePoolTypeEnum.K8S.name()));
        }
        return pool;
    }

    public static List<TestResource> setPoolResource(String id) {
        if (GenerateHashTreeUtil.isResourcePool(id).isPool() && !GenerateHashTreeUtil.isResourcePool(id).isK8s()) {
            ResourcePoolCalculation resourcePoolCalculation = CommonBeanFactory.getBean(ResourcePoolCalculation.class);
            return resourcePoolCalculation.getResourcePools(id);
        }
        return new LinkedList<>();
    }

    public static void setScenarioEnv(MsScenario scenario, ApiScenarioWithBLOBs apiScenarioWithBLOBs) {
        String environmentType = apiScenarioWithBLOBs.getEnvironmentType();
        String environmentJson = apiScenarioWithBLOBs.getEnvironmentJson();
        String environmentGroupId = apiScenarioWithBLOBs.getEnvironmentGroupId();

        if (StringUtils.isBlank(environmentType)) {
            environmentType = EnvironmentType.JSON.toString();
        }
        if (StringUtils.equals(environmentType, EnvironmentType.JSON.toString()) && StringUtils.isNotBlank(environmentJson)) {
            scenario.setEnvironmentMap(JSON.parseObject(environmentJson, Map.class));
        } else if (StringUtils.equals(environmentType, EnvironmentType.GROUP.toString())) {
            Map<String, String> map = CommonBeanFactory.getBean(BaseEnvGroupProjectService.class).getEnvMap(environmentGroupId);
            scenario.setEnvironmentMap(map);
        }
    }

    public static HashTree generateHashTree(ApiScenarioWithBLOBs item, Map<String, String> planEnvMap, JmeterRunRequestDTO runRequest) {
        // 检查执行内容合规性
        PerformInspectionUtil.countMatches(item.getScenarioDefinition(), item.getId());

        HashTree jmeterHashTree = new HashTree();
        MsTestPlan testPlan = new MsTestPlan();
        testPlan.setHashTree(new LinkedList<>());
        try {
            MsThreadGroup group = new MsThreadGroup();
            group.setLabel(item.getName());
            group.setName(runRequest.getReportId());
            JSONObject element = JSONUtil.parseObject(item.getScenarioDefinition());
            ElementUtil.dataFormatting(element);
            element.put("id", item.getId());
            String definition = element.toString();

            MsScenario scenario = JSON.parseObject(definition, MsScenario.class);
            scenario.setId(item.getId());
            group.setOnSampleError(scenario.getOnSampleError());
            if (MapUtils.isNotEmpty(planEnvMap)) {
                scenario.setEnvironmentMap(planEnvMap);
            } else {
                setScenarioEnv(scenario, item);
            }
            // 获取自定义JAR
            String currentProjectId = item.getProjectId();
            List<String> projectIds = new ArrayList<>();
            projectIds.add(currentProjectId);
            if (MapUtils.isNotEmpty(planEnvMap)) {
                planEnvMap.forEach((projectId, env) -> {
                    if (!projectIds.contains(projectId)) {
                        projectIds.add(projectId);
                    }
                });
            }
            if (MapUtils.isNotEmpty(scenario.getEnvironmentMap())) {
                scenario.getEnvironmentMap().forEach((projectId, env) -> {
                    if (!projectIds.contains(projectId)) {
                        projectIds.add(projectId);
                    }
                });
            }
            Map<String, List<ProjectJarConfig>> jarsMap = NewDriverManager.getJars(projectIds, runRequest.getPool());
            testPlan.setProjectJarIds(jarsMap.keySet().stream().toList());
            testPlan.setPoolJarsMap(jarsMap);
            GenerateHashTreeUtil.parse(definition, scenario);
            group.setEnableCookieShare(scenario.isEnableCookieShare());
            LinkedList<MsTestElement> scenarios = new LinkedList<>();
            scenarios.add(scenario);

            group.setHashTree(scenarios);
            testPlan.getHashTree().add(group);

            ParameterConfig config = new ParameterConfig(item.getProjectId(), false);
            config.setScenarioId(item.getId());
            config.setReportType(runRequest.getReportType());
            config.setConfig(getEnv(scenario.getEnvironmentMap()));
            config.setReportId(runRequest.getReportId());

            if (runRequest.isRetryEnable() && runRequest.getRetryNum() > 0) {
                config.setRetryNum(runRequest.getRetryNum());
            }
            testPlan.toHashTree(jmeterHashTree, testPlan.getHashTree(), config);

            LoggerUtil.info("场景资源：" + item.getName() + ", 生成执行脚本JMX成功", runRequest.getReportId());
        } catch (Exception ex) {
            LoggerUtil.error("场景资源：" + item.getName() + ", 生成执行脚本失败",
                    runRequest.getReportId(), ex);
            MSException.throwException("场景资源：" + item.getName() + ", 生成执行脚本失败" + ex.getMessage());
        }

        LogUtil.info(testPlan.getJmx(jmeterHashTree));
        return jmeterHashTree;
    }

    public static Map<String, EnvironmentConfig> getEnv(Map<String, String> environmentMap) {
        Map<String, EnvironmentConfig> envConfig = new HashMap<>();
        if (MapUtils.isEmpty(environmentMap)) {
            return envConfig;
        }
        for (String projectId : environmentMap.keySet()) {
            BaseEnvironmentService apiTestEnvironmentService = CommonBeanFactory.getBean(BaseEnvironmentService.class);
            ApiTestEnvironmentWithBLOBs environment = apiTestEnvironmentService.get(environmentMap.get(projectId));
            if (environment != null && StringUtils.isNotEmpty(environment.getConfig())) {
                EnvironmentConfig env = JSONUtil.parseObject(environment.getConfig(), EnvironmentConfig.class);
                env.setEnvironmentId(environment.getId());
                envConfig.put(projectId, env);
            }
        }
        return envConfig;
    }

    public static boolean isSetReport(RunModeConfigDTO config) {
        return config != null && isSetReport(config.getReportType()) && StringUtils.isNotEmpty(config.getReportName());
    }

    public static boolean isSetReport(String reportType) {
        return StringUtils.equals(reportType, RunModeConstants.SET_REPORT.toString());
    }

    public static String getPlatformUrl(BaseSystemConfigDTO baseInfo, JmeterRunRequestDTO request, String queueDetailId) {
        // 占位符
        String platformUrl = "http://localhost:8081";
        if (baseInfo != null) {
            platformUrl = baseInfo.getUrl();
        }

        platformUrl += "/api/jmeter/download?testId=" + request.getTestId() + "&reportId=" + request.getReportId() + "&runMode=" + request.getRunMode() + "&reportType=" + request.getReportType() + "&queueId=" + queueDetailId;
        return platformUrl;
    }
}
