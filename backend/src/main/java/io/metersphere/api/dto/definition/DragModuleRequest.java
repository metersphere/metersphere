package io.metersphere.api.dto.definition;

import io.metersphere.base.domain.ApiModule;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DragModuleRequest extends ApiModule {

    List<String> nodeIds;
    ApiModuleDTO nodeTree;
}
