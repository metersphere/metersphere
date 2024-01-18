package io.metersphere.project.dto.environment;

import lombok.Data;

@Data
public class KeyValueEnableParam extends KeyValueParam {
    /**
     * 是否启用
     */
    private Boolean enable = true;
    /**
     * 描述
     */
    private String description;
    /**
     * 是否必填
     */
    private Boolean required = false;
}
