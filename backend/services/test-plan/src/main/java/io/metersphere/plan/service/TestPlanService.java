package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanConfig;
import io.metersphere.plan.domain.TestPlanConfigExample;
import io.metersphere.plan.domain.TestPlanExample;
import io.metersphere.plan.dto.TestPlanQueryConditions;
import io.metersphere.plan.dto.request.TestPlanBatchProcessRequest;
import io.metersphere.plan.dto.request.TestPlanCreateRequest;
import io.metersphere.plan.dto.response.TestPlanResponse;
import io.metersphere.plan.mapper.ExtTestPlanMapper;
import io.metersphere.plan.mapper.TestPlanConfigMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.TestPlanModuleExample;
import io.metersphere.system.mapper.TestPlanModuleMapper;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.BatchProcessUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
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
    private TestPlanConfigService testPlanConfigService;
    
    @Resource
    private TestPlanFollowerService testPlanFollowerService;
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
                throw new MSException("module.not.exist");
            }
        }
    }

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
        createTestPlan.setType(TestPlanConstants.TEST_PLAN_TYPE_PLAN);
        testPlanMapper.insert(createTestPlan);

        TestPlanConfig testPlanConfig = new TestPlanConfig();
        testPlanConfig.setTestPlanId(createTestPlan.getId());
        testPlanConfig.setAutomaticStatusUpdate(testPlanCreateRequest.isAutomaticStatusUpdate());
        testPlanConfig.setRepeatCase(testPlanCreateRequest.isRepeatCase());
        testPlanConfig.setPassThreshold(testPlanCreateRequest.getPassThreshold());
        testPlanConfigMapper.insert(testPlanConfig);

        testPlanLogService.saveAddLog(createTestPlan, operator, requestUrl, requestMethod);

        return createTestPlan.getId();
    }

    private void validateTestPlan(TestPlan testPlan) {
        TestPlanExample example = new TestPlanExample();
        if (StringUtils.isBlank(testPlan.getId())) {
            TestPlanExample.Criteria criteria = example.createCriteria();
            //测试计划第一层的数据还不能超过1000个
            criteria.andGroupIdEqualTo(TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID);
            if (testPlanMapper.countByExample(example) >= MAX_TEST_PLAN_SIZE) {
                throw new MSException(Translator.getWithArgs("test_plan.too_many", MAX_TEST_PLAN_SIZE));
            }
            example.clear();
            example.createCriteria().andNameEqualTo(testPlan.getName()).andProjectIdEqualTo(testPlan.getProjectId());
            if (testPlanMapper.countByExample(example) > 0) {
                throw new MSException(Translator.get("test_plan.name.exist") + ":" + testPlan.getName());
            }
        }

        /*
        //todo 重名校验-写到测试计划修改的时候再增加
        else {
            example.createCriteria().andNameEqualTo(testPlan.getName()).andProjectIdEqualTo(testPlan.getProjectId()).andIdNotEqualTo(testPlan.getId());
            if (testPlanMapper.countByExample(example) > 0) {
                throw new MSException(Translator.get("test_plan.name.exist") + ":" + testPlan.getName());
            }
        }
         */
    }

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


    public void batchDelete(TestPlanBatchProcessRequest request, String operator, String requestUrl, String requestMethod) {
        List<String> deleteIdList = request.getSelectIds();
        if (request.isSelectAll()) {
            TestPlanQueryConditions testPlanQueryConditions = new TestPlanQueryConditions(request.getModuleIds(), request.getProjectId(), request.getCondition());
            testPlanQueryConditions.setHiddenIds(request.getExcludeIds());
            deleteIdList = extTestPlanMapper.selectIdByConditions(testPlanQueryConditions);
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
            try {
                t.deleteBatchByTestPlanId(testPlanIds);
            } catch (Exception e) {
                LogUtils.error(e);
            }
        });
        //删除测试计划配置
        TestPlanConfigExample configExample = new TestPlanConfigExample();
        configExample.createCriteria().andTestPlanIdIn(testPlanIds);
        testPlanConfigMapper.deleteByExample(configExample);
        /*
        todo
            删除计划的关注者
            删除计划报告
            删除计划定时任务
         */

    }


    public TestPlanResponse getCount(String id) {
        TestPlanResponse response = new TestPlanResponse();
        response.setId(id);
        /*
        todo  统计：测试进度、通过率、用例数、Bug数量（这些比较慢的查询，是否需要另开接口查询)
            Q:测试计划组需要查询这些数据吗？
         */
        response.setFunctionalCaseCount(0);
        response.setApiCaseCount(0);
        response.setApiScenarioCount(0);
        response.setPassRate("3.14%");
        response.setTestProgress("15.92%");
        return response;
    }
}
