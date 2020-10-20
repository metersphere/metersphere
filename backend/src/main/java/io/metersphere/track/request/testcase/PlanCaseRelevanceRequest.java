package io.metersphere.track.request.testcase;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PlanCaseRelevanceRequest {
    private String planId;
    private String projectId;
    private List<String> testCaseIds = new ArrayList<>();
}
