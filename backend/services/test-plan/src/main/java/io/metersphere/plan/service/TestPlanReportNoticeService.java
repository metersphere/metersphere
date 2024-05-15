package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlanReport;
import io.metersphere.plan.mapper.ExtTestPlanReportMapper;
import io.metersphere.plan.mapper.TestPlanReportMapper;
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
public class TestPlanReportNoticeService {

    @Resource
    private TestPlanReportMapper testPlanReportMapper;
    @Resource
    private CommonNoticeSendService commonNoticeSendService;
    @Resource
    private ExtTestPlanReportMapper extTestPlanReportMapper;

    public ApiReportMessageDTO getDto(String id) {
        TestPlanReport report = testPlanReportMapper.selectByPrimaryKey(id);
        ApiReportMessageDTO reportMessageDTO = new ApiReportMessageDTO();
        reportMessageDTO.setId(report.getId());
        reportMessageDTO.setName(report.getName());
        return reportMessageDTO;
    }

    public void batchSendNotice(List<String> ids, User user, String projectId, String event) {
        if (CollectionUtils.isNotEmpty(ids)) {
            SubListUtils.dealForSubList(ids, 100, (subList) -> {
                List<ApiReportMessageDTO> noticeLists = extTestPlanReportMapper.getNoticeList(subList);
                List<Map> resources = new ArrayList<>(JSON.parseArray(JSON.toJSONString(noticeLists), Map.class));
                commonNoticeSendService.sendNotice(NoticeConstants.TaskType.TEST_PLAN_REPORT_TASK, event, resources, user, projectId);
            });
        }
    }
}
