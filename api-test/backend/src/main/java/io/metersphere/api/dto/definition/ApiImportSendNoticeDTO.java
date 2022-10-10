package io.metersphere.api.dto.definition;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiImportSendNoticeDTO {
    private ApiDefinitionResult apiDefinitionResult;
    private List<ApiTestCaseDTO> caseDTOList;
}
