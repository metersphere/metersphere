package io.metersphere.api.controller;

import io.metersphere.api.service.ApiExecuteResourceService;
import io.metersphere.api.service.ApiExecuteService;
import io.metersphere.sdk.dto.api.task.GetRunScriptRequest;
import io.metersphere.sdk.dto.api.task.GetRunScriptResult;
import io.metersphere.sdk.file.FileRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-05  17:52
 */
@RestController
@RequestMapping("/api/execute/resource")
@Tag(name = "接口测试-执行-资源")
public class ApiExecuteResourceController {
    @Resource
    private ApiExecuteService apiExecuteService;
    @Resource
    private ApiExecuteResourceService apiExecuteResourceService;

    /**
     * 获取执行脚本
     *
     * @return
     */
    @PostMapping("script")
    @Operation(summary = "获取执行脚本")
    public GetRunScriptResult getScript(@RequestBody GetRunScriptRequest request) {
        return apiExecuteResourceService.getRunScript(request);
    }

    /**
     * 下载执行所需的文件
     *
     * @return
     */
    @PostMapping("/file")
    @Operation(summary = "下载执行所需的文件")
    public void downloadFile(@RequestParam("taskItemId") String taskItemId,
                             @RequestBody FileRequest fileRequest,
                             HttpServletResponse response) throws Exception {
        apiExecuteService.downloadFile(taskItemId, fileRequest, response);
    }

}
