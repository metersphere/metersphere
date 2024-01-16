package io.metersphere.api.mapper;

import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.dto.scenario.ApiScenarioBatchEditRequest;
import io.metersphere.api.dto.scenario.ApiScenarioDTO;
import io.metersphere.api.dto.scenario.ApiScenarioPageRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiScenarioMapper {
    List<ApiScenarioDTO> list(@Param("request") ApiScenarioPageRequest request);

    List<String> getIds(@Param("request") ApiScenarioBatchEditRequest request, @Param("deleted") boolean deleted);

    List<ApiScenario> getInfoByIds(@Param("ids") List<String> ids, @Param("deleted") boolean deleted);

    List<ApiScenario> getTagsByIds(@Param("ids") List<String> ids, @Param("deleted") boolean deleted);

}
