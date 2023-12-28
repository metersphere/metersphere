package io.metersphere.functional.mapper;

import io.metersphere.functional.dto.FunctionalCaseRelationshipDTO;
import io.metersphere.functional.request.RelationshipRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtFunctionalCaseRelationshipEdgeMapper {


    List<String> getGraphIds(@Param("ids") List<String> ids);

    List<FunctionalCaseRelationshipDTO> list(@Param("request") RelationshipRequest request, @Param("sort") String sort);
}
