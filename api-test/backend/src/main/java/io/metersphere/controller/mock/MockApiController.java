package io.metersphere.controller.mock;

import io.metersphere.base.domain.Project;
import io.metersphere.commons.utils.mock.MockApiUtils;
import io.metersphere.controller.handler.annotation.NoResultHolder;
import io.metersphere.service.BaseProjectService;
import io.metersphere.service.MockConfigService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author song.tianyang
 * @Date 2021/4/12 5:11 下午
 * @Description
 */
@RestController
@RequestMapping("/mock")
public class MockApiController {

    @Resource
    private MockConfigService mockConfigService;
    @Resource
    private BaseProjectService baseProjectService;


    @PostMapping("/{projectSystemId}/**")
    @NoResultHolder
    public String postRequest(@PathVariable String projectSystemId, HttpServletRequest request, HttpServletResponse response) {
        Project project = baseProjectService.findBySystemId(projectSystemId);
        Map<String, String> requestHeaderMap = MockApiUtils.getHttpRequestHeader(request);
        return mockConfigService.checkReturnWithMockExpectByBodyParam("POST", requestHeaderMap, project, request, response);
    }

    @GetMapping("/{projectSystemId}/**")
    @NoResultHolder
    public String getRequest(@PathVariable String projectSystemId, HttpServletRequest request, HttpServletResponse response) {
        Project project = baseProjectService.findBySystemId(projectSystemId);
        Map<String, String> requestHeaderMap = MockApiUtils.getHttpRequestHeader(request);
        return mockConfigService.checkReturnWithMockExpectByUrlParam("GET", requestHeaderMap, project, request, response);
    }

    @PutMapping("/{projectSystemId}/**")
    @NoResultHolder
    public String putRequest(@PathVariable String projectSystemId, HttpServletRequest request, HttpServletResponse response) {
        Project project = baseProjectService.findBySystemId(projectSystemId);
        Map<String, String> requestHeaderMap = MockApiUtils.getHttpRequestHeader(request);
        return mockConfigService.checkReturnWithMockExpectByBodyParam("PUT", requestHeaderMap, project, request, response);
    }

    @PatchMapping("/{projectSystemId}/**")
    @NoResultHolder
    public String patchRequest(@PathVariable String projectSystemId, HttpServletRequest request, HttpServletResponse response) {
        Project project = baseProjectService.findBySystemId(projectSystemId);
        Map<String, String> requestHeaderMap = MockApiUtils.getHttpRequestHeader(request);
        return mockConfigService.checkReturnWithMockExpectByBodyParam("PATCH", requestHeaderMap, project, request, response);
    }

    @DeleteMapping("/{projectSystemId}/**")
    @NoResultHolder
    public String deleteRequest(@PathVariable String projectSystemId, HttpServletRequest request, HttpServletResponse response) {
        Project project = baseProjectService.findBySystemId(projectSystemId);
        Map<String, String> requestHeaderMap = MockApiUtils.getHttpRequestHeader(request);
        return mockConfigService.checkReturnWithMockExpectByUrlParam("DELETE", requestHeaderMap, project, request, response);
    }

    @RequestMapping(value = "/{projectSystemId}/**", method = RequestMethod.OPTIONS)
    @NoResultHolder
    public String optionsRequest(@PathVariable String projectSystemId, HttpServletRequest request, HttpServletResponse response) {
        Project project = baseProjectService.findBySystemId(projectSystemId);
        Map<String, String> requestHeaderMap = MockApiUtils.getHttpRequestHeader(request);
        return mockConfigService.checkReturnWithMockExpectByUrlParam("OPTIONS", requestHeaderMap, project, request, response);
    }

    @RequestMapping(value = "/{projectSystemId}/**", method = RequestMethod.HEAD)
    @NoResultHolder
    public void headRequest(@PathVariable String projectSystemId, HttpServletRequest request, HttpServletResponse response) {
        Project project = baseProjectService.findBySystemId(projectSystemId);
        Map<String, String> requestHeaderMap = MockApiUtils.getHttpRequestHeader(request);
        mockConfigService.checkReturnWithMockExpectByUrlParam("HEAD", requestHeaderMap, project, request, response);
    }

}
