package io.metersphere.api.service;

import io.metersphere.api.constants.ApiDefinitionDocType;
import io.metersphere.api.dto.definition.ApiDefinitionDocDTO;
import io.metersphere.api.dto.definition.ApiDefinitionDocRequest;
import io.metersphere.api.dto.share.ShareInfoDTO;
import io.metersphere.api.mapper.ExtShareInfoMapper;
import io.metersphere.api.service.definition.ApiDefinitionService;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.sdk.constants.ShareInfoType;
import io.metersphere.sdk.domain.ShareInfo;
import io.metersphere.sdk.mapper.ShareInfoMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.dto.sdk.SessionUser;
import jakarta.annotation.Resource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author: LAN
 * @date: 2023/12/27 14:47
 * @version: 1.0
 */
@Service
public class ApiShareService {

    @Resource
    ShareInfoMapper shareInfoMapper;

    @Resource
    ExtShareInfoMapper extShareInfoMapper;

    @Resource
    ApiDefinitionService apiDefinitionService;
    @Resource
    ApiReportShareService apiReportShareService;

    /**
     * 生成 api 接口文档分享信息
     * 根据要分享的类型来进行完全匹配搜索。
     * 搜索的到就返回那条数据，搜索不到就新增一条信息
     */
    public ShareInfoDTO genApiDocShareInfo(ApiDefinitionDocRequest request, SessionUser user) {
        ShareInfo shareInfoRequest = new ShareInfo();
        String customData = genCustomData(request, shareInfoRequest);
        String lang = user.getLanguage() == null ? LocaleContextHolder.getLocale().toString().split("_#")[0] : user.getLanguage();

        Optional.ofNullable(customData)
                .ifPresent(data -> {
                    BeanUtils.copyBean(shareInfoRequest, request);
                    shareInfoRequest.setCreateUser(user.getId());
                    shareInfoRequest.setLang(lang);
                    shareInfoRequest.setCustomData(data.getBytes());
                    shareInfoRequest.setProjectId(request.getProjectId());
                });
        ShareInfo shareInfo = Optional.ofNullable(customData)
                .map(data -> genShareInfo(shareInfoRequest))
                .orElse(new ShareInfo());

        return apiReportShareService.conversionShareInfoToDTO(shareInfo);
    }

    private String genCustomData(ApiDefinitionDocRequest request, ShareInfo shareInfoRequest) {
        String customData = null;
        if (ApiDefinitionDocType.ALL.name().equals(request.getType()) || ApiDefinitionDocType.MODULE.name().equals(request.getType())) {
            customData = JSON.toJSONString(request);
            shareInfoRequest.setShareType(ShareInfoType.BATCH.name());
        } else if (ApiDefinitionDocType.API.name().equals(request.getType())) {
            apiDefinitionService.checkApiDefinition(request.getApiId());
            customData = JSON.toJSONString(request);
            shareInfoRequest.setShareType(ShareInfoType.SINGLE.name());
        }

        return customData;
    }


    /**
     * 生成分享连接
     * 如果该数据有连接则返回已有的连接，不做有效期判断, 反之创建链接
     *
     * @param request 分享请求内容
     * @return 分享数据
     */
    public ShareInfo genShareInfo(ShareInfo request) {
        List<ShareInfo> shareInfos = extShareInfoMapper.selectByShareTypeAndShareApiIdWithBLOBs(request.getShareType(), request.getCustomData(), request.getLang());
        return shareInfos.isEmpty() ? apiReportShareService.createShareInfo(request) : shareInfos.getFirst();
    }


    public ApiDefinitionDocDTO shareDocView(String shareId) {
        ShareInfo shareInfo = shareInfoMapper.selectByPrimaryKey(shareId);
        ApiDefinitionDocRequest apiDefinitionDocRequest = ApiDataUtils.parseObject(new String(shareInfo.getCustomData()), ApiDefinitionDocRequest.class);
        return apiDefinitionService.getDocInfo(apiDefinitionDocRequest);
    }
}
