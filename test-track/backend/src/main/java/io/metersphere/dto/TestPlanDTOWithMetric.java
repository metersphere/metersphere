package io.metersphere.dto;

import io.metersphere.base.domain.User;
import io.metersphere.plan.dto.TestPlanDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TestPlanDTOWithMetric extends TestPlanDTO {
    private Double passRate;
    private Double testRate;
    private Integer passed;
    private Integer tested;
    private Integer total;
    private String createUser;
    private Integer testPlanTestCaseCount = 0;
    private Integer testPlanApiCaseCount = 0;
    private Integer testPlanApiScenarioCount = 0;
    private Integer testPlanUiScenarioCount = 0;
    private Integer testPlanLoadCaseCount = 0;
    private List<User> principalUsers = new ArrayList<>();
    private List<User> followUsers = new ArrayList<>();
}
