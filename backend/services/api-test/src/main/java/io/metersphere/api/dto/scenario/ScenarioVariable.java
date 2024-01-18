package io.metersphere.api.dto.scenario;

import io.metersphere.project.dto.environment.variables.CommonVariables;
import lombok.Data;

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
    private List<CommonVariables> commonVariables;
    /**
     * csv变量
     */
    private List<CsvVariable> csvVariables;
}
