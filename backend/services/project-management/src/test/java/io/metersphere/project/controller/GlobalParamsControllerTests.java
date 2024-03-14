package io.metersphere.project.controller;

import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.environment.GlobalParams;
import io.metersphere.project.dto.environment.GlobalParamsDTO;
import io.metersphere.project.dto.environment.GlobalParamsRequest;
import io.metersphere.project.api.KeyValueEnableParam;
import io.metersphere.project.dto.environment.variables.CommonVariables;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.constants.VariableTypeConstants;
import io.metersphere.sdk.domain.ProjectParameter;
import io.metersphere.sdk.domain.ProjectParameterExample;
import io.metersphere.sdk.mapper.ProjectParameterMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.invoker.ProjectServiceInvoker;
import io.metersphere.system.log.constants.OperationLogType;
import jakarta.annotation.Resource;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GlobalParamsControllerTests extends BaseTest {
    @Resource
    private MockMvc mockMvc;
    private static final String prefix = "/project/global/params";
    private static final String add = prefix + "/add";
    private static final String get = prefix + "/get/";
    private static final String update = prefix + "/update";
    private static final String export = prefix + "/export/%s";
    private static final String importUrl = prefix + "/import";
    private static final ResultMatcher BAD_REQUEST_MATCHER = status().isBadRequest();
    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();

    @Resource
    private ProjectParameterMapper projectParametersMapper;
    private final ProjectServiceInvoker serviceInvoker;
    @Resource
    private ProjectMapper projectMapper;

    @Autowired
    public GlobalParamsControllerTests(ProjectServiceInvoker serviceInvoker) {
        this.serviceInvoker = serviceInvoker;
    }

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
            envVariable.setKey("name" + i);
            envVariable.setValue("value" + i);
            envVariable.setDescription("desc" + i);
            envVariable.setTags(List.of("tag" + i));
            envVariable.setParamType(VariableTypeConstants.CONSTANT.name());
            commonVariables.add(envVariable);
        }
        return commonVariables;
    }

    //根据需要多长的list 生成不同的List<KeyValue> headers
    private List<KeyValueEnableParam> getHeaders(int length) {
        List<KeyValueEnableParam> headers = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            KeyValueEnableParam header = new KeyValueEnableParam();
            header.setKey("key" + i);
            header.setValue("value" + i);
            headers.add(header);
        }
        return headers;
    }

    public void initData() {
        if (projectMapper.selectByPrimaryKey("projectId") == null) {
            Project initProject = new Project();
            initProject.setId("projectId");
            initProject.setNum(null);
            initProject.setOrganizationId(DEFAULT_ORGANIZATION_ID);
            initProject.setName("项目");
            initProject.setDescription("项目");
            initProject.setCreateUser("admin");
            initProject.setUpdateUser("admin");
            initProject.setCreateTime(System.currentTimeMillis());
            initProject.setUpdateTime(System.currentTimeMillis());
            initProject.setEnable(true);
            initProject.setModuleSetting("[\"apiTest\",\"uiTest\"]");
            projectMapper.insertSelective(initProject);
            serviceInvoker.invokeCreateServices(initProject.getId());
        }
        if (projectMapper.selectByPrimaryKey("projectId1") == null) {
            Project initProject = new Project();
            initProject.setId("projectId1");
            initProject.setNum(null);
            initProject.setOrganizationId(DEFAULT_ORGANIZATION_ID);
            initProject.setName("项目1");
            initProject.setDescription("项目1");
            initProject.setCreateUser("admin");
            initProject.setUpdateUser("admin");
            initProject.setCreateTime(System.currentTimeMillis());
            initProject.setUpdateTime(System.currentTimeMillis());
            initProject.setEnable(true);
            projectMapper.insertSelective(initProject);
            serviceInvoker.invokeCreateServices(initProject.getId());
        }
        if (projectMapper.selectByPrimaryKey("projectId2") == null) {
            Project initProject = new Project();
            initProject.setId("projectId2");
            initProject.setNum(null);
            initProject.setOrganizationId(DEFAULT_ORGANIZATION_ID);
            initProject.setName("项目2");
            initProject.setDescription("项目2");
            initProject.setCreateUser("admin");
            initProject.setUpdateUser("admin");
            initProject.setCreateTime(System.currentTimeMillis());
            initProject.setUpdateTime(System.currentTimeMillis());
            initProject.setEnable(true);
            projectMapper.insertSelective(initProject);
            serviceInvoker.invokeCreateServices(initProject.getId());
        }
        if (projectMapper.selectByPrimaryKey("projectId3") == null) {
            Project initProject = new Project();
            initProject.setId("projectId3");
            initProject.setNum(null);
            initProject.setOrganizationId(DEFAULT_ORGANIZATION_ID);
            initProject.setName("项目3");
            initProject.setDescription("项目3");
            initProject.setCreateUser("admin");
            initProject.setUpdateUser("admin");
            initProject.setCreateTime(System.currentTimeMillis());
            initProject.setUpdateTime(System.currentTimeMillis());
            initProject.setEnable(true);
            initProject.setModuleSetting("[\"apiTest\",\"uiTest\"]");
            projectMapper.insertSelective(initProject);
            serviceInvoker.invokeCreateServices(initProject.getId());
        }
        if (projectMapper.selectByPrimaryKey("projectId4") == null) {
            Project initProject = new Project();
            initProject.setId("projectId4");
            initProject.setNum(null);
            initProject.setOrganizationId(DEFAULT_ORGANIZATION_ID);
            initProject.setName("项目4");
            initProject.setDescription("项目4");
            initProject.setCreateUser("admin");
            initProject.setUpdateUser("admin");
            initProject.setCreateTime(System.currentTimeMillis());
            initProject.setUpdateTime(System.currentTimeMillis());
            initProject.setEnable(true);
            initProject.setModuleSetting("[\"apiTest\",\"uiTest\",\"loadTest\"]");
            projectMapper.insertSelective(initProject);
            serviceInvoker.invokeCreateServices(initProject.getId());
        }
        if (projectMapper.selectByPrimaryKey("projectId5") == null) {
            Project initProject = new Project();
            initProject.setId("projectId5");
            initProject.setNum(null);
            initProject.setOrganizationId(DEFAULT_ORGANIZATION_ID);
            initProject.setName("项目5");
            initProject.setDescription("项目5");
            initProject.setCreateUser("admin");
            initProject.setUpdateUser("admin");
            initProject.setCreateTime(System.currentTimeMillis());
            initProject.setUpdateTime(System.currentTimeMillis());
            initProject.setEnable(true);
            initProject.setModuleSetting("[\"apiTest\",\"uiTest\"]");
            projectMapper.insertSelective(initProject);
            serviceInvoker.invokeCreateServices(initProject.getId());
        }
    }

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_project.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testAddSuccess() throws Exception {
        initData();
        //添加全局参数 有headers 有envVariables
        GlobalParamsRequest request = new GlobalParamsRequest();
        request.setProjectId("projectId1");
        GlobalParams globalParams = new GlobalParams();
        globalParams.setHeaders(getHeaders(1));
        globalParams.setCommonVariables(getEnvVariables(1));
        request.setGlobalParams(globalParams);
        MvcResult mvcResult = this.responsePost(add, request);
        ProjectParameter globalParamsRequest = parseObjectFromMvcResult(mvcResult, ProjectParameter.class);
        Assertions.assertNotNull(globalParamsRequest);
        ProjectParameter projectParameters = projectParametersMapper.selectByPrimaryKey(globalParamsRequest.getId());
        Assertions.assertNotNull(projectParameters);
        Assertions.assertEquals("projectId1", projectParameters.getProjectId());
        Assertions.assertEquals(1, JSON.parseObject(new String(projectParameters.getParameters()), GlobalParams.class).getHeaders().size());
        Assertions.assertEquals(1, JSON.parseObject(new String(projectParameters.getParameters()), GlobalParams.class).getCommonVariables().size());

        //添加全局参数 有headers 无envVariables
        request = new GlobalParamsRequest();
        request.setProjectId("projectId2");
        globalParams = new GlobalParams();
        globalParams.setHeaders(getHeaders(1));
        globalParams.setCommonVariables(new ArrayList<>());
        request.setGlobalParams(globalParams);
        mvcResult = this.responsePost(add, request);
        globalParamsRequest = parseObjectFromMvcResult(mvcResult, ProjectParameter.class);
        Assertions.assertNotNull(globalParamsRequest);
        projectParameters = projectParametersMapper.selectByPrimaryKey(globalParamsRequest.getId());
        Assertions.assertNotNull(projectParameters);
        Assertions.assertEquals("projectId2", projectParameters.getProjectId());
        Assertions.assertEquals(1, JSON.parseObject(new String(projectParameters.getParameters()), GlobalParams.class).getHeaders().size());
        Assertions.assertEquals(0, JSON.parseObject(new String(projectParameters.getParameters()), GlobalParams.class).getCommonVariables().size());

        //添加全局参数 无headers 有envVariables
        request = new GlobalParamsRequest();
        request.setProjectId("projectId3");
        globalParams = new GlobalParams();
        globalParams.setHeaders(new ArrayList<>());
        globalParams.setCommonVariables(getEnvVariables(1));
        request.setGlobalParams(globalParams);
        mvcResult = this.responsePost(add, request);
        globalParamsRequest = parseObjectFromMvcResult(mvcResult, ProjectParameter.class);
        Assertions.assertNotNull(globalParamsRequest);
        projectParameters = projectParametersMapper.selectByPrimaryKey(globalParamsRequest.getId());
        Assertions.assertNotNull(projectParameters);
        Assertions.assertEquals("projectId3", projectParameters.getProjectId());
        Assertions.assertEquals(0, JSON.parseObject(new String(projectParameters.getParameters()), GlobalParams.class).getHeaders().size());
        Assertions.assertEquals(1, JSON.parseObject(new String(projectParameters.getParameters()), GlobalParams.class).getCommonVariables().size());

        //添加全局参数 无headers 无envVariables
        request = new GlobalParamsRequest();
        request.setProjectId("projectId4");
        globalParams = new GlobalParams();
        globalParams.setHeaders(new ArrayList<>());
        globalParams.setCommonVariables(new ArrayList<>());
        request.setGlobalParams(globalParams);
        mvcResult = this.responsePost(add, request);
        globalParamsRequest = parseObjectFromMvcResult(mvcResult, ProjectParameter.class);
        Assertions.assertNotNull(globalParamsRequest);
        projectParameters = projectParametersMapper.selectByPrimaryKey(globalParamsRequest.getId());
        Assertions.assertNotNull(projectParameters);
        Assertions.assertEquals("projectId4", projectParameters.getProjectId());
        Assertions.assertEquals(0, JSON.parseObject(new String(projectParameters.getParameters()), GlobalParams.class).getHeaders().size());
        Assertions.assertEquals(0, JSON.parseObject(new String(projectParameters.getParameters()), GlobalParams.class).getCommonVariables().size());

        request = new GlobalParamsRequest();
        request.setProjectId("projectId5");
        request.setGlobalParams(new GlobalParams());
        mvcResult = this.responsePost(add, request);
        globalParamsRequest = parseObjectFromMvcResult(mvcResult, ProjectParameter.class);
        Assertions.assertNotNull(globalParamsRequest);
        //校验日志
        checkLog(globalParamsRequest.getId(), OperationLogType.ADD);
        projectParameters = projectParametersMapper.selectByPrimaryKey(globalParamsRequest.getId());
        Assertions.assertNotNull(projectParameters);
        Assertions.assertEquals("projectId5", projectParameters.getProjectId());
        Assertions.assertNull(JSON.parseObject(new String(projectParameters.getParameters()), GlobalParams.class).getHeaders());
        Assertions.assertNull(JSON.parseObject(new String(projectParameters.getParameters()), GlobalParams.class).getCommonVariables());

        //校验权限
        request = new GlobalParamsRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        GlobalParams globalParams1 = new GlobalParams();
        globalParams1.setHeaders(getHeaders(2));
        globalParams1.setCommonVariables(getEnvVariables(2));
        request.setGlobalParams(globalParams1);
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
        ProjectParameterExample example = new ProjectParameterExample();
        example.createCriteria().andProjectIdEqualTo("projectId1");
        List<ProjectParameter> projectParametersList = projectParametersMapper.selectByExample(example);
        GlobalParamsRequest request = new GlobalParamsRequest();
        request.setProjectId("projectId1");
        request.setId(projectParametersList.getFirst().getId());
        GlobalParams globalParams = new GlobalParams();
        globalParams.setHeaders(getHeaders(2));
        globalParams.setCommonVariables(getEnvVariables(2));
        request.setGlobalParams(globalParams);
        MvcResult mvcResult = this.responsePost(update, request);
        ProjectParameter globalParamsRequest = parseObjectFromMvcResult(mvcResult, ProjectParameter.class);
        Assertions.assertNotNull(globalParamsRequest);
        //校验日志
        checkLog(globalParamsRequest.getId(), OperationLogType.UPDATE);
        ProjectParameter projectParameters = projectParametersMapper.selectByPrimaryKey(globalParamsRequest.getId());
        Assertions.assertNotNull(projectParameters);
        Assertions.assertEquals("projectId1", projectParameters.getProjectId());
        Assertions.assertEquals(2, JSON.parseObject(new String(projectParameters.getParameters()), GlobalParams.class).getHeaders().size());
        Assertions.assertEquals(2, JSON.parseObject(new String(projectParameters.getParameters()), GlobalParams.class).getCommonVariables().size());

        //校验权限
        request = new GlobalParamsRequest();
        example = new ProjectParameterExample();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID);
        projectParametersList = projectParametersMapper.selectByExample(example);
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setId(projectParametersList.getFirst().getId());
        GlobalParams globalParams1 = new GlobalParams();
        globalParams1.setHeaders(getHeaders(3));
        globalParams1.setCommonVariables(getEnvVariables(4));
        request.setGlobalParams(globalParams1);
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

        ProjectParameterExample example = new ProjectParameterExample();
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
        GlobalParamsDTO globalParamsRequest = parseObjectFromMvcResult(mvcResult, GlobalParamsDTO.class);
        Assertions.assertNotNull(globalParamsRequest);
        Assertions.assertEquals("projectId1", globalParamsRequest.getProjectId());
        Assertions.assertEquals(2, globalParamsRequest.getGlobalParams().getHeaders().size());
        Assertions.assertEquals(2, globalParamsRequest.getGlobalParams().getCommonVariables().size());

        //权限校验
        requestGetPermissionTest(PermissionConstants.PROJECT_ENVIRONMENT_READ, get + "100001100001");

        //获取全局参数 全局参数不存在
        mvcResult = this.responseGet(get + "projectId2");
        globalParamsRequest = parseObjectFromMvcResult(mvcResult, GlobalParamsDTO.class);
        Assertions.assertNull(globalParamsRequest);
    }

    @Test
    @Order(6)
    public void testExportSuccess() throws Exception {
        //导出全局参数
        MvcResult mvcResult = this.requestGetDownloadFile(String.format(export, DEFAULT_PROJECT_ID), null);
        byte[] fileBytes = mvcResult.getResponse().getContentAsByteArray();
        Assertions.assertNotNull(fileBytes);
        mockMvc.perform(getRequestBuilder(String.format(export, "321")))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @Order(7)
    public void testImportSuccess() throws Exception {
        //导入全局参数
        InputStream inputStream = new FileInputStream(new File(
                this.getClass().getClassLoader().getResource("file/globalParams.json")
                        .getPath()));
        MockMultipartFile file = new MockMultipartFile("file", "globalParams.json", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", List.of(file));
        requestMultipartWithOk(importUrl, paramMap);
        paramMap.clear();
        inputStream = new FileInputStream(new File(
                this.getClass().getClassLoader().getResource("file/huanj.json")
                        .getPath()));

        file = new MockMultipartFile("file", "huanj.json", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        paramMap.add("file", List.of(file));

        this.requestMultipart(importUrl, paramMap, status().is5xxServerError());

    }

    protected MvcResult requestMultipart(String url, MultiValueMap<String, Object> paramMap, ResultMatcher resultMatcher) throws Exception {
        MockMultipartHttpServletRequestBuilder requestBuilder = getMultipartRequestBuilderWithParam(url, paramMap);
        MockHttpServletRequestBuilder header = requestBuilder
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                .header(org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE, "zh-CN");
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

    protected ResultActions requestMultipartWithOk(String url, MultiValueMap<String, Object> paramMap, Object... uriVariables) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = getMultipartRequestBuilder(url, paramMap, uriVariables);
        return mockMvc.perform(requestBuilder)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    private static MockMultipartFile getMockMultipartFile(String key, Object value) throws IOException {
        MockMultipartFile multipartFile;
        if (value instanceof File) {
            File file = (File) value;
            multipartFile = new MockMultipartFile(key, file.getName(),
                    MediaType.APPLICATION_OCTET_STREAM_VALUE, Files.readAllBytes(file.toPath()));
        } else if (value instanceof MockMultipartFile) {
            multipartFile = (MockMultipartFile) value;
            // 有些地方的参数 name 写的是文件名，这里统一处理成参数名 key
            multipartFile = new MockMultipartFile(key, multipartFile.getOriginalFilename(),
                    MediaType.APPLICATION_OCTET_STREAM_VALUE, multipartFile.getBytes());
        } else {
            multipartFile = new MockMultipartFile(key, key,
                    MediaType.APPLICATION_JSON_VALUE, value.toString().getBytes());
        }
        return multipartFile;
    }

    private MockHttpServletRequestBuilder getMultipartRequestBuilder(String url,
                                                                     MultiValueMap<String, Object> paramMap,
                                                                     Object[] uriVariables) {
        MockMultipartHttpServletRequestBuilder requestBuilder = getMultipartRequestBuilderWithParam(url, paramMap, uriVariables);
        return requestBuilder
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                .header(SessionConstants.CURRENT_PROJECT, DEFAULT_PROJECT_ID)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN");
    }


    private MockMultipartHttpServletRequestBuilder getMultipartRequestBuilderWithParam(String url, MultiValueMap<String, Object> paramMap, Object[] uriVariables) {
        MockMultipartHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.multipart(getBasePath() + url, uriVariables);
        paramMap.forEach((key, value) -> {
            List list = value;
            for (Object o : list) {
                try {
                    if (o == null) {
                        continue;
                    }
                    MockMultipartFile multipartFile;
                    if (o instanceof List) {
                        List listObject = ((List) o);
                        if (CollectionUtils.isEmpty(listObject)) {
                            continue;
                        }
                        if (listObject.getFirst() instanceof File || listObject.getFirst() instanceof MockMultipartFile) {
                            // 参数是多个文件时,设置多个文件
                            for (Object subObject : ((List) o)) {
                                multipartFile = getMockMultipartFile(key, subObject);
                                requestBuilder.file(multipartFile);
                            }
                        } else {
                            multipartFile = getMockMultipartFile(key, o);
                            requestBuilder.file(multipartFile);
                        }
                    } else {
                        multipartFile = getMockMultipartFile(key, o);
                        requestBuilder.file(multipartFile);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        });
        return requestBuilder;
    }


}
