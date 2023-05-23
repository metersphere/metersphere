package io.metersphere.api.exec.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.fasterxml.jackson.core.type.TypeReference;
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
import io.metersphere.api.exec.utils.PerformInspectionUtil;
import io.metersphere.api.exec.utils.GenerateHashTreeUtil;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.api.service.RedisTemplateService;
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
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.dto.JmeterRunRequestDTO;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.service.SystemParameterService;
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
    @Resource
    private ObjectMapper mapper;
    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private RedisTemplateService redisTemplateService;

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
        RunModeConfigDTO runModeConfigDTO = new RunModeConfigDTO();
        jMeterService.verifyPool(caseWithBLOBs.getProjectId(), runModeConfigDTO);

        //提前生成报告
        ApiDefinitionExecResult report = ApiDefinitionExecResultUtil.add(caseWithBLOBs.getId(),
                APITestStatus.Running.name(),
                request.getReportId(),
                Objects.requireNonNull(SessionUtils.getUser()).getId());
        report.setActuator(runModeConfigDTO.getResourcePoolId());
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
        PerformInspectionUtil.countMatches(testCaseWithBLOBs.getRequest(), testCaseWithBLOBs.getId());

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
        config.setApi(true);
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

        String testId = request.getTestElement() != null &&
                CollectionUtils.isNotEmpty(request.getTestElement().getHashTree()) &&
                CollectionUtils.isNotEmpty(request.getTestElement().getHashTree().get(0).getHashTree()) ?
                request.getTestElement().getHashTree().get(0).getHashTree().get(0).getId() : request.getId();

        String runMode = ApiRunMode.DEFINITION.name();
        if (StringUtils.isNotBlank(request.getType()) && StringUtils.equals(request.getType(), ApiRunMode.API_PLAN.name())) {
            runMode = ApiRunMode.API_PLAN.name();
            testId = request.getTestElement() != null &&
                    CollectionUtils.isNotEmpty(request.getTestElement().getHashTree()) &&
                    CollectionUtils.isNotEmpty(request.getTestElement().getHashTree().get(0).getHashTree()) ?
                    request.getTestElement().getHashTree().get(0).getHashTree().get(0).getName() : request.getId();
        }

        HashTree hashTree = request.getTestElement().generateHashTree(config);
        String jmx = request.getTestElement().getJmx(hashTree);
        LoggerUtil.info("生成执行JMX内容【 " + jmx + " 】");
        // 检查执行内容合规性
        PerformInspectionUtil.inspection(jmx, testId, 4);
        JmeterRunRequestDTO runRequest = new JmeterRunRequestDTO(testId, request.getId(), runMode, hashTree);
        runRequest.setDebug(request.isDebug());

        runRequest.setExtendedParameters(new HashMap<String, Object>() {{
            this.put("SYN_RES", request.isSyncResult());
            this.put("userId", SessionUtils.getUser().getId());
            this.put("userName", SessionUtils.getUser().getName());
        }});
        // 开始执行
        if (StringUtils.isNotEmpty(request.getConfig().getResourcePoolId())) {
            runRequest.setPool(GenerateHashTreeUtil.isResourcePool(request.getConfig().getResourcePoolId()));
            runRequest.setPoolId(request.getConfig().getResourcePoolId());
            BaseSystemConfigDTO baseInfo = systemParameterService.getBaseInfo();
            runRequest.setPlatformUrl(GenerateHashTreeUtil.getPlatformUrl(baseInfo, runRequest, null));
        }
        if (StringUtils.equals(request.getType(), ApiRunMode.API_PLAN.name())) {
            redisTemplateService.lock(testId, request.getId());
        }
        jMeterService.run(runRequest);
        return new MsExecResponseDTO(runRequest.getTestId(), runRequest.getReportId(), runMode);
    }

    public HashTree generateHashTree(RunCaseRequest request, ApiTestCaseWithBLOBs testCaseWithBLOBs) throws Exception {
        PerformInspectionUtil.countMatches(testCaseWithBLOBs.getRequest(), testCaseWithBLOBs.getId());

        JSONObject elementObj = JSON.parseObject(testCaseWithBLOBs.getRequest(), Feature.DisableSpecialKeyDetect);
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
        parameterConfig.setApi(true);
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
