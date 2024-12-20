package io.metersphere.plan.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.plan.constants.TestPlanResourceConfig;
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanConfig;
import io.metersphere.plan.domain.TestPlanExample;
import io.metersphere.plan.dto.TestPlanCalculationDTO;
import io.metersphere.plan.dto.TestPlanResourceExecResultDTO;
import io.metersphere.plan.dto.request.TestPlanTableRequest;
import io.metersphere.plan.dto.response.TestPlanResponse;
import io.metersphere.plan.dto.response.TestPlanStatisticsResponse;
import io.metersphere.plan.mapper.ExtTestPlanFunctionalCaseMapper;
import io.metersphere.plan.mapper.ExtTestPlanMapper;
import io.metersphere.plan.mapper.ExtTestPlanModuleMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.plan.utils.TestPlanUtils;
import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.ResultStatus;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.CalculateUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.SubListUtils;
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
            setTodoParam(request);
        }
        this.initDefaultFilter(request);
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                MapUtils.isEmpty(request.getSort()) ? "t.pos desc, t.id desc" : request.getSortString("id", "t"));
        return PageUtils.setPageInfo(page, this.list(request, request.getIncludeItemTestPlanIds()));
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

    /**
     * 根据项目ID和状态查询包含的测试计划ID
     *
     * @param projectId           项目ID
     * @param dataType            页面视图 ( 全部（查找符合条件的测试计划或测试子计划）  计划（查找符合条件的测试子计划）  计划组（查找符合条件的测试计划组））
     * @param statusConditionList 状态列表
     * @return 测试计划ID列表
     */
    public TestPlanCalculationDTO selectTestPlanIdByProjectIdUnionConditions(boolean selectArchived, String projectId, String dataType, List<String> statusConditionList, List<String> passedConditionList) {
        if (CollectionUtils.isEmpty(statusConditionList) && CollectionUtils.isEmpty(passedConditionList)) {
            return new TestPlanCalculationDTO();
        }
        boolean selectPassed = CollectionUtils.isNotEmpty(passedConditionList) && CollectionUtils.size(passedConditionList) == 1;
        List<TestPlan> testPlanList = extTestPlanMapper.selectIdAndGroupIdByProjectId(projectId, selectArchived);
        Map<String, List<String>> testPlanGroupIdMap = new HashMap<>();
        List<String> noGroupPlanIdList = new ArrayList<>();
        Map<String, List<String>> noGroupPlanIdMap = new HashMap<>();
        if (StringUtils.equalsIgnoreCase(dataType, TestPlanConstants.TEST_PLAN_TYPE_PLAN)) {
            //只查游离态的测试计划
            for (TestPlan item : testPlanList) {
                if (StringUtils.equalsIgnoreCase(item.getType(), TestPlanConstants.TEST_PLAN_TYPE_PLAN) && StringUtils.equals(item.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
                    noGroupPlanIdList.add(item.getId());
                }
            }
        } else if (StringUtils.equalsIgnoreCase(dataType, TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            //不查游离态的测试计划
            testPlanList = testPlanList.stream().filter(item ->
                    StringUtils.equalsIgnoreCase(item.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP) || !StringUtils.equals(item.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)
            ).toList();
            testPlanGroupIdMap = TestPlanUtils.parseGroupIdMap(testPlanList);
        } else {
            // 全部查询
            testPlanGroupIdMap = TestPlanUtils.parseGroupIdMap(testPlanList);
            noGroupPlanIdList = testPlanGroupIdMap.get(TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID);
            testPlanGroupIdMap.remove(TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID);
        }
        if (CollectionUtils.isNotEmpty(noGroupPlanIdList)) {
            noGroupPlanIdMap.put(TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID, noGroupPlanIdList);
        }
        testPlanList = null;

        Map<String, TestPlanResourceService> beansOfType = applicationContext.getBeansOfType(TestPlanResourceService.class);

        TestPlanCalculationDTO calculationDTO = this.calculationTestPlanByConditions(noGroupPlanIdMap, beansOfType, selectPassed, dataType);

        SubListUtils.dealForSubMap(testPlanGroupIdMap, 50, groupIdMap -> {
            TestPlanCalculationDTO dto = this.calculationTestPlanByConditions(groupIdMap, beansOfType, selectPassed, dataType);
            calculationDTO.merge(dto);
        });

        calculationDTO.setStatusConditionList(statusConditionList);
        calculationDTO.setPassedConditionList(passedConditionList);

        return calculationDTO;
    }

    private TestPlanCalculationDTO calculationTestPlanByConditions(Map<String, List<String>> groupIdMap, Map<String, TestPlanResourceService> beansOfType, boolean selectPassed, String dataType) {
        TestPlanCalculationDTO returnDTO = new TestPlanCalculationDTO();
        List<String> selectTestPlanIds = new ArrayList<>();
        groupIdMap.forEach((k, v) -> selectTestPlanIds.addAll(v));
        if (CollectionUtils.isEmpty(selectTestPlanIds)) {
            return returnDTO;
        }
        // 将当前项目下未归档的测试计划结果查询出来，进行下列符合条件的筛选
        List<TestPlanResourceExecResultDTO> execResults = new ArrayList<>();
        Map<String, Map<String, List<String>>> testPlanExecMap = null;

        if (selectPassed) {
            beansOfType.forEach((k, v) -> execResults.addAll(v.selectLastExecResultByTestPlanIds(selectTestPlanIds)));
            testPlanExecMap = testPlanBaseUtilsService.parseExecResult(execResults);
            Map<String, TestPlanConfig> testPlanConfigMap = extTestPlanMapper.selectTestPlanConfigByTestPlanIds(selectTestPlanIds)
                    .stream().collect(Collectors.toMap(TestPlanConfig::getTestPlanId, item -> item));
            for (Map.Entry<String, List<String>> entry : groupIdMap.entrySet()) {
                String groupId = entry.getKey();
                List<String> testPlanIdList = entry.getValue();
                Map<String, List<String>> testPlanExecResult = testPlanExecMap.containsKey(groupId) ? testPlanExecMap.get(groupId) : new HashMap<>();
                boolean isRootTestPlan = StringUtils.equalsIgnoreCase(groupId, TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID);
                boolean groupHasUnPassItem = false;

                for (String testPlanId : testPlanIdList) {
                    TestPlanConfig config = testPlanConfigMap.get(testPlanId);
                    double passThreshold = (config == null || config.getPassThreshold() == null) ? 100 : config.getPassThreshold();

                    List<String> executeResultList = testPlanExecResult.containsKey(testPlanId) ? testPlanExecResult.get(testPlanId) : new ArrayList<>();

                    double executeRage = CalculateUtils.percentage(
                            executeResultList.stream().filter(result -> StringUtils.equalsIgnoreCase(result, ResultStatus.SUCCESS.name())).toList().size(),
                            executeResultList.size());

                    if (executeRage < passThreshold) {
                        groupHasUnPassItem = true;
                    }

                    if (StringUtils.equalsIgnoreCase(dataType, TestPlanConstants.TEST_PLAN_TYPE_PLAN) && isRootTestPlan) {
                        if (executeRage >= passThreshold) {
                            returnDTO.addPassedTestPlanId(testPlanId);
                        } else {
                            returnDTO.addNotPassedTestPlanId(testPlanId);
                        }
                    } else if (StringUtils.equalsAnyIgnoreCase(dataType, "ALL")) {
                        if (executeRage >= passThreshold) {
                            if (isRootTestPlan) {
                                returnDTO.addPassedTestPlanId(testPlanId);
                            } else {
                                returnDTO.addPassedTestPlanId(groupId);
                                returnDTO.addPassedItemTestPlanId(testPlanId);
                            }

                        } else {
                            if (isRootTestPlan) {
                                returnDTO.addNotPassedTestPlanId(testPlanId);
                            } else {
                                returnDTO.addNotPassedTestPlanId(groupId);
                                returnDTO.addNotPassedItemTestPlanId(testPlanId);
                            }
                        }
                    }
                }

                if (!StringUtils.equalsIgnoreCase(dataType, TestPlanConstants.TEST_PLAN_TYPE_PLAN) && !isRootTestPlan) {
                    if (groupHasUnPassItem || CollectionUtils.isEmpty(testPlanIdList)) {
                        returnDTO.addNotPassedTestPlanId(groupId);
                    } else {
                        returnDTO.addPassedTestPlanId(groupId);
                    }
                }
            }
        } else {
            beansOfType.forEach((k, v) -> execResults.addAll(v.selectDistinctExecResultByTestPlanIds(selectTestPlanIds)));
            testPlanExecMap = testPlanBaseUtilsService.parseExecResult(execResults);
        }

        for (Map.Entry<String, List<String>> entry : groupIdMap.entrySet()) {
            String groupId = entry.getKey();
            List<String> testPlanIdList = entry.getValue();
            Map<String, List<String>> testPlanExecResult = testPlanExecMap.containsKey(groupId) ? testPlanExecMap.get(groupId) : new HashMap<>();
            boolean isRootTestPlan = StringUtils.equalsIgnoreCase(groupId, TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID);

            List<String> testPlanStatus = new ArrayList<>();
            for (String testPlanId : testPlanIdList) {
                List<String> executeResultList = testPlanExecResult.containsKey(testPlanId) ? testPlanExecResult.get(testPlanId) : new ArrayList<>();
                String result = testPlanBaseUtilsService.calculateTestPlanStatus(executeResultList);
                testPlanStatus.add(result);
                if (StringUtils.equalsIgnoreCase(dataType, TestPlanConstants.TEST_PLAN_TYPE_PLAN) && isRootTestPlan) {
                    if (StringUtils.equals(result, TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED)) {
                        returnDTO.addCompletedTestPlanId(testPlanId);
                    } else if (StringUtils.equals(result, TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY)) {
                        returnDTO.addUnderwayTestPlanId(testPlanId);
                    } else if (StringUtils.equals(result, TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED)) {
                        returnDTO.addPreparedTestPlanId(testPlanId);
                    }
                } else if (StringUtils.equalsAnyIgnoreCase(dataType, "ALL")) {
                    if (isRootTestPlan) {
                        if (StringUtils.equals(result, TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED)) {
                            returnDTO.addCompletedTestPlanId(testPlanId);
                        } else if (StringUtils.equals(result, TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY)) {
                            returnDTO.addUnderwayTestPlanId(testPlanId);
                        } else if (StringUtils.equals(result, TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED)) {
                            returnDTO.addPreparedTestPlanId(testPlanId);
                        }
                    } else {
                        if (StringUtils.equals(result, TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED)) {
                            returnDTO.addCompletedTestPlanId(groupId);
                            returnDTO.addCompletedItemTestPlanId(testPlanId);
                        } else if (StringUtils.equals(result, TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY)) {
                            returnDTO.addUnderwayTestPlanId(groupId);
                            returnDTO.addUnderwayItemTestPlanId(testPlanId);
                        } else if (StringUtils.equals(result, TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED)) {
                            returnDTO.addPreparedTestPlanId(groupId);
                            returnDTO.addPreparedItemTestPlanId(testPlanId);

                        }
                    }
                }
            }

            if (!StringUtils.equalsIgnoreCase(dataType, TestPlanConstants.TEST_PLAN_TYPE_PLAN) && !isRootTestPlan) {
                String groupStatus = testPlanBaseUtilsService.calculateStatusByChildren(testPlanStatus);
                if (StringUtils.equals(groupStatus, TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED)) {
                    returnDTO.addCompletedTestPlanId(groupId);
                } else if (StringUtils.equals(groupStatus, TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY)) {
                    returnDTO.addUnderwayTestPlanId(groupId);
                } else if (StringUtils.equals(groupStatus, TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED)) {
                    returnDTO.addPreparedTestPlanId(groupId);
                }

            }

        }
        return returnDTO;
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
                        TestPlanCalculationDTO calculationDTO = this.selectTestPlanIdByProjectIdUnionConditions(false, request.getProjectId(), request.getType(), statusList, null);
                        request.setCombineInnerIds(calculationDTO.getConditionInnerId());
                        request.setIncludeItemTestPlanIds(calculationDTO.getConditionItemPlanId());
                        request.setCombineOperator(item.getOperator());

                        if (CollectionUtils.isEmpty(request.getCombineInnerIds())) {
                            // 如果查询条件内未查出包含ID，意味着查不出任何一条数据。
                            request.setCombineInnerIds(Collections.singletonList("COMBINE_SEARCH_NONE"));
                        }
                    }
                }
            });
        });
        if (!StringUtils.equalsIgnoreCase(request.getViewId(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
            List<String> statusSelectParam = null;
            List<String> passedSelectParam = null;

            if (request.getFilter() != null && request.getFilter().containsKey("passed")) {
                passedSelectParam = request.getFilter().get("passed");
            } else if (request.getFilter() != null && request.getFilter().containsKey("archivedPassed")) {
                passedSelectParam = request.getFilter().get("archivedPassed");

                statusSelectParam = new ArrayList<>() {{
                    this.add(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED);
                }};
                request.getFilter().put("status", statusSelectParam);
            }

            if (request.getFilter() == null || !request.getFilter().containsKey("status")) {
                if (request.getFilter() == null) {
                    request.setFilter(new HashMap<>() {{
                        this.put("status", defaultStatusList);
                    }});
                } else {
                    request.getFilter().put("status", defaultStatusList);
                }
            } else if (!request.getFilter().get("status").contains(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
                statusSelectParam = request.getFilter().get("status");
                request.getFilter().put("status", defaultStatusList);
            }

            boolean selectArchived = CollectionUtils.isNotEmpty(statusSelectParam) && statusSelectParam.contains(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED);
            boolean selectStatus = !selectArchived && CollectionUtils.isNotEmpty(statusSelectParam) && CollectionUtils.size(statusSelectParam) < 3;
            boolean selectPassed = CollectionUtils.isNotEmpty(passedSelectParam) && CollectionUtils.size(passedSelectParam) == 1;

            if (selectStatus || selectPassed) {
                TestPlanCalculationDTO calculationDTO = this.selectTestPlanIdByProjectIdUnionConditions(selectArchived, request.getProjectId(), request.getType(),
                        selectStatus ? statusSelectParam : null,
                        selectPassed ? passedSelectParam : null);
                request.setInnerIds(calculationDTO.getConditionInnerId());
                request.setIncludeItemTestPlanIds(calculationDTO.getConditionItemPlanId());

                if (CollectionUtils.isEmpty(request.getInnerIds()) && !selectArchived) {
                    // 如果查询条件内未查出包含ID，意味着查不出任何一条数据。
                    request.setCombineInnerIds(Collections.singletonList("FILTER_SEARCH_NONE"));
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

    public List<TestPlanResponse> list(TestPlanTableRequest request, List<String> includeChildTestPlanId) {
        List<TestPlanResponse> testPlanResponses = new ArrayList<>();
        if (StringUtils.equalsIgnoreCase(request.getViewId(), "my_follow")) {
            testPlanResponses = extTestPlanMapper.selectMyFollowByConditions(request);
        } else {
            testPlanResponses = extTestPlanMapper.selectByConditions(request);
        }

        handChildren(testPlanResponses, includeChildTestPlanId);
        return testPlanResponses;
    }

    public List<TestPlan> groupList(String projectId) {
        TestPlanExample example = new TestPlanExample();
        example.createCriteria().andTypeEqualTo(TestPlanConstants.TEST_PLAN_TYPE_GROUP).andProjectIdEqualTo(projectId).andStatusNotEqualTo(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED);
        example.setOrderByClause("pos desc, id desc");
        return testPlanMapper.selectByExample(example);
    }

    public List<TestPlan> testPlanList(String projectId) {
        TestPlanExample example = new TestPlanExample();
        example.createCriteria().andTypeEqualTo(TestPlanConstants.TEST_PLAN_TYPE_PLAN).andProjectIdEqualTo(projectId).andStatusNotEqualTo(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED);
        example.setOrderByClause("pos desc, id desc");
        return testPlanMapper.selectByExample(example);
    }

    /**
     * 计划组子节点
     */
    private void handChildren(List<TestPlanResponse> testPlanResponses, List<String> includeItemTestPlanId) {
        List<String> groupIds = testPlanResponses.stream().filter(item -> StringUtils.equals(item.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)).map(TestPlanResponse::getId).toList();
        if (CollectionUtils.isNotEmpty(groupIds)) {
            List<TestPlanResponse> childrenList = extTestPlanMapper.selectByGroupIds(groupIds);
            Map<String, List<TestPlanResponse>> collect = childrenList.stream().collect(Collectors.groupingBy(TestPlanResponse::getGroupId));
            testPlanResponses.forEach(item -> {
                if (collect.containsKey(item.getId())) {
                    //存在子节点
                    List<TestPlanResponse> list = collect.get(item.getId());
                    if (CollectionUtils.isNotEmpty(includeItemTestPlanId)) {
                        list = list.stream().filter(child -> includeItemTestPlanId.contains(child.getId())).toList();
                    }
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
     * 1. 获取已完成且阈值达标的计划ID集合 (作为排除条件)
     * 2. 子计划ID满足条件的作为额外ID查询
     *
     * @param request 请求参数
     */
    private void setTodoParam(TestPlanTableRequest request) {
        List<String> doneIds = new ArrayList<>();
        // 筛选出已完成/进行中的计划或计划组
        List<String> statusList = new ArrayList<>();
        statusList.add(TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED);
        statusList.add(TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY);
        TestPlanCalculationDTO calculationDTO = this.selectTestPlanIdByProjectIdUnionConditions(false, request.getProjectId(), "ALL", statusList, null);
        List<String> completePlanOrGroupIds = calculationDTO.getCompletedTestPlanIds();
        List<String> underwayPlanOrGroupIds = calculationDTO.getUnderwayTestPlanIds();

        if (CollectionUtils.isNotEmpty(completePlanOrGroupIds) || CollectionUtils.isNotEmpty(underwayPlanOrGroupIds)) {
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
            if (CollectionUtils.isNotEmpty(calculateIds)) {
                List<TestPlanStatisticsResponse> calcPlans = testPlanStatisticsService.calculateRate(calculateIds);
                calcPlans.forEach(plan -> {
                    // 筛选出已完成 && 且通过率达到阈值的子计划
                    if (plan.getPassRate() >= plan.getPassThreshold() && StringUtils.equals(plan.getStatus(), TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED)) {
                        doneIds.add(plan.getId());
                    }
                });
            }
            request.setDoneExcludeIds(doneIds);
        }
    }
}
