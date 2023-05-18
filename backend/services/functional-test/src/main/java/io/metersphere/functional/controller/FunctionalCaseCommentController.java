package io.metersphere.functional.controller;


import io.metersphere.functional.service.FunctionalCaseCommentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/functional/case/comment")
public class FunctionalCaseCommentController {

    @Resource
    private FunctionalCaseCommentService functionalCaseCommentService;
	
}