package io.metersphere.api.dto.definition;

import io.metersphere.track.dto.TreeNodeDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiModuleDTO extends TreeNodeDTO<ApiModuleDTO> {
    private String protocol;
}
