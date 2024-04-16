package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiReport;
import io.metersphere.api.mapper.ApiReportMapper;
import io.metersphere.api.mapper.ExtApiReportMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.sdk.ApiReportMessageDTO;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.service.CommonNoticeSendService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ApiReportNoticeService {

    @Resource
    private ApiReportMapper apiReportMapper;
    @Resource
    private CommonNoticeSendService commonNoticeSendService;
    @Resource
    private ExtApiReportMapper extApiReportMapper;

    public ApiReportMessageDTO getDto(String id) {
        ApiReport apiReport = apiReportMapper.selectByPrimaryKey(id);
        ApiReportMessageDTO reportMessageDTO = new ApiReportMessageDTO();
        reportMessageDTO.setId(apiReport.getId());
        reportMessageDTO.setName(apiReport.getName());
        return reportMessageDTO;
    }

    public void batchSendNotice(List<String> ids, User user, String projectId, String event) {
        if (CollectionUtils.isNotEmpty(ids)) {
            SubListUtils.dealForSubList(ids, 100, (subList) -> {
                List<ApiReportMessageDTO> noticeLists = extApiReportMapper.getNoticeList(subList);
                List<Map> resources = new ArrayList<>(JSON.parseArray(JSON.toJSONString(noticeLists), Map.class));
                commonNoticeSendService.sendNotice(NoticeConstants.TaskType.API_REPORT_TASK, event, resources, user, projectId);
            });
        }
    }
}
