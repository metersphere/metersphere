package io.metersphere.controller.remote;

import io.metersphere.service.remote.api.TrackApiTestService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = {
        "/api/automation",
        "/api/definition",
        "/api/testcase",
        "/test/plan/api/case",
        "/share/test/plan/api/case",
        "/test/plan/scenario/case",
        "/share/test/plan/scenario/case",
        "/api/module",
        "/api/automation/module",
        "/api/project",
        "/test/case/relevance/api",
        "/test/case/relevance/scenario",
        "home"
})
public class TrackApiTestController {
    @Resource
    TrackApiTestService trackApiTestService;

    @PostMapping("/**")
    public Object list(HttpServletRequest request, @RequestBody Object param) {
        return trackApiTestService.post(request.getRequestURI(), param);
    }

    @GetMapping("/**")
    public Object get(HttpServletRequest request) {
        return trackApiTestService.get(request.getRequestURI());
    }
}
