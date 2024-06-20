package io.metersphere.plan.service;

import io.metersphere.bug.dto.response.BugDTO;
import io.metersphere.bug.service.BugCommonService;
import io.metersphere.plan.constants.AssociateCaseType;
import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.*;
import io.metersphere.plan.dto.request.*;
import io.metersphere.plan.dto.response.TestPlanReportDetailResponse;
import io.metersphere.plan.dto.response.TestPlanReportPageResponse;
import io.metersphere.plan.enums.TestPlanReportAttachmentSourceType;
import io.metersphere.plan.mapper.*;
import io.metersphere.plan.utils.CountUtils;
import io.metersphere.plan.utils.ModuleTreeUtils;
import io.metersphere.plan.utils.RateCalculateUtils;
import io.metersphere.plugin.platform.dto.SelectOption;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.ExecStatus;
import io.metersphere.sdk.constants.ReportStatus;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileCopyRequest;
import io.metersphere.sdk.file.FileRepository;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.*;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.mapper.BaseUserMapper;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.service.SimpleUserService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TestPlanReportService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private SimpleUserService simpleUserService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private BugCommonService bugCommonService;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private TestPlanConfigMapper testPlanConfigMapper;
    @Resource
    private TestPlanReportMapper testPlanReportMapper;
    @Resource
    private ExtTestPlanReportMapper extTestPlanReportMapper;
    @Resource
    private ExtTestPlanReportBugMapper extTestPlanReportBugMapper;
    @Resource
    private ExtTestPlanReportFunctionalCaseMapper extTestPlanReportFunctionalCaseMapper;
	@Resource
	private TestPlanCaseExecuteHistoryMapper testPlanCaseExecuteHistoryMapper;
    @Resource
    private ExtTestPlanReportApiCaseMapper extTestPlanReportApiCaseMapper;
    @Resource
    private ExtTestPlanReportApiScenarioMapper extTestPlanReportApiScenarioMapper;
    @Resource
    private TestPlanReportLogService testPlanReportLogService;
    @Resource
    private TestPlanReportNoticeService testPlanReportNoticeService;
    @Resource
    private TestPlanReportSummaryMapper testPlanReportSummaryMapper;
    @Resource
    private TestPlanReportFunctionCaseMapper testPlanReportFunctionCaseMapper;
	@Resource
	private TestPlanReportApiCaseMapper testPlanReportApiCaseMapper;
	@Resource
	private TestPlanReportApiScenarioMapper testPlanReportApiScenarioMapper;
    @Resource
    private TestPlanReportBugMapper testPlanReportBugMapper;
    @Resource
    private TestPlanReportAttachmentMapper testPlanReportAttachmentMapper;
    @Resource
    private BaseUserMapper baseUserMapper;
	@Resource
	private TestPlanSendNoticeService testPlanSendNoticeService;

    /**
     * 分页查询报告列表
     *
     * @param request 分页请求参数
     * @return 报告列表
     */
    public List<TestPlanReportPageResponse> page(TestPlanReportPageRequest request) {
        List<TestPlanReportPageResponse> reportList = extTestPlanReportMapper.list(request);
        if (CollectionUtils.isEmpty(reportList)) {
            return new ArrayList<>();
        }
        List<String> distinctUserIds = reportList.stream().map(TestPlanReportPageResponse::getCreateUser).distinct().toList();
        Map<String, String> userMap = simpleUserService.getUserMapByIds(distinctUserIds);
        reportList.forEach(report -> report.setCreateUserName(userMap.get(report.getCreateUser())));
        return reportList;
    }

    /**
     * 报告重命名
     */
    public void rename(String id, String name) {
        if (name.length() > 300) {
            throw new MSException(Translator.get("test_plan_report_name_length_range"));
        }
        TestPlanReport report = checkReport(id);
        report.setName(name);
        testPlanReportMapper.updateByPrimaryKeySelective(report);
    }

    /**
     * 业务删除报告
     */
    public void setReportDelete(String id) {
        TestPlanReport report = checkReport(id);
        report.setDeleted(true);
        testPlanReportMapper.updateByPrimaryKeySelective(report);
    }

    /**
     * 批量参数报告
     *
     * @param request 请求参数
     */
    public void batchSetReportDelete(TestPlanReportBatchRequest request, String userId) {
        List<String> batchIds = getBatchIds(request);
        User user = userMapper.selectByPrimaryKey(userId);
        if (CollectionUtils.isNotEmpty(batchIds)) {
            SubListUtils.dealForSubList(batchIds, SubListUtils.DEFAULT_BATCH_SIZE, subList -> {
                TestPlanReportExample example = new TestPlanReportExample();
                example.createCriteria().andIdIn(subList);
                TestPlanReport testPlanReport = new TestPlanReport();
                testPlanReport.setDeleted(true);
                testPlanReportMapper.updateByExampleSelective(testPlanReport, example);
                testPlanReportLogService.batchDeleteLog(subList, userId, request.getProjectId());
                testPlanReportNoticeService.batchSendNotice(subList, user, request.getProjectId(), NoticeConstants.Event.DELETE);
            });
        }
    }

    /**
     * 清空测试计划报告（包括summary
     *
     * @param reportIdList 报告ID集合
     */
    public void cleanAndDeleteReport(List<String> reportIdList) {
        if (CollectionUtils.isNotEmpty(reportIdList)) {
            SubListUtils.dealForSubList(reportIdList, SubListUtils.DEFAULT_BATCH_SIZE, subList -> {
                TestPlanReportExample example = new TestPlanReportExample();
                example.createCriteria().andIdIn(subList);
                TestPlanReport testPlanReport = new TestPlanReport();
                testPlanReport.setDeleted(true);
                testPlanReportMapper.updateByExampleSelective(testPlanReport, example);

                this.deleteTestPlanReportBlobs(subList);
            });
        }
    }

    /**
     * 删除测试计划报告（包括summary
     */
    public void deleteByTestPlanIds(List<String> testPlanIds) {
        if (CollectionUtils.isNotEmpty(testPlanIds)) {
            List<String> reportIdList = extTestPlanReportMapper.selectReportIdTestPlanIds(testPlanIds);

            SubListUtils.dealForSubList(reportIdList, SubListUtils.DEFAULT_BATCH_SIZE, subList -> {
                TestPlanReportExample example = new TestPlanReportExample();
                example.createCriteria().andIdIn(subList);
                testPlanReportMapper.deleteByExample(example);

                this.deleteTestPlanReportBlobs(subList);
            });
        }
    }

    private void deleteTestPlanReportBlobs(List<String> reportIdList) {
        TestPlanReportSummaryExample summaryExample = new TestPlanReportSummaryExample();
        summaryExample.createCriteria().andTestPlanReportIdIn(reportIdList);
        testPlanReportSummaryMapper.deleteByExample(summaryExample);

        TestPlanReportFunctionCaseExample testPlanReportFunctionCaseExample = new TestPlanReportFunctionCaseExample();
        testPlanReportFunctionCaseExample.createCriteria().andTestPlanReportIdIn(reportIdList);
        testPlanReportFunctionCaseMapper.deleteByExample(testPlanReportFunctionCaseExample);

		TestPlanReportApiCaseExample testPlanReportApiCaseExample = new TestPlanReportApiCaseExample();
		testPlanReportApiCaseExample.createCriteria().andTestPlanReportIdIn(reportIdList);
		testPlanReportApiCaseMapper.deleteByExample(testPlanReportApiCaseExample);

		TestPlanReportApiScenarioExample testPlanReportApiScenarioExample = new TestPlanReportApiScenarioExample();
		testPlanReportApiScenarioExample.createCriteria().andTestPlanReportIdIn(reportIdList);
		testPlanReportApiScenarioMapper.deleteByExample(testPlanReportApiScenarioExample);

        TestPlanReportBugExample testPlanReportBugExample = new TestPlanReportBugExample();
        testPlanReportBugExample.createCriteria().andTestPlanReportIdIn(reportIdList);
        testPlanReportBugMapper.deleteByExample(testPlanReportBugExample);
    }

    /**
     * 手动生成报告 (计划 或者 组)
     *
     * @param request     请求参数
     * @param currentUser 当前用户
     */
    public void genReportByManual(TestPlanReportGenRequest request, String currentUser) {
		genReport(IDGenerator.nextStr(),request, true, currentUser, "/test-plan/report/gen");
    }

    /**
     * 执行生成报告
     * 新开事务，避免异步执行查不到数据
     *
     * @param request     请求参数
     * @param currentUser 当前用户
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public Map<String, String> genReportByExecution(String prepareReportId,TestPlanReportGenRequest request, String currentUser) {
        return genReport(prepareReportId,request, false, currentUser, "/test-plan/report/gen");
    }

    public Map<String, String> genReport(String prepareReportId,TestPlanReportGenRequest request, boolean manual, String currentUser, String logPath) {
		Map<String, String> preReportMap = new HashMap<>();
		try {
			// 所有计划
			List<TestPlan> plans = getPlans(request.getTestPlanId());
			// 模块参数
			TestPlanReportModuleParam moduleParam = getModuleParam(request.getProjectId());

			/*
			 * 1. 准备报告生成参数
			 * 2. 预生成报告
			 * 3. 汇总报告数据 {执行时跳过}
			 * 3. 报告后置处理 (计算通过率, 执行率, 执行状态...) {执行时跳过}
			 */
			List<String> childPlanIds = plans.stream().filter(plan -> StringUtils.equals(plan.getType(), TestPlanConstants.TEST_PLAN_TYPE_PLAN)).map(TestPlan::getId).toList();

            boolean isGroupReports = plans.size() > 1;
			plans.forEach(plan -> {
				request.setTestPlanId(plan.getId());
				TestPlanReportGenPreParam genPreParam = buildReportGenParam(request, plan, prepareReportId);
				if (!manual) {
					//不是手动保存的测试计划报告，不存储startTime
					genPreParam.setStartTime(null);
				}
				genPreParam.setUseManual(manual);
                //如果是测试计划的独立报告，使用参数中的预生成的报告id。否则只有测试计划组报告使用该id
                String prepareItemReportId = isGroupReports ? IDGenerator.nextStr() : prepareReportId;
				TestPlanReport preReport = preGenReport(prepareItemReportId, genPreParam, currentUser, logPath, moduleParam, childPlanIds);
				if (manual) {
					// 汇总
					if (genPreParam.getIntegrated()) {
						summaryGroupReport(preReport.getId());
					} else {
						summaryPlanReport(preReport.getId());
					}
					// 手动生成的报告, 汇总结束后直接进行后置处理
					TestPlanReportPostParam postParam = new TestPlanReportPostParam();
					postParam.setReportId(preReport.getId());
					// 手动生成报告, 执行状态为已完成, 执行及结束时间为当前时间
					postParam.setExecuteTime(System.currentTimeMillis());
					postParam.setEndTime(System.currentTimeMillis());
					postParam.setExecStatus(ExecStatus.COMPLETED.name());
					postHandleReport(postParam, true);
				}
				preReportMap.put(plan.getId(), preReport.getId());
			});
		} catch (Exception e) {
			LogUtils.error("生成报告异常: " + e.getMessage());
		}
		return preReportMap;
    }

	/**
	 * 预生成报告内容(汇总前调用)
	 * @return 报告
	 */
	public TestPlanReport preGenReport(String prepareId,TestPlanReportGenPreParam genParam, String currentUser, String logPath, TestPlanReportModuleParam moduleParam, List<String> childPlanIds) {
		// 计划配置
		TestPlanConfig config = testPlanConfigMapper.selectByPrimaryKey(genParam.getTestPlanId());

        /*
         * 预生成报告
         * 1. 生成报告用例数据, 缺陷数据
         * 2. 生成或计算报告统计数据
         */
        TestPlanReport report = new TestPlanReport();
        BeanUtils.copyBean(report, genParam);
        report.setId(genParam.getIntegrated() ? genParam.getGroupReportId() : prepareId);
        report.setName(genParam.getTestPlanName() + "-" + DateUtils.getTimeStr(System.currentTimeMillis()));
        report.setCreateUser(currentUser);
        report.setCreateTime(System.currentTimeMillis());
        report.setDeleted(false);
        report.setPassThreshold(config == null ? null : config.getPassThreshold());
        report.setParentId(genParam.getGroupReportId());
        testPlanReportMapper.insertSelective(report);

		TestPlanReportDetailCaseDTO reportCaseDetail;
		if (!genParam.getIntegrated()) {
			// 生成独立报告的关联数据
			reportCaseDetail = genReportDetail(genParam, moduleParam, report);
		} else {
			// 计划组报告暂不统计各用例类型, 汇总时再入库
			reportCaseDetail = TestPlanReportDetailCaseDTO.builder().build();
		}
		// 报告统计内容
		TestPlanReportSummary reportSummary = new TestPlanReportSummary();
		reportSummary.setId(IDGenerator.nextStr());
		reportSummary.setTestPlanReportId(report.getId());
		reportSummary.setFunctionalCaseCount((long) (CollectionUtils.isEmpty(reportCaseDetail.getFunctionCases()) ? 0 : reportCaseDetail.getFunctionCases().size()));
		reportSummary.setApiCaseCount((long) (CollectionUtils.isEmpty(reportCaseDetail.getApiCases()) ? 0 : reportCaseDetail.getApiCases().size()));
		reportSummary.setApiScenarioCount((long) (CollectionUtils.isEmpty(reportCaseDetail.getApiScenarios()) ? 0 : reportCaseDetail.getApiScenarios().size()));
		reportSummary.setBugCount((long) (CollectionUtils.isEmpty(reportCaseDetail.getBugs()) ? 0 : reportCaseDetail.getBugs().size()));
		reportSummary.setPlanCount(genParam.getIntegrated() ? (long) childPlanIds.size() : 0);
		testPlanReportSummaryMapper.insertSelective(reportSummary);

        // 报告日志
        testPlanReportLogService.addLog(report, currentUser, genParam.getProjectId(), logPath);
        return report;
    }

	/**
	 * 生成独立报告的关联数据
	 * @param genParam 报告生成的参数
	 * @param moduleParam 模块参数
	 * @param report 报告
	 */
	private TestPlanReportDetailCaseDTO genReportDetail(TestPlanReportGenPreParam genParam, TestPlanReportModuleParam moduleParam, TestPlanReport report) {
		SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
		// 功能用例
		List<TestPlanReportFunctionCase> reportFunctionCases = extTestPlanReportFunctionalCaseMapper.getPlanExecuteCases(genParam.getTestPlanId());
		if (CollectionUtils.isNotEmpty(reportFunctionCases)) {
			// 用例等级
			List<String> ids = reportFunctionCases.stream().map(TestPlanReportFunctionCase::getFunctionCaseId).distinct().toList();
			List<SelectOption> options = extTestPlanReportFunctionalCaseMapper.getCasePriorityByIds(ids);
			Map<String, String> casePriorityMap = options.stream().collect(Collectors.toMap(SelectOption::getValue, SelectOption::getText));
			// 关联的功能用例最新一次执行历史
			List<String> relateIds = reportFunctionCases.stream().map(TestPlanReportFunctionCase::getTestPlanFunctionCaseId).toList();
			TestPlanCaseExecuteHistoryExample example = new TestPlanCaseExecuteHistoryExample();
			example.createCriteria().andTestPlanCaseIdIn(relateIds);
			List<TestPlanCaseExecuteHistory> functionalExecHisList = testPlanCaseExecuteHistoryMapper.selectByExample(example);
			Map<String, List<TestPlanCaseExecuteHistory>> functionalExecMap = functionalExecHisList.stream().collect(Collectors.groupingBy(TestPlanCaseExecuteHistory::getTestPlanCaseId));


			reportFunctionCases.forEach(reportFunctionalCase -> {
				reportFunctionalCase.setId(IDGenerator.nextStr());
				reportFunctionalCase.setTestPlanReportId(report.getId());
				reportFunctionalCase.setFunctionCaseModule(moduleParam.getFunctionalModuleMap().getOrDefault(reportFunctionalCase.getFunctionCaseModule(),
						ModuleTreeUtils.MODULE_PATH_PREFIX + reportFunctionalCase.getFunctionCaseModule()));
				reportFunctionalCase.setFunctionCasePriority(casePriorityMap.get(reportFunctionalCase.getFunctionCaseId()));
				List<TestPlanCaseExecuteHistory> hisList = functionalExecMap.get(reportFunctionalCase.getTestPlanFunctionCaseId());
				if (CollectionUtils.isNotEmpty(hisList)) {
					Optional<String> lastExecuteHisOpt = hisList.stream().sorted(Comparator.comparing(TestPlanCaseExecuteHistory::getCreateTime).reversed()).map(TestPlanCaseExecuteHistory::getId).findFirst();
					reportFunctionalCase.setFunctionCaseExecuteReportId(lastExecuteHisOpt.orElse(null));
				} else {
					reportFunctionalCase.setFunctionCaseExecuteReportId(null);
				}
			});
			// 插入计划功能用例关联数据 -> 报告内容
			TestPlanReportFunctionCaseMapper batchMapper = sqlSession.getMapper(TestPlanReportFunctionCaseMapper.class);
			batchMapper.batchInsert(reportFunctionCases);
		}

		// 接口用例
		List<TestPlanReportApiCase> reportApiCases = extTestPlanReportApiCaseMapper.getPlanExecuteCases(genParam.getTestPlanId());
		if (CollectionUtils.isNotEmpty(reportApiCases)) {
			reportApiCases.forEach(reportApiCase -> {
				reportApiCase.setId(IDGenerator.nextStr());
				reportApiCase.setTestPlanReportId(report.getId());
				reportApiCase.setApiCaseModule(moduleParam.getApiModuleMap().getOrDefault(reportApiCase.getApiCaseModule(),
						ModuleTreeUtils.MODULE_PATH_PREFIX + reportApiCase.getApiCaseModule()));
				if (!genParam.getUseManual()) {
					// 接口执行时才更新结果
					reportApiCase.setApiCaseExecuteResult(null);
					reportApiCase.setApiCaseExecuteUser(null);
					reportApiCase.setApiCaseExecuteReportId(IDGenerator.nextStr());
				}
			});
			// 插入计划接口用例关联数据 -> 报告内容
			TestPlanReportApiCaseMapper batchMapper = sqlSession.getMapper(TestPlanReportApiCaseMapper.class);
			batchMapper.batchInsert(reportApiCases);
		}

		// 场景用例
		List<TestPlanReportApiScenario> reportApiScenarios = extTestPlanReportApiScenarioMapper.getPlanExecuteCases(genParam.getTestPlanId());
		if (CollectionUtils.isNotEmpty(reportApiScenarios)) {
			reportApiScenarios.forEach(reportApiScenario -> {
				reportApiScenario.setId(IDGenerator.nextStr());
				reportApiScenario.setTestPlanReportId(report.getId());
				reportApiScenario.setApiScenarioModule(moduleParam.getScenarioModuleMap().getOrDefault(reportApiScenario.getApiScenarioModule(),
						ModuleTreeUtils.MODULE_PATH_PREFIX + reportApiScenario.getApiScenarioModule()));
				if (!genParam.getUseManual()) {
					// 接口执行时才更新结果
					reportApiScenario.setApiScenarioExecuteResult(null);
					reportApiScenario.setApiScenarioExecuteUser(null);
					reportApiScenario.setApiScenarioExecuteReportId(IDGenerator.nextStr());
				}
			});
			// 插入计划场景用例关联数据 -> 报告内容
			TestPlanReportApiScenarioMapper batchMapper = sqlSession.getMapper(TestPlanReportApiScenarioMapper.class);
			batchMapper.batchInsert(reportApiScenarios);
		}

		// 计划报告缺陷内容
		List<TestPlanReportBug> reportBugs = extTestPlanReportBugMapper.getPlanBugs(genParam.getTestPlanId());
		if (CollectionUtils.isNotEmpty(reportBugs)) {
			// MS处理人会与第三方的值冲突, 分开查询
			List<SelectOption> headerOptions = bugCommonService.getHeaderHandlerOption(genParam.getProjectId());
			Map<String, String> headerHandleUserMap = headerOptions.stream().collect(Collectors.toMap(SelectOption::getValue, SelectOption::getText));
			List<SelectOption> localOptions = bugCommonService.getLocalHandlerOption(genParam.getProjectId());
			Map<String, String> localHandleUserMap = localOptions.stream().collect(Collectors.toMap(SelectOption::getValue, SelectOption::getText));
			Map<String, String> allStatusMap = bugCommonService.getAllStatusMap(genParam.getProjectId());
			reportBugs.forEach(reportBug -> {
				reportBug.setId(IDGenerator.nextStr());
				reportBug.setTestPlanReportId(report.getId());
				reportBug.setBugHandleUser(headerHandleUserMap.containsKey(reportBug.getBugHandleUser()) ?
						headerHandleUserMap.get(reportBug.getBugHandleUser()) : localHandleUserMap.get(reportBug.getBugHandleUser()));
				reportBug.setBugStatus(allStatusMap.get(reportBug.getBugStatus()));
			});
			// 插入计划关联用例缺陷数据(去重) -> 报告内容
			TestPlanReportBugMapper batchMapper = sqlSession.getMapper(TestPlanReportBugMapper.class);
			batchMapper.batchInsert(reportBugs);
		}

        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);

        return TestPlanReportDetailCaseDTO.builder()
                .functionCases(reportFunctionCases).apiCases(reportApiCases).apiScenarios(reportApiScenarios).bugs(reportBugs).build();
    }


    /**
     * 报告结果后置处理 (汇总操作结束后调用)
     *
     * @param postParam 后置处理参数
     */
    public void postHandleReport(TestPlanReportPostParam postParam, boolean useManual) {
        /*
         * 处理报告(执行状态, 结束时间)
         */
        TestPlanReport planReport = checkReport(postParam.getReportId());
        BeanUtils.copyBean(planReport, postParam);
        /*
         * 计算报告通过率, 并对比阈值生成报告结果状态
         */
        TestPlanReportSummaryExample example = new TestPlanReportSummaryExample();
        example.createCriteria().andTestPlanReportIdEqualTo(postParam.getReportId());
        TestPlanReportSummary reportSummary = testPlanReportSummaryMapper.selectByExampleWithBLOBs(example).get(0);
        // 用例总数
        long caseTotal = reportSummary.getFunctionalCaseCount() + reportSummary.getApiCaseCount() + reportSummary.getApiScenarioCount();
        CaseCount summaryCount = JSON.parseObject(new String(reportSummary.getExecuteResult()), CaseCount.class);
        planReport.setExecuteRate(RateCalculateUtils.divWithPrecision(((int) caseTotal - summaryCount.getPending()), (int) caseTotal, 2));
        planReport.setPassRate(RateCalculateUtils.divWithPrecision(summaryCount.getSuccess(), (int) caseTotal, 2));
		if (planReport.getIntegrated()) {
			// 计划组的(执行)结果状态: 子计划全部成功 ? 成功 : 失败
			TestPlanReportExample reportExample = new TestPlanReportExample();
			reportExample.createCriteria().andParentIdEqualTo(postParam.getReportId()).andIntegratedEqualTo(false).andResultStatusNotEqualTo(ReportStatus.SUCCESS.name());
			planReport.setResultStatus(testPlanReportMapper.countByExample(reportExample) == 0 ? ReportStatus.SUCCESS.name() : ReportStatus.ERROR.name());
		} else {
			// 计划的(执行)结果状态: 通过率 >= 阈值 ? 成功 : 失败
			planReport.setResultStatus(planReport.getPassRate() >= planReport.getPassThreshold() ? ReportStatus.SUCCESS.name() : ReportStatus.ERROR.name());
		}

        testPlanReportMapper.updateByPrimaryKeySelective(planReport);

		// 发送计划执行通知
		if (!useManual) {
			testPlanSendNoticeService.sendExecuteNotice(planReport.getCreateUser(), planReport.getTestPlanId(), planReport.getProjectId(), planReport.getResultStatus());
		}
    }

	/**
	 * 获取报告
	 * @param reportId 报告ID
	 * @return 报告详情
	 */
    public TestPlanReport selectById(String reportId) {
        return testPlanReportMapper.selectByPrimaryKey(reportId);
    }

    /**
     * 获取报告分析详情
     *
     * @param reportId 报告ID
     * @return 报告分析详情
     */
    public TestPlanReportDetailResponse getReport(String reportId) {
        TestPlanReport planReport = checkReport(reportId);
        TestPlanReportSummaryExample example = new TestPlanReportSummaryExample();
        example.createCriteria().andTestPlanReportIdEqualTo(reportId);
		List<TestPlanReportSummary> testPlanReportSummaries = testPlanReportSummaryMapper.selectByExampleWithBLOBs(example);
		if (CollectionUtils.isEmpty(testPlanReportSummaries)) {
			throw new MSException(Translator.get("test_plan_report_detail_not_exist"));
		}
		TestPlanReportSummary reportSummary = testPlanReportSummaries.get(0);
        TestPlanReportDetailResponse planReportDetail = new TestPlanReportDetailResponse();
        BeanUtils.copyBean(planReportDetail, planReport);
        int caseTotal = (int) (reportSummary.getFunctionalCaseCount() + reportSummary.getApiCaseCount() + reportSummary.getApiScenarioCount());
        planReportDetail.setCaseTotal(caseTotal);
        planReportDetail.setBugCount(reportSummary.getBugCount().intValue());
		// 暂时只有功能用例能关联缺陷
		Long functionalBugCount = extTestPlanReportFunctionalCaseMapper.countBug(reportId);
		planReportDetail.setFunctionalBugCount(functionalBugCount == null ? 0 : functionalBugCount.intValue());
		if (planReport.getIntegrated()) {
			// 计划组报告, 需要统计计划的执行数据
			planReportDetail.setPlanCount(reportSummary.getPlanCount().intValue());
			TestPlanReportExample reportExample = new TestPlanReportExample();
			reportExample.createCriteria().andParentIdEqualTo(reportId).andIntegratedEqualTo(false);
			List<TestPlanReport> testPlanReports = testPlanReportMapper.selectByExample(reportExample);
			long planPassCount = testPlanReports.stream().filter(report -> StringUtils.equals(ExecStatus.SUCCESS.name(), report.getResultStatus())).count();
			planReportDetail.setPassCountOfPlan((int) planPassCount);
			planReportDetail.setFailCountOfPlan(planReportDetail.getPlanCount() - planReportDetail.getPassCountOfPlan());
		}
        planReportDetail.setSummary(reportSummary.getSummary());
        /*
         * 统计用例执行数据
         */
        planReportDetail.setFunctionalCount(reportSummary.getFunctionalExecuteResult() == null ? CaseCount.builder().build() : JSON.parseObject(new String(reportSummary.getFunctionalExecuteResult()), CaseCount.class));
        planReportDetail.setApiCaseCount(reportSummary.getApiExecuteResult() == null ? CaseCount.builder().build() : JSON.parseObject(new String(reportSummary.getApiExecuteResult()), CaseCount.class));
        planReportDetail.setApiScenarioCount(reportSummary.getScenarioExecuteResult() == null ? CaseCount.builder().build() : JSON.parseObject(new String(reportSummary.getScenarioExecuteResult()), CaseCount.class));
        planReportDetail.setExecuteCount(reportSummary.getExecuteResult() == null ? CaseCount.builder().build() : JSON.parseObject(new String(reportSummary.getExecuteResult()), CaseCount.class));
        return planReportDetail;
    }

    /**
     * 更新报告详情
     *
     * @param request 更新请求参数
     * @return 报告详情
     */
    public TestPlanReportDetailResponse edit(TestPlanReportDetailEditRequest request, String currentUser) {
        TestPlanReport planReport = checkReport(request.getId());
        TestPlanReportSummary reportSummary = new TestPlanReportSummary();
        reportSummary.setSummary(StringUtils.isBlank(request.getSummary()) ? StringUtils.EMPTY : request.getSummary());
        TestPlanReportSummaryExample example = new TestPlanReportSummaryExample();
        example.createCriteria().andTestPlanReportIdEqualTo(planReport.getId());
        testPlanReportSummaryMapper.updateByExampleSelective(reportSummary, example);
        // 处理富文本文件
        transferRichTextTmpFile(request.getId(), planReport.getProjectId(), request.getRichTextTmpFileIds(), currentUser, TestPlanReportAttachmentSourceType.RICH_TEXT.name());
        return getReport(planReport.getId());
    }

    /**
     * 分页查询报告详情-缺陷分页数据
     *
     * @param request 请求参数
     * @return 缺陷分页数据
     */
    public List<BugDTO> listReportDetailBugs(TestPlanReportDetailPageRequest request) {
        return extTestPlanReportBugMapper.list(request);
    }

	/**
	 * 分页查询报告详情-用例分页数据
	 * @param request 请求参数
	 * @return 用例分页数据
	 */
	public List<ReportDetailCasePageDTO> listReportDetailCases(TestPlanReportDetailPageRequest request, String caseType) {
		List<ReportDetailCasePageDTO> detailCases;
		switch (caseType) {
			case AssociateCaseType.FUNCTIONAL ->  detailCases = extTestPlanReportFunctionalCaseMapper.list(request);
			case AssociateCaseType.API_CASE -> detailCases = extTestPlanReportApiCaseMapper.list(request);
			case AssociateCaseType.API_SCENARIO -> detailCases = extTestPlanReportApiScenarioMapper.list(request);
			default -> detailCases = new ArrayList<>();
		}
		List<String> distinctUserIds = detailCases.stream().map(ReportDetailCasePageDTO::getExecuteUser).distinct().toList();
		Map<String, String> userMap = getUserMap(distinctUserIds);
		detailCases.forEach(detailCase -> detailCase.setExecuteUser(userMap.getOrDefault(detailCase.getExecuteUser(), detailCase.getExecuteUser())));
		return detailCases;
	}

    /**
     * 汇总生成的计划报告
     *
     * @param reportId 报告ID
     */
    public void summaryPlanReport(String reportId) {
        TestPlanReportSummaryExample example = new TestPlanReportSummaryExample();
        example.createCriteria().andTestPlanReportIdEqualTo(reportId);
        List<TestPlanReportSummary> testPlanReportSummaries = testPlanReportSummaryMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(testPlanReportSummaries)) {
            // 报告详情不存在
            return;
        }
        // 功能用例
        List<CaseStatusCountMap> functionalCountMapList = extTestPlanReportFunctionalCaseMapper.countExecuteResult(reportId);
        CaseCount functionalCaseCount = countMap(functionalCountMapList);
        // 接口用例
        List<CaseStatusCountMap> apiCountMapList = extTestPlanReportApiCaseMapper.countExecuteResult(reportId);
        CaseCount apiCaseCount = countMap(apiCountMapList);
        // 场景用例
        List<CaseStatusCountMap> scenarioCountMapList = extTestPlanReportApiScenarioMapper.countExecuteResult(reportId);
        CaseCount scenarioCaseCount = countMap(scenarioCountMapList);

        // 规划整体的汇总数据
        CaseCount summaryCount = CaseCount.builder().success(functionalCaseCount.getSuccess() + apiCaseCount.getSuccess() + scenarioCaseCount.getSuccess())
                .error(functionalCaseCount.getError() + apiCaseCount.getError() + scenarioCaseCount.getError())
                .block(functionalCaseCount.getBlock() + apiCaseCount.getBlock() + scenarioCaseCount.getBlock())
                .pending(functionalCaseCount.getPending() + apiCaseCount.getPending() + scenarioCaseCount.getPending())
                .fakeError(functionalCaseCount.getFakeError() + apiCaseCount.getFakeError() + scenarioCaseCount.getFakeError()).build();

        // 入库汇总数据 => 报告详情表
        TestPlanReportSummary reportSummary = testPlanReportSummaries.get(0);
        reportSummary.setFunctionalExecuteResult(JSON.toJSONBytes(functionalCaseCount));
        reportSummary.setApiExecuteResult(JSON.toJSONBytes(apiCaseCount));
        reportSummary.setScenarioExecuteResult(JSON.toJSONBytes(scenarioCaseCount));
        reportSummary.setExecuteResult(JSON.toJSONBytes(summaryCount));
        testPlanReportSummaryMapper.updateByPrimaryKeySelective(reportSummary);
    }

	/**
	 * 汇总生成的计划组报告
	 * @param reportId 报告ID
	 */
	public void summaryGroupReport(String reportId) {
		TestPlanReportSummaryExample summaryExample = new TestPlanReportSummaryExample();
		summaryExample.createCriteria().andTestPlanReportIdEqualTo(reportId);
		List<TestPlanReportSummary> testPlanReportSummaries = testPlanReportSummaryMapper.selectByExample(summaryExample);
		if (CollectionUtils.isEmpty(testPlanReportSummaries)) {
			// 报告详情不存在
			return;
		}
		TestPlanReportSummary groupSummary = testPlanReportSummaries.get(0);

		TestPlanReportExample example = new TestPlanReportExample();
		example.createCriteria().andParentIdEqualTo(reportId).andIntegratedEqualTo(false);
		List<TestPlanReport> testPlanReports = testPlanReportMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(testPlanReports)){
			// 不存在子报告, 不需要汇总数据
            return;
        }

		// 汇总子报告关联的数据并入库
		SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
		List<String> ids = testPlanReports.stream().map(TestPlanReport::getId).toList();
		// 功能用例
		TestPlanReportFunctionCaseExample functionCaseExample = new TestPlanReportFunctionCaseExample();
		functionCaseExample.createCriteria().andTestPlanReportIdIn(ids);
		List<TestPlanReportFunctionCase> reportFunctionCases = testPlanReportFunctionCaseMapper.selectByExample(functionCaseExample);
		if (CollectionUtils.isNotEmpty(reportFunctionCases)) {
			groupSummary.setFunctionalCaseCount((long) reportFunctionCases.size());
			reportFunctionCases.forEach(reportFunctionCase -> {
				reportFunctionCase.setId(IDGenerator.nextStr());
				reportFunctionCase.setTestPlanReportId(reportId);
			});
			// 插入计划组报告, 功能用例关联数据
			TestPlanReportFunctionCaseMapper batchMapper = sqlSession.getMapper(TestPlanReportFunctionCaseMapper.class);
			batchMapper.batchInsert(reportFunctionCases);
		}
		// 接口用例
		TestPlanReportApiCaseExample apiCaseExample = new TestPlanReportApiCaseExample();
		apiCaseExample.createCriteria().andTestPlanReportIdIn(ids);
		List<TestPlanReportApiCase> reportApiCases = testPlanReportApiCaseMapper.selectByExample(apiCaseExample);
		if (CollectionUtils.isNotEmpty(reportApiCases)) {
			groupSummary.setApiCaseCount((long) reportApiCases.size());
			reportApiCases.forEach(reportApiCase -> {
				reportApiCase.setId(IDGenerator.nextStr());
				reportApiCase.setTestPlanReportId(reportId);
			});
			// 插入计划组报告, 接口用例关联数据
			TestPlanReportApiCaseMapper batchMapper = sqlSession.getMapper(TestPlanReportApiCaseMapper.class);
			batchMapper.batchInsert(reportApiCases);
		}
		// 场景用例
		TestPlanReportApiScenarioExample scenarioExample = new TestPlanReportApiScenarioExample();
		scenarioExample.createCriteria().andTestPlanReportIdIn(ids);
		List<TestPlanReportApiScenario> reportApiScenarios = testPlanReportApiScenarioMapper.selectByExample(scenarioExample);
		if (CollectionUtils.isNotEmpty(reportApiScenarios)) {
			groupSummary.setApiScenarioCount((long) reportApiScenarios.size());
			reportApiScenarios.forEach(reportApiScenario -> {
				reportApiScenario.setId(IDGenerator.nextStr());
				reportApiScenario.setTestPlanReportId(reportId);
			});
			// 插入计划组报告, 场景用例关联数据
			TestPlanReportApiScenarioMapper batchMapper = sqlSession.getMapper(TestPlanReportApiScenarioMapper.class);
			batchMapper.batchInsert(reportApiScenarios);
		}
		// 缺陷明细
		TestPlanReportBugExample bugExample = new TestPlanReportBugExample();
		bugExample.createCriteria().andTestPlanReportIdIn(ids);
		List<TestPlanReportBug> reportBugs = testPlanReportBugMapper.selectByExample(bugExample);
		if (CollectionUtils.isNotEmpty(reportBugs)) {
			groupSummary.setBugCount((long) reportBugs.size());
			reportBugs.forEach(reportBug -> {
				reportBug.setId(IDGenerator.nextStr());
				reportBug.setTestPlanReportId(reportId);
			});
			// 插入计划组关联用例缺陷数据
			TestPlanReportBugMapper batchMapper = sqlSession.getMapper(TestPlanReportBugMapper.class);
			batchMapper.batchInsert(reportBugs);
		}
		sqlSession.flushStatements();
		SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);

		// 汇总并计算子报告执行的数据
		summaryExample.clear();
		summaryExample.createCriteria().andTestPlanReportIdIn(ids);
		List<TestPlanReportSummary> summaryList = testPlanReportSummaryMapper.selectByExampleWithBLOBs(summaryExample);
		List<CaseCount> functionalCaseCountList = new ArrayList<>();
		List<CaseCount> apiCaseCountList = new ArrayList<>();
		List<CaseCount> scenarioCountList = new ArrayList<>();
		List<CaseCount> executeCountList = new ArrayList<>();
		summaryList.forEach(summary -> {
			CaseCount functionalCaseCount = summary.getFunctionalExecuteResult() == null ? CaseCount.builder().build() : JSON.parseObject(new String(summary.getFunctionalExecuteResult()), CaseCount.class);
			CaseCount apiCaseCount = summary.getApiExecuteResult() == null ? CaseCount.builder().build() : JSON.parseObject(new String(summary.getApiExecuteResult()), CaseCount.class);
			CaseCount scenarioCount = summary.getScenarioExecuteResult() == null ? CaseCount.builder().build() : JSON.parseObject(new String(summary.getScenarioExecuteResult()), CaseCount.class);
			CaseCount executeCount = summary.getExecuteResult() == null ? CaseCount.builder().build() : JSON.parseObject(new String(summary.getExecuteResult()), CaseCount.class);
			functionalCaseCountList.add(functionalCaseCount);
			apiCaseCountList.add(apiCaseCount);
			scenarioCountList.add(scenarioCount);
			executeCountList.add(executeCount);
		});

		// 入库组汇总数据 => 报告详情表
		groupSummary.setFunctionalExecuteResult(JSON.toJSONBytes(CountUtils.summarizeProperties(functionalCaseCountList)));
		groupSummary.setApiExecuteResult(JSON.toJSONBytes(CountUtils.summarizeProperties(apiCaseCountList)));
		groupSummary.setScenarioExecuteResult(JSON.toJSONBytes(CountUtils.summarizeProperties(scenarioCountList)));
		groupSummary.setExecuteResult(JSON.toJSONBytes(CountUtils.summarizeProperties(executeCountList)));
		testPlanReportSummaryMapper.updateByPrimaryKeySelective(groupSummary);
	}

    /**
     * 通过请求参数获取批量操作的ID集合
     *
     * @param request 请求参数
     * @return ID集合
     */
    private List<String> getBatchIds(TestPlanReportBatchRequest request) {
        if (request.isSelectAll()) {
            List<String> batchIds = extTestPlanReportMapper.getReportBatchIdsByParam(request);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                batchIds.removeIf(id -> request.getExcludeIds().contains(id));
            }
            return batchIds;
        } else {
            return request.getSelectIds();
        }
    }

    /**
     * 校验计划是否存在
     *
     * @param planId 计划ID
     * @return 测试计划
     */
    private TestPlan checkPlan(String planId) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(planId);
        if (testPlan == null) {
            throw new MSException(Translator.get("test_plan_not_exist"));
        }
        return testPlan;
    }


    /**
     * 校验报告是否存在
     *
     * @param id 报告ID
     */
    private TestPlanReport checkReport(String id) {
        TestPlanReport testPlanReport = testPlanReportMapper.selectByPrimaryKey(id);
        if (testPlanReport == null) {
            throw new MSException(Translator.get("test_plan_report_not_exist"));
        }
        return testPlanReport;
    }

    /**
     * 转存报告内容富文本临时文件
     *
     * @param reportId      报告ID
     * @param projectId     项目ID
     * @param uploadFileIds 上传的文件ID集合
     * @param userId        用户ID
     * @param source        文件来源
     */
    private void transferRichTextTmpFile(String reportId, String projectId, List<String> uploadFileIds, String userId, String source) {
        if (CollectionUtils.isEmpty(uploadFileIds)) {
            return;
        }
        //过滤已上传过的
        TestPlanReportAttachmentExample example = new TestPlanReportAttachmentExample();
        example.createCriteria().andTestPlanReportIdEqualTo(reportId).andFileIdIn(uploadFileIds).andSourceEqualTo(source);
        List<TestPlanReportAttachment> existReportMdFiles = testPlanReportAttachmentMapper.selectByExample(example);
        Map<String, TestPlanReportAttachment> existFileMap = existReportMdFiles.stream().collect(Collectors.toMap(TestPlanReportAttachment::getFileId, v -> v));
        List<String> fileIds = uploadFileIds.stream().filter(t -> !existFileMap.containsKey(t) && StringUtils.isNotBlank(t)).toList();
        if (CollectionUtils.isEmpty(fileIds)) {
            return;
        }
        // 处理本地上传文件
        FileRepository defaultRepository = FileCenter.getDefaultRepository();
        String systemTempDir = DefaultRepositoryDir.getSystemTempDir();
        // 添加文件与测试计划报告的关联关系
        Map<String, String> addFileMap = new HashMap<>(fileIds.size());
        LogUtils.info("开始上传副文本里的附件");
        List<TestPlanReportAttachment> attachments = fileIds.stream().map(fileId -> {
            TestPlanReportAttachment attachment = new TestPlanReportAttachment();
            String fileName = getTempFileNameByFileId(fileId);
            attachment.setId(IDGenerator.nextStr());
            attachment.setTestPlanReportId(reportId);
            attachment.setFileId(fileId);
            attachment.setFileName(fileName);
            attachment.setSource(source);
            long fileSize;
            try {
                FileCopyRequest fileCopyRequest = new FileCopyRequest();
                fileCopyRequest.setFolder(systemTempDir + "/" + fileId);
                fileCopyRequest.setFileName(fileName);
                fileSize = defaultRepository.getFileSize(fileCopyRequest);
            } catch (Exception e) {
                throw new MSException("读取富文本临时文件失败");
            }
            attachment.setSize(fileSize);
            attachment.setCreateUser(userId);
            attachment.setCreateTime(System.currentTimeMillis());
            addFileMap.put(fileId, fileName);
            return attachment;
        }).toList();
        testPlanReportAttachmentMapper.batchInsert(attachments);
        // 上传文件到对象存储
        LogUtils.info("upload to minio start");
        uploadFileResource(DefaultRepositoryDir.getPlanReportDir(projectId, reportId), addFileMap);
        LogUtils.info("upload to minio end");
    }

    /**
     * 根据文件ID，查询MINIO中对应目录下的文件名称
     */
    private String getTempFileNameByFileId(String fileId) {
        try {
            FileRequest fileRequest = new FileRequest();
            fileRequest.setFolder(DefaultRepositoryDir.getSystemTempDir() + "/" + fileId);
            List<String> folderFileNames = FileCenter.getDefaultRepository().getFolderFileNames(fileRequest);
            if (CollectionUtils.isEmpty(folderFileNames)) {
                return null;
            }
            String[] pathSplit = folderFileNames.get(0).split("/");
            return pathSplit[pathSplit.length - 1];
        } catch (Exception e) {
            LogUtils.error(e);
            return null;
        }
    }

    /**
     * 上传文件到资源目录
     *
     * @param folder     文件夹
     * @param addFileMap 文件ID与文件名映射
     */
    private void uploadFileResource(String folder, Map<String, String> addFileMap) {
        FileRepository defaultRepository = FileCenter.getDefaultRepository();
        for (String fileId : addFileMap.keySet()) {
            String systemTempDir = DefaultRepositoryDir.getSystemTempDir();
            try {
                String fileName = addFileMap.get(fileId);
                if (StringUtils.isEmpty(fileName)) {
                    continue;
                }
                // 按ID建文件夹，避免文件名重复
                FileCopyRequest fileCopyRequest = new FileCopyRequest();
                fileCopyRequest.setCopyFolder(systemTempDir + "/" + fileId);
                fileCopyRequest.setCopyfileName(fileName);
                fileCopyRequest.setFileName(fileName);
                fileCopyRequest.setFolder(folder + "/" + fileId);
                // 将文件从临时目录复制到资源目录
                defaultRepository.copyFile(fileCopyRequest);
                // 删除临时文件
                fileCopyRequest.setFolder(systemTempDir + "/" + fileId);
                fileCopyRequest.setFileName(fileName);
                defaultRepository.delete(fileCopyRequest);
            } catch (Exception e) {
                LogUtils.error("上传副文本文件失败：{}", e);
                throw new MSException(Translator.get("file_upload_fail"));
            }
        }
    }

	/**
	 * 构建预生成报告的参数
	 * @param genRequest 报告请求参数
	 * @return 预生成报告参数
	 */
	private TestPlanReportGenPreParam buildReportGenParam(TestPlanReportGenRequest genRequest, TestPlan testPlan, String groupReportId) {
		TestPlanReportGenPreParam genPreParam = new TestPlanReportGenPreParam();
		BeanUtils.copyBean(genPreParam, genRequest);
		genPreParam.setTestPlanName(testPlan.getName());
		genPreParam.setStartTime(System.currentTimeMillis());
		// 报告预生成时, 执行状态为未执行, 结果状态为'-'
		genPreParam.setExecStatus(ExecStatus.PENDING.name());
		genPreParam.setResultStatus("-");
		// 是否集成报告(计划组报告), 目前根据是否计划组来区分
		genPreParam.setIntegrated(StringUtils.equals(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP));
		genPreParam.setGroupReportId(groupReportId);
		return genPreParam;
	}

    /**
     * 统计执行状态的用例数量
     *
     * @param resultMapList 执行结果集合
     * @return 用例数量
     */
    private CaseCount countMap(List<CaseStatusCountMap> resultMapList) {
        CaseCount caseCount = new CaseCount();
        Map<String, Long> resultMap = resultMapList.stream().collect(Collectors.toMap(CaseStatusCountMap::getStatus, CaseStatusCountMap::getCount));
        caseCount.setSuccess(resultMap.getOrDefault(ExecStatus.SUCCESS.name(), 0L).intValue());
        caseCount.setError(resultMap.getOrDefault(ExecStatus.ERROR.name(), 0L).intValue());
        caseCount.setPending(resultMap.getOrDefault(ExecStatus.PENDING.name(), 0L).intValue() + resultMap.getOrDefault(ExecStatus.STOPPED.name(), 0L).intValue());
        caseCount.setBlock(resultMap.getOrDefault(ExecStatus.BLOCKED.name(), 0L).intValue());
        caseCount.setFakeError(resultMap.getOrDefault(ExecStatus.FAKE_ERROR.name(), 0L).intValue());
        return caseCount;
    }

    /**
     * 获取组或者计划
     *
     * @param groupOrPlanId 计划组/报告ID
     * @return 计划集合
     */
    private List<TestPlan> getPlans(String groupOrPlanId) {
        TestPlan testPlan = checkPlan(groupOrPlanId);
        List<TestPlan> plans = new ArrayList<>();
        if (StringUtils.equals(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            // 计划组
            TestPlanExample example = new TestPlanExample();
            example.createCriteria().andGroupIdEqualTo(groupOrPlanId);
            List<TestPlan> testPlans = testPlanMapper.selectByExample(example);
            plans.addAll(testPlans);
        }
        // 保证最后一条为计划组
        plans.addLast(testPlan);
        return plans;
    }

    /**
     * 获取项目下的模块参数
     *
     * @param projectId 项目ID
     * @return 模块参数
     */
    private TestPlanReportModuleParam getModuleParam(String projectId) {
        // 模块树 {功能, 接口, 场景}
        List<TestPlanBaseModule> functionalModules = extTestPlanReportFunctionalCaseMapper.getPlanExecuteCaseModules(projectId);
        Map<String, String> functionalModuleMap = new HashMap<>(functionalModules.size());
        ModuleTreeUtils.genPathMap(functionalModules, functionalModuleMap, new ArrayList<>());
        List<TestPlanBaseModule> apiModules = extTestPlanReportApiCaseMapper.getPlanExecuteCaseModules(projectId);
        Map<String, String> apiModuleMap = new HashMap<>(apiModules.size());
        ModuleTreeUtils.genPathMap(apiModules, apiModuleMap, new ArrayList<>());
        List<TestPlanBaseModule> scenarioModules = extTestPlanReportApiScenarioMapper.getPlanExecuteCaseModules(projectId);
        Map<String, String> scenarioModuleMap = new HashMap<>(apiModules.size());
        ModuleTreeUtils.genPathMap(scenarioModules, scenarioModuleMap, new ArrayList<>());
        return TestPlanReportModuleParam.builder().functionalModuleMap(functionalModuleMap).apiModuleMap(apiModuleMap).scenarioModuleMap(scenarioModuleMap).build();
    }

    /**
     * 获取用户集合
     *
     * @param userIds 用户ID集合
     * @return 用户集合
     */
    private Map<String, String> getUserMap(List<String> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return new HashMap<>(16);
        }
        List<OptionDTO> userOptions = baseUserMapper.selectUserOptionByIds(userIds);
        return userOptions.stream().collect(Collectors.toMap(OptionDTO::getId, OptionDTO::getName));
    }

    public List<TestPlanReportDetailResponse> planReportList(TestPlanReportDetailPageRequest request) {
        return extTestPlanReportMapper.getPlanReportListById(request);
    }

	public void updateExecuteTimeAndStatus(String prepareReportId) {
		extTestPlanReportMapper.batchUpdateExecuteTimeAndStatus(System.currentTimeMillis(), Collections.singletonList(prepareReportId));
	}
}
