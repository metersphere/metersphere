package io.metersphere.system.dto.pool;

import io.metersphere.system.domain.TestResourcePool;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TestResourcePoolReturnDTO  extends TestResourcePool {
    private TestResourceReturnDTO testResourceReturnDTO;

    @Schema(description =  "资源池类型, Node/Kubernetes")
    private String type;

    @Schema(description =  "资源池是否在使用中")
    private Boolean inUsed;
}
