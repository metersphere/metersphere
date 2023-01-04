package io.metersphere.api.dto.definition;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BatchDataCopyRequest {
    private List<String> ids;
    private boolean copyCase;
    private boolean copyMock;
    private String versionId;

    private ApiDefinitionRequest condition;
}
