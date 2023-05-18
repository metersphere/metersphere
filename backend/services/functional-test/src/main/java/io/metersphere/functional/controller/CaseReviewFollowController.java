package io.metersphere.functional.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.metersphere.functional.service.CaseReviewFollowService;


@RestController
@RequestMapping("/case/review/follow")
public class CaseReviewFollowController {

    @Resource
    private CaseReviewFollowService caseReviewFollowService;

}