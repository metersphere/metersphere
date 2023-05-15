package io.metersphere.api.controller;

import io.metersphere.api.service.ApiScenarioFollowService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scenario/follow")
public class ApiScenarioFollowController {
    @Resource
    private ApiScenarioFollowService apiScenarioFollowService;

}