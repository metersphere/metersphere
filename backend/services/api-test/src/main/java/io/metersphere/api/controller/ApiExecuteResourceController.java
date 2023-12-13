package io.metersphere.api.controller;

import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.sdk.util.LogUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-05  17:52
 */
@RestController
@RequestMapping("/api/execute/resource")
public class ApiExecuteResourceController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ApiExecuteService apiExecuteService;

    /**
     * 获取执行脚本
     * @param reportId
     * @param testId
     * @return
     */
    @GetMapping("script")
    public String getScript(@RequestParam("reportId") String reportId, @RequestParam("testId") String testId) {
        String key = apiExecuteService.getScriptRedisKey(reportId, testId);
        LogUtils.info("获取执行脚本: ", key);
        String script = stringRedisTemplate.opsForValue().get(key);
        stringRedisTemplate.delete(key);
        return Optional.ofNullable(script).orElse(StringUtils.EMPTY);
    }
}
