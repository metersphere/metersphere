package io.metersphere.workstation.controller.performance;

import io.metersphere.workstation.service.performance.PerformanceService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = {
        "performance",
})
public class PerformanceController {

    @Resource
    PerformanceService performanceService;

    @PostMapping("/**")
    public Object list(HttpServletRequest request, @RequestBody Object param) {
        return performanceService.post(request.getRequestURI(), param);
    }

    @GetMapping("/**")
    public Object get(HttpServletRequest request) {
        return performanceService.get(request.getRequestURI());
    }
}
