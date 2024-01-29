package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiDefinitionCustomField;
import io.metersphere.api.dto.definition.ApiDefinitionAddRequest;
import io.metersphere.api.dto.definition.ApiDefinitionUpdateRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.dto.sdk.ApiDefinitionCaseDTO;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.mapper.CustomFieldMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ApiDefinitionNoticeService {
    
    @Resource
    private CustomFieldMapper customFieldMapper;

    public ApiDefinitionCaseDTO getApiDTO(ApiDefinitionAddRequest request) {
        ApiDefinitionCaseDTO caseDTO = new ApiDefinitionCaseDTO();
        BeanUtils.copyBean(caseDTO, request);
        List<OptionDTO> fields = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(request.getCustomFields())) {
            for (ApiDefinitionCustomField customFieldDTO : request.getCustomFields()) {
                OptionDTO optionDTO = new OptionDTO();
                CustomField customField = customFieldMapper.selectByPrimaryKey(customFieldDTO.getFieldId());
                if (customField == null) {
                    continue;
                }
                optionDTO.setId(customField.getId());
                optionDTO.setName(customField.getName());
                fields.add(optionDTO);
            }
        }
        caseDTO.setFields(fields);
        return caseDTO;
    }

    public ApiDefinitionCaseDTO getUpdateApiDTO(ApiDefinitionUpdateRequest request) {
        ApiDefinitionCaseDTO caseDTO = new ApiDefinitionCaseDTO();
        BeanUtils.copyBean(caseDTO, request);
        List<OptionDTO> fields = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(request.getCustomFields())) {
            for (ApiDefinitionCustomField customFieldDTO : request.getCustomFields()) {
                OptionDTO optionDTO = new OptionDTO();
                CustomField customField = customFieldMapper.selectByPrimaryKey(customFieldDTO.getFieldId());
                if (customField == null) {
                    continue;
                }
                optionDTO.setId(customField.getId());
                optionDTO.setName(customField.getName());
                fields.add(optionDTO);
            }
        }
        caseDTO.setFields(fields);
        return caseDTO;
    }

}
