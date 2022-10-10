package io.metersphere.service.issue;

import io.metersphere.request.testcase.EditTestCaseRequest;

public interface XpackIssueService {

    void updateThirdPartyIssuesLink(EditTestCaseRequest testCase);
}
