package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.UserGroupMapper;
import io.metersphere.base.mapper.UserMapper;
import io.metersphere.base.mapper.ext.BaseProjectMapper;
import io.metersphere.base.mapper.ext.BaseProjectVersionMapper;
import io.metersphere.base.mapper.ext.BaseUserGroupMapper;
import io.metersphere.base.mapper.ext.BaseUserMapper;
import io.metersphere.commons.constants.ProjectApplicationType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.ProjectConfig;
import io.metersphere.dto.ProjectDTO;
import io.metersphere.dto.WorkspaceMemberDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import io.metersphere.metadata.service.FileMetadataService;
import io.metersphere.request.AddProjectRequest;
import io.metersphere.request.ProjectRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class BaseProjectService {
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
    private UserMapper userMapper;
    @Value("${tcp.mock.port}")
    private String tcpMockPorts;
    @Resource
    private BaseProjectVersionMapper baseProjectVersionMapper;
    @Resource
    private BaseProjectApplicationService baseProjectApplicationService;
    @Resource
    private BaseUserMapper baseUserMapper;


    private String genSystemId() {
        String maxSystemIdInDb = baseProjectMapper.getMaxSystemId();
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
        return baseProjectMapper.getProjectWithWorkspace(request);
    }

    public List<Project> getUserProject(ProjectRequest request) {
        boolean isSuper = baseUserMapper.isSuperUser(SessionUtils.getUserId());
        if (isSuper) {
            ProjectExample example = new ProjectExample();
            ProjectExample.Criteria criteria = example.createCriteria();
            if (StringUtils.isNotBlank(request.getName())) {
                criteria.andNameLike(request.getName());
            }
            if (StringUtils.isNotBlank(request.getWorkspaceId())) {
                criteria.andWorkspaceIdEqualTo(request.getWorkspaceId());
            }
            return projectMapper.selectByExample(example);
        }
        if (StringUtils.isNotBlank(request.getName())) {
            request.setName(StringUtils.wrapIfMissing(request.getName(), "%"));
        }
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        return baseProjectMapper.getUserProject(request);
    }

    public List<Project> getProjectByIds(List<String> ids) {
        if (!CollectionUtils.isEmpty(ids)) {
            ProjectExample example = new ProjectExample();
            example.createCriteria().andIdIn(ids);
            return projectMapper.selectByExample(example);
        }
        return new ArrayList<>();
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
        baseProjectMapper.updateUseDefaultCaseTemplateProject(originId, templateId, projectId);
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

    public List<FileMetadata> uploadFiles(String projectId, List<MultipartFile> files) {
        return fileMetadataService.uploadFiles(projectId, files);
    }

    public FileMetadata updateFile(String fileId, MultipartFile file) {
        return fileMetadataService.updateFile(fileId, file);
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
        return baseProjectMapper.getSystemIdByProjectId(projectId);
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
        return baseProjectMapper.getProjectIds();
    }

    public List<Project> getProjectForCustomField(String workspaceId) {
        return baseProjectMapper.getProjectForCustomField(workspaceId);
    }

    public Map<String, Project> queryNameByIds(List<String> ids) {
        return baseProjectMapper.queryNameByIds(ids);
    }

    public Map<String, Workspace> getWorkspaceNameByProjectIds(List<String> projectIds) {
        if (projectIds.isEmpty()) {
            return new HashMap<>(0);
        }
        return baseProjectMapper.queryWorkNameByProjectIds(projectIds);
    }


    public long getProjectSize() {
        return projectMapper.countByExample(new ProjectExample());
    }

    public long getProjectMemberSize(String id) {
        return baseProjectMapper.getProjectMemberSize(id);
    }

    public int getProjectBugSize(String projectId) {
        return baseProjectMapper.getProjectPlanBugSize(projectId);
    }

    public boolean isVersionEnable(String projectId) {
        return baseProjectVersionMapper.isVersionEnable(projectId);
    }

    public void checkProjectIsRepeatable(String projectId) {
        Project project = this.getProjectById(projectId);
        if (project == null) {
            MSException.throwException(Translator.get("cannot_find_project"));
        } else {
            ProjectConfig config = baseProjectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.URL_REPEATABLE.name());
            boolean urlRepeat = config.getUrlRepeatable();
            if (!urlRepeat) {
                MSException.throwException(Translator.get("project_repeatable_is_false"));
            }
        }
    }

    public void deleteFile(String fileId) {
        fileMetadataService.deleteFile(fileId);
    }

    public Project selectOne() {
        return baseProjectMapper.selectOne();
    }

    public long count() {
        return baseProjectMapper.count();
    }

    public List<String> getPage(long pageNum, long pageSize) {
        return baseProjectMapper.selectPage(pageNum, pageSize);
    }
}
