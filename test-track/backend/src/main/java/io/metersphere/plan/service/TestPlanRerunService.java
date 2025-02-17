package io.metersphere.plan.service;

import io.metersphere.base.domain.TestPlanReport;
import io.metersphere.base.mapper.TestPlanReportMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.ReportTypeConstants;
import io.metersphere.dto.TestPlanRerunParametersDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.plan.constant.ApiReportStatus;
import io.metersphere.service.remote.api.ApiRerunService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class TestPlanRerunService {
    @Resource
    private TestPlanReportMapper testPlanReportMapper;
    @Resource
    private ApiRerunService apiRerunService;

    public String rerun(TestPlanRerunParametersDTO parametersDTO) {
        String rerunResult = ApiReportStatus.ERROR.name();
        if (parametersDTO == null || StringUtils.isEmpty(parametersDTO.getType()) || StringUtils.isEmpty(parametersDTO.getReportId())) {
            return Translator.get("report_warning");
        }
        if (StringUtils.equalsAnyIgnoreCase(parametersDTO.getType(), ReportTypeConstants.TEST_PLAN.name())) {
            TestPlanReport testPlanReport = testPlanReportMapper.selectByPrimaryKey(parametersDTO.getReportId());
            if (StringUtils.equalsAnyIgnoreCase(testPlanReport.getStatus(), APITestStatus.Rerunning.name(), APITestStatus.Running.name())) {
                return Translator.get("rerun_warning");
            }
            parametersDTO.setTestPlanReport(testPlanReport);
            rerunResult = apiRerunService.rerun(parametersDTO);
            if (StringUtils.equalsIgnoreCase(rerunResult, ApiReportStatus.SUCCESS.name())) {
                testPlanReport.setStatus(APITestStatus.Rerunning.name());
                testPlanReportMapper.updateByPrimaryKey(testPlanReport);
            }
        } else {
            return Translator.get("rerun_warning");
        }
        if (!StringUtils.equalsIgnoreCase(rerunResult, ApiReportStatus.SUCCESS.name())) {
            return Translator.get("rerun_warning");
        }
        return rerunResult;
    }

}
