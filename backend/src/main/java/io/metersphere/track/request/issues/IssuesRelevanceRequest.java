package io.metersphere.track.request.issues;

import io.metersphere.track.request.testcase.QueryTestCaseRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class IssuesRelevanceRequest {
    /**
     * 缺陷ID
     */
    private String issuesId;

    private String caseId;

    /**
     * 当选择关联全部用例时把加载条件送到后台，从后台查询
     */
    private QueryTestCaseRequest request;

    /**
     * 具体要关联的用例
     */
    private List<String> testCaseIds = new ArrayList<>();

    private List<String> issueIds;

    private Boolean checked;
}
