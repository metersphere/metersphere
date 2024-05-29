package io.metersphere.plan.service;

import io.metersphere.bug.dto.response.BugDTO;
import io.metersphere.bug.service.BugCommonService;
import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.*;
import io.metersphere.plan.dto.request.*;
import io.metersphere.plan.dto.response.TestPlanReportDetailResponse;
import io.metersphere.plan.dto.response.TestPlanReportPageResponse;
import io.metersphere.plan.enums.TestPlanReportAttachmentSourceType;
import io.metersphere.plan.mapper.*;
import io.metersphere.plan.utils.ModuleTreeUtils;
import io.metersphere.plan.utils.RateCalculateUtils;
import io.metersphere.plugin.platform.dto.SelectOption;
import io.metersphere.sdk.constants.*;
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

import java.util.ArrayList;
import java.util.HashMap;
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
	private TestPlanReportLogService testPlanReportLogService;
	@Resource
	private TestPlanReportNoticeService testPlanReportNoticeService;
	@Resource
	private TestPlanReportSummaryMapper testPlanReportSummaryMapper;
	@Resource
	private TestPlanReportFunctionCaseMapper testPlanReportFunctionCaseMapper;
    @Resource
    private TestPlanReportBugMapper testPlanReportBugMapper;
	@Resource
	private TestPlanReportAttachmentMapper testPlanReportAttachmentMapper;
	@Resource
	private BaseUserMapper baseUserMapper;

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
		TestPlanReport preReport = preGenReport(genPreParam, currentUser, "/test-plan/report/gen");
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
	public TestPlanReport preGenReport(TestPlanReportGenPreParam genParam, String currentUser, String logPath) {
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
		List<TestPlanReportFunctionCase> reportFunctionCases = extTestPlanReportFunctionalCaseMapper.getPlanExecuteCases(genParam.getTestPlanId());
		if (CollectionUtils.isNotEmpty(reportFunctionCases)) {
			// 模块树
			List<TestPlanBaseModule> functionalModules = extTestPlanReportFunctionalCaseMapper.getPlanExecuteCaseModules(genParam.getProjectId());
			Map<String, String> functionalModuleMap = new HashMap<>(functionalModules.size());
			ModuleTreeUtils.genPathMap(functionalModules, functionalModuleMap, new ArrayList<>());
			// 用例等级
			List<String> ids = reportFunctionCases.stream().map(TestPlanReportFunctionCase::getFunctionCaseId).distinct().toList();
			List<SelectOption> options = extTestPlanReportFunctionalCaseMapper.getCasePriorityByIds(ids);
			Map<String, String> casePriorityMap = options.stream().collect(Collectors.toMap(SelectOption::getValue, SelectOption::getText));
			reportFunctionCases.forEach(reportFunctionalCase -> {
				reportFunctionalCase.setId(IDGenerator.nextStr());
				reportFunctionalCase.setTestPlanReportId(reportId);
				reportFunctionalCase.setFunctionCaseModule(functionalModuleMap.getOrDefault(reportFunctionalCase.getFunctionCaseModule(),
						ModuleTreeUtils.MODULE_PATH_PREFIX + reportFunctionalCase.getFunctionCaseModule()));
				reportFunctionalCase.setFunctionCasePriority(casePriorityMap.get(reportFunctionalCase.getFunctionCaseId()));
			});
			// 插入计划功能用例关联数据 -> 报告内容
			TestPlanReportFunctionCaseMapper batchMapper = sqlSession.getMapper(TestPlanReportFunctionCaseMapper.class);
			batchMapper.batchInsert(reportFunctionCases);
		}

		// TODO: 接口用例, 场景报告内容 (与接口报告是否能一致)

		// 计划报告缺陷内容
		List<TestPlanReportBug> reportBugs = extTestPlanReportBugMapper.getPlanBugs(genParam.getTestPlanId());
		// MS处理人会与第三方的值冲突, 分开查询
		List<SelectOption> headerOptions = bugCommonService.getHeaderHandlerOption(genParam.getProjectId());
		Map<String, String> headerHandleUserMap = headerOptions.stream().collect(Collectors.toMap(SelectOption::getValue, SelectOption::getText));
		List<SelectOption> localOptions = bugCommonService.getLocalHandlerOption(genParam.getProjectId());
		Map<String, String> localHandleUserMap = localOptions.stream().collect(Collectors.toMap(SelectOption::getValue, SelectOption::getText));
		Map<String, String> allStatusMap = bugCommonService.getAllStatusMap(genParam.getProjectId());
		reportBugs.forEach(reportBug -> {
			reportBug.setId(IDGenerator.nextStr());
			reportBug.setTestPlanReportId(reportId);
			reportBug.setBugHandleUser(headerHandleUserMap.containsKey(reportBug.getBugHandleUser()) ?
					headerHandleUserMap.get(reportBug.getBugHandleUser()) : localHandleUserMap.get(reportBug.getBugHandleUser()));
			reportBug.setBugStatus(allStatusMap.get(reportBug.getBugStatus()));
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

		// 插入生成报告日志
		testPlanReportLogService.addLog(report, currentUser, genParam.getProjectId(), logPath);
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
		// 通过的功能用例数
		// TODO: 接口用例, 场景用例
		long functionalCasePassCount = extTestPlanReportFunctionalCaseMapper.countExecuteSuccessCase(postParam.getReportId());
		// 用例总数
		long caseTotal = reportSummary.getFunctionalCaseCount() + reportSummary.getApiCaseCount() + reportSummary.getApiScenarioCount();
		// 通过率 {通过用例数/总用例数}
		// FIXME: 后续替换成PASS_COUNT
		planReport.setPassRate(RateCalculateUtils.divWithPrecision((int) functionalCasePassCount, (int) caseTotal, 2));
		// 计划的(执行)结果状态: 通过率 >= 阈值 ? 成功 : 失败
		planReport.setResultStatus(planReport.getPassRate() >= planReport.getPassThreshold() ? ReportStatus.SUCCESS.name() : ReportStatus.ERROR.name());

		testPlanReportMapper.updateByPrimaryKeySelective(planReport);
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
		planReportDetail.setSummary(reportSummary.getSummary());
		/*
		 * 统计用例执行数据
		 */
		return statisticsCase(planReportDetail);
	}

	/**
	 * 更新报告详情
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
	 * @param request 请求参数
	 * @return 缺陷分页数据
	 */
	public List<BugDTO> listReportDetailBugs(TestPlanReportDetailPageRequest request) {
		return extTestPlanReportBugMapper.list(request);
	}

	/**
	 * 分页查询报告详情-功能用例分页数据
	 * @param request 请求参数
	 * @return 缺陷分页数据
	 */
	public List<ReportDetailCasePageDTO> listReportDetailFunctionalCases(TestPlanReportDetailPageRequest request) {
		List<ReportDetailCasePageDTO> functionalCases = extTestPlanReportFunctionalCaseMapper.list(request);
		if (CollectionUtils.isEmpty(functionalCases)) {
			return new ArrayList<>();
		}
		List<String> distinctUserIds = functionalCases.stream().map(ReportDetailCasePageDTO::getExecuteUser).distinct().toList();
		List<OptionDTO> userOptions = baseUserMapper.selectUserOptionByIds(distinctUserIds);
		Map<String, String> userMap = userOptions.stream().collect(Collectors.toMap(OptionDTO::getId, OptionDTO::getName));
		functionalCases.forEach(functionalCase -> functionalCase.setExecuteUser(userMap.getOrDefault(functionalCase.getExecuteUser(), functionalCase.getExecuteUser())));
		return functionalCases;
	}

	/**
	 * 统计用例执行数据 (目前只统计功能用例)
	 * @param reportDetail 用例详情
	 */
	private TestPlanReportDetailResponse statisticsCase(TestPlanReportDetailResponse reportDetail) {
		// 功能用例 (无误报状态)
		List<CaseStatusCountMap> functionalCaseCountMap = extTestPlanReportFunctionalCaseMapper.countExecuteResult(reportDetail.getId());
		Map<String, Long> functionalCaseResultMap = functionalCaseCountMap.stream().collect(Collectors.toMap(CaseStatusCountMap::getStatus, CaseStatusCountMap::getCount));
		TestPlanReportDetailResponse.CaseCount functionalCaseCount = new TestPlanReportDetailResponse.CaseCount();
		functionalCaseCount.setSuccess(functionalCaseResultMap.getOrDefault(ExecStatus.SUCCESS.name(), 0L).intValue());
		functionalCaseCount.setError(functionalCaseResultMap.getOrDefault(ExecStatus.ERROR.name(), 0L).intValue());
		functionalCaseCount.setPending(functionalCaseResultMap.getOrDefault(ExecStatus.PENDING.name(), 0L).intValue());
		functionalCaseCount.setBlock(functionalCaseResultMap.getOrDefault(ExecStatus.BLOCKED.name(), 0L).intValue());

		// TODO: 接口用例, 场景用例

		// FIXME: 目前只有功能用例
		TestPlanReportDetailResponse.CaseCount executeCaseCount = new TestPlanReportDetailResponse.CaseCount();
		executeCaseCount.setSuccess(functionalCaseCount.getSuccess());
		executeCaseCount.setError(functionalCaseCount.getError());
		executeCaseCount.setFakeError(functionalCaseCount.getFakeError());
		executeCaseCount.setPending(functionalCaseCount.getPending());
		executeCaseCount.setBlock(functionalCaseCount.getBlock());

		// 计算执行完成率
		reportDetail.setExecuteRate(RateCalculateUtils.divWithPrecision(reportDetail.getCaseTotal() - executeCaseCount.getPending(), reportDetail.getCaseTotal(), 2));
		// 分析详情数据
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

	/**
	 * 转存报告内容富文本临时文件
	 * @param reportId 报告ID
	 * @param projectId 项目ID
	 * @param uploadFileIds 上传的文件ID集合
	 * @param userId 用户ID
	 * @param source 文件来源
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
	 * @param folder 文件夹
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
				LogUtils.error("上传副文本文件失败：{}",e);
				throw new MSException(Translator.get("file_upload_fail"));
			}
		}
	}
}
