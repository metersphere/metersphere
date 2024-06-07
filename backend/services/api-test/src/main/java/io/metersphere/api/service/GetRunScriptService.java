package io.metersphere.api.service;

import io.metersphere.sdk.dto.api.task.GetRunScriptRequest;
import io.metersphere.sdk.dto.api.task.GetRunScriptResult;

/**
 * @Author: jianxing
 * @CreateTime: 2024-02-06  20:47
 */
public interface GetRunScriptService {
    /**
     * 解析并返回执行脚本
     */
    GetRunScriptResult getRunScript(GetRunScriptRequest request);
}
