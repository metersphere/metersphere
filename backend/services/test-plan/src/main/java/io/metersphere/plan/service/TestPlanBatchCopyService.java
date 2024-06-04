package io.metersphere.plan.service;

import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.request.TestPlanBatchProcessRequest;
import io.metersphere.plan.dto.request.TestPlanBatchRequest;
import io.metersphere.plan.mapper.TestPlanAllocationMapper;
import io.metersphere.plan.mapper.TestPlanConfigMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanBatchCopyService {

    @Resource
    private TestPlanConfigMapper testPlanConfigMapper;
    @Resource
    private TestPlanAllocationMapper testPlanAllocationMapper;
    @Resource
    private TestPlanMapper testPlanMapper;

    public void batchCopy(Map<String, List<TestPlan>> plans, TestPlanBatchRequest request, String userId) {
        batchCopyGroup(plans, request, userId);
        batchCopyPlan(plans, request, userId);
    }

    /**
     * 批量复制组
     *
     * @param plans
     */
    private void batchCopyGroup(Map<String, List<TestPlan>> plans, TestPlanBatchProcessRequest request, String userId) {
        //TODO 批量复制计划组
    }


    /**
     * 批量复制计划
     *
     * @param plans
     */
    private void batchCopyPlan(Map<String, List<TestPlan>> plans, TestPlanBatchRequest request, String userId) {
        if (plans.containsKey(TestPlanConstants.TEST_PLAN_TYPE_PLAN)) {
            List<TestPlan> testPlans = plans.get(TestPlanConstants.TEST_PLAN_TYPE_PLAN);
            List<String> ids = testPlans.stream().map(TestPlan::getId).collect(Collectors.toList());
            //额外信息

            TestPlanConfigExample configExample = new TestPlanConfigExample();
            configExample.createCriteria().andTestPlanIdIn(ids);
            List<TestPlanConfig> testPlanConfigs = testPlanConfigMapper.selectByExample(configExample);
            //测试规划配置信息
            TestPlanAllocationExample allocationExample = new TestPlanAllocationExample();
            allocationExample.createCriteria().andTestPlanIdIn(ids);
            List<TestPlanAllocation> testPlanAllocations = testPlanAllocationMapper.selectByExample(allocationExample);
            batchInsertPlan(testPlans, testPlanConfigs, testPlanAllocations, request, userId);
        }
    }

    private void batchInsertPlan(List<TestPlan> testPlans, List<TestPlanConfig> testPlanConfigs, List<TestPlanAllocation> testPlanAllocations, TestPlanBatchRequest request, String userId) {
        Map<String, List<TestPlanConfig>> configs = testPlanConfigs.stream().collect(Collectors.groupingBy(TestPlanConfig::getTestPlanId));
        Map<String, List<TestPlanAllocation>> allocationsList = testPlanAllocations.stream().collect(Collectors.groupingBy(TestPlanAllocation::getTestPlanId));
        List<TestPlanConfig> newConfigs = new ArrayList<>();
        List<TestPlanAllocation> newAllocations = new ArrayList<>();
        testPlans.forEach(testPlan -> {
            List<TestPlanConfig> config = configs.get(testPlan.getId());
            List<TestPlanAllocation> allocations = allocationsList.get(testPlan.getId());
            Long num = testPlan.getNum();
            testPlan.setId(IDGenerator.nextStr());
            testPlan.setStatus(TestPlanConstants.TEST_PLAN_STATUS_PREPARED);
            testPlan.setNum(NumGenerator.nextNum(testPlan.getProjectId(), ApplicationNumScope.TEST_PLAN));
            testPlan.setName(getCopyName(testPlan.getName(), num, testPlan.getNum()));
            testPlan.setModuleId(request.getTargetId());
            testPlan.setCreateTime(System.currentTimeMillis());
            testPlan.setUpdateTime(System.currentTimeMillis());
            testPlan.setCreateUser(userId);
            testPlan.setUpdateUser(userId);

            if (CollectionUtils.isNotEmpty(config)) {
                TestPlanConfig testPlanConfig = config.get(0);
                testPlanConfig.setTestPlanId(testPlan.getId());
                newConfigs.add(testPlanConfig);
            }
            if (CollectionUtils.isNotEmpty(allocations)) {
                TestPlanAllocation testPlanAllocation = allocations.get(0);
                testPlanAllocation.setTestPlanId(testPlan.getId());
                testPlanAllocation.setId(IDGenerator.nextStr());
                newAllocations.add(testPlanAllocation);
            }
        });
        testPlanMapper.batchInsert(testPlans);
        if (CollectionUtils.isNotEmpty(newConfigs)) {
            testPlanConfigMapper.batchInsert(newConfigs);
        }
        if (CollectionUtils.isNotEmpty(newAllocations)) {
            testPlanAllocationMapper.batchInsert(newAllocations);
        }
    }

    private String getCopyName(String name, long oldNum, long newNum) {
        if (!StringUtils.startsWith(name, "copy_")) {
            name = "copy_" + name;
        }
        if (name.length() > 250) {
            name = name.substring(0, 200) + "...";
        }
        if (StringUtils.endsWith(name, "_" + oldNum)) {
            name = StringUtils.substringBeforeLast(name, "_" + oldNum);
        }
        name = name + "_" + newNum;
        return name;
    }
}
