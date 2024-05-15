package io.metersphere.api.dto.scenario;

import io.metersphere.project.dto.environment.variables.CommonVariables;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-12  09:49
 */
@Data
public class ScenarioVariable {
    /**
     * 普通变量
     */
    @Valid
    private List<CommonVariables> commonVariables = new ArrayList<>(0);
    /**
     * csv变量
     */
    @Valid
    private List<CsvVariable> csvVariables = new ArrayList<>(0);
}
