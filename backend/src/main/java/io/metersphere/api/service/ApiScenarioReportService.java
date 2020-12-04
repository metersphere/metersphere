package io.metersphere.api.service;

import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.APIReportResult;
import io.metersphere.api.jmeter.TestResult;
import io.metersphere.base.domain.ApiTestReportDetail;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.i18n.Translator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.security.util.Cache;

import java.nio.charset.StandardCharsets;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioReportService {

    private static Cache cache = Cache.newHardMemoryCache(0, 3600 * 24);

    public void complete(TestResult result) {
        Object obj = cache.get(result.getTestId());
        if (obj == null) {
            MSException.throwException(Translator.get("api_report_is_null"));
        }
        APIReportResult report = (APIReportResult) obj;
        // report detail
        ApiTestReportDetail detail = new ApiTestReportDetail();
        detail.setReportId(result.getTestId());
        detail.setTestId(report.getTestId());
        detail.setContent(JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8));
        // report
        report.setUpdateTime(System.currentTimeMillis());
        if (!StringUtils.equals(report.getStatus(), APITestStatus.Debug.name())) {
            if (result.getError() > 0) {
                report.setStatus(APITestStatus.Error.name());
            } else {
                report.setStatus(APITestStatus.Success.name());
            }
        }
        report.setContent(new String(detail.getContent(), StandardCharsets.UTF_8));
        cache.put(report.getTestId(), report);
    }

    public void addResult(APIReportResult res) {
        cache.put(res.getTestId(), res);
    }

    /**
     * 获取零时执行报告
     *
     * @param testId
     */
    public APIReportResult getCacheResult(String testId) {
        Object res = cache.get(testId);
        if (res != null) {
            APIReportResult reportResult = (APIReportResult) res;
            if (!reportResult.getStatus().equals(APITestStatus.Running.name())) {
                cache.remove(testId);
            }
            return reportResult;
        }
        return null;
    }
}
