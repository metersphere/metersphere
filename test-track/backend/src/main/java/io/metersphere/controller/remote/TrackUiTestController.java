package io.metersphere.controller.remote;

import io.metersphere.service.remote.project.TrackUiTestService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = {
        "/test/plan/uiScenario/case",
        "/ui/scenario/module",
        "/share/test/plan/uiScenario/case",
        "/ui/automation",
        "/test/case/relevance/uiScenario"
})
public class TrackUiTestController {
    @Resource
    TrackUiTestService trackUiTestService;

    @PostMapping("/**")
    public Object list(HttpServletRequest request, @RequestBody Object param) {
        return trackUiTestService.post(request.getRequestURI(), param);
    }

    @GetMapping("/**")
    public Object get(HttpServletRequest request) {
        return trackUiTestService.get(request.getRequestURI());
    }
}
