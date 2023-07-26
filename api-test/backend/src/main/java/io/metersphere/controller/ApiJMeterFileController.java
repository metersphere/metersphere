package io.metersphere.controller;

import io.metersphere.api.dto.BodyFileRequest;
import io.metersphere.api.jmeter.utils.JmxFileUtil;
import io.metersphere.dto.ProjectJarConfig;
import io.metersphere.service.ApiJMeterFileService;
import io.metersphere.utils.LoggerUtil;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/jmeter")
public class ApiJMeterFileController {

    @Resource
    private ApiJMeterFileService apiJmeterFileService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("stop/{name}")
    public String stop(@PathVariable String name) {
        return name;
    }

    @PostMapping("download/jar")
    public ResponseEntity<byte[]> downloadJmeterFiles(@RequestBody Map<String, List<ProjectJarConfig>> jarConfigs) {
        byte[] bytes = apiJmeterFileService.downloadJmeterJar(jarConfigs);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + UUID.randomUUID().toString() + ".zip\"")
                .body(bytes);
    }

    @GetMapping("download")
    public ResponseEntity<byte[]> downloadJmeterFiles(@RequestParam("testId") String testId, @RequestParam("reportId") String reportId,
                                                      @RequestParam("runMode") String runMode, @RequestParam("reportType") String reportType, @RequestParam("queueId") String queueId) {
        byte[] bytes = apiJmeterFileService.downloadJmeterFiles(runMode, testId, reportId, reportType, queueId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + reportId + "_" + testId + ".zip\"")
                .body(bytes);
    }

    @PostMapping("download/plugin/jar")
    public ResponseEntity<byte[]> downloadPluginFiles(@RequestBody List<String> request) {
        byte[] bytes = apiJmeterFileService.downloadPluginJar(request);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + UUID.randomUUID().toString() + ".zip\"")
                .body(bytes);
    }


    @PostMapping("download/files")
    public ResponseEntity<byte[]> downloadJmeterLocalFiles(@RequestBody BodyFileRequest request) {
        byte[] bytes = apiJmeterFileService.zipLocalFilesToByteArray(request);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + request.getReportId() + ".zip\"")
                .body(bytes);
    }

    @GetMapping("get-script")
    public String getScript(@RequestParam("reportId") String reportId, @RequestParam("testId") String testId) {
        String key = JmxFileUtil.getExecuteScriptKey(reportId, testId);
        LoggerUtil.info("获取执行脚本", key);
        Object script = redisTemplate.opsForValue().get(key);
        redisTemplate.delete(key);
        return script != null ? script.toString() : "";
    }
}
