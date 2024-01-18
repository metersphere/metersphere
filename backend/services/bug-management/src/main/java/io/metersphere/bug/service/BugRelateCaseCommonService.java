package io.metersphere.bug.service;

import io.metersphere.bug.domain.BugRelationCase;
import io.metersphere.bug.domain.BugRelationCaseExample;
import io.metersphere.bug.dto.request.BugRelatedCasePageRequest;
import io.metersphere.bug.dto.response.BugRelateCaseDTO;
import io.metersphere.bug.mapper.BugRelationCaseMapper;
import io.metersphere.bug.mapper.ExtBugRelateCaseMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.domain.ProjectVersion;
import io.metersphere.project.domain.ProjectVersionExample;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.mapper.ProjectVersionMapper;
import io.metersphere.project.service.ModuleTreeService;
import io.metersphere.project.service.PermissionCheckService;
import io.metersphere.provider.BaseAssociateCaseProvider;
import io.metersphere.request.AssociateCaseModuleRequest;
import io.metersphere.request.AssociateOtherCaseRequest;
import io.metersphere.request.TestCasePageProviderRequest;
import io.metersphere.sdk.constants.CaseType;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class BugRelateCaseCommonService extends ModuleTreeService {

    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private ProjectVersionMapper projectVersionMapper;
    @Resource
    private BugRelationCaseMapper bugRelationCaseMapper;
    @Resource
    private ExtBugRelateCaseMapper extBugRelateCaseMapper;
    @Resource
    private PermissionCheckService permissionCheckService;
    @Resource
    private BaseAssociateCaseProvider functionalCaseProvider;

    /**
     * 获取关联用例模块树(不包括数量)
     * @param request 请求参数
     * @return 模块树集合
     */
    public List<BaseTreeNode> getRelateCaseTree(AssociateCaseModuleRequest request) {
        // 目前只保留功能用例的左侧模块树方法调用, 后续其他用例根据RelateCaseType扩展
        List<BaseTreeNode> relateCaseModules = extBugRelateCaseMapper.getRelateCaseModule(request, false);
        // 构建模块树层级数量为通用逻辑
        return super.buildTreeAndCountResource(relateCaseModules, true, Translator.get("api_unplanned_request"));
    }

    /**
     * 获取关联用例模块树数量
     * @param request 请求参数
     * @return 模块树集合
     */
    public Map<String, Long> countTree(TestCasePageProviderRequest request) {
        // 目前只保留功能用例的左侧模块树方法调用, 后续其他用例根据RelateCaseType扩展
        List<ModuleCountDTO> moduleCounts = extBugRelateCaseMapper.countRelateCaseModuleTree(request, false);
        AssociateCaseModuleRequest moduleRequest = new AssociateCaseModuleRequest();
        BeanUtils.copyBean(moduleRequest, request);
        List<BaseTreeNode> relateCaseModules = extBugRelateCaseMapper.getRelateCaseModule(moduleRequest, false);
        List<BaseTreeNode> relateCaseModuleWithCount = buildTreeAndCountResource(relateCaseModules, moduleCounts, true, Translator.get("api_unplanned_request"));
        Map<String, Long> moduleCountMap = getIdCountMapByBreadth(relateCaseModuleWithCount);
        long total = getAllCount(moduleCounts);
        moduleCountMap.put("total", total);
        return moduleCountMap;
    }

    /**
     * 关联用例
     * @param request 关联用例参数
     * @param deleted 是否删除状态
     * @param currentUser 当前用户
     */
    public void relateCase(AssociateOtherCaseRequest request, boolean deleted, String currentUser) {
        // 目前只需根据关联条件获取功能用例ID, 后续扩展
        List<String> relatedIds = functionalCaseProvider.getRelatedIdsByParam(request, deleted);
        // 缺陷关联用例通用逻辑
        if (CollectionUtils.isEmpty(relatedIds)) {
            return;
        }
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        BugRelationCaseMapper relationCaseMapper = sqlSession.getMapper(BugRelationCaseMapper.class);
        // 根据用例ID筛选出已通过测试计划关联的用例
        BugRelationCaseExample bugRelationCaseExample = new BugRelationCaseExample();
        bugRelationCaseExample.createCriteria().andTestPlanCaseIdIn(relatedIds);
        List<BugRelationCase> planRelatedCases = bugRelationCaseMapper.selectByExample(bugRelationCaseExample);
        Map<String, String> planRelatedMap = planRelatedCases.stream().collect(Collectors.toMap(BugRelationCase::getTestPlanCaseId, BugRelationCase::getId));
        relatedIds.forEach(relatedId -> {
            if (planRelatedMap.containsKey(relatedId)) {
                // 计划已关联
                BugRelationCase record = new BugRelationCase();
                record.setId(planRelatedMap.get(relatedId));
                record.setCaseId(relatedId);
                record.setUpdateTime(System.currentTimeMillis());
                relationCaseMapper.updateByPrimaryKeySelective(record);
            } else {
                BugRelationCase record = new BugRelationCase();
                record.setId(IDGenerator.nextStr());
                record.setCaseId(relatedId);
                record.setBugId(request.getSourceId());
                record.setCaseType(request.getSourceType());
                record.setCreateUser(currentUser);
                record.setCreateTime(System.currentTimeMillis());
                record.setUpdateTime(System.currentTimeMillis());
                relationCaseMapper.insert(record);
            }
        });
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    /**
     * 分页查询关联用例列表
     * @param request 请求参数
     */
    public List<BugRelateCaseDTO> page(BugRelatedCasePageRequest request) {
        // 目前只查关联的功能用例类型, 后续多个用例类型SQL扩展
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

    @Override
    public void updatePos(String id, long pos) {

    }

    @Override
    public void refreshPos(String parentId) {

    }
}
