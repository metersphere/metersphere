package io.metersphere.environment.dto;

import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.base.domain.EnvironmentGroupProject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EnvironmentGroupProjectDTO extends EnvironmentGroupProject {
    private List<ApiTestEnvironmentWithBLOBs> environments;
}
