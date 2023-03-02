package io.metersphere.service;

import io.metersphere.base.domain.ProjectApplication;
import io.metersphere.base.domain.ShareInfo;
import io.metersphere.base.mapper.ShareInfoMapper;
import io.metersphere.base.mapper.ext.BaseShareInfoMapper;
import io.metersphere.commons.constants.ProjectApplicationType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.dto.ShareInfoDTO;
import io.metersphere.i18n.Translator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static io.metersphere.commons.user.ShareUtil.getTimeMills;

@Service
@Transactional(rollbackFor = Exception.class)
public class BaseShareInfoService {

    @Resource
    BaseShareInfoMapper baseShareInfoMapper;
    @Resource
    ShareInfoMapper shareInfoMapper;
    @Resource
    private BaseProjectApplicationService baseProjectApplicationService;

    /**
     * 生成分享连接
     * 如果该数据有连接则，返回已有的连接，不做有效期判断
     *
     * @param request
     * @return
     */
    public ShareInfo generateShareInfo(ShareInfo request) {
        List<ShareInfo> shareInfos = baseShareInfoMapper.selectByShareTypeAndShareApiIdWithBLOBs(request.getShareType(), request.getCustomData());
        if (shareInfos.isEmpty()) {
            return createShareInfo(request);
        } else {
            return shareInfos.get(0);
        }
    }

    public ShareInfo createShareInfo(ShareInfo shareInfo) {
        long createTime = System.currentTimeMillis();
        shareInfo.setId(UUID.randomUUID().toString());
        shareInfo.setCreateTime(createTime);
        shareInfo.setUpdateTime(createTime);
        shareInfoMapper.insert(shareInfo);
        return shareInfo;
    }

    public ShareInfoDTO conversionShareInfoToDTO(ShareInfo apiShare) {
        ShareInfoDTO returnDTO = new ShareInfoDTO();
        if (!StringUtils.isEmpty(apiShare.getCustomData())) {
            String url = "?shareId=" + apiShare.getId();
            returnDTO.setId(apiShare.getId());
            returnDTO.setShareUrl(url);
        }
        return returnDTO;
    }


    public ShareInfo get(String id) {
        return shareInfoMapper.selectByPrimaryKey(id);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void validateExpiredTestPlan(ShareInfo shareInfo, String projectId) {
        // 有效期根据类型从ProjectApplication中获取

        if (shareInfo == null) {
            MSException.throwException(Translator.get("connection_expired"));
        }
        String type = ProjectApplicationType.TRACK_SHARE_REPORT_TIME.toString();

        if (StringUtils.isBlank(type) || Strings.isBlank(projectId)) {
            millisCheck(System.currentTimeMillis() - shareInfo.getUpdateTime(), 1000 * 60 * 60 * 24, shareInfo.getId());
        } else {
            ProjectApplication projectApplication = baseProjectApplicationService.getProjectApplication(projectId, type);
            if (projectApplication.getTypeValue() == null) {
                millisCheck(System.currentTimeMillis() - shareInfo.getUpdateTime(), 1000 * 60 * 60 * 24, shareInfo.getId());
            } else {
                String expr = projectApplication.getTypeValue();
                long timeMills = getTimeMills(shareInfo.getUpdateTime(), expr);
                millisCheck(System.currentTimeMillis(), timeMills, shareInfo.getId());
            }
        }
    }

    private void millisCheck(long compareMillis, long millis, String shareInfoId) {
        if (compareMillis > millis) {
            shareInfoMapper.deleteByPrimaryKey(shareInfoId);
            MSException.throwException(Translator.get("connection_expired"));
        }
    }

}
