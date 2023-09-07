package io.metersphere.controller;

import io.metersphere.api.dto.share.ApiDocumentInfoDTO;
import io.metersphere.api.dto.share.ApiDocumentRequest;
import io.metersphere.api.dto.share.ApiDocumentShareRequest;
import io.metersphere.base.domain.ShareInfo;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.RequestResult;
import io.metersphere.dto.ShareInfoDTO;
import io.metersphere.service.ShareInfoService;
import io.metersphere.service.scenario.ApiScenarioReportService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author song.tianyang
 * @Date 2021/2/5 6:25 下午
 * @Description
 */
@RestController
@RequestMapping(value = "/share")
public class ShareController {
    @Resource
    private ShareInfoService shareInfoService;
    @Resource
    private ApiScenarioReportService apiReportService;

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<ApiDocumentInfoDTO>> list(@RequestBody ApiDocumentRequest apiDocumentRequest, @PathVariable int goPage, @PathVariable int pageSize) {
        return shareInfoService.selectApiInfoByParam(apiDocumentRequest, goPage, pageSize);
    }

    @PostMapping("/doc/export/{goPage}/{pageSize}/{lang}")
    public void exportPageHtml(@RequestBody ApiDocumentRequest apiDocumentRequest, @PathVariable int goPage, @PathVariable int pageSize, @PathVariable String lang, HttpServletResponse response) {
        shareInfoService.exportPageDoc(apiDocumentRequest, goPage, pageSize, response);
    }

    @GetMapping("/get/{id}")
    public ShareInfo get(@PathVariable String id) {
        return shareInfoService.get(id);
    }

    @PostMapping("/generate/api/document")
    public ShareInfoDTO generateApiDocumentShareInfo(@RequestBody ApiDocumentShareRequest request) {
        request.setCreateUserId(SessionUtils.getUserId());
        ShareInfo apiShare = shareInfoService.generateApiDocumentShareInfo(request);
        ShareInfoDTO returnDTO = shareInfoService.conversionShareInfoToDTO(apiShare);
        return returnDTO;
    }

    @GetMapping("/{shareId}/scenario/report/detail/{stepId}")
    public RequestResult selectReportContent(@PathVariable String stepId, @PathVariable String shareId) {
        shareInfoService.validateExpired(shareId);
        return apiReportService.selectReportContent(stepId);
    }
}

@Controller
class ShareApiIndexController {
    @GetMapping("/share-api-report")
    public String shareApiReport() {
        return "share-api-report.html";
    }
}

@Controller
class ShareDocIndexController {
    @GetMapping("/share-document")
    public String shareDocument() {
        return "share-document.html";
    }
}

