package io.metersphere.api.controller;

import io.metersphere.api.constants.ApiConstants;
import io.metersphere.api.controller.result.ApiResultCode;
import io.metersphere.api.domain.ApiDebug;
import io.metersphere.api.domain.ApiDebugBlob;
import io.metersphere.api.domain.ApiFileResource;
import io.metersphere.api.domain.ApiFileResourceExample;
import io.metersphere.api.dto.ApiFile;
import io.metersphere.api.dto.assertion.MsAssertionConfig;
import io.metersphere.api.dto.debug.*;
import io.metersphere.api.dto.definition.ResponseBinaryBody;
import io.metersphere.api.dto.definition.ResponseBody;
import io.metersphere.api.dto.request.ApiEditPosRequest;
import io.metersphere.api.dto.request.ApiTransferRequest;
import io.metersphere.api.dto.request.MsCommonElement;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.RestParam;
import io.metersphere.project.dto.environment.auth.BasicAuth;
import io.metersphere.project.dto.environment.auth.DigestAuth;
import io.metersphere.project.dto.environment.auth.HTTPAuthConfig;
import io.metersphere.api.dto.request.http.body.*;
import io.metersphere.api.mapper.ApiDebugBlobMapper;
import io.metersphere.api.mapper.ApiDebugMapper;
import io.metersphere.api.mapper.ApiFileResourceMapper;
import io.metersphere.api.parser.ImportParserFactory;
import io.metersphere.api.parser.TestElementParserFactory;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.api.service.BaseFileManagementTestService;
import io.metersphere.api.service.BaseResourcePoolTestService;
import io.metersphere.api.service.debug.ApiDebugService;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.KeyValueEnableParam;
import io.metersphere.project.api.KeyValueParam;
import io.metersphere.project.api.processor.ScriptProcessor;
import io.metersphere.project.constants.ScriptLanguageType;
import io.metersphere.project.domain.CustomFunction;
import io.metersphere.project.domain.ProjectTestResourcePool;
import io.metersphere.project.domain.ProjectTestResourcePoolExample;
import io.metersphere.project.dto.CommonScriptInfo;
import io.metersphere.project.dto.customfunction.request.CustomFunctionRequest;
import io.metersphere.project.dto.filemanagement.FileInfo;
import io.metersphere.project.mapper.ProjectTestResourcePoolMapper;
import io.metersphere.project.service.CustomFunctionService;
import io.metersphere.project.service.FileAssociationService;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.TestResourcePool;
import io.metersphere.system.domain.TestResourcePoolExample;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.TestResourcePoolMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static io.metersphere.api.controller.result.ApiResultCode.API_DEBUG_EXIST;
import static io.metersphere.system.controller.handler.result.MsHttpResultCode.NOT_FOUND;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author jianxing
 * @date : 2023-11-7
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiDebugControllerTests extends BaseTest {
    private static final String BASE_PATH = "/api/debug/";
    protected static final String DEFAULT_LIST = "list/{0}";
    protected static final String UPLOAD_TEMP_FILE = "upload/temp/file";
    protected static final String DEBUG = "debug";
    public static final String TRANSFER_OPTION = "transfer/options";
    public static final String TRANSFER = "transfer";

    @Resource
    private ApiDebugMapper apiDebugMapper;
    @Resource
    private ApiDebugBlobMapper apiDebugBlobMapper;
    @Resource
    private ApiFileResourceService apiFileResourceService;
    @Resource
    private BaseResourcePoolTestService baseResourcePoolTestService;
    @Resource
    private BaseFileManagementTestService baseFileManagementTestService;
    @Resource
    private ApiCommonService apiCommonService;
    @Resource
    private ProjectTestResourcePoolMapper projectTestResourcePoolMapper;
    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;
    @Resource
    private CustomFunctionService customFunctionService;
    @Resource
    private ApiFileResourceMapper apiFileResourceMapper;
    @Resource
    private ApiDebugService apiDebugService;
    private static ApiDebug addApiDebug;
    private static ApiDebug anotherAddApiDebug;
    private static String fileMetadataId;
    private static String uploadFileId;

    @Override
    protected String getBasePath() {
        return BASE_PATH;
    }

    @Test
    @Order(0)
    public void listEmpty() throws Exception {
        // @@校验没有数据的情况
        this.requestGetWithOk(DEFAULT_LIST, ApiConstants.HTTP_PROTOCOL);
        // 准备数据，上传文件管理文件
        MockMultipartFile file = new MockMultipartFile("file", IDGenerator.nextStr() + "_file_upload.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, "aa".getBytes());
        fileMetadataId = baseFileManagementTestService.upload(file);
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

        requestUploadPermissionTest(PermissionConstants.PROJECT_API_DEBUG_ADD, UPLOAD_TEMP_FILE, file);
        requestUploadPermissionTest(PermissionConstants.PROJECT_API_DEBUG_UPDATE, UPLOAD_TEMP_FILE, file);
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

    private static MockMultipartFile getMockMultipartFile(String fileName) {
        return new MockMultipartFile(
                "file",
                fileName,
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "Hello, World!".getBytes()
        );
    }

    @Test
    @Order(2)
    public void add() throws Exception {
        // @@请求成功
        ApiDebugAddRequest request = new ApiDebugAddRequest();
        request.setPath("http://test.com");
        request.setMethod("GET");
        request.setName("test");
        request.setProtocol(ApiConstants.HTTP_PROTOCOL);
        request.setModuleId("default");
        request.setProjectId(DEFAULT_PROJECT_ID);
        MsHTTPElement msHttpElement = MsHTTPElementTest.getMsHttpElement();
        msHttpElement.setBody(addBodyLinkFile(msHttpElement.getBody(), fileMetadataId));
        request.setRequest(getMsElementParam(msHttpElement));

        uploadFileId = doUploadTempFile(getMockMultipartFile());
        request.setUploadFileIds(List.of(uploadFileId));

        request.setUploadFileIds(List.of(uploadFileId));
        request.setLinkFileIds(List.of(fileMetadataId));

        MvcResult mvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);
        // 校验请求成功数据
        ApiDebug resultData = getResultData(mvcResult, ApiDebug.class);
        this.addApiDebug = assertUpdateApiDebug(request, msHttpElement, resultData.getId());
        assertUploadFile(resultData.getId(), List.of(uploadFileId));
        assertLinkFile(resultData.getId(), List.of(fileMetadataId));

        // 校验不同用户的重名校验
        apiDebugService.add(request, "userA");

        // 再插入一条数据，便于修改时重名校验
        request.setName("test1");
        request.setUploadFileIds(null);
        request.setLinkFileIds(null);
        request.setPath("");
        mvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);
        resultData = getResultData(mvcResult, ApiDebug.class);
        this.anotherAddApiDebug = assertUpdateApiDebug(request, msHttpElement, resultData.getId());
        assertUploadFile(resultData.getId(), List.of());
        assertLinkFile(resultData.getId(), List.of());

        // 测试关联的文件更新
        testHandleFileAssociationUpgrade();

        // @@重名校验异常
        assertErrorCode(this.requestPost(DEFAULT_ADD, request), API_DEBUG_EXIST);
        // 校验项目是否存在
        request.setProjectId("111");
        assertErrorCode(this.requestPost(DEFAULT_ADD, request), NOT_FOUND);

        // @@校验日志
        checkLog(this.addApiDebug.getId(), OperationLogType.ADD);
        // @@校验权限
        request.setProjectId(DEFAULT_PROJECT_ID);

        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEBUG_ADD, DEFAULT_ADD, request);
    }

    @Test
    @Order(3)
    public void update() throws Exception {
        // @@请求成功
        ApiDebugUpdateRequest request = new ApiDebugUpdateRequest();
        request.setId(addApiDebug.getId());
        request.setPath("http://tesat.com");
        request.setName("test1");
        request.setMethod("POST");
        request.setModuleId("default1");
        MsHTTPElement msHttpElement = MsHTTPElementTest.getMsHttpElement();
        msHttpElement.setName("test1");
        request.setRequest(getMsElementParam(msHttpElement));

        // 不带文件的更新
        request.setUnLinkFileIds(List.of(fileMetadataId));
        request.setDeleteFileIds(List.of(uploadFileId));
        this.requestPostWithOk(DEFAULT_UPDATE, request);

        // 校验请求成功数据
        assertUpdateApiDebug(request, msHttpElement, request.getId());
        assertUploadFile(addApiDebug.getId(), List.of());
        assertLinkFile(addApiDebug.getId(), List.of());

        // 带文件的更新
        String fileId = doUploadTempFile(getMockMultipartFile());
        request.setUploadFileIds(List.of(fileId));
        request.setLinkFileIds(List.of(fileMetadataId));
        request.setDeleteFileIds(null);
        request.setUnLinkFileIds(null);
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        // 校验 UploadFileIds 参数重复关联
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        // 校验请求成功数据
        assertUpdateApiDebug(request, msHttpElement, request.getId());
        assertUploadFile(addApiDebug.getId(), List.of(fileId));
        assertLinkFile(addApiDebug.getId(), List.of(fileMetadataId));

        // 删除了上一次上传的文件，重新上传一个文件
        request.setDeleteFileIds(List.of(fileId));
        String newFileId1 = doUploadTempFile(getMockMultipartFile());
        request.setUploadFileIds(List.of(newFileId1));
        request.setUnLinkFileIds(List.of(fileMetadataId));
        request.setLinkFileIds(List.of(fileMetadataId));
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        assertUpdateApiDebug(request, msHttpElement, request.getId());
        assertUploadFile(addApiDebug.getId(), List.of(newFileId1));
        assertLinkFile(addApiDebug.getId(), List.of(fileMetadataId));

        // 已有一个文件，再上传一个文件
        String newFileId2 = doUploadTempFile(getMockMultipartFile());
        request.setUploadFileIds(List.of(newFileId2));
        msHttpElement.setBody(addBodyLinkFile(msHttpElement.getBody(), fileMetadataId));
        request.setRequest(getMsElementParam(msHttpElement));
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        // 校验请求成功数据
        assertUpdateApiDebug(request, msHttpElement, request.getId());
        assertUploadFile(addApiDebug.getId(), List.of(newFileId1, newFileId2));
        assertLinkFile(addApiDebug.getId(), List.of(fileMetadataId));

        // @@重名校验异常
        request.setModuleId("default");
        assertErrorCode(this.requestPost(DEFAULT_UPDATE, request), API_DEBUG_EXIST);

        // @@校验日志
        checkLog(request.getId(), OperationLogType.UPDATE);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEBUG_UPDATE, DEFAULT_UPDATE, request);
    }

    /**
     * 校验更新数据
     *
     * @param request
     * @param msHttpElement
     * @param id
     * @return
     * @throws Exception
     */
    private ApiDebug assertUpdateApiDebug(Object request, MsHTTPElement msHttpElement, String id) {
        ApiDebug apiDebug = apiDebugMapper.selectByPrimaryKey(id);
        ApiDebugBlob apiDebugBlob = apiDebugBlobMapper.selectByPrimaryKey(id);
        ApiDebug copyApiDebug = BeanUtils.copyBean(new ApiDebug(), apiDebug);
        copyApiDebug = BeanUtils.copyBean(copyApiDebug, request);
        Assertions.assertEquals(apiDebug, copyApiDebug);
        Assertions.assertEquals(msHttpElement, ApiDataUtils.parseObject(new String(apiDebugBlob.getRequest()), AbstractMsTestElement.class));
        return apiDebug;
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

            String apiDebugDir = DefaultRepositoryDir.getApiDebugDir(DEFAULT_PROJECT_ID, id);
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
    @Order(4)
    public void list() throws Exception {
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOk(DEFAULT_LIST, ApiConstants.HTTP_PROTOCOL)
                .andReturn();
        // 校验数据是否正确
        List<ApiDebugSimpleDTO> apiDebugList = getResultDataArray(mvcResult, ApiDebugSimpleDTO.class);
        Assertions.assertEquals(apiDebugList.size(), 2);
        Assertions.assertEquals(apiDebugList.get(0), BeanUtils.copyBean(new ApiDebugSimpleDTO(),
                apiDebugMapper.selectByPrimaryKey(addApiDebug.getId())));
        Assertions.assertEquals(apiDebugList.get(1),
                BeanUtils.copyBean(new ApiDebugSimpleDTO(), apiDebugMapper.selectByPrimaryKey(anotherAddApiDebug.getId())));

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEBUG_READ, DEFAULT_LIST, ApiConstants.HTTP_PROTOCOL);
    }


    @Test
    @Order(5)
    public void get() throws Exception {
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOk(DEFAULT_GET, addApiDebug.getId())
                .andReturn();
        ApiDebugDTO apiDebugDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResult).get("data")), ApiDebugDTO.class);
        // 校验数据是否正确
        ApiDebugDTO copyApiDebugDTO = BeanUtils.copyBean(new ApiDebugDTO(), apiDebugMapper.selectByPrimaryKey(addApiDebug.getId()));
        ApiDebugBlob apiDebugBlob = apiDebugBlobMapper.selectByPrimaryKey(addApiDebug.getId());
        AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(new String(apiDebugBlob.getRequest()), AbstractMsTestElement.class);
        apiCommonService.setLinkFileInfo(addApiDebug.getId(), msTestElement);
        copyApiDebugDTO.setRequest(msTestElement);
        Assertions.assertEquals(apiDebugDTO, copyApiDebugDTO);

        // 删除关联文件
        baseFileManagementTestService.deleteFile(fileMetadataId);
        this.requestGetWithOk(DEFAULT_GET, addApiDebug.getId());
        // 重新上传文件,执行时要使用
        fileMetadataId = baseFileManagementTestService.upload(getMockMultipartFile());

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEBUG_READ, DEFAULT_GET, apiDebugDTO.getId());
    }

    @Test
    @Order(6)
    public void move() throws Exception {
        ApiEditPosRequest request = new ApiEditPosRequest();
        request.setTargetId(addApiDebug.getId());
        request.setMoveId(addApiDebug.getId());
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setModuleId("root");
        request.setMoveMode("AFTER");
        requestPost("edit/pos", request).andExpect(status().isOk());
        // @@请求成功
        request.setTargetId(anotherAddApiDebug.getId());
        this.requestPostWithOk("edit/pos", request);
        // 校验请求成功数据
        ApiDebug apiDebug = apiDebugMapper.selectByPrimaryKey(addApiDebug.getId());
        Assertions.assertEquals(apiDebug.getModuleId(), "root");
        request.setModuleId("def");
        requestPost("edit/pos", request).andExpect(status().is5xxServerError());
        request.setModuleId("root");
        apiDebugService.refreshPos("admin");
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEBUG_UPDATE, "edit/pos", request);
    }

    @Test
    @Order(7)
    public void debug() throws Exception {
        ApiDebugRunRequest request = new ApiDebugRunRequest();
        request.setId(addApiDebug.getId());
        MsHTTPElement msHTTPElement = new MsHTTPElement();
        msHTTPElement.setPath("/test");
        msHTTPElement.setMethod("GET");
        request.setRequest(JSON.parseObject(ApiDataUtils.toJSONString(msHTTPElement)));
        request.setReportId(IDGenerator.nextStr());
        request.setProjectId(DEFAULT_PROJECT_ID);

        ProjectTestResourcePoolExample projectTestResourcePoolExample = new ProjectTestResourcePoolExample();
        projectTestResourcePoolExample.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID);
        projectTestResourcePoolMapper.deleteByExample(projectTestResourcePoolExample);
        // @校验组织没有资源池权限异常
        assertRun(this.requestPostAndReturn(DEBUG, request));
        TestResourcePool resourcePool = baseResourcePoolTestService.insertResourcePool();
        baseResourcePoolTestService.insertResourcePoolOrg(resourcePool);
        // @校验项目没有资源池权限异常
        assertRun(this.requestPostAndReturn(DEBUG, request));

        TestResourcePoolExample example = new TestResourcePoolExample();
        example.createCriteria().andNameEqualTo("默认资源池");
        List<TestResourcePool> testResourcePools = testResourcePoolMapper.selectByExample(example);
        Assertions.assertFalse(testResourcePools.isEmpty());
        ProjectTestResourcePool projectTestResourcePool = new ProjectTestResourcePool();
        projectTestResourcePool.setProjectId(DEFAULT_PROJECT_ID);
        projectTestResourcePool.setTestResourcePoolId(testResourcePools.get(0).getId());
        projectTestResourcePoolMapper.insert(projectTestResourcePool);
        this.requestPost(DEBUG, request);
        projectTestResourcePoolMapper.deleteByExample(projectTestResourcePoolExample);

        baseResourcePoolTestService.insertResourcePoolProject(resourcePool);
        baseResourcePoolTestService.insertProjectApplication(resourcePool);
        // @校验资源池调用失败
        assertRun(this.requestPostAndReturn(DEBUG, request));

        mockPost("/api/debug", "");
        msHTTPElement.setPath("/test/{rest1}/aa");
        msHTTPElement.setRest(getRestParams());
        msHTTPElement.getOtherConfig().setAutoRedirects(true);
        msHTTPElement.getOtherConfig().setFollowRedirects(false);
        msHTTPElement.getOtherConfig().setResponseTimeout(7000L);
        DigestAuth digestAuth = new DigestAuth();
        digestAuth.setUserName("aa");
        digestAuth.setPassword("bb");
        msHTTPElement.getAuthConfig().setAuthType(HTTPAuthConfig.HTTPAuthType.DIGEST.name());
        msHTTPElement.getAuthConfig().setDigestAuth(digestAuth);
        request.setRequest(getMsElementParam(msHTTPElement));
        // @@请求成功
        this.requestPostWithOk(DEBUG, request);

        // 测试断言
        MsAssertionConfig msAssertionConfig = new MsAssertionConfig();
        msAssertionConfig.setEnableGlobal(false);
        msAssertionConfig.setAssertions(MsHTTPElementTest.getGeneralAssertions());
        MsCommonElement msCommonElement = new MsCommonElement();
        msCommonElement.setAssertionConfig(msAssertionConfig);
        LinkedList linkedList = new LinkedList();
        linkedList.add(msCommonElement);
        msHTTPElement = MsHTTPElementTest.getMsHttpElement();
        msHTTPElement.setChildren(linkedList);
        msHTTPElement.setEnable(true);
        BasicAuth basicAuth = new BasicAuth();
        basicAuth.setUserName("a");
        basicAuth.setPassword("b");
        msHTTPElement.getAuthConfig().setAuthType(HTTPAuthConfig.HTTPAuthType.BASIC.name());
        msHTTPElement.getAuthConfig().setBasicAuth(basicAuth);
        request.setRequest(getMsElementParam(msHTTPElement));
        this.requestPostWithOk(DEBUG, request);

        msAssertionConfig = new MsAssertionConfig();
        msAssertionConfig.setEnableGlobal(false);
        msAssertionConfig.setAssertions(MsHTTPElementTest.getGeneralXmlAssertions());
        msCommonElement = new MsCommonElement();
        msCommonElement.setAssertionConfig(msAssertionConfig);

        // 测试公共脚本
        ScriptProcessor scriptProcessor = new ScriptProcessor();
        scriptProcessor.setEnable(true);
        scriptProcessor.setName("test");
        scriptProcessor.setScriptLanguage(ScriptLanguageType.JAVASCRIPT.name());
        CustomFunction customFunction = addCustomFunction();
        scriptProcessor.setCommonScriptInfo(new CommonScriptInfo());
        scriptProcessor.getCommonScriptInfo().setId(customFunction.getId());
        scriptProcessor.setEnableCommonScript(true);
        KeyValueParam keyValueParam = new KeyValueParam();
        keyValueParam.setKey("a");
        keyValueParam.setValue("bb");
        scriptProcessor.getCommonScriptInfo().setParams(List.of(keyValueParam));
        msCommonElement.getPostProcessorConfig().getProcessors().add(scriptProcessor);

        linkedList = new LinkedList();
        linkedList.add(msCommonElement);
        msHTTPElement = MsHTTPElementTest.getMsHttpElement();
        msHTTPElement.setChildren(linkedList);
        msHTTPElement.setEnable(true);
        request.setRequest(getMsElementParam(msHTTPElement));
        this.requestPostWithOk(DEBUG, request);

        // 测试本地调试
        request.setFrontendDebug(true);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(DEBUG, request);
        TaskRequestDTO taskRequestDTO = getResultData(mvcResult, TaskRequestDTO.class);
        Assertions.assertEquals(taskRequestDTO.getTaskItem().getReportId(), request.getReportId());

        // 测试请求体
        MockMultipartFile file = getMockMultipartFile();
        String fileId = doUploadTempFile(file);
        request.setUploadFileIds(List.of(fileId, fileMetadataId));
        request.setLinkFileIds(List.of(fileId, fileMetadataId));
        Body generalBody = MsHTTPElementTest.getGeneralBody();
        msHTTPElement = MsHTTPElementTest.getMsHttpElement();
        msHTTPElement.setBody(generalBody);
        msHTTPElement.setEnable(true);
        testBodyParse(request, msHTTPElement, generalBody);

        // 增加覆盖率
        msHTTPElement.setMethod("GET");
        request.setRequest(getMsElementParam(msHTTPElement));
        this.requestPostWithOk(DEBUG, request);
        msHTTPElement.setEnable(false);
        request.setRequest(getMsElementParam(msHTTPElement));
        this.requestPostWithOk(DEBUG, request);

        // 增加覆盖率
        new TestElementParserFactory();
        new ImportParserFactory();

        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEBUG_EXECUTE, DEBUG, request);
    }

    public static void assertRun(MvcResult mvcResult) throws UnsupportedEncodingException {
        Map resultData = JSON.parseMap(mvcResult.getResponse().getContentAsString());
        Integer code = (Integer) resultData.get("code");
        if (code != ApiResultCode.RESOURCE_POOL_EXECUTE_ERROR.getCode() && code != ApiResultCode.EXECUTE_RESOURCE_POOL_NOT_CONFIG.getCode()) {
            Assertions.assertTrue(false);
        }
    }

    private CustomFunction addCustomFunction() {
        CustomFunctionRequest request = new CustomFunctionRequest();
        request.setScript("aaa");
        KeyValueEnableParam keyValueEnableParam = new KeyValueEnableParam();
        keyValueEnableParam.setKey("a");
        keyValueEnableParam.setValue("b");
        request.setParams(JSON.toJSONString(keyValueEnableParam));
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setType(ScriptLanguageType.BEANSHELL.name());
        request.setName(IDGenerator.nextStr());
        return customFunctionService.add(request, "admin");
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
        baseFileManagementTestService.upgrade(fileMetadataId, addApiDebug.getId());
        // 校验文件是否替换
        Assertions.assertEquals(originApiFiles.size(), getApiFiles(newFileId).size());
        fileMetadataId = newFileId;
    }

    private List<ApiFile> getApiFiles(String fileId) {
        ApiDebugBlob apiDebugBlob = apiDebugBlobMapper.selectByPrimaryKey(addApiDebug.getId());
        AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(new String(apiDebugBlob.getRequest()), AbstractMsTestElement.class);
        return apiCommonService.getApiFilesByFileId(fileId, msTestElement);
    }

    private List<RestParam> getRestParams() {
        RestParam restParam1 = new RestParam();
        restParam1.setKey("rest1");
        restParam1.setValue("value");
        RestParam restParam2 = new RestParam();
        restParam2.setKey("rest2");
        restParam2.setValue("value2");
        restParam2.setEncode(true);
        RestParam restParam3 = new RestParam();
        restParam3.setKey("rest3");
        restParam3.setEnable(false);
        return List.of(restParam1, restParam2, restParam3);
    }

    private void testBodyParse(ApiDebugRunRequest request, MsHTTPElement msHTTPElement, Body generalBody) throws Exception {
        // 测试 FORM_DATA
        generalBody.setBodyType(Body.BodyType.FORM_DATA.name());
        request.setRequest(getMsElementParam(msHTTPElement));
        this.requestPostWithOk(DEBUG, request);

        // 测试 WWW_FORM
        generalBody.setBodyType(Body.BodyType.WWW_FORM.name());
        request.setRequest(getMsElementParam(msHTTPElement));
        this.requestPostWithOk(DEBUG, request);

        // 测试 BINARY
        generalBody.setBodyType(Body.BodyType.BINARY.name());
        request.setRequest(getMsElementParam(msHTTPElement));
        this.requestPostWithOk(DEBUG, request);

        // 测试 JSON
        generalBody.setBodyType(Body.BodyType.JSON.name());
        request.setRequest(getMsElementParam(msHTTPElement));
        this.requestPostWithOk(DEBUG, request);

        // 测试 XML
        generalBody.setBodyType(Body.BodyType.XML.name());
        request.setRequest(getMsElementParam(msHTTPElement));
        this.requestPostWithOk(DEBUG, request);

        // 测试 RAW
        generalBody.setBodyType(Body.BodyType.RAW.name());
        request.setRequest(getMsElementParam(msHTTPElement));
        this.requestPostWithOk(DEBUG, request);
    }

    private Object getMsElementParam(MsHTTPElement msHTTPElement) {
        return JSON.parseObject(ApiDataUtils.toJSONString(msHTTPElement));
    }

    public static Body addBodyLinkFile(Body body, String fileMetadataId) {
        if (body == null) {
            body = MsHTTPElementTest.getGeneralBody();
        }
        if (body.getFormDataBody() == null) {
            body.setFormDataBody(new FormDataBody());
            body.getFormDataBody().setFormValues(new ArrayList<>(1));
        }
        List<FormDataKV> formValues = body.getFormDataBody().getFormValues();
        FormDataKV formDataKV = new FormDataKV();
        formDataKV.setKey("a");
        formDataKV.setParamType(BodyParamType.FILE.getValue());
        ApiFile apiFile = new ApiFile();
        apiFile.setLocal(false);
        apiFile.setFileId(fileMetadataId);
        apiFile.setFileName("test");
        formDataKV.setFiles(List.of(apiFile));
        formValues.add(formDataKV);
        if (body.getBinaryBody() == null) {
            body.setBinaryBody(new BinaryBody());
        }
        body.getBinaryBody().setFile(apiFile);
        return body;
    }

    public static ResponseBody addBodyLinkFile(ResponseBody body, String fileMetadataId) {
        if (body == null) {
            body = new ResponseBody();
        }
        ApiFile apiFile = new ApiFile();
        apiFile.setLocal(false);
        apiFile.setFileId(fileMetadataId);
        apiFile.setFileName("test");
        if (body.getBinaryBody() == null) {
            body.setBinaryBody(new ResponseBinaryBody());
        }
        body.getBinaryBody().setFile(apiFile);
        return body;
    }

    @Test
    @Order(8)
    void testTransfer() throws Exception {
        this.requestGetWithOk(TRANSFER_OPTION + "/" + DEFAULT_PROJECT_ID);
        ApiTransferRequest request = new ApiTransferRequest();
        request.setSourceId(addApiDebug.getId());
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setModuleId("root");
        request.setLocal(true);
        uploadFileId = doUploadTempFile(getMockMultipartFile("test-debug-file.txt"));
        request.setFileId(uploadFileId);
        request.setFileName("test-debug-file");
        request.setOriginalName("test-debug-file.txt");
        this.requestPost(TRANSFER, request).andExpect(status().isOk());
        //文件不存在
        request.setFileId("111");
        request.setFileName("test-debug-file");
        request.setOriginalName("test-debug-file.txt");
        this.requestPost(TRANSFER, request).andExpect(status().is5xxServerError());
        //文件已经上传
        ApiDebugAddRequest addRequest = new ApiDebugAddRequest();
        addRequest.setPath("http://test.com");
        addRequest.setMethod("GET");
        addRequest.setName("test-add-file");
        addRequest.setProtocol(ApiConstants.HTTP_PROTOCOL);
        addRequest.setModuleId("default");
        addRequest.setProjectId(DEFAULT_PROJECT_ID);
        MsHTTPElement msHttpElement = MsHTTPElementTest.getMsHttpElement();
        msHttpElement.setBody(addBodyLinkFile(msHttpElement.getBody(), fileMetadataId));
        addRequest.setRequest(getMsElementParam(msHttpElement));
        uploadFileId = doUploadTempFile(getMockMultipartFile("test-debug-file1.txt"));
        addRequest.setUploadFileIds(List.of(uploadFileId));

        MvcResult mvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, addRequest);
        ApiDebug resultData = getResultData(mvcResult, ApiDebug.class);
        ApiFileResourceExample apiFileResourceExample = new ApiFileResourceExample();
        apiFileResourceExample.createCriteria().andResourceIdEqualTo(resultData.getId());
        List<ApiFileResource> apiFileResources = apiFileResourceMapper.selectByExample(apiFileResourceExample);
        Assertions.assertFalse(apiFileResources.isEmpty());
        request = new ApiTransferRequest();
        request.setSourceId(resultData.getId());
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setModuleId("root");
        request.setFileName("test-debug-file1");
        request.setOriginalName("test-debug-file1.txt");
        request.setFileId(apiFileResources.get(0).getFileId());
        this.requestPost(TRANSFER, request).andExpect(status().isOk());
    }

    @Test
    @Order(17)
    public void delete() throws Exception {
        // @@请求成功
        this.requestGetWithOk(DEFAULT_DELETE, addApiDebug.getId());
        // 校验请求成功数据
        Assertions.assertNull(apiDebugMapper.selectByPrimaryKey(addApiDebug.getId()));
        Assertions.assertNull(apiDebugBlobMapper.selectByPrimaryKey(addApiDebug.getId()));
        List<ApiFileResource> apiFileResources = apiFileResourceService.getByResourceId(addApiDebug.getId());
        Assertions.assertTrue(CollectionUtils.isEmpty(apiFileResources));
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFolder(DefaultRepositoryDir.getApiDebugDir(addApiDebug.getProjectId(), addApiDebug.getId()));
        Assertions.assertTrue(CollectionUtils.isEmpty(FileCenter.getDefaultRepository().getFolderFileNames(fileRequest)));
        // @@校验日志
        checkLog(addApiDebug.getId(), OperationLogType.DELETE);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEBUG_DELETE, DEFAULT_DELETE, addApiDebug.getId());
    }

}