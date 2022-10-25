package io.metersphere.service;

import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.base.domain.ProjectApplication;
import io.metersphere.base.domain.ShareInfo;
import io.metersphere.base.mapper.LoadTestReportMapper;
import io.metersphere.base.mapper.ShareInfoMapper;
import io.metersphere.commons.constants.ProjectApplicationType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.i18n.Translator;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static io.metersphere.commons.user.ShareUtil.getTimeMills;

@Service
public class ShareInfoService {
    @Resource
    private ShareInfoMapper shareInfoMapper;
    @Resource
    private BaseProjectApplicationService baseProjectApplicationService;
    @Resource
    private LoadTestReportMapper loadTestReportMapper;

    public void validateExpired(String shareId) {
        ShareInfo shareInfo = shareInfoMapper.selectByPrimaryKey(shareId);
        this.validateExpired(shareInfo);
    }

    public void validate(String shareId, String customData) {
        ShareInfo shareInfo = shareInfoMapper.selectByPrimaryKey(shareId);
        validateExpired(shareInfo);
        if (shareInfo == null) {
            MSException.throwException("ShareInfo not exist!");
        } else {
            if (!StringUtils.equals(customData, shareInfo.getCustomData())) {
                MSException.throwException("ShareInfo validate failure!");
            }
        }
    }

    /**
     * 不加入事务，抛出异常不回滚
     * 若在当前类中调用请使用如下方式调用，否则该方法的事务注解不生效
     * ShareInfoService shareInfoService = CommonBeanFactory.getBean(ShareInfoService.class);
     * shareInfoService.validateExpired(shareInfo);
     *
     * @param shareInfo
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void validateExpired(ShareInfo shareInfo) {
        // 有效期根据类型从ProjectApplication中获取

        if (shareInfo == null) {
            MSException.throwException(Translator.get("connection_expired"));
        }
        String type = ProjectApplicationType.PERFORMANCE_SHARE_REPORT_TIME.toString();
        String projectId = "";
        LoadTestReportWithBLOBs loadTestReportWithBLOBs = loadTestReportMapper.selectByPrimaryKey(shareInfo.getCustomData());
        if (loadTestReportWithBLOBs != null) {
            projectId = loadTestReportWithBLOBs.getProjectId();
        }
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
