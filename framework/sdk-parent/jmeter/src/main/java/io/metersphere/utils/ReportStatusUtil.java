package io.metersphere.utils;

import io.metersphere.dto.RequestResult;
import io.metersphere.dto.ResultDTO;
import io.metersphere.enums.ApiReportStatus;
import io.metersphere.vo.ResultVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportStatusUtil {
    public static final String LOCAL_STATUS_KEY = "LOCAL_STATUS_KEY";
    public static final String REPORT_STATUS = "REPORT_STATUS";

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
    public static ResultVO getStatus(ResultDTO dto, ResultVO resultVO) {
        resultVO.computerTotal(dto.getRequestResults().size());
        resultVO.computerSuccess(dto.getRequestResults().stream().filter(requestResult -> StringUtils.equalsIgnoreCase(requestResult.getStatus(), ApiReportStatus.SUCCESS.name())).count());
        if (StringUtils.equals(resultVO.getStatus(), ApiReportStatus.ERROR.name())) {
            return resultVO;
        }
        if (MapUtils.isNotEmpty(dto.getArbitraryData()) && dto.getArbitraryData().containsKey(REPORT_STATUS)) {
            // 资源池执行整体传输失败，单条传输内容，获取资源池执行统计的状态
            resultVO.setStatus(JsonUtils.convertValue(dto.getArbitraryData().get(REPORT_STATUS), ResultVO.class).getStatus());
        }
        // 过滤掉重试结果后进行统计
        List<RequestResult> requestResults = filterRetryResults(dto.getRequestResults());
        long errorSize = requestResults.stream().filter(requestResult ->
                StringUtils.equalsIgnoreCase(requestResult.getStatus(), ApiReportStatus.ERROR.name())).count();
        // 误报
        long errorReportResultSize = dto.getRequestResults().stream().filter(
                requestResult -> StringUtils.equalsIgnoreCase(requestResult.getStatus(), ApiReportStatus.FAKE_ERROR.name())).count();
        // 默认状态
        String status = dto.getRequestResults().isEmpty() && StringUtils.isEmpty(resultVO.getStatus())
                ? ApiReportStatus.PENDING.name()
                : StringUtils.defaultIfEmpty(resultVO.getStatus(), ApiReportStatus.SUCCESS.name());

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
        resultVO.setStatus(status);
        return resultVO;
    }

    public static ResultVO computedProcess(ResultDTO dto) {
        ResultVO result = new ResultVO();
        if (MapUtils.isNotEmpty(dto.getArbitraryData()) && dto.getArbitraryData().containsKey(LOCAL_STATUS_KEY)) {
            // 本地执行状态
            result = JsonUtils.convertValue(dto.getArbitraryData().get(LOCAL_STATUS_KEY), ResultVO.class);
        }
        if (MapUtils.isNotEmpty(dto.getArbitraryData()) && dto.getArbitraryData().containsKey(REPORT_STATUS)) {
            // 资源池执行整体传输失败，单条传输内容，获取资源池执行统计的状态
            result = JsonUtils.convertValue(dto.getArbitraryData().get(REPORT_STATUS), ResultVO.class);
        }
        result = getStatus(dto, result);
        if (result != null && result.getScenarioTotal() > 0 && result.getScenarioTotal() == result.getScenarioSuccess()) {
            result.setStatus(ApiReportStatus.SUCCESS.name());
        }
        return result;
    }
}
