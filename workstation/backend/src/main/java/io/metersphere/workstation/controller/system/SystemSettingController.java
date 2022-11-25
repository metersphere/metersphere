package io.metersphere.workstation.controller.system;

import io.metersphere.workstation.service.system.SystemSettingService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = {
        "user"
})
public class SystemSettingController {
   @Resource
   SystemSettingService systemSettingService;

    @PostMapping("/**")
    public Object list(HttpServletRequest request, @RequestBody Object param) {
        return systemSettingService.post(request.getRequestURI(), param);
    }

    @GetMapping("/**")
    public Object get(HttpServletRequest request) {
        return systemSettingService.get(request.getRequestURI());
    }
}
