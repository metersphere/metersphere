package io.metersphere.api.controller;

import io.metersphere.api.dto.share.ApiDocumentInfoDTO;
import io.metersphere.api.dto.share.ApiDocumentRequest;
import io.metersphere.api.dto.share.ApiDocumentShareRequest;
import io.metersphere.api.dto.share.ShareInfoDTO;
import io.metersphere.api.service.ShareInfoService;
import io.metersphere.base.domain.ReportStatisticsWithBLOBs;
import io.metersphere.base.domain.ShareInfo;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.reportstatistics.dto.ReportStatisticsSaveRequest;
import io.metersphere.reportstatistics.service.ReportStatisticsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author song.tianyang
 * @Date 2021/2/5 6:25 下午
 * @Description
 */
@RestController
@RequestMapping(value = "/share/info")
public class ShareInfoController {
    @Resource
    ShareInfoService shareInfoService;
    @Resource
    ReportStatisticsService reportStatisticsService;

    @PostMapping("/selectApiInfoByParam/{goPage}/{pageSize}")
    public Pager<List<ApiDocumentInfoDTO>> list(@RequestBody ApiDocumentRequest apiDocumentRequest, @PathVariable int goPage, @PathVariable int pageSize) {
        return shareInfoService.selectApiInfoByParam(apiDocumentRequest, goPage, pageSize);
    }

    @GetMapping("/get/{id}")
    public ShareInfo get(@PathVariable String id) {
        return shareInfoService.get(id);
    }

    @PostMapping("/generateApiDocumentShareInfo")
    public ShareInfoDTO generateApiDocumentShareInfo(@RequestBody ApiDocumentShareRequest request) {
        request.setCreateUserId(SessionUtils.getUserId());
        ShareInfo apiShare = shareInfoService.generateApiDocumentShareInfo(request);
        ShareInfoDTO returnDTO = shareInfoService.conversionShareInfoToDTO(apiShare);
        return returnDTO;
    }

    @PostMapping("/generateShareInfoWithExpired")
    public ShareInfoDTO generateShareInfo(@RequestBody ShareInfo request) {
        request.setCreateUserId(SessionUtils.getUserId());
        ShareInfo apiShare = shareInfoService.createShareInfo(request);
        ShareInfoDTO returnDTO = shareInfoService.conversionShareInfoToDTO(apiShare);
        return returnDTO;
    }

    @PostMapping("/selectHistoryReportById")
    public ReportStatisticsWithBLOBs selectById(@RequestBody ReportStatisticsSaveRequest request) {
        return reportStatisticsService.selectById(request.getId());
    }
}
