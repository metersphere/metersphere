package io.metersphere.bug.service;

import io.metersphere.bug.domain.*;
import io.metersphere.bug.mapper.*;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.service.CleanupProjectResourceService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CleanupBugResourceService implements CleanupProjectResourceService {

	@Resource
	private BugMapper bugMapper;
	@Resource
	private BugContentMapper bugContentMapper;
	@Resource
	private BugFollowerMapper bugFollowerMapper;
	@Resource
	private BugLocalAttachmentMapper bugLocalAttachmentMapper;
	@Resource
	private BugCommentMapper bugCommentMapper;
	@Resource
	private BugCustomFieldMapper bugCustomFieldMapper;
	@Resource
	private BugRelationCaseMapper bugRelationCaseMapper;

	@Async
	@Override
	public void deleteResources(String projectId) {
		LogUtils.info("删除当前项目[" + projectId + "]相关缺陷测试资源");
		List<String> deleteIds = getBugIds(projectId);
		if (CollectionUtils.isNotEmpty(deleteIds)) {
			// 清理缺陷
			deleteBug(deleteIds);
			// 清理缺陷内容
			deleteBugContent(deleteIds);
			// 清理缺陷关注人信息
			deleteBugFollower(deleteIds);
			// 清理缺陷本地附件
			deleteBugLocalAttachment(deleteIds);
			// 清理缺陷评论
			deleteBugComment(deleteIds);
			// 清理缺陷自定义字段
			deleteBugCustomField(deleteIds);
			// 清理缺陷关联用例
			deleteBugRelateCase(deleteIds);
		}
	}

	@Async
	@Override
	public void cleanReportResources(String projectId) {
		LogUtils.info("清理当前项目[" + projectId + "]相关缺陷测试报告资源");
	}

	private List<String> getBugIds(String projectId) {
		BugExample example = new BugExample();
		example.createCriteria().andProjectIdEqualTo(projectId);
		List<Bug> bugs = bugMapper.selectByExample(example);
		return bugs.stream().map(Bug::getId).toList();
	}

	private void deleteBug(List<String> bugIds) {
		BugExample example = new BugExample();
		example.createCriteria().andIdIn(bugIds);
		bugMapper.deleteByExample(example);
	}

	private void deleteBugContent(List<String> bugIds) {
		BugContentExample example = new BugContentExample();
		example.createCriteria().andBugIdIn(bugIds);
		bugContentMapper.deleteByExample(example);
	}

	private void deleteBugFollower(List<String> bugIds) {
		BugFollowerExample example = new BugFollowerExample();
		example.createCriteria().andBugIdIn(bugIds);
		bugFollowerMapper.deleteByExample(example);
	}

	private void deleteBugLocalAttachment(List<String> bugIds) {
		BugLocalAttachmentExample example = new BugLocalAttachmentExample();
		example.createCriteria().andBugIdIn(bugIds);
		bugLocalAttachmentMapper.deleteByExample(example);
	}

	private void deleteBugComment(List<String> bugIds) {
		BugCommentExample example = new BugCommentExample();
		example.createCriteria().andBugIdIn(bugIds);
		bugCommentMapper.deleteByExample(example);
	}

	private void deleteBugCustomField(List<String> bugIds) {
		BugCustomFieldExample example = new BugCustomFieldExample();
		example.createCriteria().andBugIdIn(bugIds);
		bugCustomFieldMapper.deleteByExample(example);
	}

	private void deleteBugRelateCase(List<String> bugIds) {
		BugRelationCaseExample example = new BugRelationCaseExample();
		example.createCriteria().andBugIdIn(bugIds);
		bugRelationCaseMapper.deleteByExample(example);
	}
}
