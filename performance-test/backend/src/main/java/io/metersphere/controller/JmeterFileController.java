package io.metersphere.controller;


import io.metersphere.controller.handler.annotation.NoResultHolder;
import io.metersphere.service.JmeterFileService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("jmeter")
public class JmeterFileController {
    @Resource
    private JmeterFileService jmeterFileService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("ping")
    public String checkStatus() {
        return "PONG";
    }

    @GetMapping("ready")
    @NoResultHolder
    public long ready(@RequestParam("reportId") String reportId, @RequestParam("ratio") String ratio,
                      @RequestParam("resourceIndex") int resourceIndex) {
        try {
            // 保存当前节点状态到redis
            String reportIdKey = "jmeter_ready:" + reportId;
            stringRedisTemplate.opsForHash().put(reportIdKey, reportId + "_" + resourceIndex, "1");
            // 设置30分钟过期
            stringRedisTemplate.expire(reportIdKey, 30 * 60, TimeUnit.SECONDS);
            // 返回当前已经准备好的节点数量
            List<Object> values = stringRedisTemplate.opsForHash().values(reportIdKey);
            return Arrays.asList(ratio.split(",")).size() - CollectionUtils.size(values);

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
