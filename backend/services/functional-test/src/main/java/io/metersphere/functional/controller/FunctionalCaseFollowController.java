package io.metersphere.functional.controller;

import io.metersphere.functional.service.FunctionalCaseFollowService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/functional/case/follow")
public class FunctionalCaseFollowController {

    @Resource
    private FunctionalCaseFollowService functionalCaseFollowService;

}