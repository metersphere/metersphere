package io.metersphere.controller.remote;

import io.metersphere.service.remote.UiTestService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = {
        "/test/plan/uiScenario/case",
        "/ui/scenario/module",
        "/share/test/plan/uiScenario/case",
        "/ui/automation"
})
public class UiTestController {
    @Resource
    UiTestService uiTestService;

    @PostMapping("/**")
    public Object list(HttpServletRequest request, @RequestBody Object param) {
        return uiTestService.post(request.getRequestURI(), param);
    }

    @GetMapping("/**")
    public Object get(HttpServletRequest request) {
        return uiTestService.get(request.getRequestURI());
    }
}
