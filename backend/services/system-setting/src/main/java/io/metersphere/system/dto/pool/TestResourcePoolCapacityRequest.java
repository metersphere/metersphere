package io.metersphere.system.dto.pool;

import io.metersphere.system.dto.sdk.BasePageRequest;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
/**
 * @author guoyuqi
 */
@Data
public class TestResourcePoolCapacityRequest extends BasePageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "资源池id")
    @NotBlank(message = "{test_resource_pool.id.not_blank}", groups = {Updated.class})
    private String poolId;

    @Schema(description = "节点IP")
    private String ip;

    @Schema(description = "节点端口")
    private String port;
}
