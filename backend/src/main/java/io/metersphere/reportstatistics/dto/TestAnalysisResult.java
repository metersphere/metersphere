package io.metersphere.reportstatistics.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestAnalysisResult {
    private TestAnalysisChartDTO chartDTO;
    private List<TestAnalysisTableDTO> tableDTOs;

    public TestAnalysisResult() {

    }

    public TestAnalysisResult(TestAnalysisChartDTO chartDTO, List<TestAnalysisTableDTO> tableDTOs) {
        this.chartDTO = chartDTO;
        this.tableDTOs = tableDTOs;
    }
}
