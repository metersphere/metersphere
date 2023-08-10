package io.metersphere.system.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class AuthSourceStatusRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description =  "是否禁用")
    private Boolean enable;

    @Schema(description =  "id")
    private String id;
}
