package io.metersphere.api.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.APITestResult;
import io.metersphere.api.dto.DeleteAPITestRequest;
import io.metersphere.api.dto.QueryAPITestRequest;
import io.metersphere.api.dto.SaveAPITestRequest;
import io.metersphere.api.service.APITestService;
import io.metersphere.base.domain.ApiTest;
import io.metersphere.base.domain.Schedule;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api")
@RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
public class APITestController {
    @Resource
    private APITestService apiTestService;

    @GetMapping("recent/{count}")
    public List<APITestResult> recentTest(@PathVariable int count) {
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        QueryAPITestRequest request = new QueryAPITestRequest();
        request.setWorkspaceId(currentWorkspaceId);
        PageHelper.startPage(1, count, true);
        return apiTestService.recentTest(request);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<APITestResult>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryAPITestRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        return PageUtils.setPageInfo(page, apiTestService.list(request));
    }

    @GetMapping("/list/{projectId}")
    public List<ApiTest> list(@PathVariable String projectId) {
        return apiTestService.getApiTestByProjectId(projectId);
    }


    @GetMapping("/state/get/{testId}")
    public ApiTest apiState(@PathVariable String testId) {
        return apiTestService.getApiTestByTestId(testId);
    }

    @PostMapping(value = "/schedule/update")
    public void updateSchedule(@RequestBody Schedule request) {
        apiTestService.updateSchedule(request);
    }

    @PostMapping(value = "/schedule/create")
    public void createSchedule(@RequestBody Schedule request) {
        apiTestService.createSchedule(request);
    }

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public void create(@RequestPart("request") SaveAPITestRequest request, @RequestPart(value = "files") List<MultipartFile> files) {
        apiTestService.create(request, files);
    }

    @PostMapping(value = "/update", consumes = {"multipart/form-data"})
    public void update(@RequestPart("request") SaveAPITestRequest request, @RequestPart(value = "files") List<MultipartFile> files) {
        apiTestService.update(request, files);
    }

    @PostMapping(value = "/copy")
    public void copy(@RequestBody SaveAPITestRequest request) {
        apiTestService.copy(request);
    }

    @GetMapping("/get/{testId}")
    public APITestResult get(@PathVariable String testId) {
        return apiTestService.get(testId);
    }

    @PostMapping("/delete")
    public void delete(@RequestBody DeleteAPITestRequest request) {
        apiTestService.delete(request.getId());
    }

    @PostMapping(value = "/run")
    public String run(@RequestBody SaveAPITestRequest request) {
        return apiTestService.run(request);
    }

    @PostMapping("/import/{platform}")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public ApiTest testCaseImport(MultipartFile file, @PathVariable String platform) {
        return apiTestService.apiTestImport(file, platform);
    }

}
