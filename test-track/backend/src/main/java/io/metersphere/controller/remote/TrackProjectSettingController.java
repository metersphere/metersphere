package io.metersphere.controller.remote;

import io.metersphere.service.BaseProjectService;
import io.metersphere.service.remote.project.TrackProjectSettingService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = {
        "/custom/field",
        "project_application",
        "field/template/case",
        "field/template/issue",
        "project",
})
public class TrackProjectSettingController {

    @Resource
    TrackProjectSettingService trackProjectSettingService;
    @Resource
    BaseProjectService baseProjectService;

    @PostMapping("/**")
    public Object list(HttpServletRequest request, @RequestBody Object param) {
        return trackProjectSettingService.post(request.getRequestURI(), param);
    }

    @GetMapping("/**")
    public Object get(HttpServletRequest request) {
        return trackProjectSettingService.get(request.getRequestURI());
    }
}
