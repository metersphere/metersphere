package io.metersphere.controller;

import io.metersphere.service.FileService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;

@RestController
@RequestMapping(value = "/testplan")
public class TestPlanController {
    @Resource
    private FileService fileService;

    @PostMapping("/file/upload")
    public void upload(MultipartFile file) throws IOException {
        fileService.upload(file.getOriginalFilename(), file);
    }
}
