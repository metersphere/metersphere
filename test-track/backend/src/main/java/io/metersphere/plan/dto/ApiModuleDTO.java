package io.metersphere.plan.dto;

import io.metersphere.dto.TreeNodeDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiModuleDTO extends TreeNodeDTO<ApiModuleDTO> {
    private String protocol;

    private String path;
}
