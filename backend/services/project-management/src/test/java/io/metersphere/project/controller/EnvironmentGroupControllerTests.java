package io.metersphere.project.controller;


import io.metersphere.project.api.KeyValueEnableParam;
import io.metersphere.project.dto.environment.*;
import io.metersphere.project.dto.environment.http.HttpConfig;
import io.metersphere.project.service.EnvironmentGroupService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentExample;
import io.metersphere.sdk.domain.EnvironmentGroup;
import io.metersphere.sdk.domain.EnvironmentGroupExample;
import io.metersphere.sdk.mapper.EnvironmentGroupMapper;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.log.constants.OperationLogType;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EnvironmentGroupControllerTests extends BaseTest {

    private static final String prefix = "/project/environment/group";
    private static final String add = prefix + "/add";
    private static final String get = prefix + "/get/";
    private static final String update = prefix + "/update";
    private static final String delete = prefix + "/delete/";
    private static final String list = prefix + "/list";
    private static final String getProject = prefix + "/get-project/";
    private static final String POS_URL = prefix + "/edit/pos";
    private static final ResultMatcher BAD_REQUEST_MATCHER = status().isBadRequest();
    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();
    private static String GROUP_ID;
    @Resource
    private MockMvc mockMvc;
    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private EnvironmentGroupMapper environmentGroupMapper;
    private EnvironmentGroup environmentGroup;
    @Resource
    private EnvironmentGroupService environmentGroupService;

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

    private MvcResult responseGet(String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
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

    private void requestPost(String url, Object param, ResultMatcher resultMatcher) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(param))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }


    private List<HttpConfig> createHttpConfig() {
        List<HttpConfig> httpConfigs = new ArrayList<>();
        HttpConfig httpConfig = new HttpConfig();
        List<KeyValueEnableParam> headers = new ArrayList<>();
        KeyValueEnableParam keyValue = new KeyValueEnableParam();
        keyValue.setKey("key");
        keyValue.setValue("value");
        headers.add(keyValue);
        httpConfig.setHeaders(headers);
        httpConfig.setHostname("http://www.baidu.com");

        httpConfigs.add(httpConfig);
        return httpConfigs;
    }

    @Test
    @Order(1)
    public void testAddSuccess() throws Exception {
        EnvironmentRequest request = new EnvironmentRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        EnvironmentConfig envConfig = new EnvironmentConfig();

        //http配置
        envConfig.setHttpConfig(createHttpConfig());
        request.setName("httpConfig-group");
        request.setConfig(envConfig);
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.set("request", JSON.toJSONString(request));
        MvcResult mvcResult = this.requestMultipartWithOkAndReturn("/project/environment/add", paramMap);
        EnvironmentRequest response = parseObjectFromMvcResult(mvcResult, EnvironmentRequest.class);
        Assertions.assertNotNull(response);

        //创建环境组
        EnvironmentGroupRequest groupRequest = new EnvironmentGroupRequest();
        groupRequest.setProjectId(DEFAULT_PROJECT_ID);
        groupRequest.setName("group");
        //塞mock环境
        EnvironmentGroupProjectDTO environmentGroupProjectDTO1 = new EnvironmentGroupProjectDTO();
        EnvironmentExample environmentExample = new EnvironmentExample();
        environmentExample.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andMockEqualTo(true);
        List<Environment> environments = environmentMapper.selectByExample(environmentExample);
        environmentGroupProjectDTO1.setEnvironmentId(environments.getFirst().getId());
        environmentGroupProjectDTO1.setProjectId(DEFAULT_PROJECT_ID);
        groupRequest.setEnvGroupProject(List.of(environmentGroupProjectDTO1));
        mvcResult = this.responsePost(add, groupRequest);
        EnvironmentGroup groupResponse = parseObjectFromMvcResult(mvcResult, EnvironmentGroup.class);
        Assertions.assertNotNull(groupResponse);
        environmentGroup = environmentGroupMapper.selectByPrimaryKey(groupResponse.getId());
        Assertions.assertNotNull(environmentGroup);
        Assertions.assertEquals(groupResponse.getId(), environmentGroup.getId());
        Assertions.assertEquals(groupResponse.getName(), environmentGroup.getName());
        checkLog(environmentGroup.getId(), OperationLogType.ADD);
        environmentGroupService.getEnvironmentGroupRelations(List.of(environmentGroup.getId()));
        environmentGroupService.getEnvironmentGroupRelations(new ArrayList<>());
        //校验权限
        groupRequest.setName("校验权限");
        requestPostPermissionTest(PermissionConstants.PROJECT_ENVIRONMENT_READ_ADD, add, groupRequest);

    }

    @Test
    @Order(2)
    public void testAddError() throws Exception {
        //校验参数 项目id为空
        EnvironmentGroupRequest request = new EnvironmentGroupRequest();
        request.setProjectId(null);
        request.setName("name111");
        request.setEnvGroupProject(new ArrayList<>());
        requestPost(add, request, BAD_REQUEST_MATCHER);

        //名称为空
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setName(null);
        request.setEnvGroupProject(new ArrayList<>());
        requestPost(add, request, BAD_REQUEST_MATCHER);

        //环境不存在
        request.setName("test");
        EnvironmentGroupProjectDTO environmentGroupProjectDTO = new EnvironmentGroupProjectDTO();
        environmentGroupProjectDTO.setEnvironmentId(null);
        environmentGroupProjectDTO.setProjectId(DEFAULT_PROJECT_ID);
        request.setEnvGroupProject(List.of(environmentGroupProjectDTO));
        requestPost(add, request, ERROR_REQUEST_MATCHER);
        //项目不存在
        environmentGroupProjectDTO.setProjectId("ceshi");
        environmentGroupProjectDTO.setEnvironmentId("ceshi");
        request.setEnvGroupProject(List.of(environmentGroupProjectDTO));
        requestPost(add, request, ERROR_REQUEST_MATCHER);
        //重名
        environmentGroupProjectDTO.setProjectId(DEFAULT_PROJECT_ID);
        environmentGroupProjectDTO.setEnvironmentId("environmentId");
        request.setEnvGroupProject(List.of(environmentGroupProjectDTO));
        request.setName("group");
        requestPost(add, request, ERROR_REQUEST_MATCHER);

    }


    @Test
    @Order(3)
    public void testGet() throws Exception {
        //环境不存在
        MvcResult mvcResult = this.responseGet(get + getEnvironmentGroup());
        EnvironmentGroupDTO response = parseObjectFromMvcResult(mvcResult, EnvironmentGroupDTO.class);
        Assertions.assertNotNull(response);
    }


    public String getEnvironmentGroup() {
        if (GROUP_ID != null) {
            return GROUP_ID;
        }
        EnvironmentGroupExample example = new EnvironmentGroupExample();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andNameEqualTo("group");
        GROUP_ID = environmentGroupMapper.selectByExample(example).getFirst().getId();
        return GROUP_ID;
    }

    @Test
    @Order(4)
    public void testUpdate() throws Exception {
        //创建环境组
        EnvironmentGroupRequest groupRequest = new EnvironmentGroupRequest();
        groupRequest.setProjectId(DEFAULT_PROJECT_ID);
        groupRequest.setName("group");
        groupRequest.setId(getEnvironmentGroup());
        EnvironmentGroupProjectDTO environmentGroupProjectDTO = new EnvironmentGroupProjectDTO();
        EnvironmentExample environmentExample = new EnvironmentExample();
        environmentExample.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andNameEqualTo("httpConfig-group");
        List<Environment> environments = environmentMapper.selectByExample(environmentExample);
        environmentGroupProjectDTO.setEnvironmentId(environments.getFirst().getId());
        environmentGroupProjectDTO.setProjectId(DEFAULT_PROJECT_ID);
        groupRequest.setEnvGroupProject(List.of(environmentGroupProjectDTO));
        MvcResult mvcResult = this.responsePost(update, groupRequest);
        EnvironmentGroup groupResponse = parseObjectFromMvcResult(mvcResult, EnvironmentGroup.class);
        Assertions.assertNotNull(groupResponse);
        environmentGroup = environmentGroupMapper.selectByPrimaryKey(groupResponse.getId());
        Assertions.assertNotNull(environmentGroup);
        Assertions.assertEquals(groupResponse.getId(), environmentGroup.getId());
        Assertions.assertEquals(groupResponse.getName(), environmentGroup.getName());
        checkLog(environmentGroup.getId(), OperationLogType.ADD);
        groupRequest.setEnvGroupProject(new ArrayList<>());
        requestPost(update, groupRequest);
        groupRequest.setEnvGroupProject(List.of(environmentGroupProjectDTO, environmentGroupProjectDTO));
        requestPost(update, groupRequest, ERROR_REQUEST_MATCHER);
        //校验权限
        groupRequest.setName("校验权限");
        requestPostPermissionTest(PermissionConstants.PROJECT_ENVIRONMENT_READ_UPDATE, update, groupRequest);
        checkLog(environmentGroup.getId(), OperationLogType.UPDATE);

        //校验参数 项目id为空
        EnvironmentGroupRequest request = new EnvironmentGroupRequest();
        request.setProjectId(null);
        request.setName("name111");
        request.setId(getEnvironmentGroup());
        request.setEnvGroupProject(new ArrayList<>());
        requestPost(update, request, BAD_REQUEST_MATCHER);

        //名称为空
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setName(null);
        request.setEnvGroupProject(new ArrayList<>());
        request.setId(getEnvironmentGroup());
        requestPost(update, request, BAD_REQUEST_MATCHER);

        //环境不存在
        request.setName("test");
        environmentGroupProjectDTO = new EnvironmentGroupProjectDTO();
        environmentGroupProjectDTO.setEnvironmentId(null);
        environmentGroupProjectDTO.setProjectId(DEFAULT_PROJECT_ID);
        request.setEnvGroupProject(List.of(environmentGroupProjectDTO));
        request.setId(getEnvironmentGroup());
        requestPost(update, request, ERROR_REQUEST_MATCHER);
        //项目不存在
        environmentGroupProjectDTO.setProjectId("ceshi");
        environmentGroupProjectDTO.setEnvironmentId("ceshi");
        request.setEnvGroupProject(List.of(environmentGroupProjectDTO));
        request.setId(getEnvironmentGroup());
        requestPost(update, request, ERROR_REQUEST_MATCHER);
        //资源id不存在
        request.setId(null);
        requestPost(update, request, ERROR_REQUEST_MATCHER);
        //资源不存在
        request.setId("ceshi");
        requestPost(update, request, ERROR_REQUEST_MATCHER);

    }

    @Test
    @Order(11)
    public void testList() throws Exception {
        EnvironmentFilterRequest environmentDTO = new EnvironmentFilterRequest();
        environmentDTO.setProjectId(DEFAULT_PROJECT_ID);
        MvcResult mvcResult = this.responsePost(list, environmentDTO);
        List<EnvironmentGroup> response = parseObjectFromMvcResult(mvcResult, List.class);
        Assertions.assertNotNull(response);
        //校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_ENVIRONMENT_READ, list, environmentDTO);
    }

    @Test
    @Order(10)
    public void testPos() throws Exception {
        PosRequest posRequest = new PosRequest();
        posRequest.setProjectId(DEFAULT_PROJECT_ID);
        posRequest.setTargetId(getEnvironmentGroup());
        EnvironmentGroupExample example = new EnvironmentGroupExample();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andNameEqualTo("校验权限");
        List<EnvironmentGroup> environments = environmentGroupMapper.selectByExample(example);
        posRequest.setMoveId(environments.getFirst().getId());
        posRequest.setMoveMode("AFTER");
        this.requestPostWithOkAndReturn(POS_URL, posRequest);

        posRequest.setMoveMode("BEFORE");
        this.requestPostWithOkAndReturn(POS_URL, posRequest);
        environmentGroupService.refreshPos(DEFAULT_PROJECT_ID);

    }

    @Test
    @Order(12)
    public void testDeleteSuccess() throws Exception {
        //校验参数

        this.requestGet(delete + getEnvironmentGroup());
        //校验日志
        checkLog(getEnvironmentGroup(), OperationLogType.DELETE);
        //校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_ENVIRONMENT_READ_DELETE, delete + getEnvironmentGroup());

        //删除环境不存在的
        this.requestGet(delete + "environmentId2", ERROR_REQUEST_MATCHER);

    }

    @Test
    @Order(13)
    public void testGetProject() throws Exception {
        MvcResult mvcResult = this.responseGet(getProject + DEFAULT_ORGANIZATION_ID);
        List<OptionDTO> response = parseObjectFromMvcResult(mvcResult, List.class);
        Assertions.assertNotNull(response);

        //校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_ENVIRONMENT_READ, getProject + "/" + DEFAULT_ORGANIZATION_ID);

    }


}
