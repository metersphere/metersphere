package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiDefinitionMock;
import io.metersphere.api.domain.ApiDefinitionMockExample;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockAddRequest;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockUpdateRequest;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.api.mapper.ApiDefinitionMockMapper;
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
public class ApiDefinitionMockNoticeService {

    @Resource
    private ApiDefinitionMockMapper apiDefinitionMockMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;

    @Resource
    private UserMapper userMapper;
    @Resource
    private CommonNoticeSendService commonNoticeSendService;

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

    /**
     * 根据Mock ID获取API Mock DTO对象
     *
     * @param id Mock的唯一标识
     * @return 对应的DTO对象，如果找不到相关数据则返回一个空DTO对象
     */
    public ApiDefinitionCaseDTO getApiMockDTO(String id) {
        ApiDefinitionCaseDTO mockDTO = new ApiDefinitionCaseDTO();

        // 获取Mock对象
        ApiDefinitionMock apiDefinitionMock = apiDefinitionMockMapper.selectByPrimaryKey(id);
        if (apiDefinitionMock == null) {
            return mockDTO;
        }

        // 获取并复制API定义信息
        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(apiDefinitionMock.getApiDefinitionId());
        if (apiDefinition != null) {
            BeanUtils.copyBean(mockDTO, apiDefinition);
        }

        // 设置Mock名称
        mockDTO.setMockName(apiDefinitionMock.getName());
        return mockDTO;
    }

    public void batchSendNotice(List<String> ids, String userId, String projectId, String event) {
        if (CollectionUtils.isNotEmpty(ids)) {
            User user = userMapper.selectByPrimaryKey(userId);
            SubListUtils.dealForSubList(ids, 100, (subList) -> {
                ApiDefinitionMockExample example = new ApiDefinitionMockExample();
                example.createCriteria().andIdIn(subList);
                List<ApiDefinitionMock> apiMocks = apiDefinitionMockMapper.selectByExample(example);
                List<ApiDefinitionCaseDTO> noticeLists = apiMocks.stream()
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
