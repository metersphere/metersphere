package io.metersphere.performance.controller;


import io.metersphere.performance.service.JmeterFileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;

@RestController
@RequestMapping("jmeter")
public class JmeterFileController {
    @Resource
    private JmeterFileService jmeterFileService;

    @GetMapping("ping")
    public String checkStatus() {
        return "PONG";
    }

    @GetMapping("download")
    public ResponseEntity<byte[]> downloadJmeterFiles(@RequestParam("testId") String testId, @RequestParam("resourceId") String resourceId,
                                                      @RequestParam("ratio") String ratio,
                                                      @RequestParam("reportId") String reportId, @RequestParam("resourceIndex") int resourceIndex) {
        long startTime = System.currentTimeMillis();
        double[] ratios = Arrays.stream(ratio.split(",")).mapToDouble(Double::parseDouble).toArray();
        byte[] bytes = jmeterFileService.downloadZip(testId, ratios, startTime, reportId, resourceIndex);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + testId + ".zip\"")
                .body(bytes);
    }

}
