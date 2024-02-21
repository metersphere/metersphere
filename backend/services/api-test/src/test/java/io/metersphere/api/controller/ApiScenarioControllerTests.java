package io.metersphere.api.controller;

import io.metersphere.api.constants.*;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.ApiFile;
import io.metersphere.api.dto.assertion.MsAssertionConfig;
import io.metersphere.api.dto.debug.ModuleCreateRequest;
import io.metersphere.api.dto.definition.ApiDefinitionAddRequest;
import io.metersphere.api.dto.definition.ApiTestCaseAddRequest;
import io.metersphere.api.dto.request.http.Header;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.QueryParam;
import io.metersphere.api.dto.response.ApiScenarioBatchOperationResponse;
import io.metersphere.api.dto.response.OperationDataInfo;
import io.metersphere.api.dto.scenario.*;
import io.metersphere.api.job.ApiScenarioScheduleJob;
import io.metersphere.api.mapper.*;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.service.ApiScenarioBatchOperationTestService;
import io.metersphere.api.service.BaseFileManagementTestService;
import io.metersphere.api.service.BaseResourcePoolTestService;
import io.metersphere.api.service.definition.ApiDefinitionModuleService;
import io.metersphere.api.service.definition.ApiDefinitionService;
import io.metersphere.api.service.definition.ApiTestCaseService;
import io.metersphere.api.service.scenario.ApiScenarioService;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.KeyValueEnableParam;
import io.metersphere.project.api.assertion.MsResponseCodeAssertion;
import io.metersphere.project.api.assertion.MsScriptAssertion;
import io.metersphere.project.api.processor.MsProcessor;
import io.metersphere.project.api.processor.SQLProcessor;
import io.metersphere.project.dto.environment.EnvironmentConfig;
import io.metersphere.project.dto.environment.EnvironmentGroupProjectDTO;
import io.metersphere.project.dto.environment.EnvironmentGroupRequest;
import io.metersphere.project.dto.environment.EnvironmentRequest;
import io.metersphere.project.dto.environment.http.HttpConfig;
import io.metersphere.project.dto.environment.http.HttpConfigPathMatchRule;
import io.metersphere.project.dto.environment.http.SelectModule;
import io.metersphere.project.dto.environment.processors.EnvProcessorConfig;
import io.metersphere.project.dto.environment.processors.EnvRequestScriptProcessor;
import io.metersphere.project.dto.environment.processors.EnvScenarioScriptProcessor;
import io.metersphere.project.dto.filemanagement.request.FileUploadRequest;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.project.service.EnvironmentGroupService;
import io.metersphere.project.service.EnvironmentService;
import io.metersphere.project.service.FileMetadataService;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentExample;
import io.metersphere.sdk.domain.EnvironmentGroup;
import io.metersphere.sdk.dto.api.task.ApiRunModeConfigDTO;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.mapper.EnvironmentGroupMapper;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.Plugin;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.dto.request.PluginUpdateRequest;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.ScheduleMapper;
import io.metersphere.system.service.PluginService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.CheckLogModel;
import io.metersphere.system.utils.Pager;
import io.swagger.annotations.Api;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static io.metersphere.api.controller.result.ApiResultCode.API_SCENARIO_EXIST;
import static io.metersphere.sdk.constants.InternalUserRole.ADMIN;
import static io.metersphere.system.controller.handler.result.MsHttpResultCode.NOT_FOUND;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiScenarioControllerTests extends BaseTest {
    private static final String BASE_PATH = "/api/scenario/";
    private static final String TRASH_PAGE = "trash/page";
    private static final String BATCH_EDIT = "batch-operation/edit";
    private static final String FOLLOW = "follow/";
    protected static final String UPLOAD_TEMP_FILE = "upload/temp/file";
    protected static final String DELETE_TO_GC = "delete-to-gc/{0}";
    protected static final String STEP_GET = "step/get";
    protected static final String DEBUG = "debug";
    private static final String UPDATE_STATUS = "update-status";
    private static final String UPDATE_PRIORITY = "update-priority";

    private static final Map<String, String> BATCH_OPERATION_SCENARIO_MODULE_MAP = new HashMap<>();
    private static final List<String> BATCH_OPERATION_SCENARIO_ID = new ArrayList<>();

    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ApiScenarioModuleMapper apiScenarioModuleMapper;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;
    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private ApiScenarioFollowerMapper apiScenarioFollowerMapper;
    @Resource
    private EnvironmentGroupMapper environmentGroupMapper;
    @Resource
    private ApiScenarioStepMapper apiScenarioStepMapper;
    @Resource
    private ApiScenarioStepBlobMapper apiScenarioStepBlobMapper;
    @Resource
    private ApiFileResourceMapper apiFileResourceMapper;
    @Resource
    private ApiScenarioBlobMapper apiScenarioBlobMapper;
    @Resource
    private ExtBaseProjectVersionMapper extBaseProjectVersionMapper;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ApiScenarioBatchOperationTestService apiScenarioBatchOperationTestService;
    @Resource
    private BaseResourcePoolTestService baseResourcePoolTestService;
    @Resource
    private FileMetadataService fileMetadataService;
    @Resource
    private ApiScenarioCsvMapper apiScenarioCsvMapper;
    @Resource
    private ApiScenarioService apiScenarioService;
    @Resource
    private ScheduleMapper scheduleMapper;
    @Resource
    private EnvironmentService environmentService;
    @Resource
    private EnvironmentGroupService environmentGroupService;
    @Resource
    private PluginService pluginService;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ApiDefinitionModuleService apiDefinitionModuleService;
    @Resource
    private BaseFileManagementTestService baseFileManagementTestService;
    @Resource
    private ApiCommonService apiCommonService;
    private static String fileMetadataId;
    private static String localFileId;
    private static ApiScenario addApiScenario;
    private static List<ApiScenarioStepRequest> addApiScenarioSteps;
    private static ApiScenario anOtherAddApiScenario;
    private static List<ApiScenarioStepRequest> anOtherAddApiScenarioSteps;
    private static ApiDefinition apiDefinition;
    private static ApiTestCase apiTestCase;
    private static String envId;
    private static String envGroupId;
    private static String moduleId;

    private static final List<CheckLogModel> LOG_CHECK_LIST = new ArrayList<>();

    @Override
    protected String getBasePath() {
        return BASE_PATH;
    }

    public void initApiScenario() {
        ApiScenarioModule apiScenarioModule = new ApiScenarioModule();
        apiScenarioModule.setId("scenario-moduleId");
        apiScenarioModule.setProjectId(DEFAULT_PROJECT_ID);
        apiScenarioModule.setName("场景模块");
        apiScenarioModule.setCreateTime(System.currentTimeMillis());
        apiScenarioModule.setUpdateTime(System.currentTimeMillis());
        apiScenarioModule.setCreateUser("admin");
        apiScenarioModule.setUpdateUser("admin");
        apiScenarioModule.setPos(0L);
        apiScenarioModuleMapper.insertSelective(apiScenarioModule);

        // 创建环境组
        EnvironmentGroup environmentGroup = new EnvironmentGroup();
        environmentGroup.setId("scenario-environment-group-id");
        environmentGroup.setProjectId(DEFAULT_PROJECT_ID);
        environmentGroup.setName("环境组");
        environmentGroup.setCreateTime(System.currentTimeMillis());
        environmentGroup.setUpdateTime(System.currentTimeMillis());
        environmentGroup.setCreateUser("admin");
        environmentGroup.setUpdateUser("admin");
        environmentGroup.setPos(0L);
        environmentGroupMapper.insertSelective(environmentGroup);

        EnvironmentExample environmentExample = new EnvironmentExample();
        environmentExample.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andMockEqualTo(true);
        List<Environment> environments = environmentMapper.selectByExample(environmentExample);
        for (int i = 0; i < 10; i++) {
            ApiScenario apiScenario = new ApiScenario();
            apiScenario.setId("api-scenario-id" + i);
            apiScenario.setProjectId(DEFAULT_PROJECT_ID);
            apiScenario.setName(StringUtils.join("接口场景", apiScenario.getId()));
            apiScenario.setModuleId("scenario-moduleId");
            apiScenario.setStatus("未规划");
            apiScenario.setNum(NumGenerator.nextNum(DEFAULT_PROJECT_ID, ApplicationNumScope.API_SCENARIO));
            apiScenario.setPos(0L);
            apiScenario.setPriority("P0");
            apiScenario.setLatest(true);
            apiScenario.setVersionId("1.0");
            apiScenario.setRefId(apiScenario.getId());
            apiScenario.setCreateTime(System.currentTimeMillis());
            apiScenario.setUpdateTime(System.currentTimeMillis());
            apiScenario.setCreateUser("admin");
            apiScenario.setUpdateUser("admin");
            Schedule schedule = new Schedule();
            schedule.setId(IDGenerator.nextStr());
            schedule.setKey(apiScenario.getId());
            schedule.setProjectId(DEFAULT_PROJECT_ID);
            schedule.setResourceId(apiScenario.getId());
            schedule.setJob(ApiScenarioScheduleJob.class.getName());
            schedule.setResourceType(ScheduleResourceType.API_SCENARIO.name());
            schedule.setEnable(true);
            schedule.setName("定时任务");
            schedule.setCreateUser("admin");
            schedule.setUpdateTime(System.currentTimeMillis());
            schedule.setCreateTime(System.currentTimeMillis());
            schedule.setType(ScheduleType.CRON.name());
            if (i % 2 == 0) {
                apiScenario.setTags(new ArrayList<>(List.of("tag1", "tag2")));
                apiScenario.setGrouped(true);
                apiScenario.setEnvironmentId("scenario-environment-group-id");
                schedule.setValue("0 0 0/1 * * ? ");
            } else {
                apiScenario.setGrouped(false);
                apiScenario.setEnvironmentId(environments.get(0).getId());
                schedule.setValue("1111");
            }
            scheduleMapper.insertSelective(schedule);
            apiScenarioMapper.insertSelective(apiScenario);
        }
    }

    @Test
    @Order(0)
    public void uploadTempFileTest() throws Exception {
        // 准备数据，上传文件管理文件
        uploadFileMetadata();
        MockMultipartFile file = getMockMultipartFile();
        localFileId = doUploadTempFile(file);

        // 准备数据，上传文件管理文件
        fileMetadataId = baseFileManagementTestService.upload(file);
    }

    public void initApiScenarioTrash() {
        ApiScenarioModule apiScenarioModule = new ApiScenarioModule();
        apiScenarioModule.setId("scenario-moduleId-trash");
        apiScenarioModule.setProjectId(DEFAULT_PROJECT_ID);
        apiScenarioModule.setName("场景模块-trash");
        apiScenarioModule.setCreateTime(System.currentTimeMillis());
        apiScenarioModule.setUpdateTime(System.currentTimeMillis());
        apiScenarioModule.setCreateUser("admin");
        apiScenarioModule.setUpdateUser("admin");
        apiScenarioModule.setPos(0L);
        apiScenarioModuleMapper.insertSelective(apiScenarioModule);
        for (int i = 0; i < 10; i++) {
            ApiScenario apiScenario = new ApiScenario();
            apiScenario.setId("trash-api-scenario-id" + i);
            apiScenario.setProjectId(DEFAULT_PROJECT_ID);
            apiScenario.setName(StringUtils.join("接口场景-回收站", apiScenario.getId()));
            apiScenario.setModuleId("scenario-moduleId-trash");
            apiScenario.setStatus("未规划");
            apiScenario.setNum(NumGenerator.nextNum(DEFAULT_PROJECT_ID, ApplicationNumScope.API_SCENARIO));
            apiScenario.setPos(0L);
            apiScenario.setLatest(true);
            apiScenario.setPriority("P0");
            apiScenario.setVersionId("1.0");
            apiScenario.setRefId(apiScenario.getId());
            apiScenario.setCreateTime(System.currentTimeMillis());
            apiScenario.setUpdateTime(System.currentTimeMillis());
            apiScenario.setCreateUser("admin");
            apiScenario.setUpdateUser("admin");
            apiScenarioMapper.insertSelective(apiScenario);
        }
    }

    @Test
    @Order(1)
    public void add() throws Exception {
        initModule();
        initEnv();
        initTestData();

        // @@请求成功
        ApiScenarioAddRequest request = new ApiScenarioAddRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setDescription("desc");
        request.setName("test name");
        request.setModuleId("default");
        request.setGrouped(false);
        request.setEnvironmentId(envId);
        request.setTags(List.of("tag1", "tag2"));
        request.setPriority("P0");
        request.setStatus(ApiScenarioStatus.COMPLETED.name());
        List<ApiScenarioStepRequest> steps = getApiScenarioStepRequests();
        Map<String, Object> steptDetailMap = new HashMap<>();
        steptDetailMap.put(steps.get(1).getId(), getMsHttpElementParam());
        steptDetailMap.put(steps.get(0).getId(), getMsHttpElementParam());
        request.setSteps(steps);
        request.setStepDetails(steptDetailMap);
        request.setScenarioConfig(getScenarioConfig());

        MvcResult mvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);
        ApiScenario resultData = getResultData(mvcResult, ApiScenario.class);
        this.addApiScenario = apiScenarioMapper.selectByPrimaryKey(resultData.getId());
        assertUpdateApiScenario(request, request.getScenarioConfig(), addApiScenario.getId());
        assertUpdateSteps(steps, steptDetailMap);

        request.setName("anOther name");
        request.setGrouped(true);
        request.setEnvironmentId(envGroupId);
        ApiScenarioStepRequest stepRequest = new ApiScenarioStepRequest();
        stepRequest.setId(IDGenerator.nextStr());
        stepRequest.setEnable(true);
        stepRequest.setName(addApiScenario.getName());
        stepRequest.setResourceId(addApiScenario.getId());
        stepRequest.setRefType(ApiScenarioStepRefType.REF.name());
        stepRequest.setStepType(ApiScenarioStepType.API_SCENARIO.name());
        stepRequest.setProjectId(DEFAULT_PROJECT_ID);

        ApiScenarioStepRequest stepRequest2 = new ApiScenarioStepRequest();
        stepRequest2.setId(IDGenerator.nextStr());
        stepRequest2.setName(addApiScenario.getName());
        stepRequest2.setResourceId(addApiScenario.getId());
        stepRequest2.setRefType(ApiScenarioStepRefType.PARTIAL_REF.name());
        stepRequest2.setChildren(List.of(stepRequest));
        stepRequest2.setStepType(ApiScenarioStepType.API_SCENARIO.name());
        stepRequest2.setProjectId(DEFAULT_PROJECT_ID);
        steps = List.of(stepRequest, stepRequest2);
        request.setSteps(steps);
        mvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);
        this.anOtherAddApiScenario = apiScenarioMapper.selectByPrimaryKey(getResultData(mvcResult, ApiScenario.class).getId());
        assertUpdateApiScenario(request, request.getScenarioConfig(), anOtherAddApiScenario.getId());
        assertUpdateSteps(steps, steptDetailMap);
        anOtherAddApiScenarioSteps = steps;

        // @@重名校验异常
        assertErrorCode(this.requestPost(DEFAULT_ADD, request), API_SCENARIO_EXIST);
        // @@校验日志
        checkLog(this.addApiScenario.getId(), OperationLogType.ADD);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_SCENARIO_ADD, DEFAULT_ADD, request);
    }

    private Object getMsHttpElementParam() {
        return getMsHttpElementStr(MsHTTPElementTest.getMsHttpElement());
    }

    private Object getMsHttpElementStr(MsHTTPElement msHTTPElement) {
        return JSON.parseObject(ApiDataUtils.toJSONString(msHTTPElement));
    }

    /**
     * 校验更新数据
     *
     * @param request
     * @param scenarioConfig
     * @param id
     * @return
     * @throws Exception
     */
    private ApiScenario assertUpdateApiScenario(Object request, ScenarioConfig scenarioConfig, String id) {
        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(id);
        ApiScenarioBlob apiScenarioBlob = apiScenarioBlobMapper.selectByPrimaryKey(id);
        ApiScenario copyApiScenario = BeanUtils.copyBean(new ApiScenario(), apiScenario);
        copyApiScenario = BeanUtils.copyBean(copyApiScenario, request);
        Assertions.assertEquals(apiScenario, copyApiScenario);
        if (scenarioConfig != null) {
            Assertions.assertEquals(scenarioConfig, JSON.parseObject(new String(apiScenarioBlob.getConfig()), ScenarioConfig.class));
        }
        return apiScenario;
    }

    private void assertUpdateSteps(List<ApiScenarioStepRequest> steps, Map<String, Object> steptDetailMap) {
        for (ApiScenarioStepRequest step : steps) {
            ApiScenarioStep apiScenarioStep = apiScenarioStepMapper.selectByPrimaryKey(step.getId());
            ApiScenarioStep copyApiScenarioStep = BeanUtils.copyBean(new ApiScenarioStep(), apiScenarioStep);
            copyApiScenarioStep = BeanUtils.copyBean(copyApiScenarioStep, step);
            Assertions.assertEquals(apiScenarioStep, copyApiScenarioStep);
            ApiScenarioStepBlob apiScenarioStepBlob = apiScenarioStepBlobMapper.selectByPrimaryKey(step.getId());
            if (apiScenarioStepBlob != null && steptDetailMap.get(step.getId()) != null) {
                Assertions.assertEquals(steptDetailMap.get(step.getId()), JSON.parseObject(new String(apiScenarioStepBlob.getContent())));
            }
        }
    }

    private ScenarioConfig getScenarioConfig() throws Exception {
        ScenarioConfig scenarioConfig = new ScenarioConfig();
        MsAssertionConfig msAssertionConfig = new MsAssertionConfig();
        MsScriptAssertion scriptAssertion = new MsScriptAssertion();
        scriptAssertion.setScript("{}");
        scriptAssertion.setName("script");
        msAssertionConfig.setAssertions(List.of(scriptAssertion));
        MsResponseCodeAssertion responseCodeAssertion = new MsResponseCodeAssertion();
        responseCodeAssertion.setExpectedValue("200");
        responseCodeAssertion.setCondition(MsAssertionCondition.EMPTY.name());
        responseCodeAssertion.setName("test");
        scenarioConfig.getAssertionConfig().getAssertions().add(responseCodeAssertion);
        ScenarioOtherConfig scenarioOtherConfig = new ScenarioOtherConfig();
        scenarioOtherConfig.setStepWaitTime(1000);
        scenarioOtherConfig.setFailureStrategy(ScenarioOtherConfig.FailureStrategy.CONTINUE.name());
        scenarioOtherConfig.setEnableCookieShare(true);
        scenarioConfig.setOtherConfig(scenarioOtherConfig);
        ScenarioVariable scenarioVariable = new ScenarioVariable();
        scenarioVariable.setCsvVariables(getCsvVariables());
        scenarioConfig.setVariable(scenarioVariable);
        return scenarioConfig;
    }

    /**
     * 文件管理插入一条数据
     * 便于测试关联文件
     */
    private void uploadFileMetadata() throws Exception {
        FileUploadRequest fileUploadRequest = new FileUploadRequest();
        fileUploadRequest.setProjectId(DEFAULT_PROJECT_ID);
        //导入正常文件
        MockMultipartFile file = new MockMultipartFile("file", "file.csv", MediaType.APPLICATION_OCTET_STREAM_VALUE, "aa".getBytes());
        fileMetadataId = fileMetadataService.upload(fileUploadRequest, "admin", file);
    }

    public List<CsvVariable> getCsvVariables() throws Exception {
        List<CsvVariable> csvVariables = new ArrayList<>();
        CsvVariable csvVariable = new CsvVariable();
        csvVariable.setFileId(localFileId);
        csvVariable.setName("csv变量");
        csvVariable.setScope(CsvVariable.CsvVariableScope.SCENARIO.name());
        csvVariables.add(csvVariable);
        csvVariable = new CsvVariable();
        csvVariable.setFileId(fileMetadataId);
        csvVariable.setName("csv-关联的");
        csvVariable.setScope(CsvVariable.CsvVariableScope.SCENARIO.name());
        csvVariable.setAssociation(true);
        csvVariables.add(csvVariable);
        return csvVariables;
    }

    private List<ApiScenarioStepRequest> getApiScenarioStepRequests() {
        ApiScenarioStepRequest stepRequest = new ApiScenarioStepRequest();
        stepRequest.setId(IDGenerator.nextStr());
        stepRequest.setVersionId(extBaseProjectVersionMapper.getDefaultVersion(DEFAULT_PROJECT_ID));
        stepRequest.setConfig(new HashMap<>());
        stepRequest.setEnable(true);
        stepRequest.setName(apiTestCase.getName());
        stepRequest.setResourceId(apiTestCase.getId());
        stepRequest.setRefType(ApiScenarioStepRefType.REF.name());
        stepRequest.setStepType(ApiScenarioStepType.API_CASE.name());
        stepRequest.setProjectId(DEFAULT_PROJECT_ID);
        stepRequest.setConfig(new HashMap<>());
        stepRequest.setCsvFileIds(List.of(fileMetadataId));

        ApiScenarioStepRequest stepRequest2 = new ApiScenarioStepRequest();
        stepRequest2.setId(IDGenerator.nextStr());
        stepRequest2.setVersionId(extBaseProjectVersionMapper.getDefaultVersion(DEFAULT_PROJECT_ID));
        stepRequest2.setConfig(new HashMap<>());
        stepRequest2.setEnable(true);
        stepRequest2.setResourceId(apiTestCase.getId());
        stepRequest2.setName(apiTestCase.getName() + "2");
        stepRequest2.setStepType(ApiScenarioStepType.API_CASE.name());
        stepRequest2.setRefType(ApiScenarioStepRefType.COPY.name());
        stepRequest2.setProjectId(DEFAULT_PROJECT_ID);
        stepRequest2.setCsvFileIds(List.of(fileMetadataId));

        ApiScenarioStepRequest stepRequest3 = new ApiScenarioStepRequest();
        stepRequest3.setId(IDGenerator.nextStr());
        stepRequest3.setVersionId(extBaseProjectVersionMapper.getDefaultVersion(DEFAULT_PROJECT_ID));
        stepRequest3.setConfig(new HashMap<>());
        stepRequest3.setEnable(true);
        stepRequest3.setStepType(ApiScenarioStepType.API.name());
        stepRequest3.setResourceId(apiDefinition.getId());
        stepRequest3.setName(apiDefinition.getName() + "3");
        stepRequest3.setRefType(ApiScenarioStepRefType.REF.name());
        stepRequest3.setProjectId(DEFAULT_PROJECT_ID);

        return new ArrayList<>() {{
            add(stepRequest);
            add(stepRequest2);
            add(stepRequest3);
        }};
    }

    private void initTestData() {
        Header header1 = new Header();
        header1.setKey("a");
        header1.setValue("aaa");

        Header header2 = new Header();
        header2.setKey("c");
        header2.setValue("cc");

        Header header3 = new Header();
        header3.setKey("Cookie");
        header3.setValue("b=c");

        ApiDefinitionAddRequest apiDefinitionAddRequest = new ApiDefinitionAddRequest();
        apiDefinitionAddRequest.setName("test scenario");
        apiDefinitionAddRequest.setProtocol(ApiConstants.HTTP_PROTOCOL);
        apiDefinitionAddRequest.setProjectId(DEFAULT_PROJECT_ID);
        apiDefinitionAddRequest.setMethod("POST");
        apiDefinitionAddRequest.setPath("/api/admin/posts");
        apiDefinitionAddRequest.setStatus(ApiDefinitionStatus.PREPARE.getValue());
        apiDefinitionAddRequest.setModuleId(moduleId);
        apiDefinitionAddRequest.setVersionId(extBaseProjectVersionMapper.getDefaultVersion(DEFAULT_PROJECT_ID));
        apiDefinitionAddRequest.setDescription("描述内容");
        apiDefinitionAddRequest.setName("test scenario");
        MsHTTPElement msHttpElement = MsHTTPElementTest.getAddProcessorHttpElement();
        msHttpElement.setHeaders(List.of(header1, header2, header3));
        msHttpElement.setPath(apiDefinitionAddRequest.getPath());
        QueryParam queryParam1 = new QueryParam();
        queryParam1.setEncode(true);
        queryParam1.setKey("aa");
        queryParam1.setValue("bbb");
        QueryParam queryParam2 = new QueryParam();
        queryParam2.setEncode(false);
        queryParam2.setKey("aa2");
        queryParam2.setValue("bbb2");
        msHttpElement.setQuery(List.of(queryParam1, queryParam2));
        apiDefinitionAddRequest.setRequest(getMsElementParam(msHttpElement));
        apiDefinitionAddRequest.setResponse("{}");
        apiDefinition = apiDefinitionService.create(apiDefinitionAddRequest, "admin");

        ApiTestCaseAddRequest apiTestCaseAddRequest = new ApiTestCaseAddRequest();
        apiTestCaseAddRequest.setApiDefinitionId(apiDefinition.getId());
        apiTestCaseAddRequest.setName("test");
        apiTestCaseAddRequest.setProjectId(DEFAULT_PROJECT_ID);
        apiTestCaseAddRequest.setPriority("P0");
        apiTestCaseAddRequest.setStatus("Underway");
        apiTestCaseAddRequest.setTags(new LinkedHashSet<>(List.of("tag1", "tag2")));
        apiTestCaseAddRequest.setRequest(getMsElementParam(msHttpElement));
        apiTestCase = apiTestCaseService.addCase(apiTestCaseAddRequest, "admin");
    }

    private Object getMsElementParam(MsHTTPElement msHTTPElement) {
        return JSON.parseObject(ApiDataUtils.toJSONString(msHTTPElement));
    }

    @Test
    @Order(2)
    public void update() throws Exception {
        // @@请求成功
        ApiScenarioUpdateRequest request = new ApiScenarioUpdateRequest();
        request.setId(addApiScenario.getId());
        request.setDescription("desc update");
        request.setName("test name update");
        request.setModuleId("default");
        request.setGrouped(false);
        request.setEnvironmentId(envId);
        request.setTags(List.of("tag1 update", "tag2 update"));
        request.setPriority("P0 update");
        request.setStatus(ApiScenarioStatus.DEPRECATED.name());
        List<ApiScenarioStepRequest> steps = getApiScenarioStepRequests();

        // 验证修改基础信息
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        assertUpdateApiScenario(request, request.getScenarioConfig(), addApiScenario.getId());

        // 验证清除步骤
        request.setSteps(List.of());
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        assertUpdateApiScenario(request, request.getScenarioConfig(), addApiScenario.getId());
        assertUpdateSteps(List.of(), new HashMap<>());

        // 验证添加步骤
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        Map<String, Object> steptDetailMap = new HashMap<>();
        MsHTTPElement msHttpElement = MsHTTPElementTest.getMsHttpElement();
        msHttpElement.setBody(ApiDebugControllerTests.addBodyLinkFile(msHttpElement.getBody(), fileMetadataId));
        steptDetailMap.put(steps.get(0).getId(), getMsHttpElementStr(msHttpElement));
        steptDetailMap.put(steps.get(1).getId(), getMsHttpElementStr(msHttpElement));

        request.setSteps(steps);
        request.setStepDetails(steptDetailMap);
        request.setScenarioConfig(getScenarioConfig());
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        assertUpdateSteps(steps, steptDetailMap);

        ApiScenarioCsvExample apiScenarioCsvExample = new ApiScenarioCsvExample();
        apiScenarioCsvExample.createCriteria().andScenarioIdEqualTo(addApiScenario.getId());
        List<ApiScenarioCsv> apiScenarioCsvs = apiScenarioCsvMapper.selectByExample(apiScenarioCsvExample);
        Map<String, ApiScenarioCsv> collect = apiScenarioCsvs.stream().collect(Collectors.toMap(ApiScenarioCsv::getFileId, t -> t));
        // 验证修改步骤
        steps.get(0).setName("test name update");
        CsvVariable csvVariable = request.getScenarioConfig().getVariable().getCsvVariables().get(0);
        request.getScenarioConfig().getVariable().getCsvVariables().get(0).setId(collect.get(csvVariable.getFileId()).getId());
        CsvVariable csvVariable1 = request.getScenarioConfig().getVariable().getCsvVariables().get(0);
        request.getScenarioConfig().getVariable().getCsvVariables().get(1).setId(collect.get(csvVariable1.getFileId()).getId());
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        assertUpdateSteps(steps, steptDetailMap);
        addApiScenarioSteps = steps;

        // 测试关联的文件更新
        testHandleFileAssociationUpgrade();

        // @@重名校验异常
        request.setName(anOtherAddApiScenario.getName());
        assertErrorCode(this.requestPost(DEFAULT_UPDATE, request), API_SCENARIO_EXIST);
        // @@校验日志
        checkLog(request.getId(), OperationLogType.UPDATE);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_SCENARIO_UPDATE, DEFAULT_UPDATE, request);
    }

    /**
     * 测试关联的文件更新
     * @throws Exception
     */
    public void testHandleFileAssociationUpgrade() throws Exception {
        List<ApiFile> originApiFiles = getApiFiles(fileMetadataId);
        MockMultipartFile file = new MockMultipartFile("file", "file_upload.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, "aa".getBytes());
        // 重新上传新文件
        String newFileId = baseFileManagementTestService.reUpload(fileMetadataId, file);
        // 更新关联的文件到最新文件
        baseFileManagementTestService.upgrade(fileMetadataId, addApiScenario.getId());
        // 校验文件是否替换
        Assertions.assertEquals(originApiFiles.size(), getApiFiles(newFileId).size());
        fileMetadataId = newFileId;
    }

    private List<ApiFile> getApiFiles(String fileId) {
        ApiScenarioStepBlobExample example = new ApiScenarioStepBlobExample();
        example.createCriteria().andScenarioIdEqualTo(addApiScenario.getId());
        List<ApiScenarioStepBlob> apiScenarioStepBlobs = apiScenarioStepBlobMapper.selectByExampleWithBLOBs(example);
        List<ApiFile> apiFiles = new ArrayList<>();
        for (ApiScenarioStepBlob apiScenarioStepBlob : apiScenarioStepBlobs) {
            try {
                AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(new String(apiScenarioStepBlob.getContent()), AbstractMsTestElement.class);
                apiFiles.addAll(apiCommonService.getApiFilesByFileId(fileId, msTestElement));
            } catch (Exception e) {}
        }
        return apiFiles;
    }

    @Test
    @Order(5)
    public void uploadTempFile() throws Exception {
        // @@请求成功
        MockMultipartFile file = getMockMultipartFile();
        String fileId = doUploadTempFile(file);

        // 校验文件存在
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFolder(DefaultRepositoryDir.getSystemTempDir() + "/" + fileId);
        fileRequest.setFileName(file.getOriginalFilename());
        Assertions.assertNotNull(FileCenter.getDefaultRepository().getFile(fileRequest));

        requestUploadPermissionTest(PermissionConstants.PROJECT_API_SCENARIO_ADD, UPLOAD_TEMP_FILE, file);
        requestUploadPermissionTest(PermissionConstants.PROJECT_API_SCENARIO_UPDATE, UPLOAD_TEMP_FILE, file);
    }

    @Test
    @Order(6)
    public void debug() throws Exception {
        mockPost("/api/debug", "");
        baseResourcePoolTestService.initProjectResourcePool();

        // @@请求成功
        ApiScenarioDebugRequest request = new ApiScenarioDebugRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setScenarioConfig(new ScenarioConfig());
        request.setEnvironmentId(envId);
        request.setGrouped(false);
        ScenarioStepConfig scenarioStepConfig = new ScenarioStepConfig();
        scenarioStepConfig.setEnableScenarioEnv(true);

        List<ApiScenarioStepRequest> steps = getApiScenarioStepRequests();
        ApiScenarioStepRequest stepRequest = new ApiScenarioStepRequest();
        stepRequest.setName("test step");
        stepRequest.setId(IDGenerator.nextStr());
        stepRequest.setId(IDGenerator.nextStr());
        stepRequest.setRefType(ApiScenarioStepRefType.REF.name());
        stepRequest.setResourceId(addApiScenario.getId());
        stepRequest.setResourceNum(addApiScenario.getNum().toString());
        stepRequest.setName(addApiScenario.getName());
        stepRequest.setStepType(ApiScenarioStepType.API_SCENARIO.name());
        stepRequest.setChildren(getApiScenarioStepRequests());
        stepRequest.setConfig(scenarioStepConfig);
        stepRequest.setProjectId(DEFAULT_PROJECT_ID);
        steps.add(stepRequest);
        ApiScenarioStepRequest copyScenarioStep = BeanUtils.copyBean(new ApiScenarioStepRequest(), stepRequest);
        copyScenarioStep.setRefType(ApiScenarioStepRefType.COPY.name());
        copyScenarioStep.setChildren(getApiScenarioStepRequests());
        steps.add(copyScenarioStep);
        ApiScenarioStepRequest partialScenarioStep = BeanUtils.copyBean(new ApiScenarioStepRequest(), stepRequest);
        partialScenarioStep.setRefType(ApiScenarioStepRefType.PARTIAL_REF.name());
        partialScenarioStep.setChildren(getApiScenarioStepRequests());
        steps.add(partialScenarioStep);
        request.setId(addApiScenario.getId());
        request.setSteps(steps);
        request.setStepDetails(new HashMap<>());
        request.setReportId(IDGenerator.nextStr());
        this.requestPostWithOk(DEBUG, request);

        request.setEnvironmentId(envGroupId);
        request.setGrouped(true);
        this.requestPostWithOk(DEBUG, request);

        ApiScenarioStepRequest pluginStep = new ApiScenarioStepRequest();
        pluginStep.setName("plugin step");
        pluginStep.setId(IDGenerator.nextStr());
        pluginStep.setRefType(ApiScenarioStepRefType.COPY.name());
        pluginStep.setRefType(ApiScenarioStepRefType.COPY.name());
        pluginStep.setResourceId("");
        pluginStep.setResourceNum("11111");
        pluginStep.setName("plugin step");
        pluginStep.setStepType(ApiScenarioStepType.API.name());
        pluginStep.setChildren(getApiScenarioStepRequests());
        pluginStep.setProjectId(DEFAULT_PROJECT_ID);
        request.getSteps().add(pluginStep);
        HashMap<Object, Object> pluginStepDetail = new HashMap<>();
        pluginStepDetail.put("polymorphicName", "MsTCPSampler");
        pluginStepDetail.put("port", "port");
        pluginStepDetail.put("projectId", DEFAULT_PROJECT_ID);
        request.getStepDetails().put(pluginStep.getId(), pluginStepDetail);
        request.getScenarioConfig().getOtherConfig().setEnableCookieShare(true);
        request.getScenarioConfig().getOtherConfig().setEnableGlobalCookie(false);

        Plugin plugin = addEnvTestPlugin();
        this.requestPostWithOk(DEBUG, request);
        pluginService.delete(plugin.getId());

        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_SCENARIO_EXECUTE, DEBUG, request);
    }

    public Plugin addEnvTestPlugin() throws Exception {
        PluginUpdateRequest request = new PluginUpdateRequest();
        File jarFile = new File(
                this.getClass().getClassLoader().getResource("file/tcpp-sampler-env-test.jar")
                        .getPath()
        );
        FileInputStream inputStream = new FileInputStream(jarFile);
        MockMultipartFile mockMultipartFile = new MockMultipartFile(jarFile.getName(), jarFile.getName(), "jar", inputStream);
        request.setName("env_test");
        request.setGlobal(true);
        request.setEnable(true);
        request.setCreateUser(ADMIN.name());
        return pluginService.add(request, mockMultipartFile);
    }

    private void initModule() {
        ModuleCreateRequest request = new ModuleCreateRequest();
        request.setName("test");
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setParentId(ModuleConstants.ROOT_NODE_PARENT_ID);
        moduleId = apiDefinitionModuleService.add(request, "admin");
    }

    private void initEnv() {
        EnvironmentRequest envRequest = new EnvironmentRequest();
        envRequest.setProjectId(DEFAULT_PROJECT_ID);
        envRequest.setName("test scenario debug");
        // 添加插件的环境配置，供后续测试使用
        Map<String, Map<String, Object>> pluginConfigMap = new HashMap<>();
        pluginConfigMap.put("tcpp-sampler", new HashMap<>());

        EnvScenarioScriptProcessor envScenarioScriptProcessor = new EnvScenarioScriptProcessor();
        envScenarioScriptProcessor.setScript("test");
        envScenarioScriptProcessor.setEnableCommonScript(false);
        envScenarioScriptProcessor.setAssociateScenarioResult(true);
        EnvScenarioScriptProcessor envScenarioScriptProcessor1 = BeanUtils.copyBean(new EnvScenarioScriptProcessor(), envScenarioScriptProcessor);
        envScenarioScriptProcessor1.setAssociateScenarioResult(false);
        SQLProcessor sqlProcessor = new SQLProcessor();
        sqlProcessor.setScript("select * from test");
        sqlProcessor.setName("select * from test");

        EnvironmentConfig environmentConfig = new EnvironmentConfig();
        EnvProcessorConfig preProcessorConfig = environmentConfig.getPreProcessorConfig();
        EnvProcessorConfig postProcessorConfig = environmentConfig.getPostProcessorConfig();
        List<MsProcessor> preProcessors = preProcessorConfig.getApiProcessorConfig().getScenarioProcessorConfig().getProcessors();
        preProcessors.add(envScenarioScriptProcessor);
        preProcessors.add(envScenarioScriptProcessor1);
        preProcessors.add(sqlProcessor);

        List<MsProcessor> postProcessors = postProcessorConfig.getApiProcessorConfig().getScenarioProcessorConfig().getProcessors();
        postProcessors.add(envScenarioScriptProcessor);
        postProcessors.add(envScenarioScriptProcessor1);
        postProcessors.add(sqlProcessor);

        EnvRequestScriptProcessor envRequestScriptProcessor = new EnvRequestScriptProcessor();
        envRequestScriptProcessor.setScript("test");
        envRequestScriptProcessor.setBeforeStepScript(true);
        envRequestScriptProcessor.setIgnoreProtocols(List.of("TCP"));
        EnvRequestScriptProcessor envRequestScriptProcessor1 = new EnvRequestScriptProcessor();
        envRequestScriptProcessor1.setScript("test1");
        envRequestScriptProcessor1.setBeforeStepScript(false);
        envRequestScriptProcessor1.setIgnoreProtocols(List.of());
        List<MsProcessor> preRequestProcessors = preProcessorConfig.getApiProcessorConfig().getRequestProcessorConfig().getProcessors();
        preRequestProcessors.add(envRequestScriptProcessor);
        preRequestProcessors.add(sqlProcessor);
        List<MsProcessor> postRequestProcessors = postProcessorConfig.getApiProcessorConfig().getRequestProcessorConfig().getProcessors();
        postRequestProcessors.add(envRequestScriptProcessor);
        postRequestProcessors.add(sqlProcessor);

        MsResponseCodeAssertion responseCodeAssertion = new MsResponseCodeAssertion();
        responseCodeAssertion.setExpectedValue("200");
        responseCodeAssertion.setCondition(MsAssertionCondition.EMPTY.name());
        responseCodeAssertion.setName("test");
        environmentConfig.getAssertionConfig().getAssertions().add(responseCodeAssertion);

        KeyValueEnableParam header1 = new KeyValueEnableParam();
        header1.setKey("a");
        header1.setValue("aa");

        KeyValueEnableParam header2 = new KeyValueEnableParam();
        header2.setKey("b");
        header2.setValue("bb");

        KeyValueEnableParam header3 = new KeyValueEnableParam();
        header3.setKey("Cookie");
        header3.setValue("a=b");

        HttpConfig httpNoneConfig = new HttpConfig();
        httpNoneConfig.setUrl("localhost:8081");
        httpNoneConfig.setType(HttpConfig.HttpConfigMatchType.NONE.name());
        httpNoneConfig.setHeaders(List.of(header1, header2, header3));

        HttpConfig httpModuleConfig = new HttpConfig();
        httpModuleConfig.setUrl("localhost:8081");
        httpModuleConfig.setType(HttpConfig.HttpConfigMatchType.MODULE.name());
        SelectModule selectModule = new SelectModule();
        selectModule.setModuleId(moduleId);
        selectModule.setContainChildModule(true);
        httpModuleConfig.getModuleMatchRule().setModules(List.of(selectModule));
        httpModuleConfig.setHeaders(List.of(header1, header2, header3));

        HttpConfig httpPathConfig = new HttpConfig();
        httpPathConfig.setUrl("localhost:8081");
        httpPathConfig.setType(HttpConfig.HttpConfigMatchType.PATH.name());
        httpPathConfig.getPathMatchRule().setPath("/test");
        httpPathConfig.getPathMatchRule().setCondition(HttpConfigPathMatchRule.MatchRuleCondition.CONTAINS.name());
        httpPathConfig.setHeaders(List.of(header1, header2, header3));

        environmentConfig.setHttpConfig(List.of(httpNoneConfig, httpModuleConfig, httpPathConfig));

        environmentConfig.setPluginConfigMap(pluginConfigMap);
        envRequest.setConfig(environmentConfig);
        Environment environment = environmentService.add(envRequest, "admin", null);
        envId = environment.getId();

        EnvironmentGroupRequest groupRequest = new EnvironmentGroupRequest();
        groupRequest.setProjectId(DEFAULT_PROJECT_ID);
        groupRequest.setName("test scenario debug");
        EnvironmentGroupProjectDTO environmentGroupProjectDTO = new EnvironmentGroupProjectDTO();
        environmentGroupProjectDTO.setEnvironmentId(environment.getId());
        environmentGroupProjectDTO.setProjectId(DEFAULT_PROJECT_ID);
        groupRequest.setEnvGroupProject(List.of(environmentGroupProjectDTO));
        envGroupId = environmentGroupService.add(groupRequest, "admin").getId();
    }

    @Test
    @Order(7)
    public void get() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(DEFAULT_GET, addApiScenario.getId());
        ApiScenarioDetail apiScenarioDetail = getResultData(mvcResult, ApiScenarioDetail.class);
        // 验证数据
        asserGetApiScenarioSteps(this.addApiScenarioSteps, apiScenarioDetail.getSteps());

        mvcResult = this.requestGetWithOkAndReturn(DEFAULT_GET, anOtherAddApiScenario.getId());
        apiScenarioDetail = getResultData(mvcResult, ApiScenarioDetail.class);
        // 验证数据
        Assertions.assertEquals(this.anOtherAddApiScenarioSteps.size(), apiScenarioDetail.getSteps().size());
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_SCENARIO_READ, DEFAULT_GET, addApiScenario.getId());
    }

    @Test
    @Order(7)
    public void getStepDetail() throws Exception {
        ApiScenarioDetail apiScenarioDetail = apiScenarioService.get(addApiScenario.getId());
        List<ApiScenarioStepDTO> steps = apiScenarioDetail.getSteps();
        requestGetStepDetail(steps);

        apiScenarioDetail = apiScenarioService.get(anOtherAddApiScenario.getId());
        steps = apiScenarioDetail.getSteps();
        requestGetStepDetail(steps);

        StepRequest stepRequest = new StepRequest();
        stepRequest.setStepId(addApiScenario.getId());
        stepRequest.setStepType("API_SCENARIO");
        stepRequest.setResourceId(addApiScenario.getId());
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_SCENARIO_READ, STEP_GET, stepRequest);
    }

    private void requestGetStepDetail(List<? extends ApiScenarioStepCommonDTO> steps) throws Exception {
        if (CollectionUtils.isEmpty(steps)) {
            return;
        }
        for (ApiScenarioStepCommonDTO step : steps) {
            StepRequest stepRequest = new StepRequest();
            stepRequest.setStepId(step.getId());
            stepRequest.setStepType(step.getStepType());
            stepRequest.setResourceId(step.getResourceId());
            this.requestPost(STEP_GET, stepRequest);
            List<? extends ApiScenarioStepCommonDTO> children = step.getChildren();
            requestGetStepDetail(children);
        }
    }

    private void asserGetApiScenarioSteps(List<? extends ApiScenarioStepCommonDTO> addApiScenarioSteps, List<? extends ApiScenarioStepCommonDTO> steps) {
        if (addApiScenarioSteps == null || steps == null) {
            Assertions.assertEquals(addApiScenarioSteps, null);
            Assertions.assertEquals(steps, null);
            return;
        }
        Assertions.assertEquals(addApiScenarioSteps.size(), steps.size());
        for (int i = 0; i < addApiScenarioSteps.size(); i++) {
            ApiScenarioStepRequest stepRequest = (ApiScenarioStepRequest) addApiScenarioSteps.get(i);
            ApiScenarioStepDTO stepDTO = (ApiScenarioStepDTO) steps.get(i);
            Assertions.assertEquals(BeanUtils.copyBean(new ApiScenarioStepCommonDTO(), stepRequest), BeanUtils.copyBean(new ApiScenarioStepCommonDTO(), stepDTO));
            asserGetApiScenarioSteps(stepRequest.getChildren(), stepDTO.getChildren());
        }
    }

    private String doUploadTempFile(MockMultipartFile file) throws Exception {
        return JSON.parseObject(requestUploadFileWithOkAndReturn(UPLOAD_TEMP_FILE, file)
                        .getResponse()
                        .getContentAsString(), ResultHolder.class)
                .getData().toString();
    }

    private static MockMultipartFile getMockMultipartFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file_upload.JPG",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "Hello, World!".getBytes()
        );
        return file;
    }

    @Test
    @Order(9)
    public void deleteToGc() throws Exception {
        // @@请求成功
        this.requestGetWithOk(DELETE_TO_GC, addApiScenario.getId());
        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(addApiScenario.getId());
        Assertions.assertTrue(apiScenario.getDeleted());
        // @@校验日志
        checkLog(addApiScenario.getId(), OperationLogType.DELETE);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_SCENARIO_DELETE, DELETE_TO_GC, addApiScenario.getId());
    }

    @Test
    @Order(10)
    public void delete() throws Exception {
        // @@请求成功
        ApiScenarioCsv apiScenarioCsv = new ApiScenarioCsv();
        apiScenarioCsv.setId(IDGenerator.nextStr());
        apiScenarioCsv.setScenarioId(addApiScenario.getId());
        apiScenarioCsv.setFileId(fileMetadataId);
        apiScenarioCsv.setName("csv变量");
        apiScenarioCsv.setScope(CsvVariable.CsvVariableScope.SCENARIO.name());
        apiScenarioCsv.setProjectId(DEFAULT_PROJECT_ID);
        apiScenarioCsvMapper.insertSelective(apiScenarioCsv);

        this.requestGetWithOk(DEFAULT_DELETE, addApiScenario.getId());
        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(addApiScenario.getId());
        ApiScenarioBlob apiScenarioBlob = apiScenarioBlobMapper.selectByPrimaryKey(addApiScenario.getId());
        Assertions.assertNull(apiScenario);
        Assertions.assertNull(apiScenarioBlob);
        // @@校验日志
        checkLog(addApiScenario.getId(), OperationLogType.DELETE);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_SCENARIO_DELETE, DEFAULT_DELETE, addApiScenario.getId());
    }

    @Test
    @Order(11)
    public void page() throws Exception {
        // @@请求成功
        initApiScenario();
        ApiScenarioPageRequest pageRequest = new ApiScenarioPageRequest();
        pageRequest.setProjectId(DEFAULT_PROJECT_ID);
        pageRequest.setPageSize(10);
        pageRequest.setCurrent(1);
        MvcResult mvcResult = requestPostAndReturn(DEFAULT_PAGE, pageRequest);
        Pager<?> returnPager = getResultData(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), pageRequest.getCurrent());
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(((List<ApiScenarioDTO>) returnPager.getList()).size() <= pageRequest.getPageSize());

        //查询api-scenario-id1的数据
        pageRequest.setScenarioId("api-scenario-id1");
        mvcResult = requestPostAndReturn(DEFAULT_PAGE, pageRequest);
        returnPager = getResultData(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), pageRequest.getCurrent());

        List<ApiScenarioDTO> scenarioDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ApiScenarioDTO.class);
        scenarioDTOS.forEach(caseDTO -> Assertions.assertEquals(caseDTO.getId(), "api-scenario-id1"));

        //查询模块为moduleId1的数据
        pageRequest.setScenarioId(null);
        pageRequest.setModuleIds(List.of("scenario-moduleId"));
        mvcResult = requestPostAndReturn(DEFAULT_PAGE, pageRequest);
        returnPager = getResultData(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), pageRequest.getCurrent());
        scenarioDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ApiScenarioDTO.class);
        scenarioDTOS.forEach(scenario -> Assertions.assertEquals(scenario.getModuleId(), "scenario-moduleId"));

        pageRequest.setModuleIds(null);
        pageRequest.setSort(new HashMap<>() {{
            put("createTime", "asc");
        }});
        requestPostAndReturn(DEFAULT_PAGE, pageRequest);

        pageRequest = new ApiScenarioPageRequest();
        pageRequest.setProjectId(DEFAULT_PROJECT_ID);
        pageRequest.setPageSize(10);
        pageRequest.setCurrent(1);
        pageRequest.setModuleIds(List.of("scenario-moduleId"));
        pageRequest.setFilter(new HashMap<>() {{
            put("priority", List.of("P0"));
        }});
        mvcResult = requestPostAndReturn(DEFAULT_PAGE, pageRequest);
        returnPager = getResultData(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), pageRequest.getCurrent());
        scenarioDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ApiScenarioDTO.class);
        scenarioDTOS.forEach(scenario -> Assertions.assertEquals(scenario.getPriority(), "P0"));

        //校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_SCENARIO_READ, DEFAULT_PAGE, pageRequest);
    }


    @Test
    @Order(11)
    public void testGetAssociatedCase() throws Exception {
        for (int i = 0; i < 2; i++) {
            ApiScenario apiScenario = new ApiScenario();
            apiScenario.setId("test-api-scenario-id" + i);
            apiScenario.setProjectId(DEFAULT_PROJECT_ID);
            apiScenario.setNum(NumGenerator.nextNum(DEFAULT_PROJECT_ID, ApplicationNumScope.API_SCENARIO));
            apiScenario.setName(StringUtils.join("建国批量测试接口场景-", apiScenario.getId()));
            apiScenario.setModuleId("test-associated");
            apiScenario.setStatus("未规划");
            apiScenario.setPos(1L);
            apiScenario.setPriority("P0");
            apiScenario.setLatest(true);
            apiScenario.setVersionId("1.0");
            apiScenario.setRefId(apiScenario.getId());
            apiScenario.setCreateTime(System.currentTimeMillis());
            apiScenario.setUpdateTime(System.currentTimeMillis());
            apiScenario.setCreateUser("admin");
            apiScenario.setUpdateUser("admin");
            apiScenarioMapper.insertSelective(apiScenario);
        }
        for (int i = 0; i < 10; i++) {
            ApiScenarioStep step1 = new ApiScenarioStep();
            step1.setId(IDGenerator.nextStr());
            step1.setScenarioId("test-api-scenario-id1");
            step1.setName("test" + "_" + IDGenerator.nextStr());
            step1.setSort(0L);
            step1.setEnable(true);
            step1.setResourceId("test-api-scenario-id0");
            step1.setResourceNum("test-resource-num");
            if (i % 2 == 0) {
                step1.setRefType("COPY");
            } else {
                step1.setRefType("REF");
            }
            step1.setProjectId(DEFAULT_PROJECT_ID);
            apiScenarioStepMapper.insertSelective(step1);
        }
        ApiScenarioAssociationPageRequest request = new ApiScenarioAssociationPageRequest();
        request.setPageSize(10);
        request.setCurrent(1);
        this.requestPost("/association/page", request, status().is4xxClientError());
        request.setId("test-api-scenario-id1");
        this.requestPostAndReturn("/association/page", request);
        request.setId("test-api-scenario-id0");
        this.requestPostAndReturn("association/page", request);

    }

    @Test
    @Order(12)
    public void follow() throws Exception {
        // @@请求成功
        this.requestGetWithOk(FOLLOW + "api-scenario-id1");
        ApiScenarioFollowerExample example = new ApiScenarioFollowerExample();
        example.createCriteria().andApiScenarioIdEqualTo("api-scenario-id1").andUserIdEqualTo("admin");
        List<ApiScenarioFollower> followers = apiScenarioFollowerMapper.selectByExample(example);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(followers));
        // @@校验日志
        checkLog("api-scenario-id1", OperationLogType.UPDATE);
        assertErrorCode(this.requestGet(FOLLOW + "111"), NOT_FOUND);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_SCENARIO_UPDATE, FOLLOW + "api-scenario-id1");

        // @@请求成功
        this.requestGetWithOk(FOLLOW + "api-scenario-id1");
        example = new ApiScenarioFollowerExample();
        example.createCriteria().andApiScenarioIdEqualTo("api-scenario-id1").andUserIdEqualTo("admin");
        followers = apiScenarioFollowerMapper.selectByExample(example);
        Assertions.assertTrue(CollectionUtils.isEmpty(followers));
        // @@校验日志
        checkLog("api-scenario-id1", OperationLogType.UPDATE);
        assertErrorCode(this.requestGet(FOLLOW + "111"), NOT_FOUND);
    }

    @Test
    @Order(12)
    public void updateStatus() throws Exception {
        List<ApiScenario> apiScenarioList = apiScenarioMapper.selectByExample(new ApiScenarioExample());
        String scenarioId = apiScenarioList.getFirst().getId();
        // @@请求成功
        this.requestGetWithOk(UPDATE_STATUS + "/" + scenarioId + "/Underway");
        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(scenarioId);
        Assertions.assertEquals(apiScenario.getStatus(), "Underway");
        // @@校验日志
        checkLog(scenarioId, OperationLogType.UPDATE);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_SCENARIO_UPDATE, UPDATE_STATUS + "/" + scenarioId + "/Underway");
    }

    @Test
    @Order(12)
    public void updatePriority() throws Exception {
        // @@请求成功
        this.requestGetWithOk(UPDATE_PRIORITY + "/" + "api-scenario-id1" + "/P1");
        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey("api-scenario-id1");
        Assertions.assertEquals(apiScenario.getPriority(), "P1");
        // @@校验日志
        checkLog("api-scenario-id1", OperationLogType.UPDATE);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_SCENARIO_UPDATE, UPDATE_PRIORITY + "/" + "api-scenario-id1" + "/P1");
    }

    @Test
    @Order(13)
    public void batchEdit() throws Exception {
        // 追加标签
        ApiScenarioBatchEditRequest request = new ApiScenarioBatchEditRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setType("Tags");
        request.setAppendTag(true);
        request.setSelectAll(true);
        request.setTags(new LinkedHashSet<>(List.of("tag1", "tag3", "tag4")));
        requestPostAndReturn(BATCH_EDIT, request);
        ApiScenarioExample example = new ApiScenarioExample();
        List<String> ids = extApiScenarioMapper.getIds(request, false);
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andDeletedEqualTo(false).andIdIn(ids);
        apiScenarioMapper.selectByExample(example).forEach(apiTestCase -> {
            Assertions.assertTrue(apiTestCase.getTags().contains("tag1"));
            Assertions.assertTrue(apiTestCase.getTags().contains("tag3"));
            Assertions.assertTrue(apiTestCase.getTags().contains("tag4"));
        });
        //覆盖标签
        request.setTags(new LinkedHashSet<>(List.of("tag1")));
        request.setAppendTag(false);
        requestPostAndReturn(BATCH_EDIT, request);
        apiScenarioMapper.selectByExample(example).forEach(scenario -> {
            Assertions.assertEquals(scenario.getTags(), List.of("tag1"));
        });
        //标签为空  报错
        request.setTags(new LinkedHashSet<>());
        this.requestPost(BATCH_EDIT, request, ERROR_REQUEST_MATCHER);
        //ids为空的时候
        request.setTags(new LinkedHashSet<>(List.of("tag1")));
        request.setSelectAll(true);
        List<ApiScenario> apiScenarioList = apiScenarioMapper.selectByExample(example);
        //提取所有的id
        List<String> apiIdList = apiScenarioList.stream().map(ApiScenario::getId).toList();
        request.setSelectIds(apiIdList);
        request.setExcludeIds(apiIdList);
        requestPostAndReturn(BATCH_EDIT, request);

        //优先级
        request.setTags(new LinkedHashSet<>());
        request.setSelectAll(true);
        request.setType("Priority");
        request.setModuleIds(List.of("scenario-moduleId"));
        request.setPriority("P3");
        request.setExcludeIds(new ArrayList<>());
        requestPostAndReturn(BATCH_EDIT, request);
        //判断数据的优先级是不是P3
        example.clear();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andDeletedEqualTo(false).andModuleIdEqualTo("scenario-moduleId");
        List<ApiScenario> apiScenarios = apiScenarioMapper.selectByExample(example);

        apiScenarios.forEach(apiTestCase -> Assertions.assertEquals(apiTestCase.getPriority(), "P3"));
        //优先级数据为空
        request.setPriority(null);
        this.requestPost(BATCH_EDIT, request, ERROR_REQUEST_MATCHER);
        //ids为空的时候
        request.setPriority("P3");
        request.setSelectAll(false);
        request.setSelectIds(List.of("api-scenario-id1"));
        request.setExcludeIds(List.of("api-scenario-id1"));
        requestPostAndReturn(BATCH_EDIT, request);
        //状态
        request.setPriority(null);
        request.setType("Status");
        request.setStatus("Completed");
        request.setSelectAll(true);
        request.setExcludeIds(new ArrayList<>());
        requestPostAndReturn(BATCH_EDIT, request);
        //判断数据的状态是不是Completed
        apiScenarios = apiScenarioMapper.selectByExample(example);
        apiScenarios.forEach(apiTestCase -> Assertions.assertEquals(apiTestCase.getStatus(), "Completed"));
        //状态数据为空
        request.setStatus(null);
        this.requestPost(BATCH_EDIT, request, ERROR_REQUEST_MATCHER);
        //环境
        request.setStatus(null);
        request.setType("Environment");
        EnvironmentExample environmentExample = new EnvironmentExample();
        environmentExample.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andMockEqualTo(true);
        List<Environment> environments = environmentMapper.selectByExample(environmentExample);
        request.setEnvId(environments.get(0).getId());
        requestPostAndReturn(BATCH_EDIT, request);
        //判断数据的环境是不是environments.get(0).getId()
        apiScenarios = apiScenarioMapper.selectByExample(example);
        apiScenarios.forEach(apiTestCase -> Assertions.assertEquals(apiTestCase.getEnvironmentId(), environments.get(0).getId()));

        //环境数据为空
        request.setEnvId(null);
        this.requestPost(BATCH_EDIT, request, ERROR_REQUEST_MATCHER);
        //环境不存在
        request.setEnvId("111");
        this.requestPost(BATCH_EDIT, request, ERROR_REQUEST_MATCHER);
        //环境组
        request.setGrouped(true);
        request.setGroupId("scenario-environment-group-id");
        requestPostAndReturn(BATCH_EDIT, request);
        apiScenarios = apiScenarioMapper.selectByExample(example);
        apiScenarios.forEach(apiTestCase -> {
            Assertions.assertEquals(apiTestCase.getGrouped(), true);
            Assertions.assertEquals(apiTestCase.getEnvironmentId(), "scenario-environment-group-id");
        });


        //环境组数据为空
        request.setGroupId(null);
        this.requestPost(BATCH_EDIT, request, ERROR_REQUEST_MATCHER);
        //环境组不存在
        request.setGroupId("111");
        this.requestPost(BATCH_EDIT, request, ERROR_REQUEST_MATCHER);
        //类型错误
        request.setType("111");
        this.requestPost(BATCH_EDIT, request, ERROR_REQUEST_MATCHER);

        //校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_SCENARIO_UPDATE, BATCH_EDIT, request);

    }

    @Test
    @Order(15)
    public void trashPage() throws Exception {
        // @@请求成功
        initApiScenarioTrash();
        ApiScenarioPageRequest pageRequest = new ApiScenarioPageRequest();
        pageRequest.setProjectId(DEFAULT_PROJECT_ID);
        pageRequest.setPageSize(10);
        pageRequest.setCurrent(1);
        MvcResult mvcResult = requestPostAndReturn(TRASH_PAGE, pageRequest);
        Pager<?> returnPager = getResultData(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), pageRequest.getCurrent());
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(((List<ApiScenarioDTO>) returnPager.getList()).size() <= pageRequest.getPageSize());

        //查询api-scenario-id1的数据
        pageRequest.setScenarioId("trash-api-scenario-id1");
        mvcResult = requestPostAndReturn(TRASH_PAGE, pageRequest);
        returnPager = getResultData(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), pageRequest.getCurrent());

        List<ApiScenarioDTO> scenarioDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ApiScenarioDTO.class);
        scenarioDTOS.forEach(caseDTO -> Assertions.assertEquals(caseDTO.getId(), "trash-api-scenario-id1"));

        //查询模块为moduleId1的数据
        pageRequest.setScenarioId(null);
        pageRequest.setModuleIds(List.of("scenario-moduleId-trash"));
        mvcResult = requestPostAndReturn(TRASH_PAGE, pageRequest);
        returnPager = getResultData(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), pageRequest.getCurrent());
        scenarioDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ApiScenarioDTO.class);
        scenarioDTOS.forEach(scenario -> Assertions.assertEquals(scenario.getModuleId(), "scenario-moduleId-trash"));

        pageRequest.setModuleIds(null);
        pageRequest.setSort(new HashMap<>() {{
            put("createTime", "asc");
        }});
        requestPostAndReturn(TRASH_PAGE, pageRequest);
        //校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_SCENARIO_READ, TRASH_PAGE, pageRequest);
    }

    /*
        Order 21 - 30 是测试批量操作的。

           批量测试的数据结构：
                建国批量测试场景模块_default（测试批量移动）
                |
                |
                建国批量测试场景模块_1    ->50条数据 有标签和环境组     批量测试copy之后的数据为：700
                |
                |
                建国批量测试场景模块_51    ->50条数据    有环境
                    |
                    |
                    建国批量测试场景模块_101    ->50条数据   有blob       批量测试copy之后的数据为：100
                        |
                        |
                        建国批量测试场景模块_151    ->50条数据   有step
                            |
                            |
                            建国批量测试场景模块_201    ->50条数据   有step和blob
                            |
                            |
                            建国批量测试场景模块_251    ->250条数据  有文件
             */

    @Test
    @Order(21)
    void batchCopy() throws Exception {
        String testUrl = "/batch-operation/copy";
        if (CollectionUtils.isEmpty(BATCH_OPERATION_SCENARIO_ID)) {
            this.batchCreateScenarios();
        }

        /*
        正例测试 1.超过300条的批量复制
                2.移动到新模块

                测试数据：使用建国批量测试场景模块_200模块为查询条件，和建国批量测试场景模块_250里的所有id，复制到 【建国批量测试场景模块_1】中
         */
        ApiScenarioBatchCopyMoveRequest request = new ApiScenarioBatchCopyMoveRequest();
        request.setSelectIds(BATCH_OPERATION_SCENARIO_ID.subList(250, 500));
        request.setModuleIds(List.of(BATCH_OPERATION_SCENARIO_MODULE_MAP.get("建国批量测试场景模块_201")));
        request.setTargetModuleId(BATCH_OPERATION_SCENARIO_MODULE_MAP.get("建国批量测试场景模块_1"));
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSelectAll(true);

        //先测试一下没有开启模块时接口能否使用
        apiScenarioBatchOperationTestService.removeApiModule(DEFAULT_PROJECT_ID);
        this.requestPost(testUrl, request).andExpect(status().is5xxServerError());
        //恢复
        apiScenarioBatchOperationTestService.resetProjectModule(DEFAULT_PROJECT_ID);

        MvcResult result = this.requestPostAndReturn(testUrl, request);
        ResultHolder resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        ApiScenarioBatchOperationResponse resultResponse = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), ApiScenarioBatchOperationResponse.class);
        //检查返回值
        Assertions.assertEquals(resultResponse.getSuccess(), 300);
        //数据库级别的检查
        apiScenarioBatchOperationTestService.checkBatchCopy(BATCH_OPERATION_SCENARIO_ID.subList(200, 500), resultResponse.getSuccessData().stream().map(OperationDataInfo::getId).toList(), 50, request);

        //本次测试涉及到的场景ID
        List<String> operationScenarioIds = new ArrayList<>();
        resultResponse.getSuccessData().forEach(item -> operationScenarioIds.add(item.getId()));
        //增加日志检查
        operationScenarioIds.forEach(item -> {
            LOG_CHECK_LIST.add(
                    new CheckLogModel(item, OperationLogType.COPY, "/api/scenario/batch-operation/copy")
            );
        });

        //        2.没有数据
        request = new ApiScenarioBatchCopyMoveRequest();
        request.setTargetModuleId(BATCH_OPERATION_SCENARIO_MODULE_MAP.get("建国批量测试场景模块_1"));
        request.setProjectId(DEFAULT_PROJECT_ID);
        result = this.requestPostAndReturn(testUrl, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        resultResponse = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), ApiScenarioBatchOperationResponse.class);
        //检查返回值
        Assertions.assertEquals(resultResponse.getSuccess(), 0);

        /*
        正例测试
            3.移动到当前模块
            4.移动到新模块
            5.移动的场景中，有名字以copy开头的。 不会重复生成copy_copy_
            6.要移动的场景中，有名字以"_"+自身num结尾的

                测试数据：建国批量测试场景模块_1中所有的数据（350条）再复制到当前数据里
         */
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andModuleIdEqualTo(BATCH_OPERATION_SCENARIO_MODULE_MAP.get("建国批量测试场景模块_1"));
        List<ApiScenario> reCopyScenarios = apiScenarioMapper.selectByExample(example);


        request = new ApiScenarioBatchCopyMoveRequest();
        request.setModuleIds(List.of(BATCH_OPERATION_SCENARIO_MODULE_MAP.get("建国批量测试场景模块_1")));
        request.setTargetModuleId(BATCH_OPERATION_SCENARIO_MODULE_MAP.get("建国批量测试场景模块_1"));
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSelectAll(true);

        result = this.requestPostAndReturn(testUrl, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        resultResponse = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), ApiScenarioBatchOperationResponse.class);
        //检查返回值
        Assertions.assertEquals(resultResponse.getSuccess(), reCopyScenarios.size());
        //数据库级别的检查
        apiScenarioBatchOperationTestService.checkBatchCopy(new ArrayList<>(reCopyScenarios.stream().map(ApiScenario::getId).toList()), resultResponse.getSuccessData().stream().map(OperationDataInfo::getId).toList(), 350, request);


        /*
            正例测试
                5.移动的场景中，有名字长度在250以上的

                测试数据：【建国批量测试场景模块_101】的用例修改名字长度为255.  然后复制到【建国批量测试场景模块_101】中
         */
        apiScenarioBatchOperationTestService.updateNameToTestByModuleId(BATCH_OPERATION_SCENARIO_MODULE_MAP.get("建国批量测试场景模块_101"));

        request = new ApiScenarioBatchCopyMoveRequest();
        request.setModuleIds(List.of(BATCH_OPERATION_SCENARIO_MODULE_MAP.get("建国批量测试场景模块_101")));
        request.setTargetModuleId(BATCH_OPERATION_SCENARIO_MODULE_MAP.get("建国批量测试场景模块_101"));
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSelectAll(true);

        result = this.requestPostAndReturn(testUrl, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        resultResponse = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), ApiScenarioBatchOperationResponse.class);
        //检查返回值
        Assertions.assertEquals(resultResponse.getSuccess(), 50);
        //数据库级别的检查
        apiScenarioBatchOperationTestService.checkBatchCopy(BATCH_OPERATION_SCENARIO_ID.subList(100, 150), resultResponse.getSuccessData().stream().map(OperationDataInfo::getId).toList(), 50, request);

        //校验权限
        this.requestPostPermissionTest(PermissionConstants.PROJECT_API_SCENARIO_ADD, testUrl, request);

        //1.反例测试 ->模块不存在
        request = new ApiScenarioBatchCopyMoveRequest();
        request.setTargetModuleId(IDGenerator.nextStr());
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSelectAll(true);
        this.requestPost(testUrl, request).andExpect(status().is5xxServerError());
        //2.反例测试 ->要移动的模块是别的项目下的
        String otherProjectModuleId = this.createModule(500, IDGenerator.nextStr(), ModuleConstants.ROOT_NODE_PARENT_ID);
        request = new ApiScenarioBatchCopyMoveRequest();
        request.setTargetModuleId(otherProjectModuleId);
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSelectAll(true);
        this.requestPost(testUrl, request).andExpect(status().is5xxServerError());
        //3.反例测试 ->参数校验：项目ID为空 模块ID为空
        request = new ApiScenarioBatchCopyMoveRequest();
        request.setTargetModuleId(otherProjectModuleId);
        this.requestPost(testUrl, request).andExpect(status().isBadRequest());

        request = new ApiScenarioBatchCopyMoveRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        this.requestPost(testUrl, request).andExpect(status().isBadRequest());
    }

    @Test
    @Order(22)
    void batchMove() throws Exception {
        String testUrl = "/batch-operation/move";
        if (CollectionUtils.isEmpty(BATCH_OPERATION_SCENARIO_ID)) {
            this.batchCopy();
        }
        String moduleIdDefault = BATCH_OPERATION_SCENARIO_MODULE_MAP.get("建国批量测试场景模块_default");
        String moduleId201 = BATCH_OPERATION_SCENARIO_MODULE_MAP.get("建国批量测试场景模块_201");
        String moduleId251 = BATCH_OPERATION_SCENARIO_MODULE_MAP.get("建国批量测试场景模块_251");

        //本次测试涉及到的场景ID
        List<String> operationScenarioIds = new ArrayList<>(BATCH_OPERATION_SCENARIO_ID.subList(200, 500));

        /*
        正例测试
            1.超过300条的批量移动
            2.移动到当前模块 名称有重复的

            测试数据：使用建国批量测试场景模块_201模块为查询条件，和建国批量测试场景模块_251里的所有id，移动到 【建国批量测试场景模块_default】中，测试1
                    然后再指定具体id移动，测试2

                 最后：分批移动回去： 指定具体id移动回_251,  再指定moduleId为_default移动回_200
         */
        ApiScenarioBatchCopyMoveRequest request = new ApiScenarioBatchCopyMoveRequest();
        request.setSelectIds(BATCH_OPERATION_SCENARIO_ID.subList(250, 500));
        request.setModuleIds(List.of(moduleId201));
        request.setTargetModuleId(moduleIdDefault);
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSelectAll(true);

        //先测试一下没有开启模块时接口能否使用
        apiScenarioBatchOperationTestService.removeApiModule(DEFAULT_PROJECT_ID);
        this.requestPost(testUrl, request).andExpect(status().is5xxServerError());
        //恢复
        apiScenarioBatchOperationTestService.resetProjectModule(DEFAULT_PROJECT_ID);

        MvcResult result = this.requestPostAndReturn(testUrl, request);
        ResultHolder resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        ApiScenarioBatchOperationResponse resultResponse = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), ApiScenarioBatchOperationResponse.class);
        //检查返回值
        Assertions.assertEquals(resultResponse.getSuccess(), 300);
        //数据库级别的检查
        apiScenarioBatchOperationTestService.checkBatchMove(
                BATCH_OPERATION_SCENARIO_ID.subList(200, 500),
                0,
                new HashMap<>() {{
                    this.put(moduleId201, 0L);
                    this.put(moduleId251, 0L);
                }}, request);
        //增加日志检查
        operationScenarioIds.forEach(item -> {
            LOG_CHECK_LIST.add(
                    new CheckLogModel(item, OperationLogType.UPDATE, "/api/scenario/batch-operation/move")
            );
        });
        //重复关联
        result = this.requestPostAndReturn(testUrl, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        resultResponse = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), ApiScenarioBatchOperationResponse.class);
        Assertions.assertEquals(resultResponse.getSuccess(), 0);
        Assertions.assertEquals(resultResponse.getError(), 250);

        //指定具体id移动回_251,  再指定moduleId为_default移动回_200
        request = new ApiScenarioBatchCopyMoveRequest();
        request.setSelectIds(BATCH_OPERATION_SCENARIO_ID.subList(250, 500));
        request.setTargetModuleId(moduleId251);
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSelectAll(false);
        result = this.requestPostAndReturn(testUrl, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        resultResponse = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), ApiScenarioBatchOperationResponse.class);
        Assertions.assertEquals(resultResponse.getSuccess(), 250);
        apiScenarioBatchOperationTestService.checkBatchMove(
                BATCH_OPERATION_SCENARIO_ID.subList(250, 500),
                0,
                new HashMap<>() {{
                    this.put(moduleIdDefault, 50L);
                }}, request);

        //指定moduleId为_default移动回_201
        request = new ApiScenarioBatchCopyMoveRequest();
        request.setModuleIds(Collections.singletonList(moduleIdDefault));
        request.setTargetModuleId(moduleId201);
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSelectAll(true);
        result = this.requestPostAndReturn(testUrl, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        resultResponse = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), ApiScenarioBatchOperationResponse.class);
        Assertions.assertEquals(resultResponse.getSuccess(), 50);
        apiScenarioBatchOperationTestService.checkBatchMove(
                BATCH_OPERATION_SCENARIO_ID.subList(200, 250),
                0,
                new HashMap<>() {{
                    this.put(moduleIdDefault, 0L);
                }}, request);

        // 没有数据
        request = new ApiScenarioBatchCopyMoveRequest();
        request.setTargetModuleId(moduleIdDefault);
        request.setProjectId(DEFAULT_PROJECT_ID);
        result = this.requestPostAndReturn(testUrl, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        resultResponse = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), ApiScenarioBatchOperationResponse.class);
        //检查返回值
        Assertions.assertEquals(resultResponse.getSuccess(), 0);

        //1.反例测试 ->模块不存在
        request = new ApiScenarioBatchCopyMoveRequest();
        request.setTargetModuleId(IDGenerator.nextStr());
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSelectAll(true);
        this.requestPost(testUrl, request).andExpect(status().is5xxServerError());
        //2.反例测试 ->要移动的模块是别的项目下的
        String otherProjectModuleId = this.createModule(500, IDGenerator.nextStr(), ModuleConstants.ROOT_NODE_PARENT_ID);
        request = new ApiScenarioBatchCopyMoveRequest();
        request.setTargetModuleId(otherProjectModuleId);
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSelectAll(true);
        this.requestPost(testUrl, request).andExpect(status().is5xxServerError());
        //3.反例测试 ->参数校验：项目ID为空 模块ID为空
        request = new ApiScenarioBatchCopyMoveRequest();
        request.setTargetModuleId(otherProjectModuleId);
        this.requestPost(testUrl, request).andExpect(status().isBadRequest());

        request = new ApiScenarioBatchCopyMoveRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        this.requestPost(testUrl, request).andExpect(status().isBadRequest());
    }

    @Test
    @Order(23)
    void scheduleTest() throws Exception {
        String testUrl = "/schedule-config";

        if (CollectionUtils.isEmpty(BATCH_OPERATION_SCENARIO_ID)) {
            this.batchCreateScenarios();
        }

        //使用最后一个场景ID用于做定时任务的测试
        String scenarioId = BATCH_OPERATION_SCENARIO_ID.getLast();
        ApiScenarioScheduleConfigRequest request = new ApiScenarioScheduleConfigRequest();

        request.setScenarioId(scenarioId);
        request.setEnable(true);
        request.setCron("0 0 0 * * ?");

        //先测试一下没有开启模块时接口能否使用
        apiScenarioBatchOperationTestService.removeApiModule(DEFAULT_PROJECT_ID);
        this.requestPost(testUrl, request).andExpect(status().is5xxServerError());
        //恢复
        apiScenarioBatchOperationTestService.resetProjectModule(DEFAULT_PROJECT_ID);
        MvcResult result = this.requestPostAndReturn(testUrl, request);
        ResultHolder resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        String scheduleId = resultHolder.getData().toString();
        apiScenarioBatchOperationTestService.checkSchedule(scheduleId, scenarioId, request.isEnable());

        //增加日志检查
        LOG_CHECK_LIST.add(
                new CheckLogModel(scenarioId, OperationLogType.UPDATE, "/api/scenario/schedule-config")
        );

        //关闭
        request.setEnable(false);
        result = this.requestPostAndReturn(testUrl, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        String newScheduleId = resultHolder.getData().toString();
        //检查两个scheduleId是否相同
        Assertions.assertEquals(scheduleId, newScheduleId);
        apiScenarioBatchOperationTestService.checkSchedule(newScheduleId, scenarioId, request.isEnable());

        //配置configMap
        request.setEnable(true);
        request.setConfig(new ApiRunModeConfigDTO());
        result = this.requestPostAndReturn(testUrl, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        newScheduleId = resultHolder.getData().toString();
        apiScenarioBatchOperationTestService.checkSchedule(newScheduleId, scenarioId, request.isEnable());

        //清空configMap
        request.setConfig(new ApiRunModeConfigDTO());
        request.setEnable(false);
        result = this.requestPostAndReturn(testUrl, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        newScheduleId = resultHolder.getData().toString();
        apiScenarioBatchOperationTestService.checkSchedule(newScheduleId, scenarioId, request.isEnable());

        //测试各种corn表达式用于校验正则的准确性
        String[] cornStrArr = new String[]{
                "0 0 12 * * ?", //每天中午12点触发
                "0 15 10 ? * *", //每天上午10:15触发
                "0 15 10 * * ?", //每天上午10:15触发
                "0 15 10 * * ? *",//每天上午10:15触发
                "0 15 10 * * ? 2048",//2008年的每天上午10:15触发
                "0 * 10 * * ?",//每天上午10:00至10:59期间的每1分钟触发
                "0 0/5 10 * * ?",//每天上午10:00至10:55期间的每5分钟触发
                "0 0/5 10,16 * * ?",//每天上午10:00至10:55期间和下午4:00至4:55期间的每5分钟触发
                "0 0-5 10 * * ?",//每天上午10:00至10:05期间的每1分钟触发
                "0 10,14,18 15 ? 3 WED",//每年三月的星期三的下午2:10和2:18触发
                "0 10 15 ? * MON-FRI",//每个周一、周二、周三、周四、周五的下午3:10触发
                "0 15 10 15 * ?",//每月15日上午10:15触发
                "0 15 10 L * ?", //每月最后一日的上午10:15触发
                "0 15 10 ? * 6L", //每月的最后一个星期五上午10:15触发
                "0 15 10 ? * 6L 2024-2026", //从2024年至2026年每月的最后一个星期五上午10:15触发
                "0 15 10 ? * 6#3", //每月的第三个星期五上午10:15触发
        };

        //每种corn表达式开启、关闭都测试一遍，检查是否能正常开关定时任务
        for (String corn : cornStrArr) {
            request = new ApiScenarioScheduleConfigRequest();
            request.setScenarioId(scenarioId);
            request.setEnable(true);
            request.setCron(corn);
            result = this.requestPostAndReturn(testUrl, request);
            resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
            scheduleId = resultHolder.getData().toString();
            apiScenarioBatchOperationTestService.checkSchedule(scheduleId, scenarioId, request.isEnable());

            request = new ApiScenarioScheduleConfigRequest();
            request.setScenarioId(scenarioId);
            request.setEnable(false);
            request.setCron(corn);
            result = this.requestPostAndReturn(testUrl, request);
            resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
            scheduleId = resultHolder.getData().toString();
            apiScenarioBatchOperationTestService.checkSchedule(scheduleId, scenarioId, request.isEnable());
        }


        //校验权限
        this.requestPostPermissionTest(PermissionConstants.PROJECT_API_SCENARIO_EXECUTE, testUrl, request);

        //反例：scenarioId不存在
        request = new ApiScenarioScheduleConfigRequest();
        request.setCron("0 0 0 * * ?");
        this.requestPost(testUrl, request).andExpect(status().isBadRequest());
        request.setScenarioId(IDGenerator.nextStr());
        this.requestPost(testUrl, request).andExpect(status().is5xxServerError());

        //反例：不配置cron表达式
        request = new ApiScenarioScheduleConfigRequest();
        request.setScenarioId(scenarioId);
        this.requestPost(testUrl, request).andExpect(status().isBadRequest());

        //反例：配置错误的cron表达式，测试是否会关闭定时任务
        request = new ApiScenarioScheduleConfigRequest();
        request.setScenarioId(scenarioId);
        request.setEnable(true);
        request.setCron(IDGenerator.nextStr());
        this.requestPost(testUrl, request).andExpect(status().is5xxServerError());

    }

    //30开始是关于删除和恢复的
    @Test
    @Order(31)
    void batchRemoveToGc() throws Exception {
        String testUrl = "/batch-operation/delete-gc";
        if (CollectionUtils.isEmpty(BATCH_OPERATION_SCENARIO_ID)) {
            this.scheduleTest();
        }

        //使用最后一个场景ID用于做定时任务的测试
        String scenarioId = BATCH_OPERATION_SCENARIO_ID.getLast();

        //本次测试涉及到的场景ID
        List<String> operationScenarioIds = new ArrayList<>(BATCH_OPERATION_SCENARIO_ID.subList(200, 500));
        //给最后一个场景创建定时任务，测试batchToGc时，是否会删除定时任务
        ApiScenarioScheduleConfigRequest scheduleRequest = new ApiScenarioScheduleConfigRequest();
        scheduleRequest.setScenarioId(scenarioId);
        scheduleRequest.setEnable(true);
        scheduleRequest.setCron("0 0 0 * * ?");
        MvcResult scheduleResult = this.requestPostAndReturn("/schedule-config", scheduleRequest);
        ResultHolder scheduleResultHolder = JSON.parseObject(scheduleResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        String scheduleId = scheduleResultHolder.getData().toString();
        apiScenarioBatchOperationTestService.checkSchedule(scheduleId, scenarioId, scheduleRequest.isEnable());


        /*
        正例测试
            超过300条的批量删除到垃圾站
            测试数据：使用建国批量测试场景模块_200模块为查询条件，和建国批量测试场景模块_250里的所有id，复制到 【建国批量测试场景模块_1】中
         */
        ApiScenarioBatchRequest request = new ApiScenarioBatchRequest();
        request.setSelectIds(BATCH_OPERATION_SCENARIO_ID.subList(250, 500));
        request.setModuleIds(List.of(BATCH_OPERATION_SCENARIO_MODULE_MAP.get("建国批量测试场景模块_201")));
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSelectAll(true);

        //先测试一下没有开启模块时接口能否使用
        apiScenarioBatchOperationTestService.removeApiModule(DEFAULT_PROJECT_ID);
        this.requestPost(testUrl, request).andExpect(status().is5xxServerError());
        //恢复
        apiScenarioBatchOperationTestService.resetProjectModule(DEFAULT_PROJECT_ID);

        MvcResult result = this.requestPostAndReturn(testUrl, request);
        ResultHolder resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        ApiScenarioBatchOperationResponse resultResponse = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), ApiScenarioBatchOperationResponse.class);
        //检查返回值
        Assertions.assertEquals(resultResponse.getSuccess(), 300);
        //检查定时任务是否删除
        apiScenarioBatchOperationTestService.checkScheduleIsRemove(scenarioId);
        //数据库级别的检查
        apiScenarioBatchOperationTestService.checkBatchGCOperation
                (BATCH_OPERATION_SCENARIO_ID.subList(200, 500), true);
        //2.重复请求
        result = this.requestPostAndReturn(testUrl, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        resultResponse = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), ApiScenarioBatchOperationResponse.class);
        //检查返回值
        Assertions.assertEquals(resultResponse.getSuccess(), 0);

        //数据库级别的检查
        apiScenarioBatchOperationTestService.checkBatchGCOperation
                (BATCH_OPERATION_SCENARIO_ID.subList(200, 500), true);
        //增加日志检查
        operationScenarioIds.forEach(item -> {
            LOG_CHECK_LIST.add(
                    new CheckLogModel(item, OperationLogType.DELETE, "/api/scenario/batch-operation/delete-gc")
            );
        });

        //        3.没有数据
        request = new ApiScenarioBatchCopyMoveRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        result = this.requestPostAndReturn(testUrl, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        resultResponse = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), ApiScenarioBatchOperationResponse.class);
        //检查返回值
        Assertions.assertEquals(resultResponse.getSuccess(), 0);
        //校验权限
        this.requestPostPermissionTest(PermissionConstants.PROJECT_API_SCENARIO_DELETE, testUrl, request);
        //反例测试 ->参数校验：项目ID为空
        request = new ApiScenarioBatchCopyMoveRequest();
        this.requestPost(testUrl, request).andExpect(status().isBadRequest());
    }

    @Test
    @Order(32)
        //todo
    void batchRecoverToGc() throws Exception {
        String testUrl = "/batch-operation/recover-gc";
        if (CollectionUtils.isEmpty(BATCH_OPERATION_SCENARIO_ID)) {
            this.batchRemoveToGc();
        }

        //本次测试涉及到的场景ID
        List<String> operationScenarioIds = new ArrayList<>(BATCH_OPERATION_SCENARIO_ID.subList(200, 500));

        /*
        正例测试
            超过300条的批量删除到垃圾站
            测试数据：使用建国批量测试场景模块_200模块为查询条件，和建国批量测试场景模块_250里的所有id，复制到 【建国批量测试场景模块_1】中
         */
        ApiScenarioBatchRequest request = new ApiScenarioBatchRequest();
        request.setSelectIds(BATCH_OPERATION_SCENARIO_ID.subList(250, 500));
        request.setModuleIds(List.of(BATCH_OPERATION_SCENARIO_MODULE_MAP.get("建国批量测试场景模块_201")));
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSelectAll(true);

        //先测试一下没有开启模块时接口能否使用
        apiScenarioBatchOperationTestService.removeApiModule(DEFAULT_PROJECT_ID);
        this.requestPost(testUrl, request).andExpect(status().is5xxServerError());
        //恢复
        apiScenarioBatchOperationTestService.resetProjectModule(DEFAULT_PROJECT_ID);

        MvcResult result = this.requestPostAndReturn(testUrl, request);
        ResultHolder resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        ApiScenarioBatchOperationResponse resultResponse = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), ApiScenarioBatchOperationResponse.class);
        //检查返回值
        Assertions.assertEquals(resultResponse.getSuccess(), 300);
        //数据库级别的检查
        apiScenarioBatchOperationTestService.checkBatchGCOperation
                (BATCH_OPERATION_SCENARIO_ID.subList(200, 500), false);


        //增加日志检查
        operationScenarioIds.forEach(item -> {
            LOG_CHECK_LIST.add(
                    new CheckLogModel(item, OperationLogType.RECOVER, "/api/scenario/batch-operation/recover-gc")
            );
        });

        //        2.重复请求
        result = this.requestPostAndReturn(testUrl, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        resultResponse = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), ApiScenarioBatchOperationResponse.class);
        //检查返回值
        Assertions.assertEquals(resultResponse.getSuccess(), 0);
        //数据库级别的检查
        apiScenarioBatchOperationTestService.checkBatchGCOperation
                (BATCH_OPERATION_SCENARIO_ID.subList(200, 500), false);

        //        3.没有数据
        request = new ApiScenarioBatchCopyMoveRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        result = this.requestPostAndReturn(testUrl, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        resultResponse = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), ApiScenarioBatchOperationResponse.class);
        //检查返回值
        Assertions.assertEquals(resultResponse.getSuccess(), 0);

        //校验权限
        this.requestPostPermissionTest(PermissionConstants.PROJECT_API_SCENARIO_RECOVER, testUrl, request);

        //反例测试 ->参数校验：项目ID为空
        request = new ApiScenarioBatchCopyMoveRequest();
        this.requestPost(testUrl, request).andExpect(status().isBadRequest());
    }

    @Test
    @Order(33)
        //todo
    void batchDelete() throws Exception {
        String testUrl = "/batch-operation/delete";
        if (CollectionUtils.isEmpty(BATCH_OPERATION_SCENARIO_ID)) {
            this.batchRecoverToGc();
        }
        //本次测试涉及到的场景ID
        List<String> deleteScenarioIds = new ArrayList<>(BATCH_OPERATION_SCENARIO_ID.subList(200, 500));
        /*
        正例测试
            超过300条的批量删除
            测试数据：使用建国批量测试场景模块_200模块为查询条件，和建国批量测试场景模块_250里的所有id，先删除到回收站，再删除
         */
        //先将用例挪到回收站
        ApiScenarioBatchRequest request = new ApiScenarioBatchRequest();
        request.setSelectIds(BATCH_OPERATION_SCENARIO_ID.subList(250, 500));
        request.setModuleIds(List.of(BATCH_OPERATION_SCENARIO_MODULE_MAP.get("建国批量测试场景模块_201")));
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSelectAll(true);
        this.requestPostWithOk("/batch-operation/delete-gc", request);


        //先测试一下没有开启模块时接口能否使用
        apiScenarioBatchOperationTestService.removeApiModule(DEFAULT_PROJECT_ID);
        this.requestPost(testUrl, request).andExpect(status().is5xxServerError());
        //恢复
        apiScenarioBatchOperationTestService.resetProjectModule(DEFAULT_PROJECT_ID);
        MvcResult result = this.requestPostAndReturn(testUrl, request);
        ResultHolder resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        ApiScenarioBatchOperationResponse resultResponse = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), ApiScenarioBatchOperationResponse.class);
        //检查返回值
        Assertions.assertEquals(resultResponse.getSuccess(), 300);
        //数据库级别的检查：
        apiScenarioBatchOperationTestService.checkBatchDelete
                (BATCH_OPERATION_SCENARIO_ID.subList(200, 500));

        //增加日志检查
        deleteScenarioIds.forEach(item -> {
            LOG_CHECK_LIST.add(
                    new CheckLogModel(item, OperationLogType.DELETE, "/api/scenario/batch-operation/delete")
            );
        });


        //        2.重复请求
        result = this.requestPostAndReturn(testUrl, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        resultResponse = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), ApiScenarioBatchOperationResponse.class);
        //检查返回值
        Assertions.assertEquals(resultResponse.getSuccess(), 0);
        //数据库级别的检查
        apiScenarioBatchOperationTestService.checkBatchDelete
                (BATCH_OPERATION_SCENARIO_ID.subList(200, 500));


        //        3.没有数据
        request = new ApiScenarioBatchCopyMoveRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        result = this.requestPostAndReturn(testUrl, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        resultResponse = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), ApiScenarioBatchOperationResponse.class);
        //检查返回值
        Assertions.assertEquals(resultResponse.getSuccess(), 0);

        //校验权限
        this.requestPostPermissionTest(PermissionConstants.PROJECT_API_SCENARIO_DELETE, testUrl, request);

        //反例测试 ->参数校验：项目ID为空
        request = new ApiScenarioBatchCopyMoveRequest();
        this.requestPost(testUrl, request).andExpect(status().isBadRequest());

    }

    @Test
    @Order(34)
    void recover() throws Exception {
        if (CollectionUtils.isEmpty(BATCH_OPERATION_SCENARIO_ID)) {
            this.batchCreateScenarios();
        }
        this.requestGetWithOk("/recover/" + BATCH_OPERATION_SCENARIO_ID.getFirst());
    }


    @Test
    @Order(999)
    public void checkLog() throws Exception {
        Thread.sleep(5000);
        for (CheckLogModel checkLogModel : LOG_CHECK_LIST) {
            if (org.apache.commons.lang3.StringUtils.isEmpty(checkLogModel.getUrl())) {
                this.checkLog(checkLogModel.getResourceId(), checkLogModel.getOperationType());
            } else {
                this.checkLog(checkLogModel.getResourceId(), checkLogModel.getOperationType(), checkLogModel.getUrl());
            }
        }
    }

    private String createModule(int i, String projectId, String parentId) {
        ApiScenarioModule apiScenarioModule = new ApiScenarioModule();
        apiScenarioModule.setId(IDGenerator.nextStr());
        apiScenarioModule.setProjectId(projectId);
        apiScenarioModule.setName("建国批量测试场景模块_" + i);
        apiScenarioModule.setCreateTime(System.currentTimeMillis());
        apiScenarioModule.setUpdateTime(System.currentTimeMillis());
        apiScenarioModule.setCreateUser("admin");
        apiScenarioModule.setUpdateUser("admin");
        apiScenarioModule.setParentId(parentId);
        apiScenarioModule.setPos(i * 64L);
        apiScenarioModuleMapper.insertSelective(apiScenarioModule);
        return apiScenarioModule.getId();
    }

    /*
        创建模块树。结构：
            建国批量测试场景模块_default 无数据
            |
            |
            建国批量测试场景模块_1    ->50条数据 有标签和环境组
            |
            |
            建国批量测试场景模块_51    ->50条数据    有环境
                |
                |
                建国批量测试场景模块_101    ->50条数据   有blob
                    |
                    |
                    建国批量测试场景模块_151    ->50条数据   有step
                        |
                        |
                        建国批量测试场景模块_201    ->50条数据   有step和blob
                        |
                        |
                        建国批量测试场景模块_251    ->250条数据  有文件
         */
    private void batchCreateScenarios() {
        int size = 500;
        String moduleId = null;
        EnvironmentExample environmentExample = new EnvironmentExample();
        environmentExample.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andMockEqualTo(true);
        List<Environment> environments = environmentMapper.selectByExample(environmentExample);
        String parentModuleId = ModuleConstants.ROOT_NODE_PARENT_ID;


        ApiScenarioModule apiScenarioModule = new ApiScenarioModule();
        apiScenarioModule.setId(IDGenerator.nextStr());
        apiScenarioModule.setProjectId(DEFAULT_PROJECT_ID);
        apiScenarioModule.setName("建国批量测试场景模块_default");
        apiScenarioModule.setCreateTime(System.currentTimeMillis());
        apiScenarioModule.setUpdateTime(System.currentTimeMillis());
        apiScenarioModule.setCreateUser("admin");
        apiScenarioModule.setUpdateUser("admin");
        apiScenarioModule.setParentId(parentModuleId);
        apiScenarioModule.setPos(16L);
        apiScenarioModuleMapper.insertSelective(apiScenarioModule);
        BATCH_OPERATION_SCENARIO_MODULE_MAP.put("建国批量测试场景模块_default", apiScenarioModule.getId());

        for (int i = 1; i <= size; i++) {
            if (moduleId == null) {
                moduleId = createModule(i, DEFAULT_PROJECT_ID, parentModuleId);
                BATCH_OPERATION_SCENARIO_MODULE_MAP.put("建国批量测试场景模块_" + i, moduleId);
            } else if (i == 51) {
                moduleId = createModule(i, DEFAULT_PROJECT_ID, parentModuleId);
                BATCH_OPERATION_SCENARIO_MODULE_MAP.put("建国批量测试场景模块_" + i, moduleId);
            } else if (i == 101) {
                parentModuleId = moduleId;
                moduleId = createModule(i, DEFAULT_PROJECT_ID, parentModuleId);
                BATCH_OPERATION_SCENARIO_MODULE_MAP.put("建国批量测试场景模块_" + i, moduleId);
            } else if (i == 151) {
                parentModuleId = moduleId;
                moduleId = createModule(i, DEFAULT_PROJECT_ID, parentModuleId);
                BATCH_OPERATION_SCENARIO_MODULE_MAP.put("建国批量测试场景模块_" + i, moduleId);
            } else if (i == 201) {
                parentModuleId = moduleId;
                moduleId = createModule(i, DEFAULT_PROJECT_ID, parentModuleId);
                BATCH_OPERATION_SCENARIO_MODULE_MAP.put("建国批量测试场景模块_" + i, moduleId);
            } else if (i == 251) {
                //本次创建的模块和 _200 属于同一级
                moduleId = createModule(i, DEFAULT_PROJECT_ID, parentModuleId);
                BATCH_OPERATION_SCENARIO_MODULE_MAP.put("建国批量测试场景模块_" + i, moduleId);
            }
            ApiScenario apiScenario = new ApiScenario();
            apiScenario.setId(IDGenerator.nextStr());
            apiScenario.setProjectId(DEFAULT_PROJECT_ID);
            apiScenario.setNum(NumGenerator.nextNum(DEFAULT_PROJECT_ID, ApplicationNumScope.API_SCENARIO));
            apiScenario.setName(StringUtils.join("建国批量测试接口场景-", apiScenario.getId()));
            apiScenario.setModuleId(moduleId);
            apiScenario.setStatus("未规划");
            apiScenario.setPos(i * 64L);
            apiScenario.setPriority("P0");
            apiScenario.setLatest(true);
            apiScenario.setVersionId("1.0");
            apiScenario.setRefId(apiScenario.getId());
            apiScenario.setCreateTime(System.currentTimeMillis());
            apiScenario.setUpdateTime(System.currentTimeMillis());
            apiScenario.setCreateUser("admin");
            apiScenario.setUpdateUser("admin");
            if (i <= 50) {
                apiScenario.setTags(new ArrayList<>(List.of("tag1", "tag2")));
                apiScenario.setGrouped(true);
                apiScenario.setEnvironmentId("scenario-environment-group-id");
            } else if (i <= 100) {
                apiScenario.setGrouped(false);
                apiScenario.setEnvironmentId(environments.get(0).getId());
            } else if (i <= 150) {
                //带blob
                ApiScenarioBlob apiScenarioBlob = new ApiScenarioBlob();
                apiScenarioBlob.setId(apiScenario.getId());
                apiScenarioBlobMapper.insertSelective(apiScenarioBlob);
            } else if (i <= 200) {
                ApiScenarioStep step1 = new ApiScenarioStep();
                step1.setId(IDGenerator.nextStr());
                step1.setScenarioId(apiScenario.getId());
                step1.setName(apiScenario.getName() + "_" + IDGenerator.nextStr());
                step1.setSort(0L);
                step1.setEnable(true);
                apiScenarioStepMapper.insertSelective(step1);

                ApiScenarioStep step2 = new ApiScenarioStep();
                step2.setId(IDGenerator.nextStr());
                step2.setScenarioId(apiScenario.getId());
                step2.setName(apiScenario.getName() + "_" + IDGenerator.nextStr());
                step2.setSort(1L);
                step1.setEnable(false);
                apiScenarioStepMapper.insertSelective(step2);
            } else if (i <= 250) {
                //带步骤和详情的
                ApiScenarioStep step1 = new ApiScenarioStep();
                step1.setId(IDGenerator.nextStr());
                step1.setScenarioId(apiScenario.getId());
                step1.setName(apiScenario.getName() + "_" + IDGenerator.nextStr());
                step1.setSort(0L);
                step1.setEnable(true);
                apiScenarioStepMapper.insertSelective(step1);
                ApiScenarioStepBlob step1Blob = new ApiScenarioStepBlob();
                step1Blob.setId(step1.getId());
                step1Blob.setScenarioId(apiScenario.getId());
                apiScenarioStepBlobMapper.insertSelective(step1Blob);

                ApiScenarioStep step2 = new ApiScenarioStep();
                step2.setId(IDGenerator.nextStr());
                step2.setScenarioId(apiScenario.getId());
                step2.setName(apiScenario.getName() + "_" + IDGenerator.nextStr());
                step2.setSort(1L);
                step1.setEnable(false);
                apiScenarioStepMapper.insertSelective(step2);
                ApiScenarioStepBlob step2Blob = new ApiScenarioStepBlob();
                step2Blob.setId(step2.getId());
                step2Blob.setScenarioId(apiScenario.getId());
                apiScenarioStepBlobMapper.insertSelective(step2Blob);
            } else if (i <= 300) {
                //带文件的
                ApiFileResource apiFileResource = new ApiFileResource();
                apiFileResource.setResourceId(apiScenario.getId());
                apiFileResource.setFileId(IDGenerator.nextStr());
                apiFileResource.setFileName("test");
                apiFileResource.setResourceType("API_SCENARIO");
                apiFileResource.setCreateTime(System.currentTimeMillis());
                apiFileResource.setProjectId(apiScenario.getProjectId());
                apiFileResourceMapper.insertSelective(apiFileResource);

                ApiScenarioCsv apiScenarioCsv = new ApiScenarioCsv();
                apiScenarioCsv.setId(IDGenerator.nextStr());
                apiScenarioCsv.setScenarioId(apiScenario.getId());
                apiScenarioCsv.setFileId(fileMetadataId);
                apiScenarioCsv.setName("csv变量");
                apiScenarioCsv.setScope(CsvVariable.CsvVariableScope.SCENARIO.name());
                apiScenarioCsv.setProjectId(DEFAULT_PROJECT_ID);
                apiScenarioCsvMapper.insertSelective(apiScenarioCsv);
            }
            apiScenarioMapper.insertSelective(apiScenario);
            BATCH_OPERATION_SCENARIO_ID.add(apiScenario.getId());
        }
    }

    @Test
    @Order(9)
    public void testPos() throws Exception {
        PosRequest posRequest = new PosRequest();
        posRequest.setProjectId(DEFAULT_PROJECT_ID);
        posRequest.setTargetId(addApiScenario.getId());
        posRequest.setMoveId(anOtherAddApiScenario.getId());
        posRequest.setMoveMode("AFTER");
        this.requestPostWithOkAndReturn("/edit/pos", posRequest);

        posRequest.setMoveMode("BEFORE");
        this.requestPostWithOkAndReturn("/edit/pos", posRequest);

    }

    @Test
    @Order(35)
    public void testGetSystemRequest() throws Exception {
        //需要造假数据， 接口  用例  场景
        //接口
        List<ApiDefinition> apiDefinitions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ApiDefinition apiDefinition = new ApiDefinition();
            if (i == 0) {
                apiDefinition.setId("system-api-id");
            } else {
                apiDefinition.setId(IDGenerator.nextStr());
            }
            apiDefinition.setProjectId(DEFAULT_PROJECT_ID);
            apiDefinition.setName("接口" + i);
            apiDefinition.setNum(NumGenerator.nextNum(DEFAULT_PROJECT_ID, ApplicationNumScope.API_DEFINITION));
            apiDefinition.setCreateTime(System.currentTimeMillis());
            apiDefinition.setUpdateTime(System.currentTimeMillis());
            apiDefinition.setCreateUser("admin");
            apiDefinition.setUpdateUser("admin");
            apiDefinition.setPath("/test/" + i);
            apiDefinition.setModuleId("test-default");
            apiDefinition.setStatus("未规划");
            apiDefinition.setDeleted(false);
            apiDefinition.setLatest(true);
            apiDefinition.setProtocol("HTTP");
            apiDefinition.setMethod("GET");
            apiDefinition.setVersionId("1.0");
            apiDefinition.setRefId(apiDefinition.getId());
            apiDefinition.setPos(i * 64L);
            apiDefinitions.add(apiDefinition);
        }
        apiDefinitionMapper.batchInsert(apiDefinitions);

        //用例
        List<ApiTestCase> apiTestCases = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ApiTestCase apiTestCase = new ApiTestCase();
            apiTestCase.setId(IDGenerator.nextStr());
            apiTestCase.setProjectId(DEFAULT_PROJECT_ID);
            apiTestCase.setName("用例" + i);
            apiTestCase.setNum(NumGenerator.nextNum(DEFAULT_PROJECT_ID, ApplicationNumScope.API_DEFINITION));
            apiTestCase.setCreateTime(System.currentTimeMillis());
            apiTestCase.setUpdateTime(System.currentTimeMillis());
            apiTestCase.setCreateUser("admin");
            apiTestCase.setUpdateUser("admin");
            apiTestCase.setStatus("未规划");
            apiTestCase.setDeleted(false);
            apiTestCase.setPriority("P0");
            apiTestCase.setPos(i * 64L);
            apiTestCase.setApiDefinitionId("system-api-id");
            apiTestCase.setVersionId("1.0");
            apiTestCases.add(apiTestCase);
        }
        apiTestCaseMapper.batchInsert(apiTestCases);

        List<ApiScenario> apiScenarios = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ApiScenario apiScenario = new ApiScenario();
            apiScenario.setId("system-scenario-id" + i);
            apiScenario.setProjectId(DEFAULT_PROJECT_ID);
            apiScenario.setName("场景" + i);
            apiScenario.setNum(NumGenerator.nextNum(DEFAULT_PROJECT_ID, ApplicationNumScope.API_SCENARIO));
            apiScenario.setCreateTime(System.currentTimeMillis());
            apiScenario.setUpdateTime(System.currentTimeMillis());
            apiScenario.setCreateUser("admin");
            apiScenario.setUpdateUser("admin");
            apiScenario.setStatus("未规划");
            apiScenario.setDeleted(false);
            apiScenario.setPriority("P0");
            apiScenario.setStepTotal(0);
            apiScenario.setPos(i * 64L);
            apiScenario.setModuleId("test-default");
            apiScenario.setVersionId("1.0");
            apiScenario.setRequestPassRate(String.valueOf(0));
            apiScenario.setRefId(apiScenario.getId());
            apiScenario.setLatest(true);
            apiScenario.setLastReportStatus("未执行");
            apiScenarios.add(apiScenario);
        }
        apiScenarioMapper.batchInsert(apiScenarios);


        ScenarioSystemRequest scenarioSystemRequest = new ScenarioSystemRequest();
        scenarioSystemRequest.setProjectId(DEFAULT_PROJECT_ID);
        scenarioSystemRequest.setProtocol("HTTP");
        scenarioSystemRequest.setModuleIds(List.of("test-default"));
        ApiScenarioSystemRequest apiScenarioSystemRequest = new ApiScenarioSystemRequest();
        apiScenarioSystemRequest.setApiRequest(scenarioSystemRequest);
        apiScenarioSystemRequest.setCaseRequest(scenarioSystemRequest);
        apiScenarioSystemRequest.setScenarioRequest(scenarioSystemRequest);
        apiScenarioSystemRequest.setRefType(ApiScenarioStepRefType.COPY.name());
        this.requestPostWithOkAndReturn("/get/system-request", apiScenarioSystemRequest);

        StepRequest stepRequest = new StepRequest();
        stepRequest.setStepId("system-scenario-id1");
        stepRequest.setStepType(ApiScenarioStepType.API_SCENARIO.name());
        stepRequest.setResourceId("system-scenario-id1");

        mockMvc.perform(getPostRequestBuilder(STEP_GET, stepRequest))
                .andExpect(status().isOk());
    }

}