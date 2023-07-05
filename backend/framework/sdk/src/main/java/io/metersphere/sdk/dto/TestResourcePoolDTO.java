package io.metersphere.sdk.dto;

import io.metersphere.system.domain.TestResourcePool;
import lombok.Data;

@Data
public class TestResourcePoolDTO extends TestResourcePool {
    private TestResourceDTO testResourceDTO;

    private Boolean inUsed;

}
