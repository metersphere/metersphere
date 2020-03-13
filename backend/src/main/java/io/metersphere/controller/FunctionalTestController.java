package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.FileMetadata;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.testplan.*;
import io.metersphere.dto.FunctionalTestDTO;
import io.metersphere.service.FileService;
import io.metersphere.service.FuctionalTestService;
import io.metersphere.user.SessionUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/functional/plan")
@RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
public class FunctionalTestController {
    @Resource
    private FuctionalTestService fuctionalTestService;
    @Resource
    private FileService fileService;

    @GetMapping("recent/{count}")
    public List<FunctionalTestDTO> recentTestPlans(@PathVariable int count) {
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        QueryTestPlanRequest request = new QueryTestPlanRequest();
        request.setWorkspaceId(currentWorkspaceId);
        PageHelper.startPage(1, count, true);
        return fuctionalTestService.recentTestPlans(request);
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<FunctionalTestDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestPlanRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        return PageUtils.setPageInfo(page, fuctionalTestService.list(request));
    }

    @PostMapping(value = "/save", consumes = {"multipart/form-data"})
    public String save(
            @RequestPart("request") SaveTestPlanRequest request,
            @RequestPart(value = "file") MultipartFile file
    ) {
        return fuctionalTestService.save(request, file);
    }

    @PostMapping(value = "/edit", consumes = {"multipart/form-data"})
    public String edit(
            @RequestPart("request") EditTestPlanRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        return fuctionalTestService.edit(request, file);
    }

    @GetMapping("/get/{testId}")
    public FunctionalTestDTO get(@PathVariable String testId) {
        return fuctionalTestService.get(testId);
    }

    @GetMapping("/get-runtime-config/{testId}")
    public String getAdvancedConfiguration(@PathVariable String testId) {
        return fuctionalTestService.getRuntimeConfiguration(testId);
    }

    @PostMapping("/delete")
    public void delete(@RequestBody DeleteTestPlanRequest request) {
        fuctionalTestService.delete(request);
    }

    @PostMapping("/run")
    public void run(@RequestBody RunTestPlanRequest request) {
        fuctionalTestService.run(request);
    }

    @GetMapping("/file/metadata/{testId}")
    public FileMetadata getFileMetadata(@PathVariable String testId) {
        return fileService.getFucFileMetadataByTestId(testId);
    }

    @PostMapping("/file/download")
    public ResponseEntity<byte[]> downloadJmx(@RequestBody FileOperationRequest fileOperationRequest) {
        byte[] bytes = fileService.loadFileAsBytes(fileOperationRequest.getId());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileOperationRequest.getName() + "\"")
                .body(bytes);
    }
}
