package io.metersphere.track.controller;

import io.metersphere.track.dto.DemandDTO;
import io.metersphere.track.service.DemandService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("demand")
@RestController
public class TestCaseDemandController {
    @Resource
    private DemandService demandService;

    @GetMapping("/list/{projectId}")
    public List<DemandDTO> getDemandList(@PathVariable String projectId) {
        return demandService.getDemandList(projectId);
    }
}
