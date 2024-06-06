package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanConfig;
import io.metersphere.plan.domain.TestPlanExample;
import io.metersphere.plan.dto.response.TestPlanResponse;
import io.metersphere.plan.mapper.ExtTestPlanMapper;
import io.metersphere.plan.mapper.TestPlanConfigMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.project.utils.NodeSortUtils;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanBatchOperationService extends TestPlanBaseUtilsService {

    @Resource
    private ExtTestPlanMapper extTestPlanMapper;
    @Resource
    private TestPlanMapper testPlanMapper;

    @Resource
    private TestPlanGroupService testPlanGroupService;
    @Resource
    private TestPlanConfigMapper testPlanConfigMapper;
    @Autowired
    private ApplicationContext applicationContext;

    public long batchMoveModule(List<TestPlan> testPlanList, String moduleId, String userId) {
        List<String> movePlanIds = new ArrayList<>();
        for (TestPlan testPlan : testPlanList) {
            // 已归档的测试计划无法操作
            if (StringUtils.equalsIgnoreCase(testPlan.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
                continue;
            }
            if (!StringUtils.equalsIgnoreCase(testPlan.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
                // 测试计划组下的测试计划不单独处理， 如果勾选了他的测试计划组，会在下面进行逻辑补足。
                continue;
            }

            movePlanIds.add(testPlan.getId());
            if (StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
                List<TestPlanResponse> testPlanItemList = extTestPlanMapper.selectByGroupIds(Collections.singletonList(testPlan.getId()));
                for (TestPlanResponse item : testPlanItemList) {
                    movePlanIds.add(item.getId());
                }
            }
        }
        movePlanIds = movePlanIds.stream().distinct().toList();
        return batchMovePlan(movePlanIds, moduleId, userId);
    }

    public long batchMoveGroup(List<TestPlan> testPlanList, String groupId, String userId) {
        // 判断测试计划组是否存在
        String groupModuleId = null;
        if (!StringUtils.equalsIgnoreCase(groupId, TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            TestPlan groupPlan = testPlanMapper.selectByPrimaryKey(groupId);
            groupModuleId = groupPlan.getModuleId();
        }

        List<String> movePlanIds = new ArrayList<>();
        for (TestPlan testPlan : testPlanList) {
            // 已归档的测试计划无法操作 测试计划组无法操作
            if (StringUtils.equalsIgnoreCase(testPlan.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)
                    || StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
                continue;
            }
            movePlanIds.add(testPlan.getId());
        }

        long nextPos = testPlanGroupService.getNextOrder(groupId);
        long operationTimestamp = System.currentTimeMillis();
        int index = 0;
        for (TestPlan testPlan : testPlanList) {
            TestPlan updatePlan = new TestPlan();
            updatePlan.setId(testPlan.getId());
            updatePlan.setUpdateTime(operationTimestamp);
            updatePlan.setGroupId(groupId);
            updatePlan.setModuleId(StringUtils.isBlank(groupModuleId) ? testPlan.getModuleId() : groupModuleId);
            updatePlan.setPos(nextPos + index * NodeSortUtils.DEFAULT_NODE_INTERVAL_POS);
            updatePlan.setUpdateUser(userId);
            testPlanMapper.updateByPrimaryKeySelective(updatePlan);
        }
        return testPlanList.size();
    }

    /**
     * 批量移动计划
     */
    private long batchMovePlan(List<String> ids, String moduleId, String userId) {
        if (CollectionUtils.isNotEmpty(ids)) {
            return extTestPlanMapper.batchMove(ids, moduleId, userId, System.currentTimeMillis());
        } else {
            return 0;
        }
    }


    public long batchCopy(List<TestPlan> copyPlanList, String targetId, String targetType, String userId) {
        long copyCount = 0;
        long operatorTime = System.currentTimeMillis();
        /*
            此处不选择批量操作，原因有两点：
            1） 测试计划内（或者测试计划组内）数据量不可控，选择批量操作时更容易出现数据太多不走索引、数据太多内存溢出等问题。不批量操作可以减少这些问题出现的概率，代价是速度会变慢。
            2） 作为数据量不可控的操作，如果数据量少，不采用批量处理也不会消耗太多时间。如果数据量多，就会容易出现1的问题。并且本人不建议针对不可控数据量的数据支持批量操作。
         */
        for (TestPlan copyPlan : copyPlanList) {
            if (StringUtils.equalsIgnoreCase(copyPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
                copyCount += this.copyPlanGroup(copyPlan, targetId, targetType, operatorTime, userId);
            } else {
                copyCount += this.copyPlan(copyPlan, targetId, targetType, operatorTime, userId);
            }
        }
        return copyCount;
    }


    /**
     * 复制测试计划
     *
     * @param originalTestPlan 原始测试计划
     * @param targetId         目标ID
     * @param targetType       目标类型
     * @param operatorTime     操作时间
     * @param operator         操作人
     * @return 复制的数量
     */
    public int copyPlan(TestPlan originalTestPlan, String targetId, String targetType, long operatorTime, String operator) {
        //已归档的无法操作
        if (StringUtils.equalsIgnoreCase(originalTestPlan.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
            return 0;
        }
        String moduleId = originalTestPlan.getModuleId();
        String groupId = originalTestPlan.getGroupId();
        long pos = originalTestPlan.getPos();
        if (StringUtils.equals(targetType, TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            if (StringUtils.equalsIgnoreCase(targetId, TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
                pos = 0L;
            } else {
                TestPlan group = testPlanMapper.selectByPrimaryKey(targetId);
                //已归档的无法操作
                if (group == null || StringUtils.equalsIgnoreCase(group.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)
                        || !StringUtils.equalsIgnoreCase(group.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
                    throw new MSException(Translator.get("test_plan.group.error"));
                }
                pos = testPlanGroupService.getNextOrder(targetId);
                moduleId = group.getId();
            }
            groupId = targetId;
        } else {
            super.checkModule(targetId);
            moduleId = targetId;
        }

        TestPlan testPlan = new TestPlan();
        BeanUtils.copyBean(testPlan, originalTestPlan);
        testPlan.setId(IDGenerator.nextStr());
        testPlan.setNum(NumGenerator.nextNum(testPlan.getProjectId(), ApplicationNumScope.TEST_PLAN));
        testPlan.setName(this.getCopyName(originalTestPlan.getName(), originalTestPlan.getNum(), testPlan.getNum()));
        testPlan.setCreateUser(operator);
        testPlan.setCreateTime(operatorTime);
        testPlan.setUpdateUser(operator);
        testPlan.setUpdateTime(operatorTime);
        testPlan.setModuleId(moduleId);
        testPlan.setGroupId(groupId);
        testPlan.setPos(pos);
        testPlan.setStatus(TestPlanConstants.TEST_PLAN_STATUS_PREPARED);
        testPlanMapper.insert(testPlan);

        //测试配置信息
        TestPlanConfig originalTestPlanConfig = testPlanConfigMapper.selectByPrimaryKey(originalTestPlan.getId());
        if (originalTestPlanConfig != null) {
            TestPlanConfig newTestPlanConfig = new TestPlanConfig();
            BeanUtils.copyBean(newTestPlanConfig, originalTestPlanConfig);
            newTestPlanConfig.setTestPlanId(testPlan.getId());
            testPlanConfigMapper.insert(newTestPlanConfig);
        }

        //todo 测试规划信息

        //测试用例信息
        Map<String, TestPlanResourceService> beansOfType = applicationContext.getBeansOfType(TestPlanResourceService.class);
        beansOfType.forEach((k, v) -> {
            v.copyResource(originalTestPlan.getId(), testPlan.getId(), operator, operatorTime);
        });
        return 1;
    }

    public int copyPlanGroup(TestPlan originalGroup, String targetId, String targetType, long operatorTime, String operator) {
        //测试计划组复制的时候，只支持targetType为module的操作. 已归档的无法操作
        if (StringUtils.equalsIgnoreCase(targetType, TestPlanConstants.TEST_PLAN_TYPE_GROUP)
                || StringUtils.equalsIgnoreCase(originalGroup.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
            return 0;
        }

        super.checkModule(targetId);
        String moduleId = targetId;

        TestPlanExample example = new TestPlanExample();
        example.createCriteria().andGroupIdEqualTo(originalGroup.getId());
        example.setOrderByClause("pos asc");
        List<TestPlan> childList = testPlanMapper.selectByExample(example);

        int copyCount = 0;

        TestPlan testPlanGroup = new TestPlan();
        BeanUtils.copyBean(testPlanGroup, originalGroup);
        testPlanGroup.setId(IDGenerator.nextStr());
        testPlanGroup.setNum(NumGenerator.nextNum(testPlanGroup.getProjectId(), ApplicationNumScope.TEST_PLAN));
        testPlanGroup.setName(this.getCopyName(originalGroup.getName(), originalGroup.getNum(), testPlanGroup.getNum()));
        testPlanGroup.setCreateUser(operator);
        testPlanGroup.setCreateTime(operatorTime);
        testPlanGroup.setUpdateUser(operator);
        testPlanGroup.setUpdateTime(operatorTime);
        testPlanGroup.setModuleId(moduleId);
        testPlanGroup.setStatus(TestPlanConstants.TEST_PLAN_STATUS_PREPARED);
        copyCount += testPlanMapper.insert(testPlanGroup);

        for (TestPlan child : childList) {
            copyCount += copyPlan(child, testPlanGroup.getId(), TestPlanConstants.TEST_PLAN_TYPE_GROUP, operatorTime, operator);
        }

        return copyCount;
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
