package io.metersphere.api.dto.scenario.environment;

import io.metersphere.api.dto.definition.request.auth.MsAuthManager;
import lombok.Data;

@Data
public class EnvAuthManager {
    private MsAuthManager authManager;
}
