package io.metersphere.functional.controller;


import io.metersphere.functional.service.FunctionalCaseModuleService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/functional/case/Module")
public class FunctionalCaseModuleController {

    @Resource
    private FunctionalCaseModuleService functionalCaseModuleService;

}