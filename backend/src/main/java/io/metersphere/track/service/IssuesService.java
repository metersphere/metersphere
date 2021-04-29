package io.metersphere.track.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.IssueTemplateMapper;
import io.metersphere.base.mapper.IssuesMapper;
import io.metersphere.base.mapper.TestCaseIssuesMapper;
import io.metersphere.base.mapper.WorkspaceMapper;
import io.metersphere.base.mapper.ext.ExtIssuesMapper;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.IntegrationRequest;
import io.metersphere.i18n.Translator;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.service.IntegrationService;
import io.metersphere.service.ProjectService;
import io.metersphere.track.issue.*;
import io.metersphere.track.issue.domain.PlatformUser;
import io.metersphere.track.issue.domain.ZentaoBuild;
import io.metersphere.track.request.testcase.IssuesRequest;
import io.metersphere.track.request.testcase.IssuesUpdateRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class IssuesService {

    @Resource
    private IntegrationService integrationService;
    @Resource
    private ProjectService projectService;
    @Lazy
    @Resource
    private TestCaseService testCaseService;
    @Resource
    private IssuesMapper issuesMapper;
    @Resource
    private NoticeSendService noticeSendService;
    @Resource
    private TestCaseIssuesMapper testCaseIssuesMapper;
    @Resource
    private IssueTemplateMapper issueTemplateMapper;
    @Resource
    private ExtIssuesMapper extIssuesMapper;
    @Resource
    private WorkspaceMapper workspaceMapper;

    public void testAuth(String platform) {
        AbstractIssuePlatform abstractPlatform = IssueFactory.createPlatform(platform, new IssuesRequest());
        abstractPlatform.testAuth();
    }

    public void addIssues(IssuesUpdateRequest issuesRequest) {
        List<AbstractIssuePlatform> platformList = getUpdatePlatforms(issuesRequest);
        platformList.forEach(platform -> {
            platform.addIssue(issuesRequest);
        });
        noticeIssueEven(issuesRequest, "IssuesCreate");
    }

    public void noticeIssueEven(IssuesUpdateRequest issuesRequest, String type) {
        SessionUser user = SessionUtils.getUser();
        String orgId = user.getLastOrganizationId();
        List<String> userIds = new ArrayList<>();
        userIds.add(orgId);
        String context = getIssuesContext(user, issuesRequest, NoticeConstants.Event.CREATE);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("issuesName", issuesRequest.getTitle());
        paramMap.put("creator", user.getName());
        NoticeModel noticeModel = NoticeModel.builder()
                .context(context)
                .relatedUsers(userIds)
                .subject(Translator.get("task_defect_notification"))
                .mailTemplate(type)
                .paramMap(paramMap)
                .event(NoticeConstants.Event.CREATE)
                .build();
        noticeSendService.send(NoticeConstants.TaskType.DEFECT_TASK, noticeModel);
    }


    public void updateIssues(IssuesUpdateRequest issuesRequest) {
        List<AbstractIssuePlatform> platformList = getUpdatePlatforms(issuesRequest);
        platformList.forEach(platform -> {
            platform.updateIssue(issuesRequest);
        });
        // todo 缺陷更新事件？
    }

    public List<AbstractIssuePlatform> getUpdatePlatforms(IssuesUpdateRequest updateRequest) {
        List<String> platforms = new ArrayList<>();
        if (StringUtils.isNotBlank(updateRequest.getTestCaseId())) {
            // 测试计划关联
            String p = getPlatformsByCaseId(updateRequest.getTestCaseId());
            platforms.add(p);
        } else {
            // 缺陷管理关联
            String t = getIssueTemplate(updateRequest.getProjectId());
            platforms.add(t);
        }

        if (CollectionUtils.isEmpty(platforms)) {
            platforms.add(IssuesManagePlatform.Local.toString());
        }
        IssuesRequest issuesRequest = new IssuesRequest();
        issuesRequest.setTestCaseId(updateRequest.getTestCaseId());
        return IssueFactory.createPlatforms(platforms, issuesRequest);
    }

    public List<IssuesDao> getIssues(String caseId) {
        IssuesRequest issueRequest = new IssuesRequest();
        issueRequest.setTestCaseId(caseId);
        ServiceUtils.getDefaultOrder(issueRequest.getOrders());
        Project project = getProjectByCaseId(caseId);
        return getIssuesByProject(issueRequest, project);
    }
    public List<IssuesDao> getIssuesByProject(IssuesRequest issueRequest, Project project) {
        List<IssuesDao> list = new ArrayList<>();
        List<String> platforms = getPlatforms(project);
        platforms.add(IssuesManagePlatform.Local.toString());
        List<AbstractIssuePlatform> platformList = IssueFactory.createPlatforms(platforms, issueRequest);
        platformList.forEach(platform -> {
            List<IssuesDao> issue = platform.getIssue(issueRequest);
            list.addAll(issue);
        });

        return list;
    }

    public String getPlatformsByCaseId(String caseId) {
        TestCaseWithBLOBs testCase = testCaseService.getTestCase(caseId);
        Project project = projectService.getProjectById(testCase.getProjectId());
        return getIssueTemplate(project.getId());
    }

    public String getIssueTemplate(String projectId) {
        Project project = projectService.getProjectById(projectId);
        String id = project.getIssueTemplateId();
        if (StringUtils.isBlank(id)) {
            MSException.throwException("project issue template id is null.");
        }
        IssueTemplate issueTemplate = issueTemplateMapper.selectByPrimaryKey(id);
        String platform = issueTemplate.getPlatform();
        if (StringUtils.equals(platform, "metersphere")) {
            return IssuesManagePlatform.Local.name();
        }
        return platform;
    }

    public List<String> getPlatforms(Project project) {
        SessionUser user = SessionUtils.getUser();
        String orgId = user.getLastOrganizationId();
        boolean tapd = isIntegratedPlatform(orgId, IssuesManagePlatform.Tapd.toString());
        boolean jira = isIntegratedPlatform(orgId, IssuesManagePlatform.Jira.toString());
        boolean zentao = isIntegratedPlatform(orgId, IssuesManagePlatform.Zentao.toString());

        List<String> platforms = new ArrayList<>();
        if (tapd) {
            // 是否关联了项目
            String tapdId = project.getTapdId();
            if (StringUtils.isNotBlank(tapdId)) {
                platforms.add(IssuesManagePlatform.Tapd.name());
            }

        }

        if (jira) {
            String jiraKey = project.getJiraKey();
            if (StringUtils.isNotBlank(jiraKey)) {
                platforms.add(IssuesManagePlatform.Jira.name());
            }
        }

        if (zentao) {
            String zentaoId = project.getZentaoId();
            if (StringUtils.isNotBlank(zentaoId)) {
                platforms.add(IssuesManagePlatform.Zentao.name());
            }
        }
        return platforms;
    }

    private Project getProjectByCaseId(String testCaseId) {
        TestCaseWithBLOBs testCase = testCaseService.getTestCase(testCaseId);
        Project project = projectService.getProjectById(testCase.getProjectId());
        return project;
    }

    private String getTapdProjectId(String testCaseId) {
        TestCaseWithBLOBs testCase = testCaseService.getTestCase(testCaseId);
        Project project = projectService.getProjectById(testCase.getProjectId());
        return project.getTapdId();
    }

    private String getJiraProjectKey(String testCaseId) {
        TestCaseWithBLOBs testCase = testCaseService.getTestCase(testCaseId);
        Project project = projectService.getProjectById(testCase.getProjectId());
        return project.getJiraKey();
    }

    private String getZentaoProjectId(String testCaseId) {
        TestCaseWithBLOBs testCase = testCaseService.getTestCase(testCaseId);
        Project project = projectService.getProjectById(testCase.getProjectId());
        return project.getZentaoId();
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
        IssuesWithBLOBs issues = new IssuesWithBLOBs();
        issues.setId(issueId);
        issues.setStatus("closed");
        issuesMapper.updateByPrimaryKeySelective(issues);
    }

    public List<PlatformUser> getTapdProjectUsers(IssuesRequest request) {
        AbstractIssuePlatform platform = IssueFactory.createPlatform(IssuesManagePlatform.Tapd.name(), request);
        return platform.getPlatformUser();
    }

    public List<PlatformUser> getZentaoUsers(IssuesRequest request) {
        AbstractIssuePlatform platform = IssueFactory.createPlatform(IssuesManagePlatform.Zentao.name(), request);
        return platform.getPlatformUser();
    }

    public void deleteIssue(IssuesRequest request) {
        String caseId = request.getCaseId();
        String id = request.getId();
        issuesMapper.deleteByPrimaryKey(id);

        TestCaseIssuesExample example = new TestCaseIssuesExample();
        example.createCriteria().andTestCaseIdEqualTo(caseId).andIssuesIdEqualTo(id);
        testCaseIssuesMapper.deleteByExample(example);
    }

    public void delete(String id) {
        issuesMapper.deleteByPrimaryKey(id);
        TestCaseIssuesExample example = new TestCaseIssuesExample();
        example.createCriteria()
                .andIssuesIdEqualTo(id);
        testCaseIssuesMapper.deleteByExample(example);
    }

    private static String getIssuesContext(SessionUser user, IssuesUpdateRequest issuesRequest, String type) {
        String context = "";
        if (StringUtils.equals(NoticeConstants.Event.CREATE, type)) {
            context = "缺陷任务通知：" + user.getName() + "发起了一个缺陷" + "'" + issuesRequest.getTitle() + "'" + "请跟进";
        }
        return context;
    }

    public List<ZentaoBuild> getZentaoBuilds(IssuesRequest request) {
        ZentaoPlatform platform = (ZentaoPlatform) IssueFactory.createPlatform(IssuesManagePlatform.Zentao.name(), request);
        return platform.getBuilds();
    }

    public List<IssuesDao> list(IssuesRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));


//        List<IssuesDao> list = new ArrayList<>();
//        String projectId = request.getProjectId();
//        Project project = projectService.getProjectById(projectId);
//        List<String> platforms = getPlatforms(project);
//        platforms.add(IssuesManagePlatform.Local.toString());
//        List<AbstractIssuePlatform> platformList = IssueFactory.createPlatforms(platforms, request);
//        platformList.forEach(platform -> {
//            List<IssuesDao> issue = platform.getIssue(request);
//            list.addAll(issue);
//        });
        List<IssuesDao> issues = extIssuesMapper.getIssuesByProjectId(request);
//        Map<String, List<IssuesDao>> issueMap = getIssueMap(issues);
//        Map<String, AbstractIssuePlatform> platformMap = getPlatformMap(request);
//        issueMap.forEach((platformName, data) -> {
//            AbstractIssuePlatform platform = platformMap.get(platformName);
//            if (platform != null) {
//                platform.filter(data);
//            }
//        });
        return issues;
//        return list;
    }

    public Map<String, List<IssuesDao>> getIssueMap(List<IssuesDao> issues) {
        Map<String, List<IssuesDao>> issueMap = new HashMap<>();
        issues.forEach(item -> {
            String platForm = item.getPlatform();
            if (StringUtils.equalsIgnoreCase(IssuesManagePlatform.Local.toString(), item.getPlatform())) {
                // 可能有大小写的问题
                platForm = IssuesManagePlatform.Local.toString();
            }
            List<IssuesDao> issuesDao = issueMap.get(platForm);
            if (issuesDao == null) {
                issuesDao = new ArrayList<>();
            }
            issuesDao.add(item);
            issueMap.put(platForm, issuesDao);
        });
        return issueMap;
    }

    public Map<String, AbstractIssuePlatform> getPlatformMap(IssuesRequest request) {
        Project project = projectService.getProjectById(request.getProjectId());
        List<String> platforms = getPlatforms(project);
        platforms.add(IssuesManagePlatform.Local.toString());
        return IssueFactory.createPlatformsForMap(platforms, request);
    }

    public IssuesWithBLOBs getPlatformIssue(IssuesWithBLOBs issue) {
        String platform = issue.getPlatform();
        if (StringUtils.isNotBlank(issue.getProjectId())) {
            Project project = projectService.getProjectById(issue.getProjectId());
            Workspace workspace = workspaceMapper.selectByPrimaryKey(project.getWorkspaceId());
            String orgId = workspace.getOrganizationId();
            try {
                if (StringUtils.equals(platform, IssuesManagePlatform.Tapd.name())) {
                    TapdPlatform tapdPlatform = new TapdPlatform(new IssuesRequest());
                    String tapdId = projectService.getProjectById(issue.getProjectId()).getTapdId();
                    IssuesDao tapdIssues = tapdPlatform.getTapdIssues(tapdId, issue.getId());
                    issue.setTitle(tapdIssues.getTitle());
                    issue.setDescription(tapdIssues.getDescription());
                    issue.setStatus(tapdIssues.getStatus());
                } else if (StringUtils.equals(platform, IssuesManagePlatform.Jira.name())) {
                    JiraPlatform jiraPlatform = new JiraPlatform(new IssuesRequest());
                    String config = getConfig(orgId, IssuesManagePlatform.Jira.toString());
                    JSONObject object = JSON.parseObject(config);
                    HttpHeaders headers = jiraPlatform.getAuthHeader(object);
                    String url = object.getString("url");
                    IssuesDao jiraIssues = jiraPlatform.getJiraIssues(headers, url, issue.getId());
                    issue.setTitle(jiraIssues.getTitle());
                    issue.setDescription(jiraIssues.getDescription());
                    issue.setStatus(jiraIssues.getStatus());
                } else if (StringUtils.equals(platform, IssuesManagePlatform.Zentao.name())) {
                    String config = getConfig(orgId, IssuesManagePlatform.Zentao.toString());
                    JSONObject object = JSON.parseObject(config);
                    String account = object.getString("account");
                    String password = object.getString("password");
                    String url = object.getString("url");
                    ZentaoPlatform zentaoPlatform = new ZentaoPlatform(account, password, url);
                    IssuesDao zentaoIssues = zentaoPlatform.getZentaoIssues(issue.getId());
                    issue.setTitle(zentaoIssues.getTitle());
                    issue.setDescription(zentaoIssues.getDescription());
                    issue.setStatus(zentaoIssues.getStatus());
                }
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
            }
        }
        return issue;
    }

    private String getConfig(String orgId, String platform) {
        IntegrationRequest request = new IntegrationRequest();
        if (StringUtils.isBlank(orgId)) {
            MSException.throwException("organization id is null");
        }
        request.setOrgId(orgId);
        request.setPlatform(platform);

        ServiceIntegration integration = integrationService.get(request);
        return integration.getConfiguration();
    }
}
