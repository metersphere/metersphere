package io.metersphere.dashboard.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.service.PermissionCheckService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.user.UserDTO;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class DashboardProjectService {

    @Resource
    private PermissionCheckService permissionCheckService;
    @Resource
    private ProjectMapper projectMapper;

    public static final String API_TEST = "apiTest";
    public static final String TEST_PLAN = "testPlan";
    public static final String FUNCTIONAL_CASE = "caseManagement";
    public static final String BUG = "bugManagement";

    public Map<String, Set<String>> getPermissionModuleProjectIds(String organizationId, List<String> projectIds, String userId) {
        boolean isAdmin = isAdmin(userId);
        Set<String> projectSet;
        Map<String, String> moduleMap;
        if (CollectionUtils.isNotEmpty(projectIds)) {
            projectSet = new HashSet<>(projectIds);
            ProjectExample projectExample = new ProjectExample();
            projectExample.createCriteria().andIdIn(projectIds);
            List<Project> projects = projectMapper.selectByExample(projectExample);
            moduleMap = projects.stream().collect(Collectors.toMap(Project::getId, Project::getModuleSetting));
        } else {
            ProjectExample projectExample = new ProjectExample();
            projectExample.createCriteria().andOrganizationIdEqualTo(organizationId);
            List<Project> projects = projectMapper.selectByExample(projectExample);
            projectSet = projects.stream().map(Project::getId).collect(Collectors.toSet());
            moduleMap = projects.stream().collect(Collectors.toMap(Project::getId, Project::getModuleSetting));
        }

        Map<String, Set<String>> hasModuleProjectIds = new HashMap<>();
        Map<String, String> finalModuleMap = moduleMap;
        Set<String> searchCaseProjectIds = new HashSet<>();
        Set<String> searchReviewProjectIds = new HashSet<>();
        Set<String> searchApiProjectIds = new HashSet<>();
        Set<String> searchApiCaseProjectIds = new HashSet<>();
        Set<String> searchScenarioProjectIds = new HashSet<>();
        Set<String> searchPlanProjectIds = new HashSet<>();
        Set<String> searchBugProjectIds = new HashSet<>();
        //查出用户在选中的项目中有读取权限的, admin所有项目都有权限
        if (!isAdmin) {
            Set<String> permissionSet = getPermissionSet();
            Map<String, Set<String>> hasUserPermissionProjectIds = permissionCheckService.getHasUserPermissionProjectIds(userId, projectSet, permissionSet);
            //查出这些项目分别有模块的
            Set<String> functionalProjectIds = hasUserPermissionProjectIds.get(PermissionConstants.FUNCTIONAL_CASE_READ);
            //检查是否开启功能用例模块
            if (CollectionUtils.isNotEmpty(functionalProjectIds)) {
                searchCaseProjectIds = functionalProjectIds.stream().filter(t -> finalModuleMap.get(t).contains(FUNCTIONAL_CASE)).collect(Collectors.toSet());
            }
            Set<String> reviewProjectIds = hasUserPermissionProjectIds.get(PermissionConstants.CASE_REVIEW_READ);
            if (CollectionUtils.isNotEmpty(reviewProjectIds)) {
                searchReviewProjectIds = reviewProjectIds.stream().filter(t -> finalModuleMap.get(t).contains(FUNCTIONAL_CASE)).collect(Collectors.toSet());
            }
            //检查是否开启接口模块
            Set<String> apiProjectIds = hasUserPermissionProjectIds.get(PermissionConstants.PROJECT_API_DEFINITION_READ);
            if (CollectionUtils.isNotEmpty(apiProjectIds)) {
                searchApiProjectIds = apiProjectIds.stream().filter(t -> finalModuleMap.get(t).contains(API_TEST)).collect(Collectors.toSet());
            }
            Set<String> apiCaseProjectIds = hasUserPermissionProjectIds.get(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ);
            if (CollectionUtils.isNotEmpty(apiCaseProjectIds)) {
                searchApiCaseProjectIds = apiCaseProjectIds.stream().filter(t -> finalModuleMap.get(t).contains(API_TEST)).collect(Collectors.toSet());
            }
            Set<String> scenarioProjectIds = hasUserPermissionProjectIds.get(PermissionConstants.PROJECT_API_SCENARIO_READ);
            if (CollectionUtils.isNotEmpty(scenarioProjectIds)) {
                searchScenarioProjectIds = scenarioProjectIds.stream().filter(t -> finalModuleMap.get(t).contains(API_TEST)).collect(Collectors.toSet());
            }
            //检查是否开启测试计划模块
            Set<String> planProjectIds = hasUserPermissionProjectIds.get(PermissionConstants.TEST_PLAN_READ);
            if (CollectionUtils.isNotEmpty(scenarioProjectIds)) {
                searchPlanProjectIds = planProjectIds.stream().filter(t -> finalModuleMap.get(t).contains(TEST_PLAN)).collect(Collectors.toSet());
            }
            //检查是否开启缺陷模块
            Set<String> bugProjectIds = hasUserPermissionProjectIds.get(PermissionConstants.PROJECT_BUG_READ);
            if (CollectionUtils.isNotEmpty(bugProjectIds)) {
                searchBugProjectIds = bugProjectIds.stream().filter(t -> finalModuleMap.get(t).contains(BUG)).collect(Collectors.toSet());
            }
        } else {
            //查出这些项目分别有模块的
            searchCaseProjectIds = projectSet.stream().filter(t -> finalModuleMap.get(t).contains(FUNCTIONAL_CASE)).collect(Collectors.toSet());
            searchReviewProjectIds = projectSet.stream().filter(t -> finalModuleMap.get(t).contains(FUNCTIONAL_CASE)).collect(Collectors.toSet());
            searchApiProjectIds = projectSet.stream().filter(t -> finalModuleMap.get(t).contains(API_TEST)).collect(Collectors.toSet());
            searchApiCaseProjectIds = projectSet.stream().filter(t -> finalModuleMap.get(t).contains(API_TEST)).collect(Collectors.toSet());
            searchScenarioProjectIds = projectSet.stream().filter(t -> finalModuleMap.get(t).contains(API_TEST)).collect(Collectors.toSet());
            searchPlanProjectIds = projectSet.stream().filter(t -> finalModuleMap.get(t).contains(TEST_PLAN)).collect(Collectors.toSet());
            searchBugProjectIds = projectSet.stream().filter(t -> finalModuleMap.get(t).contains(BUG)).collect(Collectors.toSet());
        }
        hasModuleProjectIds.put(PermissionConstants.FUNCTIONAL_CASE_READ, searchCaseProjectIds);
        hasModuleProjectIds.put(PermissionConstants.CASE_REVIEW_READ, searchReviewProjectIds);
        hasModuleProjectIds.put(PermissionConstants.PROJECT_API_DEFINITION_READ, searchApiProjectIds);
        hasModuleProjectIds.put(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ, searchApiCaseProjectIds);
        hasModuleProjectIds.put(PermissionConstants.PROJECT_API_SCENARIO_READ, searchScenarioProjectIds);
        hasModuleProjectIds.put(PermissionConstants.TEST_PLAN_READ, searchPlanProjectIds);
        hasModuleProjectIds.put(PermissionConstants.PROJECT_BUG_READ, searchBugProjectIds);

        return hasModuleProjectIds;
    }

    private static Set<String> getPermissionSet() {
        Set<String> permissionSet = new HashSet<>();
        permissionSet.add(PermissionConstants.FUNCTIONAL_CASE_READ);
        permissionSet.add(PermissionConstants.CASE_REVIEW_READ);
        permissionSet.add(PermissionConstants.PROJECT_API_DEFINITION_READ);
        permissionSet.add(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ);
        permissionSet.add(PermissionConstants.PROJECT_API_SCENARIO_READ);
        permissionSet.add(PermissionConstants.TEST_PLAN_READ);
        permissionSet.add(PermissionConstants.PROJECT_BUG_READ);
        return permissionSet;
    }

    private boolean isAdmin(String userId) {
        UserDTO userDTO = permissionCheckService.getUserDTO(userId);
        return permissionCheckService.checkAdmin(userDTO);
    }
}
