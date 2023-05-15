package io.metersphere.api.controller;

import io.metersphere.api.service.ApiTestCaseFollowService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test/case/follow")
public class ApiTestCaseFollowController {
    @Resource
    private ApiTestCaseFollowService apiTestCaseFollowService;

}