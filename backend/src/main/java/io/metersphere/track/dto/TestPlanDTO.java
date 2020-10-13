package io.metersphere.track.dto;

import io.metersphere.base.domain.TestPlan;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestPlanDTO extends TestPlan {
    private String projectName;
    private List<String> projectIds;
}
