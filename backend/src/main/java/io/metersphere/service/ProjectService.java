package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.DeleteAPITestRequest;
import io.metersphere.api.dto.QueryAPITestRequest;
import io.metersphere.api.service.APITestService;
import io.metersphere.api.service.ApiScenarioReportService;
import io.metersphere.api.service.ApiTestDelService;
import io.metersphere.api.tcp.TCPPool;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtProjectMapper;
import io.metersphere.base.mapper.ext.ExtProjectVersionMapper;
import io.metersphere.base.mapper.ext.ExtUserGroupMapper;
import io.metersphere.base.mapper.ext.ExtUserMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.controller.request.AddProjectRequest;
import io.metersphere.controller.request.ProjectRequest;
import io.metersphere.controller.request.ScheduleRequest;
import io.metersphere.dto.ProjectConfig;
import io.metersphere.dto.ProjectDTO;
import io.metersphere.dto.WorkspaceMemberDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.job.sechedule.CleanUpReportJob;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import io.metersphere.performance.request.DeleteTestPlanRequest;
import io.metersphere.performance.request.QueryProjectFileRequest;
import io.metersphere.performance.service.PerformanceReportService;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.track.issue.AbstractIssuePlatform;
import io.metersphere.track.issue.JiraPlatform;
import io.metersphere.track.issue.TapdPlatform;
import io.metersphere.track.issue.ZentaoPlatform;
import io.metersphere.track.request.testcase.IssuesRequest;
import io.metersphere.track.service.TestCaseService;
import io.metersphere.track.service.TestPlanProjectService;
import io.metersphere.track.service.TestPlanReportService;
import io.metersphere.track.service.TestPlanService;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectService {
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ExtProjectMapper extProjectMapper;
    @Resource
    private PerformanceTestService performanceTestService;
    @Resource
    private LoadTestMapper loadTestMapper;
    @Resource
    private LoadTestReportMapper loadTestReportMapper;
    @Resource
    @Lazy
    private TestPlanService testPlanService;
    @Resource
    private TestCaseService testCaseService;
    @Resource
    private APITestService apiTestService;
    @Resource
    private TestPlanProjectService testPlanProjectService;
    @Resource
    private FileService fileService;
    @Resource
    private LoadTestFileMapper loadTestFileMapper;
    @Resource
    private ApiTestFileMapper apiTestFileMapper;
    @Resource
    private PerformanceReportService performanceReportService;
    @Resource
    private UserGroupMapper userGroupMapper;
    @Resource
    private ExtUserGroupMapper extUserGroupMapper;
    @Resource
    private ExtUserMapper extUserMapper;
    @Resource
    private ScheduleService scheduleService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ApiTestDelService apiTestDelService;
    @Value("${tcp.mock.port}")
    private String tcpMockPorts;
    @Resource
    private EnvironmentGroupProjectService environmentGroupProjectService;
    @Resource
    private ExtProjectVersionMapper extProjectVersionMapper;
    @Lazy
    @Resource
    private TestPlanReportService testPlanReportService;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;
    @Resource
    private ProjectApplicationService projectApplicationService;


    public Project addProject(AddProjectRequest project) {
        if (StringUtils.isBlank(project.getName())) {
            MSException.throwException(Translator.get("project_name_is_null"));
        }
        ProjectExample example = new ProjectExample();
        example.createCriteria()
                .andWorkspaceIdEqualTo(project.getWorkspaceId())
                .andNameEqualTo(project.getName());
        if (projectMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("project_name_already_exists"));
        }

        QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
        if (quotaService != null) {
            quotaService.checkWorkspaceProject(project.getWorkspaceId());
        }

        if (project.getMockTcpPort() != null && project.getMockTcpPort().intValue() > 0) {
            this.checkMockTcpPort(project.getMockTcpPort().intValue());
        }

        if (project.getIsMockTcpOpen() == null) {
            project.setIsMockTcpOpen(false);
        }

        if (StringUtils.isBlank(project.getPlatform())) {
            project.setPlatform(IssuesManagePlatform.Local.name());
        }
        String pjId = UUID.randomUUID().toString();
        project.setId(pjId);

        String systemId = this.genSystemId();
        long createTime = System.currentTimeMillis();
        project.setCreateTime(createTime);
        project.setUpdateTime(createTime);
        project.setSystemId(systemId);
        projectMapper.insertSelective(project);

        // 创建项目为当前用户添加用户组
        UserGroup userGroup = new UserGroup();
        userGroup.setId(UUID.randomUUID().toString());
        userGroup.setUserId(SessionUtils.getUserId());
        userGroup.setCreateTime(System.currentTimeMillis());
        userGroup.setUpdateTime(System.currentTimeMillis());
        userGroup.setGroupId(UserGroupConstants.PROJECT_ADMIN);
        userGroup.setSourceId(project.getId());
        userGroupMapper.insert(userGroup);

        // 创建新项目检查当前用户 last_project_id
        extUserMapper.updateLastProjectIdIfNull(project.getId(), SessionUtils.getUserId());

        // 设置默认的通知
        extProjectMapper.setDefaultMessageTask(project.getId());

        if (quotaService != null) {
            quotaService.projectUseDefaultQuota(pjId);
        }

        ProjectVersionService projectVersionService = CommonBeanFactory.getBean(ProjectVersionService.class);
        if (projectVersionService != null) {
            ProjectVersion projectVersion = new ProjectVersion();
            projectVersion.setId(UUID.randomUUID().toString());
            projectVersion.setName("v1.0.0");
            projectVersion.setProjectId(project.getId());
            projectVersion.setCreateTime(System.currentTimeMillis());
            projectVersion.setCreateTime(System.currentTimeMillis());
            projectVersion.setStartTime(System.currentTimeMillis());
            projectVersion.setPublishTime(System.currentTimeMillis());
            projectVersion.setLatest(true);
            projectVersion.setStatus("open");
            projectVersionService.addProjectVersion(projectVersion);
        }
        initProjectApplication(project.getId());
        return project;
    }

    private void initProjectApplication(String projectId) {
        //创建新项目也创建相关新项目的应用（分测试跟踪，接口，性能）
        ProjectApplication projectApplication = new ProjectApplication();
        projectApplication.setProjectId(projectId);
        //每个新项目都会有测试跟踪/性能报告分享链接的有效时间,默认时间24H
        projectApplication.setType(ProjectApplicationType.TRACK_SHARE_REPORT_TIME.toString());
        projectApplication.setTypeValue("24H");
        projectApplicationMapper.insert(projectApplication);
        projectApplication.setType(ProjectApplicationType.PERFORMANCE_SHARE_REPORT_TIME.toString());
        projectApplicationMapper.insert(projectApplication);
        projectApplication.setType(ProjectApplicationType.API_SHARE_REPORT_TIME.toString());
        projectApplicationMapper.insert(projectApplication);
        projectApplication.setType(ProjectApplicationType.CASE_CUSTOM_NUM.toString());
        projectApplication.setTypeValue(Boolean.FALSE.toString());
        projectApplicationMapper.insert(projectApplication);
    }

    public void checkThirdProjectExist(Project project) {
        IssuesRequest issuesRequest = new IssuesRequest();
        if (StringUtils.isBlank(project.getId())) {
            MSException.throwException("project ID cannot be empty");
        }
        issuesRequest.setProjectId(project.getId());
        issuesRequest.setWorkspaceId(project.getWorkspaceId());
        if (StringUtils.equalsIgnoreCase(project.getPlatform(), IssuesManagePlatform.Tapd.name())) {
            TapdPlatform tapd = new TapdPlatform(issuesRequest);
            this.doCheckThirdProjectExist(tapd, project.getTapdId());
        } else if (StringUtils.equalsIgnoreCase(project.getPlatform(), IssuesManagePlatform.Jira.name())) {
            JiraPlatform jira = new JiraPlatform(issuesRequest);
            this.doCheckThirdProjectExist(jira, project.getJiraKey());
        } else if (StringUtils.equalsIgnoreCase(project.getPlatform(), IssuesManagePlatform.Zentao.name())) {
            ZentaoPlatform zentao = new ZentaoPlatform(issuesRequest);
            this.doCheckThirdProjectExist(zentao, project.getZentaoId());
        }
    }

    private void doCheckThirdProjectExist(AbstractIssuePlatform platform, String relateId) {
        if (StringUtils.isBlank(relateId)) {
            MSException.throwException(Translator.get("issue_project_not_exist"));
        }
        Boolean exist = platform.checkProjectExist(relateId);
        if (BooleanUtils.isFalse(exist)) {
            MSException.throwException(Translator.get("issue_project_not_exist"));
        }
    }

    private String genSystemId() {
        String maxSystemIdInDb = extProjectMapper.getMaxSystemId();
        String systemId = "10001";
        if (StringUtils.isNotEmpty(maxSystemIdInDb)) {
            systemId = String.valueOf(Long.parseLong(maxSystemIdInDb) + 1);
        }
        return systemId;
    }

    public Project checkSystemId(Project project) {
        if (project != null) {
            ProjectExample example = new ProjectExample();
            example.createCriteria().andSystemIdEqualTo(project.getSystemId());
            long count = projectMapper.countByExample(example);
            if (count > 1) {
                String systemId = this.genSystemId();
                Project updateModel = new Project();
                updateModel.setId(project.getId());
                updateModel.setSystemId(systemId);
                projectMapper.updateByPrimaryKeySelective(updateModel);
                project = this.getProjectById(project.getId());
            }
        }
        return project;
    }

    public List<ProjectDTO> getProjectList(ProjectRequest request) {
        if (StringUtils.isNotBlank(request.getName())) {
            request.setName(StringUtils.wrapIfMissing(request.getName(), "%"));
        }
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        return extProjectMapper.getProjectWithWorkspace(request);
    }

    public List<ProjectDTO> getUserProject(ProjectRequest request) {
        if (StringUtils.isNotBlank(request.getName())) {
            request.setName(StringUtils.wrapIfMissing(request.getName(), "%"));
        }
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        return extProjectMapper.getUserProject(request);
    }

    public List<Project> getProjectByIds(List<String> ids) {
        if (!CollectionUtils.isEmpty(ids)) {
            ProjectExample example = new ProjectExample();
            example.createCriteria().andIdIn(ids);
            return projectMapper.selectByExample(example);
        }
        return new ArrayList<>();
    }

    public void deleteProject(String projectId) {
        // 删除项目下 性能测试 相关
        deleteLoadTestResourcesByProjectId(projectId);

        // 删除项目下 测试跟踪 相关
        deleteTrackResourceByProjectId(projectId);

        // 删除项目下 接口测试 相关
        deleteAPIResourceByProjectId(projectId);
        apiTestDelService.delete(projectId);

        // User Group
        deleteProjectUserGroup(projectId);

        //关闭TCP
        try {
            //捕获关闭失败的异常，使其不影响删除
            this.closeMockTcp(projectId);
        } catch (Exception e) {
        }

        // 删除环境组下的项目相关
        environmentGroupProjectService.deleteRelateProject(projectId);

        // delete project
        projectMapper.deleteByPrimaryKey(projectId);

        // 删除定时任务
        scheduleService.deleteByProjectId(projectId);
    }

    private void deleteProjectUserGroup(String projectId) {
        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andSourceIdEqualTo(projectId);
        userGroupMapper.deleteByExample(userGroupExample);
    }

    public void updateIssueTemplate(String originId, String templateId, String projectId) {
        Project project = new Project();
        project.setIssueTemplateId(templateId);
        ProjectExample example = new ProjectExample();
        example.createCriteria()
                .andIssueTemplateIdEqualTo(originId)
                .andIdEqualTo(projectId);
        projectMapper.updateByExampleSelective(project, example);
    }

    /**
     * 把原来为系统模板的项目模板设置成新的模板
     * 只设置改工作空间下的
     *
     * @param originId
     * @param templateId
     * @param projectId
     */
    public void updateCaseTemplate(String originId, String templateId, String projectId) {
        extProjectMapper.updateUseDefaultCaseTemplateProject(originId, templateId, projectId);
    }

    private void deleteLoadTestResourcesByProjectId(String projectId) {
        LoadTestExample loadTestExample = new LoadTestExample();
        loadTestExample.createCriteria().andProjectIdEqualTo(projectId);
        List<LoadTest> loadTests = loadTestMapper.selectByExample(loadTestExample);
        List<String> loadTestIdList = loadTests.stream().map(LoadTest::getId).collect(Collectors.toList());
        loadTestIdList.forEach(loadTestId -> {
            DeleteTestPlanRequest deleteTestPlanRequest = new DeleteTestPlanRequest();
            deleteTestPlanRequest.setId(loadTestId);
            deleteTestPlanRequest.setForceDelete(true);
            performanceTestService.delete(deleteTestPlanRequest);
            LoadTestReportExample loadTestReportExample = new LoadTestReportExample();
            loadTestReportExample.createCriteria().andTestIdEqualTo(loadTestId);
            List<LoadTestReport> loadTestReports = loadTestReportMapper.selectByExample(loadTestReportExample);
            if (!loadTestReports.isEmpty()) {
                List<String> reportIdList = loadTestReports.stream().map(LoadTestReport::getId).collect(Collectors.toList());
                // delete load_test_report
                reportIdList.forEach(reportId -> performanceReportService.deleteReport(reportId));
            }
        });
        //删除分享报告时间
        delReportTime(projectId, "PERFORMANCE");
    }

    private void delReportTime(String projectId, String type) {
        ProjectApplicationExample projectApplicationExample = new ProjectApplicationExample();
        projectApplicationExample.createCriteria().andProjectIdEqualTo(projectId).andTypeEqualTo(type);
        projectApplicationMapper.deleteByExample(projectApplicationExample);
    }

    private void deleteTrackResourceByProjectId(String projectId) {
        List<String> testPlanIds = testPlanProjectService.getPlanIdByProjectId(projectId);
        if (!CollectionUtils.isEmpty(testPlanIds)) {
            testPlanIds.forEach(testPlanId -> {
                testPlanService.deleteTestPlan(testPlanId);
            });
        }
        testCaseService.deleteTestCaseByProjectId(projectId);
        //删除分享报告时间
        delReportTime(projectId, "TRACK");
    }

    private void deleteAPIResourceByProjectId(String projectId) {
        QueryAPITestRequest request = new QueryAPITestRequest();
        request.setProjectId(projectId);
        apiTestService.list(request).forEach(test -> {
            DeleteAPITestRequest deleteAPITestRequest = new DeleteAPITestRequest();
            deleteAPITestRequest.setId(test.getId());
            deleteAPITestRequest.setForceDelete(true);
            apiTestService.delete(deleteAPITestRequest);
        });
    }

    public void updateProject(AddProjectRequest project) {
        project.setCreateTime(null);
        project.setCreateUser(null);
        project.setUpdateTime(System.currentTimeMillis());
        checkProjectExist(project);
        projectMapper.updateByPrimaryKeySelective(project);
    }

    public void addOrUpdateCleanUpSchedule(AddProjectRequest project) {
        Boolean cleanTrackReport = project.getCleanTrackReport();
        Boolean cleanApiReport = project.getCleanApiReport();
        Boolean cleanLoadReport = project.getCleanLoadReport();
        LogUtil.info("clean track/api/performance report: " + cleanTrackReport + "/" + cleanApiReport + "/" + cleanLoadReport);
        // 未设置则不更新定时任务
        if (cleanTrackReport == null && cleanApiReport == null && cleanLoadReport == null) {
            return;
        }
        String projectId = project.getId();
        Boolean enable = BooleanUtils.isTrue(cleanTrackReport) ||
                BooleanUtils.isTrue(cleanApiReport) ||
                BooleanUtils.isTrue(cleanLoadReport);
        Schedule schedule = scheduleService.getScheduleByResource(projectId, ScheduleGroup.CLEAN_UP_REPORT.name());
        if (schedule != null && StringUtils.isNotBlank(schedule.getId())) {
            schedule.setEnable(enable);
            scheduleService.editSchedule(schedule);
            scheduleService.addOrUpdateCronJob(schedule,
                    CleanUpReportJob.getJobKey(projectId),
                    CleanUpReportJob.getTriggerKey(projectId),
                    CleanUpReportJob.class);
        } else {
            ScheduleRequest request = new ScheduleRequest();
            request.setName("Clean Report Job");
            request.setResourceId(projectId);
            request.setKey(projectId);
            request.setProjectId(projectId);
            request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
            request.setEnable(enable);
            request.setUserId(SessionUtils.getUserId());
            request.setGroup(ScheduleGroup.CLEAN_UP_REPORT.name());
            request.setType(ScheduleType.CRON.name());
            // 每天凌晨2点执行清理任务
            request.setValue("0 0 2 * * ?");
            request.setJob(CleanUpReportJob.class.getName());
            scheduleService.addSchedule(request);
            scheduleService.addOrUpdateCronJob(request,
                    CleanUpReportJob.getJobKey(projectId),
                    CleanUpReportJob.getTriggerKey(projectId),
                    CleanUpReportJob.class);
        }
    }

    private boolean isMockTcpPortIsInRange(int port) {
        boolean inRange = false;
        if (StringUtils.isNotEmpty(this.tcpMockPorts)) {
            try {
                if (this.tcpMockPorts.contains("-")) {
                    String[] tcpMockPortArr = this.tcpMockPorts.split("-");
                    int num1 = Integer.parseInt(tcpMockPortArr[0]);
                    int num2 = Integer.parseInt(tcpMockPortArr[1]);

                    int startNum = num1 > num2 ? num2 : num1;
                    int endNum = num1 < num2 ? num2 : num1;

                    if (port < startNum || port > endNum) {
                        inRange = false;
                    } else {
                        inRange = true;
                    }
                } else {
                    int tcpPortConfigNum = Integer.parseInt(this.tcpMockPorts);
                    if (port == tcpPortConfigNum) {
                        inRange = true;
                    }
                }
            } catch (Exception e) {
            }
        }
        return inRange;
    }

    public void checkMockTcpPort(int port) {
        if (StringUtils.isNotEmpty(this.tcpMockPorts)) {
            try {
                if (this.tcpMockPorts.contains("-")) {
                    String[] tcpMockPortArr = this.tcpMockPorts.split("-");
                    int num1 = Integer.parseInt(tcpMockPortArr[0]);
                    int num2 = Integer.parseInt(tcpMockPortArr[1]);

                    int startNum = num1 > num2 ? num2 : num1;
                    int endNum = num1 < num2 ? num2 : num1;

                    if (port < startNum || port > endNum) {
                        MSException.throwException("Tcp port is not in [" + this.tcpMockPorts + "]");
                    }
                } else {
                    int tcpPortConfigNum = Integer.parseInt(this.tcpMockPorts);
                    if (port != tcpPortConfigNum) {
                        MSException.throwException("Tcp port is not equals [" + this.tcpMockPorts + "]");
                    }
                }
            } catch (Exception e) {
                String errorMsg = e.getMessage();
                if (!errorMsg.startsWith("Tcp")) {
                    MSException.throwException("Tcp port config is error!");
                } else {
                    MSException.throwException(errorMsg);
                }
            }
        } else {
            MSException.throwException("Tcp port config is error!");
        }
    }

    public void checkProjectTcpPort(AddProjectRequest project) {
        //判断端口是否重复
        if (project.getMockTcpPort() != null && project.getMockTcpPort().intValue() != 0) {
            String projectId = StringUtils.isEmpty(project.getId()) ? "" : project.getId();
            ProjectApplicationExample example = new ProjectApplicationExample();
            example.createCriteria().andTypeEqualTo(ProjectApplicationType.MOCK_TCP_PORT.name())
                    .andTypeValueEqualTo(String.valueOf(project.getMockTcpPort()))
                    .andProjectIdNotEqualTo(projectId);
            if (projectApplicationMapper.countByExample(example) > 0) {
                MSException.throwException("TCP Port is not unique！");
            }
        }
    }

    private void checkProjectExist(Project project) {
        if (project.getName() != null) {
            ProjectExample example = new ProjectExample();
            example.createCriteria()
                    .andNameEqualTo(project.getName())
                    .andWorkspaceIdEqualTo(SessionUtils.getCurrentWorkspaceId())
                    .andIdNotEqualTo(project.getId());
            if (projectMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("project_name_already_exists"));
            }
        }
    }

    public List<Project> listAll() {
        return projectMapper.selectByExample(null);
    }

    public List<Project> getRecentProjectList(ProjectRequest request) {
        ProjectExample example = new ProjectExample();
        ProjectExample.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(request.getWorkspaceId())) {
            criteria.andWorkspaceIdEqualTo(request.getWorkspaceId());
        }
        // 按照修改时间排序
        example.setOrderByClause("update_time desc");
        return projectMapper.selectByExample(example);
    }

    public Project getProjectById(String id) {
        Project project = projectMapper.selectByPrimaryKey(id);
        if (project != null) {
            String createUser = project.getCreateUser();
            if (StringUtils.isNotBlank(createUser)) {
                User user = userMapper.selectByPrimaryKey(createUser);
                if (user != null) {
                    project.setCreateUser(user.getName());
                }
            }
        }
        return project;
    }


    public boolean isThirdPartTemplate(Project project) {
        if (project.getThirdPartTemplate() != null && project.getThirdPartTemplate()
                && project.getPlatform().equals(IssuesManagePlatform.Jira.name())) {
            return true;
        }
        return false;
    }

    public boolean useCustomNum(String projectId) {
        return useCustomNum(this.getProjectById(projectId));
    }

    public boolean useCustomNum(Project project) {
        if (project != null) {
            ProjectConfig config = projectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.CASE_CUSTOM_NUM.name());
            Boolean customNum = config.getCaseCustomNum();
            // 未开启自定义ID
            if (!customNum) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public List<Project> getByCaseTemplateId(String templateId) {
        ProjectExample example = new ProjectExample();
        example.createCriteria()
                .andCaseTemplateIdEqualTo(templateId);
        return projectMapper.selectByExample(example);
    }

    public List<Project> getByIssueTemplateId(String templateId) {
        ProjectExample example = new ProjectExample();
        example.createCriteria()
                .andIssueTemplateIdEqualTo(templateId);
        return projectMapper.selectByExample(example);
    }

    public List<FileMetadata> uploadFiles(String projectId, List<MultipartFile> files) {
        List<FileMetadata> result = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                QueryProjectFileRequest request = new QueryProjectFileRequest();
                request.setName(file.getOriginalFilename());
                if (CollectionUtils.isEmpty(fileService.getProjectFiles(projectId, request))) {
                    result.add(fileService.saveFile(file, projectId));
                } else {
                    MSException.throwException(Translator.get("project_file_already_exists"));
                }
            }
        }
        return result;
    }

    public FileMetadata updateFile(String fileId, MultipartFile file) {
        QueryProjectFileRequest request = new QueryProjectFileRequest();
        request.setName(file.getOriginalFilename());
        FileMetadata fileMetadata = fileService.getFileMetadataById(fileId);
        if (fileMetadata != null) {
            fileMetadata.setSize(file.getSize());
            fileMetadata.setUpdateTime(System.currentTimeMillis());
            fileService.updateFileMetadata(fileMetadata);
            try {
                fileService.setFileContent(fileId, file.getBytes());
            } catch (IOException e) {
                MSException.throwException(e);
            }
        }
        return fileMetadata;
    }

    public void deleteFile(String fileId) {
        LoadTestFileExample example1 = new LoadTestFileExample();
        example1.createCriteria().andFileIdEqualTo(fileId);
        List<LoadTestFile> loadTestFiles = loadTestFileMapper.selectByExample(example1);
        String errorMessage = "";
        if (loadTestFiles.size() > 0) {
            List<String> testIds = loadTestFiles.stream().map(LoadTestFile::getTestId).distinct().collect(Collectors.toList());
            LoadTestExample example = new LoadTestExample();
            example.createCriteria().andIdIn(testIds);
            List<LoadTest> loadTests = loadTestMapper.selectByExample(example);
            errorMessage += Translator.get("load_test") + ": " + StringUtils.join(loadTests.stream().map(LoadTest::getName).toArray(), ",");
            errorMessage += "\n";
        }
        ApiTestFileExample example2 = new ApiTestFileExample();
        example2.createCriteria().andFileIdEqualTo(fileId);
        List<ApiTestFile> apiTestFiles = apiTestFileMapper.selectByExample(example2);
        if (apiTestFiles.size() > 0) {
            List<String> testIds = apiTestFiles.stream().map(ApiTestFile::getTestId).distinct().collect(Collectors.toList());
            LoadTestExample example = new LoadTestExample();
            example.createCriteria().andIdIn(testIds);
            QueryAPITestRequest request = new QueryAPITestRequest();
            request.setIds(testIds);
            List<ApiTest> apiTests = apiTestService.listByIds(request);
            errorMessage += Translator.get("api_test") + ": " + StringUtils.join(apiTests.stream().map(ApiTest::getName).toArray(), ",");
        }
        if (StringUtils.isNotBlank(errorMessage)) {
            MSException.throwException(errorMessage + Translator.get("project_file_in_use"));
        }
        fileService.deleteFileById(fileId);
    }

    public String getLogDetails(String id) {
        Project project = projectMapper.selectByPrimaryKey(id);
        if (project != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(project, SystemReference.projectColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(project.getId()), project.getId(), project.getName(), project.getCreateUser(), columns);
            return JSON.toJSONString(details);
        } else {
            FileMetadata fileMetadata = fileService.getFileMetadataById(id);
            if (fileMetadata != null) {
                List<DetailColumn> columns = ReflexObjectUtil.getColumns(fileMetadata, SystemReference.projectColumns);
                OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(fileMetadata.getId()), fileMetadata.getProjectId(), fileMetadata.getName(), null, columns);
                return JSON.toJSONString(details);
            }
        }
        return null;
    }

    public void updateMember(WorkspaceMemberDTO memberDTO) {
        String projectId = memberDTO.getProjectId();
        String userId = memberDTO.getId();
        // 已有角色
        List<Group> memberGroups = extUserGroupMapper.getProjectMemberGroups(projectId, userId);
        // 修改后的角色
        List<String> groups = memberDTO.getGroupIds();
        List<String> allGroupIds = memberGroups.stream().map(Group::getId).collect(Collectors.toList());
        // 更新用户时添加了角色
        for (int i = 0; i < groups.size(); i++) {
            if (checkSourceRole(projectId, userId, groups.get(i)) == 0) {
                UserGroup userGroup = new UserGroup();
                userGroup.setId(UUID.randomUUID().toString());
                userGroup.setUserId(userId);
                userGroup.setGroupId(groups.get(i));
                userGroup.setSourceId(projectId);
                userGroup.setCreateTime(System.currentTimeMillis());
                userGroup.setUpdateTime(System.currentTimeMillis());
                userGroupMapper.insertSelective(userGroup);
            }
        }
        allGroupIds.removeAll(groups);
        if (allGroupIds.size() > 0) {
            UserGroupExample userGroupExample = new UserGroupExample();
            userGroupExample.createCriteria().andUserIdEqualTo(userId)
                    .andSourceIdEqualTo(projectId)
                    .andGroupIdIn(allGroupIds);
            userGroupMapper.deleteByExample(userGroupExample);
        }
    }

    public String getLogDetails(WorkspaceMemberDTO memberDTO) {
        String userId = memberDTO.getId();
        // 已有角色
        List<DetailColumn> columns = new LinkedList<>();
        // 已有角色
        List<Group> memberGroups = extUserGroupMapper.getProjectMemberGroups(memberDTO.getProjectId(), userId);
        List<String> names = memberGroups.stream().map(Group::getName).collect(Collectors.toList());
        List<String> ids = memberGroups.stream().map(Group::getId).collect(Collectors.toList());
        DetailColumn column = new DetailColumn("成员角色", "userRoles", String.join(",", names), null);
        columns.add(column);
        OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), memberDTO.getProjectId(), "用户 " + userId + " 修改角色为：" + String.join(",", names), null, columns);
        return JSON.toJSONString(details);

    }

    public Integer checkSourceRole(String workspaceId, String userId, String roleId) {
        return extUserGroupMapper.checkSourceRole(workspaceId, userId, roleId);
    }

    public String getSystemIdByProjectId(String projectId) {
        return extProjectMapper.getSystemIdByProjectId(projectId);
    }

    public Project findBySystemId(String systemId) {
        ProjectExample example = new ProjectExample();
        example.createCriteria().andSystemIdEqualTo(systemId);
        List<Project> returnList = projectMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(returnList)) {
            return null;
        } else {
            return returnList.get(0);
        }
    }

    public List<String> getProjectIds() {
        return extProjectMapper.getProjectIds();
    }

    public List<Project> getProjectForCustomField(String workspaceId) {
        return extProjectMapper.getProjectForCustomField(workspaceId);
    }

    public Map<String, Project> queryNameByIds(List<String> ids) {
        return extProjectMapper.queryNameByIds(ids);
    }

    public void openMockTcp(Project project) {
        if (project == null) {
            MSException.throwException("Project not found!");
        } else {
            ProjectConfig config = projectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.MOCK_TCP_PORT.name());
            Integer mockPort = config.getMockTcpPort();
            if (mockPort == null) {
                MSException.throwException("Mock tcp port is not Found!");
            } else {
                TCPPool.createTcp(mockPort);
            }
        }
    }

    public void reloadMockTcp(Project project, int oldPort) {
        this.closeMockTcp(oldPort);
        this.openMockTcp(project);
    }

    public void closeMockTcp(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        this.closeMockTcp(project);
    }

    public void closeMockTcp(Project project) {
        if (project == null) {
            MSException.throwException("Project not found!");
        } else {
            ProjectConfig config = projectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.MOCK_TCP_PORT.name());
            Integer mockPort = config.getMockTcpPort();
            if (mockPort == null) {
                MSException.throwException("Mock tcp port is not Found!");
            } else {
                this.closeMockTcp(mockPort);
            }
        }
    }

    public void closeMockTcp(int tcpPort) {
        if (tcpPort != 0) {
            TCPPool.closeTcp(tcpPort);
        }
    }

    /**
     * 检查状态为开启的TCP-Mock服务端口
     */
    public void initMockTcpService() {
        try {
            ProjectApplicationExample pae = new ProjectApplicationExample();
            pae.createCriteria().andTypeEqualTo(ProjectApplicationType.MOCK_TCP_OPEN.name())
                    .andTypeValueEqualTo(String.valueOf(true));
            pae.or().andTypeEqualTo(ProjectApplicationType.MOCK_TCP_PORT.name())
                    .andTypeValueEqualTo(String.valueOf(0));
            List<ProjectApplication> projectApplications = projectApplicationMapper.selectByExample(pae);
            List<String> projectIds = projectApplications.stream().map(ProjectApplication::getProjectId).collect(Collectors.toList());
            List<Integer> openedPortList = new ArrayList<>();
            for (String projectId : projectIds) {
                ProjectConfig config = projectApplicationService.getSpecificTypeValue(projectId, ProjectApplicationType.MOCK_TCP_PORT.name());
                Integer mockPort = config.getMockTcpPort();
                boolean isPortInRange = this.isMockTcpPortIsInRange(mockPort);
                if (isPortInRange && !openedPortList.contains(mockPort)) {
                    openedPortList.add(mockPort);
                    Project project = new Project();
                    project.setId(projectId);
                    this.openMockTcp(project);
                } else {
                    if (openedPortList.contains(mockPort)) {
                        projectApplicationService.createOrUpdateConfig(projectId, ProjectApplicationType.MOCK_TCP_PORT.name(), String.valueOf(mockPort));
                    }
                    projectApplicationService.createOrUpdateConfig(projectId, ProjectApplicationType.MOCK_TCP_OPEN.name(), String.valueOf(false));
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    public String genTcpMockPort(String id) {
        int returnPort = 0;
        Project project = projectMapper.selectByPrimaryKey(id);
        ProjectConfig config = projectApplicationService.getSpecificTypeValue(id, ProjectApplicationType.MOCK_TCP_PORT.name());
        Integer mockPort = config.getMockTcpPort();
        if (project != null && mockPort != 0) {
            if (this.isMockTcpPortIsInRange(mockPort)) {
                returnPort = mockPort;
            }
        } else {
            if (StringUtils.isNotEmpty(this.tcpMockPorts)) {
                List<Integer> portInRange = new ArrayList<>();
                ProjectApplicationExample example = new ProjectApplicationExample();
                example.createCriteria().andTypeEqualTo(ProjectApplicationType.MOCK_TCP_PORT.name());
                List<ProjectApplication> projectApplications = projectApplicationMapper.selectByExample(example);
                List<Integer> tcpPortInDataBase = projectApplications.stream()
                        .map(pa -> {
                            String value = pa.getTypeValue();
                            int p = 0;
                            try {
                                p = Integer.parseInt(value);
                            } catch (Exception e) {

                            }
                            return p;
                        })
                        .distinct()
                        .collect(Collectors.toList());
                tcpPortInDataBase.remove(new Integer(0));
                for (Integer port : tcpPortInDataBase) {
                    if (this.isMockTcpPortIsInRange(port)) {
                        portInRange.add(port);
                    }
                }

                try {
                    if (this.tcpMockPorts.contains("-")) {
                        String[] tcpMockPortArr = this.tcpMockPorts.split("-");
                        int num1 = Integer.parseInt(tcpMockPortArr[0]);
                        int num2 = Integer.parseInt(tcpMockPortArr[1]);

                        int startNum = num1 > num2 ? num2 : num1;
                        int endNum = num1 < num2 ? num2 : num1;

                        for (int i = startNum; i <= endNum; i++) {
                            if (!portInRange.contains(i)) {
                                returnPort = i;
                                break;
                            }
                        }
                    } else {
                        int tcpPortConfigNum = Integer.parseInt(this.tcpMockPorts);
                        if (!portInRange.contains(tcpPortConfigNum)) {
                            returnPort = tcpPortConfigNum;
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
        if (returnPort == 0) {
            MSException.throwException("无可用TCP端口");
        }
        return String.valueOf(returnPort);
    }

    public long getProjectSize() {
        return projectMapper.countByExample(new ProjectExample());
    }

    public long getProjectMemberSize(String id) {
        return extProjectMapper.getProjectMemberSize(id);
    }

    public int getProjectBugSize(String projectId) {
        return extProjectMapper.getProjectPlanBugSize(projectId);
    }

    public boolean isVersionEnable(String projectId) {
        return extProjectVersionMapper.isVersionEnable(projectId);
    }

    public void cleanUpTrackReport(long time, String projectId) {
        if (StringUtils.isBlank(projectId)) {
            return;
        }
        LogUtil.info("clean up track plan report before: " + DateUtils.getTimeString(time) + ", resourceId : " + projectId);
        testPlanReportService.cleanUpReport(time, projectId);
    }

    public void cleanUpApiReport(long time, String projectId) {
        if (StringUtils.isBlank(projectId)) {
            return;
        }
        LogUtil.info("clean up api report before: " + DateUtils.getTimeString(time) + ", resourceId : " + projectId);
        apiScenarioReportService.cleanUpReport(time, projectId);
    }

    public void cleanUpLoadReport(long time, String projectId) {
        if (StringUtils.isBlank(projectId)) {
            return;
        }
        LogUtil.info("clean up load report before: " + DateUtils.getTimeString(time) + ", resourceId : " + projectId);
        performanceReportService.cleanUpReport(time, projectId);
    }

    public void checkProjectIsRepeatable(String projectId) {
        Project project = this.getProjectById(projectId);
        if (project == null) {
            MSException.throwException(Translator.get("cannot_find_project"));
        } else {
            ProjectConfig config = projectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.URL_REPEATABLE.name());
            boolean urlRepeat = config.getUrlRepeatable();
            if (!urlRepeat) {
                MSException.throwException(Translator.get("project_repeatable_is_false"));
            }
        }
    }
}
