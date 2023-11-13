package io.metersphere.api.controller;

import io.metersphere.api.controller.param.ApiTestCaseAddRequestDefinition;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.definition.ApiTestCaseAddRequest;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.mapper.*;
import io.metersphere.api.util.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.api.request.http.MsHTTPElement;
import io.metersphere.sdk.mapper.OperationLogMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.uid.NumGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.util.LinkedMultiValueMap;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author jianxing
 * @date : 2023-11-7
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiTestCaseControllerTests extends BaseTest {
    private static final String BASE_PATH = "/api/testCase/";
    private static final String ADD = BASE_PATH + "add";
    private static final String GET = BASE_PATH + "get-detail/";
    private static final String MOVE_TO_GC = BASE_PATH + "move-gc/";
    private static final String RECOVER = BASE_PATH + "recover/";
    private static final String FOLLOW = BASE_PATH + "follow/";
    private static final String UNFOLLOW = BASE_PATH + "unfollow/";
    private static final String DELETE = BASE_PATH + "delete/";
    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();
    private static ApiTestCase apiTestCase;
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
    private OperationLogMapper operationLogMapper;

    public void initApiData() {
        ApiDefinition apiDefinition = new ApiDefinition();
        apiDefinition.setId("apiDefinitionId");
        apiDefinition.setProjectId(DEFAULT_PROJECT_ID);
        apiDefinition.setName(StringUtils.join("接口定义", apiDefinition.getId()));
        apiDefinition.setModuleId("moduleId");
        apiDefinition.setProtocol("HTTP");
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
        apiDefinitionBlob.setRequest(new byte[0]);
        apiDefinitionBlob.setResponse(new byte[0]);
        apiDefinitionBlobMapper.insertSelective(apiDefinitionBlob);
    }

    @Test
    @Order(1)
    public void add() throws Exception {
        initApiData();
        // @@请求成功
        ApiTestCaseAddRequest request = new ApiTestCaseAddRequest();
        request.setApiDefinitionId("apiDefinitionId");
        request.setName("test");
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setPriority("P0");
        request.setStatus("Underway");
        request.setRequest("request");
        request.setTags(List.of("tag1", "tag2"));
        MsHTTPElement msHttpElement = MsHTTPElementTest.getMsHttpElement();
        request.setRequest(ApiDataUtils.toJSONString(msHttpElement));
        LinkedMultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        FileInputStream inputStream = new FileInputStream(new File(
                this.getClass().getClassLoader().getResource("file/file_upload.JPG")
                        .getPath()));
        MockMultipartFile file = new MockMultipartFile("file_upload.JPG", "file_upload.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        paramMap.add("files", List.of(file));
        MvcResult mvcResult = this.requestMultipartWithOkAndReturn(ADD, paramMap);
        // 校验请求成功数据
        ApiTestCase resultData = getResultData(mvcResult, ApiTestCase.class);
        apiTestCase = assertUpdateApiDebug(request, msHttpElement, resultData.getId());

        // 再插入一条数据，便于修改时重名校验
        request.setName("test1");
        paramMap.clear();
        paramMap.add("request", JSON.toJSONString(request));
        mvcResult = this.requestMultipartWithOkAndReturn(ADD, paramMap);
        resultData = getResultData(mvcResult, ApiTestCase.class);
        assertUpdateApiDebug(request, msHttpElement, resultData.getId());

        // @@重名校验异常
        this.requestMultipart(ADD, paramMap).andExpect(ERROR_REQUEST_MATCHER);
        // 校验接口是否存在
        request.setApiDefinitionId("111");
        paramMap.clear();
        paramMap.add("request", JSON.toJSONString(request));
        this.requestMultipart(ADD, paramMap).andExpect(ERROR_REQUEST_MATCHER);

        // 校验项目是否存在
        request.setProjectId("111");
        request.setApiDefinitionId("apiDefinitionId");
        request.setName("test123");
        paramMap.clear();
        paramMap.add("request", JSON.toJSONString(request));
        this.requestMultipart(ADD, paramMap).andExpect(ERROR_REQUEST_MATCHER);

        // @@校验日志
        checkLog(apiTestCase.getId(), OperationLogType.ADD);
        // @@异常参数校验
        createdGroupParamValidateTest(ApiTestCaseAddRequestDefinition.class, ADD);
        // @@校验权限
        request.setProjectId(DEFAULT_PROJECT_ID);
        paramMap.clear();
        request.setName("permission");
        paramMap.add("request", JSON.toJSONString(request));
        requestMultipartPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_ADD, ADD, paramMap);

    }

    private ApiTestCase assertUpdateApiDebug(Object request, MsHTTPElement msHttpElement, String id) {
        ApiTestCase apiCase = apiTestCaseMapper.selectByPrimaryKey(id);
        ApiTestCaseBlob apiTestCaseBlob = apiTestCaseBlobMapper.selectByPrimaryKey(id);
        ApiTestCase copyApiDebug = BeanUtils.copyBean(new ApiTestCase(), apiCase);
        BeanUtils.copyBean(copyApiDebug, request);
        Assertions.assertEquals(apiCase, copyApiDebug);
        ApiDataUtils.setResolver(MsHTTPElement.class);
        Assertions.assertEquals(msHttpElement, ApiDataUtils.parseObject(new String(apiTestCaseBlob.getRequest()), AbstractMsTestElement.class));
        return apiCase;
    }

    @Test
    @Order(2)
    public void get() throws Exception {
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOk(GET + apiTestCase.getId())
                .andReturn();
        ApiDataUtils.setResolver(MsHTTPElement.class);
        ApiTestCaseDTO apiDebugDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResult).get("data")), ApiTestCaseDTO.class);
        // 校验数据是否正确
        ApiTestCaseDTO copyApiDebugDTO = BeanUtils.copyBean(new ApiTestCaseDTO(), apiTestCaseMapper.selectByPrimaryKey(apiTestCase.getId()));
        ApiTestCaseBlob apiDebugBlob = apiTestCaseBlobMapper.selectByPrimaryKey(apiTestCase.getId());
        ApiTestCaseFollowerExample example = new ApiTestCaseFollowerExample();
        example.createCriteria().andCaseIdEqualTo(apiTestCase.getId()).andUserIdEqualTo("admin");
        List<ApiTestCaseFollower> followers = apiTestCaseFollowerMapper.selectByExample(example);
        copyApiDebugDTO.setFollow(CollectionUtils.isNotEmpty(followers));
        copyApiDebugDTO.setRequest(ApiDataUtils.parseObject(new String(apiDebugBlob.getRequest()), AbstractMsTestElement.class));
        Assertions.assertEquals(apiDebugDTO, copyApiDebugDTO);

        this.requestGet(GET + "111").andExpect(ERROR_REQUEST_MATCHER);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ, GET + apiTestCase.getId());
    }

    @Test
    @Order(3)
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
    @Order(4)
    public void recover() throws Exception {
        // @@请求成功
        this.requestGetWithOk(RECOVER + apiTestCase.getId());
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
    @Order(5)
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
    @Order(6)
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
    @Order(7)
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
        // @@校验日志
        checkLog(apiTestCase.getId(), OperationLogType.DELETE);
        this.requestGet(DELETE + "111").andExpect(ERROR_REQUEST_MATCHER);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_DELETE, DELETE + apiTestCase.getId());
    }

}