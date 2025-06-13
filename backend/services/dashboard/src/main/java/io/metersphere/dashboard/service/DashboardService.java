package io.metersphere.dashboard.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.constants.ApiDefinitionStatus;
import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.dto.definition.ApiDefinitionUpdateDTO;
import io.metersphere.api.dto.definition.ApiRefSourceCountDTO;
import io.metersphere.api.mapper.ExtApiDefinitionMapper;
import io.metersphere.api.mapper.ExtApiScenarioMapper;
import io.metersphere.api.mapper.ExtApiTestCaseMapper;
import io.metersphere.api.service.ApiTestService;
import io.metersphere.bug.domain.Bug;
import io.metersphere.bug.enums.BugPlatform;
import io.metersphere.bug.mapper.ExtBugMapper;
import io.metersphere.bug.service.BugCommonService;
import io.metersphere.bug.service.BugStatusService;
import io.metersphere.dashboard.constants.DashboardUserLayoutKeys;
import io.metersphere.dashboard.dto.*;
import io.metersphere.dashboard.request.DashboardFrontPageRequest;
import io.metersphere.dashboard.response.CascadeChildrenDTO;
import io.metersphere.dashboard.response.OverViewCountDTO;
import io.metersphere.dashboard.response.StatisticsDTO;
import io.metersphere.functional.constants.CaseReviewStatus;
import io.metersphere.functional.constants.FunctionalCaseReviewStatus;
import io.metersphere.functional.dto.CaseReviewDTO;
import io.metersphere.functional.dto.FunctionalCaseStatisticDTO;
import io.metersphere.functional.mapper.ExtCaseReviewMapper;
import io.metersphere.functional.mapper.ExtFunctionalCaseMapper;
import io.metersphere.functional.request.CaseReviewPageRequest;
import io.metersphere.functional.service.CaseReviewService;
import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.TestPlanAndGroupInfoDTO;
import io.metersphere.plan.dto.TestPlanBugCaseDTO;
import io.metersphere.plan.dto.response.TestPlanBugPageResponse;
import io.metersphere.plan.dto.response.TestPlanStatisticsResponse;
import io.metersphere.plan.mapper.*;
import io.metersphere.plan.service.TestPlanStatisticsService;
import io.metersphere.plugin.platform.dto.SelectOption;
import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.ProjectCountDTO;
import io.metersphere.project.dto.ProjectUserCreateCount;
import io.metersphere.project.dto.ProjectUserStatusCountDTO;
import io.metersphere.project.mapper.ExtProjectMapper;
import io.metersphere.project.mapper.ExtProjectMemberMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.system.service.PermissionCheckService;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.project.service.ProjectService;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.dto.CombineCondition;
import io.metersphere.sdk.dto.CombineSearch;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.ProtocolDTO;
import io.metersphere.system.dto.request.schedule.BaseScheduleConfigRequest;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.user.ProjectUserMemberDTO;
import io.metersphere.system.dto.user.UserExtendDTO;
import io.metersphere.system.mapper.*;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.ScheduleUtils;
import io.metersphere.system.utils.SessionUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static io.metersphere.dashboard.result.DashboardResultCode.NO_PROJECT_PERMISSION;

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
    private ExtExecTaskItemMapper extExecTaskItemMapper;
    @Resource
    private ProjectService projectService;
    @Resource
    private PermissionCheckService permissionCheckService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private UserLayoutMapper userLayoutMapper;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private ExtUserMapper extUserMapper;
    @Resource
    private BugCommonService bugCommonService;
    @Resource
    private BugStatusService bugStatusService;
    @Resource
    private ProjectApplicationService projectApplicationService;
    @Resource
    private CaseReviewService caseReviewService;
    @Resource
    private ApiTestService apiTestService;
    @Resource
    private ExtSystemProjectMapper extSystemProjectMapper;
    @Resource
    private TestPlanStatisticsService planStatisticsService;
    @Resource
    private ExtTestPlanFunctionalCaseMapper extTestPlanFunctionalCaseMapper;
    @Resource
    private ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    @Resource
    private ExtTestPlanApiScenarioMapper extTestPlanApiScenarioMapper;
    @Resource
    private ExtTestPlanBugMapper extTestPlanBugMapper;
    @Resource
    private TestPlanConfigMapper testPlanConfigMapper;
    @Resource
    private ScheduleMapper scheduleMapper;


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

    public static final String NONE = "NONE";


    public OverViewCountDTO createByMeCount(DashboardFrontPageRequest request, String userId) {
        OverViewCountDTO map = getNoProjectData(request);
        if (map != null) return map;
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

    @Nullable
    private static OverViewCountDTO getNoProjectData(DashboardFrontPageRequest request) {
        if (!request.isSelectAll() && CollectionUtils.isEmpty(request.getProjectIds())) {
            Map<String, Object> map = new HashMap<>();
            map.put(FUNCTIONAL, 0);
            map.put(CASE_REVIEW, 0);
            map.put(API, 0);
            map.put(API_CASE, 0);
            map.put(API_SCENARIO, 0);
            map.put(TEST_PLAN, 0);
            map.put(BUG_COUNT, 0);
            return new OverViewCountDTO(map, new ArrayList<>(), new ArrayList<>(), 0);
        }
        return null;
    }

    @NotNull
    private OverViewCountDTO getModuleCountMap(Map<String, Set<String>> permissionModuleProjectIdMap, List<Project> projects, Long toStartTime, Long toEndTime, String userId) {
        Map<String, String> projectNameMap = projects.stream().collect(Collectors.toMap(Project::getId, Project::getName));
        Map<String, Object> map = new HashMap<>();
        Map<String, Integer> projectCaseCountMap;
        Map<String, Integer> projectReviewCountMap;
        Map<String, Integer> projectApiCountMap;
        Map<String, Integer> projectApiScenarioCountMap;
        Map<String, Integer> projectApiCaseCountMap;
        Map<String, Integer> projectPlanCountMap;
        Map<String, Integer> projectBugCountMap;
        //功能用例
        Set<String> caseProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.FUNCTIONAL_CASE_READ);
        if (CollectionUtils.isNotEmpty(caseProjectIds)) {
            //有权限
            List<ProjectCountDTO> projectCaseCount = extFunctionalCaseMapper.projectCaseCount(caseProjectIds, toStartTime, toEndTime, userId);
            int caseCount = projectCaseCount.stream().mapToInt(ProjectCountDTO::getCount).sum();
            map.put(FUNCTIONAL, caseCount);
            projectCaseCountMap = projectCaseCount.stream().collect(Collectors.toMap(ProjectCountDTO::getProjectId, ProjectCountDTO::getCount));
        } else {
            projectCaseCountMap = new HashMap<>();
        }
        //用例评审
        Set<String> reviewProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.CASE_REVIEW_READ);
        if (CollectionUtils.isNotEmpty(reviewProjectIds)) {
            List<ProjectCountDTO> projectReviewCount = extCaseReviewMapper.projectReviewCount(reviewProjectIds, toStartTime, toEndTime, userId);
            int reviewCount = projectReviewCount.stream().mapToInt(ProjectCountDTO::getCount).sum();
            map.put(CASE_REVIEW, reviewCount);
            projectReviewCountMap = projectReviewCount.stream().collect(Collectors.toMap(ProjectCountDTO::getProjectId, ProjectCountDTO::getCount));
        } else {
            projectReviewCountMap = new HashMap<>();
        }
        //接口
        Set<String> apiProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.PROJECT_API_DEFINITION_READ);
        if (CollectionUtils.isNotEmpty(apiProjectIds)) {
            List<ProjectCountDTO> projectApiCount = extApiDefinitionMapper.projectApiCount(apiProjectIds, toStartTime, toEndTime, userId);
            int apiCount = projectApiCount.stream().mapToInt(ProjectCountDTO::getCount).sum();
            map.put(API, apiCount);
            projectApiCountMap = projectApiCount.stream().collect(Collectors.toMap(ProjectCountDTO::getProjectId, ProjectCountDTO::getCount));
        } else {
            projectApiCountMap = new HashMap<>();
        }
        //接口用例
        Set<String> apiCaseProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ);
        if (CollectionUtils.isNotEmpty(apiCaseProjectIds)) {
            List<ProjectCountDTO> projectApiCaseCount = extApiTestCaseMapper.projectApiCaseCount(apiCaseProjectIds, toStartTime, toEndTime, userId);
            int apiCaseCount = projectApiCaseCount.stream().mapToInt(ProjectCountDTO::getCount).sum();
            map.put(API_CASE, apiCaseCount);
            projectApiCaseCountMap = projectApiCaseCount.stream().collect(Collectors.toMap(ProjectCountDTO::getProjectId, ProjectCountDTO::getCount));
        } else {
            projectApiCaseCountMap = new HashMap<>();
        }
        //接口场景
        Set<String> scenarioProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.PROJECT_API_SCENARIO_READ);
        if (CollectionUtils.isNotEmpty(scenarioProjectIds)) {
            List<ProjectCountDTO> projectApiScenarioCount = extApiScenarioMapper.projectApiScenarioCount(scenarioProjectIds, toStartTime, toEndTime, userId);
            int apiScenarioCount = projectApiScenarioCount.stream().mapToInt(ProjectCountDTO::getCount).sum();
            map.put(API_SCENARIO, apiScenarioCount);
            projectApiScenarioCountMap = projectApiScenarioCount.stream().collect(Collectors.toMap(ProjectCountDTO::getProjectId, ProjectCountDTO::getCount));
        } else {
            projectApiScenarioCountMap = new HashMap<>();
        }
        //测试计划
        Set<String> planProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.TEST_PLAN_READ);
        if (CollectionUtils.isNotEmpty(planProjectIds)) {
            List<ProjectCountDTO> projectPlanCount = extTestPlanMapper.projectPlanCount(planProjectIds, toStartTime, toEndTime, userId);
            int testPlanCount = projectPlanCount.stream().mapToInt(ProjectCountDTO::getCount).sum();
            map.put(TEST_PLAN, testPlanCount);
            projectPlanCountMap = projectPlanCount.stream().collect(Collectors.toMap(ProjectCountDTO::getProjectId, ProjectCountDTO::getCount));
        } else {
            projectPlanCountMap = new HashMap<>();
        }
        //缺陷管理
        Set<String> bugProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.PROJECT_BUG_READ);
        if (CollectionUtils.isNotEmpty(bugProjectIds)) {
            List<ProjectCountDTO> projectBugCount = extBugMapper.projectBugCount(bugProjectIds, toStartTime, toEndTime, userId);
            int bugCount = projectBugCount.stream().mapToInt(ProjectCountDTO::getCount).sum();
            map.put(BUG_COUNT, bugCount);
            projectBugCountMap = projectBugCount.stream().collect(Collectors.toMap(ProjectCountDTO::getProjectId, ProjectCountDTO::getCount));

        } else {
            projectBugCountMap = new HashMap<>();
        }

        List<Integer> projectCaseCount = new ArrayList<>();
        List<Integer> projectReviewCount = new ArrayList<>();
        List<Integer> projectApiCount = new ArrayList<>();
        List<Integer> projectApiCaseCount = new ArrayList<>();
        List<Integer> projectApiScenarioCount = new ArrayList<>();
        List<Integer> projectPlanCount = new ArrayList<>();
        List<Integer> projectBugCount = new ArrayList<>();

        projectNameMap.forEach((id, userName) -> {
            if (projectCaseCountMap.get(id) != null) {
                projectCaseCount.add(projectCaseCountMap.get(id));
            } else {
                projectCaseCount.add(0);
            }
            if (projectReviewCountMap.get(id) != null) {
                projectReviewCount.add(projectReviewCountMap.get(id));
            } else {
                projectReviewCount.add(0);
            }
            if (projectApiCountMap.get(id) != null) {
                projectApiCount.add(projectApiCountMap.get(id));
            } else {
                projectApiCount.add(0);
            }
            if (projectApiCaseCountMap.get(id) != null) {
                projectApiCaseCount.add(projectApiCaseCountMap.get(id));
            } else {
                projectApiCaseCount.add(0);
            }
            if (projectApiScenarioCountMap.get(id) != null) {
                projectApiScenarioCount.add(projectApiScenarioCountMap.get(id));
            } else {
                projectApiScenarioCount.add(0);
            }
            if (projectPlanCountMap.get(id) != null) {
                projectPlanCount.add(projectPlanCountMap.get(id));
            } else {
                projectPlanCount.add(0);
            }
            if (projectBugCountMap.get(id) != null) {
                projectBugCount.add(projectBugCountMap.get(id));
            } else {
                projectBugCount.add(0);
            }
        });
        List<String> xaxis = new ArrayList<>(projectNameMap.values());

        List<NameArrayDTO> nameArrayDTOList = new ArrayList<>();
        NameArrayDTO userCaseArray = new NameArrayDTO();
        userCaseArray.setCount(projectCaseCount);
        nameArrayDTOList.add(userCaseArray);

        NameArrayDTO userReviewArray = new NameArrayDTO();
        userReviewArray.setCount(projectReviewCount);
        nameArrayDTOList.add(userReviewArray);

        NameArrayDTO userApiArray = new NameArrayDTO();
        userApiArray.setCount(projectApiCount);
        nameArrayDTOList.add(userApiArray);

        NameArrayDTO userApiCaseArray = new NameArrayDTO();
        userApiCaseArray.setCount(projectApiCaseCount);
        nameArrayDTOList.add(userApiCaseArray);

        NameArrayDTO userApiScenarioArray = new NameArrayDTO();
        userApiScenarioArray.setCount(projectApiScenarioCount);
        nameArrayDTOList.add(userApiScenarioArray);

        NameArrayDTO userPlanArray = new NameArrayDTO();
        userPlanArray.setCount(projectPlanCount);
        nameArrayDTOList.add(userPlanArray);

        NameArrayDTO userBugArray = new NameArrayDTO();
        userBugArray.setCount(projectBugCount);
        nameArrayDTOList.add(userBugArray);

        OverViewCountDTO overViewCountDTO = new OverViewCountDTO();
        overViewCountDTO.setXAxis(xaxis);
        overViewCountDTO.setCaseCountMap(map);
        overViewCountDTO.setProjectCountList(nameArrayDTOList);
        if (CollectionUtils.isEmpty(xaxis)) {
            overViewCountDTO.setErrorCode(NO_PROJECT_PERMISSION.getCode());
        }
        return overViewCountDTO;
    }

    public OverViewCountDTO projectViewCount(DashboardFrontPageRequest request, String userId) {
        OverViewCountDTO map = getNoProjectData(request);
        if (map != null) return map;
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
        Map<String, List<LayoutDTO>> getKeyMap = layoutDTO.stream().collect(Collectors.groupingBy(LayoutDTO::getKey));
        List<LayoutDTO> saveList = new ArrayList<>();
        getKeyMap.forEach((k, v) -> saveList.add(v.get(0)));
        UserLayout userLayout = new UserLayout();
        userLayout.setUserId(userId);
        userLayout.setOrgId(organizationId);
        if (CollectionUtils.isEmpty(saveList)) {
            userLayout.setConfiguration(new byte[0]);
        } else {
            String configuration = JSON.toJSONString(saveList);
            userLayout.setConfiguration(configuration.getBytes());
        }
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
        List<Project> allPermissionProjects = projectService.getUserProject(organizationId, userId);
        UserLayoutExample userLayoutExample = new UserLayoutExample();
        userLayoutExample.createCriteria().andUserIdEqualTo(userId).andOrgIdEqualTo(organizationId);
        List<UserLayout> userLayouts = userLayoutMapper.selectByExampleWithBLOBs(userLayoutExample);
        if (CollectionUtils.isEmpty(allPermissionProjects)) {
            return new ArrayList<>();
        }
        List<ProjectUserMemberDTO> orgProjectMemberList = extProjectMemberMapper.getOrgProjectMemberList(organizationId, null);
        if (CollectionUtils.isEmpty(userLayouts)) {
            List<String> userIds = orgProjectMemberList.stream().map(ProjectUserMemberDTO::getId).distinct().toList();
            return getDefaultLayoutDTOS(allPermissionProjects.getFirst().getId(), userIds);
        }
        UserLayout userLayout = userLayouts.getFirst();
        byte[] configuration = userLayout.getConfiguration();
        String layoutDTOStr = new String(configuration);
        if (StringUtils.isBlank(layoutDTOStr)) {
            return new ArrayList<>();
        }
        List<LayoutDTO> layoutDTOS = JSON.parseArray(layoutDTOStr, LayoutDTO.class);
        Map<String, Set<String>> permissionModuleProjectIdMap = dashboardProjectService.getPermissionModuleProjectIds(allPermissionProjects, userId);
        rebuildLayouts(layoutDTOS, allPermissionProjects, orgProjectMemberList, permissionModuleProjectIdMap);
        return layoutDTOS.stream().sorted(Comparator.comparing(LayoutDTO::getPos)).collect(Collectors.toList());
    }

    /**
     * 过滤用户在当前项目是否有移除或者项目是否被禁用以及用户是否被删除禁用
     *
     * @param layoutDTOS                   用户保存的布局
     * @param allPermissionProjects        用户有任意权限的所有在役项目
     * @param orgProjectMemberList         当前组织下所有有项目权限的成员
     * @param permissionModuleProjectIdMap 只读权限对应的开启模块的项目ids
     */
    private void rebuildLayouts(List<LayoutDTO> layoutDTOS, List<Project> allPermissionProjects, List<ProjectUserMemberDTO> orgProjectMemberList, Map<String, Set<String>> permissionModuleProjectIdMap) {
        for (LayoutDTO layoutDTO : layoutDTOS) {
            if (StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.PROJECT_VIEW.toString()) || StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.CREATE_BY_ME.toString())) {
                if (CollectionUtils.isEmpty(layoutDTO.getProjectIds())) {
                    layoutDTO.setProjectIds(new ArrayList<>());
                } else {
                    List<Project> list = allPermissionProjects.stream().filter(t -> layoutDTO.getProjectIds().contains(t.getId())).toList();
                    if (CollectionUtils.isNotEmpty(list)) {
                        layoutDTO.setProjectIds(list.stream().map(Project::getId).toList());
                    } else {
                        layoutDTO.setProjectIds(new ArrayList<>());
                        layoutDTO.setSelectAll(true);
                    }
                }
            } else if (StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.PROJECT_MEMBER_VIEW.toString())) {
                List<ProjectUserMemberDTO> list = orgProjectMemberList.stream().filter(t -> layoutDTO.getHandleUsers().contains(t.getId())).toList();
                layoutDTO.setHandleUsers(list.stream().map(ProjectUserMemberDTO::getId).distinct().toList());
                List<Project> projectList = allPermissionProjects.stream().filter(t -> layoutDTO.getProjectIds().contains(t.getId())).toList();
                if (CollectionUtils.isEmpty(projectList)) {
                    layoutDTO.setProjectIds(List.of(allPermissionProjects.getFirst().getId()));
                } else {
                    layoutDTO.setProjectIds(List.of(projectList.getFirst().getId()));
                }
            } else if (StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.CASE_COUNT.toString())
                    || StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.ASSOCIATE_CASE_COUNT.toString())
                    || StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.REVIEW_CASE_COUNT.toString())
                    || StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.REVIEWING_BY_ME.toString())) {
                Set<String> hasReadProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.FUNCTIONAL_CASE_READ);
                checkHasPermissionProject(layoutDTO, hasReadProjectIds);
            } else if (StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.API_COUNT.toString())
                    || StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.API_CHANGE.toString())) {
                Set<String> hasReadProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.PROJECT_API_DEFINITION_READ);
                checkHasPermissionProject(layoutDTO, hasReadProjectIds);
            } else if (StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.API_CASE_COUNT.toString())) {
                Set<String> hasReadProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ);
                checkHasPermissionProject(layoutDTO, hasReadProjectIds);
            } else if (StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.SCENARIO_COUNT.toString())) {
                Set<String> hasReadProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.PROJECT_API_SCENARIO_READ);
                checkHasPermissionProject(layoutDTO, hasReadProjectIds);
            } else if (StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.TEST_PLAN_COUNT.toString())
                    || StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.PLAN_LEGACY_BUG.toString())
                    || StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.PROJECT_PLAN_VIEW.toString())) {
                Set<String> hasReadProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.TEST_PLAN_READ);
                checkHasPermissionProject(layoutDTO, hasReadProjectIds);
                if (StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.PROJECT_PLAN_VIEW.toString())) {
                    setPlanId(layoutDTO);
                    if (StringUtils.isBlank(layoutDTO.getPlanId())) {
                        TestPlan latestPlanByProjectIds = extTestPlanMapper.getLatestPlanByProjectIds(hasReadProjectIds);
                        if (latestPlanByProjectIds!=null) {
                            layoutDTO.setPlanId(latestPlanByProjectIds.getId());
                            layoutDTO.setGroupId(latestPlanByProjectIds.getGroupId());
                            layoutDTO.setProjectIds(List.of(latestPlanByProjectIds.getProjectId()));
                        }
                    }
                }
            } else if (StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.BUG_COUNT.toString())
                    || StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.CREATE_BUG_BY_ME.toString())
                    || StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.HANDLE_BUG_BY_ME.toString())
                    || StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.BUG_HANDLE_USER.toString())) {
                Set<String> hasReadProjectIds = permissionModuleProjectIdMap.get(PermissionConstants.PROJECT_BUG_READ);
                checkHasPermissionProject(layoutDTO, hasReadProjectIds);
                if (StringUtils.equalsIgnoreCase(layoutDTO.getKey(), DashboardUserLayoutKeys.BUG_HANDLE_USER.toString())) {
                    List<ProjectUserMemberDTO> list = orgProjectMemberList.stream().filter(t -> layoutDTO.getHandleUsers().contains(t.getId())).toList();
                    layoutDTO.setHandleUsers(list.stream().map(ProjectUserMemberDTO::getId).distinct().toList());
                }
            }
        }
    }

    private void setPlanId(LayoutDTO layoutDTO) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(layoutDTO.getPlanId());
        if (testPlan == null || StringUtils.equalsIgnoreCase(testPlan.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED) || !StringUtils.equalsIgnoreCase(testPlan.getProjectId(),layoutDTO.getProjectIds().getFirst())) {
            TestPlan latestPlan = extTestPlanMapper.getLatestPlan(layoutDTO.getProjectIds().getFirst());
            if (latestPlan != null) {
                layoutDTO.setPlanId(latestPlan.getId());
                layoutDTO.setGroupId(layoutDTO.getGroupId());
            } else {
                layoutDTO.setPlanId(null);
            }
        }
    }

    private void checkHasPermissionProject(LayoutDTO layoutDTO, Set<String> hasReadProjectIds) {
        if (CollectionUtils.isEmpty(hasReadProjectIds)) {
            return;
        }
        List<String> projectIds = hasReadProjectIds.stream().filter(t -> layoutDTO.getProjectIds().contains(t)).toList();
        if (CollectionUtils.isEmpty(projectIds)) {
            layoutDTO.setProjectIds(List.of(new ArrayList<>(hasReadProjectIds).getFirst()));
        } else {
            layoutDTO.setProjectIds(List.of(projectIds.getFirst()));
        }
    }

    /**
     * 获取默认布局
     *
     * @param projectId 项目ID
     * @return List<LayoutDTO>
     */
    private List<LayoutDTO> getDefaultLayoutDTOS(String projectId, List<String> userIds) {
        List<LayoutDTO> layoutDTOS = new ArrayList<>();
        LayoutDTO projectLayoutDTO = buildDefaultLayoutDTO(DashboardUserLayoutKeys.PROJECT_VIEW, "workbench.homePage.projectOverview", 0, new ArrayList<>(), new ArrayList<>());
        layoutDTOS.add(projectLayoutDTO);
        LayoutDTO createByMeLayoutDTO = buildDefaultLayoutDTO(DashboardUserLayoutKeys.CREATE_BY_ME, "workbench.homePage.createdByMe", 1, new ArrayList<>(), new ArrayList<>());
        layoutDTOS.add(createByMeLayoutDTO);
        LayoutDTO projectMemberLayoutDTO = buildDefaultLayoutDTO(DashboardUserLayoutKeys.PROJECT_MEMBER_VIEW, "workbench.homePage.staffOverview", 2, List.of(projectId), userIds);
        layoutDTOS.add(projectMemberLayoutDTO);
        LayoutDTO planLayoutDTO = buildDefaultLayoutDTO(DashboardUserLayoutKeys.PROJECT_PLAN_VIEW, "workbench.homePage.testPlanOverview", 3, List.of(projectId), userIds);
        setPlanId(planLayoutDTO);
        layoutDTOS.add(planLayoutDTO);
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
    private static LayoutDTO buildDefaultLayoutDTO(DashboardUserLayoutKeys layoutKey, String label, int pos, List<String> projectIds, List<String> users) {
        LayoutDTO layoutDTO = new LayoutDTO();
        layoutDTO.setId(UUID.randomUUID().toString());
        layoutDTO.setKey(layoutKey.toString());
        layoutDTO.setLabel(label);
        layoutDTO.setPos(pos);
        layoutDTO.setSelectAll(true);
        layoutDTO.setFullScreen(true);
        layoutDTO.setProjectIds(projectIds);
        layoutDTO.setHandleUsers(users);
        return layoutDTO;
    }

    public OverViewCountDTO projectMemberViewCount(DashboardFrontPageRequest request) {
        String projectId = request.getProjectIds().getFirst();
        Project project = projectMapper.selectByPrimaryKey(projectId);
        List<String> moduleIds = JSON.parseArray(project.getModuleSetting(), String.class);
        Long toStartTime = request.getToStartTime();
        Long toEndTime = request.getToEndTime();
        List<ProjectUserMemberDTO> projectMemberList = extProjectMemberMapper.getProjectMemberList(projectId, request.getHandleUsers());
        Map<String, String> userNameMap = projectMemberList.stream().collect(Collectors.toMap(ProjectUserMemberDTO::getId, ProjectUserMemberDTO::getName));
        if (CollectionUtils.isEmpty(request.getHandleUsers())) {
            return new OverViewCountDTO(new HashMap<>(), new ArrayList<>(), new ArrayList<>(), 0);
        }
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
                userReviewCount.add(userReviewCountMap.get(id));
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
        if (CollectionUtils.isEmpty(xaxis)) {
            overViewCountDTO.setErrorCode(NO_PROJECT_PERMISSION.getCode());
        }
        return overViewCountDTO;
    }

    @NotNull
    private TestPlanStatisticsResponse buildStatisticsResponse(String planId, List<TestPlanFunctionalCase> functionalCases, List<TestPlanApiCase> apiCases, List<TestPlanApiScenario> apiScenarios) {
        //查出计划
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(planId);
        // 计划的更多配置
        TestPlanConfig planConfig = this.selectConfig(planId);
        //查询定时任务
        Schedule schedule = this.selectSchedule(planId);
        //构建TestPlanStatisticsResponse
        TestPlanStatisticsResponse statisticsResponse = new TestPlanStatisticsResponse();
        statisticsResponse.setId(planId);
        statisticsResponse.setStatus(testPlan.getStatus());
        // 测试计划组没有测试计划配置。同理，也不用参与用例等数据的计算
        if (planConfig != null) {
            statisticsResponse.setPassThreshold(planConfig.getPassThreshold());
            // 功能用例分组统计开始 (为空时, 默认为未执行)
            Map<String, Long> functionalCaseResultCountMap = planStatisticsService.countFunctionalCaseExecResultMap(functionalCases);
            // 接口用例分组统计开始 (为空时, 默认为未执行)
            Map<String, Long> apiCaseResultCountMap = planStatisticsService.countApiTestCaseExecResultMap(apiCases);
            // 接口场景用例分组统计开始 (为空时, 默认为未执行)
            Map<String, Long> apiScenarioResultCountMap = planStatisticsService.countApiScenarioExecResultMap(apiScenarios);

            // 用例数据汇总
            statisticsResponse.setFunctionalCaseCount(CollectionUtils.isNotEmpty(functionalCases) ? functionalCases.size() : 0);
            statisticsResponse.setApiCaseCount(CollectionUtils.isNotEmpty(apiCases) ? apiCases.size() : 0);
            statisticsResponse.setApiScenarioCount(CollectionUtils.isNotEmpty(apiScenarios) ? apiScenarios.size() : 0);
            statisticsResponse.setSuccessCount(planStatisticsService.countCaseMap(functionalCaseResultCountMap, apiCaseResultCountMap, apiScenarioResultCountMap, ResultStatus.SUCCESS.name()));
            statisticsResponse.setErrorCount(planStatisticsService.countCaseMap(functionalCaseResultCountMap, apiCaseResultCountMap, apiScenarioResultCountMap, ResultStatus.ERROR.name()));
            statisticsResponse.setFakeErrorCount(planStatisticsService.countCaseMap(functionalCaseResultCountMap, apiCaseResultCountMap, apiScenarioResultCountMap, ResultStatus.FAKE_ERROR.name()));
            statisticsResponse.setBlockCount(planStatisticsService.countCaseMap(functionalCaseResultCountMap, apiCaseResultCountMap, apiScenarioResultCountMap, ResultStatus.BLOCKED.name()));
            statisticsResponse.setPendingCount(planStatisticsService.countCaseMap(functionalCaseResultCountMap, apiCaseResultCountMap, apiScenarioResultCountMap, ExecStatus.PENDING.name()));
            statisticsResponse.calculateCaseTotal();
            statisticsResponse.calculatePassRate();
            statisticsResponse.calculateExecuteRate();
            statisticsResponse.calculateTestPlanIsPass();
        }
        //定时任务
        if (schedule != null) {
            BaseScheduleConfigRequest request = new BaseScheduleConfigRequest();
            request.setEnable(schedule.getEnable());
            request.setCron(schedule.getValue());
            request.setResourceId(planId);
            if (schedule.getConfig() != null) {
                request.setRunConfig(JSON.parseObject(schedule.getConfig(), Map.class));
            }
            statisticsResponse.setScheduleConfig(request);
            if (schedule.getEnable()) {
                statisticsResponse.setNextTriggerTime(ScheduleUtils.getNextTriggerTime(schedule.getValue()));
            }
        }
        statisticsResponse.calculateCaseTotal();
        statisticsResponse.calculatePassRate();
        statisticsResponse.calculateExecuteRate();
        statisticsResponse.calculateStatus();
        statisticsResponse.calculateTestPlanIsPass();
        return statisticsResponse;
    }

    private Schedule selectSchedule(String testPlanId) {
        ScheduleExample scheduleExample = new ScheduleExample();
        scheduleExample.createCriteria().andResourceIdEqualTo(testPlanId).andResourceTypeEqualTo(ScheduleResourceType.TEST_PLAN.name());
        List<Schedule> schedules = scheduleMapper.selectByExample(scheduleExample);
        return CollectionUtils.isNotEmpty(schedules) ? schedules.getFirst() : null;
    }

    private TestPlanConfig selectConfig(String testPlanId) {
        return testPlanConfigMapper.selectByPrimaryKey(testPlanId);
    }


    public OverViewCountDTO projectPlanViewCount(DashboardFrontPageRequest request, String currentUserId) {
        OverViewCountDTO overViewCountDTO = new OverViewCountDTO();
        String projectId = request.getProjectIds().getFirst();
        String planId = request.getPlanId();
        if (Boolean.FALSE.equals(permissionCheckService.checkModule(projectId, TEST_PLAN_MODULE, currentUserId, PermissionConstants.TEST_PLAN_READ))) {
            overViewCountDTO.setErrorCode(NO_PROJECT_PERMISSION.getCode());
        }
        if (StringUtils.isBlank(planId)) {
            return new OverViewCountDTO(new HashMap<>(), new ArrayList<>(), new ArrayList<>(), 0);
        }

        // 关联的用例数据
        List<TestPlanFunctionalCase> planFunctionalCases = extTestPlanFunctionalCaseMapper.selectByTestPlanIdAndNotDeleted(planId);
        List<TestPlanApiCase> planApiCases = extTestPlanApiCaseMapper.selectByTestPlanIdAndNotDeleted(planId);
        List<TestPlanApiScenario> planApiScenarios = extTestPlanApiScenarioMapper.selectByTestPlanIdAndNotDeleted(planId);
        TestPlanStatisticsResponse statisticsResponse = buildStatisticsResponse(planId, planFunctionalCases, planApiCases, planApiScenarios);
        // 计划-缺陷的关联数据
        List<TestPlanBugPageResponse> planBugs = extTestPlanBugMapper.selectPlanRelationBug(planId);
        //获取卡片数据
        buildCountMap(statisticsResponse, planBugs, overViewCountDTO);
        Map<String, List<TestPlanFunctionalCase>> caseUserMap = planFunctionalCases.stream().collect(Collectors.groupingBy(t -> StringUtils.isEmpty(t.getExecuteUser()) ? NONE : t.getExecuteUser()));
        Map<String, List<TestPlanApiCase>> apiCaseUserMap = planApiCases.stream().collect(Collectors.groupingBy(t -> StringUtils.isEmpty(t.getExecuteUser()) ? NONE : t.getExecuteUser()));
        Map<String, List<TestPlanApiScenario>> apiScenarioUserMap = planApiScenarios.stream().collect(Collectors.groupingBy(t -> StringUtils.isEmpty(t.getExecuteUser()) ? NONE : t.getExecuteUser()));
        Map<String, List<TestPlanBugPageResponse>> bugUserMap = planBugs.stream().collect(Collectors.groupingBy(TestPlanBugPageResponse::getCreateUser));
        int totalCount = planFunctionalCases.size() + planApiCases.size() + planApiScenarios.size() + planBugs.size();
        List<User> users = getUsers(caseUserMap, apiCaseUserMap, apiScenarioUserMap, totalCount);
        List<String> nameList = users.stream().map(User::getName).toList();
        overViewCountDTO.setXAxis(nameList);
        //获取柱状图数据
        List<NameArrayDTO> nameArrayDTOList = getNameArrayDTOS(projectId, users, caseUserMap, apiCaseUserMap, apiScenarioUserMap, bugUserMap);
        overViewCountDTO.setProjectCountList(nameArrayDTOList);
        return overViewCountDTO;

    }

    @NotNull
    private List<NameArrayDTO> getNameArrayDTOS(String projectId, List<User> users, Map<String, List<TestPlanFunctionalCase>> caseUserMap, Map<String, List<TestPlanApiCase>> apiCaseUserMap, Map<String, List<TestPlanApiScenario>> apiScenarioUserMap, Map<String, List<TestPlanBugPageResponse>> bugUserMap) {
        List<Integer> totalCaseCount = new ArrayList<>();
        List<Integer> finishCaseCount = new ArrayList<>();
        List<Integer> createBugCount = new ArrayList<>();
        List<Integer> closeBugCount = new ArrayList<>();

        String platformName = projectApplicationService.getPlatformName(projectId);
        List<String> statusList = getBugEndStatus(projectId, platformName);
        users.forEach(user -> {
            String userId = user.getId();
            int count = 0;
            int finishCount = 0;
            List<TestPlanFunctionalCase> testPlanFunctionalCases = caseUserMap.get(userId);
            List<String> currentUserCaseIds = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(testPlanFunctionalCases)) {
                List<String> functionalCaseIds = testPlanFunctionalCases.stream().map(TestPlanFunctionalCase::getFunctionalCaseId).toList();
                currentUserCaseIds.addAll(functionalCaseIds);
                count += testPlanFunctionalCases.size();
                List<TestPlanFunctionalCase> list = testPlanFunctionalCases.stream().filter(t -> !StringUtils.equalsIgnoreCase(t.getLastExecResult(), ExecStatus.PENDING.name())).toList();
                finishCount += list.size();
            }
            List<TestPlanApiCase> testPlanApiCases = apiCaseUserMap.get(userId);
            if (CollectionUtils.isNotEmpty(testPlanApiCases)) {
                List<String> apiCaseIds = testPlanApiCases.stream().map(TestPlanApiCase::getApiCaseId).toList();
                currentUserCaseIds.addAll(apiCaseIds);
                count += testPlanApiCases.size();
                List<TestPlanApiCase> list = testPlanApiCases.stream().filter(t -> !StringUtils.equalsIgnoreCase(t.getLastExecResult(), ExecStatus.PENDING.name())).toList();
                finishCount += list.size();
            }
            List<TestPlanApiScenario> testPlanApiScenarios = apiScenarioUserMap.get(userId);
            if (CollectionUtils.isNotEmpty(testPlanApiScenarios)) {
                List<String> apiScenarioIds = testPlanApiScenarios.stream().map(TestPlanApiScenario::getApiScenarioId).toList();
                currentUserCaseIds.addAll(apiScenarioIds);
                count += testPlanApiScenarios.size();
                List<TestPlanApiScenario> list = testPlanApiScenarios.stream().filter(t -> !StringUtils.equalsIgnoreCase(t.getLastExecResult(), ExecStatus.PENDING.name())).toList();
                finishCount += list.size();
            }
            List<TestPlanBugPageResponse> testPlanBugPageResponses = bugUserMap.get(userId);
            if (CollectionUtils.isNotEmpty(testPlanBugPageResponses)) {
                List<String> createBugIds = testPlanBugPageResponses.stream().map(TestPlanBugPageResponse::getId).toList();
                if (CollectionUtils.isNotEmpty(currentUserCaseIds)) {
                    List<TestPlanBugCaseDTO> bugRelatedCases = extTestPlanBugMapper.getBugRelatedCaseByCaseIds(createBugIds, currentUserCaseIds, testPlanBugPageResponses.getFirst().getTestPlanId());
                    createBugCount.add(bugRelatedCases.size());
                } else {
                    createBugCount.add(0);
                }
                if (CollectionUtils.isNotEmpty(statusList)) {
                    List<String> bugIds = testPlanBugPageResponses.stream().filter(t -> statusList.contains(t.getStatus())).map(TestPlanBugPageResponse::getId).toList();
                    if (CollectionUtils.isNotEmpty(bugIds) && CollectionUtils.isNotEmpty(currentUserCaseIds)) {
                        List<TestPlanBugCaseDTO> bugRelatedCases = extTestPlanBugMapper.getBugRelatedCaseByCaseIds(bugIds, currentUserCaseIds, testPlanBugPageResponses.getFirst().getTestPlanId());
                        closeBugCount.add(bugRelatedCases.size());
                    } else {
                        closeBugCount.add(0);
                    }
                } else {
                    closeBugCount.add(testPlanBugPageResponses.size());
                }

            } else {
                createBugCount.add(0);
                closeBugCount.add(0);
            }
            totalCaseCount.add(count);
            finishCaseCount.add(finishCount);
        });

        List<NameArrayDTO> nameArrayDTOList = new ArrayList<>();
        NameArrayDTO userCaseCountArray = new NameArrayDTO();
        userCaseCountArray.setCount(totalCaseCount);
        nameArrayDTOList.add(userCaseCountArray);

        NameArrayDTO userFinishCountArray = new NameArrayDTO();
        userFinishCountArray.setCount(finishCaseCount);
        nameArrayDTOList.add(userFinishCountArray);

        NameArrayDTO userCreateBugArray = new NameArrayDTO();
        userCreateBugArray.setCount(createBugCount);
        nameArrayDTOList.add(userCreateBugArray);

        NameArrayDTO userCloseBugArray = new NameArrayDTO();
        userCloseBugArray.setCount(closeBugCount);
        nameArrayDTOList.add(userCloseBugArray);
        return nameArrayDTOList;
    }

    private List<User> getUsers(Map<String, List<TestPlanFunctionalCase>> caseUserMap, Map<String, List<TestPlanApiCase>> apiCaseUserMap, Map<String, List<TestPlanApiScenario>> apiScenarioUserMap, int totalCount) {
        Set<String> caseUserIds = caseUserMap.keySet();
        boolean addDefaultUser = caseUserIds.contains(NONE);
        Set<String> userSet = new HashSet<>(caseUserIds);
        Set<String> apiCaseIds = apiCaseUserMap.keySet();
        userSet.addAll(apiCaseIds);
        if (apiCaseIds.contains(NONE)) {
            addDefaultUser = true;
        }
        Set<String> apiScenarioIds = apiScenarioUserMap.keySet();
        userSet.addAll(apiScenarioIds);
        if (apiScenarioIds.contains(NONE)) {
            addDefaultUser = true;
        }
        List<User> users = new ArrayList<>();
        if (CollectionUtils.isEmpty(userSet)) {
            if (totalCount > 0) {
                addDefaultUser(users);
            }
        } else {
            users = extUserMapper.selectSimpleUser(userSet);
            if (addDefaultUser) {
                addDefaultUser(users);
            }
        }
        return users;
    }

    private static void addDefaultUser(List<User> users) {
        User user = new User();
        user.setId(NONE);
        user.setName(Translator.get("plan_executor"));
        users.add(user);
    }

    private void buildCountMap(TestPlanStatisticsResponse planCount, List<TestPlanBugPageResponse> planBugs, OverViewCountDTO overViewCountDTO) {
        Map<String, Object> caseCountMap = new HashMap<>();
        caseCountMap.put(FUNCTIONAL, (int) planCount.getFunctionalCaseCount());
        caseCountMap.put(API_CASE, (int) planCount.getApiCaseCount());
        caseCountMap.put(API_SCENARIO, (int) planCount.getApiScenarioCount());
        Double passThreshold = planCount.getPassThreshold();
        int passThresholdValue = passThreshold == null ? 0 : passThreshold.intValue();
        caseCountMap.put("passThreshold", passThresholdValue);
        Double executeRate = planCount.getExecuteRate();
        caseCountMap.put("executeRate", executeRate);
        caseCountMap.put("totalCount", planCount.getCaseTotal());
        caseCountMap.put("executeCount", planCount.getCaseTotal() - planCount.getPendingCount());
        caseCountMap.put(BUG_COUNT, planBugs.size());
        List<TestPlan> testPlans = extTestPlanMapper.selectBaseInfoByIds(List.of(planCount.getId()));
        caseCountMap.put("testPlanName", testPlans.getFirst().getName());
        caseCountMap.put("status", planCount.getStatus());
        caseCountMap.put("passRate", planCount.getPassRate());
        overViewCountDTO.setCaseCountMap(caseCountMap);
    }

    public StatisticsDTO projectCaseCount(DashboardFrontPageRequest request, String userId) {
        String projectId = request.getProjectIds().getFirst();
        StatisticsDTO statisticsDTO = new StatisticsDTO();
        if (Boolean.FALSE.equals(permissionCheckService.checkModule(projectId, FUNCTIONAL_CASE_MODULE, userId, PermissionConstants.FUNCTIONAL_CASE_READ))) {
            statisticsDTO.setErrorCode(NO_PROJECT_PERMISSION.getCode());
            return statisticsDTO;
        }
        List<StatusPercentDTO> statusPercentList = new ArrayList<>();
        List<FunctionalCaseStatisticDTO> allStatisticListByProjectId = extFunctionalCaseMapper.getStatisticListByProjectId(projectId, null, null);
        buildStatusPercentList(allStatisticListByProjectId, statusPercentList);
        statisticsDTO.setStatusPercentList(statusPercentList);
        Map<String, List<FunctionalCaseStatisticDTO>> reviewStatusMap = allStatisticListByProjectId.stream().collect(Collectors.groupingBy(FunctionalCaseStatisticDTO::getReviewStatus));
        Map<String, List<NameCountDTO>> statusStatisticsMap = new HashMap<>();
        List<NameCountDTO> reviewList = getReviewList(reviewStatusMap, allStatisticListByProjectId);
        statusStatisticsMap.put("review", reviewList);
        List<NameCountDTO> passList = getPassList(reviewStatusMap, allStatisticListByProjectId);
        statusStatisticsMap.put("pass", passList);
        statisticsDTO.setStatusStatisticsMap(statusStatisticsMap);
        return statisticsDTO;
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
            BigDecimal divide = BigDecimal.valueOf(hasPassList.size()).divide(BigDecimal.valueOf(statisticListByProjectId.size()), 4, RoundingMode.HALF_UP);
            passRate.setCount(getTurnCount(divide));
        } else {
            passRate.setCount(getTurnCount(BigDecimal.ZERO));
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
        List<FunctionalCaseStatisticDTO> underReviewList = reviewStatusMap.get(FunctionalCaseReviewStatus.UNDER_REVIEWED.toString());
        List<FunctionalCaseStatisticDTO> reReviewedList = reviewStatusMap.get(FunctionalCaseReviewStatus.RE_REVIEWED.toString());
        if (CollectionUtils.isEmpty(unReviewList)) {
            unReviewList = new ArrayList<>();
        }
        if (CollectionUtils.isEmpty(underReviewList)) {
            underReviewList = new ArrayList<>();
        }
        if (CollectionUtils.isEmpty(reReviewedList)) {
            reReviewedList = new ArrayList<>();
        }
        NameCountDTO reviewRate = new NameCountDTO();
        reviewRate.setName(Translator.get("functional_case.reviewRate"));
        if (CollectionUtils.isEmpty(statisticListByProjectId)) {
            reviewRate.setCount(getTurnCount(BigDecimal.ZERO));
        } else {
            BigDecimal divide = BigDecimal.valueOf(statisticListByProjectId.size() - unReviewList.size() - underReviewList.size() - reReviewedList.size()).divide(BigDecimal.valueOf(statisticListByProjectId.size()), 4, RoundingMode.HALF_UP);
            reviewRate.setCount(getTurnCount(divide));
        }
        reviewList.add(reviewRate);
        NameCountDTO hasReview = new NameCountDTO();
        hasReview.setName(Translator.get("functional_case.hasReview"));
        hasReview.setCount(statisticListByProjectId.size() - unReviewList.size() - underReviewList.size() - reReviewedList.size());
        reviewList.add(hasReview);
        NameCountDTO unReview = new NameCountDTO();
        unReview.setName(Translator.get("functional_case.unReview"));
        unReview.setCount(unReviewList.size() + underReviewList.size() + reReviewedList.size());
        reviewList.add(unReview);
        return reviewList;
    }

    @NotNull
    private static BigDecimal getTurnCount(BigDecimal divide) {
        return divide.multiply(BigDecimal.valueOf(100)).setScale(2,RoundingMode.HALF_UP);
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
                BigDecimal divide = BigDecimal.valueOf(size).divide(BigDecimal.valueOf(statisticListByProjectId.size()), 4, RoundingMode.HALF_UP);
                statusPercentDTO.setPercentValue(getTurnCount(divide) + "%");
            } else {
                statusPercentDTO.setCount(0);
                statusPercentDTO.setPercentValue("0%");
            }
            statusPercentList.add(statusPercentDTO);
        }
    }

    public StatisticsDTO projectAssociateCaseCount(DashboardFrontPageRequest request, String userId) {
        String projectId = request.getProjectIds().getFirst();
        StatisticsDTO statisticsDTO = new StatisticsDTO();
        if (Boolean.FALSE.equals(permissionCheckService.checkModule(projectId, FUNCTIONAL_CASE_MODULE, userId, PermissionConstants.FUNCTIONAL_CASE_READ))) {
            statisticsDTO.setErrorCode(NO_PROJECT_PERMISSION.getCode());
            return statisticsDTO;
        }
        long caseTestCount = extFunctionalCaseMapper.caseTestCount(projectId, null, null);
        long simpleCaseCount = extFunctionalCaseMapper.simpleCaseCount(projectId, null, null);
        List<NameCountDTO> coverList = getCoverList((int) simpleCaseCount, Translator.get("functional_case.associateRate"), (int) caseTestCount, Translator.get("functional_case.hasAssociate"), (int) (simpleCaseCount - caseTestCount), Translator.get("functional_case.unAssociate"));
        Map<String, List<NameCountDTO>> statusStatisticsMap = new HashMap<>();
        statusStatisticsMap.put("cover", coverList);
        statisticsDTO.setStatusStatisticsMap(statusStatisticsMap);
        return statisticsDTO;
    }

    public OverViewCountDTO projectBugHandleUser(DashboardFrontPageRequest request, String userId) {
        String projectId = request.getProjectIds().getFirst();
        if (Boolean.FALSE.equals(permissionCheckService.checkModule(projectId, BUG_MODULE, userId, PermissionConstants.PROJECT_BUG_READ)))
            return new OverViewCountDTO(null, new ArrayList<>(), new ArrayList<>(), NO_PROJECT_PERMISSION.getCode());
        Long toStartTime = request.getToStartTime();
        Long toEndTime = request.getToEndTime();
        List<SelectOption> headerHandlerOption = getHandlerOption(request.getHandleUsers(), projectId);
        //获取每个人每个状态有多少数据(已按照用户id排序)
        if (CollectionUtils.isEmpty(request.getHandleUsers())) {
            return new OverViewCountDTO(new HashMap<>(), new ArrayList<>(), new ArrayList<>(), 0);
        }
        String platformName = projectApplicationService.getPlatformName(projectId);
        List<SelectOption> headerStatusOption = getStatusOption(projectId, platformName);
        Set<String> platforms = getPlatforms(platformName);
        List<String> handleUserIds = headerHandlerOption.stream().sorted(Comparator.comparing(SelectOption::getValue)).map(SelectOption::getValue).collect(Collectors.toList());
        List<ProjectUserStatusCountDTO> projectUserStatusCountDTOS = extBugMapper.projectUserBugStatusCount(projectId, toStartTime, toEndTime, handleUserIds, platforms);
        Map<String, SelectOption> statusMap = headerStatusOption.stream().collect(Collectors.toMap(SelectOption::getValue, t -> t));
        List<String> xaxis = new ArrayList<>(headerHandlerOption.stream().sorted(Comparator.comparing(SelectOption::getValue)).map(SelectOption::getText).toList());
        if (CollectionUtils.isEmpty(xaxis)) {
            xaxis.add(userId);
        }
        Map<String, List<Integer>> statusCountArrayMap = getStatusCountArrayMap(projectUserStatusCountDTOS, statusMap, handleUserIds);
        return getHandleUserCount(xaxis, statusMap, statusCountArrayMap);
    }

    @NotNull
    private List<SelectOption> getStatusOption(String projectId, String platformName) {
        List<SelectOption> headerStatusOption = new ArrayList<>();
        long localBugCount = extBugMapper.localBugCount(projectId);
        if (localBugCount >0 || StringUtils.equals(platformName, BugPlatform.LOCAL.getName())) {
            List<SelectOption> allLocalStatusOptions = bugStatusService.getAllLocalStatusOptions(projectId);
            rebuildStatusName(BugPlatform.LOCAL.getName(), allLocalStatusOptions);
            headerStatusOption.addAll(allLocalStatusOptions);
        }
        if (!StringUtils.equals(platformName, BugPlatform.LOCAL.getName())) {
            List<SelectOption> thirdStatusOptions = bugStatusService.getHeaderStatusOption(projectId);
            rebuildStatusName(platformName, thirdStatusOptions);
            headerStatusOption.addAll(new ArrayList<>(thirdStatusOptions));
        }
        headerStatusOption = headerStatusOption.stream().distinct().toList();
        return headerStatusOption;
    }

    private static void rebuildStatusName(String platformName, List<SelectOption> thirdStatusOptions) {
        for (SelectOption thirdStatusOption : thirdStatusOptions) {
            String newName = platformName + "_" + thirdStatusOption.getText();
            thirdStatusOption.setText(newName);
        }
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
            SelectOption selectOption = statusMap.get(status);
            if (selectOption != null) {
                nameArrayDTO.setName(selectOption.getText());
            } else {
                nameArrayDTO.setName(status);
            }
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
        String platformName = projectApplicationService.getPlatformName(projectId);
        List<SelectOption> localHandlerOption = bugCommonService.getLocalHandlerOption(projectId);
        List<SelectOption> headerHandlerOption = new ArrayList<>(localHandlerOption);
        if (CollectionUtils.isEmpty(handleUsers)) {
            if (!StringUtils.equals(platformName, BugPlatform.LOCAL.getName())) {
                List<SelectOption> thirdHandlerOption = bugCommonService.getHeaderHandlerOption(projectId);
                if (CollectionUtils.isNotEmpty(thirdHandlerOption)) {
                    headerHandlerOption.addAll(new ArrayList<>(thirdHandlerOption));
                }
            }
        } else {
            if (!StringUtils.equals(platformName, BugPlatform.LOCAL.getName())) {
                List<SelectOption> headerHandlerOptionList = bugCommonService.getHeaderHandlerOption(projectId);
                if (CollectionUtils.isNotEmpty(headerHandlerOptionList)) {
                    headerHandlerOption.addAll(new ArrayList<>(headerHandlerOptionList));
                }
            }
            headerHandlerOption = headerHandlerOption.stream().filter(t -> handleUsers.contains(t.getValue())).toList();
        }
        return headerHandlerOption.stream().distinct().toList();
    }

    /**
     * 根据项目id获取当前对接平台，与本地进行组装
     *
     * @return 本地与对接平台集合
     */
    private Set<String> getPlatforms(String platformName) {
        Set<String> platforms = new HashSet<>();
        platforms.add(BugPlatform.LOCAL.getName());
        if (!StringUtils.equalsIgnoreCase(platformName, BugPlatform.LOCAL.getName())) {
            platforms.add(platformName);
        }
        return platforms;
    }

    public StatisticsDTO projectReviewCaseCount(DashboardFrontPageRequest request, String userId) {
        String projectId = request.getProjectIds().getFirst();
        StatisticsDTO statisticsDTO = new StatisticsDTO();
        if (Boolean.FALSE.equals(permissionCheckService.checkModule(projectId, FUNCTIONAL_CASE_MODULE, userId, PermissionConstants.FUNCTIONAL_CASE_READ))) {
            statisticsDTO.setErrorCode(NO_PROJECT_PERMISSION.getCode());
            return statisticsDTO;
        }
        List<FunctionalCaseStatisticDTO> statisticListByProjectId = extFunctionalCaseMapper.getStatisticListByProjectId(projectId, null, null);
        List<String>unReviewStatusList = new ArrayList<>();
        unReviewStatusList.add(FunctionalCaseReviewStatus.UN_REVIEWED.toString());
        unReviewStatusList.add(FunctionalCaseReviewStatus.UNDER_REVIEWED.toString());
        unReviewStatusList.add(FunctionalCaseReviewStatus.RE_REVIEWED.toString());
        List<FunctionalCaseStatisticDTO> unReviewCaseList = statisticListByProjectId.stream().filter(t -> unReviewStatusList.contains(t.getReviewStatus())).toList();
        int reviewCount = statisticListByProjectId.size() - unReviewCaseList.size();
        List<NameCountDTO> coverList = getCoverList(statisticListByProjectId.size(), Translator.get("functional_case.reviewRate"), reviewCount, Translator.get("functional_case.hasReview"), unReviewCaseList.size(), Translator.get("functional_case.unReview"));
        Map<String, List<NameCountDTO>> statusStatisticsMap = new HashMap<>();
        statusStatisticsMap.put("cover", coverList);
        statisticsDTO.setStatusStatisticsMap(statusStatisticsMap);
        List<StatusPercentDTO> statusPercentList = getStatusPercentList(statisticListByProjectId);
        statisticsDTO.setStatusPercentList(statusPercentList);
        return statisticsDTO;
    }

    public StatisticsDTO projectApiCount(DashboardFrontPageRequest request, String userId) {
        String projectId = request.getProjectIds().getFirst();
        StatisticsDTO statisticsDTO = new StatisticsDTO();
        if (Boolean.FALSE.equals(permissionCheckService.checkModule(projectId, API_TEST_MODULE, userId, PermissionConstants.PROJECT_API_DEFINITION_READ))) {
            statisticsDTO.setErrorCode(NO_PROJECT_PERMISSION.getCode());
            return statisticsDTO;
        }
        List<ApiDefinition> createAllApiList = extApiDefinitionMapper.getCreateApiList(projectId, null, null);
        Map<String, List<ApiDefinition>> protocolMap = createAllApiList.stream().collect(Collectors.groupingBy(ApiDefinition::getProtocol));
        List<StatusPercentDTO> statusPercentList = new ArrayList<>();
        List<ProtocolDTO> protocols = apiTestService.getProtocols(request.getOrganizationId());
        int totalCount = CollectionUtils.isEmpty(createAllApiList) ? 0 : createAllApiList.size();
        for (ProtocolDTO protocol : protocols) {
            String protocolName = protocol.getProtocol();
            StatusPercentDTO statusPercentDTO = new StatusPercentDTO();
            statusPercentDTO.setStatus(protocolName);
            List<ApiDefinition> apiDefinitionList = protocolMap.get(protocolName);
            int size = CollectionUtils.isEmpty(apiDefinitionList) ? 0 : apiDefinitionList.size();
            if (totalCount == 0) {
                statusPercentDTO.setCount(0);
                statusPercentDTO.setPercentValue("0%");
            } else {
                statusPercentDTO.setCount(size);
                BigDecimal divide = BigDecimal.valueOf(size).divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP);
                statusPercentDTO.setPercentValue(getTurnCount(divide) + "%");
            }
            statusPercentList.add(statusPercentDTO);
        }

        Map<String, List<ApiDefinition>> statusMap = createAllApiList.stream().collect(Collectors.groupingBy(ApiDefinition::getStatus));
        List<ApiDefinition> doneList = statusMap.get(ApiDefinitionStatus.DONE.toString());
        List<ApiDefinition> processList = statusMap.get(ApiDefinitionStatus.PROCESSING.toString());
        List<ApiDefinition> deprecatedList = statusMap.get(ApiDefinitionStatus.DEPRECATED.toString());
        List<ApiDefinition> debugList = statusMap.get(ApiDefinitionStatus.DEBUGGING.toString());
        List<NameCountDTO> nameCountDTOS = new ArrayList<>();
        NameCountDTO doneDTO = new NameCountDTO();
        doneDTO.setName(Translator.get("api_definition.status.completed"));
        NameCountDTO completionRate = new NameCountDTO();
        completionRate.setName(Translator.get("api_definition.completionRate"));

        int doneSize = CollectionUtils.isEmpty(doneList) ? 0 : doneList.size();
        if (totalCount == 0) {
            completionRate.setCount(getTurnCount(BigDecimal.ZERO));
            doneDTO.setCount(0);
        } else {
            doneDTO.setCount(doneSize);
            BigDecimal divide = BigDecimal.valueOf(doneSize).divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP);
            completionRate.setCount(getTurnCount(divide));
        }
        NameCountDTO processDTO = getNameCountDTO(CollectionUtils.isEmpty(processList) ? 0 : processList.size(), Translator.get("api_definition.status.ongoing"));
        NameCountDTO deprecateDTO = getNameCountDTO(CollectionUtils.isEmpty(deprecatedList) ? 0 : deprecatedList.size(), Translator.get("api_definition.status.abandoned"));
        NameCountDTO debugDTO = getNameCountDTO(CollectionUtils.isEmpty(debugList) ? 0 : debugList.size(), Translator.get("api_definition.status.continuous"));
        nameCountDTOS.add(completionRate);
        nameCountDTOS.add(doneDTO);
        nameCountDTOS.add(processDTO);
        nameCountDTOS.add(debugDTO);
        nameCountDTOS.add(deprecateDTO);
        Map<String, List<NameCountDTO>> statusStatisticsMap = new HashMap<>();
        statusStatisticsMap.put("completionRate", nameCountDTOS);
        statisticsDTO.setStatusPercentList(statusPercentList);
        statisticsDTO.setStatusStatisticsMap(statusStatisticsMap);
        return statisticsDTO;
    }

    @NotNull
    private static NameCountDTO getNameCountDTO(int size, String name) {
        NameCountDTO processDTO = new NameCountDTO();
        processDTO.setName(name);
        processDTO.setCount(size);
        return processDTO;
    }

    @NotNull
    private List<StatusPercentDTO> getStatusPercentList(List<FunctionalCaseStatisticDTO> statisticListByProjectId) {
        List<StatusPercentDTO> statusPercentList = new ArrayList<>();
        List<OptionDTO> statusNameList = buildStatusNameMap();
        int totalCount = CollectionUtils.isEmpty(statisticListByProjectId) ? 0 : statisticListByProjectId.size();
        Map<String, List<FunctionalCaseStatisticDTO>> reviewStatusMap = statisticListByProjectId.stream().collect(Collectors.groupingBy(FunctionalCaseStatisticDTO::getReviewStatus));
        statusNameList.forEach(t -> {
            StatusPercentDTO statusPercentDTO = new StatusPercentDTO();
            List<FunctionalCaseStatisticDTO> functionalCaseStatisticDTOS = reviewStatusMap.get(t.getId());
            int count = CollectionUtils.isEmpty(functionalCaseStatisticDTOS) ? 0 : functionalCaseStatisticDTOS.size();
            statusPercentDTO.setStatus(t.getName());
            statusPercentDTO.setCount(count);
            if (totalCount > 0) {
                BigDecimal divide = BigDecimal.valueOf(count).divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP);
                statusPercentDTO.setPercentValue(getTurnCount(divide) + "%");
            } else {
                statusPercentDTO.setPercentValue("0%");
            }
            statusPercentList.add(statusPercentDTO);
        });
        return statusPercentList;
    }

    @NotNull
    private static List<NameCountDTO> getCoverList(int totalCount, String rateName, int coverCount, String coverName, int unCoverCount, String unCoverName) {
        List<NameCountDTO> coverList = new ArrayList<>();
        NameCountDTO coverRate = new NameCountDTO();
        if (totalCount > 0) {
            BigDecimal divide = BigDecimal.valueOf(coverCount).divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP);
            coverRate.setCount(getTurnCount(divide));
        }
        coverRate.setName(rateName);
        coverList.add(coverRate);
        NameCountDTO hasCover = new NameCountDTO();
        hasCover.setCount(coverCount);
        hasCover.setName(coverName);
        coverList.add(hasCover);
        NameCountDTO unCover = new NameCountDTO();
        unCover.setCount(unCoverCount);
        unCover.setName(unCoverName);
        coverList.add(unCover);
        return coverList;
    }

    private static List<OptionDTO> buildStatusNameMap() {
        List<OptionDTO> optionDTOList = new ArrayList<>();
        optionDTOList.add(new OptionDTO(FunctionalCaseReviewStatus.UN_REVIEWED.toString(), Translator.get("case.review.status.un_reviewed")));
        optionDTOList.add(new OptionDTO(FunctionalCaseReviewStatus.UNDER_REVIEWED.toString(), Translator.get("case.review.status.under_reviewed")));
        optionDTOList.add(new OptionDTO(FunctionalCaseReviewStatus.PASS.toString(), Translator.get("case.review.status.pass")));
        optionDTOList.add(new OptionDTO(FunctionalCaseReviewStatus.UN_PASS.toString(), Translator.get("case.review.status.un_pass")));
        optionDTOList.add(new OptionDTO(FunctionalCaseReviewStatus.RE_REVIEWED.toString(), Translator.get("case.review.status.re_reviewed")));
        return optionDTOList;
    }

    public Pager<List<CaseReviewDTO>> getFunctionalCasePage(DashboardFrontPageRequest request) {
        CaseReviewPageRequest reviewRequest = getCaseReviewPageRequest(request);
        Page<Object> page = PageHelper.startPage(reviewRequest.getCurrent(), reviewRequest.getPageSize(),
                StringUtils.isNotBlank(reviewRequest.getSortString()) ? reviewRequest.getSortString() : "pos desc");
        return PageUtils.setPageInfo(page, caseReviewService.getCaseReviewPage(reviewRequest));
    }


    @NotNull
    private static CaseReviewPageRequest getCaseReviewPageRequest(DashboardFrontPageRequest request) {
        String projectId = request.getProjectIds().getFirst();
        CaseReviewPageRequest reviewRequest = new CaseReviewPageRequest();
        reviewRequest.setProjectId(projectId);
        reviewRequest.setPageSize(request.getPageSize());
        reviewRequest.setCurrent(request.getCurrent());
        reviewRequest.setSort(request.getSort());
        CombineSearch combineSearch = getCombineSearch(request);
        reviewRequest.setCombineSearch(combineSearch);
        return reviewRequest;
    }

    @NotNull
    private static CombineSearch getCombineSearch(DashboardFrontPageRequest request) {
        CombineSearch combineSearch = new CombineSearch();
        combineSearch.setSearchMode(CombineSearch.SearchMode.AND.name());
        List<CombineCondition> conditions = new ArrayList<>();
        CombineCondition userCombineCondition = getCombineCondition(List.of(Objects.requireNonNull(SessionUtils.getUserId())), "reviewers", CombineCondition.CombineConditionOperator.IN.toString());
        conditions.add(userCombineCondition);
        CombineCondition statusCombineCondition = getCombineCondition(List.of(CaseReviewStatus.PREPARED.toString(), CaseReviewStatus.UNDERWAY.toString()), "status", CombineCondition.CombineConditionOperator.IN.toString());
        conditions.add(statusCombineCondition);
        CombineCondition createTimeCombineCondition = getCombineCondition(List.of(request.getToStartTime(), request.getToEndTime()), "createTime", CombineCondition.CombineConditionOperator.BETWEEN.toString());
        conditions.add(createTimeCombineCondition);
        combineSearch.setConditions(conditions);
        return combineSearch;
    }

    @NotNull
    private static CombineCondition getCombineCondition(List<Object> value, String reviewers, String operator) {
        CombineCondition userCombineCondition = new CombineCondition();
        userCombineCondition.setValue(value);
        userCombineCondition.setName(reviewers);
        userCombineCondition.setOperator(operator);
        userCombineCondition.setCustomField(false);
        userCombineCondition.setCustomFieldType("");
        return userCombineCondition;
    }

    public List<ApiDefinitionUpdateDTO> getApiUpdatePage(DashboardFrontPageRequest request) {
        String projectId = request.getProjectIds().getFirst();
        Long toStartTime = request.getToStartTime();
        Long toEndTime = request.getToEndTime();
        List<ApiDefinitionUpdateDTO> list = extApiDefinitionMapper.getUpdateApiList(projectId, toStartTime, toEndTime);
        processApiDefinitions(projectId, list);
        return list;
    }

    private void processApiDefinitions(String projectId, List<ApiDefinitionUpdateDTO> list) {
        List<String> apiDefinitionIds = list.stream().map(ApiDefinitionUpdateDTO::getId).toList();
        if (CollectionUtils.isEmpty(apiDefinitionIds)) {
            return;
        }
        List<ApiTestCase> apiCaseList = extApiDefinitionMapper.selectNotInTrashCaseIdsByApiIds(apiDefinitionIds);
        Map<String, List<ApiTestCase>> apiCaseMap = apiCaseList.stream().
                collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));

        List<ApiRefSourceCountDTO> apiRefSourceCountDTOS = extApiDefinitionMapper.scenarioRefApiCount(projectId, apiDefinitionIds);
        Map<String, Integer> countMap = apiRefSourceCountDTOS.stream().collect(Collectors.toMap(ApiRefSourceCountDTO::getSourceId, ApiRefSourceCountDTO::getCount));
        list.forEach(item -> {
            // Calculate API Case Metrics
            List<ApiTestCase> apiTestCases = apiCaseMap.get(item.getId());
            if (apiTestCases != null) {
                item.setCaseTotal(apiTestCases.size());
            } else {
                item.setCaseTotal(0);
            }
            Integer count = countMap.get(item.getId());
            item.setScenarioTotal(Objects.requireNonNullElse(count, 0));
        });
    }

    public List<SelectOption> getBugHandleUserList(String projectId) {
        return getHandlerOption(null, projectId);
    }

    public StatisticsDTO projectApiCaseCount(DashboardFrontPageRequest request, String userId) {
        String projectId = request.getProjectIds().getFirst();
        StatisticsDTO statisticsDTO = new StatisticsDTO();
        if (Boolean.FALSE.equals(permissionCheckService.checkModule(projectId, API_TEST_MODULE, userId, PermissionConstants.PROJECT_API_DEFINITION_CASE_READ))) {
            statisticsDTO.setErrorCode(NO_PROJECT_PERMISSION.getCode());
            return statisticsDTO;
        }
        Map<String, List<NameCountDTO>> statusStatisticsMap = new HashMap<>();
        long unDeleteCaseExecCount = extExecTaskItemMapper.getUnDeleteCaseExecCount(projectId, null, null, List.of("PLAN_RUN_API_CASE", "API_CASE"));
        List<ApiTestCase> simpleAllApiCaseList = extApiTestCaseMapper.getSimpleApiCaseList(projectId, null, null);

        int simpleAllApiCaseSize = 0;
        if (CollectionUtils.isNotEmpty(simpleAllApiCaseList)) {
            simpleAllApiCaseSize = simpleAllApiCaseList.size();
        }
        List<ApiTestCase> unExecList = simpleAllApiCaseList.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getLastReportStatus(), StringUtils.EMPTY)).toList();
        int unExecSize = CollectionUtils.isNotEmpty(unExecList) ? unExecList.size() : 0;

        List<ApiTestCase> successList = simpleAllApiCaseList.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getLastReportStatus(), ResultStatus.SUCCESS.name())).toList();
        int successSize = CollectionUtils.isNotEmpty(successList) ? successList.size() : 0;
        List<ApiTestCase> errorList = simpleAllApiCaseList.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getLastReportStatus(), ResultStatus.ERROR.name())).toList();
        int errorSize = CollectionUtils.isNotEmpty(errorList) ? errorList.size() : 0;

        List<ApiTestCase> fakeList = simpleAllApiCaseList.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getLastReportStatus(), ResultStatus.FAKE_ERROR.name())).toList();
        int fakeSize = CollectionUtils.isNotEmpty(fakeList) ? fakeList.size() : 0;

        List<NameCountDTO> execDTOS = getExecDTOS((int) unDeleteCaseExecCount);
        statusStatisticsMap.put("execCount", execDTOS);
        List<NameCountDTO> execRateDTOS = getExecRateDTOS(unExecSize, simpleAllApiCaseSize, Translator.get("api_management.apiCaseExecRate"));
        statusStatisticsMap.put("execRate", execRateDTOS);
        List<NameCountDTO> passRateDTOS = getPassRateDTOS(successSize, errorSize, simpleAllApiCaseSize, Translator.get("api_management.apiCasePassRate"));
        statusStatisticsMap.put("passRate", passRateDTOS);
        List<NameCountDTO> apiCaseDTOS = getApiCaseDTOS(fakeSize, simpleAllApiCaseSize, Translator.get("api_management.apiCaseCount"));
        statusStatisticsMap.put("apiCaseCount", apiCaseDTOS);
        statisticsDTO.setStatusStatisticsMap(statusStatisticsMap);
        return statisticsDTO;
    }

    @NotNull
    private List<NameCountDTO> getApiCaseDTOS(int fakeSize, int simpleApiCaseSize, String name) {
        List<NameCountDTO> apiCaseDTOS = new ArrayList<>();
        NameCountDTO apiCaseCountDTO = getNameCountDTO(simpleApiCaseSize, name);
        apiCaseDTOS.add(apiCaseCountDTO);
        NameCountDTO fakeCountDTO = getNameCountDTO(fakeSize, Translator.get("api_management.fakeErrorCount"));
        apiCaseDTOS.add(fakeCountDTO);
        return apiCaseDTOS;
    }

    @NotNull
    private static List<NameCountDTO> getPassRateDTOS(int successSize, int errorSize, int simpleAllApiCaseSize, String name) {
        List<NameCountDTO> passRateDTOS = new ArrayList<>();
        NameCountDTO passRateDTO = new NameCountDTO();
        passRateDTO.setName(name);
        if (simpleAllApiCaseSize == 0) {
            passRateDTO.setCount(getTurnCount(BigDecimal.ZERO));
        } else {
            BigDecimal divide = BigDecimal.valueOf(successSize).divide(BigDecimal.valueOf(simpleAllApiCaseSize), 4, RoundingMode.HALF_UP);
            passRateDTO.setCount(getTurnCount(divide));
        }
        passRateDTOS.add(passRateDTO);
        NameCountDTO unPassDTO = getNameCountDTO(errorSize, Translator.get("api_management.unPassCount"));
        passRateDTOS.add(unPassDTO);
        NameCountDTO passDTO = getNameCountDTO(successSize, Translator.get("api_management.passCount"));
        passRateDTOS.add(passDTO);
        return passRateDTOS;
    }

    @NotNull
    private static List<NameCountDTO> getExecRateDTOS(int unExecSize, int simpleAllApiCaseSize, String name) {
        List<NameCountDTO> execRateDTOS = new ArrayList<>();
        NameCountDTO execRateDTO = new NameCountDTO();
        execRateDTO.setName(name);
        if (simpleAllApiCaseSize == 0) {
            execRateDTO.setCount(getTurnCount(BigDecimal.ZERO));
        } else {
            BigDecimal divide = BigDecimal.valueOf(simpleAllApiCaseSize - unExecSize).divide(BigDecimal.valueOf(simpleAllApiCaseSize), 4, RoundingMode.HALF_UP);
            execRateDTO.setCount(getTurnCount(divide));
        }
        execRateDTOS.add(execRateDTO);
        NameCountDTO unExecDTO = getNameCountDTO(unExecSize, Translator.get("api_management.unExecCount"));
        execRateDTOS.add(unExecDTO);
        NameCountDTO execDTO = getNameCountDTO(simpleAllApiCaseSize - unExecSize, Translator.get("api_management.execCount"));
        execRateDTOS.add(execDTO);
        return execRateDTOS;
    }

    @NotNull
    private static List<NameCountDTO> getExecDTOS(int unDeleteCaseExecCount) {
        List<NameCountDTO> execDTOS = new ArrayList<>();
        NameCountDTO execCountDTO = new NameCountDTO();
        execCountDTO.setName(Translator.get("api_management.execTime"));
        execCountDTO.setCount(unDeleteCaseExecCount);
        execDTOS.add(execCountDTO);
        return execDTOS;
    }

    public StatisticsDTO projectApiScenarioCount(DashboardFrontPageRequest request, String userId) {
        String projectId = request.getProjectIds().getFirst();
        StatisticsDTO statisticsDTO = new StatisticsDTO();
        if (Boolean.FALSE.equals(permissionCheckService.checkModule(projectId, API_TEST_MODULE, userId, PermissionConstants.PROJECT_API_SCENARIO_READ))) {
            statisticsDTO.setErrorCode(NO_PROJECT_PERMISSION.getCode());
            return statisticsDTO;
        }

        Map<String, List<NameCountDTO>> statusStatisticsMap = new HashMap<>();
        long unDeleteCaseExecCount = extExecTaskItemMapper.getUnDeleteScenarioExecCount(projectId, null, null, List.of("PLAN_RUN_API_SCENARIO", "API_SCENARIO"));
        List<ApiScenario> simpleAllApiScenarioList = extApiScenarioMapper.getSimpleApiScenarioList(projectId, null, null);

        int simpleAllApiScenarioSize = 0;
        if (CollectionUtils.isNotEmpty(simpleAllApiScenarioList)) {
            simpleAllApiScenarioSize = simpleAllApiScenarioList.size();
        }
        List<String> lastReportStatuList = new ArrayList<>();
        lastReportStatuList.add(StringUtils.EMPTY);
        lastReportStatuList.add(ExecStatus.PENDING.toString());
        List<ApiScenario> unExecList = simpleAllApiScenarioList.stream().filter(t -> lastReportStatuList.contains(t.getLastReportStatus())).toList();
        int unExecSize = CollectionUtils.isNotEmpty(unExecList) ? unExecList.size() : 0;

        List<ApiScenario> successList = simpleAllApiScenarioList.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getLastReportStatus(), ResultStatus.SUCCESS.name())).toList();
        int successSize = CollectionUtils.isNotEmpty(successList) ? successList.size() : 0;
        List<ApiScenario> errorList = simpleAllApiScenarioList.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getLastReportStatus(), ResultStatus.ERROR.name())).toList();
        int errorSize = CollectionUtils.isNotEmpty(errorList) ? errorList.size() : 0;

        List<ApiScenario> fakeList = simpleAllApiScenarioList.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getLastReportStatus(), ResultStatus.FAKE_ERROR.name())).toList();
        int fakeSize = CollectionUtils.isNotEmpty(fakeList) ? fakeList.size() : 0;

        List<NameCountDTO> execDTOS = getExecDTOS((int) unDeleteCaseExecCount);
        statusStatisticsMap.put("execCount", execDTOS);
        List<NameCountDTO> execRateDTOS = getExecRateDTOS(unExecSize, simpleAllApiScenarioSize, Translator.get("api_management.scenarioExecRate"));
        statusStatisticsMap.put("execRate", execRateDTOS);
        List<NameCountDTO> passRateDTOS = getPassRateDTOS(successSize, errorSize, simpleAllApiScenarioSize, Translator.get("api_management.scenarioPassRate"));
        statusStatisticsMap.put("passRate", passRateDTOS);
        List<NameCountDTO> apiCaseDTOS = getApiCaseDTOS(fakeSize, simpleAllApiScenarioSize, Translator.get("api_management.apiScenarioCount"));
        statusStatisticsMap.put("apiScenarioCount", apiCaseDTOS);
        statisticsDTO.setStatusStatisticsMap(statusStatisticsMap);
        return statisticsDTO;
    }

    public StatisticsDTO baseProjectBugCount(DashboardFrontPageRequest request, String userId, String handleUserId, Boolean hasHandleUser, Boolean hasCreateUser) {
        String projectId = request.getProjectIds().getFirst();
        StatisticsDTO statisticsDTO = new StatisticsDTO();
        if (Boolean.FALSE.equals(permissionCheckService.checkModule(projectId, BUG_MODULE, userId, PermissionConstants.PROJECT_BUG_READ))) {
            statisticsDTO.setErrorCode(NO_PROJECT_PERMISSION.getCode());
            return statisticsDTO;
        }
        Set<String> handleUsers = new HashSet<>();
        String localHandleUser = hasHandleUser ? userId : null;
        if (StringUtils.isNotBlank(localHandleUser)) {
            handleUsers.add(localHandleUser);
        }
        String handleUser = hasHandleUser ? handleUserId : null;
        if (StringUtils.isNotBlank(handleUser)) {
            handleUsers.add(handleUser);
        }
        String createUser = hasCreateUser ? userId : null;
        String platformName = projectApplicationService.getPlatformName(projectId);
        Set<String> platforms = getPlatforms(platformName);
        List<Bug> allSimpleList;
        if (hasHandleUser) {
            allSimpleList = extBugMapper.getByHandleUser(projectId, null, null, localHandleUser, createUser, handleUser, platformName);
        } else {
            allSimpleList = extBugMapper.getSimpleList(projectId, null, null, handleUsers, createUser, platforms);
        }
        List<String> localLastStepStatus = getBugEndStatus(projectId, platformName);
        List<Bug> statusList = allSimpleList.stream().filter(t -> !localLastStepStatus.contains(t.getStatus())).toList();
        int statusSize = CollectionUtils.isEmpty(statusList) ? 0 : statusList.size();
        int totalSize = CollectionUtils.isEmpty(allSimpleList) ? 0 : allSimpleList.size();
        List<NameCountDTO> nameCountDTOS = buildBugRetentionRateList(totalSize, statusSize, false);
        Map<String, List<NameCountDTO>> statusStatisticsMap = new HashMap<>();
        statusStatisticsMap.put("retentionRate", nameCountDTOS);
        List<SelectOption> headerStatusOption = getHeaderStatusOption(projectId, platformName, new ArrayList<>());
        Map<String, List<Bug>> bugMap = allSimpleList.stream().collect(Collectors.groupingBy(Bug::getStatus));
        List<StatusPercentDTO> bugPercentList = bulidBugPercentList(headerStatusOption, bugMap, totalSize);
        statisticsDTO.setStatusStatisticsMap(statusStatisticsMap);
        statisticsDTO.setStatusPercentList(bugPercentList);
        return statisticsDTO;
    }

    @NotNull
    private List<String> getBugEndStatus(String projectId, String platformName) {
        List<String> localLastStepStatus = bugCommonService.getLocalLastStepStatus(projectId);
        if (StringUtils.equalsIgnoreCase(platformName, BugPlatform.LOCAL.getName())) {
            return localLastStepStatus;
        }
        // 项目对接三方平台
        List<String> platformLastStepStatus = new ArrayList<>();
        try {
            platformLastStepStatus = bugCommonService.getPlatformLastStepStatus(projectId);
        } catch (Exception e) {
            // 获取三方平台结束状态失败, 只过滤本地结束状态
            LogUtils.error(Translator.get("get_platform_end_status_error"));
            return localLastStepStatus;
        }
        localLastStepStatus.addAll(platformLastStepStatus);
        return localLastStepStatus;
    }


    private static List<NameCountDTO> buildBugRetentionRateList(int totalSize, int statusSize, boolean isPlan) {
        List<NameCountDTO> retentionRates = new ArrayList<>();
        NameCountDTO retentionRate = new NameCountDTO();
        retentionRate.setName(Translator.get("bug_management.retentionRate"));
        if (totalSize == 0) {
            retentionRate.setCount(getTurnCount(BigDecimal.ZERO));
        } else {
            BigDecimal divide = BigDecimal.valueOf(statusSize).divide(BigDecimal.valueOf(totalSize), 4, RoundingMode.HALF_UP);
            retentionRate.setCount(getTurnCount(divide));
        }
        retentionRates.add(retentionRate);
        if (isPlan) {
            NameCountDTO retentionDTO = getNameCountDTO(statusSize, Translator.get("bug_management.retentionCount"));
            retentionRates.add(retentionDTO);
            NameCountDTO total = getNameCountDTO(totalSize, Translator.get("bug_management.totalCount"));
            retentionRates.add(total);
        } else {
            NameCountDTO total = getNameCountDTO(totalSize, Translator.get("bug_management.totalCount"));
            retentionRates.add(total);
            NameCountDTO retentionDTO = getNameCountDTO(statusSize, Translator.get("bug_management.retentionCount"));
            retentionRates.add(retentionDTO);
        }

        return retentionRates;
    }

    private static List<StatusPercentDTO> bulidBugPercentList(List<SelectOption> headerStatusOption, Map<String, List<Bug>> bugMap, int simpleSize) {
        List<StatusPercentDTO> statusPercentList = new ArrayList<>();
        for (SelectOption selectOption : headerStatusOption) {
            StatusPercentDTO statusPercentDTO = new StatusPercentDTO();
            statusPercentDTO.setStatus(selectOption.getText());
            List<Bug> bugs = bugMap.get(selectOption.getValue());
            int bugSize = CollectionUtils.isEmpty(bugs) ? 0 : bugs.size();
            if (simpleSize == 0) {
                statusPercentDTO.setPercentValue("0%");
                statusPercentDTO.setCount(0);
            } else {
                BigDecimal divide = BigDecimal.valueOf(bugSize).divide(BigDecimal.valueOf(simpleSize), 4, RoundingMode.HALF_UP);
                statusPercentDTO.setPercentValue(getTurnCount(divide) + "%");
                statusPercentDTO.setCount(bugSize);
            }
            statusPercentList.add(statusPercentDTO);
        }
        return statusPercentList;
    }

    public StatisticsDTO projectBugCount(DashboardFrontPageRequest request, String userId, String handlerUser) {
        return baseProjectBugCount(request, userId, handlerUser, false, false);
    }

    public StatisticsDTO projectBugCountCreateByMe(DashboardFrontPageRequest request, String userId, String handlerUser) {
        return baseProjectBugCount(request, userId, handlerUser, false, true);
    }

    public StatisticsDTO projectBugCountHandleByMe(DashboardFrontPageRequest request, String userId, String handlerUser) {
        return baseProjectBugCount(request, userId, handlerUser, true, false);
    }

    public StatisticsDTO projectPlanLegacyBug(DashboardFrontPageRequest request, String userId) {
        String projectId = request.getProjectIds().getFirst();
        StatisticsDTO statisticsDTO = new StatisticsDTO();
        if (Boolean.FALSE.equals(permissionCheckService.checkModule(projectId, TEST_PLAN_MODULE, userId, PermissionConstants.TEST_PLAN_READ))) {
            statisticsDTO.setErrorCode(NO_PROJECT_PERMISSION.getCode());
            return statisticsDTO;
        }
        String platformName = projectApplicationService.getPlatformName(projectId);
        Set<String> platforms = getPlatforms(platformName);
        List<SelectOption> planBugList = extTestPlanMapper.getPlanBugList(projectId, TestPlanConstants.TEST_PLAN_TYPE_PLAN, new ArrayList<>(platforms), null);
        List<String> localLastStepStatus = getBugEndStatus(projectId, platformName);
        List<SelectOption> legacyBugList = planBugList.stream().filter(t -> !localLastStepStatus.contains(t.getText())).toList();
        List<SelectOption> headerStatusOption = getHeaderStatusOption(projectId, platformName, localLastStepStatus);
        int statusSize = CollectionUtils.isEmpty(legacyBugList) ? 0 : legacyBugList.size();
        int totalSize = CollectionUtils.isEmpty(planBugList) ? 0 : planBugList.size();
        List<NameCountDTO> nameCountDTOS = buildBugRetentionRateList(totalSize, statusSize, true);
        Map<String, List<NameCountDTO>> statusStatisticsMap = new HashMap<>();
        statusStatisticsMap.put("retentionRate", nameCountDTOS);
        Map<String, List<SelectOption>> bugMap = legacyBugList.stream().collect(Collectors.groupingBy(SelectOption::getText));
        List<StatusPercentDTO> statusPercentList = new ArrayList<>();
        for (SelectOption selectOption : headerStatusOption) {
            StatusPercentDTO statusPercentDTO = new StatusPercentDTO();
            statusPercentDTO.setStatus(selectOption.getText());
            List<SelectOption> bugs = bugMap.get(selectOption.getValue());
            int bugSize = CollectionUtils.isEmpty(bugs) ? 0 : bugs.size();
            if (statusSize == 0) {
                statusPercentDTO.setPercentValue("0%");
                statusPercentDTO.setCount(0);
            } else {
                BigDecimal divide = BigDecimal.valueOf(bugSize).divide(BigDecimal.valueOf(statusSize), 4, RoundingMode.HALF_UP);
                statusPercentDTO.setPercentValue(getTurnCount(divide) + "%");
                statusPercentDTO.setCount(bugSize);
            }
            statusPercentList.add(statusPercentDTO);
        }
        statisticsDTO.setStatusStatisticsMap(statusStatisticsMap);
        statisticsDTO.setStatusPercentList(statusPercentList);
        return statisticsDTO;
    }

    @NotNull
    private List<SelectOption> getHeaderStatusOption(String projectId, String platformName, List<String> endStatus) {
        List<SelectOption> headerStatusOption = new ArrayList<>();
        long localBugCount = extBugMapper.localBugCount(projectId);
        if (localBugCount >0 || StringUtils.equals(platformName, BugPlatform.LOCAL.getName())) {
            List<SelectOption> allLocalStatusOptions = bugStatusService.getAllLocalStatusOptions(projectId);
            rebuildStatusName(BugPlatform.LOCAL.getName(), allLocalStatusOptions);
            headerStatusOption.addAll(allLocalStatusOptions);
        }
        if (!StringUtils.equals(platformName, BugPlatform.LOCAL.getName())) {
            List<SelectOption> thirdStatusOptions = bugStatusService.getHeaderStatusOption(projectId);
            rebuildStatusName(platformName, thirdStatusOptions);
            if (CollectionUtils.isNotEmpty(thirdStatusOptions)) {
                headerStatusOption.addAll(thirdStatusOptions);
            }
        }
        headerStatusOption = headerStatusOption.stream().filter(t -> !endStatus.contains(t.getValue())).distinct().toList();
        return headerStatusOption;
    }

    public List<UserExtendDTO> getMemberOption(String projectId, String keyword) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null) {
            return new ArrayList<>();
        }
        return extSystemProjectMapper.getMemberByProjectId(projectId, keyword);
    }

    public List<CascadeChildrenDTO> getPlanOption(String projectId) {
        List<CascadeChildrenDTO> cascadeDTOList = new ArrayList<>();
        List<TestPlanAndGroupInfoDTO> groupAndPlanInfo = extTestPlanMapper.getGroupAndPlanInfo(projectId);
        TestPlanExample testPlanExample = new TestPlanExample();
        testPlanExample.createCriteria().andProjectIdEqualTo(projectId).andTypeEqualTo(TestPlanConstants.TEST_PLAN_TYPE_PLAN).andGroupIdEqualTo(NONE).andStatusEqualTo(TestPlanConstants.TEST_PLAN_STATUS_NOT_ARCHIVED);
        testPlanExample.setOrderByClause(" create_time DESC ");
        List<TestPlan> testPlans = testPlanMapper.selectByExample(testPlanExample);
        Map<String, List<TestPlanAndGroupInfoDTO>> groupMap = groupAndPlanInfo.stream().sorted(Comparator.comparing(TestPlanAndGroupInfoDTO::getGroupCreateTime).reversed()).collect(Collectors.groupingBy(TestPlanAndGroupInfoDTO::getGroupId));
        groupMap.forEach((t, list) -> {
            CascadeChildrenDTO father = new CascadeChildrenDTO();
            father.setValue(t);
            father.setLabel(list.getFirst().getGroupName());
            father.setCreateTime(list.getFirst().getCreateTime());
            List<CascadeDTO> children = new ArrayList<>();
            List<TestPlanAndGroupInfoDTO> sortList = list.stream().sorted(Comparator.comparing(TestPlanAndGroupInfoDTO::getCreateTime).reversed()).toList();
            for (TestPlanAndGroupInfoDTO testPlanAndGroupInfoDTO : sortList) {
                CascadeDTO cascadeChildrenDTO = new CascadeDTO();
                cascadeChildrenDTO.setValue(testPlanAndGroupInfoDTO.getId());
                cascadeChildrenDTO.setLabel(testPlanAndGroupInfoDTO.getName());
                children.add(cascadeChildrenDTO);
            }
            father.setChildren(children);
            cascadeDTOList.add(father);
        });
        for (TestPlan testPlan : testPlans) {
            CascadeChildrenDTO father = new CascadeChildrenDTO();
            father.setValue(testPlan.getId());
            father.setLabel(testPlan.getName());
            father.setCreateTime(testPlan.getCreateTime());
            cascadeDTOList.add(father);
        }
        return cascadeDTOList.stream().sorted(Comparator.comparing(CascadeChildrenDTO::getCreateTime).reversed()).toList();

    }
}


