package io.metersphere.functional.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.metersphere.functional.service.FunctionalCaseRelationshipEdgeService;


@RestController
@RequestMapping("/functional/case/relationship/edge")
public class FunctionalCaseRelationshipEdgeController {

    @Resource
    private FunctionalCaseRelationshipEdgeService functionalCaseRelationshipEdgeService;

}