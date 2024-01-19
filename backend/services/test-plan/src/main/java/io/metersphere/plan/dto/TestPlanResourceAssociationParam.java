package io.metersphere.plan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TestPlanResourceAssociationParam {
    @NotEmpty
    private List<String> resourceIdList;
    @NotBlank
    private String projectId;
    @NotBlank
    private String testPlanId;
    @NotBlank
    private Long testPlanNum;
    @NotBlank
    private String operator;
}
