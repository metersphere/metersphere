package io.metersphere.plan.request.function;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestCaseRelevanceRequest {
    private String planId;
    private String name;
    private List<String> projectIds;
}
