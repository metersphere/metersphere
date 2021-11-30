package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import io.metersphere.api.exec.scenario.ApiScenarioEnvService;
import io.metersphere.api.exec.scenario.ApiScenarioExecuteService;
import io.metersphere.api.exec.utils.GenerateHashTreeUtil;
import io.metersphere.api.parse.ApiImportParser;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.MsTestElementConstants;
import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.constants.ScheduleType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.controller.request.ResetOrderRequest;
import io.metersphere.controller.request.ScheduleRequest;
import io.metersphere.dto.ApiReportCountDTO;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.job.sechedule.ApiScenarioTestJob;
import io.metersphere.job.sechedule.SwaggerUrlImportJob;
import io.metersphere.job.sechedule.TestPlanTestJob;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.api.AutomationReference;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.service.*;
import io.metersphere.track.dto.TestPlanDTO;
import io.metersphere.track.request.testcase.ApiCaseRelevanceRequest;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.track.request.testplan.FileOperationRequest;
import io.metersphere.track.service.TestPlanScenarioCaseService;
import org.apache.commons.collections.CollectionUtils;
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
    private ScheduleService scheduleService;
    @Resource
    private ApiScenarioReportService apiReportService;
    @Resource
    private ExtTestPlanMapper extTestPlanMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
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
    private ApiScenarioEnvService apiScenarioEnvService;
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
    private ExtTestPlanScenarioCaseMapper extTestPlanScenarioCaseMapper;
    @Resource
    private RelationshipEdgeService relationshipEdgeService;
    @Resource
    private ApiScenarioFollowMapper apiScenarioFollowMapper;
    @Resource
    private EnvironmentGroupProjectService environmentGroupProjectService;
    @Resource
    private ApiScenarioExecuteService apiScenarioExecuteService;
    @Resource
    private ExtProjectVersionMapper extProjectVersionMapper;

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
            apiScenarioEnvService.setApiScenarioEnv(list);
        }
        return list;
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
        scenario.setRefId(request.getId());
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

        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdEqualTo(scenario.getId()).andVersionIdEqualTo(request.getVersionId());
        if (apiScenarioMapper.updateByExampleSelective(scenario, example) == 0) {
            // 插入新版本的数据
            scenario.setId(UUID.randomUUID().toString());
            scenario.setVersionId(request.getVersionId());
            scenario.setCreateTime(System.currentTimeMillis());
            scenario.setUpdateTime(System.currentTimeMillis());
            scenario.setCreateUser(SessionUtils.getUserId());
            scenario.setOrder(beforeScenario.getOrder());
            scenario.setNum(beforeScenario.getNum());
            scenario.setRefId(beforeScenario.getRefId());
            apiScenarioMapper.insertSelective(scenario);
        }


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
        MsTestElement msTestElement = GenerateHashTreeUtil.parseScenarioDefinition(oldScenario.getScenarioDefinition());
        List<MsHTTPSamplerProxy> oldRequests = MsHTTPSamplerProxy.findHttpSampleFromHashTree(msTestElement);
        oldRequests.forEach(item -> {
            if (item.isCustomizeReq() && !newRequestIds.contains(item.getId())) {
                FileUtils.deleteBodyFiles(item.getId());
            }
        });
    }

    public Set<String> getRequestIds(String scenarioDefinition) {
        MsScenario msScenario = GenerateHashTreeUtil.parseScenarioDefinition(scenarioDefinition);
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
        if (StringUtils.isEmpty(request.getVersionId())) {
            scenario.setVersionId(extProjectVersionMapper.getDefaultVersion(request.getProjectId()));
        } else {
            scenario.setVersionId(request.getVersionId());
        }
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
        ApiScenarioWithBLOBs scenario = apiScenarioMapper.selectByPrimaryKey(id);
        if (scenario == null) {
            return;
        }
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andRefIdEqualTo(scenario.getRefId());
        List<ApiScenario> apiScenarios = apiScenarioMapper.selectByExample(example);
        apiScenarios.forEach(s -> {
            //及连删除外键表
            this.preDelete(s.getId());
            testPlanScenarioCaseService.deleteByScenarioId(s.getId());
            apiScenarioMapper.deleteByPrimaryKey(s.getId());
        });
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
        for (TestPlanApiScenario api : testPlanApiScenarioList) {
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

    public void deleteBodyFile(String scenarioDefinition) {
        MsTestElement msTestElement = GenerateHashTreeUtil.parseScenarioDefinition(scenarioDefinition);
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

    public void deleteBatch(List<String> ids) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(ids);
        List<ApiScenario> apiScenarios = apiScenarioMapper.selectByExample(example);
        apiScenarios.forEach(apiScenario -> this.delete(apiScenario.getId()));
    }

    public void removeToGc(List<String> apiIds) {
        for (String id : apiIds) {
            ApiScenarioWithBLOBs scenario = apiScenarioMapper.selectByPrimaryKey(id);
            if (scenario == null) {
                return;
            }
            ApiScenarioExampleWithOperation example = new ApiScenarioExampleWithOperation();
            example.createCriteria().andRefIdEqualTo(scenario.getRefId());
            example.setOperator(SessionUtils.getUserId());
            example.setOperationTime(System.currentTimeMillis());
            extApiScenarioMapper.removeToGcByExample(example);
            ApiScenarioRequest request = new ApiScenarioRequest();
            request.setRefId(scenario.getRefId());
            List<String> scenarioIds = extApiScenarioMapper.selectIdsByQuery(request);
            //将这些场景的定时任务删除掉
            scenarioIds.forEach(scenarioId -> scheduleService.deleteByResourceId(scenarioId, ScheduleGroup.API_SCENARIO_TEST.name()));
        }
    }

    public void reduction(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        ids.forEach(id -> {
            ApiScenarioWithBLOBs scenario = apiScenarioMapper.selectByPrimaryKey(id);
            if (scenario == null) {
                return;
            }
            ApiScenarioRequest request = new ApiScenarioRequest();
            request.setRefId(scenario.getRefId());
            HashMap<String, List<String>> filters = new HashMap<>();
            filters.put("status", Collections.singletonList(ScenarioStatus.Trash.name()));
            request.setFilters(filters);
            List<String> scenarioIds = extApiScenarioMapper.selectIdsByQuery(request);
            extApiScenarioMapper.checkOriginalStatusByIds(scenarioIds);
            //检查原来模块是否还在
            ApiScenarioExample example = new ApiScenarioExample();
            example.createCriteria().andIdIn(scenarioIds);
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
            extApiScenarioMapper.reduction(scenarioIds);
        });
    }

    private void checkNameExist(SaveApiScenarioRequest request) {
        if (StringUtils.isEmpty(request.getVersionId())) {
            request.setVersionId(extProjectVersionMapper.getDefaultVersion(request.getProjectId()));
        }

        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andNameEqualTo(request.getName())
                .andProjectIdEqualTo(request.getProjectId())
                .andStatusNotEqualTo("Trash")
                .andIdNotEqualTo(request.getId())
                .andVersionIdEqualTo(request.getVersionId());
        if (apiScenarioMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("automation_name_already_exists"));
        }
    }

    public ApiScenarioDTO getApiScenario(String id) {
        return extApiScenarioMapper.selectById(id);
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
        apiScenarioEnvService.setEnvConfig(environmentMap, config);
        if (config.getConfig() != null && !config.getConfig().isEmpty()) {
            ElementUtil.dataSetDomain(element.getJSONArray("hashTree"), config);
        }
        return JSON.toJSONString(element);
    }


    public List<ApiScenarioWithBLOBs> getApiScenarios(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            return extApiScenarioMapper.selectIds(ids);
        }
        return new ArrayList<>();
    }

    public byte[] loadFileAsBytes(FileOperationRequest fileOperationRequest) {
        if (fileOperationRequest.getId().contains("/") || fileOperationRequest.getName().contains("/"))
            MSException.throwException(Translator.get("invalid_parameter"));
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
            GenerateHashTreeUtil.parse(apiScenario.getScenarioDefinition(), scenario, apiScenario.getId(), null);
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


    /**
     * 场景测试执行
     *
     * @param request
     * @param bodyFiles
     * @return
     */
    public String debugRun(RunDefinitionRequest request, List<MultipartFile> bodyFiles, List<MultipartFile> scenarioFiles) {
        return apiScenarioExecuteService.debug(request, bodyFiles, scenarioFiles);
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
            nextOrder += ServiceUtils.ORDER_STEP;
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

    public JmxInfoDTO genPerformanceTestJmx(GenScenarioRequest request) {
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
        order = (order == null ? 0 : order) + ServiceUtils.ORDER_STEP;
        currentScenarioOrder.set(order);
        return order;
    }

    public ScenarioImport scenarioImport(MultipartFile file, ApiTestImportRequest request) {
        ApiImportParser runService = ScenarioImportParserFactory.getImportParser(request.getPlatform());
        ScenarioImport apiImport = null;
        Optional.ofNullable(file)
                .ifPresent(item -> request.setFileName(file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf("."))));
        try {
            apiImport = (ScenarioImport) Objects.requireNonNull(runService).parse(file == null ? null : file.getInputStream(), request);
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

        scenarioEnv = apiScenarioEnvService.getApiScenarioEnv(definition);
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
                scenarioEnv = apiScenarioEnvService.getApiScenarioEnv(definition);
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
                            String nameListStr = "[";
                            for (String name : scenarioNames) {
                                nameListStr += name + ",";
                            }
                            if (nameListStr.length() > 1) {
                                nameListStr = nameListStr.substring(0, nameListStr.length() - 1) + "]";
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
        return apiScenarioEnvService.checkScenarioEnv(request, null);
    }

    public boolean checkScenarioEnv(String scenarioId) {
        ApiScenarioWithBLOBs apiScenarioWithBLOBs = apiScenarioMapper.selectByPrimaryKey(scenarioId);
        apiScenarioEnvService.setScenarioEnv(apiScenarioWithBLOBs);
        return apiScenarioEnvService.checkScenarioEnv(apiScenarioWithBLOBs, null);
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

    public ScenarioEnv getApiScenarioEnv(String definition) {
        return apiScenarioEnvService.getApiScenarioEnv(definition);
    }

    public List<MsExecResponseDTO> run(RunScenarioRequest request) {
        return apiScenarioExecuteService.run(request);
    }

    public List<ApiScenarioDTO> getApiScenarioVersions(String scenarioId) {
        ApiScenarioWithBLOBs scenario = apiScenarioMapper.selectByPrimaryKey(scenarioId);
        if (scenario == null) {
            return new ArrayList<>();
        }
        ApiScenarioRequest request = new ApiScenarioRequest();
        request.setRefId(scenario.getRefId());
        return this.list(request);
    }

    public ApiScenarioDTO getApiScenarioByVersion(String refId, String versionId) {
        ApiScenarioRequest request = new ApiScenarioRequest();
        request.setRefId(refId);
        request.setVersionId(versionId);
        List<ApiScenarioDTO> list = this.list(request);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    public void deleteApiScenarioByVersion(String refId, String version) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andRefIdEqualTo(refId).andVersionIdEqualTo(version);
        apiScenarioMapper.deleteByExample(example);
    }
}
