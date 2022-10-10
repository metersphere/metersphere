package io.metersphere.environment.dto;

import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import lombok.Data;

import java.util.List;

@Data
public class TestEnvironmentDTO extends ApiTestEnvironmentWithBLOBs {
    private List<String> uploadIds;
    private List<String> variablesFilesIds;
}
