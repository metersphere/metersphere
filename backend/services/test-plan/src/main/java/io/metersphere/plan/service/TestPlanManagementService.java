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
import org.apache.commons.collections4.CollectionUtils;
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
        this.initDefaultFilter(request);
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                MapUtils.isEmpty(request.getSort()) ? "t.pos desc, t.id desc" : request.getSortString("id", "t"));
        return PageUtils.setPageInfo(page, this.list(request));
    }

    @Autowired
    private ApplicationContext applicationContext;
    private void initDefaultFilter(TestPlanTableRequest request) {

        List<String> defaultStatusList = new ArrayList<>();
        defaultStatusList.add(TestPlanConstants.TEST_PLAN_STATUS_NOT_ARCHIVED);
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
            if (statusList.size() < 3) {
                List<String> innerIdList = new ArrayList<>();
                // 条件过滤
                Map<String, TestPlanResourceService> beansOfType = applicationContext.getBeansOfType(TestPlanResourceService.class);
                // 将当前项目下未归档的测试计划结果查询出来，进行下列符合条件的筛选
                List<TestPlanResourceExecResultDTO> execResults = new ArrayList<>();
                beansOfType.forEach((k, v) -> execResults.addAll(v.selectDistinctExecResult(request.getProjectId())));
                Map<String, Map<String, List<String>>> testPlanExecMap = testPlanBaseUtilsService.parseExecResult(execResults);
                Map<String, Long> groupCountMap = extTestPlanMapper.countByGroupPlan(request.getProjectId())
                        .stream().collect(Collectors.toMap(TestPlanGroupCountDTO::getGroupId, TestPlanGroupCountDTO::getCount));

                List<String> completedTestPlanIds = new ArrayList<>();
                List<String> preparedTestPlanIds = new ArrayList<>();
                List<String> underwayTestPlanIds = new ArrayList<>();
                testPlanExecMap.forEach((groupId, planMap) -> {
                    if (StringUtils.equalsIgnoreCase(groupId, TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
                        planMap.forEach((planId, resultList) -> {
                            String result = testPlanBaseUtilsService.calculateTestPlanStatus(resultList);
                            if (StringUtils.equals(result, TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED)) {
                                completedTestPlanIds.add(planId);
                            } else if (StringUtils.equals(result, TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY)) {
                                underwayTestPlanIds.add(planId);
                            } else if (StringUtils.equals(result, TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED)) {
                                preparedTestPlanIds.add(planId);
                            }
                        });
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
                    innerIdList.addAll(extTestPlanMapper.selectIdByProjectIdAndWithoutList(request.getProjectId(), withoutList));
                    withoutList = null;
                }
                request.setInnerIds(innerIdList);
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
        List<TestPlanResponse> testPlanResponses = extTestPlanMapper.selectByConditions(request);
        handChildren(testPlanResponses,request.getProjectId());
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
}
