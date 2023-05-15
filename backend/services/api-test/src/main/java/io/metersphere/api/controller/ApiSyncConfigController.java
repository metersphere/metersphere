package io.metersphere.api.controller;

import io.metersphere.api.service.ApiSyncConfigService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sync/config")
public class ApiSyncConfigController {
    @Resource
    private ApiSyncConfigService apiSyncConfigService;

}