package io.metersphere.controller.remote;

import io.metersphere.service.remote.SystemSettingService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/global/platform/plugin")
// 前缀加 global 避免 setting 服务循环调用
public class BasePlatformPluginController {

    @Resource
    SystemSettingService systemSettingService;

    @PostMapping("/**")
    public Object list(HttpServletRequest request, @RequestBody Object param) {
        String url = request.getRequestURI().replace("/global", "");
        return systemSettingService.post(url, param);
    }

    @GetMapping("/**")
    public Object get(HttpServletRequest request) {
        String url = request.getRequestURI().replace("/global", "");
        return systemSettingService.get(url);
    }
}
