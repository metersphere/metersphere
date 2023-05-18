package io.metersphere.functional.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.metersphere.functional.service.CaseReviewFunctionalCaseService;


@RestController
@RequestMapping("/case/review/functional/case")
public class CaseReviewFunctionalCaseController {

    @Resource
    private CaseReviewFunctionalCaseService caseReviewFunctionalCaseService;
	
}