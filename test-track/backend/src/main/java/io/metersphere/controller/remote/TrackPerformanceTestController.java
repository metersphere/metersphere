package io.metersphere.controller.remote;

import io.metersphere.service.remote.project.TrackPerformanceTestService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = {
        "/test/plan/load/case",
        "/share/test/plan/load/case",
        "/performance",
        "/test/case/relevance/load"
})
public class TrackPerformanceTestController {
    @Resource
    TrackPerformanceTestService trackPerformanceTestService;

    @PostMapping("/**")
    public Object list(HttpServletRequest request, @RequestBody Object param) {
        return trackPerformanceTestService.post(request.getRequestURI(), param);
    }

    @GetMapping("/**")
    public Object get(HttpServletRequest request) {
        return trackPerformanceTestService.get(request.getRequestURI());
    }
}
