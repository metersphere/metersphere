package io.metersphere.api.dto;

import lombok.Data;

/**
 * 批量执行时，用例的基本信息
 */
@Data
public class ApiResourceBatchRunInfo {
    /**
     * 资源id
     */
    private String id;
    /**
     * 资源名称
     */
    private String name;
}
