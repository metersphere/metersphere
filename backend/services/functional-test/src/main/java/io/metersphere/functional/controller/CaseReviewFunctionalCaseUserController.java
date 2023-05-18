package io.metersphere.functional.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.metersphere.functional.service.CaseReviewFunctionalCaseUserService;


@RestController
@RequestMapping("/case/review/functional/case/user")
public class CaseReviewFunctionalCaseUserController {

    @Resource
    private CaseReviewFunctionalCaseUserService caseReviewFunctionalCaseUserService;

}