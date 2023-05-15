package io.metersphere.api.controller;

import io.metersphere.api.service.ApiTestCaseService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test/case")
public class ApiTestCaseController {
    @Resource
    private ApiTestCaseService apiTestCaseService;

}