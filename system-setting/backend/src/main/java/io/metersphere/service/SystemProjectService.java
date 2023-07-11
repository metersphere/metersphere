package io.metersphere.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.BaseProjectMapper;
import io.metersphere.base.mapper.ext.BaseUserGroupMapper;
import io.metersphere.base.mapper.ext.BaseUserMapper;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.commons.constants.ProjectApplicationType;
import io.metersphere.commons.constants.UserGroupConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.ProjectDTO;
import io.metersphere.dto.WorkspaceMemberDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import io.metersphere.metadata.service.FileMetadataService;
import io.metersphere.quota.service.BaseQuotaService;
import io.metersphere.request.AddProjectRequest;
import io.metersphere.request.ProjectRequest;
import io.metersphere.xpack.api.service.ProjectApplicationSyncService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class SystemProjectService {
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private BaseProjectMapper baseProjectMapper;
    @Resource
    private FileMetadataService fileMetadataService;
    @Resource
    private UserGroupMapper userGroupMapper;
    @Resource
    private BaseUserGroupMapper baseUserGroupMapper;
    @Resource
    private BaseUserMapper baseUserMapper;
    @Resource
    private UserMapper userMapper;
    @Value("${tcp.mock.port}")
    private String tcpMockPorts;
    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;
    @Resource
    private ProjectVersionMapper projectVersionMapper;
    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;
    @Resource
    private EnvironmentGroupProjectService environmentGroupProjectService;
    @Resource
    private BaseScheduleService baseScheduleService;
    @Resource
    private BaseQuotaService baseQuotaService;

    public Project addProject(AddProjectRequest project) {
        CommonBeanFactory.getBean(SystemProjectService.class).saveProject(project);
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
        baseUserMapper.updateLastProjectIdIfNull(project.getId(), SessionUtils.getUserId());

        // 设置默认的通知
        baseProjectMapper.setDefaultMessageTask(project.getId());

        baseQuotaService.projectUseDefaultQuota(project.getId());

        // 创建默认版本
        addProjectVersion(project);
        // 初始化项目应用管理
        initProjectApplication(project.getId());
        // 初始化项目默认节点
        kafkaTemplate.send(KafkaTopicConstants.PROJECT_CREATED_TOPIC, project.getId());
        LogUtil.info("send create_project message, project id: " + project.getId());
        // 初始化项目资源池
        systemParameterService.saveProjectApplication(project.getId());
        return project;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void saveProject(AddProjectRequest project) {
        if (StringUtils.isBlank(project.getName())) {
            MSException.throwException(Translator.get("project_name_is_null"));
        }
        ProjectExample example = new ProjectExample();
        example.createCriteria().andWorkspaceIdEqualTo(project.getWorkspaceId()).andNameEqualTo(project.getName());
        if (projectMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("project_name_already_exists"));
        }
        baseQuotaService.checkWorkspaceProject(project.getWorkspaceId());

        if (project.getMockTcpPort() != null && project.getMockTcpPort() > 0) {
            this.checkMockTcpPort(project.getMockTcpPort());
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
    }

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
        String maxSystemIdInDb = baseProjectMapper.getMaxSystemId();
        String systemId = "10001";
        if (StringUtils.isNotEmpty(maxSystemIdInDb)) {
            systemId = String.valueOf(Long.parseLong(maxSystemIdInDb) + 1);
        }
        return systemId;
    }

    public List<ProjectDTO> getProjectList(ProjectRequest request) {
        if (StringUtils.isNotBlank(request.getName())) {
            request.setName(StringUtils.wrapIfMissing(request.getName(), "%"));
        }
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        return baseProjectMapper.getProjectWithWorkspace(request);
    }

    public void deleteProject(String projectId) {

        // 删除项目发送通知
        kafkaTemplate.send(KafkaTopicConstants.PROJECT_DELETED_TOPIC, projectId);
        LogUtil.info("send delete_project message, project id: " + projectId);

        // User Group
        deleteProjectUserGroup(projectId);

        environmentGroupProjectService.deleteRelateProject(projectId);

        // delete project
        projectMapper.deleteByPrimaryKey(projectId);

        baseScheduleService.deleteByProjectId(projectId);
    }

    private void deleteProjectUserGroup(String projectId) {
        UserGroupExample userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andSourceIdEqualTo(projectId);
        userGroupMapper.deleteByExample(userGroupExample);
    }

    public void updateProject(AddProjectRequest project) {
        project.setCreateTime(null);
        project.setCreateUser(null);
        project.setUpdateTime(System.currentTimeMillis());
        checkProjectExist(project);
        projectMapper.updateByPrimaryKeySelective(project);
    }

    public void checkMockTcpPort(int port) {
        if (StringUtils.isNotEmpty(this.tcpMockPorts)) {
            try {
                if (this.tcpMockPorts.contains("-")) {
                    String[] tcpMockPortArr = this.tcpMockPorts.split("-");
                    int num1 = Integer.parseInt(tcpMockPortArr[0]);
                    int num2 = Integer.parseInt(tcpMockPortArr[1]);

                    int startNum = Math.min(num1, num2);
                    int endNum = Math.max(num1, num2);

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

    private void checkProjectExist(Project project) {
        if (project.getName() != null) {
            ProjectExample example = new ProjectExample();
            example.createCriteria().andNameEqualTo(project.getName()).andWorkspaceIdEqualTo(SessionUtils.getCurrentWorkspaceId()).andIdNotEqualTo(project.getId());
            if (projectMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("project_name_already_exists"));
            }
        }
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
        for (String group : groups) {
            if (checkSourceRole(projectId, userId, group) == 0) {
                UserGroup userGroup = new UserGroup();
                userGroup.setId(UUID.randomUUID().toString());
                userGroup.setUserId(userId);
                userGroup.setGroupId(group);
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

    public long getProjectMemberSize(String id) {
        return baseProjectMapper.getProjectMemberSize(id);
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
        projectApplication.setType(ProjectApplicationType.UI_SHARE_REPORT_TIME.toString());
        projectApplicationMapper.insert(projectApplication);
        projectApplication.setType(ProjectApplicationType.CASE_CUSTOM_NUM.toString());
        projectApplication.setTypeValue(Boolean.FALSE.toString());
        projectApplicationMapper.insert(projectApplication);
        ProjectApplicationSyncService projectApplicationSyncService = CommonBeanFactory.getBean(ProjectApplicationSyncService.class);
        if (projectApplicationSyncService != null) {
            try {
                projectApplication = projectApplicationSyncService.initProjectApplicationAboutWorkstation(projectApplication);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            projectApplicationMapper.insert(projectApplication);
        }
    }

    public List<Project> getProjects() {
        return baseProjectMapper.getProjects();
    }
}
