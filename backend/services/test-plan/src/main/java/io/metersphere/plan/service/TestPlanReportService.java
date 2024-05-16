package io.metersphere.plan.service;

import io.metersphere.bug.dto.response.BugDTO;
import io.metersphere.bug.mapper.ExtBugRelateCaseMapper;
import io.metersphere.bug.service.BugService;
import io.metersphere.functional.dto.FunctionalCasePageDTO;
import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.CaseStatusCountMap;
import io.metersphere.plan.dto.TestPlanReportGenPreParam;
import io.metersphere.plan.dto.TestPlanReportPostParam;
import io.metersphere.plan.dto.request.TestPlanReportBatchRequest;
import io.metersphere.plan.dto.request.TestPlanReportDetailPageRequest;
import io.metersphere.plan.dto.request.TestPlanReportGenRequest;
import io.metersphere.plan.dto.request.TestPlanReportPageRequest;
import io.metersphere.plan.dto.response.TestPlanReportDetailResponse;
import io.metersphere.plan.dto.response.TestPlanReportPageResponse;
import io.metersphere.plan.mapper.*;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.DateUtils;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.User;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.service.UserService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TestPlanReportService {

	@Resource
	private UserMapper userMapper;
	@Resource
	private UserService userService;
	@Resource
	private SqlSessionFactory sqlSessionFactory;
	@Resource
	private BugService bugService;
	@Resource
	private TestPlanMapper testPlanMapper;
	@Resource
	private TestPlanConfigMapper testPlanConfigMapper;
	@Resource
	private TestPlanReportMapper testPlanReportMapper;
	@Resource
	private ExtBugRelateCaseMapper extBugRelateCaseMapper;
	@Resource
	private ExtTestPlanReportMapper extTestPlanReportMapper;
	@Resource
	private TestPlanReportLogService testPlanReportLogService;
	@Resource
	private TestPlanReportNoticeService testPlanReportNoticeService;
	@Resource
	private TestPlanReportSummaryMapper testPlanReportSummaryMapper;
	@Resource
	private TestPlanFunctionalCaseMapper testPlanFunctionalCaseMapper;
	@Resource
	private TestPlanReportFunctionCaseMapper testPlanReportFunctionCaseMapper;
    @Resource
    private TestPlanReportBugMapper testPlanReportBugMapper;

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
        Map<String, String> userMap = userService.getUserMapByIds(distinctUserIds);
        reportList.forEach(report -> report.setCreateUserName(userMap.get(report.getCreateUser())));
        return reportList;
    }

    /**
     * 报告重命名
     */
    public void rename(String id, String name) {
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
     * @param reportIdList
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
        // todo 后续版本增加 api_case\ api_scenario 的清理
        TestPlanReportSummaryExample summaryExample = new TestPlanReportSummaryExample();
        summaryExample.createCriteria().andTestPlanReportIdIn(reportIdList);
        testPlanReportSummaryMapper.deleteByExample(summaryExample);

        TestPlanReportFunctionCaseExample testPlanReportFunctionCaseExample = new TestPlanReportFunctionCaseExample();
        testPlanReportFunctionCaseExample.createCriteria().andTestPlanReportIdIn(reportIdList);
        testPlanReportFunctionCaseMapper.deleteByExample(testPlanReportFunctionCaseExample);

        TestPlanReportBugExample testPlanReportBugExample = new TestPlanReportBugExample();
        testPlanReportBugExample.createCriteria().andTestPlanReportIdIn(reportIdList);
        testPlanReportBugMapper.deleteByExample(testPlanReportBugExample);
    }
	/**
	 * 手动生成报告
	 * @param request 请求参数
	 * @return 报告
	 */
	public TestPlanReport genReportByManual(TestPlanReportGenRequest request, String currentUser) {
		TestPlan testPlan = checkPlan(request.getTestPlanId());
		/*
		 * 手动生成报告
		 * 1. 构建预生成报告参数
		 * 2. 预生成报告
		 * 3. 报告后置处理
		 */
		TestPlanReportGenPreParam genPreParam = new TestPlanReportGenPreParam();
		BeanUtils.copyBean(genPreParam, request);
		genPreParam.setTestPlanName(testPlan.getName());
		genPreParam.setStartTime(System.currentTimeMillis());
		// 手动触发
		genPreParam.setTriggerMode(TaskTriggerMode.MANUAL.name());
		// 报告预生成时, 执行状态为未执行, 结果状态为'-'
		genPreParam.setExecStatus(ExecStatus.PENDING.name());
		genPreParam.setResultStatus("-");
		// 是否集成报告, 目前根据是否计划组来区分
		genPreParam.setIntegrated(StringUtils.equals(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP));
		TestPlanReport preReport = preGenReport(genPreParam, currentUser);
		TestPlanReportPostParam postParam = new TestPlanReportPostParam();
		BeanUtils.copyBean(postParam, request);
		postParam.setReportId(preReport.getId());
		// 手动生成报告, 执行状态为已完成, 执行及结束时间为当前时间
		postParam.setExecuteTime(System.currentTimeMillis());
		postParam.setEndTime(System.currentTimeMillis());
		postParam.setExecStatus(ExecStatus.COMPLETED.name());
		return postHandleReport(postParam);
	}


	/**
	 * 预生成报告内容(后续拆分优化)
	 * @return 报告
	 */
	public TestPlanReport preGenReport(TestPlanReportGenPreParam genParam, String currentUser) {
		// 准备计划数据
		TestPlanConfig testPlanConfig = testPlanConfigMapper.selectByPrimaryKey(genParam.getTestPlanId());

		/*
		 * 预生成报告(后续执行生成报告复用)
		 * 1. 生成报告用例数据, 缺陷数据
		 * 2. 生成或计算报告统计数据
		 */
		SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
		String reportId = IDGenerator.nextStr();
		// 功能用例
		List<TestPlanReportFunctionCase> reportFunctionCases = new ArrayList<>();
		TestPlanFunctionalCaseExample functionalCaseExample = new TestPlanFunctionalCaseExample();
		functionalCaseExample.createCriteria().andTestPlanIdEqualTo(genParam.getTestPlanId());
		List<TestPlanFunctionalCase> testPlanFunctionalCases = testPlanFunctionalCaseMapper.selectByExample(functionalCaseExample);
		testPlanFunctionalCases.forEach(functionalCase -> {
			TestPlanReportFunctionCase reportFunctionCase = new TestPlanReportFunctionCase();
			reportFunctionCase.setId(IDGenerator.nextStr());
			reportFunctionCase.setTestPlanReportId(reportId);
			reportFunctionCase.setFunctionCaseId(functionalCase.getFunctionalCaseId());
			reportFunctionCase.setTestPlanFunctionCaseId(functionalCase.getId());
			reportFunctionCase.setExecuteResult(functionalCase.getLastExecResult());
			reportFunctionCases.add(reportFunctionCase);
		});
		if (CollectionUtils.isNotEmpty(reportFunctionCases)) {
			// 插入计划功能用例关联数据 -> 报告内容
			TestPlanReportFunctionCaseMapper batchMapper = sqlSession.getMapper(TestPlanReportFunctionCaseMapper.class);
			batchMapper.batchInsert(reportFunctionCases);
		}

		// TODO: 接口用例, 场景报告内容 (与接口报告是否能一致)

		// 计划报告缺陷内容
		List<TestPlanReportBug> reportBugs = new ArrayList<>();
		List<String> bugIds = extBugRelateCaseMapper.getPlanRelateBugIds(genParam.getTestPlanId());
		bugIds.forEach(bugId -> {
			TestPlanReportBug reportBug = new TestPlanReportBug();
			reportBug.setId(IDGenerator.nextStr());
			reportBug.setTestPlanReportId(reportId);
			reportBug.setBugId(bugId);
			reportBugs.add(reportBug);
		});
		if (CollectionUtils.isNotEmpty(reportBugs)) {
			// 插入计划关联用例缺陷数据(去重) -> 报告内容
			TestPlanReportBugMapper batchMapper = sqlSession.getMapper(TestPlanReportBugMapper.class);
			batchMapper.batchInsert(reportBugs);
		}
		sqlSession.flushStatements();
		SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);

		// 插入报告统计内容
		TestPlanReportSummary reportSummary = new TestPlanReportSummary();
		reportSummary.setId(IDGenerator.nextStr());
		reportSummary.setTestPlanReportId(reportId);
		reportSummary.setFunctionalCaseCount((long) (CollectionUtils.isEmpty(reportFunctionCases) ? 0 : reportFunctionCases.size()));
		reportSummary.setApiCaseCount(0L);
		reportSummary.setApiScenarioCount(0L);
		reportSummary.setBugCount((long) (CollectionUtils.isEmpty(reportBugs) ? 0 : reportBugs.size()));
		testPlanReportSummaryMapper.insertSelective(reportSummary);
		// 插入报告
		TestPlanReport report = new TestPlanReport();
		BeanUtils.copyBean(report, genParam);
		report.setId(reportId);
		report.setName(genParam.getTestPlanName() + "-" + DateUtils.getTimeStr(System.currentTimeMillis()));
		report.setCreateUser(currentUser);
		report.setCreateTime(System.currentTimeMillis());
		report.setDeleted(false);
		report.setPassThreshold(testPlanConfig.getPassThreshold());
		testPlanReportMapper.insertSelective(report);
		return report;
	}


	/**
	 * 报告结果后置处理
	 * @param postParam 后置处理参数
	 * @return 报告
	 */
	public TestPlanReport postHandleReport(TestPlanReportPostParam postParam) {
		/*
		 * 处理报告(执行状态, 结束时间)
		 */
		TestPlanReport planReport = checkReport(postParam.getReportId());
		BeanUtils.copyBean(planReport, postParam);
		/*
		 * TODO: 计算报告通过率, 并对比阈值生成报告结果状态(目前只有功能用例参与计算)
		 */
		TestPlanReportSummaryExample example = new TestPlanReportSummaryExample();
		example.createCriteria().andTestPlanReportIdEqualTo(postParam.getReportId());
		TestPlanReportSummary reportSummary = testPlanReportSummaryMapper.selectByExample(example).get(0);
		DecimalFormat rateFormat = new DecimalFormat("#0.0000");
		rateFormat.setMinimumFractionDigits(4);
		rateFormat.setMaximumFractionDigits(4);
		// 通过的功能用例数
		// TODO: 接口用例, 场景用例
		long functionalCasePassCount = extTestPlanReportMapper.countExecuteSuccessFunctionalCase(postParam.getReportId());
		// 用例总数
		long caseTotal = reportSummary.getFunctionalCaseCount() + reportSummary.getApiCaseCount() + reportSummary.getApiScenarioCount();
		// 通过率 {通过用例数/总用例数}
		double passRate = (functionalCasePassCount == 0 || caseTotal == 0) ? 0.0000 :
				Double.parseDouble(rateFormat.format((double) functionalCasePassCount / (double) caseTotal));
		// FIXME: 后续替换成PASS_COUNT {保留该逻辑, 四舍五入导致的边界值数据展示偏差}
		if (passRate == 0 && functionalCasePassCount > 0) {
			passRate = 0.0001;
		} else if (passRate == 100 && functionalCasePassCount < caseTotal) {
			passRate = 0.9999;
		}
		planReport.setPassRate(passRate);
		// 计划的(执行)结果状态: 通过率 >= 阈值 ? 成功 : 失败
		planReport.setResultStatus(passRate >= planReport.getPassThreshold() ? ReportStatus.SUCCESS.name() : ReportStatus.ERROR.name());
		return planReport;
	}

	/**
	 * 获取报告分析详情
	 * @param reportId 报告ID
	 * @return 报告分析详情
	 */
	public TestPlanReportDetailResponse getReport(String reportId) {
		TestPlanReport planReport = checkReport(reportId);
		TestPlanReportSummaryExample example = new TestPlanReportSummaryExample();
		example.createCriteria().andTestPlanReportIdEqualTo(reportId);
		TestPlanReportSummary reportSummary = testPlanReportSummaryMapper.selectByExample(example).get(0);
		TestPlanReportDetailResponse planReportDetail = new TestPlanReportDetailResponse();
		BeanUtils.copyBean(planReportDetail, planReport);
		int caseTotal = (int) (reportSummary.getFunctionalCaseCount() + reportSummary.getApiCaseCount() + reportSummary.getApiScenarioCount());
		planReportDetail.setCaseTotal(caseTotal);
		planReportDetail.setBugCount(reportSummary.getBugCount().intValue());
		/*
		 * 统计用例执行数据
		 */
		return statisticsCase(planReportDetail);
	}

	/**
	 * 分页查询报告详情-缺陷分页数据
	 * @param request 请求参数
	 * @return 缺陷分页数据
	 */
	public List<BugDTO> listReportDetailBugs(TestPlanReportDetailPageRequest request) {
		List<BugDTO> bugs = extTestPlanReportMapper.listReportBugs(request);
		return bugService.handleCustomField(bugs, request.getProjectId());
	}

	/**
	 * 分页查询报告详情-缺陷分页数据
	 * @param request 请求参数
	 * @return 缺陷分页数据
	 */
	public List<FunctionalCasePageDTO> listReportDetailFunctionalCases(TestPlanReportDetailPageRequest request) {
		return extTestPlanReportMapper.listReportFunctionalCases(request);
	}


	/**
	 * 统计用例执行数据 (目前只统计功能用例)
	 * @param reportDetail 用例详情
	 */
	private TestPlanReportDetailResponse statisticsCase(TestPlanReportDetailResponse reportDetail) {
		// 功能用例 (无误报状态)
		List<CaseStatusCountMap> functionalCaseCountMap = extTestPlanReportMapper.countFunctionalCaseExecuteResult(reportDetail.getId());
		Map<String, Long> functionalCaseResultMap = functionalCaseCountMap.stream().collect(Collectors.toMap(CaseStatusCountMap::getStatus, CaseStatusCountMap::getCount));
		TestPlanReportDetailResponse.CaseCount functionalCaseCount = new TestPlanReportDetailResponse.CaseCount();
		functionalCaseCount.setSuccess(functionalCaseResultMap.getOrDefault(FunctionalCaseExecuteResult.SUCCESS.name(), 0L).intValue());
		functionalCaseCount.setError(functionalCaseResultMap.getOrDefault(FunctionalCaseExecuteResult.ERROR.name(), 0L).intValue());
		functionalCaseCount.setPending(functionalCaseResultMap.getOrDefault(FunctionalCaseExecuteResult.PENDING.name(), 0L).intValue());
		functionalCaseCount.setBlock(functionalCaseResultMap.getOrDefault(FunctionalCaseExecuteResult.BLOCKED.name(), 0L).intValue());


		// TODO: 接口用例, 场景用例

		// FIXME: 目前只有功能用例
		TestPlanReportDetailResponse.CaseCount executeCaseCount = new TestPlanReportDetailResponse.CaseCount();
		executeCaseCount.setSuccess(functionalCaseCount.getSuccess());
		executeCaseCount.setError(functionalCaseCount.getError());
		executeCaseCount.setFakeError(functionalCaseCount.getFakeError());
		executeCaseCount.setPending(functionalCaseCount.getPending());
		executeCaseCount.setBlock(functionalCaseCount.getBlock());

		// 计算执行完成率
		DecimalFormat rateFormat = new DecimalFormat("#0.00");
		rateFormat.setMinimumFractionDigits(2);
		rateFormat.setMaximumFractionDigits(2);
		// 执行完成率 {已执行用例数/总用例数}
		double executeRate = (executeCaseCount.getPending().equals(reportDetail.getCaseTotal()) || reportDetail.getCaseTotal() == 0) ? 0.00 :
				Double.parseDouble(rateFormat.format((double) (reportDetail.getCaseTotal() - executeCaseCount.getPending()) / (double) reportDetail.getCaseTotal()));
		// FIXME: 后续替换成PASS_COUNT {保留该逻辑, 四舍五入导致的边界值数据展示偏差}
		if (executeRate == 0 && reportDetail.getCaseTotal() - executeCaseCount.getPending() > 0) {
			executeRate = 0.01;
		} else if (executeRate == 100 && executeCaseCount.getPending() > 0) {
			executeRate = 99.99;
		}

		// 详情数据
		reportDetail.setExecuteRate(executeRate);
		reportDetail.setFunctionalCount(functionalCaseCount);
		reportDetail.setExecuteCount(executeCaseCount);
		return reportDetail;
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
}
