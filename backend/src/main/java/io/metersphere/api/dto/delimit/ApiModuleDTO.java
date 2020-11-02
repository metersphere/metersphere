package io.metersphere.api.dto.delimit;

import io.metersphere.base.domain.ApiModule;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiModuleDTO extends ApiModule {

    private String label;
    private List<ApiModuleDTO> children;

}
