package io.metersphere.api.controller;

import io.metersphere.api.controller.result.ApiResultCode;
import io.metersphere.api.domain.ApiDebug;
import io.metersphere.api.domain.ApiDebugBlob;
import io.metersphere.api.domain.ApiFileResource;
import io.metersphere.api.dto.debug.*;
import io.metersphere.api.dto.request.MsCommonElement;
import io.metersphere.api.dto.request.MsScenario;
import io.metersphere.api.dto.request.assertion.MsAssertionConfig;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.body.Body;
import io.metersphere.api.mapper.ApiDebugBlobMapper;
import io.metersphere.api.mapper.ApiDebugMapper;
import io.metersphere.api.parser.ImportParserFactory;
import io.metersphere.api.parser.TestElementParserFactory;
import io.metersphere.api.parser.jmeter.MsScenarioConverter;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.api.service.BaseResourcePoolTestService;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.dto.filemanagement.FileInfo;
import io.metersphere.project.dto.filemanagement.request.FileUploadRequest;
import io.metersphere.project.service.FileAssociationService;
import io.metersphere.project.service.FileMetadataService;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.TestResourcePool;
import io.metersphere.system.log.constants.OperationLogType;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.jorphan.collections.ListedHashTree;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.util.LinkedList;
import java.util.List;

import static io.metersphere.api.controller.result.ApiResultCode.API_DEBUG_EXIST;
import static io.metersphere.system.controller.handler.result.MsHttpResultCode.NOT_FOUND;

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
    protected static final String HTTP_PROTOCOL = "HTTP";

    @Resource
    private ApiDebugMapper apiDebugMapper;
    @Resource
    private ApiDebugBlobMapper apiDebugBlobMapper;
    @Resource
    private ApiFileResourceService apiFileResourceService;
    @Resource
    private FileMetadataService fileMetadataService;
    @Resource
    private BaseResourcePoolTestService baseResourcePoolTestService;
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
        this.requestGetWithOk(DEFAULT_LIST, HTTP_PROTOCOL);
        // 准备数据，上传文件管理文件
        uploadFileMetadata();
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

    @Test
    @Order(2)
    public void add() throws Exception {
        // @@请求成功
        ApiDebugAddRequest request = new ApiDebugAddRequest();
        request.setPath("http://test.com");
        request.setMethod("GET");
        request.setName("test");
        request.setProtocol(HTTP_PROTOCOL);
        request.setModuleId("default");
        request.setProjectId(DEFAULT_PROJECT_ID);
        MsHTTPElement msHttpElement = MsHTTPElementTest.getMsHttpElement();
        request.setRequest(ApiDataUtils.toJSONString(msHttpElement));

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

        // 再插入一条数据，便于修改时重名校验
        request.setName("test1");
        request.setUploadFileIds(null);
        request.setLinkFileIds(null);
        mvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);
        resultData = getResultData(mvcResult, ApiDebug.class);
        this.anotherAddApiDebug = assertUpdateApiDebug(request, msHttpElement, resultData.getId());
        assertUploadFile(resultData.getId(), List.of());
        assertLinkFile(resultData.getId(), List.of());

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

    /**
     * 文件管理插入一条数据
     * 便于测试关联文件
     */
    private void uploadFileMetadata() throws Exception {
        FileUploadRequest fileUploadRequest = new FileUploadRequest();
        fileUploadRequest.setProjectId(DEFAULT_PROJECT_ID);
        //导入正常文件
        MockMultipartFile file = new MockMultipartFile("file", "file_upload.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, "aa".getBytes());
        fileMetadataId = fileMetadataService.upload(fileUploadRequest, "admin", file);
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
        request.setRequest(ApiDataUtils.toJSONString(msHttpElement));

        // 不带文件的更新
        request.setUnLinkRefIds(List.of(fileMetadataId));
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
        request.setUnLinkRefIds(null);
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        // 校验请求成功数据
        assertUpdateApiDebug(request, msHttpElement, request.getId());
        assertUploadFile(addApiDebug.getId(), List.of(fileId));
        assertLinkFile(addApiDebug.getId(), List.of(fileMetadataId));

        // 删除了上一次上传的文件，重新上传一个文件
        request.setDeleteFileIds(List.of(fileId));
        String newFileId1 = doUploadTempFile(getMockMultipartFile());
        request.setUploadFileIds(List.of(newFileId1));
        request.setUnLinkRefIds(List.of(fileMetadataId));
        request.setLinkFileIds(List.of(fileMetadataId));
        this.requestPostWithOk(DEFAULT_UPDATE, request);
        assertUpdateApiDebug(request, msHttpElement, request.getId());
        assertUploadFile(addApiDebug.getId(), List.of(newFileId1));
        assertLinkFile(addApiDebug.getId(), List.of(fileMetadataId));

        // 已有一个文件，再上传一个文件
        String newFileId2 = doUploadTempFile(getMockMultipartFile());
        request.setUploadFileIds(List.of(newFileId2));
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
        ApiDataUtils.setResolver(MsHTTPElement.class);
        Assertions.assertEquals(msHttpElement, ApiDataUtils.parseObject(new String(apiDebugBlob.getRequest()), AbstractMsTestElement.class));
        return apiDebug;
    }

    /**
     * 校验上传的文件
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
        MvcResult mvcResult = this.requestGetWithOk(DEFAULT_LIST, HTTP_PROTOCOL)
                .andReturn();
        // 校验数据是否正确
        List<ApiDebugSimpleDTO> apiDebugList = getResultDataArray(mvcResult, ApiDebugSimpleDTO.class);
        Assertions.assertEquals(apiDebugList.size(), 2);
        Assertions.assertEquals(apiDebugList.get(0), BeanUtils.copyBean(new ApiDebugSimpleDTO(),
                apiDebugMapper.selectByPrimaryKey(addApiDebug.getId())));
        Assertions.assertEquals(apiDebugList.get(1),
                BeanUtils.copyBean(new ApiDebugSimpleDTO(), apiDebugMapper.selectByPrimaryKey(anotherAddApiDebug.getId())));

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEBUG_READ, DEFAULT_LIST, HTTP_PROTOCOL);
    }


    @Test
    @Order(5)
    public void get() throws Exception {
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOk(DEFAULT_GET, addApiDebug.getId())
                .andReturn();
        ApiDataUtils.setResolver(MsHTTPElement.class);
        ApiDebugDTO apiDebugDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResult).get("data")), ApiDebugDTO.class);
        // 校验数据是否正确
        ApiDebugDTO copyApiDebugDTO = BeanUtils.copyBean(new ApiDebugDTO(), apiDebugMapper.selectByPrimaryKey(addApiDebug.getId()));
        ApiDebugBlob apiDebugBlob = apiDebugBlobMapper.selectByPrimaryKey(addApiDebug.getId());
        copyApiDebugDTO.setRequest(ApiDataUtils.parseObject(new String(apiDebugBlob.getRequest()), AbstractMsTestElement.class));
        Assertions.assertEquals(apiDebugDTO, copyApiDebugDTO);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEBUG_READ, DEFAULT_GET, apiDebugDTO.getId());
    }

    @Test
    @Order(6)
    public void debug() throws Exception {
        ApiDebugRunRequest request = new ApiDebugRunRequest();
        request.setId(addApiDebug.getId());
        MsHTTPElement msHTTPElement = new MsHTTPElement();
        msHTTPElement.setPath("/test");
        msHTTPElement.setMethod("GET");
        request.setRequest(ApiDataUtils.toJSONString(msHTTPElement));

        // @校验组织没有资源池权限异常
        assertErrorCode(this.requestPost(DEBUG, request), ApiResultCode.EXECUTE_RESOURCE_POOL_NOT_CONFIG);
        TestResourcePool resourcePool = baseResourcePoolTestService.insertResourcePool();
        baseResourcePoolTestService.insertResourcePoolOrg(resourcePool);
        // @校验项目没有资源池权限异常
        assertErrorCode(this.requestPost(DEBUG, request), ApiResultCode.EXECUTE_RESOURCE_POOL_NOT_CONFIG);

        baseResourcePoolTestService.insertResourcePoolProject(resourcePool);
        baseResourcePoolTestService.insertProjectApplication(resourcePool);
        // @校验资源池调用失败
        assertErrorCode(this.requestPost(DEBUG, request), ApiResultCode.RESOURCE_POOL_EXECUTE_ERROR);

        mockPost("/api/debug", "");
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
        request.setRequest(ApiDataUtils.toJSONString(msHTTPElement));
        this.requestPostWithOk(DEBUG, request);

        // 测试请求体
        MockMultipartFile file = getMockMultipartFile();
        String fileId = doUploadTempFile(file);
        request.setTempFileIds(List.of(fileId, fileMetadataId));
        Body generalBody = MsHTTPElementTest.getGeneralBody();
        msHTTPElement = MsHTTPElementTest.getMsHttpElement();
        msHTTPElement.setBody(generalBody);
        msHTTPElement.setEnable(true);
        testBodyParse(request, msHTTPElement, generalBody);

        // 增加覆盖率
        msHTTPElement.setMethod("GET");
        request.setRequest(ApiDataUtils.toJSONString(msHTTPElement));
        this.requestPostWithOk(DEBUG, request);
        msHTTPElement.setEnable(false);
        request.setRequest(ApiDataUtils.toJSONString(msHTTPElement));
        this.requestPostWithOk(DEBUG, request);

        // 增加覆盖率
        new TestElementParserFactory();
        new ImportParserFactory();
        MsScenarioConverter msScenarioConverter = new MsScenarioConverter();
        msScenarioConverter.toHashTree(new ListedHashTree(), new MsScenario(), new ParameterConfig());

        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEBUG_EXECUTE, DEBUG, request);
    }

    private void testBodyParse(ApiDebugRunRequest request, MsHTTPElement msHTTPElement, Body generalBody) throws Exception {
        // 测试 FORM_DATA
        generalBody.setBodyType(Body.BodyType.FORM_DATA.name());
        request.setRequest(ApiDataUtils.toJSONString(msHTTPElement));
        this.requestPostWithOk(DEBUG, request);

        // 测试 WWW_FORM
        generalBody.setBodyType(Body.BodyType.WWW_FORM.name());
        request.setRequest(ApiDataUtils.toJSONString(msHTTPElement));
        this.requestPostWithOk(DEBUG, request);

        // 测试 BINARY
        generalBody.setBodyType(Body.BodyType.BINARY.name());
        request.setRequest(ApiDataUtils.toJSONString(msHTTPElement));
        this.requestPostWithOk(DEBUG, request);

        // 测试 JSON
        generalBody.setBodyType(Body.BodyType.JSON.name());
        request.setRequest(ApiDataUtils.toJSONString(msHTTPElement));
        this.requestPostWithOk(DEBUG, request);

        // 测试 XML
        generalBody.setBodyType(Body.BodyType.XML.name());
        request.setRequest(ApiDataUtils.toJSONString(msHTTPElement));
        this.requestPostWithOk(DEBUG, request);

        // 测试 RAW
        generalBody.setBodyType(Body.BodyType.RAW.name());
        request.setRequest(ApiDataUtils.toJSONString(msHTTPElement));
        this.requestPostWithOk(DEBUG, request);
    }

    @Test
    @Order(7)
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