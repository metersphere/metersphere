package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.ApiScenarioReportResult;
import io.metersphere.base.mapper.ApiScenarioReportResultMapper;
import io.metersphere.dto.RequestResult;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioReportResultService {
    @Resource
    private ApiScenarioReportResultMapper apiScenarioReportResultMapper;

    public void save(String reportId, List<RequestResult> queue) {
        if (CollectionUtils.isNotEmpty(queue)) {
            queue.forEach(item -> {
                // 事物控制器出来的结果特殊处理
                if (StringUtils.isNotEmpty(item.getName()) && item.getName().startsWith("Transaction=") && CollectionUtils.isEmpty(item.getSubRequestResults())) {
                    LoggerUtil.debug("合并事物请求暂不入库");
                } else {
                    apiScenarioReportResultMapper.insert(this.newApiScenarioReportResult(reportId, item));
                }
            });
        }
    }

    private ApiScenarioReportResult newApiScenarioReportResult(String reportId, RequestResult result) {
        ApiScenarioReportResult report = new ApiScenarioReportResult();
        report.setId(UUID.randomUUID().toString());
        result.setEndTime(System.currentTimeMillis());
        if (result.getResponseResult() != null) {
            long time = result.getEndTime() - result.getStartTime();
            if (time > 0) {
                result.getResponseResult().setResponseTime(time);
            } else {
                result.setEndTime(result.getEndTime());
            }
        }
        String resourceId = result.getResourceId();
        report.setResourceId(resourceId);
        report.setReportId(reportId);
        report.setTotalAssertions(Long.parseLong(result.getTotalAssertions() + ""));
        report.setPassAssertions(Long.parseLong(result.getPassAssertions() + ""));
        report.setCreateTime(System.currentTimeMillis());
        report.setStatus(result.getError() == 0 ? "Success" : "Error");
        report.setRequestTime(result.getEndTime() - result.getStartTime());
        report.setContent(JSON.toJSONString(result).getBytes(StandardCharsets.UTF_8));
        return report;
    }
}
