package io.metersphere.api.controller;

import io.metersphere.api.controller.param.ApiTestCaseAddRequestDefinition;
import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiDefinitionBlob;
import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.domain.ApiTestCaseBlob;
import io.metersphere.api.dto.definition.ApiTestCaseAddRequest;
import io.metersphere.api.mapper.ApiDefinitionBlobMapper;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.api.mapper.ApiTestCaseBlobMapper;
import io.metersphere.api.mapper.ApiTestCaseMapper;
import io.metersphere.api.util.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.api.request.http.MsHTTPElement;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.uid.NumGenerator;
import jakarta.annotation.Resource;
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
    protected static final String DEFAULT_LIST = "list/{0}";
    protected static final String HTTP_PROTOCOL = "HTTP";
    private static final String BASE_PATH = "/api/testCase/";
    private static final String ADD = BASE_PATH + "add";
    private static final ResultMatcher BAD_REQUEST_MATCHER = status().isBadRequest();
    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();
    private static ApiTestCase apiTestCase;
    private static ApiTestCase anotheraddapidebug;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiDefinitionBlobMapper apiDefinitionBlobMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ApiTestCaseBlobMapper apiTestCaseBlobMapper;

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
        this.apiTestCase = assertUpdateApiDebug(request, msHttpElement, resultData.getId());

        // 再插入一条数据，便于修改时重名校验
        request.setName("test1");
        paramMap.clear();
        paramMap.add("request", JSON.toJSONString(request));
        mvcResult = this.requestMultipartWithOkAndReturn(ADD, paramMap);
        resultData = getResultData(mvcResult, ApiTestCase.class);
        this.anotheraddapidebug = assertUpdateApiDebug(request, msHttpElement, resultData.getId());

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
        checkLog(this.apiTestCase.getId(), OperationLogType.ADD);
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
        copyApiDebug = BeanUtils.copyBean(copyApiDebug, request);
        Assertions.assertEquals(apiCase, copyApiDebug);
        ApiDataUtils.setResolver(MsHTTPElement.class);
        Assertions.assertEquals(msHttpElement, ApiDataUtils.parseObject(new String(apiTestCaseBlob.getRequest()), AbstractMsTestElement.class));
        return apiCase;
    }

}