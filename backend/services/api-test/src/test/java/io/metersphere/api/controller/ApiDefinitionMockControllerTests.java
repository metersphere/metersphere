package io.metersphere.api.controller;

import io.metersphere.api.constants.ApiConstants;
import io.metersphere.api.constants.ApiDefinitionStatus;
import io.metersphere.api.controller.result.ApiResultCode;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.ApiFile;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockAddRequest;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockPageRequest;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockRequest;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockUpdateRequest;
import io.metersphere.api.dto.mockserver.KeyValueInfo;
import io.metersphere.api.dto.mockserver.MockMatchRule;
import io.metersphere.api.dto.mockserver.MockResponse;
import io.metersphere.api.dto.request.http.MsHeader;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.body.Body;
import io.metersphere.api.mapper.*;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.api.service.MockServerTestService;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.dto.filemanagement.request.FileUploadRequest;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.project.mapper.FileMetadataMapper;
import io.metersphere.project.service.FileManagementService;
import io.metersphere.project.service.FileMetadataService;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileRepository;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.TempFileUtils;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.controller.handler.result.MsHttpResultCode;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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
    private static Long NO_MOCK_NO_RESPONSE_API_NUM;

    private static ApiDefinitionMock apiDefinitionMock;

    @Resource
    private MockServerTestService mockServerTestService;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiDefinitionBlobMapper apiDefinitionBlobMapper;
    @Resource
    private ApiDefinitionMockMapper apiDefinitionMockMapper;
    @Resource
    private ApiDefinitionMockConfigMapper apiDefinitionMockConfigMapper;
    @Resource
    private ExtBaseProjectVersionMapper extBaseProjectVersionMapper;
    @Resource
    private ApiFileResourceService apiFileResourceService;
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private FileManagementService fileManagementService;
    @Resource
    private FileMetadataService fileMetadataService;
    @Resource
    private ApiFileResourceMapper apiFileResourceMapper;

    //文件管理中已存在的ID
    private static String fileMetadataId;
    private static String uploadFileId;

    private static Map<String, ApiDefinition> METHOD_API_MAP = new LinkedHashMap<>();
    private static Map<ApiDefinition, List<ApiDefinitionMock>> API_MOCK_MAP = new LinkedHashMap<>();

    private static String[] HTTP_METHODS = {"POST", "GET", "HEAD", "PUT", "PATCH", "DELETE", "OPTIONS", "TRACE"};

    /**
     * 文件管理插入一条数据
     * 便于测试关联文件
     */
    private void uploadFileMetadata() throws Exception {
        FileUploadRequest fileUploadRequest = new FileUploadRequest();
        fileUploadRequest.setProjectId(DEFAULT_PROJECT_ID);
        //导入正常文件
        MockMultipartFile file = new MockMultipartFile("file", "mock_file_upload.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, "file-metadata.file".getBytes());
        fileMetadataId = fileMetadataService.upload(fileUploadRequest, "admin", file);
    }

    public String doUploadTempFile(MockMultipartFile file) throws Exception {
        return JSON.parseObject(requestUploadFileWithOkAndReturn(UPLOAD_TEMP_FILE, file)
                        .getResponse()
                        .getContentAsString(), ResultHolder.class)
                .getData().toString();
    }

    @Test
    @Order(0)
    public void uploadTempFile() throws Exception {
        // 准备数据，上传文件管理文件
        uploadFileMetadata();
        // @@请求成功
        MockMultipartFile file = mockServerTestService.getMockMultipartFile("file_upload.JPG");
        String fileId = doUploadTempFile(file);

        // 校验文件存在
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFolder(DefaultRepositoryDir.getSystemTempDir() + "/" + fileId);
        fileRequest.setFileName(file.getOriginalFilename());
        Assertions.assertNotNull(FileCenter.getDefaultRepository().getFile(fileRequest));

        requestUploadPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_MOCK_ADD, UPLOAD_TEMP_FILE, file);
        requestUploadPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_MOCK_UPDATE, UPLOAD_TEMP_FILE, file);

        // 这个api是用于测试没有配置任何mock以及默认响应的情况
        String defaultVersion = extBaseProjectVersionMapper.getDefaultVersion(DEFAULT_PROJECT_ID);
        ApiDefinitionAddRequest noMockNoResponseApiRequest = new ApiDefinitionAddRequest();
        noMockNoResponseApiRequest.setName("MockApi_No_Response");
        noMockNoResponseApiRequest.setProtocol(ApiConstants.HTTP_PROTOCOL);
        noMockNoResponseApiRequest.setProjectId(DEFAULT_PROJECT_ID);
        noMockNoResponseApiRequest.setMethod("GET");
        noMockNoResponseApiRequest.setPath("/mock/api/notMatch/");
        noMockNoResponseApiRequest.setStatus(ApiDefinitionStatus.PROCESSING.name());
        noMockNoResponseApiRequest.setModuleId(ModuleConstants.DEFAULT_NODE_ID);
        noMockNoResponseApiRequest.setVersionId(defaultVersion);
        noMockNoResponseApiRequest.setDescription("desc");
        noMockNoResponseApiRequest.setRequest(JSON.parseObject(ApiDataUtils.toJSONString(MsHTTPElementTest.getMsHttpElement())));
        noMockNoResponseApiRequest.setResponse(new ArrayList<>());
        NO_MOCK_NO_RESPONSE_API_NUM = getResultData(this.requestPostWithOkAndReturn("/api/definition/add", noMockNoResponseApiRequest), ApiDefinition.class).getNum();

        if (MapUtils.isEmpty(METHOD_API_MAP)) {
            for (String method : HTTP_METHODS) {
                // 创建并返回一个 ApiDefinitionAddRequest 对象，用于测试
                ApiDefinitionAddRequest request = new ApiDefinitionAddRequest();
                request.setName("MockApi:" + method);
                request.setProtocol(ApiConstants.HTTP_PROTOCOL);
                request.setProjectId(DEFAULT_PROJECT_ID);
                request.setMethod(method);
                request.setPath("/mock/api/" + method + "/{param1}/{param2}");
                request.setStatus(ApiDefinitionStatus.PROCESSING.name());
                request.setModuleId(ModuleConstants.DEFAULT_NODE_ID);
                request.setVersionId(defaultVersion);
                request.setDescription("desc");
                MsHTTPElement msHttpElement = MsHTTPElementTest.getMsHttpElement();
                request.setRequest(JSON.parseObject(ApiDataUtils.toJSONString(msHttpElement)));
                request.setResponse(MsHTTPElementTest.get2MsHttpResponse(request.getPath()));
                MvcResult mvcResult = this.requestPostWithOkAndReturn("/api/definition/add", request);
                ApiDefinition resultData = getResultData(mvcResult, ApiDefinition.class);
                METHOD_API_MAP.put(method, resultData);
            }
        }
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
        MockMatchRule mockMatchRule = new MockMatchRule();
        request.setMockMatchRule(mockMatchRule);
        uploadFileId = doUploadTempFile(mockServerTestService.getMockMultipartFile("file_upload.JPG"));
        MockResponse mockResponse = new MockResponse();
        mockResponse.setBody(new ResponseBody() {{
            this.setBinaryBody(new ResponseBinaryBody() {{
                this.setFile(new ApiFile());
                this.getFile().setFileId(uploadFileId);
                this.getFile().setFileName("file_upload.JPG");
            }});
        }});
        request.setResponse(mockResponse);
        request.setUploadFileIds(List.of(uploadFileId));
        request.setLinkFileIds(List.of(fileMetadataId));

        // 执行方法调用
        MvcResult mvcResult = this.requestPostWithOkAndReturn(ADD, request);
        // 校验请求成功数据
        ApiDefinitionMock resultData = getResultData(mvcResult, ApiDefinitionMock.class);
        apiDefinitionMock = mockServerTestService.assertAddApiDefinitionMock(request, mockMatchRule, resultData.getId());
        mockServerTestService.assertUploadFile(apiDefinitionMock.getId(), List.of(uploadFileId));
        mockServerTestService.assertLinkFile(apiDefinitionMock.getId(), List.of(fileMetadataId));

        // 再插入一条数据，便于修改时重名校验
        request.setName("重名接口定义test");
        request.setTags(new LinkedHashSet<>(List.of("tag1", "tag2")));
        request.setUploadFileIds(null);
        request.setLinkFileIds(null);
        mvcResult = this.requestPostWithOkAndReturn(ADD, request);
        resultData = getResultData(mvcResult, ApiDefinitionMock.class);
        mockServerTestService.assertAddApiDefinitionMock(request, mockMatchRule, resultData.getId());
        // @@重名校验异常
        assertErrorCode(this.requestPost(ADD, request), ApiResultCode.API_DEFINITION_MOCK_EXIST);

        // 校验项目是否存在
        request.setProjectId("111");
        request.setName("test123");
        assertErrorCode(this.requestPost(ADD, request), MsHttpResultCode.NOT_FOUND);

        // @@校验日志
        this.checkLog(apiDefinitionMock.getId(), OperationLogType.ADD, ADD);
        // @@校验权限
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setName("permission-st-6");
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_MOCK_ADD, ADD, request);

    }


    @Test
    @Order(2)
    public void get() throws Exception {
        ApiDefinitionMockRequest apiDefinitionMockRequest = new ApiDefinitionMockRequest();
        apiDefinitionMockRequest.setId(apiDefinitionMock.getId());
        apiDefinitionMockRequest.setProjectId(DEFAULT_PROJECT_ID);
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
            copyApiDefinitionMockDTO.setMatching(ApiDataUtils.parseObject(new String(apiDefinitionMockConfig.getMatching()), MockMatchRule.class));
            copyApiDefinitionMockDTO.setResponse(ApiDataUtils.parseObject(new String(apiDefinitionMockConfig.getResponse()), MockResponse.class));
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

        MockMatchRule mockMatchRule = new MockMatchRule();
        ApiDefinitionMockUpdateRequest request = new ApiDefinitionMockUpdateRequest();
        BeanUtils.copyBean(request, apiDefinitionMock);
        request.setName("test1test1test1test1test1test1");
        request.setMockMatchRule(mockMatchRule);
        request.setResponse(new MockResponse());

        // 清除文件的更新
        request.setUnLinkFileIds(List.of(fileMetadataId));
        request.setDeleteFileIds(List.of(uploadFileId));

        this.requestPostWithOk(UPDATE, request);
        // 校验请求成功数据
        apiDefinitionMock = mockServerTestService.assertAddApiDefinitionMock(request, mockMatchRule, request.getId());
        mockServerTestService.assertUploadFile(apiDefinitionMock.getId(), List.of());
        mockServerTestService.assertLinkFile(apiDefinitionMock.getId(), List.of());

        // 带文件的更新
        String fileId = doUploadTempFile(mockServerTestService.getMockMultipartFile("file_upload.JPG"));
        request.setUploadFileIds(List.of(fileId));
        request.setLinkFileIds(List.of(fileMetadataId));
        request.setDeleteFileIds(null);
        request.setUnLinkFileIds(null);
        this.requestPostWithOk(UPDATE, request);
        // 校验请求成功数据
        apiDefinitionMock = mockServerTestService.assertAddApiDefinitionMock(request, mockMatchRule, request.getId());
        mockServerTestService.assertUploadFile(apiDefinitionMock.getId(), List.of(fileId));
        mockServerTestService.assertLinkFile(apiDefinitionMock.getId(), List.of(fileMetadataId));

        // 删除了上一次上传的文件，重新上传一个文件
        request.setDeleteFileIds(List.of(fileId));
        String newFileId1 = doUploadTempFile(mockServerTestService.getMockMultipartFile("file_upload.JPG"));
        request.setUploadFileIds(List.of(newFileId1));
        request.setUnLinkFileIds(List.of(fileMetadataId));
        request.setLinkFileIds(List.of(fileMetadataId));
        this.requestPostWithOk(UPDATE, request);
        apiDefinitionMock = mockServerTestService.assertAddApiDefinitionMock(request, mockMatchRule, request.getId());
        mockServerTestService.assertUploadFile(apiDefinitionMock.getId(), List.of(newFileId1));
        mockServerTestService.assertLinkFile(apiDefinitionMock.getId(), List.of(fileMetadataId));

        // 已有一个文件，再上传一个文件
        String newFileId2 = doUploadTempFile(mockServerTestService.getMockMultipartFile("file_update_upload.JPG"));
        request.setUploadFileIds(List.of(newFileId2));
        request.setUnLinkFileIds(null);
        request.setDeleteFileIds(null);
        request.setLinkFileIds(null);
        this.requestPostWithOk(UPDATE, request);
        apiDefinitionMock = mockServerTestService.assertAddApiDefinitionMock(request, mockMatchRule, request.getId());
        mockServerTestService.assertUploadFile(apiDefinitionMock.getId(), List.of(newFileId1, newFileId2));
        mockServerTestService.assertLinkFile(apiDefinitionMock.getId(), List.of(fileMetadataId));
        // 修改 tags
        request.setUploadFileIds(null);
        request.setUnLinkFileIds(null);
        request.setDeleteFileIds(null);
        request.setLinkFileIds(null);
        request.setTags(new LinkedHashSet<>(List.of("tag1", "tag2-update")));
        request.setName("接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test接口定义test");

        this.requestPostWithOk(UPDATE, request);
        // 校验请求成功数据
        mockServerTestService.assertAddApiDefinitionMock(request, mockMatchRule, request.getId());

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
        this.requestPost(UPDATE, request).andExpect(status().is5xxServerError());

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

        assertErrorCode(this.requestGet(ENABLE + "111"), MsHttpResultCode.FAILED);

        // @@开启
        // @@请求成功
        this.requestGetWithOk(ENABLE + apiDefinitionMock.getId());
        ApiDefinitionMock mockEnable = apiDefinitionMockMapper.selectByPrimaryKey(apiDefinitionMock.getId());
        Assertions.assertEquals(mock.getEnable(),!mockEnable.getEnable());
        // @@校验日志
        checkLog(apiDefinitionMock.getId(), OperationLogType.UPDATE, ENABLE + apiDefinitionMock.getId());

        assertErrorCode(this.requestGet(ENABLE + "111"), MsHttpResultCode.FAILED);
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
        MvcResult mvcResult = this.requestPostWithOkAndReturn(COPY, request);
        ApiDefinitionMock resultData = getResultData(mvcResult, ApiDefinitionMock.class);
        // @数据验证
        List<ApiFileResource> sourceFiles = apiFileResourceService.getByResourceId(apiDefinitionMock.getId());
        List<ApiFileResource> copyFiles = apiFileResourceService.getByResourceId(resultData.getId());
        if(!sourceFiles.isEmpty() && !copyFiles.isEmpty()){
            Assertions.assertEquals(sourceFiles.size(), copyFiles.size());
        }
        Assertions.assertTrue(resultData.getName().contains("copy_"));

        // @@校验日志
        checkLog(resultData.getId(), OperationLogType.UPDATE);
        request.setId("121");
        assertErrorCode(this.requestPost(COPY, request), MsHttpResultCode.FAILED);
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
        assertErrorCode(this.requestPost(DELETE, apiDefinitionMockRequest), MsHttpResultCode.FAILED);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_MOCK_DELETE, DELETE, apiDefinitionMockRequest);
    }

    @Test
    @Order(99)
    public void mockServerTest() throws Exception {
        this.initMockConfigTestData();

        //测试匹配不到任何一个api
        mockServerTestService.testNoMatchApi();
        //测试匹配到了api但是路径不一样
        mockServerTestService.testApiNoMockConfigAndNoResponse("/mock-server/100001/" + NO_MOCK_NO_RESPONSE_API_NUM + "/mock/api/testStr/");
        //测试匹配到的api没用配置mockConfig以及没有定义默认返回项
        mockServerTestService.testApiNoMockConfigAndNoResponse("/mock-server/100001/" + NO_MOCK_NO_RESPONSE_API_NUM + "/mock/api/notMatch/");

        /*
        测试用例：
            * rest全匹配:          返回接口定义的响应
            * rest半匹配:          尝试返回文件
            * header全匹配:        尝试返回body-xml
            * header半匹配:        尝试返回body-json
            * get类型的请求测试：     Query全匹配  Query半匹配
            * post类型的请求测试：    Body-kv全匹配、Body-kv半匹配、Body-json包含匹配、Body-xml包含匹配、RAW包含匹配
         */
        for (Map.Entry<ApiDefinition, List<ApiDefinitionMock>> entry : API_MOCK_MAP.entrySet()) {
            ApiDefinition apiDefinition = entry.getKey();
            List<ApiDefinitionMock> apiDefinitionMockList = entry.getValue();

            String method = apiDefinition.getMethod();
            if (StringUtils.equalsIgnoreCase(method, "TRACE")) {
                //这种不测试
                continue;
            }
            String url = "/mock-server/100001/" + apiDefinition.getNum() + apiDefinition.getPath();
            //先做一个没有匹配到任何的mock期望的测试
            mockServerTestService.testNoMatchMockConfig(method, url, apiDefinition.getPath());

            for (ApiDefinitionMock mock : apiDefinitionMockList) {
                //重置url
                url = "/mock-server/100001/" + apiDefinition.getNum() + apiDefinition.getPath();

                ApiDefinitionBlob apiDefinitionBlob = apiDefinitionBlobMapper.selectByPrimaryKey(apiDefinition.getId());
                ApiDefinitionMockConfig mockConfig = apiDefinitionMockConfigMapper.selectByPrimaryKey(mock.getId());
                MockMatchRule mockMatchRule = JSON.parseObject(new String(mockConfig.getMatching()), MockMatchRule.class);
                MockResponse mockResponse = JSON.parseObject(new String(mockConfig.getResponse()), MockResponse.class);
                List<HttpResponse> apiResponseList = JSON.parseArray(new String(apiDefinitionBlob.getResponse()), HttpResponse.class);
                HttpResponse MockUseApiRsponse = null;
                for (HttpResponse apiResponse : apiResponseList) {
                    if (mockResponse.isUseApiResponse() && StringUtils.equals(mockResponse.getApiResponseId(), apiResponse.getId())) {
                        MockUseApiRsponse = apiResponse;
                    }
                }
                String[] mockNameArr = mock.getName().split("_");
                String methodType = mockNameArr[1];  //
                String conditionType = mockNameArr[2];

                //替换rest参数
                for (KeyValueInfo keyValueInfo : mockMatchRule.getRest().getMatchRules()) {
                    url = StringUtils.replace(url, "{" + keyValueInfo.getKey() + "}", keyValueInfo.getValue());
                }

                //设置query参数
                StringBuilder queryParamBuilder = new StringBuilder();
                if (CollectionUtils.isNotEmpty(mockMatchRule.getQuery().getMatchRules())) {
                    for (KeyValueInfo keyValueInfo : mockMatchRule.getQuery().getMatchRules()) {
                        if (!queryParamBuilder.isEmpty()) {
                            queryParamBuilder.append("&");
                        }
                        queryParamBuilder.append(keyValueInfo.getKey());
                        queryParamBuilder.append("=");
                        queryParamBuilder.append(keyValueInfo.getValue());
                        if (!mockMatchRule.getQuery().isMatchAll()) {
                            break;
                        }
                    }
                    url = url + "?" + queryParamBuilder;
                }

                //开始创建请求
                MockHttpServletRequestBuilder requestBuilder = mockServerTestService.getRequestBuilder(method, url);

                //设置请求头   如果匹配类型是body-json或者body-xml，需要设置content-type
                if (StringUtils.equalsIgnoreCase(conditionType, "body-json")) {
                    requestBuilder.header("content-type", "application/json");
                } else if (StringUtils.equalsIgnoreCase(conditionType, "body-xml")) {
                    requestBuilder.header("content-type", "application/xml");
                } else if (StringUtils.equalsIgnoreCase(conditionType, "body-kv-x-www")) {
                    requestBuilder.header("content-type", "application/x-www-form-urlencoded");
                } else if (StringUtils.equalsIgnoreCase(conditionType, "Body-raw")) {
                    requestBuilder.header("content-type", "text/plain");
                }

                if (CollectionUtils.isNotEmpty(mockMatchRule.getHeader().getMatchRules())) {
                    for (KeyValueInfo keyValueInfo : mockMatchRule.getHeader().getMatchRules()) {
                        requestBuilder.header(keyValueInfo.getKey(), keyValueInfo.getValue());
                        if (!mockMatchRule.getHeader().isMatchAll()) {
                            break;
                        }
                    }
                }

                //设置body参数 (get类型的请求不设置）
                if (this.isNotGetTypeMethod(methodType) && StringUtils.equalsIgnoreCase(mockMatchRule.getBody().getParamType(), Body.BodyType.FORM_DATA.name())) {
                    for (KeyValueInfo keyValueInfo : mockMatchRule.getBody().getFormDataMatch().getMatchRules()) {
                        requestBuilder.param(keyValueInfo.getKey(), keyValueInfo.getValue());
                        if (!mockMatchRule.getBody().getFormDataMatch().isMatchAll()) {
                            break;
                        }
                    }
                } else if (StringUtils.isNotBlank(mockMatchRule.getBody().getRaw())) {
                    requestBuilder.content(mockMatchRule.getBody().getRaw());
                }

                //发送请求
                ResultActions action = mockMvc.perform(requestBuilder);

                //判断响应
                List<MsHeader> headers;
                int statusCode;
                ResponseBody responseBody;
                if (mockResponse.isUseApiResponse()) {
                    headers = MockUseApiRsponse.getHeaders();
                    statusCode = Integer.parseInt(MockUseApiRsponse.getStatusCode());
                    responseBody = MockUseApiRsponse.getBody();
                } else {
                    headers = mockResponse.getHeaders();
                    statusCode = mockResponse.getStatusCode();
                    responseBody = mockResponse.getBody();
                }

                MockHttpServletResponse mockServerResponse = action.andReturn().getResponse();
                //判断响应码
                Assertions.assertEquals(mockServerResponse.getStatus(), statusCode);
                //判断响应头
                for (MsHeader header : headers) {
                    if (header.getEnable()) {
                        Assertions.assertEquals(mockServerResponse.getHeader(header.getKey()), header.getValue());
                    }
                }
                //判断响应体
                if (StringUtils.equals(responseBody.getBodyType(), Body.BodyType.BINARY.name())) {
                    byte[] returnFileBytes = mockServerResponse.getContentAsByteArray();
                    String fileId = responseBody.getBinaryBody().getFile().getFileId();
                    byte[] bytes = new byte[0];
                    FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(fileId);
                    if (fileMetadata != null) {
                        String filePath = TempFileUtils.createFile(TempFileUtils.getTmpFilePath(fileMetadata.getId()), fileManagementService.getFile(fileMetadata));
                        bytes = TempFileUtils.getFile(filePath);
                    } else {
                        ApiFileResource apiFileResource = apiFileResourceMapper.selectByPrimaryKey(mock.getId(), fileId);
                        if (apiFileResource != null) {
                            FileRepository defaultRepository = FileCenter.getDefaultRepository();
                            FileRequest fileRequest = new FileRequest();
                            fileRequest.setFileName(apiFileResource.getFileName());
                            fileRequest.setFolder(DefaultRepositoryDir.getApiDefinitionDir(apiDefinition.getProjectId(), mock.getId()) + "/" + fileId);
                            try {
                                bytes = defaultRepository.getFile(fileRequest);
                            } catch (Exception ignore) {
                            }
                        }
                    }

                    //通过MD5判断是否是同一个文件
                    String fileMD5 = this.getFileMD5(bytes);
                    String downloadMD5 = this.getFileMD5(returnFileBytes);
                    Assertions.assertEquals(fileMD5, downloadMD5);

                } else {
                    String returnStr = mockServerResponse.getContentAsString(StandardCharsets.UTF_8);
                    String compareStr = "";
                    switch (responseBody.getBodyType()) {
                        case "JSON":
                            compareStr = responseBody.getJsonBody().getJsonValue();
                            break;
                        case "XML":
                            compareStr = responseBody.getXmlBody().getValue();
                            break;
                        case "RAW":
                            compareStr = responseBody.getRawBody().getValue();
                            break;
                        default:
                            break;
                    }
                    Assertions.assertEquals(returnStr, compareStr);
                }
            }

        }

    }

    private boolean isNotGetTypeMethod(String methodType) {
        return !StringUtils.equalsAnyIgnoreCase(methodType, HttpMethodConstants.GET.name(), HttpMethodConstants.DELETE.name(), HttpMethodConstants.OPTIONS.name(), HttpMethodConstants.HEAD.name());
    }

    private void initMockConfigTestData() throws Exception {
        if (MapUtils.isEmpty(METHOD_API_MAP)) {
            this.uploadTempFile();
        }

        //为METHOD_API_MAP每个api创建一个mock期望，用于做mockServer的测试
        for (Map.Entry<String, ApiDefinition> apiDefinitionEntry : METHOD_API_MAP.entrySet()) {
            String method = apiDefinitionEntry.getKey();
            ApiDefinition apiDefinition = apiDefinitionEntry.getValue();
            ApiDefinitionBlob apiDefinitionBlob = apiDefinitionBlobMapper.selectByPrimaryKey(apiDefinition.getId());

            List<ApiDefinitionMock> mockList = new ArrayList<>();
            //rest全匹配       返回接口定义的响应
            {
                ApiDefinitionMockAddRequest mockServerRequest = new ApiDefinitionMockAddRequest();
                mockServerRequest.setName("Mock_" + method + "_Rest_Full_Match1");
                mockServerRequest.setProjectId(apiDefinition.getProjectId());
                mockServerRequest.setApiDefinitionId(apiDefinition.getId());
                mockServerRequest.setMockMatchRule(mockServerTestService.genMockMatchRule("Rest_Full_Match1", false, true, null, true));
                mockServerRequest.setResponse(mockServerTestService.genMockResponse(null, 444, "Rest_Full_Match1", uploadFileId, null, apiDefinitionBlob));
                MvcResult mockServerResult = this.requestPostWithOkAndReturn(ADD, mockServerRequest);
                ApiDefinitionMock definitionMock = getResultData(mockServerResult, ApiDefinitionMock.class);
                mockServerTestService.assertAddApiDefinitionMock(mockServerRequest, mockServerRequest.getMockMatchRule(), definitionMock.getId());
                mockList.add(definitionMock);
            }

            //rest全匹配   返回本地上传的文件（send by body)
            {
                String mockFileMatch2Id = doUploadTempFile(mockServerTestService.getMockMultipartFile("mockFileMatch2.txt", "mockFileMatch2"));

                ApiDefinitionMockAddRequest mockServerRequest = new ApiDefinitionMockAddRequest();
                mockServerRequest.setName("Mock_" + method + "_Rest_Full_Match2");
                mockServerRequest.setProjectId(apiDefinition.getProjectId());
                mockServerRequest.setApiDefinitionId(apiDefinition.getId());
                mockServerRequest.setMockMatchRule(mockServerTestService.genMockMatchRule("Rest_Full_Match2", false, true, null, false));
                mockServerRequest.setResponse(mockServerTestService.genMockResponse("file-body", 200, "Rest_Full_Match2", mockFileMatch2Id, "mockFileMatch2.txt", null));

                mockServerRequest.setUploadFileIds(List.of(mockFileMatch2Id));

                MvcResult mockServerResult = this.requestPostWithOkAndReturn(ADD, mockServerRequest);
                ApiDefinitionMock definitionMock = getResultData(mockServerResult, ApiDefinitionMock.class);
                mockServerTestService.assertAddApiDefinitionMock(mockServerRequest, mockServerRequest.getMockMatchRule(), definitionMock.getId());
                mockList.add(definitionMock);


            }

            //rest全匹配   返回本地上传的文件（send by download)
            {
                String mockFileMatch3Id = doUploadTempFile(mockServerTestService.getMockMultipartFile("mockFileMatch3.txt", "mockFileMatch3"));

                ApiDefinitionMockAddRequest mockServerRequest = new ApiDefinitionMockAddRequest();
                mockServerRequest.setName("Mock_" + method + "_Rest_Full_Match3");
                mockServerRequest.setProjectId(apiDefinition.getProjectId());
                mockServerRequest.setApiDefinitionId(apiDefinition.getId());
                mockServerRequest.setMockMatchRule(mockServerTestService.genMockMatchRule("Rest_Full_Match3", false, true, null, false));
                mockServerRequest.setResponse(mockServerTestService.genMockResponse("file", 200, "Rest_Full_Match3", mockFileMatch3Id, "mockFileMatch3.txt", null));

                mockServerRequest.setUploadFileIds(List.of(mockFileMatch3Id));

                MvcResult mockServerResult = this.requestPostWithOkAndReturn(ADD, mockServerRequest);
                ApiDefinitionMock definitionMock = getResultData(mockServerResult, ApiDefinitionMock.class);
                mockServerTestService.assertAddApiDefinitionMock(mockServerRequest, mockServerRequest.getMockMatchRule(), definitionMock.getId());
                mockList.add(definitionMock);
            }

            //rest全匹配   返回文件管理的文件（send by download)
            {
                ApiDefinitionMockAddRequest mockServerRequest = new ApiDefinitionMockAddRequest();
                mockServerRequest.setName("Mock_" + method + "_Rest_Full_Match4");
                mockServerRequest.setProjectId(apiDefinition.getProjectId());
                mockServerRequest.setApiDefinitionId(apiDefinition.getId());
                mockServerRequest.setMockMatchRule(mockServerTestService.genMockMatchRule("Rest_Full_Match4", false, true, null, false));
                mockServerRequest.setResponse(mockServerTestService.genMockResponse("file", 200, "Rest_Full_Match4", fileMetadataId, "fileMetadata.txt", null));
                mockServerRequest.setLinkFileIds(List.of(fileMetadataId));

                MvcResult mockServerResult = this.requestPostWithOkAndReturn(ADD, mockServerRequest);
                ApiDefinitionMock definitionMock = getResultData(mockServerResult, ApiDefinitionMock.class);
                mockServerTestService.assertAddApiDefinitionMock(mockServerRequest, mockServerRequest.getMockMatchRule(), definitionMock.getId());
                mockList.add(definitionMock);
            }

            //header全匹配 尝试返回body-xml
            {
                ApiDefinitionMockAddRequest mockServerRequest = new ApiDefinitionMockAddRequest();
                mockServerRequest.setName("Mock_" + method + "_Header_Full_Match");
                mockServerRequest.setProjectId(apiDefinition.getProjectId());
                mockServerRequest.setApiDefinitionId(apiDefinition.getId());
                mockServerRequest.setMockMatchRule(mockServerTestService.genMockMatchRule("Header_Full_Match", false, false, null, true));
                mockServerRequest.setResponse(mockServerTestService.genMockResponse("xml", 201, "Header_Full_Match", uploadFileId, null, null));
                MvcResult mockServerResult = this.requestPostWithOkAndReturn(ADD, mockServerRequest);
                ApiDefinitionMock definitionMock = getResultData(mockServerResult, ApiDefinitionMock.class);
                mockServerTestService.assertAddApiDefinitionMock(mockServerRequest, mockServerRequest.getMockMatchRule(), definitionMock.getId());
                mockList.add(definitionMock);
            }

            //header半匹配 尝试返回body-json
            {
                ApiDefinitionMockAddRequest mockServerRequest = new ApiDefinitionMockAddRequest();
                mockServerRequest.setName("Mock_" + method + "_Header_Half_Match");
                mockServerRequest.setProjectId(apiDefinition.getProjectId());
                mockServerRequest.setApiDefinitionId(apiDefinition.getId());
                mockServerRequest.setMockMatchRule(mockServerTestService.genMockMatchRule("Header_Half_Match", false, false, null, false));
                mockServerRequest.setResponse(mockServerTestService.genMockResponse("json", 202, "Header_Half_Match", uploadFileId, null, null));
                MvcResult mockServerResult = this.requestPostWithOkAndReturn(ADD, mockServerRequest);
                ApiDefinitionMock definitionMock = getResultData(mockServerResult, ApiDefinitionMock.class);
                mockServerTestService.assertAddApiDefinitionMock(mockServerRequest, mockServerRequest.getMockMatchRule(), definitionMock.getId());
                mockList.add(definitionMock);
            }

            //query全匹配
            {
                ApiDefinitionMockAddRequest mockServerRequest = new ApiDefinitionMockAddRequest();
                mockServerRequest.setName("Mock_" + method + "_Query_Full_Match");
                mockServerRequest.setProjectId(apiDefinition.getProjectId());
                mockServerRequest.setApiDefinitionId(apiDefinition.getId());
                mockServerRequest.setMockMatchRule(mockServerTestService.genMockMatchRule("Query_Full_Match", true, true, null, true));
                mockServerRequest.setResponse(mockServerTestService.genMockResponse("raw", 203, "Query_Full_Match", uploadFileId, null, null));
                MvcResult mockServerResult = this.requestPostWithOkAndReturn(ADD, mockServerRequest);
                ApiDefinitionMock definitionMock = getResultData(mockServerResult, ApiDefinitionMock.class);
                mockServerTestService.assertAddApiDefinitionMock(mockServerRequest, mockServerRequest.getMockMatchRule(), definitionMock.getId());
                mockList.add(definitionMock);
            }

            //query半匹配
            {
                ApiDefinitionMockAddRequest mockServerRequest = new ApiDefinitionMockAddRequest();
                mockServerRequest.setName("Mock_" + method + "_Query_Half_Match");
                mockServerRequest.setProjectId(apiDefinition.getProjectId());
                mockServerRequest.setApiDefinitionId(apiDefinition.getId());
                mockServerRequest.setMockMatchRule(mockServerTestService.genMockMatchRule("Query_Half_Match", true, true, null, false));
                mockServerRequest.setResponse(mockServerTestService.genMockResponse("raw", 204, "Query_Half_Match", uploadFileId, null, null));
                MvcResult mockServerResult = this.requestPostWithOkAndReturn(ADD, mockServerRequest);
                ApiDefinitionMock definitionMock = getResultData(mockServerResult, ApiDefinitionMock.class);
                mockServerTestService.assertAddApiDefinitionMock(mockServerRequest, mockServerRequest.getMockMatchRule(), definitionMock.getId());
                mockList.add(definitionMock);
            }

            //body-kv 全匹配
            {
                ApiDefinitionMockAddRequest mockServerRequest = new ApiDefinitionMockAddRequest();
                mockServerRequest.setName("Mock_" + method + "_Body-kv_Full_Match");
                mockServerRequest.setProjectId(apiDefinition.getProjectId());
                mockServerRequest.setApiDefinitionId(apiDefinition.getId());
                mockServerRequest.setMockMatchRule(mockServerTestService.genMockMatchRule("Body-kv_Full_Match", false, true, "kv", true));
                mockServerRequest.setResponse(mockServerTestService.genMockResponse("raw", 204, "Body-kv_Full_Match", uploadFileId, null, null));
                MvcResult mockServerResult = this.requestPostWithOkAndReturn(ADD, mockServerRequest);
                ApiDefinitionMock definitionMock = getResultData(mockServerResult, ApiDefinitionMock.class);
                mockServerTestService.assertAddApiDefinitionMock(mockServerRequest, mockServerRequest.getMockMatchRule(), definitionMock.getId());
                mockList.add(definitionMock);
            }
            //body-kv 半匹配
            {
                ApiDefinitionMockAddRequest mockServerRequest = new ApiDefinitionMockAddRequest();
                mockServerRequest.setName("Mock_" + method + "_Body-kv_Half_Match");
                mockServerRequest.setProjectId(apiDefinition.getProjectId());
                mockServerRequest.setApiDefinitionId(apiDefinition.getId());
                mockServerRequest.setMockMatchRule(mockServerTestService.genMockMatchRule("Body-kv_Half_Match", false, true, "kv", false));
                mockServerRequest.setResponse(mockServerTestService.genMockResponse("raw", 204, "Body-kv_Half_Match", uploadFileId, null, null));
                MvcResult mockServerResult = this.requestPostWithOkAndReturn(ADD, mockServerRequest);
                ApiDefinitionMock definitionMock = getResultData(mockServerResult, ApiDefinitionMock.class);
                mockServerTestService.assertAddApiDefinitionMock(mockServerRequest, mockServerRequest.getMockMatchRule(), definitionMock.getId());
                mockList.add(definitionMock);
            }
            //body-kv 半匹配x-www-form-urlencoded
            {
                ApiDefinitionMockAddRequest mockServerRequest = new ApiDefinitionMockAddRequest();
                mockServerRequest.setName("Mock_" + method + "_Body-kv-x-www_Half_Match");
                mockServerRequest.setProjectId(apiDefinition.getProjectId());
                mockServerRequest.setApiDefinitionId(apiDefinition.getId());
                mockServerRequest.setMockMatchRule(mockServerTestService.genMockMatchRule("Body-kv-x-www_Half_Match", false, true, "kv", false));
                mockServerRequest.setResponse(mockServerTestService.genMockResponse("raw", 204, "Body-kv-x-www_Half_Match", uploadFileId, null, null));
                MvcResult mockServerResult = this.requestPostWithOkAndReturn(ADD, mockServerRequest);
                ApiDefinitionMock definitionMock = getResultData(mockServerResult, ApiDefinitionMock.class);
                mockServerTestService.assertAddApiDefinitionMock(mockServerRequest, mockServerRequest.getMockMatchRule(), definitionMock.getId());
                mockList.add(definitionMock);
            }

            //body-json包含匹配
            {
                ApiDefinitionMockAddRequest mockServerRequest = new ApiDefinitionMockAddRequest();
                mockServerRequest.setName("Mock_" + method + "_Body-json_Half_Match");
                mockServerRequest.setProjectId(apiDefinition.getProjectId());
                mockServerRequest.setApiDefinitionId(apiDefinition.getId());
                mockServerRequest.setMockMatchRule(mockServerTestService.genMockMatchRule("Body-json_Half_Match", false, true, "json", false));
                mockServerRequest.setResponse(mockServerTestService.genMockResponse("raw", 204, "Body-json_Half_Match", uploadFileId, null, null));
                MvcResult mockServerResult = this.requestPostWithOkAndReturn(ADD, mockServerRequest);
                ApiDefinitionMock definitionMock = getResultData(mockServerResult, ApiDefinitionMock.class);
                mockServerTestService.assertAddApiDefinitionMock(mockServerRequest, mockServerRequest.getMockMatchRule(), definitionMock.getId());
                mockList.add(definitionMock);
            }
            //body-xml包含匹配
            {
                ApiDefinitionMockAddRequest mockServerRequest = new ApiDefinitionMockAddRequest();
                mockServerRequest.setName("Mock_" + method + "_Body-xml_Half_Match");
                mockServerRequest.setProjectId(apiDefinition.getProjectId());
                mockServerRequest.setApiDefinitionId(apiDefinition.getId());
                mockServerRequest.setMockMatchRule(mockServerTestService.genMockMatchRule("Body-xml_Half_Match", false, true, "xml", false));
                mockServerRequest.setResponse(mockServerTestService.genMockResponse("raw", 204, "Body-xml_Half_Match", uploadFileId, null, null));
                MvcResult mockServerResult = this.requestPostWithOkAndReturn(ADD, mockServerRequest);
                ApiDefinitionMock definitionMock = getResultData(mockServerResult, ApiDefinitionMock.class);
                mockServerTestService.assertAddApiDefinitionMock(mockServerRequest, mockServerRequest.getMockMatchRule(), definitionMock.getId());
                mockList.add(definitionMock);
            }
            //raw包含匹配
            {
                ApiDefinitionMockAddRequest mockServerRequest = new ApiDefinitionMockAddRequest();
                mockServerRequest.setName("Mock_" + method + "_Body-raw_Half_Match");
                mockServerRequest.setProjectId(apiDefinition.getProjectId());
                mockServerRequest.setApiDefinitionId(apiDefinition.getId());
                mockServerRequest.setMockMatchRule(mockServerTestService.genMockMatchRule("body-raw_Half_Match", false, true, "raw", false));
                mockServerRequest.setResponse(mockServerTestService.genMockResponse("raw", 204, "body-raw_Half_Match", uploadFileId, null, null));
                MvcResult mockServerResult = this.requestPostWithOkAndReturn(ADD, mockServerRequest);
                ApiDefinitionMock definitionMock = getResultData(mockServerResult, ApiDefinitionMock.class);
                mockServerTestService.assertAddApiDefinitionMock(mockServerRequest, mockServerRequest.getMockMatchRule(), definitionMock.getId());
                mockList.add(definitionMock);
            }

            API_MOCK_MAP.put(apiDefinition, mockList);
        }
    }

    public static String getFileMD5(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(bytes, 0, bytes.length);
            BigInteger bigInt = new BigInteger(1, digest.digest());
            return bigInt.toString(16);
        } catch (Exception e) {
            return null;
        }
    }
}
