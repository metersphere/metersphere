package io.metersphere.commons.utils;


import io.metersphere.api.dto.ApiScenarioReportBaseInfoDTO;
import io.metersphere.base.domain.ApiScenarioReportResultWithBLOBs;
import io.metersphere.dto.RequestResult;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ResultConversionUtil {

    public static ApiScenarioReportResultWithBLOBs getApiScenarioReportResult(String reportId, RequestResult result) {
        String resourceId = result.getResourceId();
        ApiScenarioReportResultWithBLOBs report = newScenarioReportResult(reportId, resourceId);
        report.setTotalAssertions(Long.parseLong(result.getTotalAssertions() + StringUtils.EMPTY));
        report.setPassAssertions(Long.parseLong(result.getPassAssertions() + StringUtils.EMPTY));
        report.setErrorCode(result.getFakeErrorCode());
        report.setStatus(result.getStatus());
        report.setRequestTime(result.getEndTime() - result.getStartTime());
        LoggerUtil.info("报告ID [ " + reportId + " ] 执行请求：【 " + result.getName() + "】 入库存储");
        return report;
    }

    public static ApiScenarioReportResultWithBLOBs getApiScenarioReportResultBLOBs(String reportId, RequestResult result) {
        ApiScenarioReportResultWithBLOBs report = getApiScenarioReportResult(reportId, result);
        report.setBaseInfo(JSON.toJSONString(getBaseInfo(result)));
        report.setContent(JSON.toJSONString(result).getBytes(StandardCharsets.UTF_8));
        return report;
    }

    //记录基础信息
    private static ApiScenarioReportBaseInfoDTO getBaseInfo(RequestResult result) {
        ApiScenarioReportBaseInfoDTO baseInfoDTO = new ApiScenarioReportBaseInfoDTO();
        baseInfoDTO.setReqName(result.getName());
        baseInfoDTO.setReqSuccess(result.isSuccess());
        baseInfoDTO.setReqError(result.getError());
        baseInfoDTO.setReqStartTime(result.getStartTime());
        if (result.getResponseResult() != null) {
            baseInfoDTO.setRspCode(result.getResponseResult().getResponseCode());
            baseInfoDTO.setRspTime(result.getResponseResult().getResponseTime());
        }
        return baseInfoDTO;
    }

    public static ApiScenarioReportResultWithBLOBs newScenarioReportResult(String reportId, String resourceId) {
        ApiScenarioReportResultWithBLOBs report = new ApiScenarioReportResultWithBLOBs();
        report.setId(UUID.randomUUID().toString());
        report.setResourceId(resourceId);
        report.setReportId(reportId);
        report.setTotalAssertions(0L);
        report.setPassAssertions(0L);
        report.setCreateTime(System.currentTimeMillis());
        return report;
    }
}
