package io.metersphere.project.dto.environment.processors;


import com.fasterxml.jackson.annotation.JsonTypeName;
import io.metersphere.project.api.processor.SQLProcessor;
import lombok.Data;

@Data
@JsonTypeName("ENV_SCENARIO_SQL")
public class EnvScenarioSqlProcessor extends SQLProcessor {
}

