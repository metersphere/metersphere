package io.metersphere.controller;

import io.metersphere.service.ApiGraphService;
import io.metersphere.xpack.graph.request.GraphBatchRequest;
import io.metersphere.dto.RelationshipGraphData;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RequestMapping("/graph")
@RestController
public class ApiGraphController {

    @Resource
    ApiGraphService apiGraphService;

    @GetMapping("/relationship/graph/{id}/{type}")
    public RelationshipGraphData getGraphData(@PathVariable("id") String id, @PathVariable("type") String type) {
        return apiGraphService.getGraphData(id, type);
    }

    @PostMapping("/relationship/graph/condition/{type}")
    public RelationshipGraphData getGraphDataByCondition(@RequestBody GraphBatchRequest request, @PathVariable("type") String type) {
        return apiGraphService.getGraphDataByCondition(request, type);
    }
}
