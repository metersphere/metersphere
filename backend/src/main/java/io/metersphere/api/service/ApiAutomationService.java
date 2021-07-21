package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import io.metersphere.api.parse.ApiImportParser;
import io.metersphere.api.service.task.ParallelScenarioExecTask;
import io.metersphere.api.service.task.SerialScenarioExecTask;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.controller.request.ScheduleRequest;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.job.sechedule.ApiScenarioTestJob;
import io.metersphere.job.sechedule.SwaggerUrlImportJob;
import io.metersphere.job.sechedule.TestPlanTestJob;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.api.AutomationReference;
import io.metersphere.service.ScheduleService;
import io.metersphere.service.SystemParameterService;
import io.metersphere.track.dto.TestPlanDTO;
import io.metersphere.track.request.testcase.ApiCaseRelevanceRequest;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.track.request.testplan.FileOperationRequest;
import io.metersphere.track.service.TestPlanScenarioCaseService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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

    public ApiScenarioWithBLOBs getDto(String id) {
        return apiScenarioMapper.selectByPrimaryKey(id);
    }

    public ApiTestEnvironment get(String id) {
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
        return list;
    }

    public List<ApiScenarioWithBLOBs> listAll(ApiScenarioBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery((ApiScenarioRequest) query));
        List<ApiScenarioWithBLOBs> list = extApiScenarioMapper.selectIds(request.getIds());
        return list;
    }

    public int listAllTrash(ApiScenarioBatchRequest request) {
        return extApiScenarioMapper.selectTrash(request.getProjectId());
    }

    public List<String> idAll(ApiScenarioBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery((ApiScenarioRequest) query));
        return request.getIds();
    }

    public List<ApiScenarioDTO> listReview(ApiScenarioRequest request) {
        request = this.initRequest(request, true, true);
        List<ApiScenarioDTO> list = extApiScenarioMapper.listReview(request);
        return list;
    }

    private void setApiScenarioProjectIds(ApiScenarioDTO data) {
        // 如果场景步骤涉及多项目，则把涉及到的项目ID保存在projectIds属性
        List<String> idList = new ArrayList<>();
        String definition = data.getScenarioDefinition();
        if (StringUtils.isNotBlank(definition)) {
            RunDefinitionRequest d = JSON.parseObject(definition, RunDefinitionRequest.class);

            if (d != null) {
                Map<String, String> map = d.getEnvironmentMap();
                if (map != null) {
                    if (map.isEmpty()) {
                        try {
                            List<String> ids = (List<String>) JSONPath.read(definition, "$..projectId");
                            if (CollectionUtils.isNotEmpty(ids)) {
                                idList.addAll(new HashSet<>(ids));
                            }
                        } catch (Exception e) {
                            LogUtil.error("JSONPath.read projectId fail.");
                        }
                    } else {
                        Set<String> set = d.getEnvironmentMap().keySet();
                        idList = new ArrayList<>(set);
                    }
                } else {
                    // 兼容历史数据，无EnvironmentMap直接赋值场景所属项目
                    idList.add(data.getProjectId());
                }
            }

        }
        data.setProjectIds(idList);
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
            request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
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

        //检查场景的请求步骤。如果含有ESB请求步骤的话，要做参数计算处理。
        esbApiParamService.checkScenarioRequests(request);

        apiScenarioMapper.insert(scenario);
        apiScenarioReferenceIdService.saveByApiScenario(scenario);

        uploadFiles(request, bodyFiles, scenarioFiles);

        return scenario;
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

    public void update(SaveApiScenarioRequest request, List<MultipartFile> bodyFiles, List<MultipartFile> scenarioFiles) {
        checkNameExist(request);
        checkScenarioNum(request);

        //检查场景的请求步骤。如果含有ESB请求步骤的话，要做参数计算处理。
        esbApiParamService.checkScenarioRequests(request);
        //如果场景有TCP步骤的话，也要做参数计算处理
        tcpApiParamService.checkTestElement(request.getScenarioDefinition());

        final ApiScenarioWithBLOBs scenario = buildSaveScenario(request);

        Integer version = apiScenarioMapper.selectByPrimaryKey(request.getId()).getVersion();
        if (version == null) {
            scenario.setVersion(0);
        } else {
            scenario.setVersion(version + 1);
        }

        deleteUpdateBodyFile(scenario);
        List<ApiMethodUrlDTO> useUrl = this.parseUrl(scenario);
        scenario.setUseUrl(JSONArray.toJSONString(useUrl));
        apiScenarioMapper.updateByPrimaryKeySelective(scenario);
        apiScenarioReferenceIdService.saveByApiScenario(scenario);
        extScheduleMapper.updateNameByResourceID(request.getId(), request.getName());//  修改场景name，同步到修改首页定时任务
        uploadFiles(request, bodyFiles, scenarioFiles);
    }

    /**
     * 更新时如果有删除自定义请求，则删除对应body文件
     *
     * @param scenario
     */
    public void deleteUpdateBodyFile(ApiScenarioWithBLOBs scenario) {
        ApiScenarioWithBLOBs oldScenario = apiScenarioMapper.selectByPrimaryKey(scenario.getId());
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
        scenario.setFollowPeople(request.getFollowPeople());
        scenario.setPrincipal(request.getPrincipal());
        scenario.setStepTotal(request.getStepTotal());
        scenario.setUpdateTime(System.currentTimeMillis());
        scenario.setDescription(request.getDescription());
        scenario.setCreateUser(SessionUtils.getUserId());

        scenario.setScenarioDefinition(JSON.toJSONString(request.getScenarioDefinition()));
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
            example.createCriteria().andProjectIdEqualTo(request.getProjectId()).andNameEqualTo("默认模块");
            List<ApiScenarioModule> modules = apiScenarioModuleMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(modules)) {
                scenario.setApiScenarioModuleId(modules.get(0).getId());
                scenario.setModulePath(modules.get(0).getName());
            }
        }
        return scenario;
    }

    public void delete(String id) {
        //及连删除外键表
        this.preDelete(id);
        testPlanScenarioCaseService.deleteByScenarioId(id);
        apiScenarioMapper.deleteByPrimaryKey(id);
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

        deleteBodyFileByScenarioId(scenarioId);
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
        }
        if (!testPlanApiScenarioIdList.isEmpty()) {
            TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
            example.createCriteria().andIdIn(testPlanApiScenarioIdList);
            testPlanApiScenarioMapper.deleteByExample(example);
        }
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
        if(CollectionUtils.isNotEmpty(ids)){
            extApiScenarioMapper.checkOriginalStatusByIds(ids);
            //检查原来模块是否还在
            ApiScenarioExample example = new ApiScenarioExample();
            example.createCriteria().andIdIn(ids);
            List<ApiScenario> scenarioList = apiScenarioMapper.selectByExample(example);
            Map<String,List<ApiScenario>> nodeMap = scenarioList.stream().collect(Collectors.groupingBy(ApiScenario :: getApiScenarioModuleId));
            ApiScenarioModuleService apiScenarioModuleService = CommonBeanFactory.getBean(ApiScenarioModuleService.class);
            for(Map.Entry<String,List<ApiScenario>> entry : nodeMap.entrySet()){
                String nodeId = entry.getKey();
                List<ApiScenario> scenariosListItem = entry.getValue();
                Map<String,List<ApiScenario>> projectMap = scenariosListItem.stream().collect(Collectors.groupingBy(ApiScenario :: getProjectId));
                for(Map.Entry<String,List<ApiScenario>> projectEntry : projectMap.entrySet()){
                    String projectId = projectEntry.getKey();
                    List<ApiScenario> checkList = projectEntry.getValue();
                    if(StringUtils.isNotEmpty(projectId)){
                        long nodeCount = apiScenarioModuleService.countById(nodeId);
                        if(nodeCount <= 0){
                            ApiScenarioModule node = apiScenarioModuleService.getDefaultNode(projectId);
                            for (ApiScenario testCase: checkList) {
                                ApiScenarioWithBLOBs updateCase = new ApiScenarioWithBLOBs();
                                updateCase.setId(testCase.getId());
                                updateCase.setApiScenarioModuleId(node.getId());
                                updateCase.setModulePath("/"+node.getName());
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

    public LinkedList<MsTestElement> getScenarioHashTree(String definition) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JSONObject element = JSON.parseObject(definition);
        try {
            if (element != null) {
                return objectMapper.readValue(element.getString("hashTree"), new TypeReference<LinkedList<MsTestElement>>() {
                });
            }
        } catch (JsonProcessingException e) {
            LogUtil.error(e.getMessage(), e);
        }
        return new LinkedList<>();
    }

    public ScenarioEnv getApiScenarioEnv(String definition) {
        ObjectMapper mapper = new ObjectMapper();
        ScenarioEnv env = new ScenarioEnv();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
                            if (StringUtils.isBlank(httpSamplerProxy.getUrl()) || !tr.isURL(httpSamplerProxy.getUrl())) {
                                env.setFullUrl(false);
                                env.getProjectIds().add(httpSamplerProxy.getProjectId());
                            }
                        }
                    } else if (StringUtils.equals(tr.getType(), "JDBCSampler") || StringUtils.equals(tr.getType(), "TCPSampler")) {
                        env.getProjectIds().add(tr.getProjectId());
                    }
                }
                if (StringUtils.equals(tr.getType(), "scenario")) {
                    env.getProjectIds().add(tr.getProjectId());
                }
                if (CollectionUtils.isNotEmpty(tr.getHashTree())) {
                    getHashTree(tr.getHashTree(), env);
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
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
            LogUtil.error(ex.getMessage());
        }
        return null;
    }

    public APIScenarioReportResult createScenarioReport(String id, String scenarioId, String scenarioName, String triggerMode, String execType, String projectId, String userID, RunModeConfig config) {
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

        return report;
    }


    private void parse(String scenarioDefinition, MsScenario scenario) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            JSONObject element = JSON.parseObject(scenarioDefinition);
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
            e.printStackTrace();
            LogUtil.error(e.getMessage());
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
            this.preduceMsScenario(scenario);
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

    private void checkEnv(RunScenarioRequest request, List<ApiScenarioWithBLOBs> apiScenarios) {
        if (StringUtils.isNotBlank(request.getRunMode()) && StringUtils.equalsAny(request.getRunMode(), ApiRunMode.SCENARIO.name(), ApiRunMode.SCENARIO_PLAN.name())) {
            StringBuilder builder = new StringBuilder();
            for (ApiScenarioWithBLOBs apiScenarioWithBLOBs : apiScenarios) {
                try {
                    TestPlanApiScenario testPlanApiScenario = null;
                    if (request.getScenarioTestPlanIdMap() != null && request.getScenarioTestPlanIdMap().containsKey(apiScenarioWithBLOBs.getId())) {
                        testPlanApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(request.getScenarioTestPlanIdMap().get(apiScenarioWithBLOBs.getId()));
                    }
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

    /**
     * 场景测试执行
     *
     * @param request
     * @return
     */
    public String modeRun(RunScenarioRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery((ApiScenarioRequest) query));

        List<String> ids = request.getIds();
        // 生成集成报告
        String serialReportId = null;

        StringBuilder idStr = new StringBuilder();
        ids.forEach(item -> {
            idStr.append("\"").append(item).append("\"").append(",");
        });
        List<ApiScenarioWithBLOBs> apiScenarios = extApiScenarioMapper.selectByIds(idStr.toString().substring(0, idStr.toString().length() - 1), "\"" + StringUtils.join(ids, ",") + "\"");
        // 只有一个场景且没有测试步骤，则提示
        if (apiScenarios != null && apiScenarios.size() == 1 && (apiScenarios.get(0).getStepTotal() == null || apiScenarios.get(0).getStepTotal() == 0)) {
            MSException.throwException((apiScenarios.get(0).getName() + "，" + Translator.get("automation_exec_info")));
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
        String reportId = request.getId();
        Map<String, RunModeDataDTO> executeQueue = new LinkedHashMap<>();
        List<String> scenarioIds = new ArrayList<>();
        StringBuilder scenarioNames = new StringBuilder();
        // 按照场景执行
        for (ApiScenarioWithBLOBs item : apiScenarios) {
            if (item.getStepTotal() == null || item.getStepTotal() == 0) {
                continue;
            }
            APIScenarioReportResult report;
            Map<String, String> planEnvMap = new HashMap<>();
            //如果是测试计划页面触发的执行方式，生成报告时createScenarioReport第二个参数需要特殊处理
            if (StringUtils.equalsAny(request.getRunMode(), ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
                String testPlanScenarioId = item.getId();
                if (request.getScenarioTestPlanIdMap() != null && request.getScenarioTestPlanIdMap().containsKey(item.getId())) {
                    testPlanScenarioId = request.getScenarioTestPlanIdMap().get(item.getId());
                    // 获取场景用例单独的执行环境
                    TestPlanApiScenario planApiScenario = testPlanApiScenarioMapper.selectByPrimaryKey(testPlanScenarioId);
                    String environment = planApiScenario.getEnvironment();
                    if (StringUtils.isNotBlank(environment)) {
                        planEnvMap = JSON.parseObject(environment, Map.class);
                    }
                }
                if (request.isTestPlanScheduleJob()) {
                    String savedScenarioId = testPlanScenarioId + ":" + request.getTestPlanReportId();
                    report = createScenarioReport(reportId, savedScenarioId, item.getName(), request.getTriggerMode(),
                            request.getExecuteType(), item.getProjectId(), request.getReportUserID(), request.getConfig());
                } else {
                    report = createScenarioReport(reportId, testPlanScenarioId, item.getName(), request.getTriggerMode(),
                            request.getExecuteType(), item.getProjectId(), request.getReportUserID(), request.getConfig());
                }
            } else {
                report = createScenarioReport(reportId, ExecuteType.Marge.name().equals(request.getExecuteType()) ? serialReportId : item.getId(), item.getName(), request.getTriggerMode(),
                        request.getExecuteType(), item.getProjectId(), request.getReportUserID(), request.getConfig());
            }
            try {
                if (request.getConfig() != null && StringUtils.isNotBlank(request.getConfig().getResourcePoolId())) {
                    executeQueue.put(report.getId(), new RunModeDataDTO(item.getId(), report));
                } else {
                    // 生成报告和HashTree
                    HashTree hashTree = generateHashTree(item, reportId, planEnvMap);
                    executeQueue.put(report.getId(), new RunModeDataDTO(hashTree, report));
                }
                scenarioIds.add(item.getId());
                scenarioNames.append(item.getName()).append(",");
                // 重置报告ID
                reportId = UUID.randomUUID().toString();
            } catch (Exception ex) {
                MSException.throwException("解析运行步骤失败！场景名称：" + item.getName());
            }
        }

        if (request.getConfig() != null && StringUtils.equals(request.getConfig().getReportType(), RunModeConstants.SET_REPORT.toString()) && StringUtils.isNotEmpty(request.getConfig().getReportName())) {
            request.getConfig().setReportId(UUID.randomUUID().toString());
            if (CollectionUtils.isNotEmpty(scenarioIds) && scenarioIds.size() > 100) {
                scenarioIds = scenarioIds.subList(0, 100);
            }
            APIScenarioReportResult report = createScenarioReport(request.getConfig().getReportId(), JSON.toJSONString(scenarioIds), scenarioNames.deleteCharAt(scenarioNames.toString().length() - 1).toString(), ReportTriggerMode.MANUAL.name(),
                    ExecuteType.Saved.name(), request.getProjectId(), request.getReportUserID(), request.getConfig());
            report.setName(request.getConfig().getReportName());
            report.setId(serialReportId);
            apiScenarioReportMapper.insert(report);
            // 增加并行集合报告
            if (request.getConfig() != null && request.getConfig().getMode().equals(RunModeConstants.PARALLEL.toString())) {
                List<String> reportIds = executeQueue.entrySet().stream()
                        .map(reports -> reports.getKey())
                        .collect(Collectors.toList());
                ReportCounter counter = new ReportCounter();
                counter.setNumber(0);
                counter.setReportIds(reportIds);
                MessageCache.cache.put(serialReportId, counter);
            }
        }
        // 开始执行
        this.run(executeQueue, request, serialReportId);

        return request.getId();
    }

    private void run(Map<String, RunModeDataDTO> executeQueue, RunScenarioRequest request, String serialReportId) {
        // 开始选择执行模式
        if (executeQueue != null && executeQueue.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(executeQueue.size());
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            ApiScenarioReportMapper batchMapper = sqlSession.getMapper(ApiScenarioReportMapper.class);
            if (request.getConfig() != null && request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString())) {
                // 非集合报告，先生成执行队列
                if (StringUtils.isEmpty(serialReportId)) {
                    for (String reportId : executeQueue.keySet()) {
                        APIScenarioReportResult report = executeQueue.get(reportId).getReport();
                        report.setStatus(APITestStatus.Waiting.name());
                        batchMapper.insert(report);
                    }
                    sqlSession.flushStatements();
                }
                // 开始串行执行
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<String> reportIds = new LinkedList<>();
                        for (String key : executeQueue.keySet()) {
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
                                Future<ApiScenarioReport> future = executorService.submit(new SerialScenarioExecTask(jMeterService, apiScenarioReportMapper, executeQueue.get(key), request));
                                ApiScenarioReport scenarioReport = future.get();
                                // 如果开启失败结束执行，则判断返回结果状态
                                if (request.getConfig().isOnSampleError()) {
                                    if (scenarioReport == null || !scenarioReport.getStatus().equals("Success")) {
                                        reportIds.remove(key);
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                reportIds.remove(key);
                                LogUtil.error("执行终止：" + e.getMessage());
                                break;
                            }
                        }
                        // 清理未执行的队列
                        if (reportIds.size() < executeQueue.size()) {
                            List<String> removeList = executeQueue.entrySet().stream()
                                    .filter(map -> !reportIds.contains(map.getKey()))
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
                    }
                });
                thread.start();
            } else {
                // 开始并发执行
                for (String reportId : executeQueue.keySet()) {
                    //存储报告
                    APIScenarioReportResult report = executeQueue.get(reportId).getReport();
                    batchMapper.insert(report);
                    executorService.submit(new ParallelScenarioExecTask(jMeterService, executeQueue.get(reportId), request));
                }
                sqlSession.flushStatements();
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
                    // 只有一个场景且没有测试步骤，则提示
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
                MsScenario scenario = JSONObject.parseObject(item.getScenarioDefinition(), MsScenario.class);
                group.setOnSampleError(scenario.getOnSampleError());
                this.preduceMsScenario(scenario);
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
                            String environment = planApiScenario.getEnvironment();
                            if (StringUtils.isNotBlank(environment)) {
                                scenario.setEnvironmentMap(JSON.parseObject(environment, Map.class));
                            }
                        }
                        if (request.isTestPlanScheduleJob()) {
                            String savedScenarioId = testPlanScenarioId + ":" + request.getTestPlanReportId();
                            report = createScenarioReport(group.getName(), savedScenarioId, item.getName(), request.getTriggerMode(),
                                    request.getExecuteType(), item.getProjectId(), request.getReportUserID(), request.getConfig());
                        } else {
                            report = createScenarioReport(group.getName(), testPlanScenarioId, item.getName(), request.getTriggerMode() == null ? ReportTriggerMode.MANUAL.name() : request.getTriggerMode(),
                                    request.getExecuteType(), item.getProjectId(), request.getReportUserID(), request.getConfig());
                        }
                    } else {
                        report = createScenarioReport(group.getName(), item.getId(), item.getName(), request.getTriggerMode() == null ? ReportTriggerMode.MANUAL.name() : request.getTriggerMode(),
                                request.getExecuteType(), item.getProjectId(), request.getReportUserID(), request.getConfig());
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
        } catch (Exception ex) {
            MSException.throwException(ex.getMessage());
        }

        return jmeterHashTree;
    }

    private void preduceMsScenario(MsScenario scenario) {
        if (scenario.getHashTree() != null) {
            for (MsTestElement itemElement : scenario.getHashTree()) {
                if (itemElement instanceof MsScenario) {
                    itemElement.setId(UUID.randomUUID().toString());
                }
            }
        }
    }

    private boolean checkScenarioEnv(ApiScenarioWithBLOBs apiScenarioWithBLOBs, TestPlanApiScenario testPlanApiScenarios) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String definition = apiScenarioWithBLOBs.getScenarioDefinition();
        MsScenario scenario = JSONObject.parseObject(definition, MsScenario.class);
        boolean isEnv = true;
        Map<String, String> envMap = scenario.getEnvironmentMap();
        if (testPlanApiScenarios != null && StringUtils.isNotEmpty(testPlanApiScenarios.getEnvironment())) {
            envMap = JSON.parseObject(testPlanApiScenarios.getEnvironment(), Map.class);
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
    public String excute(RunScenarioRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery((ApiScenarioRequest) query));
        List<String> ids = request.getIds();
        //检查是否有正在执行中的情景
//        this.checkScenarioIsRunning(ids);
        StringBuilder idStr = new StringBuilder();
        ids.forEach(item -> {
            idStr.append("\"").append(item).append("\"").append(",");
        });
        List<ApiScenarioWithBLOBs> apiScenarios = extApiScenarioMapper.selectByIds(idStr.toString().substring(0, idStr.toString().length() - 1), "\"" + StringUtils.join(ids, ",") + "\"");

        String runMode = ApiRunMode.SCENARIO.name();
        if (StringUtils.isNotBlank(request.getRunMode()) && StringUtils.equals(request.getRunMode(), ApiRunMode.SCENARIO_PLAN.name())) {
            runMode = ApiRunMode.SCENARIO_PLAN.name();
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
            jMeterService.runLocal(JSON.toJSONString(reportIds), hashTree, request.getReportId(), runMode);
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
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
                return this.modeRun(request);
            } else {
                return this.modeRun(request);
            }
        } else {
            return this.excute(request);
        }
    }

//    public void checkScenarioIsRunning(List<String> ids) {
//        List<ApiScenarioReport> lastReportStatusByIds = apiReportService.selectLastReportByIds(ids);
//        for (ApiScenarioReport report : lastReportStatusByIds) {
//            if (StringUtils.equals(report.getStatus(), APITestStatus.Running.name())) {
//                MSException.throwException(report.getName() + " Is Running!");
//            }
//        }
//    }

    /**
     * 获取前台查询条件查询的所有(未经分页筛选)数据ID
     *
     * @param moduleIds   模块ID_前台查询时所选择的
     * @param name        搜索条件_名称_前台查询时所输入的
     * @param projectId   所属项目_前台查询时所在项目
     * @param filters     过滤集合__前台查询时的过滤条件
     * @param unSelectIds 未勾选ID_前台没有勾选的ID
     * @return
     */
    private List<String> getAllScenarioIdsByFontedSelect(List<String> moduleIds, String name, String projectId, Map<String, List<String>> filters, List<String> unSelectIds) {
        ApiScenarioRequest selectRequest = new ApiScenarioRequest();
        selectRequest.setModuleIds(moduleIds);
        selectRequest.setName(name);
        selectRequest.setProjectId(projectId);
        selectRequest.setFilters(filters);
        selectRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        List<ApiScenarioDTO> list = extApiScenarioMapper.list(selectRequest);
        List<String> allIds = list.stream().map(ApiScenarioDTO::getId).collect(Collectors.toList());
        List<String> ids = allIds.stream().filter(id -> !unSelectIds.contains(id)).collect(Collectors.toList());
        return ids;
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
        if (map != null) {
            map.keySet().forEach(id -> {
                ApiTestEnvironmentWithBLOBs environment = environmentService.get(map.get(id));
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
                SessionUtils.getUserId(), request.getConfig());
        apiScenarioReportMapper.insert(report);

        uploadBodyFiles(request.getBodyFileRequestIds(), bodyFiles);
        FileUtils.createBodyFiles(request.getScenarioFileIds(), scenarioFiles);

        // 调用执行方法
        jMeterService.runLocal(request.getId(), hashTree, request.getExecuteType(), ApiRunMode.SCENARIO.name());
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

        for (TestPlanDTO testPlan : list) {
            if (!set.isEmpty()) {
                set.forEach(id -> {
                    Map<String, String> newEnvMap = new HashMap<>(16);
                    if (envMap != null && !envMap.isEmpty()) {
                        List<String> lt = mapping.get(id);
                        lt.forEach(l -> {
                            newEnvMap.put(l, envMap.get(l));
                        });
                    }
                    TestPlanApiScenario testPlanApiScenario = new TestPlanApiScenario();
                    testPlanApiScenario.setId(UUID.randomUUID().toString());
                    testPlanApiScenario.setApiScenarioId(id);
                    testPlanApiScenario.setTestPlanId(testPlan.getId());
                    testPlanApiScenario.setCreateTime(System.currentTimeMillis());
                    testPlanApiScenario.setUpdateTime(System.currentTimeMillis());
                    testPlanApiScenario.setEnvironment(JSON.toJSONString(newEnvMap));
                    scenarioBatchMapper.insertIfNotExists(testPlanApiScenario);
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
            TestPlanApiScenario testPlanApiScenario = new TestPlanApiScenario();
            testPlanApiScenario.setId(UUID.randomUUID().toString());
            testPlanApiScenario.setCreateUser(SessionUtils.getUserId());
            testPlanApiScenario.setApiScenarioId(id);
            testPlanApiScenario.setTestPlanId(request.getPlanId());
            testPlanApiScenario.setCreateTime(System.currentTimeMillis());
            testPlanApiScenario.setUpdateTime(System.currentTimeMillis());
            testPlanApiScenario.setEnvironment(JSON.toJSONString(newEnvMap));
            testPlanApiScenarioMapper.insert(testPlanApiScenario);
        });
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
                (query) -> extApiScenarioMapper.selectIdsByQuery((ApiScenarioRequest) query));

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
        apiScenarioReferenceIdService.saveByApiScenario(apiScenarioWithBLOBs);
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

    public List<ApiScenarioWithBLOBs> getWithBLOBs(ApiScenarioWithBLOBs request) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId()).andStatusNotEqualTo("Trash").andIdNotEqualTo(request.getId());
        return apiScenarioMapper.selectByExampleWithBLOBs(example);
    }

    private void _importCreate(List<ApiScenarioWithBLOBs> sameRequest, ApiScenarioMapper batchMapper, ApiScenarioWithBLOBs scenarioWithBLOBs, ApiTestImportRequest apiTestImportRequest) {
        if (CollectionUtils.isEmpty(sameRequest)) {
            scenarioWithBLOBs.setId(UUID.randomUUID().toString());
            List<ApiMethodUrlDTO> useUrl = this.parseUrl(scenarioWithBLOBs);
            scenarioWithBLOBs.setUseUrl(JSONArray.toJSONString(useUrl));
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

        List<ApiScenarioWithBLOBs> sameRequest = getWithBLOBs(scenarioWithBLOBs);

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
                batchMapper.insert(scenarioWithBLOBs);
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
        int num = 0;
        Project project = new Project();
        if (!CollectionUtils.isEmpty(data) && data.get(0) != null && data.get(0).getProjectId() != null) {
            num = getNextNum(data.get(0).getProjectId());
            project = projectMapper.selectByPrimaryKey(data.get(0).getProjectId());
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
            importCreate(item, batchMapper, request);
            if (i % 300 == 0) {
                sqlSession.flushStatements();
            }
        }
        sqlSession.flushStatements();
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
                            ApiScenarioWithBLOBs bloBs = this.getDto(object.getString("id"));
                            if (bloBs != null) {
                                object = JSON.parseObject(bloBs.getScenarioDefinition());
                                hashTree.set(i, object);
                            }
                        }
                    } else if ("scenario".equals(object.getString("type"))) {
                        ApiScenarioWithBLOBs bloBs = this.getDto(object.getString("id"));
                        if (bloBs != null) {
                            object = JSON.parseObject(bloBs.getScenarioDefinition());
                            hashTree.set(i, object);
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(object.getJSONArray("hashTree"))) {
                    setHashTree(object.getJSONArray("hashTree"));
                }
            }
        }
    }

    private List<ApiScenarioWithBLOBs> getExportResult(ApiScenarioBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery((ApiScenarioRequest) query));
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(request.getIds());
        List<ApiScenarioWithBLOBs> apiScenarioWithBLOBs = apiScenarioMapper.selectByExampleWithBLOBs(example);
        // 处理引用数据
        if (CollectionUtils.isNotEmpty(apiScenarioWithBLOBs)) {
            apiScenarioWithBLOBs.forEach(item -> {
                if (StringUtils.isNotEmpty(item.getScenarioDefinition())) {
                    JSONObject scenario = JSONObject.parseObject(item.getScenarioDefinition());
                    JSONArray hashTree = scenario.getJSONArray("hashTree");
                    setHashTree(hashTree);
                    scenario.put("hashTree", hashTree);
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

    public void batchUpdateEnv(ApiScenarioBatchRequest request) {
        Map<String, String> envMap = request.getEnvMap();
        Map<String, List<String>> mapping = request.getMapping();
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
            if (!newEnvMap.isEmpty()) {
                ApiScenarioWithBLOBs scenario = apiScenarioMapper.selectByPrimaryKey(id);
                String definition = scenario.getScenarioDefinition();
                JSONObject object = JSON.parseObject(definition);
                if (object != null) {
                    object.put("environmentMap", newEnvMap);
                    String newDefinition = JSON.toJSONString(object);
                    scenario.setScenarioDefinition(newDefinition);
                    apiScenarioMapper.updateByPrimaryKeySelective(scenario);
                    apiScenarioReferenceIdService.saveByApiScenario(scenario);
                }
            }
        });
    }

    public void removeToGcByBatch(ApiScenarioBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery((ApiScenarioRequest) query));

        this.removeToGc(request.getIds());
    }

    public void deleteBatchByCondition(ApiScenarioBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery((ApiScenarioRequest) query));

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
        float intetfaceCoverage = 0;
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
            useUrl = null;
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
            String scenarioDefiniton = scenario.getScenarioDefinition();
            JSONObject scenarioObj = JSONObject.parseObject(scenarioDefiniton);
            List<ApiMethodUrlDTO> stepUrlList = this.getMethodUrlDTOByHashTreeJsonObj(scenarioObj);
            if (CollectionUtils.isNotEmpty(stepUrlList)) {
                Collection unionList = CollectionUtils.union(urlList, stepUrlList);
                urlList = new ArrayList<>(unionList);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                (query) -> extApiScenarioMapper.selectIdsByQuery((ApiScenarioRequest) query));

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
                (query) -> extApiScenarioMapper.selectIdsByQuery((ApiScenarioRequest) query));
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
                (query) -> extApiScenarioMapper.selectIdsByQuery((ApiScenarioRequest) query));
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

    public void batchCopy(ApiScenarioBatchRequest batchRequest) {

        ServiceUtils.getSelectAllIds(batchRequest, batchRequest.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery((ApiScenarioRequest) query));
        List<ApiScenarioWithBLOBs> apiScenarioList = extApiScenarioMapper.selectIds(batchRequest.getIds());
        for (ApiScenarioWithBLOBs apiModel : apiScenarioList) {
            ApiScenarioWithBLOBs newModel = apiModel;
            newModel.setId(UUID.randomUUID().toString());
            newModel.setName("copy_" + apiModel.getName());
            newModel.setCreateTime(System.currentTimeMillis());
            newModel.setNum(getNextNum(newModel.getProjectId()));

            ApiScenarioExample example = new ApiScenarioExample();
            example.createCriteria().andNameEqualTo(newModel.getName()).
                    andProjectIdEqualTo(newModel.getProjectId()).andStatusNotEqualTo("Trash").andIdNotEqualTo(newModel.getId());
            if (apiScenarioMapper.countByExample(example) > 0) {
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
    }

    public DeleteCheckResult checkBeforeDelete(ApiScenarioBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery((ApiScenarioRequest) query));
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
}
