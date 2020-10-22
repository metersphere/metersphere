package io.metersphere.track.service;

import io.metersphere.base.domain.Issues;
import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.ServiceIntegration;
import io.metersphere.base.domain.TestCaseWithBLOBs;
import io.metersphere.base.mapper.IssuesMapper;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.IntegrationRequest;
import io.metersphere.notice.domain.MessageDetail;
import io.metersphere.notice.domain.MessageSettingDetail;
import io.metersphere.notice.service.DingTaskService;
import io.metersphere.notice.service.MailService;
import io.metersphere.notice.service.NoticeService;
import io.metersphere.notice.service.WxChatTaskService;
import io.metersphere.service.IntegrationService;
import io.metersphere.service.ProjectService;
import io.metersphere.track.issue.AbstractIssuePlatform;
import io.metersphere.track.issue.IssueFactory;
import io.metersphere.track.issue.PlatformUser;
import io.metersphere.track.request.testcase.IssuesRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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
    @Resource
    MailService mailService;
    @Resource
    DingTaskService dingTaskService;
    @Resource
    WxChatTaskService wxChatTaskService;
    @Resource
    NoticeService noticeService;

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
        List<String> userIds = new ArrayList<>();
        userIds.add(orgId);
        try {
            String context = getIssuesContext(user, issuesRequest, NoticeConstants.CREATE);
            MessageSettingDetail messageSettingDetail = noticeService.searchMessage();
            List<MessageDetail> taskList = messageSettingDetail.getDefectTask();
            taskList.forEach(r -> {
                switch (r.getType()) {
                    case NoticeConstants.NAIL_ROBOT:
                        dingTaskService.sendNailRobot(r, userIds, context, NoticeConstants.CREATE);
                        break;
                    case NoticeConstants.WECHAT_ROBOT:
                        wxChatTaskService.sendWechatRobot(r, userIds, context, NoticeConstants.CREATE);
                        break;
                    case NoticeConstants.EMAIL:
                        mailService.sendIssuesNotice(r, userIds, issuesRequest, NoticeConstants.CREATE, user);
                        break;
                }
            });
        } catch (Exception e) {
            LogUtil.error(e);
        }

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

    private static String getIssuesContext(SessionUser user, IssuesRequest issuesRequest, String type) {
        String context = "";
        if (StringUtils.equals(NoticeConstants.CREATE, type)) {
            context = "缺陷任务通知：" + user.getName() + "发起了一个缺陷" + "'" + issuesRequest.getTitle() + "'" + "请跟进";
        }
        return context;
    }
}
