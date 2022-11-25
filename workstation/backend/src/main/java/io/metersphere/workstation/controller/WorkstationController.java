package io.metersphere.workstation.controller;

import io.metersphere.workstation.service.WorkstationService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.Map;

@RequestMapping("workstation")
@RestController
public class WorkstationController {
    @Resource
    private WorkstationService workstationService;

    @GetMapping("/creat_case_count/list/{isWeek}")
    public Map<String, Integer> list(@PathVariable Boolean isWeek) {
        return workstationService.getMyCreatedCaseGroupContMap(isWeek);
    }

    @GetMapping("/follow/total/count/{workstationId}")
    public Map<String, Integer> getFollowTotalCount(@PathVariable String workstationId) {
        return workstationService.getFollowTotalCount(workstationId);
    }

    @GetMapping("/coming/total/count/{workstationId}")
    public Map<String, Integer> getUpcomingTotalCount(@PathVariable String workstationId) {
        return workstationService.getUpcomingTotalCount(workstationId);
    }

    @GetMapping("/issue/week/count/{workstationId}")
    public Integer getIssueWeekCount(@PathVariable String workstationId) {
        return workstationService.getIssueWeekCount(workstationId);
    }
}
