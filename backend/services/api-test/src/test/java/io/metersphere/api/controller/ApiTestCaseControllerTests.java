package io.metersphere.api.controller;

import io.metersphere.api.constants.ApiConstants;
import io.metersphere.api.controller.param.ApiTestCaseAddRequestDefinition;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.ApiFile;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.request.ApiTransferRequest;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.mapper.*;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.api.service.BaseFileManagementTestService;
import io.metersphere.api.service.definition.ApiReportService;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.domain.ProjectVersion;
import io.metersphere.project.dto.filemanagement.FileInfo;
import io.metersphere.project.mapper.ProjectVersionMapper;
import io.metersphere.project.service.FileAssociationService;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentExample;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.file.MinioRepository;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.OperationHistoryDTO;
import io.metersphere.system.dto.request.OperationHistoryRequest;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.log.constants.OperationLogType;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiTestCaseControllerTests extends BaseTest {
    private static final String BASE_PATH = "/api/case/";
    private static final String ADD = BASE_PATH + "add";
    private static final String GET = BASE_PATH + "get-detail/";
    private static final String MOVE_TO_GC = BASE_PATH + "move-gc/";
    private static final String RECOVER = BASE_PATH + "recover/";
    private static final String FOLLOW = BASE_PATH + "follow/";
    private static final String UNFOLLOW = BASE_PATH + "unfollow/";
    private static final String DELETE = BASE_PATH + "delete/";
    private static final String UPDATE = BASE_PATH + "update";
    private static final String PAGE = BASE_PATH + "page";
    private static final String TRASH_PAGE = BASE_PATH + "trash/page";
    private static final String UPDATE_STATUS = BASE_PATH + "update-status";
    private static final String UPDATE_PRIORITY = BASE_PATH + "update-priority";
    private static final String BATCH_EDIT = BASE_PATH + "batch/edit";
    private static final String BATCH_DELETE = BASE_PATH + "batch/delete";
    private static final String BATCH_MOVE_GC = BASE_PATH + "batch/move-gc";
    private static final String BATCH_RECOVER = BASE_PATH + "batch/recover";
    private static final String POS_URL = BASE_PATH + "/edit/pos";
    private static final String UPLOAD_TEMP_FILE = BASE_PATH + "/upload/temp/file";
    private static final String EXECUTE = BASE_PATH + "/execute/page";
    private static final String HISTORY = BASE_PATH + "/operation-history/page";

    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();
    private static ApiTestCase apiTestCase;

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

    private MvcResult responsePost(String url, Object param) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(param))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    public void initApiData() {
        ApiDefinition apiDefinition = new ApiDefinition();
        apiDefinition.setId("apiDefinitionId");
        apiDefinition.setProjectId(DEFAULT_PROJECT_ID);
        apiDefinition.setName(StringUtils.join("接口定义", apiDefinition.getId()));
        apiDefinition.setModuleId("case-moduleId");
        apiDefinition.setProtocol(ApiConstants.HTTP_PROTOCOL);
        apiDefinition.setMethod("GET");
        apiDefinition.setStatus("未规划");
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
        apiDefinition.setId("apiDefinitionId1");
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
            apiTestCase.setApiDefinitionId("apiDefinitionId");
            apiTestCase.setProjectId(DEFAULT_PROJECT_ID);
            apiTestCase.setName(StringUtils.join("接口用例", apiTestCase.getId()));
            apiTestCase.setPriority("P0");
            apiTestCase.setStatus("Underway");
            apiTestCase.setNum(NumGenerator.nextNum(DEFAULT_PROJECT_ID + "_" + "100001", ApplicationNumScope.API_TEST_CASE));
            apiTestCase.setPos(0L);
            apiTestCase.setCreateTime(System.currentTimeMillis());
            apiTestCase.setUpdateTime(System.currentTimeMillis());
            apiTestCase.setCreateUser("admin");
            apiTestCase.setUpdateUser("admin");
            apiTestCase.setVersionId("1.0");
            apiTestCase.setDeleted(false);
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
        request.setApiDefinitionId("apiDefinitionId");
        request.setName("test");
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setPriority("P0");
        request.setStatus("Underway");
        request.setTags(new LinkedHashSet<>(List.of("tag1", "tag2")));
        request.setEnvironmentId(environments.get(0).getId());
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
        request.setApiDefinitionId("apiDefinitionId");
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
    public void get() throws Exception {
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOk(GET + apiTestCase.getId())
                .andReturn();
        ApiTestCaseDTO apiDebugDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResult).get("data")), ApiTestCaseDTO.class);
        // 校验数据是否正确
        ApiTestCase testCase = apiTestCaseMapper.selectByPrimaryKey(apiTestCase.getId());
        ApiTestCaseDTO copyApiDebugDTO = BeanUtils.copyBean(new ApiTestCaseDTO(), testCase);
        if (CollectionUtils.isNotEmpty(testCase.getTags())) {
            copyApiDebugDTO.setTags(testCase.getTags());
        } else {
            copyApiDebugDTO.setTags(new ArrayList<>());
        }
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(apiTestCase.getApiDefinitionId());
        copyApiDebugDTO.setMethod(apiDefinition.getMethod());
        copyApiDebugDTO.setPath(apiDefinition.getPath());
        ApiTestCaseBlob apiTestCaseBlob = apiTestCaseBlobMapper.selectByPrimaryKey(apiTestCase.getId());
        ApiTestCaseFollowerExample example = new ApiTestCaseFollowerExample();
        example.createCriteria().andCaseIdEqualTo(apiTestCase.getId()).andUserIdEqualTo("admin");
        List<ApiTestCaseFollower> followers = apiTestCaseFollowerMapper.selectByExample(example);
        copyApiDebugDTO.setFollow(CollectionUtils.isNotEmpty(followers));
        AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(new String(apiTestCaseBlob.getRequest()), AbstractMsTestElement.class);
        apiCommonService.setLinkFileInfo(apiTestCase.getId(), msTestElement);
        copyApiDebugDTO.setRequest(msTestElement);
        Assertions.assertEquals(apiDebugDTO, copyApiDebugDTO);
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
        this.requestGetWithOk(MOVE_TO_GC + apiTestCase.getId());
        ApiTestCase apiCase = apiTestCaseMapper.selectByPrimaryKey(apiTestCase.getId());
        Assertions.assertTrue(apiCase.getDeleted());
        Assertions.assertEquals(apiCase.getDeleteUser(), "admin");
        Assertions.assertNotNull(apiCase.getDeleteTime());
        // @@校验日志
        checkLog(apiTestCase.getId(), OperationLogType.DELETE);
        this.requestGet(MOVE_TO_GC + "111").andExpect(ERROR_REQUEST_MATCHER);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_DELETE, MOVE_TO_GC + apiTestCase.getId());
    }

    @Test
    @Order(5)
    public void recover() throws Exception {
        // @@请求成功
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(RECOVER + apiTestCase.getId());
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
        this.requestGet(RECOVER + "111").andExpect(ERROR_REQUEST_MATCHER);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_RECOVER, RECOVER + apiTestCase.getId());
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
        request.setStatus("Underway");
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
        MvcResult mvcResult = this.responsePost(UPDATE, request);
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
        this.requestGetWithOk("/api/case/transfer/options/" + "/" + DEFAULT_PROJECT_ID);
        ApiTransferRequest apiTransferRequest = new ApiTransferRequest();
        apiTransferRequest.setSourceId(apiTestCase.getId());
        apiTransferRequest.setProjectId(DEFAULT_PROJECT_ID);
        apiTransferRequest.setModuleId("root");
        apiTransferRequest.setLocal(true);
        String uploadFileId = doUploadTempFile(getMockMultipartFile());
        apiTransferRequest.setFileId(uploadFileId);
        apiTransferRequest.setFileName("test-api-test-case.txt");
        this.requestPost("/api/case/transfer", apiTransferRequest).andExpect(status().isOk());
        //文件不存在
        apiTransferRequest.setFileId("111");
        this.requestPost("/api/case/transfer", apiTransferRequest).andExpect(status().is5xxServerError());
        //文件已经上传
        ApiFileResourceExample apiFileResourceExample = new ApiFileResourceExample();
        apiFileResourceExample.createCriteria().andResourceIdEqualTo(apiTestCase.getId());
        List<ApiFileResource> apiFileResources = apiFileResourceMapper.selectByExample(apiFileResourceExample);
        Assertions.assertFalse(apiFileResources.isEmpty());
        apiTransferRequest.setFileId(apiFileResources.get(0).getFileId());
        apiTransferRequest.setFileName("test-api-test-case-1.txt");
        this.requestPost("/api/case/transfer", apiTransferRequest).andExpect(status().isOk());

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

    }

    @Test
    @Order(10)
    public void testExecuteList() throws Exception {
        ApiTestCase first = apiTestCaseMapper.selectByExample(new ApiTestCaseExample()).getFirst();
        List<ApiReport> reports = new ArrayList<>();
        List<ApiTestCaseRecord> records = new ArrayList<>();
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
                apiReport.setStatus(ApiReportStatus.SUCCESS.name());
            } else {
                apiReport.setStatus(ApiReportStatus.ERROR.name());
            }
            apiReport.setTriggerMode("api-trigger-mode" + i);
            apiReport.setVersionId("api-version-id" + i);
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
        MvcResult mvcResult = responsePost(EXECUTE, request);
        Pager<?> returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        request.setFilter(new HashMap<>() {{
            put("status", List.of(ApiReportStatus.SUCCESS.name()));
        }});
        mvcResult = responsePost(EXECUTE, request);
        returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        Assertions.assertTrue(((List<ApiReport>) returnPager.getList()).size() <= request.getPageSize());
        List<ExecuteReportDTO> reportDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ExecuteReportDTO.class);
        reportDTOS.forEach(apiReport -> {
            Assertions.assertEquals(apiReport.getStatus(), ApiReportStatus.SUCCESS.name());
        });
    }

    @Test
    @Order(11)
    public void page() throws Exception {
        // @@请求成功
        ApiTestCaseAddRequest request = new ApiTestCaseAddRequest();
        request.setApiDefinitionId("apiDefinitionId1");
        request.setName("testApiDefinitionId1");
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setPriority("P0");
        request.setStatus("Underway");
        request.setTags(new LinkedHashSet<>(List.of("tag1", "tag2")));
        MsHTTPElement msHttpElement = MsHTTPElementTest.getMsHttpElement();
        request.setRequest(getMsElementParam(msHttpElement));
        this.requestPost(ADD, request);
        ApiTestCasePageRequest pageRequest = new ApiTestCasePageRequest();
        pageRequest.setProjectId(DEFAULT_PROJECT_ID);
        pageRequest.setPageSize(10);
        pageRequest.setCurrent(1);
        MvcResult mvcResult = responsePost(PAGE, pageRequest);
        Pager<?> returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), pageRequest.getCurrent());
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(((List<ApiTestCaseDTO>) returnPager.getList()).size() <= pageRequest.getPageSize());

        //查询apiDefinitionId1的数据
        pageRequest.setApiDefinitionId("apiDefinitionId1");
        mvcResult = responsePost(PAGE, pageRequest);
        returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), pageRequest.getCurrent());

        List<ApiTestCaseDTO> caseDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ApiTestCaseDTO.class);
        caseDTOS.forEach(caseDTO -> Assertions.assertEquals(caseDTO.getApiDefinitionId(), "apiDefinitionId1"));

        //查询模块为moduleId1的数据
        pageRequest.setApiDefinitionId(null);
        pageRequest.setModuleIds(List.of("moduleId1"));
        mvcResult = responsePost(PAGE, pageRequest);
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
        responsePost(PAGE, pageRequest);
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
        request.setAppendTag(true);
        request.setSelectAll(true);
        request.setTags(new LinkedHashSet<>(List.of("tag1", "tag3", "tag4")));
        responsePost(BATCH_EDIT, request);
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
        request.setAppendTag(false);
        responsePost(BATCH_EDIT, request);
        apiTestCaseMapper.selectByExample(example).forEach(apiTestCase -> {
            Assertions.assertEquals(apiTestCase.getTags(), List.of("tag1"));
        });
        //标签为空  报错
        request.setTags(new LinkedHashSet<>());
        this.requestPost(BATCH_EDIT, request, ERROR_REQUEST_MATCHER);
        //ids为空的时候
        request.setTags(new LinkedHashSet<>(List.of("tag1")));
        request.setSelectAll(true);
        List<ApiTestCase> caseList1 = apiTestCaseMapper.selectByExample(example);
        //提取所有的id
        List<String> apiIdList = caseList1.stream().map(ApiTestCase::getId).toList();
        request.setSelectIds(apiIdList);
        request.setExcludeIds(apiIdList);
        responsePost(BATCH_EDIT, request);

        initCaseData();
        //优先级
        request.setTags(new LinkedHashSet<>());
        request.setSelectAll(true);
        request.setType("Priority");
        request.setModuleIds(List.of("case-moduleId"));
        request.setPriority("P3");
        request.setExcludeIds(new ArrayList<>());
        responsePost(BATCH_EDIT, request);
        //判断数据的优先级是不是P3
        example.clear();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andApiDefinitionIdEqualTo("apiDefinitionId").andDeletedEqualTo(false);
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
        responsePost(BATCH_EDIT, request);
        //状态
        request.setPriority(null);
        request.setType("Status");
        request.setStatus("Completed");
        request.setSelectAll(true);
        request.setExcludeIds(new ArrayList<>());
        responsePost(BATCH_EDIT, request);
        //判断数据的状态是不是Completed
        caseList = apiTestCaseMapper.selectByExample(example);
        caseList.forEach(apiTestCase -> Assertions.assertEquals(apiTestCase.getStatus(), "Completed"));
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
        responsePost(BATCH_EDIT, request);
        //判断数据的环境是不是environments.get(0).getId()
        caseList = apiTestCaseMapper.selectByExample(example);
        caseList.forEach(apiTestCase -> Assertions.assertEquals(apiTestCase.getEnvironmentId(), environments.get(0).getId()));
        //环境数据为空
        request.setEnvId(null);
        this.requestPost(BATCH_EDIT, request, ERROR_REQUEST_MATCHER);
        //环境不存在
        request.setEnvId("111");
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

        MvcResult mvcResult = responsePost(HISTORY, request);
        Pager<?> returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        List<OperationHistoryDTO> reportDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), OperationHistoryDTO.class);
        reportDTOS.forEach(reportDTO -> Assertions.assertEquals(reportDTO.getSourceId(), first.getId()));

        List<OperationHistoryDTO> operationHistoryDTOS = operationHistoryService.listWidthTable(request, "api_test_case");
        Assertions.assertTrue(CollectionUtils.isNotEmpty(operationHistoryDTOS));

        request = new OperationHistoryRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSourceId("111");
        request.setPageSize(10);
        request.setCurrent(1);
        responsePost(HISTORY, request);
    }

    @Test
    @Order(15)
    public void batchMoveGc() throws Exception {
        // @@请求成功
        ApiTestCaseBatchRequest request = new ApiTestCaseBatchRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSelectAll(false);
        request.setSelectIds(List.of(apiTestCase.getId()));
        request.setExcludeIds(List.of(apiTestCase.getId()));
        responsePost(BATCH_MOVE_GC, request);

        request.setSelectAll(true);
        request.setExcludeIds(new ArrayList<>());
        request.setApiDefinitionId("apiDefinitionId");
        request.setModuleIds(List.of("case-moduleId"));
        responsePost(BATCH_MOVE_GC, request);
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andApiDefinitionIdEqualTo("apiDefinitionId").andDeletedEqualTo(true);
        List<ApiTestCase> caseList = apiTestCaseMapper.selectByExample(example);
        caseList.forEach(apiTestCase -> Assertions.assertTrue(apiTestCase.getDeleted()));

        request.setSelectAll(true);
        request.setExcludeIds(new ArrayList<>());
        request.setModuleIds(List.of("case-moduleId"));
        responsePost(BATCH_MOVE_GC, request);
        //校验日志
        checkLog(apiTestCase.getId(), OperationLogType.DELETE);
        //校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_DELETE, BATCH_MOVE_GC, request);
    }

    @Test
    @Order(16)
    public void trashPage() throws Exception {
        // @@请求成功
        ApiTestCasePageRequest pageRequest = new ApiTestCasePageRequest();
        pageRequest.setProjectId(DEFAULT_PROJECT_ID);
        pageRequest.setPageSize(10);
        pageRequest.setCurrent(1);
        MvcResult mvcResult = responsePost(TRASH_PAGE, pageRequest);
        Pager<?> returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), pageRequest.getCurrent());
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(((List<ApiTestCaseDTO>) returnPager.getList()).size() <= pageRequest.getPageSize());

        //查询apiDefinitionId1的数据
        pageRequest.setApiDefinitionId("apiDefinitionId1");
        mvcResult = responsePost(TRASH_PAGE, pageRequest);
        returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), pageRequest.getCurrent());

        List<ApiTestCaseDTO> caseDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ApiTestCaseDTO.class);
        caseDTOS.forEach(caseDTO -> Assertions.assertEquals(caseDTO.getApiDefinitionId(), "apiDefinitionId1"));

        //查询模块为moduleId1的数据
        pageRequest.setApiDefinitionId(null);
        pageRequest.setModuleIds(List.of("moduleId1"));
        mvcResult = responsePost(TRASH_PAGE, pageRequest);
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
        responsePost(TRASH_PAGE, pageRequest);
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
        request.setSelectIds(List.of(apiTestCase.getId()));
        request.setExcludeIds(List.of(apiTestCase.getId()));
        responsePost(BATCH_RECOVER, request);

        ApiDefinition apiDefinition = new ApiDefinition();
        apiDefinition.setId("apiDefinitionId");
        apiDefinition.setDeleted(true);
        apiDefinitionMapper.updateByPrimaryKeySelective(apiDefinition);
        request.setSelectAll(true);
        request.setSelectIds(List.of(apiTestCase.getId()));
        request.setExcludeIds(List.of(apiTestCase.getId()));
        request.setApiDefinitionId("apiDefinitionId");
        request.setModuleIds(List.of("case-moduleId"));
        responsePost(BATCH_RECOVER, request);
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andApiDefinitionIdEqualTo("apiDefinitionId").andDeletedEqualTo(false);
        List<ApiTestCase> caseList = apiTestCaseMapper.selectByExample(example);
        caseList.forEach(apiTestCase -> Assertions.assertFalse(apiTestCase.getDeleted()));

        request.setSelectAll(true);
        request.setExcludeIds(new ArrayList<>());
        request.setModuleIds(List.of("case-moduleId"));
        responsePost(BATCH_RECOVER, request);
        //校验日志
        checkLog(apiTestCase.getId(), OperationLogType.DELETE);
        //校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_RECOVER, BATCH_RECOVER, request);
        ApiTestCaseBatchRequest gcRequest = new ApiTestCaseBatchRequest();
        gcRequest.setProjectId(DEFAULT_PROJECT_ID);
        gcRequest.setSelectAll(true);
        gcRequest.setExcludeIds(new ArrayList<>());
        gcRequest.setApiDefinitionId("apiDefinitionId");
        responsePost(BATCH_MOVE_GC, gcRequest);
        ApiTestCaseExample example1 = new ApiTestCaseExample();
        example1.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andApiDefinitionIdEqualTo("apiDefinitionId").andDeletedEqualTo(true);
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
        request.setSelectIds(List.of(apiTestCase.getId()));
        request.setExcludeIds(List.of(apiTestCase.getId()));
        responsePost(BATCH_DELETE, request);
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSelectAll(true);
        request.setApiDefinitionId("apiDefinitionId");
        responsePost(BATCH_DELETE, request);
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andApiDefinitionIdEqualTo("apiDefinitionId");
        //数据为空
        Assertions.assertEquals(0, apiTestCaseMapper.selectByExample(example).size());

        //校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_DELETE, BATCH_DELETE, request);
    }


}