package io.metersphere.api.dto.converter;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExistenceApiDefinitionDetail {
    private ApiDefinitionDetail importApiDefinition;
    private ApiDefinitionDetail existenceApiDefinition;
}
