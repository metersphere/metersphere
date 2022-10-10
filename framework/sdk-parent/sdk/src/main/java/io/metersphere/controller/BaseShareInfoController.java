package io.metersphere.controller;

import io.metersphere.base.domain.ShareInfo;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.ShareInfoDTO;
import io.metersphere.service.BaseShareInfoService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/share")
public class BaseShareInfoController {

    @Resource
    BaseShareInfoService baseShareInfoService;

    @PostMapping("/generate/expired")
    public ShareInfoDTO generateShareInfo(@RequestBody ShareInfo request) {
        request.setCreateUserId(SessionUtils.getUserId());
        ShareInfo shareInfo = baseShareInfoService.createShareInfo(request);
        return baseShareInfoService.conversionShareInfoToDTO(shareInfo);
    }

    @GetMapping("/info/get/{id}")
    public ShareInfo get(@PathVariable String id) {
        return baseShareInfoService.get(id);
    }
}
