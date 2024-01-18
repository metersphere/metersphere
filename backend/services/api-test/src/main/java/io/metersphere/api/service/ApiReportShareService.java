package io.metersphere.api.service;

import io.metersphere.api.constants.ShareInfoType;
import io.metersphere.api.domain.ApiReport;
import io.metersphere.api.dto.share.ShareInfoDTO;
import io.metersphere.api.mapper.ApiReportMapper;
import io.metersphere.api.mapper.ApiScenarioReportMapper;
import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.domain.ProjectApplicationExample;
import io.metersphere.project.mapper.ProjectApplicationMapper;
import io.metersphere.sdk.domain.ShareInfo;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.mapper.ShareInfoMapper;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.SessionUser;
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

import static io.metersphere.sdk.util.ShareUtil.getTimeMills;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiReportShareService {
    @Resource
    private ApiReportMapper apiReportMapper;
    @Resource
    private ShareInfoMapper shareInfoMapper;
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;

    private static final Long DEFAULT = 1000L * 60 * 60 * 24 * 30;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void validateExpired(ShareInfo shareInfo) {
        String shareType = shareInfo.getShareType();
        String projectId = "";
        if (StringUtils.equals(shareType, ShareInfoType.API_SHARE_REPORT.name())) {
            ApiReport apiReport = apiReportMapper.selectByPrimaryKey(new String(shareInfo.getCustomData()));
            if (apiReport != null && BooleanUtils.isFalse(apiReport.getDeleted())) {
                projectId = apiReport.getProjectId();
            } /*else {
                ApiScenarioReport result = apiScenarioReportMapper.selectByPrimaryKey(new String(shareInfo.getCustomData()));
                if (result != null && BooleanUtils.isFalse(result.getDeleted())) {
                    projectId = result.getProjectId();
                }
            }*/
        }
        if (StringUtils.isBlank(projectId)) {
            throw new MSException(Translator.get("api_case_report_not_exist"));
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

    private void millisCheck(long compareMillis, long millis, String shareInfoId) {
        if (compareMillis > millis) {
            shareInfoMapper.deleteByPrimaryKey(shareInfoId);
            throw new MSException(Translator.get("connection_expired"));
        }
    }

    public ShareInfo createShareInfo(ShareInfo shareInfo) {
        long createTime = System.currentTimeMillis();
        shareInfo.setId(IDGenerator.nextStr());
        shareInfo.setCreateTime(createTime);
        shareInfo.setUpdateTime(createTime);
        shareInfoMapper.insert(shareInfo);
        return shareInfo;
    }

    public ShareInfoDTO conversionShareInfoToDTO(ShareInfo shareInfo) {
        ShareInfoDTO returnDTO = new ShareInfoDTO();
        if (null != shareInfo.getCustomData()) {
            String url = "?shareId=" + shareInfo.getId();
            returnDTO.setId(shareInfo.getId());
            returnDTO.setShareUrl(url);
        }
        return returnDTO;
    }

    public ShareInfoDTO gen(ShareInfo request, SessionUser user) {
        String lang = user.getLanguage() == null ? LocaleContextHolder.getLocale().toString() : user.getLanguage();
        request.setLang(lang);
        request.setCreateUser(user.getId());
        ShareInfo shareInfo = createShareInfo(request);
        return conversionShareInfoToDTO(shareInfo);
    }

    public ShareInfo checkResource(String id) {
        ShareInfo shareInfo = shareInfoMapper.selectByPrimaryKey(id);
        if (shareInfo == null) {
            throw new RuntimeException(Translator.get("connection_expired"));
        }
        return shareInfo;
    }

    public ShareInfo get(String id) {
        return checkResource(id);
    }
}
