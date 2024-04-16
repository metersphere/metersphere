package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.domain.ApiTestCaseExample;
import io.metersphere.api.dto.definition.ApiTestCaseAddRequest;
import io.metersphere.api.dto.definition.ApiTestCaseUpdateRequest;
import io.metersphere.api.mapper.ApiTestCaseMapper;
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
public class ApiTestCaseNoticeService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private CommonNoticeSendService commonNoticeSendService;

    public ApiDefinitionCaseDTO addCaseDto(ApiTestCaseAddRequest request) {
        ApiDefinitionCaseDTO caseDTO = new ApiDefinitionCaseDTO();
        BeanUtils.copyBean(caseDTO, request);
        return caseDTO;
    }

    public ApiDefinitionCaseDTO getCaseDTO(ApiTestCaseUpdateRequest request) {
        ApiDefinitionCaseDTO caseDTO = new ApiDefinitionCaseDTO();
        ApiTestCase testCase = apiTestCaseMapper.selectByPrimaryKey(request.getId());
        BeanUtils.copyBean(caseDTO, testCase);
        return caseDTO;
    }

    public ApiDefinitionCaseDTO getCaseDTO(String id) {
        ApiTestCase testCase = apiTestCaseMapper.selectByPrimaryKey(id);
        ApiDefinitionCaseDTO caseDTO = new ApiDefinitionCaseDTO();
        BeanUtils.copyBean(caseDTO, testCase);
        return caseDTO;
    }

    public void batchSendNotice(List<String> ids, String userId, String projectId, String event) {
        if (CollectionUtils.isNotEmpty(ids)) {
            User user = userMapper.selectByPrimaryKey(userId);
            SubListUtils.dealForSubList(ids, 100, (subList) -> {
                ApiTestCaseExample example = new ApiTestCaseExample();
                example.createCriteria().andIdIn(subList);
                List<ApiTestCase> apiTestCases = apiTestCaseMapper.selectByExample(example);
                List<ApiDefinitionCaseDTO> noticeLists = apiTestCases.stream()
                        .map(apiTestCase -> {
                            ApiDefinitionCaseDTO apiDefinitionCaseDTO = new ApiDefinitionCaseDTO();
                            BeanUtils.copyBean(apiDefinitionCaseDTO, apiTestCase);
                            return apiDefinitionCaseDTO;
                        })
                        .toList();
                List<Map> resources = new ArrayList<>(JSON.parseArray(JSON.toJSONString(noticeLists), Map.class));
                commonNoticeSendService.sendNotice(NoticeConstants.TaskType.API_DEFINITION_TASK, event, resources, user, projectId);
            });
        }
    }
}
