package io.metersphere.api.exec.api;


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
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.constants.PropertyConstant;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.commons.utils.*;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.dto.ResultDTO;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.service.ApiExecutionQueueService;
import io.metersphere.service.RemakeReportService;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.xpack.api.service.ApiRetryOnFailureService;
import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.collections.HashTree;
import org.json.JSONObject;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiCaseSerialService {
    private final static String PROJECT_ID = "projectId";
    @Resource
    private JMeterService jMeterService;
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ObjectMapper mapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;

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
                this.put("userId", execResult.getUserId());
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
                String data = caseWithBLOBs.getRequest();
                HashTree jmeterHashTree = new HashTree();
                MsTestPlan testPlan = new MsTestPlan();

                if (!runRequest.getPool().isPool()) {
                    // 获取自定义JAR
                    String projectId = caseWithBLOBs.getProjectId();
                    testPlan.setJarPaths(NewDriverManager.getJars(new ArrayList<>() {{
                        this.add(projectId);
                    }}));
                }
                testPlan.setHashTree(new LinkedList<>());
                MsThreadGroup group = new MsThreadGroup();
                group.setLabel(caseWithBLOBs.getName());
                group.setName(runRequest.getReportId());
                // 接口用例集成报告
                if (StringUtils.isNotEmpty(runRequest.getTestPlanReportId())
                        && StringUtils.equals(runRequest.getReportType(), RunModeConstants.SET_REPORT.toString())) {
                    group.setName(runRequest.getTestPlanReportId());
                }
                group.setProjectId(caseWithBLOBs.getProjectId());
                MsTestElement testElement;
                if (runRequest.isRetryEnable() && runRequest.getRetryNum() > 0) {
                    // 失败重试
                    ApiRetryOnFailureService apiRetryOnFailureService = CommonBeanFactory.getBean(ApiRetryOnFailureService.class);
                    String retryData = apiRetryOnFailureService.retry(data, runRequest.getRetryNum(), true);
                    data = StringUtils.isNotEmpty(retryData) ? retryData : data;
                    // 格式化数据
                    testElement = apiRetryOnFailureService.retryParse(data);
                    MsTestElement element = parse(JSON.toJSONString(testElement.getHashTree().get(0)), testId, envId, caseWithBLOBs.getProjectId());
                    testElement.setHashTree(new LinkedList<>() {{
                        this.add(element);
                    }});
                } else {
                    testElement = parse(data, testId, envId, caseWithBLOBs.getProjectId());
                }

                group.setHashTree(new LinkedList<>());
                group.getHashTree().add(testElement);
                testPlan.getHashTree().add(group);
                testPlan.toHashTree(jmeterHashTree, testPlan.getHashTree(), new ParameterConfig());

                LoggerUtil.info("用例资源：" + caseWithBLOBs.getName() + ", 生成执行脚本JMX成功", runRequest.getReportId());
                return jmeterHashTree;
            }
        } catch (Exception ex) {
            RemakeReportService remakeReportService = CommonBeanFactory.getBean(RemakeReportService.class);
            remakeReportService.remake(runRequest);
            ResultDTO dto = new ResultDTO();
            BeanUtils.copyBean(dto, runRequest);
            CommonBeanFactory.getBean(ApiExecutionQueueService.class).queueNext(dto);
            LoggerUtil.error("用例资源：" + testId + ", 生成执行脚本失败", runRequest.getReportId(), ex);
        }
        return null;
    }

    private MsTestElement parse(String api, String planId, String envId, String projectId) {
        try {
            JSONObject element = JSONUtil.parseObject(api);
            ElementUtil.dataFormatting(element);

            LinkedList<MsTestElement> list = new LinkedList<>();
            if (element != null && StringUtils.isNotEmpty(element.optString(ElementConstants.HASH_TREE))) {
                LinkedList<MsTestElement> elements = mapper.readValue(element.optString(ElementConstants.HASH_TREE),
                        new TypeReference<LinkedList<MsTestElement>>() {
                        });
                list.addAll(elements);
            }
            if (element.optString(PropertyConstant.TYPE).equals(ElementConstants.HTTP_SAMPLER)) {
                MsHTTPSamplerProxy httpSamplerProxy = mapper.readValue(element.toString(), new TypeReference<MsHTTPSamplerProxy>() {
                });
                httpSamplerProxy.setHashTree(list);
                httpSamplerProxy.setName(planId);
                if (StringUtils.isNotEmpty(envId)) {
                    httpSamplerProxy.setUseEnvironment(envId);
                }
                return httpSamplerProxy;
            }
            if (element.optString(PropertyConstant.TYPE).equals(ElementConstants.TCP_SAMPLER)) {
                MsTCPSampler msTCPSampler = JSON.parseObject(api, MsTCPSampler.class);
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
            if (element.optString(PropertyConstant.TYPE).equals(ElementConstants.DUBBO_SAMPLER)) {
                MsDubboSampler dubboSampler = JSON.parseObject(api, MsDubboSampler.class);
                if (StringUtils.isNotEmpty(envId)) {
                    dubboSampler.setUseEnvironment(envId);
                }
                dubboSampler.setHashTree(list);
                dubboSampler.setName(planId);
                return dubboSampler;
            }
            if (element.optString(PropertyConstant.TYPE).equals(ElementConstants.JDBC_SAMPLER)) {
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
