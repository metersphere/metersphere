package io.metersphere.api.controller;

import io.metersphere.api.constants.ApiConstants;
import io.metersphere.api.constants.ApiDefinitionStatus;
import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiDefinitionBlob;
import io.metersphere.api.dto.definition.ApiTestCaseAIRequest;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.mapper.ApiDefinitionBlobMapper;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.uid.NumGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiTestCaseAiControllerTests extends BaseTest {
    private static final String BASE_PATH = "/api/case/ai/";
    private static final String CHAT = "chat";

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
        apiTestCaseAIRequest.setConversationId(UUID.randomUUID().toString());
        this.requestPost(CHAT, apiTestCaseAIRequest);

        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ, CHAT, apiTestCaseAIRequest);
    }

}