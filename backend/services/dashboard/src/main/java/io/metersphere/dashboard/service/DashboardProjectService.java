package io.metersphere.dashboard.service;

import io.metersphere.project.domain.Project;
import io.metersphere.system.service.PermissionCheckService;
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



    public static final String API_TEST = "apiTest";
    public static final String TEST_PLAN = "testPlan";
    public static final String FUNCTIONAL_CASE = "caseManagement";
    public static final String BUG = "bugManagement";

    /**
     * 获取用户组织内有只读权限且开启相关模块的项目
     *
     * @param userId 当前用户
     * @return 只读权限对应的开启模块的项目ids
     */
    public Map<String, Set<String>> getPermissionModuleProjectIds(List<Project> projects, String userId) {
        boolean isAdmin = isAdmin(userId);
        Set<String> projectSet;
        Map<String, String> moduleMap;
        projectSet = projects.stream() .map(Project::getId).collect(Collectors.toCollection(LinkedHashSet::new));
        moduleMap = projects.stream().collect(Collectors.toMap(Project::getId, Project::getModuleSetting));
        Map<String, Set<String>> hasModuleProjectIds = new HashMap<>();
        Map<String, String> finalModuleMap = moduleMap;
        Set<String> searchCaseProjectIds = new LinkedHashSet<>();
        Set<String> searchReviewProjectIds = new LinkedHashSet<>();
        Set<String> searchApiProjectIds = new LinkedHashSet<>();
        Set<String> searchApiCaseProjectIds = new LinkedHashSet<>();
        Set<String> searchScenarioProjectIds = new LinkedHashSet<>();
        Set<String> searchPlanProjectIds = new LinkedHashSet<>();
        Set<String> searchBugProjectIds = new LinkedHashSet<>();
        //查出用户在选中的项目中有读取权限的, admin所有项目都有权限
        if (!isAdmin) {
            Set<String> permissionSet = getPermissionSet();
            Map<String, Set<String>> hasUserPermissionProjectIds = permissionCheckService.getHasUserPermissionProjectIds(userId, projectSet, permissionSet);
            //查出这些项目分别有模块的
            Set<String> functionalProjectIds = hasUserPermissionProjectIds.get(PermissionConstants.FUNCTIONAL_CASE_READ);
            //检查是否开启功能用例模块
            if (CollectionUtils.isNotEmpty(functionalProjectIds)) {
                //有权限
                searchCaseProjectIds = functionalProjectIds.stream().filter(t -> finalModuleMap.get(t).contains(FUNCTIONAL_CASE)).collect(Collectors.toCollection(LinkedHashSet::new));
            }
            Set<String> reviewProjectIds = hasUserPermissionProjectIds.get(PermissionConstants.CASE_REVIEW_READ);
            if (CollectionUtils.isNotEmpty(reviewProjectIds)) {
                searchReviewProjectIds = reviewProjectIds.stream().filter(t -> finalModuleMap.get(t).contains(FUNCTIONAL_CASE)).collect(Collectors.toCollection(LinkedHashSet::new));
            }
            //检查是否开启接口模块
            Set<String> apiProjectIds = hasUserPermissionProjectIds.get(PermissionConstants.PROJECT_API_DEFINITION_READ);
            if (CollectionUtils.isNotEmpty(apiProjectIds)) {
                searchApiProjectIds = apiProjectIds.stream().filter(t -> finalModuleMap.get(t).contains(API_TEST)).collect(Collectors.toCollection(LinkedHashSet::new));
            }
            Set<String> apiCaseProjectIds = hasUserPermissionProjectIds.get(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ);
            if (CollectionUtils.isNotEmpty(apiCaseProjectIds)) {
                searchApiCaseProjectIds = apiCaseProjectIds.stream().filter(t -> finalModuleMap.get(t).contains(API_TEST)).collect(Collectors.toCollection(LinkedHashSet::new));
            }
            Set<String> scenarioProjectIds = hasUserPermissionProjectIds.get(PermissionConstants.PROJECT_API_SCENARIO_READ);
            if (CollectionUtils.isNotEmpty(scenarioProjectIds)) {
                searchScenarioProjectIds = scenarioProjectIds.stream().filter(t -> finalModuleMap.get(t).contains(API_TEST)).collect(Collectors.toCollection(LinkedHashSet::new));
            }
            //检查是否开启测试计划模块
            Set<String> planProjectIds = hasUserPermissionProjectIds.get(PermissionConstants.TEST_PLAN_READ);
            if (CollectionUtils.isNotEmpty(planProjectIds)) {
                searchPlanProjectIds = planProjectIds.stream().filter(t -> finalModuleMap.get(t).contains(TEST_PLAN)).collect(Collectors.toCollection(LinkedHashSet::new));
            }
            //检查是否开启缺陷模块
            Set<String> bugProjectIds = hasUserPermissionProjectIds.get(PermissionConstants.PROJECT_BUG_READ);
            if (CollectionUtils.isNotEmpty(bugProjectIds)) {
                searchBugProjectIds = bugProjectIds.stream().filter(t -> finalModuleMap.get(t).contains(BUG)).collect(Collectors.toCollection(LinkedHashSet::new));
            }
        } else {
            //查出这些项目分别有模块的
            searchCaseProjectIds = projectSet.stream().filter(t -> finalModuleMap.get(t).contains(FUNCTIONAL_CASE)).collect(Collectors.toCollection(LinkedHashSet::new));
            searchReviewProjectIds = projectSet.stream().filter(t -> finalModuleMap.get(t).contains(FUNCTIONAL_CASE)).collect(Collectors.toCollection(LinkedHashSet::new));
            searchApiProjectIds = projectSet.stream().filter(t -> finalModuleMap.get(t).contains(API_TEST)).collect(Collectors.toCollection(LinkedHashSet::new));
            searchApiCaseProjectIds = projectSet.stream().filter(t -> finalModuleMap.get(t).contains(API_TEST)).collect(Collectors.toCollection(LinkedHashSet::new));
            searchScenarioProjectIds = projectSet.stream().filter(t -> finalModuleMap.get(t).contains(API_TEST)).collect(Collectors.toCollection(LinkedHashSet::new));
            searchPlanProjectIds = projectSet.stream().filter(t -> finalModuleMap.get(t).contains(TEST_PLAN)).collect(Collectors.toCollection(LinkedHashSet::new));
            searchBugProjectIds = projectSet.stream().filter(t -> finalModuleMap.get(t).contains(BUG)).collect(Collectors.toCollection(LinkedHashSet::new));
        }
        //如果value 为空，则没有权限或者没开启模块
        hasModuleProjectIds.put(PermissionConstants.FUNCTIONAL_CASE_READ, searchCaseProjectIds);
        hasModuleProjectIds.put(PermissionConstants.CASE_REVIEW_READ, searchReviewProjectIds);
        hasModuleProjectIds.put(PermissionConstants.PROJECT_API_DEFINITION_READ, searchApiProjectIds);
        hasModuleProjectIds.put(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ, searchApiCaseProjectIds);
        hasModuleProjectIds.put(PermissionConstants.PROJECT_API_SCENARIO_READ, searchScenarioProjectIds);
        hasModuleProjectIds.put(PermissionConstants.TEST_PLAN_READ, searchPlanProjectIds);
        hasModuleProjectIds.put(PermissionConstants.PROJECT_BUG_READ, searchBugProjectIds);

        return hasModuleProjectIds;
    }

    /**
     * 当前用户在组织内有任意权限的且开启模块的项目
     *
     * @param userProject 在组织内有任意权限项目
     * @return 模块开启对应的项目ids
     */
    public Map<String, Set<String>> getModuleProjectIds(List<Project> userProject) {
        Map<String, String> moduleMap = userProject.stream().collect(Collectors.toMap(Project::getId, Project::getModuleSetting));
        Set<String> projectIds = userProject.stream().map(Project::getId).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> searchCaseProjectIds = projectIds.stream().filter(t -> moduleMap.get(t).contains(FUNCTIONAL_CASE)).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> searchApiProjectIds = projectIds.stream().filter(t -> moduleMap.get(t).contains(API_TEST)).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> searchPlanProjectIds = projectIds.stream().filter(t -> moduleMap.get(t).contains(TEST_PLAN)).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> searchBugProjectIds = projectIds.stream().filter(t -> moduleMap.get(t).contains(BUG)).collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, Set<String>> hasModuleProjectIds = new HashMap<>();
        hasModuleProjectIds.put(PermissionConstants.FUNCTIONAL_CASE_READ, searchCaseProjectIds);
        hasModuleProjectIds.put(PermissionConstants.CASE_REVIEW_READ, searchCaseProjectIds);
        hasModuleProjectIds.put(PermissionConstants.PROJECT_API_DEFINITION_READ, searchApiProjectIds);
        hasModuleProjectIds.put(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ, searchApiProjectIds);
        hasModuleProjectIds.put(PermissionConstants.PROJECT_API_SCENARIO_READ, searchApiProjectIds);
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
        if (userDTO == null) {
            return false;
        }
        return permissionCheckService.checkAdmin(userDTO);
    }
}
