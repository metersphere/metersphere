package io.metersphere.api.service.scenario;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiScenarioExample;
import io.metersphere.api.dto.scenario.ApiScenarioAddRequest;
import io.metersphere.api.dto.scenario.ApiScenarioScheduleConfigRequest;
import io.metersphere.api.dto.scenario.ApiScenarioUpdateRequest;
import io.metersphere.api.job.ApiScenarioScheduleJob;
import io.metersphere.api.mapper.ApiScenarioMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.domain.ScheduleExample;
import io.metersphere.system.dto.sdk.ApiScenarioMessageDTO;
import io.metersphere.system.mapper.ScheduleMapper;
import io.metersphere.system.notice.NoticeModel;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.notice.utils.MessageTemplateUtils;
import io.metersphere.system.service.NoticeSendService;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApiScenarioNoticeService {

    @Resource
    private ApiScenarioMapper apiScenarioMapper;

    @Resource
    private ScheduleMapper scheduleMapper;
    @Resource
    private NoticeSendService noticeSendService;

    public void sendScheduleNotice(ApiScenarioScheduleConfigRequest request, String userId) {
        ScheduleExample example = new ScheduleExample();
        example.createCriteria().andResourceIdEqualTo(request.getScenarioId()).andJobEqualTo(ApiScenarioScheduleJob.class.getName());
        List<Schedule> schedules = scheduleMapper.selectByExample(example);
        Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
        String event = NoticeConstants.Event.OPEN;
        if (BooleanUtils.isFalse(request.isEnable())) {
            event = NoticeConstants.Event.CLOSE;
        }
        if (CollectionUtils.isNotEmpty(schedules)) {
            BeanMap beanMap = new BeanMap(schedules.getFirst());
            Map paramMap = new HashMap<>(beanMap);
            String template = defaultTemplateMap.get(NoticeConstants.TaskType.SCHEDULE_TASK + "_" + event);
            Map<String, String> defaultSubjectMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
            String subject = defaultSubjectMap.get(NoticeConstants.TaskType.SCHEDULE_TASK + "_" + event);
            NoticeModel noticeModel = NoticeModel.builder()
                    .operator(userId)
                    .context(template)
                    .subject(subject)
                    .paramMap(paramMap)
                    .event(event)
                    .excludeSelf(true)
                    .build();
            noticeSendService.send(NoticeConstants.TaskType.SCHEDULE_TASK, noticeModel);
        }
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
