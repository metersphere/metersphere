package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.automation.ApiScenarioDTO;
import io.metersphere.api.dto.automation.ApiScenarioRequest;
import io.metersphere.base.domain.ApiScenario;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiScenarioMapper {
    List<ApiScenarioDTO> list(@Param("request") ApiScenarioRequest request);

    List<ApiScenario> selectByTagId(@Param("id") String id);

    List<ApiScenario> selectIds(@Param("ids") List<String> ids);

    List<ApiScenario> selectReference(@Param("request") ApiScenarioRequest request);

    int removeToGc(@Param("ids") List<String> ids);

    int reduction(@Param("ids") List<String> ids);
}
