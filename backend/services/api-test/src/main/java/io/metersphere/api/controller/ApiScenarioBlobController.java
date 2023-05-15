package io.metersphere.api.controller;

import io.metersphere.api.service.ApiScenarioBlobService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scenario/blob")
public class ApiScenarioBlobController {
    @Resource
    private ApiScenarioBlobService apiScenarioBlobService;

}