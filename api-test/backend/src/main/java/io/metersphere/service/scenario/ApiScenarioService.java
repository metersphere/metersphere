package io.metersphere.service.scenario;

import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.*;
import io.metersphere.api.dto.automation.*;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.datacount.response.CoveredDTO;
import io.metersphere.api.dto.definition.ApiTestCaseInfo;
import io.metersphere.api.dto.definition.RunDefinitionRequest;
import io.metersphere.api.dto.definition.request.*;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.export.ScenarioToPerformanceInfoDTO;
import io.metersphere.api.dto.scenario.ApiScenarioParamDTO;
import io.metersphere.api.exec.scenario.ApiScenarioEnvService;
import io.metersphere.api.exec.scenario.ApiScenarioExecuteService;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.parse.ApiImportParser;
import io.metersphere.api.parse.scenario.ApiScenarioImportUtil;
import io.metersphere.api.parse.scenario.ScenarioImport;
import io.metersphere.api.parse.scenario.ScenarioImportParserFactory;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.base.mapper.plan.TestPlanApiScenarioMapper;
import io.metersphere.base.mapper.plan.ext.ExtTestPlanApiCaseMapper;
import io.metersphere.base.mapper.plan.ext.ExtTestPlanApiScenarioMapper;
import io.metersphere.base.mapper.plan.ext.ExtTestPlanScenarioCaseMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.commons.enums.ApiTestDataStatus;
import io.metersphere.commons.enums.ExecutionExecuteTypeEnum;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.commons.utils.mock.MockApiUtils;
import io.metersphere.dto.BaseCase;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.dto.ProjectConfig;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.environment.service.BaseEnvGroupProjectService;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.api.AutomationReference;
import io.metersphere.log.vo.schedule.ScheduleReference;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.quota.service.BaseQuotaService;
import io.metersphere.request.ResetOrderRequest;
import io.metersphere.sechedule.ApiScenarioTestJob;
import io.metersphere.sechedule.SwaggerUrlImportJob;
import io.metersphere.service.*;
import io.metersphere.service.definition.ApiTestCaseService;
import io.metersphere.service.definition.TcpApiParamService;
import io.metersphere.service.ext.ExtApiScheduleService;
import io.metersphere.service.ext.ExtFileAssociationService;
import io.metersphere.service.plan.TestPlanScenarioCaseService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mybatis.spring.SqlSessionUtils;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.TriggerBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioService {
    @Resource
    ApiScenarioModuleMapper apiScenarioModuleMapper;
    @Resource
    private ExtScheduleMapper extScheduleMapper;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ExtApiScheduleService scheduleService;
    @Resource
    private ScheduleMapper scheduleMapper;
    @Resource
    private ApiScenarioReportService apiReportService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ApiScenarioEnvService apiScenarioEnvService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private TcpApiParamService tcpApiParamService;
    @Resource
    private ApiScenarioReferenceIdService apiScenarioReferenceIdService;
    @Resource
    private ApiScenarioFollowMapper apiScenarioFollowMapper;
    @Resource
    private BaseEnvGroupProjectService environmentGroupProjectService;
    @Resource
    private ApiScenarioExecuteService apiScenarioExecuteService;
    @Resource
    private BaseProjectVersionMapper baseProjectVersionMapper;
    @Resource
    private MsHashTreeService hashTreeService;
    @Resource
    private BaseProjectApplicationService baseProjectApplicationService;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private ApiScenarioExecutionInfoService scenarioExecutionInfoService;
    @Resource
    private ExtFileAssociationService extFileAssociationService;
    @Resource
    private FileAssociationMapper fileAssociationMapper;
    @Resource
    private NoticeSendService noticeSendService;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;
    @Resource
    private RelationshipEdgeService relationshipEdgeService;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    private TestPlanScenarioCaseService testPlanScenarioCaseService;
    @Resource
    private ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    @Resource
    private ExtTestPlanScenarioCaseMapper extTestPlanScenarioCaseMapper;
    @Resource
    private JMeterService jMeterService;
    @Resource
    private ExtApiScenarioReferenceIdMapper extApiScenarioReferenceIdMapper;
    @Resource
    private ExtTestPlanApiScenarioMapper extTestPlanApiScenarioMapper;
    @Resource
    private BaseQuotaService baseQuotaService;
    @Resource
    private ApiAutomationRelationshipEdgeService apiAutomationRelationshipEdgeService;
    @Resource
    private ApiTestCaseService apiTestCaseService;

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

    public void buildApiCaseRelevanceRequest(ApiCaseRelevanceRequest request) {
        this.initRequest(request.getCondition(), true, true);
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extApiScenarioMapper.selectRelevanceIdsByQuery(query));
    }

    public List<ApiScenarioDTO> listAll(ApiScenarioBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extApiScenarioMapper.selectIdsByQuery(query));
        List<ApiScenarioDTO> list = extApiScenarioMapper.selectIds(request.getIds());
        return list;
    }

    public int listAllTrash(ApiScenarioBatchRequest request) {
        return extApiScenarioMapper.selectTrash(request.getProjectId());
    }

    public List<String> idAll(ApiScenarioBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extApiScenarioMapper.selectIdsByQuery(query));
        return request.getIds();
    }

    /**
     * 初始化部分参数
     *
     * @param request
     * @param setDefaultOrders
     * @param checkThisWeekData
     * @return
     */
    private ApiScenarioRequest initRequest(ApiScenarioRequest request, boolean setDefaultOrders, boolean checkThisWeekData) {
        if (setDefaultOrders) {
            request.setOrders(ServiceUtils.getDefaultSortOrder(request.getOrders()));
        }
        if (MapUtils.isNotEmpty(request.getFilters())
                && request.getFilters().containsKey(ApiTestConstants.LAST_RESULT)) {
            if (request.getFilters().get(ApiTestConstants.LAST_RESULT) != null && request.getFilters().get(ApiTestConstants.LAST_RESULT).contains(ApiReportStatus.PENDING.name())) {
                request.getFilters().get(ApiTestConstants.LAST_RESULT).add(StringUtils.EMPTY);
            }
            if (request.getFilters().get(ApiTestConstants.LAST_RESULT) != null && request.getFilters().get(ApiTestConstants.LAST_RESULT).contains(ApiTestConstants.FAKE_ERROR)) {
                request.getFilters().get(ApiTestConstants.LAST_RESULT).add(ApiReportStatus.FAKE_ERROR.name());
            }
        }
        if (StringUtils.isNotEmpty(request.getExecuteStatus())) {
            Map<String, List<String>> statusFilter = new HashMap<>();
            List<String> list = new ArrayList<>();
            list.add(ApiTestDataStatus.PREPARE.getValue());
            list.add(ApiTestDataStatus.UNDERWAY.getValue());
            list.add(ApiTestDataStatus.COMPLETED.getValue());
            statusFilter.put("status", list);
            request.setFilters(statusFilter);
        }
        if (checkThisWeekData) {
            if (request.isSelectThisWeedData()) {
                Map<String, Date> weekFirstTimeAndLastTime = DateUtils.getWeedFirstTimeAndLastTime(new Date());
                Date weekFirstTime = weekFirstTimeAndLastTime.get("firstTime");
                if (weekFirstTime != null) {
                    if (StringUtils.equalsIgnoreCase(request.getSelectDataType(), "SCHEDULE")) {
                        request.setScheduleCreateTime(weekFirstTime.getTime());
                    } else {
                        request.setCreateTime(weekFirstTime.getTime());
                    }
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

    public ApiScenarioWithBLOBs create(SaveApiScenarioRequest request, List<MultipartFile> bodyFiles, List<MultipartFile> scenarioFiles) {
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
        scenario.setLastResult(StringUtils.EMPTY);
        scenario.setCreateTime(System.currentTimeMillis());
        scenario.setNum(nextNum);
        scenario.setOrder(ServiceUtils.getNextOrder(scenario.getProjectId(), extApiScenarioMapper::getLastOrder));
        scenario.setRefId(request.getId());
        scenario.setLatest(true);

        apiScenarioMapper.insert(scenario);
        apiScenarioReferenceIdService.saveApiAndScenarioRelation(scenario);
        // 存储依赖关系
        apiAutomationRelationshipEdgeService.initRelationshipEdge(null, scenario);
        apiTestCaseService.checkAndSendReviewMessage(
                scenario.getId(),
                scenario.getName(),
                scenario.getProjectId(),
                "场景用例通知",
                NoticeConstants.TaskType.API_AUTOMATION_TASK,
                null,
                scenario.getScenarioDefinition(),
                scenario.getPrincipal()
        );

        uploadFiles(request, bodyFiles, scenarioFiles);
        return scenario;
    }

    private void checkQuota(String projectId) {
        baseQuotaService.checkAPIAutomationQuota(projectId);
    }

    private void uploadFiles(SaveApiScenarioRequest request, List<MultipartFile> bodyFiles, List<MultipartFile> scenarioFiles) {
        ApiFileUtil.createBodyFiles(request.getScenarioFileIds(), scenarioFiles);
        List<String> bodyFileRequestIds = request.getBodyFileRequestIds();
        if (CollectionUtils.isNotEmpty(bodyFileRequestIds)) {
            bodyFileRequestIds.forEach(requestId -> {
                ApiFileUtil.createBodyFiles(requestId, bodyFiles);
            });
        }
    }

    private void checkScenarioNum(SaveApiScenarioRequest request) {
        String projectId = request.getProjectId();
        Project project = projectMapper.selectByPrimaryKey(projectId);

        if (project == null) {
            MSException.throwException("add scenario fail, project is not find.");
        }

        ProjectConfig config = baseProjectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.SCENARIO_CUSTOM_NUM.name());

        if (BooleanUtils.isTrue(config.getScenarioCustomNum())) {
            checkCustomNumExist(request);
        }
    }

    private void checkCustomNumExist(SaveApiScenarioRequest request) {
        ApiScenarioExample example = new ApiScenarioExample();
        String id = request.getId();
        ApiScenarioWithBLOBs apiScenarioWithBLOBs = apiScenarioMapper.selectByPrimaryKey(id);
        ApiScenarioExample.Criteria criteria = example.createCriteria();
        criteria.andCustomNumEqualTo(request.getCustomNum()).andProjectIdEqualTo(request.getProjectId()).andIdNotEqualTo(id);
        if (apiScenarioWithBLOBs != null && StringUtils.isNotBlank(apiScenarioWithBLOBs.getRefId())) {
            criteria.andRefIdNotEqualTo(apiScenarioWithBLOBs.getRefId());
        }
        List<ApiScenario> list = apiScenarioMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(list)) {
            MSException.throwException("自定义ID " + request.getCustomNum() + " 已存在！");
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

    public ApiScenarioWithBLOBs update(SaveApiScenarioRequest request, List<MultipartFile> bodyFiles, List<MultipartFile> scenarioFiles) {
        checkNameExist(request, false);
        checkScenarioNum(request);
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
        apiAutomationRelationshipEdgeService.initRelationshipEdge(beforeScenario, scenario);

        apiTestCaseService.checkAndSendReviewMessage(
                scenario.getId(),
                scenario.getName(),
                scenario.getProjectId(),
                "场景用例通知",
                NoticeConstants.TaskType.API_AUTOMATION_TASK,
                beforeScenario.getScenarioDefinition(),
                scenario.getScenarioDefinition(),
                scenario.getPrincipal()
        );

        String defaultVersion = baseProjectVersionMapper.getDefaultVersion(request.getProjectId());
        if (StringUtils.equalsIgnoreCase(request.getVersionId(), defaultVersion)) {
            checkAndSetLatestVersion(beforeScenario.getRefId());
        }
        //同步修改所有版本的模块路径
        updateOtherVersionModule(beforeScenario.getRefId(), scenario);
        // 存储附件关系
        extFileAssociationService.saveScenario(scenario.getId(), request.getScenarioDefinition());
        return scenario;
    }

    private void updateOtherVersionModule(String refId, ApiScenarioWithBLOBs scenario) {
        extApiScenarioMapper.updateVersionModule(refId, scenario.getVersionId(), scenario.getApiScenarioModuleId(), scenario.getModulePath());
    }

    private void checkReferenceCase(ApiScenarioWithBLOBs scenario, ApiScenarioParamDTO apiScenarioParamDto) {
        if (scenario == null || StringUtils.isEmpty(scenario.getScenarioDefinition())) {
            return;
        }
        JSONObject element = JSONUtil.parseObject(scenario.getScenarioDefinition());
        JSONArray hashTree = element.optJSONArray(ElementConstants.HASH_TREE);
        ApiScenarioImportUtil.formatHashTree(hashTree);
        setReferenced(hashTree, scenario.getProjectId(), scenario.getVersionId(), apiScenarioParamDto);
        scenario.setScenarioDefinition(element.toString());
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
        try {
            Set<String> newRequestIds = getRequestIds(scenario.getScenarioDefinition());
            MsTestElement msTestElement = GenerateHashTreeUtil.parseScenarioDefinition(oldScenario.getScenarioDefinition());
            List<MsHTTPSamplerProxy> oldRequests = MsHTTPSamplerProxy.findHttpSampleFromHashTree(msTestElement);
            oldRequests.forEach(item -> {
                if (item.isCustomizeReq() && !newRequestIds.contains(item.getId())) {
                    ApiFileUtil.deleteBodyFiles(item.getId());
                }
            });
        } catch (Exception e) {
            LogUtil.error("Historical data processing exception");
        }
    }

    public Set<String> getRequestIds(String scenarioDefinition) {
        MsScenario msScenario = GenerateHashTreeUtil.parseScenarioDefinition(scenarioDefinition);
        List<MsHTTPSamplerProxy> httpSampleFromHashTree = MsHTTPSamplerProxy.findHttpSampleFromHashTree(msScenario);
        return httpSampleFromHashTree.stream().map(MsHTTPSamplerProxy::getId).collect(Collectors.toSet());
    }

    public ApiScenarioWithBLOBs buildSaveScenario(SaveApiScenarioRequest request) {
        ApiScenarioWithBLOBs scenario = new ApiScenarioWithBLOBs();
        scenario.setId(request.getId());
        scenario.setName(request.getName());
        scenario.setProjectId(request.getProjectId());
        scenario.setCustomNum(request.getCustomNum());
        if (StringUtils.equals(request.getTags(), "[]")) {
            scenario.setTags(StringUtils.EMPTY);
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
        request.getScenarioDefinition().setId(scenario.getId());
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
            scenario.setStatus(ApiTestDataStatus.UNDERWAY.getValue());
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
            scenario.setVersionId(baseProjectVersionMapper.getDefaultVersion(request.getProjectId()));
        } else {
            scenario.setVersionId(request.getVersionId());
        }
        // 存储附件关系
        extFileAssociationService.saveScenario(scenario.getId(), request.getScenarioDefinition());
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
        // 删除引用关系
        relationshipEdgeService.delete(scenarioId);
        deleteBodyFile(scenarioDefinition);
        deleteFollows(scenarioId);
    }

    public void deleteBodyFile(String scenarioDefinition) {
        MsTestElement msTestElement = GenerateHashTreeUtil.parseScenarioDefinition(scenarioDefinition);
        List<MsHTTPSamplerProxy> httpSampleFromHashTree = MsHTTPSamplerProxy.findHttpSampleFromHashTree(msTestElement);
        httpSampleFromHashTree.forEach((httpSamplerProxy) -> {
            if (httpSamplerProxy.isCustomizeReq()) {
                ApiFileUtil.deleteBodyFiles(httpSamplerProxy.getId());
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
        ApiReportBatchRequest reportRequest = new ApiReportBatchRequest();
        reportRequest.setIds(ids);
        reportRequest.setCaseType(ReportTypeConstants.SCENARIO.name());
        apiReportService.deleteAPIReportBatch(reportRequest);
    }

    public void deleteBatch(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        List<ApiScenarioWithBLOBs> apiScenarioWithBLOBs = extApiScenarioMapper.listWithRefIds(ids);
        List<String> scenarioIds = apiScenarioWithBLOBs.stream().map(ApiScenarioWithBLOBs::getId).collect(Collectors.toList());
        Map<String, String> scenarioIdDefinitionMap = apiScenarioWithBLOBs.stream().collect(Collectors.toMap(ApiScenarioWithBLOBs::getId, scenario -> scenario.getScenarioDefinition() == null ? StringUtils.SPACE : scenario.getScenarioDefinition()));
        preDelAndResource(scenarioIdDefinitionMap);

        TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
        example.createCriteria().andApiScenarioIdIn(scenarioIds);
        testPlanApiScenarioMapper.deleteByExample(example);

        FileAssociationExample associationExample = new FileAssociationExample();
        associationExample.createCriteria().andSourceIdIn(ids);
        fileAssociationMapper.deleteByExample(associationExample);
        deleteScenarioByIds(scenarioIds);
    }

    public void removeToGc(List<String> apiIds) {
        if (CollectionUtils.isEmpty(apiIds)) {
            return;
        }
        ApiScenarioExample apiScenarioExample = new ApiScenarioExample();
        apiScenarioExample.createCriteria().andIdIn(apiIds);
        List<ApiScenario> apiScenarios = apiScenarioMapper.selectByExample(apiScenarioExample);
        if (CollectionUtils.isEmpty(apiScenarios)) {
            return;
        }

        List<String> refIds = apiScenarios.stream().map(ApiScenario::getRefId).collect(toList());

        apiScenarioExample = new ApiScenarioExample();
        apiScenarioExample.createCriteria().andRefIdIn(refIds);
        List<ApiScenario> apiScenarioVersions = apiScenarioMapper.selectByExample(apiScenarioExample);
        if (CollectionUtils.isEmpty(apiScenarioVersions)) {
            return;
        }
        List<String> scenarioIds = apiScenarioVersions.stream().map(ApiScenario::getId).collect(toList());
        scenarioIds.forEach(scenarioId -> scheduleService.closeByResourceId(scenarioId, ScheduleGroup.API_SCENARIO_TEST.name()));

        ApiScenarioExampleWithOperation example = new ApiScenarioExampleWithOperation();
        example.createCriteria().andRefIdIn(refIds);
        example.setOperator(SessionUtils.getUserId());
        example.setOperationTime(System.currentTimeMillis());
        extApiScenarioMapper.removeToGcByExample(example);

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
            filters.put("status", Collections.singletonList(ApiTestDataStatus.TRASH.getValue()));
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
                apiScenarioRequest.setVersionId(baseProjectVersionMapper.getDefaultVersion(request.getProjectId()));
                String moduleId = api.getApiScenarioModuleId();
                long nodeCount = apiScenarioModuleService.countById(moduleId);
                if (nodeCount <= 0) {
                    checkNameExist(apiScenarioRequest, true);
                } else {
                    checkNameExist(apiScenarioRequest, false);
                }
                if (StringUtils.isEmpty(moduleId)) {
                    moduleId = StringUtils.EMPTY;
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
            request.setVersionId(baseProjectVersionMapper.getDefaultVersion(request.getProjectId()));
        }

        ApiScenarioExample example = new ApiScenarioExample();
        ApiScenarioExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId()).andStatusNotEqualTo("Trash").andIdNotEqualTo(request.getId()).andApiScenarioModuleIdEqualTo(request.getApiScenarioModuleId());
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
    }

    public ApiScenarioDTO getNewApiScenario(String id) {
        ApiScenarioDTO scenarioWithBLOBs = extApiScenarioMapper.selectById(id);
        if (scenarioWithBLOBs == null) {
            return null;
        }
        if (StringUtils.isNotEmpty(scenarioWithBLOBs.getScenarioDefinition())) {
            JSONObject element = JSONUtil.parseObject(scenarioWithBLOBs.getScenarioDefinition());
            List<String> caseIds = new ArrayList<>();
            Map<String, Boolean> keyMap = MsHashTreeService.getIndexKeyMap(element, element.optString(ElementConstants.INDEX));
            hashTreeService.dataFormatting(element, caseIds, keyMap);
            // 处理用例
            hashTreeService.caseFormatting(element, caseIds, getConfig(scenarioWithBLOBs));
            ElementUtil.dataFormatting(element);
            scenarioWithBLOBs.setScenarioDefinition(element.toString());
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
            if (!target.has(key)) {
                target.put(key, value);
            } else {
                if (value instanceof JSONObject) {
                    JSONObject valueJson = (JSONObject) value;
                    JSONObject targetValue = jsonMerge(valueJson, target.optJSONObject(key));
                    target.put(key, targetValue);
                } else if (value instanceof JSONArray) {
                    JSONArray valueArray = (JSONArray) value;
                    for (int i = 0; i < valueArray.length(); i++) {
                        try {
                            JSONObject obj = (JSONObject) valueArray.get(i);
                            JSONObject targetValue = jsonMerge(obj, (JSONObject) target.optJSONArray(key).get(i));
                            target.optJSONArray(key).put(i, targetValue);
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

    public ParameterConfig getConfig(ApiScenarioDTO scenario) {
        try {
            ParameterConfig config = new ParameterConfig();
            Map<String, String> environmentMap = new HashMap<>();
            String environmentType = scenario.getEnvironmentType();
            String environmentGroupId = scenario.getEnvironmentGroupId();
            String environmentJson = scenario.getEnvironmentJson();
            if (StringUtils.equals(environmentType, EnvironmentType.GROUP.name())
                    && StringUtils.isNotEmpty(environmentGroupId)) {
                environmentMap = environmentGroupProjectService.getEnvMap(environmentGroupId);
            } else if (StringUtils.equals(environmentType, EnvironmentType.JSON.name())
                    && StringUtils.isNotEmpty(environmentJson)) {
                environmentMap = JSON.parseObject(environmentJson, Map.class);
            } else {
                return config;
            }
            apiScenarioEnvService.setEnvConfig(environmentMap, config);
            return config;
        } catch (Exception e) {
            LogUtil.error(e);
            return null;
        }
    }

    public String setDomain(ApiScenarioEnvRequest request) {
        Boolean enable = request.getEnvironmentEnable();
        String scenarioDefinition = request.getDefinition();
        JSONObject element = JSONUtil.parseObject(scenarioDefinition);
        ElementUtil.dataFormatting(element);
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
                    String referenced = element.optString("referenced");
                    if (StringUtils.equalsIgnoreCase("REF", referenced)) {
                        JSONObject source = JSONUtil.parseObject(scenario.getScenarioDefinition());
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
                ElementUtil.dataSetDomain(element.optJSONArray(ElementConstants.HASH_TREE), config);
            }
            return element.toString();
        } catch (Exception e) {
            return scenarioDefinition;
        }
    }

    public int getScenarioStep(List<String> ids) {
        return extApiScenarioReferenceIdMapper.selectByScenarioIds(ids);
    }

    public List<ApiScenarioDTO> getScenarioDetail(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        List<ApiScenarioDTO> list = extApiScenarioMapper.selectByScenarioIds(ids);
        list.forEach(dto -> {
            if (dto != null) {
                if (StringUtils.isNotEmpty(dto.getScenarioDefinition())) {
                    JSONObject element = JSONUtil.parseObject(dto.getScenarioDefinition());
                    // 获取所有case
                    List<String> caseIds = new ArrayList<>();
                    Map<String, Boolean> keyMap = MsHashTreeService.getIndexKeyMap(element, element.optString(ElementConstants.INDEX));
                    hashTreeService.dataFormatting(element, caseIds, keyMap);
                    // 处理用例
                    hashTreeService.caseFormatting(element, caseIds, null);

                    ElementUtil.dataFormatting(element);
                    dto.setScenarioDefinition(element.toString());
                }
                if (StringUtils.isNotBlank(dto.getEnvironmentJson())) {
                    ApiScenarioEnvRequest request = new ApiScenarioEnvRequest();
                    request.setEnvironmentEnable(false);
                    request.setDefinition(dto.getScenarioDefinition());
                    request.setEnvironmentMap(JSON.parseObject(dto.getEnvironmentJson(), Map.class));
                    request.setEnvironmentType(dto.getEnvironmentType());
                    request.setEnvironmentGroupId(dto.getEnvironmentGroupId());
                    request.setId(dto.getId());
                    dto.setScenarioDefinition(this.setDomain(request));
                }
            }
        });
        return list;
    }

    public byte[] loadFileAsBytes(FileOperationRequest fileOperationRequest) {
        if (fileOperationRequest.getId().contains("/") || fileOperationRequest.getName().contains("/"))
            MSException.throwException(Translator.get("invalid_parameter"));
        File file = new File(ApiFileUtil.BODY_FILE_DIR + "/" + fileOperationRequest.getId() + "_" + fileOperationRequest.getName());
        try (FileInputStream fis = new FileInputStream(file); ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);) {
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


    private HashTreeInfoDTO generateJmx(ApiScenarioWithBLOBs apiScenario) {
        String jmx = null;
        HashTree jmeterHashTree = new ListedHashTree();
        List<FileMetadata> repositoryMetadata = new ArrayList<>();
        MsTestPlan testPlan = new MsTestPlan();
        // 获取自定义JAR
        String projectId = apiScenario.getProjectId();
        List<String> projectIds = new ArrayList<>();
        projectIds.add(projectId);
        testPlan.setName(apiScenario.getName());
        testPlan.setHashTree(new LinkedList<>());
        ParameterConfig config = new ParameterConfig();
        config.setOperating(true);
        config.getExcludeScenarioIds().add(apiScenario.getId());
        config.setScenarioId(apiScenario.getId());
        try {
            MsScenario scenario = JSON.parseObject(apiScenario.getScenarioDefinition(), MsScenario.class);
            if (scenario == null) {
                return null;
            }
            scenario.setId(apiScenario.getId());
            GenerateHashTreeUtil.parse(apiScenario.getScenarioDefinition(), scenario);
            String environmentType = apiScenario.getEnvironmentType();
            String environmentJson = apiScenario.getEnvironmentJson();
            String environmentGroupId = apiScenario.getEnvironmentGroupId();
            if (StringUtils.equals(environmentType, EnvironmentType.JSON.name()) && StringUtils.isNotBlank(environmentJson)) {
                scenario.setEnvironmentMap(JSON.parseObject(environmentJson, Map.class));
            } else if (StringUtils.equals(environmentType, EnvironmentType.GROUP.name()) && StringUtils.isNotBlank(environmentGroupId)) {
                Map<String, String> envMap = environmentGroupProjectService.getEnvMap(environmentGroupId);
                scenario.setEnvironmentMap(envMap);
            }

            MsThreadGroup group = new MsThreadGroup();
            group.setLabel(apiScenario.getName());
            group.setName(apiScenario.getName());
            group.setEnableCookieShare(scenario.isEnableCookieShare());
            group.setOnSampleError(scenario.getOnSampleError());
            group.setHashTree(new LinkedList<MsTestElement>() {{
                this.add(scenario);
            }});
            testPlan.getHashTree().add(group);
            testPlan.toHashTree(jmeterHashTree, testPlan.getHashTree(), config);
            repositoryMetadata = ApiFileUtil.getRepositoryFileMetadata(jmeterHashTree);
            jmx = testPlan.getJmx(jmeterHashTree);

        } catch (Exception ex) {
            LogUtil.error(ex);
            MSException.throwException(ex.getMessage());
        }


        HashTreeInfoDTO returnDTO = new HashTreeInfoDTO();
        returnDTO.setJmx(jmx);
        returnDTO.setHashTree(jmeterHashTree);
        returnDTO.setRepositoryFiles(repositoryMetadata);
        return returnDTO;
    }


    /**
     * 场景测试执行
     *
     * @param request
     * @param bodyFiles
     * @return
     */
    public String debugRun(RunDefinitionRequest request, List<MultipartFile> bodyFiles, List<MultipartFile> scenarioFiles) {
        request.setConfig(new RunModeConfigDTO());
        jMeterService.verifyPool(request.getProjectId(), request.getConfig());
        return apiScenarioExecuteService.debug(request, bodyFiles, scenarioFiles);
    }

    public ReferenceDTO getReference(ApiScenarioRequest request) {
        ReferenceDTO dto = new ReferenceDTO();
        //dto.setScenarioList(extApiScenarioMapper.selectReference(request));
        QueryReferenceRequest planRequest = new QueryReferenceRequest();
        planRequest.setScenarioId(request.getId());
        planRequest.setProjectId(request.getProjectId());
        dto.setTestPlanList(extTestPlanApiCaseMapper.selectTestPlanByRelevancy(planRequest));
        return dto;
    }


    public String addScenarioToPlan(SaveApiPlanRequest request) {
        if (CollectionUtils.isEmpty(request.getPlanIds())) {
            MSException.throwException(Translator.get("plan id is null "));
        }
        Map<String, List<String>> mapping = request.getMapping();
        Map<String, String> envMap = request.getEnvMap();
        Set<String> set = mapping.keySet();
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ExtTestPlanScenarioCaseMapper scenarioBatchMapper = sqlSession.getMapper(ExtTestPlanScenarioCaseMapper.class);
        ExtTestPlanApiCaseMapper apiCaseBatchMapper = sqlSession.getMapper(ExtTestPlanApiCaseMapper.class);

        String environmentType = request.getEnvironmentType();
        String envGroupId = request.getEnvGroupId();
        for (String planId : request.getPlanIds()) {
            if (!set.isEmpty()) {
                set.forEach(id -> {
                    TestPlanApiScenario testPlanApiScenario = new TestPlanApiScenario();
                    testPlanApiScenario.setId(UUID.randomUUID().toString());
                    testPlanApiScenario.setApiScenarioId(id);
                    testPlanApiScenario.setTestPlanId(planId);
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
                        Long nextScenarioOrder = ServiceUtils.getNextOrder(planId, extTestPlanScenarioCaseMapper::getLastOrder);
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
                    testPlanApiCase.setTestPlanId(planId);
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

    public long countScenarioByProjectID(String projectId, String versionId) {
        return extApiScenarioMapper.countByProjectID(projectId, versionId);
    }

    public long countScenarioByProjectIDAndCreatInThisWeek(String projectId, String versionId) {
        Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());
        Date firstTime = startAndEndDateInWeek.get("firstTime");
        Date lastTime = startAndEndDateInWeek.get("lastTime");
        if (firstTime == null || lastTime == null) {
            return 0;
        } else {
            return extApiScenarioMapper.countByProjectIDAndCreatInThisWeek(projectId, versionId, firstTime.getTime(), lastTime.getTime());
        }
    }

    public List<ApiDataCountResult> countRunResultByProjectID(String projectId, String versionId) {
        return extApiScenarioMapper.countRunResultByProjectID(projectId, versionId);
    }

    public List<ApiScenarioWithBLOBs> selectByIdsWithBLOBs(List<String> ids) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(ids);
        return apiScenarioMapper.selectByExampleWithBLOBs(example);
    }

    public List<ApiScenario> selectByIds(List<String> ids) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(ids);
        return apiScenarioMapper.selectByExample(example);
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
        scheduleService.updateSchedule(request);
    }

    private void addOrUpdateApiScenarioCronJob(Schedule request) {
        if (StringUtils.equals(request.getGroup(), ScheduleGroup.SWAGGER_IMPORT.name())) {
            scheduleService.addOrUpdateCronJob(request, SwaggerUrlImportJob.getJobKey(request.getResourceId()), SwaggerUrlImportJob.getTriggerKey(request.getResourceId()), SwaggerUrlImportJob.class);
        } else {
            scheduleService.addOrUpdateCronJob(request, ApiScenarioTestJob.getJobKey(request.getResourceId()), ApiScenarioTestJob.getTriggerKey(request.getResourceId()), ApiScenarioTestJob.class);
        }
    }

    public ScenarioToPerformanceInfoDTO genPerformanceTestJmx(GenScenarioRequest request) {
        ScenarioToPerformanceInfoDTO returnDTO = new ScenarioToPerformanceInfoDTO();
        List<String> ids = request.getIds();
        List<ApiScenarioDTO> apiScenarios = extApiScenarioMapper.selectIds(ids);
        String id = StringUtils.EMPTY;
        if (!apiScenarios.isEmpty()) {
            id = apiScenarios.get(0).getId();
        }
        if (CollectionUtils.isEmpty(apiScenarios)) {
            return null;
        }
        Map<String, List<String>> projectEnvMap = apiScenarioEnvService.selectApiScenarioEnv(apiScenarios);
        MsTestPlan testPlan = new MsTestPlan();
        testPlan.setHashTree(new LinkedList<>());
        ApiScenarioDTO scenario = apiScenarios.get(0);

        HashTreeInfoDTO hashTreeInfoDTO = generateJmx(scenario);
        JmxInfoDTO jmxInfo = DataFormattingUtil.updateJmxString(hashTreeInfoDTO.getJmx(), true);
        jmxInfo.addFileMetadataLists(hashTreeInfoDTO.getRepositoryFiles());

        String name = request.getName() + ".jmx";
        jmxInfo.setName(name);
        jmxInfo.setId(id);
        returnDTO.setJmxInfoDTO(jmxInfo);
        returnDTO.setProjectEnvMap(projectEnvMap);
        return returnDTO;
    }

    public void bathEdit(ApiScenarioBatchRequest request) {

        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extApiScenarioMapper.selectIdsByQuery(query));

        if (StringUtils.equals("tags", request.getType())) {
            this.batchEditTags(request, request.getIds());
            return;
        }

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
                if (StringUtils.isNotEmpty(request.getApiScenarioModuleId()) && StringUtils.isNotEmpty(request.getModulePath())) {
                    scenario.setApiScenarioModuleId(request.getApiScenarioModuleId());
                    scenario.setModulePath(request.getModulePath());
                    updateOtherVersionModule(scenario.getRefId(), scenario);
                }
            });
        }
        apiScenarioMapper.updateByExampleSelective(apiScenarioWithBLOBs, apiScenarioExample);
    }

    private void batchEditTags(ApiScenarioBatchRequest request, List<String> ids) {
        if (request.getTagList().isEmpty() || CollectionUtils.isEmpty(ids)) {
            return;
        }
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiScenarioMapper mapper = sqlSession.getMapper(ApiScenarioMapper.class);
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(ids);
        List<ApiScenario> apiScenarios = apiScenarioMapper.selectByExample(example);
        for (ApiScenario apiScenario : apiScenarios) {
            String tags = apiScenario.getTags();
            if (StringUtils.isBlank(tags) || BooleanUtils.isFalse(request.isAppendTag())) {
                apiScenario.setTags(JSON.toJSONString(request.getTagList()));
                apiScenario.setUpdateTime(System.currentTimeMillis());
            } else {
                try {
                    List<String> list = JSON.parseArray(tags, String.class);
                    list.addAll(request.getTagList());
                    apiScenario.setTags(JSON.toJSONString(list));
                    apiScenario.setUpdateTime(System.currentTimeMillis());
                } catch (Exception e) {
                    LogUtil.error("batch edit tags error.");
                    LogUtil.error(e, e.getMessage());
                    apiScenario.setTags(JSON.toJSONString(request.getTagList()));
                }
            }
            mapper.updateByPrimaryKey(apiScenario);
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public void bathEditEnv(ApiScenarioBatchRequest request) {
        if (StringUtils.isNotBlank(request.getEnvironmentId())) {
            List<ApiScenarioWithBLOBs> apiScenarios = selectByIdsWithBLOBs(request.getIds());
            apiScenarios.forEach(item -> {
                JSONObject object = JSONUtil.parseObject(item.getScenarioDefinition());
                object.put(PropertyConstant.ENVIRONMENT_ID, request.getEnvironmentId());
                if (object != null) {
                    item.setScenarioDefinition(object.toString());
                }
                apiScenarioMapper.updateByPrimaryKeySelective(item);
                apiScenarioReferenceIdService.saveApiAndScenarioRelation(item);
            });
        }
    }

    private void _importCreate(List<ApiScenarioWithBLOBs> sameRequest, ApiScenarioWithBLOBs scenarioWithBLOBs, ApiTestImportRequest apiTestImportRequest, ApiScenarioParamDTO apiScenarioParamDto) {
        ApiScenarioMapper batchMapper = apiScenarioParamDto.getBatchMapper();
        ExtApiScenarioMapper extApiScenarioMapper = apiScenarioParamDto.getExtApiScenarioMapper();
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

            checkReferenceCase(scenarioWithBLOBs, apiScenarioParamDto);
            sendImportScenarioCreateNotice(scenarioWithBLOBs);
            batchMapper.insert(scenarioWithBLOBs);
            apiScenarioReferenceIdService.saveApiAndScenarioRelation(scenarioWithBLOBs);
            extApiScenarioMapper.clearLatestVersion(scenarioWithBLOBs.getRefId());
            extApiScenarioMapper.addLatestVersion(scenarioWithBLOBs.getRefId());
        } else {
            //如果存在则修改
            if (StringUtils.isEmpty(apiTestImportRequest.getUpdateVersionId())) {
                apiTestImportRequest.setUpdateVersionId(apiTestImportRequest.getDefaultVersion());
            }
            Optional<ApiScenarioWithBLOBs> scenarioOp = sameRequest.stream().filter(api -> StringUtils.equals(api.getVersionId(), apiTestImportRequest.getUpdateVersionId())).findFirst();

            // 新增对应的版本
            if (!scenarioOp.isPresent()) {
                scenarioWithBLOBs.setId(UUID.randomUUID().toString());
                scenarioWithBLOBs.setCreateTime(System.currentTimeMillis());
                scenarioWithBLOBs.setVersionId(apiTestImportRequest.getUpdateVersionId());
                scenarioWithBLOBs.setLatest(apiTestImportRequest.getVersionId().equals(apiTestImportRequest.getDefaultVersion()));
                scenarioWithBLOBs.setRefId(sameRequest.get(0).getRefId() == null ? sameRequest.get(0).getId() : sameRequest.get(0).getRefId());
                scenarioWithBLOBs.setNum(sameRequest.get(0).getNum()); // 使用第一个num当作本次的num
                scenarioWithBLOBs.setOrder(sameRequest.get(0).getOrder());
                checkReferenceCase(scenarioWithBLOBs, apiScenarioParamDto);
                sendImportScenarioCreateNotice(scenarioWithBLOBs);
                batchMapper.insert(scenarioWithBLOBs);
            } else {
                ApiScenarioWithBLOBs existScenario = scenarioOp.get();
                scenarioWithBLOBs.setId(existScenario.getId());
                scenarioWithBLOBs.setRefId(existScenario.getRefId());
                scenarioWithBLOBs.setVersionId(apiTestImportRequest.getUpdateVersionId());
                scenarioWithBLOBs.setOrder(existScenario.getOrder());
                scenarioWithBLOBs.setNum(existScenario.getNum());
                checkReferenceCase(scenarioWithBLOBs, apiScenarioParamDto);
                sendImportScenarioUpdateNotice(scenarioWithBLOBs);
                batchMapper.updateByPrimaryKeyWithBLOBs(scenarioWithBLOBs);
            }
            apiScenarioReferenceIdService.saveApiAndScenarioRelation(scenarioWithBLOBs);
            extApiScenarioMapper.clearLatestVersion(scenarioWithBLOBs.getRefId());
            extApiScenarioMapper.addLatestVersion(scenarioWithBLOBs.getRefId());
        }
    }

    private ApiScenarioWithBLOBs importCreate(ApiScenarioWithBLOBs request, ApiTestImportRequest apiTestImportRequest, List<ApiScenarioWithBLOBs> sameList, ApiScenarioParamDTO apiScenarioParamDto) {
        final ApiScenarioWithBLOBs scenarioWithBLOBs = new ApiScenarioWithBLOBs();
        ApiScenarioMapper batchMapper = apiScenarioParamDto.getBatchMapper();
        ExtApiScenarioMapper extApiScenarioMapper = apiScenarioParamDto.getExtApiScenarioMapper();
        BeanUtils.copyBean(scenarioWithBLOBs, request);
        scenarioWithBLOBs.setUpdateTime(System.currentTimeMillis());
        if (StringUtils.isEmpty(scenarioWithBLOBs.getStatus())) {
            scenarioWithBLOBs.setStatus(ApiTestDataStatus.UNDERWAY.getValue());
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
        scenarioWithBLOBs.setLastResult(StringUtils.EMPTY);

        Boolean openCustomNum = apiTestImportRequest.getOpenCustomNum();
        List<ApiScenario> list = new ArrayList<>();
        if (BooleanUtils.isTrue(openCustomNum)) {
            ApiScenarioExample example = new ApiScenarioExample();
            ApiScenarioExample.Criteria criteria = example.createCriteria();
            if (CollectionUtils.isEmpty(sameList)) {
                criteria.andCustomNumEqualTo(scenarioWithBLOBs.getCustomNum()).andProjectIdEqualTo(scenarioWithBLOBs.getProjectId());
            } else {
                if (StringUtils.equals("fullCoverage", apiTestImportRequest.getModeId())) {
                    criteria.andNameEqualTo(scenarioWithBLOBs.getName()).andCustomNumEqualTo(scenarioWithBLOBs.getCustomNum()).andProjectIdEqualTo(scenarioWithBLOBs.getProjectId()).andIdNotEqualTo(sameList.get(0).getId());
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
            _importCreate(sameList, scenarioWithBLOBs, apiTestImportRequest, apiScenarioParamDto);
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
                if (scenarioWithBLOBs.getOrder() == null) {
                    scenarioWithBLOBs.setOrder(getImportNextOrder(request.getProjectId()));
                }
                if (scenarioWithBLOBs.getNum() == null) {
                    scenarioWithBLOBs.setNum(getNextNum(scenarioWithBLOBs.getProjectId()));
                }
                if (scenarioWithBLOBs.getRefId() == null) {
                    scenarioWithBLOBs.setRefId(scenarioWithBLOBs.getId());
                }
                checkReferenceCase(scenarioWithBLOBs, apiScenarioParamDto);
                sendImportScenarioCreateNotice(scenarioWithBLOBs);
                batchMapper.insert(scenarioWithBLOBs);
                // 存储依赖关系
                apiAutomationRelationshipEdgeService.initRelationshipEdge(null, scenarioWithBLOBs);

                apiScenarioReferenceIdService.saveApiAndScenarioRelation(scenarioWithBLOBs);
                extApiScenarioMapper.clearLatestVersion(scenarioWithBLOBs.getRefId());
                extApiScenarioMapper.addLatestVersion(scenarioWithBLOBs.getRefId());
            }

        } else {
            _importCreate(sameList, scenarioWithBLOBs, apiTestImportRequest, apiScenarioParamDto);
        }
        return scenarioWithBLOBs;
    }

    public void sendImportScenarioUpdateNotice(ApiScenario apiScenario) {
        String context = SessionUtils.getUserId().concat(Translator.get("update_scenario")).concat(":").concat(apiScenario.getName());
        Map<String, Object> paramMap = new HashMap<>();
        getParamMap(paramMap, apiScenario.getProjectId(), SessionUtils.getUserId(), apiScenario.getId(), apiScenario.getName(), apiScenario.getCreateUser());
        paramMap.put(CommonConstants.USER_ID, apiScenario.getUserId());
        NoticeModel noticeModel = NoticeModel.builder().operator(SessionUtils.getUserId()).context(context).testId(apiScenario.getId()).subject(Translator.get("scenario_update_notice")).paramMap(paramMap).excludeSelf(true).event(NoticeConstants.Event.UPDATE).build();
        noticeSendService.send(NoticeConstants.TaskType.API_AUTOMATION_TASK, noticeModel);
    }

    public void sendImportScenarioCreateNotice(ApiScenario apiScenario) {
        String context = SessionUtils.getUserId().concat(Translator.get("create_scenario")).concat(":").concat(apiScenario.getName());
        Map<String, Object> paramMap = new HashMap<>();
        getParamMap(paramMap, apiScenario.getProjectId(), SessionUtils.getUserId(), apiScenario.getId(), apiScenario.getName(), apiScenario.getCreateUser());
        paramMap.put(CommonConstants.USER_ID, apiScenario.getUserId());
        NoticeModel noticeModel = NoticeModel.builder().operator(SessionUtils.getUserId()).context(context).testId(apiScenario.getId()).subject(Translator.get("scenario_create_notice")).paramMap(paramMap).excludeSelf(true).event(NoticeConstants.Event.CREATE).build();
        noticeSendService.send(NoticeConstants.TaskType.API_AUTOMATION_TASK, noticeModel);
    }

    private void getParamMap(Map<String, Object> paramMap, String projectId, String userId, String id, String name, String createUser) {
        paramMap.put("projectId", projectId);
        paramMap.put("operator", userId);
        paramMap.put("id", id);
        paramMap.put("name", name);
        paramMap.put("createUser", createUser);
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

        String defaultVersion = baseProjectVersionMapper.getDefaultVersion(request.getProjectId());
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
            ProjectConfig config = baseProjectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.SCENARIO_CUSTOM_NUM.name());
            num = getNextNum(data.get(0).getProjectId());
            request.setOpenCustomNum(config.getScenarioCustomNum());
        }

        Map<String, ApiDefinition> definitionMap = new HashMap<>();
        Map<String, Set<String>> apiIdCaseNameMap = new HashMap<>();
        for (int i = 0; i < data.size(); i++) {
            ApiScenarioWithBLOBs item = data.get(i);
            JSONObject jsonObject = JSONUtil.parseObject(JSON.toJSONString(item));
            ElementUtil.dataFormatting(jsonObject);
            if (StringUtils.isBlank(item.getName())) {
                MSException.throwException(Translator.get("scenario_name_is_null"));
            }
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
            ApiScenarioParamDTO apiScenarioParamDto = buildParamDto(batchMapper, extApiScenarioMapper, apiTestCaseMapper, apiDefinitionMapper, definitionMap, apiIdCaseNameMap);
            importCreate(item, request, sameList, apiScenarioParamDto);

            if (i % 300 == 0) {
                sqlSession.flushStatements();
            }
        }

        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    @NotNull
    private static ApiScenarioParamDTO buildParamDto(ApiScenarioMapper batchMapper, ExtApiScenarioMapper extApiScenarioMapper, ApiTestCaseMapper apiTestCaseMapper, ApiDefinitionMapper apiDefinitionMapper, Map<String, ApiDefinition> definitionMap, Map<String, Set<String>> apiIdCaseNameMap) {
        ApiScenarioParamDTO apiScenarioParamDto = new ApiScenarioParamDTO();
        apiScenarioParamDto.setBatchMapper(batchMapper);
        apiScenarioParamDto.setExtApiScenarioMapper(extApiScenarioMapper);
        apiScenarioParamDto.setApiTestCaseMapper(apiTestCaseMapper);
        apiScenarioParamDto.setApiDefinitionMapper(apiDefinitionMapper);
        apiScenarioParamDto.setDefinitionMap(definitionMap);
        apiScenarioParamDto.setApiIdCaseNameMap(apiIdCaseNameMap);
        return apiScenarioParamDto;
    }


    private void replenishScenarioModuleIdPath(String request, ApiScenarioModuleMapper apiScenarioModuleMapper, ApiScenarioWithBLOBs item) {
        ApiScenarioModuleExample example = new ApiScenarioModuleExample();
        example.createCriteria().andProjectIdEqualTo(request).andNameEqualTo(ProjectModuleDefaultNodeEnum.API_SCENARIO_DEFAULT_NODE.getNodeName());
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
        Optional.ofNullable(file).ifPresent(item -> request.setFileName(file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf("."))));
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
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extApiScenarioMapper.selectIdsByQuery(query));
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
            JSONObject element = JSONUtil.parseObject(scenario.getScenarioDefinition());
            if (element != null) {
                JSONArray hashTree = element.optJSONArray(ElementConstants.HASH_TREE);
                ApiScenarioImportUtil.formatHashTree(hashTree);
                setHashTree(hashTree);
                scenario.setScenarioDefinition(element.toString());
            }
            names.add(scenario.getName());
            ids.add(scenario.getId());
        }
    }

    public void setHashTree(JSONArray hashTree) {
        try {
            if (hashTree != null) {
                for (int i = 0; i < hashTree.length(); i++) {
                    JSONObject object = (JSONObject) hashTree.get(i);
                    String referenced = object.optString("referenced");
                    if (StringUtils.isNotBlank(referenced) && StringUtils.equals(referenced, "REF")) {
                        // 检测引用对象是否存在，若果不存在则改成复制对象
                        String refType = object.optString("refType");
                        if (StringUtils.isNotEmpty(refType)) {
                            if (refType.equals(CommonConstants.CASE)) {
                                if (object.optJSONArray(ElementConstants.HASH_TREE) == null || object.optJSONArray(ElementConstants.HASH_TREE).length() == 0) {
                                    ApiTestCaseInfo model = extApiTestCaseMapper.selectApiCaseInfoByPrimaryKey(object.optString("id"));
                                    if (model != null) {
                                        JSONObject element = JSONUtil.parseObject(model.getRequest());
                                        object.put(ElementConstants.HASH_TREE, element.optJSONArray(ElementConstants.HASH_TREE));
                                    }
                                }
                            }
                        }
                    }
                    if (StringUtils.isNotEmpty(object.optString("refType"))) {
                        if (object.optJSONArray(ElementConstants.HASH_TREE) != null) {
                            setHashTree(object.optJSONArray(ElementConstants.HASH_TREE));
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ScenarioToPerformanceInfoDTO exportJmx(ApiScenarioBatchRequest request) {
        List<ApiScenarioWithBLOBs> apiScenarioWithBLOBs = getExportResult(request);
        //检查运行环境
        checkExportEnv(apiScenarioWithBLOBs);
        // 生成jmx
        List<ApiScenarioExportJmxDTO> resList = new ArrayList<>();

        apiScenarioWithBLOBs.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getScenarioDefinition())) {
                HashTreeInfoDTO hashTreeInfoDTO = generateJmx(item);
                if (StringUtils.isNotEmpty(hashTreeInfoDTO.getJmx())) {
                    JmxInfoDTO dto = DataFormattingUtil.updateJmxString(hashTreeInfoDTO.getJmx(), true);
                    ApiScenarioExportJmxDTO scenariosExportJmx = new ApiScenarioExportJmxDTO(item.getName(), dto.getXml());
                    dto.addFileMetadataLists(hashTreeInfoDTO.getRepositoryFiles());
                    scenariosExportJmx.setId(item.getId());
                    scenariosExportJmx.setVersion(item.getVersion());
                    //扫描需要哪些文件
                    scenariosExportJmx.setFileMetadataList(dto.getFileMetadataList());
                    Map<String, List<String>> projectEnvMap = apiScenarioEnvService.selectApiScenarioEnv(new ArrayList<>() {{
                        this.add(item);
                    }});
                    scenariosExportJmx.setProjectEnvMap(projectEnvMap);
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

        ScenarioToPerformanceInfoDTO returnDTO = new ScenarioToPerformanceInfoDTO();
        returnDTO.setScenarioJmxList(resList);
        return returnDTO;
    }

    public byte[] exportZip(ApiScenarioBatchRequest request) {
        List<ApiScenarioWithBLOBs> scenarios = getExportResult(request);
        //环境检查
        checkExportEnv(scenarios);
        // 生成jmx
        Map<String, byte[]> files = new LinkedHashMap<>();
        scenarios.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getScenarioDefinition())) {
                HashTreeInfoDTO hashTreeInfoDTO = generateJmx(item);
                if (StringUtils.isNotEmpty(hashTreeInfoDTO.getJmx())) {
                    ApiScenarioExportJmxDTO scenariosExportJmx = new ApiScenarioExportJmxDTO(item.getName(), DataFormattingUtil.updateJmxString(hashTreeInfoDTO.getJmx(), false).getXml());
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
        return ApiFileUtil.listBytesToZip(files);
    }

    private void checkExportEnv(List<ApiScenarioWithBLOBs> scenarios) {
        StringBuilder builder = new StringBuilder();
        for (ApiScenarioWithBLOBs apiScenarioWithBLOBs : scenarios) {
            try {
                apiScenarioEnvService.setScenarioEnv(apiScenarioWithBLOBs, null);
                boolean haveEnv = apiScenarioEnvService.verifyScenarioEnv(apiScenarioWithBLOBs);
                if (!haveEnv) {
                    builder.append(apiScenarioWithBLOBs.getName()).append("; ");
                }
            } catch (RuntimeException e) {
                MSException.throwException(Translator.get("scenario_warning"));
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
        if (request.getCondition() != null && request.getCondition().isSelectAll()) {
            ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) ->
                    extApiScenarioMapper.selectIdsByQuery(query));
        }
        this.removeToGc(request.getIds());
    }

    public void deleteBatchByCondition(ApiScenarioBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) ->
                extApiScenarioMapper.selectIdsByQuery(query));
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
     * @param scenarioUrlMap 场景使用到的url  key:method
     * @param apiList        接口集合（id / path 必须有数据）
     * @return
     */
    public CoveredDTO countInterfaceCoverage(String projectId, Map<String, Map<String, String>> scenarioUrlMap, List<ApiDefinition> apiList) {
        CoveredDTO coverage = new CoveredDTO();
        if (CollectionUtils.isEmpty(apiList)) {
            return coverage;
        }
        int urlContainsCount = this.getApiIdInScenario(projectId, scenarioUrlMap, apiList).size();
        coverage.setCovered(urlContainsCount);
        coverage.setNotCovered(apiList.size() - urlContainsCount);
        float coverageRageNumber = (float) urlContainsCount * 100 / apiList.size();
        DecimalFormat df = new DecimalFormat("0.0");
        coverage.setRateOfCovered(df.format(coverageRageNumber) + "%");
        return coverage;
    }

    public List<String> getApiIdInScenario(String projectId, Map<String, Map<String, String>> scenarioUrlMap, List<ApiDefinition> apiList) {
        List<String> apiIdList = new ArrayList<>();
        if (MapUtils.isNotEmpty(scenarioUrlMap) && CollectionUtils.isNotEmpty(apiList)) {
            for (ApiDefinition model : apiList) {
                if (StringUtils.equalsIgnoreCase(model.getProtocol(), "http")) {
                    Map<String, String> stepIdAndUrlMap = scenarioUrlMap.get(model.getMethod());
                    if (stepIdAndUrlMap != null) {
                        Collection<String> scenarioUrlList = scenarioUrlMap.get(model.getMethod()).values();
                        boolean matchedUrl = MockApiUtils.isUrlInList(model.getPath(), scenarioUrlList);
                        if (matchedUrl) {
                            apiIdList.add(model.getId());
                        }
                    }
                } else {
                    Map<String, String> stepIdAndUrlMap = scenarioUrlMap.get("MS_NOT_HTTP");
                    if (stepIdAndUrlMap != null && stepIdAndUrlMap.containsKey(model.getId())) {
                        apiIdList.add(model.getId());
                    }
                }
            }
        }
        return apiIdList;
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
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extApiScenarioMapper.selectIdsByQuery(query));

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

    public List<ApiScenarioWithBLOBs> listWithIds(ApiScenarioBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extApiScenarioMapper.selectIdsByQuery(query));
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

    public String getLogDetails(ScheduleRequest request) {
        Schedule bloBs = scheduleService.getScheduleByResource(request.getResourceId(), request.getGroup());
        if (bloBs != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(bloBs, ScheduleReference.scheduleColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(bloBs.getId()), bloBs.getProjectId(), bloBs.getName(), bloBs.getUserId(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public ScenarioToPerformanceInfoDTO batchGenPerformanceTestJmx(ApiScenarioBatchRequest request) {
        ScenarioToPerformanceInfoDTO returnDTO = new ScenarioToPerformanceInfoDTO();
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiScenarioMapper.selectIdsByQuery(query));
        List<JmxInfoDTO> jmxInfoList = new ArrayList<>();

        List<String> ids = request.getIds();
        List<ApiScenarioDTO> apiScenarioList = extApiScenarioMapper.selectIds(ids);
        if (CollectionUtils.isEmpty(apiScenarioList)) {
            returnDTO.setScenarioJmxList(new ArrayList<>());
            return returnDTO;
        } else {
            Map<String, List<String>> projectEnvironments = apiScenarioEnvService.selectApiScenarioEnv(apiScenarioList);
            apiScenarioList.forEach(item -> {
                MsTestPlan testPlan = new MsTestPlan();
                testPlan.setHashTree(new LinkedList<>());
                HashTreeInfoDTO hashTreeInfoDTO = generateJmx(item);
                JmxInfoDTO dto = DataFormattingUtil.updateJmxString(hashTreeInfoDTO.getJmx(), true);
                dto.setFileMetadataList(hashTreeInfoDTO.getRepositoryFiles());
                String name = item.getName() + ".jmx";
                dto.setId(item.getId());
                dto.setName(name);
                jmxInfoList.add(dto);
            });
            if (MapUtils.isNotEmpty(projectEnvironments)) {
                returnDTO.setProjectEnvMap(projectEnvironments);
            }
            returnDTO.setJmxInfoDTOList(jmxInfoList);
            return returnDTO;
        }
    }

    public void batchCopy(ApiScenarioBatchRequest request) {

        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extApiScenarioMapper.selectIdsByQuery(query));
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
                api.setCreateUser(SessionUtils.getUserId());
                api.setUserId(SessionUtils.getUserId());
                api.setPrincipal(SessionUtils.getUserId());
                api.setOrder(nextOrder += ServiceUtils.ORDER_STEP);
                api.setCreateTime(System.currentTimeMillis());
                api.setUpdateTime(System.currentTimeMillis());
                api.setRefId(api.getId());
                api.setNum(nextNum++);
                api.setCustomNum(String.valueOf(api.getNum()));
                mapper.insert(api);
                if (i % 50 == 0) sqlSession.flushStatements();
            }
            sqlSession.flushStatements();
        } finally {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public DeleteCheckResult checkBeforeDelete(ApiScenarioBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extApiScenarioMapper.selectIdsByQuery(query));
        List<String> deleteIds = request.getIds();
        DeleteCheckResult result = new DeleteCheckResult();

        if (CollectionUtils.isNotEmpty(deleteIds)) {
            Map<String, List<String>> scenarioDic = new HashMap<>();
            List<ApiScenarioReferenceId> apiScenarioReferenceIdList = extApiScenarioReferenceIdMapper.selectReferenceIdByIds(deleteIds);
            if (CollectionUtils.isNotEmpty(apiScenarioReferenceIdList)) {
                apiScenarioReferenceIdList.forEach(item -> {
                    String referenceId = item.getReferenceId();
                    String scenarioId = item.getApiScenarioId();
                    if (scenarioDic.containsKey(referenceId)) {
                        scenarioDic.get(referenceId).add(scenarioId);
                    } else {
                        List<String> list = new ArrayList<>();
                        list.add(scenarioId);
                        scenarioDic.put(referenceId, list);
                    }
                });
            }
            //测试计划引用
            List<TestPlanApiScenario> testPlanApiScenarios = extTestPlanApiScenarioMapper.selectByScenarioIds(deleteIds);
            Map<String, List<String>> planList = new HashMap<>();
            if (CollectionUtils.isNotEmpty(testPlanApiScenarios)) {
                testPlanApiScenarios.forEach(item -> {
                    String referenceId = item.getApiScenarioId();
                    String testPlanId = item.getTestPlanId();
                    if (planList.containsKey(referenceId)) {
                        planList.get(referenceId).add(testPlanId);
                    } else {
                        List<String> list = new ArrayList<>();
                        list.add(testPlanId);
                        planList.put(referenceId, list);
                    }
                });
            }
            if (MapUtils.isNotEmpty(scenarioDic) || MapUtils.isNotEmpty(planList)) {
                if (StringUtils.equals("batch", request.getType())) {
                    Map<String, List<String>> map = scenarioDic.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2, () -> new HashMap<>(planList)));
                    result.setRefCount(map.size());
                    result.setCheckMsg(new ArrayList<>(map.keySet()));
                } else {
                    ArrayList<List<String>> scenarioList = new ArrayList<>(scenarioDic.values());
                    if (CollectionUtils.isNotEmpty(scenarioList)) {
                        result.setScenarioCount(new TreeSet<String>(scenarioList.get(0)).size());
                    }
                    ArrayList<List<String>> testPlanList = new ArrayList<>(planList.values());
                    if (CollectionUtils.isNotEmpty(testPlanList)) {
                        result.setPlanCount(new TreeSet<String>(testPlanList.get(0)).size());
                    }
                }
                result.setDeleteFlag(scenarioDic.size() > 0 || planList.size() > 0);
            }
        }
        return result;
    }

    public long countExecuteTimesByProjectID(String projectId, String triggerMode, String executeType, String version) {
        return scenarioExecutionInfoService.countExecuteTimesByProjectID(projectId, triggerMode, executeType, version);
    }

    /**
     * 用例自定义排序
     *
     * @param request
     */
    public void updateOrder(ResetOrderRequest request) {
        ServiceUtils.updateOrderField(request, ApiScenarioWithBLOBs.class, apiScenarioMapper::selectByPrimaryKey, extApiScenarioMapper::getPreOrder, extApiScenarioMapper::getLastOrder, apiScenarioMapper::updateByPrimaryKeySelective);
    }

    public boolean verifyScenarioEnv(ApiScenarioWithBLOBs request) {
        return apiScenarioEnvService.verifyScenarioEnv(request);
    }

    public boolean verifyScenarioEnv(String scenarioId) {
        ApiScenarioWithBLOBs apiScenarioWithBLOBs = apiScenarioMapper.selectByPrimaryKey(scenarioId);
        apiScenarioEnvService.setScenarioEnv(apiScenarioWithBLOBs, null);
        return apiScenarioEnvService.verifyScenarioEnv(apiScenarioWithBLOBs);
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

    public ScenarioEnv getApiScenarioEnv(byte[] request) {
        String definition = new String(request, StandardCharsets.UTF_8);
        return apiScenarioEnvService.getApiScenarioEnv(definition);
    }

    public List<MsExecResponseDTO> run(RunScenarioRequest request) {
        jMeterService.verifyPool(request.getProjectId(), request.getConfig());
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
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extApiScenarioMapper.selectIdsByQuery(query));

        List<String> ids = request.getIds();
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(ids);
        List<ApiScenarioWithBLOBs> apiScenarios = apiScenarioMapper.selectByExampleWithBLOBs(example);

        List<String> strings = new LinkedList<>();
        apiScenarios.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getScenarioDefinition())) {
                ScenarioEnv env = apiScenarioEnvService.getApiScenarioEnv(item.getScenarioDefinition());
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

    private void setReferenced(JSONArray hashTree, String projectId, String versionId, ApiScenarioParamDTO apiScenarioParamDto) {
        // 将引用转成复制
        if (hashTree == null) {
            return;
        }
        for (int i = 0; i < hashTree.length(); i++) {
            JSONObject object = (JSONObject) hashTree.get(i);
            String referenced = object.optString("referenced");
            if (StringUtils.isNotBlank(referenced) && StringUtils.equals(referenced, "REF")) {
                // 检测引用对象是否存在，若果不存在则改成复制对象
                String refType = object.optString("refType");
                if (StringUtils.isNotEmpty(refType)) {
                    if (refType.equals(CommonConstants.CASE)) {
                        ApiScenarioImportUtil.checkCase(object, versionId, projectId, apiScenarioParamDto);
                    } else {
                        checkAutomation(object);
                        object.put("projectId", projectId);
                    }
                } else {
                    object.put("referenced", "Copy");
                }
            } else {
                //将复制的或者类型不是引用case的步骤赋予当前项目id，目的是为了运行的时候可以配置运行环境
                object.put("projectId", projectId);
                if (StringUtils.isEmpty(object.optString("url"))
                        && StringUtils.equals(object.optString("type"), HTTPSamplerProxy.class.getCanonicalName())) {
                    object.put("isRefEnvironment", true);
                }
            }
            JSONObject environmentMap = null;
            if (object.has("environmentMap")) {
                environmentMap = object.optJSONObject("environmentMap");
            }
            if (environmentMap != null) {
                object.put("environmentMap", new HashMap<>());
            }
            setReferenced(object.optJSONArray(ElementConstants.HASH_TREE), projectId, versionId, apiScenarioParamDto);
        }
    }

    public void checkAutomation(JSONObject object) {
        ApiScenarioWithBLOBs bloBs = getDto(object.optString("id"));
        if (bloBs == null) {
            object.put("referenced", "Copy");
        } else {
            BaseCheckPermissionService baseCheckPermissionService = CommonBeanFactory.getBean(BaseCheckPermissionService.class);
            Set<String> userRelatedProjectIds = baseCheckPermissionService.getUserRelatedProjectIds();
            if (!userRelatedProjectIds.contains(bloBs.getProjectId())) {
                object.put("referenced", "Copy");
            } else {
                object.put("id", bloBs.getId());
                object.put(ElementConstants.RESOURCE_ID, bloBs.getId());
            }
        }
    }

    /**
     * 获取当前项目的场景下引用的接口路径和id
     *
     * @param projectId
     * @return <get/post, <step-id,url>>
     */
    public Map<String, Map<String, String>> selectScenarioUseUrlByProjectId(String projectId, String versionId) {
        List<ApiScenarioReferenceId> list = apiScenarioReferenceIdService.selectUrlByProjectId(projectId, versionId);
        Map<String, Map<String, String>> returnMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(item -> {
                String method = item.getMethod() == null ? "MS_NOT_HTTP" : item.getMethod();
                if (returnMap.containsKey(method)) {
                    returnMap.get(method).put(item.getReferenceId(), item.getUrl());
                } else {
                    Map<String, String> urlMap = new HashMap<>() {{
                        this.put(item.getReferenceId(), item.getUrl());
                    }};
                    returnMap.put(method, urlMap);
                }
            });
        }
        return returnMap;
    }

    public void updateNoModuleToDefaultModule(String projectId, String status, String id) {
        this.extApiScenarioMapper.updateNoModuleToDefaultModule(projectId, status, id);
    }

    public Map<String, List<ApiScenario>> selectApiBaseInfoGroupByModuleId(String projectId, String status) {
        List<ApiScenario> apiScenarioList = extApiScenarioMapper.selectBaseInfoByProjectIdAndStatus(projectId, status);
        return apiScenarioList.stream().collect(Collectors.groupingBy(ApiScenario::getApiScenarioModuleId));
    }

    public Map<String, List<ApiScenario>> selectApiBaseInfoGroupByModuleId(String projectId, String status, ApiScenarioRequest request) {
        List<ApiScenario> apiScenarioList = extApiScenarioMapper.selectBaseInfoByCondition(projectId, status, request);
        return apiScenarioList.stream().collect(Collectors.groupingBy(ApiScenario::getApiScenarioModuleId));
    }

    public List<ApiCountChartResult> countByRequest(ApiCountRequest request) {
        return extApiScenarioMapper.countByRequest(request);
    }

    public List<ApiScenario> getScenarioCaseByIds(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            ApiScenarioExample example = new ApiScenarioExample();
            example.createCriteria().andIdIn(ids).andStatusNotEqualTo(CommonConstants.TRASH_STATUS);
            return apiScenarioMapper.selectByExample(example);
        }
        return new ArrayList<>();
    }

    public List<BaseCase> getBaseCaseByProjectId(String projectId) {
        return extApiScenarioMapper.selectBaseCaseByProjectId(projectId);
    }

    public Map<String, List<String>> getProjectEnvMap(RunScenarioRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extApiScenarioMapper.selectIdsByQuery(query));

        List<String> ids = request.getIds();
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(ids);
        List<ApiScenarioWithBLOBs> apiScenarios = apiScenarioMapper.selectByExampleWithBLOBs(example);
        Map<String, List<String>> projectEnvMap = new HashMap<>();
        apiScenarios.forEach(item -> {
            if (StringUtils.isNotBlank(item.getEnvironmentJson())) {
                JSONObject jsonObject = JSONUtil.parseObject(item.getEnvironmentJson());
                Map<String, Object> projectIdEnvMap = jsonObject.toMap();
                if (MapUtils.isNotEmpty(projectIdEnvMap)) {
                    Set<String> projectIds = projectIdEnvMap.keySet();
                    projectIds.forEach(t -> {
                        List<String> envIds = projectEnvMap.get(t);
                        if (CollectionUtils.isNotEmpty(envIds)) {
                            if (!envIds.contains(projectIdEnvMap.get(t).toString())) {
                                envIds.add(projectIdEnvMap.get(t).toString());
                            }
                        } else {
                            Object o = projectIdEnvMap.get(t);
                            List<String> envIdList = new ArrayList<>();
                            envIdList.add(o.toString());
                            projectEnvMap.put(t, envIdList);
                        }
                    });
                }
            }
        });
        return projectEnvMap;
    }

    @Async
    public void setProjectIdInExecutionInfo() {
        long lastCount = 0;
        long allSourceIdCount = scenarioExecutionInfoService.countSourceIdByProjectIdIsNull();
        //分批进行查询更新处理
        int pageSize = 1000;
        while (allSourceIdCount > 0) {
            if (allSourceIdCount == lastCount) {
                //数据无法再更新时跳出循环
                break;
            } else {
                lastCount = allSourceIdCount;
            }
            PageHelper.startPage(0, pageSize, false);
            List<String> sourceIdAboutProjectIdIsNull = scenarioExecutionInfoService.selectSourceIdByProjectIdIsNull();
            PageHelper.clearPage();
            //批量更新
            for (String sourceId : sourceIdAboutProjectIdIsNull) {
                ApiScenario scenario = this.selectApiScenarioById(sourceId);
                if (scenario != null) {
                    scenarioExecutionInfoService.updateProjectIdBySourceIdAndProjectIdIsNull(scenario.getProjectId(), ExecutionExecuteTypeEnum.BASIC.name(), scenario.getVersionId(), sourceId);
                } else {
                    TestPlanApiScenario testPlanApiScenario = testPlanScenarioCaseService.get(sourceId);
                    if (testPlanApiScenario != null) {
                        String projectId = testPlanScenarioCaseService.selectProjectId(testPlanApiScenario.getTestPlanId());
                        scenario = this.selectApiScenarioById(testPlanApiScenario.getApiScenarioId());
                        if (StringUtils.isNotEmpty(projectId) && scenario != null) {
                            scenarioExecutionInfoService.updateProjectIdBySourceIdAndProjectIdIsNull(projectId, ExecutionExecuteTypeEnum.TEST_PLAN.name(), scenario.getVersionId(), sourceId);
                        } else {
                            scenarioExecutionInfoService.deleteBySourceIdAndProjectIdIsNull(sourceId);
                        }
                    } else {
                        scenarioExecutionInfoService.deleteBySourceIdAndProjectIdIsNull(sourceId);
                    }
                }
            }
            allSourceIdCount = scenarioExecutionInfoService.countSourceIdByProjectIdIsNull();
        }
    }

    private ApiScenario selectApiScenarioById(String id) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdEqualTo(id);
        List<ApiScenario> scenarioList = apiScenarioMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(scenarioList)) {
            return null;
        } else {
            return scenarioList.get(0);
        }
    }

    public ScenarioProjectDTO projectIdInlist(ApiScenarioRequest request) {
        request = this.initRequest(request, true, true);
        List<String> scenarioIdList = extApiScenarioMapper.selectIdByScenarioRequest(request);
        if (CollectionUtils.isNotEmpty(request.getUnSelectIds())) {
            scenarioIdList.removeAll(request.getUnSelectIds());
        }
        return BatchProcessingUtil.getProjectIdsByScenarioIdList(scenarioIdList, this::getProjectIdsByScenarioIdList);
    }

    public ScenarioProjectDTO getProjectIdsByScenarioIdList(List<String> scenarioIdList) {
        ScenarioProjectDTO returnDTO = new ScenarioProjectDTO();
        if (CollectionUtils.isNotEmpty(scenarioIdList)) {
            ApiScenarioExample example = new ApiScenarioExample();
            example.createCriteria().andIdIn(scenarioIdList);
            List<ApiScenarioWithBLOBs> scenarioWithBLOBsList = apiScenarioMapper.selectByExampleWithBLOBs(example);
            for (ApiScenarioWithBLOBs scenario : scenarioWithBLOBsList) {
                ScenarioEnv scenarioEnv = apiScenarioEnvService.getApiScenarioEnv(scenario.getScenarioDefinition());
                if (CollectionUtils.isNotEmpty(scenarioEnv.getProjectIds())) {
                    scenarioEnv.getProjectIds().forEach(projectId -> {
                        if (!returnDTO.getProjectIdList().contains(projectId)) {
                            returnDTO.getProjectIdList().add(projectId);
                        }
                    });
                }
                returnDTO.getScenarioProjectIdMap().put(scenario.getId(), new ArrayList<>(scenarioEnv.getProjectIds()));
            }
        }
        return returnDTO;
    }


    public Map<String, ScheduleDTO> selectScheduleInfo(List<String> scenarioIds) {
        if (CollectionUtils.isNotEmpty(scenarioIds)) {
            List<ScheduleDTO> scheduleInfoList = scheduleService.selectByResourceIds(scenarioIds);
            for (ScheduleDTO schedule : scheduleInfoList) {
                schedule.setScheduleExecuteTime(this.getNextTriggerTime(schedule.getValue()));
            }
            return scheduleInfoList.stream().collect(Collectors.toMap(Schedule::getResourceId, item -> item));
        } else {
            return new HashMap<>();
        }
    }

    //获取下次执行时间（getFireTimeAfter，也可以下下次...）
    private long getNextTriggerTime(String cron) {
        if (!CronExpression.isValidExpression(cron)) {
            return 0;
        }
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity("Calculate Date").withSchedule(CronScheduleBuilder.cronSchedule(cron)).build();
        Date time0 = trigger.getStartTime();
        Date time1 = trigger.getFireTimeAfter(time0);
        return time1 == null ? 0 : time1.getTime();
    }
}
