package io.metersphere.system.dto.pool;

import io.metersphere.system.domain.TestResourcePool;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class TestResourcePoolDTO extends TestResourcePool {
    private TestResourceDTO testResourceDTO;

    @Schema(description =  "资源池是否在使用中")
    private Boolean inUsed;

    @Schema(description =  "最大并发数")
    private int maxConcurrentNumber;

    @Schema(description =  "剩余并发数")
    private int lastConcurrentNumber;;

    @Schema(description =  "组织名称集合")
    private List<String> orgNames;

}
