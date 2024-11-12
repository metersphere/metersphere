package io.metersphere.dashboard.service;

import io.metersphere.api.mapper.ExtApiDefinitionMapper;
import io.metersphere.api.mapper.ExtApiScenarioMapper;
import io.metersphere.api.mapper.ExtApiTestCaseMapper;
import io.metersphere.bug.enums.BugPlatform;
import io.metersphere.bug.mapper.ExtBugMapper;
import io.metersphere.bug.service.BugCommonService;
import io.metersphere.bug.service.BugStatusService;
import io.metersphere.dashboard.constants.DashboardUserLayoutKeys;
import io.metersphere.dashboard.dto.LayoutDTO;
import io.metersphere.dashboard.dto.NameArrayDTO;
import io.metersphere.dashboard.dto.NameCountDTO;
import io.metersphere.dashboard.dto.StatusPercentDTO;
import io.metersphere.dashboard.request.DashboardFrontPageRequest;
import io.metersphere.dashboard.response.OverViewCountDTO;
import io.metersphere.dashboard.response.StatisticsDTO;
import io.metersphere.functional.constants.CaseReviewStatus;
import io.metersphere.functional.constants.FunctionalCaseReviewStatus;
import io.metersphere.functional.dto.FunctionalCaseStatisticDTO;
import io.metersphere.functional.mapper.ExtCaseReviewMapper;
import io.metersphere.functional.mapper.ExtFunctionalCaseMapper;
import io.metersphere.plan.mapper.ExtTestPlanMapper;
import io.metersphere.plugin.platform.dto.SelectOption;
import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.ProjectCountDTO;
import io.metersphere.project.dto.ProjectUserCreateCount;
import io.metersphere.project.dto.ProjectUserStatusCountDTO;
import io.metersphere.project.mapper.ExtProjectMapper;
import io.metersphere.project.mapper.ExtProjectMemberMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.project.service.ProjectService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.UserLayout;
import io.metersphere.system.domain.UserLayoutExample;
import io.metersphere.system.dto.user.ProjectUserMemberDTO;
import io.metersphere.system.mapper.UserLayoutMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private ExtProjectMemberMapper extProjectMemberMapper;
    @Resource
    private ProjectService projectService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private UserLayoutMapper userLayoutMapper;
    @Resource
    private BugCommonService bugCommonService;
    @Resource
    private BugStatusService bugStatusService;
    @Resource
    private ProjectApplicationService projectApplicationService;


    public static final String FUNCTIONAL = "FUNCTIONAL"; // 功能用例
    public static final String CASE_REVIEW = "CASE_REVIEW"; // 用例评审
    public static final String API = "API";
    public static final String API_CASE = "API_CASE";
    public static final String API_SCENARIO = "API_SCENARIO";
    public static final String TEST_PLAN = "TEST_PLAN"; // 测试计划
    public static final String BUG_COUNT = "BUG_COUNT"; // 缺陷数量

    public static final String API_TEST_MODULE = "apiTest";
    public static final String TEST_PLAN_MODULE = "testPlan";
    public static final String FUNCTIONAL_CASE_MODULE = "caseManagement";
    public static final String BUG_MODULE = "bugManagement";


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
            xaxis.add(BUG_COUNT);
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
        List<Project> collect = getHasPermissionProjects(request, userId);
        Map<String, Set<String>> permissionModuleProjectIdMap = dashboardProjectService.getModuleProjectIds(collect);
        Long toStartTime = request.getToStartTime();
        Long toEndTime = request.getToEndTime();
        return getModuleCountMap(permissionModuleProjectIdMap, collect, toStartTime, toEndTime, null);
    }

    private List<Project> getHasPermissionProjects(DashboardFrontPageRequest request, String userId) {
        List<Project> userProject = projectService.getUserProject(request.getOrganizationId(), userId);
        List<Project> collect;
        if (CollectionUtils.isNotEmpty(request.getProjectIds())) {
            collect = userProject.stream().filter(t -> request.getProjectIds().contains(t.getId())).toList();
        } else {
            collect = userProject;
        }
        return collect;
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
        if (CollectionUtils.isEmpty(userLayouts)) {
            return getDefaultLayoutDTOS(organizationId);
        }

        UserLayout userLayout = userLayouts.getFirst();
        byte[] configuration = userLayout.getConfiguration();
        String layoutDTOStr = new String(configuration);
        List<LayoutDTO> layoutDTOS = JSON.parseArray(layoutDTOStr, LayoutDTO.class);

        //重新查询排除项目禁用的或者用户已经移除某个项目的项目或者成员
        List<String> allProjectIds = new ArrayList<>();
        List<String> allHandleUsers = new ArrayList<>();
        for (LayoutDTO layoutDTO : layoutDTOS) {
            allProjectIds.addAll(layoutDTO.getProjectIds());
            allHandleUsers.addAll(layoutDTO.getHandleUsers());
        }
        List<Project> getUserProjectIdName;
        if (CollectionUtils.isEmpty(allProjectIds)) {
            getUserProjectIdName = extProjectMapper.getUserProjectIdName(organizationId, null, userId);
        } else {
            List<String> projectIds = allProjectIds.stream().distinct().toList();
            getUserProjectIdName = extProjectMapper.getUserProjectIdName(null, projectIds, userId);
        }
        Map<String, Project> projectMap = getUserProjectIdName.stream().collect(Collectors.toMap(Project::getId, t -> t));
        List<String> handleUsers = allHandleUsers.stream().distinct().toList();
        List<ProjectUserMemberDTO> orgProjectMemberList = extProjectMemberMapper.getOrgProjectMemberList(organizationId, handleUsers);
        //重新填充填充返回的项目id 和 用户id
        rebuildProjectOrUser(layoutDTOS, getUserProjectIdName, projectMap, orgProjectMemberList);
        return layoutDTOS;
    }

    /**
     * 获取默认布局
     *
     * @param organizationId 组织ID
     * @return List<LayoutDTO>
     */
    private static List<LayoutDTO> getDefaultLayoutDTOS(String organizationId) {
        List<LayoutDTO> layoutDTOS = new ArrayList<>();
        LayoutDTO projectLayoutDTO = buildDefaultLayoutDTO(DashboardUserLayoutKeys.PROJECT_VIEW, "workbench.homePage.projectOverview", 0, new ArrayList<>());
        layoutDTOS.add(projectLayoutDTO);
        LayoutDTO createByMeLayoutDTO = buildDefaultLayoutDTO(DashboardUserLayoutKeys.CREATE_BY_ME, "workbench,homePage.createdByMe", 1, new ArrayList<>());
        layoutDTOS.add(createByMeLayoutDTO);
        LayoutDTO projectMemberLayoutDTO = buildDefaultLayoutDTO(DashboardUserLayoutKeys.PROJECT_MEMBER_VIEW, "workbench,homePage.staffOverview", 2, List.of(organizationId));
        layoutDTOS.add(projectMemberLayoutDTO);
        return layoutDTOS;
    }

    /**
     * 构建默认布局内容
     *
     * @param layoutKey  布局卡片的key
     * @param label      布局卡片页面显示的label
     * @param pos        布局卡片 排序
     * @param projectIds 布局卡片所选的项目ids
     * @return LayoutDTO
     */
    private static LayoutDTO buildDefaultLayoutDTO(DashboardUserLayoutKeys layoutKey, String label, int pos, List<String> projectIds) {
        LayoutDTO layoutDTO = new LayoutDTO();
        layoutDTO.setId(UUID.randomUUID().toString());
        layoutDTO.setKey(layoutKey.toString());
        layoutDTO.setLabel(label);
        layoutDTO.setPos(pos);
        layoutDTO.setFullScreen(true);
        layoutDTO.setProjectIds(projectIds);
        layoutDTO.setHandleUsers(new ArrayList<>());
        return layoutDTO;
    }

    /**
     * 过滤用户在当前项目是否有移除或者项目是否被禁用以及用户是否被删除禁用
     *
     * @param layoutDTOS           获取的所有布局卡片
     * @param getUserProjectIdName 用户有任意权限的项目
     * @param projectMap           用户有任意权限的项目Map
     * @param orgProjectMemberList 组织下所有的项目人员
     */
    private static void rebuildProjectOrUser(List<LayoutDTO> layoutDTOS, List<Project> getUserProjectIdName, Map<String, Project> projectMap, List<ProjectUserMemberDTO> orgProjectMemberList) {
        for (LayoutDTO layoutDTO : layoutDTOS) {
            if (StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.PROJECT_VIEW.toString()) || StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.CREATE_BY_ME.toString())) {
                List<Project> list = getUserProjectIdName.stream().filter(t -> layoutDTO.getProjectIds().contains(t.getId())).toList();
                layoutDTO.setProjectIds(list.stream().map(Project::getId).toList());
            } else if (StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.PROJECT_MEMBER_VIEW.toString())) {
                List<ProjectUserMemberDTO> list = orgProjectMemberList.stream().filter(t -> layoutDTO.getHandleUsers().contains(t.getId())).toList();
                layoutDTO.setHandleUsers(list.stream().map(ProjectUserMemberDTO::getId).toList());
            } else {
                if (CollectionUtils.isNotEmpty(layoutDTO.getProjectIds()) && projectMap.get(layoutDTO.getProjectIds().getFirst()) == null) {
                    layoutDTO.setProjectIds(List.of(getUserProjectIdName.get(0).getId()));
                }
            }
        }
    }

    public OverViewCountDTO projectMemberViewCount(DashboardFrontPageRequest request) {
        String projectId = request.getProjectIds().getFirst();
        Project project = projectMapper.selectByPrimaryKey(projectId);
        List<String> moduleIds = JSON.parseArray(project.getModuleSetting(), String.class);
        Long toStartTime = request.getToStartTime();
        Long toEndTime = request.getToEndTime();
        List<ProjectUserMemberDTO> projectMemberList = extProjectMemberMapper.getProjectMemberList(projectId, request.getHandleUsers());
        Map<String, String> userNameMap = projectMemberList.stream().collect(Collectors.toMap(ProjectUserMemberDTO::getId, ProjectUserMemberDTO::getName));
        return getUserCountDTO(userNameMap, moduleIds, projectId, toStartTime, toEndTime);

    }

    @NotNull
    private OverViewCountDTO getUserCountDTO(Map<String, String> userNameMap, List<String> moduleIds, String projectId, Long toStartTime, Long toEndTime) {
        List<String> xaxis = new ArrayList<>(userNameMap.values());
        Set<String> userIds = userNameMap.keySet();
        Map<String, Integer> userCaseCountMap;
        Map<String, Integer> userReviewCountMap;
        Map<String, Integer> userApiCountMap;
        Map<String, Integer> userApiScenarioCountMap;
        Map<String, Integer> userApiCaseCountMap;
        Map<String, Integer> userPlanCountMap;
        Map<String, Integer> userBugCountMap;

        if (moduleIds.contains(FUNCTIONAL_CASE_MODULE)) {
            List<ProjectUserCreateCount> userCreateCaseCount = extFunctionalCaseMapper.userCreateCaseCount(projectId, toStartTime, toEndTime, userIds);
            userCaseCountMap = userCreateCaseCount.stream().collect(Collectors.toMap(ProjectUserCreateCount::getUserId, ProjectUserCreateCount::getCount));
            List<ProjectUserCreateCount> userCreateReviewCount = extCaseReviewMapper.userCreateReviewCount(projectId, toStartTime, toEndTime, userIds);
            userReviewCountMap = userCreateReviewCount.stream().collect(Collectors.toMap(ProjectUserCreateCount::getUserId, ProjectUserCreateCount::getCount));
        } else {
            userReviewCountMap = new HashMap<>();
            userCaseCountMap = new HashMap<>();
        }
        if (moduleIds.contains(API_TEST_MODULE)) {
            List<ProjectUserCreateCount> userCreateApiCount = extApiDefinitionMapper.userCreateApiCount(projectId, toStartTime, toEndTime, userIds);
            userApiCountMap = userCreateApiCount.stream().collect(Collectors.toMap(ProjectUserCreateCount::getUserId, ProjectUserCreateCount::getCount));
            List<ProjectUserCreateCount> userCreateApiScenarioCount = extApiScenarioMapper.userCreateApiScenarioCount(projectId, toStartTime, toEndTime, userIds);
            userApiScenarioCountMap = userCreateApiScenarioCount.stream().collect(Collectors.toMap(ProjectUserCreateCount::getUserId, ProjectUserCreateCount::getCount));
            List<ProjectUserCreateCount> userCreateApiCaseCount = extApiTestCaseMapper.userCreateApiCaseCount(projectId, toStartTime, toEndTime, userIds);
            userApiCaseCountMap = userCreateApiCaseCount.stream().collect(Collectors.toMap(ProjectUserCreateCount::getUserId, ProjectUserCreateCount::getCount));
        } else {
            userApiCountMap = new HashMap<>();
            userApiScenarioCountMap = new HashMap<>();
            userApiCaseCountMap = new HashMap<>();
        }
        if (moduleIds.contains(TEST_PLAN_MODULE)) {
            List<ProjectUserCreateCount> userCreatePlanCount = extTestPlanMapper.userCreatePlanCount(projectId, toStartTime, toEndTime, userIds);
            userPlanCountMap = userCreatePlanCount.stream().collect(Collectors.toMap(ProjectUserCreateCount::getUserId, ProjectUserCreateCount::getCount));
        } else {
            userPlanCountMap = new HashMap<>();
        }
        if (moduleIds.contains(BUG_MODULE)) {
            List<ProjectUserCreateCount> userCreateBugCount = extBugMapper.userCreateBugCount(projectId, toStartTime, toEndTime, userIds);
            userBugCountMap = userCreateBugCount.stream().collect(Collectors.toMap(ProjectUserCreateCount::getUserId, ProjectUserCreateCount::getCount));
        } else {
            userBugCountMap = new HashMap<>();
        }

        List<Integer> userCaseCount = new ArrayList<>();
        List<Integer> userReviewCount = new ArrayList<>();
        List<Integer> userApiCount = new ArrayList<>();
        List<Integer> userApiCaseCount = new ArrayList<>();
        List<Integer> userApiScenarioCount = new ArrayList<>();
        List<Integer> userPlanCount = new ArrayList<>();
        List<Integer> userBugCount = new ArrayList<>();

        userNameMap.forEach((id, userName) -> {
            if (userCaseCountMap.get(id) != null) {
                userCaseCount.add(userCaseCountMap.get(id));
            } else {
                userCaseCount.add(0);
            }
            if (userReviewCountMap.get(id) != null) {
                userReviewCount.add(userCaseCountMap.get(id));
            } else {
                userReviewCount.add(0);
            }
            if (userApiCountMap.get(id) != null) {
                userApiCount.add(userApiCountMap.get(id));
            } else {
                userApiCount.add(0);
            }
            if (userApiCaseCountMap.get(id) != null) {
                userApiCaseCount.add(userApiCaseCountMap.get(id));
            } else {
                userApiCaseCount.add(0);
            }
            if (userApiScenarioCountMap.get(id) != null) {
                userApiScenarioCount.add(userApiScenarioCountMap.get(id));
            } else {
                userApiScenarioCount.add(0);
            }
            if (userPlanCountMap.get(id) != null) {
                userPlanCount.add(userPlanCountMap.get(id));
            } else {
                userPlanCount.add(0);
            }
            if (userBugCountMap.get(id) != null) {
                userBugCount.add(userBugCountMap.get(id));
            } else {
                userBugCount.add(0);
            }
        });
        List<NameArrayDTO> nameArrayDTOList = new ArrayList<>();
        NameArrayDTO userCaseArray = new NameArrayDTO();
        userCaseArray.setCount(userCaseCount);
        nameArrayDTOList.add(userCaseArray);

        NameArrayDTO userReviewArray = new NameArrayDTO();
        userReviewArray.setCount(userReviewCount);
        nameArrayDTOList.add(userReviewArray);

        NameArrayDTO userApiArray = new NameArrayDTO();
        userApiArray.setCount(userApiCount);
        nameArrayDTOList.add(userApiArray);

        NameArrayDTO userApiCaseArray = new NameArrayDTO();
        userApiCaseArray.setCount(userApiCaseCount);
        nameArrayDTOList.add(userApiCaseArray);

        NameArrayDTO userApiScenarioArray = new NameArrayDTO();
        userApiScenarioArray.setCount(userApiScenarioCount);
        nameArrayDTOList.add(userApiScenarioArray);

        NameArrayDTO userPlanArray = new NameArrayDTO();
        userPlanArray.setCount(userPlanCount);
        nameArrayDTOList.add(userPlanArray);

        NameArrayDTO userBugArray = new NameArrayDTO();
        userBugArray.setCount(userBugCount);
        nameArrayDTOList.add(userBugArray);

        OverViewCountDTO overViewCountDTO = new OverViewCountDTO();
        overViewCountDTO.setXAxis(xaxis);
        overViewCountDTO.setProjectCountList(nameArrayDTOList);
        return overViewCountDTO;
    }


    public StatisticsDTO projectCaseCount(DashboardFrontPageRequest request) {
        String projectId = request.getProjectIds().getFirst();
        StatisticsDTO statisticsDTO = new StatisticsDTO();
        if (Boolean.FALSE.equals(checkModule(projectId, FUNCTIONAL_CASE_MODULE))) return statisticsDTO;
        List<StatusPercentDTO> statusPercentList = new ArrayList<>();
        Long toStartTime = request.getToStartTime();
        Long toEndTime = request.getToEndTime();
        List<FunctionalCaseStatisticDTO> statisticListByProjectId = extFunctionalCaseMapper.getStatisticListByProjectId(projectId, toStartTime, toEndTime);
        buildStatusPercentList(statisticListByProjectId, statusPercentList);
        statisticsDTO.setStatusPercentList(statusPercentList);
        Map<String, List<FunctionalCaseStatisticDTO>> reviewStatusMap = statisticListByProjectId.stream().collect(Collectors.groupingBy(FunctionalCaseStatisticDTO::getReviewStatus));
        Map<String, List<NameCountDTO>> statusStatisticsMap = new HashMap<>();
        List<NameCountDTO> reviewList = getReviewList(reviewStatusMap, statisticListByProjectId);
        statusStatisticsMap.put("review", reviewList);
        List<NameCountDTO> passList = getPassList(reviewStatusMap, statisticListByProjectId);
        statusStatisticsMap.put("pass", passList);
        statisticsDTO.setStatusStatisticsMap(statusStatisticsMap);
        return statisticsDTO;
    }

    private Boolean checkModule(String projectId, String module) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        List<String> moduleIds = JSON.parseArray(project.getModuleSetting(), String.class);
        return moduleIds.contains(module);
    }

    @NotNull
    private static List<NameCountDTO> getPassList(Map<String, List<FunctionalCaseStatisticDTO>> reviewStatusMap, List<FunctionalCaseStatisticDTO> statisticListByProjectId) {
        List<NameCountDTO> passList = new ArrayList<>();
        List<FunctionalCaseStatisticDTO> hasPassList = reviewStatusMap.get(FunctionalCaseReviewStatus.PASS.toString());
        if (CollectionUtils.isEmpty(hasPassList)) {
            hasPassList = new ArrayList<>();
        }
        NameCountDTO passRate = new NameCountDTO();
        passRate.setName(Translator.get("functional_case.passRate"));
        if (CollectionUtils.isNotEmpty(statisticListByProjectId)) {
            BigDecimal divide = BigDecimal.valueOf(hasPassList.size()).divide(BigDecimal.valueOf(statisticListByProjectId.size()), 0, RoundingMode.HALF_UP);
            passRate.setCount(Integer.valueOf(String.valueOf(divide.multiply(BigDecimal.valueOf(100)))));
        } else {
            passRate.setCount(0);
        }
        passList.add(passRate);
        NameCountDTO hasPass = new NameCountDTO();
        hasPass.setName(Translator.get("functional_case.hasPass"));
        hasPass.setCount(hasPassList.size());
        passList.add(hasPass);
        NameCountDTO unPass = new NameCountDTO();
        unPass.setName(Translator.get("functional_case.unPass"));
        unPass.setCount(statisticListByProjectId.size() - hasPassList.size());
        passList.add(unPass);
        return passList;
    }

    @NotNull
    private static List<NameCountDTO> getReviewList(Map<String, List<FunctionalCaseStatisticDTO>> reviewStatusMap, List<FunctionalCaseStatisticDTO> statisticListByProjectId) {
        List<NameCountDTO> reviewList = new ArrayList<>();
        List<FunctionalCaseStatisticDTO> unReviewList = reviewStatusMap.get(FunctionalCaseReviewStatus.UN_REVIEWED.toString());
        if (CollectionUtils.isEmpty(unReviewList)) {
            unReviewList = new ArrayList<>();
        }
        NameCountDTO reviewRate = new NameCountDTO();
        reviewRate.setName(Translator.get("functional_case.reviewRate"));
        if (CollectionUtils.isEmpty(statisticListByProjectId)) {
            reviewRate.setCount(0);
        } else {
            BigDecimal divide = BigDecimal.valueOf(statisticListByProjectId.size() - unReviewList.size()).divide(BigDecimal.valueOf(statisticListByProjectId.size()), 0, RoundingMode.HALF_UP);
            reviewRate.setCount(Integer.valueOf(String.valueOf(divide.multiply(BigDecimal.valueOf(100)))));
        }
        reviewList.add(reviewRate);
        NameCountDTO hasReview = new NameCountDTO();
        hasReview.setName(Translator.get("functional_case.hasReview"));
        hasReview.setCount(statisticListByProjectId.size() - unReviewList.size());
        reviewList.add(hasReview);
        NameCountDTO unReview = new NameCountDTO();
        unReview.setName(Translator.get("functional_case.unReview"));
        unReview.setCount(unReviewList.size());
        reviewList.add(unReview);
        return reviewList;
    }

    private static void buildStatusPercentList(List<FunctionalCaseStatisticDTO> statisticListByProjectId, List<StatusPercentDTO> statusPercentList) {
        Map<String, List<FunctionalCaseStatisticDTO>> priorityMap = statisticListByProjectId.stream().collect(Collectors.groupingBy(FunctionalCaseStatisticDTO::getPriority));
        for (int i = 0; i < 4; i++) {
            String priority = "P" + i;
            StatusPercentDTO statusPercentDTO = new StatusPercentDTO();
            statusPercentDTO.setStatus(priority);
            List<FunctionalCaseStatisticDTO> functionalCaseStatisticDTOS = priorityMap.get(priority);
            if (CollectionUtils.isNotEmpty(functionalCaseStatisticDTOS)) {
                int size = functionalCaseStatisticDTOS.size();
                statusPercentDTO.setCount(size);
                BigDecimal divide = BigDecimal.valueOf(size).divide(BigDecimal.valueOf(statisticListByProjectId.size()), 2, RoundingMode.HALF_UP);
                statusPercentDTO.setPercentValue(divide.multiply(BigDecimal.valueOf(100)) + "%");
            } else {
                statusPercentDTO.setCount(0);
                statusPercentDTO.setPercentValue("0%");
            }
            statusPercentList.add(statusPercentDTO);
        }
    }

    public StatisticsDTO projectAssociateCaseCount(DashboardFrontPageRequest request) {
        String projectId = request.getProjectIds().getFirst();
        StatisticsDTO statisticsDTO = new StatisticsDTO();
        if (Boolean.FALSE.equals(checkModule(projectId, FUNCTIONAL_CASE_MODULE))) return statisticsDTO;
        Long toStartTime = request.getToStartTime();
        Long toEndTime = request.getToEndTime();
        long caseTestCount = extFunctionalCaseMapper.caseTestCount(projectId, toStartTime, toEndTime);
        long simpleCaseCount = extFunctionalCaseMapper.simpleCaseCount(projectId, toStartTime, toEndTime);
        List<NameCountDTO> coverList = getCoverList((int) simpleCaseCount, (int) caseTestCount, (int) (simpleCaseCount - caseTestCount));
        Map<String, List<NameCountDTO>> statusStatisticsMap = new HashMap<>();
        statusStatisticsMap.put("cover", coverList);
        statisticsDTO.setStatusStatisticsMap(statusStatisticsMap);
        return statisticsDTO;
    }

    public OverViewCountDTO projectBugHandleUser(DashboardFrontPageRequest request) {
        String projectId = request.getProjectIds().getFirst();
        if (Boolean.FALSE.equals(checkModule(projectId, BUG_MODULE)))
            return new OverViewCountDTO(null, new ArrayList<>(), new ArrayList<>());
        Long toStartTime = request.getToStartTime();
        Long toEndTime = request.getToEndTime();
        List<SelectOption> headerHandlerOption = getHandlerOption(request.getHandleUsers(), projectId);
        //获取每个人每个状态有多少数据(已按照用户id排序)
        List<SelectOption> headerStatusOption = bugStatusService.getHeaderStatusOption(projectId);
        Set<String> platforms = getPlatforms(projectId);
        List<String> handleUserIds = headerHandlerOption.stream().sorted(Comparator.comparing(SelectOption::getValue)).map(SelectOption::getValue).collect(Collectors.toList());
        List<ProjectUserStatusCountDTO> projectUserStatusCountDTOS = extBugMapper.projectUserBugStatusCount(projectId, toStartTime, toEndTime, handleUserIds, platforms);
        Map<String, SelectOption> statusMap = headerStatusOption.stream().collect(Collectors.toMap(SelectOption::getValue, t -> t));
        List<String> xaxis = headerHandlerOption.stream().sorted(Comparator.comparing(SelectOption::getValue)).map(SelectOption::getText).toList();
        Map<String, List<Integer>> statusCountArrayMap = getStatusCountArrayMap(projectUserStatusCountDTOS, statusMap, handleUserIds);
        return getHandleUserCount(xaxis, statusMap, statusCountArrayMap);
    }

    @NotNull
    private static OverViewCountDTO getHandleUserCount(List<String> xaxis, Map<String, SelectOption> statusMap, Map<String, List<Integer>> statusCountArrayMap) {
        OverViewCountDTO overViewCountDTO = new OverViewCountDTO();
        //组装X轴数据
        overViewCountDTO.setXAxis(xaxis);
        List<NameArrayDTO> projectCountList = getProjectCountList(statusMap, statusCountArrayMap);
        overViewCountDTO.setProjectCountList(projectCountList);
        return overViewCountDTO;
    }

    /**
     * 同一状态不同人的数量统计Map转换数据结构增加状态名称
     *
     * @param statusMap           状态名称集合
     * @param statusCountArrayMap 同一状态不同人的数量统计Map
     * @return List<NameArrayDTO>
     */
    private static List<NameArrayDTO> getProjectCountList(Map<String, SelectOption> statusMap, Map<String, List<Integer>> statusCountArrayMap) {
        List<NameArrayDTO> projectCountList = new ArrayList<>();
        statusCountArrayMap.forEach((status, countArray) -> {
            NameArrayDTO nameArrayDTO = new NameArrayDTO();
            nameArrayDTO.setName(statusMap.get(status).getText());
            nameArrayDTO.setCount(countArray);
            projectCountList.add(nameArrayDTO);
        });
        return projectCountList;
    }

    /**
     * 根据处理人排序的处理人状态统计 将同一状态不同人的数量统计到一起
     *
     * @param projectUserStatusCountDTOS 根据处理人排序的处理人状态统计集合
     * @return 同一状态不同人的数量统计Map
     */
    private static Map<String, List<Integer>> getStatusCountArrayMap(List<ProjectUserStatusCountDTO> projectUserStatusCountDTOS, Map<String, SelectOption> statusMap, List<String> handleUserIds) {
        Map<String, List<Integer>> statusCountArrayMap = new HashMap<>();
        Map<String, List<String>> statusUserArrayMap = new HashMap<>();
        for (ProjectUserStatusCountDTO projectUserStatusCountDTO : projectUserStatusCountDTOS) {
            String status = projectUserStatusCountDTO.getStatus();
            List<Integer> countList = statusCountArrayMap.get(status);
            List<String> userIds = statusUserArrayMap.get(status);
            if (CollectionUtils.isEmpty(countList)) {
                List<Integer> countArray = new ArrayList<>();
                List<String> userArray = new ArrayList<>();
                countArray.add(projectUserStatusCountDTO.getCount());
                userArray.add(projectUserStatusCountDTO.getUserId());
                statusCountArrayMap.put(status, countArray);
                statusUserArrayMap.put(status, userArray);
            } else {
                userIds.add(projectUserStatusCountDTO.getUserId());
                countList.add(projectUserStatusCountDTO.getCount());
                statusCountArrayMap.put(status, countList);
                statusUserArrayMap.put(status, userIds);
            }
        }
        List<Integer> countArray = new ArrayList<>();
        for (int i = 0; i < handleUserIds.size(); i++) {
            countArray.add(0);
        }
        statusMap.forEach((k, v) -> {
            List<Integer> handleUserCounts = statusCountArrayMap.get(k);
            List<String> userIds = statusUserArrayMap.get(k);
            if (CollectionUtils.isEmpty(handleUserCounts)) {
                statusCountArrayMap.put(k, countArray);
            } else {
                for (int i = 0; i < handleUserIds.size(); i++) {
                    if (userIds.size() > i) {
                        if (!StringUtils.equalsIgnoreCase(userIds.get(i), handleUserIds.get(i))) {
                            userIds.add(i, handleUserIds.get(i));
                            handleUserCounts.add(i, 0);
                        }
                    } else {
                        handleUserCounts.add(0);
                    }
                }
            }
        });
        return statusCountArrayMap;
    }

    /**
     * 获取当前项目根据筛选有多少处理人
     *
     * @param handleUsers 页面选择的处理人
     * @param projectId   项目id
     * @return 处理人id 与 名称的集合
     */
    private List<SelectOption> getHandlerOption(List<String> handleUsers, String projectId) {
        List<SelectOption> headerHandlerOption;
        if (CollectionUtils.isEmpty(handleUsers)) {
            headerHandlerOption = bugCommonService.getHeaderHandlerOption(projectId);
        } else {
            List<SelectOption> headerHandlerOptionList = bugCommonService.getHeaderHandlerOption(projectId);
            headerHandlerOption = headerHandlerOptionList.stream().filter(t -> handleUsers.contains(t.getValue())).toList();
        }
        return headerHandlerOption;
    }

    /**
     * 根据项目id获取当前对接平台，与本地进行组装
     *
     * @param projectId 项目ID
     * @return 本地与对接平台集合
     */
    private Set<String> getPlatforms(String projectId) {
        String platformName = projectApplicationService.getPlatformName(projectId);
        Set<String> platforms = new HashSet<>();
        platforms.add(BugPlatform.LOCAL.getName());
        if (!StringUtils.equalsIgnoreCase(platformName, BugPlatform.LOCAL.getName())) {
            platforms.add(platformName);
        }
        return platforms;
    }

    public StatisticsDTO projectReviewCaseCount(DashboardFrontPageRequest request) {
        String projectId = request.getProjectIds().getFirst();
        StatisticsDTO statisticsDTO = new StatisticsDTO();
        if (Boolean.FALSE.equals(checkModule(projectId, FUNCTIONAL_CASE_MODULE))) return statisticsDTO;
        Long toStartTime = request.getToStartTime();
        Long toEndTime = request.getToEndTime();
        List<FunctionalCaseStatisticDTO> statisticListByProjectId = extFunctionalCaseMapper.getStatisticListByProjectId(projectId, toStartTime, toEndTime);
        List<FunctionalCaseStatisticDTO> unReviewCaseList = statisticListByProjectId.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getReviewStatus(), FunctionalCaseReviewStatus.UN_REVIEWED.toString())).toList();
        int reviewCount = statisticListByProjectId.size() - unReviewCaseList.size();
        List<NameCountDTO> coverList = getCoverList(statisticListByProjectId.size(), reviewCount, unReviewCaseList.size());
        Map<String, List<NameCountDTO>> statusStatisticsMap = new HashMap<>();
        statusStatisticsMap.put("cover", coverList);
        statisticsDTO.setStatusStatisticsMap(statusStatisticsMap);
        List<StatusPercentDTO> statusPercentList = getStatusPercentList(projectId, toStartTime, toEndTime);
        statisticsDTO.setStatusPercentList(statusPercentList);
        return statisticsDTO;
    }

    @NotNull
    private List<StatusPercentDTO> getStatusPercentList(String projectId, Long toStartTime, Long toEndTime) {
        List<StatusPercentDTO> statusPercentList = new ArrayList<>();
        Map<String, String> statusNameMap = buildStatusNameMap();
        List<ProjectUserStatusCountDTO> projectUserStatusCountDTOS = extCaseReviewMapper.statusReviewCount(projectId, toStartTime, toEndTime);
        Map<String, Integer> statusCountMap = projectUserStatusCountDTOS.stream().collect(Collectors.toMap(ProjectUserStatusCountDTO::getStatus, ProjectUserStatusCountDTO::getCount));
        statusNameMap.forEach((k, v) -> {
            StatusPercentDTO statusPercentDTO = new StatusPercentDTO();
            Integer count = statusCountMap.get(k);
            statusPercentDTO.setStatus(v);
            if (count != null) {
                statusPercentDTO.setCount(count);
            } else {
                count = 0;
                statusPercentDTO.setCount(0);
            }
            if (CollectionUtils.isNotEmpty(projectUserStatusCountDTOS)) {
                BigDecimal divide = BigDecimal.valueOf(count).divide(BigDecimal.valueOf(projectUserStatusCountDTOS.size()), 2, RoundingMode.HALF_UP);
                statusPercentDTO.setPercentValue(divide.multiply(BigDecimal.valueOf(100)) + "%");
            } else {
                statusPercentDTO.setPercentValue("0%");
            }
            statusPercentList.add(statusPercentDTO);
        });
        return statusPercentList;
    }

    @NotNull
    private static List<NameCountDTO> getCoverList(int totalCount, int coverCount, int unCoverCount) {
        List<NameCountDTO> coverList = new ArrayList<>();
        NameCountDTO coverRate = new NameCountDTO();
        if (totalCount>0) {
            BigDecimal divide = BigDecimal.valueOf(coverCount).divide(BigDecimal.valueOf(totalCount), 0, RoundingMode.HALF_UP);
            coverRate.setCount(Integer.valueOf(String.valueOf(divide.multiply(BigDecimal.valueOf(100)))));
        }
        coverRate.setName(Translator.get("functional_case.coverRate"));
        coverList.add(coverRate);
        NameCountDTO hasCover = new NameCountDTO();
        hasCover.setCount(coverCount);
        hasCover.setName(Translator.get("functional_case.hasCover"));
        coverList.add(hasCover);
        NameCountDTO unCover = new NameCountDTO();
        unCover.setCount(unCoverCount);
        unCover.setName(Translator.get("functional_case.unCover"));
        coverList.add(unCover);
        return coverList;
    }

    private static Map<String, String> buildStatusNameMap() {
        Map<String, String> statusNameMap = new HashMap<>();
        statusNameMap.put(CaseReviewStatus.PREPARED.toString(), Translator.get("case_review.prepared"));
        statusNameMap.put(CaseReviewStatus.UNDERWAY.toString(), Translator.get("case_review.underway"));
        statusNameMap.put(CaseReviewStatus.COMPLETED.toString(), Translator.get("case_review.completed"));
        return statusNameMap;
    }
}


