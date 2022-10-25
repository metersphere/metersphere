package io.metersphere.api.jmeter.utils;

import io.metersphere.commons.constants.CommonConstants;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.dto.RequestResult;
import io.metersphere.dto.ResultDTO;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.utils.RetryResultUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportStatusUtil {
    public static List<RequestResult> filterRetryResults(List<RequestResult> results) {
        List<RequestResult> list = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(results)) {
            Map<String, List<RequestResult>> resultMap = results.stream().collect(Collectors.groupingBy(RequestResult::getResourceId));
            resultMap.forEach((k, v) -> {
                if (CollectionUtils.isNotEmpty(v)) {
                    // 校验是否含重试结果
                    List<RequestResult> isRetryResults = v.stream().filter(c -> StringUtils.isNotEmpty(c.getName()) && c.getName().startsWith(RetryResultUtil.RETRY_CN)).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(isRetryResults)) {
                        list.add(isRetryResults.get(isRetryResults.size() - 1));
                    } else {
                        // 成功的结果
                        list.addAll(v);
                    }
                }
            });
        }
        return list;
    }

    /**
     * 返回正确的报告状态
     *
     * @param dto jmeter返回
     * @return
     */
    public static String getStatus(ResultDTO dto, String status) {
        if (StringUtils.equals(status, ApiReportStatus.ERROR.name())) {
            return status;
        }
        if (MapUtils.isNotEmpty(dto.getArbitraryData()) && dto.getArbitraryData().containsKey(CommonConstants.REPORT_STATUS)) {
            // 资源池执行整体传输失败，单条传输内容，获取资源池执行统计的状态
            return String.valueOf(dto.getArbitraryData().get(CommonConstants.REPORT_STATUS));
        }
        // 过滤掉重试结果后进行统计
        List<RequestResult> requestResults = filterRetryResults(dto.getRequestResults());
        long errorSize = requestResults.stream().filter(requestResult ->
                StringUtils.equalsIgnoreCase(requestResult.getStatus(), ApiReportStatus.ERROR.name())).count();
        // 误报
        long errorReportResultSize = dto.getRequestResults().stream(). filter(
                requestResult -> StringUtils.equalsIgnoreCase(requestResult.getStatus(), ApiReportStatus.FAKE_ERROR.name())).count();
        // 默认状态
        status = dto.getRequestResults().isEmpty() && StringUtils.isEmpty(status)
                ? ApiReportStatus.PENDING.name()
                : StringUtils.defaultIfEmpty(status, ApiReportStatus.SUCCESS.name());
        if (errorSize > 0) {
            status = ApiReportStatus.ERROR.name();
        } else if (errorReportResultSize > 0) {
            status = ApiReportStatus.FAKE_ERROR.name();
        }
        // 超时状态
        if (dto != null && dto.getArbitraryData() != null
                && dto.getArbitraryData().containsKey(ApiReportStatus.TIMEOUT.name())
                && (Boolean) dto.getArbitraryData().get(ApiReportStatus.TIMEOUT.name())) {
            LoggerUtil.info("资源 " + dto.getTestId() + " 执行超时", dto.getReportId());
            status = ApiReportStatus.ERROR.name();
        }
        return status;
    }

    public static String getStatus(ResultDTO dto) {
        if (MapUtils.isNotEmpty(dto.getArbitraryData()) && dto.getArbitraryData().containsKey(CommonConstants.LOCAL_STATUS_KEY)) {
            // 本地执行状态
            return String.valueOf(dto.getArbitraryData().get(CommonConstants.LOCAL_STATUS_KEY));
        }
        if (MapUtils.isNotEmpty(dto.getArbitraryData()) && dto.getArbitraryData().containsKey(CommonConstants.REPORT_STATUS)) {
            // 资源池执行整体传输失败，单条传输内容，获取资源池执行统计的状态
            return String.valueOf(dto.getArbitraryData().get(CommonConstants.REPORT_STATUS));
        }
        return getStatus(dto, "");
    }
}
