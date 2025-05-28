package io.metersphere.system.service;

import io.metersphere.ai.engine.ChatToolEngine;
import io.metersphere.ai.engine.common.AIChatOptions;
import io.metersphere.ai.engine.common.AIModelParamType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.constants.AIConfigConstants;
import io.metersphere.system.domain.ModelSource;
import io.metersphere.system.domain.ModelSourceExample;
import io.metersphere.system.dto.request.ai.AdvSettingDTO;
import io.metersphere.system.dto.request.ai.ModelSourceDTO;
import io.metersphere.system.dto.request.ai.ModelSourceRequest;
import io.metersphere.system.mapper.ExtModelSourceMapper;
import io.metersphere.system.mapper.ModelSourceMapper;
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
    private ModelSourceMapper modelSourceMapper;
    @Resource
    private ExtModelSourceMapper extModelSourceMapper;


    /**
     * 编辑模型配置
     *
     * @param modelSourceDTO 模型配置数据传输对象
     * @param userId         用户ID
     */
    public void editModuleConfig(ModelSourceDTO modelSourceDTO, String userId) {
        String id = IDGenerator.nextStr();
        //根据是否有id,判断新增还是编辑，并且检查模型源名称是否重复
        ModelSourceExample modelSourceExample = new ModelSourceExample();
        modelSourceExample.createCriteria().andNameEqualTo(modelSourceDTO.getName());
        boolean add = true;
        if (StringUtils.isNotBlank(modelSourceDTO.getId())) {
            id = modelSourceDTO.getId();
            add = false;
            modelSourceExample.createCriteria().andIdNotEqualTo(id);
        }
        long sameNameCount = modelSourceMapper.countByExample(modelSourceExample);
        if (sameNameCount>0) {
            throw new MSException(Translator.get("system_model_name_exist"));
        }
        ModelSource modelSource = new ModelSource();
        buildModelSource(modelSourceDTO, userId, modelSource, id);
        //进入之前进行校验
        validModel(modelSourceDTO, modelSource);
        //保存
        if (add) {
            modelSourceMapper.insert(modelSource);
        }else {
            modelSourceMapper.updateByPrimaryKey(modelSource);
        }
    }

    /**
     * 构建模型源对象
     *
     * @param modelSourceDTO 模型源数据传输对象
     * @param userId         用户ID
     * @param modelSource    模型源对象
     * @param id             模型源ID
     */
    private void buildModelSource(ModelSourceDTO modelSourceDTO, String userId, ModelSource modelSource, String id) {
        modelSource.setId(id);
        modelSource.setType(modelSourceDTO.getType());
        modelSource.setName(modelSourceDTO.getName());
        modelSource.setProviderName(modelSourceDTO.getProviderName());
        modelSource.setAvatar(modelSourceDTO.getAvatar());
        modelSource.setPermissionType(modelSourceDTO.getPermissionType());
        modelSource.setStatus(modelSourceDTO.getStatus());
        modelSource.setOwnerType(modelSourceDTO.getOwnerType());
        if (StringUtils.equalsIgnoreCase(modelSourceDTO.getPermissionType(), AIConfigConstants.AiPermissionType.PRIVATE.toString())) {
            modelSource.setOwner(userId);
        } else {
            modelSource.setOwner(modelSourceDTO.getOwner());
        }
        modelSource.setBaseName(modelSourceDTO.getBaseName());
        modelSource.setAppKey(modelSourceDTO.getAppKey());
        modelSource.setApiUrl(modelSourceDTO.getApiUrl());
        //校验高级参数是否合格，以及默认值设置
        List<AdvSettingDTO> advSettingDTOList = modelSourceDTO.getAdvSettingDTOList();
        List<AdvSettingDTO> advSettingDTOS = getAdvSettingDTOS(advSettingDTOList);
        modelSource.setAdvSettings(JSON.toJSONString(advSettingDTOS));
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
     * @param modelSourceDTO 模型源数据传输对象
     * @param modelSource 模型源对象
     */
    private static void validModel(ModelSourceDTO modelSourceDTO, ModelSource modelSource) {
        String response = ChatToolEngine.builder(modelSourceDTO.getProviderName(),
                        AIChatOptions.builder()
                                .modelType(modelSource.getBaseName())
                                .apiKey(modelSource.getAppKey())
                                .baseUrl(modelSource.getApiUrl())
                                .build())
                .prompt("How are you?")
                .execute();
        if (StringUtils.isBlank(response)) {
            throw new MSException(Translator.get("system_model_test_link_error"));
        }
    }

    /**
     * 获取模型源列表
     * @param modelSourceRequest 模型源请求数据传输对象
     * @return 模型源数据传输对象列表
     */
    public List<ModelSourceDTO> getModelSourceList(ModelSourceRequest modelSourceRequest) {
        List<ModelSource> list = extModelSourceMapper.list(modelSourceRequest);
        List<ModelSourceDTO>resultList = new ArrayList<>();
        for (ModelSource modelSource : list) {
            ModelSourceDTO modelSourceDTO = getModelSourceDTO(modelSource);
            resultList.add(modelSourceDTO);
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

    @NotNull
    private static ModelSourceDTO getModelSourceDTO(ModelSource modelSource) {
        ModelSourceDTO modelSourceDTO = new ModelSourceDTO();
        BeanUtils.copyBean(modelSourceDTO, modelSource);
        modelSourceDTO.setAppKey(maskSkString(modelSource.getAppKey()));
        List<AdvSettingDTO> advSettingDTOList = JSON.parseArray(modelSource.getAdvSettings(), AdvSettingDTO.class);
        modelSourceDTO.setAdvSettingDTOList(advSettingDTOList);
        return modelSourceDTO;
    }

    /**
     * 根据ID获取模型源数据传输对象
     * @param id 模型源ID
     * @return 模型源数据传输对象
     */
    public ModelSourceDTO getModelSourceDTO(String id, String userId) {
        ModelSource modelSource = modelSourceMapper.selectByPrimaryKey(id);
        if (modelSource == null) {
            throw new MSException(Translator.get("system_model_not_exist"));
        }
        //检查个人模型查看权限
        if (StringUtils.isNotBlank(userId) && !StringUtils.equalsIgnoreCase(modelSource.getOwner(),userId)) {
            throw new MSException(Translator.get("system_model_not_exist"));
        }
        return getModelSourceDTO(modelSource);
    }
}
