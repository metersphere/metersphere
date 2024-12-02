package io.metersphere.plan.service;

import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.TestPlanCollectionDTO;
import io.metersphere.plan.dto.TestPlanExecuteHisDTO;
import io.metersphere.plan.dto.TestPlanResourceExecResultDTO;
import io.metersphere.plan.dto.request.*;
import io.metersphere.plan.dto.response.TestPlanCoverageDTO;
import io.metersphere.plan.dto.response.TestPlanDetailResponse;
import io.metersphere.plan.dto.response.TestPlanOperationResponse;
import io.metersphere.plan.dto.response.TestPlanStatisticsResponse;
import io.metersphere.plan.enums.ExecuteMethod;
import io.metersphere.plan.job.TestPlanScheduleJob;
import io.metersphere.plan.mapper.*;
import io.metersphere.project.request.ProjectApplicationRequest;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.*;
import io.metersphere.system.domain.ScheduleExample;
import io.metersphere.system.domain.TestPlanModule;
import io.metersphere.system.domain.TestPlanModuleExample;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.LogInsertModule;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.BaseUserMapper;
import io.metersphere.system.mapper.ScheduleMapper;
import io.metersphere.system.mapper.TestPlanModuleMapper;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.schedule.ScheduleService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.BatchProcessUtils;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
    private BaseUserMapper baseUserMapper;
    @Resource
    private TestPlanGroupService testPlanGroupService;
    @Resource
    private TestPlanConfigMapper testPlanConfigMapper;
    @Resource
    private TestPlanLogService testPlanLogService;
    @Resource
    private TestPlanModuleMapper testPlanModuleMapper;
    @Resource
    private TestPlanFollowerMapper testPlanFollowerMapper;
    @Resource
    private TestPlanBatchOperationService testPlanBatchOperationService;
    @Resource
    private TestPlanStatisticsService testPlanStatisticsService;
    @Resource
    private ScheduleMapper scheduleMapper;
    @Resource
    SqlSessionFactory sqlSessionFactory;
    @Resource
    private UserMapper userMapper;
    @Resource
    private TestPlanSendNoticeService testPlanSendNoticeService;
    @Resource
    private TestPlanCaseExecuteHistoryMapper testPlanCaseExecuteHistoryMapper;
    @Resource
    private ScheduleService scheduleService;
    @Resource
    private ProjectApplicationService projectApplicationService;
    @Resource
    private TestPlanCollectionMapper testPlanCollectionMapper;
    @Resource
    private TestPlanFunctionalCaseMapper testPlanFunctionalCaseMapper;
    @Resource
    private TestPlanReportFunctionCaseMapper testPlanReportFunctionCaseMapper;
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    private TestPlanReportCaseService testPlanReportCaseService;
    @Resource
    private TestPlanReportService testPlanReportService;
    @Resource
    private TestPlanReportMapper testPlanReportMapper;
    @Resource
    private ExtTestPlanReportMapper extTestPlanReportMapper;

    public void autoUpdateFunctionalCase(String testPlanReportId) {
        TestPlanReport testPlanReport = testPlanReportMapper.selectByPrimaryKey(testPlanReportId);
        if (testPlanReport == null) {
            return;
        }
        TestPlanConfig testPlanConfig = testPlanConfigMapper.selectByPrimaryKey(testPlanReport.getTestPlanId());
        if (testPlanConfig != null && BooleanUtils.isTrue(testPlanConfig.getAutomaticStatusUpdate())) {
            TestPlanFunctionalCaseService testPlanFunctionalCaseService = CommonBeanFactory.getBean(TestPlanFunctionalCaseService.class);
            Map<String, List<String>> execResultFuncCaseMap = new HashMap<>();
            List<String> functionalCaseIds = testPlanReportCaseService.selectFunctionalCaseIdsByTestPlanId(testPlanReport.getTestPlanId());
            Map<String, List<String>> funcCaseAssociationCaseMap = testPlanFunctionalCaseService.getFuncCaseAssociationCaseMap(functionalCaseIds);
            Map<String, String> caseExecResult = testPlanReportCaseService.selectCaseExecResultByReportId(testPlanReportId);
            funcCaseAssociationCaseMap.forEach((funcCaseId, apiIdList) -> {
                List<String> execResultList = new ArrayList<>();
                apiIdList.forEach(apiId -> {
                    String result = caseExecResult.get(apiId);
                    if (StringUtils.isNotBlank(result)) {
                        execResultList.add(result);
                    }
                });
                String lastResult = testPlanReportCaseService.calculateFuncCaseExecResult(execResultList);
                if (StringUtils.isNotBlank(lastResult)) {
                    if (execResultFuncCaseMap.containsKey(lastResult)) {
                        execResultFuncCaseMap.get(lastResult).add(funcCaseId);
                    } else {
                        execResultFuncCaseMap.put(lastResult, new ArrayList<>() {{
                            this.add(funcCaseId);
                        }});
                    }
                }
            });
            execResultFuncCaseMap.forEach((result, funcCaseIds) -> {
                TestPlanFunctionalCaseExample updateExample = new TestPlanFunctionalCaseExample();
                updateExample.createCriteria().andTestPlanIdEqualTo(testPlanReport.getTestPlanId()).andFunctionalCaseIdIn(funcCaseIds);
                TestPlanFunctionalCase updateRecord = new TestPlanFunctionalCase();
                updateRecord.setLastExecResult(result);
                testPlanFunctionalCaseMapper.updateByExampleSelective(updateRecord, updateExample);
                // 报告-功能用例的执行结果同步刷新
                TestPlanReportFunctionCaseExample caseExample = new TestPlanReportFunctionCaseExample();
                caseExample.createCriteria().andTestPlanReportIdEqualTo(testPlanReportId).andFunctionCaseIdIn(funcCaseIds);
                TestPlanReportFunctionCase reportFunctionCase = new TestPlanReportFunctionCase();
                reportFunctionCase.setFunctionCaseExecuteResult(result);
                reportFunctionCase.setFunctionCaseExecuteReportId(null);
                testPlanReportFunctionCaseMapper.updateByExampleSelective(reportFunctionCase, caseExample);
            });
        }
    }

    /**
     * 创建测试计划
     */
    public TestPlan add(TestPlanCreateRequest testPlanCreateRequest, String operator, String requestUrl, String requestMethod) {
        TestPlan testPlan = savePlanDTO(testPlanCreateRequest, operator);
        //自动生成测试规划
        this.initDefaultPlanCollection(testPlan.getId(), operator);

        testPlanLogService.saveAddLog(testPlan, operator, requestUrl, requestMethod);
        return testPlan;
    }


    /**
     * 保存数据
     *
     * @param createOrCopyRequest
     * @param operator
     */
    private TestPlan savePlanDTO(TestPlanCreateRequest createOrCopyRequest, String operator) {
        //检查模块的合法性
        checkModule(createOrCopyRequest.getModuleId());
        if (StringUtils.equalsIgnoreCase(createOrCopyRequest.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)
                && !StringUtils.equalsIgnoreCase(createOrCopyRequest.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            throw new MSException(Translator.get("test_plan.group.error"));
        }

        TestPlan createTestPlan = new TestPlan();
        BeanUtils.copyBean(createTestPlan, createOrCopyRequest);
        createTestPlan.setTags(ServiceUtils.parseTags(createTestPlan.getTags()));
        if (!StringUtils.equals(createTestPlan.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            TestPlan groupPlan = testPlanGroupService.validateGroupCapacity(createTestPlan.getGroupId(), 1);
            // 判断测试计划组是否存在
            createTestPlan.setModuleId(groupPlan.getModuleId());
        }

        initTestPlanPos(createTestPlan);

        createTestPlan.setId(IDGenerator.nextStr());
        long operateTime = System.currentTimeMillis();
        createTestPlan.setNum(NumGenerator.nextNum(createOrCopyRequest.getProjectId(), ApplicationNumScope.TEST_PLAN));
        createTestPlan.setCreateUser(operator);
        createTestPlan.setUpdateUser(operator);
        createTestPlan.setCreateTime(operateTime);
        createTestPlan.setUpdateTime(operateTime);
        createTestPlan.setStatus(TestPlanConstants.TEST_PLAN_STATUS_NOT_ARCHIVED);

        TestPlanConfig testPlanConfig = new TestPlanConfig();
        testPlanConfig.setTestPlanId(createTestPlan.getId());
        testPlanConfig.setAutomaticStatusUpdate(createOrCopyRequest.isAutomaticStatusUpdate());
        testPlanConfig.setRepeatCase(createOrCopyRequest.isRepeatCase());
        testPlanConfig.setPassThreshold(createOrCopyRequest.getPassThreshold());

        testPlanMapper.insert(createTestPlan);
        testPlanConfigMapper.insertSelective(testPlanConfig);
        return createTestPlan;
    }


    //校验测试计划
    private void initTestPlanPos(TestPlan createTestPlan) {
        if (StringUtils.equals(createTestPlan.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            createTestPlan.setPos(this.getNextOrder(createTestPlan.getProjectId(), createTestPlan.getGroupId()));
        } else {
            createTestPlan.setPos(testPlanGroupService.getNextOrder(createTestPlan.getGroupId()));
        }
    }

    public long getNextOrder(String projectId, String groupId) {
        long maxPos = extTestPlanMapper.selectMaxPosByProjectIdAndGroupId(projectId, groupId);
        return maxPos + ServiceUtils.POS_STEP;
    }

    /**
     * 删除测试计划
     */
    public void delete(String id, String operator, String requestUrl, String requestMethod) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(id);
        if (StringUtils.equals(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
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
            assert testPlanReportService != null;
            BatchProcessUtils.consumerByString(testPlanGroupIds, (deleteGroupIds) -> {
                /*
                 * 计划组删除逻辑{第一版需求: 删除组, 组下的子计划Group置为None}:
                 * 1. 查询计划组下的全部子计划并删除(级联删除这些子计划的关联资源)
                 */
                TestPlanExample testPlanExample = new TestPlanExample();
                testPlanExample.createCriteria().andGroupIdIn(deleteGroupIds);
                List<TestPlan> deleteGroupPlans = testPlanMapper.selectByExample(testPlanExample);
                List<String> deleteGroupPlanIds = deleteGroupPlans.stream().map(TestPlan::getId).toList();
                List<String> allDeleteIds = ListUtils.union(deleteGroupIds, deleteGroupPlanIds);
                if (CollectionUtils.isNotEmpty(allDeleteIds)) {
                    // 级联删除子计划关联的资源(计划组不存在关联的资源,但是存在报告)
                    this.cascadeDeleteTestPlanIds(allDeleteIds, testPlanReportService);
                    testPlanExample.clear();
                    testPlanExample.createCriteria().andIdIn(allDeleteIds);
                    testPlanMapper.deleteByExample(testPlanExample);
                }
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
                testPlanSendNoticeService.batchSendNotice(request.getProjectId(), deleteIdList, userMapper.selectByPrimaryKey(operator), NoticeConstants.Event.DELETE);
                this.deleteByList(testPlanIdList);
                // 计划组的删除暂时预留
                this.deleteGroupByList(testPlanGroupList);
                //记录日志
                testPlanLogService.saveBatchLog(deleteTestPlanList, operator, requestUrl, requestMethod, OperationLogType.DELETE.name());
            }
        }
    }


    /**
     * 级联删除计划关联的资源
     *
     * @param testPlanIds           计划ID集合
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

        TestPlanCaseExecuteHistoryExample historyExample = new TestPlanCaseExecuteHistoryExample();
        historyExample.createCriteria().andTestPlanIdIn(testPlanIds);
        testPlanCaseExecuteHistoryMapper.deleteByExample(historyExample);

        //删除测试计划报告
        testPlanReportService.deleteByTestPlanIds(testPlanIds);
        //删除定时任务
        scheduleService.deleteByResourceIds(testPlanIds, TestPlanScheduleJob.class.getName());
    }


    /**
     * 更新测试计划
     */
    public TestPlan update(TestPlanUpdateRequest request, String userId, String requestUrl, String requestMethod) {
        this.checkTestPlanNotArchived(request.getId());
        TestPlan originalTestPlan = testPlanMapper.selectByPrimaryKey(request.getId());

        TestPlan updateTestPlan = new TestPlan();
        updateTestPlan.setId(request.getId());
        updateTestPlan.setName(request.getName());
        updateTestPlan.setPlannedStartTime(request.getPlannedStartTime());
        updateTestPlan.setPlannedEndTime(request.getPlannedEndTime());
        updateTestPlan.setDescription(request.getDescription());
        //判断有没有用户组的变化
        if (StringUtils.isNotBlank(request.getGroupId()) && !StringUtils.equalsIgnoreCase(request.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            if (!StringUtils.equalsIgnoreCase(originalTestPlan.getGroupId(), request.getGroupId())) {
                //用户更换了测试计划组
                TestPlan testPlanGroup = testPlanGroupService.validateGroupCapacity(request.getGroupId(), 1);
                updateTestPlan.setGroupId(testPlanGroup.getId());
                this.deleteScheduleConfig(request.getId());
                updateTestPlan.setPos(testPlanGroupService.getNextOrder(request.getGroupId()));
                updateTestPlan.setModuleId(testPlanGroup.getModuleId());
            }
        } else {
            updateTestPlan.setGroupId(TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID);
            if (!StringUtils.equalsIgnoreCase(originalTestPlan.getGroupId(), request.getGroupId())) {
                //移出了测试计划组
                this.deleteScheduleConfig(request.getId());
                updateTestPlan.setPos(testPlanGroupService.getNextOrder(updateTestPlan.getGroupId()));
            }
        }
        //判断有没有模块的变化
        if (StringUtils.isNotBlank(request.getModuleId())) {
            if (!StringUtils.equalsIgnoreCase(request.getModuleId(), originalTestPlan.getModuleId())) {
                //检查模块的合法性
                checkModule(request.getModuleId());
                updateTestPlan.setModuleId(request.getModuleId());
            }
        }

        //判断标签的变化
        if (CollectionUtils.isNotEmpty(request.getTags())) {
            List<String> tags = new ArrayList<>(request.getTags());
            updateTestPlan.setTags(ServiceUtils.parseTags(tags));
        } else {
            updateTestPlan.setTags(new ArrayList<>());
        }
        testPlanMapper.updateByPrimaryKeySelective(updateTestPlan);
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(updateTestPlan.getId());

        if (!ObjectUtils.allNull(request.getAutomaticStatusUpdate(), request.getRepeatCase(), request.getPassThreshold(), request.getTestPlanning())) {
            TestPlanConfig testPlanConfig = new TestPlanConfig();
            testPlanConfig.setTestPlanId(request.getId());
            testPlanConfig.setAutomaticStatusUpdate(request.getAutomaticStatusUpdate());
            testPlanConfig.setRepeatCase(request.getRepeatCase());
            testPlanConfig.setPassThreshold(request.getPassThreshold());
            testPlanConfigMapper.updateByPrimaryKeySelective(testPlanConfig);
        }

        testPlanLogService.saveUpdateLog(testPlan, testPlanMapper.selectByPrimaryKey(request.getId()), testPlan.getProjectId(), userId, requestUrl, requestMethod);
        return updateTestPlan;
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
     */
    public void archived(String id, String userId) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(id);

        if (StringUtils.equalsAnyIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            //判断当前计划组下是否都已完成 (由于算法原因，只需要校验当前测试计划组即可）
            if (!this.isTestPlanCompleted(id)) {
                throw new MSException(Translator.get("test_plan.group.not_plan"));
            }
            //测试计划组归档
            updateCompletedGroupStatus(testPlan.getId(), userId);
            //关闭定时任务
            this.deleteScheduleConfig(testPlan.getId());
        } else if (this.isTestPlanCompleted(id) && StringUtils.equalsIgnoreCase(testPlan.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            //测试计划
            testPlan.setStatus(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED);
            testPlan.setUpdateUser(userId);
            testPlan.setUpdateTime(System.currentTimeMillis());
            testPlanMapper.updateByPrimaryKeySelective(testPlan);
            //关闭定时任务
            this.deleteScheduleConfig(testPlan.getId());
        } else {
            throw new MSException(Translator.get("test_plan.cannot.archived"));
        }
    }

    /**
     * 测试计划归档
     */
    public boolean archived(TestPlan testPlan, String userId) {
        if (!this.isTestPlanCompleted(testPlan.getId())) {
            return false;
        }
        if (StringUtils.equalsAnyIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            //测试计划组归档
            updateCompletedGroupStatus(testPlan.getId(), userId);
            //关闭定时任务
            this.deleteScheduleConfig(testPlan.getId());
            return true;
        } else if (StringUtils.equalsIgnoreCase(testPlan.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            //测试计划
            testPlan.setStatus(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED);
            testPlan.setUpdateUser(userId);
            testPlan.setUpdateTime(System.currentTimeMillis());
            testPlanMapper.updateByPrimaryKeySelective(testPlan);
            //关闭定时任务
            this.deleteScheduleConfig(testPlan.getId());
            return true;
        }
        return false;
    }

    public boolean isTestPlanCompleted(String testPlanId) {
        TestPlanStatisticsResponse statisticsResponse = testPlanStatisticsService.calculateRate(Collections.singletonList(testPlanId)).getFirst();
        return StringUtils.equalsIgnoreCase(statisticsResponse.getStatus(), TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED);
    }

    /**
     * 批量归档
     *
     * @param request     批量请求参数
     * @param currentUser 当前用户
     */
    public void batchArchived(TestPlanBatchProcessRequest request, String currentUser) {
        List<String> batchArchivedIds = request.getSelectIds();
        if (CollectionUtils.isNotEmpty(batchArchivedIds)) {
            TestPlanExample example = new TestPlanExample();
            example.createCriteria().andIdIn(batchArchivedIds).andStatusNotEqualTo(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED);
            List<TestPlan> testPlanList = testPlanMapper.selectByExample(example).stream().filter(
                    testPlan -> StringUtils.equalsAnyIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)
                            || (StringUtils.equalsIgnoreCase(testPlan.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID))
            ).toList();
            List<TestPlan> archivedPlanGroupList = new ArrayList<>();
            testPlanList.forEach(item -> {
                boolean result = this.archived(item, currentUser);
                if (result) {
                    archivedPlanGroupList.add(item);
                }
            });
            //日志
            testPlanLogService.saveBatchLog(archivedPlanGroupList, currentUser, "/test-plan/batch-archived", HttpMethodConstants.POST.name(), OperationLogType.ARCHIVED.name());
        }
    }

    /**
     * 测试计划组归档
     *
     * @param id
     * @param userId
     */
    private void updateCompletedGroupStatus(String id, String userId) {
        TestPlanExample example = new TestPlanExample();
        example.createCriteria().andGroupIdEqualTo(id);
        List<TestPlan> testPlanList = testPlanMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(testPlanList)) {
            throw new MSException(Translator.get("test_plan.group.not_plan"));
        }
        List<String> ids = testPlanList.stream().map(TestPlan::getId).collect(Collectors.toList());
        ids.add(id);
        extTestPlanMapper.batchUpdateStatus(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED, userId, System.currentTimeMillis(), ids);
    }

    /**
     * 复制测试计划
     */
    public String copy(String testPlanId, String userId) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(testPlanId);
        TestPlan copyPlan = null;
        if (StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            copyPlan = testPlanBatchOperationService.copyPlanGroup(testPlan, testPlan.getModuleId(), ModuleConstants.NODE_TYPE_DEFAULT, System.currentTimeMillis(), userId);
        } else {
            copyPlan = testPlanBatchOperationService.copyPlan(testPlan, testPlan.getGroupId(), TestPlanConstants.TEST_PLAN_TYPE_GROUP, System.currentTimeMillis(), userId);
        }
        testPlanLogService.copyLog(copyPlan, userId);
        return copyPlan.getId();
    }

    /**
     * 获取单个测试计划或测试计划组详情（用于编辑）
     *
     * @param id 计划ID
     * @return 计划的详情数据
     */
    public TestPlanDetailResponse detail(String id, String userId) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(id);
        TestPlanDetailResponse response = new TestPlanDetailResponse();

        String moduleName = Translator.get("unplanned.plan");
        if (!ModuleConstants.DEFAULT_NODE_ID.equals(testPlan.getModuleId())) {
            TestPlanModule module = testPlanModuleMapper.selectByPrimaryKey(testPlan.getModuleId());
            moduleName = module == null ? Translator.get("unplanned.plan") : module.getName();
            response.setModuleId(module == null ? ModuleConstants.DEFAULT_NODE_ID : module.getId());
        } else {
            response.setModuleId(ModuleConstants.DEFAULT_NODE_ID);
        }

        //计划组只有几个参数
        response.setId(testPlan.getId());
        response.setNum(testPlan.getNum());
        response.setStatus(testPlan.getStatus());
        response.setName(testPlan.getName());
        response.setTags(testPlan.getTags());
        response.setModuleName(moduleName);
        response.setDescription(testPlan.getDescription());
        response.setType(testPlan.getType());
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

        //是否关注计划
        Boolean isFollow = checkIsFollowCase(id, userId);
        response.setFollowFlag(isFollow);

        // 是否计划已初始化默认测试集
        TestPlanCollectionExample collectionExample = new TestPlanCollectionExample();
        collectionExample.createCriteria().andTestPlanIdEqualTo(id);
        if (testPlanCollectionMapper.countByExample(collectionExample) == 0) {
            List<TestPlanCollectionDTO> collections = initDefaultPlanCollection(id, userId);
            TestPlanService testPlanService = CommonBeanFactory.getBean(TestPlanService.class);
            testPlanService.initResourceDefaultCollection(id, collections);
        }
        return response;
    }


    /**
     * 关联的资源初始化默认测试集
     *
     * @param planId         计划ID
     * @param allCollections 测试集
     */
    @Async
    public void initResourceDefaultCollection(String planId, List<TestPlanCollectionDTO> allCollections) {
        // 批处理旧数据
        List<TestPlanCollectionDTO> defaultCollections = new ArrayList<>();
        allCollections.forEach(allCollection -> defaultCollections.addAll(allCollection.getChildren()));
        Map<String, TestPlanResourceService> beansOfType = applicationContext.getBeansOfType(TestPlanResourceService.class);
        beansOfType.forEach((k, v) -> v.initResourceDefaultCollection(planId, defaultCollections));
    }


    private Boolean checkIsFollowCase(String testPlanId, String userId) {
        TestPlanFollowerExample example = new TestPlanFollowerExample();
        example.createCriteria().andTestPlanIdEqualTo(testPlanId).andUserIdEqualTo(userId);
        return testPlanFollowerMapper.countByExample(example) > 0;
    }

    private void getOtherConfig(TestPlanDetailResponse response, TestPlan testPlan) {
        TestPlanConfig testPlanConfig = testPlanConfigMapper.selectByPrimaryKey(testPlan.getId());
        response.setAutomaticStatusUpdate(testPlanConfig.getAutomaticStatusUpdate());
        response.setRepeatCase(testPlanConfig.getRepeatCase());
        response.setPassThreshold(testPlanConfig.getPassThreshold());
    }

    private void getGroupName(TestPlanDetailResponse response, TestPlan testPlan) {
        if (!StringUtils.equalsIgnoreCase(testPlan.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            TestPlan group = testPlanMapper.selectByPrimaryKey(testPlan.getGroupId());
            response.setGroupId(testPlan.getGroupId());
            response.setGroupName(group.getName());
        }
    }
    

    /**
     * 批量复制 （计划/计划组）
     *
     * @param request 批量请求参数
     * @param userId  当前登录用户
     * @param url     请求URL
     * @param method  请求方法
     */
    public long batchCopy(TestPlanBatchRequest request, String userId, String url, String method) {
        // 目前计划的批量操作不支持全选所有页
        List<String> copyIds = request.getSelectIds();
        long copyCount = 0;

        if (CollectionUtils.isNotEmpty(copyIds)) {
            TestPlanExample example = new TestPlanExample();
            example.createCriteria().andIdIn(copyIds);
            List<TestPlan> originalTestPlanList = testPlanMapper.selectByExample(example);

            //批量复制时，不允许存在测试计划组下的测试计划。
            originalTestPlanList = originalTestPlanList.stream().filter(item -> StringUtils.equalsIgnoreCase(item.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID))
                    .collect(Collectors.toList());
            //日志
            if (CollectionUtils.isNotEmpty(originalTestPlanList)) {
                List<TestPlan> copyPlanList = testPlanBatchOperationService.batchCopy(originalTestPlanList, request.getTargetId(), request.getMoveType(), userId);
                copyCount = copyPlanList.size();
                testPlanLogService.saveBatchLog(copyPlanList, userId, url, method, OperationLogType.ADD.name());
            }

        }
        return copyCount;
    }

    /**
     * 批量移动 (计划/计划组)
     *
     * @param request      批量请求参数
     * @param userId       当前登录用户
     * @param operationUrl 请求URL
     * @param method       请求方法
     */
    public long batchMove(TestPlanBatchRequest request, String userId, String operationUrl, String method) {
        // 目前计划的批量操作不支持全选所有页
        List<String> moveIds = request.getSelectIds();

        long moveCount = 0;
        if (CollectionUtils.isNotEmpty(moveIds)) {
            TestPlanExample example = new TestPlanExample();
            example.createCriteria().andIdIn(moveIds);
            List<TestPlan> moveTestPlanList = testPlanMapper.selectByExample(example);

            //判断移动的是测试计划组还是模块
            if (StringUtils.equalsIgnoreCase(request.getMoveType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
                moveTestPlanList = moveTestPlanList.stream().filter(
                        item -> StringUtils.equalsIgnoreCase(item.getType(), TestPlanConstants.TEST_PLAN_TYPE_PLAN) && !StringUtils.equalsIgnoreCase(item.getGroupId(), request.getTargetId())
                ).collect(Collectors.toList());
                if (!StringUtils.equalsIgnoreCase(request.getTargetId(), TestPlanConstants.DEFAULT_PARENT_ID)) {
                    testPlanGroupService.validateGroupCapacity(request.getTargetId(), moveTestPlanList.size());
                }
                moveCount = testPlanBatchOperationService.batchMoveGroup(moveTestPlanList, request.getTargetId(), userId);
                //移动到计划组后要删除定时任务
                moveTestPlanList.forEach(item -> this.deleteScheduleConfig(item.getId()));
            } else {
                moveCount = testPlanBatchOperationService.batchMoveModule(moveTestPlanList, request.getTargetId(), userId);
            }
            //日志
            if (CollectionUtils.isNotEmpty(moveTestPlanList)) {
                testPlanLogService.saveBatchLog(moveTestPlanList, userId, operationUrl, method, OperationLogType.UPDATE.name());
            }
        }
        return moveCount;
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
                //默认编辑tags
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
                        testPlan.setTags(ServiceUtils.parseTags(tags));
                    } else {
                        testPlan.setTags(ServiceUtils.parseTags(request.getTags()));
                    }
                    testPlan.setId(id);
                    testPlan.setUpdateTime(System.currentTimeMillis());
                    testPlan.setUpdateUser(userId);
                    testPlanMapper.updateByPrimaryKeySelective(testPlan);
                });
                sqlSession.flushStatements();
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            } else  {
                //替换标签
                TestPlan testPlan = new TestPlan();
                testPlan.setTags(request.isClear() ? new ArrayList<>() : ServiceUtils.parseTags(request.getTags()));
                testPlan.setProjectId(request.getProjectId());
                testPlan.setUpdateTime(System.currentTimeMillis());
                testPlan.setUpdateUser(userId);
                extTestPlanMapper.batchUpdate(testPlan, ids);
            }
        } else if (request.isClear()){
            //替换标签
            TestPlan testPlan = new TestPlan();
            testPlan.setTags(new ArrayList<>());
            testPlan.setProjectId(request.getProjectId());
            testPlan.setUpdateTime(System.currentTimeMillis());
            testPlan.setUpdateUser(userId);
            extTestPlanMapper.batchUpdate(testPlan, ids);
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

    // 过滤掉已归档的测试计划
    public void filterArchivedIds(TestPlanBatchProcessRequest request) {
        if (CollectionUtils.isNotEmpty(request.getSelectIds())) {
            request.setSelectIds(extTestPlanMapper.selectNotArchivedIds(request.getSelectIds()));
        }
    }

    public void checkTestPlanNotArchived(String testPlanId) {
        TestPlanExample example = new TestPlanExample();
        example.createCriteria().andIdEqualTo(testPlanId).andStatusEqualTo(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED);
        if (testPlanMapper.countByExample(example) > 0) {
            throw new MSException(Translator.get("test_plan.is.archived"));
        }
    }

    @Autowired
    private ApplicationContext applicationContext;

    public void setActualStartTime(String testPlanId) {
        long actualStartTime = System.currentTimeMillis();
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(testPlanId);
        if (testPlan != null) {
            extTestPlanMapper.setActualStartTime(testPlan.getId(), actualStartTime);
            if (!StringUtils.equalsIgnoreCase(testPlan.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
                extTestPlanMapper.setActualStartTime(testPlan.getGroupId(), actualStartTime);
            }
        }
    }

    public TestPlanOperationResponse sort(PosRequest request, LogInsertModule logInsertModule) {
        testPlanGroupService.sort(request);
        testPlanLogService.saveMoveLog(testPlanMapper.selectByPrimaryKey(request.getMoveId()), request.getMoveId(), logInsertModule);
        return new TestPlanOperationResponse(1);
    }

    public void deleteScheduleConfig(String testPlanId) {
        scheduleService.deleteByResourceId(testPlanId, TestPlanScheduleJob.getJobKey(testPlanId), TestPlanScheduleJob.getTriggerKey(testPlanId));

        //判断是不是测试计划组
        TestPlanExample testPlanExample = new TestPlanExample();
        testPlanExample.createCriteria().andGroupIdEqualTo(testPlanId);
        testPlanMapper.selectByExample(testPlanExample).forEach(
                item -> scheduleService.deleteByResourceId(testPlanId, TestPlanScheduleJob.getJobKey(testPlanId), TestPlanScheduleJob.getTriggerKey(testPlanId)));
    }

    public List<TestPlanExecuteHisDTO> listHis(TestPlanExecuteHisPageRequest request) {
        List<TestPlanExecuteHisDTO> hisList = extTestPlanMapper.listHis(request);
        if (CollectionUtils.isEmpty(hisList)) {
            return new ArrayList<>();
        }
        List<String> userIds = hisList.stream().map(TestPlanExecuteHisDTO::getOperationUser).distinct().toList();
        List<OptionDTO> userOptions = baseUserMapper.selectUserOptionByIds(userIds);
        Map<String, String> userMap = userOptions.stream().collect(Collectors.toMap(OptionDTO::getId, OptionDTO::getName));
        hisList.forEach(his -> {
            his.setOperationUser(userMap.getOrDefault(his.getOperationUser(), his.getOperationUser()));
            his.setResultDeleted(StringUtils.isEmpty(his.getReportId()));
        });
        return hisList;
    }

    //只查找未归档的测试计划组以及游离态测试计划
    public List<String> selectRightfulIds(List<String> executeIds) {
        TestPlanExample testPlanExample = new TestPlanExample();
        testPlanExample.createCriteria().andIdIn(executeIds).andStatusNotEqualTo(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED);
        Map<String, TestPlan> testPlanIdMap = testPlanMapper.selectByExample(testPlanExample)
                .stream().collect(Collectors.toMap(TestPlan::getId, v -> v));
        List<String> returnIds = new ArrayList<>();
        for (String executeId : executeIds) {
            TestPlan testPlan = testPlanIdMap.get(executeId);
            if (testPlan != null &&
                    (StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)
                            || StringUtils.equalsIgnoreCase(testPlan.getGroupId(), TestPlanConstants.DEFAULT_PARENT_ID))) {
                returnIds.add(executeId);
            }
        }
        return returnIds;
    }

    public List<TestPlan> selectNotArchivedChildren(String testPlanGroupId) {
        TestPlanExample example = new TestPlanExample();
        example.createCriteria().andGroupIdEqualTo(testPlanGroupId).andStatusNotEqualTo(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED);
        example.setOrderByClause("pos desc");
        return testPlanMapper.selectByExample(example);
    }

    /**
     * 初始化测试规划默认节点
     *
     * @param planId      计划ID
     * @param currentUser 当前用户
     */
    public List<TestPlanCollectionDTO> initDefaultPlanCollection(String planId, String currentUser) {
        List<TestPlanCollectionDTO> collectionDTOS = new ArrayList<>();
        // 获取项目下默认资源池
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(planId);
        ProjectApplicationRequest projectApplicationRequest = new ProjectApplicationRequest();
        projectApplicationRequest.setProjectId(testPlan.getProjectId());
        projectApplicationRequest.setType("apiTest");
        Map<String, Object> configMap = projectApplicationService.get(projectApplicationRequest, Arrays.stream(ProjectApplicationType.API.values()).map(ProjectApplicationType.API::name).collect(Collectors.toList()));

        // 批量插入测试集
        List<TestPlanCollection> collections = new ArrayList<>();
        TestPlanCollection defaultCollection = new TestPlanCollection();
        defaultCollection.setTestPlanId(planId);
        defaultCollection.setExecuteMethod(ExecuteMethod.SERIAL.name());
        defaultCollection.setExtended(true);
        defaultCollection.setGrouped(false);
        defaultCollection.setEnvironmentId("NONE");
        defaultCollection.setTestResourcePoolId(configMap.getOrDefault(ProjectApplicationType.API.API_RESOURCE_POOL_ID.name(), StringUtils.EMPTY).toString());
        defaultCollection.setRetryOnFail(false);
        defaultCollection.setStopOnFail(false);
        defaultCollection.setCreateUser(currentUser);
        defaultCollection.setCreateTime(System.currentTimeMillis());
        long initPos = 1L;
        for (CaseType caseType : CaseType.values()) {
            // 测试集分类
            TestPlanCollectionDTO parentCollectionDTO = new TestPlanCollectionDTO();
            TestPlanCollection parentCollection = new TestPlanCollection();
            BeanUtils.copyBean(parentCollection, defaultCollection);
            parentCollection.setId(IDGenerator.nextStr());
            parentCollection.setParentId(TestPlanConstants.DEFAULT_PARENT_ID);
            parentCollection.setName(caseType.getType());
            parentCollection.setType(caseType.getKey());
            parentCollection.setPos(initPos << 12);
            collections.add(parentCollection);
            BeanUtils.copyBean(parentCollectionDTO, parentCollection);
            // 测试集
            TestPlanCollectionDTO childCollectionDTO = new TestPlanCollectionDTO();
            TestPlanCollection childCollection = new TestPlanCollection();
            BeanUtils.copyBean(childCollection, defaultCollection);
            childCollection.setId(IDGenerator.nextStr());
            childCollection.setParentId(parentCollection.getId());
            childCollection.setName(caseType.getPlanDefaultCollection());
            childCollection.setType(caseType.getKey());
            childCollection.setPos(1L << 12);
            collections.add(childCollection);
            BeanUtils.copyBean(childCollectionDTO, childCollection);
            parentCollectionDTO.setChildren(List.of(childCollectionDTO));
            // 更新pos
            initPos++;

            collectionDTOS.add(parentCollectionDTO);
        }
        testPlanCollectionMapper.batchInsert(collections);

        return collectionDTOS;
    }

    /**
     * 删除测试集资源 (删除测试集及所关联的关联用例)
     */
    public void deletePlanCollectionResource(List<String> collectionIds) {
        TestPlanCollectionExample example = new TestPlanCollectionExample();
        example.createCriteria().andIdIn(collectionIds);
        List<TestPlanCollection> collections = testPlanCollectionMapper.selectByExample(example);
        List<TestPlanCollection> functionalCollections = collections.stream().filter(collection -> StringUtils.equals(collection.getType(), CaseType.FUNCTIONAL_CASE.getKey())).toList();
        List<TestPlanCollection> apiCollections = collections.stream().filter(collection -> StringUtils.equals(collection.getType(), CaseType.API_CASE.getKey())).toList();
        List<TestPlanCollection> scenarioCollections = collections.stream().filter(collection -> StringUtils.equals(collection.getType(), CaseType.SCENARIO_CASE.getKey())).toList();
        if (CollectionUtils.isNotEmpty(functionalCollections)) {
            // 删除{计划集-功能用例}关系
            TestPlanFunctionalCaseExample functionalCaseExample = new TestPlanFunctionalCaseExample();
            functionalCaseExample.createCriteria().andTestPlanCollectionIdIn(functionalCollections.stream().map(TestPlanCollection::getId).toList());
            testPlanFunctionalCaseMapper.deleteByExample(functionalCaseExample);
        }
        if (CollectionUtils.isNotEmpty(apiCollections)) {
            // 删除{计划集-接口用例}关系
            TestPlanApiCaseExample apiCaseExample = new TestPlanApiCaseExample();
            apiCaseExample.createCriteria().andTestPlanCollectionIdIn(apiCollections.stream().map(TestPlanCollection::getId).toList());
            testPlanApiCaseMapper.deleteByExample(apiCaseExample);
        }
        if (CollectionUtils.isNotEmpty(scenarioCollections)) {
            // 删除{计划集-场景用例}关系
            TestPlanApiScenarioExample scenarioExample = new TestPlanApiScenarioExample();
            scenarioExample.createCriteria().andTestPlanCollectionIdIn(scenarioCollections.stream().map(TestPlanCollection::getId).toList());
            testPlanApiScenarioMapper.deleteByExample(scenarioExample);
        }

        // 删除测试集
        testPlanCollectionMapper.deleteByExample(example);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NOT_SUPPORTED)
    public void setExecuteConfig(String testPlanId, String testPlanReportId) {
        this.setActualStartTime(testPlanId);
        if (StringUtils.isNotBlank(testPlanReportId)) {
            testPlanReportService.updateExecuteTimeAndStatus(testPlanReportId);
        }
    }

    public TestPlanCoverageDTO rageByProjectIdAndTimestamp(@NotBlank String projectId) {
        TestPlanCoverageDTO returnDTO = new TestPlanCoverageDTO();
        TestPlanExample testPlanExample = new TestPlanExample();
        testPlanExample.createCriteria().andProjectIdEqualTo(projectId).andTypeEqualTo(TestPlanConstants.TEST_PLAN_TYPE_PLAN);
        List<TestPlan> testPlanList = extTestPlanMapper.selectIdAndStatusByProjectIdAndCreateTimeRangeAndType(projectId, null, null, TestPlanConstants.TEST_PLAN_TYPE_PLAN);

        Map<String, String> testPlanStatusMap = new HashMap<>();

        testPlanList.forEach(item -> testPlanStatusMap.put(item.getId(), item.getStatus()));
        List<String> testPlanIds = new ArrayList<>(testPlanStatusMap.keySet());

        // 将当前项目下未归档的测试计划结果查询出来，进行下列符合条件的筛选
        Map<String, TestPlanResourceService> beansOfType = applicationContext.getBeansOfType(TestPlanResourceService.class);

        // 批量处理
        SubListUtils.dealForSubList(testPlanIds, SubListUtils.DEFAULT_BATCH_SIZE, dealList -> {

            TestPlanConfigExample configExample = new TestPlanConfigExample();
            configExample.createCriteria().andTestPlanIdIn(dealList);
            List<TestPlanConfig> testPlanConfigList = testPlanConfigMapper.selectByExample(configExample);
            Map<String, TestPlanConfig> testPlanConfigMap = testPlanConfigList.stream().collect(Collectors.toMap(TestPlanConfig::getTestPlanId, item -> item));

            List<TestPlanResourceExecResultDTO> execResults = new ArrayList<>();
            beansOfType.forEach((k, v) -> execResults.addAll(v.selectLastExecResultByTestPlanIds(dealList)));
            Map<String, List<String>> testPlanExecResultMap = execResults.stream().collect(
                    Collectors.groupingBy(TestPlanResourceExecResultDTO::getTestPlanId, Collectors.mapping(TestPlanResourceExecResultDTO::getExecResult, Collectors.toList())));

            for (String testPlanId : dealList) {
                boolean isArchived = StringUtils.equalsIgnoreCase(testPlanStatusMap.get(testPlanId), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED);
                List<String> executeResultList = testPlanExecResultMap.get(testPlanId);
                TestPlanConfig testPlanConfig = testPlanConfigMap.get(testPlanId);
                double executeRage = testPlanConfig == null ? 100
                        : testPlanConfig.getPassThreshold() == null ? 100 : testPlanConfig.getPassThreshold();
                if (CollectionUtils.isEmpty(executeResultList)) {
                    // 未运行
                    returnDTO.notStartedAutoIncrement(isArchived);
                } else {
                    double passphrase = CalculateUtils.percentage(
                            executeResultList.stream().filter(result -> StringUtils.equalsIgnoreCase(result, ResultStatus.SUCCESS.name())).toList().size(),
                            executeResultList.size()
                    );

                    List<String> calculateList = executeResultList.stream().distinct().toList();
                    //目前只有三个状态。如果同时包含多种状态(进行中/未开始、进行中/已完成、已完成/未开始、进行中/未开始/已完成),根据算法可得测试计划都会是进行中
                    if (calculateList.size() == 1) {
                        if (calculateList.contains(ResultStatus.SUCCESS.name())) {
                            returnDTO.passAndFinishedAutoIncrement(isArchived);
                        } else if (calculateList.contains(ExecStatus.PENDING.name())) {
                            returnDTO.notStartedAutoIncrement(isArchived);
                        } else {
                            returnDTO.unSuccessAutoIncrement(isArchived);
                        }
                    } else {
                        if (passphrase >= executeRage) {
                            if (calculateList.contains(ExecStatus.PENDING.name())) {
                                // 通过却未完成
                                returnDTO.passAndNotFinishedAutoIncrement(isArchived);
                            } else {
                                // 通过且完成
                                returnDTO.passAndFinishedAutoIncrement(isArchived);
                            }
                        } else if (calculateList.contains(ExecStatus.PENDING.name())) {
                            // 存在还未完成的用例，测试计划为进行中
                            returnDTO.testPlanRunningAutoIncrement(isArchived);
                        } else {
                            returnDTO.unSuccessAutoIncrement(isArchived);
                        }
                    }
                }

            }
        });
        return returnDTO;
    }
}
