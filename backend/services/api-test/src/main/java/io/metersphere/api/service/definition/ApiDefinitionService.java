package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiDefinitionExample;
import io.metersphere.api.dto.definition.ApiCaseComputeDTO;
import io.metersphere.api.dto.definition.ApiDefinitionDTO;
import io.metersphere.api.dto.definition.ListRequestDTO;
import io.metersphere.api.dto.request.ApiDefinitionPageRequest;
import io.metersphere.api.enums.ApiReportStatus;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.api.mapper.ExtApiDefinitionMapper;
import io.metersphere.system.service.UserLoginService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ApiDefinitionService {

    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;

    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;

    @Resource
    private UserLoginService userLoginService;


    public ApiDefinitionDTO create(ApiDefinitionDTO request, List<MultipartFile> bodyFiles) {
        return request;
    }

    public List<ApiDefinition> list(@NotNull ListRequestDTO request) {
        ApiDefinitionExample example = new ApiDefinitionExample();
        ApiDefinitionExample.Criteria criteria = example.createCriteria();
        criteria.andProjectIdEqualTo(request.getProjectId());
        if (StringUtils.isNotBlank(request.getName())) {
            criteria.andNameLike("%" + request.getName() + "%");
        }
        if (StringUtils.isNotBlank(request.getProtocol())) {
            criteria.andProtocolEqualTo(request.getProtocol());
        }
        return apiDefinitionMapper.selectByExample(example);
    }

    public void batchDelete(List<String> ids) {

    }

    public List<ApiDefinitionDTO> getApiDefinitionPage(ApiDefinitionPageRequest request, Boolean deleted){
        List<ApiDefinitionDTO> list = extApiDefinitionMapper.list(request, deleted);
        if (!CollectionUtils.isEmpty(list)) {
            convertUserIdToName(list);
            calculateApiCase(list, request.getProjectId());
        }
        return list;
    }
    private void convertUserIdToName(List<ApiDefinitionDTO> list) {
        Set<String> userIds = extractUserIds(list);
        Map<String, String> userMap = userLoginService.getUserNameMap(new ArrayList<>(userIds));

        list.forEach(item -> {
            item.setCreateUserName(userMap.get(item.getCreateUser()));
            item.setDeleteUserName(userMap.get(item.getDeleteUser()));
            item.setUpdateUserName(userMap.get(item.getUpdateUser()));
        });
    }

    private Set<String> extractUserIds(List<ApiDefinitionDTO> list) {
        return list.stream()
                .flatMap(apiDefinition -> Stream.of(apiDefinition.getUpdateUser(), apiDefinition.getDeleteUser(), apiDefinition.getCreateUser()))
                .collect(Collectors.toSet());
    }

    private void calculateApiCase(List<ApiDefinitionDTO> list, String projectId) {
        List<String> ids = list.stream().map(ApiDefinitionDTO::getId).toList();
        List<ApiCaseComputeDTO> apiCaseComputeList = extApiDefinitionMapper.selectApiCaseByIdsAndStatusIsNotTrash(ids, projectId);
        Map<String, ApiCaseComputeDTO> resultMap = apiCaseComputeList.stream().collect(Collectors.toMap(ApiCaseComputeDTO::getApiDefinitionId, Function.identity()));

        list.forEach(item -> {
            ApiCaseComputeDTO apiCaseComputeDTO = resultMap.get(item.getId());
            if (apiCaseComputeDTO != null) {
                item.setCaseTotal(apiCaseComputeDTO.getCaseTotal());
                item.setCasePassRate(apiCaseComputeDTO.getCasePassRate());
                // 状态优先级 未执行，未通过，误报（FAKE_ERROR），通过
                if ((apiCaseComputeDTO.getError() + apiCaseComputeDTO.getSuccess()) < apiCaseComputeDTO.getCaseTotal()) {
                    item.setCaseStatus(ApiReportStatus.PENDING.name());
                } else if (apiCaseComputeDTO.getError() > 0) {
                    item.setCaseStatus(ApiReportStatus.ERROR.name());
                } else if (apiCaseComputeDTO.getFakeError() > 0) {
                    item.setCaseStatus(ApiReportStatus.FAKE_ERROR.name());
                } else {
                    item.setCaseStatus(ApiReportStatus.SUCCESS.name());
                }
            } else {
                item.setCaseTotal(0);
                item.setCasePassRate("-");
                item.setCaseStatus("-");
            }
        });
    }

}
