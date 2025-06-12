package io.metersphere.functional.service;

import io.metersphere.ai.engine.utils.TextCleaner;
import io.metersphere.functional.constants.FunctionalCaseReviewStatus;
import io.metersphere.functional.constants.FunctionalCaseTypeConstants;
import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseBlob;
import io.metersphere.functional.dto.*;
import io.metersphere.functional.mapper.ExtFunctionalCaseMapper;
import io.metersphere.functional.mapper.FunctionalCaseBlobMapper;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.functional.request.FunctionalCaseAIChatRequest;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.ExecStatus;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.constants.AIConfigConstants;
import io.metersphere.system.domain.AiUserPromptConfig;
import io.metersphere.system.domain.AiUserPromptConfigExample;
import io.metersphere.system.dto.request.ai.AIChatOption;
import io.metersphere.system.dto.request.ai.AIChatRequest;
import io.metersphere.system.dto.request.ai.AiModelSourceDTO;
import io.metersphere.system.mapper.AiUserPromptConfigMapper;
import io.metersphere.system.service.AiChatBaseService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.ServiceUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class FunctionalCaseAIService {

    @Resource
    private AiUserPromptConfigMapper aiUserPromptConfigMapper;
    @Resource
    private AiChatBaseService aiChatBaseService;

    @Value("classpath:/prompts/generate.st")
    private org.springframework.core.io.Resource generatePrompt;

    @Resource
    private FunctionalCaseMapper functionalCaseMapper;

    @Resource
    private FunctionalCaseBlobMapper functionalCaseBlobMapper;

    @Resource
    private ExtFunctionalCaseMapper extFunctionalCaseMapper;

    @Resource
    private ExtBaseProjectVersionMapper extBaseProjectVersionMapper;

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
     * @param userId 用户ID
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

    public FunctionalCaseAiDTO transformToDTO(AIChatRequest request, String userId) {
        String caseEditType = getCaseEditType(userId);
        AiModelSourceDTO module = aiChatBaseService.getModule(request, userId);
        String prompt = "请解析以下格式并转为一条java对象数据:\n" + request.getPrompt();
        AIChatOption aiChatOption = AIChatOption.builder()
                .module(module)
                .prompt(prompt)
                .build();
        //根据用户配置的编辑类型，返回不同的实体类
        FunctionalCaseAiDTO functionalCaseAiDTO = new FunctionalCaseAiDTO();
        if (StringUtils.equalsIgnoreCase(caseEditType, FunctionalCaseTypeConstants.CaseEditType.STEP.name())) {
            FunctionalCaseStepAiDTO entity = aiChatBaseService.chat(aiChatOption).entity(FunctionalCaseStepAiDTO.class);
            BeanUtils.copyBean(functionalCaseAiDTO,entity);
            functionalCaseAiDTO.setCaseEditType(FunctionalCaseTypeConstants.CaseEditType.STEP.name());
        } else {
            FunctionalCaseTextAiDTO entity = aiChatBaseService.chat(aiChatOption).entity(FunctionalCaseTextAiDTO.class);
            BeanUtils.copyBean(functionalCaseAiDTO,entity);
            functionalCaseAiDTO.setCaseEditType(FunctionalCaseTypeConstants.CaseEditType.TEXT.name());
        }
        return functionalCaseAiDTO;
    }

    /**
     * 获取用户配置的用例编辑类型
     * @param userId 用户ID
     * @return 用例编辑类型，默认返回文本类型步骤描述
     */
    @NotNull
    private String getCaseEditType(String userId) {
        AiUserPromptConfigExample example = new AiUserPromptConfigExample();
        example.createCriteria().andUserIdEqualTo(userId).andTypeEqualTo(AIConfigConstants.AiPromptType.FUNCTIONAL_CASE.toString());
        List<AiUserPromptConfig> aiUserPromptConfigs = aiUserPromptConfigMapper.selectByExampleWithBLOBs(example);
        //如果用户没配置，默认返回文本类型步骤描述
        if (CollectionUtils.isEmpty(aiUserPromptConfigs)) {
            return FunctionalCaseTypeConstants.CaseEditType.TEXT.name();
        }else {
            AiUserPromptConfig first = aiUserPromptConfigs.getFirst();
            String configStr = new String(first.getConfig(), StandardCharsets.UTF_8);
            FunctionalCaseAIConfigDTO configDTO = JSON.parseObject(configStr, FunctionalCaseAIConfigDTO.class);
            FunctionalCaseAITemplateConfigDTO templateConfig = configDTO.getTemplateConfig();
            return templateConfig.getCaseEditType() != null ? templateConfig.getCaseEditType() : FunctionalCaseTypeConstants.CaseEditType.TEXT.name();
        }
    }

    /**
     * AI对话
     *
     * @param request 请求参数
     * @param userId 用户ID
     * @return 返回对话内容
     */
    public String chat(AIChatRequest request, String userId) {
        AiModelSourceDTO module = aiChatBaseService.getModule(request, userId);
        aiChatBaseService.saveUserConversationContent(request.getConversationId(), request.getPrompt());

        String prompt = "你是一个测试用例生成助手，请判断用户是否希望生成测试用例   用户输入是：\n" + request.getPrompt() + "\n 如果是，请返回true，否则返回false";
        AIChatOption aiChatOption = AIChatOption.builder()
                .conversationId(request.getConversationId())
                .module(module)
                .system("你是一个语义识别引擎，负责判断是否要生成测试用例")
                .prompt(prompt)
                .build();
        Boolean isGenerateCase = aiChatBaseService.chat(aiChatOption)
                .entity(Boolean.class);

        if (Boolean.TRUE.equals(isGenerateCase)) {
            SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(generatePrompt);
            Message systemMessage = systemPromptTemplate.createMessage();
            aiChatOption.setSystem(systemMessage.toString());
            aiChatOption.setPrompt(buildUserPromptTpl(userId, request.getPrompt()));
        } else {
            aiChatOption.setSystem("接下来不是测试生成任务，请作为AI助手来回答以下问题");
            aiChatOption.setPrompt(request.getPrompt());
        }
        String content = TextCleaner.cleanMdTitle(aiChatBaseService.chatWithMemory(aiChatOption).content());
        aiChatBaseService.saveAssistantConversationContent(request.getConversationId(), content);
        return content;
    }

    /**
     * 构建用户提示词模板
     * @param userId 用户ID
     * @param input 用户输入
     * @return 提示词
     */
    private String buildUserPromptTpl(String userId, String input) {
        FunctionalCaseAIConfigDTO userPrompt = getUserPrompt(userId);
        FunctionalCaseAITemplateConfigDTO templateConfig = userPrompt.getTemplateConfig();
        List<String> modules = new ArrayList<>();
        if (templateConfig != null) {
            for (Field field : FunctionalCaseAITemplateConfigDTO.class.getDeclaredFields()) {
                if (field.isAnnotationPresent(Schema.class) && !StringUtils.equalsAny(field.getName(), "caseName", "caseSteps", "expectedResult")) {
                    field.setAccessible(true);
                    Schema annotation = field.getAnnotation(Schema.class);
                    try {
                        Object value = field.get(templateConfig);
                        if (value instanceof String) {
                            if (StringUtils.equals("STEP", value.toString())) {
                                modules.add("步骤描述");
                            } else if (StringUtils.equals("TEXT", value.toString())) {
                                modules.add("文本描述");
                                modules.add("预期结果");
                            }
                        }
                        if (value instanceof Boolean && Boolean.TRUE.equals(value)) {
                            modules.add(annotation.description());
                        }
                    } catch (IllegalAccessException e) {
                        LogUtils.error(e.getMessage());
                    }
                }
            }
        } else {
            modules.addAll(Arrays.asList("用例名称", "前置条件", "文本描述", "预期结果", "备注"));
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
                            designs.add(annotation.description());
                        }
                    } catch (IllegalAccessException e) {
                        LogUtils.error(e.getMessage());
                    }
                }
            }
        } else {
            designs.addAll(Arrays.asList("等价类划分", "边界值分析", "决策表测试", "因果图法", "正交实验法", "场景法", "场景法描述"));
        }
        return String.format("%s，合理运用设计方法: %s，必须包含以下模块: %s", input, String.join(", ", designs), String.join(", ", modules));
    }


    public void batchSave(FunctionalCaseAIChatRequest request, String userId) {
        String caseEditType = getCaseEditType(userId);
        AiModelSourceDTO module = aiChatBaseService.getModule(request, userId);
        String prompt = "请解析以下格式并转为java数组对象:\n" + request.getPrompt();
        AIChatOption aiChatOption = AIChatOption.builder()
                .module(module)
                .prompt(prompt)
                .build();
        if (StringUtils.equalsIgnoreCase(caseEditType, FunctionalCaseTypeConstants.CaseEditType.STEP.name())) {
            List<FunctionalCaseStepAiDTO> caseStepAiDTOList  = aiChatBaseService.chat(aiChatOption).entity(new ParameterizedTypeReference<>() {});
            if (CollectionUtils.isEmpty(caseStepAiDTOList)) {
                return;
            }
            saveStepCaseList(request, userId, caseStepAiDTOList);
        } else {
            List<FunctionalCaseTextAiDTO> caseTextAiDTOList  = aiChatBaseService.chat(aiChatOption).entity(new ParameterizedTypeReference<>() {});
            if (CollectionUtils.isEmpty(caseTextAiDTOList)) {
                return;
            }
            saveTextCaseList(request, userId, caseTextAiDTOList);
        }

    }

    private void saveTextCaseList(FunctionalCaseAIChatRequest request, String userId, List<FunctionalCaseTextAiDTO> caseTextAiDTOList) {
        List<FunctionalCase> functionalCases = new ArrayList<>();
        List<FunctionalCaseBlob> functionalCaseBlobs = new ArrayList<>();
        for (FunctionalCaseTextAiDTO functionalCaseTextAiDTO : caseTextAiDTOList) {
            FunctionalCase functionalCase = new FunctionalCase();
            String id = IDGenerator.nextStr();
            functionalCase.setId(id);
            functionalCase.setNum(getNextNum(request.getProjectId()));
            functionalCase.setModuleId(request.getModuleId());
            functionalCase.setProjectId(request.getProjectId());
            functionalCase.setTemplateId(request.getTemplateId());
            functionalCase.setName(functionalCaseTextAiDTO.getName());
            functionalCase.setReviewStatus(FunctionalCaseReviewStatus.UN_REVIEWED.name());
            functionalCase.setCaseEditType(FunctionalCaseTypeConstants.CaseEditType.STEP.name());
            functionalCase.setPos(getNextOrder(request.getProjectId()));
            functionalCase.setTags(null);
            functionalCase.setVersionId(extBaseProjectVersionMapper.getDefaultVersion(request.getProjectId()));
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
            functionalCases.add(functionalCase);
            //附属表
            FunctionalCaseBlob functionalCaseBlob = new FunctionalCaseBlob();
            functionalCaseBlob.setId(id);
            functionalCaseBlob.setSteps(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8));
            functionalCaseBlob.setTextDescription(StringUtils.defaultIfBlank(functionalCaseTextAiDTO.getTextDescription(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
            functionalCaseBlob.setExpectedResult(StringUtils.defaultIfBlank(functionalCaseTextAiDTO.getExpectedResult(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
            functionalCaseBlob.setPrerequisite(StringUtils.defaultIfBlank(functionalCaseTextAiDTO.getPrerequisite(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
            functionalCaseBlob.setDescription(StringUtils.defaultIfBlank(functionalCaseTextAiDTO.getDescription(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
            functionalCaseBlobs.add(functionalCaseBlob);
        }
        functionalCaseMapper.batchInsert(functionalCases);
        functionalCaseBlobMapper.batchInsert(functionalCaseBlobs);
    }

    private void saveStepCaseList(FunctionalCaseAIChatRequest request, String userId, List<FunctionalCaseStepAiDTO> caseStepAiDTOList) {
        List<FunctionalCase> functionalCases = new ArrayList<>();
        List<FunctionalCaseBlob> functionalCaseBlobs = new ArrayList<>();
        for (FunctionalCaseStepAiDTO functionalCaseStepAiDTO : caseStepAiDTOList) {
            FunctionalCase functionalCase = new FunctionalCase();
            String id = IDGenerator.nextStr();
            functionalCase.setId(id);
            functionalCase.setNum(getNextNum(request.getProjectId()));
            functionalCase.setModuleId(request.getModuleId());
            functionalCase.setProjectId(request.getProjectId());
            functionalCase.setTemplateId(request.getTemplateId());
            functionalCase.setName(functionalCaseStepAiDTO.getName());
            functionalCase.setReviewStatus(FunctionalCaseReviewStatus.UN_REVIEWED.name());
            functionalCase.setCaseEditType(FunctionalCaseTypeConstants.CaseEditType.STEP.name());
            functionalCase.setPos(getNextOrder(request.getProjectId()));
            functionalCase.setTags(null);
            functionalCase.setVersionId(extBaseProjectVersionMapper.getDefaultVersion(request.getProjectId()));
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
            functionalCases.add(functionalCase);
            //附属表
            List<FunctionalCaseAIStep> stepDescription = functionalCaseStepAiDTO.getStepDescription();
            FunctionalCaseBlob functionalCaseBlob = new FunctionalCaseBlob();
            functionalCaseBlob.setId(id);
            functionalCaseBlob.setSteps(JSON.toJSONString(stepDescription).getBytes(StandardCharsets.UTF_8));
            functionalCaseBlob.setTextDescription(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8));
            functionalCaseBlob.setExpectedResult(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8));
            functionalCaseBlob.setPrerequisite(StringUtils.defaultIfBlank(functionalCaseStepAiDTO.getPrerequisite(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
            functionalCaseBlob.setDescription(StringUtils.defaultIfBlank(functionalCaseStepAiDTO.getDescription(), StringUtils.EMPTY).getBytes(StandardCharsets.UTF_8));
            functionalCaseBlobs.add(functionalCaseBlob);
        }
        functionalCaseMapper.batchInsert(functionalCases);
        functionalCaseBlobMapper.batchInsert(functionalCaseBlobs);
    }

    public Long getNextOrder(String projectId) {
        Long pos = extFunctionalCaseMapper.getPos(projectId);
        return (pos == null ? 0 : pos) + ServiceUtils.POS_STEP;
    }

    public long getNextNum(String projectId) {
        return NumGenerator.nextNum(projectId, ApplicationNumScope.CASE_MANAGEMENT);
    }
}
