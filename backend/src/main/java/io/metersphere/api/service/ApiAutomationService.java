package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.cache.TestPlanReportExecuteCatch;
import io.metersphere.api.dto.*;
import io.metersphere.api.dto.automation.*;
import io.metersphere.api.dto.automation.parse.ScenarioImport;
import io.metersphere.api.dto.automation.parse.ScenarioImportParserFactory;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.datacount.ApiMethodUrlDTO;
import io.metersphere.api.dto.definition.RunDefinitionRequest;
import io.metersphere.api.dto.definition.request.*;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.unknown.MsJmeterElement;
import io.metersphere.api.dto.definition.request.variable.ScenarioVariable;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.jmeter.MessageCache;
import io.metersphere.api.jmeter.ReportCounter;
import io.metersphere.api.jmeter.ResourcePoolCalculation;
import io.metersphere.api.parse.ApiImportParser;
import io.metersphere.api.service.task.NamedThreadFactory;
import io.metersphere.api.service.task.SerialScenarioExecTask;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.controller.request.ResetOrderRequest;
import io.metersphere.controller.request.ScheduleRequest;
import io.metersphere.dto.ApiReportCountDTO;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.job.sechedule.ApiScenarioTestJob;
import io.metersphere.job.sechedule.SwaggerUrlImportJob;
import io.metersphere.job.sechedule.TestPlanTestJob;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.api.AutomationReference;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.service.EnvironmentGroupProjectService;
import io.metersphere.service.QuotaService;
import io.metersphere.service.RelationshipEdgeService;
import io.metersphere.service.ScheduleService;
import io.metersphere.service.SystemParameterService;
import io.metersphere.track.dto.TestPlanDTO;
import io.metersphere.track.request.testcase.ApiCaseRelevanceRequest;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.track.request.testplan.FileOperationRequest;
import io.metersphere.track.service.TestPlanScenarioCaseService;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.comparators.FixedOrderComparator;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiAutomationService {
    @Resource
    ApiScenarioModuleMapper apiScenarioModuleMapper;
    @Resource
    private ExtScheduleMapper extScheduleMapper;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private APITestService apiTestService;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    private TestCaseReviewScenarioMapper testCaseReviewScenarioMapper;
    @Resource
    private JMeterService jMeterService;
    @Resource
    private ApiTestEnvironmentService environmentService;
    @Resource
    private ScheduleService scheduleService;
    @Resource
    private ApiScenarioReportService apiReportService;
    @Resource
    private ExtTestPlanMapper extTestPlanMapper;
    @Resource
    SqlSessionFactory sqlSessionFactory;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    @Lazy
    private TestPlanScenarioCaseService testPlanScenarioCaseService;
    @Resource
    private EsbApiParamService esbApiParamService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private ApiTestEnvironmentMapper apiTestEnvironmentMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private TcpApiParamService tcpApiParamService;
    @Resource
    private ApiScenarioReferenceIdService apiScenarioReferenceIdService;
    @Resource
    private ResourcePoolCalculation resourcePoolCalculation;
    @Resource
    private RemakeReportService remakeReportService;
    @Resource
    private ExtTestPlanScenarioCaseMapper extTestPlanScenarioCaseMapper;
    @Resource
    private RelationshipEdgeService relationshipEdgeService;
    @Resource
    private ApiScenarioFollowMapper apiScenarioFollowMapper;
    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;
    @Resource
    private EnvironmentGroupProjectService environmentGroupProjectService;

    private ThreadLocal<Long> currentScenarioOrder = new ThreadLocal<>();

    public ApiScenarioDTO getDto(String id) {
        ApiScenarioRequest request = new ApiScenarioRequest();
        request.setId(id);
        List<ApiScenarioDTO> list = extApiScenarioMapper.list(request);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        }
        return null;
    }

    public ApiTestEnvironment getEnvironment(String id) {
        return apiTestEnvironmentMapper.selectByPrimaryKey(id);
    }

    public String getUser(String id) {
        User user = userMapper.selectByPrimaryKey(id);
        if (user != null) {
            return user.getName();
        }
        return null;
    }

    public List<ApiScenarioDTO> list(ApiScenarioRequest request) {
        request = this.initRequest(request, true, true);
        List<ApiScenarioDTO> list = extApiScenarioMapper.list(request);
        if (BooleanUtils.isTrue(request.isSelectEnvironment())) {
            this.setApiScenarioEnv(list);
        }
        return list;
    }

    private void setApiScenarioEnv(List<ApiScenarioDTO> list) {
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

    public List<ApiScenarioWithBLOBs> listAll(ApiScenarioBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery(query));
        List<ApiScenarioWithBLOBs> list = extApiScenarioMapper.selectIds(request.getIds());
        return list;
    }

    public int listAllTrash(ApiScenarioBatchRequest request) {
        return extApiScenarioMapper.selectTrash(request.getProjectId());
    }

    public List<String> idAll(ApiScenarioBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery(query));
        return request.getIds();
    }

    public List<ApiScenarioDTO> listReview(ApiScenarioRequest request) {
        request = this.initRequest(request, true, true);
        List<ApiScenarioDTO> list = extApiScenarioMapper.listReview(request);
        return list;
    }

    /**
     * 初始化部分参数
     *
     * @param request
     * @param setDefultOrders
     * @param checkThisWeekData
     * @return
     */
    private ApiScenarioRequest initRequest(ApiScenarioRequest request, boolean setDefultOrders, boolean checkThisWeekData) {
        if (setDefultOrders) {
            request.setOrders(ServiceUtils.getDefaultSortOrder(request.getOrders()));
        }
        if (StringUtils.isNotEmpty(request.getExecuteStatus())) {
            Map<String, List<String>> statusFilter = new HashMap<>();
            List<String> list = new ArrayList<>();
            list.add("Prepare");
            list.add("Underway");
            list.add("Completed");
            statusFilter.put("status", list);
            request.setFilters(statusFilter);
        }
        if (checkThisWeekData) {
            if (request.isSelectThisWeedData()) {
                Map<String, Date> weekFirstTimeAndLastTime = DateUtils.getWeedFirstTimeAndLastTime(new Date());
                Date weekFirstTime = weekFirstTimeAndLastTime.get("firstTime");
                if (weekFirstTime != null) {
                    request.setCreateTime(weekFirstTime.getTime());
                }
            }
        }
        return request;
    }

    public void removeToGcByIds(List<String> nodeIds) {
        ApiScenarioExampleWithOperation example = new ApiScenarioExampleWithOperation();
        example.createCriteria().andApiScenarioModuleIdIn(nodeIds);
        example.setOperator(SessionUtils.getUserId());
        example.setOperationTime(System.currentTimeMillis());
        extApiScenarioMapper.removeToGcByExample(example);
    }

    public ApiScenario create(SaveApiScenarioRequest request, List<MultipartFile> bodyFiles, List<MultipartFile> scenarioFiles) {
        checkQuota();
        request.setId(UUID.randomUUID().toString());
        checkNameExist(request);
        int nextNum = getNextNum(request.getProjectId());
        if (StringUtils.isBlank(request.getCustomNum())) {
            request.setCustomNum(String.valueOf(nextNum));
        }
        checkScenarioNum(request);
        final ApiScenarioWithBLOBs scenario = buildSaveScenario(request);
        scenario.setVersion(0);

        scenario.setCreateTime(System.currentTimeMillis());
        scenario.setNum(nextNum);
        List<ApiMethodUrlDTO> useUrl = this.parseUrl(scenario);
        scenario.setUseUrl(JSONArray.toJSONString(useUrl));
        scenario.setOrder(ServiceUtils.getNextOrder(scenario.getProjectId(), extApiScenarioMapper::getLastOrder));

        //检查场景的请求步骤。如果含有ESB请求步骤的话，要做参数计算处理。
        esbApiParamService.checkScenarioRequests(request);

        apiScenarioMapper.insert(scenario);
        apiScenarioReferenceIdService.saveByApiScenario(scenario);
        // 存储依赖关系
        ApiAutomationRelationshipEdgeService relationshipEdgeService = CommonBeanFactory.getBean(ApiAutomationRelationshipEdgeService.class);
        if (relationshipEdgeService != null) {
            relationshipEdgeService.initRelationshipEdge(null, scenario);
        }
        uploadFiles(request, bodyFiles, scenarioFiles);

        return scenario;
    }

    private void checkQuota() {
        QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
        if (quotaService != null) {
            quotaService.checkAPIAutomationQuota();
        }
    }

    private void uploadFiles(SaveApiScenarioRequest request, List<MultipartFile> bodyFiles, List<MultipartFile> scenarioFiles) {
        FileUtils.createBodyFiles(request.getScenarioFileIds(), scenarioFiles);
        List<String> bodyFileRequestIds = request.getBodyFileRequestIds();
        if (CollectionUtils.isNotEmpty(bodyFileRequestIds)) {
            bodyFileRequestIds.forEach(requestId -> {
                FileUtils.createBodyFiles(requestId, bodyFiles);
            });
        }
    }

    private void uploadBodyFiles(List<String> bodyFileRequestIds, List<MultipartFile> bodyFiles) {
        if (CollectionUtils.isNotEmpty(bodyFileRequestIds)) {
            bodyFileRequestIds.forEach(requestId -> {
                FileUtils.createBodyFiles(requestId, bodyFiles);
            });
        }
    }

    private void checkScenarioNum(SaveApiScenarioRequest request) {
        String projectId = request.getProjectId();
        Project project = projectMapper.selectByPrimaryKey(projectId);

        if (project == null) {
            MSException.throwException("add scenario fail, project is not find.");
        }

        Boolean openCustomNum = project.getScenarioCustomNum();
        if (BooleanUtils.isTrue(openCustomNum)) {
            checkCustomNumExist(request);
        }
    }

    private void checkCustomNumExist(SaveApiScenarioRequest request) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria()
                .andCustomNumEqualTo(request.getCustomNum())
                .andProjectIdEqualTo(request.getProjectId())
                .andIdNotEqualTo(request.getId());
        List<ApiScenario> list = apiScenarioMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(list)) {
            MSException.throwException("自定义ID " + request.getCustomNum() + " 已存在！");
        }
    }

    private boolean isCustomNumExist(ApiScenarioWithBLOBs blobs) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria()
                .andCustomNumEqualTo(blobs.getCustomNum())
                .andProjectIdEqualTo(blobs.getProjectId())
                .andIdNotEqualTo(blobs.getId());
        List<ApiScenario> list = apiScenarioMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(list)) {
            return true;
        } else {
            return false;
        }
    }

    private int getNextNum(String projectId) {
        ApiScenario apiScenario = extApiScenarioMapper.getNextNum(projectId);
        if (apiScenario == null || apiScenario.getNum() == null) {
            return 100001;
        } else {
            return Optional.of(apiScenario.getNum() + 1).orElse(100001);
        }
    }

    public ApiScenario update(SaveApiScenarioRequest request, List<MultipartFile> bodyFiles, List<MultipartFile> scenarioFiles) {
        checkQuota();
        checkNameExist(request);
        checkScenarioNum(request);

        //检查场景的请求步骤。如果含有ESB请求步骤的话，要做参数计算处理。
        esbApiParamService.checkScenarioRequests(request);
        //如果场景有TCP步骤的话，也要做参数计算处理
        tcpApiParamService.checkTestElement(request.getScenarioDefinition());

        final ApiScenarioWithBLOBs scenario = buildSaveScenario(request);

        ApiScenarioWithBLOBs beforeScenario = apiScenarioMapper.selectByPrimaryKey(request.getId());
        Integer version = beforeScenario.getVersion();
        if (version == null) {
            scenario.setVersion(0);
        } else {
            scenario.setVersion(version + 1);
        }

        deleteUpdateBodyFile(scenario, beforeScenario);
        List<ApiMethodUrlDTO> useUrl = this.parseUrl(scenario);
        scenario.setUseUrl(JSONArray.toJSONString(useUrl));
        apiScenarioMapper.updateByPrimaryKeySelective(scenario);
        apiScenarioReferenceIdService.saveByApiScenario(scenario);
        extScheduleMapper.updateNameByResourceID(request.getId(), request.getName());//  修改场景name，同步到修改首页定时任务
        uploadFiles(request, bodyFiles, scenarioFiles);

        // 存储依赖关系
        ApiAutomationRelationshipEdgeService relationshipEdgeService = CommonBeanFactory.getBean(ApiAutomationRelationshipEdgeService.class);
        if (relationshipEdgeService != null) {
            relationshipEdgeService.initRelationshipEdge(beforeScenario, scenario);
        }
        return scenario;
    }

    /**
     * 更新时如果有删除自定义请求，则删除对应body文件
     *
     * @param scenario
     */
    public void deleteUpdateBodyFile(ApiScenarioWithBLOBs scenario, ApiScenarioWithBLOBs oldScenario) {
        Set<String> newRequestIds = getRequestIds(scenario.getScenarioDefinition());
        MsTestElement msTestElement = parseScenarioDefinition(oldScenario.getScenarioDefinition());
        List<MsHTTPSamplerProxy> oldRequests = MsHTTPSamplerProxy.findHttpSampleFromHashTree(msTestElement);
        oldRequests.forEach(item -> {
            if (item.isCustomizeReq() && !newRequestIds.contains(item.getId())) {
                FileUtils.deleteBodyFiles(item.getId());
            }
        });
    }

    public MsScenario parseScenarioDefinition(String scenarioDefinition) {
        MsScenario scenario = JSONObject.parseObject(scenarioDefinition, MsScenario.class);
        parse(scenarioDefinition, scenario);
        return scenario;
    }

    public Set<String> getRequestIds(String scenarioDefinition) {
        MsScenario msScenario = parseScenarioDefinition(scenarioDefinition);
        List<MsHTTPSamplerProxy> httpSampleFromHashTree = MsHTTPSamplerProxy.findHttpSampleFromHashTree(msScenario);
        return httpSampleFromHashTree.stream()
                .map(MsHTTPSamplerProxy::getId).collect(Collectors.toSet());
    }

    public ApiScenarioWithBLOBs buildSaveScenario(SaveApiScenarioRequest request) {
        ApiScenarioWithBLOBs scenario = new ApiScenarioWithBLOBs();
        scenario.setId(request.getId());
        scenario.setName(request.getName());
        scenario.setProjectId(request.getProjectId());
        scenario.setCustomNum(request.getCustomNum());
        if (StringUtils.equals(request.getTags(), "[]")) {
            scenario.setTags("");
        } else {
            scenario.setTags(request.getTags());
        }
        scenario.setApiScenarioModuleId(request.getApiScenarioModuleId());
        scenario.setModulePath(request.getModulePath());
        scenario.setLevel(request.getLevel());
        scenario.setPrincipal(request.getPrincipal());
        scenario.setStepTotal(request.getStepTotal());
        scenario.setUpdateTime(System.currentTimeMillis());
        scenario.setDescription(request.getDescription());
        scenario.setCreateUser(SessionUtils.getUserId());

        scenario.setScenarioDefinition(JSON.toJSONString(request.getScenarioDefinition()));
        Boolean isValidEnum = EnumUtils.isValidEnum(EnvironmentType.class, request.getEnvironmentType());
        if (BooleanUtils.isTrue(isValidEnum)) {
            scenario.setEnvironmentType(request.getEnvironmentType());
        } else {
            scenario.setEnvironmentType(EnvironmentType.JSON.toString());
        }
        scenario.setEnvironmentJson(request.getEnvironmentJson());
        scenario.setEnvironmentGroupId(request.getEnvironmentGroupId());
        if (StringUtils.isNotEmpty(request.getStatus())) {
            scenario.setStatus(request.getStatus());
        } else {
            scenario.setStatus(ScenarioStatus.Underway.name());
        }
        if (StringUtils.isNotEmpty(request.getUserId())) {
            scenario.setUserId(request.getUserId());
        } else {
            scenario.setUserId(SessionUtils.getUserId());
        }

        if (StringUtils.isEmpty(request.getApiScenarioModuleId()) || "default-module".equals(request.getApiScenarioModuleId())) {
            ApiScenarioModuleExample example = new ApiScenarioModuleExample();
            example.createCriteria().andProjectIdEqualTo(request.getProjectId()).andNameEqualTo("未规划场景");
            List<ApiScenarioModule> modules = apiScenarioModuleMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(modules)) {
                scenario.setApiScenarioModuleId(modules.get(0).getId());
                scenario.setModulePath(modules.get(0).getName());
            }
        }
        saveFollows(scenario.getId(), request.getFollows());
        return scenario;
    }

    public void saveFollows(String scenarioId, List<String> follows) {
        ApiScenarioFollowExample example = new ApiScenarioFollowExample();
        example.createCriteria().andScenarioIdEqualTo(scenarioId);
        apiScenarioFollowMapper.deleteByExample(example);
        if (!org.springframework.util.CollectionUtils.isEmpty(follows)) {
            for (String follow : follows) {
                ApiScenarioFollow apiScenarioFollow = new ApiScenarioFollow();
                apiScenarioFollow.setScenarioId(scenarioId);
                apiScenarioFollow.setFollowId(follow);
                apiScenarioFollowMapper.insert(apiScenarioFollow);
            }
        }
    }

    public void delete(String id) {
        //及连删除外键表
        this.preDelete(id);
        testPlanScenarioCaseService.deleteByScenarioId(id);
        apiScenarioMapper.deleteByPrimaryKey(id);
    }

    private void deleteFollows(String id) {
        ApiScenarioFollowExample example = new ApiScenarioFollowExample();
        example.createCriteria().andScenarioIdEqualTo(id);
        apiScenarioFollowMapper.deleteByExample(example);
    }

    public void preDelete(String scenarioId) {
        //删除引用
        apiScenarioReferenceIdService.deleteByScenarioId(scenarioId);

        List<String> ids = new ArrayList<>();
        ids.add(scenarioId);
        deleteApiScenarioReport(ids);

        scheduleService.deleteByResourceId(scenarioId, ScheduleGroup.API_SCENARIO_TEST.name());
        TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
        example.createCriteria().andApiScenarioIdEqualTo(scenarioId);
        List<TestPlanApiScenario> testPlanApiScenarioList = testPlanApiScenarioMapper.selectByExample(example);

        List<String> idList = new ArrayList<>(testPlanApiScenarioList.size());
        for (TestPlanApiScenario api :
                testPlanApiScenarioList) {
            idList.add(api.getId());
        }
        example = new TestPlanApiScenarioExample();

        if (!idList.isEmpty()) {
            example.createCriteria().andIdIn(idList);
            testPlanApiScenarioMapper.deleteByExample(example);
        }
        // 删除引用关系
        relationshipEdgeService.delete(scenarioId);
        deleteBodyFileByScenarioId(scenarioId);
        deleteFollows(scenarioId);
    }

    public void deleteBodyFileByScenarioId(String scenarioId) {
        ApiScenarioWithBLOBs apiScenarioWithBLOBs = apiScenarioMapper.selectByPrimaryKey(scenarioId);
        String scenarioDefinition = apiScenarioWithBLOBs.getScenarioDefinition();
        deleteBodyFile(scenarioDefinition);
    }

    public void deleteBodyFileByScenarioIds(List<String> ids) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(ids);
        List<ApiScenarioWithBLOBs> apiScenarios = apiScenarioMapper.selectByExampleWithBLOBs(example);
        apiScenarios.forEach((item) -> {
            deleteBodyFile(item.getScenarioDefinition());
        });
    }

    public void deleteBodyFile(String scenarioDefinition) {
        MsTestElement msTestElement = parseScenarioDefinition(scenarioDefinition);
        List<MsHTTPSamplerProxy> httpSampleFromHashTree = MsHTTPSamplerProxy.findHttpSampleFromHashTree(msTestElement);
        httpSampleFromHashTree.forEach((httpSamplerProxy) -> {
            if (httpSamplerProxy.isCustomizeReq()) {
                FileUtils.deleteBodyFiles(httpSamplerProxy.getId());
            }
        });
    }

    private void deleteApiScenarioReport(List<String> scenarioIds) {
        if (scenarioIds == null || scenarioIds.isEmpty()) {
            return;
        }
        ApiScenarioReportExample scenarioReportExample = new ApiScenarioReportExample();
        scenarioReportExample.createCriteria().andScenarioIdIn(scenarioIds);
        List<ApiScenarioReport> list = apiScenarioReportMapper.selectByExample(scenarioReportExample);
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> ids = list.stream().map(ApiScenarioReport::getId).collect(Collectors.toList());
            APIReportBatchRequest reportRequest = new APIReportBatchRequest();
            reportRequest.setIds(ids);
            apiReportService.deleteAPIReportBatch(reportRequest);
        }
    }

    public void preDeleteBatch(List<String> scenarioIds) {
        deleteApiScenarioReport(scenarioIds);

        List<String> testPlanApiScenarioIdList = new ArrayList<>();
        for (String id : scenarioIds) {
            TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
            example.createCriteria().andApiScenarioIdEqualTo(id);
            List<TestPlanApiScenario> testPlanApiScenarioList = testPlanApiScenarioMapper.selectByExample(example);
            for (TestPlanApiScenario api : testPlanApiScenarioList) {
                if (!testPlanApiScenarioIdList.contains(api.getId())) {
                    testPlanApiScenarioIdList.add(api.getId());
                }
            }

            scheduleService.deleteByResourceId(id, ScheduleGroup.API_SCENARIO_TEST.name());
            deleteFollows(id);
        }
        if (!testPlanApiScenarioIdList.isEmpty()) {
            TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
            example.createCriteria().andIdIn(testPlanApiScenarioIdList);
            testPlanApiScenarioMapper.deleteByExample(example);
        }
        // 删除引用关系
        relationshipEdgeService.delete(scenarioIds);
        deleteBodyFileByScenarioIds(scenarioIds);
    }

    public void deleteBatch(List<String> ids) {
        // 删除外键表
        preDeleteBatch(ids);
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(ids);
        apiScenarioMapper.deleteByExample(example);
    }

    public void removeToGc(List<String> apiIds) {
        ApiScenarioExampleWithOperation example = new ApiScenarioExampleWithOperation();
        example.createCriteria().andIdIn(apiIds);
        example.setOperator(SessionUtils.getUserId());
        example.setOperationTime(System.currentTimeMillis());
        extApiScenarioMapper.removeToGcByExample(example);
        //将这些场景的定时任务删除掉
        for (String id : apiIds) {
            scheduleService.deleteByResourceId(id, ScheduleGroup.API_SCENARIO_TEST.name());
        }
    }

    public void reduction(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            extApiScenarioMapper.checkOriginalStatusByIds(ids);
            //检查原来模块是否还在
            ApiScenarioExample example = new ApiScenarioExample();
            example.createCriteria().andIdIn(ids);
            List<ApiScenario> scenarioList = apiScenarioMapper.selectByExample(example);
            Map<String, List<ApiScenario>> nodeMap = new HashMap<>();
            for (ApiScenario api : scenarioList) {
                String moduleId = api.getApiScenarioModuleId();
                if (StringUtils.isEmpty(moduleId)) {
                    moduleId = "";
                }
                if (nodeMap.containsKey(moduleId)) {
                    nodeMap.get(moduleId).add(api);
                } else {
                    List<ApiScenario> list = new ArrayList<>();
                    list.add(api);
                    nodeMap.put(moduleId, list);
                }
            }
            ApiScenarioModuleService apiScenarioModuleService = CommonBeanFactory.getBean(ApiScenarioModuleService.class);
            for (Map.Entry<String, List<ApiScenario>> entry : nodeMap.entrySet()) {
                String nodeId = entry.getKey();
                List<ApiScenario> scenariosListItem = entry.getValue();
                Map<String, List<ApiScenario>> projectMap = scenariosListItem.stream().collect(Collectors.groupingBy(ApiScenario::getProjectId));
                for (Map.Entry<String, List<ApiScenario>> projectEntry : projectMap.entrySet()) {
                    String projectId = projectEntry.getKey();
                    List<ApiScenario> checkList = projectEntry.getValue();
                    if (StringUtils.isNotEmpty(projectId)) {
                        long nodeCount = apiScenarioModuleService.countById(nodeId);
                        if (nodeCount <= 0) {
                            ApiScenarioModule node = apiScenarioModuleService.getDefaultNode(projectId);
                            for (ApiScenario testCase : checkList) {
                                ApiScenarioWithBLOBs updateCase = new ApiScenarioWithBLOBs();
                                updateCase.setId(testCase.getId());
                                updateCase.setApiScenarioModuleId(node.getId());
                                updateCase.setModulePath("/" + node.getName());
                                apiScenarioMapper.updateByPrimaryKeySelective(updateCase);
                            }
                        }
                    }
                }
            }
            extApiScenarioMapper.reduction(ids);
        }
    }

    private void checkNameExist(SaveApiScenarioRequest request) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId()).andStatusNotEqualTo("Trash").andIdNotEqualTo(request.getId());
        if (apiScenarioMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("automation_name_already_exists"));
        }
    }

    public ApiScenarioWithBLOBs getApiScenario(String id) {
        return apiScenarioMapper.selectByPrimaryKey(id);
    }

    public String setDomain(ApiScenarioEnvRequest request) {
        Boolean enable = request.getEnvironmentEnable();
        String scenarioDefinition = request.getDefinition();
        JSONObject element = JSON.parseObject(scenarioDefinition);
        Map<String, String> environmentMap = new HashMap<>();
        if (BooleanUtils.isFalse(enable)) {
            String envType = request.getEnvironmentType();
            String envGroupId = request.getEnvironmentGroupId();
            if (StringUtils.equals(envType, EnvironmentType.GROUP.name())) {
                environmentMap = environmentGroupProjectService.getEnvMap(envGroupId);
            } else if (StringUtils.equals(envType, EnvironmentType.JSON.name())) {
                environmentMap = request.getEnvironmentMap();
            }
        } else {
            String scenarioId = request.getId();
            ApiScenarioWithBLOBs apiScenarioWithBLOBs = apiScenarioMapper.selectByPrimaryKey(scenarioId);
            String environmentType = apiScenarioWithBLOBs.getEnvironmentType();
            String environmentGroupId = apiScenarioWithBLOBs.getEnvironmentGroupId();
            String environmentJson = apiScenarioWithBLOBs.getEnvironmentJson();
            if (StringUtils.equals(environmentType, EnvironmentType.GROUP.name())) {
                environmentMap = environmentGroupProjectService.getEnvMap(environmentGroupId);
            } else if (StringUtils.equals(environmentType, EnvironmentType.JSON.name())) {
                environmentMap = JSON.parseObject(environmentJson, Map.class);
            }
        }


        ParameterConfig config = new ParameterConfig();
        Map<String, EnvironmentConfig> envConfig = new HashMap<>(16);
        if (environmentMap != null && !environmentMap.isEmpty()) {
            Map<String, String> finalEnvironmentMap = environmentMap;
            environmentMap.keySet().forEach(projectId -> {
                ApiTestEnvironmentService environmentService = CommonBeanFactory.getBean(ApiTestEnvironmentService.class);
                ApiTestEnvironmentWithBLOBs environment = environmentService.get(finalEnvironmentMap.get(projectId));
                if (environment != null && environment.getConfig() != null) {
                    EnvironmentConfig env = JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class);
                    env.setApiEnvironmentid(environment.getId());
                    envConfig.put(projectId, env);
                }
            });
            config.setConfig(envConfig);
        }
        if (config.getConfig() != null && !config.getConfig().isEmpty()) {
            ElementUtil.dataSetDomain(element.getJSONArray("hashTree"), config);
        }
        return JSON.toJSONString(element);
    }


    public LinkedList<MsTestElement> getScenarioHashTree(String definition) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JSONObject element = JSON.parseObject(definition);
        try {
            if (element != null) {
                ElementUtil.dataFormatting(element);
                return objectMapper.readValue(element.getString("hashTree"), new TypeReference<LinkedList<MsTestElement>>() {
                });
            }
        } catch (JsonProcessingException e) {
            LogUtil.error(e.getMessage(), e);
        }
        return new LinkedList<>();
    }

    public ScenarioEnv getApiScenarioEnv(String definition) {
        ScenarioEnv env = new ScenarioEnv();
        List<MsTestElement> hashTree = getScenarioHashTree(definition);
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
                        }
                    } else {
                        ApiDefinition apiDefinition = apiDefinitionService.get(tr.getId());
                        if (apiDefinition != null) {
                            env.getProjectIds().add(apiDefinition.getProjectId());
                        }
                    }
                } else if (StringUtils.equals(tr.getType(), "scenario")) {
                    if (tr.isEnable()) {
                        ApiScenarioWithBLOBs apiScenario = getApiScenario(tr.getId());
                        if (apiScenario != null) {
                            env.getProjectIds().add(apiScenario.getProjectId());
                            String scenarioDefinition = apiScenario.getScenarioDefinition();
                            tr.setHashTree(getScenarioHashTree(scenarioDefinition));
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
                        } else {
                            ApiDefinition apiDefinition = apiDefinitionService.get(tr.getId());
                            env.getProjectIds().add(apiDefinition.getProjectId());
                        }
                    } else if (StringUtils.equals(tr.getType(), "scenario")) {
                        if (tr.isEnable()) {
                            ApiScenarioWithBLOBs apiScenario = getApiScenario(tr.getId());
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
                            if (StringUtils.isBlank(httpSamplerProxy.getUrl()) || (httpSamplerProxy.getIsRefEnvironment() != null && httpSamplerProxy.getIsRefEnvironment())) {
                                env.setFullUrl(false);
                                env.getProjectIds().add(httpSamplerProxy.getProjectId());
                            }
                        }
                    } else if (StringUtils.equals(tr.getType(), "JDBCSampler") || StringUtils.equals(tr.getType(), "TCPSampler")) {
                        env.getProjectIds().add(tr.getProjectId());
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


    public List<ApiScenarioWithBLOBs> getApiScenarios(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            return extApiScenarioMapper.selectIds(ids);
        }
        return new ArrayList<>();
    }

    public byte[] loadFileAsBytes(FileOperationRequest fileOperationRequest) {
        File file = new File(FileUtils.BODY_FILE_DIR + "/" + fileOperationRequest.getId() + "_" + fileOperationRequest.getName());
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);) {
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            return bos.toByteArray();
        } catch (Exception ex) {
            LogUtil.error(ex);
        }
        return null;
    }

    public APIScenarioReportResult createScenarioReport(String id, String scenarioId, String scenarioName, String triggerMode, String execType, String projectId, String userID, RunModeConfig config, String desc) {
        APIScenarioReportResult report = new APIScenarioReportResult();
        if (triggerMode.equals(ApiRunMode.SCENARIO.name()) || triggerMode.equals(ApiRunMode.DEFINITION.name())) {
            triggerMode = ReportTriggerMode.MANUAL.name();
        }
        report.setId(id);
        report.setTestId(id);
        if (StringUtils.isNotEmpty(scenarioName)) {
            report.setName(scenarioName);
        } else {
            report.setName("场景调试");
        }
        report.setUpdateTime(System.currentTimeMillis());
        report.setCreateTime(System.currentTimeMillis());

        report.setStatus(APITestStatus.Running.name());
        if (StringUtils.isNotEmpty(userID)) {
            report.setUserId(userID);
            report.setCreateUser(userID);
        } else {
            report.setUserId(SessionUtils.getUserId());
            report.setCreateUser(SessionUtils.getUserId());
        }
        if (config != null && StringUtils.isNotBlank(config.getResourcePoolId())) {
            report.setActuator(config.getResourcePoolId());
        } else {
            report.setActuator("LOCAL");
        }
        report.setTriggerMode(triggerMode);
        report.setExecuteType(execType);
        report.setProjectId(projectId);
        report.setScenarioName(scenarioName);
        report.setScenarioId(scenarioId);
        report.setDescription(desc);
        return report;
    }


    private void parse(String scenarioDefinition, MsScenario scenario) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            JSONObject element = JSON.parseObject(scenarioDefinition);
            ElementUtil.dataFormatting(element);

            // 多态JSON普通转换会丢失内容，需要通过 ObjectMapper 获取
            if (element != null && StringUtils.isNotEmpty(element.getString("hashTree"))) {
                LinkedList<MsTestElement> elements = mapper.readValue(element.getString("hashTree"),
                        new TypeReference<LinkedList<MsTestElement>>() {
                        });
                scenario.setHashTree(elements);
            }
            if (element != null && StringUtils.isNotEmpty(element.getString("variables"))) {
                LinkedList<ScenarioVariable> variables = mapper.readValue(element.getString("variables"),
                        new TypeReference<LinkedList<ScenarioVariable>>() {
                        });
                scenario.setVariables(variables);
            }
        } catch (Exception e) {
            LogUtil.error(e);
            LogUtil.error(e);
        }
    }

    public HashTree generateHashTree(ApiScenarioWithBLOBs item, String reportId, Map<String, String> planEnvMap) {
        HashTree jmeterHashTree = new HashTree();
        MsTestPlan testPlan = new MsTestPlan();
        testPlan.setHashTree(new LinkedList<>());
        try {
            MsThreadGroup group = new MsThreadGroup();
            group.setLabel(item.getName());
            group.setName(reportId);
            MsScenario scenario = JSONObject.parseObject(item.getScenarioDefinition(), MsScenario.class);
            group.setOnSampleError(scenario.getOnSampleError());
            if (planEnvMap.size() > 0) {
                scenario.setEnvironmentMap(planEnvMap);
            }
            parse(item.getScenarioDefinition(), scenario);

            group.setEnableCookieShare(scenario.isEnableCookieShare());
            LinkedList<MsTestElement> scenarios = new LinkedList<>();
            scenarios.add(scenario);

            group.setHashTree(scenarios);
            testPlan.getHashTree().add(group);
        } catch (Exception ex) {
            MSException.throwException(ex.getMessage());
        }

        testPlan.toHashTree(jmeterHashTree, testPlan.getHashTree(), new ParameterConfig());
        return jmeterHashTree;
    }

    private String generateJmx(ApiScenarioWithBLOBs apiScenario) {
        HashTree jmeterHashTree = new ListedHashTree();
        MsTestPlan testPlan = new MsTestPlan();
        testPlan.setName(apiScenario.getName());
        testPlan.setHashTree(new LinkedList<>());
        ParameterConfig config = new ParameterConfig();
        config.setOperating(true);
        try {

            MsScenario scenario = JSONObject.parseObject(apiScenario.getScenarioDefinition(), MsScenario.class);
            if (scenario == null) {
                return null;
            }
            parse(apiScenario.getScenarioDefinition(), scenario);
            String environmentType = apiScenario.getEnvironmentType();
            String environmentJson = apiScenario.getEnvironmentJson();
            String environmentGroupId = apiScenario.getEnvironmentGroupId();
            if (StringUtils.equals(environmentType, EnvironmentType.JSON.name()) && StringUtils.isNotBlank(environmentJson)) {
                scenario.setEnvironmentMap(JSON.parseObject(environmentJson, Map.class));
            } else if (StringUtils.equals(environmentType, EnvironmentType.GROUP.name()) && StringUtils.isNotBlank(environmentGroupId)) {
                Map<String, String> envMap = environmentGroupProjectService.getEnvMap(environmentGroupId);
                scenario.setEnvironmentMap(envMap);
            }
            // 针对导入的jmx 处理
            if (CollectionUtils.isNotEmpty(scenario.getHashTree()) && (scenario.getHashTree().get(0) instanceof MsJmeterElement)) {
                scenario.toHashTree(jmeterHashTree, scenario.getHashTree(), config);
                return scenario.getJmx(jmeterHashTree);
            } else {
                MsThreadGroup group = new MsThreadGroup();
                group.setLabel(apiScenario.getName());
                group.setName(apiScenario.getName());
                group.setEnableCookieShare(scenario.isEnableCookieShare());
                group.setOnSampleError(scenario.getOnSampleError());
                group.setHashTree(new LinkedList<MsTestElement>() {{
                    this.add(scenario);
                }});
                testPlan.getHashTree().add(group);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            MSException.throwException(ex.getMessage());
        }
        testPlan.toHashTree(jmeterHashTree, testPlan.getHashTree(), config);
        return testPlan.getJmx(jmeterHashTree);
    }

    private void checkPlanScenarioEnv(RunScenarioRequest request) {
        StringBuilder builder = new StringBuilder();
        List<String> planCaseIds = request.getPlanCaseIds();
        if (CollectionUtils.isNotEmpty(planCaseIds)) {
            TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
            example.createCriteria().andIdIn(planCaseIds);
            List<TestPlanApiScenario> testPlanApiScenarios = testPlanApiScenarioMapper.selectByExampleWithBLOBs(example);
            for (TestPlanApiScenario testPlanApiScenario : testPlanApiScenarios) {
                try {
                    ApiScenarioWithBLOBs apiScenarioWithBLOBs = apiScenarioMapper.selectByPrimaryKey(testPlanApiScenario.getApiScenarioId());
                    boolean haveEnv = checkScenarioEnv(apiScenarioWithBLOBs, testPlanApiScenario);
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

    private void checkEnv(RunScenarioRequest request, List<ApiScenarioWithBLOBs> apiScenarios) {
        if (StringUtils.equals(request.getRequestOriginator(), "TEST_PLAN")) {
            this.checkPlanScenarioEnv(request);
        } else if (StringUtils.isNotBlank(request.getRunMode()) &&
                StringUtils.equalsAny(request.getRunMode(), ApiRunMode.SCENARIO.name(), ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
            StringBuilder builder = new StringBuilder();
            for (ApiScenarioWithBLOBs apiScenarioWithBLOBs : apiScenarios) {
                try {
                    this.setScenarioEnv(apiScenarioWithBLOBs);
                    boolean haveEnv = checkScenarioEnv(apiScenarioWithBLOBs, null);
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
                    this.setScenarioEnv(apiScenarioWithBLOBs);
                } catch (Exception e) {
                    MSException.throwException("定时任务设置场景环境失败，场景ID： " + apiScenarioWithBLOBs.getId());
                }
            }
        }
    }

    /**
     * 设置场景的运行环境 环境组/环境JSON
     *
     * @param apiScenarioWithBLOBs
     */
    private void setScenarioEnv(ApiScenarioWithBLOBs apiScenarioWithBLOBs) {
        String environmentType = apiScenarioWithBLOBs.getEnvironmentType();
        String environmentJson = apiScenarioWithBLOBs.getEnvironmentJson();
        String environmentGroupId = apiScenarioWithBLOBs.getEnvironmentGroupId();
        if (StringUtils.isBlank(environmentType)) {
            environmentType = EnvironmentType.JSON.toString();
        }
        String definition = apiScenarioWithBLOBs.getScenarioDefinition();
        MsScenario scenario = JSONObject.parseObject(definition, MsScenario.class);
        this.parse(definition, scenario);
        if (StringUtils.equals(environmentType, EnvironmentType.JSON.toString())) {
            scenario.setEnvironmentMap(JSON.parseObject(environmentJson, Map.class));
        } else if (StringUtils.equals(environmentType, EnvironmentType.GROUP.toString())) {
            Map<String, String> map = environmentGroupProjectService.getEnvMap(environmentGroupId);
            scenario.setEnvironmentMap(map);
        }
        apiScenarioWithBLOBs.setScenarioDefinition(JSON.toJSONString(scenario));
    }

    /**
     * 场景测试执行
     *
     * @param request
     * @return
     */
    public String modeRun(RunScenarioRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery(query));

        List<String> ids = request.getIds();
        // 生成集成报告
        String serialReportId = null;

        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(ids);
        List<ApiScenarioWithBLOBs> apiScenarios = apiScenarioMapper.selectByExampleWithBLOBs(example);
        if (request.getConfig() != null && request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString())) {
            if (request.getCondition() == null || !request.getCondition().isSelectAll()) {
                // 按照id指定顺序排序
                FixedOrderComparator<String> fixedOrderComparator = new FixedOrderComparator<String>(ids);
                fixedOrderComparator.setUnknownObjectBehavior(FixedOrderComparator.UnknownObjectBehavior.BEFORE);
                BeanComparator beanComparator = new BeanComparator("id", fixedOrderComparator);
                Collections.sort(apiScenarios, beanComparator);
            }
        }

        // 只有一个场景且没有测试步骤，则提示
        if (apiScenarios != null && apiScenarios.size() == 1 && (apiScenarios.get(0).getStepTotal() == null || apiScenarios.get(0).getStepTotal() == 0)) {
            MSException.throwException((apiScenarios.get(0).getName() + "，" + Translator.get("automation_exec_info")));
        }
        // 资源池
        if (request.getConfig() != null && StringUtils.isNotEmpty(request.getConfig().getResourcePoolId())) {
            TestResourcePool pool = testResourcePoolMapper.selectByPrimaryKey(request.getConfig().getResourcePoolId());
            if (pool != null && pool.getApi() && pool.getType().equals(ResourcePoolTypeEnum.K8S.name())) {
                LogUtil.info("K8S 暂时不做校验 ");
            } else {
                List<JvmInfoDTO> testResources = resourcePoolCalculation.getPools(request.getConfig().getResourcePoolId());
                request.getConfig().setTestResources(testResources);
            }
        }
        // 环境检查
        this.checkEnv(request, apiScenarios);
        // 集合报告设置
        if (request.getConfig() != null && StringUtils.equals(request.getConfig().getReportType(), RunModeConstants.SET_REPORT.toString()) && StringUtils.isNotEmpty(request.getConfig().getReportName())) {
            if (request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString())) {
                request.setExecuteType(ExecuteType.Completed.name());
            } else {
                request.setExecuteType(ExecuteType.Marge.name());
            }
            serialReportId = UUID.randomUUID().toString();
        }
        if (StringUtils.isEmpty(request.getTriggerMode())) {
            request.setTriggerMode(ReportTriggerMode.MANUAL.name());
        }

        Map<String, RunModeDataDTO> executeQueue = new LinkedHashMap<>();
        List<String> scenarioIds = new ArrayList<>();
        StringBuilder scenarioNames = new StringBuilder();

        if (StringUtils.equalsAny(request.getRunMode(), ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
            //测试计划执行
            this.prepareExecutedPlanScenario(apiScenarios, request, executeQueue, scenarioIds, scenarioNames);
        } else {
            // 按照场景执行
            this.prepareExecutedScenario(apiScenarios, request, executeQueue, scenarioIds, scenarioNames, serialReportId);
        }

        if (request.getConfig() != null && StringUtils.equals(request.getConfig().getReportType(), RunModeConstants.SET_REPORT.toString()) && StringUtils.isNotEmpty(request.getConfig().getReportName())) {
            request.getConfig().setReportId(UUID.randomUUID().toString());
            APIScenarioReportResult report = createScenarioReport(request.getConfig().getReportId(),
                    JSON.toJSONString(CollectionUtils.isNotEmpty(scenarioIds) && scenarioIds.size() > 50 ? scenarioIds.subList(0, 50) : scenarioIds),
                    scenarioNames.length() >= 3000 ? scenarioNames.substring(0, 2000) : scenarioNames.deleteCharAt(scenarioNames.toString().length() - 1).toString(),
                    ReportTriggerMode.MANUAL.name(), ExecuteType.Saved.name(), request.getProjectId(), request.getReportUserID(), request.getConfig(), JSON.toJSONString(scenarioIds));

            report.setName(request.getConfig().getReportName());
            report.setId(serialReportId);
            apiScenarioReportMapper.insert(report);
            // 增加并行集合报告
            if (request.getConfig() != null && request.getConfig().getMode().equals(RunModeConstants.PARALLEL.toString())) {
                List<String> reportIds = executeQueue.entrySet().stream()
                        .map(reports -> reports.getKey()).collect(Collectors.toList());
                ReportCounter counter = new ReportCounter();
                counter.setCompletedIds(new LinkedList<>());
                if (CollectionUtils.isNotEmpty(request.getConfig().getTestResources())) {
                    counter.setPoolUrls(request.getConfig().getTestResources());
                }
                counter.setReportIds(reportIds);
                request.getConfig().setAmassReport(serialReportId);
                MessageCache.cache.put(serialReportId, counter);
            }
        }
        // 开始执行
        if (executeQueue != null && executeQueue.size() > 0) {
            if (request.getConfig() != null && request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString())) {
                this.serial(executeQueue, request, serialReportId);
            } else {
                this.parallel(executeQueue, request);
            }
        }
        return request.getId();
    }

    /**
     * 测试计划接口场景的预执行（生成场景报告）
     *
     * @param apiScenarios
     * @param request
     * @param executeQueue
     * @param scenarioIds
     * @param scenarioNames
     */
    private void prepareExecutedPlanScenario(List<ApiScenarioWithBLOBs> apiScenarios, RunScenarioRequest request, Map<String, RunModeDataDTO> executeQueue, List<String> scenarioIds, StringBuilder scenarioNames) {
        String reportId = request.getId();
        Map<String, String> planScenarioIdMap = request.getScenarioTestPlanIdMap();
        if (MapUtils.isEmpty(planScenarioIdMap)) {
            return;
        }
        String projectId = request.getProjectId();
        Map<String, ApiScenarioWithBLOBs> scenarioMap = apiScenarios.stream().collect(Collectors.toMap(ApiScenarioWithBLOBs::getId, Function.identity(), (t1, t2) -> t1));
        for (Map.Entry<String, String> entry : planScenarioIdMap.entrySet()) {
            String testPlanScenarioId = entry.getKey();
            String scenarioId = entry.getValue();
            ApiScenarioWithBLOBs scenario = scenarioMap.get(scenarioId);

            if (scenario.getStepTotal() == null || scenario.getStepTotal() == 0) {
                continue;
            }
            APIScenarioReportResult report;
            Map<String, String> planEnvMap = new HashMap<>();

            //测试计划页面触发的执行方式，生成报告时createScenarioReport第二个参数需要特殊处理
            // 获取场景用例单独的执行环境
            TestPlanApiScenario planApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(testPlanScenarioId);
            String envJson = planApiScenario.getEnvironment();
            String envType = planApiScenario.getEnvironmentType();
            String envGroupId = planApiScenario.getEnvironmentGroupId();
            if (StringUtils.equals(envType, EnvironmentType.JSON.toString()) && StringUtils.isNotBlank(envJson)) {
                planEnvMap = JSON.parseObject(envJson, Map.class);
            } else if (StringUtils.equals(envType, EnvironmentType.GROUP.toString()) && StringUtils.isNotBlank(envGroupId)) {
                planEnvMap = environmentGroupProjectService.getEnvMap(envGroupId);
            }
            if (StringUtils.isEmpty(projectId)) {
                projectId = testPlanScenarioCaseService.getProjectIdById(testPlanScenarioId);
            }
            if (StringUtils.isEmpty(projectId)) {
                projectId = scenario.getProjectId();
            }
            if (request.isTestPlanScheduleJob()) {
                String savedScenarioId = testPlanScenarioId + ":" + request.getTestPlanReportId();
                report = createScenarioReport(reportId, savedScenarioId, scenario.getName(), request.getTriggerMode(),
                        request.getExecuteType(), projectId, request.getReportUserID(), request.getConfig(), scenario.getId());
            } else {
                report = createScenarioReport(reportId, testPlanScenarioId, scenario.getName(), request.getTriggerMode(),
                        request.getExecuteType(), projectId, request.getReportUserID(), request.getConfig(), scenario.getId());
            }
            if (report != null && StringUtils.isNotEmpty(request.getTestPlanReportId())) {
                Map<String, String> scenarioReportIdMap = new HashMap<>();
                scenarioReportIdMap.put(testPlanScenarioId, report.getId());
                TestPlanReportExecuteCatch.updateTestPlanThreadInfo(request.getTestPlanReportId(), null, scenarioReportIdMap, null);
            }

            try {
                if (request.getConfig() != null && StringUtils.isNotBlank(request.getConfig().getResourcePoolId())) {
                    RunModeDataDTO runModeDataDTO = new RunModeDataDTO();
                    runModeDataDTO.setTestId(testPlanScenarioId);
                    runModeDataDTO.setPlanEnvMap(planEnvMap);
                    runModeDataDTO.setReport(report);
                    executeQueue.put(report.getId(), runModeDataDTO);
                } else {
                    // 生成报告和HashTree
                    try {
                        HashTree hashTree = generateHashTree(scenario, reportId, planEnvMap);
                        executeQueue.put(report.getId(), new RunModeDataDTO(hashTree, report));
                    } catch (Exception ex) {
                        if (StringUtils.equalsAny(request.getTriggerMode(), TriggerMode.BATCH.name(), TriggerMode.SCHEDULE.name())) {
                            remakeReportService.remakeScenario(request.getRunMode(), testPlanScenarioId, request.getConfig(), scenario, report);
                        } else {
                            MSException.throwException(ex);
                        }
                    }
                }
                scenarioIds.add(scenario.getId());
                scenarioNames.append(scenario.getName()).append(",");
                // 重置报告ID
                reportId = UUID.randomUUID().toString();
            } catch (Exception ex) {
                ex.printStackTrace();
                MSException.throwException("解析运行步骤失败！场景名称：" + scenario.getName());
            }
        }
    }

    /**
     * 接口场景的预执行（生成场景报告）
     *
     * @param apiScenarios
     * @param request
     * @param executeQueue
     * @param scenarioIds
     * @param scenarioNames
     * @param serialReportId
     */
    private void prepareExecutedScenario(List<ApiScenarioWithBLOBs> apiScenarios, RunScenarioRequest request, Map<String, RunModeDataDTO> executeQueue, List<String> scenarioIds, StringBuilder scenarioNames, String serialReportId) {
        String reportId = request.getId();
        for (ApiScenarioWithBLOBs item : apiScenarios) {
            if (item.getStepTotal() == null || item.getStepTotal() == 0) {
                continue;
            }
            APIScenarioReportResult report;
            Map<String, String> planEnvMap = new HashMap<>();
            report = createScenarioReport(reportId, ExecuteType.Marge.name().equals(request.getExecuteType()) ? serialReportId : item.getId(), item.getName(), request.getTriggerMode(),
                    request.getExecuteType(), item.getProjectId(), request.getReportUserID(), request.getConfig(), item.getId());

            try {
                if (request.getConfig() != null && StringUtils.isNotBlank(request.getConfig().getResourcePoolId())) {
                    RunModeDataDTO runModeDataDTO = new RunModeDataDTO();
                    runModeDataDTO.setTestId(item.getId());
                    runModeDataDTO.setPlanEnvMap(planEnvMap);
                    runModeDataDTO.setReport(report);
                    executeQueue.put(report.getId(), runModeDataDTO);
                } else {
                    // 生成报告和HashTree
                    try {
                        HashTree hashTree = generateHashTree(item, reportId, planEnvMap);
                        executeQueue.put(report.getId(), new RunModeDataDTO(hashTree, report));
                    } catch (Exception ex) {
                        if (StringUtils.equalsAny(request.getTriggerMode(), TriggerMode.BATCH.name(), TriggerMode.SCHEDULE.name())) {
                            String testPlanScenarioId;
                            if (request.getScenarioTestPlanIdMap() != null && request.getScenarioTestPlanIdMap().containsKey(item.getId())) {
                                testPlanScenarioId = request.getScenarioTestPlanIdMap().get(item.getId());
                            } else {
                                testPlanScenarioId = request.getPlanScenarioId();
                            }
                            remakeReportService.remakeScenario(request.getRunMode(), testPlanScenarioId, request.getConfig(), item, report);
                        } else {
                            MSException.throwException(ex);
                        }
                    }
                }
                scenarioIds.add(item.getId());
                scenarioNames.append(item.getName()).append(",");
                // 重置报告ID
                reportId = UUID.randomUUID().toString();
            } catch (Exception ex) {
                ex.printStackTrace();
                MSException.throwException("解析运行步骤失败！场景名称：" + item.getName());
            }
        }
    }

    /**
     * 串行
     *
     * @param executeQueue
     * @param request
     * @param serialReportId
     */
    private void serial(Map<String, RunModeDataDTO> executeQueue, RunScenarioRequest request, String serialReportId) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiScenarioReportMapper batchMapper = sqlSession.getMapper(ApiScenarioReportMapper.class);
        // 非集合报告，先生成执行队列
        if (StringUtils.isEmpty(serialReportId)) {
            for (String reportId : executeQueue.keySet()) {
                APIScenarioReportResult report = executeQueue.get(reportId).getReport();
                report.setStatus(APITestStatus.Waiting.name());
                batchMapper.insert(report);
            }
            sqlSession.flushStatements();
            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }

        // 开始串行执行
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("Scenario串行执行线程");
                ExecutorService executorService = Executors.newFixedThreadPool(1, new NamedThreadFactory("串行执行等待线程池"));
                try {
                    List<String> reportIds = new LinkedList<>();
                    //记录串行执行中的环境参数，供下一个场景执行时使用。 <envId,<key,data>>
                    Map<String, Map<String, String>> executeEnvParams = new LinkedHashMap<>();
                    ApiTestEnvironmentService apiTestEnvironmentService = CommonBeanFactory.getBean(ApiTestEnvironmentService.class);
                    HashTreeUtil hashTreeUtil = new HashTreeUtil();
                    for (String key : executeQueue.keySet()) {
                        // 终止执行
                        if (MessageCache.terminationOrderDeque.contains(key)) {
                            MessageCache.terminationOrderDeque.remove(key);
                            break;
                        }
                        MessageCache.executionQueue.put(key, System.currentTimeMillis());
                        reportIds.add(key);
                        APIScenarioReportResult report = executeQueue.get(key).getReport();
                        if (StringUtils.isNotEmpty(serialReportId)) {
                            report.setExecuteType(ExecuteType.Marge.name());
                            apiScenarioReportMapper.insert(report);
                        } else {
                            report.setStatus(APITestStatus.Running.name());
                            report.setCreateTime(System.currentTimeMillis());
                            report.setUpdateTime(System.currentTimeMillis());
                            apiScenarioReportMapper.updateByPrimaryKey(report);
                        }
                        try {
                            if (!executeEnvParams.isEmpty()) {
                                HashTree hashTree = executeQueue.get(key).getHashTree();
                                hashTreeUtil.setEnvParamsMapToHashTree(hashTree, executeEnvParams);
                                executeQueue.get(key).setHashTree(hashTree);
                            }
                            if (request.getConfig() != null && StringUtils.isNotEmpty(serialReportId)) {
                                request.getConfig().setAmassReport(serialReportId);
                            }
                            Future<ApiScenarioReport> future = executorService.submit(new SerialScenarioExecTask(jMeterService, apiScenarioReportMapper, executeQueue.get(key), request));
                            future.get();
                            // 如果开启失败结束执行，则判断返回结果状态
                            if (request.getConfig().isOnSampleError()) {
                                ApiScenarioReport scenarioReport = apiScenarioReportMapper.selectByPrimaryKey(key);
                                if (scenarioReport == null || !scenarioReport.getStatus().equals("Success")) {
                                    reportIds.remove(key);
                                    break;
                                }
                            }

                            Map<String, Map<String, String>> envParamsMap = hashTreeUtil.getEnvParamsDataByHashTree(executeQueue.get(key).getHashTree(), apiTestEnvironmentService);
                            executeEnvParams = hashTreeUtil.mergeParamDataMap(executeEnvParams, envParamsMap);
                        } catch (Exception e) {
                            reportIds.remove(key);
                            MessageCache.executionQueue.remove(key);
                            LogUtil.error("执行终止：" + e.getMessage());
                            break;
                        }
                    }
                    // 清理未执行的队列
                    if (reportIds.size() < executeQueue.size() && StringUtils.isNotEmpty(serialReportId)) {
                        List<String> removeList = executeQueue.entrySet().stream().filter(map -> !reportIds.contains(map.getKey()))
                                .map(map -> map.getKey()).collect(Collectors.toList());
                        ApiScenarioReportExample example = new ApiScenarioReportExample();
                        example.createCriteria().andIdIn(removeList);
                        apiScenarioReportMapper.deleteByExample(example);
                    }
                    // 更新集成报告
                    if (StringUtils.isNotEmpty(serialReportId)) {
                        apiScenarioReportService.margeReport(serialReportId, reportIds);
                        executeQueue.clear();
                    }

                } catch (Exception e) {
                    LogUtil.error(e);
                } finally {
                    executorService.shutdownNow();
                }
            }
        });
        thread.start();
    }

    /**
     * 并行
     *
     * @param executeQueue
     * @param request
     */
    private void parallel(Map<String, RunModeDataDTO> executeQueue, RunScenarioRequest request) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
                ApiScenarioReportMapper batchMapper = sqlSession.getMapper(ApiScenarioReportMapper.class);
                // 开始并发执行
                for (String reportId : executeQueue.keySet()) {
                    //存储报告
                    APIScenarioReportResult report = executeQueue.get(reportId).getReport();
                    batchMapper.insert(report);
                }
                sqlSession.flushStatements();
                if (sqlSession != null && sqlSessionFactory != null) {
                    SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
                }
            }
        });

        try {
            thread.start();
            while (!StringUtils.equals(thread.getState().name(), Thread.State.TERMINATED.name())){
                Thread.sleep(500);
            }
        }catch (Exception e){
            LogUtil.error("场景报告入库异常 报错：["+e.getMessage()+"]. 场景报告ID list："+JSONArray.toJSONString(executeQueue.keySet()));
            LogUtil.error(e);
        }

        for (String reportId : executeQueue.keySet()) {
            MessageCache.scenarioExecResourceLock.put(reportId, executeQueue.get(reportId).getReport());
            // 增加一个本地锁，防止并发找不到资源
            if (request.getConfig() != null && StringUtils.isNotEmpty(request.getConfig().getResourcePoolId())) {
                String testPlanScenarioId = "";
                String testId = executeQueue.get(reportId).getTestId();
                if (request.getScenarioTestPlanIdMap() != null && request.getScenarioTestPlanIdMap().containsKey(executeQueue.get(reportId).getTestId())) {
                    testPlanScenarioId = executeQueue.get(reportId).getTestId();
                    testId = request.getScenarioTestPlanIdMap().get(executeQueue.get(reportId).getTestId());
                } else {
                    testPlanScenarioId = request.getPlanScenarioId();
                }
                jMeterService.runTest(testId, reportId, request.getRunMode(), testPlanScenarioId, request.getConfig());
            } else {
                jMeterService.runLocal(reportId, request.getConfig(), executeQueue.get(reportId).getHashTree(),
                        TriggerMode.BATCH.name().equals(request.getTriggerMode()) ? TriggerMode.BATCH.name() : request.getReportId(), request.getRunMode());
            }
        }
    }

    /**
     * 生成HashTree
     *
     * @param apiScenarios 场景
     * @param request      请求参数
     * @param reportIds    报告ID
     * @return hashTree
     */
    private HashTree generateHashTree(List<ApiScenarioWithBLOBs> apiScenarios, RunScenarioRequest request, List<String> reportIds) {
        HashTree jmeterHashTree = new ListedHashTree();
        MsTestPlan testPlan = new MsTestPlan();
        testPlan.setHashTree(new LinkedList<>());
        try {
            boolean isFirst = true;
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            ApiScenarioReportMapper batchMapper = sqlSession.getMapper(ApiScenarioReportMapper.class);
            for (ApiScenarioWithBLOBs item : apiScenarios) {
                if (item.getStepTotal() == null || item.getStepTotal() == 0) {
                    // 只有 一个场景且没有测试步骤，则提示
                    if (apiScenarios.size() == 1) {
                        MSException.throwException((item.getName() + "，" + Translator.get("automation_exec_info")));
                    }
                    LogUtil.warn(item.getName() + "，" + Translator.get("automation_exec_info"));
                    continue;
                }
                MsThreadGroup group = new MsThreadGroup();
                group.setLabel(item.getName());
                group.setName(UUID.randomUUID().toString());
                if (request.getConfig() != null) {
                    group.setOnSampleError(request.getConfig().isOnSampleError());
                }
                // 批量执行的结果直接存储为报告
                if (isFirst && StringUtils.isNotEmpty(request.getId())) {
                    group.setName(request.getId());
                }
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                JSONObject element = JSON.parseObject(item.getScenarioDefinition());
                // 历史数据处理
                ElementUtil.dataFormatting(element.getJSONArray("hashTree"));

                MsScenario scenario = JSONObject.parseObject(item.getScenarioDefinition(), MsScenario.class);
                group.setOnSampleError(scenario.getOnSampleError());
                // 多态JSON普通转换会丢失内容，需要通过 ObjectMapper 获取
                if (element != null && StringUtils.isNotEmpty(element.getString("hashTree"))) {
                    LinkedList<MsTestElement> elements = mapper.readValue(element.getString("hashTree"),
                            new TypeReference<LinkedList<MsTestElement>>() {
                            });
                    scenario.setHashTree(elements);
                }
                if (StringUtils.isNotEmpty(element.getString("variables"))) {
                    LinkedList<ScenarioVariable> variables = mapper.readValue(element.getString("variables"),
                            new TypeReference<LinkedList<ScenarioVariable>>() {
                            });
                    scenario.setVariables(variables);
                }
                group.setEnableCookieShare(scenario.isEnableCookieShare());
                group.setOnSampleError(scenario.getOnSampleError());
                LinkedList<MsTestElement> scenarios = new LinkedList<>();
                scenarios.add(scenario);
                // 创建场景报告
                if (reportIds != null) {
                    //如果是测试计划页面触发的执行方式，生成报告时createScenarioReport第二个参数需要特殊处理
                    APIScenarioReportResult report = null;
                    if (StringUtils.equalsAny(request.getRunMode(), ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
                        String testPlanScenarioId = item.getId();
                        if (request.getScenarioTestPlanIdMap() != null && request.getScenarioTestPlanIdMap().containsKey(item.getId())) {
                            testPlanScenarioId = request.getScenarioTestPlanIdMap().get(item.getId());
                            // 获取场景用例单独的执行环境
                            TestPlanApiScenario planApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(testPlanScenarioId);
                            String envJson = planApiScenario.getEnvironment();
                            String envType = planApiScenario.getEnvironmentType();
                            String envGroupId = planApiScenario.getEnvironmentGroupId();
                            if (StringUtils.equals(envType, EnvironmentType.JSON.toString()) && StringUtils.isNotBlank(envJson)) {
                                scenario.setEnvironmentMap(JSON.parseObject(envJson, Map.class));
                            } else if (StringUtils.equals(envType, EnvironmentType.GROUP.toString()) && StringUtils.isNotBlank(envGroupId)) {
                                Map<String, String> envMap = environmentGroupProjectService.getEnvMap(envGroupId);
                                scenario.setEnvironmentMap(envMap);
                            }
                        }

                        String projectId = testPlanScenarioCaseService.getProjectIdById(testPlanScenarioId);
                        if (StringUtils.isEmpty(projectId)) {
                            projectId = item.getProjectId();
                        }

                        if (request.isTestPlanScheduleJob()) {
                            String savedScenarioId = testPlanScenarioId + ":" + request.getTestPlanReportId();
                            report = createScenarioReport(group.getName(), savedScenarioId, item.getName(), request.getTriggerMode(),
                                    request.getExecuteType(), projectId, request.getReportUserID(), request.getConfig(), item.getId());
                        } else {
                            report = createScenarioReport(group.getName(), testPlanScenarioId, item.getName(), request.getTriggerMode() == null ? ReportTriggerMode.MANUAL.name() : request.getTriggerMode(),
                                    request.getExecuteType(), projectId, request.getReportUserID(), request.getConfig(), item.getId());
                        }
                    } else {
                        report = createScenarioReport(group.getName(), item.getId(), item.getName(), request.getTriggerMode() == null ? ReportTriggerMode.MANUAL.name() : request.getTriggerMode(),
                                request.getExecuteType(), item.getProjectId(), request.getReportUserID(), request.getConfig(), item.getId());
                    }
                    batchMapper.insert(report);
                    reportIds.add(group.getName());
                }
                group.setHashTree(scenarios);
                testPlan.getHashTree().add(group);
                isFirst = false;
            }
            testPlan.toHashTree(jmeterHashTree, testPlan.getHashTree(), new ParameterConfig());
            sqlSession.flushStatements();
            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        } catch (Exception ex) {
            MSException.throwException(ex.getMessage());
        }

        return jmeterHashTree;
    }

    private boolean checkScenarioEnv(ApiScenarioWithBLOBs apiScenarioWithBLOBs, TestPlanApiScenario testPlanApiScenarios) {
        String definition = apiScenarioWithBLOBs.getScenarioDefinition();
        MsScenario scenario = JSONObject.parseObject(definition, MsScenario.class);
        boolean isEnv = true;
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
        return isEnv;
    }

    /**
     * 串行，利用JMETER自身串行机制执行
     *
     * @param request
     * @return
     */
    public String execute(RunScenarioRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery(query));
        List<String> ids = request.getIds();
        //检查是否有正在执行中的情景
//        this.checkScenarioIsRunning(ids);
        StringBuilder idStr = new StringBuilder();
        ids.forEach(item -> {
            idStr.append("\"").append(item).append("\"").append(",");
        });
        List<ApiScenarioWithBLOBs> apiScenarios = extApiScenarioMapper.
                selectByIds(idStr.toString().substring(0, idStr.toString().length() - 1), "\"" + StringUtils.join(ids, ",") + "\"");

        String runMode = ApiRunMode.SCENARIO.name();
        if (StringUtils.isNotBlank(request.getRunMode()) && StringUtils.equals(request.getRunMode(), ApiRunMode.SCENARIO_PLAN.name())) {
            runMode = ApiRunMode.SCENARIO_PLAN.name();
        } else if (StringUtils.isNotBlank(request.getRunMode()) && StringUtils.equals(request.getRunMode(), ApiRunMode.SCHEDULE_SCENARIO.name())) {
            runMode = ApiRunMode.SCHEDULE_SCENARIO.name();
        }
        if (StringUtils.isNotBlank(request.getRunMode()) && StringUtils.equals(request.getRunMode(), ApiRunMode.DEFINITION.name())) {
            runMode = ApiRunMode.DEFINITION.name();
        }

        // 环境检查
        this.checkEnv(request, apiScenarios);

        // 调用执行方法
        List<String> reportIds = new LinkedList<>();
        try {
            HashTree hashTree = generateHashTree(apiScenarios, request, reportIds);
            jMeterService.runLocal(reportIds.size() == 1 ? reportIds.get(0) : JSON.toJSONString(reportIds), request.getConfig(), hashTree, request.getReportId(), runMode);

            Map<String, String> scenarioReportIdMap = new HashMap<>();
            for (String id : ids) {
                scenarioReportIdMap.put(id, request.getReportId());
            }
            TestPlanReportExecuteCatch.updateTestPlanThreadInfo(request.getTestPlanReportId(), null, scenarioReportIdMap, null);

        } catch (Exception e) {
            LogUtil.error(e);
            MSException.throwException(e.getMessage());
        }
        return request.getId();
    }

    public String run(RunScenarioRequest request) {
        if (request.getConfig() != null) {
            if (request.getConfig().getMode().equals(RunModeConstants.PARALLEL.toString())) {
                // 校验并发数量
                int count = 50;
                BaseSystemConfigDTO dto = systemParameterService.getBaseInfo();
                if (StringUtils.isNotEmpty(dto.getConcurrency())) {
                    count = Integer.parseInt(dto.getConcurrency());
                }
                if (request.getIds().size() > count) {
                    MSException.throwException("并发数量过大，请重新选择！");
                }
            }
            return this.modeRun(request);
        } else {
            return this.execute(request);
        }
    }

    /**
     * 场景测试执行
     *
     * @param request
     * @param bodyFiles
     * @return
     */
    public String debugRun(RunDefinitionRequest request, List<MultipartFile> bodyFiles, List<MultipartFile> scenarioFiles) {
        Map<String, EnvironmentConfig> envConfig = new HashMap<>();
        Map<String, String> map = request.getEnvironmentMap();
        String envType = request.getEnvironmentType();
        if (StringUtils.equals(envType, EnvironmentType.GROUP.toString())) {
            String environmentGroupId = request.getEnvironmentGroupId();
            map = environmentGroupProjectService.getEnvMap(environmentGroupId);
        }

        if (map != null) {
            Map<String, String> finalMap = map;
            map.keySet().forEach(id -> {
                ApiTestEnvironmentWithBLOBs environment = environmentService.get(finalMap.get(id));
                if (environment != null && environment.getConfig() != null) {
                    EnvironmentConfig env = JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class);
                    env.setApiEnvironmentid(environment.getId());
                    envConfig.put(id, env);
                }
            });
        }
        try {
            this.preduceTestElement(request);
        } catch (Exception e) {
        }
        ParameterConfig config = new ParameterConfig();
        config.setConfig(envConfig);
        HashTree hashTree = null;
        try {
            hashTree = request.getTestElement().generateHashTree(config);
            LogUtil.info(request.getTestElement().getJmx(hashTree));
        } catch (Exception e) {
            MSException.throwException(e.getMessage());
        }

        APIScenarioReportResult report = createScenarioReport(request.getId(), request.getScenarioId(), request.getScenarioName(), ReportTriggerMode.MANUAL.name(), request.getExecuteType(), request.getProjectId(),
                SessionUtils.getUserId(), request.getConfig(), request.getId());
        apiScenarioReportMapper.insert(report);

        uploadBodyFiles(request.getBodyFileRequestIds(), bodyFiles);
        FileUtils.createBodyFiles(request.getScenarioFileIds(), scenarioFiles);

        // 调用执行方法
        jMeterService.runLocal(request.getId(), request.getConfig(), hashTree, request.getExecuteType(), ApiRunMode.SCENARIO.name());
        return request.getId();
    }

    public void preduceTestElement(RunDefinitionRequest request) throws Exception {
        if (request.getTestElement() != null) {
            tcpApiParamService.checkTestElement(request.getTestElement());
        }
    }


    public ReferenceDTO getReference(ApiScenarioRequest request) {
        ReferenceDTO dto = new ReferenceDTO();
        dto.setScenarioList(extApiScenarioMapper.selectReference(request));
        QueryTestPlanRequest planRequest = new QueryTestPlanRequest();
        planRequest.setScenarioId(request.getId());
        planRequest.setProjectId(request.getProjectId());
        dto.setTestPlanList(extTestPlanMapper.selectTestPlanByRelevancy(planRequest));
        return dto;
    }


    public String addScenarioToPlan(SaveApiPlanRequest request) {
        if (CollectionUtils.isEmpty(request.getPlanIds())) {
            MSException.throwException(Translator.get("plan id is null "));
        }
        Map<String, List<String>> mapping = request.getMapping();
        Map<String, String> envMap = request.getEnvMap();
        Set<String> set = mapping.keySet();
        List<TestPlanDTO> list = extTestPlanMapper.selectByIds(request.getPlanIds());
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ExtTestPlanScenarioCaseMapper scenarioBatchMapper = sqlSession.getMapper(ExtTestPlanScenarioCaseMapper.class);
        ExtTestPlanApiCaseMapper apiCaseBatchMapper = sqlSession.getMapper(ExtTestPlanApiCaseMapper.class);

        String environmentType = request.getEnvironmentType();
        String envGroupId = request.getEnvGroupId();
        for (TestPlanDTO testPlan : list) {
            if (!set.isEmpty()) {
                set.forEach(id -> {
                    TestPlanApiScenario testPlanApiScenario = new TestPlanApiScenario();
                    testPlanApiScenario.setId(UUID.randomUUID().toString());
                    testPlanApiScenario.setApiScenarioId(id);
                    testPlanApiScenario.setTestPlanId(testPlan.getId());
                    testPlanApiScenario.setCreateTime(System.currentTimeMillis());
                    testPlanApiScenario.setUpdateTime(System.currentTimeMillis());
                    if (StringUtils.equals(environmentType, EnvironmentType.GROUP.name()) && StringUtils.isNotBlank(envGroupId)) {
                        testPlanApiScenario.setEnvironmentType(EnvironmentType.GROUP.name());
                        testPlanApiScenario.setEnvironmentGroupId(envGroupId);
                    } else if (StringUtils.equals(environmentType, EnvironmentType.JSON.name())) {
                        Map<String, String> newEnvMap = new HashMap<>(16);
                        if (envMap != null && !envMap.isEmpty()) {
                            List<String> lt = mapping.get(id);
                            lt.forEach(l -> {
                                newEnvMap.put(l, envMap.get(l));
                            });
                        }
                        testPlanApiScenario.setEnvironmentType(EnvironmentType.JSON.name());
                        testPlanApiScenario.setEnvironment(JSON.toJSONString(newEnvMap));
                    }
                    if (StringUtils.isNotBlank(testPlanApiScenario.getEnvironmentType())) {
                        Long nextScenarioOrder = ServiceUtils.getNextOrder(testPlan.getId(), extTestPlanScenarioCaseMapper::getLastOrder);
                        testPlanApiScenario.setOrder(nextScenarioOrder);
                        scenarioBatchMapper.insertIfNotExists(testPlanApiScenario);
                    }
                });
            }
            if (request.getApiIds() != null) {
                for (String caseId : request.getApiIds()) {
                    TestPlanApiCase testPlanApiCase = new TestPlanApiCase();
                    testPlanApiCase.setId(UUID.randomUUID().toString());
                    testPlanApiCase.setApiCaseId(caseId);
                    testPlanApiCase.setTestPlanId(testPlan.getId());
                    testPlanApiCase.setCreateTime(System.currentTimeMillis());
                    testPlanApiCase.setUpdateTime(System.currentTimeMillis());
                    apiCaseBatchMapper.insertIfNotExists(testPlanApiCase);
                }
            }
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
        return "success";
    }

    public long countScenarioByProjectID(String projectId) {
        return extApiScenarioMapper.countByProjectID(projectId);
    }

    public List<ApiScenarioWithBLOBs> selectIdAndUseUrlByProjectId(String projectId) {
        return extApiScenarioMapper.selectIdAndUseUrlByProjectId(projectId);
    }

    public long countScenarioByProjectIDAndCreatInThisWeek(String projectId) {
        Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());
        Date firstTime = startAndEndDateInWeek.get("firstTime");
        Date lastTime = startAndEndDateInWeek.get("lastTime");
        if (firstTime == null || lastTime == null) {
            return 0;
        } else {
            return extApiScenarioMapper.countByProjectIDAndCreatInThisWeek(projectId, firstTime.getTime(), lastTime.getTime());
        }
    }

    public List<ApiDataCountResult> countRunResultByProjectID(String projectId) {
        return extApiScenarioMapper.countRunResultByProjectID(projectId);
    }

    public void relevance(ApiCaseRelevanceRequest request) {
        Map<String, List<String>> mapping = request.getMapping();
        Map<String, String> envMap = request.getEnvMap();
        Set<String> set = mapping.keySet();
        String envType = request.getEnvironmentType();
        String envGroupId = request.getEnvGroupId();
        if (set.isEmpty()) {
            return;
        }
        Long nextOrder = ServiceUtils.getNextOrder(request.getPlanId(), extTestPlanScenarioCaseMapper::getLastOrder);
        for (String id : set) {
            Map<String, String> newEnvMap = new HashMap<>(16);
            List<String> list = mapping.get(id);
            list.forEach(l -> newEnvMap.put(l, envMap == null ? "" : envMap.getOrDefault(l, "")));
            TestPlanApiScenario testPlanApiScenario = new TestPlanApiScenario();
            testPlanApiScenario.setId(UUID.randomUUID().toString());
            testPlanApiScenario.setCreateUser(SessionUtils.getUserId());
            testPlanApiScenario.setApiScenarioId(id);
            testPlanApiScenario.setTestPlanId(request.getPlanId());
            testPlanApiScenario.setCreateTime(System.currentTimeMillis());
            testPlanApiScenario.setUpdateTime(System.currentTimeMillis());
            String environmentJson = JSON.toJSONString(newEnvMap);
            if (StringUtils.equals(envType, EnvironmentType.JSON.name())) {
                testPlanApiScenario.setEnvironment(environmentJson);
                testPlanApiScenario.setEnvironmentType(EnvironmentType.JSON.name());
            } else if (StringUtils.equals(envType, EnvironmentType.GROUP.name())) {
                testPlanApiScenario.setEnvironmentType(EnvironmentType.GROUP.name());
                testPlanApiScenario.setEnvironmentGroupId(envGroupId);
                // JSON类型环境中也保存最新值
                testPlanApiScenario.setEnvironment(environmentJson);
            }
            testPlanApiScenario.setOrder(nextOrder);
            nextOrder += 5000;
            testPlanApiScenarioMapper.insert(testPlanApiScenario);
        }
    }

    public void relevanceReview(ApiCaseRelevanceRequest request) {
        Map<String, List<String>> mapping = request.getMapping();
        Map<String, String> envMap = request.getEnvMap();
        Set<String> set = mapping.keySet();
        if (set.isEmpty()) {
            return;
        }
        set.forEach(id -> {
            Map<String, String> newEnvMap = new HashMap<>(16);
            if (envMap != null && !envMap.isEmpty()) {
                List<String> list = mapping.get(id);
                list.forEach(l -> {
                    newEnvMap.put(l, envMap.get(l));
                });
            }
            TestCaseReviewScenario testCaseReviewScenario = new TestCaseReviewScenario();
            testCaseReviewScenario.setId(UUID.randomUUID().toString());
            testCaseReviewScenario.setApiScenarioId(id);
            testCaseReviewScenario.setTestCaseReviewId(request.getReviewId());
            testCaseReviewScenario.setCreateTime(System.currentTimeMillis());
            testCaseReviewScenario.setUpdateTime(System.currentTimeMillis());
            testCaseReviewScenario.setEnvironment(JSON.toJSONString(newEnvMap));
            testCaseReviewScenarioMapper.insert(testCaseReviewScenario);
        });
    }

    public List<ApiScenario> selectByIds(List<String> ids) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(ids);
        return apiScenarioMapper.selectByExample(example);
    }

    public List<ApiScenarioWithBLOBs> selectByIdsWithBLOBs(List<String> ids) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(ids);
        return apiScenarioMapper.selectByExampleWithBLOBs(example);
    }


    public void createSchedule(ScheduleRequest request) {
        Schedule schedule = scheduleService.buildApiTestSchedule(request);
        ApiScenarioWithBLOBs apiScene = apiScenarioMapper.selectByPrimaryKey(request.getResourceId());
        schedule.setName(apiScene.getName());   //  add场景定时任务时，设置新增的数据库表字段的值
        schedule.setProjectId(apiScene.getProjectId());
        schedule.setJob(ApiScenarioTestJob.class.getName());
        schedule.setGroup(ScheduleGroup.API_SCENARIO_TEST.name());
        schedule.setType(ScheduleType.CRON.name());
        scheduleService.addSchedule(schedule);
        this.addOrUpdateApiScenarioCronJob(request);
    }

    public void updateSchedule(Schedule request) {
        scheduleService.editSchedule(request);
        this.addOrUpdateApiScenarioCronJob(request);
    }

    private void addOrUpdateApiScenarioCronJob(Schedule request) {
        if (StringUtils.equals(request.getGroup(), ScheduleGroup.TEST_PLAN_TEST.name())) {
            scheduleService.addOrUpdateCronJob(
                    request, TestPlanTestJob.getJobKey(request.getResourceId()), TestPlanTestJob.getTriggerKey(request.getResourceId()), TestPlanTestJob.class);
        } else if (StringUtils.equals(request.getGroup(), ScheduleGroup.SWAGGER_IMPORT.name())) {
            scheduleService.addOrUpdateCronJob(request, SwaggerUrlImportJob.getJobKey(request.getResourceId()), SwaggerUrlImportJob.getTriggerKey(request.getResourceId()), SwaggerUrlImportJob.class);
        } else {
            scheduleService.addOrUpdateCronJob(
                    request, ApiScenarioTestJob.getJobKey(request.getResourceId()), ApiScenarioTestJob.getTriggerKey(request.getResourceId()), ApiScenarioTestJob.class);
        }

    }

    public JmxInfoDTO genPerformanceTestJmx(RunScenarioRequest request) {
        List<ApiScenarioWithBLOBs> apiScenarios = null;
        List<String> ids = request.getIds();
        apiScenarios = extApiScenarioMapper.selectIds(ids);
        String testName = "";
        String id = "";
        if (!apiScenarios.isEmpty()) {
            testName = apiScenarios.get(0).getName();
            id = apiScenarios.get(0).getId();
        }
        if (CollectionUtils.isEmpty(apiScenarios)) {
            return null;
        }
        MsTestPlan testPlan = new MsTestPlan();
        testPlan.setHashTree(new LinkedList<>());
        JmxInfoDTO dto = apiTestService.updateJmxString(generateJmx(apiScenarios.get(0)), testName, true);

        String name = request.getName() + ".jmx";
        dto.setName(name);
        dto.setId(id);
        return dto;
    }

    public void bathEdit(ApiScenarioBatchRequest request) {

        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery(query));

        if (StringUtils.isNotBlank(request.getEnvironmentId())) {
            bathEditEnv(request);
            return;
        }
        ApiScenarioExample apiScenarioExample = new ApiScenarioExample();
        apiScenarioExample.createCriteria().andIdIn(request.getIds());
        ApiScenarioWithBLOBs apiScenarioWithBLOBs = new ApiScenarioWithBLOBs();
        BeanUtils.copyBean(apiScenarioWithBLOBs, request);
        apiScenarioWithBLOBs.setUpdateTime(System.currentTimeMillis());
        if (apiScenarioWithBLOBs.getScenarioDefinition() != null) {
            List<ApiMethodUrlDTO> useUrl = this.parseUrl(apiScenarioWithBLOBs);
            apiScenarioWithBLOBs.setUseUrl(JSONArray.toJSONString(useUrl));
        }
        apiScenarioMapper.updateByExampleSelective(
                apiScenarioWithBLOBs,
                apiScenarioExample);
//        apiScenarioReferenceIdService.saveByApiScenario(apiScenarioWithBLOBs);
    }

    public void bathEditEnv(ApiScenarioBatchRequest request) {
        if (StringUtils.isNotBlank(request.getEnvironmentId())) {
            List<ApiScenarioWithBLOBs> apiScenarios = selectByIdsWithBLOBs(request.getIds());
            apiScenarios.forEach(item -> {
                JSONObject object = JSONObject.parseObject(item.getScenarioDefinition());
                object.put("environmentId", request.getEnvironmentId());
                if (object != null) {
                    item.setScenarioDefinition(JSONObject.toJSONString(object));
                }
                apiScenarioMapper.updateByPrimaryKeySelective(item);
                apiScenarioReferenceIdService.saveByApiScenario(item);
            });
        }
    }

    public List<ApiScenarioWithBLOBs> getSameScenario(ApiScenarioWithBLOBs request) {
        ApiScenarioExample example = new ApiScenarioExample();
        ApiScenarioExample.Criteria criteria = example.createCriteria();
        criteria.andProjectIdEqualTo(request.getProjectId())
                .andStatusNotEqualTo("Trash")
                .andNameEqualTo(request.getName());
        if (StringUtils.isNotBlank(request.getId())) {
            // id 不为空 则判断，id一样或者名字一样则是同一个用例
            ApiScenarioExample.Criteria criteria1 = example.createCriteria();
            criteria1.andProjectIdEqualTo(request.getProjectId())
                    .andStatusNotEqualTo("Trash")
                    .andIdEqualTo(request.getId());
            example.or(criteria1);
        }
        return apiScenarioMapper.selectByExampleWithBLOBs(example);
    }

    private void _importCreate(List<ApiScenarioWithBLOBs> sameRequest, ApiScenarioMapper batchMapper, ApiScenarioWithBLOBs scenarioWithBLOBs, ApiTestImportRequest apiTestImportRequest) {
        if (CollectionUtils.isEmpty(sameRequest)) {
            scenarioWithBLOBs.setId(UUID.randomUUID().toString());
            List<ApiMethodUrlDTO> useUrl = this.parseUrl(scenarioWithBLOBs);
            scenarioWithBLOBs.setUseUrl(JSONArray.toJSONString(useUrl));
            scenarioWithBLOBs.setOrder(getImportNextOrder(apiTestImportRequest.getProjectId()));
            batchMapper.insert(scenarioWithBLOBs);
            apiScenarioReferenceIdService.saveByApiScenario(scenarioWithBLOBs);
        } else {
            //如果存在则修改
            scenarioWithBLOBs.setId(sameRequest.get(0).getId());
            scenarioWithBLOBs.setNum(sameRequest.get(0).getNum());
            List<ApiMethodUrlDTO> useUrl = this.parseUrl(scenarioWithBLOBs);
            scenarioWithBLOBs.setUseUrl(JSONArray.toJSONString(useUrl));
            batchMapper.updateByPrimaryKeyWithBLOBs(scenarioWithBLOBs);
            apiScenarioReferenceIdService.saveByApiScenario(scenarioWithBLOBs);
        }
    }

    private ApiScenarioWithBLOBs importCreate(ApiScenarioWithBLOBs request, ApiScenarioMapper batchMapper, ApiTestImportRequest apiTestImportRequest) {
        final ApiScenarioWithBLOBs scenarioWithBLOBs = new ApiScenarioWithBLOBs();
        BeanUtils.copyBean(scenarioWithBLOBs, request);
        scenarioWithBLOBs.setCreateTime(System.currentTimeMillis());
        scenarioWithBLOBs.setUpdateTime(System.currentTimeMillis());
        if (StringUtils.isEmpty(scenarioWithBLOBs.getStatus())) {
            scenarioWithBLOBs.setStatus(APITestStatus.Underway.name());
        }
        scenarioWithBLOBs.setProjectId(apiTestImportRequest.getProjectId());
        if (StringUtils.isEmpty(request.getPrincipal())) {
            scenarioWithBLOBs.setPrincipal(Objects.requireNonNull(SessionUtils.getUser()).getId());
        }
        if (request.getUserId() == null) {
            scenarioWithBLOBs.setUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        } else {
            scenarioWithBLOBs.setUserId(request.getUserId());
        }
        scenarioWithBLOBs.setDescription(request.getDescription());

        List<ApiScenarioWithBLOBs> sameRequest = getSameScenario(scenarioWithBLOBs);

        Boolean openCustomNum = apiTestImportRequest.getOpenCustomNum();
        List<ApiScenario> list = new ArrayList<>();
        if (BooleanUtils.isTrue(openCustomNum)) {
            ApiScenarioExample example = new ApiScenarioExample();
            ApiScenarioExample.Criteria criteria = example.createCriteria();
            if (CollectionUtils.isEmpty(sameRequest)) {
                criteria.andCustomNumEqualTo(scenarioWithBLOBs.getCustomNum())
                        .andProjectIdEqualTo(scenarioWithBLOBs.getProjectId());
            } else {
                if (StringUtils.equals("fullCoverage", apiTestImportRequest.getModeId())) {
                    criteria.andNameEqualTo(scenarioWithBLOBs.getName())
                            .andCustomNumEqualTo(scenarioWithBLOBs.getCustomNum())
                            .andProjectIdEqualTo(scenarioWithBLOBs.getProjectId())
                            .andIdNotEqualTo(sameRequest.get(0).getId());
                }

            }
            if (criteria.isValid()) {
                list = apiScenarioMapper.selectByExample(example);
            }
        }

        if (CollectionUtils.isNotEmpty(list)) {
            LogUtil.error("import scenario fail, custom num is exist: " + scenarioWithBLOBs.getCustomNum());
            return scenarioWithBLOBs;
        }

        if (StringUtils.equals("fullCoverage", apiTestImportRequest.getModeId())) {
            _importCreate(sameRequest, batchMapper, scenarioWithBLOBs, apiTestImportRequest);
        } else if (StringUtils.equals("incrementalMerge", apiTestImportRequest.getModeId())) {
            if (CollectionUtils.isEmpty(sameRequest)) {
                List<ApiMethodUrlDTO> useUrl = this.parseUrl(scenarioWithBLOBs);
                scenarioWithBLOBs.setUseUrl(JSONArray.toJSONString(useUrl));
                scenarioWithBLOBs.setOrder(getImportNextOrder(request.getProjectId()));
                scenarioWithBLOBs.setId(UUID.randomUUID().toString());
                batchMapper.insert(scenarioWithBLOBs);

                // 存储依赖关系
                ApiAutomationRelationshipEdgeService relationshipEdgeService = CommonBeanFactory.getBean(ApiAutomationRelationshipEdgeService.class);
                if (relationshipEdgeService != null) {
                    relationshipEdgeService.initRelationshipEdge(null, scenarioWithBLOBs);
                }
                apiScenarioReferenceIdService.saveByApiScenario(scenarioWithBLOBs);
            }

        } else {
            _importCreate(sameRequest, batchMapper, scenarioWithBLOBs, apiTestImportRequest);
        }
        return scenarioWithBLOBs;
    }

    private void editScenario(ApiTestImportRequest request, ScenarioImport apiImport) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiScenarioMapper batchMapper = sqlSession.getMapper(ApiScenarioMapper.class);
        List<ApiScenarioWithBLOBs> data = apiImport.getData();
        currentScenarioOrder.remove();
        int num = 0;
        Project project = new Project();
        if (!CollectionUtils.isEmpty(data) && data.get(0) != null && data.get(0).getProjectId() != null) {
            project = projectMapper.selectByPrimaryKey(data.get(0).getProjectId());
            num = getNextNum(data.get(0).getProjectId());
            request.setOpenCustomNum(project.getScenarioCustomNum());
        }
        for (int i = 0; i < data.size(); i++) {
            ApiScenarioWithBLOBs item = data.get(i);
            if (item.getName().length() > 255) {
                item.setName(item.getName().substring(0, 255));
            }
            item.setNum(num);
            if (BooleanUtils.isFalse(project.getScenarioCustomNum())) {
                // 如果未开启，即使有自定值也直接覆盖
                item.setCustomNum(String.valueOf(num));
            } else {
                if (StringUtils.isBlank(item.getCustomNum())) {
                    item.setCustomNum(String.valueOf(num));
                }
            }
            num++;
            if (StringUtils.isBlank(item.getId())) {
                item.setId(UUID.randomUUID().toString());
            }
            importCreate(item, batchMapper, request);
            if (i % 300 == 0) {
                sqlSession.flushStatements();
            }
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    private Long getImportNextOrder(String projectId) {
        Long order = currentScenarioOrder.get();
        if (order == null) {
            order = ServiceUtils.getNextOrder(projectId, extApiScenarioMapper::getLastOrder);
        }
        order = (order == null ? 0 : order) + 5000;
        currentScenarioOrder.set(order);
        return order;
    }

    public ScenarioImport scenarioImport(MultipartFile file, ApiTestImportRequest request) {
        ApiImportParser apiImportParser = ScenarioImportParserFactory.getImportParser(request.getPlatform());
        ScenarioImport apiImport = null;
        Optional.ofNullable(file)
                .ifPresent(item -> request.setFileName(file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf("."))));
        try {
            apiImport = (ScenarioImport) Objects.requireNonNull(apiImportParser).parse(file == null ? null : file.getInputStream(), request);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(Translator.get("parse_data_error"));
        }
        if (apiImport != null) {
            editScenario(request, apiImport);
            if (CollectionUtils.isNotEmpty(apiImport.getData())) {
                List<String> names = apiImport.getData().stream().map(ApiScenarioWithBLOBs::getName).collect(Collectors.toList());
                List<String> ids = apiImport.getData().stream().map(ApiScenarioWithBLOBs::getId).collect(Collectors.toList());
                request.setName(String.join(",", names));
                request.setId(JSON.toJSONString(ids));
            }
        }
        return apiImport;
    }

    private void setHashTree(JSONArray hashTree) {
        // 将引用转成复制
        if (CollectionUtils.isNotEmpty(hashTree)) {
            for (int i = 0; i < hashTree.size(); i++) {
                JSONObject object = (JSONObject) hashTree.get(i);
                String referenced = object.getString("referenced");
                if (StringUtils.isNotBlank(referenced) && StringUtils.equals(referenced, "REF")) {
                    // 检测引用对象是否存在，若果不存在则改成复制对象
                    String refType = object.getString("refType");
                    if (StringUtils.isNotEmpty(refType)) {
                        if (refType.equals("CASE")) {
                            ApiTestCaseWithBLOBs bloBs = apiTestCaseService.get(object.getString("id"));
                            if (bloBs != null) {
                                object = JSON.parseObject(bloBs.getRequest());
                                object.put("id", bloBs.getId());
                                object.put("name", bloBs.getName());
                                hashTree.set(i, object);
                            }
                        } else {
                            ApiScenarioWithBLOBs bloBs = apiScenarioMapper.selectByPrimaryKey(object.getString("id"));
                            if (bloBs != null) {
                                object = JSON.parseObject(bloBs.getScenarioDefinition());
                                hashTree.set(i, object);
                            }
                        }
                    } else if ("scenario".equals(object.getString("type"))) {
                        ApiScenarioWithBLOBs bloBs = apiScenarioMapper.selectByPrimaryKey(object.getString("id"));
                        if (bloBs != null) {
                            object = JSON.parseObject(bloBs.getScenarioDefinition());
                            hashTree.set(i, object);
                        }
                    }
                }
                if (object != null && CollectionUtils.isNotEmpty(object.getJSONArray("hashTree"))) {
                    setHashTree(object.getJSONArray("hashTree"));
                }
            }
        }
    }

    private List<ApiScenarioWithBLOBs> getExportResult(ApiScenarioBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery(query));
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(request.getIds());
        List<ApiScenarioWithBLOBs> apiScenarioWithBLOBs = apiScenarioMapper.selectByExampleWithBLOBs(example);
        // 处理引用数据
        if (CollectionUtils.isNotEmpty(apiScenarioWithBLOBs)) {
            apiScenarioWithBLOBs.forEach(item -> {
                if (StringUtils.isNotEmpty(item.getScenarioDefinition())) {
                    JSONObject scenario = JSONObject.parseObject(item.getScenarioDefinition());
                    JSONArray hashTree = scenario.getJSONArray("hashTree");
                    if (hashTree != null) {
                        setHashTree(hashTree);
                        scenario.put("hashTree", hashTree);
                    }
                    item.setScenarioDefinition(JSON.toJSONString(scenario));
                }
            });
        }
        return apiScenarioWithBLOBs;
    }

    public ApiScenrioExportResult export(ApiScenarioBatchRequest request) {
        ApiScenrioExportResult result = new ApiScenrioExportResult();
        result.setData(getExportResult(request));
        result.setProjectId(request.getProjectId());
        result.setVersion(System.getenv("MS_VERSION"));
        if (CollectionUtils.isNotEmpty(result.getData())) {
            List<String> names = result.getData().stream().map(ApiScenarioWithBLOBs::getName).collect(Collectors.toList());
            request.setName(String.join(",", names));
            List<String> ids = result.getData().stream().map(ApiScenarioWithBLOBs::getId).collect(Collectors.toList());
            request.setId(JSON.toJSONString(ids));
        }
        return result;
    }

    public List<ApiScenrioExportJmx> exportJmx(ApiScenarioBatchRequest request) {
        List<ApiScenarioWithBLOBs> apiScenarioWithBLOBs = getExportResult(request);
        // 生成jmx
        List<ApiScenrioExportJmx> resList = new ArrayList<>();
        apiScenarioWithBLOBs.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getScenarioDefinition())) {
                String jmx = generateJmx(item);
                if (StringUtils.isNotEmpty(jmx)) {
                    ApiScenrioExportJmx scenrioExportJmx = new ApiScenrioExportJmx(item.getName(), apiTestService.updateJmxString(jmx, null, true).getXml());
                    JmxInfoDTO dto = apiTestService.updateJmxString(jmx, item.getName(), true);
                    scenrioExportJmx.setId(item.getId());
                    scenrioExportJmx.setVersion(item.getVersion());
                    //扫描需要哪些文件
                    scenrioExportJmx.setFileMetadataList(dto.getFileMetadataList());
                    resList.add(scenrioExportJmx);
                }
            }
        });
        if (CollectionUtils.isNotEmpty(apiScenarioWithBLOBs)) {
            List<String> names = apiScenarioWithBLOBs.stream().map(ApiScenarioWithBLOBs::getName).collect(Collectors.toList());
            request.setName(String.join(",", names));
            List<String> ids = apiScenarioWithBLOBs.stream().map(ApiScenarioWithBLOBs::getId).collect(Collectors.toList());
            request.setId(JSON.toJSONString(ids));
        }
        return resList;
    }

    public byte[] exportZip(ApiScenarioBatchRequest request) {
        List<ApiScenarioWithBLOBs> apiScenarioWithBLOBs = getExportResult(request);
        // 生成jmx
        Map<String, byte[]> files = new LinkedHashMap<>();
        apiScenarioWithBLOBs.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getScenarioDefinition())) {
                String jmx = generateJmx(item);
                if (StringUtils.isNotEmpty(jmx)) {
                    ApiScenrioExportJmx scenrioExportJmx = new ApiScenrioExportJmx(item.getName(), apiTestService.updateJmxString(jmx, null, true).getXml());
                    String fileName = item.getName() + ".jmx";
                    String jmxStr = scenrioExportJmx.getJmx();
                    files.put(fileName, jmxStr.getBytes(StandardCharsets.UTF_8));
                }
            }
        });
        if (CollectionUtils.isNotEmpty(apiScenarioWithBLOBs)) {
            List<String> names = apiScenarioWithBLOBs.stream().map(ApiScenarioWithBLOBs::getName).collect(Collectors.toList());
            request.setName(String.join(",", names));
            List<String> ids = apiScenarioWithBLOBs.stream().map(ApiScenarioWithBLOBs::getId).collect(Collectors.toList());
            request.setId(JSON.toJSONString(ids));
        }
        return FileUtils.listBytesToZip(files);
    }

    public void batchUpdateEnv(ApiScenarioBatchRequest request) {
        Map<String, String> envMap = request.getEnvMap();
        String envType = request.getEnvironmentType();
        String envGroupId = request.getEnvironmentGroupId();
        Map<String, List<String>> mapping = request.getMapping();
        Set<String> set = mapping.keySet();
        if (set.isEmpty()) {
            return;
        }
        if (StringUtils.equals(envType, EnvironmentType.GROUP.name()) && StringUtils.isNotBlank(envGroupId)) {
            set.forEach(id -> {
                ApiScenarioWithBLOBs apiScenario = new ApiScenarioWithBLOBs();
                apiScenario.setId(id);
                apiScenario.setEnvironmentType(EnvironmentType.GROUP.name());
                apiScenario.setEnvironmentGroupId(envGroupId);
                apiScenarioMapper.updateByPrimaryKeySelective(apiScenario);
            });

        } else if (StringUtils.equals(envType, EnvironmentType.JSON.name())) {
            set.forEach(id -> {
                Map<String, String> newEnvMap = new HashMap<>(16);
                if (envMap != null && !envMap.isEmpty()) {
                    List<String> list = mapping.get(id);
                    list.forEach(l -> {
                        newEnvMap.put(l, envMap.get(l));
                    });
                }
                if (!newEnvMap.isEmpty()) {
                    ApiScenarioWithBLOBs apiScenario = new ApiScenarioWithBLOBs();
                    apiScenario.setId(id);
                    apiScenario.setEnvironmentType(EnvironmentType.JSON.name());
                    apiScenario.setEnvironmentJson(JSON.toJSONString(newEnvMap));
                    apiScenarioMapper.updateByPrimaryKeySelective(apiScenario);
                }
            });
        }
    }

    public void removeToGcByBatch(ApiScenarioBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery(query));

        this.removeToGc(request.getIds());
    }

    public void deleteBatchByCondition(ApiScenarioBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery(query));
        this.deleteBatch(request.getIds());
    }

    /**
     * 统计接口覆盖率
     * 1.场景中复制的接口
     * 2.场景中引用/复制的案例
     * 3.场景中的自定义路径与接口定义中的匹配
     * <p>
     * 匹配场景中用到的路径
     *
     * @param allScenarioInfoList 场景集合（id / scenario大字段 必须有数据）
     * @param allEffectiveApiList 接口集合（id / path 必须有数据）
     * @return
     */
    public float countInterfaceCoverage(List<ApiScenarioWithBLOBs> allScenarioInfoList, List<ApiDefinition> allEffectiveApiList) {
        if (allEffectiveApiList == null || allEffectiveApiList.isEmpty()) {
            return 100;
        }

        Map<ApiMethodUrlDTO, List<String>> urlMap = new HashMap<>();
        for (ApiDefinition model : allEffectiveApiList) {
            String url = model.getPath();
            String method = model.getMethod();
            String id = model.getId();

            ApiMethodUrlDTO dto = new ApiMethodUrlDTO(url, method);

            if (urlMap.containsKey(dto)) {
                urlMap.get(dto).add(id);
            } else {
                List<String> list = new ArrayList<>();
                list.add(id);
                urlMap.put(dto, list);
            }
        }

        if (urlMap.isEmpty()) {
            return 100;
        }

        List<ApiMethodUrlDTO> urlList = new ArrayList<>();
        for (ApiScenarioWithBLOBs model : allScenarioInfoList) {
            List<ApiMethodUrlDTO> useUrl = this.getScenarioUseUrl(model);
            if (CollectionUtils.isNotEmpty(useUrl)) {
                for (ApiMethodUrlDTO dto : useUrl) {
                    if (!urlList.contains(dto)) {
                        urlList.add(dto);
                    }
                }
            }
        }

        List<String> containsApiIdList = new ArrayList<>();
        for (ApiMethodUrlDTO urlDTO : urlList) {
            List<String> apiIdList = urlMap.get(urlDTO);
            if (apiIdList != null) {
                for (String api : apiIdList) {
                    if (!containsApiIdList.contains(api)) {
                        containsApiIdList.add(api);
                    }
                }
            }
        }

        int allApiIdCount = 0;
        for (List<String> allApiIdList : urlMap.values()) {
            if (CollectionUtils.isNotEmpty(allApiIdList)) {
                allApiIdCount += allApiIdList.size();
            }
        }

        float coverageRageNumber = (float) containsApiIdList.size() * 100 / allApiIdCount;
        return coverageRageNumber;
    }

    private List<ApiMethodUrlDTO> getScenarioUseUrl(ApiScenarioWithBLOBs model) {
        List<ApiMethodUrlDTO> useUrlList = new ArrayList<>();
        try {
            useUrlList = JSONArray.parseArray(model.getUseUrl(), ApiMethodUrlDTO.class);
        } catch (Exception e) {
        }
        return useUrlList;
    }

    public List<ApiMethodUrlDTO> parseUrl(ApiScenarioWithBLOBs scenario) {
        List<ApiMethodUrlDTO> urlList = new ArrayList<>();

        try {
            String scenarioDefinition = scenario.getScenarioDefinition();
            JSONObject scenarioObj = JSONObject.parseObject(scenarioDefinition);
            List<ApiMethodUrlDTO> stepUrlList = this.getMethodUrlDTOByHashTreeJsonObj(scenarioObj);
            if (CollectionUtils.isNotEmpty(stepUrlList)) {
                Collection unionList = CollectionUtils.union(urlList, stepUrlList);
                urlList = new ArrayList<>(unionList);
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return urlList;
    }

    private List<ApiMethodUrlDTO> getMethodUrlDTOByHashTreeJsonObj(JSONObject obj) {
        List<ApiMethodUrlDTO> returnList = new ArrayList<>();
        if (obj != null && obj.containsKey("hashTree")) {
            JSONArray hashArr = obj.getJSONArray("hashTree");
            for (int i = 0; i < hashArr.size(); i++) {
                JSONObject elementObj = hashArr.getJSONObject(i);
                if (elementObj == null) {
                    continue;
                }
                if (elementObj.containsKey("url") && elementObj.containsKey("method")) {
                    String url = elementObj.getString("url");
                    String method = elementObj.getString("method");
                    ApiMethodUrlDTO dto = new ApiMethodUrlDTO(url, method);
                    if (!returnList.contains(dto)) {
                        returnList.add(dto);
                    }
                }
                if (elementObj.containsKey("path") && elementObj.containsKey("method")) {
                    String path = elementObj.getString("path");
                    String method = elementObj.getString("method");
                    ApiMethodUrlDTO dto = new ApiMethodUrlDTO(path, method);
                    if (!returnList.contains(dto)) {
                        returnList.add(dto);
                    }
                }

                if (elementObj.containsKey("id") && elementObj.containsKey("refType")) {
                    String refType = elementObj.getString("refType");
                    String id = elementObj.getString("id");
                    if (StringUtils.equals("CASE", refType)) {
                        ApiDefinition apiDefinition = apiTestCaseService.findApiUrlAndMethodById(id);
                        if (apiDefinition != null) {
                            ApiMethodUrlDTO dto = new ApiMethodUrlDTO(apiDefinition.getPath(), apiDefinition.getMethod());
                            if (!returnList.contains(dto)) {
                                returnList.add(dto);
                            }
                        }
                    } else if (StringUtils.equals("API", refType)) {
                        ApiDefinition apiDefinition = apiDefinitionService.selectUrlAndMethodById(id);
                        if (apiDefinition != null) {
                            ApiMethodUrlDTO dto = new ApiMethodUrlDTO(apiDefinition.getPath(), apiDefinition.getMethod());
                            if (!returnList.contains(dto)) {
                                returnList.add(dto);
                            }
                        }
                    }
                }

                List<ApiMethodUrlDTO> stepUrlList = this.getMethodUrlDTOByHashTreeJsonObj(elementObj);
                if (CollectionUtils.isNotEmpty(stepUrlList)) {
                    Collection unionList = CollectionUtils.union(returnList, stepUrlList);
                    returnList = new ArrayList<>(unionList);
                }
            }
        }
        return returnList;
    }


    private void addUrlAndIdToList(String scenarioDefiniton, List<String> urlList, List<String> idList) {
        try {
            JSONObject scenarioObj = JSONObject.parseObject(scenarioDefiniton);
            if (scenarioObj.containsKey("hashTree")) {
                JSONArray hashArr = scenarioObj.getJSONArray("hashTree");
                for (int i = 0; i < hashArr.size(); i++) {
                    JSONObject elementObj = hashArr.getJSONObject(i);
                    if (elementObj.containsKey("id")) {
                        String id = elementObj.getString("id");
                        idList.add(id);
                    }
                    if (elementObj.containsKey("url")) {
                        String url = elementObj.getString("url");
                        urlList.add(url);
                    }
                    if (elementObj.containsKey("path")) {
                        String path = elementObj.getString("path");
                        urlList.add(path);
                    }
                }
            }
        } catch (Exception e) {
        }
    }


    public ScenarioEnv getApiScenarioProjectId(String id) {
        ApiScenarioWithBLOBs scenario = apiScenarioMapper.selectByPrimaryKey(id);
        ScenarioEnv scenarioEnv = new ScenarioEnv();
        if (scenario == null) {
            return scenarioEnv;
        }

        String definition = scenario.getScenarioDefinition();
        if (StringUtils.isBlank(definition)) {
            return scenarioEnv;
        }

        scenarioEnv = getApiScenarioEnv(definition);
        scenarioEnv.getProjectIds().remove(null);
        scenarioEnv.getProjectIds().add(scenario.getProjectId());
        return scenarioEnv;
    }

    public List<ScenarioIdProjectInfo> getApiScenarioProjectIdByConditions(ApiScenarioBatchRequest request) {
        List<ScenarioIdProjectInfo> returnList = new ArrayList<>();
        if (request.getIds() == null) {
            request.setIds(new ArrayList<>(0));
        }
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery(query));

        if (!request.getIds().isEmpty()) {
            ApiScenarioExample example = new ApiScenarioExample();
            example.createCriteria().andIdIn(request.getIds());
            List<ApiScenarioWithBLOBs> scenarioList = apiScenarioMapper.selectByExampleWithBLOBs(example);
            for (ApiScenarioWithBLOBs scenario : scenarioList) {
                ScenarioEnv scenarioEnv = new ScenarioEnv();
                if (scenario == null) {
                    continue;
                }
                String definition = scenario.getScenarioDefinition();
                if (StringUtils.isBlank(definition)) {
                    continue;
                }
                scenarioEnv = getApiScenarioEnv(definition);
                scenarioEnv.getProjectIds().add(scenario.getProjectId());
                ScenarioIdProjectInfo info = new ScenarioIdProjectInfo();

                info.setProjectIds(scenarioEnv.getProjectIds());
                info.setId(scenario.getId());
                returnList.add(info);
            }
        }
        return returnList;
    }

    public void updateCustomNumByProjectId(String id) {
        extApiScenarioMapper.updateCustomNumByProjectId(id);
    }

    public List<ApiScenarioWithBLOBs> listWithIds(ApiScenarioBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery(query));
        List<ApiScenarioWithBLOBs> list = extApiScenarioMapper.listWithIds(request.getIds());
        return list;
    }

    public String getLogDetails(String id) {
        ApiScenarioWithBLOBs bloBs = apiScenarioMapper.selectByPrimaryKey(id);
        if (bloBs != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(bloBs, AutomationReference.automationColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(id), bloBs.getProjectId(), bloBs.getName(), bloBs.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            ApiScenarioExample example = new ApiScenarioExample();
            example.createCriteria().andIdIn(ids);
            List<ApiScenario> definitions = apiScenarioMapper.selectByExample(example);
            List<String> names = definitions.stream().map(ApiScenario::getName).collect(Collectors.toList());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), definitions.get(0).getProjectId(), String.join(",", names), definitions.get(0).getCreateUser(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(ApiCaseRelevanceRequest request) {
        Map<String, List<String>> mapping = request.getMapping();
        Set<String> set = mapping.keySet();
        if (CollectionUtils.isNotEmpty(set)) {
            ApiScenarioExample example = new ApiScenarioExample();
            example.createCriteria().andIdIn(new ArrayList<>(set));
            List<ApiScenario> scenarios = apiScenarioMapper.selectByExample(example);
            List<String> names = scenarios.stream().map(ApiScenario::getName).collect(Collectors.toList());
            TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getPlanId());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(request.getSelectIds()), testPlan.getProjectId(), String.join(",", names), testPlan.getCreator(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public void checkApiScenarioUseUrl() {
        List<String> noUrlScenarioIdList = extApiScenarioMapper.selectIdsByUseUrlIsNull();
        for (String id : noUrlScenarioIdList) {
            ApiScenarioWithBLOBs scenario = apiScenarioMapper.selectByPrimaryKey(id);
            if (scenario.getUseUrl() == null) {
                List<ApiMethodUrlDTO> useUrl = this.parseUrl(scenario);
                if (useUrl != null) {
                    ApiScenarioWithBLOBs updateModel = new ApiScenarioWithBLOBs();
                    updateModel.setId(scenario.getId());
                    updateModel.setUseUrl(JSONArray.toJSONString(useUrl));
                    apiScenarioMapper.updateByPrimaryKeySelective(updateModel);
                    apiScenarioReferenceIdService.saveByApiScenario(updateModel);
                    updateModel = null;
                }
            }
            scenario = null;
        }
    }

    public void checkApiScenarioReferenceId() {
        List<ApiScenarioWithBLOBs> scenarioNoRefs = extApiScenarioMapper.selectByNoReferenceId();
        for (ApiScenarioWithBLOBs model : scenarioNoRefs) {
            apiScenarioReferenceIdService.saveByApiScenario(model);
        }
    }

    public List<JmxInfoDTO> batchGenPerformanceTestJmx(ApiScenarioBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery(query));
        List<JmxInfoDTO> returnList = new ArrayList<>();

        List<String> ids = request.getIds();
        List<ApiScenarioWithBLOBs> apiScenarioList = extApiScenarioMapper.selectIds(ids);
        if (CollectionUtils.isEmpty(apiScenarioList)) {
            return returnList;
        } else {
            apiScenarioList.forEach(item -> {
                String testName = item.getName();
                MsTestPlan testPlan = new MsTestPlan();
                testPlan.setHashTree(new LinkedList<>());
                JmxInfoDTO dto = apiTestService.updateJmxString(generateJmx(item), testName, true);
                String name = item.getName() + ".jmx";
                dto.setId(item.getId());
                dto.setName(name);
                returnList.add(dto);
            });
            return returnList;
        }
    }

    public BatchOperaResponse batchCopy(ApiScenarioBatchRequest batchRequest) {

        ServiceUtils.getSelectAllIds(batchRequest, batchRequest.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery(query));
        List<ApiScenarioWithBLOBs> apiScenarioList = extApiScenarioMapper.selectIds(batchRequest.getIds());
        StringBuffer stringBuffer = new StringBuffer();
        for (ApiScenarioWithBLOBs apiModel : apiScenarioList) {
            long time = System.currentTimeMillis();
            ApiScenarioWithBLOBs newModel = apiModel;
            newModel.setId(UUID.randomUUID().toString());
            newModel.setName("copy_" + apiModel.getName());
            newModel.setCreateTime(time);
            newModel.setUpdateTime(time);
            newModel.setNum(getNextNum(newModel.getProjectId()));

            ApiScenarioExample example = new ApiScenarioExample();
            example.createCriteria().andNameEqualTo(newModel.getName()).
                    andProjectIdEqualTo(newModel.getProjectId()).andStatusNotEqualTo("Trash").andIdNotEqualTo(newModel.getId());
            if (apiScenarioMapper.countByExample(example) > 0) {
                stringBuffer.append(newModel.getName() + ";");
                continue;
            } else {
                boolean insertFlag = true;
                if (StringUtils.isNotBlank(newModel.getCustomNum())) {
                    insertFlag = false;
                    String projectId = newModel.getProjectId();
                    Project project = projectMapper.selectByPrimaryKey(projectId);
                    if (project != null) {
                        Boolean customNum = project.getScenarioCustomNum();
                        // 未开启自定义ID
                        if (!customNum) {
                            insertFlag = true;
                            newModel.setCustomNum(null);
                        } else {
                            boolean isCustomNumExist = true;
                            try {
                                isCustomNumExist = this.isCustomNumExist(newModel);
                            } catch (Exception e) {
                            }
                            insertFlag = !isCustomNumExist;
                        }
                    }
                }

                if (insertFlag) {
                    apiScenarioMapper.insert(newModel);
                    apiScenarioReferenceIdService.saveByApiScenario(newModel);
                }
            }
        }

        BatchOperaResponse result = new BatchOperaResponse();
        if (stringBuffer.length() == 0) {
            result.result = true;
        } else {
            result.result = false;
            result.errorMsg = stringBuffer.substring(0, stringBuffer.length() - 1);
        }
        return result;
    }

    public DeleteCheckResult checkBeforeDelete(ApiScenarioBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery(query));
        List<String> deleteIds = request.getIds();
        DeleteCheckResult result = new DeleteCheckResult();
        List<String> checkMsgList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<ApiScenarioReferenceId> apiScenarioReferenceIdList = apiScenarioReferenceIdService.findByReferenceIdsAndRefType(deleteIds, MsTestElementConstants.REF.name());
            if (CollectionUtils.isNotEmpty(apiScenarioReferenceIdList)) {
                Map<String, List<String>> scenarioDic = new HashMap<>();
                apiScenarioReferenceIdList.forEach(item -> {
                    String refreceID = item.getReferenceId();
                    String scenarioId = item.getApiScenarioId();
                    if (scenarioDic.containsKey(refreceID)) {
                        scenarioDic.get(refreceID).add(scenarioId);
                    } else {
                        List<String> list = new ArrayList<>();
                        list.add(scenarioId);
                        scenarioDic.put(refreceID, list);
                    }
                });

                for (Map.Entry<String, List<String>> entry : scenarioDic.entrySet()) {
                    String refreceId = entry.getKey();
                    List<String> scenarioIdList = entry.getValue();
                    if (CollectionUtils.isNotEmpty(scenarioIdList)) {
                        String deleteScenarioName = extApiScenarioMapper.selectNameById(refreceId);
                        List<String> scenarioNames = extApiScenarioMapper.selectNameByIdIn(scenarioIdList);

                        if (StringUtils.isNotEmpty(deleteScenarioName) && CollectionUtils.isNotEmpty(scenarioNames)) {
                            String nameListStr = "【";
                            for (String name : scenarioNames) {
                                nameListStr += name + ",";
                            }
                            if (nameListStr.length() > 1) {
                                nameListStr = nameListStr.substring(0, nameListStr.length() - 1) + "】";
                            }
                            String msg = deleteScenarioName + " " + Translator.get("delete_check_reference_by") + ": " + nameListStr + " ";
                            checkMsgList.add(msg);
                        }
                    }
                }
            }
        }

        result.setDeleteFlag(checkMsgList.isEmpty());
        result.setCheckMsg(checkMsgList);
        return result;
    }

    public List<ApiScenario> getScenarioCaseByIds(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            ApiScenarioExample example = new ApiScenarioExample();
            example.createCriteria().andIdIn(ids);
            return apiScenarioMapper.selectByExample(example);
        }
        return new ArrayList<>();
    }

    public void initExecuteTimes() {
        List<String> apiScenarioIds = extApiScenarioMapper.selectIdsByExecuteTimeIsNull();
        Map<String, Long> scenarioIdMap = new HashMap<>();
        List<ApiReportCountDTO> reportCount = apiScenarioReportService.countByApiScenarioId();
        for (ApiReportCountDTO dto : reportCount) {
            scenarioIdMap.put(dto.getId(), dto.getCountNum());
        }
        for (String id : apiScenarioIds) {
            int count = 0;
            if (scenarioIdMap.containsKey(id)) {
                Long countNum = scenarioIdMap.get(id);
                if (countNum != null) {
                    count = countNum.intValue();
                }
            }
            ApiScenarioWithBLOBs apiScenario = new ApiScenarioWithBLOBs();
            apiScenario.setId(id);
            apiScenario.setExecuteTimes(count);
            apiScenarioMapper.updateByPrimaryKeySelective(apiScenario);
        }
    }

    public long countExecuteTimesByProjectID(String projectId) {
        Long result = extApiScenarioMapper.countExecuteTimesByProjectID(projectId);
        if (result == null) {
            return 0;
        } else {
            return result.longValue();
        }
    }

    public void initOrderField() {
        ServiceUtils.initOrderField(ApiScenarioWithBLOBs.class, ApiScenarioMapper.class,
                extApiScenarioMapper::selectProjectIds,
                extApiScenarioMapper::getIdsOrderByUpdateTime);
    }

    /**
     * 用例自定义排序
     *
     * @param request
     */
    public void updateOrder(ResetOrderRequest request) {
        ServiceUtils.updateOrderField(request, ApiScenarioWithBLOBs.class,
                apiScenarioMapper::selectByPrimaryKey,
                extApiScenarioMapper::getPreOrder,
                extApiScenarioMapper::getLastOrder,
                apiScenarioMapper::updateByPrimaryKeySelective);
    }

    public boolean checkScenarioEnv(ApiScenarioWithBLOBs request) {
        return this.checkScenarioEnv(request, null);
    }

    public boolean checkScenarioEnv(String scenarioId) {
        ApiScenarioWithBLOBs apiScenarioWithBLOBs = apiScenarioMapper.selectByPrimaryKey(scenarioId);
        this.setScenarioEnv(apiScenarioWithBLOBs);
        return this.checkScenarioEnv(apiScenarioWithBLOBs, null);
    }

    public List<String> getFollows(String scenarioId) {
        List<String> result = new ArrayList<>();
        if (StringUtils.isBlank(scenarioId)) {
            return result;
        }
        ApiScenarioFollowExample example = new ApiScenarioFollowExample();
        example.createCriteria().andScenarioIdEqualTo(scenarioId);
        List<ApiScenarioFollow> follows = apiScenarioFollowMapper.selectByExample(example);
        return follows.stream().map(ApiScenarioFollow::getFollowId).distinct().collect(Collectors.toList());
    }

    public void setScenarioEnvGroupIdNull(String envGroupId) {
        extApiScenarioMapper.setScenarioEnvGroupIdNull(envGroupId);
    }
}
