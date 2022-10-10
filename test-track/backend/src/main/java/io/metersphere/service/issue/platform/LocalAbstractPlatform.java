package io.metersphere.service.issue.platform;

import io.metersphere.base.domain.Project;
import io.metersphere.dto.IssuesDao;
import io.metersphere.dto.UserDTO;
import io.metersphere.request.testcase.IssuesRequest;
import io.metersphere.service.issue.domain.PlatformUser;

import java.util.List;

public abstract class LocalAbstractPlatform extends AbstractIssuePlatform {

    public LocalAbstractPlatform(IssuesRequest issuesRequest) { super(issuesRequest); }

    @Override
    public void testAuth() {}

    @Override
    public void userAuth(UserDTO.PlatformInfo userInfo) {}

    @Override
    public List<PlatformUser> getPlatformUser() { return null; }

    @Override
    public void syncIssues(Project project, List<IssuesDao> tapdIssues) {}

    @Override
    public String getProjectId(String projectId) { return null; }
}
