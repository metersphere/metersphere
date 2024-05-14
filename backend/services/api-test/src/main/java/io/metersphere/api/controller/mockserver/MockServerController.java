package io.metersphere.api.controller.mockserver;

import io.metersphere.api.service.mockserver.MockServerService;
import io.metersphere.system.controller.handler.annotation.NoResultHolder;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/mock-server/{projectNum}/{apiNum}/**")
@Tag(name = "接口测试-接口管理-接口定义-Mock")
@MultipartConfig
public class MockServerController {

    @Resource
    private MockServerService mockServerService;

    @RequestMapping(method = RequestMethod.OPTIONS)
    @NoResultHolder
    public ResponseEntity<?> optionsRequest(@PathVariable String projectNum, @PathVariable String apiNum, HttpServletRequest request) {
        return mockServerService.execute(HttpMethod.OPTIONS.name(), projectNum, apiNum, request);
    }

    @RequestMapping(method = RequestMethod.HEAD)
    @NoResultHolder
    public ResponseEntity<?> headerRequest(@PathVariable String projectNum, @PathVariable String apiNum, HttpServletRequest request) {
        return mockServerService.execute(HttpMethod.HEAD.name(), projectNum, apiNum, request);
    }

    @RequestMapping(method = RequestMethod.TRACE)
    @NoResultHolder
    public ResponseEntity<?> traceRequest(@PathVariable String projectNum, @PathVariable String apiNum, HttpServletRequest request) {
        return mockServerService.execute(HttpMethod.TRACE.name(), projectNum, apiNum, request);
    }

    @GetMapping
    @NoResultHolder
    public ResponseEntity<?> getMockRequest(@PathVariable String projectNum, @PathVariable String apiNum, HttpServletRequest request) {
        return handleMockRequest(HttpMethod.GET.name(), projectNum, apiNum, request);
    }

    @PostMapping
    @NoResultHolder
    public ResponseEntity<?> postMockRequest(@PathVariable String projectNum, @PathVariable String apiNum, HttpServletRequest request) {
        return handleMockRequest(HttpMethod.POST.name(), projectNum, apiNum, request);
    }

    @PutMapping
    @NoResultHolder
    public ResponseEntity<?> putMockRequest(@PathVariable String projectNum, @PathVariable String apiNum, HttpServletRequest request) {
        return handleMockRequest(HttpMethod.PUT.name(), projectNum, apiNum, request);
    }

    @DeleteMapping
    @NoResultHolder
    public ResponseEntity<?> deleteMockRequest(@PathVariable String projectNum, @PathVariable String apiNum, HttpServletRequest request) {
        return handleMockRequest(HttpMethod.DELETE.name(), projectNum, apiNum, request);
    }

    @PatchMapping
    @NoResultHolder
    public ResponseEntity<?> patchMockRequest(@PathVariable String projectNum, @PathVariable String apiNum, HttpServletRequest request) {
        return handleMockRequest(HttpMethod.PATCH.name(), projectNum, apiNum, request);
    }

    private ResponseEntity<?> handleMockRequest(String method, String projectNum, String apiNum, HttpServletRequest request) {
        return mockServerService.execute(method, projectNum, apiNum, request);
    }

}
