package io.metersphere.api.controller;

import io.metersphere.api.constants.ApiConstants;
import io.metersphere.api.constants.ApiDefinitionDocType;
import io.metersphere.api.constants.ApiDefinitionStatus;
import io.metersphere.api.constants.ApiImportPlatform;
import io.metersphere.api.controller.result.ApiResultCode;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.ApiFile;
import io.metersphere.api.dto.ReferenceRequest;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.request.ApiEditPosRequest;
import io.metersphere.api.dto.request.ApiTransferRequest;
import io.metersphere.api.dto.request.ImportRequest;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.mapper.*;
import io.metersphere.api.model.CheckLogModel;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.api.service.BaseFileManagementTestService;
import io.metersphere.api.service.definition.ApiDefinitionService;
import io.metersphere.api.service.definition.ApiTestCaseService;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.dto.filemanagement.FileInfo;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.project.service.FileAssociationService;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.*;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.controller.handler.result.MsHttpResultCode;
import io.metersphere.system.domain.OperationHistory;
import io.metersphere.system.domain.OperationHistoryExample;
import io.metersphere.system.dto.request.OperationHistoryRequest;
import io.metersphere.system.dto.request.OperationHistoryVersionRequest;
import io.metersphere.system.dto.sdk.BaseCondition;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.OperationHistoryMapper;
import io.metersphere.system.service.UserLoginService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ApiDefinitionControllerTests extends BaseTest {

    private static final String BASE_PATH = "/api/definition/";
    private final static String ADD = "add";
    private final static String UPDATE = "update";
    private final static String BATCH_UPDATE = "batch-update";
    private final static String DELETE_TO_GC = "delete-to-gc/{0}?deleteAllVersion={1}";
    private final static String SINGLE_DELETE_TO_GC = "delete-to-gc/";
    private final static String BATCH_DELETE_TO_GC = "batch/delete-to-gc";
    private final static String COPY = "copy";
    private final static String BATCH_MOVE = "batch-move";

    private final static String RESTORE = "recover";
    private final static String BATCH_RESTORE = "batch-recover";

    private final static String DELETE = "delete/{0}";
    private final static String BATCH_DELETE = "batch/delete";

    private final static String PAGE = "page";
    private final static String PAGE_DOC = "page-doc";
    private final static String DOC = "doc";
    private static final String GET = "get-detail/";
    private static final String FOLLOW = "follow/";
    private static final String VERSION = "version/";
    private static final String OPERATION_HISTORY = "operation-history";
    private static final String OPERATION_HISTORY_RECOVER = "operation-history/recover";
    private static final String OPERATION_HISTORY_SAVE = "operation-history/save";
    private static final String UPLOAD_TEMP_FILE = "upload/temp/file";
    private static final String DEBUG = "debug";
    private static final String IMPORT = "import";

    private static final String DEFAULT_MODULE_ID = "10001";

    private static final String ALL_API = "api_definition_module.api.all";
    private static final String UNPLANNED_API = "api_unplanned_request";
    private static ApiDefinition apiDefinition;

    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiDefinitionBlobMapper apiDefinitionBlobMapper;

    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;

    @Resource
    private ApiDefinitionFollowerMapper apiDefinitionFollowerMapper;

    @Resource
    private ExtBaseProjectVersionMapper extBaseProjectVersionMapper;

    @Resource
    private ApiFileResourceService apiFileResourceService;

    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;

    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;

    @Resource
    private ApiDefinitionModuleMapper apiDefinitionModuleMapper;

    @Resource
    private ExtApiDefinitionCustomFieldMapper extApiDefinitionCustomFieldMapper;

    @Resource
    private OperationHistoryMapper operationHistoryMapper;
    @Resource
    private BaseFileManagementTestService baseFileManagementTestService;
    @Resource
    private ApiCommonService apiCommonService;
    @Resource
    private ApiFileResourceMapper apiFileResourceMapper;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ApiScenarioStepMapper apiScenarioStepMapper;
    @Resource
    private UserLoginService userLoginService;
    private static String fileMetadataId;
    private static String uploadFileId;

    private static final List<CheckLogModel> checkLogModelList = new ArrayList<>();


    @Override
    public String getBasePath() {
        return BASE_PATH;
    }

    @Test
    @Order(0)
    public void uploadTempFile() throws Exception {
        // 准备数据，上传文件管理文件
        MockMultipartFile file = new MockMultipartFile("file", IDGenerator.nextStr() + "_file_upload.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, "aa".getBytes());
        fileMetadataId = baseFileManagementTestService.upload(file);
        // @@请求成功
        String fileId = doUploadTempFile(file);

        // 校验文件存在
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFolder(DefaultRepositoryDir.getSystemTempDir() + "/" + fileId);
        fileRequest.setFileName(file.getOriginalFilename());
        Assertions.assertNotNull(FileCenter.getDefaultRepository().getFile(fileRequest));

        requestUploadPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_ADD, UPLOAD_TEMP_FILE, file);
        requestUploadPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_UPDATE, UPLOAD_TEMP_FILE, file);
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

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_api_definition.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testAdd() throws Exception {
        LogUtils.info("create api test");
        // 创建测试数据
        ApiDefinitionAddRequest request = createApiDefinitionAddRequest();
        MsHTTPElement msHttpElement = MsHTTPElementTest.getMsHttpElement();
        msHttpElement.setBody(ApiDebugControllerTests.addBodyLinkFile(msHttpElement.getBody(), fileMetadataId));
        request.setRequest(getMsElementParam(msHttpElement));
        List<HttpResponse> msHttpResponse = MsHTTPElementTest.getMsHttpResponse();
        msHttpResponse.get(0).setBody(ApiDebugControllerTests.addBodyLinkFile(msHttpResponse.get(0).getBody(), fileMetadataId));
        request.setResponse(msHttpResponse);

        uploadFileId = doUploadTempFile(getMockMultipartFile("api-add-file_upload.JPG"));
        request.setUploadFileIds(List.of(uploadFileId));
        request.setLinkFileIds(List.of(fileMetadataId));

        // 执行方法调用
        MvcResult mvcResult = this.requestPostWithOkAndReturn(ADD, request);
        // 校验请求成功数据
        ApiDefinition resultData = getResultData(mvcResult, ApiDefinition.class);
        apiDefinition = assertAddApiDefinition(request, msHttpElement, resultData.getId());
        assertUploadFile(apiDefinition.getId(), List.of(uploadFileId));
        assertLinkFile(apiDefinition.getId());

        this.requestGetWithOk("transfer/options/" + DEFAULT_PROJECT_ID);
        ApiTransferRequest apiTransferRequest = new ApiTransferRequest();
        apiTransferRequest.setSourceId(apiDefinition.getId());
        apiTransferRequest.setProjectId(DEFAULT_PROJECT_ID);
        apiTransferRequest.setModuleId("root");
        apiTransferRequest.setLocal(true);
        String uploadFileId = doUploadTempFile(getMockMultipartFile("api-file_upload.JPG"));
        apiTransferRequest.setFileId(uploadFileId);
        apiTransferRequest.setFileName(StringUtils.EMPTY);
        apiTransferRequest.setOriginalName("api-file_upload.JPG");
        this.requestPost("transfer", apiTransferRequest).andExpect(status().isOk());
        //文件不存在
        apiTransferRequest.setFileId("111");
        this.requestPost("transfer", apiTransferRequest).andExpect(status().is5xxServerError());
        //文件已经上传
        ApiFileResourceExample apiFileResourceExample = new ApiFileResourceExample();
        apiFileResourceExample.createCriteria().andResourceIdEqualTo(apiDefinition.getId());
        List<ApiFileResource> apiFileResources = apiFileResourceMapper.selectByExample(apiFileResourceExample);
        Assertions.assertFalse(apiFileResources.isEmpty());
        apiTransferRequest.setFileId(apiFileResources.get(0).getFileId());
        apiTransferRequest.setFileName("test-file_upload");
        apiTransferRequest.setOriginalName("test-file_upload.JPG");
        this.requestPost("transfer", apiTransferRequest).andExpect(status().isOk());

        // 再插入一条数据，便于修改时重名校验
        request.setMethod("GET");
        request.setPath("/api/admin/posts");
        request.setUploadFileIds(null);
        request.setLinkFileIds(null);
        String versionId = request.getVersionId();
        request.setVersionId(null);
        mvcResult = this.requestPostWithOkAndReturn(ADD, request);
        resultData = getResultData(mvcResult, ApiDefinition.class);
        request.setVersionId(versionId);
        assertAddApiDefinition(request, msHttpElement, resultData.getId());

        testHandleFileAssociationUpgrade();

        // @@重名校验异常
        assertErrorCode(this.requestPost(ADD, request), ApiResultCode.API_DEFINITION_EXIST);

        // 校验其他协议
        request.setProtocol("TCP");
        request.setMethod(null);
        request.setPath(null);
        this.requestPostWithOk(ADD, request);

        // @@响应名+响应码唯一校验异常
        request.setName("test123-response");
        request.setMethod("GET");
        request.setPath("/api/halo/posts");
        List<HttpResponse> mergedList = new ArrayList<>();
        List<HttpResponse> msHttpResponse1 = MsHTTPElementTest.getMsHttpResponse();
        mergedList.addAll(msHttpResponse);
        mergedList.addAll(msHttpResponse1);
        request.setResponse(mergedList);
        assertErrorCode(this.requestPost(ADD, request), ApiResultCode.API_RESPONSE_NAME_CODE_UNIQUE);

        // 校验项目是否存在
        request.setProjectId("111");
        request.setName("test123");
        assertErrorCode(this.requestPost(ADD, request), MsHttpResultCode.NOT_FOUND);

        // @@校验日志
        checkLogModelList.add(new CheckLogModel(apiDefinition.getId(), OperationLogType.ADD, ADD));

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_PATH + "transfer", apiTransferRequest)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();

        // @@校验权限
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setName("permission-st-6");
        request.setMethod("DELETE");
        request.setPath("/api/admin/posts");
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_ADD, ADD, request);
    }


    public void testHandleFileAssociationUpgrade() throws Exception {
        List<ApiFile> originApiFiles = getApiFiles(fileMetadataId);
        MockMultipartFile file = new MockMultipartFile("file", "file_upload.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, "aa".getBytes());
        // 重新上传新文件
        String newFileId = baseFileManagementTestService.reUpload(fileMetadataId, file);
        // 更新关联的文件到最新文件
        baseFileManagementTestService.upgrade(fileMetadataId, apiDefinition.getId());
        // 校验文件是否替换
        Assertions.assertEquals(originApiFiles.size(), getApiFiles(newFileId).size());
        fileMetadataId = newFileId;
    }

    private List<ApiFile> getApiFiles(String fileId) {
        ApiDefinitionBlob apiDefinitionBlob = apiDefinitionBlobMapper.selectByPrimaryKey(apiDefinition.getId());
        AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(new String(apiDefinitionBlob.getRequest()), AbstractMsTestElement.class);
        List<ApiFile> apiFiles = apiCommonService.getApiFilesByFileId(fileId, msTestElement);
        List<HttpResponse> httpResponses = ApiDataUtils.parseArray(new String(apiDefinitionBlob.getRequest()), HttpResponse.class);
        for (HttpResponse httpResponse : httpResponses) {
            List<ApiFile> responseFiles = apiCommonService.getApiBodyFiles(httpResponse.getBody())
                    .stream()
                    .filter(file -> StringUtils.equals(fileId, file.getFileId()))
                    .toList();
            apiFiles.addAll(responseFiles);
        }
        return apiFiles;
    }

    private Object getMsElementParam(MsHTTPElement msHTTPElement) {
        return JSON.parseObject(ApiDataUtils.toJSONString(msHTTPElement));
    }

    public static ApiDefinitionAddRequest createApiDefinitionAddRequest() {
        ExtBaseProjectVersionMapper extBaseProjectVersionMapper = CommonBeanFactory.getBean(ExtBaseProjectVersionMapper.class);
        // 创建并返回一个 ApiDefinitionAddRequest 对象，用于测试
        String defaultVersion = extBaseProjectVersionMapper.getDefaultVersion(DEFAULT_PROJECT_ID);
        ApiDefinitionAddRequest request = new ApiDefinitionAddRequest();
        request.setName("接口定义test");
        request.setProtocol(ApiConstants.HTTP_PROTOCOL);
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setMethod("POST");
        request.setPath("/api/admin/posts");
        request.setStatus(ApiDefinitionStatus.PROCESSING.name());
        request.setModuleId("default");
        request.setVersionId(defaultVersion);
        request.setDescription("描述内容");
        request.setTags(new LinkedHashSet<>(List.of("tag1", "tag2")));
        List<ApiDefinitionCustomField> customFields = createCustomFields();
        request.setCustomFields(customFields);
        return request;
    }

    private static List<ApiDefinitionCustomField> createCustomFields() {
        List<ApiDefinitionCustomField> list = new ArrayList<>();
        ApiDefinitionCustomField customField = new ApiDefinitionCustomField();
        customField.setFieldId("custom-field");
        customField.setValue("oasis");
        list.add(customField);
        ApiDefinitionCustomField customField2 = new ApiDefinitionCustomField();
        customField2.setFieldId("test_field");
        customField2.setValue(JSON.toJSONString(List.of("test")));
        list.add(customField2);
        return list;
    }

    private ApiDefinition assertAddApiDefinition(Object request, MsHTTPElement msHttpElement, String id) {
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(id);
        ApiDefinitionBlob apiDefinitionBlob = apiDefinitionBlobMapper.selectByPrimaryKey(id);
        ApiDefinition copyApiDefinition = BeanUtils.copyBean(new ApiDefinition(), apiDefinition);
        BeanUtils.copyBean(copyApiDefinition, request);
        Assertions.assertEquals(apiDefinition, copyApiDefinition);
        if (apiDefinitionBlob != null) {
            Assertions.assertEquals(msHttpElement, ApiDataUtils.parseObject(new String(apiDefinitionBlob.getRequest()), AbstractMsTestElement.class));
        }
        return apiDefinition;
    }

    @Test
    @Order(2)
    public void get() throws Exception {
        if (apiDefinition == null) {
            apiDefinition = apiDefinitionMapper.selectByPrimaryKey("1001");
        }
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOkAndReturn(GET + apiDefinition.getId());
        ApiDefinitionDTO apiDefinitionDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResult).get("data")), ApiDefinitionDTO.class);
        // 校验数据是否正确
        ApiDefinitionDTO copyApiDefinitionDTO = BeanUtils.copyBean(new ApiDefinitionDTO(), apiDefinition);
        ApiDefinitionBlob apiDefinitionBlob = apiDefinitionBlobMapper.selectByPrimaryKey(apiDefinition.getId());
        ApiDefinitionFollowerExample example = new ApiDefinitionFollowerExample();
        example.createCriteria().andApiDefinitionIdEqualTo(apiDefinition.getId()).andUserIdEqualTo("admin");
        List<ApiDefinitionFollower> followers = apiDefinitionFollowerMapper.selectByExample(example);
        copyApiDefinitionDTO.setFollow(CollectionUtils.isNotEmpty(followers));
        Set<String> userIds = List.of(apiDefinitionDTO).stream()
                .flatMap(apiDefinition -> Stream.of(apiDefinition.getUpdateUser(), apiDefinition.getDeleteUser(), apiDefinition.getCreateUser()))
                .collect(Collectors.toSet());
        Map<String, String> userMap = userLoginService.getUserNameMap(new ArrayList<>(userIds));
        copyApiDefinitionDTO.setCreateUserName(userMap.get(apiDefinitionDTO.getCreateUser()));
        copyApiDefinitionDTO.setUpdateUserName(userMap.get(apiDefinitionDTO.getUpdateUser()));
        List<ApiDefinitionCustomFieldDTO> customFields = extApiDefinitionCustomFieldMapper.getApiCustomFields(Collections.singletonList(apiDefinition.getId()), apiDefinition.getProjectId());
        if (!customFields.isEmpty()) {
            Map<String, List<ApiDefinitionCustomFieldDTO>> customFieldMap = customFields.stream().collect(Collectors.groupingBy(ApiDefinitionCustomFieldDTO::getApiId));
            copyApiDefinitionDTO.setCustomFields(customFieldMap.get(apiDefinition.getId()));
        }
        if (apiDefinitionBlob != null) {
            AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(new String(apiDefinitionBlob.getRequest()), AbstractMsTestElement.class);
            apiCommonService.setLinkFileInfo(apiDefinition.getId(), msTestElement);
            MsHTTPElement msHTTPElement = (MsHTTPElement) msTestElement;
            msHTTPElement.setMethod(apiDefinition.getMethod());
            msHTTPElement.setPath(apiDefinition.getPath());
            msHTTPElement.setModuleId(apiDefinition.getModuleId());
            msHTTPElement.setNum(apiDefinition.getNum());
            copyApiDefinitionDTO.setRequest(msTestElement);
            List<HttpResponse> httpResponses = ApiDataUtils.parseArray(new String(apiDefinitionBlob.getResponse()), HttpResponse.class);
            for (HttpResponse httpResponse : httpResponses) {
                apiCommonService.setLinkFileInfo(apiDefinition.getId(), httpResponse.getBody());
            }
            copyApiDefinitionDTO.setResponse(httpResponses);
        }

        MsHTTPElement msHTTPElement = (MsHTTPElement) apiDefinitionDTO.getRequest();
        Assertions.assertEquals(msHTTPElement.getMethod(), apiDefinition.getMethod());
        Assertions.assertEquals(msHTTPElement.getPath(), apiDefinition.getPath());
        Assertions.assertEquals(msHTTPElement.getModuleId(), apiDefinition.getModuleId());
        Assertions.assertEquals(msHTTPElement.getNum(), apiDefinition.getNum());

        Assertions.assertEquals(apiDefinitionDTO, copyApiDefinitionDTO);

        assertErrorCode(this.requestGet(GET + "111"), ApiResultCode.API_DEFINITION_NOT_EXIST);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_READ, GET + apiDefinition.getId());
    }

    @Test
    @Order(3)
    @Sql(scripts = {"/dml/init_api_definition.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testUpdate() throws Exception {
        LogUtils.info("update api test");
        if (apiDefinition == null) {
            apiDefinition = apiDefinitionMapper.selectByPrimaryKey("1001");
        }
        ApiDefinitionUpdateRequest request = new ApiDefinitionUpdateRequest();
        BeanUtils.copyBean(request, apiDefinition);
        request.setPath("test.com/admin/test");
        request.setName("test1test1test1test1test1test1");
        request.setMethod("POST");
        request.setModuleId("default1");
        request.setTags(new LinkedHashSet<>(List.of("tag1", "tag2-update")));
        request.setCustomFields(updateCustomFields());
        MsHTTPElement msHttpElement = MsHTTPElementTest.getMsHttpElement();
        request.setRequest(getMsElementParam(msHttpElement));
        List<HttpResponse> msHttpResponse = MsHTTPElementTest.getMsHttpResponse();
        request.setResponse(msHttpResponse);

        // 清除文件的更新
        request.setUnLinkFileIds(List.of(fileMetadataId));
        request.setDeleteFileIds(List.of(uploadFileId));
        this.requestPostWithOk(UPDATE, request);
        // 校验请求成功数据
        apiDefinition = assertAddApiDefinition(request, msHttpElement, request.getId());
        assertUploadFile(apiDefinition.getId(), List.of());


        // 带文件的更新
        String fileId = doUploadTempFile(getMockMultipartFile("file_upload.JPG"));
        request.setUploadFileIds(List.of(fileId));
        request.setLinkFileIds(List.of(fileMetadataId));
        request.setDeleteFileIds(null);
        request.setUnLinkFileIds(null);
        this.requestPostWithOk(UPDATE, request);
        // 校验请求成功数据
        apiDefinition = assertAddApiDefinition(request, msHttpElement, request.getId());
        assertUploadFile(apiDefinition.getId(), List.of(fileId));
        assertLinkFile(apiDefinition.getId());

        // 删除了上一次上传的文件，重新上传一个文件
        request.setDeleteFileIds(List.of(fileId));
        String newFileId1 = doUploadTempFile(getMockMultipartFile("file_upload.JPG"));
        request.setUploadFileIds(List.of(newFileId1));
        request.setUnLinkFileIds(List.of(fileMetadataId));
        request.setLinkFileIds(List.of(fileMetadataId));
        this.requestPostWithOk(UPDATE, request);
        apiDefinition = assertAddApiDefinition(request, msHttpElement, request.getId());
        assertUploadFile(apiDefinition.getId(), List.of(newFileId1));
        assertLinkFile(apiDefinition.getId());

        // 已有一个文件，再上传一个文件
        String newFileId2 = doUploadTempFile(getMockMultipartFile("file_update_upload.JPG"));
        request.setUploadFileIds(List.of(newFileId2));
        request.setUnLinkFileIds(null);
        request.setDeleteFileIds(null);
        request.setLinkFileIds(null);
        this.requestPostWithOk(UPDATE, request);
        apiDefinition = assertAddApiDefinition(request, msHttpElement, request.getId());
        assertUploadFile(apiDefinition.getId(), List.of(newFileId1, newFileId2));
        assertLinkFile(apiDefinition.getId());

        // 单独更新状态
        ApiDefinitionUpdateRequest updateStatusRequest = new ApiDefinitionUpdateRequest();
        updateStatusRequest.setId(apiDefinition.getId());
        updateStatusRequest.setStatus(ApiDefinitionStatus.DONE.name());
        this.requestPostWithOk(UPDATE, updateStatusRequest);
        apiDefinition = apiDefinitionMapper.selectByPrimaryKey(updateStatusRequest.getId());
        Assertions.assertEquals(apiDefinition.getStatus(), ApiDefinitionStatus.DONE.name());

        // @@重名校验异常
        ApiDefinitionUpdateRequest repeatRequest = new ApiDefinitionUpdateRequest();
        repeatRequest.setId(apiDefinition.getId());
        repeatRequest.setPath("/api/admin/posts");
        repeatRequest.setMethod("GET");
        assertErrorCode(this.requestPost(UPDATE, repeatRequest), ApiResultCode.API_DEFINITION_EXIST);

        // @@响应名+响应码唯一校验异常
        request.setName("test123-response");
        request.setMethod("GET");
        request.setPath("/api/halo/posts");
        List<HttpResponse> mergedList = new ArrayList<>();
        List<HttpResponse> msHttpResponse1 = MsHTTPElementTest.getMsHttpResponse();
        mergedList.addAll(msHttpResponse);
        mergedList.addAll(msHttpResponse1);
        request.setResponse(mergedList);
        assertErrorCode(this.requestPost(UPDATE, request), ApiResultCode.API_RESPONSE_NAME_CODE_UNIQUE);

        // 校验数据是否存在
        request.setId("111");
        request.setName("test123");
        assertErrorCode(this.requestPost(UPDATE, request), ApiResultCode.API_DEFINITION_NOT_EXIST);

        // @@校验日志
        checkLogModelList.add(new CheckLogModel(apiDefinition.getId(), OperationLogType.UPDATE, UPDATE));

        //校验修改path和method时，是否会影响用例
        ApiDefinitionAddRequest addRequest = new ApiDefinitionAddRequest();
        addRequest.setName("测试修改path和method");
        addRequest.setProtocol(ApiConstants.HTTP_PROTOCOL);
        addRequest.setProjectId(DEFAULT_PROJECT_ID);
        addRequest.setMethod("POST");
        addRequest.setPath("/api/admin/posts");
        addRequest.setStatus(ApiDefinitionStatus.PROCESSING.name());
        addRequest.setModuleId("default");
        addRequest.setVersionId(DEFAULT_PROJECT_ID);
        addRequest.setDescription("描述内容");
        addRequest.setTags(new LinkedHashSet<>(List.of("tag1", "tag2")));
        addRequest.setCustomFields(new ArrayList<>());
        addRequest.setRequest(getMsElementParam(msHttpElement));
        addRequest.setResponse(msHttpResponse);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(ADD, addRequest);
        ApiDefinition apiDefinition = getResultData(mvcResult, ApiDefinition.class);
        ApiDefinition apiPathAndMethod = apiDefinitionMapper.selectByPrimaryKey(apiDefinition.getId());
        Assertions.assertEquals(addRequest.getPath(), apiPathAndMethod.getPath());
        Assertions.assertEquals(addRequest.getMethod(), apiPathAndMethod.getMethod());
        ApiDefinitionUpdateRequest updateRequest = new ApiDefinitionUpdateRequest();
        BeanUtils.copyBean(updateRequest, apiPathAndMethod);
        updateRequest.setPath("/api/test/path/method");
        updateRequest.setRequest(getMsElementParam(msHttpElement));
        updateRequest.setResponse(msHttpResponse);
        updateRequest.setMethod("GET");
        this.requestPostWithOk(UPDATE, updateRequest);
        //增加用例
        for (int i = 0; i < 3; i++) {
            ApiTestCaseAddRequest testCaseAddRequest = new ApiTestCaseAddRequest();
            testCaseAddRequest.setApiDefinitionId(apiPathAndMethod.getId());
            testCaseAddRequest.setName("test-path" + i);
            testCaseAddRequest.setProjectId(DEFAULT_PROJECT_ID);
            testCaseAddRequest.setPriority("P0");
            testCaseAddRequest.setStatus(ApiDefinitionStatus.PROCESSING.name());
            testCaseAddRequest.setTags(new LinkedHashSet<>(List.of("tag1", "tag2")));
            testCaseAddRequest.setRequest(getMsElementParam(msHttpElement));
            apiTestCaseService.addCase(testCaseAddRequest, "admin");
        }
        updateRequest.setPath("/api/test/path/method/case");
        this.requestPostWithOk(UPDATE, updateRequest);

        // @@校验权限
        request.setId(apiDefinition.getId());
        request.setName("permission-st-6");
        request.setModuleId("module-st-6");
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_UPDATE, UPDATE, request);
    }

    @Test
    @Order(4)
    public void debug() throws Exception {
        ApiDefinitionRunRequest request = new ApiDefinitionRunRequest();
        request.setId(apiDefinition.getId());
        MsHTTPElement msHTTPElement = new MsHTTPElement();
        msHTTPElement.setPath("/test");
        msHTTPElement.setMethod("GET");
        request.setRequest(JSON.parseObject(ApiDataUtils.toJSONString(msHTTPElement)));
        request.setReportId(IDGenerator.nextStr());
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setPath("/test");
        request.setMethod("GET");
        MvcResult mvcResult = this.requestPostAndReturn(DEBUG, request);
        ResultHolder resultHolder = JSON.parseObject(mvcResult.getResponse().getContentAsString(Charset.defaultCharset()), ResultHolder.class);
        Assertions.assertTrue(resultHolder.getCode() == ApiResultCode.RESOURCE_POOL_EXECUTE_ERROR.getCode() ||
                resultHolder.getCode() == MsHttpResultCode.SUCCESS.getCode());

        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_EXECUTE, DEBUG, request);
    }

    private List<ApiDefinitionCustomField> updateCustomFields() {
        List<ApiDefinitionCustomField> list = new ArrayList<>();
        ApiDefinitionCustomField customField = new ApiDefinitionCustomField();
        customField.setFieldId("custom-field");
        customField.setValue("oasis-update");
        list.add(customField);
        ApiDefinitionCustomField customField2 = new ApiDefinitionCustomField();
        customField2.setFieldId("test_field");
        customField2.setValue(JSON.toJSONString(List.of("test-update")));
        list.add(customField2);
        return list;
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
            assert apiFileResourceService != null;
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
     *
     * @param id
     */
    private static void assertLinkFile(String id) {
        FileAssociationService fileAssociationService = CommonBeanFactory.getBean(FileAssociationService.class);
        assert fileAssociationService != null;
        List<String> linkFileIds = fileAssociationService.getFiles(id)
                .stream()
                .map(FileInfo::getFileId)
                .toList();
        Assertions.assertFalse(linkFileIds.isEmpty());
    }

    @Test
    @Order(4)
    @Sql(scripts = {"/dml/init_api_definition.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testBatchUpdate() throws Exception {
        LogUtils.info("batch update api test");
        ApiDefinitionBatchUpdateRequest apiDefinitionBatchUpdateRequest = new ApiDefinitionBatchUpdateRequest();
        apiDefinitionBatchUpdateRequest.setProjectId(DEFAULT_PROJECT_ID);
        apiDefinitionBatchUpdateRequest.setSelectIds(List.of("1001", "1002", "1005"));
        apiDefinitionBatchUpdateRequest.setExcludeIds(List.of("1005"));
        apiDefinitionBatchUpdateRequest.setSelectAll(false);
        apiDefinitionBatchUpdateRequest.setProtocols(List.of("HTTP"));
        apiDefinitionBatchUpdateRequest.setType("tags");
        // 修改标签，追加
        apiDefinitionBatchUpdateRequest.setSelectIds(List.of("1001", "1002"));
        apiDefinitionBatchUpdateRequest.setTags(new LinkedHashSet<>(List.of("tag-append", "tag-append1")));
        apiDefinitionBatchUpdateRequest.setAppend(true);
        this.requestPostWithOk(BATCH_UPDATE, apiDefinitionBatchUpdateRequest);
        assertBatchUpdateApiDefinition(apiDefinitionBatchUpdateRequest, List.of("1001", "1002"));
        // 修改标签，覆盖
        apiDefinitionBatchUpdateRequest.setSelectIds(List.of("1003", "1004"));
        apiDefinitionBatchUpdateRequest.setTags(new LinkedHashSet<>(List.of("tag-append", "tag-append1")));
        apiDefinitionBatchUpdateRequest.setAppend(false);
        this.requestPostWithOk(BATCH_UPDATE, apiDefinitionBatchUpdateRequest);
        assertBatchUpdateApiDefinition(apiDefinitionBatchUpdateRequest, List.of("1003", "1004"));
        // 自定义字段覆盖
        apiDefinitionBatchUpdateRequest.setType("customs");
        apiDefinitionBatchUpdateRequest.setSelectIds(List.of("1002", "1003", "1004"));
        ApiDefinitionCustomFieldDTO field = new ApiDefinitionCustomFieldDTO();
        field.setId("test_field");
        field.setValue(JSON.toJSONString(List.of("test1-batch")));
        apiDefinitionBatchUpdateRequest.setCustomField(field);
        apiDefinitionBatchUpdateRequest.setAppend(false);
        this.requestPostWithOk(BATCH_UPDATE, apiDefinitionBatchUpdateRequest);
        // 修改协议类型
        apiDefinitionBatchUpdateRequest.setType("method");
        apiDefinitionBatchUpdateRequest.setMethod("batch-method");
        this.requestPostWithOk(BATCH_UPDATE, apiDefinitionBatchUpdateRequest);
        // 修改状态
        apiDefinitionBatchUpdateRequest.setType("status");
        apiDefinitionBatchUpdateRequest.setStatus(ApiDefinitionStatus.DEBUGGING.name());
        this.requestPostWithOk(BATCH_UPDATE, apiDefinitionBatchUpdateRequest);
        // 修改版本
        apiDefinitionBatchUpdateRequest.setType("version");
        apiDefinitionBatchUpdateRequest.setVersionId("batch-version");
        this.requestPostWithOk(BATCH_UPDATE, apiDefinitionBatchUpdateRequest);
        // 修改全部
        apiDefinitionBatchUpdateRequest.setSelectAll(true);
        BaseCondition baseCondition = new BaseCondition();
        baseCondition.setKeyword("st-6");
        apiDefinitionBatchUpdateRequest.setCondition(baseCondition);
        this.requestPostWithOk(BATCH_UPDATE, apiDefinitionBatchUpdateRequest);
        // 校验项目是否存在
        apiDefinitionBatchUpdateRequest.setProjectId("111");
        apiDefinitionBatchUpdateRequest.setMethod("test123");

        assertErrorCode(this.requestPost(BATCH_UPDATE, apiDefinitionBatchUpdateRequest), MsHttpResultCode.NOT_FOUND);

        // @@校验日志
        String[] ids = {"1001", "1002", "1003", "1004"};
        for (String id : ids) {
            checkLogModelList.add(new CheckLogModel(id, OperationLogType.UPDATE, BATCH_UPDATE));
        }
        // @@异常参数校验
        createdGroupParamValidateTest(ApiDefinitionBatchRequest.class, BATCH_UPDATE);
        // @@校验权限
        apiDefinitionBatchUpdateRequest.setProjectId(DEFAULT_PROJECT_ID);
        apiDefinitionBatchUpdateRequest.setMethod("permission");
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_UPDATE, BATCH_UPDATE, apiDefinitionBatchUpdateRequest);
    }

    private void assertBatchUpdateApiDefinition(ApiDefinitionBatchUpdateRequest request, List<String> ids) {
        ApiDefinitionExample apiDefinitionExample = new ApiDefinitionExample();
        apiDefinitionExample.createCriteria().andIdIn(ids);
        List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(apiDefinitionExample);
        apiDefinitions.forEach(item -> {
            if (request.getStatus() != null) {
                Assertions.assertEquals(item.getStatus(), request.getStatus());
            }
            if (request.getMethod() != null) {
                Assertions.assertEquals(item.getMethod(), request.getMethod());
            }
            if (request.getVersionId() != null) {
                Assertions.assertEquals(item.getVersionId(), request.getVersionId());
            }
            if (request.getTags() != null) {
                if (request.isAppend()) {
                    Assertions.assertTrue(item.getTags().containsAll(request.getTags()));
                } else {
                    Assertions.assertTrue(item.getTags().containsAll(request.getTags()));
                }
            }
        });
    }

    @Test
    @Order(5)
    public void copy() throws Exception {
        LogUtils.info("copy api test");
        ApiDefinitionCopyRequest request = new ApiDefinitionCopyRequest();
        request.setId(apiDefinition.getId());
        MvcResult mvcResult = this.requestPostWithOkAndReturn(COPY, request);
        ApiDefinition resultData = getResultData(mvcResult, ApiDefinition.class);
        // @数据验证
        List<ApiFileResource> sourceFiles = apiFileResourceService.getByResourceId(apiDefinition.getId());
        List<ApiFileResource> copyFiles = apiFileResourceService.getByResourceId(resultData.getId());
        if (!sourceFiles.isEmpty() && !copyFiles.isEmpty()) {
            Assertions.assertEquals(sourceFiles.size(), copyFiles.size());
        }
        Assertions.assertTrue(resultData.getName().contains("copy_"));
        // @@校验日志
        checkLogModelList.add(new CheckLogModel(resultData.getId(), OperationLogType.UPDATE, COPY));

        request.setId("1001");
        MvcResult mvcResultCopy = this.requestPostWithOkAndReturn(COPY, request);
        ApiDefinition resultDataCopy = getResultData(mvcResultCopy, ApiDefinition.class);
        // @数据验证
        Assertions.assertTrue(resultDataCopy.getName().contains("copy_"));

        request.setId("121");
        assertErrorCode(this.requestPost(COPY, request), ApiResultCode.API_DEFINITION_NOT_EXIST);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_UPDATE, COPY, request);
    }

    @Test
    @Order(6)
    public void batchMove() throws Exception {
        LogUtils.info("batch move api test");
        ApiDefinitionBatchMoveRequest request = new ApiDefinitionBatchMoveRequest();
        request.setModuleId(DEFAULT_MODULE_ID);
        request.setProjectId(DEFAULT_PROJECT_ID);

        // 移动选中
        request.setSelectIds(List.of("1001", "1002", "1005"));
        request.setExcludeIds(List.of("1005"));
        request.setSelectAll(false);
        request.setProtocols(List.of("HTTP"));
        this.requestPostWithOkAndReturn(BATCH_MOVE, request);

        // 移动全部 条件为关键字为st-6的数据
        request.setSelectAll(true);
        BaseCondition baseCondition = new BaseCondition();
        baseCondition.setKeyword("st-6");
        request.setCondition(baseCondition);
        this.requestPostWithOkAndReturn(BATCH_MOVE, request);
        // @@校验日志
        //checkLogModelList.add(new CheckLogModel("1006", OperationLogType.UPDATE, BATCH_MOVE));
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_UPDATE, BATCH_MOVE, request);
    }


    @Test
    @Order(7)
    public void follow() throws Exception {
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey("1006");
        // @@关注
        // @@请求成功
        this.requestGetWithOk(FOLLOW + apiDefinition.getId());
        ApiDefinitionFollowerExample example = new ApiDefinitionFollowerExample();
        example.createCriteria().andApiDefinitionIdEqualTo(apiDefinition.getId()).andUserIdEqualTo("admin");
        List<ApiDefinitionFollower> followers = apiDefinitionFollowerMapper.selectByExample(example);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(followers));
        // @@校验日志
        checkLogModelList.add(new CheckLogModel(apiDefinition.getId(), OperationLogType.UPDATE, FOLLOW + apiDefinition.getId()));

        assertErrorCode(this.requestGet(FOLLOW + "111"), ApiResultCode.API_DEFINITION_NOT_EXIST);

        // @@取消关注
        // @@请求成功
        this.requestGetWithOk(FOLLOW + apiDefinition.getId());
        ApiDefinitionFollowerExample unFollowerExample = new ApiDefinitionFollowerExample();
        example.createCriteria().andApiDefinitionIdEqualTo(apiDefinition.getId()).andUserIdEqualTo("admin");
        List<ApiDefinitionFollower> unFollowers = apiDefinitionFollowerMapper.selectByExample(unFollowerExample);
        Assertions.assertTrue(CollectionUtils.isEmpty(unFollowers));
        // @@校验日志
        checkLogModelList.add(new CheckLogModel(apiDefinition.getId(), OperationLogType.UPDATE, FOLLOW + apiDefinition.getId()));
        assertErrorCode(this.requestGet(FOLLOW + "111"), ApiResultCode.API_DEFINITION_NOT_EXIST);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_UPDATE, FOLLOW + apiDefinition.getId());
    }

    @Test
    @Order(8)
    public void version() throws Exception {
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey("1001");
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOk(VERSION + apiDefinition.getId()).andReturn();
        List<ApiDefinitionVersionDTO> apiDefinitionVersionDTO = getResultDataArray(mvcResult, ApiDefinitionVersionDTO.class);
        // 校验数据是否正确
        List<ApiDefinitionVersionDTO> copyApiDefinitionVersionDTO = extApiDefinitionMapper.getApiDefinitionByRefId(apiDefinition.getRefId());
        Assertions.assertEquals(apiDefinitionVersionDTO, copyApiDefinitionVersionDTO);
        assertErrorCode(this.requestGet(VERSION + "111"), ApiResultCode.API_DEFINITION_NOT_EXIST);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_READ, VERSION + apiDefinition.getId());
    }

    @Test
    @Order(9)
    @Sql(scripts = {"/dml/init_api_definition.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void getPage() throws Exception {
        assertPateDate(doApiDefinitionPage("All", PAGE));
        assertPateDate(doApiDefinitionPage("KEYWORD", PAGE));
        //doApiDefinitionPage("FILTER", PAGE);
        assertPateDate(doApiDefinitionPage("COMBINE", PAGE));
        assertPateDate(doApiDefinitionPage("DELETED", PAGE));
        ApiDefinitionPageRequest request = new ApiDefinitionPageRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setCurrent(1);
        request.setPageSize(10);
        request.setDeleted(false);
        request.setSort(Map.of("createTime", "asc"));
        this.requestPostWithOkAndReturn(PAGE, request);
    }

    private void assertPateDate(Pager pageData) {
        List<ApiDefinitionDTO> apiDefinitions = ApiDataUtils.parseArray(JSON.toJSONString(pageData.getList()), ApiDefinitionDTO.class);
        if (CollectionUtils.isNotEmpty(apiDefinitions)) {
            ApiDefinitionDTO apiDefinitionDTO = apiDefinitions.get(0);
            // 判断用例数是否正确
            ApiTestCaseExample example = new ApiTestCaseExample();
            example.createCriteria()
                    .andApiDefinitionIdEqualTo(apiDefinitionDTO.getId())
                    .andDeletedEqualTo(false);
            List<ApiTestCase> apiTestCases = apiTestCaseMapper.selectByExample(example);
            Assertions.assertEquals(apiDefinitionDTO.getCaseTotal(), apiTestCases.size());
            // 判断模块名是否正确
            ApiDefinitionModule apiDefinitionModule = apiDefinitionModuleMapper.selectByPrimaryKey(apiDefinitionDTO.getModuleId());
            if (apiDefinitionModule == null) {
                Assertions.assertEquals(apiDefinitionDTO.getModuleName(), Translator.get("api_unplanned_request"));
            } else {
                Assertions.assertEquals(apiDefinitionDTO.getModuleName(), apiDefinitionModule.getName());
            }
        }
    }

    private Pager doApiDefinitionPage(String search, String url) throws Exception {
        ApiDefinitionPageRequest request = new ApiDefinitionPageRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setCurrent(1);
        request.setPageSize(10);
        request.setProtocols(List.of("HTTP"));
        request.setDeleted(false);
        request.setSort(Map.of("createTime", "asc"));
        // "ALL", "KEYWORD", "FILTER", "COMBINE", "DELETED"
        switch (search) {
            case "ALL" -> configureAllSearch(request);
            case "KEYWORD" -> configureKeywordSearch(request);
            case "FILTER" -> configureFilterSearch(request);
            case "COMBINE" -> configureCombineSearch(request);
            case "DELETED" -> configureDeleteSearch(request);
            default -> {
            }
        }

        MvcResult mvcResult = this.requestPostWithOkAndReturn(url, request);
        // 返回请求正常
        Pager pageData = getResultData(mvcResult, Pager.class);
        // 返回值不为空
        Assertions.assertNotNull(pageData);
        // 返回值的页码和当前页码相同
        Assertions.assertEquals(pageData.getCurrent(), request.getCurrent());
        // 返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(pageData.getList())).size() <= request.getPageSize());
        return pageData;
    }

    private void configureAllSearch(ApiDefinitionPageRequest request) {
        request.setKeyword("100");
        Map<String, List<String>> filters = new HashMap<>();
        filters.put("status", Arrays.asList("Underway", "Completed"));
        filters.put("method", List.of("GET"));
        filters.put("version_id", List.of("1005704995741369851"));
        request.setFilter(filters);

        Map<String, Object> map = new HashMap<>();
        map.put("name", Map.of("operator", "like", "value", "test-1"));
        map.put("method", Map.of("operator", "in", "value", Arrays.asList("GET", "POST")));
        request.setCombine(map);
    }

    private void configureKeywordSearch(ApiDefinitionPageRequest request) {
        request.setKeyword("100");
        request.setSort(Map.of("status", "asc"));
        request.setVersionId("100570499574136985");
    }

    private void configureFilterSearch(ApiDefinitionPageRequest request) {
        Map<String, List<String>> filters = new HashMap<>();
        request.setSort(Map.of());
        filters.put("status", Arrays.asList("Underway", "Completed"));
        filters.put("method", List.of("GET"));
        filters.put("version_id", List.of("1005704995741369851"));
        filters.put("custom_multiple_custom-field", List.of("oasis"));
        request.setFilter(filters);
    }

    private void configureCombineSearch(ApiDefinitionPageRequest request) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", Map.of("operator", "like", "value", "test"));
        map.put("method", Map.of("operator", "in", "value", Arrays.asList("GET", "POST")));
        map.put("createUser", Map.of("operator", "current user", "value", StringUtils.EMPTY));
        List<Map<String, Object>> customs = new ArrayList<>();
        Map<String, Object> custom = new HashMap<>();
        custom.put("id", "test_field");
        custom.put("operator", "in");
        custom.put("type", "MULTIPLE_SELECT");
        custom.put("value", JSON.toJSONString(List.of("test", "default")));
        customs.add(custom);
        Map<String, Object> currentUserCustom = new HashMap<>();
        currentUserCustom.put("id", "test_field");
        currentUserCustom.put("operator", "current user");
        currentUserCustom.put("type", "MULTIPLE_MEMBER");
        currentUserCustom.put("value", "current user");
        customs.add(currentUserCustom);
        map.put("customs", customs);

        request.setCombine(map);
    }

    private void configureDeleteSearch(ApiDefinitionPageRequest request) {
        request.setKeyword("100");
        request.setVersionId("100570499574136985");
        request.setDeleted(true);
    }

    @Test
    @Order(10)
    public void getPageDoc() throws Exception {
        doApiDefinitionPage("All", PAGE_DOC);
        doApiDefinitionPage("KEYWORD", PAGE_DOC);
        doApiDefinitionPage("FILTER", PAGE_DOC);
        doApiDefinitionPage("COMBINE", PAGE_DOC);
        doApiDefinitionPage("DELETED", PAGE_DOC);
    }

    @Test
    @Order(11)
    public void getDoc() throws Exception {
        ApiDefinitionDocRequest request = new ApiDefinitionDocRequest();
        apiDefinition = apiDefinitionMapper.selectByPrimaryKey("1001");
        request.setApiId(apiDefinition.getId());
        request.setProjectId(DEFAULT_PROJECT_ID);
        this.requestPostWithOkAndReturn(DOC, request);
        request.setProtocols(List.of("HTTP"));
        request.setType(ApiDefinitionDocType.API.name());
        // @@请求成功
        MvcResult mvcResult = this.requestPostWithOkAndReturn(DOC, request);
        ApiDefinitionDocDTO apiDefinitionDocDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResult).get("data")), ApiDefinitionDocDTO.class);
        // 校验数据是否正确
        ApiDefinitionDocDTO copyApiDefinitionDocDTO = new ApiDefinitionDocDTO();
        ApiDefinitionDTO copyApiDefinitionDTO = BeanUtils.copyBean(new ApiDefinitionDTO(), apiDefinition);
        ApiDefinitionBlob apiDefinitionBlob = apiDefinitionBlobMapper.selectByPrimaryKey(apiDefinition.getId());
        if (apiDefinitionBlob != null) {
            copyApiDefinitionDTO.setRequest(ApiDataUtils.parseObject(new String(apiDefinitionBlob.getRequest()), AbstractMsTestElement.class));
            copyApiDefinitionDTO.setResponse(ApiDataUtils.parseArray(new String(apiDefinitionBlob.getResponse()), HttpResponse.class));
        }
        copyApiDefinitionDocDTO.setDocTitle(apiDefinition.getName());
        copyApiDefinitionDocDTO.setType(ApiDefinitionDocType.API.name());
        copyApiDefinitionDocDTO.setDocInfo(copyApiDefinitionDTO);
        Assertions.assertEquals(apiDefinitionDocDTO.getType(), copyApiDefinitionDocDTO.getType());
        Assertions.assertEquals(apiDefinitionDocDTO.getDocTitle(), copyApiDefinitionDocDTO.getDocTitle());
        Assertions.assertEquals(apiDefinitionDocDTO.getDocInfo().getId(), copyApiDefinitionDocDTO.getDocInfo().getId());

        request.setApiId("111");
        assertErrorCode(this.requestPost(DOC, request), ApiResultCode.API_DEFINITION_NOT_EXIST);

        // @@模块查看文档
        request.setApiId(null);
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setType(ApiDefinitionDocType.MODULE.name());
        request.setModuleIds(List.of("10001"));
        MvcResult mvcResultModule = this.requestPostWithOkAndReturn(DOC, request);
        ApiDefinitionDocDTO moduleApiDefinitionDocDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResultModule).get("data")), ApiDefinitionDocDTO.class);
        // 校验数据是否正确
        ApiDefinitionDocDTO copyModuleApiDefinitionDocDTO = new ApiDefinitionDocDTO();
        List<ApiDefinitionDTO> list = extApiDefinitionMapper.listDoc(request);
        if (null != list) {
            ApiDefinitionDTO first = list.stream().findFirst().orElseThrow(() -> new MSException(ApiResultCode.API_DEFINITION_NOT_EXIST));
            ApiDefinitionBlob moduleApiDefinitionBlob = apiDefinitionBlobMapper.selectByPrimaryKey(first.getId());
            if (moduleApiDefinitionBlob != null) {
                first.setRequest(ApiDataUtils.parseObject(new String(moduleApiDefinitionBlob.getRequest()), AbstractMsTestElement.class));
                first.setResponse(ApiDataUtils.parseArray(new String(moduleApiDefinitionBlob.getResponse()), HttpResponse.class));
            }
            ApiDefinitionModule apiDefinitionModule = apiDefinitionModuleMapper.selectByPrimaryKey(first.getModuleId());
            if (apiDefinitionModule != null && StringUtils.isNotBlank(apiDefinitionModule.getName())) {
                copyModuleApiDefinitionDocDTO.setDocTitle(apiDefinitionModule.getName());
            } else {
                copyModuleApiDefinitionDocDTO.setDocTitle(Translator.get(UNPLANNED_API));
            }
            copyModuleApiDefinitionDocDTO.setDocInfo(first);
            copyModuleApiDefinitionDocDTO.setType(ApiDefinitionDocType.MODULE.name());
        }

        Assertions.assertEquals(moduleApiDefinitionDocDTO.getType(), copyModuleApiDefinitionDocDTO.getType());
        Assertions.assertEquals(moduleApiDefinitionDocDTO.getDocTitle(), copyModuleApiDefinitionDocDTO.getDocTitle());
        Assertions.assertEquals(moduleApiDefinitionDocDTO.getDocInfo().getId(), copyModuleApiDefinitionDocDTO.getDocInfo().getId());

        // @@查看全部文档
        request.setApiId(null);
        request.setModuleIds(null);
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setType(ApiDefinitionDocType.ALL.name());
        MvcResult mvcResultAll = this.requestPostWithOkAndReturn(DOC, request);
        ApiDefinitionDocDTO allApiDefinitionDocDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResultAll).get("data")), ApiDefinitionDocDTO.class);
        // 校验数据是否正确
        ApiDefinitionDocDTO copyAllApiDefinitionDocDTO = new ApiDefinitionDocDTO();
        List<ApiDefinitionDTO> allList = extApiDefinitionMapper.listDoc(request);
        if (null != allList) {
            ApiDefinitionDTO info = allList.stream().findFirst().orElseThrow(() -> new MSException(ApiResultCode.API_DEFINITION_NOT_EXIST));
            ApiDefinitionBlob allApiDefinitionBlob = apiDefinitionBlobMapper.selectByPrimaryKey(info.getId());
            if (allApiDefinitionBlob != null) {
                info.setRequest(ApiDataUtils.parseObject(new String(allApiDefinitionBlob.getRequest()), AbstractMsTestElement.class));
                info.setResponse(ApiDataUtils.parseArray(new String(allApiDefinitionBlob.getResponse()), HttpResponse.class));
            }
            if (StringUtils.isBlank(copyAllApiDefinitionDocDTO.getDocTitle())) {
                copyAllApiDefinitionDocDTO.setDocTitle(Translator.get(ALL_API));
            }
            copyAllApiDefinitionDocDTO.setType(ApiDefinitionDocType.ALL.name());
            copyAllApiDefinitionDocDTO.setDocInfo(info);
        }

        Assertions.assertEquals(allApiDefinitionDocDTO.getType(), copyAllApiDefinitionDocDTO.getType());
        Assertions.assertEquals(allApiDefinitionDocDTO.getDocTitle(), copyAllApiDefinitionDocDTO.getDocTitle());
        Assertions.assertEquals(allApiDefinitionDocDTO.getDocInfo().getId(), copyAllApiDefinitionDocDTO.getDocInfo().getId());

        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_DOC_SHARE, DOC, request);
    }

    @Test
    @Order(11)
    public void getDocModuleIsNull() throws Exception {
        ApiDefinitionDocRequest request = new ApiDefinitionDocRequest();
        // @@模块查看文档
        request.setApiId(null);
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setType(ApiDefinitionDocType.MODULE.name());
        request.setModuleIds(List.of("1001001"));
        this.requestPostWithOkAndReturn(DOC, request);
        request.setProtocols(List.of("HTTP"));
        MvcResult mvcResultModule = this.requestPostWithOkAndReturn(DOC, request);
        ApiDefinitionDocDTO moduleApiDefinitionDocDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResultModule).get("data")), ApiDefinitionDocDTO.class);
        // 校验数据是否正确
        ApiDefinitionDocDTO copyModuleApiDefinitionDocDTO = new ApiDefinitionDocDTO();
        List<ApiDefinitionDTO> list = extApiDefinitionMapper.listDoc(request);
        if (null != list) {
            ApiDefinitionDTO first = list.stream().findFirst().orElseThrow(() -> new MSException(ApiResultCode.API_DEFINITION_NOT_EXIST));
            ApiDefinitionBlob moduleApiDefinitionBlob = apiDefinitionBlobMapper.selectByPrimaryKey(first.getId());
            if (moduleApiDefinitionBlob != null) {
                first.setRequest(ApiDataUtils.parseObject(new String(moduleApiDefinitionBlob.getRequest()), AbstractMsTestElement.class));
                first.setResponse(ApiDataUtils.parseArray(new String(moduleApiDefinitionBlob.getResponse()), HttpResponse.class));
            }
            ApiDefinitionModule apiDefinitionModule = apiDefinitionModuleMapper.selectByPrimaryKey(first.getModuleId());
            if (apiDefinitionModule != null && StringUtils.isNotBlank(apiDefinitionModule.getName())) {
                copyModuleApiDefinitionDocDTO.setDocTitle(apiDefinitionModule.getName());
            } else {
                copyModuleApiDefinitionDocDTO.setDocTitle(Translator.get(UNPLANNED_API));
            }
            copyModuleApiDefinitionDocDTO.setDocInfo(first);
            copyModuleApiDefinitionDocDTO.setType(ApiDefinitionDocType.MODULE.name());
        }

        Assertions.assertEquals(moduleApiDefinitionDocDTO.getType(), copyModuleApiDefinitionDocDTO.getType());
        Assertions.assertEquals(moduleApiDefinitionDocDTO.getDocTitle(), copyModuleApiDefinitionDocDTO.getDocTitle());
        Assertions.assertEquals(moduleApiDefinitionDocDTO.getDocInfo().getId(), copyModuleApiDefinitionDocDTO.getDocInfo().getId());
    }

    @Test
    @Order(12)
    public void testOperationHistoryList() throws Exception {
        OperationHistoryRequest request = new OperationHistoryRequest();
        apiDefinition = apiDefinitionMapper.selectByPrimaryKey("1001");
        request.setSourceId(apiDefinition.getId());
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setCurrent(1);
        request.setPageSize(10);
        request.setSort(Map.of("createTime", "asc"));

        MvcResult mvcResult = this.requestPostWithOkAndReturn(OPERATION_HISTORY, request);
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
        request.setSort(Map.of());
        this.requestPost(OPERATION_HISTORY, request);

    }

    @Test
    @Order(12)
    public void testOperationHistorySave() throws Exception {
        OperationHistoryVersionRequest operationHistoryVersionRequest = new OperationHistoryVersionRequest();
        apiDefinition = apiDefinitionMapper.selectByPrimaryKey("1002");
        OperationHistoryExample operationHistoryExample = new OperationHistoryExample();
        operationHistoryExample.createCriteria().andSourceIdEqualTo(apiDefinition.getId());
        operationHistoryExample.setOrderByClause("id DESC");
        OperationHistory operationHistory = operationHistoryMapper.selectByExample(operationHistoryExample).getFirst();
        operationHistoryVersionRequest.setId(operationHistory.getId());
        operationHistoryVersionRequest.setSourceId(apiDefinition.getId());
        operationHistoryVersionRequest.setVersionId(apiDefinition.getVersionId());
        this.requestPostWithOkAndReturn(OPERATION_HISTORY_SAVE, operationHistoryVersionRequest);
        checkLogModelList.add(new CheckLogModel(apiDefinition.getId(), OperationLogType.UPDATE, OPERATION_HISTORY_SAVE));
        OperationHistoryExample comparisonExample = new OperationHistoryExample();
        comparisonExample.createCriteria().andSourceIdEqualTo(apiDefinition.getId()).andRefIdEqualTo(operationHistory.getId()).andTypeEqualTo(OperationLogType.UPDATE.name());
        comparisonExample.setOrderByClause("id DESC");
        OperationHistory comparison = operationHistoryMapper.selectByExample(operationHistoryExample).getFirst();
        Assertions.assertNotNull(comparison);

        operationHistoryVersionRequest.setId(operationHistory.getId());
        operationHistoryVersionRequest.setSourceId("1002");
        operationHistoryVersionRequest.setVersionId("1002002002");
        this.requestPostWithOkAndReturn(OPERATION_HISTORY_SAVE, operationHistoryVersionRequest);
        checkLogModelList.add(new CheckLogModel(apiDefinition.getId(), OperationLogType.UPDATE, OPERATION_HISTORY_SAVE));
        OperationHistoryExample comparisonExampleNewVersion = new OperationHistoryExample();
        comparisonExampleNewVersion.createCriteria().andSourceIdEqualTo(apiDefinition.getId()).andRefIdEqualTo(operationHistory.getId()).andTypeEqualTo(OperationLogType.UPDATE.name());
        comparisonExampleNewVersion.setOrderByClause("id DESC");
        OperationHistory comparisonNewVersion = operationHistoryMapper.selectByExample(operationHistoryExample).getFirst();
        Assertions.assertNotNull(comparisonNewVersion);
    }

    @Test
    @Order(9)
    public void testPos() throws Exception {

        ApiEditPosRequest request = new ApiEditPosRequest();
        apiDefinition = apiDefinitionMapper.selectByPrimaryKey("1005");
        ApiDefinition apiDefinition1 = apiDefinitionMapper.selectByPrimaryKey("1004");
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setTargetId(apiDefinition.getId());
        request.setMoveId(apiDefinition.getId());
        request.setModuleId("root");
        request.setMoveMode("AFTER");
        this.requestPostWithOkAndReturn("edit/pos", request);
        request.setMoveId(apiDefinition1.getId());
        this.requestPostWithOkAndReturn("edit/pos", request);
        request.setMoveMode("BEFORE");
        this.requestPostWithOkAndReturn("edit/pos", request);

        request.setModuleId("module-st-6");
        requestPost("edit/pos", request).andExpect(status().is5xxServerError());
        apiDefinitionService.refreshPos(DEFAULT_PROJECT_ID);


    }

    @Test
    @Order(13)
    public void testOperationHistoryRecover() throws Exception {
        OperationHistoryVersionRequest operationHistoryVersionRequest = new OperationHistoryVersionRequest();
        apiDefinition = apiDefinitionMapper.selectByPrimaryKey("1002");
        OperationHistoryExample operationHistoryExample = new OperationHistoryExample();
        operationHistoryExample.createCriteria().andSourceIdEqualTo(apiDefinition.getId());
        operationHistoryExample.setOrderByClause("id DESC");
        OperationHistory operationHistory = operationHistoryMapper.selectByExample(operationHistoryExample).getFirst();
        operationHistoryVersionRequest.setId(operationHistory.getId());
        operationHistoryVersionRequest.setSourceId(apiDefinition.getId());
        operationHistoryVersionRequest.setVersionId(apiDefinition.getVersionId());
        this.requestPostWithOkAndReturn(OPERATION_HISTORY_RECOVER, operationHistoryVersionRequest);
        checkLogModelList.add(new CheckLogModel(apiDefinition.getId(), OperationLogType.RECOVER, OPERATION_HISTORY_RECOVER));
        OperationHistoryExample comparisonExample = new OperationHistoryExample();
        comparisonExample.createCriteria().andSourceIdEqualTo(apiDefinition.getId()).andRefIdEqualTo(operationHistory.getId()).andTypeEqualTo(OperationLogType.RECOVER.name());
        comparisonExample.setOrderByClause("id DESC");
        OperationHistory comparison = operationHistoryMapper.selectByExample(operationHistoryExample).getFirst();
        Assertions.assertNotNull(comparison);
    }

    @Test
    @Order(14)
    public void testDel() throws Exception {
        LogUtils.info("delete api test");
        apiDefinition = apiDefinitionMapper.selectByPrimaryKey("1001");
        // @只存在一个版本
        // @@请求成功
        this.requestGetWithOkAndReturn(DELETE_TO_GC, apiDefinition.getId(), false);
        checkLogModelList.add(new CheckLogModel(apiDefinition.getId(), OperationLogType.DELETE, DELETE_TO_GC.replace("{0}", apiDefinition.getId()).replace("{1}", "false")));
        ApiDefinition apiDefinitionInfo = apiDefinitionMapper.selectByPrimaryKey(apiDefinition.getId());
        Assertions.assertTrue(apiDefinitionInfo.getDeleted());
        Assertions.assertEquals("admin", apiDefinitionInfo.getDeleteUser());
        Assertions.assertNotNull(apiDefinitionInfo.getDeleteTime());

        this.requestGetWithOk(SINGLE_DELETE_TO_GC + "/" + apiDefinition.getId());

        // @存在多个版本
        String id = "1004";
        // 列表删除
        ApiDefinition delApiDefinition = apiDefinitionMapper.selectByPrimaryKey(id);
        MvcResult mvcResult = this.requestGetWithOk(VERSION + id).andReturn();
        List<ApiDefinitionVersionDTO> apiDefinitionVersionDTO = getResultDataArray(mvcResult, ApiDefinitionVersionDTO.class);
        if (!apiDefinitionVersionDTO.isEmpty()) {
            this.requestGetWithOkAndReturn(DELETE_TO_GC, id, false);
            // 效验数据
            // 删除的数据为最新版本需要更新最近一条数据为最新数据
            if (delApiDefinition.getLatest()) {
                ApiDefinitionExample example = new ApiDefinitionExample();
                example.createCriteria().andRefIdEqualTo(delApiDefinition.getRefId()).andDeletedEqualTo(false).andProjectIdEqualTo(delApiDefinition.getProjectId());
                example.setOrderByClause("update_time DESC");
                ApiDefinition updateApiDefinition = apiDefinitionMapper.selectByExample(example).stream().findFirst().orElse(null);
                if (updateApiDefinition != null) {
                    Assertions.assertTrue(updateApiDefinition.getLatest());
                    Assertions.assertFalse(updateApiDefinition.getDeleted());
                }
            }
            ApiDefinition delApiDefinitionInfo = apiDefinitionMapper.selectByPrimaryKey(id);
            if (delApiDefinitionInfo != null) {
                Assertions.assertTrue(delApiDefinitionInfo.getDeleted());
                Assertions.assertEquals("admin", delApiDefinitionInfo.getDeleteUser());
                Assertions.assertNotNull(delApiDefinitionInfo.getDeleteTime());
            }
            checkLogModelList.add(new CheckLogModel(id, OperationLogType.DELETE, DELETE_TO_GC.replace("{0}", id).replace("{1}", "false")));
        }
        // 全部删除
        id = "1002";
        // @@请求成功
        this.requestGetWithOkAndReturn(DELETE_TO_GC, id, true);

        List<String> ids = extApiDefinitionMapper.getApiDefinitionByRefId(id).stream().map(ApiDefinitionVersionDTO::getId).toList();
        ApiDefinitionExample apiDefinitionExample = new ApiDefinitionExample();
        apiDefinitionExample.createCriteria().andIdIn(ids);
        List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(apiDefinitionExample);
        if (CollectionUtils.isNotEmpty(apiDefinitions)) {
            apiDefinitions.forEach(item -> {
                Assertions.assertTrue(item.getDeleted());
                Assertions.assertEquals("admin", item.getDeleteUser());
                Assertions.assertNotNull(item.getDeleteTime());
            });
        }
        checkLogModelList.add(new CheckLogModel(id, OperationLogType.DELETE, DELETE_TO_GC.replace("{0}", id).replace("{1}", "true")));
        id = "121";
        assertErrorCode(this.requestGet(DELETE_TO_GC, id, false), ApiResultCode.API_DEFINITION_NOT_EXIST);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_DELETE, DELETE_TO_GC, apiDefinition.getId(), false);
    }

    @Test
    @Order(15)
    public void testBatchDel() throws Exception {
        LogUtils.info("batch delete api test");
        ApiDefinitionBatchDeleteRequest request = new ApiDefinitionBatchDeleteRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);

        // 删除选中
        request.setSelectIds(List.of("1004"));
        request.setDeleteAllVersion(false);
        request.setSelectAll(false);
        this.requestPostWithOkAndReturn(BATCH_DELETE_TO_GC, request);
        request.setProtocols(List.of("HTTP"));
        this.requestPostWithOkAndReturn(BATCH_DELETE_TO_GC, request);
        // @@校验日志
        checkLogModelList.add(new CheckLogModel("1004", OperationLogType.DELETE, BATCH_DELETE_TO_GC));

        request.setSelectIds(List.of("1002"));
        request.setDeleteAllVersion(false);
        request.setSelectAll(false);
        assertErrorCode(this.requestPost(BATCH_DELETE_TO_GC, request), ApiResultCode.API_DEFINITION_NOT_EXIST);
        // 删除全部 条件为关键字为st-6的数据
        request.setDeleteAllVersion(true);
        request.setExcludeIds(List.of("1005"));
        request.setSelectAll(true);
        BaseCondition baseCondition = new BaseCondition();
        baseCondition.setKeyword("st-6");
        request.setCondition(baseCondition);
        this.requestPostWithOkAndReturn(BATCH_DELETE_TO_GC, request);
        // @@校验日志
        checkLogModelList.add(new CheckLogModel("1006", OperationLogType.DELETE, BATCH_DELETE_TO_GC));
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_DELETE, BATCH_DELETE_TO_GC, request);
    }


    @Test
    @Order(16)
    public void testRecover() throws Exception {
        LogUtils.info("recover api test");
        apiDefinition = apiDefinitionMapper.selectByPrimaryKey("1001");
        // @恢复一条数据
        ApiDefinitionDeleteRequest apiDefinitionDeleteRequest = new ApiDefinitionDeleteRequest();
        apiDefinitionDeleteRequest.setId(apiDefinition.getId());
        apiDefinitionDeleteRequest.setProjectId(DEFAULT_PROJECT_ID);
        // @@请求成功
        this.requestPostWithOkAndReturn(RESTORE, apiDefinitionDeleteRequest);
        checkLogModelList.add(new CheckLogModel(apiDefinition.getId(), OperationLogType.RECOVER, RESTORE));
        ApiDefinition apiDefinitionInfo = apiDefinitionMapper.selectByPrimaryKey(apiDefinition.getId());
        Assertions.assertFalse(apiDefinitionInfo.getDeleted());
        Assertions.assertNull(apiDefinitionInfo.getDeleteUser());
        Assertions.assertNull(apiDefinitionInfo.getDeleteTime());

        // @查询恢复的数据版本是否为默认版本
        String defaultVersion = extBaseProjectVersionMapper.getDefaultVersion(apiDefinition.getProjectId());
        if (defaultVersion.equals(apiDefinition.getVersionId())) {
            // 需要处理此数据为最新标识
            // 此数据不为最新版本，验证是否更新为最新版本
            if (!apiDefinition.getLatest()) {
                Assertions.assertTrue(apiDefinitionInfo.getLatest());
            }
        }
        // 效验 关联数据
        List<ApiTestCase> caseLists = extApiTestCaseMapper.getCaseInfoByApiIds(Collections.singletonList(apiDefinition.getId()), false);
        if (!caseLists.isEmpty()) {
            caseLists.forEach(item -> {
                Assertions.assertNull(item.getDeleteUser());
                Assertions.assertNull(item.getDeleteTime());
            });
        }

        // @恢复一条数据
        apiDefinitionDeleteRequest.setId("111");
        // @@请求成功
        assertErrorCode(this.requestPost(RESTORE, apiDefinitionDeleteRequest), ApiResultCode.API_DEFINITION_NOT_EXIST);

        // @@校验权限
        apiDefinitionDeleteRequest.setId(apiDefinition.getId());
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_DELETE, RESTORE, apiDefinitionDeleteRequest);
    }

    @Test
    @Order(20)
    public void testBatchRecover() throws Exception {
        LogUtils.info("batch recover api test");
        ApiDefinitionBatchRequest request = new ApiDefinitionBatchRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        // 恢复选中
        request.setSelectIds(List.of("1002", "1004", "1005"));
        request.setExcludeIds(List.of("1005"));
        request.setSelectAll(false);
        this.requestPostWithOk(BATCH_RESTORE, request);
        request.setProtocols(List.of("HTTP"));
        this.requestPostWithOk(BATCH_RESTORE, request);


        // 效验数据结果
        ApiDefinitionExample apiDefinitionExample = new ApiDefinitionExample();
        apiDefinitionExample.createCriteria().andIdIn(request.getSelectIds());
        List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(apiDefinitionExample);
        if (!apiDefinitions.isEmpty()) {
            apiDefinitions.forEach(item -> {
                Assertions.assertFalse(item.getDeleted());
                Assertions.assertNull(item.getDeleteUser());
                Assertions.assertNull(item.getDeleteTime());
                // 效验 关联数据
                List<ApiTestCase> caseLists = extApiTestCaseMapper.getCaseInfoByApiIds(Collections.singletonList(item.getId()), false);
                if (!caseLists.isEmpty()) {
                    caseLists.forEach(test -> {
                        Assertions.assertNull(test.getDeleteUser());
                        Assertions.assertNull(test.getDeleteTime());
                    });
                }
            });
        }

        // 恢复全部 条件为关键字为st-6的数据
        request.setSelectAll(true);
        BaseCondition baseCondition = new BaseCondition();
        baseCondition.setKeyword("st-6");
        request.setCondition(baseCondition);
        this.requestPostWithOk(BATCH_RESTORE, request);

        // @@校验日志
        String[] ids = {"1002", "1004", "1006"};
        for (String id : ids) {
            checkLogModelList.add(new CheckLogModel(id, OperationLogType.RECOVER, BATCH_RESTORE));
        }
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_DELETE, BATCH_RESTORE, request);
    }

    @Test
    @Order(21)
    public void testTrashDel() throws Exception {
        LogUtils.info("trashDel api test");
        apiDefinition = apiDefinitionMapper.selectByPrimaryKey("1001");
        if (!apiDefinition.getDeleted()) {
            // @@请求成功
            this.requestGetWithOkAndReturn(DELETE_TO_GC, apiDefinition.getId(), false);
            apiDefinition = apiDefinitionMapper.selectByPrimaryKey(apiDefinition.getId());
            Assertions.assertTrue(apiDefinition.getDeleted());
            Assertions.assertEquals("admin", apiDefinition.getDeleteUser());
            Assertions.assertNotNull(apiDefinition.getDeleteTime());
        }
        // @只存在一个版本
        // @@请求成功
        this.requestGetWithOk(DELETE, apiDefinition.getId());
        checkLogModelList.add(new CheckLogModel(apiDefinition.getId(), OperationLogType.DELETE, DELETE.replace("{0}", apiDefinition.getId())));
        // 验证数据
        ApiDefinition apiDefinitionInfo = apiDefinitionMapper.selectByPrimaryKey(apiDefinition.getId());
        Assertions.assertNull(apiDefinitionInfo);

        // 文件是否删除
        List<ApiFileResource> apiFileResources = apiFileResourceService.getByResourceId(apiDefinition.getId());
        Assertions.assertEquals(0, apiFileResources.size());

        // 效验 关联数据
        List<ApiTestCase> caseLists = extApiTestCaseMapper.getCaseInfoByApiIds(Collections.singletonList(apiDefinition.getId()), false);
        Assertions.assertEquals(0, caseLists.size());

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_DELETE, DELETE, apiDefinition.getId());
    }

    @Test
    @Order(25)
    public void testBatchTrashDel() throws Exception {
        LogUtils.info("batch trash delete api test");
        ApiDefinitionBatchRequest request = new ApiDefinitionBatchRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);

        // 删除选中
        request.setSelectIds(List.of("1003"));
        request.setSelectAll(false);
        this.requestPostWithOk(BATCH_DELETE, request);
        request.setProtocols(List.of("HTTP"));
        this.requestPostWithOk(BATCH_DELETE, request);
        // 效验数据结果
        ApiDefinitionExample apiDefinitionExample = new ApiDefinitionExample();
        apiDefinitionExample.createCriteria().andIdIn(request.getSelectIds());
        List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(apiDefinitionExample);
        Assertions.assertEquals(0, apiDefinitions.size());

        request.getSelectIds().forEach(item -> {
            // 文件是否删除
            List<ApiFileResource> apiFileResources = apiFileResourceService.getByResourceId(item);
            Assertions.assertEquals(0, apiFileResources.size());
        });
        // 效验 关联数据
        List<ApiTestCase> caseLists = extApiTestCaseMapper.getCaseInfoByApiIds(request.getSelectIds(), false);
        Assertions.assertEquals(0, caseLists.size());

        // @@校验日志
        apiDefinition = apiDefinitionMapper.selectByPrimaryKey("1006");
        if (!apiDefinition.getDeleted()) {
            // @@请求成功
            this.requestGetWithOkAndReturn(DELETE_TO_GC, apiDefinition.getId(), false);
            apiDefinition = apiDefinitionMapper.selectByPrimaryKey(apiDefinition.getId());
            Assertions.assertTrue(apiDefinition.getDeleted());
            Assertions.assertEquals("admin", apiDefinition.getDeleteUser());
            Assertions.assertNotNull(apiDefinition.getDeleteTime());
        }
        // 删除全部 条件为关键字为st-6的数据
        request.setSelectAll(true);
        request.setExcludeIds(List.of("1005"));
        BaseCondition baseCondition = new BaseCondition();
        baseCondition.setKeyword("st-6");
        request.setCondition(baseCondition);
        this.requestPostWithOk(BATCH_DELETE, request);
        // @@校验日志
        String[] ids = {"1003", "1006"};
        for (String id : ids) {
            checkLogModelList.add(new CheckLogModel(id, OperationLogType.DELETE, BATCH_DELETE));
        }
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_DELETE, BATCH_DELETE, request);
    }

    @Test
    @Order(101)
    public void testLog() throws Exception {
        Thread.sleep(5000);
        for (CheckLogModel checkLogModel : checkLogModelList) {
            if (StringUtils.isEmpty(checkLogModel.getUrl())) {
                this.checkLog(checkLogModel.getResourceId(), checkLogModel.getOperationType());
            } else {
                this.checkLog(checkLogModel.getResourceId(), checkLogModel.getOperationType(), BASE_PATH + checkLogModel.getUrl());
            }
        }
    }

    @Test
    @Order(102)
    public void testImport() throws Exception {
        LogUtils.info("import api test");
        ApiDefinitionModule apiDefinitionModule = new ApiDefinitionModule();
        apiDefinitionModule.setId("test-import");
        apiDefinitionModule.setName("test-import");
        apiDefinitionModule.setProjectId(DEFAULT_PROJECT_ID);
        apiDefinitionModule.setCreateUser("admin");
        apiDefinitionModule.setUpdateUser("admin");
        apiDefinitionModule.setPos(1L);
        apiDefinitionModule.setCreateTime(System.currentTimeMillis());
        apiDefinitionModule.setUpdateTime(System.currentTimeMillis());
        apiDefinitionModuleMapper.insertSelective(apiDefinitionModule);
        ImportRequest request = new ImportRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setPlatform("Swagger3");
        request.setCoverData(true);
        request.setCoverModule(true);
        request.setModuleId(apiDefinitionModule.getId());
        request.setProtocol(ApiConstants.HTTP_PROTOCOL);
        request.setUserId("admin");
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        FileInputStream inputStream = new FileInputStream(new File(
                this.getClass().getClassLoader().getResource("file/openapi.json")
                        .getPath()));

        MockMultipartFile file = new MockMultipartFile("file", "openapi.json", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        paramMap.add("file", file);
        this.requestMultipartWithOkAndReturn(IMPORT, paramMap);
        request.setCoverModule(false);
        request.setCoverData(false);
        this.requestMultipartWithOkAndReturn(IMPORT, paramMap);
        paramMap.clear();
        inputStream = new FileInputStream(new File(
                this.getClass().getClassLoader().getResource("file/openapi1.json")
                        .getPath()));
        file = new MockMultipartFile("file", "openapi1.json", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        paramMap.add("file", file);
        request.setModuleId("root");
        request.setCoverModule(true);
        request.setCoverData(true);
        paramMap.add("request", JSON.toJSONString(request));
        this.requestMultipartWithOkAndReturn(IMPORT, paramMap);
        paramMap.clear();
        inputStream = new FileInputStream(new File(
                this.getClass().getClassLoader().getResource("file/openapi2.json")
                        .getPath()));
        file = new MockMultipartFile("file", "openapi2.json", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        paramMap.add("file", file);
        request.setCoverModule(false);
        request.setCoverData(false);
        paramMap.add("request", JSON.toJSONString(request));
        this.requestMultipart(IMPORT, paramMap, status().is5xxServerError());

        paramMap.clear();
        inputStream = new FileInputStream(new File(
                this.getClass().getClassLoader().getResource("file/openapi3.json")
                        .getPath()));
        file = new MockMultipartFile("file", "openapi3.json", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        paramMap.add("file", file);
        request.setCoverModule(false);
        request.setCoverData(false);
        paramMap.add("request", JSON.toJSONString(request));
        this.requestMultipartWithOkAndReturn(IMPORT, paramMap);
        paramMap.clear();
        inputStream = new FileInputStream(new File(
                this.getClass().getClassLoader().getResource("file/openapi4.json")
                        .getPath()));
        file = new MockMultipartFile("file", "openapi4.json", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        paramMap.add("file", file);
        request.setCoverModule(false);
        request.setCoverData(false);
        paramMap.add("request", JSON.toJSONString(request));
        this.requestMultipartWithOkAndReturn(IMPORT, paramMap);
        paramMap.clear();

        paramMap.add("file", file);
        request.setCoverModule(false);
        request.setCoverData(false);
        request.setSwaggerUrl("http://localhost:8080/v2/api-docs");
        paramMap.add("request", JSON.toJSONString(request));
        this.requestMultipart(IMPORT, paramMap, status().is5xxServerError());

        request.setPlatform(ApiImportPlatform.Postman.name());
        request.setCoverModule(true);
        request.setCoverData(true);
        paramMap.clear();
        inputStream = new FileInputStream(new File(
                Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/postman.json"))
                        .getPath()));
        file = new MockMultipartFile("file", "postman.json", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(request));
        this.requestMultipartWithOkAndReturn(IMPORT, paramMap);
        paramMap.clear();
        request.setCoverModule(true);
        request.setCoverData(true);
        inputStream = new FileInputStream(new File(
                Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/postman2.json"))
                        .getPath()));
        file = new MockMultipartFile("file", "postman2.json", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(request));
        this.requestMultipartWithOkAndReturn(IMPORT, paramMap);


    }

    protected MvcResult requestMultipart(String url, MultiValueMap<String, Object> paramMap, ResultMatcher resultMatcher) throws Exception {
        MockMultipartHttpServletRequestBuilder requestBuilder = getMultipartRequestBuilderWithParam(url, paramMap);
        MockHttpServletRequestBuilder header = requestBuilder
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN");
        return mockMvc.perform(header)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher).andReturn();
    }

    private MockMultipartHttpServletRequestBuilder getMultipartRequestBuilderWithParam(String url, MultiValueMap<String, Object> paramMap) {
        MockMultipartHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.multipart(getBasePath() + url);
        paramMap.forEach((key, value) -> {
            List list = value;
            for (Object o : list) {
                if (o instanceof List fileList) {
                    fileList.forEach(o1 -> {
                        if (o1 instanceof MockMultipartFile mockMultipartFile) {
                            try {
                                MockMultipartFile mockMultipartFile1 = null;
                                mockMultipartFile1 = new MockMultipartFile(key, mockMultipartFile.getOriginalFilename(),
                                        MediaType.APPLICATION_OCTET_STREAM_VALUE, mockMultipartFile.getBytes());
                                requestBuilder.file(mockMultipartFile1);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }

                    });
                } else {
                    MockMultipartFile multipartFile = null;
                    multipartFile = new MockMultipartFile(key, null,
                            MediaType.APPLICATION_JSON_VALUE, o.toString().getBytes());
                    requestBuilder.file(multipartFile);
                }
            }
        });
        return requestBuilder;

    }

    @Test
    @Order(103)
    public void testPreview() throws Exception {
        String jsonString = """
                {
                          "example":  null,
                          "id":  null,
                          "title":  null,
                          "type":  "object",
                          "description":  null,
                          "items":  null,
                          "properties":  {
                                    "id":  {
                                              "example":  10,
                                              "id":  null,
                                              "title":  null,
                                              "type":  "integer",
                                              "description":  "",
                                              "items":  null,
                                              "properties":  null,
                                              "additionalProperties":  null,
                                              "required":  null,
                                              "pattern":  null,
                                              "maxLength":  null,
                                              "minLength":  null,
                                              "minimum":  null,
                                              "maximum":  null,
                                              "schema":  null,
                                              "format":  "int64",
                                              "enumString":  null,
                                              "enumInteger":  null,
                                              "enumNumber":  null,
                                              "extensions":  null
                                    },
                                    "name":  {
                                              "example":  "@string",
                                              "id":  null,
                                              "title":  null,
                                              "type":  "string",
                                              "description":  "",
                                              "items":  null,
                                              "properties":  null,
                                              "additionalProperties":  null,
                                              "required":  null,
                                              "pattern":  null,
                                              "maxLength":  null,
                                              "minLength":  null,
                                              "minimum":  null,
                                              "maximum":  null,
                                              "schema":  null,
                                              "format":  "",
                                              "enumString":  null,
                                              "enumInteger":  null,
                                              "enumNumber":  null,
                                              "extensions":  null
                                    },
                                    "category":  {
                                              "example":  null,
                                              "id":  null,
                                              "title":  null,
                                              "type":  "object",
                                              "description":  null,
                                              "items":  null,
                                              "properties":  {
                                                        "id":  {
                                                                  "example":  "@integer",
                                                                  "id":  null,
                                                                  "title":  null,
                                                                  "type":  "integer",
                                                                  "description":  "",
                                                                  "items":  null,
                                                                  "properties":  null,
                                                                  "additionalProperties":  null,
                                                                  "required":  null,
                                                                  "pattern":  null,
                                                                  "maxLength":  null,
                                                                  "minLength":  null,
                                                                  "minimum":  null,
                                                                  "maximum":  null,
                                                                  "schema":  null,
                                                                  "format":  "int64",
                                                                  "enumString":  null,
                                                                  "enumInteger":  null,
                                                                  "enumNumber":  null,
                                                                  "extensions":  null
                                                        },
                                                        "name":  {
                                                                  "example":  "Dogs",
                                                                  "id":  null,
                                                                  "title":  null,
                                                                  "type":  "string",
                                                                  "description":  "",
                                                                  "items":  null,
                                                                  "properties":  null,
                                                                  "additionalProperties":  null,
                                                                  "required":  null,
                                                                  "pattern":  null,
                                                                  "maxLength":  null,
                                                                  "minLength":  null,
                                                                  "minimum":  null,
                                                                  "maximum":  null,
                                                                  "schema":  null,
                                                                  "format":  "",
                                                                  "enumString":  null,
                                                                  "enumInteger":  null,
                                                                  "enumNumber":  null,
                                                                  "extensions":  null
                                                        }
                                              },
                                              "additionalProperties":  null,
                                              "required":  null,
                                              "pattern":  null,
                                              "maxLength":  null,
                                              "minLength":  null,
                                              "minimum":  null,
                                              "maximum":  null,
                                              "schema":  null,
                                              "format":  null,
                                              "enumString":  null,
                                              "enumInteger":  null,
                                              "enumNumber":  null,
                                              "extensions":  null
                                    },
                                    "photoUrls":  {
                                              "example":  null,
                                              "id":  null,
                                              "title":  null,
                                              "type":  "array",
                                              "description":  null,
                                              "items":  {
                                                        "example":  null,
                                                        "id":  null,
                                                        "title":  null,
                                                        "type":  "string",
                                                        "description":  "",
                                                        "items":  null,
                                                        "properties":  null,
                                                        "additionalProperties":  null,
                                                        "required":  null,
                                                        "pattern":  null,
                                                        "maxLength":  null,
                                                        "minLength":  null,
                                                        "minimum":  null,
                                                        "maximum":  null,
                                                        "schema":  null,
                                                        "format":  "",
                                                        "enumString":  null,
                                                        "enumInteger":  null,
                                                        "enumNumber":  null,
                                                        "extensions":  null
                                              },
                                              "properties":  null,
                                              "additionalProperties":  null,
                                              "required":  null,
                                              "pattern":  null,
                                              "maxLength":  null,
                                              "minLength":  null,
                                              "minimum":  null,
                                              "maximum":  null,
                                              "schema":  null,
                                              "format":  null,
                                              "enumString":  null,
                                              "enumInteger":  null,
                                              "enumNumber":  null,
                                              "extensions":  null
                                    },
                                    "tags":  {
                                              "example":  null,
                                              "id":  null,
                                              "title":  null,
                                              "type":  "array",
                                              "description":  null,
                                              "items":  {
                                                        "example":  null,
                                                        "id":  null,
                                                        "title":  null,
                                                        "type":  "object",
                                                        "description":  null,
                                                        "items":  null,
                                                        "properties":  {
                                                                  "id":  {
                                                                            "example":  null,
                                                                            "id":  null,
                                                                            "title":  null,
                                                                            "type":  "integer",
                                                                            "description":  "",
                                                                            "items":  null,
                                                                            "properties":  null,
                                                                            "additionalProperties":  null,
                                                                            "required":  null,
                                                                            "pattern":  null,
                                                                            "maxLength":  null,
                                                                            "minLength":  null,
                                                                            "minimum":  null,
                                                                            "maximum":  null,
                                                                            "schema":  null,
                                                                            "format":  "int64",
                                                                            "enumString":  null,
                                                                            "enumInteger":  null,
                                                                            "enumNumber":  null,
                                                                            "extensions":  null
                                                                  },
                                                                  "name":  {
                                                                            "example":  null,
                                                                            "id":  null,
                                                                            "title":  null,
                                                                            "type":  "string",
                                                                            "description":  "",
                                                                            "items":  null,
                                                                            "properties":  null,
                                                                            "additionalProperties":  null,
                                                                            "required":  null,
                                                                            "pattern":  null,
                                                                            "maxLength":  null,
                                                                            "minLength":  null,
                                                                            "mainimum":  null,
                                                                            "maximum":  null,
                                                                            "schema":  null,
                                                                            "format":  "",
                                                                            "enumString":  null,
                                                                            "enumInteger":  null,
                                                                            "enumNumber":  null,
                                                                            "extensions":  null
                                                                  }
                                                        },
                                                        "additionalProperties":  null,
                                                        "required":  null,
                                                        "pattern":  null,
                                                        "maxLength":  null,
                                                        "minLength":  null,
                                                        "minimum":  null,
                                                        "maximum":  null,
                                                        "schema":  null,
                                                        "format":  null,
                                                        "enumString":  null,
                                                        "enumInteger":  null,
                                                        "enumNumber":  null,
                                                        "extensions":  null
                                              },
                                              "properties":  null,
                                              "additionalProperties":  null,
                                              "required":  null,
                                              "pattern":  null,
                                              "maxLength":  null,
                                              "minLength":  null,
                                              "minimum":  null,
                                              "maximum":  null,
                                              "schema":  null,
                                              "format":  null,
                                              "enumString":  null,
                                              "enumInteger":  null,
                                              "enumNumber":  null,
                                              "extensions":  null
                                    },
                                    "status":  {
                                              "example":  "available",
                                              "id":  null,
                                              "title":  null,
                                              "type":  "string",
                                              "description":  "pet status in the store",
                                              "items":  null,
                                              "properties":  null,
                                              "additionalProperties":  null,
                                              "required":  null,
                                              "pattern":  null,
                                              "maxLength":  null,
                                              "minLength":  null,
                                              "minimum":  null,
                                              "maximum":  null,
                                              "schema":  null,
                                              "format":  "",
                                              "enumString":  [
                                                        "available",
                                                        "pending",
                                                        "sold"
                                              ],
                                              "enumInteger":  null,
                                              "enumNumber":  null,
                                              "extensions":  null
                                    },
                                    "testnumber":  {
                                              "example":  1.23139183198000000283719387,
                                              "id":  null,
                                              "title":  null,
                                              "type":  "number",
                                              "description":  "pet status in the store",
                                              "items":  null,
                                              "properties":  null,
                                              "additionalProperties":  null,
                                              "required":  null,
                                              "pattern":  null,
                                              "maxLength":  null,
                                              "minLength":  null,
                                              "minimum":  null,
                                              "maximum":  null,
                                              "schema":  null,
                                              "format":  "",
                                              "enumString":  [
                                                        "available",
                                                        "pending",
                                                        "sold"
                                              ],
                                              "enumInteger":  null,
                                              "enumNumber":  null,
                                              "extensions":  null
                                    },
                                    "testnumber11":  {
                                              "example":  "@number",
                                              "id":  null,
                                              "title":  null,
                                              "type":  "number",
                                              "description":  "pet status in the store",
                                              "items":  null,
                                              "properties":  null,
                                              "additionalProperties":  null,
                                              "required":  null,
                                              "pattern":  null,
                                              "maxLength":  null,
                                              "minLength":  null,
                                              "minimum":  null,
                                              "maximum":  null,
                                              "schema":  null,
                                              "format":  "",
                                              "enumString":  [
                                                        "available",
                                                        "pending",
                                                        "sold"
                                              ],
                                              "enumInteger":  null,
                                              "enumNumber":  null,
                                              "extensions":  null
                                    },
                                    "testfalse":  {
                                              "example":  "@boolean",
                                              "id":  null,
                                              "title":  null,
                                              "type":  "boolean",
                                              "description":  "pet status in the store",
                                              "items":  null,
                                              "properties":  null,
                                              "additionalProperties":  null,
                                              "required":  null,
                                              "pattern":  null,
                                              "maxLength":  null,
                                              "minLength":  null,
                                              "minimum":  null,
                                              "maximum":  null,
                                              "schema":  null,
                                              "format":  "",
                                              "enumString":  [
                                                        "available",
                                                        "pending",
                                                        "sold"
                                              ],
                                              "enumInteger":  null,
                                              "enumNumber":  null,
                                              "extensions":  null
                                    },
                                    "testfalse":  {
                                              "example":  false,
                                              "id":  null,
                                              "title":  null,
                                              "type":  "boolean",
                                              "description":  "pet status in the store",
                                              "items":  null,
                                              "properties":  null,
                                              "additionalProperties":  null,
                                              "required":  null,
                                              "pattern":  null,
                                              "maxLength":  null,
                                              "minLength":  null,
                                              "minimum":  null,
                                              "maximum":  null,
                                              "schema":  null,
                                              "format":  "",
                                              "enumString":  [
                                                        "available",
                                                        "pending",
                                                        "sold"
                                              ],
                                              "enumInteger":  null,
                                              "enumNumber":  null,
                                              "extensions":  null
                                    },
                                    "testnull":  {
                                              "example":  null,
                                              "id":  null,
                                              "title":  null,
                                              "type":  null,
                                              "description":  "pet status in the store",
                                              "items":  null,
                                              "properties":  null,
                                              "additionalProperties":  null,
                                              "required":  null,
                                              "pattern":  null,
                                              "maxLength":  null,
                                              "minLength":  null,
                                              "minimum":  null,
                                              "maximum":  null,
                                              "schema":  null,
                                              "format":  "",
                                              "enumInteger":  null,
                                              "enumNumber":  null,
                                              "extensions":  null
                                    },
                                    "testass": null
                          },
                          "additionalProperties":  null,
                          "required":  [
                                    "name",
                                    "photoUrls"
                          ],
                          "pattern":  null,
                          "maxLength":  null,
                          "minLength":  null,
                          "minimum":  null,
                          "maximum":  null,
                          "schema":  null,
                          "format":  null,
                          "enumString":  null,
                          "enumInteger":  null,
                          "enumNumber":  null,
                          "extensions":  null
                }
                """;
        //正常数据;
        requestPost("preview", jsonString).andExpect(status().isOk());
        //非正常json数据    会走try catch
        String abnormalString = """
                    {
                      "id" : 10,
                      "name" : @string,
                      "category" : {
                        "id" : "@integer",
                        "name" : "Dogs"
                      },
                      "photoUrls" : [ "string" ],
                      "tags" : [ {
                        "id" : 0,
                        "name" : "string"
                      } ],
                      "status" : "available",
                      "testnumber" : 1.23139183198000000283719387,
                      "testfalse" : false
                    }            
                """;
        requestPost("preview", abnormalString).andExpect(status().isOk());
        //正常array数据
        String jsonArray = """
                    {
                                                  "example":  null,
                                                  "id":  null,
                                                  "title":  null,
                                                  "type":  "array",
                                                  "description":  null,
                                                  "items":  {
                                                            "example":  null,
                                                            "id":  null,
                                                            "title":  null,
                                                            "type":  "object",
                                                            "description":  null,
                                                            "items":  null,
                                                            "properties":  {
                                                                      "id":  {
                                                                                "example":  "@integer",
                                                                                "id":  null,
                                                                                "title":  null,
                                                                                "type":  "integer",
                                                                                "description":  "",
                                                                                "items":  null,
                                                                                "properties":  null,
                                                                                "additionalProperties":  null,
                                                                                "required":  null,
                                                                                "pattern":  null,
                                                                                "maxLength":  null,
                                                                                "minLength":  null,
                                                                                "minimum":  null,
                                                                                "maximum":  null,
                                                                                "schema":  null,
                                                                                "format":  "int64",
                                                                                "enumString":  null,
                                                                                "enumInteger":  null,
                                                                                "enumNumber":  null,
                                                                                "extensions":  null
                                                                      },
                                                                      "name":  {
                                                                                "example":  null,
                                                                                "id":  null,
                                                                                "title":  null,
                                                                                "type":  "string",
                                                                                "description":  "",
                                                                                "items":  null,
                                                                                "properties":  null,
                                                                                "additionalProperties":  null,
                                                                                "required":  null,
                                                                                "pattern":  null,
                                                                                "maxLength":  null,
                                                                                "minLength":  null,
                                                                                "mainimum":  null,
                                                                                "maximum":  null,
                                                                                "schema":  null,
                                                                                "format":  "",
                                                                                "enumString":  null,
                                                                                "enumInteger":  null,
                                                                                "enumNumber":  null,
                                                                                "extensions":  null
                                                                      }
                                                            },
                                                            "additionalProperties":  null,
                                                            "required":  null,
                                                            "pattern":  null,
                                                            "maxLength":  null,
                                                            "minLength":  null,
                                                            "minimum":  null,
                                                            "maximum":  null,
                                                            "schema":  null,
                                                            "format":  null,
                                                            "enumString":  null,
                                                            "enumInteger":  null,
                                                            "enumNumber":  null,
                                                            "extensions":  null
                                                  },
                                                  "properties":  null,
                                                  "additionalProperties":  null,
                                                  "required":  null,
                                                  "pattern":  null,
                                                  "maxLength":  null,
                                                  "minLength":  null,
                                                  "minimum":  null,
                                                  "maximum":  null,
                                                  "schema":  null,
                                                  "format":  null,
                                                  "enumString":  null,
                                                  "enumInteger":  null,
                                                  "enumNumber":  null,
                                                  "extensions":  null
                                        }            
                """;
        requestPost("preview", jsonArray).andExpect(status().isOk());
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
        apiScenarioStep.setResourceId("test-api-get-ref");
        apiScenarioStep.setRefType("COPY");
        apiScenarioStep.setStepType("API");
        apiScenarioStep.setName("接口步骤");
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
        apiScenarioStep.setStepType("API");
        apiScenarioStep.setName("接口步骤");
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
        apiScenarioStep.setStepType("API");
        apiScenarioStep.setName("接口步骤");
        apiScenarioStep.setSort(1L);
        apiScenarioStep.setParentId("NONE");
        apiScenarioStep.setVersionId("1.0");
        apiScenarioStep.setEnable(true);
        apiScenarioSteps.add(apiScenarioStep);
        apiScenarioStepMapper.batchInsert(apiScenarioSteps);
        ReferenceRequest request = new ReferenceRequest();
        request.setResourceId("test-api-get-ref");
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
