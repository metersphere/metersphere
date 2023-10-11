package io.metersphere.functional.controller;

import io.metersphere.functional.dto.CaseReviewDto;
import io.metersphere.functional.service.CaseReviewService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/case/review")
public class CaseReviewController {

    @Resource
    private CaseReviewService caseReviewService;


    @GetMapping("/get/{id}")
    public CaseReviewDto get(@PathVariable String id) {
        return caseReviewService.get(id);
    }
}