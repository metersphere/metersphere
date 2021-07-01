package io.metersphere.track.issue;

import io.metersphere.base.domain.IssuesDao;
import io.metersphere.base.domain.IssuesWithBLOBs;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.track.dto.DemandDTO;
import io.metersphere.track.request.testcase.IssuesRequest;
import io.metersphere.track.request.testcase.IssuesUpdateRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.UUID;

public class LocalPlatform extends LocalAbstractPlatform {

    protected String key = IssuesManagePlatform.Local.toString();

    public LocalPlatform(IssuesRequest issuesRequest) {
        super(issuesRequest);
    }

    @Override
    public List<IssuesDao> getIssue(IssuesRequest issuesRequest) {
        String projectId = issuesRequest.getProjectId();
        issuesRequest.setPlatform(IssuesManagePlatform.Local.toString());
        if (StringUtils.isNotBlank(projectId)) {
            return extIssuesMapper.getIssuesByProjectId(issuesRequest);
        }
        return extIssuesMapper.getIssuesByCaseId(issuesRequest);
    }

    @Override
    public List<DemandDTO> getDemandList(String projectId) {
        return null;
    }

    @Override
    public void addIssue(IssuesUpdateRequest issuesRequest) {
        SessionUser user = SessionUtils.getUser();
        String id = UUID.randomUUID().toString();
        IssuesWithBLOBs issues = new IssuesWithBLOBs();
        BeanUtils.copyBean(issues, issuesRequest);
        issues.setId(id);
        issues.setStatus("new");
        issues.setReporter(user.getId());
        issues.setCreateTime(System.currentTimeMillis());
        issues.setUpdateTime(System.currentTimeMillis());
        issues.setPlatform(IssuesManagePlatform.Local.toString());;
        issues.setNum(getNextNum(issuesRequest.getProjectId()));
        issuesMapper.insert(issues);

        issuesRequest.setId(id);
        handleTestCaseIssues(issuesRequest);
    }

    @Override
    public void updateIssue(IssuesUpdateRequest request) {
        handleIssueUpdate(request);
    }

    @Override
    public void deleteIssue(String id) {
        issuesMapper.deleteByPrimaryKey(id);
    }
}
