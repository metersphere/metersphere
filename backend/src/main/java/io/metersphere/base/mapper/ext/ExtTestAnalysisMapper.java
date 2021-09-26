package io.metersphere.base.mapper.ext;


import io.metersphere.reportstatistics.dto.TestAnalysisChartRequest;
import io.metersphere.reportstatistics.dto.TestAnalysisChartResult;

import java.util.List;

public interface ExtTestAnalysisMapper {

    List<TestAnalysisChartResult> getCraeteCaseReport(TestAnalysisChartRequest request);

    List<TestAnalysisChartResult> getUpdateCaseReport(TestAnalysisChartRequest request);
}
