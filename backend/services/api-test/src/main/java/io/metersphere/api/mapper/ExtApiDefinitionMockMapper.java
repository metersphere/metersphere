package io.metersphere.api.mapper;

import io.metersphere.api.dto.definition.ApiDefinitionMockDTO;
import io.metersphere.api.dto.definition.request.ApiDefinitionMockPageRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiDefinitionMockMapper {

    List<ApiDefinitionMockDTO> list(@Param("request") ApiDefinitionMockPageRequest request);

}
