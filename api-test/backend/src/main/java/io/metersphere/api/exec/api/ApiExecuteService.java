package io.metersphere.api.exec.api;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.dto.definition.RunCaseRequest;
import io.metersphere.api.dto.definition.RunDefinitionRequest;
import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.dto.definition.request.MsTestPlan;
import io.metersphere.api.dto.definition.request.MsThreadGroup;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.processors.MsJSR223Processor;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.jmeter.NewDriverManager;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ApiDefinitionMapper;
import io.metersphere.base.mapper.ApiTestCaseMapper;
import io.metersphere.base.mapper.ext.ExtApiTestCaseMapper;
import io.metersphere.base.mapper.plan.TestPlanApiCaseMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.service.definition.TcpApiParamService;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.json.JSONObject;
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
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private BaseEnvironmentService apiTestEnvironmentService;
    @Resource
    private TcpApiParamService tcpApiParamService;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private ObjectMapper mapper;
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;

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
        ApiDefinitionExecResultWithBLOBs report = ApiDefinitionExecResultUtil.add(caseWithBLOBs.getId(),
                ApiReportStatus.RUNNING.name(), request.getReportId(),
                Objects.requireNonNull(SessionUtils.getUser()).getId());
        report.setName(caseWithBLOBs.getName());
        report.setTriggerMode(ApiRunMode.JENKINS.name());
        report.setType(ApiRunMode.JENKINS.name());
        report.setProjectId(caseWithBLOBs.getProjectId());
        apiDefinitionExecResultMapper.insert(report);
        //更新接口案例的最后执行状态等信息
        caseWithBLOBs.setLastResultId(report.getId());
        caseWithBLOBs.setUpdateTime(System.currentTimeMillis());
        caseWithBLOBs.setStatus(ApiReportStatus.RUNNING.name());
        apiTestCaseMapper.updateByPrimaryKey(caseWithBLOBs);
        request.setReport(report);

        if (StringUtils.isEmpty(request.getRunMode())) {
            request.setRunMode(ApiRunMode.DEFINITION.name());
        }
        return this.exec(request);
    }

    public MsExecResponseDTO exec(RunCaseRequest request) {
        return exec(request, null);
    }

    public MsExecResponseDTO exec(RunCaseRequest request, Map<String, Object> extendedParameters) {
        ApiTestCaseWithBLOBs testCaseWithBLOBs = request.getBloBs();
        if (StringUtils.equals(request.getRunMode(), ApiRunMode.JENKINS_API_PLAN.name())) {
            testCaseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(request.getReportId());
            request.setCaseId(request.getReportId());
            //通过测试计划id查询环境
            request.setReportId(request.getTestPlanId());
        }
        LoggerUtil.info("开始执行单条用例【 " + testCaseWithBLOBs.getId() + " 】", request.getReportId());

        // 多态JSON普通转换会丢失内容，需要通过 ObjectMapper 获取
        if (testCaseWithBLOBs != null && StringUtils.isNotEmpty(testCaseWithBLOBs.getRequest())) {
            try {
                HashTree jmeterHashTree = this.generateHashTree(request, testCaseWithBLOBs);
                if (LoggerUtil.getLogger().isDebugEnabled()) {
                    LoggerUtil.debug("生成jmx文件：" + ElementUtil.hashTreeToString(jmeterHashTree));
                }
                // 调用执行方法
                JmeterRunRequestDTO runRequest = new JmeterRunRequestDTO(testCaseWithBLOBs.getId(), StringUtils.isEmpty(request.getReportId()) ? request.getId() : request.getReportId(), request.getRunMode(), jmeterHashTree);
                if (MapUtils.isNotEmpty(extendedParameters)) {
                    runRequest.setExtendedParameters(extendedParameters);
                }
                jMeterService.run(runRequest);
            } catch (Exception ex) {
                ApiDefinitionExecResult result = apiDefinitionExecResultMapper.selectByPrimaryKey(request.getReportId());
                if (result != null) {
                    result.setStatus(ApiReportStatus.ERROR.name());
                    apiDefinitionExecResultMapper.updateByPrimaryKey(result);
                    ApiTestCaseWithBLOBs caseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(request.getCaseId());
                    caseWithBLOBs.setStatus(ApiReportStatus.ERROR.name());
                    apiTestCaseMapper.updateByPrimaryKey(caseWithBLOBs);
                    ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = apiDefinitionMapper.selectByPrimaryKey(caseWithBLOBs.getApiDefinitionId());
                    if (apiDefinitionWithBLOBs.getProtocol().equals("HTTP")) {
                        apiDefinitionWithBLOBs.setToBeUpdated(true);
                        apiDefinitionWithBLOBs.setToBeUpdateTime(System.currentTimeMillis());
                        apiDefinitionMapper.updateByPrimaryKey(apiDefinitionWithBLOBs);
                    }
                }
                LogUtil.error(ex.getMessage(), ex);
            }
        }
        return new MsExecResponseDTO(request.getCaseId(), request.getReport().getId(), request.getRunMode());
    }

    public MsExecResponseDTO debug(RunDefinitionRequest request, List<MultipartFile> bodyFiles) {
        // 补充线程组ID
        if (request.getTestElement() != null
                && CollectionUtils.isNotEmpty(request.getTestElement().getHashTree())
                && StringUtils.equalsIgnoreCase(request.getTestElement().getHashTree().get(0).getType(), ElementConstants.THREAD_GROUP)) {
            request.getTestElement().getHashTree().get(0).setName(request.getId());
        }
        JmeterRunRequestDTO runRequest = this.initRunRequest(request, bodyFiles);
        // 开始执行
        jMeterService.run(runRequest);
        return new MsExecResponseDTO(runRequest.getTestId(), runRequest.getReportId(), runRequest.getRunMode());
    }

    public String getHashTree(MsJSR223Processor request) {
        MsTestPlan testPlan = new MsTestPlan();
        testPlan.setName(MsTestPlan.class.getCanonicalName());
        testPlan.setHashTree(new LinkedList<>());
        MsThreadGroup threadGroup = new MsThreadGroup();
        threadGroup.setName(MsThreadGroup.class.getCanonicalName());
        threadGroup.setHashTree(new LinkedList<>());
        testPlan.getHashTree().add(threadGroup);
        testPlan.setJarPaths(NewDriverManager.getJars(new ArrayList<>() {{
            this.add(request.getProjectId());
        }}));
        threadGroup.getHashTree().add(request);
        ParameterConfig config = new ParameterConfig();
        config.setProjectId(request.getProjectId());
        return new MsTestPlan().getJmx(testPlan.generateHashTree(config));
    }

    private JmeterRunRequestDTO initRunRequest(RunDefinitionRequest request, List<MultipartFile> bodyFiles) {
        ParameterConfig config = new ParameterConfig();
        config.setProjectId(request.getProjectId());

        Map<String, EnvironmentConfig> envConfig = new HashMap<>();
        Map<String, String> map = request.getEnvironmentMap();
        if (map != null && map.size() > 0) {
            for (String key : map.keySet()) {
                ApiTestEnvironmentWithBLOBs environment = apiTestEnvironmentService.get(map.get(key));
                if (environment != null) {
                    EnvironmentConfig env = JSONUtil.parseObject(environment.getConfig(), EnvironmentConfig.class);
                    env.setEnvironmentId(environment.getId());
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
                String tmpFilePath = "tmp/" + request.getId();
                body.setTmpFilePath(tmpFilePath);
                FileUtils.copyBdyFile(item.getId(), tmpFilePath);
                FileUtils.createBodyFiles(tmpFilePath, bodyFiles);
            });
        }
        //检查TCP数据结构，等其他进行处理
        tcpApiParamService.checkTestElement(request.getTestElement());

        String testId = request.getTestElement() != null && CollectionUtils.isNotEmpty(request.getTestElement().getHashTree()) && CollectionUtils.isNotEmpty(request.getTestElement().getHashTree().get(0).getHashTree()) ? request.getTestElement().getHashTree().get(0).getHashTree().get(0).getName() : request.getId();

        String runMode = ApiRunMode.DEFINITION.name();
        if (StringUtils.isNotBlank(request.getType()) && StringUtils.equals(request.getType(), ApiRunMode.API_PLAN.name())) {
            runMode = ApiRunMode.API_PLAN.name();
        }
        // 加载自定义JAR
        NewDriverManager.loadJar(request);
        HashTree hashTree = request.getTestElement().generateHashTree(config);
        if (LoggerUtil.getLogger().isDebugEnabled()) {
            LoggerUtil.debug("生成执行JMX内容【 " + request.getTestElement().getJmx(hashTree) + " 】");
        }

        JmeterRunRequestDTO runRequest = new JmeterRunRequestDTO(testId, request.getId(), runMode, hashTree);
        runRequest.setDebug(request.isDebug());
        runRequest.setRunMode(runMode);
        runRequest.setExtendedParameters(new HashMap<String, Object>() {{
            this.put("SYN_RES", request.isSyncResult());
            this.put("userId", SessionUtils.getUser().getId());
            this.put("userName", SessionUtils.getUser().getName());
        }});
        return runRequest;
    }

    public HashTree generateHashTree(RunCaseRequest request, ApiTestCaseWithBLOBs testCaseWithBLOBs) throws Exception {
        JSONObject elementObj = JSONUtil.parseObject(testCaseWithBLOBs.getRequest());
        ElementUtil.dataFormatting(elementObj);

        MsTestElement element = mapper.readValue(elementObj.toString(), new TypeReference<MsTestElement>() {
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
        // 获取自定义JAR
        String projectId = testCaseWithBLOBs.getProjectId();
        testPlan.setJarPaths(NewDriverManager.getJars(new ArrayList<>() {{
            this.add(projectId);
        }}));
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

        BaseEnvironmentService apiTestEnvironmentService = CommonBeanFactory.getBean(BaseEnvironmentService.class);
        ApiTestEnvironmentWithBLOBs environment = apiTestEnvironmentService.get(request.getEnvironmentId());
        ParameterConfig parameterConfig = new ParameterConfig();

        Map<String, EnvironmentConfig> envConfig = new HashMap<>(16);
        if (environment != null && environment.getConfig() != null) {
            EnvironmentConfig environmentConfig = JSONUtil.parseObject(environment.getConfig(), EnvironmentConfig.class);
            environmentConfig.setEnvironmentId(environment.getId());
            envConfig.put(testCaseWithBLOBs.getProjectId(), environmentConfig);
            parameterConfig.setConfig(envConfig);
        }

        testPlan.toHashTree(jmeterHashTree, testPlan.getHashTree(), parameterConfig);
        return jmeterHashTree;
    }
}
