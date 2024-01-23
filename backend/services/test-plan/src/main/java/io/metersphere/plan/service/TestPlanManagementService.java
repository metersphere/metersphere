package io.metersphere.plan.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.plan.constants.TestPlanResourceConfig;
import io.metersphere.plan.dto.TestPlanQueryConditions;
import io.metersphere.plan.dto.request.TestPlanTableRequest;
import io.metersphere.plan.dto.response.TestPlanResponse;
import io.metersphere.plan.mapper.ExtTestPlanMapper;
import io.metersphere.plan.mapper.ExtTestPlanModuleMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.service.CommonProjectService;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

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
    private CommonProjectService commonProjectService;
    @Resource
    private TestPlanModuleService testPlanModuleService;

    public Map<String, Long> moduleCount(TestPlanTableRequest request) {
        //查出每个模块节点下的资源数量。 不需要按照模块进行筛选
        TestPlanQueryConditions testPlanQueryConditions = new TestPlanQueryConditions(null, request.getProjectId(), request);
        List<ModuleCountDTO> moduleCountDTOList = extTestPlanMapper.countModuleIdByConditions(testPlanQueryConditions);
        Map<String, Long> moduleCountMap = testPlanModuleService.getModuleCountMap(request.getProjectId(), moduleCountDTOList);
        return moduleCountMap;
    }

    public Pager<List<TestPlanResponse>> page(TestPlanTableRequest request) {
        TestPlanQueryConditions queryConditions = this.generateTestPlanConditions(request);
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "t.update_time desc");
        return PageUtils.setPageInfo(page, this.getTableList(queryConditions));
    }

    /**
     * 生成查询条件
     *
     * @param request 前端传来的筛选条件
     * @return
     */
    private TestPlanQueryConditions generateTestPlanConditions(TestPlanTableRequest request) {
        TestPlanQueryConditions conditions = new TestPlanQueryConditions(request.getModuleIds(), request.getProjectId(), request);
        if (!request.conditionIsEmpty()) {
            //查询符合匹配的子节点时不需要传入groupId
            conditions.setGroupId(null);
            List<String> includeGroupIds = extTestPlanMapper.selectGroupIdByConditions(conditions);
            conditions.setIncludeIds(includeGroupIds);
        }
        return conditions;
    }

    private List<TestPlanResponse> getTableList(TestPlanQueryConditions request) {
        List<TestPlanResponse> testPlanResponses = extTestPlanMapper.selectByConditions(request);
        testPlanResponses.forEach(item -> {
            if (StringUtils.equals(item.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
                TestPlanQueryConditions childrenCondition = new TestPlanQueryConditions();
                childrenCondition.setProjectId(request.getProjectId());
                childrenCondition.setGroupId(item.getId());
                item.setChildren(extTestPlanMapper.selectByConditions(childrenCondition));
            }
            this.initTestPlanResponse(item);
        });
        return testPlanResponses;
    }

    private void initTestPlanResponse(TestPlanResponse testPlanResponse) {
        testPlanResponse.setModuleName(testPlanModuleService.getNameById(testPlanResponse.getModuleId()));
        //todo 定时任务相关信息处理

        if (CollectionUtils.isNotEmpty(testPlanResponse.getChildren())) {
            testPlanResponse.getChildren().forEach(this::initTestPlanResponse);
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
}
