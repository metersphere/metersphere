package io.metersphere.service.scenario.dto;

import io.metersphere.base.domain.ApiDefinition;
import io.metersphere.base.mapper.ApiDefinitionMapper;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.ApiTestCaseMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioMapper;
import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class ApiScenarioParamDto {
   private ApiScenarioMapper batchMapper;
   private ExtApiScenarioMapper extApiScenarioMapper;
   private ApiTestCaseMapper apiTestCaseMapper;
   private ApiDefinitionMapper apiDefinitionMapper;
   private Map<String, ApiDefinition> definitionMap;
   private Map<String, Set<String>> apiIdCaseNameMap;
}
