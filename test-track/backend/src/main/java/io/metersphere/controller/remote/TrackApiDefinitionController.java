package io.metersphere.controller.remote;

import io.metersphere.plan.request.api.ApiDefinitionRequest;
import io.metersphere.plan.service.remote.api.PlanApiDefinitionService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RequestMapping("/api/definition")
@RestController
public class TrackApiDefinitionController {

    @Resource
    PlanApiDefinitionService planApiDefinitionService;


    @PostMapping("/list/relevance/{goPage}/{pageSize}")
    public Object listRelevance(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody ApiDefinitionRequest request) {
        return planApiDefinitionService.listRelevance(request, goPage, pageSize);
    }
}
