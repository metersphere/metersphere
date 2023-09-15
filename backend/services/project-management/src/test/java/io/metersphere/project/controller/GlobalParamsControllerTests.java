package io.metersphere.project.controller;

import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.constants.VariableTypeConstants;
import io.metersphere.sdk.domain.ProjectParameters;
import io.metersphere.sdk.domain.ProjectParametersExample;
import io.metersphere.sdk.dto.environment.GlobalParamsDTO;
import io.metersphere.sdk.dto.environment.GlobalParamsRequest;
import io.metersphere.sdk.dto.environment.KeyValue;
import io.metersphere.sdk.dto.environment.variables.CommonVariables;
import io.metersphere.sdk.mapper.ProjectParametersMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GlobalParamsControllerTests extends BaseTest {
    @Resource
    private MockMvc mockMvc;
    private static final String prefix = "/project/global/params";
    private static final String add = prefix + "/add";
    private static final String get = prefix + "/get/";
    private static final String update = prefix + "/update";
    private static final ResultMatcher BAD_REQUEST_MATCHER = status().isBadRequest();
    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();

    @Resource
    private ProjectParametersMapper projectParametersMapper;

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

    //根据需要多长的list 生成不同的List<EnvVariables> envVariables
    private List<CommonVariables> getEnvVariables(int length) {
        List<CommonVariables> commonVariables = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            CommonVariables envVariable = new CommonVariables();
            envVariable.setName("name" + i);
            envVariable.setValue("value" + i);
            envVariable.setDescription("desc" + i);
            envVariable.setType(VariableTypeConstants.CONSTANT.name());
            commonVariables.add(envVariable);
        }
        return commonVariables;
    }

    //根据需要多长的list 生成不同的List<KeyValue> headers
    private List<KeyValue> getHeaders(int length) {
        List<KeyValue> headers = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            KeyValue header = new KeyValue();
            header.setName("key" + i);
            header.setValue("value" + i);
            headers.add(header);
        }
        return headers;
    }

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_project.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testAddSuccess() throws Exception {
        //添加全局参数 有headers 有envVariables
        GlobalParamsRequest request = new GlobalParamsRequest();
        request.setProjectId("projectId1");
        GlobalParamsDTO globalParamsDTO = new GlobalParamsDTO();
        globalParamsDTO.setHeaders(getHeaders(1));
        globalParamsDTO.setCommonVariables(getEnvVariables(1));
        request.setGlobalParams(globalParamsDTO);
        MvcResult mvcResult = this.responsePost(add, request);
        GlobalParamsRequest globalParamsRequest = parseObjectFromMvcResult(mvcResult, GlobalParamsRequest.class);
        Assertions.assertNotNull(globalParamsRequest);
        ProjectParameters projectParameters = projectParametersMapper.selectByPrimaryKey(globalParamsRequest.getId());
        Assertions.assertNotNull(projectParameters);
        Assertions.assertEquals("projectId1", projectParameters.getProjectId());
        Assertions.assertEquals(1, JSON.parseObject(new String(projectParameters.getParameters()), GlobalParamsDTO.class).getHeaders().size());
        Assertions.assertEquals(1, JSON.parseObject(new String(projectParameters.getParameters()), GlobalParamsDTO.class).getCommonVariables().size());

        //添加全局参数 有headers 无envVariables
        request = new GlobalParamsRequest();
        request.setProjectId("projectId2");
        globalParamsDTO = new GlobalParamsDTO();
        globalParamsDTO.setHeaders(getHeaders(1));
        globalParamsDTO.setCommonVariables(new ArrayList<>());
        request.setGlobalParams(globalParamsDTO);
        mvcResult = this.responsePost(add, request);
        globalParamsRequest = parseObjectFromMvcResult(mvcResult, GlobalParamsRequest.class);
        Assertions.assertNotNull(globalParamsRequest);
        projectParameters = projectParametersMapper.selectByPrimaryKey(globalParamsRequest.getId());
        Assertions.assertNotNull(projectParameters);
        Assertions.assertEquals("projectId2", projectParameters.getProjectId());
        Assertions.assertEquals(1, JSON.parseObject(new String(projectParameters.getParameters()), GlobalParamsDTO.class).getHeaders().size());
        Assertions.assertEquals(0, JSON.parseObject(new String(projectParameters.getParameters()), GlobalParamsDTO.class).getCommonVariables().size());

        //添加全局参数 无headers 有envVariables
        request = new GlobalParamsRequest();
        request.setProjectId("projectId3");
        globalParamsDTO = new GlobalParamsDTO();
        globalParamsDTO.setHeaders(new ArrayList<>());
        globalParamsDTO.setCommonVariables(getEnvVariables(1));
        request.setGlobalParams(globalParamsDTO);
        mvcResult = this.responsePost(add, request);
        globalParamsRequest = parseObjectFromMvcResult(mvcResult, GlobalParamsRequest.class);
        Assertions.assertNotNull(globalParamsRequest);
        projectParameters = projectParametersMapper.selectByPrimaryKey(globalParamsRequest.getId());
        Assertions.assertNotNull(projectParameters);
        Assertions.assertEquals("projectId3", projectParameters.getProjectId());
        Assertions.assertEquals(0, JSON.parseObject(new String(projectParameters.getParameters()), GlobalParamsDTO.class).getHeaders().size());
        Assertions.assertEquals(1, JSON.parseObject(new String(projectParameters.getParameters()), GlobalParamsDTO.class).getCommonVariables().size());

        //添加全局参数 无headers 无envVariables
        request = new GlobalParamsRequest();
        request.setProjectId("projectId4");
        globalParamsDTO = new GlobalParamsDTO();
        globalParamsDTO.setHeaders(new ArrayList<>());
        globalParamsDTO.setCommonVariables(new ArrayList<>());
        request.setGlobalParams(globalParamsDTO);
        mvcResult = this.responsePost(add, request);
        globalParamsRequest = parseObjectFromMvcResult(mvcResult, GlobalParamsRequest.class);
        Assertions.assertNotNull(globalParamsRequest);
        projectParameters = projectParametersMapper.selectByPrimaryKey(globalParamsRequest.getId());
        Assertions.assertNotNull(projectParameters);
        Assertions.assertEquals("projectId4", projectParameters.getProjectId());
        Assertions.assertEquals(0, JSON.parseObject(new String(projectParameters.getParameters()), GlobalParamsDTO.class).getHeaders().size());
        Assertions.assertEquals(0, JSON.parseObject(new String(projectParameters.getParameters()), GlobalParamsDTO.class).getCommonVariables().size());

        request = new GlobalParamsRequest();
        request.setProjectId("projectId5");
        request.setGlobalParams(new GlobalParamsDTO());
        mvcResult = this.responsePost(add, request);
        globalParamsRequest = parseObjectFromMvcResult(mvcResult, GlobalParamsRequest.class);
        Assertions.assertNotNull(globalParamsRequest);
        projectParameters = projectParametersMapper.selectByPrimaryKey(globalParamsRequest.getId());
        Assertions.assertNotNull(projectParameters);
        Assertions.assertEquals("projectId5", projectParameters.getProjectId());
        Assertions.assertNull(JSON.parseObject(new String(projectParameters.getParameters()), GlobalParamsDTO.class).getHeaders());
        Assertions.assertNull(JSON.parseObject(new String(projectParameters.getParameters()), GlobalParamsDTO.class).getCommonVariables());

        //校验权限
        request = new GlobalParamsRequest();
        request.setProjectId("100001100001");
        //权限校验
        requestPostPermissionTest(PermissionConstants.PROJECT_ENVIRONMENT_READ_ADD, add, request);
    }

    @Test
    @Order(2)
    public void testAddError() throws Exception {
        // 添加全局参数 无projectId
        GlobalParamsRequest request = new GlobalParamsRequest();
        this.requestPost(add, request, BAD_REQUEST_MATCHER);

        // 添加全局参数 projectId不存在
        request = new GlobalParamsRequest();
        request.setProjectId("projectId1111");
        this.requestPost(add, request, ERROR_REQUEST_MATCHER);

        // 添加全局参数 已存在
        request = new GlobalParamsRequest();
        request.setProjectId("projectId1");
        this.requestPost(add, request, ERROR_REQUEST_MATCHER);
    }

    @Test
    @Order(3)
    public void testUpdateSuccess() throws Exception {
        //修改全局参数 有headers 有envVariables
        ProjectParametersExample example = new ProjectParametersExample();
        example.createCriteria().andProjectIdEqualTo("projectId1");
        List<ProjectParameters> projectParametersList = projectParametersMapper.selectByExample(example);
        GlobalParamsRequest request = new GlobalParamsRequest();
        request.setProjectId("projectId1");
        request.setId(projectParametersList.get(0).getId());
        GlobalParamsDTO globalParamsDTO = new GlobalParamsDTO();
        globalParamsDTO.setHeaders(getHeaders(2));
        globalParamsDTO.setCommonVariables(getEnvVariables(2));
        request.setGlobalParams(globalParamsDTO);
        MvcResult mvcResult = this.responsePost(update, request);
        GlobalParamsRequest globalParamsRequest = parseObjectFromMvcResult(mvcResult, GlobalParamsRequest.class);
        Assertions.assertNotNull(globalParamsRequest);
        ProjectParameters projectParameters = projectParametersMapper.selectByPrimaryKey(globalParamsRequest.getId());
        Assertions.assertNotNull(projectParameters);
        Assertions.assertEquals("projectId1", projectParameters.getProjectId());
        Assertions.assertEquals(2, JSON.parseObject(new String(projectParameters.getParameters()), GlobalParamsDTO.class).getHeaders().size());
        Assertions.assertEquals(2, JSON.parseObject(new String(projectParameters.getParameters()), GlobalParamsDTO.class).getCommonVariables().size());

        //校验权限
        request = new GlobalParamsRequest();
        example = new ProjectParametersExample();
        example.createCriteria().andProjectIdEqualTo("100001100001");
        projectParametersList = projectParametersMapper.selectByExample(example);
        request.setProjectId("100001100001");
        request.setId(projectParametersList.get(0).getId());
        //权限校验
        requestPostPermissionTest(PermissionConstants.PROJECT_ENVIRONMENT_READ_UPDATE, update, request);

    }

    @Test
    @Order(4)
    public void testUpdateError() throws Exception {
        // 修改全局参数 无projectId
        GlobalParamsRequest request = new GlobalParamsRequest();
        request.setProjectId(null);
        request.setId("id");
        this.requestPost(update, request, BAD_REQUEST_MATCHER);

        // 修改全局参数 projectId不存在
        request = new GlobalParamsRequest();
        request.setId("id");
        request.setProjectId("projectId1111");
        this.requestPost(update, request, ERROR_REQUEST_MATCHER);

        ProjectParametersExample example = new ProjectParametersExample();
        example.createCriteria().andProjectIdEqualTo("projectId2");
        projectParametersMapper.deleteByExample(example);
        // 修改全局参数 全局参数不存在
        request = new GlobalParamsRequest();
        request.setId("id");
        request.setProjectId("projectId2");
        this.requestPost(update, request, ERROR_REQUEST_MATCHER);

    }

    @Test
    @Order(5)
    public void testGetSuccess() throws Exception {
        //获取全局参数
        MvcResult mvcResult = this.responseGet(get + "projectId1");
        GlobalParamsRequest globalParamsRequest = parseObjectFromMvcResult(mvcResult, GlobalParamsRequest.class);
        Assertions.assertNotNull(globalParamsRequest);
        Assertions.assertEquals("projectId1", globalParamsRequest.getProjectId());
        Assertions.assertEquals(2, globalParamsRequest.getGlobalParams().getHeaders().size());
        Assertions.assertEquals(2, globalParamsRequest.getGlobalParams().getCommonVariables().size());

        //权限校验
        requestGetPermissionTest(PermissionConstants.PROJECT_ENVIRONMENT_READ, get + "100001100001");

        //获取全局参数 全局参数不存在
        mvcResult = this.responseGet(get + "projectId2");
        globalParamsRequest = parseObjectFromMvcResult(mvcResult, GlobalParamsRequest.class);
        Assertions.assertNull(globalParamsRequest);
    }


}
