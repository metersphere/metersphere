package io.metersphere.track.request.testcase;

import io.metersphere.controller.request.BaseQueryRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QueryTestCaseRequest extends BaseQueryRequest {

    private String id;

    private String name;

    private String relationshipType;

    private List<String> testCaseIds;

    private String planId;

    private String issuesId;

    private String userId;

    private String reviewId;

    private boolean isSelectThisWeedData = false;
    private boolean isSelectThisWeedRelevanceData = false;

    private String caseCoverage;

    private String nodeId;

    private String statusIsNot;

    private long createTime = 0;
    private long relevanceCreateTime = 0;
    private List<String> testCaseContainIds;
}
