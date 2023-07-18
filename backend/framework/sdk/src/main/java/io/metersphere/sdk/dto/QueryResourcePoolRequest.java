package io.metersphere.sdk.dto;

import io.metersphere.sdk.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryResourcePoolRequest extends BasePageRequest {
    @Schema(title = "资源池名称")
    private String name;
    @Schema(title = "是否禁用")
    private Boolean enable;
}
