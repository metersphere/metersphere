package io.metersphere.controller.remote;

import io.metersphere.service.remote.project.TrackUiTestService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = {
        "/test/plan/uiScenario/case",
        "/ui/scenario/module",
        "/share/test/plan/uiScenario/case",
        "/ui/automation"
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
