package io.metersphere.workstation.controller.track;

import io.metersphere.workstation.service.track.TrackSettingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping(path = {
        "issues",
        "case/node",
        "/test/case",
        "/test/plan"
})
public class TrackController {

    @Resource
    TrackSettingService trackPSettingService;

    @PostMapping("/**")
    public Object list(HttpServletRequest request, @RequestBody Object param) {
        return trackPSettingService.post(request.getRequestURI(), param);
    }

    @GetMapping("/**")
    public Object get(HttpServletRequest request) {
        return trackPSettingService.get(request.getRequestURI());
    }
}
