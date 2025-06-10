package io.metersphere.api.controller;

import io.metersphere.api.constants.ApiConstants;
import io.metersphere.api.constants.ApiDefinitionStatus;
import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiDefinitionBlob;
import io.metersphere.api.dto.ApiCaseAIConfigDTO;
import io.metersphere.api.dto.definition.ApiTestCaseAIRequest;
import io.metersphere.api.dto.definition.ApiTestCaseAiAddRequest;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.mapper.ApiDefinitionBlobMapper;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.dto.request.ai.AIChatRequest;
import io.metersphere.system.uid.NumGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.Assert;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiTestCaseAiControllerTests extends BaseTest {
    private static final String BASE_PATH = "/api/case/ai/";
    private static final String CHAT = "chat";
    public static final String EDIT_CONFIG = "/save/config";
    public static final String GET_CONFIG = "/get/config";
    public static final String TRANSFORM = "/transform";
    public static final String BATCH_SAVE = "/batch/save";

    private static String apiDefinitionId = UUID.randomUUID().toString();
    private static String anotherApiDefinitionId = UUID.randomUUID().toString();
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiDefinitionBlobMapper apiDefinitionBlobMapper;

    @Override
    public String getBasePath() {
        return BASE_PATH;
    }


    public void initApiData() {
        ApiDefinition apiDefinition = new ApiDefinition();
        apiDefinition.setId(apiDefinitionId);
        apiDefinition.setProjectId(DEFAULT_PROJECT_ID);
        apiDefinition.setName(StringUtils.join("接口定义", apiDefinition.getId()));
        apiDefinition.setModuleId("case-moduleId");
        apiDefinition.setProtocol(ApiConstants.HTTP_PROTOCOL);
        apiDefinition.setMethod("GET");
        apiDefinition.setStatus(ApiDefinitionStatus.DEBUGGING.name());
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
        MsHTTPElement msHttpElement = MsHTTPElementTest.getMsHttpElement();
        apiDefinitionBlob.setRequest(JSON.toJSONBytes(msHttpElement));
        apiDefinitionBlobMapper.insertSelective(apiDefinitionBlob);
        apiDefinition.setId(anotherApiDefinitionId);
        apiDefinition.setModuleId("moduleId1");
        apiDefinitionMapper.insertSelective(apiDefinition);
    }

    @Test
    @Order(1)
    public void chat() throws Exception {
        initApiData();
        ApiTestCaseAIRequest apiTestCaseAIRequest = new ApiTestCaseAIRequest();
        apiTestCaseAIRequest.setApiDefinitionId(apiDefinitionId);
        apiTestCaseAIRequest.setChatModelId("deepseek-chat");
        apiTestCaseAIRequest.setPrompt("生成一个用例");
        apiTestCaseAIRequest.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        apiTestCaseAIRequest.setConversationId(UUID.randomUUID().toString());
        this.requestPost(CHAT, apiTestCaseAIRequest);

        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ, CHAT, apiTestCaseAIRequest);
    }

    @Test
    @Order(2)
    public void testEdit() throws Exception {
        ApiCaseAIConfigDTO apiCaseAIConfigDTO = new ApiCaseAIConfigDTO();
        apiCaseAIConfigDTO.setAbnormal(true);
        apiCaseAIConfigDTO.setNormal(true);
        apiCaseAIConfigDTO.setCaseName(true);
        apiCaseAIConfigDTO.setRequestParams(true);
        apiCaseAIConfigDTO.setPostScript(true);
        apiCaseAIConfigDTO.setPreScript(true);
        apiCaseAIConfigDTO.setAssertion(true);
        this.requestPost(EDIT_CONFIG, apiCaseAIConfigDTO).andExpect(status().isOk());
    }

    @Test
    @Order(3)
    public void testGet() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(GET_CONFIG);
        ApiCaseAIConfigDTO apiCaseAIConfigDTO = getResultData(mvcResult, ApiCaseAIConfigDTO.class);
        Assert.notNull(apiCaseAIConfigDTO, "获取AI提示词配置失败");
        System.out.println(apiCaseAIConfigDTO);


    }

    @Test
    @Order(3)
    public void transform() throws Exception {
        AIChatRequest aiChatRequest = new AIChatRequest();
        aiChatRequest.setPrompt("# 用例名称\n" +
                "登入成功用例\n" +
                "\n" +
                "## 请求头\n" +
                "| 参数名称 | 参数值 | 描述 |\n" +
                "| --- | --- | --- |\n" +
                "| name | admin | 用户名 |\n" +
                "\n" +
                "## Query参数\n" +
                "| 参数名称 | 类型 | 参数值 | 描述 |\n" +
                "| --- | --- | --- | --- |\n" +
                "| name | string | admin | 用户名 |");
        aiChatRequest.setChatModelId("deepseek-chat");
        aiChatRequest.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        aiChatRequest.setConversationId(UUID.randomUUID().toString());

        this.requestPost(TRANSFORM, aiChatRequest);

    }


    @Test
    @Order(4)
    public void batchSave() throws Exception {
        ApiTestCaseAiAddRequest aiChatRequest = new ApiTestCaseAiAddRequest();
        aiChatRequest.setPrompt("caseStart\n" +
                "# 用例名称\n" +
                "登入成功用例测试1\n" +
                "\n" +
                "## 请求头\n" +
                "| 参数名称 | 参数值 | 描述   |\n" +
                "| -------- | ------ | ------ |\n" +
                "| name     | admin  | 用户名 |\n" +
                "\n" +
                "## Query参数\n" +
                "| 参数名称 | 类型   | 参数值 | 描述   |\n" +
                "| -------- | ------ | ------ | ------ |\n" +
                "| name     | string | admin  | 用户名 |\n" +
                "\n" +
                "## Rest参数\n" +
                "| 参数名称 | 类型   | 参数值 | 描述   |\n" +
                "| -------- | ------ | ------ | ------ |\n" +
                "| name     | string | admin  | 用户名 |\n" +
                "\n" +
                "# 请求体\n" +
                "请求体类型： from-data/x-www-form-urlencoded\n" +
                "\n" +
                "| 参数名称 | 类型   | 参数值 | 描述   |\n" +
                "| -------- | ------ | ------ | ------ |\n" +
                "| name     | string | admin  | 用户名 |\n" +
                "\n" +
                "请求体类型：json/xml/raw\n" +
                "\n" +
                "```json/xml/tex\n" +
                "{\n" +
                "  \"name\":\"admin\"\n" +
                "}\n" +
                "```\n" +
                "\n" +
                "# 断言\n" +
                "### 状态码\n" +
                "| 匹配条件 | 匹配值 |\n" +
                "| 等于 | admin |\n" +
                "\n" +
                "### 响应头\n" +
                "| 响应头       | 匹配条件 | 匹配值 |\n" +
                "| ------------ | -------- | ------ |\n" +
                "| Content-Type | 等于     | admin  |\n" +
                "\n" +
                "### 响应体\n" +
                "#### JSONPath\n" +
                "| 表达式 | 匹配条件    | 匹配值 |\n" +
                "| ------ | ----------- | ------ |\n" +
                "| $.name | 等于/不等于 | admin  |\n" +
                "\n" +
                "#### xpath\n" +
                "响应格式： XML/HTML\n" +
                "\n" +
                "| 表达式                        |\n" +
                "| ----------------------------- |\n" +
                "| /html/body//a/@href=http://xx |\n" +
                "\n" +
                "#### 正则\n" +
                "| 表达式    |\n" +
                "| --------- |\n" +
                "| (?=\\d{3}) |\n" +
                "\n" +
                "# 前置脚本\n" +
                "获取登入token\n" +
                "# 后置脚本\n" +
                "清理缓存\n" +
                "caseEnd" +
                "caseStart\n" +
                "# 用例名称\n" +
                "登入成功用例测试2\n" +
                "\n" +
                "## 请求头\n" +
                "| 参数名称 | 参数值 | 描述   |\n" +
                "| -------- | ------ | ------ |\n" +
                "| name     | admin  | 用户名 |\n" +
                "\n" +
                "## Query参数\n" +
                "| 参数名称 | 类型   | 参数值 | 描述   |\n" +
                "| -------- | ------ | ------ | ------ |\n" +
                "| name     | string | admin  | 用户名 |\n" +
                "\n" +
                "## Rest参数\n" +
                "| 参数名称 | 类型   | 参数值 | 描述   |\n" +
                "| -------- | ------ | ------ | ------ |\n" +
                "| name     | string | admin  | 用户名 |\n" +
                "\n" +
                "# 请求体\n" +
                "请求体类型： from-data/x-www-form-urlencoded\n" +
                "\n" +
                "| 参数名称 | 类型   | 参数值 | 描述   |\n" +
                "| -------- | ------ | ------ | ------ |\n" +
                "| name     | string | admin  | 用户名 |\n" +
                "\n" +
                "请求体类型：json/xml/raw\n" +
                "\n" +
                "```json/xml/tex\n" +
                "{\n" +
                "  \"name\":\"admin\"\n" +
                "}\n" +
                "```\n" +
                "\n" +
                "# 断言\n" +
                "### 状态码\n" +
                "| 匹配条件 | 匹配值 |\n" +
                "| 等于 | admin |\n" +
                "\n" +
                "### 响应头\n" +
                "| 响应头       | 匹配条件 | 匹配值 |\n" +
                "| ------------ | -------- | ------ |\n" +
                "| Content-Type | 等于     | admin  |\n" +
                "\n" +
                "### 响应体\n" +
                "#### JSONPath\n" +
                "| 表达式 | 匹配条件    | 匹配值 |\n" +
                "| ------ | ----------- | ------ |\n" +
                "| $.name | 等于/不等于 | admin  |\n" +
                "\n" +
                "#### xpath\n" +
                "响应格式： XML/HTML\n" +
                "\n" +
                "| 表达式                        |\n" +
                "| ----------------------------- |\n" +
                "| /html/body//a/@href=http://xx |\n" +
                "\n" +
                "#### 正则\n" +
                "| 表达式    |\n" +
                "| --------- |\n" +
                "| (?=\\d{3}) |\n" +
                "\n" +
                "# 前置脚本\n" +
                "获取登入token\n" +
                "# 后置脚本\n" +
                "清理缓存\n" +
                "caseEnd");
        aiChatRequest.setChatModelId("deepseek-chat");
        aiChatRequest.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        aiChatRequest.setProjectId(DEFAULT_PROJECT_ID);
        aiChatRequest.setApiDefinitionId(apiDefinitionId);
        aiChatRequest.setConversationId(UUID.randomUUID().toString());

        this.requestPost(BATCH_SAVE, aiChatRequest);

    }
}