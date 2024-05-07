package io.metersphere.plan.service;

import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.TestPlanResourceAssociationParam;
import io.metersphere.plan.dto.request.TestPlanBatchProcessRequest;
import io.metersphere.plan.dto.request.TestPlanCreateRequest;
import io.metersphere.plan.dto.request.TestPlanUpdateRequest;
import io.metersphere.plan.dto.response.TestPlanCountResponse;
import io.metersphere.plan.mapper.ExtTestPlanMapper;
import io.metersphere.plan.mapper.TestPlanConfigMapper;
import io.metersphere.plan.mapper.TestPlanFollowerMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.plan.utils.TestPlanXPackFactory;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.TestPlanModuleExample;
import io.metersphere.system.mapper.TestPlanModuleMapper;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.BatchProcessUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanService {
    private static final long MAX_TEST_PLAN_SIZE = 999;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private ExtTestPlanMapper extTestPlanMapper;
    @Resource
    private TestPlanConfigMapper testPlanConfigMapper;
    @Resource
    private TestPlanLogService testPlanLogService;
    @Resource
    private TestPlanModuleMapper testPlanModuleMapper;
    @Resource
    private TestPlanFollowerMapper testPlanFollowerMapper;
    @Resource
    private TestPlanXPackFactory testPlanXPackFactory;

    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    private TestPlanApiScenarioService testPlanApiScenarioService;
    @Resource
    private TestPlanFunctionalCaseService testPlanFunctionCaseService;

    public void checkModule(String moduleId) {
        if (!StringUtils.equals(moduleId, ModuleConstants.DEFAULT_NODE_ID)) {
            TestPlanModuleExample example = new TestPlanModuleExample();
            example.createCriteria().andIdEqualTo(moduleId);
            if (testPlanModuleMapper.countByExample(example) == 0) {
                throw new MSException(Translator.get("module.not.exist"));
            }
        }
    }


    /**
     * 创建测试计划
     *
     * @param testPlanCreateRequest
     * @param operator
     * @param requestUrl
     * @param requestMethod
     * @return
     */
    public String add(TestPlanCreateRequest testPlanCreateRequest, String operator, String requestUrl, String requestMethod) {
        //检查模块的合法性
        this.checkModule(testPlanCreateRequest.getModuleId());

        TestPlan createTestPlan = new TestPlan();
        BeanUtils.copyBean(createTestPlan, testPlanCreateRequest);
        this.validateTestPlan(createTestPlan);

        createTestPlan.setId(IDGenerator.nextStr());
        long operateTime = System.currentTimeMillis();
        createTestPlan.setNum(NumGenerator.nextNum(testPlanCreateRequest.getProjectId(), ApplicationNumScope.TEST_PLAN));
        createTestPlan.setCreateUser(operator);
        createTestPlan.setUpdateUser(operator);
        createTestPlan.setCreateTime(operateTime);
        createTestPlan.setUpdateTime(operateTime);
        createTestPlan.setStatus(TestPlanConstants.TEST_PLAN_STATUS_PREPARED);

        TestPlanConfig testPlanConfig = new TestPlanConfig();
        testPlanConfig.setTestPlanId(createTestPlan.getId());
        testPlanConfig.setAutomaticStatusUpdate(testPlanCreateRequest.isAutomaticStatusUpdate());
        testPlanConfig.setRepeatCase(testPlanCreateRequest.isRepeatCase());
        testPlanConfig.setPassThreshold(testPlanCreateRequest.getPassThreshold());
        testPlanConfig.setTestPlanning(testPlanCreateRequest.isTestPlanning());

        if (testPlanCreateRequest.isGroupOption()) {
            testPlanXPackFactory.getTestPlanGroupService().validateGroup(createTestPlan, testPlanConfig);
        }

        handleAssociateCase(testPlanCreateRequest, createTestPlan);

        testPlanMapper.insert(createTestPlan);
        testPlanConfigMapper.insert(testPlanConfig);
        testPlanLogService.saveAddLog(createTestPlan, operator, requestUrl, requestMethod);
        return createTestPlan.getId();
    }

    /**
     * 处理关联的用例
     *
     * @param request
     * @return
     */
    private void handleAssociateCase(TestPlanCreateRequest request, TestPlan testPlan) {
        //关联的功能用例
        handleFunctionalCase(request.getBaseAssociateCaseRequest().getFunctionalSelectIds(), testPlan);
        //TODO 关联接口用例/接口场景用例

    }

    /**
     * 关联的功能用例
     *
     * @param functionalSelectIds
     */
    private void handleFunctionalCase(List<String> functionalSelectIds, TestPlan testPlan) {
        if (CollectionUtils.isNotEmpty(functionalSelectIds)) {
            TestPlanResourceAssociationParam associationParam = new TestPlanResourceAssociationParam(functionalSelectIds, testPlan.getProjectId(), testPlan.getId(), testPlan.getNum(), testPlan.getCreateUser());
            testPlanFunctionCaseService.saveTestPlanResource(associationParam);
        }
    }

    private void validateTestPlan(TestPlan testPlan) {
        TestPlanExample example = new TestPlanExample();
        if (StringUtils.isBlank(testPlan.getId())) {
            TestPlanExample.Criteria criteria = example.createCriteria();
            //测试计划第一层的数据还不能超过1000个
            if (StringUtils.equals(testPlan.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
                criteria.andGroupIdEqualTo(TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID);
                if (testPlanMapper.countByExample(example) >= MAX_TEST_PLAN_SIZE) {
                    throw new MSException(Translator.getWithArgs("test_plan.too_many", MAX_TEST_PLAN_SIZE));
                }
            }
            example.clear();
            example.createCriteria().andNameEqualTo(testPlan.getName()).andProjectIdEqualTo(testPlan.getProjectId());
            if (testPlanMapper.countByExample(example) > 0) {
                throw new MSException(Translator.get("test_plan.name.exist") + ":" + testPlan.getName());
            }
        } else {
            example.createCriteria().andNameEqualTo(testPlan.getName()).andProjectIdEqualTo(testPlan.getProjectId()).andIdNotEqualTo(testPlan.getId());
            if (testPlanMapper.countByExample(example) > 0) {
                throw new MSException(Translator.get("test_plan.name.exist") + ":" + testPlan.getName());
            }
        }
    }


    /**
     * 删除测试计划
     *
     * @param id
     * @param operator
     * @param requestUrl
     * @param requestMethod
     */
    public void delete(String id, String operator, String requestUrl, String requestMethod) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(id);
        if (StringUtils.equals(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            this.deleteGroupByList(Collections.singletonList(testPlan.getId()));
        } else {
            testPlanMapper.deleteByPrimaryKey(id);
            //级联删除
            this.cascadeDeleteTestPlanIds(Collections.singletonList(id));
        }
        //记录日志
        testPlanLogService.saveDeleteLog(testPlan, operator, requestUrl, requestMethod);
    }

    private void deleteByList(List<String> testPlanIds) {
        if (CollectionUtils.isNotEmpty(testPlanIds)) {
            BatchProcessUtils.consumerByString(testPlanIds, (deleteIds) -> {
                TestPlanExample testPlanExample = new TestPlanExample();
                testPlanExample.createCriteria().andIdIn(deleteIds);
                testPlanMapper.deleteByExample(testPlanExample);
                //级联删除
                this.cascadeDeleteTestPlanIds(deleteIds);
            });
        }
    }

    private void deleteGroupByList(List<String> testPlanGroupIds) {
        if (CollectionUtils.isNotEmpty(testPlanGroupIds)) {
            BatchProcessUtils.consumerByString(testPlanGroupIds, (deleteGroupIds) -> {
                TestPlanExample testPlanExample = new TestPlanExample();
                testPlanExample.createCriteria().andIdIn(deleteGroupIds);
                testPlanMapper.deleteByExample(testPlanExample);
                //级联删除
                this.cascadeDeleteTestPlanIds(deleteGroupIds);
                //更新组下的数据
                extTestPlanMapper.updateDefaultGroupId(deleteGroupIds);
            });
        }
    }


    /**
     * 批量删除测试计划
     *
     * @param request
     * @param operator
     * @param requestUrl
     * @param requestMethod
     */
    public void batchDelete(TestPlanBatchProcessRequest request, String operator, String requestUrl, String requestMethod) {
        List<String> deleteIdList = request.getSelectIds();
        if (request.isSelectAll()) {
            deleteIdList = extTestPlanMapper.selectIdByConditions(request);
        }
        if (CollectionUtils.isEmpty(deleteIdList)) {
            return;
        }
        List<TestPlan> deleteTestPlanList = extTestPlanMapper.selectBaseInfoByIds(deleteIdList);
        if (CollectionUtils.isNotEmpty(deleteTestPlanList)) {
            List<String> testPlanGroupList = new ArrayList<>();
            List<String> testPlanIdList = new ArrayList<>();
            for (TestPlan testPlan : deleteTestPlanList) {
                if (StringUtils.equals(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
                    testPlanGroupList.add(testPlan.getId());
                } else {
                    testPlanIdList.add(testPlan.getId());
                }
            }
            this.deleteByList(deleteIdList);
            this.deleteGroupByList(testPlanGroupList);
            //记录日志
            testPlanLogService.saveBatchDeleteLog(deleteTestPlanList, operator, requestUrl, requestMethod);
        }
    }


    private void cascadeDeleteTestPlanIds(List<String> testPlanIds) {
        //删除当前计划对应的资源
        Map<String, TestPlanResourceService> subTypes = CommonBeanFactory.getBeansOfType(TestPlanResourceService.class);
        subTypes.forEach((k, t) -> {
            t.deleteBatchByTestPlanId(testPlanIds);
        });
        //删除测试计划配置
        TestPlanConfigExample configExample = new TestPlanConfigExample();
        configExample.createCriteria().andTestPlanIdIn(testPlanIds);
        testPlanConfigMapper.deleteByExample(configExample);

        TestPlanFollowerExample testPlanFollowerExample = new TestPlanFollowerExample();
        testPlanFollowerExample.createCriteria().andTestPlanIdIn(testPlanIds);
        testPlanFollowerMapper.deleteByExample(testPlanFollowerExample);
        /*
        todo
            删除计划定时任务  执行相关配置
         */

    }


    public TestPlanCountResponse getCount(String id) {
        TestPlanCountResponse response = new TestPlanCountResponse();
        response.setId(id);
        /*
        todo  统计：测试进度、通过率、用例数、Bug数量（这些比较慢的查询，是否需要另开接口查询)
         */

        response.setFunctionalCaseCount(0);
        response.setApiCaseCount(0);
        response.setApiScenarioCount(0);
        response.setPassRate("3.14%");
        response.setTestProgress("15.92%");
        return response;
    }


    /**
     * 更新测试计划
     *
     * @param request
     * @param userId
     * @param requestUrl
     * @param requestMethod
     * @return
     */
    public String update(TestPlanUpdateRequest request, String userId, String requestUrl, String requestMethod) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getId());
        if (!ObjectUtils.allNull(request.getName(), request.getModuleId(), request.getTags(), request.getPlannedEndTime(), request.getPlannedStartTime(), request.getDescription(), request.getTestPlanGroupId())) {
            TestPlan updateTestPlan = new TestPlan();
            updateTestPlan.setId(request.getId());
            if (StringUtils.isNotBlank(request.getName())) {
                updateTestPlan.setName(request.getName());
                updateTestPlan.setProjectId(testPlan.getProjectId());
                this.validateTestPlan(updateTestPlan);
            }
            if (StringUtils.isNotBlank(request.getModuleId())) {
                //检查模块的合法性
                this.checkModule(request.getModuleId());
                updateTestPlan.setModuleId(request.getModuleId());
            }
            if (CollectionUtils.isNotEmpty(request.getTags())) {
                updateTestPlan.setTags(new ArrayList<>(request.getTags()));
            }
            updateTestPlan.setPlannedStartTime(request.getPlannedStartTime());
            updateTestPlan.setPlannedEndTime(request.getPlannedEndTime());
            updateTestPlan.setDescription(request.getDescription());
            updateTestPlan.setGroupId(request.getTestPlanGroupId());
            updateTestPlan.setType(testPlan.getType());

            if (StringUtils.equals(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP) || StringUtils.isNotEmpty(request.getTestPlanGroupId())) {
                //修改组、移动测试计划进组出组，需要特殊的处理方式
                testPlanXPackFactory.getTestPlanGroupService().validateGroup(testPlan, null);
            }
            testPlanMapper.updateByPrimaryKeySelective(updateTestPlan);
        }

        if (!ObjectUtils.allNull(request.getAutomaticStatusUpdate(), request.getRepeatCase(), request.getPassThreshold(), request.getTestPlanning())) {
            TestPlanConfig testPlanConfig = new TestPlanConfig();
            testPlanConfig.setTestPlanId(request.getId());
            testPlanConfig.setAutomaticStatusUpdate(request.getAutomaticStatusUpdate());
            testPlanConfig.setRepeatCase(request.getRepeatCase());
            testPlanConfig.setPassThreshold(request.getPassThreshold());
            testPlanConfig.setTestPlanning(request.getTestPlanning());
            testPlanConfigMapper.updateByPrimaryKeySelective(testPlanConfig);
        }
        testPlanLogService.saveUpdateLog(testPlan, testPlanMapper.selectByPrimaryKey(request.getId()), testPlan.getProjectId(), userId, requestUrl, requestMethod);
        return request.getId();
    }


    /**
     * 关注/取消关注 测试计划
     *
     * @param testPlanId
     * @param userId
     */
    public void editFollower(String testPlanId, String userId) {
        TestPlanFollowerExample example = new TestPlanFollowerExample();
        example.createCriteria().andTestPlanIdEqualTo(testPlanId).andUserIdEqualTo(userId);
        if (testPlanFollowerMapper.countByExample(example) > 0) {
            testPlanFollowerMapper.deleteByPrimaryKey(testPlanId, userId);
        } else {
            TestPlanFollower testPlanFollower = new TestPlanFollower();
            testPlanFollower.setTestPlanId(testPlanId);
            testPlanFollower.setUserId(userId);
            testPlanFollowerMapper.insert(testPlanFollower);
        }
    }
}
