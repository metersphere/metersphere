package io.metersphere.service;

import io.metersphere.base.domain.ShareInfo;
import io.metersphere.base.mapper.ShareInfoMapper;
import io.metersphere.base.mapper.ext.BaseShareInfoMapper;
import io.metersphere.dto.ShareInfoDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
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
}
