package io.metersphere.api.dto.definition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: jianxing
 * @CreateTime: 2024-08-08  11:06
 */
@Data
public class ApiCaseSyncItemRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "请求头", defaultValue = "true")
    private Boolean header = true;
    @Schema(description = "请求体", defaultValue = "true")
    private Boolean body = true;
    @Schema(description = "Query参数", defaultValue = "true")
    private Boolean query = true;
    @Schema(description = "Rest参数", defaultValue = "true")
    private Boolean rest = true;
}
