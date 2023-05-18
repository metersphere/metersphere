package io.metersphere.functional.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.metersphere.functional.service.CaseReviewUserService;


@RestController
@RequestMapping("/case/review/user")
public class CaseReviewUserController {

    @Resource
    private CaseReviewUserService caseReviewUserService;

}