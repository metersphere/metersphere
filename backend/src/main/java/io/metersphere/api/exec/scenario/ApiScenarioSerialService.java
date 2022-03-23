package io.metersphere.api.exec.scenario;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.dto.definition.request.MsTestPlan;
import io.metersphere.api.dto.definition.request.MsThreadGroup;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.sampler.MsDubboSampler;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.sampler.MsJDBCSampler;
import io.metersphere.api.dto.definition.request.sampler.MsTCPSampler;
import io.metersphere.api.exec.utils.GenerateHashTreeUtil;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.service.ApiExecutionQueueService;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.api.service.RemakeReportService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.HashTreeUtil;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.dto.ResultDTO;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.collections.HashTree;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

@Service
public class ApiScenarioSerialService {
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private JMeterService jMeterService;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    private ApiScenarioEnvService apiScenarioEnvService;

    public void serial(ApiExecutionQueue executionQueue, ApiExecutionQueueDetail queue) {
        LoggerUtil.debug("Scenario run-执行脚本装载-进入串行准备");
        if (!StringUtils.equals(executionQueue.getReportType(), RunModeConstants.SET_REPORT.toString())
                || StringUtils.equalsIgnoreCase(executionQueue.getRunMode(), ApiRunMode.DEFINITION.name())) {
            if (StringUtils.equalsAny(executionQueue.getRunMode(), ApiRunMode.SCENARIO.name(), ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
                ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(queue.getReportId());
                if (report != null) {
                    report.setStatus(APITestStatus.Running.name());
                    report.setCreateTime(System.currentTimeMillis());
                    report.setUpdateTime(System.currentTimeMillis());
                    apiScenarioReportMapper.updateByPrimaryKey(report);
                }
            } else {
                ApiDefinitionExecResult execResult = apiDefinitionExecResultMapper.selectByPrimaryKey(queue.getReportId());
                if (execResult != null) {
                    execResult.setStatus(APITestStatus.Running.name());
                    apiDefinitionExecResultMapper.updateByPrimaryKeySelective(execResult);
                }
            }
        }

        LoggerUtil.info("Scenario run-开始执行，队列ID：【 " + executionQueue.getReportId() + " 】");
        String reportId = StringUtils.isNotEmpty(executionQueue.getReportId()) ? executionQueue.getReportId() : queue.getReportId();
        if (!StringUtils.equalsAny(executionQueue.getRunMode(), ApiRunMode.SCENARIO.name())) {
            reportId = queue.getReportId();
        }
        HashTree hashTree = null;
        JmeterRunRequestDTO runRequest = new JmeterRunRequestDTO(queue.getTestId(), reportId, executionQueue.getRunMode(), hashTree);
        runRequest.setReportType(executionQueue.getReportType());
        runRequest.setPool(GenerateHashTreeUtil.isResourcePool(executionQueue.getPoolId()));
        runRequest.setTestPlanReportId(executionQueue.getReportId());
        runRequest.setRunType(RunModeConstants.SERIAL.toString());
        runRequest.setQueueId(executionQueue.getId());
        runRequest.setPoolId(executionQueue.getPoolId());
        try {
            if (StringUtils.isEmpty(executionQueue.getPoolId())) {
                if (StringUtils.equalsAny(executionQueue.getRunMode(), ApiRunMode.SCENARIO.name(), ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
                    ApiScenarioWithBLOBs scenario = null;
                    Map<String, String> planEnvMap = new LinkedHashMap<>();
                    if (StringUtils.equalsAny(executionQueue.getRunMode(), ApiRunMode.SCENARIO.name())) {
                        scenario = apiScenarioMapper.selectByPrimaryKey(queue.getTestId());
                    } else {
                        TestPlanApiScenario planApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(queue.getTestId());
                        if (planApiScenario != null) {
                            planEnvMap = apiScenarioEnvService.planEnvMap(queue.getTestId());
                            queue.setEvnMap(JSON.toJSONString(planEnvMap));
                            scenario = apiScenarioMapper.selectByPrimaryKey(planApiScenario.getApiScenarioId());
                        }
                    }
                    if ((planEnvMap == null || planEnvMap.isEmpty()) && StringUtils.isNotEmpty(queue.getEvnMap())) {
                        planEnvMap = JSON.parseObject(queue.getEvnMap(), Map.class);
                    }
                    hashTree = GenerateHashTreeUtil.generateHashTree(scenario, planEnvMap, runRequest);
                } else {
                    Map<String, String> map = new LinkedHashMap<>();
                    if (StringUtils.isNotEmpty(queue.getEvnMap())) {
                        map = JSON.parseObject(queue.getEvnMap(), Map.class);
                    }
                    hashTree = generateHashTree(queue.getTestId(), map, runRequest);
                }
                // 更新环境变量
                this.initEnv(hashTree);
            }
            runRequest.setHashTree(hashTree);
            if (queue != null) {
                runRequest.setPlatformUrl(queue.getId());
            }
            // 开始执行
            jMeterService.run(runRequest);
        } catch (Exception e) {
            RemakeReportService remakeReportService = CommonBeanFactory.getBean(RemakeReportService.class);
            remakeReportService.remake(runRequest);
            ResultDTO dto = new ResultDTO();
            BeanUtils.copyBean(dto, runRequest);
            CommonBeanFactory.getBean(ApiExecutionQueueService.class).queueNext(dto);
            LoggerUtil.error("执行终止：", e);
        }
    }

    private void initEnv(HashTree hashTree) {
        ApiTestEnvironmentService apiTestEnvironmentService = CommonBeanFactory.getBean(ApiTestEnvironmentService.class);
        HashTreeUtil hashTreeUtil = new HashTreeUtil();
        Map<String, Map<String, String>> envParamsMap = hashTreeUtil.getEnvParamsDataByHashTree(hashTree, apiTestEnvironmentService);
        hashTreeUtil.mergeParamDataMap(null, envParamsMap);
    }

    public HashTree generateHashTree(String testId, Map<String, String> envMap, JmeterRunRequestDTO runRequest) {
        try {
            ApiTestCaseWithBLOBs caseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(testId);
            String envId = null;
            if (caseWithBLOBs == null) {
                TestPlanApiCase apiCase = testPlanApiCaseMapper.selectByPrimaryKey(testId);
                if (apiCase != null) {
                    caseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(apiCase.getApiCaseId());
                    envId = apiCase.getEnvironmentId();
                }
            }
            if (envMap != null && envMap.containsKey(caseWithBLOBs.getProjectId())) {
                envId = envMap.get(caseWithBLOBs.getProjectId());
            }
            if (caseWithBLOBs != null) {
                HashTree jmeterHashTree = new HashTree();
                MsTestPlan testPlan = new MsTestPlan();
                testPlan.setHashTree(new LinkedList<>());

                MsThreadGroup group = new MsThreadGroup();
                group.setLabel(caseWithBLOBs.getName());
                group.setName(runRequest.getReportId());
                group.setProjectId(caseWithBLOBs.getProjectId());

                MsTestElement testElement = parse(caseWithBLOBs, testId, envId);
                group.setHashTree(new LinkedList<>());
                group.getHashTree().add(testElement);
                testPlan.getHashTree().add(group);
                testPlan.toHashTree(jmeterHashTree, testPlan.getHashTree(), new ParameterConfig());
                return jmeterHashTree;
            }
        } catch (Exception ex) {
            RemakeReportService remakeReportService = CommonBeanFactory.getBean(RemakeReportService.class);
            remakeReportService.remake(runRequest);
            ResultDTO dto = new ResultDTO();
            BeanUtils.copyBean(dto, runRequest);
            CommonBeanFactory.getBean(ApiExecutionQueueService.class).queueNext(dto);
            LoggerUtil.error("生成JMX执行脚本失败：", ex);
        }
        return null;
    }

    private MsTestElement parse(ApiTestCaseWithBLOBs caseWithBLOBs, String planId, String envId) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            String api = caseWithBLOBs.getRequest();
            JSONObject element = JSON.parseObject(api);
            ElementUtil.dataFormatting(element);

            LinkedList<MsTestElement> list = new LinkedList<>();
            if (element != null && StringUtils.isNotEmpty(element.getString("hashTree"))) {
                LinkedList<MsTestElement> elements = mapper.readValue(element.getString("hashTree"),
                        new TypeReference<LinkedList<MsTestElement>>() {
                        });
                list.addAll(elements);
            }
            if (element.getString("type").equals("HTTPSamplerProxy")) {
                MsHTTPSamplerProxy httpSamplerProxy = JSON.parseObject(api, MsHTTPSamplerProxy.class);
                httpSamplerProxy.setHashTree(list);
                httpSamplerProxy.setName(planId);
                if (StringUtils.isNotEmpty(envId)) {
                    httpSamplerProxy.setUseEnvironment(envId);
                }
                return httpSamplerProxy;
            }
            if (element.getString("type").equals("TCPSampler")) {
                MsTCPSampler msTCPSampler = JSON.parseObject(api, MsTCPSampler.class);
                if (StringUtils.isNotEmpty(envId)) {
                    msTCPSampler.setUseEnvironment(envId);
                }
                msTCPSampler.setHashTree(list);
                msTCPSampler.setName(planId);
                return msTCPSampler;
            }
            if (element.getString("type").equals("DubboSampler")) {
                MsDubboSampler dubboSampler = JSON.parseObject(api, MsDubboSampler.class);
                if (StringUtils.isNotEmpty(envId)) {
                    dubboSampler.setUseEnvironment(envId);
                }
                dubboSampler.setHashTree(list);
                dubboSampler.setName(planId);
                return dubboSampler;
            }
            if (element.getString("type").equals("JDBCSampler")) {
                MsJDBCSampler jDBCSampler = JSON.parseObject(api, MsJDBCSampler.class);
                if (StringUtils.isNotEmpty(envId)) {
                    jDBCSampler.setUseEnvironment(envId);
                }
                jDBCSampler.setHashTree(list);
                jDBCSampler.setName(planId);
                return jDBCSampler;
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return null;
    }
}
