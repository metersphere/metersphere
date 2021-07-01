package io.metersphere.track.issue;

import io.metersphere.base.domain.IssuesDao;
import io.metersphere.base.domain.Project;
import io.metersphere.dto.UserDTO;
import io.metersphere.track.issue.domain.PlatformUser;
import io.metersphere.track.request.testcase.IssuesRequest;

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
    String getProjectId(String projectId) { return null; }
}
