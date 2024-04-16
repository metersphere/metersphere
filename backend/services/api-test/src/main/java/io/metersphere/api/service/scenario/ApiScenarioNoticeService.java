package io.metersphere.api.service.scenario;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiScenarioExample;
import io.metersphere.api.dto.scenario.ApiScenarioAddRequest;
import io.metersphere.api.dto.scenario.ApiScenarioUpdateRequest;
import io.metersphere.api.mapper.ApiScenarioMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.sdk.ApiScenarioMessageDTO;
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
public class ApiScenarioNoticeService {

    @Resource
    private ApiScenarioMapper apiScenarioMapper;

    @Resource
    private UserMapper userMapper;
    @Resource
    private CommonNoticeSendService commonNoticeSendService;


    public ApiScenarioMessageDTO addScenarioDTO(ApiScenarioAddRequest request) {
        ApiScenarioMessageDTO scenarioDTO = new ApiScenarioMessageDTO();
        BeanUtils.copyBean(scenarioDTO, request);
        return scenarioDTO;
    }

    public ApiScenarioMessageDTO getScenarioDTO(ApiScenarioUpdateRequest request) {
        ApiScenarioMessageDTO scenarioDTO = new ApiScenarioMessageDTO();
        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(request.getId());
        BeanUtils.copyBean(scenarioDTO, apiScenario);
        return scenarioDTO;
    }

    public ApiScenarioMessageDTO getScenarioDTO(String id) {
        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(id);
        ApiScenarioMessageDTO scenarioDTO = new ApiScenarioMessageDTO();
        BeanUtils.copyBean(scenarioDTO, apiScenario);
        return scenarioDTO;
    }

    public void batchSendNotice(List<String> ids, String userId, String projectId, String event) {
        if (CollectionUtils.isNotEmpty(ids)) {
            User user = userMapper.selectByPrimaryKey(userId);
            SubListUtils.dealForSubList(ids, 100, (subList) -> {
                ApiScenarioExample example = new ApiScenarioExample();
                example.createCriteria().andIdIn(subList);
                List<ApiScenario> apiScenarios = apiScenarioMapper.selectByExample(example);
                List<ApiScenarioMessageDTO> noticeLists = apiScenarios.stream()
                        .map(apiScenario -> {
                            ApiScenarioMessageDTO scenarioMessageDTO = new ApiScenarioMessageDTO();
                            BeanUtils.copyBean(scenarioMessageDTO, apiScenario);
                            return scenarioMessageDTO;
                        })
                        .toList();
                List<Map> resources = new ArrayList<>(JSON.parseArray(JSON.toJSONString(noticeLists), Map.class));
                commonNoticeSendService.sendNotice(NoticeConstants.TaskType.API_SCENARIO_TASK, event, resources, user, projectId);
            });
        }
    }

}
