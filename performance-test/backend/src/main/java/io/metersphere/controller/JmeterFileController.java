package io.metersphere.controller;


import io.metersphere.commons.utils.WeakConcurrentHashMap;
import io.metersphere.controller.handler.annotation.NoResultHolder;
import io.metersphere.service.JmeterFileService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("jmeter")
public class JmeterFileController {
    @Resource
    private JmeterFileService jmeterFileService;
    private final WeakConcurrentHashMap<String, List<Double>> readyMap = new WeakConcurrentHashMap<>(30 * 60 * 1000);// 默认保留30分钟

    @GetMapping("ping")
    public String checkStatus() {
        return "PONG";
    }

    @GetMapping("ready")
    @NoResultHolder
    public long ready(@RequestParam("reportId") String reportId, @RequestParam("ratio") String ratio,
                      @RequestParam("resourceIndex") int resourceIndex) {
        try {
            List<Double> ratios = readyMap.getOrDefault(reportId, Arrays.stream(ratio.split(",")).map(Double::parseDouble).collect(Collectors.toList()));
            ratios.set(resourceIndex, -1.0);
            readyMap.put(reportId, ratios);
            return ratios.stream().filter(r -> r > 0).count();
        } catch (Exception e) {
            return 0;
        }
    }

    @GetMapping("download")
    public void downloadJmeterFiles(@RequestParam("ratio") String ratio,
                                    @RequestParam("reportId") String reportId,
                                    @RequestParam("resourceIndex") int resourceIndex, HttpServletResponse response) {
        double[] ratios = Arrays.stream(ratio.split(",")).mapToDouble(Double::parseDouble).toArray();
        jmeterFileService.downloadZip(reportId, ratios, resourceIndex, response);
    }

}
