package io.metersphere.api.service;

import io.metersphere.api.domain.*;
import io.metersphere.api.mapper.*;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class CleanupApiReportDetailServiceImpl {

	@Resource
	private ApiReportMapper apiReportMapper;
	@Resource
	private ApiReportStepMapper apiReportStepMapper;
	@Resource
	private ApiReportDetailMapper apiReportDetailMapper;
	@Resource
	private ApiReportLogMapper apiReportLogMapper;
	@Resource
	private ApiScenarioReportMapper apiScenarioReportMapper;
	@Resource
	private ApiScenarioReportStepMapper apiScenarioReportStepMapper;
	@Resource
	private ApiScenarioReportDetailMapper apiScenarioReportDetailMapper;
	@Resource
	private ApiScenarioReportLogMapper apiScenarioReportLogMapper;
	@Resource
	private ApiScenarioReportDetailBlobMapper apiScenarioReportDetailBlobMapper;
	@Resource
	private ApiReportRelateTaskMapper apiReportRelateTaskMapper;

	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
	public void cleanApiReportOrDetail(List<String> cleanIds) {
		ApiReportExample reportExample = new ApiReportExample();
		reportExample.createCriteria().andIdIn(cleanIds);
		ApiReport report = new ApiReport();
		report.setDeleted(true);
		apiReportMapper.updateByExampleSelective(report, reportExample);
		// 任务执行结果存在报告，明细做保留
		List<String> taskReportIds = getTaskReportIds(cleanIds);
		cleanIds.removeAll(taskReportIds);
		deleteApiReportDetail(cleanIds);
	}

	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
	public void cleanScenarioReportOrDetail(List<String> cleanIds) {
		ApiScenarioReportExample reportExample = new ApiScenarioReportExample();
		reportExample.createCriteria().andIdIn(cleanIds);
		ApiScenarioReport report = new ApiScenarioReport();
		report.setDeleted(true);
		apiScenarioReportMapper.updateByExampleSelective(report, reportExample);
		// 任务执行结果存在报告，明细做保留
		List<String> taskReportIds = getTaskReportIds(cleanIds);
		cleanIds.removeAll(taskReportIds);
		deleteScenarioReportDetail(cleanIds);
	}

	/**
	 * 获取任务报告ID
	 *
	 * @param reportIds 报告ID集合
	 * @return 任务报告ID集合
	 */
	private List<String> getTaskReportIds(List<String> reportIds) {
		ApiReportRelateTaskExample example = new ApiReportRelateTaskExample();
		example.createCriteria().andReportIdIn(reportIds);
		List<ApiReportRelateTask> relateTasks = apiReportRelateTaskMapper.selectByExample(example);
		return relateTasks.stream().map(ApiReportRelateTask::getReportId).toList();
	}

	/**
	 * 清理接口报告明细
	 * @param ids 报告ID集合
	 */
	private void deleteApiReportDetail(List<String> ids) {
		if (CollectionUtils.isNotEmpty(ids)) {
			ApiReportStepExample stepExample = new ApiReportStepExample();
			stepExample.createCriteria().andReportIdIn(ids);
			apiReportStepMapper.deleteByExample(stepExample);
			ApiReportDetailExample detailExample = new ApiReportDetailExample();
			detailExample.createCriteria().andReportIdIn(ids);
			apiReportDetailMapper.deleteByExample(detailExample);
			ApiReportLogExample logExample = new ApiReportLogExample();
			logExample.createCriteria().andReportIdIn(ids);
			apiReportLogMapper.deleteByExample(logExample);
		}
	}

	/**
	 * 清理场景报告明细
	 * @param ids 报告ID集合
	 */
	private void deleteScenarioReportDetail(List<String> ids) {
		if (CollectionUtils.isNotEmpty(ids)) {
			ApiScenarioReportStepExample stepExample = new ApiScenarioReportStepExample();
			stepExample.createCriteria().andReportIdIn(ids);
			apiScenarioReportStepMapper.deleteByExample(stepExample);
			ApiScenarioReportDetailExample detailExample = new ApiScenarioReportDetailExample();
			detailExample.createCriteria().andReportIdIn(ids);
			apiScenarioReportDetailMapper.deleteByExample(detailExample);
			ApiScenarioReportDetailBlobExample blobExample = new ApiScenarioReportDetailBlobExample();
			blobExample.createCriteria().andReportIdIn(ids);
			apiScenarioReportDetailBlobMapper.deleteByExample(blobExample);
			ApiScenarioReportLogExample logExample = new ApiScenarioReportLogExample();
			logExample.createCriteria().andReportIdIn(ids);
			apiScenarioReportLogMapper.deleteByExample(logExample);
		}
	}
}
