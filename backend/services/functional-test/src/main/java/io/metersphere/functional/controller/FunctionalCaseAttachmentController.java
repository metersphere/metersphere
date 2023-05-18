package io.metersphere.functional.controller;


import io.metersphere.functional.service.FunctionalCaseAttachmentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/functional/case/attachment")
public class FunctionalCaseAttachmentController {

    @Resource
    private FunctionalCaseAttachmentService functionalCaseAttachmentService;
	
}