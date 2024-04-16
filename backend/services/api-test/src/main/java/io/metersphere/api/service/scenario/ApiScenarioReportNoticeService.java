package io.metersphere.api.service.scenario;

import io.metersphere.api.domain.ApiScenarioReport;
import io.metersphere.api.mapper.ApiScenarioReportMapper;
import io.metersphere.api.mapper.ExtApiScenarioReportMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.sdk.ApiReportMessageDTO;
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
public class ApiScenarioReportNoticeService {

    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private CommonNoticeSendService commonNoticeSendService;
    @Resource
    private ExtApiScenarioReportMapper extApiScenarioReportMapper;

    public ApiReportMessageDTO getDto(String id) {
        ApiScenarioReport scenarioReport = apiScenarioReportMapper.selectByPrimaryKey(id);
        ApiReportMessageDTO reportMessageDTO = new ApiReportMessageDTO();
        reportMessageDTO.setId(scenarioReport.getId());
        reportMessageDTO.setName(scenarioReport.getName());
        return reportMessageDTO;
    }

    public void batchSendNotice(List<String> ids, User user, String projectId, String event) {
        if (CollectionUtils.isNotEmpty(ids)) {
            SubListUtils.dealForSubList(ids, 100, (subList) -> {
                List<ApiReportMessageDTO> noticeLists = extApiScenarioReportMapper.getNoticeList(subList);
                List<Map> resources = new ArrayList<>(JSON.parseArray(JSON.toJSONString(noticeLists), Map.class));
                commonNoticeSendService.sendNotice(NoticeConstants.TaskType.API_REPORT_TASK, event, resources, user, projectId);
            });
        }
    }
}
