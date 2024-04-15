package io.metersphere.api.service.scenario;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiScenarioExample;
import io.metersphere.api.dto.scenario.*;
import io.metersphere.api.job.ApiScenarioScheduleJob;
import io.metersphere.api.mapper.ApiScenarioMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.domain.ScheduleExample;
import io.metersphere.system.dto.sdk.ApiScenarioMessageDTO;
import io.metersphere.system.mapper.ScheduleMapper;
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

    @Resource
    private ScheduleMapper scheduleMapper;

    public Schedule getScheduleNotice(ApiScenarioScheduleConfigRequest request) {
        ScheduleExample example = new ScheduleExample();
        example.createCriteria().andResourceIdEqualTo(request.getScenarioId()).andJobEqualTo(ApiScenarioScheduleJob.class.getName());
        List<Schedule> schedules = scheduleMapper.selectByExample(example);
        return CollectionUtils.isEmpty(schedules) ? null : schedules.get(0);
    }

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

    public ApiScenarioMessageDTO getScenarioDTO(ApiScenarioAddRequest request) {
        ApiScenarioMessageDTO scenarioDTO = new ApiScenarioMessageDTO();
        BeanUtils.copyBean(scenarioDTO, request);
        return scenarioDTO;
    }

    public ApiScenarioMessageDTO getScenarioDTO(ApiScenarioUpdateRequest request) {
        ApiScenarioMessageDTO scenarioDTO = new ApiScenarioMessageDTO();
        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(request.getId());
        BeanUtils.copyBean(scenarioDTO, apiScenario);
        BeanUtils.copyBean(scenarioDTO, request);
        return scenarioDTO;
    }

    public ApiScenarioMessageDTO getScenarioDTO(String id) {
        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(id);
        ApiScenarioMessageDTO scenarioDTO = new ApiScenarioMessageDTO();
        BeanUtils.copyBean(scenarioDTO, apiScenario);
        return scenarioDTO;
    }


}
