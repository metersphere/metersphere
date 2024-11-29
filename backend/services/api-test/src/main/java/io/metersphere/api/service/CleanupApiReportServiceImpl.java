package io.metersphere.api.service;

import io.metersphere.api.mapper.ExtApiReportMapper;
import io.metersphere.api.mapper.ExtApiScenarioReportMapper;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.system.service.BaseCleanUpReport;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static io.metersphere.sdk.util.ShareUtil.getCleanDate;

@Component
@Transactional(rollbackFor = Exception.class)
public class CleanupApiReportServiceImpl implements BaseCleanUpReport {

    @Resource
    private ExtApiReportMapper extApiReportMapper;
    @Resource
    private ExtApiScenarioReportMapper extApiScenarioReportMapper;
    @Resource
    private CleanupApiReportDetailServiceImpl cleanupApiReportDetailService;

    @Override
    public void cleanReport(Map<String, String> map, String projectId) {
        LogUtils.info("清理当前项目[" + projectId + "]相关接口测试报告");
        String expr = map.get(ProjectApplicationType.API.API_CLEAN_REPORT.name());
        long timeMills = getCleanDate(expr);
        List<String> apiReportIds = extApiReportMapper.selectApiReportByProjectIdAndTime(timeMills, projectId);
        List<String> scenarioReportIds = extApiScenarioReportMapper.selectApiReportByProjectIdAndTime(timeMills, projectId);
        LogUtils.info("清理当前项目[" + projectId + "]相关接口测试报告, 共[" + (apiReportIds.size() + scenarioReportIds.size()) + "]条");
        if (CollectionUtils.isNotEmpty(apiReportIds)) {
            SubListUtils.dealForSubList(apiReportIds, 100, (subIds) -> {
                cleanupApiReportDetailService.cleanApiReportOrDetail(subIds);
            });
        }
        if (CollectionUtils.isNotEmpty(scenarioReportIds)) {
            SubListUtils.dealForSubList(scenarioReportIds, 100, (subIds) -> {
                cleanupApiReportDetailService.cleanScenarioReportOrDetail(subIds);
            });
        }
        LogUtils.info("清理当前项目[" + projectId + "]相关接口测试报告结束!");
    }
}
