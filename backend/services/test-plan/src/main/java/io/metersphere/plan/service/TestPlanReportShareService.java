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
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.mapper.ShareInfoMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.user.UserDTO;
import io.metersphere.system.mapper.BaseUserMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static io.metersphere.sdk.util.ShareUtil.getTimeMills;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanReportShareService {

    @Resource
    private BaseUserMapper baseUserMapper;
    @Resource
    private ShareInfoMapper shareInfoMapper;
    @Resource
    private TestPlanReportMapper testPlanReportMapper;
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;

    private static final Long DEFAULT = 1000L * 60 * 60 * 24;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void validateExpired(ShareInfo shareInfo) {
        String shareType = shareInfo.getShareType();
        String projectId = "";
        if (StringUtils.equals(shareType, ShareInfoType.TEST_PLAN_SHARE_REPORT.name())) {
            TestPlanReport planReport = testPlanReportMapper.selectByPrimaryKey(new String(shareInfo.getCustomData()));
            if (planReport != null && BooleanUtils.isFalse(planReport.getDeleted())) {
                projectId = planReport.getProjectId();
            }
        }
        if (StringUtils.isBlank(projectId)) {
            throw new MSException(Translator.get("test_plan_report_not_exist"));
        }
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andTypeEqualTo(shareType);
        List<ProjectApplication> projectApplications = projectApplicationMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(projectApplications)) {
            millisCheck(System.currentTimeMillis() - shareInfo.getUpdateTime(), DEFAULT, shareInfo.getId());
        } else {
            String expr = projectApplications.getFirst().getTypeValue();
            long timeMills = getTimeMills(shareInfo.getUpdateTime(), expr);
            millisCheck(System.currentTimeMillis(), timeMills, shareInfo.getId());
        }
    }

    /**
     * 生成计划报告分享信息
     *
     * @param shareRequest 分享请求参数
     * @param currentUser  当前用户
     * @return 计划报告分享信息
     */
    public TestPlanShareInfo gen(TestPlanReportShareRequest shareRequest, String currentUser) {
        UserDTO userDTO = baseUserMapper.selectById(currentUser);
        String lang = Optional.ofNullable(userDTO)
                .map(UserDTO::getLanguage)
                .orElse(LocaleContextHolder.getLocale().toString().split("_#")[0]);

        ShareInfo request = new ShareInfo();
        BeanUtils.copyBean(request, shareRequest);
        request.setLang(lang);
        request.setCreateUser(currentUser);
        request.setCustomData(shareRequest.getReportId().getBytes());
        request.setShareType(ShareInfoType.TEST_PLAN_SHARE_REPORT.name());

        return conversionShareInfoToDTO(createShareInfo(request));
    }

    /**
     * 获取分享信息
     *
     * @param id 分享ID
     * @return 分享信息
     */
    public TestPlanShareResponse get(String id) {
        TestPlanShareResponse dto = new TestPlanShareResponse();
        ShareInfo shareInfo = shareInfoMapper.selectByPrimaryKey(id);
        // 无分享记录, 过期直接返回
        if (shareInfo == null) {
            dto.setExpired(true);
            return dto;
        }
        BeanUtils.copyBean(dto, shareInfo);
        dto.setReportId(new String(shareInfo.getCustomData()));
        // 检查报告ID是否存在
        TestPlanReport testPlanReport = testPlanReportMapper.selectByPrimaryKey(dto.getReportId());
        if (testPlanReport == null || testPlanReport.getDeleted()) {
            dto.setDeleted(true);
        }
        // 报告正常, 校验分享时间是否过期
        if (!dto.isDeleted()) {
            try {
                validateExpired(shareInfo);
            } catch (Exception e) {
                dto.setExpired(true);
            }
        }
        return dto;
    }

    /**
     * 获取项目计划报告分享有效期
     *
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
     *
     * @param id 分享ID
     * @return 分享资源信息
     */
    public ShareInfo checkResource(String id) {
        ShareInfo shareInfo = shareInfoMapper.selectByPrimaryKey(id);
        if (shareInfo == null) {
            throw new RuntimeException(Translator.get("connection_expired"));
        }
        return shareInfo;
    }

    /**
     * 创建分享信息
     *
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
     *
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

    /**
     * 校验时间是否过期
     *
     * @param compareMillis 比较时间
     * @param millis        分享时间
     * @param shareInfoId   分享ID
     */
    private void millisCheck(long compareMillis, long millis, String shareInfoId) {
        if (compareMillis > millis) {
            shareInfoMapper.deleteByPrimaryKey(shareInfoId);
            throw new MSException(Translator.get("connection_expired"));
        }
    }
}
