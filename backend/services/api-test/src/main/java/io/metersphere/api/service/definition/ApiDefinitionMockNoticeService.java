package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiDefinitionMock;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockAddRequest;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockUpdateRequest;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.api.mapper.ApiDefinitionMockMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.dto.sdk.ApiDefinitionCaseDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class ApiDefinitionMockNoticeService {

    @Resource
    private ApiDefinitionMockMapper apiDefinitionMockMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;


    public ApiDefinitionCaseDTO getApiMockDTO(ApiDefinitionMockAddRequest request) {
        ApiDefinitionCaseDTO mockDTO = new ApiDefinitionCaseDTO();
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(request.getApiDefinitionId());
        BeanUtils.copyBean(mockDTO, apiDefinition);
        mockDTO.setMockName(request.getName());
        return mockDTO;
    }

    public ApiDefinitionCaseDTO getApiMockDTO(ApiDefinitionMockUpdateRequest request) {
        ApiDefinitionCaseDTO mockDTO = new ApiDefinitionCaseDTO();
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(request.getApiDefinitionId());
        BeanUtils.copyBean(mockDTO, apiDefinition);
        mockDTO.setMockName(request.getName());
        return mockDTO;
    }

    public ApiDefinitionCaseDTO getApiMockDTO(String id) {
        ApiDefinitionCaseDTO mockDTO = new ApiDefinitionCaseDTO();
        ApiDefinitionMock apiDefinitionMock = apiDefinitionMockMapper.selectByPrimaryKey(id);
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(apiDefinitionMock.getApiDefinitionId());
        BeanUtils.copyBean(mockDTO, apiDefinition);
        mockDTO.setMockName(apiDefinitionMock.getName());
        return mockDTO;
    }


}
