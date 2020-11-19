package io.metersphere.api.controller;

import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.api.dto.definition.ApiTestCaseResult;
import io.metersphere.api.dto.definition.SaveApiTestCaseRequest;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.SessionUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/api/testcase")
@RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
public class ApiTestCaseController {

    @Resource
    private ApiTestCaseService apiTestCaseService;

    @PostMapping("/list")
    public List<ApiTestCaseResult> list(@RequestBody ApiTestCaseRequest request) {
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        return apiTestCaseService.list(request);
    }

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public void create(@RequestPart("request") SaveApiTestCaseRequest request, @RequestPart(value = "files") List<MultipartFile> bodyFiles) {
        apiTestCaseService.create(request, bodyFiles);
    }

    @PostMapping(value = "/update", consumes = {"multipart/form-data"})
    public void update(@RequestPart("request") SaveApiTestCaseRequest request, @RequestPart(value = "files") List<MultipartFile> bodyFiles) {
        apiTestCaseService.update(request, bodyFiles);
    }

    @GetMapping("/delete/{id}")
    public void delete(@PathVariable String id) {
        apiTestCaseService.delete(id);
    }

}
