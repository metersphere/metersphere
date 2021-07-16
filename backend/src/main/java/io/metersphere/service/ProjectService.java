package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.DeleteAPITestRequest;
import io.metersphere.api.dto.QueryAPITestRequest;
import io.metersphere.api.service.APITestService;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtOrganizationMapper;
import io.metersphere.base.mapper.ext.ExtProjectMapper;
import io.metersphere.base.mapper.ext.ExtUserGroupMapper;
import io.metersphere.base.mapper.ext.ExtUserMapper;
import io.metersphere.commons.constants.UserGroupConstants;
import io.metersphere.commons.exception.MSException;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
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
    private ApiAutomationService apiAutomationService;
    @Resource
    private PerformanceReportService performanceReportService;
    @Resource
    private UserGroupMapper userGroupMapper;
    @Resource
    private ExtOrganizationMapper extOrganizationMapper;
    @Resource
    private ExtUserGroupMapper extUserGroupMapper;
    @Resource
    private ExtUserMapper extUserMapper;

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
        if(StringUtils.isNotEmpty(maxSystemIdInDb)){
            systemId = String.valueOf(Long.parseLong(maxSystemIdInDb) + 1);
        }
        return systemId;
    }

    public Project checkSystemId(Project project){
        if(project!=null){
            ProjectExample example = new ProjectExample();
            example.createCriteria().andSystemIdEqualTo(project.getSystemId());
            long count = projectMapper.countByExample(example);
            if(count > 1){
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

        // User Group
        deleteProjectUserGroup(projectId);

        // delete project
        projectMapper.deleteByPrimaryKey(projectId);
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
        project.setCreateTime(null);
        project.setUpdateTime(System.currentTimeMillis());
        checkProjectExist(project);
        if (BooleanUtils.isTrue(project.getCustomNum())) {
            testCaseService.updateTestCaseCustomNumByProjectId(project.getId());
        }
        projectMapper.updateByPrimaryKeySelective(project);
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
        return projectMapper.selectByPrimaryKey(id);
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
        return extOrganizationMapper.checkSourceRole(workspaceId, userId, roleId);
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
}
