package io.metersphere.system.service;

import io.metersphere.ai.engine.common.AIModelParamType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.constants.AIConfigConstants;
import io.metersphere.system.domain.AiModelSource;
import io.metersphere.system.domain.AiModelSourceExample;
import io.metersphere.system.dto.request.ai.AIChatOption;
import io.metersphere.system.dto.request.ai.AdvSettingDTO;
import io.metersphere.system.dto.request.ai.AiModelSourceDTO;
import io.metersphere.system.dto.request.ai.AiModelSourceRequest;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.mapper.ExtAiModelSourceMapper;
import io.metersphere.system.mapper.AiModelSourceMapper;
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

    /**
     * 编辑模型配置
     *
     * @param aiModelSourceDTO 模型配置数据传输对象
     * @param userId         用户ID
     */
    public AiModelSource editModuleConfig(AiModelSourceDTO aiModelSourceDTO, String userId) {
        String id = IDGenerator.nextStr();
        //根据是否有id,判断新增还是编辑，并且检查模型源名称是否重复
        AiModelSourceExample aiModelSourceExample = new AiModelSourceExample();
        aiModelSourceExample.createCriteria().andNameEqualTo(aiModelSourceDTO.getName());
        boolean add = true;
        if (StringUtils.isNotBlank(aiModelSourceDTO.getId())) {
            id = aiModelSourceDTO.getId();
            add = false;
            aiModelSourceExample.createCriteria().andIdNotEqualTo(id);
        }
        long sameNameCount = aiModelSourceMapper.countByExample(aiModelSourceExample);
        if (sameNameCount>0) {
            throw new MSException(Translator.get("system_model_name_exist"));
        }
        AiModelSource aiModelSource = new AiModelSource();
        buildModelSource(aiModelSourceDTO, userId, aiModelSource, id);
        //进入之前进行校验
        validModel(aiModelSourceDTO);
        //保存
        if (add) {
            aiModelSourceMapper.insert(aiModelSource);
        }else {
            aiModelSourceMapper.updateByPrimaryKey(aiModelSource);
        }
        return aiModelSource;
    }

    /**
     * 构建模型源对象
     *
     * @param aiModelSourceDTO 模型源数据传输对象
     * @param userId         用户ID
     * @param aiModelSource    模型源对象
     * @param id             模型源ID
     */
    private void buildModelSource(AiModelSourceDTO aiModelSourceDTO, String userId, AiModelSource aiModelSource, String id) {
        aiModelSource.setId(id);
        aiModelSource.setType(aiModelSourceDTO.getType());
        aiModelSource.setName(aiModelSourceDTO.getName());
        aiModelSource.setProviderName(aiModelSourceDTO.getProviderName());
        aiModelSource.setPermissionType(aiModelSourceDTO.getPermissionType());
        aiModelSource.setStatus(aiModelSourceDTO.getStatus());
        aiModelSource.setOwnerType(aiModelSourceDTO.getOwnerType());
        if (StringUtils.equalsIgnoreCase(aiModelSourceDTO.getPermissionType(), AIConfigConstants.AiPermissionType.PRIVATE.toString())) {
            aiModelSource.setOwner(userId);
        } else {
            aiModelSource.setOwner(aiModelSourceDTO.getOwner());
        }
        aiModelSource.setBaseName(aiModelSourceDTO.getBaseName());
        aiModelSource.setAppKey(aiModelSourceDTO.getAppKey());
        aiModelSource.setApiUrl(aiModelSourceDTO.getApiUrl());
        //校验高级参数是否合格，以及默认值设置
        List<AdvSettingDTO> advSettingDTOList = aiModelSourceDTO.getAdvSettingDTOList();
        List<AdvSettingDTO> advSettingDTOS = getAdvSettingDTOS(advSettingDTOList);
        aiModelSource.setAdvSettings(JSON.toJSONString(advSettingDTOS));
    }

    /**
     * 校验参数类型是否有效
     *
     * @param paramType 参数类型
     * @return 是否有效
     */
    public boolean isValidParamType(String paramType) {
        List<String>paramTypes = List.of(
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
        if (CollectionUtils.isNotEmpty(advSettingDTOList)){
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
     * @return Map<String, AdvSettingDTO>
     */
    private static Map<String, AdvSettingDTO> getDefaultAdvSettingDTOMap() {
        Map<String,AdvSettingDTO> advSettingDTOMap = new HashMap<>();
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

    /** 检查高级参数默认值，如果没有配置，则设置为默认值
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
     * @param aiModelSourceDTO 模型源数据传输对象
     */
    private void validModel(AiModelSourceDTO aiModelSourceDTO) {
        AIChatOption aiChatOption = AIChatOption.builder()
                .module(aiModelSourceDTO)
                .prompt("How are you?")
                .build();
        String response = aiChatBaseService.chat(aiChatOption).content();
        if (StringUtils.isBlank(response)) {
            throw new MSException(Translator.get("system_model_test_link_error"));
        }
    }

    /**
     * 获取模型源列表
     * @param aiModelSourceRequest 模型源请求数据传输对象
     * @return 模型源数据传输对象列表
     */
    public List<AiModelSourceDTO> getModelSourceList(AiModelSourceRequest aiModelSourceRequest) {
        List<AiModelSource> list = extAiModelSourceMapper.list(aiModelSourceRequest);
        List<AiModelSourceDTO>resultList = new ArrayList<>();
        for (AiModelSource aiModelSource : list) {
            AiModelSourceDTO aiModelSourceDTO = getModelSourceDTO(aiModelSource);
            resultList.add(aiModelSourceDTO);
        }
        return resultList;
    }

    /**
     * 将APPkey 字符串进行掩码处理
     * @param input 输入的字符串
     * @return 掩码后的字符串
     */
    public static String maskSkString(String input) {
        if (StringUtils.isBlank(input)) {
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
     * @param id 模型源ID
     * @return 模型源数据传输对象
     */
    public AiModelSourceDTO getModelSourceDTO(String id, String userId) {
        AiModelSource aiModelSource = aiModelSourceMapper.selectByPrimaryKey(id);
        if (aiModelSource == null) {
            throw new MSException(Translator.get("system_model_not_exist"));
        }
        //检查个人模型查看权限
        if (StringUtils.isNotBlank(userId) && !StringUtils.equalsIgnoreCase(aiModelSource.getOwner(),userId)) {
            throw new MSException(Translator.get("system_model_not_exist"));
        }
        return getModelSourceDTO(aiModelSource);
    }

    public List<OptionDTO> getModelSourceNameList(String id, String userId) {
        return extAiModelSourceMapper.sourceNameList(id, userId);
    }

    /**
     * 根据ID获取模型源数据传输对象
     * @param id 模型源ID
     * @return 模型源数据传输对象
     */
    public AiModelSourceDTO getModelSourceDTOWithKey(String id, String userId, String orgId) {
        AiModelSource aiModelSource = aiModelSourceMapper.selectByPrimaryKey(id);
        if (aiModelSource == null) {
            throw new MSException(Translator.get("system_model_not_exist"));
        }
        // 校验权限，全局的和自己的
        if (!StringUtils.equalsAny(aiModelSource.getOwner(), userId, orgId)) {
            throw new MSException(Translator.get("system_model_not_exist"));
        }
        return getModelSourceDTOWithKey(aiModelSource);
    }

    public void delModelInformation(String id, String orgId, String userId) {
        AiModelSource aiModelSource = aiModelSourceMapper.selectByPrimaryKey(id);
        if (aiModelSource == null) {
            throw new MSException(Translator.get("system_model_not_exist"));
        }
        // 校验全局权限
        if (StringUtils.isNotBlank(orgId) && !StringUtils.equalsIgnoreCase(aiModelSource.getOwner(),orgId)){
            throw new MSException(Translator.get("system_model_not_exist"));
        }
        //检查个人模型查看权限
        if (StringUtils.isNotBlank(userId) && !StringUtils.equalsIgnoreCase(aiModelSource.getOwner(),userId)) {
            throw new MSException(Translator.get("system_model_not_exist"));
        }
        aiModelSourceMapper.deleteByPrimaryKey(id);
    }
}
