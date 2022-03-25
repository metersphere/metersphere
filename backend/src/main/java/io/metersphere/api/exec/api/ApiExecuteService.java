package io.metersphere.api.exec.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.dto.definition.RunCaseRequest;
import io.metersphere.api.dto.definition.RunDefinitionRequest;
import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.dto.definition.request.MsTestPlan;
import io.metersphere.api.dto.definition.request.MsThreadGroup;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.exec.utils.ApiDefinitionExecResultUtil;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.api.service.TcpApiParamService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ApiTestCaseMapper;
import io.metersphere.base.mapper.TestPlanApiCaseMapper;
import io.metersphere.base.mapper.ext.ExtApiTestCaseMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiExecuteService {
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private JMeterService jMeterService;
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    private ApiTestEnvironmentService environmentService;
    @Resource
    private TcpApiParamService tcpApiParamService;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;

    public MsExecResponseDTO jenkinsRun(RunCaseRequest request) {
        ApiTestCaseWithBLOBs caseWithBLOBs = null;
        if (request.getBloBs() == null) {
            caseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(request.getCaseId());
            if (caseWithBLOBs == null) {
                return null;
            }
            request.setBloBs(caseWithBLOBs);
        } else {
            caseWithBLOBs = request.getBloBs();
        }
        if (caseWithBLOBs == null) {
            return null;
        }
        if (StringUtils.isBlank(request.getEnvironmentId())) {
            request.setEnvironmentId(extApiTestCaseMapper.getApiCaseEnvironment(request.getCaseId()));
        }
        //提前生成报告
        ApiDefinitionExecResult report = ApiDefinitionExecResultUtil.add(caseWithBLOBs.getId(), APITestStatus.Running.name(), request.getReportId());
        report.setName(caseWithBLOBs.getName());
        report.setTriggerMode(ApiRunMode.JENKINS.name());
        report.setType(ApiRunMode.JENKINS.name());
        report.setProjectId(caseWithBLOBs.getProjectId());
        apiDefinitionExecResultMapper.insert(report);
        //更新接口案例的最后执行状态等信息
        caseWithBLOBs.setLastResultId(report.getId());
        caseWithBLOBs.setUpdateTime(System.currentTimeMillis());
        caseWithBLOBs.setStatus(APITestStatus.Running.name());
        apiTestCaseMapper.updateByPrimaryKey(caseWithBLOBs);
        request.setReport(report);

        if (StringUtils.isEmpty(request.getRunMode())) {
            request.setRunMode(ApiRunMode.DEFINITION.name());
        }
        return this.exec(request);
    }

    public MsExecResponseDTO exec(RunCaseRequest request) {
        ApiTestCaseWithBLOBs testCaseWithBLOBs = request.getBloBs();
        if (StringUtils.equals(request.getRunMode(), ApiRunMode.JENKINS_API_PLAN.name())) {
            testCaseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(request.getReportId());
            request.setCaseId(request.getReportId());
            //通过测试计划id查询环境
            request.setReportId(request.getTestPlanId());
        }
        LoggerUtil.info("开始执行单条用例【 " + testCaseWithBLOBs.getId() + " 】");

        // 多态JSON普通转换会丢失内容，需要通过 ObjectMapper 获取
        if (testCaseWithBLOBs != null && StringUtils.isNotEmpty(testCaseWithBLOBs.getRequest())) {
            try {
                HashTree jmeterHashTree = this.generateHashTree(request, testCaseWithBLOBs);
                if (LoggerUtil.getLogger().isDebugEnabled()) {
                    LoggerUtil.debug("生成jmx文件：" + ElementUtil.hashTreeToString(jmeterHashTree));
                }
                // 调用执行方法
                JmeterRunRequestDTO runRequest = new JmeterRunRequestDTO(testCaseWithBLOBs.getId(), StringUtils.isEmpty(request.getReportId()) ? request.getId() : request.getReportId(), request.getRunMode(), jmeterHashTree);
                jMeterService.run(runRequest);
            } catch (Exception ex) {
                ApiDefinitionExecResult result = apiDefinitionExecResultMapper.selectByPrimaryKey(request.getReportId());
                if (result != null) {
                    result.setStatus("error");
                    apiDefinitionExecResultMapper.updateByPrimaryKey(result);
                    ApiTestCaseWithBLOBs caseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(request.getCaseId());
                    caseWithBLOBs.setStatus("error");
                    apiTestCaseMapper.updateByPrimaryKey(caseWithBLOBs);
                }
                LogUtil.error(ex.getMessage(), ex);
            }
        }
        return new MsExecResponseDTO(request.getCaseId(), request.getReport().getId(), request.getRunMode());
    }

    public MsExecResponseDTO debug(RunDefinitionRequest request, List<MultipartFile> bodyFiles) {
        ParameterConfig config = new ParameterConfig();
        config.setProjectId(request.getProjectId());

        Map<String, EnvironmentConfig> envConfig = new HashMap<>();
        Map<String, String> map = request.getEnvironmentMap();
        if (map != null && map.size() > 0) {
            for (String key : map.keySet()) {
                ApiTestEnvironmentWithBLOBs environment = environmentService.get(map.get(key));
                if (environment != null) {
                    EnvironmentConfig env = JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class);
                    env.setApiEnvironmentid(environment.getId());
                    envConfig.put(key, env);
                }
            }
            config.setConfig(envConfig);
        }

        if (CollectionUtils.isNotEmpty(bodyFiles)) {
            List<MsHTTPSamplerProxy> requests = MsHTTPSamplerProxy.findHttpSampleFromHashTree(request.getTestElement());
            // 单接口调试生成tmp临时目录
            requests.forEach(item -> {
                Body body = item.getBody();
                String tmpFilePath = "tmp/" + UUID.randomUUID().toString();
                body.setTmpFilePath(tmpFilePath);
                FileUtils.copyBdyFile(item.getId(), tmpFilePath);
                FileUtils.createBodyFiles(tmpFilePath, bodyFiles);
            });
        }
        //检查TCP数据结构，等其他进行处理
        tcpApiParamService.checkTestElement(request.getTestElement());

        HashTree hashTree = request.getTestElement().generateHashTree(config);
        if (LoggerUtil.getLogger().isDebugEnabled()) {
            LoggerUtil.debug("生成执行JMX内容【 " + request.getTestElement().getJmx(hashTree) + " 】");
        }

        String runMode = ApiRunMode.DEFINITION.name();
        if (StringUtils.isNotBlank(request.getType()) && StringUtils.equals(request.getType(), ApiRunMode.API_PLAN.name())) {
            runMode = ApiRunMode.API_PLAN.name();
        }

        String testId = request.getTestElement() != null &&
                CollectionUtils.isNotEmpty(request.getTestElement().getHashTree()) &&
                CollectionUtils.isNotEmpty(request.getTestElement().getHashTree().get(0).getHashTree()) ?
                request.getTestElement().getHashTree().get(0).getHashTree().get(0).getName() : request.getId();
        JmeterRunRequestDTO runRequest = new JmeterRunRequestDTO(testId, request.getId(), runMode, hashTree);
        runRequest.setDebug(request.isDebug());
        // 开始执行
        jMeterService.run(runRequest);
        return new MsExecResponseDTO(runRequest.getTestId(), runRequest.getReportId(), runMode);
    }

    public HashTree generateHashTree(RunCaseRequest request, ApiTestCaseWithBLOBs testCaseWithBLOBs) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JSONObject elementObj = JSON.parseObject(testCaseWithBLOBs.getRequest());
        ElementUtil.dataFormatting(elementObj);

        MsTestElement element = mapper.readValue(elementObj.toJSONString(), new TypeReference<MsTestElement>() {
        });
        element.setProjectId(testCaseWithBLOBs.getProjectId());
        if (StringUtils.isBlank(request.getEnvironmentId())) {
            TestPlanApiCaseExample example = new TestPlanApiCaseExample();
            example.createCriteria().andTestPlanIdEqualTo(request.getTestPlanId()).andApiCaseIdEqualTo(request.getCaseId());
            List<TestPlanApiCase> list = testPlanApiCaseMapper.selectByExample(example);
            request.setEnvironmentId(list.get(0).getEnvironmentId());
            element.setName(list.get(0).getId());
        } else {
            element.setName(request.getCaseId());
        }

        // 测试计划
        MsTestPlan testPlan = new MsTestPlan();
        testPlan.setHashTree(new LinkedList<>());
        HashTree jmeterHashTree = new ListedHashTree();

        // 线程组
        MsThreadGroup group = new MsThreadGroup();
        group.setLabel(testCaseWithBLOBs.getName());
        group.setName(testCaseWithBLOBs.getId());
        group.setOnSampleError(true);
        LinkedList<MsTestElement> hashTrees = new LinkedList<>();
        hashTrees.add(element);
        group.setHashTree(hashTrees);
        testPlan.getHashTree().add(group);

        ApiTestEnvironmentService environmentService = CommonBeanFactory.getBean(ApiTestEnvironmentService.class);
        ApiTestEnvironmentWithBLOBs environment = environmentService.get(request.getEnvironmentId());
        ParameterConfig parameterConfig = new ParameterConfig();

        Map<String, EnvironmentConfig> envConfig = new HashMap<>(16);
        if (environment != null && environment.getConfig() != null) {
            EnvironmentConfig environmentConfig = JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class);
            environmentConfig.setApiEnvironmentid(environment.getId());
            envConfig.put(testCaseWithBLOBs.getProjectId(), environmentConfig);
            parameterConfig.setConfig(envConfig);
        }

        testPlan.toHashTree(jmeterHashTree, testPlan.getHashTree(), parameterConfig);
        return jmeterHashTree;
    }

}
