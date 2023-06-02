package io.metersphere.functional.controller;

import io.metersphere.functional.domain.CaseReview;
import io.metersphere.functional.dto.CaseReviewDto;
import io.metersphere.validation.groups.Created;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.metersphere.functional.service.CaseReviewService;


@RestController
@RequestMapping("/case/review")
public class CaseReviewController {

    @Resource
    private CaseReviewService caseReviewService;

    @PostMapping("/add")
    public void addCaseReview(@Validated({Created.class}) @RequestBody CaseReview caseReview) {
        caseReviewService.addCaseReview(caseReview);
    }


    @PostMapping("/delete")
    public void delCaseReview(@Validated({Created.class}) @RequestBody CaseReview caseReview) {
        caseReviewService.delCaseReview(caseReview);
    }


    @GetMapping("/get/{id}")
    public CaseReviewDto get(@PathVariable String id) {
        return caseReviewService.get(id);
    }
}