package io.metersphere.controller;

import io.metersphere.requests.testplan.FileOperationRequest;
import io.metersphere.service.FileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping(value = "/testplan")
public class TestPlanController {
    @Resource
    private FileService fileService;

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
