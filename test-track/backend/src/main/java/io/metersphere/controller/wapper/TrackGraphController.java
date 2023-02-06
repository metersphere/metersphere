package io.metersphere.controller.wapper;

import io.metersphere.dto.RelationshipGraphData;
import io.metersphere.request.GraphBatchRequest;
import io.metersphere.service.wapper.TrackGraphService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RequestMapping("/graph")
@RestController
public class TrackGraphController {

    @Resource
    TrackGraphService trackGraphService;

    @GetMapping("/relationship/graph/{id}/{type}")
    public RelationshipGraphData getGraphData(@PathVariable("id") String id, @PathVariable("type") String type) {
        return trackGraphService.getGraphData(id, type);
    }

    @PostMapping("/relationship/graph/{type}")
    public RelationshipGraphData getGraphDataByCondition(@RequestBody GraphBatchRequest request, @PathVariable("type") String type) {
        return trackGraphService.getGraphDataByCondition(request, type);
    }
}
