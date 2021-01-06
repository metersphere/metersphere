package io.metersphere.api.dto;

import io.metersphere.api.dto.scenario.DatabaseConfig;
import lombok.Data;

@Data
public class EnvironmentDTO {
    private String environmentId;
    private DatabaseConfig databaseConfig;
}
