package io.metersphere.system.service;

import io.metersphere.ai.engine.common.AIModelParamType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.constants.AIConfigConstants;
import io.metersphere.system.domain.AiModelSource;
import io.metersphere.system.domain.AiModelSourceExample;
import io.metersphere.system.dto.request.ai.*;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.mapper.AiModelSourceMapper;
import io.metersphere.system.mapper.ExtAiModelSourceMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class SystemAIConfigService {

    @Resource
    private AiModelSourceMapper aiModelSourceMapper;
    @Resource
    private ExtAiModelSourceMapper extAiModelSourceMapper;
    @Resource
    private AiChatBaseService aiChatBaseService;
    @Resource
    private PermissionCheckService permissionCheckService;

    private static final String DEFAULT_OWNER = "system";

    /**
     * 编辑模型配置
     *
     * @param aiModelSourceDTO 模型配置数据传输对象
     * @param userId           用户ID
     */
    public AiModelSource editModuleConfig(AiModelSourceDTO aiModelSourceDTO, String userId) {
        // 使用条件表达式确定ID并设置操作标志
        String id = StringUtils.isNotBlank(aiModelSourceDTO.getId()) ?
                aiModelSourceDTO.getId() : IDGenerator.nextStr();
        boolean isAddOperation = StringUtils.isBlank(aiModelSourceDTO.getId());

        //设置模型拥有者
        setOwner(aiModelSourceDTO, userId);

        // 校验模型名称唯一性
        if (isModelNameDuplicated(aiModelSourceDTO.getName(), id, isAddOperation, aiModelSourceDTO.getOwner())) {
            throw new MSException(Translator.get("system_model_name_exist") + StringUtils.SPACE + aiModelSourceDTO.getName() + StringUtils.SPACE + Translator.get("system_model_name_exist_label"));
        }

        // 检查AppKey变更
        validateAppKey(aiModelSourceDTO, id);

        // 创建并填充模型对象
        var aiModelSource = new AiModelSource();
        buildModelSource(aiModelSourceDTO, userId, aiModelSource, id, isAddOperation);

        // 根据操作类型执行不同逻辑
        if (isAddOperation) {
            // 新增时如果开启状态，验证模型
            validateModelIfEnabled(aiModelSource, aiModelSourceDTO);
            aiModelSourceMapper.insert(aiModelSource);
        } else {
            // 更新时如果开启状态且Key变更，需要验证模型
            if (aiModelSource.getStatus()) {
                validModel(aiModelSourceDTO);
            }
            aiModelSourceMapper.updateByPrimaryKey(aiModelSource);
        }

        return aiModelSource;
    }

    private static void setOwner(AiModelSourceDTO aiModelSourceDTO, String userId) {
        if (StringUtils.equalsIgnoreCase(aiModelSourceDTO.getPermissionType(), AIConfigConstants.AiPermissionType.PRIVATE.toString())) {
            aiModelSourceDTO.setOwner(userId);
        } else {
            aiModelSourceDTO.setOwner(DEFAULT_OWNER);
        }
    }

    // 提取验证模型名称是否重复的逻辑
    private boolean isModelNameDuplicated(String name, String id, boolean isAddOperation, String owner) {
        var example = new AiModelSourceExample();
        if (isAddOperation) {
            if (StringUtils.equalsIgnoreCase(owner, DEFAULT_OWNER)) {
                example.createCriteria().andNameEqualTo(name); // 如果是系统模型，判断所有数据
            } else {
                example.createCriteria().andNameEqualTo(name).andOwnerIn(List.of(owner,DEFAULT_OWNER)); // 如果是个人模型，则需要判断当前用户和系统中是否有同名模型
            }
        } else {
            // 更新操作时，排除当前ID
            if (StringUtils.equalsIgnoreCase(owner, DEFAULT_OWNER)) {
                example.createCriteria().andNameEqualTo(name).andIdNotEqualTo(id); // 系统模型
            } else {
                // 个人模型需要owner条件
                example.createCriteria().andNameEqualTo(name).andIdNotEqualTo(id).andOwnerIn(List.of(owner,DEFAULT_OWNER));
            }
        }
        return aiModelSourceMapper.countByExample(example) > 0;
    }

    // 验证模型有效性
    private void validateModelIfEnabled(AiModelSource aiModelSource, AiModelSourceDTO aiModelSourceDTO) {
        if (aiModelSource.getStatus()) {
            validModel(aiModelSourceDTO);
        }
    }

    private void validateAppKey(AiModelSourceDTO aiModelSourceDTO, String id) {
        AiModelSource oldSource = aiModelSourceMapper.selectByPrimaryKey(id);
        if (oldSource == null) {
            return; // 新增模型源时不需要验证AppKey
        }
        String oldKey = maskSkString(oldSource.getAppKey());
        if (StringUtils.equalsIgnoreCase(oldKey, aiModelSourceDTO.getAppKey())) {
            aiModelSourceDTO.setAppKey(oldSource.getAppKey());
        }
    }

    /**
     * 构建模型源对象
     *
     * @param aiModelSourceDTO 模型源数据传输对象
     * @param userId           用户ID
     * @param aiModelSource    模型源对象
     * @param id               模型源ID
     */
    private void buildModelSource(AiModelSourceDTO aiModelSourceDTO, String userId, AiModelSource aiModelSource, String id, boolean isAddOperation) {
        aiModelSource.setId(id);
        aiModelSource.setType(aiModelSourceDTO.getType());
        aiModelSource.setName(aiModelSourceDTO.getName());
        aiModelSource.setProviderName(aiModelSourceDTO.getProviderName());
        aiModelSource.setPermissionType(aiModelSourceDTO.getPermissionType());
        aiModelSource.setStatus(aiModelSourceDTO.getStatus());
        aiModelSource.setOwnerType(aiModelSourceDTO.getOwnerType());
        aiModelSource.setOwner(aiModelSourceDTO.getOwner());
        aiModelSource.setBaseName(aiModelSourceDTO.getBaseName());
        aiModelSource.setAppKey(aiModelSourceDTO.getAppKey());
        aiModelSource.setApiUrl(aiModelSourceDTO.getApiUrl());
        //校验高级参数是否合格，以及默认值设置
        List<AdvSettingDTO> advSettingDTOList = aiModelSourceDTO.getAdvSettingDTOList();
        List<AdvSettingDTO> advSettingDTOS = getAdvSettingDTOS(advSettingDTOList);
        aiModelSource.setAdvSettings(JSON.toJSONString(advSettingDTOS));
        aiModelSource.setCreateTime(System.currentTimeMillis());
        if (isAddOperation) {
            aiModelSource.setCreateUser(userId);
        } else {
            // 更新操作时，保留原创建人
            aiModelSource.setCreateUser(aiModelSourceDTO.getCreateUser());
        }

        aiModelSource.setStatus(aiModelSourceDTO.getStatus() != null && aiModelSourceDTO.getStatus());
    }

    /**
     * 校验参数类型是否有效
     *
     * @param paramType 参数类型
     * @return 是否有效
     */
    public boolean isValidParamType(String paramType) {
        List<String> paramTypes = List.of(
                AIModelParamType.MAX_TOKENS,
                AIModelParamType.TOP_P,
                AIModelParamType.FREQUENCY_PENALTY,
                AIModelParamType.TEMPERATURE
        );

        return paramTypes.contains(paramType);
    }

    /**
     * 获取高级参数配置列表
     *
     * @param advSettingDTOList 高级参数配置数据传输对象列表
     * @return 高级参数配置数据传输对象列表
     */
    @NotNull
    private List<AdvSettingDTO> getAdvSettingDTOS(List<AdvSettingDTO> advSettingDTOList) {
        //设置默认高级参数配置
        Map<String, AdvSettingDTO> advSettingDTOMap = getDefaultAdvSettingDTOMap();
        List<AdvSettingDTO> advSettingDTOS = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(advSettingDTOList)) {
            for (AdvSettingDTO advSettingDTO : advSettingDTOList) {
                //校验前端的高级参数属性,如果不存在，则不保存
                if (!isValidParamType(advSettingDTO.getName())) {
                    continue;
                }
                AdvSettingDTO advSetting = advSettingDTOMap.get(advSettingDTO.getName());
                BeanUtils.copyBean(advSetting, advSettingDTO);
                checkParamDefault(advSetting);
                advSettingDTOS.add(advSetting);
            }
        }
        return advSettingDTOS;
    }

    /**
     * 设置默认高级参数配置, 类型固定，防止前端传入错误的参数类型
     *
     * @return Map<String, AdvSettingDTO>
     */
    private static Map<String, AdvSettingDTO> getDefaultAdvSettingDTOMap() {
        Map<String, AdvSettingDTO> advSettingDTOMap = new HashMap<>();
        AdvSettingDTO modelConfigDTO = new AdvSettingDTO(AIModelParamType.TEMPERATURE, "温度", null, false);
        advSettingDTOMap.put(modelConfigDTO.getName(), modelConfigDTO);
        modelConfigDTO = new AdvSettingDTO(AIModelParamType.TOP_P, "Top P", null, false);
        advSettingDTOMap.put(modelConfigDTO.getName(), modelConfigDTO);
        modelConfigDTO = new AdvSettingDTO(AIModelParamType.MAX_TOKENS, "最大Token", null, false);
        advSettingDTOMap.put(modelConfigDTO.getName(), modelConfigDTO);
        modelConfigDTO = new AdvSettingDTO(AIModelParamType.FREQUENCY_PENALTY, "频率惩罚", null, false);
        advSettingDTOMap.put(modelConfigDTO.getName(), modelConfigDTO);
        return advSettingDTOMap;
    }

    /**
     * 检查高级参数默认值，如果没有配置，则设置为默认值
     *
     * @param advSetting 高级参数配置数据传输对象
     */
    private static void checkParamDefault(AdvSettingDTO advSetting) {
        //如果是温度，则默认值为0.7
        if (StringUtils.equalsIgnoreCase(advSetting.getName(), AIModelParamType.TEMPERATURE)
                && advSetting.getValue() == null) {
            advSetting.setValue(0.7);
            advSetting.setEnable(false);
        }
        //如果是topP，则默认值为1.0
        if (StringUtils.equalsIgnoreCase(advSetting.getName(), AIModelParamType.TOP_P)
                && advSetting.getValue() == null) {
            advSetting.setValue(1.0);
            advSetting.setEnable(false);
        }
        //如果是最大token，则默认值为1024
        if (StringUtils.equalsIgnoreCase(advSetting.getName(), AIModelParamType.MAX_TOKENS)
                && advSetting.getValue() == null) {
            advSetting.setValue(1024.0);
            advSetting.setEnable(false);
        }
        //如果是频率惩罚，则默认值为0.0
        if (StringUtils.equalsIgnoreCase(advSetting.getName(), AIModelParamType.FREQUENCY_PENALTY)
                && advSetting.getValue() == null) {
            advSetting.setValue(0.0);
            advSetting.setEnable(false);
        }
    }

    /**
     * 验证模型连接是否成功
     *
     * @param aiModelSourceDTO 模型源数据传输对象
     */
    private void validModel(@NotNull AiModelSourceDTO aiModelSourceDTO) {
        try {
            var aiChatOption = AIChatOption.builder()
                    .module(aiModelSourceDTO)
                    .prompt("How are you?")
                    .build();
            var response = aiChatBaseService.chat(aiChatOption).content();
            if (StringUtils.isBlank(response)) {
                throw new MSException(Translator.get("system_model_test_link_error"));
            }
        } catch (Exception e) {
            var message = e.getMessage();
            if (StringUtils.isNotBlank(message) && message.contains("-")) {
                var substring = StringUtils.substringBefore(message, "-");
                throw new MSException(
                        String.format("%s[%s]", Translator.get("system_model_test_chat_error"), substring), e
                );
            }
            throw new MSException(
                    String.format("%s[ Unknown response code: 0 ]", Translator.get("system_model_test_chat_error")), e
            );
        }
    }

    /**
     * 获取模型源列表
     *
     * @param aiModelSourceRequest 模型源请求数据传输对象
     * @return 模型源数据传输对象列表
     */
    public List<AiModelSourceDTO> getModelSourceList(AiModelSourceRequest aiModelSourceRequest) {
        if (StringUtils.isBlank(aiModelSourceRequest.getOwner())) {
            aiModelSourceRequest.setOwner(DEFAULT_OWNER);
        }
        List<AiModelSourceCreateNameDTO> list = extAiModelSourceMapper.list(aiModelSourceRequest);
        List<AiModelSourceDTO> resultList = new ArrayList<>();
        for (AiModelSourceCreateNameDTO aiModelSource : list) {
            AiModelSourceDTO aiModelSourceDTO = getModelSourceDTO(aiModelSource);
            aiModelSourceDTO.setCreateUserName(aiModelSource.getCreateUserName());
            resultList.add(aiModelSourceDTO);
        }
        return resultList;
    }

    /**
     * 将APPkey 字符串进行掩码处理
     *
     * @param input 输入的字符串
     * @return 掩码后的字符串
     */
    public static String maskSkString(String input) {
        if (StringUtils.isBlank(input)) {
            return input;
        }
        // 如果输入为空或长度小于等于6，直接返回原字符串
        if (input.length() <= 6) {
            return input;
        }
        // 提取前缀和后缀
        String prefix = input.substring(0, 4); // sk-AB
        String suffix = input.substring(input.length() - 2); // 最后两个字符

        return prefix + "**** " + suffix;
    }

    private AiModelSourceDTO getModelSourceDTO(AiModelSource modelSource) {
        AiModelSourceDTO modelSourceDTO = getModelSourceDTOWithKey(modelSource);
        modelSourceDTO.setAppKey(maskSkString(modelSource.getAppKey()));
        return modelSourceDTO;
    }

    private AiModelSourceDTO getModelSourceDTOWithKey(AiModelSource modelSource) {
        AiModelSourceDTO modelSourceDTO = new AiModelSourceDTO();
        BeanUtils.copyBean(modelSourceDTO, modelSource);
        List<AdvSettingDTO> advSettingDTOList = JSON.parseArray(modelSource.getAdvSettings(), AdvSettingDTO.class);
        modelSourceDTO.setAdvSettingDTOList(advSettingDTOList);
        return modelSourceDTO;
    }

    /**
     * 根据ID获取模型源数据传输对象
     *
     * @param id 模型源ID
     * @return 模型源数据传输对象
     */
    public AiModelSourceDTO getModelSourceDTO(String id, String userId) {
        AiModelSource aiModelSource = aiModelSourceMapper.selectByPrimaryKey(id);
        if (aiModelSource == null) {
            throw new MSException(Translator.get("system_model_not_exist"));
        }
        //检查个人模型查看权限
        if (StringUtils.isNotBlank(userId) && !StringUtils.equalsIgnoreCase(aiModelSource.getOwner(), userId)) {
            throw new MSException(Translator.get("system_model_not_exist"));
        }
        return getModelSourceDTO(aiModelSource);
    }

    public List<OptionDTO> getModelSourceNameList(String userId) {
        // 如果用户有系统级权限，则获取所有模型名称列表
        return extAiModelSourceMapper.enableSourceNameList(userId);
    }

    /**
     * 根据ID获取模型源数据传输对象
     *
     * @param id 模型源ID
     * @return 模型源数据传输对象
     */
    public AiModelSourceDTO getModelSourceDTOWithKey(String id, String userId) {
        AiModelSource aiModelSource = aiModelSourceMapper.selectByPrimaryKey(id);
        if (aiModelSource == null) {
            throw new MSException(Translator.get("system_model_not_exist"));
        }
        //检查模型是否开启
        if (!aiModelSource.getStatus()) {
            throw new MSException(Translator.get("system_model_not_enable"));
        }
        // 校验权限，全局的和自己的
        if (!StringUtils.equalsAny(aiModelSource.getOwner(), userId, DEFAULT_OWNER)) {
            throw new MSException(Translator.get("system_model_not_exist"));
        }
        return getModelSourceDTOWithKey(aiModelSource);
    }

    public void delModelInformation(String id, String userId) {
        AiModelSource aiModelSource = aiModelSourceMapper.selectByPrimaryKey(id);
        if (aiModelSource == null) {
            throw new MSException(Translator.get("system_model_not_exist"));
        }
        //检查个人模型查看权限
        if (StringUtils.isNotBlank(userId) && !StringUtils.equalsIgnoreCase(aiModelSource.getOwner(), userId)) {
            throw new MSException(Translator.get("system_model_not_exist"));
        }
        aiModelSourceMapper.deleteByPrimaryKey(id);
    }
}
