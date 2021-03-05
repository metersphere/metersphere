package io.metersphere.notice.service;

import io.metersphere.api.dto.APIReportResult;
import io.metersphere.base.domain.ApiTestReportDetail;
import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.base.domain.Schedule;
import io.metersphere.base.mapper.ApiTestReportDetailMapper;
import io.metersphere.base.mapper.LoadTestReportMapper;
import io.metersphere.base.mapper.ext.ExtApiTestReportMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestMapper;
import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.performance.request.QueryTestPlanRequest;
import io.metersphere.service.ScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;


@Service
public class ApiAndPerformanceHelper {
    @Resource
    private ExtLoadTestMapper extLoadTestMapper;
    @Resource
    private ExtApiTestReportMapper extApiTestReportMapper;
    @Resource
    private ApiTestReportDetailMapper apiTestReportDetailMapper;
    @Resource
    private ScheduleService scheduleService;
    @Resource
    private LoadTestReportMapper loadTestReportMapper;

    public APIReportResult getApi(String reportId) {
        APIReportResult result = extApiTestReportMapper.get(reportId);
        ApiTestReportDetail detail = apiTestReportDetailMapper.selectByPrimaryKey(reportId);
        if (detail != null) {
            result.setContent(new String(detail.getContent(), StandardCharsets.UTF_8));
        }
        return result;
    }

    public LoadTestDTO getPerformance(String testId) {
        QueryTestPlanRequest request = new QueryTestPlanRequest();
        request.setId(testId);
        List<LoadTestDTO> testDTOS = extLoadTestMapper.list(request);
        if (!CollectionUtils.isEmpty(testDTOS)) {
            LoadTestDTO loadTestDTO = testDTOS.get(0);
            Schedule schedule = scheduleService.getScheduleByResource(loadTestDTO.getId(), ScheduleGroup.PERFORMANCE_TEST.name());
            loadTestDTO.setSchedule(schedule);
            return loadTestDTO;
        }
        return null;
    }

    public LoadTestReportWithBLOBs getLoadTestReport(String id) {
        return loadTestReportMapper.selectByPrimaryKey(id);
    }
}

