package io.metersphere.api.controller;

import io.metersphere.api.dto.scenario.request.BodyFile;
import io.metersphere.api.service.ApiJmeterFileService;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/jmeter")
public class ApiJmeterFileController {

    @Resource
    private ApiJmeterFileService apiJmeterFileService;

    @PostMapping("download/files")
    public ResponseEntity<byte[]> downloadJmeterFiles(@RequestBody List<BodyFile> bodyFileList) {
        byte[] bytes = new byte[10];
        if (CollectionUtils.isNotEmpty(bodyFileList)) {
            bytes = apiJmeterFileService.downloadJmeterFiles(bodyFileList);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + UUID.randomUUID().toString() + ".zip\"")
                .body(bytes);
    }

    @GetMapping("download")
    public byte[] downloadJmx(@RequestParam("testId") String testId, @RequestParam("reportId") String reportId, @RequestParam("runMode") String runMode, @RequestParam("testPlanScenarioId") String testPlanScenarioId) {
        return apiJmeterFileService.downloadJmx(runMode, testId, reportId, testPlanScenarioId);
    }

    @GetMapping("download/jar")
    public ResponseEntity<byte[]> downloadJmeterFiles() {
        byte[] bytes = apiJmeterFileService.downloadJmeterJar();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + UUID.randomUUID().toString() + ".zip\"")
                .body(bytes);
    }
}
