package io.metersphere.functional.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.metersphere.functional.service.CaseReviewService;


@RestController
@RequestMapping("/case/review")
public class CaseReviewController {

    @Resource
    private CaseReviewService caseReviewService;

}