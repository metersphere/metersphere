package io.metersphere.api.dto.definition.information;

import io.metersphere.api.dto.definition.ApiDefinitionResult;
import io.metersphere.api.dto.share.ApiDocumentInfoDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiDetailedInformationDTO {
    private ApiDefinitionResult apiDefinition;
    private ApiDocumentInfoDTO apiInfo;
}
