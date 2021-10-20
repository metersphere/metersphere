package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.DeleteAPITestRequest;
import io.metersphere.api.dto.QueryAPITestRequest;
import io.metersphere.api.service.APITestService;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.api.service.ApiTestDelService;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.api.tcp.TCPPool;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtProjectMapper;
import io.metersphere.base.mapper.ext.ExtUserGroupMapper;
import io.metersphere.base.mapper.ext.ExtUserMapper;
import io.metersphere.commons.constants.UserGroupConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.ProjectRequest;
import io.metersphere.dto.ProjectDTO;
import io.metersphere.dto.WorkspaceMemberDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import io.metersphere.performance.request.DeleteTestPlanRequest;
import io.metersphere.performance.request.QueryProjectFileRequest;
import io.metersphere.performance.service.PerformanceReportService;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.track.service.TestCaseService;
import io.metersphere.track.service.TestPlanProjectService;
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

    public Project addProject(Project project) {
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
        project.setId(UUID.randomUUID().toString());

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

        return project;
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

    public void updateIssueTemplate(String originId, String templateId) {
        Project project = new Project();
        project.setIssueTemplateId(templateId);
        ProjectExample example = new ProjectExample();
        example.createCriteria()
                .andIssueTemplateIdEqualTo(originId);
        projectMapper.updateByExampleSelective(project, example);
    }

    public void updateCaseTemplate(String originId, String templateId) {
        Project project = new Project();
        project.setCaseTemplateId(templateId);
        ProjectExample example = new ProjectExample();
        example.createCriteria()
                .andCaseTemplateIdEqualTo(originId);
        projectMapper.updateByExampleSelective(project, example);
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
    }

    private void deleteTrackResourceByProjectId(String projectId) {
        List<String> testPlanIds = testPlanProjectService.getPlanIdByProjectId(projectId);
        if (!CollectionUtils.isEmpty(testPlanIds)) {
            testPlanIds.forEach(testPlanId -> {
                testPlanService.deleteTestPlan(testPlanId);
            });
        }
        testCaseService.deleteTestCaseByProjectId(projectId);
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

    public void updateProject(Project project) {
        //查询之前的TCP端口，用于检查是否需要开启/关闭 TCP接口
        int lastTcpNum = 0;
        Project oldData = projectMapper.selectByPrimaryKey(project.getId());
        if (oldData != null && oldData.getMockTcpPort() != null) {
            lastTcpNum = oldData.getMockTcpPort().intValue();
        }

        if (project.getMockTcpPort().intValue() > 0) {
            this.checkMockTcpPort(project.getMockTcpPort().intValue());
        }

        this.checkProjectTcpPort(project);

        project.setCreateTime(null);
        project.setUpdateTime(System.currentTimeMillis());
        checkProjectExist(project);
        if (BooleanUtils.isTrue(project.getCustomNum())) {
            testCaseService.updateTestCaseCustomNumByProjectId(project.getId());
        }
        projectMapper.updateByPrimaryKeySelective(project);

        //检查Mock环境是否需要同步更新
        ApiTestEnvironmentService apiTestEnvironmentService = CommonBeanFactory.getBean(ApiTestEnvironmentService.class);
        apiTestEnvironmentService.getMockEnvironmentByProjectId(project.getId());
        //开启tcp mock
        if (project.getIsMockTcpOpen()) {
            this.reloadMockTcp(project, lastTcpNum);
        } else {
            this.closeMockTcp(project);
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

    private void checkMockTcpPort(int port) {
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

    private void checkProjectTcpPort(Project project) {
        //判断端口是否重复
        if (project.getMockTcpPort() != null && project.getMockTcpPort().intValue() != 0) {
            String projectId = StringUtils.isEmpty(project.getId()) ? "" : project.getId();
            ProjectExample example = new ProjectExample();
            example.createCriteria().andMockTcpPortEqualTo(project.getMockTcpPort()).andIdNotEqualTo(projectId);
            long countResult = projectMapper.countByExample(example);
            if (countResult > 0) {
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
        String createUser = project.getCreateUser();
        if (StringUtils.isNotBlank(createUser)) {
            User user = userMapper.selectByPrimaryKey(createUser);
            if (user != null) {
                project.setCreateUser(user.getName());
            }
        }
        return project;
    }

    public boolean useCustomNum(String projectId) {
        Project project = this.getProjectById(projectId);
        if (project != null) {
            Boolean customNum = project.getCustomNum();
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

    public Map<String, Project> queryNameByIds(List<String> ids) {
        return extProjectMapper.queryNameByIds(ids);
    }

    public void openMockTcp(Project project) {
        if (project == null) {
            MSException.throwException("Project not found!");
        } else {
            if (project.getMockTcpPort() == null) {
                MSException.throwException("Mock tcp port is not Found!");
            } else {
                TCPPool.createTcp(project.getMockTcpPort());
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
            if (project.getMockTcpPort() == null) {
                MSException.throwException("Mock tcp port is not Found!");
            } else {
                this.closeMockTcp(project.getMockTcpPort().intValue());
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
            ProjectExample example = new ProjectExample();
            Integer portInteger = new Integer(0);
            Boolean statusBoolean = new Boolean(true);
            example.createCriteria().andIsMockTcpOpenEqualTo(statusBoolean).andMockTcpPortNotEqualTo(portInteger);
            List<Project> projectList = projectMapper.selectByExample(example);

            List<Integer> opendPortList = new ArrayList<>();
            for (Project p : projectList) {
                boolean isPortInRange = this.isMockTcpPortIsInRange(p.getMockTcpPort());
                if (isPortInRange && !opendPortList.contains(p.getMockTcpPort())) {
                    opendPortList.add(p.getMockTcpPort());
                    this.openMockTcp(p);
                } else {
                    if (opendPortList.contains(p.getMockTcpPort())) {
                        p.setMockTcpPort(0);
                    }
                    p.setIsMockTcpOpen(false);
                    projectMapper.updateByPrimaryKeySelective(p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String genTcpMockPort(String id) {
        int returnPort = 0;
        Project project = projectMapper.selectByPrimaryKey(id);
        if (project != null && project.getMockTcpPort() != null && project.getMockTcpPort().intValue() != 0) {
            if (this.isMockTcpPortIsInRange(project.getMockTcpPort().intValue())) {
                returnPort = project.getMockTcpPort();
            }
        } else {
            if (StringUtils.isNotEmpty(this.tcpMockPorts)) {
                List<Integer> portInRange = new ArrayList<>();
                List<Integer> tcpPortInDataBase = extProjectMapper.selectTcpPorts();
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
}
