package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiScenarioExample;
import io.metersphere.api.dto.scenario.ApiScenarioBatchRequest;
import io.metersphere.api.mapper.ApiScenarioMapper;
import io.metersphere.api.service.scenario.ApiScenarioService;
import io.metersphere.sdk.util.SubListUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ApiScenarioNoticeService {

    @Resource
    private ApiScenarioService apiScenarioService;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;


    public List<ApiScenario> getBatchOptionScenarios(ApiScenarioBatchRequest request) {
        List<String> ids = apiScenarioService.doSelectIds(request, false);
        return handleBatchNotice(ids);
    }

    private List<ApiScenario> handleBatchNotice(List<String> ids) {
        List<ApiScenario> dtoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            SubListUtils.dealForSubList(ids, 100, (subList) -> {
                ApiScenarioExample example = new ApiScenarioExample();
                example.createCriteria().andIdIn(subList);
                dtoList.addAll(apiScenarioMapper.selectByExample(example));
            });
        }
        return dtoList;
    }

}
