package io.metersphere.reportstatistics.service.remote.performance;

import io.metersphere.commons.utils.LogUtil;
import io.metersphere.reportstatistics.dto.TestCaseCountChartResult;
import io.metersphere.reportstatistics.dto.TestCaseCountRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class PerformanceRemoteService extends PerformanceTestBaseService {
    private static final String URL_COUNT_PERFORMANCE_CASE_BY_REQUEST = "performance/count";

    public List<TestCaseCountChartResult> countTestCaseByRequest(TestCaseCountRequest request) {
        List<TestCaseCountChartResult> returnList = new ArrayList<>(0);
        try {
            returnList = microService.postForDataArray(serviceName, URL_COUNT_PERFORMANCE_CASE_BY_REQUEST, request,
                    TestCaseCountChartResult.class);
        } catch (Exception e) {
            LogUtil.error("调用微服务失败!", e);
        }
        return returnList;
    }
}