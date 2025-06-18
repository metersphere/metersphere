package io.metersphere.functional.service;

import io.metersphere.ai.engine.utils.TextCleaner;
import io.metersphere.functional.constants.CaseMdTitleConstants;
import io.metersphere.functional.constants.FunctionalCaseReviewStatus;
import io.metersphere.functional.constants.FunctionalCaseTypeConstants;
import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseBlob;
import io.metersphere.functional.domain.FunctionalCaseCustomField;
import io.metersphere.functional.dto.FunctionalCaseAIConfigDTO;
import io.metersphere.functional.dto.FunctionalCaseAIDesignConfigDTO;
import io.metersphere.functional.dto.FunctionalCaseAITemplateConfigDTO;
import io.metersphere.functional.dto.FunctionalCaseAiDTO;
import io.metersphere.functional.mapper.ExtFunctionalCaseMapper;
import io.metersphere.functional.mapper.FunctionalCaseBlobMapper;
import io.metersphere.functional.mapper.FunctionalCaseCustomFieldMapper;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.functional.request.FunctionalCaseAIChatRequest;
import io.metersphere.functional.utils.MdUtil;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.project.service.ProjectTemplateService;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.CustomFieldType;
import io.metersphere.sdk.constants.ExecStatus;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.constants.AIConfigConstants;
import io.metersphere.system.domain.AiUserPromptConfig;
import io.metersphere.system.domain.AiUserPromptConfigExample;
import io.metersphere.system.dto.request.ai.AIChatOption;
import io.metersphere.system.dto.request.ai.AIChatRequest;
import io.metersphere.system.dto.request.ai.AiModelSourceDTO;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import io.metersphere.system.dto.sdk.TemplateDTO;
import io.metersphere.system.mapper.AiUserPromptConfigMapper;
import io.metersphere.system.service.AiChatBaseService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.ServiceUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jetbrains.annotations.NotNull;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class FunctionalCaseAIService {

    @Resource
    private AiUserPromptConfigMapper aiUserPromptConfigMapper;
    @Resource
    private AiChatBaseService aiChatBaseService;

    @Value("classpath:/prompts/generate_step.st")
    private org.springframework.core.io.Resource stepPrompt;
    @Value("classpath:/prompts/generate_text.st")
    private org.springframework.core.io.Resource textPrompt;

    @Resource
    private ExtFunctionalCaseMapper extFunctionalCaseMapper;

    @Resource
    private ExtBaseProjectVersionMapper extBaseProjectVersionMapper;

    @Resource
    private ProjectTemplateService projectTemplateService;

    @Resource
    SqlSessionFactory sqlSessionFactory;

    /**
     * 获取用户的AI提示词
     *
     * @param userId 用户ID
     * @return FunctionalCaseAIPromptDTO 包含AI模型用例生成方法提示词和生成用例的提示语
     */
    public FunctionalCaseAIConfigDTO getUserPrompt(String userId) {
        AiUserPromptConfigExample example = new AiUserPromptConfigExample();
        example.createCriteria().andUserIdEqualTo(userId).andTypeEqualTo(AIConfigConstants.AiPromptType.FUNCTIONAL_CASE.toString());
        List<AiUserPromptConfig> aiUserPromptConfigs = aiUserPromptConfigMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isEmpty(aiUserPromptConfigs)) {
            // 如果没有配置，则返回一个空的配置对象
            return getFunctionalCaseAIConfigDTO();
        }
        AiUserPromptConfig aiUserPromptConfig = aiUserPromptConfigs.getFirst();
        // 解析AI模型用例生成方法提示词
        String configStr = new String(aiUserPromptConfig.getConfig(), StandardCharsets.UTF_8);
        return JSON.parseObject(configStr, FunctionalCaseAIConfigDTO.class);
    }


    /**
     * 获取默认的AI提示词配置
     */
    @NotNull
    private static FunctionalCaseAIConfigDTO getFunctionalCaseAIConfigDTO() {
        FunctionalCaseAIDesignConfigDTO aiDesignConfigDTO = new FunctionalCaseAIDesignConfigDTO();
        FunctionalCaseAITemplateConfigDTO templateConfigDTO = new FunctionalCaseAITemplateConfigDTO();
        FunctionalCaseAIConfigDTO functionalCaseAIConfigDTO = new FunctionalCaseAIConfigDTO();
        functionalCaseAIConfigDTO.setDesignConfig(aiDesignConfigDTO);
        functionalCaseAIConfigDTO.setTemplateConfig(templateConfigDTO);
        return functionalCaseAIConfigDTO;
    }

    /**
     * 保存用户的AI提示词
     *
     * @param userId    用户ID
     * @param promptDTO 包含AI模型用例生成方法提示词和生成用例的提示语
     */
    public void saveUserPrompt(String userId, FunctionalCaseAIConfigDTO promptDTO) {
        AiUserPromptConfigExample example = new AiUserPromptConfigExample();
        example.createCriteria().andUserIdEqualTo(userId).andTypeEqualTo(AIConfigConstants.AiPromptType.FUNCTIONAL_CASE.toString());
        List<AiUserPromptConfig> aiUserPromptConfigs = aiUserPromptConfigMapper.selectByExample(example);
        AiUserPromptConfig aiUserPromptConfig;
        if (CollectionUtils.isEmpty(aiUserPromptConfigs)) {
            aiUserPromptConfig = new AiUserPromptConfig();
            aiUserPromptConfig.setUserId(userId);
            aiUserPromptConfig.setType(AIConfigConstants.AiPromptType.FUNCTIONAL_CASE.toString());
        } else {
            aiUserPromptConfig = aiUserPromptConfigs.getFirst();
        }
        String configStr = JSON.toJSONString(promptDTO);
        aiUserPromptConfig.setConfig(configStr.getBytes(StandardCharsets.UTF_8));
        if (aiUserPromptConfig.getId() == null) {
            aiUserPromptConfig.setId(IDGenerator.nextStr());
            aiUserPromptConfigMapper.insert(aiUserPromptConfig);
        } else {
            aiUserPromptConfigMapper.updateByPrimaryKeyWithBLOBs(aiUserPromptConfig);
        }
    }

    public FunctionalCaseAiDTO transformToDTO(AIChatRequest request) {
        return MdUtil.transformToCaseDTO(request.getPrompt());
    }

    /**
     * AI对话
     *
     * @param request 请求参数
     * @param userId  用户ID
     * @return 返回对话内容
     */
    public String chat(AIChatRequest request, String userId) {
        AiModelSourceDTO module = aiChatBaseService.getModule(request, userId);
        aiChatBaseService.saveUserConversationContent(request.getConversationId(), request.getPrompt());
        String prompt = String.format("""
                作为测试用例生成助手，判断：用户是否想要生成测试用例？
                
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

        LogUtils.info("AI判断是否生成测试用例: {}", isGenerateCase);

        Consumer<AIChatOption> configureTestGeneration = option -> {
            option.setPrompt(request.getPrompt() + renderSystemPromptTpl(userId));
        };

        Consumer<AIChatOption> configureNormalAssistant = option -> option.setPrompt(request.getPrompt());

        // 根据条件选择并执行相应的配置行为
        (isGenerateCase ? configureTestGeneration : configureNormalAssistant).accept(aiChatOption);

        String content = TextCleaner.cleanMdTitle(aiChatBaseService.chatWithMemory(aiChatOption).content());
        aiChatBaseService.saveAssistantConversationContent(request.getConversationId(), content);
        return content;
    }

    /**
     * 解析用户提示词模板
     *
     * @param userId 用户ID
     * @return 提示词
     */
    private String renderSystemPromptTpl(String userId) {
        Map<String, Object> variables = new HashMap<>();
        FunctionalCaseAIConfigDTO userPrompt = getUserPrompt(userId);
        FunctionalCaseAITemplateConfigDTO templateConfig = userPrompt.getTemplateConfig();
        PromptTemplate promptTemplate;
        if (StringUtils.equals(templateConfig.getCaseEditType(), FunctionalCaseTypeConstants.CaseEditType.STEP.name())) {
            promptTemplate = new PromptTemplate(stepPrompt);
        } else {
            promptTemplate = new PromptTemplate(textPrompt);
        }
        if (templateConfig.getPreCondition()) {
            variables.put("preCondition", "### " + CaseMdTitleConstants.PRE_REQUISITE);
        } else {
            variables.put("preCondition", "");
        }
        if (templateConfig.getRemark()) {
            variables.put("remark", "### " + CaseMdTitleConstants.DESCRIPTION);
        } else {
            variables.put("remark", "");
        }

        FunctionalCaseAIDesignConfigDTO designConfig = userPrompt.getDesignConfig();
        List<String> designs = new ArrayList<>();
        if (designConfig != null) {
            for (Field field : FunctionalCaseAIDesignConfigDTO.class.getDeclaredFields()) {
                if (field.isAnnotationPresent(Schema.class) && !StringUtils.equalsAny(field.getName(), "normal", "abnormal")) {
                    field.setAccessible(true);
                    Schema annotation = field.getAnnotation(Schema.class);
                    try {
                        Object value = field.get(designConfig);
                        if (Boolean.TRUE.equals(value)) {
                            designs.add("`" + annotation.description() + "`");
                        }
                    } catch (IllegalAccessException e) {
                        LogUtils.error(e.getMessage());
                    }
                }
            }
        }
        variables.put("designs", designs.isEmpty() ? "" : String.join(",", designs));
        List<String> scenes = new ArrayList<>();
        if (designConfig.getNormal()) {
            scenes.add("`正常`");
        }
        if (designConfig.getAbnormal()) {
            scenes.add("`异常`");
        }
        variables.put("scenes", scenes.isEmpty() ? "`正常`, `异常`" : String.join(",", scenes));
        if (StringUtils.isNotBlank(designConfig.getScenarioMethodDescription())) {
            variables.put("sceneTips", designConfig.getScenarioMethodDescription());
        } else {
            variables.put("sceneTips", "");
        }

        Message systemMessage = promptTemplate.createMessage(variables);
        return systemMessage.toString();
    }


    public void batchSave(FunctionalCaseAIChatRequest request, String userId) {
        List<FunctionalCaseAiDTO> caseList = MdUtil.batchTransformToCaseDTO(request.getPrompt());
        if (CollectionUtils.isEmpty(caseList)) {
            return;
        }
        saveFunctionalCaseList(request, userId, caseList);
    }

    private void saveFunctionalCaseList(FunctionalCaseAIChatRequest request, String userId, List<FunctionalCaseAiDTO> cases) {
        String projectId = request.getProjectId();
        String moduleId = request.getModuleId();
        String templateId = request.getTemplateId();
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        FunctionalCaseMapper caseMapper = sqlSession.getMapper(FunctionalCaseMapper.class);
        FunctionalCaseBlobMapper caseBlobMapper = sqlSession.getMapper(FunctionalCaseBlobMapper.class);
        FunctionalCaseCustomFieldMapper customFieldMapper = sqlSession.getMapper(FunctionalCaseCustomFieldMapper.class);
        Map<String, TemplateCustomFieldDTO> customFieldsMap = getStringTemplateCustomFieldDTOMap(templateId, projectId);
        Long nextPos = getNextOrder(projectId);
        long pos = nextPos + ((long) ServiceUtils.POS_STEP * cases.size());
        for (FunctionalCaseAiDTO aiCase : cases) {
            FunctionalCase functionalCase = new FunctionalCase();
            String id = IDGenerator.nextStr();
            functionalCase.setId(id);
            functionalCase.setNum(getNextNum(projectId));
            functionalCase.setModuleId(moduleId);
            functionalCase.setProjectId(projectId);
            functionalCase.setTemplateId(templateId);
            functionalCase.setName(aiCase.getName());
            functionalCase.setReviewStatus(FunctionalCaseReviewStatus.UN_REVIEWED.name());
            functionalCase.setCaseEditType(aiCase.getCaseEditType());
            functionalCase.setPos(pos);
            functionalCase.setTags(null);
            functionalCase.setVersionId(extBaseProjectVersionMapper.getDefaultVersion(projectId));
            functionalCase.setRefId(id);
            functionalCase.setLastExecuteResult(ExecStatus.PENDING.name());
            functionalCase.setDeleted(false);
            functionalCase.setAiCreate(true);
            functionalCase.setPublicCase(false);
            functionalCase.setLatest(true);
            functionalCase.setCreateUser(userId);
            functionalCase.setUpdateUser(userId);
            functionalCase.setCreateTime(System.currentTimeMillis());
            functionalCase.setUpdateTime(System.currentTimeMillis());
            caseMapper.insert(functionalCase);

            //附属表
            FunctionalCaseBlob functionalCaseBlob = new FunctionalCaseBlob();
            functionalCaseBlob.setId(id);
            if (StringUtils.equals(FunctionalCaseTypeConstants.CaseEditType.TEXT.name(), aiCase.getCaseEditType())) {
                functionalCaseBlob.setTextDescription(StringUtils.defaultIfBlank(aiCase.getTextDescription(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
            } else {
                functionalCaseBlob.setSteps(StringUtils.defaultIfBlank(aiCase.getSteps(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
            }
            functionalCaseBlob.setExpectedResult(StringUtils.defaultIfBlank(aiCase.getExpectedResult(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
            functionalCaseBlob.setPrerequisite(StringUtils.defaultIfBlank(aiCase.getPrerequisite(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
            functionalCaseBlob.setDescription(StringUtils.defaultIfBlank(aiCase.getDescription(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
            caseBlobMapper.insert(functionalCaseBlob);
            saveCustomField(userId, customFieldsMap, id, customFieldMapper);
            pos -= ServiceUtils.POS_STEP;
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);

    }

    @NotNull
    private Map<String, TemplateCustomFieldDTO> getStringTemplateCustomFieldDTOMap(String templateId, String projectId) {
        TemplateDTO templateDTO = projectTemplateService.getTemplateDTOById(templateId, projectId, TemplateScene.FUNCTIONAL.name());
        List<TemplateCustomFieldDTO> customFields = Optional.ofNullable(templateDTO.getCustomFields()).orElse(new ArrayList<>());
        return customFields.stream().collect(Collectors.toMap(TemplateCustomFieldDTO::getFieldName, i -> i));
    }

    private static void saveCustomField(String userId, Map<String, TemplateCustomFieldDTO> customFieldsMap, String id, FunctionalCaseCustomFieldMapper customFieldMapper) {
        customFieldsMap.forEach((k, v) -> {
            //用例等级如果没有默认值，则为P0
            if (StringUtils.equalsIgnoreCase(v.getInternalFieldKey(), "functional_priority") && (v.getDefaultValue() == null || StringUtils.isBlank(v.getDefaultValue().toString()))) {
                v.setDefaultValue("P0");
            }
            FunctionalCaseCustomField caseCustomField = new FunctionalCaseCustomField();
            caseCustomField.setCaseId(id);
            caseCustomField.setFieldId(v.getFieldId());

            if (StringUtils.equalsIgnoreCase(v.getType(), CustomFieldType.MEMBER.name()) && v.getDefaultValue() != null && v.getDefaultValue().toString().contains("CREATE_USER")) {
                caseCustomField.setValue(userId);
            } else if (StringUtils.equalsIgnoreCase(v.getType(), CustomFieldType.MULTIPLE_MEMBER.name()) && v.getDefaultValue() != null && v.getDefaultValue().toString().contains("CREATE_USER")) {
                caseCustomField.setValue(JSON.toJSONString(List.of(userId)));
            } else {
                caseCustomField.setValue(v.getDefaultValue() == null ? StringUtils.EMPTY : v.getDefaultValue().toString());
            }
            customFieldMapper.insertSelective(caseCustomField);
        });
    }

    public Long getNextOrder(String projectId) {
        Long pos = extFunctionalCaseMapper.getPos(projectId);
        return (pos == null ? 0 : pos) + ServiceUtils.POS_STEP;
    }

    public long getNextNum(String projectId) {
        return NumGenerator.nextNum(projectId, ApplicationNumScope.CASE_MANAGEMENT);
    }
}
