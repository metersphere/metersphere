package io.metersphere.controller.remote;

import io.metersphere.service.remote.CustomFunctionService;
import io.metersphere.api.dto.CustomFunctionRequest;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RequestMapping("/custom/func")
@RestController
public class CustomFunctionController {

    @Resource
    private CustomFunctionService customFunctionService;

    @PostMapping("/**")
    public Object query(HttpServletRequest request, @RequestBody CustomFunctionRequest params) {
        return customFunctionService.getPage(request.getRequestURI(), params);
    }

    @GetMapping("/**")
    public Object get(HttpServletRequest request) {
        return customFunctionService.get(request.getRequestURI());
    }
}
