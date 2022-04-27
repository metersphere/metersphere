package io.metersphere.track.issue.service;

import io.metersphere.track.request.testcase.EditTestCaseRequest;

public interface XpackIssueService {

    void updateThirdPartyIssuesLink(EditTestCaseRequest testCase);
}
