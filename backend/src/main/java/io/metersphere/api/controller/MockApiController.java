package io.metersphere.api.controller;

import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.MockConfigService;
import io.metersphere.base.domain.Project;
import io.metersphere.controller.handler.annotation.NoResultHolder;
import io.metersphere.service.ProjectService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
        String returnStr = mockConfigService.checkReturnWithMockExpectByBodyParam("POST", project, request, response);
        return returnStr;
    }

    @GetMapping("/{projectSystemId}/**")
    @NoResultHolder
    public String getRequest(@PathVariable String projectSystemId, HttpServletRequest request, HttpServletResponse response) {
        Project project = projectService.findBySystemId(projectSystemId);
        String returnStr = mockConfigService.checkReturnWithMockExpectByUrlParam("GET", project, request, response);
        return returnStr;
    }

    @PutMapping("/{projectSystemId}/**")
    @NoResultHolder
    public String putRequest(@PathVariable String projectSystemId, HttpServletRequest request, HttpServletResponse response) {
        Project project = projectService.findBySystemId(projectSystemId);
        String returnStr = mockConfigService.checkReturnWithMockExpectByBodyParam("PUT", project, request, response);
        return returnStr;
    }

    @PatchMapping("/{projectSystemId}/**")
    @NoResultHolder
    public String patchRequest(@PathVariable String projectSystemId, HttpServletRequest request, HttpServletResponse response) {
        Project project = projectService.findBySystemId(projectSystemId);
        String returnStr = mockConfigService.checkReturnWithMockExpectByBodyParam("PATCH", project, request, response);
        return returnStr;
    }

    @DeleteMapping("/{projectSystemId}/**")
    @NoResultHolder
    public String deleteRequest(@PathVariable String projectSystemId, HttpServletRequest request, HttpServletResponse response) {
        Project project = projectService.findBySystemId(projectSystemId);
        String returnStr = mockConfigService.checkReturnWithMockExpectByUrlParam("DELETE", project, request, response);
        return returnStr;
    }

    @RequestMapping(value = "/{projectSystemId}/**", method = RequestMethod.OPTIONS)
    @NoResultHolder
    public String optionsRequest(@PathVariable String projectSystemId, HttpServletRequest request, HttpServletResponse response) {
        Project project = projectService.findBySystemId(projectSystemId);
        String returnStr = mockConfigService.checkReturnWithMockExpectByUrlParam("OPTIONS", project, request, response);
        return returnStr;
    }

    @RequestMapping(value = "/{projectSystemId}/**", method = RequestMethod.HEAD)
    @NoResultHolder
    public void headRequest(@PathVariable String projectSystemId, HttpServletRequest request, HttpServletResponse response) {
        Project project = projectService.findBySystemId(projectSystemId);
        mockConfigService.checkReturnWithMockExpectByUrlParam("HEAD", project, request, response);
    }
}
