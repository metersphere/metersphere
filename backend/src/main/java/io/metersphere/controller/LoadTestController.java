package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.FileMetadata;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.testplan.*;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.service.FileService;
import io.metersphere.service.LoadTestService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
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

    @PostMapping(value = "/save", consumes = {"multipart/form-data"})
    public void save(
            @RequestPart("request") SaveTestPlanRequest request,
            @RequestPart(value = "file") MultipartFile file
    ) {
        loadTestService.save(request, file);
    }

    @PostMapping(value = "/edit", consumes = {"multipart/form-data"})
    public void edit(
            @RequestPart("request") EditTestPlanRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        loadTestService.edit(request, file);
    }

    @PostMapping("/delete")
    public void delete(@RequestBody DeleteTestPlanRequest request) {
        loadTestService.delete(request);
    }

    @GetMapping("/file/metadata/{testId}")
    public FileMetadata getFileMetadata(@PathVariable String testId) {
        return fileService.getFileMetadataByTestId(testId);
    }

    @PostMapping("/file/download")
    public ResponseEntity<org.springframework.core.io.Resource> downloadJmx(@RequestBody FileOperationRequest fileOperationRequest) {
        org.springframework.core.io.Resource resource = fileService.loadFileAsResource(fileOperationRequest.getName());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileOperationRequest.getName() + "\"")
                .body(resource);
    }
}
