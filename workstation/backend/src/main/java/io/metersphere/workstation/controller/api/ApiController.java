package io.metersphere.workstation.controller.api;

import io.metersphere.workstation.service.api.ApiService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = {
        "/api/definition",
        "/api/testcase",
        "/api/automation",
})
public class ApiController {

    @Resource
    ApiService apiService;

    @PostMapping("/**")
    public Object list(HttpServletRequest request, @RequestBody Object param) {

        return apiService.post(request.getRequestURI(), param);
    }

    @GetMapping("/**")
    public Object get(HttpServletRequest request) {
        return apiService.get(request.getRequestURI());
    }
}
