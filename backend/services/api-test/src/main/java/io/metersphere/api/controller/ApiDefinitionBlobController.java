package io.metersphere.api.controller;

import io.metersphere.api.service.ApiDefinitionBlobService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/definition/blob")
public class ApiDefinitionBlobController {
    @Resource
    private ApiDefinitionBlobService apiDefinitionBlobService;

}