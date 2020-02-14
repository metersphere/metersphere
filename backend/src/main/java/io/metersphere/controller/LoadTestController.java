package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.testplan.DeleteTestPlanRequest;
import io.metersphere.controller.request.testplan.FileOperationRequest;
import io.metersphere.controller.request.testplan.QueryTestPlanRequest;
import io.metersphere.controller.request.testplan.SaveTestPlanRequest;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.service.FileService;
import io.metersphere.service.LoadTestService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/testplan")
public class LoadTestController {
    @Resource
    private LoadTestService loadTestService;
    @Resource
    private FileService fileService;

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<LoadTestDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestPlanRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, loadTestService.list(request));
    }

    @PostMapping("/save")
    public void save(@RequestBody SaveTestPlanRequest request) {
        System.out.println(String.format("save test plan: %s", request.getName()));
    }

    @PostMapping("/delete")
    public void delete(@RequestBody DeleteTestPlanRequest request) {
        loadTestService.delete(request);
    }

    @PostMapping("/file/upload")
    public void uploadJmx(MultipartFile file) throws IOException {
        fileService.upload(file.getOriginalFilename(), file);
    }

    @PostMapping("/file/delete")
    public void deleteJmx(@RequestBody FileOperationRequest request) {
        System.out.println(String.format("delete %s", request.getName()));
    }

    @PostMapping("/file/download")
    public ResponseEntity<org.springframework.core.io.Resource> downloadJmx(@RequestBody FileOperationRequest fileOperationRequest, HttpServletResponse response) {
        org.springframework.core.io.Resource resource = fileService.loadFileAsResource(fileOperationRequest.getName());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileOperationRequest.getName() + "\"")
                .body(resource);
    }
}
