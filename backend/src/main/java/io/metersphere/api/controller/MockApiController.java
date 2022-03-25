package io.metersphere.api.controller;

import io.metersphere.api.dto.mock.MockApiUtils;
import io.metersphere.api.service.MockConfigService;
import io.metersphere.api.tcp.TCPPool;
import io.metersphere.base.domain.Project;
import io.metersphere.controller.handler.annotation.NoResultHolder;
import io.metersphere.service.ProjectService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    private ProjectService projectService;

    @PostMapping("/{projectSystemId}/**")
    @NoResultHolder
    public String postRequest(@PathVariable String projectSystemId, HttpServletRequest request, HttpServletResponse response) {
        Project project = projectService.findBySystemId(projectSystemId);
        Map<String,String> requestHeaderMap = MockApiUtils.getHttpRequestHeader(request);
        String returnStr = mockConfigService.checkReturnWithMockExpectByBodyParam("POST", requestHeaderMap,project, request, response);
        return returnStr;
    }

    @GetMapping("/{projectSystemId}/**")
    @NoResultHolder
    public String getRequest(@PathVariable String projectSystemId, HttpServletRequest request, HttpServletResponse response) {
        Project project = projectService.findBySystemId(projectSystemId);
        Map<String,String> requestHeaderMap = MockApiUtils.getHttpRequestHeader(request);
        String returnStr = mockConfigService.checkReturnWithMockExpectByUrlParam("GET", requestHeaderMap, project, request, response);
        return returnStr;
    }

    @PutMapping("/{projectSystemId}/**")
    @NoResultHolder
    public String putRequest(@PathVariable String projectSystemId, HttpServletRequest request, HttpServletResponse response) {
        Project project = projectService.findBySystemId(projectSystemId);
        Map<String,String> requestHeaderMap = MockApiUtils.getHttpRequestHeader(request);
        String returnStr = mockConfigService.checkReturnWithMockExpectByBodyParam("PUT", requestHeaderMap, project, request, response);
        return returnStr;
    }

    @PatchMapping("/{projectSystemId}/**")
    @NoResultHolder
    public String patchRequest(@PathVariable String projectSystemId, HttpServletRequest request, HttpServletResponse response) {
        Project project = projectService.findBySystemId(projectSystemId);
        Map<String,String> requestHeaderMap = MockApiUtils.getHttpRequestHeader(request);
        String returnStr = mockConfigService.checkReturnWithMockExpectByBodyParam("PATCH", requestHeaderMap, project, request, response);
        return returnStr;
    }

    @DeleteMapping("/{projectSystemId}/**")
    @NoResultHolder
    public String deleteRequest(@PathVariable String projectSystemId, HttpServletRequest request, HttpServletResponse response) {
        Project project = projectService.findBySystemId(projectSystemId);
        Map<String,String> requestHeaderMap = MockApiUtils.getHttpRequestHeader(request);
        String returnStr = mockConfigService.checkReturnWithMockExpectByUrlParam("DELETE", requestHeaderMap, project, request, response);
        return returnStr;
    }

    @RequestMapping(value = "/{projectSystemId}/**", method = RequestMethod.OPTIONS)
    @NoResultHolder
    public String optionsRequest(@PathVariable String projectSystemId, HttpServletRequest request, HttpServletResponse response) {
        Project project = projectService.findBySystemId(projectSystemId);
        Map<String,String> requestHeaderMap = MockApiUtils.getHttpRequestHeader(request);
        String returnStr = mockConfigService.checkReturnWithMockExpectByUrlParam("OPTIONS", requestHeaderMap, project, request, response);
        return returnStr;
    }

    @RequestMapping(value = "/{projectSystemId}/**", method = RequestMethod.HEAD)
    @NoResultHolder
    public void headRequest(@PathVariable String projectSystemId, HttpServletRequest request, HttpServletResponse response) {
        Project project = projectService.findBySystemId(projectSystemId);
        Map<String,String> requestHeaderMap = MockApiUtils.getHttpRequestHeader(request);
        mockConfigService.checkReturnWithMockExpectByUrlParam("HEAD", requestHeaderMap, project, request, response);
    }

    @GetMapping("/getTcpMockPortStatus/")
    public String genTcpMockPort(){
        return TCPPool.getTcpStatus();
    }
}
