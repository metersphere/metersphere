package io.metersphere.api.controller;

import io.metersphere.api.service.ApiDefinitionFollowService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/definition/follow")
public class ApiDefinitionFollowController {
    @Resource
    private ApiDefinitionFollowService apiDefinitionFollowService;

}