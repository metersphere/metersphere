package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.ErrorReportLibraryParseDTO;
import io.metersphere.base.domain.ApiScenarioReportResult;
import io.metersphere.base.mapper.ApiScenarioReportResultMapper;
import io.metersphere.commons.constants.ExecuteResult;
import io.metersphere.commons.utils.ErrorReportLibraryUtil;
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

    public void uiSave(String reportId, List<RequestResult> queue) {
        if (CollectionUtils.isNotEmpty(queue)) {
            queue.forEach(item -> {
                String header = item.getResponseResult().getHeaders();
                if (StringUtils.isNoneBlank(header)) {
                    JSONObject jsonObject = JSONObject.parseObject(header);
                    for (String resourceId : jsonObject.keySet()) {
                        apiScenarioReportResultMapper.insert(this.newUiScenarioReportResult(reportId, resourceId, jsonObject.getString(resourceId)));
                    }
                }
            });
        }
    }

    private ApiScenarioReportResult newUiScenarioReportResult(String reportId, String resourceId, String value) {
        ApiScenarioReportResult report = new ApiScenarioReportResult();
        report.setId(UUID.randomUUID().toString());
        report.setResourceId(resourceId);
        report.setReportId(reportId);
        report.setTotalAssertions(0L);
        report.setPassAssertions(0L);
        report.setCreateTime(System.currentTimeMillis());
        String status = value.equalsIgnoreCase("OK") ? ExecuteResult.Success.name() : ExecuteResult.Error.name();
        report.setStatus(status);
        report.setContent(value.getBytes(StandardCharsets.UTF_8));
        return report;
    }

    private ApiScenarioReportResult newApiScenarioReportResult(String reportId, RequestResult result) {
        ApiScenarioReportResult report = new ApiScenarioReportResult();
        //解析误报内容
        ErrorReportLibraryParseDTO errorCodeDTO = ErrorReportLibraryUtil.parseAssertions(result);
        report.setId(UUID.randomUUID().toString());
        String resourceId = result.getResourceId();
        report.setResourceId(resourceId);
        report.setReportId(reportId);
        report.setTotalAssertions(Long.parseLong(result.getTotalAssertions() + ""));
        report.setPassAssertions(Long.parseLong(result.getPassAssertions() + ""));
        report.setCreateTime(System.currentTimeMillis());
        String status = result.getError() == 0 ? ExecuteResult.Success.name() : ExecuteResult.Error.name();
        if (CollectionUtils.isNotEmpty(errorCodeDTO.getErrorCodeList())) {
            status = ExecuteResult.errorReportResult.name();
            report.setErrorCode(errorCodeDTO.getErrorCodeStr());
        }
        report.setStatus(status);
        report.setRequestTime(result.getEndTime() - result.getStartTime());
        report.setContent(JSON.toJSONString(result).getBytes(StandardCharsets.UTF_8));
        return report;
    }
}
