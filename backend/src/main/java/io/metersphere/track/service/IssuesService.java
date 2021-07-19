package io.metersphere.track.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtIssuesMapper;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.IntegrationRequest;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.track.TestPlanReference;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.service.IntegrationService;
import io.metersphere.service.IssueTemplateService;
import io.metersphere.service.ProjectService;
import io.metersphere.service.SystemParameterService;
import io.metersphere.track.issue.*;
import io.metersphere.track.issue.domain.PlatformUser;
import io.metersphere.track.issue.domain.zentao.ZentaoBuild;
import io.metersphere.track.request.testcase.AuthUserIssueRequest;
import io.metersphere.track.request.testcase.IssuesRequest;
import io.metersphere.track.request.testcase.IssuesUpdateRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class IssuesService {

    @Resource
    private IntegrationService integrationService;
    @Resource
    private ProjectService projectService;
    @Resource
    private TestPlanService testPlanService;
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
    @Resource
    private IssueTemplateService issueTemplateService;
    @Resource
    private TestCaseMapper testCaseMapper;
    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private TestPlanTestCaseService testPlanTestCaseService;

    public void testAuth(String orgId, String platform) {
        IssuesRequest issuesRequest = new IssuesRequest();
        issuesRequest.setOrganizationId(orgId);
        AbstractIssuePlatform abstractPlatform = IssueFactory.createPlatform(platform, issuesRequest);
        abstractPlatform.testAuth();
    }

    public void addIssues(IssuesUpdateRequest issuesRequest) {
        List<AbstractIssuePlatform> platformList = getUpdatePlatforms(issuesRequest);
        platformList.forEach(platform -> {
            platform.addIssue(issuesRequest);
        });
        issuesRequest.getTestCaseIds().forEach(l -> {
            try {
                List<IssuesDao> issues = this.getIssues(l);
                if (org.apache.commons.collections4.CollectionUtils.isEmpty(issues)) {
                    LogUtil.error(l + "下的缺陷为空");
                }
                int issuesCount = issues.size();
                testPlanTestCaseService.updateIssues(issuesCount, "", l, JSON.toJSONString(issues));
            } catch (Exception e) {
                LogUtil.error("处理bug数量报错caseId: {}, message: {}", l, ExceptionUtils.getStackTrace(e));
            }
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
        BeanUtils.copyBean(issuesRequest, updateRequest);
        return IssueFactory.createPlatforms(platforms, issuesRequest);
    }

    public List<IssuesDao> getIssues(String caseId) {
        IssuesRequest issueRequest = new IssuesRequest();
        issueRequest.setTestCaseId(caseId);
        ServiceUtils.getDefaultOrder(issueRequest.getOrders());
        Project project = getProjectByCaseId(caseId);
        // project 不存在
        if (project == null) {
            return null;
        }
        String workspaceId = project.getWorkspaceId();
        Workspace workspace = workspaceMapper.selectByPrimaryKey(workspaceId);
        TestCase testCase = testCaseMapper.selectByPrimaryKey(caseId);
        String orgId = workspace.getOrganizationId();
        String userId = testCase.getMaintainer();
        issueRequest.setOrganizationId(orgId);
        issueRequest.setUserId(userId);
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
        IssueTemplate issueTemplate = null;
        String id = project.getIssueTemplateId();
        if (StringUtils.isBlank(id)) {
            issueTemplate = issueTemplateService.getDefaultTemplate(project.getWorkspaceId());
        } else {
            issueTemplate = issueTemplateMapper.selectByPrimaryKey(id);
        }
        if (issueTemplate == null) {
            MSException.throwException("project issue template id is null.");
        }
        String platform = issueTemplate.getPlatform();
        if (StringUtils.equals(platform, "metersphere")) {
            return IssuesManagePlatform.Local.name();
        }
        return platform;
    }

    public List<String> getPlatforms(Project project) {
        String workspaceId = project.getWorkspaceId();
        Workspace workspace = workspaceMapper.selectByPrimaryKey(workspaceId);
        String orgId = workspace.getOrganizationId();
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
        // testCase 不存在
        if (testCase == null) {
            return null;
        }
        return projectService.getProjectById(testCase.getProjectId());
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
        issuesMapper.deleteByPrimaryKey(request.getId());
        deleteIssueRelate(request);
    }

    public void deleteIssueRelate(IssuesRequest request) {
        String caseId = request.getCaseId();
        String id = request.getId();
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
        List<IssuesDao> issues = extIssuesMapper.getIssuesByProjectId(request);

        List<String> ids = issues.stream()
                .map(IssuesDao::getCreator)
                .collect(Collectors.toList());
        Map<String, User> userMap = ServiceUtils.getUserMap(ids);
        List<String> resourceIds = issues.stream()
                .map(IssuesDao::getResourceId)
                .collect(Collectors.toList());

        List<TestPlan> testPlans = testPlanService.getTestPlanByIds(resourceIds);
        Map<String, String> planMap = testPlans.stream()
                .collect(Collectors.toMap(TestPlan::getId, TestPlan::getName));

        issues.forEach(item -> {
            User createUser = userMap.get(item.getCreator());
            if (createUser != null) {
                item.setCreatorName(createUser.getName());
            }
            if (planMap.get(item.getResourceId()) != null) {
                item.setResourceName(planMap.get(item.getResourceId()));
            }
            TestCaseIssuesExample example = new TestCaseIssuesExample();
            example.createCriteria().andIssuesIdEqualTo(item.getId());
            List<TestCaseIssues> testCaseIssues = testCaseIssuesMapper.selectByExample(example);
            List<String> caseIds = testCaseIssues.stream()
                    .map(TestCaseIssues::getTestCaseId)
                    .collect(Collectors.toList());
            item.setCaseIds(caseIds);
            item.setCaseCount(testCaseIssues.size());
        });
        return issues;
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

    public void syncThirdPartyIssues() {
        List<String> projectIds = projectService.getProjectIds();
        projectIds.forEach(id -> {
            try {
                syncThirdPartyIssues(id);
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
            }
        });
    }

    public void issuesCount() {
        LogUtil.info("测试计划-测试用例同步缺陷信息开始");
        int pageSize = 100;
        int pages = 1;
        for (int i = 0; i < pages; i++) {
            Page<List<TestPlanTestCase>> page = PageHelper.startPage(i, pageSize, true);
            List<TestPlanTestCaseWithBLOBs> list = testPlanTestCaseService.listAll();
            pages = page.getPages();// 替换成真实的值
            list.forEach(l -> {
                try {
                    List<IssuesDao> issues = this.getIssues(l.getCaseId());
                    if (org.apache.commons.collections4.CollectionUtils.isEmpty(issues)) {
                        return;
                    }
                    int issuesCount = issues.size();
                    testPlanTestCaseService.updateIssues(issuesCount, l.getPlanId(), l.getCaseId(), JSON.toJSONString(issues));
                } catch (Exception e) {
                    LogUtil.error("定时任务处理bug数量报错planId: {}, message: {}", l.getPlanId(), ExceptionUtils.getStackTrace(e));
                }
            });
        }
        LogUtil.info("测试计划-测试用例同步缺陷信息结束");
    }

    public void syncThirdPartyIssues(String projectId) {
        if (StringUtils.isNotBlank(projectId)) {
            Project project = projectService.getProjectById(projectId);
            Workspace workspace = workspaceMapper.selectByPrimaryKey(project.getWorkspaceId());

            List<IssuesDao> issues = extIssuesMapper.getIssueForSync(projectId);

            if (CollectionUtils.isEmpty(issues)) {
                return;
            }

            List<IssuesDao> tapdIssues = issues.stream()
                    .filter(item -> item.getPlatform().equals(IssuesManagePlatform.Tapd.name()))
                    .collect(Collectors.toList());
            List<IssuesDao> jiraIssues = issues.stream()
                    .filter(item -> item.getPlatform().equals(IssuesManagePlatform.Jira.name()))
                    .collect(Collectors.toList());
            List<IssuesDao> zentaoIssues = issues.stream()
                    .filter(item -> item.getPlatform().equals(IssuesManagePlatform.Zentao.name()))
                    .collect(Collectors.toList());

            IssuesRequest issuesRequest = new IssuesRequest();
            issuesRequest.setProjectId(projectId);
            issuesRequest.setOrganizationId(workspace.getOrganizationId());
            if (CollectionUtils.isNotEmpty(tapdIssues)) {
                TapdPlatform tapdPlatform = new TapdPlatform(issuesRequest);
                syncThirdPartyIssues(tapdPlatform::syncIssues, project, tapdIssues);
            }
            if (CollectionUtils.isNotEmpty(jiraIssues)) {
                JiraPlatform jiraPlatform = new JiraPlatform(issuesRequest);
                syncThirdPartyIssues(jiraPlatform::syncIssues, project, jiraIssues);
            }
            if (CollectionUtils.isNotEmpty(zentaoIssues)) {
                ZentaoPlatform zentaoPlatform = new ZentaoPlatform(issuesRequest);
                syncThirdPartyIssues(zentaoPlatform::syncIssues, project, zentaoIssues);
            }
        }
    }

    public void syncThirdPartyIssues(BiConsumer<Project, List<IssuesDao>> syncFuc, Project project, List<IssuesDao> issues) {
        try {
            syncFuc.accept(project, issues);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
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

    public String getLogDetails(String id) {
        IssuesWithBLOBs issuesWithBLOBs = issuesMapper.selectByPrimaryKey(id);
        if (issuesWithBLOBs != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(issuesWithBLOBs, TestPlanReference.issuesColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(issuesWithBLOBs.getId()), issuesWithBLOBs.getProjectId(), issuesWithBLOBs.getTitle(), issuesWithBLOBs.getCreator(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(IssuesUpdateRequest issuesRequest) {
        if (issuesRequest != null) {
            issuesRequest.setCreator(SessionUtils.getUserId());
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(issuesRequest, TestPlanReference.issuesColumns);
            OperatingLogDetails details = new OperatingLogDetails(null, issuesRequest.getProjectId(), issuesRequest.getTitle(), issuesRequest.getCreator(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public List<IssuesDao> relateList(IssuesRequest request) {
        return extIssuesMapper.getRelateIssues(request);
    }

    public void userAuth(AuthUserIssueRequest authUserIssueRequest) {
        IssuesRequest issuesRequest = new IssuesRequest();
        issuesRequest.setOrganizationId(authUserIssueRequest.getOrgId());
        AbstractIssuePlatform abstractPlatform = IssueFactory.createPlatform(authUserIssueRequest.getPlatform(), issuesRequest);
        abstractPlatform.userAuth(authUserIssueRequest);
    }
}
