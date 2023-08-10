package io.metersphere.sdk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryResourcePoolRequest extends BasePageRequest {
    @Schema(description =  "是否禁用")
    private Boolean enable;
}
