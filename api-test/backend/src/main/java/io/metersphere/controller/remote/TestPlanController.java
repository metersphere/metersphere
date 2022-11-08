package io.metersphere.controller.remote;

import io.metersphere.api.dto.QueryTestPlanRequest;
import io.metersphere.commons.exception.MSException;
import io.metersphere.service.remote.RemoteTestPlanService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RequestMapping("/test/plan")
@RestController
public class TestPlanController {
    @Resource
    private RemoteTestPlanService remoteTestPlanService;

    public static final List<String> EXCLUDE_APIS = new ArrayList<>(2) {{
        add("test/plan/api/case");
        add("test/plan/scenario/case");
    }};

    @PostMapping("/list/all/{goPage}/{pageSize}")
    public Object planListAll(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestPlanRequest request) {
        return remoteTestPlanService.planListAll("/test/plan", goPage, pageSize, request);
    }

    @GetMapping("/**")
    public List getStageOption(HttpServletRequest request) {
        excludeApi(request.getRequestURI());
        return remoteTestPlanService.get(request.getRequestURI());
    }

    @PostMapping("/**")
    public List getPage(HttpServletRequest request, @RequestBody QueryTestPlanRequest params) {
        excludeApi(request.getRequestURI());
        return remoteTestPlanService.post(request.getRequestURI(), params);
    }

    /**
     * 解决不同模块路径冲突，导致循环调用
     * todo 待优化
     *
     * @param url
     */
    public void excludeApi(String url) {
        EXCLUDE_APIS.forEach(item -> {
            if (StringUtils.contains(url, item)) {
                MSException.throwException("接口不存在");
            }
        });
    }
}
