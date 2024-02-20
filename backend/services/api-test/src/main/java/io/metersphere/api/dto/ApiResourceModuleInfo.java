package io.metersphere.api.dto;

import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2024-02-17  18:46
 */
@Data
public class ApiResourceModuleInfo {
    /**
     * 模块Id
     */
    private String moduleId;
    /**
     * 资源id，接口定义，接口用例等
     */
    private String resourceId;
}
