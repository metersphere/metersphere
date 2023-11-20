package io.metersphere.api.controller;

import io.metersphere.api.controller.result.ApiResultCode;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.enums.ApiDefinitionStatus;
import io.metersphere.api.mapper.*;
import io.metersphere.api.util.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.api.request.http.MsHTTPElement;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.sdk.BaseCondition;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
    private final static String BATCH_DELETE= BASE_PATH + "batch-del";
    private final static String COPY= BASE_PATH + "copy";
    private final static String BATCH_MOVE= BASE_PATH + "batch-move";

    private final static String PAGE= BASE_PATH + "page";
    private static final String GET = BASE_PATH + "get-detail/";
    private static final String FOLLOW = BASE_PATH + "follow/";
    private static final String VERSION = BASE_PATH + "version/";

    private static final String DEFAULT_MODULE_ID = "10001";

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


    @Test
    @Order(1)
    public void testAdd() throws Exception {
        LogUtils.info("create api test");
        // 创建测试数据
        ApiDefinitionAddRequest request = createApiDefinitionAddRequest();
        MsHTTPElement msHttpElement = MsHTTPElementTest.getMsHttpElement();
        request.setRequest(ApiDataUtils.toJSONString(msHttpElement));
        // 构造请求参数
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        FileInputStream inputStream = new FileInputStream(new File(
                Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/file_upload.JPG"))
                        .getPath()));
        MockMultipartFile file = new MockMultipartFile("file_upload.JPG", "file_upload.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        paramMap.add("files", List.of(file));
        paramMap.add("request", JSON.toJSONString(request));

        // 执行方法调用
        MvcResult mvcResult = this.requestMultipartWithOkAndReturn(ADD, paramMap);
        // 校验请求成功数据
        ApiDefinition resultData = getResultData(mvcResult, ApiDefinition.class);
        ApiDefinition apiDefinition = assertAddApiDefinition(request, msHttpElement, resultData.getId());
        // 再插入一条数据，便于修改时重名校验
        request.setName("接口定义test-6");
        paramMap.clear();
        paramMap.add("request", JSON.toJSONString(request));
        mvcResult = this.requestMultipartWithOkAndReturn(ADD, paramMap);
        resultData = getResultData(mvcResult, ApiDefinition.class);
        assertAddApiDefinition(request, msHttpElement, resultData.getId());

        // @@重名校验异常
        assertErrorCode(this.requestMultipart(ADD, paramMap), ApiResultCode.API_DEFINITION_EXIST);
        // 校验项目是否存在
        request.setProjectId("111");
        request.setName("test123");
        paramMap.clear();
        paramMap.add("request", JSON.toJSONString(request));
        assertErrorCode(this.requestMultipart(ADD, paramMap), NOT_FOUND);

        // @@校验日志
        checkLog(apiDefinition.getId(), OperationLogType.ADD);
        // @@异常参数校验
        createdGroupParamValidateTest(ApiDefinitionAddRequest.class, ADD);
        // @@校验权限
        request.setProjectId(DEFAULT_PROJECT_ID);
        paramMap.clear();
        request.setName("permission");
        paramMap.add("request", JSON.toJSONString(request));
        requestMultipartPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_ADD, ADD, paramMap);
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
        request.setModuleId("root");
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
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey("1001");
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOkAndReturn(GET + apiDefinition.getId());
        ApiDataUtils.setResolver(MsHTTPElement.class);
        ApiDefinitionDTO apiDefinitionDTO = getResultData(mvcResult, ApiDefinitionDTO.class);
        // 校验数据是否正确
        ApiDefinitionDTO copyApiDefinitionDTO = BeanUtils.copyBean(new ApiDefinitionDTO(), apiDefinitionMapper.selectByPrimaryKey(apiDefinition.getId()));
        ApiDefinitionBlob apiDefinitionBlob = apiDefinitionBlobMapper.selectByPrimaryKey(apiDefinition.getId());
        ApiDefinitionFollowerExample example = new ApiDefinitionFollowerExample();
        example.createCriteria().andApiDefinitionIdEqualTo(apiDefinition.getId()).andUserIdEqualTo("admin");
        List<ApiDefinitionFollower> followers = apiDefinitionFollowerMapper.selectByExample(example);
        copyApiDefinitionDTO.setFollow(CollectionUtils.isNotEmpty(followers));
        if(apiDefinitionBlob != null){
            copyApiDefinitionDTO.setRequest(ApiDataUtils.parseObject(new String(apiDefinitionBlob.getRequest()), AbstractMsTestElement.class));
        }
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
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey("1001");
        ApiDefinitionUpdateRequest request = new ApiDefinitionUpdateRequest();
        BeanUtils.copyBean(request, apiDefinition);
        request.setPath("test.com/admin/test");
        request.setName("test1");
        request.setMethod("POST");
        request.setModuleId("default1");
        MsHTTPElement msHttpElement = MsHTTPElementTest.getMsHttpElement();
        request.setRequest(ApiDataUtils.toJSONString(msHttpElement));

        // 构造请求参数
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        FileInputStream inputStream = new FileInputStream(new File(
                Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/file_update_upload.JPG"))
                        .getPath()));
        MockMultipartFile file = new MockMultipartFile("file_update_upload.JPG", "file_update_upload.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        paramMap.add("files", List.of(file));
        paramMap.add("request", JSON.toJSONString(request));

        // 执行方法调用
        MvcResult mvcResult = this.requestMultipartWithOkAndReturn(UPDATE, paramMap);
        // 校验请求成功数据
        ApiDefinition resultData = getResultData(mvcResult, ApiDefinition.class);
        apiDefinition = assertAddApiDefinition(request, msHttpElement, resultData.getId());
        // 校验数据是否存在
        request.setId("111");
        request.setName("test123");
        paramMap.clear();
        paramMap.add("request", JSON.toJSONString(request));
        assertErrorCode(this.requestMultipart(UPDATE, paramMap), ApiResultCode.API_DEFINITION_NOT_EXIST);

        // 校验项目是否存在
        request.setProjectId("111");
        request.setName("test123");
        paramMap.clear();
        paramMap.add("request", JSON.toJSONString(request));
        assertErrorCode(this.requestMultipart(UPDATE, paramMap), NOT_FOUND);

        // @@校验日志
        checkLog(apiDefinition.getId(), OperationLogType.UPDATE);
        // @@异常参数校验
        createdGroupParamValidateTest(ApiDefinitionUpdateRequest.class, UPDATE);
        // @@校验权限
        request.setProjectId(DEFAULT_PROJECT_ID);
        paramMap.clear();
        request.setName("permission");
        paramMap.add("request", JSON.toJSONString(request));
        requestMultipartPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_UPDATE, UPDATE, paramMap);
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
    @Sql(scripts = {"/dml/init_api_definition.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void copy() throws Exception {
        LogUtils.info("copy api test");
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey("1001");
        ApiDefinitionCopyRequest request = new ApiDefinitionCopyRequest();
        request.setId(apiDefinition.getId());
        MvcResult mvcResult = this.requestPostWithOkAndReturn(COPY, request);
        ApiDefinition resultData = getResultData(mvcResult, ApiDefinition.class);
        // @@校验日志
        checkLog(resultData.getId(), OperationLogType.UPDATE);
        request.setId("121");
        assertErrorCode(this.requestPost(COPY, request), ApiResultCode.API_DEFINITION_NOT_EXIST);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_UPDATE, COPY, request);
    }

    @Test
    @Order(6)
    @Sql(scripts = {"/dml/init_api_definition.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
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
    @Sql(scripts = {"/dml/init_api_definition.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
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
        assertErrorCode(this.requestGet(FOLLOW + "111"), ApiResultCode.API_DEFINITION_NOT_EXIST);

        // @@取消关注
        // @@请求成功
        this.requestGetWithOk(FOLLOW + apiDefinition.getId());
        ApiDefinitionFollowerExample unFollowerExample = new ApiDefinitionFollowerExample();
        example.createCriteria().andApiDefinitionIdEqualTo(apiDefinition.getId()).andUserIdEqualTo("admin");
        List<ApiDefinitionFollower> unFollowers = apiDefinitionFollowerMapper.selectByExample(unFollowerExample);
        Assertions.assertTrue(CollectionUtils.isEmpty(unFollowers));
        // @@校验日志
        checkLog(apiDefinition.getId(), OperationLogType.UPDATE);
        assertErrorCode(this.requestGet(FOLLOW + "111"), ApiResultCode.API_DEFINITION_NOT_EXIST);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_UPDATE, FOLLOW + apiDefinition.getId());
    }

    @Test
    @Order(8)
    @Sql(scripts = {"/dml/init_api_definition.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void version() throws Exception {
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey("1001");
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOk(VERSION + apiDefinition.getId()).andReturn();
        ApiDataUtils.setResolver(MsHTTPElement.class);
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
    public void testDel() throws Exception {
        LogUtils.info("delete api test");
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey("1001");
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
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_DELETE, DELETE, apiDefinitionDeleteRequest);
    }

    @Test
    @Order(10)
    @Sql(scripts = {"/dml/init_api_definition.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testBatchDel() throws Exception {
        LogUtils.info("batch delete api test");
        ApiDefinitionBatchRequest request = new ApiDefinitionBatchRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);

        // 删除选中
        request.setSelectIds(List.of("1001","1002","1005"));
        request.setExcludeIds(List.of("1005"));
        request.setDeleteAll(false);
        request.setSelectAll(false);
        this.requestPostWithOkAndReturn(BATCH_DELETE, request);
        // @@校验日志
        checkLog("1001", OperationLogType.DELETE);
        checkLog("1002", OperationLogType.DELETE);
        checkLog("1005", OperationLogType.DELETE);
        // 删除全部 条件为关键字为st-6的数据
        request.setDeleteAll(true);
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
    @Order(11)
    @Sql(scripts = {"/dml/init_api_definition.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void getPage() throws Exception {
        ApiDefinitionPageRequest request = new ApiDefinitionPageRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setCurrent(1);
        request.setPageSize(10);
        this.requestPost(PAGE, request);
        request.setSort(new HashMap<>() {{
            put("createTime", "asc");
        }});

        // ALL 全部 KEYWORD 关键字 FILTER 筛选 COMBINE 自定义
        String search = "KEYWORD";
        Map<String, List<String>> filters = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        switch (search) {
            case "ALL":
                // Perform all search types
                request.setKeyword("100");
                filters.put("status", Arrays.asList("Underway", "Completed"));
                filters.put("method", List.of("GET"));
                filters.put("version_id", List.of("1005704995741369851"));
                request.setFilter(filters);

                map.put("name", Map.of("operator", "like", "value", "test-1"));
                map.put("method", Map.of("operator", "in", "value", Arrays.asList("GET", "POST")));
                request.setCombine(map);
                break;
            case "KEYWORD":
                // 基础查询
                request.setKeyword("100");
                // 版本查询
                request.setVersionId("100570499574136985");
                break;
            case "FILTER":
                // 筛选
                filters.put("status", Arrays.asList("Underway", "Completed"));
                filters.put("method", List.of("GET"));
                filters.put("version_id", List.of("1005704995741369851"));
                request.setFilter(filters);
                break;
            case "COMBINE":
                // 自定义字段 测试
                map.put("name", Map.of("operator", "like", "value", "test-1"));
                map.put("method", Map.of("operator", "in", "value", Arrays.asList("GET", "POST")));
                request.setCombine(map);
                break;
            default:
                break;
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

}
