package io.metersphere.functional.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.metersphere.functional.service.FunctionalCaseTestService;


@RestController
@RequestMapping("/functional/case/test")
public class FunctionalCaseTestController {

    @Resource
    private FunctionalCaseTestService functionalCaseTestService;
	
}