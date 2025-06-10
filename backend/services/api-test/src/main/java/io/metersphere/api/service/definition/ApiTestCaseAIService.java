package io.metersphere.api.service.definition;

import io.metersphere.api.constants.ApiCaseAiPromptConstants;
import io.metersphere.api.constants.ApiDefinitionStatus;
import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiDefinitionBlob;
import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.domain.ApiTestCaseBlob;
import io.metersphere.api.dto.ApiCaseAIConfigDTO;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.body.Body;
import io.metersphere.api.dto.request.http.body.FormDataBody;
import io.metersphere.api.dto.request.http.body.WWWFormBody;
import io.metersphere.api.mapper.ApiDefinitionBlobMapper;
import io.metersphere.api.mapper.ApiTestCaseBlobMapper;
import io.metersphere.api.mapper.ApiTestCaseMapper;
import io.metersphere.api.utils.ApiCasePromptTemplateCache;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.KeyValueEnableParam;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.EnumValidator;
import io.metersphere.sdk.util.JSON;
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

    public String generateApiTestCase(ApiTestCaseAIRequest request, AiModelSourceDTO module, String userId) {
        ApiDefinitionBlob blob = apiDefinitionBlobMapper.selectByPrimaryKey(request.getApiDefinitionId());
        AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(new String(blob.getRequest()), AbstractMsTestElement.class);
        if (!(msTestElement instanceof MsHTTPElement)) {
            throw new MSException("仅支持HTTP协议的用例生成");
        }
        MsHTTPElement httpElement = (MsHTTPElement) msTestElement;
        httpElement = pruneHTTPElement(httpElement);

        String prompt = "# 接口定义:\n" + JSON.toJSONStringWithoutNull(BeanUtils.copyBean(new ApiAiCaseDTO(), httpElement));

        if (blob.getResponse() != null) {
            List<HttpResponse> httpResponses = ApiDataUtils.parseArray(new String(blob.getResponse()), HttpResponse.class);
            if (CollectionUtils.isNotEmpty(httpResponses)) {
                httpResponses = pruneHttpResponses(httpResponses);
                prompt += "\n# 接口响应示例:\n" + JSON.toJSONStringWithoutNull(httpResponses);
            }
        }

        ApiCaseAIConfigDTO apiAIConfig = getApiAIConfig(userId);
        prompt += "\n#用例生成配置:\n" + JSON.toJSONStringWithoutNull(apiAIConfig);
        prompt += "\n#需求描述:\n" + request.getPrompt();

        request.setPrompt(prompt);

        AIChatOption aiChatOption = AIChatOption.builder()
                .conversationId(request.getConversationId())
                .module(module)
                .prompt(prompt)
                .system(apiCasePromptTemplateCache.getTemplate())
                .build();

        return aiChatBaseService.chatWithMemory(aiChatOption)
                .content();
    }

    private List<HttpResponse> pruneHttpResponses(List<HttpResponse> httpResponses) {
        for (HttpResponse httpResponse : httpResponses) {
            // 处理响应体
            httpResponse.setBody(pruneResponseBody(httpResponse.getBody()));
            // 处理响应头
            httpResponse.setHeaders(filterEnableAndValidKVs(httpResponse.getHeaders()));
        }
        return httpResponses;
    }

    /**
     * 精简 MsHTTPElement 对象，去除无效或不必要的字段
     * 减少token消耗同时减少不必要信息对AI模型的干扰
     * @param httpElement
     * @return
     */
    private MsHTTPElement pruneHTTPElement(MsHTTPElement httpElement) {
        // 处理请求体
        httpElement.setBody(pruneBody(httpElement.getBody()));

        // 处理请求头、rest参数和query参数
        httpElement.setHeaders(filterEnableAndValidKVs(httpElement.getHeaders()));
        httpElement.setRest(filterEnableAndValidKVs(httpElement.getRest()));
        httpElement.setQuery(filterEnableAndValidKVs(httpElement.getQuery()));
        return httpElement;
    }

    private Body pruneBody(Body body) {
        if (body == null || StringUtils.isBlank(body.getBodyType())) {
            return null;
        } else {
            Body.BodyType bodyType = EnumValidator.validateEnum(Body.BodyType.class, body.getBodyType());
            Body tidyBody = new Body();
            tidyBody.setBodyType(bodyType.name());
            switch (bodyType) {
                case WWW_FORM -> {
                    WWWFormBody wwwFormBody = body.getWwwFormBody();
                    if (wwwFormBody != null) {
                        wwwFormBody.setFormValues(filterEnableAndValidKVs(wwwFormBody.getFormValues()));
                    }
                    tidyBody.setWwwFormBody(wwwFormBody);
                }
                case FORM_DATA -> {
                    FormDataBody formDataBody = body.getFormDataBody();
                    if (formDataBody != null) {
                        formDataBody.setFormValues(filterEnableAndValidKVs(formDataBody.getFormValues()));
                        if (formDataBody.getFormValues() != null) {
                            formDataBody.getFormValues().forEach(kv -> kv.setFiles(null));
                        }
                    }
                    tidyBody.setFormDataBody(formDataBody);
                }
                case XML ->
                        tidyBody.setXmlBody(body.getXmlBody());
                case JSON -> {
                    tidyBody.setJsonBody(body.getJsonBody());
                    tidyBody.getJsonBody().setJsonValue(null);
                    tidyBody.getJsonBody().setEnableJsonSchema(null);
                }
                case RAW ->
                        tidyBody.setRawBody(body.getRawBody());
                case NONE ->
                        tidyBody.setNoneBody(body.getNoneBody());
                default -> {
                    return null;
                }
            }
            return tidyBody;
        }
    }

    private ResponseBody pruneResponseBody(ResponseBody body) {
        if (body == null || StringUtils.isBlank(body.getBodyType())) {
            return null;
        } else {
            Body.BodyType bodyType = EnumValidator.validateEnum(Body.BodyType.class, body.getBodyType());
            ResponseBody tidyBody = new ResponseBody();
            tidyBody.setBodyType(bodyType.name());
            tidyBody.setBinaryBody(null);
            switch (bodyType) {
                case XML ->
                        tidyBody.setXmlBody(body.getXmlBody());
                case JSON -> {
                    tidyBody.setJsonBody(body.getJsonBody());
                    tidyBody.getJsonBody().setJsonValue(null);
                    tidyBody.getJsonBody().setEnableJsonSchema(null);
                }
                case RAW -> tidyBody.setRawBody(body.getRawBody());
                default -> {
                    return null;
                }
            }
            return tidyBody;
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
    public List<ApiTestCase> batchSave(ApiTestCaseAiAddRequest request, String userId) {
        AiModelSourceDTO module = aiChatBaseService.getModule(request, userId);
        String prompt = ApiCaseAiPromptConstants.AI_CASE_TRANSFORM_MODULE_PROMPT + request.getPrompt();
        AIChatOption aiChatOption = AIChatOption.builder()
                .module(module)
                .prompt(prompt)
                .build();
        List<ApiTestCaseAiDTO> aiCaseList = aiChatBaseService.chat(aiChatOption)
                .entity(new ParameterizedTypeReference<>() {
                });
        List<ApiTestCase> testCase = saveCaseModule(aiCaseList, request, userId);
        return testCase;
    }


    private List<ApiTestCase> saveCaseModule(List<ApiTestCaseAiDTO> aiCaseList, ApiTestCaseAiAddRequest request, String userId) {
        List<ApiTestCase> testCaseList = new LinkedList<>();
        ApiDefinition apiDefinition = apiTestCaseService.getApiDefinition(request.getApiDefinitionId());
        aiCaseList.forEach(aiCase -> {
            ApiTestCaseDTO apiTestCaseDTO = caseFormat(aiCase, apiDefinition);
            apiTestCaseDTO.setMethod(apiDefinition.getMethod());
            ApiTestCase testCase = new ApiTestCase();
            testCase.setId(IDGenerator.nextStr());
            testCase.setNum(NumGenerator.nextNum(request.getProjectId() + "_" + apiDefinition.getNum(), ApplicationNumScope.API_TEST_CASE));
            testCase.setApiDefinitionId(request.getApiDefinitionId());
            testCase.setName(aiCase.getMsHTTPElement().getName());
            testCase.setPriority("P0");
            testCase.setStatus(ApiDefinitionStatus.PROCESSING.name());
            testCase.setPos(apiTestCaseService.getNextOrder(request.getProjectId()));
            testCase.setProjectId(request.getProjectId());
            apiTestCaseService.checkNameExist(testCase);
            testCase.setVersionId(apiDefinition.getVersionId());
            testCase.setCreateUser(userId);
            testCase.setUpdateUser(userId);
            testCase.setCreateTime(System.currentTimeMillis());
            testCase.setUpdateTime(System.currentTimeMillis());
            testCase.setAiCreate(true);
            apiTestCaseMapper.insertSelective(testCase);
            testCaseList.add(testCase);


            ApiTestCaseBlob caseBlob = new ApiTestCaseBlob();
            caseBlob.setId(testCase.getId());
            caseBlob.setRequest(apiTestCaseService.getMsTestElementStr(apiTestCaseDTO.getRequest()).getBytes());
            apiTestCaseBlobMapper.insert(caseBlob);

        });

        return testCaseList;
    }
}
