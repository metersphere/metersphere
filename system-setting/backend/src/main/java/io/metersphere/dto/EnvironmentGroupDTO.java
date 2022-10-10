package io.metersphere.dto;

import io.metersphere.base.domain.EnvironmentGroup;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EnvironmentGroupDTO extends EnvironmentGroup {
    private Boolean disabled = false;
}
