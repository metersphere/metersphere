package io.metersphere.api.controller;

import io.metersphere.api.service.ApiTestCaseBlobService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test/case/blob")
public class ApiTestCaseBlobController {
    @Resource
    private ApiTestCaseBlobService apiTestCaseBlobService;

}