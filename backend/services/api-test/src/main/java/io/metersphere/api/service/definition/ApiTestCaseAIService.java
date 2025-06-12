package io.metersphere.api.service.definition;

import io.metersphere.api.constants.ApiCaseAiPromptConstants;
import io.metersphere.api.constants.ApiDefinitionStatus;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.ApiCaseAIConfigDTO;
import io.metersphere.api.dto.ApiCaseAiResponse;
import io.metersphere.api.dto.ApiCaseAIRenderConfig;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.body.Body;
import io.metersphere.api.dto.request.http.body.FormDataBody;
import io.metersphere.api.dto.request.http.body.WWWFormBody;
import io.metersphere.api.mapper.ApiDefinitionBlobMapper;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.api.mapper.ApiTestCaseBlobMapper;
import io.metersphere.api.mapper.ApiTestCaseMapper;
import io.metersphere.api.utils.ApiCasePromptTemplateCache;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.KeyValueEnableParam;
import io.metersphere.project.dto.environment.auth.HTTPAuthConfig;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.EnumValidator;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.constants.AIConfigConstants;
import io.metersphere.system.domain.AiUserPromptConfig;
import io.metersphere.system.domain.AiUserPromptConfigExample;
import io.metersphere.system.dto.request.ai.AIChatOption;
import io.metersphere.system.dto.request.ai.AiModelSourceDTO;
import io.metersphere.system.mapper.AiUserPromptConfigMapper;
import io.metersphere.system.service.AiChatBaseService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiTestCaseAIService {

    @Resource
    private ApiDefinitionBlobMapper apiDefinitionBlobMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    AiChatBaseService aiChatBaseService;
    @Resource
    ApiCasePromptTemplateCache apiCasePromptTemplateCache;
    @Resource
    private AiUserPromptConfigMapper aiUserPromptConfigMapper;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ApiTestCaseBlobMapper apiTestCaseBlobMapper;
    @Resource
    private ApiDefinitionService apiDefinitionService;

    public String generateApiTestCase(ApiTestCaseAIRequest request, AiModelSourceDTO module, String userId) {
        ApiDefinitionBlob blob = apiDefinitionBlobMapper.selectByPrimaryKey(request.getApiDefinitionId());
        AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(new String(blob.getRequest()), AbstractMsTestElement.class);
        if (!(msTestElement instanceof MsHTTPElement)) {
            throw new MSException("仅支持HTTP协议的用例生成");
        }
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(request.getApiDefinitionId());

        // 模板变量
        ApiCaseAIRenderConfig renderConfig = getApiCaseAIRenderConfig(userId);
        renderConfig.setApiName(apiDefinition.getName());

        setHTTPElementRenderConfig((MsHTTPElement) msTestElement, renderConfig);

        if (blob.getResponse() != null) {
            List<HttpResponse> httpResponses = ApiDataUtils.parseArray(new String(blob.getResponse()), HttpResponse.class);
            if (CollectionUtils.isNotEmpty(httpResponses)) {
                // 精简 httpResponses 对象
                setHttpResponsesRenderConfig(httpResponses, renderConfig);
            }
        }

        renderConfig.setUserMessage(request.getPrompt());
        String prompt = apiCasePromptTemplateCache.getTemplate(renderConfig).replace("\\#", "#");

        AIChatOption aiChatOption = AIChatOption.builder()
                .conversationId(request.getConversationId())
                .module(module)
                .prompt(prompt)
                .build();

        return aiChatBaseService.chatWithMemory(aiChatOption)
                .content();
    }

    private ApiCaseAIRenderConfig getApiCaseAIRenderConfig(String userId) {
        ApiCaseAIRenderConfig renderConfig = new ApiCaseAIRenderConfig();
        ApiCaseAIConfigDTO apiAIConfig = getApiAIConfig(userId);
        renderConfig.setPreScript(apiAIConfig.getPreScript());
        renderConfig.setPostScript(apiAIConfig.getPostScript());
        renderConfig.setAsserts(apiAIConfig.getAssertion());
        return renderConfig;
    }

    private HttpResponse setHttpResponsesRenderConfig(List<HttpResponse> httpResponses, ApiCaseAIRenderConfig renderConfig) {
        httpResponses = httpResponses.stream()
                .filter(httpResponse -> httpResponse.getBody() != null && StringUtils.isNotBlank(httpResponse.getBody().getBodyType()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(httpResponses)) {
            return null;
        }
        HttpResponse httpResponse = httpResponses.getFirst();
        // 处理响应体
        setResponseBodyRenderConfig(httpResponse.getBody(), renderConfig);
        // 处理响应头
        httpResponse.setHeaders(filterEnableAndValidKVs(httpResponse.getHeaders()));
        renderConfig.setResponse(httpResponse);
        return httpResponse;
    }

    /**
     * 设置请求的渲染配置
     *
     * @param httpElement
     * @return
     */
    private MsHTTPElement setHTTPElementRenderConfig(MsHTTPElement httpElement, ApiCaseAIRenderConfig renderConfig) {
        // 处理请求体
        setBodyRenderConfig(httpElement.getBody(), renderConfig);

        // 处理请求头、rest参数和query参数
        httpElement.setHeaders(filterEnableAndValidKVs(httpElement.getHeaders()));
        httpElement.setRest(filterEnableAndValidKVs(httpElement.getRest()));
        httpElement.setQuery(filterEnableAndValidKVs(httpElement.getQuery()));

        ApiAiCaseDTO apiDefinition = BeanUtils.copyBean(new ApiAiCaseDTO(), httpElement);
        renderConfig.setApi(apiDefinition);
        return httpElement;
    }

    private void setBodyRenderConfig(Body body, ApiCaseAIRenderConfig renderConfig) {
        if (body == null || StringUtils.isBlank(body.getBodyType())) {
            renderConfig.setBody(false);
        } else {
            Body.BodyType bodyType = EnumValidator.validateEnum(Body.BodyType.class, body.getBodyType());
            switch (bodyType) {
                case WWW_FORM -> {
                    WWWFormBody wwwFormBody = body.getWwwFormBody();
                    if (wwwFormBody != null && CollectionUtils.isNotEmpty(wwwFormBody.getFormValues())) {
                        wwwFormBody.setFormValues(filterEnableAndValidKVs(wwwFormBody.getFormValues()));
                        if (CollectionUtils.isNotEmpty(wwwFormBody.getFormValues())) {
                            renderConfig.setWwwFormBody(true);
                        } else {
                            renderConfig.setBody(false);
                        }
                    } else {
                        renderConfig.setBody(false);
                    }
                }
                case FORM_DATA -> {
                    FormDataBody formDataBody = body.getFormDataBody();
                    if (formDataBody != null && CollectionUtils.isNotEmpty(formDataBody.getFormValues())) {
                        formDataBody.setFormValues(filterEnableAndValidKVs(formDataBody.getFormValues()));
                        if (CollectionUtils.isNotEmpty(formDataBody.getFormValues())) {
                            renderConfig.setFromDataBody(true);
                        } else {
                            renderConfig.setBody(false);
                        }
                    } else {
                        renderConfig.setBody(false);
                    }
                }
                case XML ->{
                    if (body.getXmlBody() != null && StringUtils.isNotBlank(body.getXmlBody().getValue())) {
                        renderConfig.setTextBodyValue(body.getXmlBody().getValue());
                        renderConfig.setXmlBody(true);
                    } else {
                        renderConfig.setBody(false);
                    }
                }
                case JSON -> {
                    if (body.getJsonBody() != null && body.getJsonBody().getJsonSchema() != null) {
                        String jsonValue = apiDefinitionService.jsonSchemaAutoGenerate(body.getJsonBody().getJsonSchema());
                        renderConfig.setTextBodyValue(jsonValue);
                        renderConfig.setJsonBody(true);
                    } else {
                        renderConfig.setBody(false);
                    }
                }
                case RAW -> {
                    renderConfig.setRawBody(true);
                    if (body.getRawBody() != null && StringUtils.isNotBlank(body.getRawBody().getValue())) {
                        renderConfig.setTextBodyValue(body.getRawBody().getValue());
                    } else {
                        renderConfig.setTextBodyValue("${参数值}");
                    }
                }
                default -> renderConfig.setBody(false);
            }
        }
    }

    private void setResponseBodyRenderConfig(ResponseBody body, ApiCaseAIRenderConfig renderConfig) {
        Body.BodyType bodyType = EnumValidator.validateEnum(Body.BodyType.class, body.getBodyType());
        switch (bodyType) {
            case XML -> {
                if (body.getXmlBody() != null && StringUtils.isNotBlank(body.getXmlBody().getValue())) {
                    renderConfig.setTextResponseBodyValue(body.getXmlBody().getValue());
                    renderConfig.setXmlBody(true);
                    renderConfig.setXpathAssert(true);
                } else {
                    renderConfig.setXpathAssert(false);
                }
            }
            case JSON -> {
                if (body.getJsonBody() != null && body.getJsonBody().getJsonSchema() != null) {
                    String jsonValue = apiDefinitionService.jsonSchemaAutoGenerate(body.getJsonBody().getJsonSchema());
                    if (!StringUtils.equalsAny(jsonValue, "{}", "{ }")) {
                        renderConfig.setJsonPathAssert(true);
                        renderConfig.setTextResponseBodyValue(jsonValue);
                    }
                } else {
                    renderConfig.setJsonPathAssert(false);
                }
            }
            default -> {}
        }
    }

    private <T extends KeyValueEnableParam> List<T> filterEnableAndValidKVs(List<T> kvs) {
        if (CollectionUtils.isEmpty(kvs)) {
            return null;
        }
        return kvs.stream()
                .filter(kv -> BooleanUtils.isTrue(kv.getEnable()) && kv.isValid())
                .collect(Collectors.toList());
    }

    public String chat(ApiTestCaseAIRequest request, String userId) {
        AiModelSourceDTO module = aiChatBaseService.getModule(request, userId);

        // 持久化原始提示词
        aiChatBaseService.saveUserConversationContent(request.getConversationId(), request.getPrompt());

        String prompt = "判断我下面这段话中是否需要生成用例，是的话返回 true，不是的话返回 false？文本如下：\n" + request.getPrompt();

        AIChatOption aiChatOption = AIChatOption.builder()
                .conversationId(request.getConversationId())
                .module(module)
                .prompt(prompt)
                .build();

        Boolean isGenerateCase = aiChatBaseService.chat(aiChatOption)
                .entity(Boolean.class);

        String assistantMessage;
        if (BooleanUtils.isTrue(isGenerateCase)) {
            // 判断对话是否是需要生成用例
            assistantMessage = generateApiTestCase(request, module, userId);
        } else {
            aiChatOption.setPrompt(request.getPrompt());
            assistantMessage = aiChatBaseService.chatWithMemory(aiChatOption)
                    .content();
        }

        // 持久化回答内容
        aiChatBaseService.saveAssistantConversationContent(request.getConversationId(), assistantMessage);

        return assistantMessage;
    }


    /**
     * 获取用户的AI提示词
     *
     * @param userId 用户ID
     */
    public ApiCaseAIConfigDTO getUserPrompt(String userId) {
        AiUserPromptConfigExample example = new AiUserPromptConfigExample();
        example.createCriteria().andUserIdEqualTo(userId).andTypeEqualTo(AIConfigConstants.AiPromptType.API_CASE.toString());
        List<AiUserPromptConfig> aiUserPromptConfigs = aiUserPromptConfigMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isEmpty(aiUserPromptConfigs)) {
            return new ApiCaseAIConfigDTO();
        }
        AiUserPromptConfig aiUserPromptConfig = aiUserPromptConfigs.getFirst();
        // 解析AI模型用例生成方法提示词
        String configStr = new String(aiUserPromptConfig.getConfig(), StandardCharsets.UTF_8);
        return JSON.parseObject(configStr, ApiCaseAIConfigDTO.class);
    }

    /**
     * 保存用户的AI提示词
     *
     * @param userId    用户ID
     * @param promptDTO 包含AI模型用例生成方法提示词和生成用例的提示语
     */
    public void saveUserPrompt(String userId, ApiCaseAIConfigDTO promptDTO) {
        AiUserPromptConfig aiUserPromptConfig = getAiUserPromptConfig(userId);
        String configStr = JSON.toJSONString(promptDTO);
        aiUserPromptConfig.setConfig(configStr.getBytes(StandardCharsets.UTF_8));
        if (aiUserPromptConfig.getId() == null) {
            aiUserPromptConfig.setId(IDGenerator.nextStr());
            aiUserPromptConfigMapper.insert(aiUserPromptConfig);
        } else {
            aiUserPromptConfigMapper.updateByPrimaryKeyWithBLOBs(aiUserPromptConfig);
        }
    }

    private ApiCaseAIConfigDTO getApiAIConfig(String userId) {
        AiUserPromptConfig aiUserPromptConfig = getAiUserPromptConfig(userId);
        if (aiUserPromptConfig.getConfig() != null) {
            String configStr = new String(aiUserPromptConfig.getConfig(), StandardCharsets.UTF_8);
            return JSON.parseObject(configStr, ApiCaseAIConfigDTO.class);
        } else {
            return new ApiCaseAIConfigDTO();
        }
    }

    private AiUserPromptConfig getAiUserPromptConfig(String userId) {
        AiUserPromptConfigExample example = new AiUserPromptConfigExample();
        example.createCriteria().andUserIdEqualTo(userId).andTypeEqualTo(AIConfigConstants.AiPromptType.API_CASE.toString());
        List<AiUserPromptConfig> aiUserPromptConfigs = aiUserPromptConfigMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(aiUserPromptConfigs)) {
            AiUserPromptConfig aiUserPromptConfig = new AiUserPromptConfig();
            aiUserPromptConfig.setUserId(userId);
            aiUserPromptConfig.setType(AIConfigConstants.AiPromptType.API_CASE.toString());
            return aiUserPromptConfig;
        } else {
            return aiUserPromptConfigs.getFirst();
        }
    }

    public ApiTestCaseDTO transformToDTO(ApiTestCaseAIRequest request, String userId) {
        AiModelSourceDTO module = aiChatBaseService.getModule(request, userId);
        String prompt = ApiCaseAiPromptConstants.AI_CASE_TRANSFORM_MODULE_PROMPT + request.getPrompt();
        AIChatOption aiChatOption = AIChatOption.builder()
                .module(module)
                .prompt(prompt)
                .build();
        ApiTestCaseAiDTO entity = aiChatBaseService.chat(aiChatOption)
                .entity(ApiTestCaseAiDTO.class);
        ApiDefinition apiDefinition = apiTestCaseService.getApiDefinition(request.getApiDefinitionId());
        ApiTestCaseDTO apiTestCaseDTO = caseFormat(entity, apiDefinition);
        return apiTestCaseDTO;
    }

    private ApiTestCaseDTO caseFormat(ApiTestCaseAiDTO entity, ApiDefinition apiDefinition) {
        entity.getMsHTTPElement().setMethod(apiDefinition.getMethod());
        if (StringUtils.isBlank(entity.getMsHTTPElement().getAuthConfig().getAuthType())) {
            entity.getMsHTTPElement().getAuthConfig().setAuthType(HTTPAuthConfig.HTTPAuthType.NONE.name());
        }
        AbstractMsTestElement msTestElement = entity.getMsHTTPElement();
        AbstractMsTestElement processorConfig = entity.getProcessorConfig();
        LinkedList<AbstractMsTestElement> children = new LinkedList<>();
        children.add(processorConfig);
        msTestElement.setChildren(children);
        ApiTestCaseDTO apiTestCaseDTO = new ApiTestCaseDTO();
        apiTestCaseDTO.setRequest(msTestElement);
        return apiTestCaseDTO;
    }


    /**
     * 批量保存AI用例
     *
     * @param request
     * @param userId
     * @return
     */
    public ApiCaseAiResponse batchSave(ApiTestCaseAiAddRequest request, String userId) {
        AiModelSourceDTO module = aiChatBaseService.getModule(request, userId);
        String prompt = ApiCaseAiPromptConstants.AI_CASE_TRANSFORM_MODULE_PROMPT + request.getPrompt();
        AIChatOption aiChatOption = AIChatOption.builder()
                .module(module)
                .prompt(prompt)
                .build();
        List<ApiTestCaseAiDTO> aiCaseList = aiChatBaseService.chat(aiChatOption)
                .entity(new ParameterizedTypeReference<>() {
                });
        return saveCaseModule(aiCaseList, request, userId);
    }


    private ApiCaseAiResponse saveCaseModule(List<ApiTestCaseAiDTO> aiCaseList, ApiTestCaseAiAddRequest request, String userId) {
        ApiCaseAiResponse response = new ApiCaseAiResponse();
        ApiDefinition apiDefinition = apiTestCaseService.getApiDefinition(request.getApiDefinitionId());
        aiCaseList.forEach(aiCase -> {
            ApiTestCaseDTO apiTestCaseDTO = caseFormat(aiCase, apiDefinition);
            apiTestCaseDTO.setMethod(apiDefinition.getMethod());
            ApiTestCase testCase = new ApiTestCase();
            testCase.setId(IDGenerator.nextStr());
            testCase.setProjectId(request.getProjectId());
            testCase.setApiDefinitionId(request.getApiDefinitionId());
            testCase.setName(aiCase.getMsHTTPElement().getName());
            if (!checkCaseNameExist(testCase, response)) {
                testCase.setPriority("P0");
                testCase.setStatus(ApiDefinitionStatus.PROCESSING.name());
                testCase.setPos(apiTestCaseService.getNextOrder(request.getProjectId()));
                testCase.setNum(NumGenerator.nextNum(request.getProjectId() + "_" + apiDefinition.getNum(), ApplicationNumScope.API_TEST_CASE));
                testCase.setVersionId(apiDefinition.getVersionId());
                testCase.setCreateUser(userId);
                testCase.setUpdateUser(userId);
                testCase.setCreateTime(System.currentTimeMillis());
                testCase.setUpdateTime(System.currentTimeMillis());
                testCase.setAiCreate(true);
                apiTestCaseMapper.insertSelective(testCase);
                response.incrementSuccessCount();

                ApiTestCaseBlob caseBlob = new ApiTestCaseBlob();
                caseBlob.setId(testCase.getId());
                caseBlob.setRequest(apiTestCaseService.getMsTestElementStr(apiTestCaseDTO.getRequest()).getBytes());
                apiTestCaseBlobMapper.insert(caseBlob);
            }

        });

        return response;
    }


    public boolean checkCaseNameExist(ApiTestCase apiTestCase, ApiCaseAiResponse response) {
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andProjectIdEqualTo(apiTestCase.getProjectId())
                .andApiDefinitionIdEqualTo(apiTestCase.getApiDefinitionId())
                .andNameEqualTo(apiTestCase.getName()).andIdNotEqualTo(apiTestCase.getId()).andDeletedEqualTo(false);
        List<ApiTestCase> apiTestCases = apiTestCaseMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(apiTestCases)) {
            response.setErrorDetail(Translator.get("api_test_case_name_exist"));
            response.incrementErrCount();
            return true;
        }
        return false;
    }
}
