package io.metersphere.service;

import io.metersphere.base.domain.ShareInfo;
import io.metersphere.base.mapper.ShareInfoMapper;
import io.metersphere.base.mapper.ext.BaseShareInfoMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.dto.ShareInfoDTO;
import io.metersphere.i18n.Translator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class BaseShareInfoService {

    @Resource
    BaseShareInfoMapper baseShareInfoMapper;
    @Resource
    ShareInfoMapper shareInfoMapper;

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

    public void validateExpired(String shareId) {
        ShareInfo shareInfo = shareInfoMapper.selectByPrimaryKey(shareId);
        this.validateExpired(shareInfo);
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
        // todo check
        millisCheck(System.currentTimeMillis() - shareInfo.getUpdateTime(), 1000 * 60 * 60 * 24, shareInfo.getId());
    }

    private void millisCheck(long compareMillis, long millis, String shareInfoId) {
        if (compareMillis > millis) {
            shareInfoMapper.deleteByPrimaryKey(shareInfoId);
            MSException.throwException(Translator.get("connection_expired"));
        }
    }

}
