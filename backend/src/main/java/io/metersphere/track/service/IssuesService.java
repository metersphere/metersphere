package io.metersphere.track.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.IssuesMapper;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.IntegrationRequest;
import io.metersphere.service.IntegrationService;
import io.metersphere.service.ProjectService;
import io.metersphere.track.issue.AbstractIssuePlatform;
import io.metersphere.track.issue.IssueFactory;
import io.metersphere.track.issue.PlatformUser;
import io.metersphere.track.request.testcase.IssuesRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class IssuesService {

    @Resource
    private IntegrationService integrationService;
    @Resource
    private ProjectService projectService;
    @Resource
    private TestCaseService testCaseService;
    @Resource
    private IssuesMapper issuesMapper;


    public void testAuth(String platform) {
        AbstractIssuePlatform abstractPlatform = IssueFactory.createPlatform(platform, new IssuesRequest());
        abstractPlatform.testAuth();
    }

    public void addIssues(IssuesRequest issuesRequest) {
        SessionUser user = SessionUtils.getUser();
        String orgId = user.getLastOrganizationId();

        boolean tapd = isIntegratedPlatform(orgId, IssuesManagePlatform.Tapd.toString());
        boolean jira = isIntegratedPlatform(orgId, IssuesManagePlatform.Jira.toString());

        String tapdId = getTapdProjectId(issuesRequest.getTestCaseId());
        String jiraKey = getJiraProjectKey(issuesRequest.getTestCaseId());

        List<String> platforms = new ArrayList<>();

        if (tapd) {
            // 是否关联了项目
            if (StringUtils.isNotBlank(tapdId)) {
                platforms.add(IssuesManagePlatform.Tapd.name());
            }
        }

        if (jira) {
            if (StringUtils.isNotBlank(jiraKey)) {
                platforms.add(IssuesManagePlatform.Jira.name());
            }
        }

        if (StringUtils.isBlank(tapdId) && StringUtils.isBlank(jiraKey)) {
            platforms.add("LOCAL");
        }

        List<AbstractIssuePlatform> platformList = IssueFactory.createPlatforms(platforms, issuesRequest);
        platformList.forEach(platform -> {
            platform.addIssue(issuesRequest);
        });

    }

    public List<Issues> getIssues(String caseId) {
        List<Issues> list = new ArrayList<>();
        SessionUser user = SessionUtils.getUser();
        String orgId = user.getLastOrganizationId();

        boolean tapd = isIntegratedPlatform(orgId, IssuesManagePlatform.Tapd.toString());
        boolean jira = isIntegratedPlatform(orgId, IssuesManagePlatform.Jira.toString());

        List<String> platforms = new ArrayList<>();
        if (tapd) {
            // 是否关联了项目
            String tapdId = getTapdProjectId(caseId);
            if (StringUtils.isNotBlank(tapdId)) {
                platforms.add(IssuesManagePlatform.Tapd.name());
            }

        }

        if (jira) {
            String jiraKey = getJiraProjectKey(caseId);
            if (StringUtils.isNotBlank(jiraKey)) {
                platforms.add(IssuesManagePlatform.Jira.name());
            }
        }

        platforms.add("LOCAL");
        IssuesRequest issueRequest = new IssuesRequest();
        issueRequest.setTestCaseId(caseId);
        List<AbstractIssuePlatform> platformList = IssueFactory.createPlatforms(platforms, issueRequest);
        platformList.forEach(platform -> {
            List<Issues> issue = platform.getIssue();
            list.addAll(issue);
        });

        return list;
    }

    public String getTapdProjectId(String testCaseId) {
        TestCaseWithBLOBs testCase = testCaseService.getTestCase(testCaseId);
        Project project = projectService.getProjectById(testCase.getProjectId());
        return project.getTapdId();
    }

    public String getJiraProjectKey(String testCaseId) {
        TestCaseWithBLOBs testCase = testCaseService.getTestCase(testCaseId);
        Project project = projectService.getProjectById(testCase.getProjectId());
        return project.getJiraKey();
    }

    /**
     * 是否关联平台
     */
    public boolean isIntegratedPlatform(String orgId, String platform) {
        IntegrationRequest request = new IntegrationRequest();
        request.setPlatform(platform);
        request.setOrgId(orgId);
        ServiceIntegration integration = integrationService.get(request);
        return StringUtils.isNotBlank(integration.getId());
    }

    public void closeLocalIssue(String issueId) {
        Issues issues = new Issues();
        issues.setId(issueId);
        issues.setStatus("closed");
        issuesMapper.updateByPrimaryKeySelective(issues);
    }

    public List<PlatformUser> getTapdProjectUsers(String caseId) {
        IssuesRequest issueRequest = new IssuesRequest();
        issueRequest.setTestCaseId(caseId);
        AbstractIssuePlatform platform = IssueFactory.createPlatform(IssuesManagePlatform.Tapd.name(), issueRequest);
        return platform.getPlatformUser();
    }

    public void deleteIssue(String id) {
        issuesMapper.deleteByPrimaryKey(id);
    }
}
