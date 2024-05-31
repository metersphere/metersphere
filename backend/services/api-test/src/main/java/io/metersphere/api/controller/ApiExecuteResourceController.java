package io.metersphere.api.controller;

import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.api.service.definition.ApiReportService;
import io.metersphere.api.service.scenario.ApiScenarioReportService;
import io.metersphere.sdk.constants.ExecStatus;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.LogUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-05  17:52
 */
@RestController
@RequestMapping("/api/execute/resource")
@Tag(name = "接口测试-执行-资源")
public class ApiExecuteResourceController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ApiExecuteService apiExecuteService;
    @Resource
    private ApiReportService apiReportService;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;

    /**
     * 获取执行脚本
     *
     * @param reportId
     * @param testId
     * @return
     */
    @GetMapping("script")
    @Operation(summary = "获取执行脚本")
    public String getScript(@RequestParam("reportId") String reportId, @RequestParam("testId") String testId) {
        String key = apiExecuteService.getScriptRedisKey(reportId, testId);
        LogUtils.info("获取执行脚本: {}", key);
        String script = stringRedisTemplate.opsForValue().get(key);
        stringRedisTemplate.delete(key);
        apiReportService.updateReportStatus(reportId, ExecStatus.RUNNING.name());
        apiScenarioReportService.updateReportStatus(reportId, ExecStatus.RUNNING.name());
        return Optional.ofNullable(script).orElse(StringUtils.EMPTY);
    }

    /**
     * 下载执行所需的文件
     *
     * @return
     */
    @PostMapping("/file")
    @Operation(summary = "下载执行所需的文件")
    public void downloadFile(@RequestParam("reportId") String reportId,
                             @RequestParam("testId") String testId,
                             @RequestBody FileRequest fileRequest,
                             HttpServletResponse response) throws Exception {
        apiExecuteService.downloadFile(reportId, testId, fileRequest, response);
    }

}
