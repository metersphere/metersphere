package io.metersphere.api.mapper;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-16  19:57
 */
public interface ExtApiScenarioStepMapper {
    List<String> getStepIdsByScenarioId(String scenarioId);
}
