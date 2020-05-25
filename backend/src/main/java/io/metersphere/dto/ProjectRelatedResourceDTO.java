package io.metersphere.dto;

import lombok.Data;

@Data
public class ProjectRelatedResourceDTO {
    Long testCaseCount;
    Long testPlanCount;
    Long loadTestCount;
    Long apiTestCount;
}
