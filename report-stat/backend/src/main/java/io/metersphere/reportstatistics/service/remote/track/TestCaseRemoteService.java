package io.metersphere.reportstatistics.service.remote.track;

import io.metersphere.commons.utils.LogUtil;
import io.metersphere.reportstatistics.dto.TestAnalysisChartRequest;
import io.metersphere.reportstatistics.dto.TestAnalysisChartResult;
import io.metersphere.reportstatistics.dto.TestCaseCountChartResult;
import io.metersphere.reportstatistics.dto.TestCaseCountRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestCaseRemoteService extends TestTrackBaseService {
    private static final String URL_COUNT_TEST_CASE_BY_REQUEST = "/test/case/count";
    private static final String URL_COUNT_TEST_CASE_BY_TYPE_AND_REQUEST = "/test/case/count/%s";

    public List<TestCaseCountChartResult> countTestCaseByRequest(TestCaseCountRequest request) {
        List<TestCaseCountChartResult> returnList = new ArrayList<>(0);
        try {
            returnList = microService.postForDataArray(serviceName, URL_COUNT_TEST_CASE_BY_REQUEST, request,
                    TestCaseCountChartResult.class);
        } catch (Exception e) {
            LogUtil.error("调用微服务失败!", e);
        }
        return returnList;
    }

    public List<TestAnalysisChartResult> countCaseByTypeAndRequest(String type, TestAnalysisChartRequest request) {
        List<TestAnalysisChartResult> returnList = new ArrayList<>(0);
        try {
            returnList = microService.postForDataArray(serviceName, String.format(URL_COUNT_TEST_CASE_BY_TYPE_AND_REQUEST, type), request,
                    TestAnalysisChartResult.class);
        } catch (Exception e) {
            LogUtil.error("调用微服务失败!", e);
        }
        return returnList;
    }
}