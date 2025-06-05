package io.metersphere.api.controller;

import io.metersphere.api.constants.ApiConstants;
import io.metersphere.api.constants.ApiDefinitionStatus;
import io.metersphere.api.controller.param.ApiTestCaseAddRequestDefinition;
import io.metersphere.api.controller.result.ApiResultCode;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.ApiFile;
import io.metersphere.api.dto.ApiRunModeRequest;
import io.metersphere.api.dto.ReferenceRequest;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.request.ApiTransferRequest;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.body.Body;
import io.metersphere.api.mapper.*;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.api.service.BaseFileManagementTestService;
import io.metersphere.api.service.definition.ApiReportService;
import io.metersphere.api.service.definition.ApiTestCaseNoticeService;
import io.metersphere.api.service.definition.ApiTestCaseService;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plan.domain.TestPlanApiCase;
import io.metersphere.plan.domain.TestPlanExample;
import io.metersphere.plan.mapper.TestPlanApiCaseMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.domain.ProjectVersion;
import io.metersphere.project.dto.environment.EnvironmentConfig;
import io.metersphere.project.dto.environment.host.Host;
import io.metersphere.project.dto.environment.host.HostConfig;
import io.metersphere.project.dto.environment.http.HttpConfig;
import io.metersphere.project.dto.filemanagement.FileInfo;
import io.metersphere.project.mapper.ProjectVersionMapper;
import io.metersphere.project.service.FileAssociationService;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentBlob;
import io.metersphere.sdk.domain.EnvironmentExample;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.file.MinioRepository;
import io.metersphere.sdk.mapper.EnvironmentBlobMapper;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.controller.handler.result.MsHttpResultCode;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.OperationHistoryDTO;
import io.metersphere.system.dto.request.OperationHistoryRequest;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.service.OperationHistoryService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.*;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiTestCaseControllerTests extends BaseTest {
    private static final String BASE_PATH = "/api/case/";
    private static final String ADD = "add";
    private static final String GET = "get-detail/";
    private static final String DELETE_TO_GC = "delete-to-gc/{0}";
    private static final String RECOVER = "recover/";
    private static final String FOLLOW = "follow/";
    private static final String UNFOLLOW = "unfollow/";
    private static final String DELETE = "delete/";
    private static final String UPDATE = "update";
    private static final String PAGE = "page";
    private static final String TRASH_PAGE = "trash/page";
    private static final String UPDATE_STATUS = "update-status";
    private static final String UPDATE_PRIORITY = "update-priority";
    private static final String BATCH_EDIT = "batch/edit";
    private static final String BATCH_DELETE = "batch/delete";
    private static final String BATCH_DELETE_TO_GC = "batch/delete-to-gc";
    private static final String BATCH_RECOVER = "batch/recover";
    private static final String POS_URL = "edit/pos";
    private static final String UPLOAD_TEMP_FILE = "upload/temp/file";
    private static final String EXECUTE = "execute/page";
    private static final String HISTORY = "operation-history/page";
    private static final String DEBUG = "debug";
    private static final String RUN_REAL_TIME = "run/{0}?reportId={1}";
    private static final String RUN_GET = "run/{0}";
    private static final String RUN_POST = "run";
    private static final String BATCH_RUN = "batch/run";
    private static final String API_CHANGE_CLEAR = "api-change/clear/{0}";
    private static final String API_CHANGE_IGNORE = "api-change/ignore/{0}?ignore={1}";
    private static final String BATCH_API_CHANGE_SYNC = "batch/api-change/sync";
    private static final String API_CHANGE_SYNC = "api-change/sync";
    private static final String API_COMPARE = "api/compare/{0}";

    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();
    private static ApiTestCase apiTestCase;
    private static String apiDefinitionId = UUID.randomUUID().toString();
    private static String anotherApiDefinitionId = UUID.randomUUID().toString();

    private static ApiTestCase anotherApiTestCase;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiDefinitionBlobMapper apiDefinitionBlobMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ApiTestCaseBlobMapper apiTestCaseBlobMapper;
    @Resource
    private ApiTestCaseFollowerMapper apiTestCaseFollowerMapper;
    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private EnvironmentBlobMapper environmentBlobMapper;
    private static String fileMetadataId;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    private static String uploadFileId;
    @Resource
    private ApiReportService apiReportService;
    @Resource
    private ProjectVersionMapper projectVersionMapper;
    @Resource
    private OperationHistoryService operationHistoryService;
    @Resource
    private BaseFileManagementTestService baseFileManagementTestService;
    @Resource
    private ApiCommonService apiCommonService;
    @Resource
    private ApiFileResourceMapper apiFileResourceMapper;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ApiScenarioStepMapper apiScenarioStepMapper;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    private ApiTestCaseNoticeService apiTestCaseNoticeService;
    @Resource
    private UserMapper userMapper;

    @Override
    public String getBasePath() {
        return BASE_PATH;
    }

    public static <T> T parseObjectFromMvcResult(MvcResult mvcResult, Class<T> parseClass) {
        try {
            String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
            ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
            //返回请求正常
            Assertions.assertNotNull(resultHolder);
            return JSON.parseObject(JSON.toJSONString(resultHolder.getData()), parseClass);
        } catch (Exception ignore) {
        }
        return null;
    }

    public void initApiData() {
        ApiDefinition apiDefinition = new ApiDefinition();
        apiDefinition.setId(apiDefinitionId);
        apiDefinition.setProjectId(DEFAULT_PROJECT_ID);
        apiDefinition.setName(StringUtils.join("接口定义", apiDefinition.getId()));
        apiDefinition.setModuleId("case-moduleId");
        apiDefinition.setProtocol(ApiConstants.HTTP_PROTOCOL);
        apiDefinition.setMethod("GET");
        apiDefinition.setStatus(ApiDefinitionStatus.DEBUGGING.name());
        apiDefinition.setNum(NumGenerator.nextNum(DEFAULT_PROJECT_ID, ApplicationNumScope.API_DEFINITION));
        apiDefinition.setPos(0L);
        apiDefinition.setPath(StringUtils.join("api/definition/", apiDefinition.getId()));
        apiDefinition.setLatest(true);
        apiDefinition.setVersionId("1.0");
        apiDefinition.setRefId(apiDefinition.getId());
        apiDefinition.setCreateTime(System.currentTimeMillis());
        apiDefinition.setUpdateTime(System.currentTimeMillis());
        apiDefinition.setCreateUser("admin");
        apiDefinition.setUpdateUser("admin");
        apiDefinitionMapper.insertSelective(apiDefinition);
        ApiDefinitionBlob apiDefinitionBlob = new ApiDefinitionBlob();
        apiDefinitionBlob.setId(apiDefinition.getId());
        MsHTTPElement msHttpElement = MsHTTPElementTest.getMsHttpElement();
        apiDefinitionBlob.setRequest(JSON.toJSONBytes(msHttpElement));
        apiDefinitionBlobMapper.insertSelective(apiDefinitionBlob);
        apiDefinition.setId(anotherApiDefinitionId);
        apiDefinition.setModuleId("moduleId1");
        apiDefinitionMapper.insertSelective(apiDefinition);
    }

    public void initCaseData() {
        //2000条数据
        MsHTTPElement msHttpElement = MsHTTPElementTest.getMsHttpElement();
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiTestCaseMapper caseMapper = sqlSession.getMapper(ApiTestCaseMapper.class);
        ApiTestCaseBlobMapper caseBlobMapper = sqlSession.getMapper(ApiTestCaseBlobMapper.class);
        for (int i = 0; i < 2100; i++) {
            ApiTestCase apiTestCase = new ApiTestCase();
            apiTestCase.setId("apiTestCaseId" + i);
            apiTestCase.setApiDefinitionId(apiDefinitionId);
            apiTestCase.setProjectId(DEFAULT_PROJECT_ID);
            apiTestCase.setName(StringUtils.join("接口用例", apiTestCase.getId()));
            apiTestCase.setPriority("P0");
            apiTestCase.setStatus(ApiDefinitionStatus.PROCESSING.name());
            apiTestCase.setNum(NumGenerator.nextNum(DEFAULT_PROJECT_ID + "_" + "100001", ApplicationNumScope.API_TEST_CASE));
            apiTestCase.setPos(0L);
            apiTestCase.setCreateTime(System.currentTimeMillis());
            apiTestCase.setUpdateTime(System.currentTimeMillis());
            apiTestCase.setCreateUser("admin");
            apiTestCase.setUpdateUser("admin");
            apiTestCase.setVersionId("1.0");
            apiTestCase.setDeleted(false);
            apiTestCase.setLastReportStatus("SUCCESS");
            apiTestCase.setApiChange(false);
            apiTestCase.setIgnoreApiChange(false);
            apiTestCase.setIgnoreApiDiff(false);
            apiTestCase.setAiCreate(false);
            caseMapper.insert(apiTestCase);
            ApiTestCaseBlob apiTestCaseBlob = new ApiTestCaseBlob();
            apiTestCaseBlob.setId(apiTestCase.getId());
            apiTestCaseBlob.setRequest(JSON.toJSONBytes(msHttpElement));
            caseBlobMapper.insert(apiTestCaseBlob);
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    private static MockMultipartFile getMockMultipartFile() {
        return new MockMultipartFile(
                "file",
                IDGenerator.nextStr() + "_file_upload.JPG",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "Hello, World!".getBytes()
        );
    }

    /**
     * 校验上传的文件
     *
     * @param id
     * @param fileIds 全部的文件ID
     */
    public static void assertUploadFile(String id, List<String> fileIds) throws Exception {
        if (fileIds != null) {
            ApiFileResourceService apiFileResourceService = CommonBeanFactory.getBean(ApiFileResourceService.class);
            // 验证文件的关联关系，以及是否存入对象存储
            List<ApiFileResource> apiFileResources = apiFileResourceService.getByResourceId(id);
            Assertions.assertEquals(apiFileResources.size(), fileIds.size());

            String apiDebugDir = DefaultRepositoryDir.getApiCaseDir(DEFAULT_PROJECT_ID, id);
            FileRequest fileRequest = new FileRequest();
            if (fileIds.size() > 0) {
                for (ApiFileResource apiFileResource : apiFileResources) {
                    Assertions.assertEquals(apiFileResource.getProjectId(), DEFAULT_PROJECT_ID);
                    fileRequest.setFolder(apiDebugDir + "/" + apiFileResource.getFileId());
                    fileRequest.setFileName(apiFileResource.getFileName());
                    Assertions.assertNotNull(FileCenter.getDefaultRepository().getFile(fileRequest));
                }
                fileRequest.setFolder(apiDebugDir);
            } else {
                fileRequest.setFolder(apiDebugDir);
                Assertions.assertTrue(CollectionUtils.isEmpty(FileCenter.getDefaultRepository().getFolderFileNames(fileRequest)));
            }
        }
    }

    /**
     * 校验上传的文件
     *
     * @param id
     * @param fileIds 全部的文件ID
     */
    private static void assertLinkFile(String id, List<String> fileIds) {
        FileAssociationService fileAssociationService = CommonBeanFactory.getBean(FileAssociationService.class);
        List<String> linkFileIds = fileAssociationService.getFiles(id)
                .stream()
                .map(FileInfo::getFileId)
                .toList();
        Assertions.assertEquals(fileIds, linkFileIds);
    }

    @Test
    @Order(1)
    public void uploadTempFile() throws Exception {
        // @@请求成功
        MockMultipartFile file = getMockMultipartFile();
        String fileId = doUploadTempFile(file);

        // 校验文件存在
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFolder(DefaultRepositoryDir.getSystemTempDir() + "/" + fileId);
        fileRequest.setFileName(file.getOriginalFilename());
        Assertions.assertNotNull(FileCenter.getDefaultRepository().getFile(fileRequest));

        requestUploadPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_ADD, UPLOAD_TEMP_FILE, file);
        requestUploadPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE, UPLOAD_TEMP_FILE, file);

        // 准备数据，上传文件管理文件
        fileMetadataId = baseFileManagementTestService.upload(file);
    }

    private ApiTestCase assertUpdateApiDebug(Object request, MsHTTPElement msHttpElement, String id) {
        ApiTestCase apiCase = apiTestCaseMapper.selectByPrimaryKey(id);
        ApiTestCaseBlob apiTestCaseBlob = apiTestCaseBlobMapper.selectByPrimaryKey(id);
        ApiTestCase copyApiDebug = BeanUtils.copyBean(new ApiTestCase(), apiCase);
        BeanUtils.copyBean(copyApiDebug, request);
        Assertions.assertEquals(apiCase, copyApiDebug);
        Assertions.assertEquals(msHttpElement, ApiDataUtils.parseObject(new String(apiTestCaseBlob.getRequest()), AbstractMsTestElement.class));
        return apiCase;
    }

    private String doUploadTempFile(MockMultipartFile file) throws Exception {
        return JSON.parseObject(requestUploadFileWithOkAndReturn(UPLOAD_TEMP_FILE, file)
                        .getResponse()
                        .getContentAsString(), ResultHolder.class)
                .getData().toString();
    }

    @Test
    @Order(2)
    public void add() throws Exception {
        initApiData();
        EnvironmentExample environmentExample = new EnvironmentExample();
        environmentExample.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andMockEqualTo(true);
        List<Environment> environments = environmentMapper.selectByExample(environmentExample);
        // @@请求成功
        ApiTestCaseAddRequest request = new ApiTestCaseAddRequest();
        request.setApiDefinitionId(apiDefinitionId);
        request.setName("test");
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setPriority("P0");
        request.setStatus(ApiDefinitionStatus.PROCESSING.name());
        request.setTags(new LinkedHashSet<>(List.of("tag1", "tag2")));
        request.setEnvironmentId(environments.getFirst().getId());
        MsHTTPElement msHttpElement = MsHTTPElementTest.getMsHttpElement();
        msHttpElement.setBody(ApiDebugControllerTests.addBodyLinkFile(msHttpElement.getBody(), fileMetadataId));
        request.setRequest(getMsElementParam(msHttpElement));

        uploadFileId = doUploadTempFile(getMockMultipartFile());
        request.setUploadFileIds(List.of(uploadFileId));

        request.setLinkFileIds(List.of(fileMetadataId));

        MvcResult mvcResult = this.requestPostWithOkAndReturn(ADD, request);
        // 校验请求成功数据
        ApiTestCase resultData = getResultData(mvcResult, ApiTestCase.class);
        apiTestCase = assertUpdateApiDebug(request, msHttpElement, resultData.getId());
        assertUploadFile(resultData.getId(), List.of(uploadFileId));
        assertLinkFile(resultData.getId(), List.of(fileMetadataId));
        // 再插入一条数据，便于修改时重名校验
        request.setName("test1");
        request.setTags(new LinkedHashSet<>());
        request.setUploadFileIds(null);
        request.setLinkFileIds(null);
        mvcResult = this.requestPostWithOkAndReturn(ADD, request);
        resultData = getResultData(mvcResult, ApiTestCase.class);
        anotherApiTestCase = apiTestCaseMapper.selectByPrimaryKey(resultData.getId());
        assertUpdateApiDebug(request, msHttpElement, resultData.getId());
        assertUploadFile(resultData.getId(), List.of());
        assertLinkFile(resultData.getId(), List.of());

        // 测试关联的文件更新
        testHandleFileAssociationUpgrade();

        // @@重名校验异常
        this.requestPost(ADD, request).andExpect(ERROR_REQUEST_MATCHER);
        // 校验接口是否存在
        request.setApiDefinitionId("111");
        this.requestPost(ADD, request).andExpect(ERROR_REQUEST_MATCHER);

        // 校验项目是否存在
        request.setProjectId("111");
        request.setApiDefinitionId(apiDefinitionId);
        request.setName("test123");
        this.requestPost(ADD, request).andExpect(ERROR_REQUEST_MATCHER);
        // @@校验日志
        checkLog(apiTestCase.getId(), OperationLogType.ADD);
        // @@异常参数校验
        createdGroupParamValidateTest(ApiTestCaseAddRequestDefinition.class, ADD);
        // @@校验权限
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setName("permission");
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_ADD, ADD, request);
    }

    @Test
    @Order(3)
    public void handleApiParamChange() {
        ApiTestCase updateCase = new ApiTestCase();
        updateCase.setApiChange(false);
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andApiDefinitionIdEqualTo(apiTestCase.getApiDefinitionId());
        apiTestCaseMapper.updateByExampleSelective(updateCase, example);
        apiTestCaseService.handleApiParamChange(apiTestCase.getApiDefinitionId(), new MsHTTPElement(), new MsHTTPElement());
        example = new ApiTestCaseExample();
        example.createCriteria().andApiDefinitionIdEqualTo(apiTestCase.getApiDefinitionId())
                .andApiChangeEqualTo(true);
        Assertions.assertEquals(apiTestCaseMapper.selectByExample(example), List.of());

        // 设置忽略变更通知
        updateCase = new ApiTestCase();
        updateCase.setId(apiTestCase.getId());
        updateCase.setIgnoreApiChange(true);
        apiTestCaseMapper.updateByPrimaryKeySelective(updateCase);

        MsHTTPElement changeRequest = new MsHTTPElement();
        changeRequest.setBody(new Body());
        changeRequest.getBody().setBodyType(Body.BodyType.FORM_DATA.name());
        MsHTTPElement originRequest = new MsHTTPElement();
        originRequest.setBody(new Body());
        originRequest.getBody().setBodyType(Body.BodyType.XML.name());
        apiTestCaseService.handleApiParamChange(apiTestCase.getApiDefinitionId(), changeRequest, originRequest);
        // 校验忽略变更通知
        Assertions.assertEquals(apiTestCaseMapper.selectByPrimaryKey(apiTestCase.getId()).getApiChange(), false);

        updateCase.setIgnoreApiChange(false);
        apiTestCaseMapper.updateByPrimaryKeySelective(updateCase);
        apiTestCaseService.handleApiParamChange(apiTestCase.getApiDefinitionId(), changeRequest, originRequest);
        // 校验变更通知
        Assertions.assertEquals(apiTestCaseMapper.selectByPrimaryKey(apiTestCase.getId()).getApiChange(), true);
        Assertions.assertEquals(apiTestCaseMapper.selectByPrimaryKey(apiTestCase.getId()).getIgnoreApiDiff(), false);
    }

    @Test
    @Order(3)
    public void getApiCompareData() throws Exception {
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOkAndReturn(API_COMPARE, apiTestCase.getId());
        Map apiCaseCompareData = (Map) parseResponse(mvcResult).get("data");
        Assertions.assertNotNull(apiCaseCompareData.get("apiRequest"));
        Assertions.assertNotNull(apiCaseCompareData.get("caseRequest"));
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ, API_COMPARE, apiTestCase.getId());
    }

    @Test
    @Order(3)
    public void clearApiChange() throws Exception {
        ApiTestCase updateCase = new ApiTestCase();
        updateCase.setApiChange(true);
        updateCase.setId(apiTestCase.getId());
        apiTestCaseMapper.updateByPrimaryKeySelective(updateCase);
        this.requestGetWithOk(API_CHANGE_CLEAR, apiTestCase.getId());
        ApiTestCase result = apiTestCaseMapper.selectByPrimaryKey(apiTestCase.getId());
        Assertions.assertFalse(result.getApiChange());
        Assertions.assertTrue(result.getIgnoreApiDiff());

        //校验日志
        checkLog(apiTestCase.getId(), OperationLogType.UPDATE, getBasePath() + MessageFormat.format(API_CHANGE_CLEAR, apiTestCase.getId()));

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_ADD, API_CHANGE_CLEAR, apiTestCase.getId());
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE, API_CHANGE_CLEAR, apiTestCase.getId());
    }

    @Test
    @Order(3)
    public void ignoreApiChange() throws Exception {
        ApiTestCase updateCase = new ApiTestCase();
        updateCase.setApiChange(true);
        updateCase.setId(apiTestCase.getId());
        apiTestCaseMapper.updateByPrimaryKeySelective(updateCase);
        this.requestGetWithOk(API_CHANGE_IGNORE, apiTestCase.getId(), true);
        ApiTestCase result = apiTestCaseMapper.selectByPrimaryKey(apiTestCase.getId());
        Assertions.assertFalse(result.getApiChange());
        Assertions.assertTrue(result.getIgnoreApiDiff());
        Assertions.assertTrue(result.getIgnoreApiChange());
        this.requestGetWithOk(API_CHANGE_IGNORE, apiTestCase.getId(), false);
        result = apiTestCaseMapper.selectByPrimaryKey(apiTestCase.getId());
        Assertions.assertFalse(result.getApiChange());
        Assertions.assertTrue(result.getIgnoreApiDiff());
        Assertions.assertFalse(result.getIgnoreApiChange());

        //校验日志
        checkLog(apiTestCase.getId(), OperationLogType.UPDATE, getBasePath() + MessageFormat.format(API_CHANGE_IGNORE, apiTestCase.getId(), true));

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_ADD, API_CHANGE_IGNORE, apiTestCase.getId(), true);
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE, API_CHANGE_IGNORE, apiTestCase.getId(), false);
    }

    @Test
    @Order(4)
    public void batchSyncApiChange() throws Exception {
        ApiCaseBatchSyncRequest request = new ApiCaseBatchSyncRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.getNotificationConfig().setApiCaseCreator(true);
        request.getNotificationConfig().setScenarioCreator(true);
        this.requestPostWithOk(BATCH_API_CHANGE_SYNC, request);

        request.setSelectIds(List.of(apiTestCase.getId()));
        request.setDeleteRedundantParam(true);
        this.requestPostWithOk(BATCH_API_CHANGE_SYNC, request);

        ApiTestCase result = apiTestCaseMapper.selectByPrimaryKey(apiTestCase.getId());
        Assertions.assertFalse(result.getApiChange());

        User user = userMapper.selectByPrimaryKey("admin");
        request.getNotificationConfig().setScenarioCreator(false);
        apiTestCaseNoticeService.batchSyncSendNotice(List.of(apiTestCase), user, DEFAULT_PROJECT_ID, request.getNotificationConfig(), NoticeConstants.Event.CASE_UPDATE);

        request.getNotificationConfig().setApiCaseCreator(false);
        apiTestCaseNoticeService.batchSyncSendNotice(List.of(apiTestCase), user, DEFAULT_PROJECT_ID, request.getNotificationConfig(), NoticeConstants.Event.CASE_UPDATE);

        request.getNotificationConfig().setScenarioCreator(true);
        apiTestCaseNoticeService.batchSyncSendNotice(List.of(apiTestCase), user, DEFAULT_PROJECT_ID, request.getNotificationConfig(), NoticeConstants.Event.CASE_UPDATE);

        //校验日志
        checkLog(apiTestCase.getId(), OperationLogType.UPDATE, getBasePath() + BATCH_API_CHANGE_SYNC);

        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE, BATCH_API_CHANGE_SYNC, request);
    }

    @Test
    @Order(4)
    public void syncApiChange() throws Exception {
        ApiCaseSyncRequest request = new ApiCaseSyncRequest();
        request.setId(apiTestCase.getId());
        request.setApiCaseRequest(JSON.parseObject(ApiDataUtils.toJSONString(new MsHTTPElement())));
        this.requestPostWithOk(API_CHANGE_SYNC, request);
        ApiTestCase result = apiTestCaseMapper.selectByPrimaryKey(apiTestCase.getId());
        Assertions.assertFalse(result.getApiChange());

        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE, API_CHANGE_SYNC, request);
    }

    /**
     * 测试关联的文件更新
     *
     * @throws Exception
     */
    public void testHandleFileAssociationUpgrade() throws Exception {
        List<ApiFile> originApiFiles = getApiFiles(fileMetadataId);
        MockMultipartFile file = new MockMultipartFile("file", "file_upload.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, "aa".getBytes());
        // 重新上传新文件
        String newFileId = baseFileManagementTestService.reUpload(fileMetadataId, file);
        // 更新关联的文件到最新文件
        baseFileManagementTestService.upgrade(fileMetadataId, apiTestCase.getId());
        // 校验文件是否替换
        Assertions.assertEquals(originApiFiles.size(), getApiFiles(newFileId).size());
        fileMetadataId = newFileId;
    }

    private List<ApiFile> getApiFiles(String fileId) {
        ApiTestCaseBlob apiTestCaseBlob = apiTestCaseBlobMapper.selectByPrimaryKey(apiTestCase.getId());
        AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(new String(apiTestCaseBlob.getRequest()), AbstractMsTestElement.class);
        return apiCommonService.getApiFilesByFileId(fileId, msTestElement);
    }

    private Object getMsElementParam(MsHTTPElement msHTTPElement) {
        return JSON.parseObject(ApiDataUtils.toJSONString(msHTTPElement));
    }

    @Test
    @Order(3)
    public void debug() throws Exception {
        ApiCaseRunRequest request = new ApiCaseRunRequest();
        request.setId(apiTestCase.getId());
        MsHTTPElement msHTTPElement = new MsHTTPElement();
        msHTTPElement.setPath("/test");
        msHTTPElement.setMethod("GET");
        request.setRequest(JSON.parseObject(ApiDataUtils.toJSONString(msHTTPElement)));
        request.setReportId(IDGenerator.nextStr());
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setApiDefinitionId(apiTestCase.getApiDefinitionId());
        MvcResult mvcResult = this.requestPostAndReturn(DEBUG, request);
        ResultHolder resultHolder = JSON.parseObject(mvcResult.getResponse().getContentAsString(Charset.defaultCharset()), ResultHolder.class);
        Assertions.assertTrue(resultHolder.getCode() == ApiResultCode.RESOURCE_POOL_EXECUTE_ERROR.getCode() ||
                resultHolder.getCode() == MsHttpResultCode.SUCCESS.getCode());

        // 测试执行
        Environment environment = new Environment();
        environment.setId("test-host");
        environment.setProjectId(DEFAULT_PROJECT_ID);
        environment.setName("test-host");
        environment.setPos(0L);
        environment.setCreateTime(System.currentTimeMillis());
        environment.setUpdateTime(System.currentTimeMillis());
        environment.setCreateUser("admin");
        environment.setUpdateUser("admin");
        environment.setMock(false);
        environmentMapper.insert(environment);
        EnvironmentConfig environmentConfig = new EnvironmentConfig();
        List<HttpConfig> httpConfigs = new ArrayList<>();
        HttpConfig httpConfig = new HttpConfig();
        httpConfig.setHostname("www.aa.com");
        httpConfig.setType("NONE");
        httpConfig.setProtocol("HTTP");
        httpConfigs.add(httpConfig);
        environmentConfig.setHttpConfig(httpConfigs);
        HostConfig hostConfig = new HostConfig();
        hostConfig.setEnable(true);
        List<Host> hosts = new ArrayList<>();
        Host host = new Host();
        host.setIp("172.0.0.1");
        host.setDomain("www.aa.com");
        hosts.add(host);
        hostConfig.setHosts(hosts);
        environmentConfig.setHostConfig(hostConfig);
        EnvironmentBlob environmentBlob = new EnvironmentBlob();
        environmentBlob.setId(environment.getId());
        environmentBlob.setConfig(JSON.toJSONBytes(environmentConfig));
        environmentBlobMapper.insert(environmentBlob);

        request.setEnvironmentId("test-host");
        mvcResult = this.requestPostAndReturn(RUN_POST, request);
        resultHolder = JSON.parseObject(mvcResult.getResponse().getContentAsString(Charset.defaultCharset()), ResultHolder.class);
        Assertions.assertTrue(resultHolder.getCode() == ApiResultCode.RESOURCE_POOL_EXECUTE_ERROR.getCode() ||
                resultHolder.getCode() == MsHttpResultCode.SUCCESS.getCode());

        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_EXECUTE, DEBUG, request);
    }

    @Test
    @Order(3)
    public void run() throws Exception {
        assertErrorCode(this.requestGet(RUN_REAL_TIME, apiTestCase.getId(), "111"), ApiResultCode.RESOURCE_POOL_EXECUTE_ERROR);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_EXECUTE, RUN_REAL_TIME, apiTestCase.getId(), "11111");

        assertErrorCode(this.requestGet(RUN_GET, apiTestCase.getId()), ApiResultCode.RESOURCE_POOL_EXECUTE_ERROR);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_EXECUTE, RUN_GET, apiTestCase.getId());
    }

    @Test
    @Order(3)
    public void batchRun() throws Exception {
        ApiTestCaseBatchRunRequest request = new ApiTestCaseBatchRunRequest();
        List<String> ids = new ArrayList<>();
        ids.add(apiTestCase.getId());
        request.setSelectIds(ids);
        request.setProjectId(apiTestCase.getProjectId());
        ApiRunModeRequest apiRunModeRequest = new ApiRunModeRequest();
        apiRunModeRequest.setRunMode(ApiBatchRunMode.PARALLEL.name());
        apiRunModeRequest.setIntegratedReport(true);
        apiRunModeRequest.setStopOnFailure(false);
        apiRunModeRequest.setIntegratedReportName("aaaa");
        apiRunModeRequest.setPoolId("poolId");
        request.setRunModeConfig(apiRunModeRequest);
        this.requestPostWithOk(BATCH_RUN, request);
        request.setProtocols(List.of("HTTP"));
        this.requestPostWithOk(BATCH_RUN, request);

        apiRunModeRequest.setIntegratedReport(false);
        apiRunModeRequest.setStopOnFailure(true);
        this.requestPostWithOk(BATCH_RUN, request);

        apiRunModeRequest.setRunMode(ApiBatchRunMode.SERIAL.name());
        this.requestPostWithOk(BATCH_RUN, request);

        apiRunModeRequest.setIntegratedReport(true);
        this.requestPostWithOk(BATCH_RUN, request);

        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_EXECUTE, BATCH_RUN, request);
    }

    @Test
    @Order(3)
    public void get() throws Exception {
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOk(GET + apiTestCase.getId())
                .andReturn();
        ApiTestCaseDTO apiTestCaseDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResult).get("data")), ApiTestCaseDTO.class);
        // 校验数据是否正确
        ApiTestCase testCase = apiTestCaseMapper.selectByPrimaryKey(apiTestCase.getId());
        ApiTestCaseDTO copyApiTestCaseDTO = BeanUtils.copyBean(new ApiTestCaseDTO(), testCase);
        copyApiTestCaseDTO.setInconsistentWithApi(true);
        if (CollectionUtils.isNotEmpty(testCase.getTags())) {
            copyApiTestCaseDTO.setTags(testCase.getTags());
        } else {
            copyApiTestCaseDTO.setTags(new ArrayList<>());
        }
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(apiTestCase.getApiDefinitionId());
        copyApiTestCaseDTO.setMethod(apiDefinition.getMethod());
        copyApiTestCaseDTO.setPath(apiDefinition.getPath());
        copyApiTestCaseDTO.setProtocol(apiDefinition.getProtocol());
        ApiTestCaseBlob apiTestCaseBlob = apiTestCaseBlobMapper.selectByPrimaryKey(apiTestCase.getId());
        ApiTestCaseFollowerExample example = new ApiTestCaseFollowerExample();
        example.createCriteria().andCaseIdEqualTo(apiTestCase.getId()).andUserIdEqualTo("admin");
        List<ApiTestCaseFollower> followers = apiTestCaseFollowerMapper.selectByExample(example);
        copyApiTestCaseDTO.setFollow(CollectionUtils.isNotEmpty(followers));
        AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(new String(apiTestCaseBlob.getRequest()), AbstractMsTestElement.class);
        apiCommonService.setLinkFileInfo(apiTestCase.getId(), msTestElement);
        MsHTTPElement msHTTPElement = (MsHTTPElement) msTestElement;
        msHTTPElement.setMethod(apiDefinition.getMethod());
        msHTTPElement.setPath(apiDefinition.getPath());
        msHTTPElement.setModuleId(apiDefinition.getModuleId());
        msHTTPElement.setNum(apiDefinition.getNum());
        copyApiTestCaseDTO.setRequest(msTestElement);
        copyApiTestCaseDTO.setApiDefinitionName(apiDefinition.getName());
        copyApiTestCaseDTO.setApiDefinitionNum(apiDefinition.getNum());

        msHTTPElement = (MsHTTPElement) apiTestCaseDTO.getRequest();
        Assertions.assertEquals(msHTTPElement.getMethod(), apiDefinition.getMethod());
        Assertions.assertEquals(msHTTPElement.getPath(), apiDefinition.getPath());
        Assertions.assertEquals(msHTTPElement.getModuleId(), apiDefinition.getModuleId());
        Assertions.assertEquals(apiTestCaseDTO, copyApiTestCaseDTO);

        this.requestGetWithOk(GET + anotherApiTestCase.getId())
                .andReturn();

        this.requestGet(GET + "111").andExpect(ERROR_REQUEST_MATCHER);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ, GET + apiTestCase.getId());
    }

    @Test
    @Order(4)
    public void moveToGC() throws Exception {
        // @@请求成功
        this.requestGetWithOk(DELETE_TO_GC, apiTestCase.getId());
        ApiTestCase apiCase = apiTestCaseMapper.selectByPrimaryKey(apiTestCase.getId());
        Assertions.assertTrue(apiCase.getDeleted());
        Assertions.assertEquals(apiCase.getDeleteUser(), "admin");
        Assertions.assertNotNull(apiCase.getDeleteTime());
        // @@校验日志
        checkLog(apiTestCase.getId(), OperationLogType.DELETE);
        this.requestGet(DELETE_TO_GC, "111").andExpect(ERROR_REQUEST_MATCHER);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_DELETE, DELETE_TO_GC, apiTestCase.getId());
    }

    @Test
    @Order(5)
    public void recover() throws Exception {
        // @@请求成功
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(BASE_PATH + RECOVER + apiTestCase.getId());
        requestBuilder
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN")
                .header(SessionConstants.CURRENT_PROJECT, DEFAULT_PROJECT_ID);
        mockMvc.perform(requestBuilder)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        ApiTestCase apiCase = apiTestCaseMapper.selectByPrimaryKey(apiTestCase.getId());
        Assertions.assertFalse(apiCase.getDeleted());
        // @@校验日志
        checkLog(apiTestCase.getId(), OperationLogType.RECOVER);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_DELETE, RECOVER + apiTestCase.getId());
    }

    //关注
    @Test
    @Order(6)
    public void follow() throws Exception {
        // @@请求成功
        this.requestGetWithOk(FOLLOW + apiTestCase.getId());
        ApiTestCaseFollowerExample example = new ApiTestCaseFollowerExample();
        example.createCriteria().andCaseIdEqualTo(apiTestCase.getId()).andUserIdEqualTo("admin");
        List<ApiTestCaseFollower> followers = apiTestCaseFollowerMapper.selectByExample(example);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(followers));
        // @@校验日志
        checkLog(apiTestCase.getId(), OperationLogType.UPDATE);
        this.requestGet(FOLLOW + "111").andExpect(ERROR_REQUEST_MATCHER);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE, FOLLOW + apiTestCase.getId());
    }

    @Test
    @Order(7)
    public void unfollow() throws Exception {
        // @@请求成功
        this.requestGetWithOk(UNFOLLOW + apiTestCase.getId());
        ApiTestCaseFollowerExample example = new ApiTestCaseFollowerExample();
        example.createCriteria().andCaseIdEqualTo(apiTestCase.getId()).andUserIdEqualTo("admin");
        List<ApiTestCaseFollower> followers = apiTestCaseFollowerMapper.selectByExample(example);
        Assertions.assertTrue(CollectionUtils.isEmpty(followers));
        // @@校验日志
        checkLog(apiTestCase.getId(), OperationLogType.UPDATE);
        this.requestGet(UNFOLLOW + "111").andExpect(ERROR_REQUEST_MATCHER);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE, UNFOLLOW + apiTestCase.getId());
    }

    @Test
    @Order(8)
    public void update() throws Exception {
        // @@请求成功
        ApiTestCaseUpdateRequest request = new ApiTestCaseUpdateRequest();
        request.setId(apiTestCase.getId());
        request.setName("update");
        request.setPriority("P1");
        request.setStatus(ApiDefinitionStatus.PROCESSING.name());
        request.setTags(List.of("tag1", "tag2"));
        request.setEnvironmentId(null);
        MsHTTPElement msHttpElement = MsHTTPElementTest.getMsHttpElement();
        request.setRequest(getMsElementParam(msHttpElement));
        // 不带文件的更新
        request.setUnLinkFileIds(List.of(fileMetadataId));
        request.setDeleteFileIds(List.of(uploadFileId));
        this.requestPostWithOk(UPDATE, request);
        // 校验请求成功数据
        assertUpdateApiDebug(request, msHttpElement, request.getId());
        assertUploadFile(apiTestCase.getId(), List.of());
        assertLinkFile(apiTestCase.getId(), List.of());
        // 校验请求成功数据
        request.setTags(new ArrayList<>());
        MvcResult mvcResult = this.requestPostWithOkAndReturn(UPDATE, request);
        ApiTestCase resultData = getResultData(mvcResult, ApiTestCase.class);
        assertUpdateApiDebug(request, msHttpElement, resultData.getId());

        // 带文件的更新
        String fileId = doUploadTempFile(getMockMultipartFile());
        request.setUploadFileIds(List.of(fileId));
        request.setLinkFileIds(List.of(fileMetadataId));
        request.setDeleteFileIds(null);
        request.setUnLinkFileIds(null);
        this.requestPostWithOk(UPDATE, request);
        // 校验请求成功数据
        assertUpdateApiDebug(request, msHttpElement, request.getId());
        assertUploadFile(apiTestCase.getId(), List.of(fileId));
        assertLinkFile(apiTestCase.getId(), List.of(fileMetadataId));

        // 删除了上一次上传的文件，重新上传一个文件
        request.setDeleteFileIds(List.of(fileId));
        String newFileId1 = doUploadTempFile(getMockMultipartFile());
        request.setUploadFileIds(List.of(newFileId1));
        request.setUnLinkFileIds(List.of(fileMetadataId));
        request.setLinkFileIds(List.of(fileMetadataId));
        this.requestPostWithOk(UPDATE, request);
        assertUpdateApiDebug(request, msHttpElement, request.getId());
        assertUploadFile(apiTestCase.getId(), List.of(newFileId1));
        assertLinkFile(apiTestCase.getId(), List.of(fileMetadataId));

        // 已有一个文件，再上传一个文件
        String newFileId2 = doUploadTempFile(getMockMultipartFile());
        request.setUploadFileIds(List.of(newFileId2));
        this.requestPostWithOk(UPDATE, request);
        // 校验请求成功数据
        assertUpdateApiDebug(request, msHttpElement, request.getId());
        assertUploadFile(apiTestCase.getId(), List.of(newFileId1, newFileId2));
        assertLinkFile(apiTestCase.getId(), List.of(fileMetadataId));

        // @@重名校验异常
        request.setName("update");
        request.setId(anotherApiTestCase.getId());
        this.requestPost(UPDATE, request).andExpect(ERROR_REQUEST_MATCHER);
        // 校验接口是否存在
        request.setId("111");
        this.requestPost(UPDATE, request).andExpect(ERROR_REQUEST_MATCHER);

        // @@校验日志
        checkLog(apiTestCase.getId(), OperationLogType.UPDATE);
        // @@异常参数校验
        createdGroupParamValidateTest(ApiTestCaseUpdateRequest.class, UPDATE);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE, UPDATE, request);
    }

    @Test
    @Order(9)
    public void testTransfer() throws Exception {
        this.requestGetWithOk("transfer/options/" + DEFAULT_PROJECT_ID);
        ApiTransferRequest apiTransferRequest = new ApiTransferRequest();
        apiTransferRequest.setSourceId(apiTestCase.getId());
        apiTransferRequest.setProjectId(DEFAULT_PROJECT_ID);
        apiTransferRequest.setModuleId("root");
        apiTransferRequest.setLocal(true);
        String uploadFileId = doUploadTempFile(getMockMultipartFile());
        apiTransferRequest.setFileId(uploadFileId);
        apiTransferRequest.setFileName("test-api-test-case");
        apiTransferRequest.setOriginalName("test-api-test-case.txt");
        this.requestPost("transfer", apiTransferRequest).andExpect(status().isOk());
        //文件不存在
        apiTransferRequest.setFileId("111");
        this.requestPost("transfer", apiTransferRequest).andExpect(status().is5xxServerError());
        //文件已经上传
        ApiFileResourceExample apiFileResourceExample = new ApiFileResourceExample();
        apiFileResourceExample.createCriteria().andResourceIdEqualTo(apiTestCase.getId());
        List<ApiFileResource> apiFileResources = apiFileResourceMapper.selectByExample(apiFileResourceExample);
        Assertions.assertFalse(apiFileResources.isEmpty());
        apiTransferRequest.setFileId(apiFileResources.getFirst().getFileId());
        apiTransferRequest.setFileName("test-api-test-case-1");
        apiTransferRequest.setOriginalName("test-api-test-case-1.txt");
        this.requestPost("transfer", apiTransferRequest).andExpect(status().isOk());

    }

    @Test
    @Order(9)
    public void testPos() throws Exception {
        PosRequest posRequest = new PosRequest();
        posRequest.setProjectId(DEFAULT_PROJECT_ID);
        posRequest.setTargetId(apiTestCase.getId());
        posRequest.setMoveId(anotherApiTestCase.getId());
        posRequest.setMoveMode("AFTER");
        this.requestPostWithOkAndReturn(POS_URL, posRequest);

        posRequest.setMoveMode("BEFORE");
        this.requestPostWithOkAndReturn(POS_URL, posRequest);
        apiTestCaseService.refreshPos(DEFAULT_PROJECT_ID);
    }

    @Test
    @Order(10)
    public void testExecuteList() throws Exception {
        ApiTestCase first = apiTestCaseMapper.selectByExample(new ApiTestCaseExample()).getFirst();
        List<ApiReport> reports = new ArrayList<>();
        List<ApiTestCaseRecord> records = new ArrayList<>();

        String planId = testPlanMapper.selectByExample(new TestPlanExample()).getFirst().getId();
        TestPlanApiCase testPlanApiCase = new TestPlanApiCase();
        testPlanApiCase.setTestPlanId(first.getId());
        testPlanApiCase.setId(IDGenerator.nextStr());
        testPlanApiCase.setApiCaseId(first.getId());
        testPlanApiCase.setCreateUser("admin");
        testPlanApiCase.setCreateTime(System.currentTimeMillis());
        testPlanApiCase.setLastExecTime(System.currentTimeMillis());
        testPlanApiCase.setLastExecReportId(IDGenerator.nextStr());
        testPlanApiCase.setLastExecResult(ResultStatus.SUCCESS.name());
        testPlanApiCase.setPos(1024l);
        testPlanApiCase.setTestPlanCollectionId(planId);
        testPlanApiCaseMapper.insert(testPlanApiCase);

        for (int i = 0; i < 10; i++) {
            ApiReport apiReport = new ApiReport();
            apiReport.setId(IDGenerator.nextStr());
            apiReport.setProjectId(DEFAULT_PROJECT_ID);
            apiReport.setName("api-case-name" + i);
            apiReport.setStartTime(System.currentTimeMillis());
            apiReport.setCreateUser("admin");
            apiReport.setUpdateUser("admin");
            apiReport.setUpdateTime(System.currentTimeMillis());
            apiReport.setPoolId("api-pool-id" + i);
            apiReport.setEnvironmentId("api-environment-id" + i);
            apiReport.setRunMode("api-run-mode" + i);
            if (i % 2 == 0) {
                apiReport.setStatus(ResultStatus.SUCCESS.name());
            } else {
                apiReport.setTestPlanCaseId(testPlanApiCase.getId());
                apiReport.setStatus(ResultStatus.ERROR.name());
            }
            apiReport.setTriggerMode("api-trigger-mode" + i);
            reports.add(apiReport);
            ApiTestCaseRecord record = new ApiTestCaseRecord();
            record.setApiTestCaseId(first.getId());
            record.setApiReportId(apiReport.getId());
            records.add(record);
        }
        apiReportService.insertApiReport(reports, records);
        ExecutePageRequest request = new ExecutePageRequest();
        request.setId(first.getId());
        request.setPageSize(10);
        request.setCurrent(1);
        MvcResult mvcResult = requestPostWithOkAndReturn(EXECUTE, request);
        Pager<?> returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        request.setFilter(new HashMap<>() {{
            put("status", List.of(ResultStatus.SUCCESS.name()));
        }});
        mvcResult = requestPostWithOkAndReturn(EXECUTE, request);
        returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        Assertions.assertTrue(((List<ApiReport>) returnPager.getList()).size() <= request.getPageSize());
        List<ExecuteReportDTO> reportDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ExecuteReportDTO.class);
        reportDTOS.forEach(apiReport -> {
            Assertions.assertEquals(apiReport.getStatus(), ResultStatus.SUCCESS.name());
        });
    }

    @Test
    @Order(11)
    public void page() throws Exception {
        // @@请求成功
        ApiTestCaseAddRequest request = new ApiTestCaseAddRequest();
        request.setApiDefinitionId(anotherApiDefinitionId);
        request.setName("testApiDefinitionId1");
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setPriority("P0");
        request.setStatus(ApiDefinitionStatus.DEBUGGING.name());
        request.setTags(new LinkedHashSet<>(List.of("tag1", "tag2")));
        MsHTTPElement msHttpElement = MsHTTPElementTest.getMsHttpElement();
        request.setRequest(getMsElementParam(msHttpElement));
        this.requestPost(ADD, request);
        ApiTestCasePageRequest pageRequest = new ApiTestCasePageRequest();
        pageRequest.setProjectId(DEFAULT_PROJECT_ID);
        pageRequest.setPageSize(10);
        pageRequest.setCurrent(1);
        requestPostWithOkAndReturn(PAGE, pageRequest);
        pageRequest.setProtocols(List.of("HTTP"));
        MvcResult mvcResult = requestPostWithOkAndReturn(PAGE, pageRequest);
        Pager<?> returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), pageRequest.getCurrent());
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(((List<ApiTestCaseDTO>) returnPager.getList()).size() <= pageRequest.getPageSize());

        //查询apiDefinitionId1的数据
        pageRequest.setApiDefinitionId(anotherApiDefinitionId);
        mvcResult = requestPostWithOkAndReturn(PAGE, pageRequest);
        returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), pageRequest.getCurrent());

        List<ApiTestCaseDTO> caseDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ApiTestCaseDTO.class);
        caseDTOS.forEach(caseDTO -> Assertions.assertEquals(caseDTO.getApiDefinitionId(), anotherApiDefinitionId));

        //查询模块为moduleId1的数据
        pageRequest.setApiDefinitionId(null);
        pageRequest.setModuleIds(List.of("moduleId1"));
        mvcResult = requestPostWithOkAndReturn(PAGE, pageRequest);
        returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), pageRequest.getCurrent());
        caseDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ApiTestCaseDTO.class);
        caseDTOS.forEach(caseDTO -> Assertions.assertEquals(apiDefinitionMapper.selectByPrimaryKey(caseDTO.getApiDefinitionId()).getModuleId(), "moduleId1"));

        pageRequest.setModuleIds(null);
        pageRequest.setSort(new HashMap<>() {{
            put("createTime", "asc");
        }});
        requestPostWithOkAndReturn(PAGE, pageRequest);
        //校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ, PAGE, pageRequest);
    }

    @Test
    @Order(12)
    public void updateStatus() throws Exception {
        // @@请求成功
        this.requestGetWithOk(UPDATE_STATUS + "/" + apiTestCase.getId() + "/Underway");
        ApiTestCase apiCase = apiTestCaseMapper.selectByPrimaryKey(apiTestCase.getId());
        Assertions.assertEquals(apiCase.getStatus(), "Underway");
        // @@校验日志
        checkLog(apiTestCase.getId(), OperationLogType.UPDATE);
        this.requestGet(UPDATE_STATUS + "/" + "11111" + "/Underway").andExpect(ERROR_REQUEST_MATCHER);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE, UPDATE_STATUS + "/" + apiTestCase.getId() + "/Underway");
    }

    @Test
    @Order(12)
    public void updatePriority() throws Exception {
        // @@请求成功
        this.requestGetWithOk(UPDATE_PRIORITY + "/" + apiTestCase.getId() + "/P1");
        ApiTestCase apiCase = apiTestCaseMapper.selectByPrimaryKey(apiTestCase.getId());
        Assertions.assertEquals(apiCase.getPriority(), "P1");
        // @@校验日志
        checkLog(apiTestCase.getId(), OperationLogType.UPDATE);
        this.requestGet(UPDATE_PRIORITY + "/" + "11111" + "/P1").andExpect(ERROR_REQUEST_MATCHER);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE, UPDATE_PRIORITY + "/" + apiTestCase.getId() + "/P1");
    }

    @Test
    @Order(13)
    public void batchEdit() throws Exception {
        // 追加标签
        ApiCaseBatchEditRequest request = new ApiCaseBatchEditRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setType("Tags");
        request.setAppend(true);
        request.setClear(false);
        request.setSelectAll(true);
        request.setTags(new LinkedHashSet<>(List.of("tag1", "tag3", "tag4")));
        requestPostWithOkAndReturn(BATCH_EDIT, request);
        request.setProtocols(List.of("HTTP"));
        requestPostWithOkAndReturn(BATCH_EDIT, request);
        ApiTestCaseExample example = new ApiTestCaseExample();
        List<String> ids = extApiTestCaseMapper.getIds(request, false);
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andDeletedEqualTo(false).andIdIn(ids);
        apiTestCaseMapper.selectByExample(example).forEach(apiTestCase -> {
            Assertions.assertTrue(apiTestCase.getTags().contains("tag1"));
            Assertions.assertTrue(apiTestCase.getTags().contains("tag3"));
            Assertions.assertTrue(apiTestCase.getTags().contains("tag4"));
        });
        //覆盖标签
        request.setTags(new LinkedHashSet<>(List.of("tag1")));
        request.setAppend(false);
        request.setClear(false);
        requestPostWithOkAndReturn(BATCH_EDIT, request);
        apiTestCaseMapper.selectByExample(example).forEach(apiTestCase -> {
            Assertions.assertEquals(apiTestCase.getTags(), List.of("tag1"));
        });
        request.setClear(true);
        requestPostWithOkAndReturn(BATCH_EDIT, request);
        apiTestCaseMapper.selectByExample(example).forEach(apiTestCase -> {
            Assertions.assertTrue(CollectionUtils.isEmpty(apiTestCase.getTags()));
        });
        //标签为空  报错
        request.setTags(new LinkedHashSet<>());
        this.requestPost(BATCH_EDIT, request, ERROR_REQUEST_MATCHER);
        //标签超出10个报错
        request.setTags(new LinkedHashSet<>(List.of("tag1", "tag2", "tag3", "tag4", "tag5", "tag6", "tag7", "tag8", "tag9", "tag10", "tag11")));
        this.requestPost(BATCH_EDIT, request, ERROR_REQUEST_MATCHER);
        //ids为空的时候
        request.setTags(new LinkedHashSet<>(List.of("tag1")));
        request.setSelectAll(true);
        List<ApiTestCase> caseList1 = apiTestCaseMapper.selectByExample(example);
        //提取所有的id
        List<String> apiIdList = caseList1.stream().map(ApiTestCase::getId).toList();
        request.setSelectIds(apiIdList);
        request.setExcludeIds(apiIdList);
        requestPostWithOkAndReturn(BATCH_EDIT, request);

        initCaseData();
        //优先级
        request.setTags(new LinkedHashSet<>());
        request.setSelectAll(true);
        request.setType("Priority");
        request.setModuleIds(List.of("case-moduleId"));
        request.setPriority("P3");
        request.setExcludeIds(new ArrayList<>());
        requestPostWithOkAndReturn(BATCH_EDIT, request);
        //判断数据的优先级是不是P3
        example.clear();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andApiDefinitionIdEqualTo(apiDefinitionId).andDeletedEqualTo(false);
        List<ApiTestCase> caseList = apiTestCaseMapper.selectByExample(example);

        caseList.forEach(apiTestCase -> Assertions.assertEquals(apiTestCase.getPriority(), "P3"));
        //优先级数据为空
        request.setPriority(null);
        this.requestPost(BATCH_EDIT, request, ERROR_REQUEST_MATCHER);
        //ids为空的时候
        request.setPriority("P3");
        request.setSelectAll(false);
        request.setSelectIds(List.of(apiTestCase.getId()));
        request.setExcludeIds(List.of(apiTestCase.getId()));
        requestPostWithOkAndReturn(BATCH_EDIT, request);
        //状态
        request.setPriority(null);
        request.setType("Status");
        request.setStatus(ApiDefinitionStatus.DEBUGGING.name());
        request.setSelectAll(true);
        request.setExcludeIds(new ArrayList<>());
        requestPostWithOkAndReturn(BATCH_EDIT, request);
        //判断数据的状态是不是DEBUGGING
        caseList = apiTestCaseMapper.selectByExample(example);
        caseList.forEach(apiTestCase -> Assertions.assertEquals(apiTestCase.getStatus(), ApiDefinitionStatus.DEBUGGING.name()));
        //状态数据为空
        request.setStatus(null);
        this.requestPost(BATCH_EDIT, request, ERROR_REQUEST_MATCHER);
        //环境
        request.setStatus(null);
        request.setType("Environment");
        EnvironmentExample environmentExample = new EnvironmentExample();
        environmentExample.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andMockEqualTo(true);
        List<Environment> environments = environmentMapper.selectByExample(environmentExample);
        request.setEnvironmentId(environments.getFirst().getId());
        requestPostWithOkAndReturn(BATCH_EDIT, request);
        //判断数据的环境是不是environments.getFirst().getId()
        caseList = apiTestCaseMapper.selectByExample(example);
        caseList.forEach(apiTestCase -> Assertions.assertEquals(apiTestCase.getEnvironmentId(), environments.getFirst().getId()));
        //环境数据为空
        request.setEnvironmentId(null);
        this.requestPost(BATCH_EDIT, request, ERROR_REQUEST_MATCHER);
        //环境不存在
        request.setEnvironmentId("111");
        this.requestPost(BATCH_EDIT, request, ERROR_REQUEST_MATCHER);
        //类型错误
        request.setType("111");
        this.requestPost(BATCH_EDIT, request, ERROR_REQUEST_MATCHER);

        //校验日志
        checkLog(apiTestCase.getId(), OperationLogType.UPDATE);
        //校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE, BATCH_EDIT, request);

    }

    @Test
    @Order(14)
    public void testGetHistory() throws Exception {
        ApiTestCase first = apiTestCaseMapper.selectByExample(new ApiTestCaseExample()).getFirst();
        OperationHistoryRequest request = new OperationHistoryRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSourceId(first.getId());
        request.setPageSize(10);
        request.setCurrent(1);
        projectVersionMapper.deleteByPrimaryKey("1.0");
        ProjectVersion version = new ProjectVersion();
        version.setId("1.0");
        version.setName("1.0");
        version.setProjectId(DEFAULT_PROJECT_ID);
        version.setCreateTime(System.currentTimeMillis());
        version.setLatest(true);
        version.setCreateUser("admin");
        projectVersionMapper.insertSelective(version);

        MvcResult mvcResult = requestPostWithOkAndReturn(HISTORY, request);
        Pager<?> returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        List<OperationHistoryDTO> reportDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), OperationHistoryDTO.class);
        reportDTOS.forEach(reportDTO -> Assertions.assertEquals(reportDTO.getSourceId(), first.getId()));

        request = new OperationHistoryRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSourceId("111");
        request.setPageSize(10);
        request.setCurrent(1);
        requestPostWithOkAndReturn(HISTORY, request);
    }

    @Test
    @Order(15)
    public void batchMoveGc() throws Exception {
        // @@请求成功
        ApiTestCaseBatchRequest request = new ApiTestCaseBatchRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSelectAll(false);
        requestPostWithOkAndReturn(BATCH_DELETE_TO_GC, request);
        request.setSelectIds(List.of(apiTestCase.getId()));
        request.setExcludeIds(List.of(apiTestCase.getId()));
        request.setProtocols(List.of("HTTP"));
        requestPostWithOkAndReturn(BATCH_DELETE_TO_GC, request);

        request.setSelectAll(true);
        request.setExcludeIds(new ArrayList<>());
        request.setApiDefinitionId(apiDefinitionId);
        request.setModuleIds(List.of("case-moduleId"));
        requestPostWithOkAndReturn(BATCH_DELETE_TO_GC, request);
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andApiDefinitionIdEqualTo(apiDefinitionId).andDeletedEqualTo(true);
        List<ApiTestCase> caseList = apiTestCaseMapper.selectByExample(example);
        caseList.forEach(apiTestCase -> Assertions.assertTrue(apiTestCase.getDeleted()));

        request.setSelectAll(true);
        request.setExcludeIds(new ArrayList<>());
        request.setModuleIds(List.of("case-moduleId"));
        requestPostWithOkAndReturn(BATCH_DELETE_TO_GC, request);
        //校验日志
        checkLog(apiTestCase.getId(), OperationLogType.DELETE);
        //校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_DELETE, BATCH_DELETE_TO_GC, request);
    }

    @Test
    @Order(16)
    public void trashPage() throws Exception {
        // @@请求成功
        ApiTestCasePageRequest pageRequest = new ApiTestCasePageRequest();
        pageRequest.setProjectId(DEFAULT_PROJECT_ID);
        pageRequest.setPageSize(10);
        pageRequest.setCurrent(1);
        requestPostWithOkAndReturn(TRASH_PAGE, pageRequest);
        pageRequest.setProtocols(List.of("HTTP"));
        MvcResult mvcResult = requestPostWithOkAndReturn(TRASH_PAGE, pageRequest);
        Pager<?> returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), pageRequest.getCurrent());
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(((List<ApiTestCaseDTO>) returnPager.getList()).size() <= pageRequest.getPageSize());

        //查询apiDefinitionId1的数据
        pageRequest.setApiDefinitionId(anotherApiDefinitionId);
        mvcResult = requestPostWithOkAndReturn(TRASH_PAGE, pageRequest);
        returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), pageRequest.getCurrent());

        List<ApiTestCaseDTO> caseDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ApiTestCaseDTO.class);
        caseDTOS.forEach(caseDTO -> Assertions.assertEquals(caseDTO.getApiDefinitionId(), anotherApiDefinitionId));

        //查询模块为moduleId1的数据
        pageRequest.setApiDefinitionId(null);
        pageRequest.setModuleIds(List.of("moduleId1"));
        mvcResult = requestPostWithOkAndReturn(TRASH_PAGE, pageRequest);
        returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), pageRequest.getCurrent());
        caseDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ApiTestCaseDTO.class);
        caseDTOS.forEach(caseDTO -> Assertions.assertEquals(apiDefinitionMapper.selectByPrimaryKey(caseDTO.getApiDefinitionId()).getModuleId(), "moduleId1"));

        pageRequest.setModuleIds(null);
        pageRequest.setSort(new HashMap<>() {{
            put("createTime", "asc");
        }});
        requestPostWithOkAndReturn(TRASH_PAGE, pageRequest);
        //校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ, TRASH_PAGE, pageRequest);
    }

    @Test
    @Order(17)
    public void batchRecover() throws Exception {
        // @@请求成功
        ApiTestCaseBatchRequest request = new ApiTestCaseBatchRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSelectAll(false);
        requestPostWithOkAndReturn(BATCH_RECOVER, request);
        request.setProtocols(List.of("HTTP"));
        request.setSelectIds(List.of(apiTestCase.getId()));
        request.setExcludeIds(List.of(apiTestCase.getId()));
        requestPostWithOkAndReturn(BATCH_RECOVER, request);

        ApiDefinition apiDefinition = new ApiDefinition();
        apiDefinition.setId(apiDefinitionId);
        apiDefinition.setDeleted(true);
        apiDefinitionMapper.updateByPrimaryKeySelective(apiDefinition);
        request.setSelectAll(true);
        request.setSelectIds(List.of(apiTestCase.getId()));
        request.setExcludeIds(List.of(apiTestCase.getId()));
        request.setApiDefinitionId(apiDefinitionId);
        request.setModuleIds(List.of("case-moduleId"));
        requestPostWithOkAndReturn(BATCH_RECOVER, request);
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andApiDefinitionIdEqualTo(apiDefinitionId).andDeletedEqualTo(false);
        List<ApiTestCase> caseList = apiTestCaseMapper.selectByExample(example);
        caseList.forEach(apiTestCase -> Assertions.assertFalse(apiTestCase.getDeleted()));

        request.setSelectAll(true);
        request.setExcludeIds(new ArrayList<>());
        request.setModuleIds(List.of("case-moduleId"));
        requestPostWithOkAndReturn(BATCH_RECOVER, request);
        //校验日志
        checkLog(apiTestCase.getId(), OperationLogType.DELETE);
        //校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_DELETE, BATCH_RECOVER, request);
        ApiTestCaseBatchRequest gcRequest = new ApiTestCaseBatchRequest();
        gcRequest.setProjectId(DEFAULT_PROJECT_ID);
        gcRequest.setSelectAll(true);
        gcRequest.setExcludeIds(new ArrayList<>());
        gcRequest.setApiDefinitionId(apiDefinitionId);
        requestPostWithOkAndReturn(BATCH_DELETE_TO_GC, gcRequest);
        ApiTestCaseExample example1 = new ApiTestCaseExample();
        example1.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andApiDefinitionIdEqualTo(apiDefinitionId).andDeletedEqualTo(true);
        List<ApiTestCase> caseList1 = apiTestCaseMapper.selectByExample(example1);
        caseList1.forEach(apiTestCase -> Assertions.assertTrue(apiTestCase.getDeleted()));
    }


    @Test
    @Order(20)
    public void delete() throws Exception {
        // @@请求成功
        this.requestGetWithOk(DELETE + apiTestCase.getId());
        ApiTestCase apiCase = apiTestCaseMapper.selectByPrimaryKey(apiTestCase.getId());
        Assertions.assertNull(apiCase);
        Assertions.assertNull(apiTestCaseBlobMapper.selectByPrimaryKey(apiTestCase.getId()));
        ApiTestCaseFollowerExample example = new ApiTestCaseFollowerExample();
        example.createCriteria().andCaseIdEqualTo(apiTestCase.getId());
        List<ApiTestCaseFollower> followers = apiTestCaseFollowerMapper.selectByExample(example);
        Assertions.assertTrue(CollectionUtils.isEmpty(followers));
        //校验minio文件为空
        FileRequest request = new FileRequest();
        request.setFolder(DefaultRepositoryDir.getApiCaseDir(DEFAULT_PROJECT_ID, apiTestCase.getId()));
        MinioRepository minioRepository = CommonBeanFactory.getBean(MinioRepository.class);
        assert minioRepository != null;
        List<String> fileNames = minioRepository.getFolderFileNames(request);
        //校验文件
        Assertions.assertEquals(0, fileNames.size());
        // @@校验日志
        checkLog(apiTestCase.getId(), OperationLogType.DELETE);
        this.requestGet(DELETE + "111").andExpect(ERROR_REQUEST_MATCHER);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_DELETE, DELETE + apiTestCase.getId());
    }

    @Test
    @Order(21)
    public void batchDeleted() throws Exception {
        // @@请求成功
        ApiTestCaseBatchRequest request = new ApiTestCaseBatchRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSelectAll(false);
        requestPostWithOkAndReturn(BATCH_DELETE, request);
        request.setSelectIds(List.of(apiTestCase.getId()));
        request.setExcludeIds(List.of(apiTestCase.getId()));
        request.setProtocols(List.of("HTTP"));
        requestPostWithOkAndReturn(BATCH_DELETE, request);
        request.setSelectAll(true);
        request.setExcludeIds(new ArrayList<>());
        request.setApiDefinitionId(apiDefinitionId);
        requestPostWithOkAndReturn(BATCH_DELETE_TO_GC, request);
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSelectAll(true);
        request.setApiDefinitionId(apiDefinitionId);
        requestPostWithOkAndReturn(BATCH_DELETE, request);
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andApiDefinitionIdEqualTo(apiDefinitionId);
        //数据为空
        Assertions.assertEquals(0, apiTestCaseMapper.selectByExample(example).size());

        //校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_DELETE, BATCH_DELETE, request);
    }

    @Test
    @Order(20)
    public void testGetRef() throws Exception {
        //插入假数据
        ApiScenario apiScenario = new ApiScenario();
        apiScenario.setId(IDGenerator.nextStr());
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
        apiScenario.setGrouped(false);
        apiScenarioMapper.insertSelective(apiScenario);
        List<ApiScenarioStep> apiScenarioSteps = new ArrayList<>();
        ApiScenarioStep apiScenarioStep = new ApiScenarioStep();
        apiScenarioStep.setId(IDGenerator.nextStr());
        apiScenarioStep.setProjectId(DEFAULT_PROJECT_ID);
        apiScenarioStep.setScenarioId(apiScenario.getId());
        apiScenarioStep.setResourceId("test-case-get-ref");
        apiScenarioStep.setRefType("COPY");
        apiScenarioStep.setStepType("CASE");
        apiScenarioStep.setName("用例步骤");
        apiScenarioStep.setSort(1L);
        apiScenarioStep.setParentId("NONE");
        apiScenarioStep.setVersionId("1.0");
        apiScenarioStep.setEnable(true);
        apiScenarioSteps.add(apiScenarioStep);
        apiScenarioStep = new ApiScenarioStep();
        apiScenarioStep.setId(IDGenerator.nextStr());
        apiScenarioStep.setProjectId(DEFAULT_PROJECT_ID);
        apiScenarioStep.setScenarioId(apiScenario.getId());
        apiScenarioStep.setResourceId("test-api-get-ref");
        apiScenarioStep.setRefType("REF");
        apiScenarioStep.setStepType("CASE");
        apiScenarioStep.setName("用例步骤");
        apiScenarioStep.setSort(1L);
        apiScenarioStep.setParentId("NONE");
        apiScenarioStep.setVersionId("1.0");
        apiScenarioStep.setEnable(true);
        apiScenarioSteps.add(apiScenarioStep);
        apiScenarioStep = new ApiScenarioStep();
        apiScenarioStep.setId(IDGenerator.nextStr());
        apiScenarioStep.setProjectId(DEFAULT_PROJECT_ID);
        apiScenarioStep.setScenarioId(apiScenario.getId());
        apiScenarioStep.setResourceId("test-api-get-ref");
        apiScenarioStep.setRefType("REF");
        apiScenarioStep.setStepType("CASE");
        apiScenarioStep.setName("用例步骤");
        apiScenarioStep.setSort(1L);
        apiScenarioStep.setParentId("NONE");
        apiScenarioStep.setVersionId("1.0");
        apiScenarioStep.setEnable(true);
        apiScenarioSteps.add(apiScenarioStep);
        apiScenarioStepMapper.batchInsert(apiScenarioSteps);
        ReferenceRequest request = new ReferenceRequest();
        request.setResourceId("test-case-get-ref");
        request.setCurrent(1);
        request.setPageSize(10);
        MvcResult mvcResult = this.requestPostWithOkAndReturn("/get-reference", request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        Pager<?> pageData = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        Assertions.assertNotNull(pageData);
    }


}