package io.metersphere.api.service;

import io.metersphere.api.domain.*;
import io.metersphere.api.mapper.*;
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
public class CleanupApiReportServiceImpl implements BaseCleanUpReport {

    @Resource
    private ApiReportMapper apiReportMapper;
    @Resource
    private ExtApiReportMapper extApiReportMapper;
    @Resource
    private ApiReportStepMapper apiReportStepMapper;
    @Resource
    private ApiReportDetailMapper apiReportDetailMapper;
    @Resource
    private ApiReportLogMapper apiReportLogMapper;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private ExtApiScenarioReportMapper extApiScenarioReportMapper;
    @Resource
    private ApiScenarioReportStepMapper apiScenarioReportStepMapper;
    @Resource
    private ApiScenarioReportDetailMapper apiScenarioReportDetailMapper;
    @Resource
    private ApiScenarioReportLogMapper apiScenarioReportLogMapper;

    @Override
    public void cleanReport(Map<String, String> map, String projectId) {
        LogUtils.info("清理当前项目[" + projectId + "]相关接口测试报告");
        String expr = map.get(ProjectApplicationType.API.API_CLEAN_REPORT.name());
        long timeMills = getCleanDate(expr);
        int apiReportCount = extApiReportMapper.selectApiReportByTime(timeMills, projectId);
        while (apiReportCount > 0) {
            List<String> ids = extApiReportMapper.selectApiReportByProjectIdAndTime(timeMills, projectId);
            ApiReportExample reportExample = new ApiReportExample();
            reportExample.createCriteria().andIdIn(ids);
            ApiReport report = new ApiReport();
            report.setDeleted(true);
            apiReportMapper.updateByExampleSelective(report, reportExample);
            deleteApiReport(ids);
            apiReportCount = extApiReportMapper.selectApiReportByTime(timeMills, projectId);
        }
        int scenarioReportCount = extApiScenarioReportMapper.selectScenarioReportByTime(timeMills, projectId);
        while (scenarioReportCount > 0) {
            List<String> ids = extApiScenarioReportMapper.selectApiReportByProjectIdAndTime(timeMills, projectId);
            ApiScenarioReportExample reportExample = new ApiScenarioReportExample();
            reportExample.createCriteria().andIdIn(ids);
            ApiScenarioReport report = new ApiScenarioReport();
            report.setDeleted(true);
            apiScenarioReportMapper.updateByExampleSelective(report, reportExample);
            deleteScenarioReport(ids);
            scenarioReportCount = extApiScenarioReportMapper.selectScenarioReportByTime(timeMills, projectId);
        }
    }

    private void deleteApiReport(List<String> ids) {
        ApiReportStepExample stepExample = new ApiReportStepExample();
        stepExample.createCriteria().andReportIdIn(ids);
        apiReportStepMapper.deleteByExample(stepExample);
        ApiReportDetailExample detailExample = new ApiReportDetailExample();
        detailExample.createCriteria().andReportIdIn(ids);
        apiReportDetailMapper.deleteByExample(detailExample);
        ApiReportLogExample logExample = new ApiReportLogExample();
        logExample.createCriteria().andReportIdIn(ids);
        apiReportLogMapper.deleteByExample(logExample);
    }

    private void deleteScenarioReport(List<String> ids) {
        ApiScenarioReportStepExample stepExample = new ApiScenarioReportStepExample();
        stepExample.createCriteria().andReportIdIn(ids);
        apiScenarioReportStepMapper.deleteByExample(stepExample);
        ApiScenarioReportDetailExample detailExample = new ApiScenarioReportDetailExample();
        detailExample.createCriteria().andReportIdIn(ids);
        apiScenarioReportDetailMapper.deleteByExample(detailExample);
        ApiScenarioReportLogExample logExample = new ApiScenarioReportLogExample();
        logExample.createCriteria().andReportIdIn(ids);
        apiScenarioReportLogMapper.deleteByExample(logExample);
    }

}
