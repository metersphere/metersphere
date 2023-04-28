package io.metersphere.api.exec.scenario;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.fasterxml.jackson.core.type.TypeReference;
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
import io.metersphere.api.exec.utils.PerformInspectionUtil;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.jmeter.utils.SmoothWeighted;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.api.service.RemakeReportService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.HashTreeUtil;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.service.SystemParameterService;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.collections.HashTree;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
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
    @Resource
    private ObjectMapper mapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RemakeReportService remakeReportService;

    public void serial(ApiExecutionQueue executionQueue, ApiExecutionQueueDetail queue) {
        String reportId = StringUtils.isNotEmpty(executionQueue.getReportId()) ? executionQueue.getReportId() : queue.getReportId();
        if (!StringUtils.equalsAny(executionQueue.getRunMode(), ApiRunMode.SCENARIO.name())) {
            reportId = queue.getReportId();
        }
        JmeterRunRequestDTO runRequest = new JmeterRunRequestDTO(queue.getTestId(), reportId, executionQueue.getRunMode(), null);
        // 获取可以执行的资源池
        BaseSystemConfigDTO baseInfo = CommonBeanFactory.getBean(SystemParameterService.class).getBaseInfo();
        try {
            runRequest.setReportType(executionQueue.getReportType());
            runRequest.setPool(GenerateHashTreeUtil.isResourcePool(executionQueue.getPoolId()));
            runRequest.setTestPlanReportId(executionQueue.getReportId());
            runRequest.setRunType(RunModeConstants.SERIAL.toString());
            runRequest.setQueueId(executionQueue.getId());
            runRequest.setPoolId(executionQueue.getPoolId());

            if (StringUtils.isEmpty(executionQueue.getPoolId())) {
                if (StringUtils.equalsAny(executionQueue.getRunMode(),
                        ApiRunMode.SCENARIO.name(),
                        ApiRunMode.SCENARIO_PLAN.name(),
                        ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(),
                        ApiRunMode.SCHEDULE_SCENARIO.name(),
                        ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {

                    ApiScenarioWithBLOBs scenario = null;
                    Map<String, String> planEnvMap = new LinkedHashMap<>();
                    if (StringUtils.equalsAny(executionQueue.getRunMode(), ApiRunMode.SCENARIO.name(), ApiRunMode.SCHEDULE_SCENARIO.name())) {
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
                    runRequest.setHashTree(GenerateHashTreeUtil.generateHashTree(scenario, planEnvMap, runRequest));
                } else {
                    Map<String, String> map = new LinkedHashMap<>();
                    if (StringUtils.isNotEmpty(queue.getEvnMap())) {
                        map = JSON.parseObject(queue.getEvnMap(), Map.class);
                    }
                    runRequest.setHashTree(generateHashTree(queue.getTestId(), map, runRequest));
                }

                // 更新环境变量
                if (runRequest.getHashTree() != null) {
                    this.initEnv(runRequest.getHashTree());
                }
            }

            if (queue != null) {
                runRequest.setPlatformUrl(GenerateHashTreeUtil.getPlatformUrl(baseInfo, runRequest, queue.getId()));
            }
            if (runRequest.getPool().isPool()) {
                SmoothWeighted.setServerConfig(runRequest.getPoolId(), redisTemplate);
            }
            runRequest.setRunType(RunModeConstants.SERIAL.toString());
        } catch (Exception e) {
            remakeReportService.testEnded(runRequest, e.getMessage());
            LoggerUtil.error("执行队列[" + queue.getId() + "]入队列失败：", queue.getReportId(), e);
            return;
        }
        // 判断触发资源对象是用例/场景更新对应报告状态
        if (!GenerateHashTreeUtil.isSetReport(executionQueue.getReportType())
                || StringUtils.equalsIgnoreCase(executionQueue.getRunMode(), ApiRunMode.DEFINITION.name())) {
            if (StringUtils.equalsAny(executionQueue.getRunMode(),
                    ApiRunMode.SCENARIO.name(),
                    ApiRunMode.SCENARIO_PLAN.name(),
                    ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(),
                    ApiRunMode.SCHEDULE_SCENARIO.name(),
                    ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
                updateReportToRunning(queue, runRequest);
            } else {
                updateDefinitionExecResultToRunning(queue, runRequest);
            }
        }
        // 开始执行
        jMeterService.run(runRequest);
    }

    protected void updateDefinitionExecResultToRunning(ApiExecutionQueueDetail queue, JmeterRunRequestDTO runRequest) {
        ApiDefinitionExecResult execResult = apiDefinitionExecResultMapper.selectByPrimaryKey(queue.getReportId());
        if (execResult != null) {
            runRequest.setExtendedParameters(new HashMap<String, Object>() {{
                this.put("userId", execResult.getUserId());
            }});
            execResult.setStartTime(System.currentTimeMillis());
            execResult.setStatus(APITestStatus.Running.name());
            apiDefinitionExecResultMapper.updateByPrimaryKeySelective(execResult);
            LoggerUtil.info("进入串行模式，准备执行资源：[" + execResult.getName() + " ]", execResult.getId());
        }
    }

    public void updateReportToRunning(ApiExecutionQueueDetail queue, JmeterRunRequestDTO runRequest) {
        ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(queue.getReportId());
        if (report != null) {
            report.setStatus(APITestStatus.Running.name());
            report.setCreateTime(System.currentTimeMillis());
            report.setUpdateTime(System.currentTimeMillis());
            runRequest.setExtendedParameters(new HashMap<String, Object>() {{
                this.put("userId", report.getCreateUser());
            }});
            apiScenarioReportMapper.updateByPrimaryKey(report);
            LoggerUtil.info("进入串行模式，准备执行资源：[ " + report.getName() + " ]", report.getId());
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
                String data = caseWithBLOBs.getRequest();
                // 检查执行内容合规性
                PerformInspectionUtil.countMatches(data, caseWithBLOBs.getId());

                HashTree jmeterHashTree = new HashTree();
                MsTestPlan testPlan = new MsTestPlan();
                testPlan.setHashTree(new LinkedList<>());

                MsThreadGroup group = new MsThreadGroup();
                group.setLabel(caseWithBLOBs.getName());
                group.setName(runRequest.getReportId());
                group.setProjectId(caseWithBLOBs.getProjectId());

                MsTestElement testElement = parse(data, testId, envId, caseWithBLOBs.getProjectId());
                group.setHashTree(new LinkedList<>());
                group.getHashTree().add(testElement);
                testPlan.getHashTree().add(group);
                testPlan.toHashTree(jmeterHashTree, testPlan.getHashTree(), new ParameterConfig());

                LoggerUtil.info("用例资源：" + caseWithBLOBs.getName() + ", 生成执行脚本JMX成功", runRequest.getReportId());
                return jmeterHashTree;
            }
        } catch (Exception ex) {
            MSException.throwException("生成脚本失败：【 " + ex.getMessage() + " 】");
        }
        return null;
    }

    private MsTestElement parse(String api, String planId, String envId, String projectId) {
        try {
            JSONObject element = JSON.parseObject(api, Feature.DisableSpecialKeyDetect);
            ElementUtil.dataFormatting(element);

            LinkedList<MsTestElement> list = new LinkedList<>();
            if (element != null && StringUtils.isNotEmpty(element.getString("hashTree"))) {
                LinkedList<MsTestElement> elements = mapper.readValue(element.getString("hashTree"),
                        new TypeReference<LinkedList<MsTestElement>>() {
                        });
                list.addAll(elements);
            }
            if (element.getString("type").equals("HTTPSamplerProxy")) {
                MsHTTPSamplerProxy httpSamplerProxy = JSON.parseObject(api, MsHTTPSamplerProxy.class, Feature.DisableSpecialKeyDetect);
                httpSamplerProxy.setHashTree(list);
                httpSamplerProxy.setName(planId);
                if (StringUtils.isNotEmpty(envId)) {
                    httpSamplerProxy.setUseEnvironment(envId);
                }
                return httpSamplerProxy;
            }
            if (element.getString("type").equals("TCPSampler")) {
                MsTCPSampler msTCPSampler = JSON.parseObject(api, MsTCPSampler.class, Feature.DisableSpecialKeyDetect);
                if (StringUtils.isEmpty(msTCPSampler.getProjectId())) {
                    msTCPSampler.setProjectId(projectId);
                }
                if (StringUtils.isNotEmpty(envId)) {
                    msTCPSampler.setUseEnvironment(envId);
                }
                msTCPSampler.setHashTree(list);
                msTCPSampler.setName(planId);
                return msTCPSampler;
            }
            if (element.getString("type").equals("DubboSampler")) {
                MsDubboSampler dubboSampler = JSON.parseObject(api, MsDubboSampler.class, Feature.DisableSpecialKeyDetect);
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
