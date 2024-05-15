package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlanReport;
import io.metersphere.plan.dto.TestPlanShareInfo;
import io.metersphere.plan.dto.request.TestPlanReportShareRequest;
import io.metersphere.plan.dto.response.TestPlanShareResponse;
import io.metersphere.plan.mapper.TestPlanReportMapper;
import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.domain.ProjectApplicationExample;
import io.metersphere.project.mapper.ProjectApplicationMapper;
import io.metersphere.sdk.constants.ShareInfoType;
import io.metersphere.sdk.domain.ShareInfo;
import io.metersphere.sdk.mapper.ShareInfoMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.user.UserDTO;
import io.metersphere.system.mapper.BaseUserMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestPlanReportShareService {

	@Resource
	private BaseUserMapper baseUserMapper;
	@Resource
	private ShareInfoMapper shareInfoMapper;
	@Resource
	private TestPlanReportMapper testPlanReportMapper;
	@Resource
	private ProjectApplicationMapper projectApplicationMapper;

	/**
	 * 生成计划报告分享信息
	 * @param shareRequest 分享请求参数
	 * @param currentUser 当前用户
	 * @return 计划报告分享信息
	 */
	public TestPlanShareInfo gen(TestPlanReportShareRequest shareRequest, String currentUser) {
		UserDTO userDTO = baseUserMapper.selectById(currentUser);
		String lang = userDTO.getLanguage() == null ? LocaleContextHolder.getLocale().toString() : userDTO.getLanguage();
		ShareInfo request = new ShareInfo();
		BeanUtils.copyBean(request, shareRequest);
		request.setLang(lang);
		request.setCreateUser(currentUser);
		request.setCustomData(shareRequest.getReportId().getBytes());
		request.setShareType(ShareInfoType.TEST_PLAN_SHARE_REPORT.name());
		ShareInfo shareInfo = createShareInfo(request);
		return conversionShareInfoToDTO(shareInfo);
	}

	/**
	 * 获取分享信息
	 * @param id 分享ID
	 * @return 分享信息
	 */
	public TestPlanShareResponse get(String id) {
		ShareInfo shareInfo = checkResource(id);
		TestPlanShareResponse dto = new TestPlanShareResponse();
		BeanUtils.copyBean(dto, shareInfo);
		dto.setReportId(new String(shareInfo.getCustomData()));
		//检查报告ID是否存在
		dto.setDeleted(false);
		TestPlanReport testPlanReport = testPlanReportMapper.selectByPrimaryKey(dto.getReportId());
		if (testPlanReport == null) {
			dto.setDeleted(true);
		}
		return dto;
	}

	/**
	 * 获取项目计划报告分享有效期
	 * @param projectId 项目ID
	 * @return 有效期
	 */
	public String getShareTime(String projectId) {
		ProjectApplicationExample example = new ProjectApplicationExample();
		example.createCriteria().andProjectIdEqualTo(projectId).andTypeEqualTo(ShareInfoType.TEST_PLAN_SHARE_REPORT.name());
		List<ProjectApplication> projectApplications = projectApplicationMapper.selectByExample(example);
		if (CollectionUtils.isEmpty(projectApplications)) {
			return "1D";
		} else {
			return projectApplications.getFirst().getTypeValue();
		}
	}

	/**
	 * 校验分享的资源是否存在
	 * @param id 分享ID
	 * @return 分享资源信息
	 */
	private ShareInfo checkResource(String id) {
		ShareInfo shareInfo = shareInfoMapper.selectByPrimaryKey(id);
		if (shareInfo == null) {
			throw new RuntimeException(Translator.get("connection_expired"));
		}
		return shareInfo;
	}

	/**
	 * 创建分享信息
	 * @param shareInfo 分享的信息
	 * @return 分享信息
	 */
	private ShareInfo createShareInfo(ShareInfo shareInfo) {
		long createTime = System.currentTimeMillis();
		shareInfo.setId(IDGenerator.nextStr());
		shareInfo.setCreateTime(createTime);
		shareInfo.setUpdateTime(createTime);
		shareInfoMapper.insert(shareInfo);
		return shareInfo;
	}

	/**
	 * 设置分享信息并返回
	 * @param shareInfo 分享信息
	 * @return 计划报告分享信息返回
	 */
	private TestPlanShareInfo conversionShareInfoToDTO(ShareInfo shareInfo) {
		TestPlanShareInfo testPlanShareInfo = new TestPlanShareInfo();
		if (shareInfo.getCustomData() != null) {
			String url = "?shareId=" + shareInfo.getId();
			testPlanShareInfo.setId(shareInfo.getId());
			testPlanShareInfo.setShareUrl(url);
		}
		return testPlanShareInfo;
	}
}
