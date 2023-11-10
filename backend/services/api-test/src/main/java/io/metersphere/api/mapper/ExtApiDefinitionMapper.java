package io.metersphere.api.mapper;

import io.metersphere.api.dto.definition.ApiCaseComputeDTO;
import io.metersphere.api.dto.definition.ApiDefinitionDTO;
import io.metersphere.api.dto.request.ApiDefinitionPageRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiDefinitionMapper {
    void deleteApiToGc(@Param("ids") List<String> ids, @Param("userId") String userId, @Param("time") long time);

    List<ApiDefinitionDTO> list(@Param("request") ApiDefinitionPageRequest request, @Param("deleted") boolean deleted);

    List<ApiCaseComputeDTO> selectApiCaseByIdsAndStatusIsNotTrash(@Param("ids") List<String> ids, @Param("projectId") String projectId);

}
