package io.metersphere.functional.mapper;

import io.metersphere.functional.dto.FunctionalCaseRelationshipDTO;
import io.metersphere.functional.request.RelationshipRequest;
import io.metersphere.system.dto.RelationshipEdgeDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtFunctionalCaseRelationshipEdgeMapper {


    List<String> getGraphIds(@Param("ids") List<String> ids);

    List<FunctionalCaseRelationshipDTO> list(@Param("request") RelationshipRequest request, @Param("sort") String sort);

    RelationshipEdgeDTO getGraphId(@Param("id") String id);

    List<RelationshipEdgeDTO> getEdgeByGraphId(@Param("graphId") String graphId);

    void update(@Param("ids") List<String> ids, @Param("graphId") String graphId);
}
