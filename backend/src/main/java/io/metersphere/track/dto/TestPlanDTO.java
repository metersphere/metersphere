package io.metersphere.track.dto;

import io.metersphere.base.domain.TestPlan;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestPlanDTO extends TestPlan {
    private String projectName;
}
