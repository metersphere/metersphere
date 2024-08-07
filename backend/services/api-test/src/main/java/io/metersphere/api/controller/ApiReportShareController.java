package io.metersphere.api.controller;

import io.metersphere.api.dto.share.ApiReportShareDTO;
import io.metersphere.api.dto.share.ApiReportShareRequest;
import io.metersphere.api.dto.share.ShareInfoDTO;
import io.metersphere.api.service.ApiReportShareService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.validation.groups.Created;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/report/share")
@Tag(name = "接口测试-接口报告-分享")
public class ApiReportShareController {

    @Resource
    private ApiReportShareService apiReportShareService;

    @PostMapping("/gen")
    @Operation(summary = "接口测试-接口报告-生成分享链接")
    @RequiresPermissions(PermissionConstants.PROJECT_API_REPORT_SHARE)
    public ShareInfoDTO generateShareInfo(@Validated(Created.class) @RequestBody ApiReportShareRequest request) {
        return apiReportShareService.gen(request, SessionUtils.getUserId());
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "接口测试-接口报告-获取分享链接")
    public ApiReportShareDTO get(@PathVariable String id) {
        return apiReportShareService.get(id);
    }

    @GetMapping("/get-share-time/{id}")
    @Operation(summary = "接口测试-接口报告-获取分享链接的有效时间")
    public String getShareTime(@PathVariable String id) {
        return apiReportShareService.getShareTime(id);
    }


}
