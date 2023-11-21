package io.metersphere.api.controller;

import io.metersphere.api.controller.param.ApiTestCaseAddRequestDefinition;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.mapper.*;
import io.metersphere.api.util.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentExample;
import io.metersphere.sdk.dto.api.request.http.MsHTTPElement;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.file.FileRequest;
import io.metersphere.system.file.MinioRepository;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
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
    private static final String BASE_PATH = "/api/testCase/";
    private static final String ADD = BASE_PATH + "add";
    private static final String GET = BASE_PATH + "get-detail/";
    private static final String MOVE_TO_GC = BASE_PATH + "move-gc/";
    private static final String RECOVER = BASE_PATH + "recover/";
    private static final String FOLLOW = BASE_PATH + "follow/";
    private static final String UNFOLLOW = BASE_PATH + "unfollow/";
    private static final String DELETE = BASE_PATH + "delete/";
    private static final String UPDATE = BASE_PATH + "update";
    private static final String PAGE = BASE_PATH + "page";
    private static final String UPDATE_STATUS = BASE_PATH + "update-status";
    private static final String BATCH_EDIT = BASE_PATH + "batch/edit";
    private static final String BATCH_DELETE = BASE_PATH + "batch/delete";
    private static final String BATCH_MOVE_GC = BASE_PATH + "batch/move-gc";

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
        apiDefinition.setId("apiDefinitionId1");
        apiDefinition.setModuleId("moduleId1");
        apiDefinitionMapper.insertSelective(apiDefinition);
    }

    public void initCaseData() {
        //2000条数据
        List<ApiTestCase> apiTestCasesList = new ArrayList<>();
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
            apiTestCasesList.add(apiTestCase);
        }
        apiTestCaseMapper.batchInsert(apiTestCasesList);

    }


    @Test
    @Order(1)
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
        request.setTags(new LinkedHashSet<>());
        paramMap.clear();
        paramMap.add("request", JSON.toJSONString(request));
        mvcResult = this.requestMultipartWithOkAndReturn(ADD, paramMap);
        resultData = getResultData(mvcResult, ApiTestCase.class);
        anotherApiTestCase = apiTestCaseMapper.selectByPrimaryKey(resultData.getId());
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
        ApiTestCase testCase = apiTestCaseMapper.selectByPrimaryKey(apiTestCase.getId());
        ApiTestCaseDTO copyApiDebugDTO = BeanUtils.copyBean(new ApiTestCaseDTO(), testCase);
        if (StringUtils.isNotEmpty(testCase.getTags())) {
            copyApiDebugDTO.setTags(JSON.parseArray(testCase.getTags(), String.class));
        } else {
            copyApiDebugDTO.setTags(new ArrayList<>());
        }
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(apiTestCase.getApiDefinitionId());
        copyApiDebugDTO.setMethod(apiDefinition.getMethod());
        copyApiDebugDTO.setPath(apiDefinition.getPath());
        ApiTestCaseBlob apiDebugBlob = apiTestCaseBlobMapper.selectByPrimaryKey(apiTestCase.getId());
        ApiTestCaseFollowerExample example = new ApiTestCaseFollowerExample();
        example.createCriteria().andCaseIdEqualTo(apiTestCase.getId()).andUserIdEqualTo("admin");
        List<ApiTestCaseFollower> followers = apiTestCaseFollowerMapper.selectByExample(example);
        copyApiDebugDTO.setFollow(CollectionUtils.isNotEmpty(followers));
        copyApiDebugDTO.setRequest(ApiDataUtils.parseObject(new String(apiDebugBlob.getRequest()), AbstractMsTestElement.class));
        Assertions.assertEquals(apiDebugDTO, copyApiDebugDTO);
        this.requestGetWithOk(GET + anotherApiTestCase.getId())
                .andReturn();

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
        ApiTestCase updateCase = new ApiTestCase();
        updateCase.setId(apiTestCase.getId());
        updateCase.setApiDefinitionId("aaaa");
        apiTestCaseMapper.updateByPrimaryKeySelective(updateCase);
        this.requestGet(RECOVER + apiTestCase.getId()).andExpect(ERROR_REQUEST_MATCHER);
        updateCase.setApiDefinitionId("apiDefinitionId");
        apiTestCaseMapper.updateByPrimaryKeySelective(updateCase);
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
        request.setRequest(ApiDataUtils.toJSONString(msHttpElement));
        LinkedMultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        MvcResult mvcResult = this.requestMultipartWithOkAndReturn(UPDATE, paramMap);
        // 校验请求成功数据
        ApiTestCase resultData = getResultData(mvcResult, ApiTestCase.class);
        assertUpdateApiDebug(request, msHttpElement, resultData.getId());
        request.setTags(new ArrayList<>());
        paramMap.clear();
        paramMap.add("request", JSON.toJSONString(request));
        mvcResult = this.requestMultipartWithOkAndReturn(UPDATE, paramMap);
        resultData = getResultData(mvcResult, ApiTestCase.class);
        assertUpdateApiDebug(request, msHttpElement, resultData.getId());

        // @@重名校验异常
        request.setName("update");
        request.setId(anotherApiTestCase.getId());
        paramMap.clear();
        paramMap.add("request", JSON.toJSONString(request));
        this.requestMultipart(UPDATE, paramMap).andExpect(ERROR_REQUEST_MATCHER);
        // 校验接口是否存在
        request.setId("111");
        paramMap.clear();
        paramMap.add("request", JSON.toJSONString(request));
        this.requestMultipart(UPDATE, paramMap).andExpect(ERROR_REQUEST_MATCHER);

        // @@校验日志
        checkLog(apiTestCase.getId(), OperationLogType.UPDATE);
        // @@异常参数校验
        createdGroupParamValidateTest(ApiTestCaseUpdateRequest.class, UPDATE);
        // @@校验权限
        requestMultipartPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_UPDATE, UPDATE, paramMap);
    }

    @Test
    @Order(8)
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
        request.setRequest(ApiDataUtils.toJSONString(msHttpElement));
        LinkedMultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        this.requestMultipartWithOkAndReturn(ADD, paramMap);
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
    @Order(9)
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
    @Order(10)
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
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andDeletedEqualTo(false);
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
            Assertions.assertEquals(apiTestCase.getTags(), "[\"tag1\"]");
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
    @Order(11)
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
        request.setModuleIds(List.of("case-moduleId"));
        responsePost(BATCH_DELETE, request);
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andApiDefinitionIdEqualTo("apiDefinitionId");
        //数据为空
        Assertions.assertEquals(0, apiTestCaseMapper.selectByExample(example).size());

        //校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_DELETE, BATCH_DELETE, request);
    }


}