package io.metersphere.plan.service;

import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.response.TestPlanResponse;
import io.metersphere.plan.job.TestPlanScheduleJob;
import io.metersphere.plan.mapper.ExtTestPlanMapper;
import io.metersphere.plan.mapper.TestPlanCollectionMapper;
import io.metersphere.plan.mapper.TestPlanConfigMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.project.utils.NodeSortUtils;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.dto.request.schedule.BaseScheduleConfigRequest;
import io.metersphere.system.schedule.ScheduleService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanBatchOperationService extends TestPlanBaseUtilsService {

    @Resource
    private ExtTestPlanMapper extTestPlanMapper;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private ScheduleService scheduleService;
    @Resource
    private TestPlanScheduleService testPlanScheduleService;
    @Resource
    private TestPlanGroupService testPlanGroupService;
    @Resource
    private TestPlanConfigMapper testPlanConfigMapper;
    @Autowired
    private ApplicationContext applicationContext;
    @Resource
    private TestPlanCollectionMapper testPlanCollectionMapper;

    public long batchMoveModule(List<TestPlan> testPlanList, String moduleId, String userId) {
        List<String> movePlanIds = new ArrayList<>();
        for (TestPlan testPlan : testPlanList) {
            // 已归档的测试计划无法操作
            if (StringUtils.equalsIgnoreCase(testPlan.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
                throw new MSException(Translator.get("test_plan.is.archived"));
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
            // 已归档的测试计划无法操作
            if (StringUtils.equalsIgnoreCase(testPlan.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
                throw new MSException(Translator.get("test_plan.is.archived"));
            }
            // 已归档的测试计划无法操作 测试计划组无法操作
            if (StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
                throw new MSException(Translator.get("test_plan.group.error"));
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


    public List<TestPlan> batchCopy(List<TestPlan> originalPlanList, String targetId, String targetType, String userId) {
        List<TestPlan> copyPlanResult = new ArrayList<>();
        long operatorTime = System.currentTimeMillis();
        //如果目标ID是测试计划组， 需要进行容量校验
        if (!StringUtils.equalsIgnoreCase(targetType, ModuleConstants.NODE_TYPE_DEFAULT)) {
            testPlanGroupService.validateGroupCapacity(targetId, originalPlanList.size());
        }
        /*
            此处不进行批量处理，原因有两点：
            1） 测试计划内（或者测试计划组内）数据量不可控，选择批量操作时更容易出现数据太多不走索引、数据太多内存溢出等问题。不批量操作可以减少这些问题出现的概率，代价是速度会变慢。
            2） 作为数据量不可控的操作，如果数据量少，不采用批量处理也不会消耗太多时间。如果数据量多，就会容易出现1的问题。并且本人不建议针对不可控数据量的数据支持批量操作。
         */
        for (TestPlan copyPlan : originalPlanList) {
            if (StringUtils.equalsIgnoreCase(copyPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
                copyPlanResult.add(this.copyPlanGroup(copyPlan, targetId, targetType, operatorTime, userId));
            } else {
                copyPlanResult.add(this.copyPlan(copyPlan, targetId, targetType, operatorTime, userId));
            }
        }
        return copyPlanResult;
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
    public TestPlan copyPlan(TestPlan originalTestPlan, String targetId, String targetType, long operatorTime, String operator) {
        //已归档的无法操作
        if (StringUtils.equalsIgnoreCase(originalTestPlan.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
            throw new MSException(Translator.get("test_plan.error"));
        }
        String moduleId = originalTestPlan.getModuleId();
        String groupId = originalTestPlan.getGroupId();
        long pos = originalTestPlan.getPos();
        String sortRangeId = TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID;
        if (StringUtils.equals(targetType, TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            if (!StringUtils.equalsIgnoreCase(targetId, TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
                TestPlan group = testPlanMapper.selectByPrimaryKey(targetId);
                //如果目标ID是测试计划组， 需要进行容量校验
                if (!StringUtils.equalsIgnoreCase(targetType, ModuleConstants.NODE_TYPE_DEFAULT)) {
                    testPlanGroupService.validateGroupCapacity(targetId, 1);
                }
                moduleId = group.getModuleId();
                sortRangeId = targetId;
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
        testPlan.setPos(testPlanGroupService.getNextOrder(sortRangeId));
        testPlan.setActualEndTime(null);
        testPlan.setActualStartTime(null);
        testPlan.setStatus(TestPlanConstants.TEST_PLAN_STATUS_NOT_ARCHIVED);
        testPlanMapper.insert(testPlan);

        //测试配置信息
        TestPlanConfig originalTestPlanConfig = testPlanConfigMapper.selectByPrimaryKey(originalTestPlan.getId());
        if (originalTestPlanConfig != null) {
            TestPlanConfig newTestPlanConfig = new TestPlanConfig();
            BeanUtils.copyBean(newTestPlanConfig, originalTestPlanConfig);
            newTestPlanConfig.setTestPlanId(testPlan.getId());
            testPlanConfigMapper.insertSelective(newTestPlanConfig);
        }

        //测试规划信息
        TestPlanCollectionExample parentCollectionExample = new TestPlanCollectionExample();
        parentCollectionExample.createCriteria().andTestPlanIdEqualTo(originalTestPlan.getId()).andParentIdEqualTo(TestPlanConstants.DEFAULT_PARENT_ID);
        List<TestPlanCollection> testPlanCollectionList = testPlanCollectionMapper.selectByExample(parentCollectionExample);
        Map<String, String> oldCollectionIdToNewCollectionId = new HashMap<>();
        if (CollectionUtils.isEmpty(testPlanCollectionList)) {
            //自动生成测试规划
            Objects.requireNonNull(CommonBeanFactory.getBean(TestPlanService.class)).initDefaultPlanCollection(testPlan.getId(), operator);
        } else {
            List<TestPlanCollection> newTestPlanCollectionList = new ArrayList<>();
            for (TestPlanCollection testPlanCollection : testPlanCollectionList) {
                TestPlanCollection newTestPlanCollection = new TestPlanCollection();
                BeanUtils.copyBean(newTestPlanCollection, testPlanCollection);
                newTestPlanCollection.setId(IDGenerator.nextStr());
                newTestPlanCollection.setTestPlanId(testPlan.getId());
                newTestPlanCollection.setCreateUser(operator);
                newTestPlanCollection.setCreateTime(operatorTime);
                newTestPlanCollectionList.add(newTestPlanCollection);

                //查找测试集信息
                TestPlanCollectionExample childExample = new TestPlanCollectionExample();
                childExample.createCriteria().andParentIdEqualTo(testPlanCollection.getId());
                List<TestPlanCollection> children = testPlanCollectionMapper.selectByExample(childExample);
                for (TestPlanCollection child : children) {
                    TestPlanCollection childCollection = new TestPlanCollection();
                    BeanUtils.copyBean(childCollection, child);
                    childCollection.setId(IDGenerator.nextStr());
                    childCollection.setParentId(newTestPlanCollection.getId());
                    childCollection.setTestPlanId(testPlan.getId());
                    childCollection.setCreateUser(operator);
                    childCollection.setCreateTime(operatorTime);
                    newTestPlanCollectionList.add(childCollection);
                    oldCollectionIdToNewCollectionId.put(child.getId(), childCollection.getId());
                }
            }
            testPlanCollectionMapper.batchInsert(newTestPlanCollectionList);
        }

        //测试用例信息
        Map<String, TestPlanResourceService> beansOfType = applicationContext.getBeansOfType(TestPlanResourceService.class);
        beansOfType.forEach((k, v) -> {
            v.copyResource(originalTestPlan.getId(), testPlan.getId(), oldCollectionIdToNewCollectionId, operator, operatorTime);
        });

        // 复制计划-定时任务信息
        copySchedule(originalTestPlan.getId(), testPlan.getId(), operator);

        return testPlan;
    }

    public TestPlan copyPlanGroup(TestPlan originalGroup, String targetId, String targetType, long operatorTime, String operator) {
        //测试计划组复制的时候，只支持targetType为module的操作. 已归档的无法操作
        if (StringUtils.equalsIgnoreCase(targetType, TestPlanConstants.TEST_PLAN_TYPE_GROUP)
                || StringUtils.equalsIgnoreCase(originalGroup.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
            throw new MSException(Translator.get("test_plan.group.error"));
        }

        super.checkModule(targetId);
        String moduleId = targetId;

        TestPlanExample example = new TestPlanExample();
        example.createCriteria().andGroupIdEqualTo(originalGroup.getId()).andStatusNotEqualTo(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED);
        example.setOrderByClause("pos asc");
        List<TestPlan> childList = testPlanMapper.selectByExample(example);

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
        testPlanGroup.setPos(testPlanGroupService.getNextOrder(originalGroup.getGroupId()));
        testPlanGroup.setActualEndTime(null);
        testPlanGroup.setActualStartTime(null);
        testPlanGroup.setStatus(TestPlanConstants.TEST_PLAN_STATUS_NOT_ARCHIVED);
        testPlanMapper.insert(testPlanGroup);

        //测试配置信息
        TestPlanConfig originalTestPlanConfig = testPlanConfigMapper.selectByPrimaryKey(originalGroup.getId());
        if (originalTestPlanConfig != null) {
            TestPlanConfig newTestPlanConfig = new TestPlanConfig();
            BeanUtils.copyBean(newTestPlanConfig, originalTestPlanConfig);
            newTestPlanConfig.setTestPlanId(testPlanGroup.getId());
            testPlanConfigMapper.insertSelective(newTestPlanConfig);
        }

        for (TestPlan child : childList) {
            copyPlan(child, testPlanGroup.getId(), TestPlanConstants.TEST_PLAN_TYPE_GROUP, operatorTime, operator);
        }

        // 复制计划组-定时任务信息
        copySchedule(originalGroup.getId(), testPlanGroup.getId(), operator);
        return testPlanGroup;
    }

    /**
     * 复制 计划/计划组 定时任务
     * @param resourceId 来源ID
     * @param targetId 目标ID
     * @param operator 操作人
     */
    private void copySchedule(String resourceId, String targetId, String operator) {
        Schedule originalSchedule = scheduleService.getScheduleByResource(resourceId, TestPlanScheduleJob.class.getName());
        if (originalSchedule != null) {
            // 来源的 "计划/计划组" 存在定时任务即复制, 无论开启或关闭
            BaseScheduleConfigRequest scheduleRequest = new BaseScheduleConfigRequest();
            scheduleRequest.setEnable(originalSchedule.getEnable());
            scheduleRequest.setCron(originalSchedule.getValue());
            // noinspection unchecked
            scheduleRequest.setRunConfig(JSON.parseMap(originalSchedule.getConfig()));
            scheduleRequest.setResourceId(targetId);
            testPlanScheduleService.scheduleConfig(scheduleRequest, operator);
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
