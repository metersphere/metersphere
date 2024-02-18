package io.metersphere.api.controller;

import io.metersphere.api.controller.result.ApiResultCode;
import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiDefinitionMock;
import io.metersphere.api.domain.ApiDefinitionMockConfig;
import io.metersphere.api.domain.ApiFileResource;
import io.metersphere.api.dto.definition.ApiDefinitionAddRequest;
import io.metersphere.api.dto.definition.ApiDefinitionMockDTO;
import io.metersphere.api.dto.definition.HttpResponse;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockAddRequest;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockPageRequest;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockRequest;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockUpdateRequest;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.api.mapper.ApiDefinitionMockConfigMapper;
import io.metersphere.api.mapper.ApiDefinitionMockMapper;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.api.utils.ApiDataUtils;
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
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.controller.handler.result.MsHttpResultCode;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;


@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ApiDefinitionMockControllerTests extends BaseTest {

    private static final String BASE_PATH = "/api/definition/mock/";
    private final static String ADD = BASE_PATH + "add";
    private final static String UPDATE = BASE_PATH + "update";
    private final static String DELETE = BASE_PATH + "delete";
    private final static String COPY = BASE_PATH + "copy";
    private final static String PAGE = BASE_PATH + "page";
    private static final String DETAIL = BASE_PATH + "detail";
    private static final String ENABLE = BASE_PATH + "enable/";

    private static final String UPLOAD_TEMP_FILE = BASE_PATH + "/upload/temp/file";

    private static final String DEFAULT_API_ID = "1001";

    private static ApiDefinitionMock apiDefinitionMock;

    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiDefinitionMockMapper apiDefinitionMockMapper;

    @Resource
    private ApiDefinitionMockConfigMapper apiDefinitionMockConfigMapper;

    @Resource
    private ApiFileResourceService apiFileResourceService;

    @Resource
    private FileMetadataService fileMetadataService;
    private static String fileMetadataId;
    private static String uploadFileId;



    @Test
    @Order(0)
    public void uploadTempFile() throws Exception {
        // 准备数据，上传文件管理文件
        uploadFileMetadata();
        // @@请求成功
        MockMultipartFile file = getMockMultipartFile("file_upload.JPG");
        String fileId = doUploadTempFile(file);

        // 校验文件存在
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFolder(DefaultRepositoryDir.getSystemTempDir() + "/" + fileId);
        fileRequest.setFileName(file.getOriginalFilename());
        Assertions.assertNotNull(FileCenter.getDefaultRepository().getFile(fileRequest));

        requestUploadPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_MOCK_ADD, UPLOAD_TEMP_FILE, file);
        requestUploadPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_MOCK_UPDATE, UPLOAD_TEMP_FILE, file);
    }

    private String doUploadTempFile(MockMultipartFile file) throws Exception {
        return JSON.parseObject(requestUploadFileWithOkAndReturn(UPLOAD_TEMP_FILE, file)
                        .getResponse()
                        .getContentAsString(), ResultHolder.class)
                .getData().toString();
    }

    private static MockMultipartFile getMockMultipartFile(String fileName) {
        return new MockMultipartFile(
                "file",
                fileName,
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "Hello, World!".getBytes()
        );
    }

    /**
     * 文件管理插入一条数据
     * 便于测试关联文件
     */
    private void uploadFileMetadata() throws Exception {
        FileUploadRequest fileUploadRequest = new FileUploadRequest();
        fileUploadRequest.setProjectId(DEFAULT_PROJECT_ID);
        //导入正常文件
        MockMultipartFile file = new MockMultipartFile("file", "mock_file_upload.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, "mock".getBytes());
        fileMetadataId = fileMetadataService.upload(fileUploadRequest, "admin", file);
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

            String apiDefinitionDir = DefaultRepositoryDir.getApiDefinitionDir(DEFAULT_PROJECT_ID, id);
            FileRequest fileRequest = new FileRequest();
            if (!fileIds.isEmpty()) {
                for (ApiFileResource apiFileResource : apiFileResources) {
                    Assertions.assertEquals(DEFAULT_PROJECT_ID, apiFileResource.getProjectId());
                    fileRequest.setFolder(apiDefinitionDir + "/" + apiFileResource.getFileId());
                    fileRequest.setFileName(apiFileResource.getFileName());
                    Assertions.assertNotNull(FileCenter.getDefaultRepository().getFile(fileRequest));
                }
                fileRequest.setFolder(apiDefinitionDir);
            } else {
                fileRequest.setFolder(apiDefinitionDir);
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
    @Order(1)
    @Sql(scripts = {"/dml/init_api_definition.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testAdd() throws Exception {
        LogUtils.info("create api mock test");
        // 创建测试数据
        ApiDefinitionMockAddRequest request = new ApiDefinitionMockAddRequest();
        request.setName("接口定义test");
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setApiDefinitionId(DEFAULT_API_ID);
        MsHTTPElement msHttpElement = MsHTTPElementTest.getMsHttpElement();
        request.setMatching(ApiDataUtils.toJSONString(msHttpElement));
        List<HttpResponse> msHttpResponse = MsHTTPElementTest.getMsHttpResponse();
        request.setResponse(ApiDataUtils.toJSONString(msHttpResponse));

        uploadFileId = doUploadTempFile(getMockMultipartFile("file_upload.JPG"));
        request.setUploadFileIds(List.of(uploadFileId));
        request.setLinkFileIds(List.of(fileMetadataId));

        // 执行方法调用
        MvcResult mvcResult = this.requestPostWithOkAndReturn(ADD, request);
        // 校验请求成功数据
        ApiDefinitionMock resultData = getResultData(mvcResult, ApiDefinitionMock.class);
        apiDefinitionMock = assertAddApiDefinitionMock(request, msHttpElement, resultData.getId());
        assertUploadFile(apiDefinitionMock.getId(), List.of(uploadFileId));
        assertLinkFile(apiDefinitionMock.getId(), List.of(fileMetadataId));

        // 再插入一条数据，便于修改时重名校验
        request.setName("重名接口定义test");
        request.setTags(new LinkedHashSet<>(List.of("tag1", "tag2")));
        request.setUploadFileIds(null);
        request.setLinkFileIds(null);
        mvcResult = this.requestPostWithOkAndReturn(ADD, request);
        resultData = getResultData(mvcResult, ApiDefinitionMock.class);
        assertAddApiDefinitionMock(request, msHttpElement, resultData.getId());
        // @@重名校验异常
        assertErrorCode(this.requestPost(ADD, request), ApiResultCode.API_DEFINITION_MOCK_EXIST);

        // 校验项目是否存在
        request.setProjectId("111");
        request.setName("test123");
        assertErrorCode(this.requestPost(ADD, request), MsHttpResultCode.NOT_FOUND);

        // @@校验日志
        this.checkLog(apiDefinitionMock.getId(), OperationLogType.ADD, ADD);
        // @@异常参数校验
        createdGroupParamValidateTest(ApiDefinitionAddRequest.class, ADD);
        // @@校验权限
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setName("permission-st-6");
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_MOCK_ADD, ADD, request);
    }

    private ApiDefinitionMock assertAddApiDefinitionMock(Object request, MsHTTPElement msHttpElement, String id) {
        ApiDefinitionMock apiDefinitionMock = apiDefinitionMockMapper.selectByPrimaryKey(id);
        ApiDefinitionMockConfig apiDefinitionMockConfig = apiDefinitionMockConfigMapper.selectByPrimaryKey(id);
        ApiDefinitionMock copyApiDefinitionMock = BeanUtils.copyBean(new ApiDefinitionMock(), apiDefinitionMock);
        BeanUtils.copyBean(copyApiDefinitionMock, request);
        Assertions.assertEquals(apiDefinitionMock, copyApiDefinitionMock);
        if(apiDefinitionMockConfig != null){
            Assertions.assertEquals(msHttpElement, ApiDataUtils.parseObject(new String(apiDefinitionMockConfig.getMatching()), AbstractMsTestElement.class));
        }
        return apiDefinitionMock;
    }

    @Test
    @Order(2)
    public void get() throws Exception {
        ApiDefinitionMockRequest apiDefinitionMockRequest = new ApiDefinitionMockRequest();
        apiDefinitionMockRequest.setId(apiDefinitionMock.getId());
        apiDefinitionMockRequest.setProjectId(DEFAULT_PROJECT_ID);
        apiDefinitionMockRequest.setApiDefinitionId(apiDefinitionMock.getApiDefinitionId());
        // @@请求成功
        MvcResult mvcResult = this.requestPostWithOkAndReturn(DETAIL, apiDefinitionMockRequest);
        ApiDefinitionMockDTO apiDefinitionMockDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResult).get("data")), ApiDefinitionMockDTO.class);
        // 校验数据是否正确
        ApiDefinitionMockDTO copyApiDefinitionMockDTO = BeanUtils.copyBean(new ApiDefinitionMockDTO(), apiDefinitionMock);


        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(apiDefinitionMock.getApiDefinitionId());
        copyApiDefinitionMockDTO.setApiNum(apiDefinition.getNum());
        copyApiDefinitionMockDTO.setApiName(apiDefinition.getName());
        copyApiDefinitionMockDTO.setApiPath(apiDefinition.getPath());
        copyApiDefinitionMockDTO.setApiMethod(apiDefinition.getMethod());

        ApiDefinitionMockConfig apiDefinitionMockConfig = apiDefinitionMockConfigMapper.selectByPrimaryKey(apiDefinitionMock.getId());
        if(apiDefinitionMockConfig != null){
            copyApiDefinitionMockDTO.setMatching(ApiDataUtils.parseObject(new String(apiDefinitionMockConfig.getMatching()), AbstractMsTestElement.class));
            copyApiDefinitionMockDTO.setResponse(ApiDataUtils.parseArray(new String(apiDefinitionMockConfig.getResponse()), HttpResponse.class));
        }
        Assertions.assertEquals(apiDefinitionMockDTO, copyApiDefinitionMockDTO);

        apiDefinitionMockRequest.setId("111");
        assertErrorCode(this.requestPost(DETAIL, apiDefinitionMockRequest), MsHttpResultCode.NOT_FOUND);

        // @@校验权限
        apiDefinitionMockRequest.setId(apiDefinitionMock.getId());
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_MOCK_READ, DETAIL, apiDefinitionMockRequest);
    }

    @Test
    @Order(3)
    public void testUpdate() throws Exception {
        LogUtils.info("update api mock test");

        ApiDefinitionMockUpdateRequest request = new ApiDefinitionMockUpdateRequest();
        BeanUtils.copyBean(request, apiDefinitionMock);
        request.setName("test1test1test1test1test1test1");

        MsHTTPElement msHttpElement = MsHTTPElementTest.getMsHttpElement();
        request.setMatching(ApiDataUtils.toJSONString(msHttpElement));
        List<HttpResponse> msHttpResponse = MsHTTPElementTest.getMsHttpResponse();
        request.setResponse(ApiDataUtils.toJSONString(msHttpResponse));

        // 清除文件的更新
        request.setUnLinkRefIds(List.of(fileMetadataId));
        request.setDeleteFileIds(List.of(uploadFileId));

        this.requestPostWithOk(UPDATE, request);
        // 校验请求成功数据
        apiDefinitionMock = assertAddApiDefinitionMock(request, msHttpElement, request.getId());
        assertUploadFile(apiDefinitionMock.getId(), List.of());
        assertLinkFile(apiDefinitionMock.getId(), List.of());

        // 带文件的更新
        String fileId = doUploadTempFile(getMockMultipartFile("file_upload.JPG"));
        request.setUploadFileIds(List.of(fileId));
        request.setLinkFileIds(List.of(fileMetadataId));
        request.setDeleteFileIds(null);
        request.setUnLinkRefIds(null);
        this.requestPostWithOk(UPDATE, request);
        // 校验请求成功数据
        apiDefinitionMock = assertAddApiDefinitionMock(request, msHttpElement, request.getId());
        assertUploadFile(apiDefinitionMock.getId(), List.of(fileId));
        assertLinkFile(apiDefinitionMock.getId(), List.of(fileMetadataId));

        // 删除了上一次上传的文件，重新上传一个文件
        request.setDeleteFileIds(List.of(fileId));
        String newFileId1 = doUploadTempFile(getMockMultipartFile("file_upload.JPG"));
        request.setUploadFileIds(List.of(newFileId1));
        request.setUnLinkRefIds(List.of(fileMetadataId));
        request.setLinkFileIds(List.of(fileMetadataId));
        this.requestPostWithOk(UPDATE, request);
        apiDefinitionMock = assertAddApiDefinitionMock(request, msHttpElement, request.getId());
        assertUploadFile(apiDefinitionMock.getId(), List.of(newFileId1));
        assertLinkFile(apiDefinitionMock.getId(), List.of(fileMetadataId));

        // 已有一个文件，再上传一个文件
        String newFileId2 = doUploadTempFile(getMockMultipartFile("file_update_upload.JPG"));
        request.setUploadFileIds(List.of(newFileId2));
        request.setUnLinkRefIds(null);
        request.setDeleteFileIds(null);
        request.setLinkFileIds(null);
        this.requestPostWithOk(UPDATE, request);
        apiDefinitionMock = assertAddApiDefinitionMock(request, msHttpElement, request.getId());
        assertUploadFile(apiDefinitionMock.getId(), List.of(newFileId1, newFileId2));
        assertLinkFile(apiDefinitionMock.getId(), List.of(fileMetadataId));
        // 修改 tags
        request.setUploadFileIds(null);
        request.setUnLinkRefIds(null);
        request.setDeleteFileIds(null);
        request.setLinkFileIds(null);
        request.setTags(new LinkedHashSet<>(List.of("tag1", "tag2-update")));
        request.setName("接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test");
        MsHTTPElement msHttpElementTag = MsHTTPElementTest.getMsHttpElement();
        request.setMatching(ApiDataUtils.toJSONString(msHttpElementTag));
        List<HttpResponse> msHttpResponseTag = MsHTTPElementTest.getMsHttpResponse();
        request.setResponse(ApiDataUtils.toJSONString(msHttpResponseTag));

        this.requestPostWithOk(UPDATE, request);
        // 校验请求成功数据
        assertAddApiDefinitionMock(request, msHttpElement, request.getId());

        request.setName("重名接口定义test");
        // @@重名校验异常
        assertErrorCode(this.requestPost(UPDATE, request), ApiResultCode.API_DEFINITION_MOCK_EXIST);

        // 校验项目是否存在
        request.setProjectId("111");
        request.setName("test123");
        assertErrorCode(this.requestPost(UPDATE, request), MsHttpResultCode.NOT_FOUND);

        // 校验数据是否存在
        request.setId("111");
        request.setName("test123");
        assertErrorCode(this.requestPost(UPDATE, request), MsHttpResultCode.NOT_FOUND);

        // @@校验日志
        checkLog(apiDefinitionMock.getId(), OperationLogType.UPDATE, UPDATE);
        // @@异常参数校验
        createdGroupParamValidateTest(ApiDefinitionMockUpdateRequest.class, UPDATE);
        // @@校验权限
        request.setId(apiDefinitionMock.getId());
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setName("permission-st-6");
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_MOCK_UPDATE, UPDATE, request);

    }

    @Test
    @Order(4)
    public void testUpdateEnable() throws Exception {
        LogUtils.info("update enable api mock test");
        // @@关闭
        // @@请求成功
        this.requestGetWithOk(ENABLE + apiDefinitionMock.getId());
        ApiDefinitionMock mock = apiDefinitionMockMapper.selectByPrimaryKey(apiDefinitionMock.getId());
        Assertions.assertEquals(apiDefinitionMock.getEnable(),!mock.getEnable());
        // @@校验日志
        checkLog(apiDefinitionMock.getId(), OperationLogType.UPDATE, ENABLE + apiDefinitionMock.getId());

        assertErrorCode(this.requestGet(ENABLE + "111"), MsHttpResultCode.NOT_FOUND);

        // @@开启
        // @@请求成功
        this.requestGetWithOk(ENABLE + apiDefinitionMock.getId());
        ApiDefinitionMock mockEnable = apiDefinitionMockMapper.selectByPrimaryKey(apiDefinitionMock.getId());
        Assertions.assertEquals(mock.getEnable(),!mockEnable.getEnable());
        // @@校验日志
        checkLog(apiDefinitionMock.getId(), OperationLogType.UPDATE, ENABLE + apiDefinitionMock.getId());

        assertErrorCode(this.requestGet(ENABLE + "111"), MsHttpResultCode.NOT_FOUND);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_MOCK_UPDATE, ENABLE + apiDefinitionMock.getId());
    }

    @Test
    @Order(5)
    public void copy() throws Exception {
        LogUtils.info("copy api test");
        ApiDefinitionMockRequest request = new ApiDefinitionMockRequest();
        request.setId(apiDefinitionMock.getId());
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setApiDefinitionId(apiDefinitionMock.getApiDefinitionId());
        MvcResult mvcResult = this.requestPostWithOkAndReturn(COPY, request);
        ApiDefinitionMock resultData = getResultData(mvcResult, ApiDefinitionMock.class);
        // @数据验证
        List<ApiFileResource> sourceFiles = apiFileResourceService.getByResourceId(apiDefinitionMock.getId());
        List<ApiFileResource> copyFiles = apiFileResourceService.getByResourceId(resultData.getId());
        if(!sourceFiles.isEmpty() && !copyFiles.isEmpty()){
            Assertions.assertEquals(sourceFiles.size(), copyFiles.size());
        }
        Assertions.assertTrue(resultData.getName().contains("copy_"));

        ApiDefinitionMockUpdateRequest apiDefinitionMockUpdateRequest = new ApiDefinitionMockUpdateRequest();
        BeanUtils.copyBean(apiDefinitionMockUpdateRequest, apiDefinitionMock);
        apiDefinitionMockUpdateRequest.setName("test1test1test1test1test1test1");

        MsHTTPElement msHttpElement = MsHTTPElementTest.getMsHttpElement();
        apiDefinitionMockUpdateRequest.setMatching(ApiDataUtils.toJSONString(msHttpElement));
        List<HttpResponse> msHttpResponse = MsHTTPElementTest.getMsHttpResponse();
        apiDefinitionMockUpdateRequest.setResponse(ApiDataUtils.toJSONString(msHttpResponse));

        this.requestPostWithOk(UPDATE, apiDefinitionMockUpdateRequest);
        // 校验请求成功数据
        apiDefinitionMock = assertAddApiDefinitionMock(request, msHttpElement, request.getId());
        request.setId(apiDefinitionMock.getId());
        MvcResult mvcResultCopy = this.requestPostWithOkAndReturn(COPY, request);
        ApiDefinitionMock resultDataCopy = getResultData(mvcResultCopy, ApiDefinitionMock.class);
        // @数据验证
        Assertions.assertTrue(resultDataCopy.getName().contains("copy_"));

        // @@校验日志
        checkLog(resultData.getId(), OperationLogType.UPDATE);
        request.setId("121");
        assertErrorCode(this.requestPost(COPY, request),  MsHttpResultCode.NOT_FOUND);
        // @@校验权限
        request.setId(apiDefinitionMock.getId());
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_MOCK_UPDATE, COPY, request);
    }


    @Test
    @Order(9)
    public void getPage() throws Exception {
        doApiDefinitionPage("KEYWORD");
        doApiDefinitionPage("FILTER");
    }

    private void doApiDefinitionPage(String search) throws Exception {
        ApiDefinitionMockPageRequest request = new ApiDefinitionMockPageRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setApiDefinitionId(apiDefinitionMock.getApiDefinitionId());
        request.setCurrent(1);
        request.setPageSize(10);
        request.setSort(Map.of("createTime", "asc"));
        // "ALL", "KEYWORD", "FILTER", "COMBINE", "DELETED"
        switch (search) {
            case "KEYWORD" -> configureKeywordSearch(request);
            case "FILTER" -> configureFilterSearch(request);
            default -> {}
        }

        MvcResult mvcResult = this.requestPostWithOkAndReturn(PAGE, request);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        Pager<?> pageData = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        // 返回值不为空
        Assertions.assertNotNull(pageData);
        // 返回值的页码和当前页码相同
        Assertions.assertEquals(pageData.getCurrent(), request.getCurrent());
        // 返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(pageData.getList())).size() <= request.getPageSize());

    }

    private void configureKeywordSearch(ApiDefinitionMockPageRequest request) {
        request.setKeyword("100");
        request.setSort(Map.of("enable", "asc"));
    }

    private void configureFilterSearch(ApiDefinitionMockPageRequest request) {
        Map<String, List<String>> filters = new HashMap<>();
        request.setSort(Map.of());
        filters.put("enable", List.of("1"));
        filters.put("tags", List.of("tag1"));
        request.setFilter(filters);
    }

    @Test
    @Order(12)
    public void testDel() throws Exception {
        LogUtils.info("delete api mock test");
        ApiDefinitionMockRequest apiDefinitionMockRequest = new ApiDefinitionMockRequest();
        apiDefinitionMockRequest.setId(apiDefinitionMock.getId());
        apiDefinitionMockRequest.setProjectId(DEFAULT_PROJECT_ID);
        apiDefinitionMockRequest.setApiDefinitionId(apiDefinitionMock.getApiDefinitionId());
        // @@请求成功
        this.requestPostWithOkAndReturn(DELETE, apiDefinitionMockRequest);
        checkLog(apiDefinitionMock.getId(), OperationLogType.DELETE);
        ApiDefinitionMock apiDefinitionMockInfo = apiDefinitionMockMapper.selectByPrimaryKey(apiDefinitionMock.getId());
        Assertions.assertNull(apiDefinitionMockInfo);

        // config是否删除
        ApiDefinitionMockConfig apiDefinitionMockConfig = apiDefinitionMockConfigMapper.selectByPrimaryKey(apiDefinitionMock.getId());
        Assertions.assertNull(apiDefinitionMockConfig);

        // 文件是否删除
        List<ApiFileResource> apiFileResources = apiFileResourceService.getByResourceId(apiDefinitionMock.getId());
        Assertions.assertEquals(0, apiFileResources.size());

        checkLog(apiDefinitionMockRequest.getId(), OperationLogType.DELETE);
        apiDefinitionMockRequest.setId("121");
        assertErrorCode(this.requestPost(DELETE, apiDefinitionMockRequest), MsHttpResultCode.NOT_FOUND);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_MOCK_DELETE, DELETE, apiDefinitionMockRequest);
    }




}
