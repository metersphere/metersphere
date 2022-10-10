package io.metersphere.controller.remote;

import io.metersphere.service.remote.ApiProjectSettingService;
import io.metersphere.api.dto.QueryTestPlanRequest;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = {
        "/custom/field",
        "field/template/api",
})
public class ApiProjectSettingController {

    @Resource
    private ApiProjectSettingService apiProjectSettingService;

    @GetMapping("/**")
    public Object get(HttpServletRequest request) {
        return apiProjectSettingService.get(request.getRequestURI());
    }

    @PostMapping("/**")
    public Object post(HttpServletRequest request, @RequestBody QueryTestPlanRequest params) {
        return apiProjectSettingService.post(request.getRequestURI(), params);
    }
}
