package io.metersphere.api.dto.mockserver;

import io.metersphere.api.domain.ApiDefinitionMockConfig;
import lombok.Data;

@Data
public class ApiMockConfigDTO extends ApiDefinitionMockConfig {
    private boolean enable;
}
