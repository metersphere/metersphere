package io.metersphere.sdk.dto;

import io.metersphere.system.domain.TestResourcePool;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TestResourcePoolDTO extends TestResourcePool {
    private TestResourceDTO testResourceDTO;

    @Schema(title = "资源池是否在使用中")
    private Boolean inUsed;

}
