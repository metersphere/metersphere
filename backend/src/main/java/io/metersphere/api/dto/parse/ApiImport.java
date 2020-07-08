package io.metersphere.api.dto.parse;

import io.metersphere.api.dto.scenario.Scenario;
import lombok.Data;

import java.util.List;

@Data
public class ApiImport {
    private List<Scenario> scenarios;
}
