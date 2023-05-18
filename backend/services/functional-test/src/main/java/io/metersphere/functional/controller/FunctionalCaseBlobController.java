package io.metersphere.functional.controller;

import io.metersphere.functional.service.FunctionalCaseBlobService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/functional/case/blob")
public class FunctionalCaseBlobController {

    @Resource
    private FunctionalCaseBlobService functionalCaseBlobService;
}
