package io.metersphere.track.dto;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.commons.constants.TestPlanApiExecuteStatus;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TestCaseReportAdvanceStatusResultDTO {
    private List<TestCaseReportStatusResultDTO> functionalResult;
    private List<TestCaseReportStatusResultDTO> apiResult;
    private List<TestCaseReportStatusResultDTO> scenarioResult;
    private List<TestCaseReportStatusResultDTO> loadResult;
    private List<String> executedScenarioIds;
}

