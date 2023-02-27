package io.metersphere.plan.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestCaseRelevanceVo {
    private String testCaseId;
    private String relevanceCaseId;

    private String relevanceType;
}
