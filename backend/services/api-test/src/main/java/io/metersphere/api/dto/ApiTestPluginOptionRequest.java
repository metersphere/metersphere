package io.metersphere.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ApiTestPluginOptionRequest {
    /**
     * 组织ID
     */
    @NotBlank
    @Size(max = 50)
    private String orgId;

    /**
     * 插件ID
     */
    @NotBlank
    @Size(max = 50)
    private String pluginId;

    /**
     * 需要调用的插件方法
     */
    @NotBlank
    @Size(max = 100)
    private String optionMethod;

    /**
     * 方法所需的查询参数
     * 例如：
     * 查询的过滤条件
     * 级联的情况，需要表单的其他参数值等
     */
    private Object queryParam;
}
