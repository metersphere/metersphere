package io.metersphere.performance.controller;


import io.metersphere.commons.utils.WeakConcurrentHashMap;
import io.metersphere.controller.handler.annotation.NoResultHolder;
import io.metersphere.performance.dto.ZipDTO;
import io.metersphere.performance.service.JMeterFileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("jmeter")
public class JMeterFileController {
    @Resource
    private JMeterFileService jmeterFileService;
    private final WeakConcurrentHashMap<String, List<Double>> readyMap = new WeakConcurrentHashMap<>(30 * 60 * 1000);// 默认保留30分钟

    @GetMapping("ping")
    public String checkStatus() {
        return "PONG";
    }

    @GetMapping("ready")
    @NoResultHolder
    public long ready(@RequestParam("reportId") String reportId, @RequestParam("ratio") String ratio,
                      @RequestParam("resourceIndex") String resourceIndex) {
        try {
            List<Double> ratios = readyMap.getOrDefault(reportId, Arrays.stream(ratio.split(",")).map(Double::parseDouble).collect(Collectors.toList()));
            ratios.set(Integer.parseInt(resourceIndex), -1.0);
            readyMap.put(reportId, ratios);
            return ratios.stream().filter(r -> r > 0).count();
        } catch (Exception e) {
            return 0;
        }
    }

    @GetMapping("download")
    public ResponseEntity<byte[]> downloadJmeterFiles(@RequestParam("ratio") String ratio,
                                                      @RequestParam("reportId") String reportId,
                                                      @RequestParam("resourceIndex") String resourceIndex) {
        double[] ratios = Arrays.stream(ratio.split(",")).mapToDouble(Double::parseDouble).toArray();
        ZipDTO zipDTO = jmeterFileService.downloadZip(reportId, ratios, Integer.parseInt(resourceIndex));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipDTO.getTestId() + ".zip\"")
                .body(zipDTO.getContent());
    }

}
