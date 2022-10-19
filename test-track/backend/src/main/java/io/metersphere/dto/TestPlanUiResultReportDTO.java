package io.metersphere.dto;

import io.metersphere.plan.dto.TestCaseReportStatusResultDTO;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class TestPlanUiResultReportDTO implements Serializable {
    //历史的case数据
    private List<TestCaseReportStatusResultDTO> uiScenarioCaseData;
    //场景的分类统计数据
    private List<TestCaseReportStatusResultDTO> uiScenarioData;
    //步骤的分类统计数据
    private List<TestCaseReportStatusResultDTO> uiScenarioStepData;
}

