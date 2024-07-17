package io.metersphere.plan.dto;

import io.metersphere.plan.domain.TestPlanCollection;
import lombok.Data;

@Data
public class TestPlanCollectionConfigDTO extends TestPlanCollection {
    private String poolName;

    private String envName;

    private boolean noResourcePool = false;
}
