package io.metersphere.api.service.definition;

import io.metersphere.ai.engine.utils.TextCleaner;
import io.metersphere.api.constants.ApiDefinitionStatus;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.ApiCaseAIConfigDTO;
import io.metersphere.api.dto.ApiCaseAIRenderConfig;
import io.metersphere.api.dto.ApiCaseAiResponse;
import io.metersphere.api.dto.definition.ApiAiCaseDTO;
import io.metersphere.api.dto.definition.ApiCaseAiTransformDTO;
import io.metersphere.api.dto.definition.ApiTestCaseAIRequest;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.body.Body;
import io.metersphere.api.dto.request.http.body.FormDataBody;
import io.metersphere.api.dto.request.http.body.WWWFormBody;
import io.metersphere.api.mapper.ApiDefinitionBlobMapper;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.api.mapper.ApiTestCaseBlobMapper;
import io.metersphere.api.mapper.ApiTestCaseMapper;
import io.metersphere.api.service.ApiCommonService;
import io.metersphere.api.utils.ApiCasePromptTemplateCache;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.api.utils.ApiTestCaseDTOParser;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.KeyValueEnableParam;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    @Resource
    private ApiCommonService apiCommonService;

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

        renderConfig.setUserMessage(request.getPrompt());
        String prompt = apiCasePromptTemplateCache.getTemplate(renderConfig).replace("\\#", "#");

        AIChatOption aiChatOption = AIChatOption.builder()
                .conversationId(request.getConversationId())
                .module(module)
                .prompt(prompt)
                .build();

        String content = aiChatBaseService.chatWithMemory(aiChatOption)
                .content();

        return formatAiCase(content);
    }


    /**
     * 格式化AI生成的用例内容
     * @param content
     * @return
     */
    public static String formatAiCase(String content) {
        if (StringUtils.isBlank(content)) {
            return content;
        }
        // 保证生成内容不包含额外内容
        Pattern pattern = Pattern.compile("apiCaseStart(.*?)apiCaseEnd", Pattern.DOTALL);
        assert content != null;
        Matcher matcher = pattern.matcher(content);
        boolean found = false;
        while (matcher.find()) {
            if (!found) {
                content = StringUtils.EMPTY;
            }
            found = true;
            content += matcher.group(0).trim();
        }

        StringBuilder result = new StringBuilder();
        String[] lines = content.split("\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            // 处理标题格式
            line = TextCleaner.formMdTitle(line);
            result.append(line);
            // 最后一行不添加换行符
            if(i != lines.length - 1) {
                result.append("\n");
            }
        }
        return result.toString();
    }

    private ApiCaseAIRenderConfig getApiCaseAIRenderConfig(String userId) {
        ApiCaseAIConfigDTO apiAIConfig = getApiAIConfig(userId);
        ApiCaseAIRenderConfig renderConfig = BeanUtils.copyBean(new ApiCaseAIRenderConfig(), apiAIConfig);
        return renderConfig;
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
                            renderConfig.setFormDataBody(true);
                        } else {
                            renderConfig.setBody(false);
                        }
                    } else {
                        renderConfig.setBody(false);
                    }
                }
                case XML -> {
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

        String prompt = String.format("""
                判断：用户输入是否想要生成用例？
                
                用户输入：%s
                
                只返回单个布尔值：
                - 是 → true
                - 否 → false
                - 不要返回任何其他文字或解释
                """, request.getPrompt());

        AIChatOption aiChatOption = AIChatOption.builder()
                .conversationId(request.getConversationId())
                .module(module)
                .prompt(prompt)
                .build();

        boolean isGenerateCase = Optional.ofNullable(aiChatBaseService.chat(aiChatOption).content())
                .map(content -> StringUtils.containsIgnoreCase(content, "true"))
                .orElse(false);

        String assistantMessage = isGenerateCase
                ? generateApiTestCase(request, module, userId)  // 生成测试用例
                : aiChatBaseService.chatWithMemory(aiChatOption.withPrompt(request.getPrompt())).content();

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
        List<AiUserPromptConfig> aiUserPromptConfigs = aiUserPromptConfigMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isEmpty(aiUserPromptConfigs)) {
            AiUserPromptConfig aiUserPromptConfig = new AiUserPromptConfig();
            aiUserPromptConfig.setUserId(userId);
            aiUserPromptConfig.setType(AIConfigConstants.AiPromptType.API_CASE.toString());
            return aiUserPromptConfig;
        } else {
            return aiUserPromptConfigs.getFirst();
        }
    }

    public ApiTestCaseDTO transformToDTO(ApiCaseAiTransformDTO request) {
        ApiTestCaseDTO apiTestCaseDTO = ApiTestCaseDTOParser.parse(request.getApiDefinitionId(), request.getPrompt());
        return apiTestCaseDTO;
    }

    /**
     * 批量保存AI用例
     *
     * @param request
     * @param userId
     * @return
     */
    public ApiCaseAiResponse batchSave(ApiCaseAiTransformDTO request, String userId) {
        List<String> prompts = Arrays.stream(request.getPrompt().split("apiCaseEnd")).toList();
        ApiDefinition apiDefinition = apiTestCaseService.getApiDefinition(request.getApiDefinitionId());
        List<ApiTestCaseDTO> aiCaseList = new ArrayList<>();
        prompts.forEach(prompt -> {
            ApiTestCaseDTO apiTestCaseDTO = ApiTestCaseDTOParser.parse(request.getApiDefinitionId(), prompt);
            aiCaseList.add(apiTestCaseDTO);
        });
        return saveAiTestCase(aiCaseList, userId, apiDefinition);
    }


    private ApiCaseAiResponse saveAiTestCase(List<ApiTestCaseDTO> aiCaseList, String userId, ApiDefinition apiDefinition) {
        ApiCaseAiResponse response = new ApiCaseAiResponse();
        aiCaseList.forEach(aiCase -> {
            ApiTestCase testCase = new ApiTestCase();
            testCase.setId(IDGenerator.nextStr());
            testCase.setProjectId(apiDefinition.getProjectId());
            testCase.setApiDefinitionId(apiDefinition.getId());
            testCase.setName(aiCase.getName());
            if (!checkCaseNameExist(testCase, response)) {
                testCase.setPriority("P0");
                testCase.setStatus(ApiDefinitionStatus.PROCESSING.name());
                testCase.setPos(apiTestCaseService.getNextOrder(apiDefinition.getProjectId()));
                testCase.setNum(NumGenerator.nextNum(apiDefinition.getProjectId() + "_" + apiDefinition.getNum(), ApplicationNumScope.API_TEST_CASE));
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
                caseBlob.setRequest(apiTestCaseService.getMsTestElementStr(aiCase.getRequest()).getBytes());
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
