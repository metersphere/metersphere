package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.RelationshipEdge;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface BaseRelationshipEdgeMapper {

    List<String> getGraphIdsByNodeIds(@Param("ids") List<String> ids);

    int insertBatch(List<RelationshipEdge> subList);

    int batchUpdateGraphId(Map<String, Object> params);

}
