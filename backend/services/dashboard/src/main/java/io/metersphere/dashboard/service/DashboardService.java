package io.metersphere.dashboard.service;

import io.metersphere.api.mapper.ExtApiDefinitionMapper;
import io.metersphere.api.mapper.ExtApiScenarioMapper;
import io.metersphere.api.mapper.ExtApiTestCaseMapper;
import io.metersphere.bug.mapper.ExtBugMapper;
import io.metersphere.dashboard.dto.LayoutDTO;
import io.metersphere.dashboard.dto.NameArrayDTO;
import io.metersphere.dashboard.request.DashboardFrontPageRequest;
import io.metersphere.dashboard.response.OverViewCountDTO;
import io.metersphere.functional.mapper.ExtCaseReviewMapper;
import io.metersphere.functional.mapper.ExtFunctionalCaseMapper;
import io.metersphere.plan.mapper.ExtTestPlanMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.ProjectCountDTO;
import io.metersphere.project.mapper.ExtProjectMapper;
import io.metersphere.project.service.ProjectService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.UserLayout;
import io.metersphere.system.domain.UserLayoutExample;
import io.metersphere.system.mapper.UserLayoutMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guoyuqi
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class DashboardService {
    @Resource
    private DashboardProjectService dashboardProjectService;
    @Resource
    private ExtFunctionalCaseMapper extFunctionalCaseMapper;
    @Resource
    private ExtCaseReviewMapper extCaseReviewMapper;
    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;
    @Resource
    private ExtTestPlanMapper extTestPlanMapper;
    @Resource
    private ExtBugMapper extBugMapper;
    @Resource
    private ExtProjectMapper extProjectMapper;
    @Resource
    private ProjectService projectService;
    @Resource
    private UserLayoutMapper userLayoutMapper;


    public static final String FUNCTIONAL = "FUNCTIONAL"; // 功能用例
    public static final String CASE_REVIEW = "CASE_REVIEW"; // 用例评审
    public static final String API = "API";
    public static final String API_CASE = "API_CASE";
    public static final String API_SCENARIO = "API_SCENARIO";
    public static final String TEST_PLAN = "TEST_PLAN"; // 测试计划
    public static final String BUG_COUNT = "BUG_COUNT"; // 缺陷数量


    public OverViewCountDTO createByMeCount(DashboardFrontPageRequest request, String userId) {
        List<Project> projects;
        if (CollectionUtils.isNotEmpty(request.getProjectIds())) {
            projects = extProjectMapper.getProjectNameModule(null, request.getProjectIds());
        } else {
            projects = extProjectMapper.getProjectNameModule(request.getOrganizationId(), null);
        }
        Map<String, Set<String>> permissionModuleProjectIdMap = dashboardProjectService.getPermissionModuleProjectIds(projects, userId);
        Long toStartTime = request.getToStartTime();
        Long toEndTime = request.getToEndTime();
        return getModuleCountMap(permissionModuleProjectIdMap, projects, toStartTime, toEndTime, userId);
    }

    @NotNull
    private OverViewCountDTO getModuleCountMap(Map<String, Set<String>> permissionModuleProjectIdMap, List<Project> projects, Long toStartTime, Long toEndTime, String userId) {
        Map<String, Integer> map = new HashMap<>();
        List<String> xaxis = new ArrayList<>();
        //TODO： 返回数量顺序，
        List<NameArrayDTO> nameArrayDTOList = new ArrayList<>();

        //功能用例
        Map<String, ProjectCountDTO> caseProjectCount = new HashMap<>();
        Set<String> caseProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.FUNCTIONAL_CASE_READ);
        if (CollectionUtils.isNotEmpty(caseProjectIds)) {
            //有权限
            List<ProjectCountDTO> projectCaseCount = extFunctionalCaseMapper.projectCaseCount(caseProjectIds, toStartTime, toEndTime, userId);
            int caseCount = projectCaseCount.stream().mapToInt(ProjectCountDTO::getCount).sum();
            map.put(FUNCTIONAL, caseCount);
            xaxis.add(FUNCTIONAL);
            caseProjectCount = projectCaseCount.stream().collect(Collectors.toMap(ProjectCountDTO::getProjectId, t -> t));
        }

        //用例评审
        Map<String, ProjectCountDTO> reviewProjectCount = new HashMap<>();
        Set<String> reviewProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.CASE_REVIEW_READ);
        if (CollectionUtils.isNotEmpty(reviewProjectIds)) {
            List<ProjectCountDTO> projectReviewCount = extCaseReviewMapper.projectReviewCount(reviewProjectIds, toStartTime, toEndTime, userId);
            int reviewCount = projectReviewCount.stream().mapToInt(ProjectCountDTO::getCount).sum();
            map.put(CASE_REVIEW, reviewCount);
            xaxis.add(CASE_REVIEW);
            reviewProjectCount = projectReviewCount.stream().collect(Collectors.toMap(ProjectCountDTO::getProjectId, t -> t));
        }
        //接口
        Map<String, ProjectCountDTO> apiProjectCount = new HashMap<>();
        Set<String> apiProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.PROJECT_API_DEFINITION_READ);
        if (CollectionUtils.isNotEmpty(apiProjectIds)) {
            List<ProjectCountDTO> projectApiCount = extApiDefinitionMapper.projectApiCount(apiProjectIds, toStartTime, toEndTime, userId);
            int apiCount = projectApiCount.stream().mapToInt(ProjectCountDTO::getCount).sum();
            map.put(API, apiCount);
            xaxis.add(API);
            apiProjectCount = projectApiCount.stream().collect(Collectors.toMap(ProjectCountDTO::getProjectId, t -> t));

        }
        //接口用例
        Map<String, ProjectCountDTO> apiCaseProjectCount = new HashMap<>();
        Set<String> apiCaseProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ);
        if (CollectionUtils.isNotEmpty(apiCaseProjectIds)) {
            List<ProjectCountDTO> projectApiCaseCount = extApiTestCaseMapper.projectApiCaseCount(apiCaseProjectIds, toStartTime, toEndTime, userId);
            int apiCaseCount = projectApiCaseCount.stream().mapToInt(ProjectCountDTO::getCount).sum();
            map.put(API_CASE, apiCaseCount);
            xaxis.add(API_CASE);
            apiCaseProjectCount = projectApiCaseCount.stream().collect(Collectors.toMap(ProjectCountDTO::getProjectId, t -> t));

        }
        //接口场景
        Map<String, ProjectCountDTO> apiScenarioProjectCount = new HashMap<>();
        Set<String> scenarioProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.PROJECT_API_SCENARIO_READ);
        if (CollectionUtils.isNotEmpty(scenarioProjectIds)) {
            List<ProjectCountDTO> projectApiScenarioCount = extApiScenarioMapper.projectApiScenarioCount(scenarioProjectIds, toStartTime, toEndTime, userId);
            int apiScenarioCount = projectApiScenarioCount.stream().mapToInt(ProjectCountDTO::getCount).sum();
            map.put(API_SCENARIO, apiScenarioCount);
            xaxis.add(API_SCENARIO);
            apiScenarioProjectCount = projectApiScenarioCount.stream().collect(Collectors.toMap(ProjectCountDTO::getProjectId, t -> t));
        }
        //测试计划
        Map<String, ProjectCountDTO> testPlanProjectCount = new HashMap<>();
        Set<String> planProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.TEST_PLAN_READ);
        if (CollectionUtils.isNotEmpty(planProjectIds)) {
            List<ProjectCountDTO> projectPlanCount = extTestPlanMapper.projectPlanCount(planProjectIds, toStartTime, toEndTime, userId);
            int testPlanCount = projectPlanCount.stream().mapToInt(ProjectCountDTO::getCount).sum();
            map.put(TEST_PLAN, testPlanCount);
            xaxis.add(TEST_PLAN);
            testPlanProjectCount = projectPlanCount.stream().collect(Collectors.toMap(ProjectCountDTO::getProjectId, t -> t));

        }
        //缺陷管理
        Map<String, ProjectCountDTO> bugProjectCount = new HashMap<>();
        Set<String> bugProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.PROJECT_BUG_READ);
        if (CollectionUtils.isNotEmpty(bugProjectIds)) {
            List<ProjectCountDTO> projectBugCount = extBugMapper.projectBugCount(bugProjectIds, toStartTime, toEndTime, userId);
            int bugCount = projectBugCount.stream().mapToInt(ProjectCountDTO::getCount).sum();
            map.put(BUG_COUNT, bugCount);
            xaxis.add(TEST_PLAN);
            bugProjectCount = projectBugCount.stream().collect(Collectors.toMap(ProjectCountDTO::getProjectId, t -> t));
        }

        for (Project project : projects) {
            String projectId = project.getId();
            String projectName = project.getName();
            NameArrayDTO nameArrayDTO = new NameArrayDTO();
            nameArrayDTO.setId(projectId);
            nameArrayDTO.setName(projectName);
            List<Integer> count = new ArrayList<>();
            ProjectCountDTO projectCountDTO = caseProjectCount.get(projectId);
            if (projectCountDTO != null) {
                count.add(projectCountDTO.getCount());
            } else {
                count.add(0);
            }
            ProjectCountDTO reviewDTO = reviewProjectCount.get(projectId);
            if (reviewDTO != null) {
                count.add(reviewDTO.getCount());
            } else {
                count.add(0);
            }
            ProjectCountDTO apiDTO = apiProjectCount.get(projectId);
            if (apiDTO != null) {
                count.add(apiDTO.getCount());
            } else {
                count.add(0);
            }
            ProjectCountDTO apiCaseDTO = apiCaseProjectCount.get(projectId);
            if (apiCaseDTO != null) {
                count.add(apiCaseDTO.getCount());
            } else {
                count.add(0);
            }
            ProjectCountDTO apiScenarioDTO = apiScenarioProjectCount.get(projectId);
            if (apiScenarioDTO != null) {
                count.add(apiScenarioDTO.getCount());
            } else {
                count.add(0);
            }
            ProjectCountDTO testPlanDTO = testPlanProjectCount.get(projectId);
            if (testPlanDTO != null) {
                count.add(testPlanDTO.getCount());
            } else {
                count.add(0);
            }
            ProjectCountDTO bugDTO = bugProjectCount.get(projectId);
            if (bugDTO != null) {
                count.add(bugDTO.getCount());
            } else {
                count.add(0);
            }
            nameArrayDTO.setCount(count);
            nameArrayDTOList.add(nameArrayDTO);
        }
        OverViewCountDTO overViewCountDTO = new OverViewCountDTO();
        overViewCountDTO.setCaseCountMap(map);
        overViewCountDTO.setXAxis(xaxis);
        overViewCountDTO.setProjectCountList(nameArrayDTOList);
        return overViewCountDTO;
    }

    public OverViewCountDTO projectViewCount(DashboardFrontPageRequest request, String userId) {
        List<Project> userProject = projectService.getUserProject(request.getOrganizationId(), userId);
        List<Project> collect = userProject.stream().filter(t -> request.getProjectIds().contains(t.getId())).toList();
        Map<String, Set<String>> permissionModuleProjectIdMap = dashboardProjectService.getModuleProjectIds(collect);
        Long toStartTime = request.getToStartTime();
        Long toEndTime = request.getToEndTime();
        return getModuleCountMap(permissionModuleProjectIdMap, collect, toStartTime, toEndTime, null);
    }


    public List<LayoutDTO> editLayout(String organizationId, String userId, List<LayoutDTO> layoutDTO) {
        UserLayoutExample userLayoutExample = new UserLayoutExample();
        userLayoutExample.createCriteria().andUserIdEqualTo(userId).andOrgIdEqualTo(organizationId);
        List<UserLayout> userLayouts = userLayoutMapper.selectByExample(userLayoutExample);
        UserLayout userLayout = new UserLayout();
        userLayout.setUserId(userId);
        userLayout.setOrgId(organizationId);
        String configuration = JSON.toJSONString(layoutDTO);
        userLayout.setConfiguration(configuration.getBytes());
        if (CollectionUtils.isEmpty(userLayouts)) {
            userLayout.setId(IDGenerator.nextStr());
            userLayoutMapper.insert(userLayout);
        } else {
            userLayout.setId(userLayouts.getFirst().getId());
            userLayoutMapper.updateByPrimaryKeyWithBLOBs(userLayout);
        }
        return layoutDTO;
    }

    public List<LayoutDTO> getLayout(String organizationId, String userId) {
        UserLayoutExample userLayoutExample = new UserLayoutExample();
        userLayoutExample.createCriteria().andUserIdEqualTo(userId).andOrgIdEqualTo(organizationId);
        List<UserLayout> userLayouts = userLayoutMapper.selectByExampleWithBLOBs(userLayoutExample);
        UserLayout userLayout = userLayouts.getFirst();
        byte[] configuration = userLayout.getConfiguration();
        String layoutDTOStr = new String(configuration);
        return JSON.parseArray(layoutDTOStr, LayoutDTO.class);
    }
}
