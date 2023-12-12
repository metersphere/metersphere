package io.metersphere.bug.service;

import io.metersphere.bug.domain.BugRelationCase;
import io.metersphere.bug.dto.request.BugRelatedCasePageRequest;
import io.metersphere.bug.dto.response.BugRelateCaseDTO;
import io.metersphere.bug.mapper.BugRelationCaseMapper;
import io.metersphere.bug.mapper.ExtBugRelateCaseMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.domain.ProjectVersion;
import io.metersphere.project.domain.ProjectVersionExample;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.mapper.ProjectVersionMapper;
import io.metersphere.project.service.PermissionCheckService;
import io.metersphere.sdk.constants.CaseType;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class BugRelateCaseService {

    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ProjectVersionMapper projectVersionMapper;
    @Resource
    private BugRelationCaseMapper bugRelationCaseMapper;
    @Resource
    private ExtBugRelateCaseMapper extBugRelateCaseMapper;
    @Resource
    private PermissionCheckService permissionCheckService;

    /**
     * 分页查询关联用例列表
     * @param request 请求参数
     */
    public List<BugRelateCaseDTO> page(BugRelatedCasePageRequest request) {
        List<BugRelateCaseDTO> relateCases = extBugRelateCaseMapper.list(request);
        if (CollectionUtils.isEmpty(relateCases)) {
            return new ArrayList<>();
        }
        Map<String, String> projectMap = getProjectMap(relateCases.stream().map(BugRelateCaseDTO::getProjectId).toList());
        Map<String, String> versionMap = getVersionMap(relateCases.stream().map(BugRelateCaseDTO::getVersionId).toList());
        relateCases.forEach(relateCase -> {
            relateCase.setProjectName(projectMap.get(relateCase.getProjectId()));
            relateCase.setVersionName(versionMap.get(relateCase.getVersionId()));
            relateCase.setRelateCaseTypeName(CaseType.getValue(relateCase.getRelateCaseType()));
        });
        return relateCases;
    }

    /**
     * 取消关联用例
     * @param id 关联ID
     */
    public void unRelate(String id) {
        // 只用取消关联直接关联的用例, 保留测试计划关联的用例
        BugRelationCase bugRelationCase = checkRelate(id);
        if (StringUtils.isEmpty(bugRelationCase.getTestPlanId())) {
            // 不包含测试计划关联的用例, 直接删除
            bugRelationCaseMapper.deleteByPrimaryKey(id);
        } else {
            // 包含测试计划关联的用例, 只需将直接关联CaseId置空即可
            bugRelationCase.setCaseId(null);
            bugRelationCaseMapper.updateByPrimaryKey(bugRelationCase);
        }
    }

    /**
     * 校验当前用户是否有关联用例的权限
     * @param projectId 项目ID
     * @param currentUser 当前用户
     * @param caseType 用例类型
     */
    public void checkPermission(String projectId, String currentUser, String caseType) {
        // 校验关联用例的查看权限, 目前只支持功能用例的查看权限, 后续支持除功能用例外的其他类型用例
        if (!CaseType.FUNCTIONAL_CASE.getKey().equals(caseType)) {
            // 关联的用例类型未知
            throw new MSException(Translator.get("bug_relate_case_type_unknown"));
        }
        boolean hasPermission = permissionCheckService.userHasProjectPermission(currentUser, projectId, PermissionConstants.FUNCTIONAL_CASE_READ);
        if (!hasPermission) {
            // 没有该用例的访问权限
            throw new MSException(Translator.get("bug_relate_case_permission_error"));
        }
    }

    /**
     * 校验关联用例
     * @param relateId 关联ID
     * @return 关联用例
     */
    private BugRelationCase checkRelate(String relateId) {
        BugRelationCase bugRelationCase = bugRelationCaseMapper.selectByPrimaryKey(relateId);
        if (bugRelationCase == null) {
            throw new MSException(Translator.get("bug_relate_case_not_found"));
        }
        return bugRelationCase;
    }

    /**
     * 获取项目Map
     * @param projectIds 项目ID集合
     * @return 获取项目Map
     */
    private Map<String, String> getProjectMap(List<String> projectIds) {
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andIdIn(projectIds);
        List<Project> projects = projectMapper.selectByExample(projectExample);
        return projects.stream().collect(Collectors.toMap(Project::getId, Project::getName));
    }

    /**
     * 获取版本Map
     * @param versionIds 版本ID集合
     * @return 获取版本Map
     */
    private Map<String, String> getVersionMap(List<String> versionIds) {
        ProjectVersionExample projectVersionExample = new ProjectVersionExample();
        projectVersionExample.createCriteria().andIdIn(versionIds);
        List<ProjectVersion> projectVersions = projectVersionMapper.selectByExample(projectVersionExample);
        return projectVersions.stream().collect(Collectors.toMap(ProjectVersion::getId, ProjectVersion::getName));
    }
}
