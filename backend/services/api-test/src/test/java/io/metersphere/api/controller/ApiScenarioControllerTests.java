package io.metersphere.api.controller;

import io.metersphere.api.domain.*;
import io.metersphere.api.dto.scenario.ApiScenarioBatchEditRequest;
import io.metersphere.api.dto.scenario.ApiScenarioDTO;
import io.metersphere.api.dto.scenario.ApiScenarioPageRequest;
import io.metersphere.api.mapper.*;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentExample;
import io.metersphere.sdk.domain.EnvironmentGroup;
import io.metersphere.sdk.mapper.EnvironmentGroupMapper;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

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
public class ApiScenarioControllerTests extends BaseTest {
    private static final String BASE_PATH = "/api/scenario/";
    private static final String PAGE = BASE_PATH + "page";
    private static final String TRASH_PAGE = BASE_PATH + "trash/page";
    private static final String BATCH_EDIT = BASE_PATH + "batch/edit";
    private static final String FOLLOW = BASE_PATH + "follow/";

    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ApiScenarioModuleMapper apiScenarioModuleMapper;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;
    @Resource
    private ApiScenarioEnvironmentMapper apiScenarioEnvironmentMapper;
    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private ApiScenarioFollowerMapper apiScenarioFollowerMapper;
    @Resource
    private EnvironmentGroupMapper environmentGroupMapper;

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

    public void initApiScenario() {
        ApiScenarioModule apiScenarioModule = new ApiScenarioModule();
        apiScenarioModule.setId("scenario-moduleId");
        apiScenarioModule.setProjectId(DEFAULT_PROJECT_ID);
        apiScenarioModule.setName("场景模块");
        apiScenarioModule.setCreateTime(System.currentTimeMillis());
        apiScenarioModule.setUpdateTime(System.currentTimeMillis());
        apiScenarioModule.setCreateUser("admin");
        apiScenarioModule.setUpdateUser("admin");
        apiScenarioModule.setPos(0L);
        apiScenarioModuleMapper.insertSelective(apiScenarioModule);

        // 创建环境组
        EnvironmentGroup environmentGroup = new EnvironmentGroup();
        environmentGroup.setId("scenario-environment-group-id");
        environmentGroup.setProjectId(DEFAULT_PROJECT_ID);
        environmentGroup.setName("环境组");
        environmentGroup.setCreateTime(System.currentTimeMillis());
        environmentGroup.setUpdateTime(System.currentTimeMillis());
        environmentGroup.setCreateUser("admin");
        environmentGroup.setUpdateUser("admin");
        environmentGroup.setPos(0L);
        environmentGroupMapper.insertSelective(environmentGroup);

        EnvironmentExample environmentExample = new EnvironmentExample();
        environmentExample.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andMockEqualTo(true);
        List<Environment> environments = environmentMapper.selectByExample(environmentExample);

        for (int i = 0; i < 10; i++) {
            ApiScenario apiScenario = new ApiScenario();
            apiScenario.setId("api-scenario-id" + i);
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
            ApiScenarioEnvironment apiScenarioEnvironment = new ApiScenarioEnvironment();
            apiScenarioEnvironment.setApiScenarioId(apiScenario.getId());
            if (i % 2 == 0) {
                apiScenario.setTags(new ArrayList<>(List.of("tag1", "tag2")));
                apiScenario.setGrouped(true);
                apiScenarioEnvironment.setEnvironmentGroupId("scenario-environment-group-id");
            } else {
                apiScenario.setGrouped(false);
                apiScenarioEnvironment.setEnvironmentId(environments.get(0).getId());
            }
            apiScenarioMapper.insertSelective(apiScenario);
            apiScenarioEnvironmentMapper.insertSelective(apiScenarioEnvironment);
        }
    }

    public void initApiScenarioTrash() {
        ApiScenarioModule apiScenarioModule = new ApiScenarioModule();
        apiScenarioModule.setId("scenario-moduleId-trash");
        apiScenarioModule.setProjectId(DEFAULT_PROJECT_ID);
        apiScenarioModule.setName("场景模块-trash");
        apiScenarioModule.setCreateTime(System.currentTimeMillis());
        apiScenarioModule.setUpdateTime(System.currentTimeMillis());
        apiScenarioModule.setCreateUser("admin");
        apiScenarioModule.setUpdateUser("admin");
        apiScenarioModule.setPos(0L);
        apiScenarioModuleMapper.insertSelective(apiScenarioModule);
        for (int i = 0; i < 10; i++) {
            ApiScenario apiScenario = new ApiScenario();
            apiScenario.setId("trash-api-scenario-id" + i);
            apiScenario.setProjectId(DEFAULT_PROJECT_ID);
            apiScenario.setName(StringUtils.join("接口场景-回收站", apiScenario.getId()));
            apiScenario.setModuleId("scenario-moduleId-trash");
            apiScenario.setStatus("未规划");
            apiScenario.setNum(NumGenerator.nextNum(DEFAULT_PROJECT_ID, ApplicationNumScope.API_SCENARIO));
            apiScenario.setPos(0L);
            apiScenario.setLatest(true);
            apiScenario.setPriority("P0");
            apiScenario.setVersionId("1.0");
            apiScenario.setRefId(apiScenario.getId());
            apiScenario.setCreateTime(System.currentTimeMillis());
            apiScenario.setUpdateTime(System.currentTimeMillis());
            apiScenario.setCreateUser("admin");
            apiScenario.setUpdateUser("admin");
            apiScenarioMapper.insertSelective(apiScenario);
            ApiScenarioEnvironment apiScenarioEnvironment = new ApiScenarioEnvironment();
            apiScenarioEnvironment.setApiScenarioId(apiScenario.getId());
            apiScenarioEnvironmentMapper.insertSelective(apiScenarioEnvironment);
        }
    }

    @Test
    @Order(11)
    public void page() throws Exception {
        // @@请求成功
        initApiScenario();
        ApiScenarioPageRequest pageRequest = new ApiScenarioPageRequest();
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
        Assertions.assertTrue(((List<ApiScenarioDTO>) returnPager.getList()).size() <= pageRequest.getPageSize());

        //查询api-scenario-id1的数据
        pageRequest.setScenarioId("api-scenario-id1");
        mvcResult = responsePost(PAGE, pageRequest);
        returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), pageRequest.getCurrent());

        List<ApiScenarioDTO> scenarioDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ApiScenarioDTO.class);
        scenarioDTOS.forEach(caseDTO -> Assertions.assertEquals(caseDTO.getId(), "api-scenario-id1"));

        //查询模块为moduleId1的数据
        pageRequest.setScenarioId(null);
        pageRequest.setModuleIds(List.of("scenario-moduleId"));
        mvcResult = responsePost(PAGE, pageRequest);
        returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), pageRequest.getCurrent());
        scenarioDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ApiScenarioDTO.class);
        scenarioDTOS.forEach(scenario -> Assertions.assertEquals(scenario.getModuleId(), "scenario-moduleId"));

        pageRequest.setModuleIds(null);
        pageRequest.setSort(new HashMap<>() {{
            put("createTime", "asc");
        }});
        responsePost(PAGE, pageRequest);

        pageRequest = new ApiScenarioPageRequest();
        pageRequest.setProjectId(DEFAULT_PROJECT_ID);
        pageRequest.setPageSize(10);
        pageRequest.setCurrent(1);
        pageRequest.setModuleIds(List.of("scenario-moduleId"));
        pageRequest.setFilter(new HashMap<>() {{
            put("priority", List.of("P0"));
        }});
        mvcResult = responsePost(PAGE, pageRequest);
        returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), pageRequest.getCurrent());
        scenarioDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ApiScenarioDTO.class);
        scenarioDTOS.forEach(scenario -> Assertions.assertEquals(scenario.getPriority(), "P0"));

        //校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_SCENARIO_READ, PAGE, pageRequest);
    }

    @Test
    @Order(12)
    public void follow() throws Exception {
        // @@请求成功
        this.requestGetWithOk(FOLLOW + "api-scenario-id1");
        ApiScenarioFollowerExample example = new ApiScenarioFollowerExample();
        example.createCriteria().andApiScenarioIdEqualTo("api-scenario-id1").andUserIdEqualTo("admin");
        List<ApiScenarioFollower> followers = apiScenarioFollowerMapper.selectByExample(example);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(followers));
        // @@校验日志
        checkLog("api-scenario-id1", OperationLogType.UPDATE);
        this.requestGet(FOLLOW + "111").andExpect(ERROR_REQUEST_MATCHER);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_API_SCENARIO_UPDATE, FOLLOW + "api-scenario-id1");

        // @@请求成功
        this.requestGetWithOk(FOLLOW + "api-scenario-id1");
        example = new ApiScenarioFollowerExample();
        example.createCriteria().andApiScenarioIdEqualTo("api-scenario-id1").andUserIdEqualTo("admin");
        followers = apiScenarioFollowerMapper.selectByExample(example);
        Assertions.assertTrue(CollectionUtils.isEmpty(followers));
        // @@校验日志
        checkLog("api-scenario-id1", OperationLogType.UPDATE);
        this.requestGet(FOLLOW + "111").andExpect(ERROR_REQUEST_MATCHER);

    }


    @Test
    @Order(13)
    public void batchEdit() throws Exception {
        // 追加标签
        ApiScenarioBatchEditRequest request = new ApiScenarioBatchEditRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setType("Tags");
        request.setAppendTag(true);
        request.setSelectAll(true);
        request.setTags(new LinkedHashSet<>(List.of("tag1", "tag3", "tag4")));
        responsePost(BATCH_EDIT, request);
        ApiScenarioExample example = new ApiScenarioExample();
        List<String> ids = extApiScenarioMapper.getIds(request, false);
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andDeletedEqualTo(false).andIdIn(ids);
        apiScenarioMapper.selectByExample(example).forEach(apiTestCase -> {
            Assertions.assertTrue(apiTestCase.getTags().contains("tag1"));
            Assertions.assertTrue(apiTestCase.getTags().contains("tag3"));
            Assertions.assertTrue(apiTestCase.getTags().contains("tag4"));
        });
        //覆盖标签
        request.setTags(new LinkedHashSet<>(List.of("tag1")));
        request.setAppendTag(false);
        responsePost(BATCH_EDIT, request);
        apiScenarioMapper.selectByExample(example).forEach(scenario -> {
            Assertions.assertEquals(scenario.getTags(), List.of("tag1"));
        });
        //标签为空  报错
        request.setTags(new LinkedHashSet<>());
        this.requestPost(BATCH_EDIT, request, ERROR_REQUEST_MATCHER);
        //ids为空的时候
        request.setTags(new LinkedHashSet<>(List.of("tag1")));
        request.setSelectAll(true);
        List<ApiScenario> apiScenarioList = apiScenarioMapper.selectByExample(example);
        //提取所有的id
        List<String> apiIdList = apiScenarioList.stream().map(ApiScenario::getId).toList();
        request.setSelectIds(apiIdList);
        request.setExcludeIds(apiIdList);
        responsePost(BATCH_EDIT, request);

        //优先级
        request.setTags(new LinkedHashSet<>());
        request.setSelectAll(true);
        request.setType("Priority");
        request.setModuleIds(List.of("scenario-moduleId"));
        request.setPriority("P3");
        request.setExcludeIds(new ArrayList<>());
        responsePost(BATCH_EDIT, request);
        //判断数据的优先级是不是P3
        example.clear();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andDeletedEqualTo(false);
        List<ApiScenario> apiScenarios = apiScenarioMapper.selectByExample(example);

        apiScenarios.forEach(apiTestCase -> Assertions.assertEquals(apiTestCase.getPriority(), "P3"));
        //优先级数据为空
        request.setPriority(null);
        this.requestPost(BATCH_EDIT, request, ERROR_REQUEST_MATCHER);
        //ids为空的时候
        request.setPriority("P3");
        request.setSelectAll(false);
        request.setSelectIds(List.of("api-scenario-id1"));
        request.setExcludeIds(List.of("api-scenario-id1"));
        responsePost(BATCH_EDIT, request);
        //状态
        request.setPriority(null);
        request.setType("Status");
        request.setStatus("Completed");
        request.setSelectAll(true);
        request.setExcludeIds(new ArrayList<>());
        responsePost(BATCH_EDIT, request);
        //判断数据的状态是不是Completed
        apiScenarios = apiScenarioMapper.selectByExample(example);
        apiScenarios.forEach(apiTestCase -> Assertions.assertEquals(apiTestCase.getStatus(), "Completed"));
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
        //取所有的ids
        List<String> scenarioIds = apiScenarios.stream().map(ApiScenario::getId).toList();
        //判断数据的环境是不是environments.get(0).getId()
        ApiScenarioEnvironmentExample apiScenarioEnvironmentExample = new ApiScenarioEnvironmentExample();
        apiScenarioEnvironmentExample.createCriteria().andApiScenarioIdIn(scenarioIds);
        List<ApiScenarioEnvironment> apiScenarioEnvironments = apiScenarioEnvironmentMapper.selectByExample(apiScenarioEnvironmentExample);
        apiScenarioEnvironments.forEach(apiTestCase -> Assertions.assertEquals(apiTestCase.getEnvironmentId(), environments.get(0).getId()));

        //环境数据为空
        request.setEnvId(null);
        this.requestPost(BATCH_EDIT, request, ERROR_REQUEST_MATCHER);
        //环境不存在
        request.setEnvId("111");
        this.requestPost(BATCH_EDIT, request, ERROR_REQUEST_MATCHER);
        //环境组
        request.setGrouped(true);
        request.setGroupId("scenario-environment-group-id");
        responsePost(BATCH_EDIT, request);
        apiScenarioEnvironments = apiScenarioEnvironmentMapper.selectByExample(apiScenarioEnvironmentExample);
        apiScenarioEnvironments.forEach(apiTestCase -> Assertions.assertEquals(apiTestCase.getEnvironmentGroupId(), "scenario-environment-group-id"));
        apiScenarios = apiScenarioMapper.selectByExample(example);
        apiScenarios.forEach(apiTestCase -> Assertions.assertEquals(apiTestCase.getGrouped(), true));

        //环境组数据为空
        request.setGroupId(null);
        this.requestPost(BATCH_EDIT, request, ERROR_REQUEST_MATCHER);
        //环境组不存在
        request.setGroupId("111");
        this.requestPost(BATCH_EDIT, request, ERROR_REQUEST_MATCHER);
        //类型错误
        request.setType("111");
        this.requestPost(BATCH_EDIT, request, ERROR_REQUEST_MATCHER);

        //校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_SCENARIO_UPDATE, BATCH_EDIT, request);

    }

    @Test
    @Order(15)
    public void trashPage() throws Exception {
        // @@请求成功
        initApiScenarioTrash();
        ApiScenarioPageRequest pageRequest = new ApiScenarioPageRequest();
        pageRequest.setProjectId(DEFAULT_PROJECT_ID);
        pageRequest.setPageSize(10);
        pageRequest.setCurrent(1);
        MvcResult mvcResult = responsePost(TRASH_PAGE, pageRequest);
        Pager<?> returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), pageRequest.getCurrent());
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(((List<ApiScenarioDTO>) returnPager.getList()).size() <= pageRequest.getPageSize());

        //查询api-scenario-id1的数据
        pageRequest.setScenarioId("trash-api-scenario-id1");
        mvcResult = responsePost(TRASH_PAGE, pageRequest);
        returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), pageRequest.getCurrent());

        List<ApiScenarioDTO> scenarioDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ApiScenarioDTO.class);
        scenarioDTOS.forEach(caseDTO -> Assertions.assertEquals(caseDTO.getId(), "trash-api-scenario-id1"));

        //查询模块为moduleId1的数据
        pageRequest.setScenarioId(null);
        pageRequest.setModuleIds(List.of("scenario-moduleId-trash"));
        mvcResult = responsePost(TRASH_PAGE, pageRequest);
        returnPager = parseObjectFromMvcResult(mvcResult, Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), pageRequest.getCurrent());
        scenarioDTOS = JSON.parseArray(JSON.toJSONString(returnPager.getList()), ApiScenarioDTO.class);
        scenarioDTOS.forEach(scenario -> Assertions.assertEquals(scenario.getModuleId(), "scenario-moduleId-trash"));

        pageRequest.setModuleIds(null);
        pageRequest.setSort(new HashMap<>() {{
            put("createTime", "asc");
        }});
        responsePost(TRASH_PAGE, pageRequest);
        //校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_SCENARIO_READ, TRASH_PAGE, pageRequest);
    }


}