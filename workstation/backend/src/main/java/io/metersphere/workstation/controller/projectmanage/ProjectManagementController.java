package io.metersphere.workstation.controller.projectmanage;

import io.metersphere.workstation.service.projectmanage.ProjectManagementService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = {
        "/field/template/case",
        "/field/template/issue"
})
public class ProjectManagementController {
    @Resource
    ProjectManagementService projectManagementService;

    @PostMapping("/**")
    public Object list(HttpServletRequest request, @RequestBody Object param) {
        return projectManagementService.post(request.getRequestURI(), param);
    }

    @GetMapping("/**")
    public Object get(HttpServletRequest request) {
        return projectManagementService.get(request.getRequestURI());
    }
}
