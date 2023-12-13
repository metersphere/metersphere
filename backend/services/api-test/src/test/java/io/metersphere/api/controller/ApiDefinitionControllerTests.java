package io.metersphere.api.controller;

import io.metersphere.api.constants.ApiDefinitionStatus;
import io.metersphere.api.controller.result.ApiResultCode;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.enums.ApiDefinitionDocType;
import io.metersphere.api.mapper.*;
import io.metersphere.api.service.ApiFileResourceService;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.dto.filemanagement.FileInfo;
import io.metersphere.project.dto.filemanagement.request.FileUploadRequest;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.project.service.FileAssociationService;
import io.metersphere.project.service.FileMetadataService;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.util.*;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.sdk.BaseCondition;
import io.metersphere.system.file.FileCenter;
import io.metersphere.system.file.FileRequest;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.*;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.metersphere.api.controller.result.ApiResultCode.API_DEFINITION_NOT_EXIST;
import static io.metersphere.system.controller.handler.result.MsHttpResultCode.NOT_FOUND;


@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ApiDefinitionControllerTests extends BaseTest {

    private static final String BASE_PATH = "/api/definition/";
    private final static String ADD = BASE_PATH + "add";
    private final static String UPDATE = BASE_PATH + "update";
    private final static String BATCH_UPDATE = BASE_PATH + "batch-update";
    private final static String DELETE = BASE_PATH + "delete";
    private final static String BATCH_DELETE = BASE_PATH + "batch-del";
    private final static String COPY = BASE_PATH + "copy";
    private final static String BATCH_MOVE = BASE_PATH + "batch-move";

    private final static String RESTORE = BASE_PATH + "restore";
    private final static String BATCH_RESTORE = BASE_PATH + "batch-restore";

    private final static String TRASH_DEL = BASE_PATH + "trash-del";
    private final static String BATCH_TRASH_DEL = BASE_PATH + "batch-trash-del";

    private final static String PAGE = BASE_PATH + "page";
    private final static String PAGE_DOC = BASE_PATH + "page-doc";
    private final static String DOC = BASE_PATH + "doc";
    private static final String GET = BASE_PATH + "get-detail/";
    private static final String FOLLOW = BASE_PATH + "follow/";
    private static final String VERSION = BASE_PATH + "version/";
    private static final String UPLOAD_TEMP_FILE = BASE_PATH + "/upload/temp/file";

    private static final String DEFAULT_MODULE_ID = "10001";

    private static final String ALL_API = "api_definition_module.api.all";
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
    private ApiDefinitionModuleMapper apiDefinitionModuleMapper;

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
    public void testAdd() throws Exception {
        LogUtils.info("create api test");
        // 创建测试数据
        ApiDefinitionAddRequest request = createApiDefinitionAddRequest();
        MsHTTPElement msHttpElement = MsHTTPElementTest.getMsHttpElement();
        request.setRequest(ApiDataUtils.toJSONString(msHttpElement));
        List<HttpResponse> msHttpResponse = MsHTTPElementTest.getMsHttpResponse();
        request.setResponse(ApiDataUtils.toJSONString(msHttpResponse));

        uploadFileId = doUploadTempFile(getMockMultipartFile("file_upload.JPG"));
        request.setUploadFileIds(List.of(uploadFileId));
        request.setLinkFileIds(List.of(fileMetadataId));

        // 执行方法调用
        MvcResult mvcResult = this.requestPostWithOkAndReturn(ADD, request);
        // 校验请求成功数据
        ApiDefinition resultData = getResultData(mvcResult, ApiDefinition.class);
        apiDefinition = assertAddApiDefinition(request, msHttpElement, resultData.getId());
        assertUploadFile(apiDefinition.getId(), List.of(uploadFileId));
        assertLinkFile(apiDefinition.getId(), List.of(fileMetadataId));
        // 再插入一条数据，便于修改时重名校验
        request.setMethod("GET");
        request.setPath("/api/admin/posts");
        request.setUploadFileIds(null);
        request.setLinkFileIds(null);
        mvcResult = this.requestPostWithOkAndReturn(ADD, request);
        resultData = getResultData(mvcResult, ApiDefinition.class);
        assertAddApiDefinition(request, msHttpElement, resultData.getId());

        // @@重名校验异常
        assertErrorCode(this.requestPost(ADD, request), ApiResultCode.API_DEFINITION_EXIST);
        // 校验项目是否存在
        request.setProjectId("111");
        request.setName("test123");
        assertErrorCode(this.requestPost(ADD, request), NOT_FOUND);

        // @@校验日志
        checkLog(apiDefinition.getId(), OperationLogType.ADD);
        // @@异常参数校验
        createdGroupParamValidateTest(ApiDefinitionAddRequest.class, ADD);
        // @@校验权限
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setName("permission-st-6");
        request.setMethod("DELETE");
        request.setPath("/api/admin/posts");
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_ADD, ADD, request);
    }

    private ApiDefinitionAddRequest createApiDefinitionAddRequest() {
        // 创建并返回一个 ApiDefinitionAddRequest 对象，用于测试
        String defaultVersion = extBaseProjectVersionMapper.getDefaultVersion(DEFAULT_PROJECT_ID);
        ApiDefinitionAddRequest request = new ApiDefinitionAddRequest();
        request.setName("接口定义");
        request.setProtocol("HTTP");
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setMethod("POST");
        request.setPath("/api/admin/posts");
        request.setStatus(ApiDefinitionStatus.PREPARE.getValue());
        request.setModuleId("default");
        request.setVersionId(defaultVersion);
        request.setDescription("描述内容");
        request.setTags(new LinkedHashSet<>(List.of("tag1", "tag2")));
        return request;
    }

    private ApiDefinition assertAddApiDefinition(Object request, MsHTTPElement msHttpElement, String id) {
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(id);
        ApiDefinitionBlob apiDefinitionBlob = apiDefinitionBlobMapper.selectByPrimaryKey(id);
        ApiDefinition copyApiDefinition = BeanUtils.copyBean(new ApiDefinition(), apiDefinition);
        BeanUtils.copyBean(copyApiDefinition, request);
        Assertions.assertEquals(apiDefinition, copyApiDefinition);
        ApiDataUtils.setResolver(MsHTTPElement.class);
        if(apiDefinitionBlob != null){
            Assertions.assertEquals(msHttpElement, ApiDataUtils.parseObject(new String(apiDefinitionBlob.getRequest()), AbstractMsTestElement.class));
        }
        return apiDefinition;
    }

    @Test
    @Order(2)
    @Sql(scripts = {"/dml/init_api_definition.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void get() throws Exception {
        if(apiDefinition == null){
            apiDefinition = apiDefinitionMapper.selectByPrimaryKey("1001");
        }
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOkAndReturn(GET + apiDefinition.getId());
        ApiDataUtils.setResolver(MsHTTPElement.class);
        ApiDefinitionDTO apiDefinitionDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResult).get("data")), ApiDefinitionDTO.class);
        // 校验数据是否正确
        ApiDefinitionDTO copyApiDefinitionDTO = BeanUtils.copyBean(new ApiDefinitionDTO(), apiDefinition);
        ApiDefinitionBlob apiDefinitionBlob = apiDefinitionBlobMapper.selectByPrimaryKey(apiDefinition.getId());
        ApiDefinitionFollowerExample example = new ApiDefinitionFollowerExample();
        example.createCriteria().andApiDefinitionIdEqualTo(apiDefinition.getId()).andUserIdEqualTo("admin");
        List<ApiDefinitionFollower> followers = apiDefinitionFollowerMapper.selectByExample(example);
        copyApiDefinitionDTO.setFollow(CollectionUtils.isNotEmpty(followers));
        if(apiDefinitionBlob != null){
            copyApiDefinitionDTO.setRequest(ApiDataUtils.parseObject(new String(apiDefinitionBlob.getRequest()), AbstractMsTestElement.class));
            copyApiDefinitionDTO.setResponse(ApiDataUtils.parseArray(new String(apiDefinitionBlob.getResponse()), HttpResponse.class));
        }
        Assertions.assertEquals(apiDefinitionDTO, copyApiDefinitionDTO);

        assertErrorCode(this.requestGet(GET + "111"), API_DEFINITION_NOT_EXIST);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_READ, GET + apiDefinition.getId());
    }

    @Test
    @Order(3)
    @Sql(scripts = {"/dml/init_api_definition.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testUpdate() throws Exception {
        LogUtils.info("update api test");
        if(apiDefinition == null){
            apiDefinition = apiDefinitionMapper.selectByPrimaryKey("1001");
        }
        ApiDefinitionUpdateRequest request = new ApiDefinitionUpdateRequest();
        BeanUtils.copyBean(request, apiDefinition);
        request.setPath("test.com/admin/test");
        request.setName("test1test1test1test1test1test1");
        request.setMethod("POST");
        request.setModuleId("default1");
        request.setTags(new LinkedHashSet<>(List.of("tag1", "tag2-update")));
        MsHTTPElement msHttpElement = MsHTTPElementTest.getMsHttpElement();
        request.setRequest(ApiDataUtils.toJSONString(msHttpElement));
        List<HttpResponse> msHttpResponse = MsHTTPElementTest.getMsHttpResponse();
        request.setResponse(ApiDataUtils.toJSONString(msHttpResponse));

        // 清除文件的更新
        request.setUnLinkRefIds(List.of(fileMetadataId));
        request.setDeleteFileIds(List.of(uploadFileId));
        this.requestPostWithOk(UPDATE, request);
        // 校验请求成功数据
        apiDefinition = assertAddApiDefinition(request, msHttpElement, request.getId());
        assertUploadFile(apiDefinition.getId(), List.of());
        assertLinkFile(apiDefinition.getId(), List.of());

        // 带文件的更新
        String fileId = doUploadTempFile(getMockMultipartFile("file_upload.JPG"));
        request.setUploadFileIds(List.of(fileId));
        request.setLinkFileIds(List.of(fileMetadataId));
        request.setDeleteFileIds(null);
        request.setUnLinkRefIds(null);
        this.requestPostWithOk(UPDATE, request);
        // 校验请求成功数据
        apiDefinition = assertAddApiDefinition(request, msHttpElement, request.getId());
        assertUploadFile(apiDefinition.getId(), List.of(fileId));
        assertLinkFile(apiDefinition.getId(), List.of(fileMetadataId));

        // 删除了上一次上传的文件，重新上传一个文件
        request.setDeleteFileIds(List.of(fileId));
        String newFileId1 = doUploadTempFile(getMockMultipartFile("file_upload.JPG"));
        request.setUploadFileIds(List.of(newFileId1));
        request.setUnLinkRefIds(List.of(fileMetadataId));
        request.setLinkFileIds(List.of(fileMetadataId));
        this.requestPostWithOk(UPDATE, request);
        apiDefinition = assertAddApiDefinition(request, msHttpElement, request.getId());
        assertUploadFile(apiDefinition.getId(), List.of(newFileId1));
        assertLinkFile(apiDefinition.getId(), List.of(fileMetadataId));

        // 已有一个文件，再上传一个文件
        String newFileId2 = doUploadTempFile(getMockMultipartFile("file_update_upload.JPG"));
        request.setUploadFileIds(List.of(newFileId2));
        request.setUnLinkRefIds(null);
        request.setDeleteFileIds(null);
        request.setLinkFileIds(null);
        this.requestPostWithOk(UPDATE, request);
        apiDefinition = assertAddApiDefinition(request, msHttpElement, request.getId());
        assertUploadFile(apiDefinition.getId(), List.of(newFileId1, newFileId2));
        assertLinkFile(apiDefinition.getId(), List.of(fileMetadataId));

        // @@重名校验异常
        request.setModuleId("default");
        request.setPath("/api/admin/posts");
        request.setMethod("GET");
        request.setUploadFileIds(null);
        request.setLinkFileIds(null);
        request.setDeleteFileIds(null);
        request.setUnLinkRefIds(null);
        assertErrorCode(this.requestPost(UPDATE, request), ApiResultCode.API_DEFINITION_EXIST);

        // 校验数据是否存在
        request.setId("111");
        request.setName("test123");
        assertErrorCode(this.requestPost(UPDATE, request), API_DEFINITION_NOT_EXIST);

        // 校验项目是否存在
        request.setProjectId("111");
        request.setName("test123");
        assertErrorCode(this.requestPost(UPDATE, request), NOT_FOUND);

        // @@校验日志
        checkLog(apiDefinition.getId(), OperationLogType.UPDATE);
        // @@异常参数校验
        createdGroupParamValidateTest(ApiDefinitionUpdateRequest.class, UPDATE);
        // @@校验权限
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setName("permission-st-6");
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_UPDATE, UPDATE, request);
    }

    /**
     * 文件管理插入一条数据
     * 便于测试关联文件
     */
    private void uploadFileMetadata() throws Exception {
        FileUploadRequest fileUploadRequest = new FileUploadRequest();
        fileUploadRequest.setProjectId(DEFAULT_PROJECT_ID);
        //导入正常文件
        MockMultipartFile file = new MockMultipartFile("file", "file_update_upload.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, "aa".getBytes());
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
        List<String> linkFileIds = fileAssociationService.getFiles(id, FileAssociationSourceUtil.SOURCE_TYPE_API_DEFINITION)
                .stream()
                .map(FileInfo::getFileId)
                .toList();
        Assertions.assertEquals(fileIds, linkFileIds);
    }

    @Test
    @Order(4)
    @Sql(scripts = {"/dml/init_api_definition.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testBatchUpdate() throws Exception {
        LogUtils.info("batch update api test");
        ApiDefinitionBatchUpdateRequest apiDefinitionBatchUpdateRequest = new ApiDefinitionBatchUpdateRequest();
        apiDefinitionBatchUpdateRequest.setProjectId(DEFAULT_PROJECT_ID);
        apiDefinitionBatchUpdateRequest.setSelectIds(List.of("1001","1002","1005"));
        apiDefinitionBatchUpdateRequest.setExcludeIds(List.of("1005"));
        apiDefinitionBatchUpdateRequest.setSelectAll(false);
        apiDefinitionBatchUpdateRequest.setType("tags");
        // 修改标签，追加
        apiDefinitionBatchUpdateRequest.setSelectIds(List.of("1001","1002"));
        apiDefinitionBatchUpdateRequest.setTags(new LinkedHashSet<>(List.of("tag-append","tag-append1")));
        apiDefinitionBatchUpdateRequest.setAppend(true);
        this.requestPostWithOk(BATCH_UPDATE, apiDefinitionBatchUpdateRequest);
        assertBatchUpdateApiDefinition(apiDefinitionBatchUpdateRequest, List.of("1001","1002"));
        // 修改标签，覆盖
        apiDefinitionBatchUpdateRequest.setSelectIds(List.of("1003","1004"));
        apiDefinitionBatchUpdateRequest.setTags(new LinkedHashSet<>(List.of("tag-append","tag-append1")));
        apiDefinitionBatchUpdateRequest.setAppend(false);
        this.requestPostWithOk(BATCH_UPDATE, apiDefinitionBatchUpdateRequest);
        assertBatchUpdateApiDefinition(apiDefinitionBatchUpdateRequest, List.of("1003","1004"));
        // 修改协议类型
        apiDefinitionBatchUpdateRequest.setType("method");
        apiDefinitionBatchUpdateRequest.setMethod("batch-method");
        this.requestPostWithOk(BATCH_UPDATE, apiDefinitionBatchUpdateRequest);
        // 修改状态
        apiDefinitionBatchUpdateRequest.setType("status");
        apiDefinitionBatchUpdateRequest.setStatus(ApiDefinitionStatus.DEBUGGING.getValue());
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

        assertErrorCode(this.requestPost(BATCH_UPDATE, apiDefinitionBatchUpdateRequest), NOT_FOUND);

        // @@校验日志
        checkLog("1001", OperationLogType.UPDATE);
        checkLog("1002", OperationLogType.UPDATE);
        checkLog("1003", OperationLogType.UPDATE);
        checkLog("1004", OperationLogType.UPDATE);
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
            if(request.getStatus() != null){
                Assertions.assertEquals(item.getStatus(), request.getStatus());
            }
            if(request.getMethod() != null){
                Assertions.assertEquals(item.getMethod(), request.getMethod());
            }
            if(request.getVersionId() != null) {
                Assertions.assertEquals(item.getVersionId(), request.getVersionId());
            }
            if(request.getTags() != null) {
                if (request.isAppend()) {
                    Assertions.assertTrue(JSON.parseArray(item.getTags(), String.class).containsAll(request.getTags()));
                } else {
                    Assertions.assertEquals(item.getTags(), JSON.toJSONString(request.getTags()));
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
        if(!sourceFiles.isEmpty() && !copyFiles.isEmpty()){
            Assertions.assertEquals(sourceFiles.size(), copyFiles.size());
        }
        Assertions.assertTrue(resultData.getName().contains("copy_"));
        // @@校验日志
        checkLog(resultData.getId(), OperationLogType.UPDATE);

        request.setId("1001");
        MvcResult mvcResultCopy = this.requestPostWithOkAndReturn(COPY, request);
        ApiDefinition resultDataCopy = getResultData(mvcResultCopy, ApiDefinition.class);
        // @数据验证
        Assertions.assertTrue(resultDataCopy.getName().contains("copy_"));

        request.setId("121");
        assertErrorCode(this.requestPost(COPY, request), API_DEFINITION_NOT_EXIST);
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
        request.setSelectIds(List.of("1001","1002","1005"));
        request.setExcludeIds(List.of("1005"));
        request.setDeleteAll(false);
        request.setSelectAll(false);
        this.requestPostWithOkAndReturn(BATCH_MOVE, request);
        // @@校验日志
        checkLog("1001", OperationLogType.UPDATE);
        checkLog("1002", OperationLogType.UPDATE);
        checkLog("1005", OperationLogType.UPDATE);
        // 移动全部 条件为关键字为st-6的数据
        request.setSelectAll(true);
        BaseCondition baseCondition = new BaseCondition();
        baseCondition.setKeyword("st-6");
        request.setCondition(baseCondition);
        this.requestPostWithOkAndReturn(BATCH_MOVE, request);
        // @@校验日志
        checkLog("1006", OperationLogType.UPDATE);
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
        checkLog(apiDefinition.getId(), OperationLogType.UPDATE);
        assertErrorCode(this.requestGet(FOLLOW + "111"), API_DEFINITION_NOT_EXIST);

        // @@取消关注
        // @@请求成功
        this.requestGetWithOk(FOLLOW + apiDefinition.getId());
        ApiDefinitionFollowerExample unFollowerExample = new ApiDefinitionFollowerExample();
        example.createCriteria().andApiDefinitionIdEqualTo(apiDefinition.getId()).andUserIdEqualTo("admin");
        List<ApiDefinitionFollower> unFollowers = apiDefinitionFollowerMapper.selectByExample(unFollowerExample);
        Assertions.assertTrue(CollectionUtils.isEmpty(unFollowers));
        // @@校验日志
        checkLog(apiDefinition.getId(), OperationLogType.UPDATE);
        assertErrorCode(this.requestGet(FOLLOW + "111"), API_DEFINITION_NOT_EXIST);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_UPDATE, FOLLOW + apiDefinition.getId());
    }

    @Test
    @Order(8)
    public void version() throws Exception {
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey("1001");
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOk(VERSION + apiDefinition.getId()).andReturn();
        ApiDataUtils.setResolver(MsHTTPElement.class);
        List<ApiDefinitionVersionDTO> apiDefinitionVersionDTO = getResultDataArray(mvcResult, ApiDefinitionVersionDTO.class);
        // 校验数据是否正确
        List<ApiDefinitionVersionDTO> copyApiDefinitionVersionDTO = extApiDefinitionMapper.getApiDefinitionByRefId(apiDefinition.getRefId());
        Assertions.assertEquals(apiDefinitionVersionDTO, copyApiDefinitionVersionDTO);
        assertErrorCode(this.requestGet(VERSION + "111"), API_DEFINITION_NOT_EXIST);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_READ, VERSION + apiDefinition.getId());
    }

    @Test
    @Order(9)
    @Sql(scripts = {"/dml/init_api_definition.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void getPage() throws Exception {
        doApiDefinitionPage("All", PAGE);
        doApiDefinitionPage("KEYWORD", PAGE);
        doApiDefinitionPage("FILTER", PAGE);
        doApiDefinitionPage("COMBINE", PAGE);
        doApiDefinitionPage("DELETED", PAGE);
    }

    private void doApiDefinitionPage(String search, String url) throws Exception {
        ApiDefinitionPageRequest request = new ApiDefinitionPageRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setCurrent(1);
        request.setPageSize(10);
        request.setDeleted(false);
        request.setSort(Map.of("createTime", "asc"));
        // "ALL", "KEYWORD", "FILTER", "COMBINE", "DELETED"
        switch (search) {
            case "ALL" -> configureAllSearch(request);
            case "KEYWORD" -> configureKeywordSearch(request);
            case "FILTER" -> configureFilterSearch(request);
            case "COMBINE" -> configureCombineSearch(request);
            case "DELETED" -> configureDeleteSearch(request);
            default -> {}
        }

        MvcResult mvcResult = this.requestPostWithOkAndReturn(url, request);
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
        request.setSort(Map.of("updateTime", "asc"));
        filters.put("status", Arrays.asList("Underway", "Completed"));
        filters.put("method", List.of("GET"));
        filters.put("version_id", List.of("1005704995741369851"));
        request.setFilter(filters);
    }

    private void configureCombineSearch(ApiDefinitionPageRequest request) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", Map.of("operator", "like", "value", "test-1"));
        map.put("method", Map.of("operator", "in", "value", Arrays.asList("GET", "POST")));
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
        request.setType(ApiDefinitionDocType.API.name());
        // @@请求成功
        MvcResult mvcResult = this.requestPostWithOkAndReturn(DOC, request);
        ApiDataUtils.setResolver(MsHTTPElement.class);
        ApiDefinitionDocDTO apiDefinitionDocDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResult).get("data")), ApiDefinitionDocDTO.class);
        // 校验数据是否正确
        ApiDefinitionDocDTO copyApiDefinitionDocDTO = new ApiDefinitionDocDTO();
        ApiDefinitionDTO copyApiDefinitionDTO = BeanUtils.copyBean(new ApiDefinitionDTO(), apiDefinition);
        ApiDefinitionBlob apiDefinitionBlob = apiDefinitionBlobMapper.selectByPrimaryKey(apiDefinition.getId());
        if(apiDefinitionBlob != null){
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
        assertErrorCode(this.requestPost(DOC, request), API_DEFINITION_NOT_EXIST);

        // @@模块查看文档
        request.setApiId(null);
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setType(ApiDefinitionDocType.MODULE.name());
        request.setModuleIds(List.of("10001"));
        MvcResult mvcResultModule = this.requestPostWithOkAndReturn(DOC, request);
        ApiDataUtils.setResolver(MsHTTPElement.class);
        ApiDefinitionDocDTO moduleApiDefinitionDocDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResultModule).get("data")), ApiDefinitionDocDTO.class);
        // 校验数据是否正确
        ApiDefinitionDocDTO copyModuleApiDefinitionDocDTO = new ApiDefinitionDocDTO();
        List<ApiDefinitionDTO> list = extApiDefinitionMapper.listDoc(request);
        if(null != list){
            ApiDefinitionDTO first = list.stream().findFirst().orElseThrow(() -> new MSException(API_DEFINITION_NOT_EXIST));
            ApiDefinitionBlob moduleApiDefinitionBlob = apiDefinitionBlobMapper.selectByPrimaryKey(first.getId());
            if(moduleApiDefinitionBlob != null){
                first.setRequest(ApiDataUtils.parseObject(new String(moduleApiDefinitionBlob.getRequest()), AbstractMsTestElement.class));
                first.setResponse(ApiDataUtils.parseArray(new String(moduleApiDefinitionBlob.getResponse()), HttpResponse.class));
            }
            ApiDefinitionModule apiDefinitionModule = apiDefinitionModuleMapper.selectByPrimaryKey(first.getModuleId());
            if(StringUtils.isBlank(copyModuleApiDefinitionDocDTO.getDocTitle())){
                copyModuleApiDefinitionDocDTO.setDocTitle(apiDefinitionModule.getName());
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
        ApiDataUtils.setResolver(MsHTTPElement.class);
        ApiDefinitionDocDTO allApiDefinitionDocDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResultAll).get("data")), ApiDefinitionDocDTO.class);
        // 校验数据是否正确
        ApiDefinitionDocDTO copyAllApiDefinitionDocDTO = new ApiDefinitionDocDTO();
        List<ApiDefinitionDTO> allList = extApiDefinitionMapper.listDoc(request);
        if(null != allList){
            ApiDefinitionDTO info = allList.stream().findFirst().orElseThrow(() -> new MSException(API_DEFINITION_NOT_EXIST));
            ApiDefinitionBlob allApiDefinitionBlob = apiDefinitionBlobMapper.selectByPrimaryKey(info.getId());
            if(allApiDefinitionBlob != null){
                info.setRequest(ApiDataUtils.parseObject(new String(allApiDefinitionBlob.getRequest()), AbstractMsTestElement.class));
                info.setResponse(ApiDataUtils.parseArray(new String(allApiDefinitionBlob.getResponse()), HttpResponse.class));
            }
            if(StringUtils.isBlank(copyAllApiDefinitionDocDTO.getDocTitle())){
                copyAllApiDefinitionDocDTO.setDocTitle(Translator.get(ALL_API));
            }
            copyAllApiDefinitionDocDTO.setType(ApiDefinitionDocType.ALL.name());
            copyAllApiDefinitionDocDTO.setDocInfo(info);
        }

        Assertions.assertEquals(allApiDefinitionDocDTO.getType(), copyAllApiDefinitionDocDTO.getType());
        Assertions.assertEquals(allApiDefinitionDocDTO.getDocTitle(), copyAllApiDefinitionDocDTO.getDocTitle());
        Assertions.assertEquals(allApiDefinitionDocDTO.getDocInfo().getId(), copyAllApiDefinitionDocDTO.getDocInfo().getId());

        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_READ, DOC, request);
    }

    @Test
    @Order(12)
    public void testDel() throws Exception {
        LogUtils.info("delete api test");
        apiDefinition = apiDefinitionMapper.selectByPrimaryKey("1001");
        // @只存在一个版本
        ApiDefinitionDeleteRequest apiDefinitionDeleteRequest = new ApiDefinitionDeleteRequest();
        apiDefinitionDeleteRequest.setId(apiDefinition.getId());
        apiDefinitionDeleteRequest.setProjectId(DEFAULT_PROJECT_ID);
        apiDefinitionDeleteRequest.setDeleteAll(false);
        // @@请求成功
        this.requestPostWithOkAndReturn(DELETE, apiDefinitionDeleteRequest);
        checkLog(apiDefinition.getId(), OperationLogType.DELETE);
        ApiDefinition apiDefinitionInfo = apiDefinitionMapper.selectByPrimaryKey(apiDefinition.getId());
        Assertions.assertTrue(apiDefinitionInfo.getDeleted());
        Assertions.assertEquals("admin", apiDefinitionInfo.getDeleteUser());
        Assertions.assertNotNull(apiDefinitionInfo.getDeleteTime());

        // @存在多个版本
        // 列表删除
        apiDefinitionDeleteRequest.setDeleteAll(false);
        apiDefinitionDeleteRequest.setId("1004");
        ApiDefinition delApiDefinition = apiDefinitionMapper.selectByPrimaryKey(apiDefinitionDeleteRequest.getId());
        MvcResult mvcResult = this.requestGetWithOk(VERSION + apiDefinitionDeleteRequest.getId()).andReturn();
        ApiDataUtils.setResolver(MsHTTPElement.class);
        List<ApiDefinitionVersionDTO> apiDefinitionVersionDTO =  getResultDataArray(mvcResult, ApiDefinitionVersionDTO.class);
        if(!apiDefinitionVersionDTO.isEmpty()){
            this.requestPostWithOkAndReturn(DELETE, apiDefinitionDeleteRequest);
            // 效验数据
            // 删除的数据为最新版本需要更新最近一条数据为最新数据
            if(delApiDefinition.getLatest()){
                ApiDefinitionExample example = new ApiDefinitionExample();
                example.createCriteria().andRefIdEqualTo(delApiDefinition.getRefId()).andDeletedEqualTo(false).andProjectIdEqualTo(delApiDefinition.getProjectId());
                example.setOrderByClause("update_time DESC");
                ApiDefinition updateApiDefinition = apiDefinitionMapper.selectByExample(example).stream().findFirst().orElse(null);
                if(updateApiDefinition != null) {
                    Assertions.assertTrue(updateApiDefinition.getLatest());
                    Assertions.assertFalse(updateApiDefinition.getDeleted());
                }
            }
            ApiDefinition delApiDefinitionInfo = apiDefinitionMapper.selectByPrimaryKey(apiDefinitionDeleteRequest.getId());
            if(delApiDefinitionInfo != null){
                Assertions.assertTrue(delApiDefinitionInfo.getDeleted());
                Assertions.assertEquals("admin", delApiDefinitionInfo.getDeleteUser());
                Assertions.assertNotNull(delApiDefinitionInfo.getDeleteTime());
            }
            checkLog(apiDefinitionDeleteRequest.getId(), OperationLogType.DELETE);

        }
        // 全部删除
        apiDefinitionDeleteRequest.setDeleteAll(true);
        apiDefinitionDeleteRequest.setId("1002");
        // @@请求成功
        this.requestPostWithOkAndReturn(DELETE, apiDefinitionDeleteRequest);

        List<String> ids = extApiDefinitionMapper.getApiDefinitionByRefId(apiDefinitionDeleteRequest.getId()).stream().map(ApiDefinitionVersionDTO::getId).toList();
        ApiDefinitionExample apiDefinitionExample = new ApiDefinitionExample();
        apiDefinitionExample.createCriteria().andIdIn(ids);
        List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(apiDefinitionExample);
        if(CollectionUtils.isNotEmpty(apiDefinitions)){
            apiDefinitions.forEach(item -> {
                Assertions.assertTrue(item.getDeleted());
                Assertions.assertEquals("admin", item.getDeleteUser());
                Assertions.assertNotNull(item.getDeleteTime());
            });
        }
        checkLog(apiDefinitionDeleteRequest.getId(), OperationLogType.DELETE);
        apiDefinitionDeleteRequest.setId("121");
        apiDefinitionDeleteRequest.setDeleteAll(false);
        assertErrorCode(this.requestPost(DELETE, apiDefinitionDeleteRequest), API_DEFINITION_NOT_EXIST);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_DELETE, DELETE, apiDefinitionDeleteRequest);
    }

    @Test
    @Order(13)
    public void testBatchDel() throws Exception {
        LogUtils.info("batch delete api test");
        ApiDefinitionBatchRequest request = new ApiDefinitionBatchRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);

        // 删除选中
        request.setSelectIds(List.of("1004"));
        request.setDeleteAll(false);
        request.setSelectAll(false);
        this.requestPostWithOkAndReturn(BATCH_DELETE, request);
        // @@校验日志
        checkLog("1004", OperationLogType.DELETE);

        request.setSelectIds(List.of("1002"));
        request.setDeleteAll(false);
        request.setSelectAll(false);
        assertErrorCode(this.requestPost(BATCH_DELETE, request), API_DEFINITION_NOT_EXIST);
        // 删除全部 条件为关键字为st-6的数据
        request.setDeleteAll(true);
        request.setExcludeIds(List.of("1005"));
        request.setSelectAll(true);
        BaseCondition baseCondition = new BaseCondition();
        baseCondition.setKeyword("st-6");
        request.setCondition(baseCondition);
        this.requestPostWithOkAndReturn(BATCH_DELETE, request);
        // @@校验日志
        checkLog("1006", OperationLogType.DELETE);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_DELETE, BATCH_DELETE, request);
    }


    @Test
    @Order(14)
    public void testRestore() throws Exception {
        LogUtils.info("restore api test");
        apiDefinition = apiDefinitionMapper.selectByPrimaryKey("1001");
        // @恢复一条数据
        ApiDefinitionDeleteRequest apiDefinitionDeleteRequest = new ApiDefinitionDeleteRequest();
        apiDefinitionDeleteRequest.setId(apiDefinition.getId());
        apiDefinitionDeleteRequest.setProjectId(DEFAULT_PROJECT_ID);
        // @@请求成功
        this.requestPostWithOkAndReturn(RESTORE, apiDefinitionDeleteRequest);
        checkLog(apiDefinition.getId(), OperationLogType.UPDATE);
        ApiDefinition apiDefinitionInfo = apiDefinitionMapper.selectByPrimaryKey(apiDefinition.getId());
        Assertions.assertFalse(apiDefinitionInfo.getDeleted());
        Assertions.assertNull(apiDefinitionInfo.getDeleteUser());
        Assertions.assertNull(apiDefinitionInfo.getDeleteTime());

        // @查询恢复的数据版本是否为默认版本
        String defaultVersion = extBaseProjectVersionMapper.getDefaultVersion(apiDefinition.getProjectId());
        if(defaultVersion.equals(apiDefinition.getVersionId())){
            // 需要处理此数据为最新标识
            // 此数据不为最新版本，验证是否更新为最新版本
            if(!apiDefinition.getLatest()){
                Assertions.assertTrue(apiDefinitionInfo.getLatest());
            }
        }
        // 效验 关联数据
        List<ApiTestCase> caseLists = extApiTestCaseMapper.getCaseInfoByApiIds(Collections.singletonList(apiDefinition.getId()), false);
        if(!caseLists.isEmpty()) {
            caseLists.forEach(item -> {
                Assertions.assertNull(item.getDeleteUser());
                Assertions.assertNull(item.getDeleteTime());
            });
        }

        // @恢复一条数据
        apiDefinitionDeleteRequest.setId("111");
        // @@请求成功
        assertErrorCode(this.requestPost(RESTORE, apiDefinitionDeleteRequest), API_DEFINITION_NOT_EXIST);

        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_UPDATE, RESTORE, apiDefinitionDeleteRequest);
    }

    @Test
    @Order(15)
    public void testBatchRestore() throws Exception {
        LogUtils.info("batch restore api test");
        ApiDefinitionBatchRequest request = new ApiDefinitionBatchRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        // 恢复选中
        request.setSelectIds(List.of("1002","1004","1005"));
        request.setExcludeIds(List.of("1005"));
        request.setSelectAll(false);
        this.requestPostWithOk(BATCH_RESTORE, request);

        // 效验数据结果
        ApiDefinitionExample apiDefinitionExample = new ApiDefinitionExample();
        apiDefinitionExample.createCriteria().andIdIn(request.getSelectIds());
        List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(apiDefinitionExample);
        if(!apiDefinitions.isEmpty()){
            apiDefinitions.forEach(item -> {
                Assertions.assertFalse(item.getDeleted());
                Assertions.assertNull(item.getDeleteUser());
                Assertions.assertNull(item.getDeleteTime());
                // 效验 关联数据
                List<ApiTestCase> caseLists = extApiTestCaseMapper.getCaseInfoByApiIds(Collections.singletonList(item.getId()), false);
                if(!caseLists.isEmpty()) {
                    caseLists.forEach(test -> {
                        Assertions.assertNull(test.getDeleteUser());
                        Assertions.assertNull(test.getDeleteTime());
                    });
                }
            });
        }


        // @@校验日志
        checkLog("1002", OperationLogType.UPDATE);
        checkLog("1004", OperationLogType.UPDATE);
        checkLog("1005", OperationLogType.UPDATE);

        // 恢复全部 条件为关键字为st-6的数据
        request.setSelectAll(true);
        BaseCondition baseCondition = new BaseCondition();
        baseCondition.setKeyword("st-6");
        request.setCondition(baseCondition);
        this.requestPostWithOk(BATCH_RESTORE, request);

        // @@校验日志
        checkLog("1006", OperationLogType.UPDATE);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_UPDATE, BATCH_RESTORE, request);
    }

    @Test
    @Order(16)
    public void testTrashDel() throws Exception {
        LogUtils.info("trashDel api test");
        apiDefinition = apiDefinitionMapper.selectByPrimaryKey("1001");
        if(!apiDefinition.getDeleted()){
            ApiDefinitionDeleteRequest apiDefinitionDeleteRequest = new ApiDefinitionDeleteRequest();
            apiDefinitionDeleteRequest.setId(apiDefinition.getId());
            apiDefinitionDeleteRequest.setProjectId(DEFAULT_PROJECT_ID);
            apiDefinitionDeleteRequest.setDeleteAll(false);
            // @@请求成功
            this.requestPostWithOkAndReturn(DELETE, apiDefinitionDeleteRequest);
            checkLog(apiDefinition.getId(), OperationLogType.DELETE);
            apiDefinition = apiDefinitionMapper.selectByPrimaryKey(apiDefinition.getId());
            Assertions.assertTrue(apiDefinition.getDeleted());
            Assertions.assertEquals("admin", apiDefinition.getDeleteUser());
            Assertions.assertNotNull(apiDefinition.getDeleteTime());
        }
        // @只存在一个版本
        ApiDefinitionDeleteRequest apiDefinitionDeleteRequest = new ApiDefinitionDeleteRequest();
        apiDefinitionDeleteRequest.setId(apiDefinition.getId());
        apiDefinitionDeleteRequest.setProjectId(DEFAULT_PROJECT_ID);
        apiDefinitionDeleteRequest.setDeleteAll(false);
        // @@请求成功
        this.requestPostWithOk(TRASH_DEL, apiDefinitionDeleteRequest);
        checkLog(apiDefinition.getId(), OperationLogType.DELETE);
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
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_DELETE, TRASH_DEL, apiDefinitionDeleteRequest);
    }

    @Test
    @Order(17)
    public void testBatchTrashDel() throws Exception {
        LogUtils.info("batch trash delete api test");
        ApiDefinitionBatchRequest request = new ApiDefinitionBatchRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);

        // 删除选中
        request.setSelectIds(List.of("1003"));
        request.setSelectAll(false);
        this.requestPostWithOk(BATCH_TRASH_DEL, request);
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
        checkLog("1003", OperationLogType.DELETE);
        // 删除全部 条件为关键字为st-6的数据
        request.setSelectAll(true);
        request.setExcludeIds(List.of("1005"));
        BaseCondition baseCondition = new BaseCondition();
        baseCondition.setKeyword("st-6");
        request.setCondition(baseCondition);
        this.requestPostWithOk(BATCH_TRASH_DEL, request);
        // @@校验日志
        checkLog("1006", OperationLogType.DELETE);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_DELETE, BATCH_TRASH_DEL, request);
    }


}
