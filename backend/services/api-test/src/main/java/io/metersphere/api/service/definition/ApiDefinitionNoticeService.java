package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiDefinitionExample;
import io.metersphere.api.dto.definition.ApiDefinitionAddRequest;
import io.metersphere.api.dto.definition.ApiDefinitionUpdateRequest;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.sdk.ApiDefinitionCaseDTO;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.service.CommonNoticeSendService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ApiDefinitionNoticeService {

    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private CommonNoticeSendService commonNoticeSendService;

    public ApiDefinitionCaseDTO getApiDTO(ApiDefinitionAddRequest request) {
        ApiDefinitionCaseDTO caseDTO = new ApiDefinitionCaseDTO();
        BeanUtils.copyBean(caseDTO, request);
        return caseDTO;
    }

    public ApiDefinitionCaseDTO getUpdateApiDTO(ApiDefinitionUpdateRequest request) {
        ApiDefinitionCaseDTO caseDTO = new ApiDefinitionCaseDTO();
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(request.getId());
        BeanUtils.copyBean(caseDTO, apiDefinition);
        return caseDTO;
    }

    public ApiDefinitionCaseDTO getDeleteApiDTO(String id) {
        ApiDefinitionCaseDTO caseDTO = new ApiDefinitionCaseDTO();
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(id);
        BeanUtils.copyBean(caseDTO, apiDefinition);
        return caseDTO;
    }

    public void batchSendNotice(List<String> ids, String userId, String projectId, String event) {
        if (CollectionUtils.isNotEmpty(ids)) {
            User user = userMapper.selectByPrimaryKey(userId);
            SubListUtils.dealForSubList(ids, 100, (subList) -> {
                ApiDefinitionExample example = new ApiDefinitionExample();
                example.createCriteria().andIdIn(subList);
                List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
                List<ApiDefinitionCaseDTO> noticeLists = apiDefinitions.stream()
                        .map(apiDefinition -> {
                            ApiDefinitionCaseDTO apiDefinitionCaseDTO = new ApiDefinitionCaseDTO();
                            BeanUtils.copyBean(apiDefinitionCaseDTO, apiDefinition);
                            return apiDefinitionCaseDTO;
                        })
                        .toList();
                List<Map> resources = new ArrayList<>(JSON.parseArray(JSON.toJSONString(noticeLists), Map.class));
                commonNoticeSendService.sendNotice(NoticeConstants.TaskType.API_DEFINITION_TASK, event, resources, user, projectId);
            });
        }
    }
}
