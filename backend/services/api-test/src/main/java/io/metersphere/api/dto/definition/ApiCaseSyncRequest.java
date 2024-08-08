package io.metersphere.api.dto.definition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class ApiCaseSyncRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "同步项")
    private ApiCaseSyncItemRequest syncItems = new ApiCaseSyncItemRequest();
    @Schema(description = "是否删除多余参数", defaultValue = "false")
    private Boolean deleteRedundantParam = false;
    @Schema(description = "用例的请求详情")
    @NotNull
    private Object apiCaseRequest;
    @Schema(description = "用例ID")
    @NotBlank
    private String id;
}
