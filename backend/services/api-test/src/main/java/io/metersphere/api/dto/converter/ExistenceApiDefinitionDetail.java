package io.metersphere.api.dto.converter;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ExistenceApiDefinitionDetail {
    private ApiDefinitionDetail importApiDefinition;
    private List<ApiDefinitionDetail> existenceApiDefinition;
}
