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
import io.metersphere.api.dto.export.MetersphereApiDefinitionExportResponse;
import io.metersphere.api.dto.request.ApiEditPosRequest;
import io.metersphere.api.dto.request.ApiTransferRequest;
import io.metersphere.api.dto.request.ImportRequest;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.MsHeader;
import io.metersphere.api.dto.schema.JsonSchemaItem;
import io.metersphere.api.mapper.*;
import io.metersphere.api.model.CheckLogModel;
import io.metersphere.api.parser.ImportParserFactory;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.service.ApiDefinitionImportTestService;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.api.service.BaseFileManagementTestService;
import io.metersphere.api.service.definition.ApiDefinitionService;
import io.metersphere.api.service.definition.ApiTestCaseService;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.api.utils.ApiDefinitionImportUtils;
import io.metersphere.functional.domain.ExportTask;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.filemanagement.FileInfo;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.service.FileAssociationService;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.dto.BaseCondition;
import io.metersphere.sdk.dto.CombineCondition;
import io.metersphere.sdk.dto.CombineSearch;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.*;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.constants.ExportConstants;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.controller.handler.result.MsHttpResultCode;
import io.metersphere.system.domain.OperationHistory;
import io.metersphere.system.domain.OperationHistoryExample;
import io.metersphere.system.dto.AddProjectRequest;
import io.metersphere.system.dto.request.OperationHistoryRequest;
import io.metersphere.system.dto.request.OperationHistoryVersionRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.manager.ExportTaskManager;
import io.metersphere.system.mapper.OperationHistoryMapper;
import io.metersphere.system.service.CommonProjectService;
import io.metersphere.system.service.UserLoginService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
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
    private static final String JSON_SCHEMA_PREVIEW = "json-schema/preview";
    private static final String JSON_SCHEMA_AUTO_GENERATE = "json-schema/auto-generate";

    private static final String EXPORT = "/export/";
    private static ApiDefinition apiDefinition;
    private static ApiDefinition tcpApiDefinition;

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
    @Resource
    private ApiDefinitionImportTestService apiDefinitionImportTestService;

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
        msHttpResponse.getFirst().setBody(ApiDebugControllerTests.addBodyLinkFile(msHttpResponse.getFirst().getBody(), fileMetadataId));
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
        apiTransferRequest.setFileId(apiFileResources.getFirst().getFileId());
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
        mvcResult = this.requestPostWithOkAndReturn(ADD, request);
        resultData = getResultData(mvcResult, ApiDefinition.class);
        tcpApiDefinition = apiDefinitionMapper.selectByPrimaryKey(resultData.getId());

        // @@TCP重名校验异常
        assertErrorCode(this.requestPost(ADD, request), ApiResultCode.API_DEFINITION_EXIST);

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

        assertionModuleName(apiDefinitionDTO);
        copyApiDefinitionDTO.setModuleName(apiDefinitionDTO.getModuleName());

        Assertions.assertEquals(apiDefinitionDTO, copyApiDefinitionDTO);

        assertErrorCode(this.requestGet(GET + "111"), ApiResultCode.API_DEFINITION_NOT_EXIST);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_READ, GET + apiDefinition.getId());
    }

    private void assertionModuleName(ApiDefinitionDTO apiDefinitionDTO) {
        // 判断模块名是否正确
        ApiDefinitionModule apiDefinitionModule = apiDefinitionModuleMapper.selectByPrimaryKey(apiDefinitionDTO.getModuleId());
        if (apiDefinitionModule == null) {
            Assertions.assertEquals(apiDefinitionDTO.getModuleName(), Translator.get("api_unplanned_request"));
        } else {
            Assertions.assertEquals(apiDefinitionDTO.getModuleName(), apiDefinitionModule.getName());
        }
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

        ApiDefinitionUpdateRequest tcpRequest = new ApiDefinitionUpdateRequest();
        tcpRequest.setId(tcpApiDefinition.getId());
        tcpRequest.setDescription("TEST");
        this.requestPostWithOk(UPDATE, tcpRequest);

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
        MsHeader msHeader = new MsHeader();
        msHeader.setKey("111");
        // 添加差异
        msHttpElement.setHeaders(List.of(msHeader));
        updateRequest.setRequest(getMsElementParam(msHttpElement));
        this.requestPostWithOk(UPDATE, updateRequest);
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andApiDefinitionIdEqualTo(apiDefinition.getId());
        List<ApiTestCase> apiTestCases = apiTestCaseMapper.selectByExample(example);
        // 校验差异后的变更通知
        apiTestCases.forEach(apiTestCase -> Assertions.assertTrue(apiTestCase.getApiChange()));

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
        apiDefinitionBatchUpdateRequest.setClear(false);
        this.requestPostWithOk(BATCH_UPDATE, apiDefinitionBatchUpdateRequest);
        assertBatchUpdateApiDefinition(apiDefinitionBatchUpdateRequest, List.of("1001", "1002"));
        // 修改标签，覆盖
        apiDefinitionBatchUpdateRequest.setSelectIds(List.of("1003", "1004"));
        apiDefinitionBatchUpdateRequest.setTags(new LinkedHashSet<>(List.of("tag-append", "tag-append1")));
        apiDefinitionBatchUpdateRequest.setAppend(false);
        apiDefinitionBatchUpdateRequest.setClear(false);
        this.requestPostWithOk(BATCH_UPDATE, apiDefinitionBatchUpdateRequest);
        assertBatchUpdateApiDefinition(apiDefinitionBatchUpdateRequest, List.of("1003", "1004"));
        apiDefinitionBatchUpdateRequest.setClear(true);
        this.requestPostWithOk(BATCH_UPDATE, apiDefinitionBatchUpdateRequest);
        ApiDefinitionExample apiDefinitionExample = new ApiDefinitionExample();
        apiDefinitionExample.createCriteria().andIdIn(List.of("1003", "1004"));
        List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(apiDefinitionExample);
        for (ApiDefinition definition : apiDefinitions) {
            Assertions.assertTrue(CollectionUtils.isEmpty(definition.getTags()));
        }
        // 自定义字段覆盖
        apiDefinitionBatchUpdateRequest.setType("customs");
        apiDefinitionBatchUpdateRequest.setSelectIds(List.of("1002", "1003", "1004"));
        ApiDefinitionCustomFieldDTO field = new ApiDefinitionCustomFieldDTO();
        field.setId("test_field");
        field.setValue(JSON.toJSONString(List.of("test1-batch")));
        apiDefinitionBatchUpdateRequest.setCustomField(field);
        apiDefinitionBatchUpdateRequest.setAppend(false);
        apiDefinitionBatchUpdateRequest.setClear(false);
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
            ApiDefinitionDTO apiDefinitionDTO = apiDefinitions.getFirst();
            // 判断用例数是否正确
            ApiTestCaseExample example = new ApiTestCaseExample();
            example.createCriteria()
                    .andApiDefinitionIdEqualTo(apiDefinitionDTO.getId())
                    .andDeletedEqualTo(false);
            List<ApiTestCase> apiTestCases = apiTestCaseMapper.selectByExample(example);
            Assertions.assertEquals(apiDefinitionDTO.getCaseTotal(), apiTestCases.size());
            // 判断模块名是否正确
            assertionModuleName(apiDefinitionDTO);
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

        CombineSearch combineSearch = new CombineSearch();
        CombineCondition condition = new CombineCondition();
        condition.setName("name");
        condition.setOperator(CombineCondition.CombineConditionOperator.CONTAINS.name());
        condition.setValue("test-1");
        CombineCondition condition2 = new CombineCondition();
        condition2.setName("method");
        condition2.setOperator(CombineCondition.CombineConditionOperator.IN.name());
        condition2.setValue(List.of("GET", "POST"));
        combineSearch.setConditions(List.of(condition, condition2));
        request.setCombineSearch(combineSearch);
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
        CombineSearch combineSearch = new CombineSearch();
        CombineCondition condition = new CombineCondition();
        condition.setName("name");
        condition.setOperator(CombineCondition.CombineConditionOperator.CONTAINS.name());
        condition.setValue("test");

        CombineCondition condition2 = new CombineCondition();
        condition2.setName("method");
        condition2.setOperator(CombineCondition.CombineConditionOperator.IN.name());
        condition2.setValue(List.of("GET", "POST"));

        CombineCondition condition3 = new CombineCondition();
        condition3.setName("createUser");
        condition3.setOperator(CombineCondition.CombineConditionOperator.IN.name());
        condition3.setValue(List.of("currentUser"));

        CombineCondition customCondition = new CombineCondition();
        customCondition.setCustomField(true);
        customCondition.setName("test_field");
        customCondition.setOperator(CombineCondition.CombineConditionOperator.CONTAINS.name());
        customCondition.setValue("test");

        combineSearch.setConditions(List.of(condition, condition2, condition3, customCondition));
        request.setCombineSearch(combineSearch);
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
        // 测试 getUniqueName 方法
        List<String> uniqueNameTestList = new ArrayList<>() {{
            this.add("用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例-1");
            this.add("用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录666");
        }};
        Assertions.assertEquals("用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例-2",
                ApiDefinitionImportUtils.getUniqueName(
                        "用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录66成功用例登录666",
                        uniqueNameTestList
                ));


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
        inputStream = new FileInputStream(
                this.getClass().getClassLoader().getResource("file/openapi2.json")
                        .getPath());
        file = new MockMultipartFile("file", "openapi2.json", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        paramMap.add("file", file);
        request.setCoverModule(false);
        request.setCoverData(false);
        paramMap.add("request", JSON.toJSONString(request));
        this.requestMultipart(IMPORT, paramMap, status().is5xxServerError());

        paramMap.clear();
        inputStream = new FileInputStream(
                this.getClass().getClassLoader().getResource("file/openapi3.json")
                        .getPath());
        file = new MockMultipartFile("file", "openapi3.json", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        paramMap.add("file", file);
        request.setCoverModule(false);
        request.setCoverData(false);
        paramMap.add("request", JSON.toJSONString(request));
        this.requestMultipartWithOkAndReturn(IMPORT, paramMap);
        paramMap.clear();
        inputStream = new FileInputStream(
                this.getClass().getClassLoader().getResource("file/openapi4.json")
                        .getPath());
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
        inputStream = new FileInputStream(
                Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/postman.json"))
                        .getPath());
        file = new MockMultipartFile("file", "postman.json", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(request));
        this.requestMultipartWithOkAndReturn(IMPORT, paramMap);
        paramMap.clear();
        request.setCoverModule(true);
        request.setCoverData(true);
        inputStream = new FileInputStream(
                Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/postman2.json"))
                        .getPath());
        file = new MockMultipartFile("file", "postman2.json", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(request));
        this.requestMultipartWithOkAndReturn(IMPORT, paramMap);

        this.importTest();
    }

    @Resource
    private CommonProjectService commonProjectService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ApiDefinitionMockMapper apiDefinitionMockMapper;

    private void importTest() throws Exception {
        //测试ImportParserFactory不按规定获取会返回null
        Assertions.assertNull(ImportParserFactory.getApiDefinitionImportParser("test"));
        // 创建用于导入的项目
        //测试计划专用项目
        AddProjectRequest initProject = new AddProjectRequest();
        initProject.setOrganizationId("100001");
        initProject.setName("接口测试导入专用项目");
        initProject.setDescription("建国创建的接口测试专用项目");
        initProject.setEnable(true);
        initProject.setUserIds(List.of("admin"));
        Project importProject = commonProjectService.add(initProject, "admin", "/organization-project/add", OperationLogModule.SETTING_ORGANIZATION_PROJECT);
        ArrayList<String> moduleList = new ArrayList<>(List.of("workstation", "testPlan", "bugManagement", "caseManagement", "apiTest", "uiTest", "loadTest"));
        Project updateProject = new Project();
        updateProject.setId(importProject.getId());
        updateProject.setModuleSetting(JSON.toJSONString(moduleList));
        projectMapper.updateByPrimaryKeySelective(updateProject);

        initProject.setName("接口测试导出专用项目");
        Project exportProject = commonProjectService.add(initProject, "admin", "/organization-project/add", OperationLogModule.SETTING_ORGANIZATION_PROJECT);
        updateProject.setId(exportProject.getId());
        projectMapper.updateByPrimaryKeySelective(updateProject);

        /*

            导入测试。 不同类型的文件，要按照如下指定的文件类型，走同样的导入逻辑判断。确保数据的高可用和对代码的高覆盖
            以下是对导入文件的要求：
                    file/import/{importType}/single: 1个接口，无用例。
                    file/import/{importType}/simple: 4个接口(（其中2个url重复，合法数据只有3条,并且都在同一个模块下）)；
                                                        2个路径重复的接口下都有2个用例。 （用于校验路径相同的接口下，用例要合并起来导入）  (Metersphere的还要有15个Mock，其中有名字一样的)
                    file/import/{importType}/repeatFileDiffApi: 和simple一样路径的4个接口（其中2个路径重复，合法数据只有3条），但是参数不一样;
                                                        每个接口下都有2个用例，一共4个。 simple中的4个用例相比，有1个用例名字一样、对应的接口路径也一样。剩下的用例名称全部一样。（用于校验相同路径的接口下、相同名称的用例处理方式）
                    file/import/{importType}/repeatFileDiffModule: 和repeatFileDiffApi一样的4个接口（其中2个路径重复，合法数据只有3条），参数也一样，但是所属模块不一样;
                                                        没有用例。（用于校验覆盖模块时，接口的变更）

            以下是导入操作：

                导入不存在的接口
                    · 不指定导入模块   导入 {importType}/simple， 同步导入用例。校验有新增的模块，下面一共有3个API。其中有个API包含4个case
                    · 指定导入模块     导入 {importType}/single，校验模块新增了1个并有api
                导入存在的接口
                    · 不覆盖接口   导入 {importType}/repeatFileDiffApi，校验模块没有变化，api数量没有变化，case数量没有变化
                    · 覆盖接口
                        · 不覆盖模块
                            导入 {importType}/repeatFileDiffApi，不导入用例，校验模块没有变化，api更新时间变了，case数量没有变化
                            再导入 {importType}/repeatFileDiffApi，导入用例，校验模块没有变化，api更新时间没变，case数量增加，应该是4+3+3 -1（名称重复的）
                        · 覆盖模块
                            接口一样，模块不一样：     导入 {importType}/repeatFileDiffModule， (测试指定导入模块，测试一下会不会创建多个模块）   判断接口对应的模块有更新， api内容没更新；
                            接口不一样，模块不一样：   重新导入{importType}/simple，          (测试测试不指定导入模块，测试一下会不会回到原模块）   判断接口对应的模块有更新， api内容有更新
                            接口不一样，模块一样：     重新导入{importType}/repeatFileDiffApi，     判断接口对应的模块没更新， api内容有更新
                            接口一样，模块一样： 再导入{importType}/repeatFileDiffApi，     判断接口对应的模块没更新， api内容没更新
         */

        //导入类型以及文件后缀
        Map<String, String> importTypeAndSuffix = new LinkedHashMap<>();
        importTypeAndSuffix.put("metersphere", "json");
        importTypeAndSuffix.put("postman", "json");
        importTypeAndSuffix.put("har", "har");
        List<String> importCaseType = Arrays.asList("postman", "metersphere");
        List<String> importModulesType = Arrays.asList("postman", "metersphere");

        ApiDefinitionModuleExample moduleExample = new ApiDefinitionModuleExample();
        moduleExample.createCriteria().andProjectIdEqualTo(importProject.getId());
        ApiTestCaseExample apiTestCaseExample = new ApiTestCaseExample();
        apiTestCaseExample.createCriteria().andProjectIdEqualTo(importProject.getId());

        for (Map.Entry<String, String> entry : importTypeAndSuffix.entrySet()) {
            ImportRequest request = new ImportRequest();
            request.setProjectId(importProject.getId());
            request.setProtocol(ApiConstants.HTTP_PROTOCOL);
            request.setUserId("admin");
            
            List<ApiDefinitionModule> apiDefinitionModuleList = apiDefinitionModuleMapper.selectByExample(moduleExample);
            List<ApiDefinitionBlob> apiBlobList = apiDefinitionImportTestService.selectBlobByProjectId(importProject.getId());
            List<ApiTestCase> apiTestCaseList = apiTestCaseMapper.selectByExample(apiTestCaseExample);
            Assertions.assertEquals(apiDefinitionModuleList.size(), 0);
            Assertions.assertEquals(apiBlobList.size(), 0);
            Assertions.assertEquals(apiTestCaseList.size(), 0);
            boolean checkTestCase = importCaseType.contains(entry.getKey());
            boolean checkModules = importModulesType.contains(entry.getKey());

            String importType = entry.getKey();
            String fileSuffix = entry.getValue();
            request.setPlatform(importType);
            request.setSyncCase(true);
            FileInputStream inputStream = new FileInputStream(new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/import/" + importType + "/simple." + fileSuffix)).getPath()));
            MockMultipartFile file = new MockMultipartFile("file", "simple." + fileSuffix, MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
            MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
            paramMap.add("request", JSON.toJSONString(request));
            paramMap.add("file", file);
            this.requestMultipartWithOkAndReturn(IMPORT, paramMap);
            request.setSyncCase(false);
            apiDefinitionModuleList = apiDefinitionModuleMapper.selectByExample(moduleExample);
            apiBlobList = apiDefinitionImportTestService.selectBlobByProjectId(importProject.getId());
            Assertions.assertEquals(apiDefinitionModuleList.size(), checkModules ? 1 : 0);
            Assertions.assertEquals(apiBlobList.size(), 3);
            if (checkTestCase) {
                apiTestCaseList = apiTestCaseMapper.selectByExample(apiTestCaseExample);
                Assertions.assertEquals(apiTestCaseList.size(), 4);
            }

            if (StringUtils.equalsIgnoreCase(importType, "metersphere")) {
                request.setSyncMock(true);
                request.setCoverData(true);
                List<String> apiString = apiBlobList.stream().map(ApiDefinitionBlob::getId).toList();
                ApiDefinitionMockExample apiDefinitionMockExample = new ApiDefinitionMockExample();
                apiDefinitionMockExample.createCriteria().andApiDefinitionIdIn(apiString).andProjectIdEqualTo(importProject.getId());
                List<ApiDefinitionMock> mockList = apiDefinitionMockMapper.selectByExample(apiDefinitionMockExample);
                Assertions.assertEquals(mockList.size(), 0);
                paramMap = new LinkedMultiValueMap<>();
                paramMap.add("file", file);
                paramMap.add("request", JSON.toJSONString(request));
                this.requestMultipartWithOkAndReturn(IMPORT, paramMap);
                mockList = apiDefinitionMockMapper.selectByExample(apiDefinitionMockExample);
                Assertions.assertEquals(mockList.size(), 15);

                this.requestMultipartWithOkAndReturn(IMPORT, paramMap);
                mockList = apiDefinitionMockMapper.selectByExample(apiDefinitionMockExample);
                Assertions.assertEquals(mockList.size(), 15);
                request.setSyncMock(false);
                request.setCoverData(false);
            }

            if (CollectionUtils.isEmpty(apiDefinitionModuleList)) {
                request.setModuleId(ModuleConstants.DEFAULT_NODE_ID);
            } else {
                request.setModuleId(apiDefinitionModuleList.getFirst().getId());
            }
            inputStream = new FileInputStream(new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/import/" + importType + "/single." + fileSuffix)).getPath()));
            file = new MockMultipartFile("file", "single." + fileSuffix, MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
            paramMap = new LinkedMultiValueMap<>();
            paramMap.add("request", JSON.toJSONString(request));
            paramMap.add("file", file);
            this.requestMultipartWithOkAndReturn(IMPORT, paramMap);
            request.setModuleId(null);
            apiDefinitionModuleList = apiDefinitionModuleMapper.selectByExample(moduleExample);
            apiBlobList = apiDefinitionImportTestService.selectBlobByProjectId(importProject.getId());
            Assertions.assertEquals(apiDefinitionModuleList.size(), checkModules ? 2 : 0);
            Assertions.assertEquals(apiBlobList.size(), 4);
            if (checkTestCase) {
                apiTestCaseList = apiTestCaseMapper.selectByExample(apiTestCaseExample);
                Assertions.assertEquals(apiTestCaseList.size(), 4);
            }

            if (StringUtils.equalsIgnoreCase(importType, "metersphere")) {
                //恰逢ms格式的导入，可以顺便测试导出
                this.testExportAndImport(importProject.getId(), apiBlobList);
            }


            //            · 不覆盖接口   导入 {importType}/repeatFileDiffApi，校验模块没有变化，api无变化，case数量没有变化
            inputStream = new FileInputStream(new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/import/" + importType + "/repeatFileDiffApi." + fileSuffix)).getPath()));
            file = new MockMultipartFile("file", "repeatFileDiffApi." + fileSuffix, MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
            paramMap = new LinkedMultiValueMap<>();
            paramMap.add("request", JSON.toJSONString(request));
            paramMap.add("file", file);
            this.requestMultipartWithOkAndReturn(IMPORT, paramMap);
            request.setModuleId(null);
            apiDefinitionModuleList = apiDefinitionModuleMapper.selectByExample(moduleExample);
            Assertions.assertEquals(apiDefinitionModuleList.size(), checkModules ? 2 : 0);
            List<ApiDefinitionBlob> newApiBlobList = apiDefinitionImportTestService.selectBlobByProjectId(importProject.getId());
            apiDefinitionImportTestService.compareApiBlobList(apiBlobList, newApiBlobList, 0);
            apiBlobList = newApiBlobList;
            if (checkTestCase) {
                List<ApiTestCase> newApiTestCaseList = apiTestCaseMapper.selectByExample(apiTestCaseExample);
                apiDefinitionImportTestService.compareApiTestCaseList(apiTestCaseList, newApiTestCaseList, 0, 0);
                apiTestCaseList = newApiTestCaseList;
            }

            //· 覆盖接口
            request.setCoverData(true);
            //    · 不覆盖模块
            //            导入 {importType}/repeatFileDiffApi，不导入用例，校验模块没有变化，api有3个变了，case数量没有变化
            inputStream = new FileInputStream(new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/import/" + importType + "/repeatFileDiffApi." + fileSuffix)).getPath()));
            file = new MockMultipartFile("file", "repeatFileDiffApi." + fileSuffix, MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
            paramMap = new LinkedMultiValueMap<>();
            paramMap.add("request", JSON.toJSONString(request));
            paramMap.add("file", file);
            this.requestMultipartWithOkAndReturn(IMPORT, paramMap);
            apiDefinitionModuleList = apiDefinitionModuleMapper.selectByExample(moduleExample);
            Assertions.assertEquals(apiDefinitionModuleList.size(), checkModules ? 2 : 0);
            newApiBlobList = apiDefinitionImportTestService.selectBlobByProjectId(importProject.getId());
            apiDefinitionImportTestService.compareApiBlobList(apiBlobList, newApiBlobList, 3);
            apiBlobList = newApiBlobList;
            if (checkTestCase) {
                List<ApiTestCase> newApiTestCaseList = apiTestCaseMapper.selectByExample(apiTestCaseExample);
                apiDefinitionImportTestService.compareApiTestCaseList(apiTestCaseList, newApiTestCaseList, 0, 0);
                apiTestCaseList = newApiTestCaseList;
            }

            //            再导入 {importType}/repeatFileDiffApi，导入用例，校验模块没有变化，api无更新，case数量增加，应该是4+3+3 -1（名称重复的）、
            request.setSyncCase(true);
            inputStream = new FileInputStream(new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/import/" + importType + "/repeatFileDiffApi." + fileSuffix)).getPath()));
            file = new MockMultipartFile("file", "repeatFileDiffApi." + fileSuffix, MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
            paramMap = new LinkedMultiValueMap<>();
            paramMap.add("request", JSON.toJSONString(request));
            paramMap.add("file", file);
            this.requestMultipartWithOkAndReturn(IMPORT, paramMap);
            request.setSyncCase(false);
            apiDefinitionModuleList = apiDefinitionModuleMapper.selectByExample(moduleExample);
            Assertions.assertEquals(apiDefinitionModuleList.size(), checkModules ? 2 : 0);
            newApiBlobList = apiDefinitionImportTestService.selectBlobByProjectId(importProject.getId());
            apiDefinitionImportTestService.compareApiBlobList(apiBlobList, newApiBlobList, 0);
            apiBlobList = newApiBlobList;
            if (checkTestCase) {
                List<ApiTestCase> newApiTestCaseList = apiTestCaseMapper.selectByExample(apiTestCaseExample);
                apiDefinitionImportTestService.compareApiTestCaseList(apiTestCaseList, newApiTestCaseList, 1, 3);
                apiTestCaseList = newApiTestCaseList;
            }

            //                · 覆盖模块
            request.setCoverModule(true);
            List<ApiDefinition> newApiDefinition = new ArrayList<>();
            List<ApiDefinition> oldApiDefinition = new ArrayList<>();
            if (checkModules) {
                // 以下只针对需要检查模块的导入文件方式。  部分比如har文件，由于导入接口没有模块数据，故不需要检查模块
                //            接口一样，模块不一样：     导入 {importType}/repeatFileDiffModule， (测试测试指定导入模块，测试一下会不会创建多个模块）   判断接口对应的模块有更新， api内容没更新；
                inputStream = new FileInputStream(new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/import/" + importType + "/repeatFileDiffModule." + fileSuffix)).getPath()));
                file = new MockMultipartFile("file", "repeatFileDiffModule." + fileSuffix, MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
                paramMap = new LinkedMultiValueMap<>();
                paramMap.add("request", JSON.toJSONString(request));
                paramMap.add("file", file);
                this.requestMultipartWithOkAndReturn(IMPORT, paramMap);
                apiDefinitionModuleList = apiDefinitionModuleMapper.selectByExample(moduleExample);
                Assertions.assertTrue(apiDefinitionModuleList.size() > 2);
                newApiBlobList = apiDefinitionImportTestService.selectBlobByProjectId(importProject.getId());
                apiDefinitionImportTestService.compareApiBlobList(apiBlobList, newApiBlobList, 0);
                apiBlobList = newApiBlobList;
                if (checkTestCase) {
                    List<ApiTestCase> newApiTestCaseList = apiTestCaseMapper.selectByExample(apiTestCaseExample);
                    apiDefinitionImportTestService.compareApiTestCaseList(apiTestCaseList, newApiTestCaseList, 0, 0);
                }

                //            接口不一样，模块不一样：   重新导入{importType}/simple，          (测试测试不指定导入模块，测试一下会不会回到原模块）   判断接口对应的模块有更新， api内容有更新
                oldApiDefinition = apiDefinitionImportTestService.selectApiDefinitionByProjectId(importProject.getId());
                inputStream = new FileInputStream(new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/import/" + importType + "/simple." + fileSuffix)).getPath()));
                file = new MockMultipartFile("file", "simple." + fileSuffix, MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
                paramMap = new LinkedMultiValueMap<>();
                paramMap.add("request", JSON.toJSONString(request));
                paramMap.add("file", file);
                this.requestMultipartWithOkAndReturn(IMPORT, paramMap);
                apiDefinitionModuleList = apiDefinitionModuleMapper.selectByExample(moduleExample);
                Assertions.assertTrue(apiDefinitionModuleList.size() > 2);
                newApiDefinition = apiDefinitionImportTestService.selectApiDefinitionByProjectId(importProject.getId());
                apiDefinitionImportTestService.checkApiModuleChange(oldApiDefinition, newApiDefinition, 3);
                newApiBlobList = apiDefinitionImportTestService.selectBlobByProjectId(importProject.getId());
                apiDefinitionImportTestService.compareApiBlobList(apiBlobList, newApiBlobList, 3);
                apiBlobList = newApiBlobList;
                oldApiDefinition = newApiDefinition;

                //            接口不一样，模块一样：     重新导入{importType}/repeatFileDiffApi，     判断接口对应的模块没更新， api内容有更新
                inputStream = new FileInputStream(new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/import/" + importType + "/repeatFileDiffApi." + fileSuffix)).getPath()));
                file = new MockMultipartFile("file", "repeatFileDiffApi." + fileSuffix, MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
                paramMap = new LinkedMultiValueMap<>();
                paramMap.add("request", JSON.toJSONString(request));
                paramMap.add("file", file);
                this.requestMultipartWithOkAndReturn(IMPORT, paramMap);
                newApiDefinition = apiDefinitionImportTestService.selectApiDefinitionByProjectId(importProject.getId());
                apiDefinitionImportTestService.checkApiModuleChange(oldApiDefinition, newApiDefinition, 0);
                newApiBlobList = apiDefinitionImportTestService.selectBlobByProjectId(importProject.getId());
                apiDefinitionImportTestService.compareApiBlobList(apiBlobList, newApiBlobList, 3);
                apiBlobList = newApiBlobList;
            }


            //            接口一样，模块一样： 再导入{importType}/repeatFileDiffApi，     判断接口对应的模块没更新， api内容没更新
            oldApiDefinition = newApiDefinition;
            inputStream = new FileInputStream(new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/import/" + importType + "/repeatFileDiffApi." + fileSuffix)).getPath()));
            file = new MockMultipartFile("file", "repeatFileDiffApi." + fileSuffix, MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
            paramMap = new LinkedMultiValueMap<>();
            paramMap.add("request", JSON.toJSONString(request));
            paramMap.add("file", file);
            this.requestMultipartWithOkAndReturn(IMPORT, paramMap);
            newApiDefinition = apiDefinitionImportTestService.selectApiDefinitionByProjectId(importProject.getId());
            apiDefinitionImportTestService.checkApiModuleChange(oldApiDefinition, newApiDefinition, 0);
            newApiBlobList = apiDefinitionImportTestService.selectBlobByProjectId(importProject.getId());
            apiDefinitionImportTestService.compareApiBlobList(apiBlobList, newApiBlobList, 0);

            //删除本次导入的数据
            List<String> apiIds = newApiDefinition.stream().map(ApiDefinition::getId).collect(Collectors.toList());
            apiDefinitionService.handleDeleteApiDefinition(apiIds, true, request.getProjectId(), "admin", true);
            apiDefinitionService.handleTrashDelApiDefinition(apiIds, "admin", importProject.getId(), true);
            newApiDefinition = apiDefinitionImportTestService.selectApiDefinitionByProjectId(importProject.getId());
            newApiBlobList = apiDefinitionImportTestService.selectBlobByProjectId(importProject.getId());
            List<ApiTestCase> newApiTestCaseList = apiTestCaseMapper.selectByExample(apiTestCaseExample);
            ApiDefinitionModuleExample deleteModuleExample = new ApiDefinitionModuleExample();
            deleteModuleExample.createCriteria().andProjectIdEqualTo(importProject.getId());
            apiDefinitionModuleMapper.deleteByExample(deleteModuleExample);
            apiDefinitionModuleList = apiDefinitionModuleMapper.selectByExample(moduleExample);
            Assertions.assertEquals(apiDefinitionModuleList.size(), 0);
            Assertions.assertEquals(newApiDefinition.size(), 0);
            Assertions.assertEquals(newApiBlobList.size(), 0);
            Assertions.assertEquals(newApiTestCaseList.size(), 0);

        }

        // 最后测试一把JMeter。  5个请求，其中有4个重复的。 导入之后应该是2个请求5个用例，其中1个请求有4个用例
        File httpJmx = new File(
                this.getClass().getClassLoader().getResource("file/import/jmeter/post-page.jmx")
                        .getPath()
        );

        FileInputStream inputStream = new FileInputStream(new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/import/jmeter/post-page.jmx")).getPath()));
        MockMultipartFile file = new MockMultipartFile("file", "post-page.jmx", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        ImportRequest request = new ImportRequest();
        request.setProjectId(importProject.getId());
        request.setUserId("admin");
        request.setPlatform("Jmeter");
        request.setSyncCase(true);
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("file", file);
        this.requestMultipartWithOkAndReturn(IMPORT, paramMap);
        List<ApiDefinitionModule> apiDefinitionModuleList = apiDefinitionModuleMapper.selectByExample(moduleExample);
        Assertions.assertEquals(1, apiDefinitionModuleList.size());
        List<ApiDefinitionBlob> apiDefinitionBlobs = apiDefinitionImportTestService.selectBlobByProjectId(importProject.getId());
        Assertions.assertEquals(2, apiDefinitionBlobs.size());

        List<ApiTestCase> newApiTestCaseList = apiTestCaseMapper.selectByExample(apiTestCaseExample);
        Assertions.assertEquals(5, newApiTestCaseList.size());
        //去重处理
        List<String> apiDefinitionIdList = newApiTestCaseList.stream().map(ApiTestCase::getApiDefinitionId).distinct().collect(Collectors.toList());
        Assertions.assertEquals(2, apiDefinitionIdList.size());

        // 导入额外的har文件
        inputStream = new FileInputStream(new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/import/har/testjsandcss.har")).getPath()));
        file = new MockMultipartFile("file", "post-page.har", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        request.setPlatform("har");
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("file", file);
        this.requestMultipartWithOkAndReturn(IMPORT, paramMap);
        apiDefinitionBlobs = apiDefinitionImportTestService.selectBlobByProjectId(importProject.getId());
        Assertions.assertTrue(apiDefinitionBlobs.size() > 2);
    }

    @Resource
    private ExportTaskManager exportTaskManager;

    private void testExportAndImport(String exportProjectId, List<ApiDefinitionBlob> exportApiBlobs) throws Exception {
        ApiDefinitionBatchExportRequest exportRequest = new ApiDefinitionBatchExportRequest();
        String fileId = IDGenerator.nextStr();
        exportRequest.setProjectId(exportProjectId);
        exportRequest.setFileId(fileId);
        exportRequest.setSelectAll(true);
        exportRequest.setExportApiCase(true);
        exportRequest.setExportApiMock(true);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(EXPORT + "metersphere", exportRequest);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        String taskId = JSON.parseObject(returnData, ResultHolder.class).getData().toString();
        Assertions.assertTrue(StringUtils.isNotBlank(fileId));
        List<ExportTask> taskList = exportTaskManager.getExportTasks(exportProjectId, null, null, "admin", fileId);
        while (CollectionUtils.isEmpty(taskList)) {
            Thread.sleep(1000);
            taskList = exportTaskManager.getExportTasks(exportProjectId, null, null, "admin", fileId);
        }
        ExportTask task = taskList.getFirst();
        while (!StringUtils.equalsIgnoreCase(task.getState(), ExportConstants.ExportState.SUCCESS.name())) {
            Thread.sleep(1000);
            task = exportTaskManager.getExportTasks(exportProjectId, null, null, "admin", fileId).getFirst();
        }

        mvcResult = this.download(exportProjectId, fileId);

        byte[] fileBytes = mvcResult.getResponse().getContentAsByteArray();

        File zipFile = new File("/tmp/api-export/downloadFiles.zip");
        FileUtils.writeByteArrayToFile(zipFile, fileBytes);

        File[] files = MsFileUtils.unZipFile(zipFile, "/tmp/api-export/unzip/");
        assert files != null;
        Assertions.assertEquals(files.length, 1);
        String fileContent = FileUtils.readFileToString(files[0], StandardCharsets.UTF_8);

        MetersphereApiDefinitionExportResponse exportResponse = ApiDataUtils.parseObject(fileContent, MetersphereApiDefinitionExportResponse.class);

        apiDefinitionImportTestService.compareApiExport(exportResponse, exportApiBlobs);

        MsFileUtils.deleteDir("/tmp/api-export/");

        //测试stop
        this.requestGetWithOk("/stop/" + taskId);
    }

    private MvcResult download(String projectId, String fileId) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get("/api/definition/download/file/" + projectId + "/" + fileId)
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)).andReturn();
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
                {"example":null,"id":null,"title":null,"type":"object","description":null,"items":null,"properties":{"id":{"example":10,"id":null,"title":null,"type":"integer","description":"","items":null,"properties":null,"additionalProperties":null,"required":null,"pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"schema":null,"format":"int64","enumString":null,"enumInteger":null,"enumNumber":null,"extensions":null},"name":{"example":"@string","id":null,"title":null,"type":"string","description":"","items":null,"properties":null,"additionalProperties":null,"required":null,"pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"schema":null,"format":"","enumString":null,"enumInteger":null,"enumNumber":null,"extensions":null},"category":{"example":null,"id":null,"title":null,"type":"object","description":null,"items":null,"properties":{"id":{"example":"@integer","id":null,"title":null,"type":"integer","description":"","items":null,"properties":null,"additionalProperties":null,"required":null,"pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"schema":null,"format":"int64","enumString":null,"enumInteger":null,"enumNumber":null,"extensions":null},"name":{"example":"Dogs","id":null,"title":null,"type":"string","description":"","items":null,"properties":null,"additionalProperties":null,"required":null,"pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"schema":null,"format":"","enumString":null,"enumInteger":null,"enumNumber":null,"extensions":null}},"additionalProperties":null,"required":null,"pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"schema":null,"format":null,"enumString":null,"enumInteger":null,"enumNumber":null,"extensions":null},"photoUrls":{"example":null,"id":null,"title":null,"type":"array","description":null,"items":[{"example":null,"id":null,"title":null,"type":"string","description":"","items":null,"properties":null,"additionalProperties":null,"required":null,"pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"schema":null,"format":"","enumString":null,"enumInteger":null,"enumNumber":null,"extensions":null}],"properties":null,"additionalProperties":null,"required":null,"pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"schema":null,"format":null,"enumString":null,"enumInteger":null,"enumNumber":null,"extensions":null},"tags":{"example":null,"id":null,"title":null,"type":"array","description":null,"items":[{"example":null,"id":null,"title":null,"type":"object","description":null,"items":null,"properties":{"id":{"example":null,"id":null,"title":null,"type":"integer","description":"","items":null,"properties":null,"additionalProperties":null,"required":null,"pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"schema":null,"format":"int64","enumString":null,"enumInteger":null,"enumNumber":null,"extensions":null},"name":{"example":null,"id":null,"title":null,"type":"string","description":"","items":null,"properties":null,"additionalProperties":null,"required":null,"pattern":null,"maxLength":null,"minLength":null,"mainimum":null,"maximum":null,"schema":null,"format":"","enumString":null,"enumInteger":null,"enumNumber":null,"extensions":null}},"additionalProperties":null,"required":null,"pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"schema":null,"format":null,"enumString":null,"enumInteger":null,"enumNumber":null,"extensions":null}],"properties":null,"additionalProperties":null,"required":null,"pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"schema":null,"format":null,"enumString":null,"enumInteger":null,"enumNumber":null,"extensions":null},"status":{"example":"available","id":null,"title":null,"type":"string","description":"pet status in the store","items":null,"properties":null,"additionalProperties":null,"required":null,"pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"schema":null,"format":"","enumString":["available","pending","sold"],"enumInteger":null,"enumNumber":null,"extensions":null},"testnumber":{"example":1.23139183198000000283719387,"id":null,"title":null,"type":"number","description":"pet status in the store","items":null,"properties":null,"additionalProperties":null,"required":null,"pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"schema":null,"format":"","enumString":["available","pending","sold"],"enumInteger":null,"enumNumber":null,"extensions":null},"testnumber11":{"example":"@number","id":null,"title":null,"type":"number","description":"pet status in the store","items":null,"properties":null,"additionalProperties":null,"required":null,"pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"schema":null,"format":"","enumString":["available","pending","sold"],"enumInteger":null,"enumNumber":null,"extensions":null},"testfalse":{"example":"@boolean","id":null,"title":null,"type":"boolean","description":"pet status in the store","items":null,"properties":null,"additionalProperties":null,"required":null,"pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"schema":null,"format":"","enumString":["available","pending","sold"],"enumInteger":null,"enumNumber":null,"extensions":null},"testfalse":{"example":false,"id":null,"title":null,"type":"boolean","description":"pet status in the store","items":null,"properties":null,"additionalProperties":null,"required":null,"pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"schema":null,"format":"","enumString":["available","pending","sold"],"enumInteger":null,"enumNumber":null,"extensions":null},"testnull":{"example":null,"id":null,"title":null,"type":"null","description":"pet status in the store","items":null,"properties":null,"additionalProperties":null,"required":null,"pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"schema":null,"format":"","enumInteger":null,"enumNumber":null,"extensions":null},"testass": null},"additionalProperties":null,"required":["name","photoUrls"],"pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"schema":null,"format":null,"enumString":null,"enumInteger":null,"enumNumber":null,"extensions":null}
               """;
        //正常数据;
        requestPostWithOk(JSON_SCHEMA_PREVIEW, JSON.parseObject(jsonString, JsonSchemaItem.class));
        //正常array数据
        String jsonArray = """
                {"example":null,"id":null,"title":null,"type":"array","description":null,"items":{"example":null,"id":null,"title":null,"type":"object","description":null,"items":null,"properties":{"id":{"example":"@integer","id":null,"title":null,"type":"integer","description":"","items":null,"properties":null,"additionalProperties":null,"required":null,"pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"schema":null,"format":"int64","enumString":null,"enumInteger":null,"enumNumber":null,"extensions":null},"name":{"example":null,"id":null,"title":null,"type":"string","description":"","items":null,"properties":null,"additionalProperties":null,"required":null,"pattern":null,"maxLength":null,"minLength":null,"mainimum":null,"maximum":null,"schema":null,"format":"","enumString":null,"enumInteger":null,"enumNumber":null,"extensions":null}},"additionalProperties":null,"required":null,"pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"schema":null,"format":null,"enumString":null,"enumInteger":null,"enumNumber":null,"extensions":null},"properties":null,"additionalProperties":null,"required":null,"pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"schema":null,"format":null,"enumString":null,"enumInteger":null,"enumNumber":null,"extensions":null}
                """;
        requestPostWithOk(JSON_SCHEMA_PREVIEW, JSON.parseObject(jsonArray, JsonSchemaItem.class));

        // 校验转换是否正确
        String schema = """
                {"type":"object","enable":true,"properties":{"array":{"type":"array","enable":true,"items":[{"type":"string","example":"1","enable":true},{"type":"number","example":"2","enable":true}]},"string":{"type":"string","example":"stringValue","enable":true},"int":{"type":"integer","example":"1","enable":true},"num":{"type":"number","example":"1.00","enable":true},"boolean":{"type":"boolean","example":"booleanValue","enable":true},"null":{"type":"null","enable":true},"":{"type":"string","enable":true}}}
                """;
        String jsonResult = """
                {
                  "array" : [ "1", 2 ],
                  "string" : "stringValue",
                  "int" : 1,
                  "num" : 1.00,
                  "boolean" : false,
                  "null" : null
                }
                """;
        MvcResult mvcResult = requestPostWithOkAndReturn(JSON_SCHEMA_PREVIEW, JSON.parseObject(schema, JsonSchemaItem.class));
        String resultData = getResultData(mvcResult, String.class);
        Assertions.assertEquals(JSON.parseObject(jsonResult), JSON.parseObject(resultData));

        // 校验根节点数组是否正确
        schema = """
                {"type":"array","enable":true,"items":[{"type":"array","enable":true,"items":[{"type":"string","example":"1","description":"","additionalProperties":null,"defaultValue":"","pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enable":true},{"type":"number","example":"2","description":"","additionalProperties":null,"defaultValue":"","pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enable":true}]},{"type":"string","example":"stringValue","description":"","additionalProperties":null,"defaultValue":"","pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enable":true},{"type":"integer","example":"intValue","description":"","additionalProperties":null,"defaultValue":"","pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enable":true},{"type":"number","example":"1.00","description":"","additionalProperties":null,"defaultValue":"","pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enable":true},{"type":"boolean","example":"booleanValue","description":"","additionalProperties":null,"defaultValue":"","pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enable":true},{"type":"null","enable":true}]}
                """;
        jsonResult = """
                [
                   [
                     "1",
                     2
                   ],
                   "stringValue",
                   0,
                   1.00,
                   false,
                   null
                ]
                """;
        mvcResult = requestPostWithOkAndReturn(JSON_SCHEMA_PREVIEW, JSON.parseObject(schema, JsonSchemaItem.class));
        resultData = getResultData(mvcResult, String.class);
        Assertions.assertEquals(JSON.parseObject(jsonResult), JSON.parseObject(resultData));

        // 校验禁用
        schema = """
                {"type":"object","enable":false}
                """;
        mvcResult = requestPostWithOkAndReturn(JSON_SCHEMA_PREVIEW, JSON.parseObject(schema, JsonSchemaItem.class));
        Assertions.assertEquals(getResultData(mvcResult, String.class), "{}");

        // 校验禁用
        schema = """
                {"type":"object","enable":true,"properties":{"array":{"type":"array","enable":false,"items":[{"type":"string","example":"1","enable":false},{"type":"number","example":"2","enable":false}]},"string":{"type":"string","example":"stringValue","enable":false},"int":{"type":"integer","example":"1","enable":false},"num":{"type":"number","example":"1.00","enable":false},"boolean":{"type":"boolean","example":"booleanValue","enable":false},"null":{"type":"null","enable":false}}}
                """;
        mvcResult = requestPostWithOkAndReturn(JSON_SCHEMA_PREVIEW, JSON.parseObject(schema, JsonSchemaItem.class));
        resultData = getResultData(mvcResult, String.class);
        Assertions.assertEquals(JSON.parseObject("{}"), JSON.parseObject(resultData));
    }

    @Test
    @Order(104)
    public void testJsonSchemaAutoGenerate() throws Exception {
        String jsonString = """
                {"id":null,"title":null,"example":null,"type":"object","description":null,"items":null,"properties":{"array":{"id":null,"title":null,"example":null,"type":"array","description":null,"items":[{"id":null,"title":null,"example":"","type":"string","description":"","items":null,"properties":null,"additionalProperties":null,"required":null,"defaultValue":"","pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enumValues":null,"enable":true},{"id":null,"title":null,"example":"","type":"number","description":"","items":null,"properties":null,"additionalProperties":null,"required":null,"defaultValue":"","pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enumValues":null,"enable":true}],"properties":null,"additionalProperties":null,"required":null,"defaultValue":null,"pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enumValues":null,"enable":true},"string":{"id":null,"title":null,"example":"","type":"string","description":"","items":null,"properties":null,"additionalProperties":null,"required":null,"defaultValue":"","pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enumValues":null,"enable":true},"int":{"id":null,"title":null,"example":"","type":"integer","description":"","items":null,"properties":null,"additionalProperties":null,"required":null,"defaultValue":"","pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enumValues":null,"enable":true},"num":{"id":null,"title":null,"example":"","type":"number","description":"","items":null,"properties":null,"additionalProperties":null,"required":null,"defaultValue":"","pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enumValues":null,"enable":true},"boolean":{"id":null,"title":null,"example":"","type":"boolean","description":"","items":null,"properties":null,"additionalProperties":null,"required":null,"defaultValue":"","pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enumValues":null,"enable":true},"null":{"id":null,"title":null,"example":null,"type":"null","description":null,"items":null,"properties":null,"additionalProperties":null,"required":null,"defaultValue":null,"pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enumValues":null,"enable":true}},"additionalProperties":null,"required":null,"defaultValue":null,"pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enumValues":null,"enable":true}
                """;
        // 默认生成
        requestPost(JSON_SCHEMA_AUTO_GENERATE, JSON.parseObject(jsonString, JsonSchemaItem.class));
        // 带枚举值
        jsonString = """
                {"type":"object","enable":true,"properties":{"array":{"type":"array","enable":true,"items":[{"type":"string","example":"","description":"","additionalProperties":null,"defaultValue":"","pattern":null,"maxLength":4,"minLength":0,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enable":true,"regex":"^[A-Z]"},{"type":"number","example":"","description":"","additionalProperties":null,"defaultValue":"","pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enable":true}]},"string":{"type":"string","example":"","description":"","additionalProperties":null,"defaultValue":"","pattern":null,"maxLength":5,"minLength":1,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enumValues":["11111","22222"],"enable":true},"int":{"type":"integer","example":"","description":"","additionalProperties":null,"defaultValue":"","pattern":null,"maxLength":null,"minLength":null,"minimum":0,"maximum":4,"maxItems":null,"minItems":null,"format":null,"enumValues":["111","2333","444"],"enable":true},"num":{"type":"number","example":"","description":"","additionalProperties":null,"defaultValue":"","pattern":null,"maxLength":null,"minLength":null,"minimum":0,"maximum":5,"maxItems":null,"minItems":null,"format":null,"enumValues":["111","222","3333"],"enable":true},"boolean":{"type":"boolean","example":"","description":"","additionalProperties":null,"defaultValue":"","pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enable":true},"null":{"type":"null","enable":true}}}
                """;
        requestPostWithOk(JSON_SCHEMA_AUTO_GENERATE, JSON.parseObject(jsonString, JsonSchemaItem.class));

        // 默认值和正则
        jsonString = """
                {"id":null,"title":null,"example":null,"type":"object","description":null,"items":null,"properties":{"array":{"id":null,"title":null,"example":null,"type":"array","description":null,"items":[{"id":null,"title":null,"example":"","type":"string","description":"","items":null,"properties":null,"additionalProperties":null,"required":null,"defaultValue":"默认值","pattern":null,"maxLength":4,"minLength":0,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enumValues":null,"enable":true},{"id":null,"title":null,"example":"","type":"number","description":"","items":null,"properties":null,"additionalProperties":null,"required":null,"defaultValue":"","pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enumValues":null,"enable":true}],"properties":null,"additionalProperties":null,"required":null,"defaultValue":null,"pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enumValues":null,"enable":true},"string":{"id":null,"title":null,"example":"","type":"string","description":"","items":null,"properties":null,"additionalProperties":null,"required":null,"defaultValue":"","pattern":"[A-Z0-9_]+","maxLength":5,"minLength":1,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enumValues":null,"enable":true},"int":{"id":null,"title":null,"example":"","type":"integer","description":"","items":null,"properties":null,"additionalProperties":null,"required":null,"defaultValue":3,"pattern":null,"maxLength":null,"minLength":null,"minimum":0,"maximum":4,"maxItems":null,"minItems":null,"format":null,"enumValues":null,"enable":true},"num":{"id":null,"title":null,"example":"","type":"number","description":"","items":null,"properties":null,"additionalProperties":null,"required":null,"defaultValue":2,"pattern":null,"maxLength":null,"minLength":null,"minimum":0,"maximum":5,"maxItems":null,"minItems":null,"format":null,"enumValues":null,"enable":true},"boolean":{"id":null,"title":null,"example":"","type":"boolean","description":"","items":null,"properties":null,"additionalProperties":null,"required":null,"defaultValue":"true","pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enumValues":null,"enable":true},"null":{"id":null,"title":null,"example":null,"type":"null","description":null,"items":null,"properties":null,"additionalProperties":null,"required":null,"defaultValue":null,"pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enumValues":null,"enable":true}},"additionalProperties":null,"required":null,"defaultValue":null,"pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enumValues":null,"enable":true}
                """;
        requestPostWithOk(JSON_SCHEMA_AUTO_GENERATE, JSON.parseObject(jsonString, JsonSchemaItem.class));

        // 长度截取
        jsonString = """
                {"type":"object","enable":true,"properties":{"arraywww":{"type":"array","enable":true,"items":[{"type":"string","example":"1","description":"","additionalProperties":null,"defaultValue":"","pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enable":true},{"type":"number","example":"2","description":"","additionalProperties":null,"defaultValue":"","pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enable":true}],"minItems":null,"maxItems":null},"string":{"type":"string","example":"@natural(1,100)","description":"","additionalProperties":null,"defaultValue":"","pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enable":true},"int":{"type":"integer","example":"intValue","description":"","additionalProperties":null,"defaultValue":"","pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enable":true},"num":{"type":"number","example":"","description":"","additionalProperties":null,"defaultValue":"","pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enable":true},"boolean":{"type":"boolean","example":"booleanValue","description":"","additionalProperties":null,"defaultValue":"","pattern":null,"maxLength":null,"minLength":null,"minimum":null,"maximum":null,"maxItems":null,"minItems":null,"format":null,"enable":true},"null":{"type":"null","enable":true},"默认值大于最大长度":{"type":"string","example":"","description":"","enable":true,"defaultValue":"sdfdsd","maxLength":1,"minLength":0},"默认值小于最小长度":{"type":"string","example":"","description":"","enable":true,"defaultValue":"1","maxLength":3,"minLength":2},"数组项小于最小项":{"type":"array","enable":true,"items":[],"minItems":2,"maxItems":3},"数组项大于最大项":{"type":"array","enable":true,"items":[null,null],"minItems":0,"maxItems":1},"正则大于最大长度":{"type":"string","example":"","description":"","enable":true,"defaultValue":"","maxLength":2,"minLength":0,"pattern":"[A-Z]{4}"}}}
                """;
        requestPostWithOk(JSON_SCHEMA_AUTO_GENERATE, JSON.parseObject(jsonString, JsonSchemaItem.class));

        // 字符串自动生成
        jsonString = """
                {"type":"object","enable":true,"properties":{"默认8位":{"type":"string","example":"","description":"","enable":true,"defaultValue":"","format":"date-time"},"最小2位":{"type":"string","example":"","description":"","enable":true,"defaultValue":"","minLength":2},"最大4":{"type":"string","example":"","description":"","enable":true,"defaultValue":"","maxLength":4},"最小2位，最大4":{"type":"string","example":"","description":"","enable":true,"defaultValue":"","maxLength":4,"minLength":2}}}
                """;
        requestPostWithOk(JSON_SCHEMA_AUTO_GENERATE, JSON.parseObject(jsonString, JsonSchemaItem.class));
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

    @Test
    @Order(104)
    public void testExport() throws Exception {
        ApiDefinitionBatchRequest request = new ApiDefinitionBatchRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setProtocols(List.of("HTTP"));
        request.setSelectAll(false);
        request.setSelectIds(List.of("1002"));
        this.requestPost(EXPORT + "swagger", request);
    }

}
