package io.metersphere.api.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.*;
import io.metersphere.api.dto.scenario.request.dubbo.RegistryCenter;
import io.metersphere.api.service.APITestService;
import io.metersphere.base.domain.ApiTest;
import io.metersphere.base.domain.Schedule;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.QueryScheduleRequest;
import io.metersphere.dto.ScheduleDao;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

import java.util.List;

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

    @PostMapping("/list/ids")
    public List<ApiTest> listByIds(@RequestBody QueryAPITestRequest request) {
        return apiTestService.listByIds(request);
    }

    @GetMapping("/list/{projectId}")
    public List<ApiTest> list(@PathVariable String projectId) {
        return apiTestService.getApiTestByProjectId(projectId);
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
    public void create(@RequestPart("request") SaveAPITestRequest request, @RequestPart(value = "file") MultipartFile file, @RequestPart(value = "files") List<MultipartFile> bodyFiles) {
        apiTestService.create(request, file, bodyFiles);
    }

    @PostMapping(value = "/update", consumes = {"multipart/form-data"})
    public void update(@RequestPart("request") SaveAPITestRequest request, @RequestPart(value = "file") MultipartFile file, @RequestPart(value = "files") List<MultipartFile> bodyFiles) {
        apiTestService.update(request, file, bodyFiles);
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

    @PostMapping(value = "/run/debug", consumes = {"multipart/form-data"})
    public String runDebug(@RequestPart("request") SaveAPITestRequest request, @RequestPart(value = "file") MultipartFile file, @RequestPart(value = "files") List<MultipartFile> bodyFiles) {
        return apiTestService.runDebug(request, file, bodyFiles);
    }

    @PostMapping(value = "/checkName")
    public void checkName(@RequestBody SaveAPITestRequest request) {
        apiTestService.checkName(request);
    }

    @PostMapping(value = "/import", consumes = {"multipart/form-data"})
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public ApiTest testCaseImport(@RequestPart(value = "file", required = false) MultipartFile file, @RequestPart("request") ApiTestImportRequest request) {
        return apiTestService.apiTestImport(file, request);
    }

    @PostMapping("/dubbo/providers")
    public List<DubboProvider> getProviders(@RequestBody RegistryCenter registry) {
        return apiTestService.getProviders(registry);
    }

    @PostMapping("/list/schedule/{goPage}/{pageSize}")
    public List<ScheduleDao> listSchedule(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryScheduleRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return apiTestService.listSchedule(request);
    }

    @PostMapping("/list/schedule")
    public List<ScheduleDao> listSchedule(@RequestBody QueryScheduleRequest request) {
        return apiTestService.listSchedule(request);
    }
}
