package io.metersphere.api.controller;

import io.metersphere.api.service.ApiJmeterFileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.UUID;

@RestController
@RequestMapping("/api/jmeter")
public class ApiJmeterFileController {

    @Resource
    private ApiJmeterFileService apiJmeterFileService;

    @GetMapping("download")
    public ResponseEntity<byte[]> downloadJmeterFiles(@RequestParam("testId") String testId, @RequestParam("reportId") String reportId, @RequestParam("runMode") String runMode, @RequestParam("testPlanScenarioId") String testPlanScenarioId) {
        byte[] bytes = apiJmeterFileService.downloadJmeterFiles(runMode,testId, reportId, testPlanScenarioId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + testId + ".zip\"")
                .body(bytes);
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
