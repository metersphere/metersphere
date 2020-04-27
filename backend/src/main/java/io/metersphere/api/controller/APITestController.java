package io.metersphere.api.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.APITestResult;
import io.metersphere.api.dto.DeleteAPITestRequest;
import io.metersphere.api.dto.QueryAPITestRequest;
import io.metersphere.api.dto.SaveAPITestRequest;
import io.metersphere.api.service.ApiTestService;
import io.metersphere.base.domain.ApiTestWithBLOBs;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.testplan.SaveTestPlanRequest;
import io.metersphere.service.FileService;
import io.metersphere.user.SessionUtils;
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
    private ApiTestService apiTestService;
    @Resource
    private FileService fileService;

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

    @PostMapping(value = "/save", consumes = {"multipart/form-data"})
    public String save(@RequestPart("request") SaveAPITestRequest request, @RequestPart(value = "files") List<MultipartFile> files) {
        return apiTestService.save(request, files);
    }

    @GetMapping("/get/{testId}")
    public ApiTestWithBLOBs get(@PathVariable String testId) {
        return apiTestService.get(testId);
    }

    @PostMapping("/delete")
    public void delete(@RequestBody DeleteAPITestRequest request) {
        apiTestService.delete(request);
    }

    @PostMapping(value = "/run", consumes = {"multipart/form-data"})
    public String run(@RequestPart("request") SaveAPITestRequest request, @RequestPart(value = "files") List<MultipartFile> files) {
        return apiTestService.run(request, files);
    }
}
