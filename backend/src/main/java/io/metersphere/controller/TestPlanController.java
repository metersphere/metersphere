package io.metersphere.controller;

import com.github.pagehelper.Page;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.testplan.DeleteTestPlanRequest;
import io.metersphere.controller.request.testplan.FileOperationRequest;
import io.metersphere.controller.request.testplan.QueryTestPlanRequest;
import io.metersphere.controller.request.testplan.SaveTestPlanRequest;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.service.FileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/testplan")
public class TestPlanController {
    private static List<LoadTestDTO> loadTests = new ArrayList<>();

    static {
        // 模拟数据
        for (int i = 0; i < 100; i++) {
            final LoadTestDTO loadTest = new LoadTestDTO();
            loadTest.setId(String.valueOf(i));
            loadTest.setName("load test " + i);
            loadTest.setDescription("no description");
            loadTest.setScenarioDefinition("no scenario description");
            loadTest.setCreateTime(System.currentTimeMillis());
            loadTest.setUpdateTime(System.currentTimeMillis());
            loadTest.setProjectId(String.valueOf(i));
            loadTest.setProjectName("project " + i);
            loadTests.add(loadTest);
        }
    }

    @Resource
    private FileService fileService;

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<LoadTestDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryTestPlanRequest request) {
        final Page page = new Page((int) Math.ceil(loadTests.size() * 1.0 / pageSize), pageSize);
        page.setTotal(loadTests.size());
        return PageUtils.setPageInfo(
                page,
                loadTests.stream().skip((goPage - 1) * pageSize).limit(pageSize).collect(Collectors.toList())
        );
    }

    @PostMapping("/save")
    public void save(@RequestBody SaveTestPlanRequest request) {
        System.out.println(String.format("save test plan: %s", request.getName()));
    }

    @PostMapping("/delete")
    public void delete(@RequestBody DeleteTestPlanRequest request) {
        System.out.println(String.format("delete test plan: %s", request.getName()));
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
