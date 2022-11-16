package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.ProjectConfig;
import io.metersphere.dto.ProjectDTO;
import io.metersphere.dto.WorkspaceMemberDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.job.schedule.CleanUpReportJob;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import io.metersphere.metadata.service.FileMetadataService;
import io.metersphere.request.*;
import io.metersphere.request.member.AddMemberRequest;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
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
    private FileMetadataService fileMetadataService;
    @Resource
    private UserGroupMapper userGroupMapper;
    @Resource
    private BaseUserGroupMapper baseUserGroupMapper;
    @Resource
    private BaseUserMapper baseUserMapper;
    @Resource
    private BaseProjectMapper baseProjectMapper;
    @Resource
    private UserMapper userMapper;
    @Value("${tcp.mock.port}")
    private String tcpMockPorts;
    @Resource
    private ExtProjectVersionMapper extProjectVersionMapper;
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;
    @Resource
    private ProjectVersionMapper projectVersionMapper;
    @Resource
    private MicroService microService;
    @Resource
    private BaseScheduleService baseScheduleService;
    @Resource
    private ProjectApplicationService projectApplicationService;

    public void addProjectVersion(Project project) {
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

        String name = projectVersion.getName();
        ProjectVersionExample example = new ProjectVersionExample();
        example.createCriteria().andProjectIdEqualTo(projectVersion.getProjectId()).andNameEqualTo(name);
        if (projectVersionMapper.countByExample(example) > 0) {
            MSException.throwException("当前版本已经存在");
        }
        projectVersion.setId(UUID.randomUUID().toString());
        projectVersion.setCreateUser(SessionUtils.getUserId());
        projectVersion.setCreateTime(System.currentTimeMillis());
        projectVersionMapper.insertSelective(projectVersion);
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

    private void deleteProjectUserGroup(String projectId) {
        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andSourceIdEqualTo(projectId);
        userGroupMapper.deleteByExample(userGroupExample);
    }

    public void updateIssueTemplate(String originId, String templateId, String projectId) {
        Project project = new Project();
        project.setIssueTemplateId(templateId);
        ProjectExample example = new ProjectExample();
        example.createCriteria().andIssueTemplateIdEqualTo(originId).andIdEqualTo(projectId);
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

    public void updateProject(AddProjectRequest project) {
        project.setCreateTime(null);
        project.setCreateUser(null);
        project.setUpdateTime(System.currentTimeMillis());
        checkProjectExist(project);
        projectMapper.updateByPrimaryKeySelective(project);
    }

    private boolean isMockTcpPortIsInRange(int port) {
        boolean inRange = false;
        if (StringUtils.isNotEmpty(this.tcpMockPorts)) {
            try {
                if (this.tcpMockPorts.contains("-")) {
                    String[] tcpMockPortArr = this.tcpMockPorts.split("-");
                    int num1 = Integer.parseInt(tcpMockPortArr[0]);
                    int num2 = Integer.parseInt(tcpMockPortArr[1]);

                    int startNum = Math.min(num1, num2);
                    int endNum = Math.max(num1, num2);

                    inRange = port >= startNum && port <= endNum;
                } else {
                    int tcpPortConfigNum = Integer.parseInt(this.tcpMockPorts);
                    if (port == tcpPortConfigNum) {
                        inRange = true;
                    }
                }
            } catch (Exception e) {
                // nothing
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
            String projectId = StringUtils.isEmpty(project.getId()) ? StringUtils.EMPTY : project.getId();
            ProjectApplicationExample example = new ProjectApplicationExample();
            example.createCriteria().andTypeEqualTo(ProjectApplicationType.MOCK_TCP_PORT.name()).andTypeValueEqualTo(String.valueOf(project.getMockTcpPort())).andProjectIdNotEqualTo(projectId);
            if (projectApplicationMapper.countByExample(example) > 0) {
                MSException.throwException(Translator.get("tcp_mock_not_unique"));
            }
        }
    }

    private void checkProjectExist(Project project) {
        if (project.getName() != null) {
            ProjectExample example = new ProjectExample();
            example.createCriteria().andNameEqualTo(project.getName()).andWorkspaceIdEqualTo(SessionUtils.getCurrentWorkspaceId()).andIdNotEqualTo(project.getId());
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
        if (project.getThirdPartTemplate() != null && project.getThirdPartTemplate() && project.getPlatform().equals(IssuesManagePlatform.Jira.name())) {
            return true;
        }
        return false;
    }

    public List<Project> getByCaseTemplateId(String templateId) {
        ProjectExample example = new ProjectExample();
        example.createCriteria().andCaseTemplateIdEqualTo(templateId);
        return projectMapper.selectByExample(example);
    }

    public List<Project> getByIssueTemplateId(String templateId) {
        ProjectExample example = new ProjectExample();
        example.createCriteria().andIssueTemplateIdEqualTo(templateId);
        return projectMapper.selectByExample(example);
    }


    public String getLogDetails(String id) {
        Project project = projectMapper.selectByPrimaryKey(id);
        if (project != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(project, SystemReference.projectColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(project.getId()), project.getId(), project.getName(), project.getCreateUser(), columns);
            return JSON.toJSONString(details);
        } else {
            FileMetadata fileMetadata = fileMetadataService.getFileMetadataById(id);
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
        List<Group> memberGroups = baseUserGroupMapper.getProjectMemberGroups(projectId, userId);
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
            userGroupExample.createCriteria().andUserIdEqualTo(userId).andSourceIdEqualTo(projectId).andGroupIdIn(allGroupIds);
            userGroupMapper.deleteByExample(userGroupExample);
        }
    }

    public String getLogDetails(WorkspaceMemberDTO memberDTO) {
        String userId = memberDTO.getId();
        // 已有角色
        List<DetailColumn> columns = new LinkedList<>();
        // 已有角色
        List<Group> memberGroups = baseUserGroupMapper.getProjectMemberGroups(memberDTO.getProjectId(), userId);
        List<String> names = memberGroups.stream().map(Group::getName).collect(Collectors.toList());
        List<String> ids = memberGroups.stream().map(Group::getId).collect(Collectors.toList());
        DetailColumn column = new DetailColumn("成员角色", "userRoles", String.join(",", names), null);
        columns.add(column);
        OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), memberDTO.getProjectId(), "用户 " + userId + " 修改角色为：" + String.join(",", names), null, columns);
        return JSON.toJSONString(details);

    }

    public Integer checkSourceRole(String workspaceId, String userId, String roleId) {
        return baseUserGroupMapper.checkSourceRole(workspaceId, userId, roleId);
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

    public Map<String, Workspace> getWorkspaceNameByProjectIds(List<String> projectIds) {
        if (projectIds.isEmpty()) {
            return new HashMap<>(0);
        }
        return extProjectMapper.queryWorkNameByProjectIds(projectIds);
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

    public List<ServiceIntegration> getAllServiceIntegration() {
        return microService.getForDataArray(MicroServiceName.SYSTEM_SETTING, "/service/integration/all", ServiceIntegration.class);
    }

    public void checkThirdProjectExist(Project project) {
        microService.postForData(MicroServiceName.TEST_TRACK, "/issues/check/third/project", project);
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
        Schedule schedule = baseScheduleService.getScheduleByResource(projectId, ScheduleGroup.CLEAN_UP_REPORT.name());
        if (schedule != null && StringUtils.isNotBlank(schedule.getId())) {
            schedule.setEnable(enable);
            baseScheduleService.editSchedule(schedule);
            baseScheduleService.addOrUpdateCronJob(schedule,
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
            baseScheduleService.addSchedule(request);
            baseScheduleService.addOrUpdateCronJob(request,
                    CleanUpReportJob.getJobKey(projectId),
                    CleanUpReportJob.getTriggerKey(projectId),
                    CleanUpReportJob.class);
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
                                // nothing
                            }
                            return p;
                        })
                        .distinct()
                        .collect(Collectors.toList());
                tcpPortInDataBase.remove(Integer.valueOf(0));
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

                        int startNum = Math.min(num1, num2);
                        int endNum = Math.max(num1, num2);

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
                    // nothing
                }
            }
        }
        if (returnPort == 0) {
            MSException.throwException(Translator.get("no_tcp_mock_port"));
        }
        return String.valueOf(returnPort);
    }

    //修改默认接口模版id
    public void updateApiTemplate(String originId, String templateId, String projectId) {
        extProjectMapper.updateUseDefaultApiTemplateProject(originId, templateId, projectId);
    }

    public List<Project> getByApiTemplateId(String templateId) {
        ProjectExample example = new ProjectExample();
        example.createCriteria()
                .andApiTemplateIdEqualTo(templateId);
        return projectMapper.selectByExample(example);
    }

    public void addProjectMember(AddMemberRequest request) {
        microService.postForData(MicroServiceName.SYSTEM_SETTING, "/user/project/member/add", request);
    }

}
