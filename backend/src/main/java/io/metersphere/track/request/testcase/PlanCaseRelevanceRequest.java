package io.metersphere.track.request.testcase;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PlanCaseRelevanceRequest {
    private String planId;
    private List<String> testCaseIds = new ArrayList<>();
}
