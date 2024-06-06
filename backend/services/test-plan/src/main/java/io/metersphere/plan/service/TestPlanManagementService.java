package io.metersphere.plan.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.plan.constants.TestPlanResourceConfig;
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanExample;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private TestPlanMapper testPlanMapper;

    public Map<String, Long> moduleCount(TestPlanTableRequest request) {
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
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                MapUtils.isEmpty(request.getSort()) ? "t.num desc" : request.getSortString());
        return PageUtils.setPageInfo(page, this.list(request));
    }

    public List<TestPlanResponse> list(TestPlanTableRequest request) {
        List<TestPlanResponse> testPlanResponses = extTestPlanMapper.selectByConditions(request);
        handChildren(testPlanResponses,request.getProjectId());
        return testPlanResponses;
    }

    public List<TestPlan> groupList(String projectId) {
        TestPlanExample example = new TestPlanExample();
        example.createCriteria().andTypeEqualTo(TestPlanConstants.TEST_PLAN_TYPE_GROUP).andProjectIdEqualTo(projectId).andStatusNotEqualTo(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED);
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
