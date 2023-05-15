package io.metersphere.api.controller;

import io.metersphere.api.service.ApiFakeErrorConfigService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fake/error/config")
public class ApiFakeErrorConfigController {
    @Resource
    private ApiFakeErrorConfigService apiFakeErrorConfigService;

}