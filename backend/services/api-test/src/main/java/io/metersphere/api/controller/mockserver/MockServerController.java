package io.metersphere.api.controller.mockserver;

import io.metersphere.api.service.mockserver.MockServerService;
import io.metersphere.api.utils.MockServerUtils;
import io.metersphere.system.controller.handler.annotation.NoResultHolder;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/mock-server/{projectNum}/{apiNum}/**")
@Tag(name = "接口测试-接口管理-接口定义-Mock")
public class MockServerController {

    @Resource
    private MockServerService mockServerService;

    @RequestMapping(method = RequestMethod.OPTIONS)
    @NoResultHolder
    public Object optionsRequest(@PathVariable String projectNum, @PathVariable String apiNum,
                                 HttpServletRequest request, HttpServletResponse response) {
        var requestHeaderMap = MockServerUtils.getHttpRequestHeader(request);
        return mockServerService.execute(HttpMethod.OPTIONS.name(), requestHeaderMap, projectNum, apiNum, request, response);
    }

    @RequestMapping(method = RequestMethod.HEAD)
    @NoResultHolder
    public Object headerRequest(@PathVariable String projectNum, @PathVariable String apiNum,
                                 HttpServletRequest request, HttpServletResponse response) {
        var requestHeaderMap = MockServerUtils.getHttpRequestHeader(request);
        return mockServerService.execute(HttpMethod.HEAD.name(), requestHeaderMap, projectNum, apiNum, request, response);
    }

    @RequestMapping(method = RequestMethod.TRACE)
    @NoResultHolder
    public Object traceRequest(@PathVariable String projectNum, @PathVariable String apiNum,
                                HttpServletRequest request, HttpServletResponse response) {
        var requestHeaderMap = MockServerUtils.getHttpRequestHeader(request);
        return mockServerService.execute(HttpMethod.TRACE.name(), requestHeaderMap, projectNum, apiNum, request, response);
    }
    @GetMapping
    @NoResultHolder
    public Object getMockRequest(@PathVariable String projectNum, @PathVariable String apiNum,
                                 HttpServletRequest request, HttpServletResponse response) {
        return handleMockRequest(HttpMethod.GET.name(), projectNum, apiNum, request, response);
    }

    @PostMapping
    @NoResultHolder
    public Object postMockRequest(@PathVariable String projectNum, @PathVariable String apiNum,
                                  HttpServletRequest request, HttpServletResponse response) {
        return handleMockRequest(HttpMethod.POST.name(), projectNum, apiNum, request, response);
    }

    @PutMapping
    @NoResultHolder
    public Object putMockRequest(@PathVariable String projectNum, @PathVariable String apiNum,
                                 HttpServletRequest request, HttpServletResponse response) {
        return handleMockRequest(HttpMethod.PUT.name(), projectNum, apiNum, request, response);
    }

    @DeleteMapping
    @NoResultHolder
    public Object deleteMockRequest(@PathVariable String projectNum, @PathVariable String apiNum,
                                    HttpServletRequest request, HttpServletResponse response) {
        return handleMockRequest(HttpMethod.DELETE.name(), projectNum, apiNum, request, response);
    }

    @PatchMapping
    @NoResultHolder
    public Object patchMockRequest(@PathVariable String projectNum, @PathVariable String apiNum,
                                   HttpServletRequest request, HttpServletResponse response) {
        return handleMockRequest(HttpMethod.PATCH.name(), projectNum, apiNum, request, response);
    }

    private Object handleMockRequest(String method, String projectNum, String apiNum,
                                     HttpServletRequest request, HttpServletResponse response) {
        var requestHeaderMap = MockServerUtils.getHttpRequestHeader(request);
        return mockServerService.execute(method, requestHeaderMap, projectNum, apiNum, request, response);
    }

}
