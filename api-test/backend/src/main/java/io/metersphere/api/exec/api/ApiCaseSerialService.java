package io.metersphere.api.exec.api;


import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.dto.definition.request.MsTestPlan;
import io.metersphere.api.dto.definition.request.MsThreadGroup;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.exec.queue.DBTestQueue;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.jmeter.NewDriverManager;
import io.metersphere.api.jmeter.utils.SmoothWeighted;
import io.metersphere.base.domain.ApiDefinitionExecResultWithBLOBs;
import io.metersphere.base.domain.ApiExecutionQueueDetail;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.base.domain.TestPlanApiCase;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ApiTestCaseMapper;
import io.metersphere.base.mapper.plan.TestPlanApiCaseMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.CommonConstants;
import io.metersphere.commons.constants.PropertyConstant;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.commons.utils.*;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.dto.ProjectJarConfig;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.service.ApiRetryOnFailureService;
import io.metersphere.service.RemakeReportService;
import io.metersphere.utils.LoggerUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.collections.HashTree;
import org.json.JSONObject;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiCaseSerialService {
    private final static String PROJECT_ID = "projectId";
    public static final String NAME = "name";
    @Resource
    private JMeterService jMeterService;
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    private ApiRetryOnFailureService apiRetryOnFailureService;

    public void serial(DBTestQueue executionQueue) {
        ApiExecutionQueueDetail queue = executionQueue.getDetail();
        JmeterRunRequestDTO runRequest = RequestParamsUtil.init(executionQueue, queue, queue.getReportId());
        // 判断触发资源对象是用例
        if (!GenerateHashTreeUtil.isSetReport(executionQueue.getReportType())
                || StringUtils.equalsIgnoreCase(executionQueue.getRunMode(), ApiRunMode.DEFINITION.name())) {
            updateDefinitionExecResultToRunning(queue, runRequest);
        }
        try {
            if (StringUtils.isEmpty(executionQueue.getPoolId())) {
                Map<String, String> map = new LinkedHashMap<>();
                if (StringUtils.isNotEmpty(queue.getEvnMap())) {
                    map = JSON.parseObject(queue.getEvnMap(), Map.class);
                }
                runRequest.setHashTree(generateHashTree(queue.getTestId(), map, runRequest));
                // 更新环境变量
                if (runRequest.getHashTree() != null) {
                    this.initEnv(runRequest.getHashTree());
                }
            }

            if (runRequest.getPool().isPool()) {
                SmoothWeighted.setServerConfig(runRequest.getPoolId(), redisTemplate);
            }
            // 开始执行
            runRequest.getExtendedParameters().put(PROJECT_ID, queue.getProjectIds());
            jMeterService.run(runRequest);
        } catch (Exception e) {
            RequestParamsUtil.rollback(runRequest, e);
        }
    }

    protected void updateDefinitionExecResultToRunning(ApiExecutionQueueDetail queue, JmeterRunRequestDTO runRequest) {
        ApiDefinitionExecResultWithBLOBs execResult = apiDefinitionExecResultMapper.selectByPrimaryKey(queue.getReportId());
        if (execResult != null) {
            runRequest.setExtendedParameters(new HashMap<String, Object>() {{
                this.put(CommonConstants.USER_ID, execResult.getUserId());
            }});
            execResult.setStartTime(System.currentTimeMillis());
            execResult.setStatus(ApiReportStatus.RUNNING.name());
            apiDefinitionExecResultMapper.updateByPrimaryKeySelective(execResult);
            LoggerUtil.info("进入串行模式，准备执行资源：[" + execResult.getName() + " ]", execResult.getId());
        }
    }

    private void initEnv(HashTree hashTree) {
        BaseEnvironmentService apiTestEnvironmentService = CommonBeanFactory.getBean(BaseEnvironmentService.class);
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
                // 获取自定义JAR
                String projectId = caseWithBLOBs.getProjectId();
                Map<String, List<ProjectJarConfig>> jars = NewDriverManager.getJars(new ArrayList<>() {{
                    this.add(projectId);
                }}, runRequest.getPool());
                testPlan.setProjectJarIds(jars.keySet().stream().toList());
                testPlan.setPoolJarsMap(jars);
                testPlan.setHashTree(new LinkedList<>());
                MsThreadGroup group = new MsThreadGroup();
                group.setLabel(caseWithBLOBs.getName());
                group.setName(runRequest.getReportId());
                group.setHashTree(new LinkedList<>());
                // 接口用例集成报告
                if (StringUtils.isNotEmpty(runRequest.getTestPlanReportId())
                        && StringUtils.equals(runRequest.getReportType(), RunModeConstants.SET_REPORT.toString())) {
                    group.setName(runRequest.getTestPlanReportId());
                }
                group.setProjectId(caseWithBLOBs.getProjectId());
                // 数据兼容处理
                JSONObject element = JSONUtil.parseObject(caseWithBLOBs.getRequest());
                ElementUtil.dataFormatting(element);
                parse(element, testId, envId, caseWithBLOBs.getProjectId());
                String runData = element.toString();
                if (runRequest.isRetryEnable() && runRequest.getRetryNum() > 0) {
                    try {
                        // 失败重试
                        String retryData = apiRetryOnFailureService.retry(runData, runRequest.getRetryNum(), true);
                        if (StringUtils.isNotBlank(retryData)) {
                            runData = retryData;
                        }
                    } catch (Exception e) {
                        LoggerUtil.error("失败重试脚本生成失败 ", runRequest.getReportId(), e);
                    }
                }
                group.getHashTree().add(JSONUtil.parseObject(runData, MsTestElement.class));
                testPlan.getHashTree().add(group);
                testPlan.toHashTree(jmeterHashTree, testPlan.getHashTree(), new ParameterConfig());
                LoggerUtil.info("用例资源：" + caseWithBLOBs.getName() + ", 生成执行脚本JMX成功", runRequest.getReportId());
                return jmeterHashTree;
            }
        } catch (Exception ex) {
            RemakeReportService remakeReportService = CommonBeanFactory.getBean(RemakeReportService.class);
            remakeReportService.testEnded(runRequest, ex.getMessage());
            LoggerUtil.error("用例资源：" + testId + ", 生成执行脚本失败", runRequest.getReportId(), ex);
        }
        return null;
    }

    private void parse(JSONObject element, String testId, String envId, String projectId) {
        try {
            element.putOpt(NAME, testId);
            if (StringUtils.isNotEmpty(envId)) {
                element.putOpt(PropertyConstant.ENVIRONMENT, envId);
            }
            if (StringUtils.isBlank(element.optString(PROJECT_ID))) {
                element.putOpt(PROJECT_ID, projectId);
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }
}
