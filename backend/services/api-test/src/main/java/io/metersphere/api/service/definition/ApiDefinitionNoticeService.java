package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiDefinitionExample;
import io.metersphere.api.dto.definition.ApiDefinitionAddRequest;
import io.metersphere.api.dto.definition.ApiDefinitionBatchRequest;
import io.metersphere.api.dto.definition.ApiDefinitionUpdateRequest;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.api.mapper.ExtApiDefinitionMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.system.dto.sdk.ApiDefinitionCaseDTO;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ApiDefinitionNoticeService {

    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;

    public ApiDefinitionCaseDTO getApiDTO(ApiDefinitionAddRequest request) {
        ApiDefinitionCaseDTO caseDTO = new ApiDefinitionCaseDTO();
        BeanUtils.copyBean(caseDTO, request);
        return caseDTO;
    }

    public ApiDefinitionCaseDTO getUpdateApiDTO(ApiDefinitionUpdateRequest request) {
        ApiDefinitionCaseDTO caseDTO = new ApiDefinitionCaseDTO();
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(request.getId());
        BeanUtils.copyBean(caseDTO, apiDefinition);
        BeanUtils.copyBean(caseDTO, request);
        return caseDTO;
    }

    public List<ApiDefinitionCaseDTO> getBatchEditApiDTO(ApiDefinitionBatchRequest request) {
        List<String> ids = this.doSelectIds(request, request.getProjectId(), request.getProtocol(), false);
        return handleBatchNotice(ids);
    }

    public List<ApiDefinitionCaseDTO> getEditApiDTO(String id) {
        List<String> ids = new ArrayList<>();
        ids.add(id);
        return handleBatchNotice(ids);
    }

    private List<ApiDefinitionCaseDTO> handleBatchNotice(List<String> ids) {
        List<ApiDefinitionCaseDTO> dtoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            SubListUtils.dealForSubList(ids, 100, (subList) -> {
                detailApiData(subList, dtoList);
            });
        }
        return dtoList;
    }

    private void detailApiData(List<String> ids, List<ApiDefinitionCaseDTO> dtoList) {
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdIn(ids);
        List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
        Map<String, ApiDefinition> apiDefinitionMap = apiDefinitions.stream().collect(Collectors.toMap(ApiDefinition::getId, a -> a));
        ids.forEach(id -> {
            ApiDefinition api = apiDefinitionMap.get(id);
            ApiDefinitionCaseDTO apiDefinitionCaseDTO = new ApiDefinitionCaseDTO();
            BeanUtils.copyBean(apiDefinitionCaseDTO, api);
            dtoList.add(apiDefinitionCaseDTO);
        });
    }


    public <T> List<String> doSelectIds(T dto, String projectId, String protocol, boolean deleted) {
        TableBatchProcessDTO request = (TableBatchProcessDTO) dto;
        if (request.isSelectAll()) {
            List<String> ids = extApiDefinitionMapper.getIds(request, projectId, protocol, deleted);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            ids.addAll(request.getSelectIds());
            return ids.stream().distinct().toList();
        } else {
            request.getSelectIds().removeAll(request.getExcludeIds());
            return request.getSelectIds();
        }
    }

}
