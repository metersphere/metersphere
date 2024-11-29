package io.metersphere.plan.service;

import io.metersphere.plan.mapper.ExtTestPlanReportMapper;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.service.BaseCleanUpReport;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static io.metersphere.sdk.util.ShareUtil.getCleanDate;

@Component
@Transactional(rollbackFor = Exception.class)
public class CleanupTestPlanReportServiceImpl implements BaseCleanUpReport {

    @Resource
    private TestPlanReportService testPlanReportService;
    @Resource
    private ExtTestPlanReportMapper extTestPlanReportMapper;

    @Override
    public void cleanReport(Map<String, String> map, String projectId) {
        LogUtils.info("清理当前项目[" + projectId + "]测试计划报告");
        String expr = map.get(ProjectApplicationType.TEST_PLAN.TEST_PLAN_CLEAN_REPORT.name());
        long timeMills = getCleanDate(expr);
        List<String> ids = extTestPlanReportMapper.selectReportIdByProjectIdAndTime(timeMills, projectId);
        LogUtils.info("清理当前项目[" + projectId + "]测试计划报告, 共[" + ids.size() + "]条");
        testPlanReportService.cleanAndDeleteReport(ids);
        LogUtils.info("清理当前项目[" + projectId + "]测试计划报告结束!");
    }
}
