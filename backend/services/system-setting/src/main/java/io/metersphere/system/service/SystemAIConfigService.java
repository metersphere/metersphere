package io.metersphere.system.service;

import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CodingUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.ModelSource;
import io.metersphere.system.dto.request.ai.ModelConfigDTO;
import io.metersphere.system.dto.request.ai.ModelSourceDTO;
import io.metersphere.system.dto.request.ai.ModelSourceRequest;
import io.metersphere.system.mapper.ExtModelSourceMapper;
import io.metersphere.system.mapper.ModelSourceMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class SystemAIConfigService {

    @Resource
    private ModelSourceMapper modelSourceMapper;
    @Resource
    private ExtModelSourceMapper extModelSourceMapper;

    public void editModuleConfig(ModelSourceDTO modelSourceDTO, String userId) {
        String id = IDGenerator.nextStr();
        boolean add = true;
        if (StringUtils.isNotBlank(modelSourceDTO.getId())) {
            id = modelSourceDTO.getId();
            add = false;
        }
        ModelSource modelSource = new ModelSource();
        modelSource.setId(id);
        modelSource.setType(modelSourceDTO.getType());
        if (StringUtils.equalsIgnoreCase(modelSourceDTO.getType(),"PUBLIC")) {
            modelSource.setOwner(userId);
        } else {
            modelSource.setOwner(modelSourceDTO.getOrgId());
        }
        ModelConfigDTO modelConfigDTO = new ModelConfigDTO();
        modelConfigDTO.setApiKey(CodingUtils.md5(modelSourceDTO.getApiKey()));
        modelConfigDTO.setApiUrl(modelSourceDTO.getApiUrl());
        modelConfigDTO.setBaseName(modelSourceDTO.getBaseName());
        modelSource.setContent(JSON.toJSONString(modelConfigDTO).getBytes());
        if (add) {
            modelSourceMapper.insert(modelSource);
        }else {
            modelSourceMapper.updateByPrimaryKey(modelSource);
        }
    }

    public List<ModelSourceDTO> getModelSourceList(ModelSourceRequest modelSourceRequest) {
        List<ModelSource> list = extModelSourceMapper.list(modelSourceRequest);
        List<ModelSourceDTO>resultList = new ArrayList<>();
        for (ModelSource modelSource : list) {
            ModelSourceDTO modelSourceDTO = getModelSourceDTO(modelSource);
            resultList.add(modelSourceDTO);
        }
        return resultList;
    }

    @NotNull
    private static ModelSourceDTO getModelSourceDTO(ModelSource modelSource) {
        ModelSourceDTO modelSourceDTO = new ModelSourceDTO();
        BeanUtils.copyBean(modelSourceDTO, modelSource);
        ModelConfigDTO modelConfigDTO = JSON.parseObject(new String(modelSource.getContent()), ModelConfigDTO.class);
        modelSourceDTO.setApiKey(modelConfigDTO.getApiKey());
        modelSourceDTO.setApiUrl(modelConfigDTO.getApiUrl());
        modelSourceDTO.setBaseName(modelConfigDTO.getBaseName());
        return modelSourceDTO;
    }

    public ModelSourceDTO getModelSourceDTO(String id) {
        ModelSource modelSource = modelSourceMapper.selectByPrimaryKey(id);
        return getModelSourceDTO(modelSource);
    }
}
