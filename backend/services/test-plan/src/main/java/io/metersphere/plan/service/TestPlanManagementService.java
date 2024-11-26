package io.metersphere.plan.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.plan.constants.TestPlanResourceConfig;
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanExample;
import io.metersphere.plan.dto.TestPlanGroupCountDTO;
import io.metersphere.plan.dto.TestPlanResourceExecResultDTO;
import io.metersphere.plan.dto.request.TestPlanTableRequest;
import io.metersphere.plan.dto.response.TestPlanResponse;
import io.metersphere.plan.dto.response.TestPlanStatisticsResponse;
import io.metersphere.plan.mapper.ExtTestPlanFunctionalCaseMapper;
import io.metersphere.plan.mapper.ExtTestPlanMapper;
import io.metersphere.plan.mapper.ExtTestPlanModuleMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanManagementService {
    @Resource
    private ExtTestPlanMapper extTestPlanMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ExtTestPlanModuleMapper extTestPlanModuleMapper;
    @Resource
    private ExtTestPlanFunctionalCaseMapper extTestPlanFunctionalCaseMapper;
    @Resource
    private TestPlanModuleService testPlanModuleService;
    @Resource
    private TestPlanStatisticsService testPlanStatisticsService;
    @Resource
    private TestPlanBaseUtilsService testPlanBaseUtilsService;
    @Resource
    private TestPlanMapper testPlanMapper;

    public Map<String, Long> moduleCount(TestPlanTableRequest request) {
        this.initDefaultFilter(request);
        //查出每个模块节点下的资源数量。 不需要按照模块进行筛选
        request.setModuleIds(null);
        List<ModuleCountDTO> moduleCountDTOList = extTestPlanMapper.countModuleIdByConditions(request);
        Map<String, Long> moduleCountMap = testPlanModuleService.getModuleCountMap(request.getProjectId(), moduleCountDTOList);
        long allCount = 0;
        for (ModuleCountDTO item : moduleCountDTOList) {
            allCount += item.getDataCount();
        }
        moduleCountMap.put("all", allCount);
        return moduleCountMap;
    }

    /**
     * 测试计划列表查询
     */
    public Pager<List<TestPlanResponse>> page(TestPlanTableRequest request) {
        if (request.isMyTodo()) {
            request.setDoneExcludeIds(this.getDoneIds(request.getProjectId()));
        }
        this.initDefaultFilter(request);
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                MapUtils.isEmpty(request.getSort()) ? "t.pos desc, t.id desc" : request.getSortString("id", "t"));
        return PageUtils.setPageInfo(page, this.list(request));
    }

    public void filterTestPlanIdWithStatus(Map<String, List<String>> testPlanExecMap, List<String> completedTestPlanIds, List<String> preparedTestPlanIds, List<String> underwayTestPlanIds) {
        testPlanExecMap.forEach((planId, resultList) -> {
            String result = testPlanBaseUtilsService.calculateTestPlanStatus(resultList);
            if (StringUtils.equals(result, TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED)) {
                completedTestPlanIds.add(planId);
            } else if (StringUtils.equals(result, TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY)) {
                underwayTestPlanIds.add(planId);
            } else if (StringUtils.equals(result, TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED)) {
                preparedTestPlanIds.add(planId);
            }
        });
    }

    public List<String> selectTestPlanIdByFuncCaseIdAndStatus(String functionalCaseId, @NotEmpty List<String> statusList) {
        List<String> returnIdList = new ArrayList<>();

        List<String> testPlanIdList = extTestPlanFunctionalCaseMapper.selectTestPlanIdByFunctionCaseId(functionalCaseId);
        if (CollectionUtils.isEmpty(testPlanIdList)) {
            return new ArrayList<>();
        }
        List<String> completedTestPlanIds = new ArrayList<>();
        List<String> preparedTestPlanIds = new ArrayList<>();
        List<String> underwayTestPlanIds = new ArrayList<>();

        Map<String, TestPlanResourceService> beansOfType = applicationContext.getBeansOfType(TestPlanResourceService.class);
        // 将当前项目下未归档的测试计划结果查询出来，进行下列符合条件的筛选
        List<TestPlanResourceExecResultDTO> execResults = new ArrayList<>();
        beansOfType.forEach((k, v) -> execResults.addAll(v.selectDistinctExecResultByTestPlanIds(testPlanIdList)));
        Map<String, List<String>> testPlanExecMap = execResults.stream().collect(
                Collectors.groupingBy(TestPlanResourceExecResultDTO::getTestPlanId, Collectors.mapping(TestPlanResourceExecResultDTO::getExecResult, Collectors.toList())));
        this.filterTestPlanIdWithStatus(testPlanExecMap, completedTestPlanIds, preparedTestPlanIds, underwayTestPlanIds);

        if (statusList.contains(TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED)) {
            // 已完成
            returnIdList.addAll(completedTestPlanIds);
        }
        if (statusList.contains(TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY)) {
            // 进行中
            returnIdList.addAll(underwayTestPlanIds);
        }
        if (statusList.contains(TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED)) {
            // 未开始
            returnIdList.addAll(preparedTestPlanIds);
        }
        return returnIdList;
    }

    private List<String> selectTestPlanIdByProjectIdAndStatus(String projectId, @NotEmpty List<String> statusList) {
        List<String> innerIdList = new ArrayList<>();
        Map<String, TestPlanResourceService> beansOfType = applicationContext.getBeansOfType(TestPlanResourceService.class);
        // 将当前项目下未归档的测试计划结果查询出来，进行下列符合条件的筛选
        List<TestPlanResourceExecResultDTO> execResults = new ArrayList<>();
        beansOfType.forEach((k, v) -> execResults.addAll(v.selectDistinctExecResultByProjectId(projectId)));
        Map<String, Map<String, List<String>>> testPlanExecMap = testPlanBaseUtilsService.parseExecResult(execResults);
        Map<String, Long> groupCountMap = extTestPlanMapper.countByGroupPlan(projectId)
                .stream().collect(Collectors.toMap(TestPlanGroupCountDTO::getGroupId, TestPlanGroupCountDTO::getCount));

        List<String> completedTestPlanIds = new ArrayList<>();
        List<String> preparedTestPlanIds = new ArrayList<>();
        List<String> underwayTestPlanIds = new ArrayList<>();
        testPlanExecMap.forEach((groupId, planMap) -> {
            if (StringUtils.equalsIgnoreCase(groupId, TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
                this.filterTestPlanIdWithStatus(planMap, completedTestPlanIds, preparedTestPlanIds, underwayTestPlanIds);
            } else {
                long itemPlanCount = groupCountMap.getOrDefault(groupId, 0L);
                List<String> itemStatusList = new ArrayList<>();
                if (itemPlanCount > planMap.size()) {
                    // 存在未执行或者没有用例的测试计划。 此时这种测试计划的状态为未开始
                    itemStatusList.add(TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED);
                }
                planMap.forEach((planId, resultList) -> {
                    itemStatusList.add(testPlanBaseUtilsService.calculateTestPlanStatus(resultList));
                });
                String groupStatus = testPlanBaseUtilsService.calculateStatusByChildren(itemStatusList);
                if (StringUtils.equals(groupStatus, TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED)) {
                    completedTestPlanIds.add(groupId);
                } else if (StringUtils.equals(groupStatus, TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY)) {
                    underwayTestPlanIds.add(groupId);
                } else if (StringUtils.equals(groupStatus, TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED)) {
                    preparedTestPlanIds.add(groupId);
                }
            }
        });

        testPlanExecMap = null;
        if (statusList.contains(TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED)) {
            // 已完成
            innerIdList.addAll(completedTestPlanIds);
        }

        if (statusList.contains(TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY)) {
            // 进行中
            innerIdList.addAll(underwayTestPlanIds);
        }
        if (statusList.contains(TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED)) {
            // 未开始   有一些测试计划/计划组没有用例 / 测试计划， 在上面的计算中无法过滤。所以用排除法机型处理
            List<String> withoutList = new ArrayList<>();
            withoutList.addAll(completedTestPlanIds);
            withoutList.addAll(underwayTestPlanIds);
            innerIdList.addAll(extTestPlanMapper.selectIdByProjectIdAndWithoutList(projectId, withoutList));
        }
        return innerIdList;
    }

    @Autowired
    private ApplicationContext applicationContext;

    private void initDefaultFilter(TestPlanTableRequest request) {

        List<String> defaultStatusList = new ArrayList<>();
        defaultStatusList.add(TestPlanConstants.TEST_PLAN_STATUS_NOT_ARCHIVED);
        Optional.ofNullable(request.getCombineSearch()).ifPresent(combineSearch -> {
            combineSearch.getConditions().forEach(item -> {
                if (StringUtils.equalsIgnoreCase(item.getName(), "status")) {
                    List<String> statusList = (List<String>) item.getValue();
                    item.setValue(defaultStatusList);
                    //目前未归档的测试计划只有3中类型。所以这里判断如果是3个的话等于直接查询未归档
                    if (statusList.size() < 3) {
                        request.setCombineInnerIds(this.selectTestPlanIdByProjectIdAndStatus(request.getProjectId(), statusList));
                        request.setCombineOperator(item.getOperator());
                    }
                }
            });
        });
        if (!StringUtils.equalsIgnoreCase(request.getViewId(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
            if (request.getFilter() == null || !request.getFilter().containsKey("status")) {
                if (request.getFilter() == null) {
                    request.setFilter(new HashMap<>() {{
                        this.put("status", defaultStatusList);
                    }});
                } else {
                    request.getFilter().put("status", defaultStatusList);
                }
            } else if (!request.getFilter().get("status").contains(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
                List<String> statusList = request.getFilter().get("status");
                request.getFilter().put("status", defaultStatusList);
                //目前未归档的测试计划只有3中类型。所以这里判断如果是3个的话等于直接查询未归档
                if (statusList.size() < 3) {
                    request.setInnerIds(this.selectTestPlanIdByProjectIdAndStatus(request.getProjectId(), statusList));
                }
            }
        }

        if (StringUtils.isNotBlank(request.getKeyword())) {
            List<String> groupIdList = extTestPlanMapper.selectGroupIdByKeyword(request.getProjectId(), request.getKeyword());
            if (CollectionUtils.isNotEmpty(groupIdList)) {
                request.setKeywordFilterIds(groupIdList);
            }

        }
    }

    public List<TestPlanResponse> list(TestPlanTableRequest request) {
        List<TestPlanResponse> testPlanResponses = new ArrayList<>();
        if (StringUtils.equalsIgnoreCase(request.getViewId(), "my_follow")) {
            testPlanResponses = extTestPlanMapper.selectMyFollowByConditions(request);
        } else {
            testPlanResponses = extTestPlanMapper.selectByConditions(request);
        }
        handChildren(testPlanResponses, request.getProjectId());
        return testPlanResponses;
    }

    public List<TestPlan> groupList(String projectId) {
        TestPlanExample example = new TestPlanExample();
        example.createCriteria().andTypeEqualTo(TestPlanConstants.TEST_PLAN_TYPE_GROUP).andProjectIdEqualTo(projectId).andStatusNotEqualTo(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED);
        example.setOrderByClause("pos desc, id desc");
        return testPlanMapper.selectByExample(example);
    }

    /**
     * 计划组子节点
     */
    private void handChildren(List<TestPlanResponse> testPlanResponses, String projectId) {
        List<String> groupIds = testPlanResponses.stream().filter(item -> StringUtils.equals(item.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)).map(TestPlanResponse::getId).toList();
        if (CollectionUtils.isNotEmpty(groupIds)) {
            List<TestPlanResponse> childrenList = extTestPlanMapper.selectByGroupIds(groupIds);
            Map<String, List<TestPlanResponse>> collect = childrenList.stream().collect(Collectors.groupingBy(TestPlanResponse::getGroupId));
            testPlanResponses.forEach(item -> {
                if (collect.containsKey(item.getId())) {
                    //存在子节点
                    List<TestPlanResponse> list = collect.get(item.getId());
                    item.setChildren(list);
                    item.setChildrenCount(list.size());
                }
            });
        }
    }

    public void checkModuleIsOpen(String resourceId, String resourceType, List<String> moduleMenus) {
        Project project;

        if (StringUtils.equals(resourceType, TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN)) {
            project = projectMapper.selectByPrimaryKey(extTestPlanMapper.selectProjectIdByTestPlanId(resourceId));
        } else if (StringUtils.equals(resourceType, TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN_MODULE)) {
            project = projectMapper.selectByPrimaryKey(extTestPlanModuleMapper.selectProjectIdByModuleId(resourceId));
        } else if (StringUtils.equals(resourceType, TestPlanResourceConfig.CHECK_TYPE_PROJECT)) {
            project = projectMapper.selectByPrimaryKey(resourceId);
        } else {
            throw new MSException(Translator.get("project.module_menu.check.error"));
        }

        if (project == null || StringUtils.isEmpty(project.getModuleSetting())) {
            throw new MSException(Translator.get("project.module_menu.check.error"));
        }
        List<String> projectModuleMenus = JSON.parseArray(project.getModuleSetting(), String.class);
        if (!projectModuleMenus.containsAll(moduleMenus)) {
            throw new MSException(Translator.get("project.module_menu.check.error"));
        }
    }

    public List<TestPlanResponse> selectByGroupId(String groupId) {
        return extTestPlanMapper.selectByGroupIds(Collections.singletonList(groupId));
    }


    /**
     * 根据项目id检查模块是否开启
     *
     * @param projectId
     * @return
     */
    public boolean checkModuleIsOpenByProjectId(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null || StringUtils.isEmpty(project.getModuleSetting())) {
            return false;
        }
        List<String> projectModuleMenus = JSON.parseArray(project.getModuleSetting(), String.class);
        if (projectModuleMenus.contains(TestPlanResourceConfig.CONFIG_TEST_PLAN)) {
            return true;
        }
        return false;
    }

    /**
     * 获取已完成且阈值达标的计划ID集合 (作为排除条件)
     *
     * @param projectId 项目ID
     * @return 已办计划ID集合
     */
    private List<String> getDoneIds(String projectId) {
        List<String> doneIds = new ArrayList<>();
        // 筛选出已完成/进行中的计划或计划组
        List<String> completePlanOrGroupIds = selectTestPlanIdByProjectIdAndStatus(projectId, List.of(TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED));
        List<String> underwayPlanOrGroupIds = selectTestPlanIdByProjectIdAndStatus(projectId, List.of(TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY));
        if (CollectionUtils.isEmpty(completePlanOrGroupIds) && CollectionUtils.isEmpty(underwayPlanOrGroupIds)) {
            return null;
        }

        TestPlanExample example = new TestPlanExample();
        example.createCriteria().andIdIn(ListUtils.union(completePlanOrGroupIds, underwayPlanOrGroupIds));
        List<TestPlan> allPlans = testPlanMapper.selectByExample(example);
        // 筛选出已完成且阈值达标的计划ID集合, 计划组除外
        List<String> calculateIds = new ArrayList<>();
        List<String> groupPlanIds = new ArrayList<>();
        allPlans.forEach(plan -> {
            if (completePlanOrGroupIds.contains(plan.getId())) {
                if (StringUtils.equals(plan.getType(), TestPlanConstants.TEST_PLAN_TYPE_PLAN)) {
                    // 已完成的计划, 待计算通过率比对
                    calculateIds.add(plan.getId());
                } else {
                    // 已完成的计划组, 待获取下级子计划
                    groupPlanIds.add(plan.getId());
                }
            } else {
                // 进行中的状态, 直接获取计划组下的子计划
                if (StringUtils.equals(plan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
                    groupPlanIds.add(plan.getId());
                }
            }
        });

        // 处理计划组下级子计划
        List<TestPlan> childPlans = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(groupPlanIds)) {
            example.clear();
            example.createCriteria().andGroupIdIn(groupPlanIds);
            childPlans = testPlanMapper.selectByExample(example);
        }
        calculateIds.addAll(childPlans.stream().map(TestPlan::getId).toList());
        if (CollectionUtils.isEmpty(calculateIds)) {
            return null;
        }
        List<TestPlanStatisticsResponse> calcPlans = testPlanStatisticsService.calculateRate(calculateIds);
        calcPlans.forEach(plan -> {
            // 筛选出已完成的计划 && 子计划且通过率达到阈值
            if (plan.getPassRate() >= plan.getPassThreshold() && StringUtils.equals(plan.getStatus(), TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED)) {
                doneIds.add(plan.getId());
            }
        });
        return doneIds;
    }
}
