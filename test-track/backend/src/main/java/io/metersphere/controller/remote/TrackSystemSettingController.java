package io.metersphere.controller.remote;

import io.metersphere.service.remote.project.TrackSystemSettingService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = {
        "/environment/group"
})
public class TrackSystemSettingController {
    @Resource
    TrackSystemSettingService trackSystemSettingService;

    @PostMapping("/**")
    public Object list(HttpServletRequest request, @RequestBody Object param) {
        return trackSystemSettingService.post(request.getRequestURI(), param);
    }

    @GetMapping("/**")
    public Object get(HttpServletRequest request) {
        return trackSystemSettingService.get(request.getRequestURI());
    }
}
