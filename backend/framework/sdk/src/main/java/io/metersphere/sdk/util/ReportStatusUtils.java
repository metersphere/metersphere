package io.metersphere.sdk.util;

import io.metersphere.sdk.constants.ApiReportStatus;
import io.metersphere.sdk.dto.api.result.ProcessResultDTO;
import io.metersphere.sdk.dto.api.result.RequestResult;
import io.metersphere.sdk.dto.api.result.TaskResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportStatusUtils {
    public static List<RequestResult> filterRetryResults(List<RequestResult> results) {
        List<RequestResult> list = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(results)) {
            Map<String, List<RequestResult>> resultMap = results.stream().collect(Collectors.groupingBy(RequestResult::getResourceId));
            resultMap.forEach((k, v) -> {
                if (CollectionUtils.isNotEmpty(v)) {
                    // 校验是否含重试结果
                    List<RequestResult> isRetryResults = v.stream().filter(c ->
                                    StringUtils.isNotEmpty(c.getName()) && c.getName().startsWith(RetryResultUtils.RETRY_CN))
                            .collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(isRetryResults)) {
                        list.add(isRetryResults.getLast());
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
     */
    public static ProcessResultDTO getStatus(TaskResult dto, ProcessResultDTO process) {
        process.computerSuccess(dto.getRequestResults().stream()
                .filter(requestResult -> StringUtils.equalsIgnoreCase(requestResult.getStatus(), ApiReportStatus.SUCCESS.name())).count());

        process.computerFakeError(dto.getRequestResults().stream()
                .filter(requestResult -> StringUtils.equalsIgnoreCase(requestResult.getStatus(), ApiReportStatus.FAKE_ERROR.name())).count());

        process.computerError(dto.getRequestResults().stream()
                .filter(requestResult -> StringUtils.equalsIgnoreCase(requestResult.getStatus(), ApiReportStatus.ERROR.name())).count());

        process.computerPending(dto.getRequestResults().stream()
                .filter(requestResult -> StringUtils.equalsIgnoreCase(requestResult.getStatus(), ApiReportStatus.PENDING.name())).count());

        // 断言计算
        dto.getRequestResults().forEach(requestResult -> {
            process.computerAssertion(requestResult.getAssertionTotal());
            process.computerSuccessAssertion(requestResult.getPassAssertionsTotal());
        });

        // 各种通过率
        process.computerRequestExecutionRate();
        process.computerRequestPassRate();
        process.computerAssertionPassRate();

        if (StringUtils.equals(process.getStatus(), ApiReportStatus.ERROR.name())) {
            return process;
        }
        if (ObjectUtils.isNotEmpty(dto.getProcessResultDTO())) {
            // 资源池执行整体传输失败，单条传输内容，获取资源池执行统计的状态
            process.setStatus(dto.getProcessResultDTO().getStatus());
        }
        // 过滤掉重试结果后进行统计
        long errorSize = process.getErrorCount();
        // 误报
        long errorReportResultSize = process.getFakeErrorCount();
        // 默认状态
        String status = dto.getRequestResults().isEmpty() && StringUtils.isEmpty(process.getStatus())
                ? ApiReportStatus.PENDING.name()
                : StringUtils.defaultIfEmpty(process.getStatus(), ApiReportStatus.SUCCESS.name());

        if (errorSize > 0) {
            status = ApiReportStatus.ERROR.name();
        } else if (errorReportResultSize > 0) {
            status = ApiReportStatus.FAKE_ERROR.name();
        }
        process.setStatus(status);
        process.computerTotal();
        return process;
    }

    public static ProcessResultDTO computedProcess(TaskResult dto) {
        ProcessResultDTO result = dto.getProcessResultDTO();
        result = getStatus(dto, result);
        if (result.getTotal() > 0 && result.getTotal() == result.getSuccessCount()) {
            result.setStatus(ApiReportStatus.SUCCESS.name());
        }
        return result;
    }
}
