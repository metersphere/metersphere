package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.dto.*;
import io.metersphere.api.dto.automation.*;
import io.metersphere.api.dto.automation.parse.ApiScenarioImportUtil;
import io.metersphere.api.dto.automation.parse.ScenarioImport;
import io.metersphere.api.dto.automation.parse.ScenarioImportParserFactory;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.definition.ApiTestCaseInfo;
import io.metersphere.api.dto.definition.RunDefinitionRequest;
import io.metersphere.api.dto.definition.request.*;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.unknown.MsJmeterElement;
import io.metersphere.api.exec.scenario.ApiScenarioEnvService;
import io.metersphere.api.exec.scenario.ApiScenarioExecuteService;
import io.metersphere.api.exec.utils.GenerateHashTreeUtil;
import io.metersphere.api.mock.utils.MockApiUtils;
import io.metersphere.api.parse.ApiImportParser;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.controller.request.ResetOrderRequest;
import io.metersphere.controller.request.ScheduleRequest;
import io.metersphere.dto.ApiReportCountDTO;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.dto.ProjectConfig;
import io.metersphere.i18n.Translator;
import io.metersphere.job.sechedule.ApiScenarioTestJob;
import io.metersphere.job.sechedule.SwaggerUrlImportJob;
import io.metersphere.job.sechedule.TestPlanTestJob;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.api.AutomationReference;
import io.metersphere.log.vo.schedule.ScheduleReference;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.service.*;
import io.metersphere.track.dto.TestPlanDTO;
import io.metersphere.track.request.testcase.ApiCaseRelevanceRequest;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.track.request.testplan.FileOperationRequest;
import io.metersphere.track.service.TestCaseService;
import io.metersphere.track.service.TestPlanScenarioCaseService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.mybatis.spring.SqlSessionUtils;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
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

import static java.util.stream.Collectors.toList;

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
    private ScheduleMapper scheduleMapper;
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
    private TestCaseService testCaseService;
    @Resource
    private EsbApiParamService esbApiParamService;
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
    @Resource
    private MsHashTreeService hashTreeService;
    @Resource
    private ProjectApplicationService projectApplicationService;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;

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

    public List<ApiScenarioDTO> listAll(ApiScenarioBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery(query));
        List<ApiScenarioDTO> list = extApiScenarioMapper.selectIds(request.getIds());
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
        checkQuota(request.getProjectId());
        request.setId(UUID.randomUUID().toString());
        if (request.getScenarioDefinition() == null) {
            MsScenario msScenario = new MsScenario();
            msScenario.setHashTree(new LinkedList<>());
            request.setScenarioDefinition(msScenario);
        }
        request.setNewCreate(true);
        checkNameExist(request, false);
        int nextNum = getNextNum(request.getProjectId());
        if (StringUtils.isBlank(request.getCustomNum())) {
            request.setCustomNum(String.valueOf(nextNum));
        }
        checkScenarioNum(request);
        final ApiScenarioWithBLOBs scenario = buildSaveScenario(request);
        scenario.setVersion(0);

        scenario.setCreateTime(System.currentTimeMillis());
        scenario.setNum(nextNum);
        scenario.setOrder(ServiceUtils.getNextOrder(scenario.getProjectId(), extApiScenarioMapper::getLastOrder));
        scenario.setRefId(request.getId());
        scenario.setLatest(true);
        //检查场景的请求步骤。如果含有ESB请求步骤的话，要做参数计算处理。
        esbApiParamService.checkScenarioRequests(request);

        apiScenarioMapper.insert(scenario);
        apiScenarioReferenceIdService.saveApiAndScenarioRelation(scenario);
        // 存储依赖关系
        ApiAutomationRelationshipEdgeService relationshipEdgeService = CommonBeanFactory.getBean(ApiAutomationRelationshipEdgeService.class);
        if (relationshipEdgeService != null) {
            relationshipEdgeService.initRelationshipEdge(null, scenario);
        }
        uploadFiles(request, bodyFiles, scenarioFiles);

        return scenario;
    }

    private void checkQuota(String projectId) {
        QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
        if (quotaService != null) {
            quotaService.checkAPIAutomationQuota(projectId);
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

        ProjectConfig config = projectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.SCENARIO_CUSTOM_NUM.name());

        if (BooleanUtils.isTrue(config.getScenarioCustomNum())) {
            checkCustomNumExist(request);
        }
    }

    private void checkCustomNumExist(SaveApiScenarioRequest request) {
        ApiScenarioExample example = new ApiScenarioExample();
        String id = request.getId();
        ApiScenarioWithBLOBs apiScenarioWithBLOBs = apiScenarioMapper.selectByPrimaryKey(id);
        ApiScenarioExample.Criteria criteria = example.createCriteria();
        criteria.andCustomNumEqualTo(request.getCustomNum())
                .andProjectIdEqualTo(request.getProjectId())
                .andIdNotEqualTo(id);
        if (apiScenarioWithBLOBs != null && StringUtils.isNotBlank(apiScenarioWithBLOBs.getRefId())) {
            criteria.andRefIdNotEqualTo(apiScenarioWithBLOBs.getRefId());
        }
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
        checkNameExist(request, false);
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
        scenario.setCreateUser(null); // 更新时不更新创建人
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

        if (StringUtils.isNotBlank(request.getCustomNum()) && !StringUtils.equals(request.getCustomNum(), beforeScenario.getCustomNum())) {
            example.clear();
            example.createCriteria().andRefIdEqualTo(beforeScenario.getRefId());
            ApiScenarioWithBLOBs apiScenarioWithBLOBs = new ApiScenarioWithBLOBs();
            apiScenarioWithBLOBs.setCustomNum(request.getCustomNum());
            apiScenarioMapper.updateByExampleSelective(apiScenarioWithBLOBs, example);
        }

        apiScenarioReferenceIdService.saveApiAndScenarioRelation(scenario);
        extScheduleMapper.updateNameByResourceID(request.getId(), request.getName());//  修改场景name，同步到修改首页定时任务
        uploadFiles(request, bodyFiles, scenarioFiles);


        // 存储依赖关系
        ApiAutomationRelationshipEdgeService relationshipEdgeService = CommonBeanFactory.getBean(ApiAutomationRelationshipEdgeService.class);
        if (relationshipEdgeService != null) {
            relationshipEdgeService.initRelationshipEdge(beforeScenario, scenario);
        }
        checkAndSetLatestVersion(beforeScenario.getRefId());
        return scenario;
    }

    private void checkReferenceCase(ApiScenarioWithBLOBs scenario, ApiTestCaseMapper apiTestCaseMapper, ApiDefinitionMapper apiDefinitionMapper) {
        if (scenario == null || StringUtils.isEmpty(scenario.getScenarioDefinition())) {
            return;
        }
        JSONObject element = JSON.parseObject(scenario.getScenarioDefinition(), Feature.DisableSpecialKeyDetect);
        JSONArray hashTree = element.getJSONArray("hashTree");
        ApiScenarioImportUtil.formatHashTree(hashTree);
        setReferenced(hashTree, scenario.getVersionId(), scenario.getProjectId(), apiTestCaseMapper, apiDefinitionMapper, true);
        scenario.setScenarioDefinition(JSONObject.toJSONString(element));

    }

    private void checkAndSetLatestVersion(String refId) {
        extApiScenarioMapper.clearLatestVersion(refId);
        extApiScenarioMapper.addLatestVersion(refId);
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
            replenishScenarioModuleIdPath(request.getProjectId(), apiScenarioModuleMapper, scenario);
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
        List<String> ids = new ArrayList<>();
        ids.add(id);
        this.deleteBatch(ids);
    }

    private void deleteFollows(String id) {
        ApiScenarioFollowExample example = new ApiScenarioFollowExample();
        example.createCriteria().andScenarioIdEqualTo(id);
        apiScenarioFollowMapper.deleteByExample(example);
    }

    public void preDelete(String scenarioId, String scenarioDefinition) {
        //删除引用
        apiScenarioReferenceIdService.deleteByScenarioId(scenarioId);

        List<String> ids = new ArrayList<>();
        ids.add(scenarioId);
        deleteApiScenarioReport(ids);

        scheduleService.deleteByResourceId(scenarioId, ScheduleGroup.API_SCENARIO_TEST.name());
        TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
        example.createCriteria().andApiScenarioIdEqualTo(scenarioId);
        testPlanApiScenarioMapper.deleteByExample(example);
        // 删除引用关系
        relationshipEdgeService.delete(scenarioId);
        deleteBodyFile(scenarioDefinition);
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
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> ids = list.stream().map(ApiScenarioReport::getId).collect(Collectors.toList());
        APIReportBatchRequest reportRequest = new APIReportBatchRequest();
        reportRequest.setIds(ids);
        apiReportService.deleteAPIReportBatch(reportRequest);
    }

    public void deleteBatch(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        List<ApiScenarioWithBLOBs> apiScenarioWithBLOBs = extApiScenarioMapper.listWithRefIds(ids);
        List<String> scenarioIds = apiScenarioWithBLOBs.stream().map(ApiScenarioWithBLOBs::getId).collect(Collectors.toList());
        Map<String, String> scenarioIdDefinitionMap = apiScenarioWithBLOBs.stream().collect(Collectors.toMap(ApiScenarioWithBLOBs::getId, scenario -> scenario.getScenarioDefinition() == null ? " " : scenario.getScenarioDefinition()));
        preDelAndResource(scenarioIdDefinitionMap);
        testPlanScenarioCaseService.bathDeleteByScenarioIds(scenarioIds);
        testCaseService.deleteTestCaseTestByTestIds(ids);
        deleteScenarioByIds(scenarioIds);
    }

    public void removeToGc(List<String> apiIds) {
        for (String id : apiIds) {
            ApiScenarioWithBLOBs scenario = apiScenarioMapper.selectByPrimaryKey(id);
            if (scenario == null) {
                return;
            }
            List<String> scenarioIds = new ArrayList<>();
            if (StringUtils.isNotBlank(scenario.getRefId())) {
                ApiScenarioRequest request = new ApiScenarioRequest();
                request.setRefId(scenario.getRefId());
                scenarioIds = extApiScenarioMapper.selectIdsByQuery(request);
            } else {
                scenarioIds.add(scenario.getId());
            }
            //将这些场景的定时任务删除掉
            scenarioIds.forEach(scenarioId -> scheduleService.deleteByResourceId(scenarioId, ScheduleGroup.API_SCENARIO_TEST.name()));
            ApiScenarioExampleWithOperation example = new ApiScenarioExampleWithOperation();
            example.createCriteria().andRefIdEqualTo(scenario.getRefId());
            example.setOperator(SessionUtils.getUserId());
            example.setOperationTime(System.currentTimeMillis());
            extApiScenarioMapper.removeToGcByExample(example);
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
            ApiScenarioModuleService apiScenarioModuleService = CommonBeanFactory.getBean(ApiScenarioModuleService.class);
            for (ApiScenario api : scenarioList) {
                //检查是否同名
                SaveApiScenarioRequest apiScenarioRequest = new SaveApiScenarioRequest();
                apiScenarioRequest.setProjectId(api.getProjectId());
                apiScenarioRequest.setName(api.getName());
                apiScenarioRequest.setId(api.getId());
                apiScenarioRequest.setApiScenarioModuleId(api.getApiScenarioModuleId());
                apiScenarioRequest.setModulePath(api.getModulePath());
                apiScenarioRequest.setVersionId(extProjectVersionMapper.getDefaultVersion(request.getProjectId()));
                String moduleId = api.getApiScenarioModuleId();
                long nodeCount = apiScenarioModuleService.countById(moduleId);
                if (nodeCount <= 0) {
                    checkNameExist(apiScenarioRequest, true);
                } else {
                    checkNameExist(apiScenarioRequest, false);
                }
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

    private void checkNameExist(SaveApiScenarioRequest request, Boolean moduleIdNotExist) {
        if (StringUtils.isEmpty(request.getVersionId())) {
            request.setVersionId(extProjectVersionMapper.getDefaultVersion(request.getProjectId()));
        }

        ApiScenarioExample example = new ApiScenarioExample();
        ApiScenarioExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(request.getName())
                .andProjectIdEqualTo(request.getProjectId())
                .andStatusNotEqualTo("Trash")
                .andIdNotEqualTo(request.getId())
                .andApiScenarioModuleIdEqualTo(request.getApiScenarioModuleId());
        if (moduleIdNotExist) {
            criteria.andModulePathEqualTo(request.getModulePath());
        } else {
            criteria.andApiScenarioModuleIdEqualTo(request.getApiScenarioModuleId());
        }
        if (apiScenarioMapper.countByExample(example) > 0 && request.getNewCreate() != null && request.getNewCreate()) {
            MSException.throwException(Translator.get("automation_versions_create"));
        }
        criteria.andVersionIdEqualTo(request.getVersionId());
        if (apiScenarioMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("automation_name_already_exists") + " :" + Translator.get("api_definition_module") + request.getModulePath() + " ," + Translator.get("automation_name") + " :" + request.getName());
        }
        if (StringUtils.isNotBlank(request.getId())) {
            ApiScenarioWithBLOBs scenario = apiScenarioMapper.selectByPrimaryKey(request.getId());
            if (scenario != null) {
                example = new ApiScenarioExample();
                example.createCriteria().andRefIdEqualTo(scenario.getRefId()).andStatusNotEqualTo("Trash");
                List<ApiScenario> apiScenarios = apiScenarioMapper.selectByExample(example);
                if (apiScenarios != null && apiScenarios.size() > 1) {
                    if (!StringUtils.equals(scenario.getName(), request.getName())) {
                        MSException.throwException(Translator.get("automation_versions_update"));
                    }
                }
            }
        }
    }

    public ApiScenarioDTO getNewApiScenario(String id) {
        ApiScenarioDTO scenarioWithBLOBs = extApiScenarioMapper.selectById(id);
        if (scenarioWithBLOBs != null && StringUtils.isNotEmpty(scenarioWithBLOBs.getScenarioDefinition())) {
            JSONObject element = JSON.parseObject(scenarioWithBLOBs.getScenarioDefinition(), Feature.DisableSpecialKeyDetect);
            hashTreeService.dataFormatting(element);
            scenarioWithBLOBs.setScenarioDefinition(JSON.toJSONString(element));
        }
        return scenarioWithBLOBs;
    }

    private static JSONObject jsonMerge(JSONObject source, JSONObject target) {
        // 覆盖目标JSON为空，直接返回覆盖源
        if (target == null) {
            return source;
        }
        for (String key : source.keySet()) {
            Object value = source.get(key);
            if (!target.containsKey(key)) {
                target.put(key, value);
            } else {
                if (value instanceof JSONObject) {
                    JSONObject valueJson = (JSONObject) value;
                    JSONObject targetValue = jsonMerge(valueJson, target.getJSONObject(key));
                    target.put(key, targetValue);
                } else if (value instanceof JSONArray) {
                    JSONArray valueArray = (JSONArray) value;
                    for (int i = 0; i < valueArray.size(); i++) {
                        try {
                            JSONObject obj = (JSONObject) valueArray.get(i);
                            JSONObject targetValue = jsonMerge(obj, (JSONObject) target.getJSONArray(key).get(i));
                            target.getJSONArray(key).set(i, targetValue);
                        } catch (Exception e) {
                            LogUtil.error(e);
                        }
                    }
                } else {
                    target.put(key, value);
                }
            }
        }
        return target;
    }

    public String setDomain(ApiScenarioEnvRequest request) {
        Boolean enable = request.getEnvironmentEnable();
        String scenarioDefinition = request.getDefinition();
        JSONObject element = JSON.parseObject(scenarioDefinition, Feature.DisableSpecialKeyDetect);
        try {
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
                ApiScenarioDTO scenario = getNewApiScenario(scenarioId);
                if (scenario != null) {
                    String referenced = element.getString("referenced");
                    if (StringUtils.equalsIgnoreCase("REF", referenced)) {
                        JSONObject source = JSON.parseObject(scenario.getScenarioDefinition(), Feature.DisableSpecialKeyDetect);
                        element = jsonMerge(source, element);
                    }
                    element.put("referenced", referenced);
                    String environmentType = scenario.getEnvironmentType();
                    String environmentGroupId = scenario.getEnvironmentGroupId();
                    String environmentJson = scenario.getEnvironmentJson();
                    if (StringUtils.equals(environmentType, EnvironmentType.GROUP.name())) {
                        environmentMap = environmentGroupProjectService.getEnvMap(environmentGroupId);
                    } else if (StringUtils.equals(environmentType, EnvironmentType.JSON.name())) {
                        environmentMap = JSON.parseObject(environmentJson, Map.class);
                    }
                }
            }

            ParameterConfig config = new ParameterConfig();
            apiScenarioEnvService.setEnvConfig(environmentMap, config);
            if (config.getConfig() != null && !config.getConfig().isEmpty()) {
                ElementUtil.dataSetDomain(element.getJSONArray("hashTree"), config);
            }
            return JSON.toJSONString(element);
        } catch (Exception e) {
            return scenarioDefinition;
        }
    }


    public List<ApiScenarioDTO> getApiScenarios(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            return extApiScenarioMapper.selectIds(ids);
        }
        return new ArrayList<>();
    }

    public List<ApiScenarioDTO> getNewApiScenarios(List<String> ids) {
        List<ApiScenarioDTO> list = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            ids.forEach(item -> {
                ApiScenarioDTO dto = this.getNewApiScenario(item);
                list.add(dto);
            });
        }
        return list;
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
        config.getExcludeScenarioIds().add(apiScenario.getId());
        try {
            MsScenario scenario = JSONObject.parseObject(apiScenario.getScenarioDefinition(), MsScenario.class, Feature.DisableSpecialKeyDetect);
            if (scenario == null) {
                return null;
            }
            scenario.setId(apiScenario.getId());
            GenerateHashTreeUtil.parse(apiScenario.getScenarioDefinition(), scenario);
            String environmentType = apiScenario.getEnvironmentType();
            String environmentJson = apiScenario.getEnvironmentJson();
            String environmentGroupId = apiScenario.getEnvironmentGroupId();
            if (StringUtils.equals(environmentType, EnvironmentType.JSON.name()) && StringUtils.isNotBlank(environmentJson)) {
                scenario.setEnvironmentMap(JSON.parseObject(environmentJson, Map.class, Feature.DisableSpecialKeyDetect));
            } else if (StringUtils.equals(environmentType, EnvironmentType.GROUP.name()) && StringUtils.isNotBlank(environmentGroupId)) {
                Map<String, String> envMap = environmentGroupProjectService.getEnvMap(environmentGroupId);
                scenario.setEnvironmentMap(envMap);
            }
            // 针对导入的jmx 处理
            boolean isUseElement = false;
            if (CollectionUtils.isNotEmpty(scenario.getHashTree())) {
                for (MsTestElement testElement : scenario.getHashTree()) {
                    if (testElement instanceof MsJmeterElement) {
                        isUseElement = true;
                    }
                }
            }
            if (isUseElement) {
                scenario.toHashTree(jmeterHashTree, scenario.getHashTree(), config);
                ElementUtil.accuracyHashTree(jmeterHashTree);
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
            testPlan.toHashTree(jmeterHashTree, testPlan.getHashTree(), config);
        } catch (Exception ex) {
            LogUtil.error(ex);
            MSException.throwException(ex.getMessage());
        }
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

    public List<String> selectIdsByProjectId(String projectId) {
        return extApiScenarioMapper.selectIdsByProjectId(projectId);
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
        List<String> relevanceIds = request.getSelectIds();
        Collections.reverse(relevanceIds);
        String envType = request.getEnvironmentType();
        String envGroupId = request.getEnvGroupId();
        if (set.isEmpty()) {
            return;
        }
        Long nextOrder = ServiceUtils.getNextOrder(request.getPlanId(), extTestPlanScenarioCaseMapper::getLastOrder);
        for (String id : relevanceIds) {
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
        JobKey jobKey = null;
        TriggerKey triggerKey = null;
        Class clazz = null;
        scheduleService.editSchedule(request);
        jobKey = ApiScenarioTestJob.getJobKey(request.getResourceId());
        triggerKey = ApiScenarioTestJob.getTriggerKey(request.getResourceId());
        clazz = ApiScenarioTestJob.class;
        request.setJob(ApiScenarioTestJob.class.getName());
        this.addOrUpdateApiScenarioCronJob(request);
        scheduleService.resetJob(request, jobKey, triggerKey, clazz);
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
        List<String> ids = request.getIds();
        List<ApiScenarioDTO> apiScenarios = extApiScenarioMapper.selectIds(ids);
        String id = "";
        if (!apiScenarios.isEmpty()) {
            id = apiScenarios.get(0).getId();
        }
        if (CollectionUtils.isEmpty(apiScenarios)) {
            return null;
        }
        MsTestPlan testPlan = new MsTestPlan();
        testPlan.setHashTree(new LinkedList<>());
        ApiScenarioDTO scenario = apiScenarios.get(0);
        JmxInfoDTO dto = apiTestService.updateJmxString(generateJmx(scenario), scenario.getProjectId(), true);

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
        if (request != null && (request.getIds() != null || !request.getIds().isEmpty())) {
            request.getIds().forEach(apiId -> {
                ApiScenarioWithBLOBs scenario = apiScenarioMapper.selectByPrimaryKey(apiId);
                if (scenario == null) {
                    return;
                }
                //检查是否同名
                SaveApiScenarioRequest scenarioRequest = new SaveApiScenarioRequest();
                scenarioRequest.setProjectId(scenario.getProjectId());
                scenarioRequest.setName(scenario.getName());
                scenarioRequest.setId(scenario.getId());
                if (StringUtils.isEmpty(request.getApiScenarioModuleId())) {
                    scenarioRequest.setApiScenarioModuleId(scenario.getApiScenarioModuleId());
                } else {
                    scenarioRequest.setApiScenarioModuleId(request.getApiScenarioModuleId());
                }
                if (StringUtils.isEmpty(request.getModulePath())) {
                    scenarioRequest.setModulePath(scenario.getModulePath());
                } else {
                    scenarioRequest.setModulePath(request.getModulePath());
                }
                scenarioRequest.setModulePath(request.getModulePath());
                scenarioRequest.setVersionId(scenario.getVersionId());
                checkNameExist(scenarioRequest, false);
            });
        }
        apiScenarioMapper.updateByExampleSelective(
                apiScenarioWithBLOBs,
                apiScenarioExample);
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
                apiScenarioReferenceIdService.saveApiAndScenarioRelation(item);
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

    private void _importCreate(List<ApiScenarioWithBLOBs> sameRequest, ApiScenarioMapper batchMapper, ExtApiScenarioMapper extApiScenarioMapper,
                               ApiScenarioWithBLOBs scenarioWithBLOBs, ApiTestImportRequest apiTestImportRequest, ApiTestCaseMapper apiTestCaseMapper, ApiDefinitionMapper apiDefinitionMapper) {
        if (CollectionUtils.isEmpty(sameRequest)) {
            // 没有这个场景 新增
            scenarioWithBLOBs.setId(UUID.randomUUID().toString());
            scenarioWithBLOBs.setCreateTime(System.currentTimeMillis());
            if (scenarioWithBLOBs.getVersionId() != null && scenarioWithBLOBs.getVersionId().equals("update")) {
                if (StringUtils.isNotEmpty(apiTestImportRequest.getUpdateVersionId())) {
                    scenarioWithBLOBs.setVersionId(apiTestImportRequest.getUpdateVersionId());
                } else {
                    scenarioWithBLOBs.setVersionId(apiTestImportRequest.getDefaultVersion());
                }
                scenarioWithBLOBs.setLatest(true);
            } else {
                scenarioWithBLOBs.setRefId(scenarioWithBLOBs.getId());
                scenarioWithBLOBs.setLatest(true); // 新增接口 latest = true
                scenarioWithBLOBs.setOrder(getImportNextOrder(apiTestImportRequest.getProjectId()));
                if (StringUtils.isNotEmpty(apiTestImportRequest.getVersionId())) {
                    scenarioWithBLOBs.setVersionId(apiTestImportRequest.getVersionId());
                } else {
                    scenarioWithBLOBs.setVersionId(apiTestImportRequest.getDefaultVersion());
                }
            }

            checkReferenceCase(scenarioWithBLOBs, apiTestCaseMapper, apiDefinitionMapper);
            batchMapper.insert(scenarioWithBLOBs);
            apiScenarioReferenceIdService.saveApiAndScenarioRelation(scenarioWithBLOBs);
            extApiScenarioMapper.clearLatestVersion(scenarioWithBLOBs.getRefId());
            extApiScenarioMapper.addLatestVersion(scenarioWithBLOBs.getRefId());
        } else {
            //如果存在则修改
            if (StringUtils.isEmpty(apiTestImportRequest.getUpdateVersionId())) {
                apiTestImportRequest.setUpdateVersionId(apiTestImportRequest.getDefaultVersion());
            }
            Optional<ApiScenarioWithBLOBs> scenarioOp = sameRequest.stream()
                    .filter(api -> StringUtils.equals(api.getVersionId(), apiTestImportRequest.getUpdateVersionId()))
                    .findFirst();

            // 新增对应的版本
            if (!scenarioOp.isPresent()) {
                scenarioWithBLOBs.setId(UUID.randomUUID().toString());
                scenarioWithBLOBs.setCreateTime(System.currentTimeMillis());
                scenarioWithBLOBs.setVersionId(apiTestImportRequest.getUpdateVersionId());
                scenarioWithBLOBs.setLatest(apiTestImportRequest.getVersionId().equals(apiTestImportRequest.getDefaultVersion()));
                scenarioWithBLOBs.setRefId(sameRequest.get(0).getRefId() == null ? sameRequest.get(0).getId() : sameRequest.get(0).getRefId());
                scenarioWithBLOBs.setNum(sameRequest.get(0).getNum()); // 使用第一个num当作本次的num
                scenarioWithBLOBs.setOrder(sameRequest.get(0).getOrder());
                batchMapper.insert(scenarioWithBLOBs);
            } else {
                ApiScenarioWithBLOBs existScenario = scenarioOp.get();
                scenarioWithBLOBs.setId(existScenario.getId());
                scenarioWithBLOBs.setRefId(existScenario.getRefId());
                scenarioWithBLOBs.setVersionId(apiTestImportRequest.getUpdateVersionId());
                scenarioWithBLOBs.setOrder(existScenario.getOrder());
                scenarioWithBLOBs.setNum(existScenario.getNum());
                batchMapper.updateByPrimaryKeyWithBLOBs(scenarioWithBLOBs);
            }
            checkReferenceCase(scenarioWithBLOBs, apiTestCaseMapper, apiDefinitionMapper);
            apiScenarioReferenceIdService.saveApiAndScenarioRelation(scenarioWithBLOBs);
            extApiScenarioMapper.clearLatestVersion(scenarioWithBLOBs.getRefId());
            extApiScenarioMapper.addLatestVersion(scenarioWithBLOBs.getRefId());
        }
    }

    private ApiScenarioWithBLOBs importCreate(ApiScenarioWithBLOBs request, ApiScenarioMapper batchMapper, ExtApiScenarioMapper extApiScenarioMapper,
                                              ApiTestImportRequest apiTestImportRequest, List<ApiScenarioWithBLOBs> sameList, ApiTestCaseMapper apiTestCaseMapper, ApiDefinitionMapper apiDefinitionMapper) {
        final ApiScenarioWithBLOBs scenarioWithBLOBs = new ApiScenarioWithBLOBs();
        BeanUtils.copyBean(scenarioWithBLOBs, request);
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

        Boolean openCustomNum = apiTestImportRequest.getOpenCustomNum();
        List<ApiScenario> list = new ArrayList<>();
        if (BooleanUtils.isTrue(openCustomNum)) {
            ApiScenarioExample example = new ApiScenarioExample();
            ApiScenarioExample.Criteria criteria = example.createCriteria();
            if (CollectionUtils.isEmpty(sameList)) {
                criteria.andCustomNumEqualTo(scenarioWithBLOBs.getCustomNum())
                        .andProjectIdEqualTo(scenarioWithBLOBs.getProjectId());
            } else {
                if (StringUtils.equals("fullCoverage", apiTestImportRequest.getModeId())) {
                    criteria.andNameEqualTo(scenarioWithBLOBs.getName())
                            .andCustomNumEqualTo(scenarioWithBLOBs.getCustomNum())
                            .andProjectIdEqualTo(scenarioWithBLOBs.getProjectId())
                            .andIdNotEqualTo(sameList.get(0).getId());
                }

            }
            if (criteria.isValid()) {
                list = apiScenarioMapper.selectByExample(example);
            }
        }

        if (CollectionUtils.isNotEmpty(list)) {
            LogUtil.error("import scenario fail, custom num is exist: " + scenarioWithBLOBs.getCustomNum());
            MSException.throwException(Translator.get("import_fail_custom_num_exists") + ": " + scenarioWithBLOBs.getCustomNum());
        }

        if (StringUtils.equals("fullCoverage", apiTestImportRequest.getModeId())) {
            _importCreate(sameList, batchMapper, extApiScenarioMapper, scenarioWithBLOBs, apiTestImportRequest, apiTestCaseMapper, apiDefinitionMapper);
        } else if (StringUtils.equals("incrementalMerge", apiTestImportRequest.getModeId())) {
            scenarioWithBLOBs.setId(UUID.randomUUID().toString());
            scenarioWithBLOBs.setCreateTime(System.currentTimeMillis());
            if (CollectionUtils.isEmpty(sameList)) {
                if (scenarioWithBLOBs.getVersionId() != null && scenarioWithBLOBs.getVersionId().equals("new")) {
                    scenarioWithBLOBs.setLatest(apiTestImportRequest.getVersionId().equals(apiTestImportRequest.getDefaultVersion()));
                } else {
                    scenarioWithBLOBs.setOrder(getImportNextOrder(request.getProjectId()));
                    scenarioWithBLOBs.setRefId(scenarioWithBLOBs.getId());
                    scenarioWithBLOBs.setLatest(true);
                }
                if (StringUtils.isNotEmpty(apiTestImportRequest.getVersionId())) {
                    scenarioWithBLOBs.setVersionId(apiTestImportRequest.getVersionId());
                } else {
                    scenarioWithBLOBs.setVersionId(apiTestImportRequest.getDefaultVersion());
                }
                checkReferenceCase(scenarioWithBLOBs, apiTestCaseMapper, apiDefinitionMapper);
                if (scenarioWithBLOBs.getOrder() == null) {
                    scenarioWithBLOBs.setOrder(getImportNextOrder(request.getProjectId()));
                }
                if (scenarioWithBLOBs.getNum() == null) {
                    scenarioWithBLOBs.setNum(getNextNum(scenarioWithBLOBs.getProjectId()));
                }
                if (scenarioWithBLOBs.getRefId() == null) {
                    scenarioWithBLOBs.setRefId(scenarioWithBLOBs.getId());
                }
                batchMapper.insert(scenarioWithBLOBs);
                // 存储依赖关系
                ApiAutomationRelationshipEdgeService relationshipEdgeService = CommonBeanFactory.getBean(ApiAutomationRelationshipEdgeService.class);
                if (relationshipEdgeService != null) {
                    relationshipEdgeService.initRelationshipEdge(null, scenarioWithBLOBs);
                }
                apiScenarioReferenceIdService.saveApiAndScenarioRelation(scenarioWithBLOBs);
                extApiScenarioMapper.clearLatestVersion(scenarioWithBLOBs.getRefId());
                extApiScenarioMapper.addLatestVersion(scenarioWithBLOBs.getRefId());
            }

        } else {
            _importCreate(sameList, batchMapper, extApiScenarioMapper, scenarioWithBLOBs, apiTestImportRequest, apiTestCaseMapper, apiDefinitionMapper);
        }
        return scenarioWithBLOBs;
    }

    private void editScenario(ApiTestImportRequest request, ScenarioImport apiImport) {
        ApiScenarioModuleService apiScenarioModuleService = CommonBeanFactory.getBean(ApiScenarioModuleService.class);

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiScenarioMapper batchMapper = sqlSession.getMapper(ApiScenarioMapper.class);
        ExtApiScenarioMapper extApiScenarioMapper = sqlSession.getMapper(ExtApiScenarioMapper.class);
        ApiTestCaseMapper apiTestCaseMapper = sqlSession.getMapper(ApiTestCaseMapper.class);
        ApiDefinitionMapper apiDefinitionMapper = sqlSession.getMapper(ApiDefinitionMapper.class);
        ApiScenarioModuleMapper apiScenarioModuleMapper = sqlSession.getMapper(ApiScenarioModuleMapper.class);

        List<ApiScenarioWithBLOBs> initData = apiImport.getData();
        currentScenarioOrder.remove();
        String defaultVersion = extProjectVersionMapper.getDefaultVersion(request.getProjectId());
        request.setDefaultVersion(defaultVersion);
        if (request.getVersionId() == null) {
            request.setVersionId(defaultVersion);
        }

        UpdateScenarioModuleDTO updateScenarioModuleDTO = apiScenarioModuleService.checkScenarioModule(request, initData, StringUtils.equals("fullCoverage", request.getModeId()), request.getCoverModule());
        List<ApiScenarioModule> moduleList = updateScenarioModuleDTO.getModuleList();
        List<ApiScenarioWithBLOBs> data = updateScenarioModuleDTO.getApiScenarioWithBLOBsList();
        List<ApiScenarioWithBLOBs> needUpdateList = updateScenarioModuleDTO.getNeedUpdateList();

        if (moduleList != null) {
            for (ApiScenarioModule apiScenarioModule : moduleList) {
                apiScenarioModuleMapper.insert(apiScenarioModule);
            }
        }
        int num = 0;
        Project project;
        if (!CollectionUtils.isEmpty(data) && data.get(0) != null && data.get(0).getProjectId() != null) {
            project = projectMapper.selectByPrimaryKey(data.get(0).getProjectId());
            ProjectConfig config = projectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.SCENARIO_CUSTOM_NUM.name());
            num = getNextNum(data.get(0).getProjectId());
            request.setOpenCustomNum(config.getScenarioCustomNum());
        }

        for (int i = 0; i < data.size(); i++) {
            ApiScenarioWithBLOBs item = data.get(i);
            List<ApiScenarioWithBLOBs> sameList = needUpdateList.stream().filter(t -> t.getId().equals(item.getId())).collect(toList());
            if (StringUtils.isBlank(item.getCreateUser())) {
                item.setCreateUser(SessionUtils.getUserId());
            }
            if (item.getName().length() > 255) {
                item.setName(item.getName().substring(0, 255));
            }
            if (item.getVersionId() == null || (!item.getVersionId().equals("new") && !item.getVersionId().equals("update"))) {
                item.setNum(num);
            }
            if (StringUtils.isBlank(item.getLevel())) {
                item.setLevel("P0");
            }
            if (BooleanUtils.isFalse(request.getOpenCustomNum())) {
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
            item.setCreateUser(SessionUtils.getUserId());
            item.setUserId(SessionUtils.getUserId());
            item.setPrincipal(SessionUtils.getUserId());
            // 导入之后刷新latest
            importCreate(item, batchMapper, extApiScenarioMapper, request, sameList, apiTestCaseMapper, apiDefinitionMapper);
            if (i % 300 == 0) {
                sqlSession.flushStatements();
            }
        }

        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    private void replenishScenarioModuleIdPath(String request, ApiScenarioModuleMapper apiScenarioModuleMapper, ApiScenarioWithBLOBs item) {
        ApiScenarioModuleExample example = new ApiScenarioModuleExample();
        example.createCriteria().andProjectIdEqualTo(request).andNameEqualTo("未规划场景");
        List<ApiScenarioModule> modules = apiScenarioModuleMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(modules)) {
            item.setApiScenarioModuleId(modules.get(0).getId());
            item.setModulePath(modules.get(0).getName());
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


    private List<ApiScenarioWithBLOBs> getExportResult(ApiScenarioBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery(query));
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(request.getIds());
        List<ApiScenarioWithBLOBs> apiScenarioWithBLOBs = apiScenarioMapper.selectByExampleWithBLOBs(example);
        return apiScenarioWithBLOBs;
    }

    public ApiScenrioExportResult export(ApiScenarioBatchRequest request) {
        ApiScenrioExportResult result = new ApiScenrioExportResult();
        result.setData(getExportResult(request));
        result.setProjectId(request.getProjectId());
        result.setVersion(System.getenv("MS_VERSION"));
        if (CollectionUtils.isNotEmpty(result.getData())) {
            List<String> names = new ArrayList<>();
            List<String> ids = new ArrayList<>();
            checkDefinition(result, names, ids);
            request.setName(String.join(",", names));
            request.setId(JSON.toJSONString(ids));
        }
        return result;
    }

    public void checkDefinition(ApiScenrioExportResult result, List<String> names, List<String> ids) {
        for (ApiScenarioWithBLOBs scenario : result.getData()) {
            if (scenario == null || StringUtils.isEmpty(scenario.getScenarioDefinition())) {
                return;
            }
            JSONObject element = JSON.parseObject(scenario.getScenarioDefinition(), Feature.DisableSpecialKeyDetect);
            if (element != null) {
                JSONArray hashTree = element.getJSONArray("hashTree");
                ApiScenarioImportUtil.formatHashTree(hashTree);
                setHashTree(hashTree);
                scenario.setScenarioDefinition(JSONObject.toJSONString(element));
            }
            names.add(scenario.getName());
            ids.add(scenario.getId());
        }
    }

    public void setHashTree(JSONArray hashTree) {
        try {
            if (CollectionUtils.isNotEmpty(hashTree)) {
                for (int i = 0; i < hashTree.size(); i++) {
                    JSONObject object = (JSONObject) hashTree.get(i);
                    String referenced = object.getString("referenced");
                    if (StringUtils.isNotBlank(referenced) && StringUtils.equals(referenced, "REF")) {
                        // 检测引用对象是否存在，若果不存在则改成复制对象
                        String refType = object.getString("refType");
                        if (StringUtils.isNotEmpty(refType)) {
                            if (refType.equals("CASE")) {
                                if (CollectionUtils.isEmpty(object.getJSONArray("hashTree"))) {
                                    ApiTestCaseInfo model = extApiTestCaseMapper.selectApiCaseInfoByPrimaryKey(object.getString("id"));
                                    if (model != null) {
                                        JSONObject element = JSON.parseObject(model.getRequest(), Feature.DisableSpecialKeyDetect);
                                        object.put("hashTree", element.getJSONArray("hashTree"));
                                    }
                                }
                            }
                        }
                    }
                    if (StringUtils.isNotEmpty(object.getString("refType"))) {
                        if (CollectionUtils.isNotEmpty(object.getJSONArray("hashTree"))) {
                            setHashTree(object.getJSONArray("hashTree"));
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<ApiScenarioExportJmxDTO> exportJmx(ApiScenarioBatchRequest request) {
        List<ApiScenarioWithBLOBs> apiScenarioWithBLOBs = getExportResult(request);
        //检查运行环境
        checkExportEnv(apiScenarioWithBLOBs);
        // 生成jmx
        List<ApiScenarioExportJmxDTO> resList = new ArrayList<>();
        apiScenarioWithBLOBs.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getScenarioDefinition())) {
                String jmx = generateJmx(item);
                if (StringUtils.isNotEmpty(jmx)) {
                    ApiScenarioExportJmxDTO scenariosExportJmx = new ApiScenarioExportJmxDTO(item.getName(), apiTestService.updateJmxString(jmx, item.getProjectId(), false).getXml());
                    JmxInfoDTO dto = apiTestService.updateJmxString(jmx, item.getProjectId(), true);
                    scenariosExportJmx.setId(item.getId());
                    scenariosExportJmx.setVersion(item.getVersion());
                    //扫描需要哪些文件
                    scenariosExportJmx.setFileMetadataList(dto.getFileMetadataList());
                    resList.add(scenariosExportJmx);
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
        List<ApiScenarioWithBLOBs> scenarios = getExportResult(request);
        //环境检查
        checkExportEnv(scenarios);
        // 生成jmx
        Map<String, byte[]> files = new LinkedHashMap<>();
        scenarios.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getScenarioDefinition())) {
                String jmx = generateJmx(item);
                if (StringUtils.isNotEmpty(jmx)) {
                    ApiScenarioExportJmxDTO scenariosExportJmx = new ApiScenarioExportJmxDTO(item.getName(), apiTestService.updateJmxString(jmx, item.getProjectId(), false).getXml());
                    String fileName = item.getName() + ".jmx";
                    String jmxStr = scenariosExportJmx.getJmx();
                    files.put(fileName, jmxStr.getBytes(StandardCharsets.UTF_8));
                }
            }
        });
        if (CollectionUtils.isNotEmpty(scenarios)) {
            List<String> names = scenarios.stream().map(ApiScenarioWithBLOBs::getName).collect(Collectors.toList());
            request.setName(String.join(",", names));
            List<String> ids = scenarios.stream().map(ApiScenarioWithBLOBs::getId).collect(Collectors.toList());
            request.setId(JSON.toJSONString(ids));
        }
        return FileUtils.listBytesToZip(files);
    }

    private void checkExportEnv(List<ApiScenarioWithBLOBs> scenarios) {
        StringBuilder builder = new StringBuilder();
        for (ApiScenarioWithBLOBs apiScenarioWithBLOBs : scenarios) {
            try {
                boolean haveEnv = apiScenarioEnvService.checkScenarioEnv(apiScenarioWithBLOBs, null);
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
        List<String> ids = request.getIds();
        this.deleteBatch(ids);
    }

    private void deleteScenarioByIds(List<String> scenarioIds) {
        ApiScenarioExample apiScenarioExample = new ApiScenarioExample();
        apiScenarioExample.createCriteria().andIdIn(scenarioIds);
        apiScenarioMapper.deleteByExample(apiScenarioExample);
    }

    private void preDelAndResource(Map<String, String> scenarioIdDefinitionMap) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("PRE_DELETE：" + System.currentTimeMillis());
                scenarioIdDefinitionMap.forEach((scenarioId, scenarioDefinition) -> {
                    preDelete(scenarioId, scenarioDefinition);
                    scheduleService.deleteByResourceId(scenarioId, ScheduleGroup.API_SCENARIO_TEST.name());
                });
            }
        });
        thread.start();
    }

    /**
     * 统计接口覆盖率
     * 1.场景中复制的接口
     * 2.场景中引用/复制的案例
     * 3.场景中的自定义路径与接口定义中的匹配
     * <p>
     * 匹配场景中用到的路径
     *
     * @param scenarioUrlMap      场景使用到的url  key:method
     * @param allEffectiveApiList 接口集合（id / path 必须有数据）
     * @return
     */
    public float countInterfaceCoverage(Map<String, List<String>> scenarioUrlMap, List<ApiDefinition> allEffectiveApiList) {
        if (MapUtils.isEmpty(scenarioUrlMap) || CollectionUtils.isEmpty(allEffectiveApiList)) {
            return 0;
        }
        int containsCount = 0;
        for (ApiDefinition model : allEffectiveApiList) {
            List<String> scenarioUrlList = scenarioUrlMap.get(model.getMethod());
            boolean matchedUrl = MockApiUtils.isUrlInList(model.getPath(), scenarioUrlList);
            if (matchedUrl) {
                containsCount++;
            }
        }
        float coverageRageNumber = (float) containsCount * 100 / allEffectiveApiList.size();
        return coverageRageNumber;
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
        Schedule bloB = scheduleMapper.selectByPrimaryKey(id);
        if (bloB != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(bloB, ScheduleReference.scheduleColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(bloB.getId()), bloB.getProjectId(), bloB.getName(), bloB.getUserId(), columns);
            return JSON.toJSONString(details);
        }
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

    public String getLogDetails(ScheduleRequest request) {
        Schedule bloBs = scheduleService.getScheduleByResource(request.getResourceId(), request.getGroup());
        if (bloBs != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(bloBs, ScheduleReference.scheduleColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(bloBs.getId()), bloBs.getProjectId(), bloBs.getName(), bloBs.getUserId(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    @Async
    public void resetApiScenarioReferenceId() {
        LogUtil.info("Reset apiScenarioReferenceId is start.");
        List<ApiScenarioWithBLOBs> scenarios = extApiScenarioMapper.selectByStatusIsNotTrash();
        Map<String, List<ApiScenarioWithBLOBs>> scenariosGroupByProjectId =
                scenarios.stream().collect(Collectors.groupingBy(ApiScenarioWithBLOBs::getProjectId));
        for (Map.Entry<String, List<ApiScenarioWithBLOBs>> entry : scenariosGroupByProjectId.entrySet()) {
            String projectId = entry.getKey();
            List<ApiScenarioWithBLOBs> list = entry.getValue();
            try {
                apiScenarioReferenceIdService.saveApiAndScenarioRelation(list);
            } catch (Exception e) {
                LogUtil.error("Reset scenario reference id error. Project_id:" + projectId + "; error :" + e.getMessage());
            }
        }
        LogUtil.info("Reset apiScenarioReferenceId is end.");
    }

    public List<JmxInfoDTO> batchGenPerformanceTestJmx(ApiScenarioBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery(query));
        List<JmxInfoDTO> returnList = new ArrayList<>();

        List<String> ids = request.getIds();
        List<ApiScenarioDTO> apiScenarioList = extApiScenarioMapper.selectIds(ids);
        if (CollectionUtils.isEmpty(apiScenarioList)) {
            return returnList;
        } else {
            apiScenarioList.forEach(item -> {
                MsTestPlan testPlan = new MsTestPlan();
                testPlan.setHashTree(new LinkedList<>());
                JmxInfoDTO dto = apiTestService.updateJmxString(generateJmx(item), item.getProjectId(), true);
                String name = item.getName() + ".jmx";
                dto.setId(item.getId());
                dto.setName(name);
                returnList.add(dto);
            });
            return returnList;
        }
    }

    public void batchCopy(ApiScenarioBatchRequest request) {

        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery(query));
        List<String> ids = request.getIds();
        if (CollectionUtils.isEmpty(ids)) return;
        List<ApiScenarioDTO> apiScenarioList = extApiScenarioMapper.selectIds(ids);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiScenarioMapper mapper = sqlSession.getMapper(ApiScenarioMapper.class);
        Long nextOrder = ServiceUtils.getNextOrder(request.getProjectId(), extApiScenarioMapper::getLastOrder);
        int nextNum = getNextNum(request.getProjectId());

        try {
            for (int i = 0; i < apiScenarioList.size(); i++) {
                ApiScenarioWithBLOBs api = apiScenarioList.get(i);
                api.setId(UUID.randomUUID().toString());
                api.setName(ServiceUtils.getCopyName(api.getName()));
                api.setApiScenarioModuleId(request.getApiScenarioModuleId());
                api.setModulePath(request.getModulePath());
                api.setOrder(nextOrder += ServiceUtils.ORDER_STEP);
                api.setCreateTime(System.currentTimeMillis());
                api.setUpdateTime(System.currentTimeMillis());
                api.setRefId(api.getId());
                api.setNum(nextNum++);
                api.setCustomNum(String.valueOf(api.getNum()));
                mapper.insert(api);
                if (i % 50 == 0)
                    sqlSession.flushStatements();
            }
            sqlSession.flushStatements();
        } finally {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
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
                            String nameListStr = " ";
                            for (String name : scenarioNames) {
                                nameListStr += name + ",";
                            }
                            if (nameListStr.length() > 1) {
                                nameListStr = nameListStr.substring(0, nameListStr.length() - 1) + " ";
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
            example.createCriteria()
                    .andIdIn(ids)
                    .andStatusNotEqualTo(CommonConstants.TrashStatus);
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
        apiScenarioEnvService.setScenarioEnv(apiScenarioWithBLOBs, null);
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
        scheduleService.deleteByResourceId(refId, ScheduleGroup.API_SCENARIO_TEST.name());
        checkAndSetLatestVersion(refId);
    }

    public List<String> getProjects(RunScenarioRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery(query));

        List<String> ids = request.getIds();
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(ids);
        List<ApiScenarioWithBLOBs> apiScenarios = apiScenarioMapper.selectByExampleWithBLOBs(example);

        List<String> strings = new LinkedList<>();
        apiScenarios.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getScenarioDefinition())) {
                ScenarioEnv env = getApiScenarioEnv(item.getScenarioDefinition());
                if (!strings.contains(item.getProjectId())) {
                    strings.add(item.getProjectId());
                }
                if (env != null && CollectionUtils.isNotEmpty(env.getProjectIds())) {
                    env.getProjectIds().forEach(projectId -> {
                        if (!strings.contains(projectId)) {
                            strings.add(projectId);
                        }
                    });
                }
            }
        });
        return strings;
    }

    private void setReferenced(JSONArray hashTree, String versionId, String projectId, ApiTestCaseMapper apiTestCaseMapper, ApiDefinitionMapper apiDefinitionMapper, boolean isAdd) {
        // 将引用转成复制
        if (CollectionUtils.isNotEmpty(hashTree)) {
            Map<String, ApiDefinition> definitionMap = new HashMap<>();
            for (int i = 0; i < hashTree.size(); i++) {
                JSONObject object = (JSONObject) hashTree.get(i);
                String referenced = object.getString("referenced");
                if (StringUtils.isNotBlank(referenced) && StringUtils.equals(referenced, "REF")) {
                    // 检测引用对象是否存在，若果不存在则改成复制对象
                    String refType = object.getString("refType");
                    if (StringUtils.isNotEmpty(refType)) {
                        if (refType.equals("CASE") && isAdd) {
                            ApiScenarioImportUtil.checkCase(i, object, versionId, projectId, apiTestCaseMapper, apiDefinitionMapper, definitionMap);
                        } else {
                            checkAutomation(object);
                            object.put("projectId", projectId);
                        }
                    } else {
                        object.put("referenced", "Copy");
                    }
                } else {
                    object.put("projectId", projectId);
                    if (StringUtils.isEmpty(object.getString("url"))) {
                        object.put("isRefEnvironment", true);
                    }
                }
                JSONObject environmentMap = object.getJSONObject("environmentMap");
                if (environmentMap != null) {
                    object.put("environmentMap", new HashMap<>());
                }
                if (StringUtils.isNotEmpty(object.getString("refType")) && object.getString("refType").equals("CASE")) {
                    if (CollectionUtils.isNotEmpty(object.getJSONArray("hashTree"))) {
                        setReferenced(object.getJSONArray("hashTree"), versionId, projectId, apiTestCaseMapper, apiDefinitionMapper, true);
                    }
                } else {
                    if (CollectionUtils.isNotEmpty(object.getJSONArray("hashTree"))) {
                        setReferenced(object.getJSONArray("hashTree"), versionId, projectId, apiTestCaseMapper, apiDefinitionMapper, false);
                    }
                }

            }
        }
    }

    public void checkAutomation(JSONObject object) {
        ApiScenarioWithBLOBs bloBs = getDto(object.getString("id"));
        if (bloBs == null) {
            object.put("referenced", "Copy");
        } else {
            CheckPermissionService checkPermissionService = CommonBeanFactory.getBean(CheckPermissionService.class);
            Set<String> userRelatedProjectIds = checkPermissionService.getUserRelatedProjectIds();
            if (!userRelatedProjectIds.contains(bloBs.getProjectId())) {
                object.put("referenced", "Copy");
            } else {
                object.put("id", bloBs.getId());
                object.put("resourceId", bloBs.getId());
            }
        }
    }

    public Map<String, List<String>> selectScenarioUseUrlByProjectId(String projectId) {
        List<ApiScenarioReferenceId> list = apiScenarioReferenceIdService.selectUrlByProjectId(projectId);
        Map<String, List<String>> returnMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(item -> {
                if (returnMap.containsKey(item.getMethod())) {
                    returnMap.get(item.getMethod()).add(item.getUrl());
                } else {
                    List<String> urlList = new ArrayList<>();
                    urlList.add(item.getUrl());
                    returnMap.put(item.getMethod(), urlList);
                }
            });
        }
        return returnMap;
    }

    public Boolean checkIsSynchronize(ApiScenarioWithBLOBs existApiScenario, ApiScenarioWithBLOBs apiScenario) {

        ApiScenario exScenario;
        ApiScenario scenario;
        exScenario = existApiScenario;
        scenario = apiScenario;
        ObjectMapper objectMapper = new ObjectMapper();
        String exScenarioString = null;
        String scenarioString = null;
        try {
            exScenarioString = objectMapper.writeValueAsString(exScenario);
            scenarioString = objectMapper.writeValueAsString(scenario);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        //Compare the basic information of the APIScenario. 

        if (!StringUtils.equals(exScenario.getName(), scenario.getName())) {
            return true;
        }
        if (!StringUtils.equals(exScenarioString, scenarioString)) {
            return true;
        }
        if (!StringUtils.equals(existApiScenario.getEnvironmentJson(), apiScenario.getEnvironmentJson())) {
            return true;
        }

        if (!StringUtils.equals(existApiScenario.getDescription(), apiScenario.getDescription())) {
            return true;
        }

        return !StringUtils.equals(existApiScenario.getScenarioDefinition(), apiScenario.getScenarioDefinition());
    }
}
