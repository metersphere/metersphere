package io.metersphere.base.mapper.ext;


import io.metersphere.dto.TestAnalysisChartRequest;
import io.metersphere.dto.TestAnalysisChartResult;

import java.util.List;

public interface ExtTestAnalysisMapper {

    List<TestAnalysisChartResult> getCreateCaseReport(TestAnalysisChartRequest request);

    List<TestAnalysisChartResult> getUpdateCaseReport(TestAnalysisChartRequest request);
}
