package io.metersphere.plan.service;

import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.request.*;
import io.metersphere.plan.dto.response.TestPlanDetailResponse;
import io.metersphere.plan.mapper.*;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.ScheduleExample;
import io.metersphere.system.domain.TestPlanModuleExample;
import io.metersphere.system.domain.User;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.ScheduleMapper;
import io.metersphere.system.mapper.TestPlanModuleMapper;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.BatchProcessUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanService extends TestPlanBaseUtilsService {
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
    private TestPlanAllocationMapper testPlanAllocationMapper;
    @Resource
    private TestPlanBatchCopyService testPlanBatchCopyService;
    @Resource
    private TestPlanBatchMoveService testPlanBatchMoveService;
    @Resource
    private TestPlanBatchArchivedService testPlanBatchArchivedService;
    @Resource
    private TestPlanStatisticsService testPlanStatisticsService;
    @Resource
    private TestPlanCaseService testPlanCaseService;
    @Resource
    private ScheduleMapper scheduleMapper;
    @Resource
    SqlSessionFactory sqlSessionFactory;
    @Resource
    private UserMapper userMapper;
    @Resource
    private TestPlanSendNoticeService testPlanSendNoticeService;
    private static final int MAX_TAG_SIZE = 10;

    /**
     * 创建测试计划
     *
     * @param testPlanCreateRequest
     * @param operator
     * @param requestUrl
     * @param requestMethod
     * @return
     */
    public TestPlan add(TestPlanCreateRequest testPlanCreateRequest, String operator, String requestUrl, String requestMethod) {
        TestPlan testPlan = savePlanDTO(testPlanCreateRequest, operator, null);
        testPlanLogService.saveAddLog(testPlan, operator, requestUrl, requestMethod);
        return testPlan;
    }


    /**
     * 保存数据
     *
     * @param createOrCopyRequest
     * @param operator
     * @param id                  复制的计划/计划组id 判断新增还是复制
     */
    private TestPlan savePlanDTO(TestPlanCreateRequest createOrCopyRequest, String operator, String id) {
        //检查模块的合法性
        checkModule(createOrCopyRequest.getModuleId());

        TestPlan createTestPlan = new TestPlan();
        BeanUtils.copyBean(createTestPlan, createOrCopyRequest);
        validateTestPlan(createTestPlan);

        createTestPlan.setId(IDGenerator.nextStr());
        long operateTime = System.currentTimeMillis();
        createTestPlan.setNum(NumGenerator.nextNum(createOrCopyRequest.getProjectId(), ApplicationNumScope.TEST_PLAN));
        createTestPlan.setCreateUser(operator);
        createTestPlan.setUpdateUser(operator);
        createTestPlan.setCreateTime(operateTime);
        createTestPlan.setUpdateTime(operateTime);
        createTestPlan.setStatus(TestPlanConstants.TEST_PLAN_STATUS_PREPARED);

        TestPlanConfig testPlanConfig = new TestPlanConfig();
        testPlanConfig.setTestPlanId(createTestPlan.getId());
        testPlanConfig.setAutomaticStatusUpdate(createOrCopyRequest.isAutomaticStatusUpdate());
        testPlanConfig.setRepeatCase(createOrCopyRequest.isRepeatCase());
        testPlanConfig.setPassThreshold(createOrCopyRequest.getPassThreshold());
        testPlanConfig.setTestPlanning(createOrCopyRequest.isTestPlanning());

        if (StringUtils.isBlank(id)) {
            handleAssociateCase(createOrCopyRequest.getBaseAssociateCaseRequest(), createTestPlan);
        } else {
            //复制
            handleCopy(createTestPlan, id);
        }

        testPlanMapper.insert(createTestPlan);
        testPlanConfigMapper.insert(testPlanConfig);
        return createTestPlan;
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
            // 计划组的删除暂时预留
            this.deleteGroupByList(Collections.singletonList(testPlan.getId()));
        } else {
            testPlanMapper.deleteByPrimaryKey(id);
            //级联删除
            TestPlanReportService testPlanReportService = CommonBeanFactory.getBean(TestPlanReportService.class);
            this.cascadeDeleteTestPlanIds(Collections.singletonList(id), testPlanReportService);
        }
        //记录日志
        testPlanLogService.saveDeleteLog(testPlan, operator, requestUrl, requestMethod);
    }

    private void deleteByList(List<String> testPlanIds) {
        if (CollectionUtils.isNotEmpty(testPlanIds)) {
            TestPlanReportService testPlanReportService = CommonBeanFactory.getBean(TestPlanReportService.class);

            BatchProcessUtils.consumerByString(testPlanIds, (deleteIds) -> {
                TestPlanExample testPlanExample = new TestPlanExample();
                testPlanExample.createCriteria().andIdIn(deleteIds);
                testPlanMapper.deleteByExample(testPlanExample);
                //级联删除
                this.cascadeDeleteTestPlanIds(deleteIds, testPlanReportService);
            });
        }
    }

    /**
     * 计划组删除的相关逻辑(待定)
     *
     * @param testPlanGroupIds 计划组ID集合
     */
    private void deleteGroupByList(List<String> testPlanGroupIds) {
        if (CollectionUtils.isNotEmpty(testPlanGroupIds)) {
            TestPlanReportService testPlanReportService = CommonBeanFactory.getBean(TestPlanReportService.class);
            BatchProcessUtils.consumerByString(testPlanGroupIds, (deleteGroupIds) -> {
                /*
                 * 计划组删除逻辑{第一版需求: 删除组, 组下的子计划Group置为None}:
                 * 1. 查询计划组下的全部子计划并删除(级联删除这些子计划的关联资源)
                 * 2. 删除所有计划组
                 */
                TestPlanExample testPlanExample = new TestPlanExample();
                testPlanExample.createCriteria().andGroupIdIn(deleteGroupIds);
                List<TestPlan> deleteGroupPlans = testPlanMapper.selectByExample(testPlanExample);
                List<String> deleteGroupPlanIds = deleteGroupPlans.stream().map(TestPlan::getId).toList();
                if (CollectionUtils.isNotEmpty(deleteGroupPlanIds)) {
                    // 级联删除子计划关联的资源(计划组不存在关联的资源)
                    this.cascadeDeleteTestPlanIds(deleteGroupPlanIds, testPlanReportService);
                }
                testPlanExample.clear();
                testPlanExample.createCriteria().andIdIn(ListUtils.union(deleteGroupIds, deleteGroupPlanIds));
                testPlanMapper.deleteByExample(testPlanExample);
            });
        }
    }


    /**
     * 批量删除测试计划
     *
     * @param request       批量请求参数
     * @param operator      当前登录操作人
     * @param requestUrl    请求URL
     * @param requestMethod 请求方法
     */
    public void batchDelete(TestPlanBatchProcessRequest request, String operator, String requestUrl, String requestMethod) {
        // 目前计划的批量操作不支持全选所有页
        List<String> deleteIdList = request.getSelectIds();
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
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
                this.deleteByList(testPlanIdList);
                // 计划组的删除暂时预留
                this.deleteGroupByList(testPlanGroupList);
                //记录日志
                testPlanLogService.saveBatchLog(deleteTestPlanList, operator, requestUrl, requestMethod, OperationLogType.DELETE.name(), "delete");
            }
        }
    }


    /**
     * 级联删除计划关联的资源
     *
     * @param testPlanIds 计划ID集合
     * @param testPlanReportService 这个方法会在批处理中使用，所以service在调用处通过传参的方式传入
     */
    private void cascadeDeleteTestPlanIds(List<String> testPlanIds, TestPlanReportService testPlanReportService) {
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

        TestPlanAllocationExample allocationExample = new TestPlanAllocationExample();
        allocationExample.createCriteria().andTestPlanIdIn(testPlanIds);
        testPlanAllocationMapper.deleteByExample(allocationExample);

        //删除测试计划报告 todo: 正式版增加接口用例报告、接口场景报告的清理
        testPlanReportService.deleteByTestPlanIds(testPlanIds);
        /*
        todo
            删除计划定时任务
         */
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
            if (StringUtils.isNotBlank(request.getModuleId())) {
                //检查模块的合法性
                checkModule(request.getModuleId());
                updateTestPlan.setModuleId(request.getModuleId());
            }
            if (StringUtils.isNotBlank(request.getName())) {
                updateTestPlan.setName(request.getName());
                updateTestPlan.setProjectId(testPlan.getProjectId());
                validateTestPlan(updateTestPlan);
            }
            if (CollectionUtils.isNotEmpty(request.getTags())) {
                updateTestPlan.setTags(new ArrayList<>(request.getTags()));
            }
            updateTestPlan.setPlannedStartTime(request.getPlannedStartTime());
            updateTestPlan.setPlannedEndTime(request.getPlannedEndTime());
            updateTestPlan.setDescription(request.getDescription());
            updateTestPlan.setGroupId(request.getTestPlanGroupId());
            updateTestPlan.setType(testPlan.getType());
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


    /**
     * 测试计划归档
     *
     * @param id
     * @param userId
     */
    public void archived(String id, String userId) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(id);
        if (StringUtils.equalsAnyIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            //测试计划组归档
            updateGroupStatus(testPlan.getId(), userId);
        } else {
            //测试计划
            testPlan.setStatus(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED);
            testPlan.setUpdateUser(userId);
            testPlan.setUpdateTime(System.currentTimeMillis());
            testPlanMapper.updateByPrimaryKeySelective(testPlan);
        }

    }

    /**
     * 批量归档
     *
     * @param request     批量请求参数
     * @param currentUser 当前用户
     */
    public void batchArchived(TestPlanBatchRequest request, String currentUser) {
        List<String> batchArchivedIds = request.getSelectIds();
        if (CollectionUtils.isNotEmpty(batchArchivedIds)) {
            TestPlanExample example = new TestPlanExample();
            example.createCriteria().andIdIn(batchArchivedIds);
            List<TestPlan> archivedPlanList = testPlanMapper.selectByExample(example);
            Map<String, List<TestPlan>> plans = archivedPlanList.stream().collect(Collectors.groupingBy(TestPlan::getType));
            testPlanBatchArchivedService.batchArchived(plans, request, currentUser);
            //日志
            testPlanLogService.saveBatchLog(archivedPlanList, currentUser, "/test-plan/batch-archived", HttpMethodConstants.POST.name(), OperationLogType.UPDATE.name(), "archive");
        }
    }

    /**
     * 测试计划组归档
     *
     * @param id
     * @param userId
     */
    private void updateGroupStatus(String id, String userId) {
        TestPlanExample example = new TestPlanExample();
        example.createCriteria().andGroupIdEqualTo(id);
        List<TestPlan> testPlanList = testPlanMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(testPlanList)) {
            throw new MSException(Translator.get("test_plan.group.not_plan"));
        }
        List<String> ids = testPlanList.stream().filter(item -> StringUtils.equalsIgnoreCase(item.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_COMPLETED)).map(TestPlan::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ids)) {
            throw new MSException(Translator.get("test_plan.group.not_plan"));
        }
        extTestPlanMapper.batchUpdateStatus(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED, userId, System.currentTimeMillis(), ids);
    }

    /**
     * 复制测试计划
     *
     * @param request
     * @param userId
     * @return
     */
    public TestPlan copy(TestPlanCopyRequest request, String userId) {
        TestPlan testPlan = savePlanDTO(request, userId, request.getId());
        return testPlan;
    }


    /**
     * 处理复制
     *
     * @param testPlan
     * @param id
     */
    private void handleCopy(TestPlan testPlan, String id) {
        if (StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            //计划组
            TestPlanExample example = new TestPlanExample();
            example.createCriteria().andGroupIdEqualTo(id);
            List<TestPlan> testPlans = testPlanMapper.selectByExample(example);
            if (CollectionUtils.isEmpty(testPlans)) {
                return;
            }
            List<String> ids = testPlans.stream().map(TestPlan::getId).collect(Collectors.toList());
            doHandleAssociateCase(ids, testPlan);
        } else {
            //计划
            doHandleAssociateCase(Arrays.asList(id), testPlan);
        }

    }

    /**
     * 处理复制 关联用例数据
     *
     * @param ids
     */
    private void doHandleAssociateCase(List<String> ids, TestPlan testPlan) {
        testPlanCaseService.saveTestPlanByPlanId(ids, testPlan);
        //TODO 复制关联接口用例/接口场景用例

    }


    /**
     * 获取单个测试计划或测试计划组详情（用于编辑）
     *
     * @param id 计划ID
     * @return 计划的详情数据
     */
    public TestPlanDetailResponse detail(String id) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(id);
        TestPlanDetailResponse response = new TestPlanDetailResponse();
        String moduleName = getModuleName(testPlan.getModuleId());
        //计划组只有几个参数
        response.setId(testPlan.getId());
        response.setNum(testPlan.getNum());
        response.setStatus(testPlan.getStatus());
        response.setName(testPlan.getName());
        response.setTags(testPlan.getTags());
        response.setModuleId(testPlan.getModuleId());
        response.setModuleName(moduleName);
        response.setDescription(testPlan.getDescription());
        if (StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_PLAN)) {
            //计划的 其他参数
            getGroupName(response, testPlan);
            response.setPlannedStartTime(testPlan.getPlannedStartTime());
            response.setPlannedEndTime(testPlan.getPlannedEndTime());
            getOtherConfig(response, testPlan);
            testPlanStatisticsService.calculateCaseCount(List.of(response));
        }
        // 是否定时任务
        ScheduleExample example = new ScheduleExample();
        example.createCriteria().andResourceIdEqualTo(id).andResourceTypeEqualTo("TEST_PLAN");
        response.setUseSchedule(scheduleMapper.countByExample(example) > 0);
        return response;
    }

    private void getOtherConfig(TestPlanDetailResponse response, TestPlan testPlan) {
        TestPlanConfig testPlanConfig = testPlanConfigMapper.selectByPrimaryKey(testPlan.getId());
        response.setAutomaticStatusUpdate(testPlanConfig.getAutomaticStatusUpdate());
        response.setRepeatCase(testPlanConfig.getRepeatCase());
        response.setPassThreshold(testPlanConfig.getPassThreshold());
        response.setTestPlanning(testPlanConfig.getTestPlanning());
    }

    private void getGroupName(TestPlanDetailResponse response, TestPlan testPlan) {
        if (!StringUtils.equalsIgnoreCase(testPlan.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            TestPlan group = testPlanMapper.selectByPrimaryKey(testPlan.getGroupId());
            response.setGroupId(testPlan.getGroupId());
            response.setGroupName(group.getName());
        }
    }


    public String getModuleName(String id) {
        if (ModuleConstants.DEFAULT_NODE_ID.equals(id)) {
            return Translator.get("functional_case.module.default.name");
        }
        return testPlanModuleMapper.selectByPrimaryKey(id).getName();
    }


    /**
     * 批量复制 （计划/计划组）
     *
     * @param request 批量请求参数
     * @param userId  当前登录用户
     * @param url     请求URL
     * @param method  请求方法
     */
    public void batchCopy(TestPlanBatchRequest request, String userId, String url, String method) {
        // 目前计划的批量操作不支持全选所有页
        List<String> copyIds = request.getSelectIds();
        if (CollectionUtils.isNotEmpty(copyIds)) {
            TestPlanExample example = new TestPlanExample();
            example.createCriteria().andIdIn(copyIds);
            List<TestPlan> copyTestPlanList = testPlanMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(copyTestPlanList)) {
                Map<String, List<TestPlan>> plans = copyTestPlanList.stream().collect(Collectors.groupingBy(TestPlan::getType));
                testPlanBatchCopyService.batchCopy(plans, request, userId);
                //日志
                testPlanLogService.saveBatchLog(copyTestPlanList, userId, url, method, OperationLogType.COPY.name(), "copy");
            }
        }
    }

    /**
     * 批量移动 (计划/计划组)
     *
     * @param request 批量请求参数
     * @param userId  当前登录用户
     * @param url     请求URL
     * @param method  请求方法
     */
    public void batchMove(TestPlanBatchRequest request, String userId, String url, String method) {
        // 目前计划的批量操作不支持全选所有页
        List<String> moveIds = request.getSelectIds();
        if (CollectionUtils.isNotEmpty(moveIds)) {
            TestPlanExample example = new TestPlanExample();
            example.createCriteria().andIdIn(moveIds);
            List<TestPlan> moveTestPlanList = testPlanMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(moveTestPlanList)) {
                Map<String, List<TestPlan>> plans = moveTestPlanList.stream().collect(Collectors.groupingBy(TestPlan::getType));
                testPlanBatchMoveService.batchMove(plans, request, userId);
                //日志
                testPlanLogService.saveBatchLog(moveTestPlanList, userId, url, method, OperationLogType.UPDATE.name(), "update");
            }
        }
    }


    /**
     * 批量编辑
     *
     * @param request
     * @param userId
     */
    public void batchEdit(TestPlanBatchEditRequest request, String userId) {
        // 目前计划的批量操作不支持全选所有页
        List<String> ids = request.getSelectIds();
        if (CollectionUtils.isNotEmpty(ids)) {
            User user = userMapper.selectByPrimaryKey(userId);
            handleTags(request, userId, ids);
            testPlanSendNoticeService.batchSendNotice(request.getProjectId(), ids, user, NoticeConstants.Event.UPDATE);
        }
    }

    /**
     * 处理标签
     *
     * @param request
     * @param userId
     * @param ids
     */
    private void handleTags(TestPlanBatchEditRequest request, String userId, List<String> ids) {
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            if (request.isAppend()) {
                //追加标签
                List<TestPlan> planList = extTestPlanMapper.getTagsByIds(ids);
                Map<String, TestPlan> collect = planList.stream().collect(Collectors.toMap(TestPlan::getId, v -> v));
                SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
                TestPlanMapper testPlanMapper = sqlSession.getMapper(TestPlanMapper.class);
                ids.forEach(id -> {
                    TestPlan testPlan = new TestPlan();
                    if (CollectionUtils.isNotEmpty(collect.get(id).getTags())) {
                        List<String> tags = collect.get(id).getTags();
                        tags.addAll(request.getTags());
                        checkTagsLength(tags);
                        List<String> newTags = tags.stream().distinct().collect(Collectors.toList());
                        testPlan.setTags(newTags);
                    } else {
                        testPlan.setTags(request.getTags());
                    }
                    testPlan.setId(id);
                    testPlan.setUpdateTime(System.currentTimeMillis());
                    testPlan.setUpdateUser(userId);
                    testPlanMapper.updateByPrimaryKeySelective(testPlan);
                });
                sqlSession.flushStatements();
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            } else {
                //替换标签
                TestPlan testPlan = new TestPlan();
                testPlan.setTags(request.getTags());
                testPlan.setProjectId(request.getProjectId());
                testPlan.setUpdateTime(System.currentTimeMillis());
                testPlan.setUpdateUser(userId);
                extTestPlanMapper.batchUpdate(testPlan, ids);
            }
        }

    }


    /**
     * 校验追加标签长度
     *
     * @param tags
     */
    private void checkTagsLength(List<String> tags) {
        if (tags.size() > MAX_TAG_SIZE) {
            throw new MSException(Translator.getWithArgs("tags_length_large_than", String.valueOf(MAX_TAG_SIZE)));
        }
    }

    //通过项目删除测试计划
    public void deleteByProjectId(String projectId) {

        List<String> testPlanIdList = extTestPlanMapper.selectIdByProjectId(projectId);

        //删除测试计划模块
        TestPlanModuleExample moduleExample = new TestPlanModuleExample();
        moduleExample.createCriteria().andProjectIdEqualTo(projectId);
        testPlanModuleMapper.deleteByExample(moduleExample);

        SubListUtils.dealForSubList(testPlanIdList, SubListUtils.DEFAULT_BATCH_SIZE, dealList -> {
            this.deleteByList(testPlanIdList);
        });
    }
}
