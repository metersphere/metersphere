package io.metersphere.api.controller;

import io.metersphere.api.dto.share.ApiReportShareDTO;
import io.metersphere.api.dto.share.ApiReportShareRequest;
import io.metersphere.api.dto.share.ShareInfoDTO;
import io.metersphere.api.service.ApiReportShareService;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.validation.groups.Created;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping(value = "/api/report/share")
@Tag(name = "接口测试-接口报告-分享")
public class ApiReportShareController {

    @Resource
    private ApiReportShareService apiReportShareService;

    @PostMapping("/gen")
    public ShareInfoDTO generateShareInfo(@Validated(Created.class) @RequestBody ApiReportShareRequest request) {
        return apiReportShareService.gen(request, Objects.requireNonNull(SessionUtils.getUser()));
    }

    @GetMapping("/get/{id}")
    public ApiReportShareDTO get(@PathVariable String id) {
        return apiReportShareService.get(id);
    }


}
